/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2009.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.universAAL.context.sesame.sail;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import info.aduna.concurrent.locks.Lock;
import info.aduna.iteration.CloseableIteration;
import info.aduna.iteration.CloseableIteratorIteration;
import info.aduna.iteration.ExceptionConvertingIteration;
import info.aduna.iteration.Iterations;

import org.openrdf.OpenRDFUtil;
import org.openrdf.model.Namespace;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.NamespaceImpl;
import org.openrdf.query.BindingSet;
import org.openrdf.query.Dataset;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.algebra.QueryRoot;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.Var;
import org.openrdf.query.algebra.evaluation.impl.BindingAssigner;
import org.openrdf.query.algebra.evaluation.impl.CompareOptimizer;
import org.openrdf.query.algebra.evaluation.impl.ConjunctiveConstraintSplitter;
import org.openrdf.query.algebra.evaluation.impl.ConstantOptimizer;
import org.openrdf.query.algebra.evaluation.impl.DisjunctiveConstraintOptimizer;
import org.openrdf.query.algebra.evaluation.impl.EvaluationStrategyImpl;
import org.openrdf.query.algebra.evaluation.impl.FilterOptimizer;
import org.openrdf.query.algebra.evaluation.impl.IterativeEvaluationOptimizer;
import org.openrdf.query.algebra.evaluation.impl.OrderLimitOptimizer;
import org.openrdf.query.algebra.evaluation.impl.QueryJoinOptimizer;
import org.openrdf.query.algebra.evaluation.impl.QueryModelNormalizer;
import org.openrdf.query.algebra.evaluation.impl.SameTermFilterOptimizer;
import org.openrdf.query.algebra.helpers.QueryModelVisitorBase;
import org.openrdf.query.impl.EmptyBindingSet;
import org.openrdf.sail.SailException;
import org.openrdf.sail.helpers.DefaultSailChangedEvent;
import org.openrdf.sail.helpers.NotifyingSailConnectionBase;
import org.openrdf.sail.inferencer.InferencerConnection;
import org.openrdf.sail.nativerdf.btree.RecordIterator;
import org.universAAL.context.sesame.sail.model.NativeValue;

/**
 * @author Arjohn Kampman
 */
public class CardinalityNativeStoreConnection extends NotifyingSailConnectionBase implements InferencerConnection {

	/*-----------*
	 * Constants *
	 *-----------*/

	protected final CardinalityNativeStore nativeStore;

	/*-----------*
	 * Variables *
	 *-----------*/

	protected volatile DefaultSailChangedEvent sailChangedEvent;

	/**
	 * The exclusive transaction lock held by this connection during
	 * transactions.
	 */
	private volatile Lock txnLock;

	private URI onProp;
	private URI maxCard;
	private URI nonNativeInt;
	private Value one;

	/*--------------*
	 * Constructors *
	 *--------------*/

	protected CardinalityNativeStoreConnection(CardinalityNativeStore nativeStore)
		throws IOException
	{
		super(nativeStore);
		this.nativeStore = nativeStore;
		//Preload for performance of cardinality
		onProp = nativeStore.getValueFactory().createURI(
			"http://www.w3.org/2002/07/owl#onProperty");
		maxCard = nativeStore.getValueFactory().createURI(
			"http://www.w3.org/2002/07/owl#maxCardinality");
		nonNativeInt= nativeStore.getValueFactory().createURI(
			"http://www.w3.org/2001/XMLSchema#nonNegativeInteger");
		one = nativeStore.getValueFactory().createLiteral("1",
			nonNativeInt);
		
		sailChangedEvent = new DefaultSailChangedEvent(nativeStore);
	}

	/*---------*
	 * Methods *
	 *---------*/

	@Override
	protected void closeInternal() {
		// FIXME we should check for open iteration objects.
	}

