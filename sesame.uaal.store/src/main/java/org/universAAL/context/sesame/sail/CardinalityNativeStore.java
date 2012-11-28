/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2009.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.universAAL.context.sesame.sail;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;

import info.aduna.concurrent.locks.ExclusiveLockManager;
import info.aduna.concurrent.locks.Lock;
import info.aduna.iteration.CloseableIteration;
import info.aduna.iteration.ConvertingIteration;
import info.aduna.iteration.DistinctIteration;
import info.aduna.iteration.EmptyIteration;
import info.aduna.iteration.FilterIteration;
import info.aduna.iteration.ReducedIteration;
import info.aduna.iteration.UnionIteration;

import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.sail.NotifyingSailConnection;
import org.openrdf.sail.SailException;
import org.openrdf.sail.helpers.DirectoryLockManager;
import org.openrdf.sail.helpers.NotifyingSailBase;
import org.openrdf.sail.nativerdf.btree.RecordIterator;
import org.universAAL.context.sesame.sail.model.NativeValue;

/**
 * A SAIL implementation using B-Tree indexing on disk for storing and querying
 * its data.
 * 
 * @author Arjohn Kampman
 * @author jeen
 */
public class CardinalityNativeStore extends NotifyingSailBase {

	/*-----------*
	 * Variables *
	 *-----------*/

	/**
	 * Specifies which triple indexes this native store must use.
	 */
	private volatile String tripleIndexes;

	/**
	 * Flag indicating whether updates should be synced to disk forcefully. This
	 * may have a severe impact on write performance. By default, this feature is
	 * disabled.
	 */
	private volatile boolean forceSync = false;

	private volatile int valueCacheSize = ValueStore.VALUE_CACHE_SIZE;

	private volatile int valueIDCacheSize = ValueStore.VALUE_ID_CACHE_SIZE;

	private volatile int namespaceCacheSize = ValueStore.NAMESPACE_CACHE_SIZE;

	private volatile int namespaceIDCacheSize = ValueStore.NAMESPACE_ID_CACHE_SIZE;

	private volatile TripleStore tripleStore;

	private volatile ValueStore valueStore;

	private volatile NamespaceStore namespaceStore;

	/**
	 * Lock manager used to prevent concurrent transactions.
	 */
	private final ExclusiveLockManager txnLockManager = new ExclusiveLockManager(debugEnabled());

	/**
	 * Data directory lock.
	 */
	private volatile Lock dirLock;

	private boolean encrypt;

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new NativeStore.
	 */
	public CardinalityNativeStore() {
		super();
	}

	public CardinalityNativeStore(File dataDir) {
		this();
		setDataDir(dataDir);
	}

	public CardinalityNativeStore(File dataDir, String tripleIndexes, boolean encrypt) {
		this(dataDir);
		this.encrypt=encrypt;
		setTripleIndexes(tripleIndexes);
	}

	/*---------*
	 * Methods *
	 *---------*/

	/**
	 * Sets the triple indexes for the native store, must be called before
	 * initialization.
	 * 
	 * @param tripleIndexes
	 *        An index strings, e.g. <tt>spoc,posc</tt>.
	 */
	public void setTripleIndexes(String tripleIndexes) {
		if (isInitialized()) {
			throw new IllegalStateException("sail has already been intialized");
		}

		this.tripleIndexes = tripleIndexes;
	}

	public String getTripleIndexes() {
		return tripleIndexes;
	}

	/**
	 * Specifiec whether updates should be synced to disk forcefully, must be
	 * called before initialization. Enabling this feature may prevent corruption
	 * in case of events like power loss, but can have a severe impact on write
	 * performance. By default, this feature is disabled.
	 */
	public void setForceSync(boolean forceSync) {
		this.forceSync = forceSync;
	}

	public boolean getForceSync() {
		return forceSync;
	}

	public void setValueCacheSize(int valueCacheSize) {
		this.valueCacheSize = valueCacheSize;
	}

	public void setValueIDCacheSize(int valueIDCacheSize) {
		this.valueIDCacheSize = valueIDCacheSize;
	}

	public void setNamespaceCacheSize(int namespaceCacheSize) {
		this.namespaceCacheSize = namespaceCacheSize;
	}

	public void setNamespaceIDCacheSize(int namespaceIDCacheSize) {
		this.namespaceIDCacheSize = namespaceIDCacheSize;
	}

	/**
	 * Initializes this NativeStore.
	 * 
	 * @exception SailException
	 *            If this NativeStore could not be initialized using the
	 *            parameters that have been set.
	 */
	@Override
	protected void initializeInternal()
		throws SailException
	{
		logger.debug("Initializing NativeStore...");

		// Check initialization parameters
		File dataDir = getDataDir();

		if (dataDir == null) {
			throw new SailException("Data dir has not been set");
		}
		else if (!dataDir.exists()) {
			boolean success = dataDir.mkdirs();
			if (!success) {
				throw new SailException("Unable to create data directory: " + dataDir);
			}
		}
		else if (!dataDir.isDirectory()) {
			throw new SailException("The specified path does not denote a directory: " + dataDir);
		}
		else if (!dataDir.canRead()) {
			throw new SailException("Not allowed to read from the specified directory: " + dataDir);
		}

		// try to lock the directory or fail
		dirLock = new DirectoryLockManager(dataDir).lockOrFail();

		logger.debug("Data dir is " + dataDir);

		try {
			namespaceStore = new NamespaceStore(dataDir);
			valueStore = new ValueStore(dataDir, forceSync, valueCacheSize, valueIDCacheSize,
					namespaceCacheSize, namespaceIDCacheSize, encrypt);
			tripleStore = new TripleStore(dataDir, tripleIndexes, forceSync);
		}
		catch (IOException e) {
			// NativeStore initialization failed, release any allocated files
			if (valueStore != null) {
				try {
					valueStore.close();
				}
				catch (IOException e1) {
					logger.warn("Failed to close value store after native store initialization failure", e);
				}
				valueStore = null;
			}
			if (namespaceStore != null) {
				namespaceStore.close();
				namespaceStore = null;
			}

			dirLock.release();

			throw new SailException(e);
		}

		logger.debug("NativeStore initialized");
	}

	@Override
	protected void shutDownInternal()
		throws SailException
	{
		logger.debug("Shutting down NativeStore...");

		try {
			tripleStore.close();
			valueStore.close();
			namespaceStore.close();

			logger.debug("NativeStore shut down");
		}
		catch (IOException e) {
			throw new SailException(e);
		}
		finally {
			dirLock.release();
		}
	}

	public boolean isWritable() {
		return getDataDir().canWrite();
	}

	@Override
	protected NotifyingSailConnection getConnectionInternal()
		throws SailException
	{
		try {
			return new CardinalityNativeStoreConnection(this);
		}
		catch (IOException e) {
			throw new SailException(e);
		}
	}

	public ValueFactory getValueFactory() {
		return valueStore;
	}

	protected TripleStore getTripleStore() {
		return tripleStore;
	}

	protected ValueStore getValueStore() {
		return valueStore;
	}

	protected NamespaceStore getNamespaceStore() {
		return namespaceStore;
	}

	protected Lock getTransactionLock()
		throws SailException
	{
		try {
			return txnLockManager.getExclusiveLock();
		}
		catch (InterruptedException e) {
			throw new SailException(e);
		}
	}

	protected List<Integer> getContextIDs(Resource... contexts)
		throws IOException
	{
		assert contexts.length > 0 : "contexts must not be empty";

		// Filter duplicates
		LinkedHashSet<Resource> contextSet = new LinkedHashSet<Resource>();
		Collections.addAll(contextSet, contexts);

		// Fetch IDs, filtering unknown resources from the result
		List<Integer> contextIDs = new ArrayList<Integer>(contextSet.size());
		for (Resource context : contextSet) {
			if (context == null) {
				contextIDs.add(0);
			}
			else {
				int contextID = valueStore.getID(context);
				if (contextID != NativeValue.UNKNOWN_ID) {
					contextIDs.add(contextID);
				}
			}
		}

		return contextIDs;
	}