	@Override
	protected CloseableIteration<? extends BindingSet, QueryEvaluationException> evaluateInternal(
			TupleExpr tupleExpr, Dataset dataset, BindingSet bindings, boolean includeInferred)
		throws SailException
	{
		logger.trace("Incoming query model:\n{}", tupleExpr);

		// Clone the tuple expression to allow for more aggressive optimizations
		tupleExpr = tupleExpr.clone();

		if (!(tupleExpr instanceof QueryRoot)) {
			// Add a dummy root node to the tuple expressions to allow the
			// optimizers to modify the actual root node
			tupleExpr = new QueryRoot(tupleExpr);
		}

		try {
			replaceValues(tupleExpr);

			NativeTripleSource tripleSource = new NativeTripleSource(nativeStore, includeInferred,
					transactionActive());
			EvaluationStrategyImpl strategy = new EvaluationStrategyImpl(tripleSource, dataset);

			new BindingAssigner().optimize(tupleExpr, dataset, bindings);
			new ConstantOptimizer(strategy).optimize(tupleExpr, dataset, bindings);
			new CompareOptimizer().optimize(tupleExpr, dataset, bindings);
			new ConjunctiveConstraintSplitter().optimize(tupleExpr, dataset, bindings);
			new DisjunctiveConstraintOptimizer().optimize(tupleExpr, dataset, bindings);
			new SameTermFilterOptimizer().optimize(tupleExpr, dataset, bindings);
			new QueryModelNormalizer().optimize(tupleExpr, dataset, bindings);
			new QueryJoinOptimizer(new NativeEvaluationStatistics(nativeStore)).optimize(tupleExpr, dataset,
					bindings);
			new IterativeEvaluationOptimizer().optimize(tupleExpr, dataset, bindings);
			new FilterOptimizer().optimize(tupleExpr, dataset, bindings);
			new OrderLimitOptimizer().optimize(tupleExpr, dataset, bindings);

			logger.trace("Optimized query model:\n{}", tupleExpr);

			return strategy.evaluate(tupleExpr, EmptyBindingSet.getInstance());
		}
		catch (QueryEvaluationException e) {
			throw new SailException(e);
		}
	}

	protected void replaceValues(TupleExpr tupleExpr)
		throws SailException
	{
		// Replace all Value objects stored in variables with NativeValue objects,
		// which cache internal IDs
		tupleExpr.visit(new QueryModelVisitorBase<SailException>() {

			@Override
			public void meet(Var var) {
				if (var.hasValue()) {
					var.setValue(nativeStore.getValueStore().getNativeValue(var.getValue()));
				}
			}
		});
	}

	@Override
	protected CloseableIteration<? extends Resource, SailException> getContextIDsInternal()
		throws SailException
	{
		// Which resources are used as context identifiers is not stored
		// separately. Iterate over all statements and extract their context.
		try {
			CloseableIteration<? extends Resource, IOException> contextIter = nativeStore.getContextIDs(transactionActive());

			return new ExceptionConvertingIteration<Resource, SailException>(contextIter) {

				@Override
				protected SailException convert(Exception e) {
					if (e instanceof IOException) {
						return new SailException(e);
					}
					else if (e instanceof RuntimeException) {
						throw (RuntimeException)e;
					}
					else if (e == null) {
						throw new IllegalArgumentException("e must not be null");
					}
					else {
						throw new IllegalArgumentException("Unexpected exception type: " + e.getClass());
					}
				}
			};
		}
		catch (IOException e) {
			throw new SailException(e);
		}
	}

	@Override
	protected CloseableIteration<? extends Statement, SailException> getStatementsInternal(Resource subj,
			URI pred, Value obj, boolean includeInferred, Resource... contexts)
		throws SailException
	{
		try {
			CloseableIteration<? extends Statement, IOException> iter = nativeStore.createStatementIterator(
					subj, pred, obj, includeInferred, transactionActive(), contexts);

			return new ExceptionConvertingIteration<Statement, SailException>(iter) {

				@Override
				protected SailException convert(Exception e) {
					if (e instanceof IOException) {
						return new SailException(e);
					}
					else if (e instanceof RuntimeException) {
						throw (RuntimeException)e;
					}
					else if (e == null) {
						throw new IllegalArgumentException("e must not be null");
					}
					else {
						throw new IllegalArgumentException("Unexpected exception type: " + e.getClass());
					}
				}
			};
		}
		catch (IOException e) {
			throw new SailException("Unable to get statements", e);
		}
	}