	protected CloseableIteration<Resource, IOException> getContextIDs(boolean readTransaction)
		throws IOException
	{
		CloseableIteration<? extends Statement, IOException> stIter;
		CloseableIteration<Resource, IOException> ctxIter;
		RecordIterator btreeIter;
		btreeIter = tripleStore.getAllTriplesSortedByContext(readTransaction);
		if (btreeIter == null) {
			// Iterator over all statements
			stIter = createStatementIterator(null, null, null, true, readTransaction);
		}
		else {
			stIter = new NativeStatementIterator(btreeIter, valueStore);
		}
		// Filter statements without context resource
		stIter = new FilterIteration<Statement, IOException>(stIter) {

			@Override
			protected boolean accept(Statement st) {
				return st.getContext() != null;
			}
		};
		// Return the contexts of the statements
		ctxIter = new ConvertingIteration<Statement, Resource, IOException>(stIter) {

			@Override
			protected Resource convert(Statement st) {
				return st.getContext();
			}
		};
		if (btreeIter == null) {
			// Filtering any duplicates
			ctxIter = new DistinctIteration<Resource, IOException>(ctxIter);
		}
		else {
			// Filtering sorted duplicates
			ctxIter = new ReducedIteration<Resource, IOException>(ctxIter);
		}
		return ctxIter;
	}

	/**
	 * Creates a statement iterator based on the supplied pattern.
	 * 
	 * @param subj
	 *        The subject of the pattern, or <tt>null</tt> to indicate a
	 *        wildcard.
	 * @param pred
	 *        The predicate of the pattern, or <tt>null</tt> to indicate a
	 *        wildcard.
	 * @param obj
	 *        The object of the pattern, or <tt>null</tt> to indicate a wildcard.
	 * @param contexts
	 *        The context(s) of the pattern. Note that this parameter is a vararg
	 *        and as such is optional. If no contexts are supplied the method
	 *        operates on the entire repository.
	 * @return A StatementIterator that can be used to iterate over the
	 *         statements that match the specified pattern.
	 */
	protected CloseableIteration<? extends Statement, IOException> createStatementIterator(Resource subj,
			URI pred, Value obj, boolean includeInferred, boolean readTransaction, Resource... contexts)
		throws IOException
	{
		int subjID = NativeValue.UNKNOWN_ID;
		if (subj != null) {
			subjID = valueStore.getID(subj);
			if (subjID == NativeValue.UNKNOWN_ID) {
				return new EmptyIteration<Statement, IOException>();
			}
		}

		int predID = NativeValue.UNKNOWN_ID;
		if (pred != null) {
			predID = valueStore.getID(pred);
			if (predID == NativeValue.UNKNOWN_ID) {
				return new EmptyIteration<Statement, IOException>();
			}
		}

		int objID = NativeValue.UNKNOWN_ID;
		if (obj != null) {
			objID = valueStore.getID(obj);
			if (objID == NativeValue.UNKNOWN_ID) {
				return new EmptyIteration<Statement, IOException>();
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

		ArrayList<NativeStatementIterator> perContextIterList = new ArrayList<NativeStatementIterator>(
				contextIDList.size());

		for (int contextID : contextIDList) {
			RecordIterator btreeIter;

			if (includeInferred) {
				// Get both explicit and inferred statements
				btreeIter = tripleStore.getTriples(subjID, predID, objID, contextID, readTransaction);
			}
			else {
				// Only get explicit statements
				btreeIter = tripleStore.getTriples(subjID, predID, objID, contextID, true, readTransaction);
			}

			perContextIterList.add(new NativeStatementIterator(btreeIter, valueStore));
		}

		if (perContextIterList.size() == 1) {
			return perContextIterList.get(0);
		}
		else {
			return new UnionIteration<Statement, IOException>(perContextIterList);
		}
	}

	protected double cardinality(Resource subj, URI pred, Value obj, Resource context)
		throws IOException
	{
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

		int contextID = NativeValue.UNKNOWN_ID;
		if (context != null) {
			contextID = valueStore.getID(context);
			if (contextID == NativeValue.UNKNOWN_ID) {
				return 0;
			}
		}

		return tripleStore.cardinality(subjID, predID, objID, contextID);
	}
}