	@Override
	protected long sizeInternal(Resource... contexts)
		throws SailException
	{
		OpenRDFUtil.verifyContextNotNull(contexts);

		try {
			List<Integer> contextIDs;
			if (contexts.length == 0) {
				contextIDs = Arrays.asList(NativeValue.UNKNOWN_ID);
			}
			else {
				contextIDs = nativeStore.getContextIDs(contexts);
			}

			long size = 0L;

			for (int contextID : contextIDs) {
				// Iterate over all explicit statements
				RecordIterator iter = nativeStore.getTripleStore().getTriples(-1, -1, -1, contextID, true,
						transactionActive());
				try {
					while (iter.next() != null) {
						size++;
					}
				}
				finally {
					iter.close();
				}
			}

			return size;
		}
		catch (IOException e) {
			throw new SailException(e);
		}
	}
	
	public long size(Resource subj, URI pred, Value obj, boolean includeInferred, Resource... contexts)
		throws SailException
	{
	    OpenRDFUtil.verifyContextNotNull(contexts);
		try {
			ValueStore valueStore = nativeStore.getValueStore();

			int subjID = NativeValue.UNKNOWN_ID;
			if (subj != null) {
				subjID = valueStore.getID(subj);
				if (subjID == NativeValue.UNKNOWN_ID) {
					return 0;
				}
			}
			int predID = NativeValue.UNKNOWN_ID;
			if (pred != null) {
				predID = valueStore.getID(pred);
				if (predID == NativeValue.UNKNOWN_ID) {
					return 0;
				}
			}
			int objID = NativeValue.UNKNOWN_ID;
			if (obj != null) {
				objID = valueStore.getID(obj);
				if (objID == NativeValue.UNKNOWN_ID) {
					return 0;
				}
			}
			List<Integer> contextIDs;
			if (contexts != null && contexts.length == 0) {
				contextIDs = Arrays.asList(NativeValue.UNKNOWN_ID);
			}
			else {
				contextIDs = nativeStore.getContextIDs(contexts);
			}

			long size = 0L;

			for (int contextID : contextIDs) {
				// Iterate over all explicit statements
				RecordIterator iter = nativeStore.getTripleStore().getTriples(subjID, predID, objID, contextID,
						!includeInferred, transactionActive());

				try {
					while (iter.next() != null) {
						size++;
					}
				}
				finally {
					iter.close();
				}
			}

			return size;
		}
		catch (IOException e) {
			throw new SailException(e);
		}
	}

	@Override
	protected CloseableIteration<? extends Namespace, SailException> getNamespacesInternal()
		throws SailException
	{
		return new CloseableIteratorIteration<NamespaceImpl, SailException>(
				nativeStore.getNamespaceStore().iterator());
	}

	@Override
	protected String getNamespaceInternal(String prefix)
		throws SailException
	{
		return nativeStore.getNamespaceStore().getNamespace(prefix);
	}

	@Override
	protected void startTransactionInternal()
		throws SailException
	{
		txnLock = nativeStore.getTransactionLock();

		try {
			nativeStore.getTripleStore().startTransaction();
		}
		catch (IOException e) {
			txnLock.release();
			throw new SailException(e);
		}
		catch (RuntimeException e) {
			txnLock.release();
			throw e;
		}
	}

	@Override
	protected void commitInternal()
		throws SailException
	{
		try {
			nativeStore.getValueStore().sync();
			nativeStore.getNamespaceStore().sync();
			nativeStore.getTripleStore().commit();

			txnLock.release();
		}
		catch (IOException e) {
			throw new SailException(e);
		}
		catch (RuntimeException e) {
			logger.error("Encountered an unexpected problem while trying to commit", e);
			throw e;
		}

		nativeStore.notifySailChanged(sailChangedEvent);

		// create a fresh event object.
		sailChangedEvent = new DefaultSailChangedEvent(nativeStore);
	}

	@Override
	protected void rollbackInternal()
		throws SailException
	{
		try {
			nativeStore.getValueStore().sync();
			nativeStore.getTripleStore().rollback();
		}
		catch (IOException e) {
			throw new SailException(e);
		}
		catch (RuntimeException e) {
			logger.error("Encountered an unexpected problem while trying to roll back", e);
			throw e;
		}
		finally {
			txnLock.release();
		}
	}

	@Override
	protected void addStatementInternal(Resource subj, URI pred, Value obj, Resource... contexts)
		throws SailException
	{
		addStatement(subj, pred, obj, true, contexts);
	}

	public boolean addInferredStatement(Resource subj, URI pred, Value obj, Resource... contexts)
		throws SailException
	{
		connectionLock.readLock().lock();
		try {
			verifyIsOpen();

			updateLock.lock();
			try {
				autoStartTransaction();
				return addStatement(subj, pred, obj, false, contexts);
			}
			finally {
				updateLock.unlock();
			}
		}
		finally {
			connectionLock.readLock().unlock();
		}
	}

	protected boolean addStatement(Resource subj, URI pred, Value obj, boolean explicit, Resource... contexts)
		throws SailException
	{
		OpenRDFUtil.verifyContextNotNull(contexts);

		boolean result = false;

		try {
			ValueStore valueStore = nativeStore.getValueStore();
			int subjID = valueStore.storeValue(subj);
			int predID = valueStore.storeValue(pred);
			int objID = valueStore.storeValue(obj);

			if (contexts.length == 0) {
				contexts = new Resource[] { null };
			}

			for (Resource context : contexts) {
				int contextID = 0;
				if (context != null) {
					contextID = valueStore.storeValue(context);
				}

				//START PATCH CARDINALITY
				if(hasMaxCardinality1(pred, contexts)){//Check if property has maxCard=1
				    removeStatements(subj, pred, null, true, contexts);//remove existing values so there is only 1
				}
				//END PATCH CARDINALITY
				boolean wasNew = nativeStore.getTripleStore().storeTriple(subjID, predID, objID, contextID,
						explicit);
				result |= wasNew;

				if (wasNew) {
					// The triple was not yet present in the triple store
					sailChangedEvent.setStatementsAdded(true);

					if (hasConnectionListeners()) {
						Statement st;

						if (context != null) {
							st = valueStore.createStatement(subj, pred, obj, context);
						}
						else {
							st = valueStore.createStatement(subj, pred, obj);
						}

						notifyStatementAdded(st);
					}
				}
			}
		}
		catch (IOException e) {
			throw new SailException(e);
		}
		catch (RuntimeException e) {
			logger.error("Encountered an unexpected problem while trying to add a statement", e);
			throw e;
		}

		return result;
	}

    protected boolean hasMaxCardinality1(URI pred, Resource[] contexts)
	    throws SailException {
//	URI onProp = nativeStore.getValueFactory().createURI(
//		"http://www.w3.org/2002/07/owl#onProperty");
	// Get all restrictions on Property "pred"
	CloseableIteration<Statement, SailException> subs = (CloseableIteration<Statement, SailException>) getStatementsInternal(
		null, onProp, pred, true, contexts);
	if (!subs.hasNext()) {
	    return false;// Property has no OWL restrictions
	} else {
	    try {
//		URI maxCard = nativeStore.getValueFactory().createURI(
//			"http://www.w3.org/2002/07/owl#maxCardinality");
//		URI nonNativeInt= nativeStore.getValueFactory().createURI(
//			"http://www.w3.org/2001/XMLSchema#nonNegativeInteger");
//		Value one = nativeStore.getValueFactory().createLiteral("1",
//			nonNativeInt);
		while (subs.hasNext()) {
		    Statement st = subs.next();
		    // For each subject (a Restriction on "pred") check if it�s
		    // maxCardinality and ==1
		    if (size(st.getSubject(), maxCard, one, true, contexts) > 0)
			return true;
		}
	    } finally {
		subs.close();
	    }
	}
	return false;
    }
	

	@Override
	protected void removeStatementsInternal(Resource subj, URI pred, Value obj, Resource... contexts)
		throws SailException
	{
		removeStatements(subj, pred, obj, true, contexts);
	}

	public boolean removeInferredStatement(Resource subj, URI pred, Value obj, Resource... contexts)
		throws SailException
	{
		connectionLock.readLock().lock();
		try {
			verifyIsOpen();

			updateLock.lock();
			try {
				autoStartTransaction();
				int removeCount = removeStatements(subj, pred, obj, false, contexts);
				return removeCount > 0;
			}
			finally {
				updateLock.unlock();
			}
		}
		finally {
			connectionLock.readLock().unlock();
		}
	}

	protected int removeStatements(Resource subj, URI pred, Value obj, boolean explicit, Resource... contexts)
		throws SailException
	{
		OpenRDFUtil.verifyContextNotNull(contexts);

		try {
			TripleStore tripleStore = nativeStore.getTripleStore();
			ValueStore valueStore = nativeStore.getValueStore();

			int subjID = NativeValue.UNKNOWN_ID;
			if (subj != null) {
				subjID = valueStore.getID(subj);
				if (subjID == NativeValue.UNKNOWN_ID) {
					return 0;
				}
			}
			int predID = NativeValue.UNKNOWN_ID;
			if (pred != null) {
				predID = valueStore.getID(pred);
				if (predID == NativeValue.UNKNOWN_ID) {
					return 0;
				}
			}
			int objID = NativeValue.UNKNOWN_ID;
			if (obj != null) {
				objID = valueStore.getID(obj);
				if (objID == NativeValue.UNKNOWN_ID) {
					return 0;
				}
			}

			List<Integer> contextIDList = new ArrayList<Integer>(contexts.length);
			if (contexts.length == 0) {
				contextIDList.add(NativeValue.UNKNOWN_ID);
			}
			else {
				for (Resource context : contexts) {
					if (context == null) {
						contextIDList.add(0);
					}
					else {
						int contextID = valueStore.getID(context);
						if (contextID != NativeValue.UNKNOWN_ID) {
							contextIDList.add(contextID);
						}
					}
				}
			}

			int removeCount = 0;

			for (int i = 0; i < contextIDList.size(); i++) {
				int contextID = contextIDList.get(i);

				List<Statement> removedStatements = Collections.emptyList();

				if (hasConnectionListeners()) {
					// We need to iterate over all matching triples so that they can
					// be reported
					RecordIterator btreeIter = tripleStore.getTriples(subjID, predID, objID, contextID, explicit,
							true);

					NativeStatementIterator iter = new NativeStatementIterator(btreeIter, valueStore);

					removedStatements = Iterations.asList(iter);
				}

				removeCount += tripleStore.removeTriples(subjID, predID, objID, contextID, explicit);

				for (Statement st : removedStatements) {
					notifyStatementRemoved(st);
				}
			}

			if (removeCount > 0) {
				sailChangedEvent.setStatementsRemoved(true);
			}

			return removeCount;
		}
		catch (IOException e) {
			throw new SailException(e);
		}
		catch (RuntimeException e) {
			logger.error("Encountered an unexpected problem while trying to remove statements", e);
			throw e;
		}
	}

	@Override
	protected void clearInternal(Resource... contexts)
		throws SailException
	{
		removeStatements(null, null, null, true, contexts);
	}

	public void clearInferred(Resource... contexts)
		throws SailException
	{
		connectionLock.readLock().lock();
		try {
			verifyIsOpen();

			updateLock.lock();
			try {
				autoStartTransaction();
				removeStatements(null, null, null, false, contexts);
			}
			finally {
				updateLock.unlock();
			}
		}
		finally {
			connectionLock.readLock().unlock();
		}
	}

	public void flushUpdates() {
		// no-op; changes are reported as soon as they come in
	}

	@Override
	protected void setNamespaceInternal(String prefix, String name)
		throws SailException
	{
		nativeStore.getNamespaceStore().setNamespace(prefix, name);
	}

	@Override
	protected void removeNamespaceInternal(String prefix)
		throws SailException
	{
		nativeStore.getNamespaceStore().removeNamespace(prefix);
	}

	@Override
	protected void clearNamespacesInternal()
		throws SailException
	{
		nativeStore.getNamespaceStore().clear();
	}
}