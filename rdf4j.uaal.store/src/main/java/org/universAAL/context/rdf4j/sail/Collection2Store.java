/*******************************************************************************
 * Copyright (c) 2015 Eclipse RDF4J contributors, Aduna, and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Distribution License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *******************************************************************************/
package org.universAAL.context.rdf4j.sail;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.io.FileUtils;
import org.eclipse.rdf4j.IsolationLevel;
import org.eclipse.rdf4j.IsolationLevels;
import org.eclipse.rdf4j.common.concurrent.locks.Lock;
import org.eclipse.rdf4j.common.concurrent.locks.LockManager;
import org.eclipse.rdf4j.common.io.MavenUtil;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.ModelFactory;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.query.algebra.evaluation.EvaluationStrategy;
import org.eclipse.rdf4j.query.algebra.evaluation.EvaluationStrategyFactory;
import org.eclipse.rdf4j.query.algebra.evaluation.federation.FederatedServiceResolver;
import org.eclipse.rdf4j.query.algebra.evaluation.federation.FederatedServiceResolverClient;
import org.eclipse.rdf4j.query.algebra.evaluation.impl.StrictEvaluationStrategyFactory;
import org.eclipse.rdf4j.repository.sparql.federation.SPARQLServiceResolver;
import org.eclipse.rdf4j.sail.NotifyingSailConnection;
import org.eclipse.rdf4j.sail.SailException;
import org.eclipse.rdf4j.sail.base.SailSource;
import org.eclipse.rdf4j.sail.base.SailStore;
import org.eclipse.rdf4j.sail.base.SnapshotSailStore;
import org.eclipse.rdf4j.sail.helpers.AbstractNotifyingSail;
import org.eclipse.rdf4j.sail.helpers.DirectoryLockManager;

/**
 * A SAIL implementation using B-Tree indexing on disk for storing and querying its data.
 *
 * The NativeStore is designed for datasets between 100,000 and 100 million triples. 
 * On most operating systems, if there is sufficient physical memory, the NativeStore 
 * will act like the MemoryStore, because the read/write commands will be cached by the OS.
 * This technique allows the NativeStore to operate quite well for millions of triples.
 * 
 * @author Arjohn Kampman
 * @author jeen
 * 
 * PATCH UAAL: Copied from NativeStore and modified to allow encryption 
 * and enforce cardinality 1 of any value on top of previous closed collections
 */
public class Collection2Store extends AbstractNotifyingSail implements FederatedServiceResolverClient {

	/*-----------*
	 * Variables *
	 *-----------*/

	protected static final String VERSION = MavenUtil.loadVersion("org.eclipse.rdf4j", "rdf4j-sail-nativerdf",
			"devel");

	/**
	 * Specifies which triple indexes this native store must use.
	 */
	protected volatile String tripleIndexes;

	/**
	 * Flag indicating whether updates should be synced to disk forcefully. This may have a severe impact on
	 * write performance. By default, this feature is disabled.
	 */
	protected volatile boolean forceSync = false;

	protected volatile int valueCacheSize = ValueStore.VALUE_CACHE_SIZE;

	protected volatile int valueIDCacheSize = ValueStore.VALUE_ID_CACHE_SIZE;

	protected volatile int namespaceCacheSize = ValueStore.NAMESPACE_CACHE_SIZE;

	protected volatile int namespaceIDCacheSize = ValueStore.NAMESPACE_ID_CACHE_SIZE;

	protected SailStore store;

	/**
	 * Data directory lock.
	 */
	protected volatile Lock dirLock;

	private EvaluationStrategyFactory evalStratFactory;

	/** independent life cycle */
	private FederatedServiceResolver serviceResolver;

	/** dependent life cycle */
	private SPARQLServiceResolver dependentServiceResolver;

	/**
	 * Lock manager used to prevent concurrent {@link #getTransactionLock(IsolationLevel)} calls.
	 */
	private final ReentrantLock txnLockManager = new ReentrantLock();

	/**
	 * Holds locks for all isolated transactions.
	 */
	private final LockManager isolatedLockManager = new LockManager(debugEnabled());

	/**
	 * Holds locks for all {@link IsolationLevels#NONE} isolation transactions.
	 */
	private final LockManager disabledIsolationLockManager = new LockManager(debugEnabled());

	protected boolean encrypt;
	    
	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new NativeStore.
	 */
	public Collection2Store() {
		super();
		setSupportedIsolationLevels(IsolationLevels.NONE, IsolationLevels.READ_COMMITTED,
				IsolationLevels.SNAPSHOT_READ, IsolationLevels.SNAPSHOT, IsolationLevels.SERIALIZABLE);
		setDefaultIsolationLevel(IsolationLevels.SNAPSHOT_READ);
	}

	public Collection2Store(File dataDir) {
		this();
		setDataDir(dataDir);
	}

	public Collection2Store(File dataDir, String tripleIndexes, boolean encrypt) {
		this(dataDir);
		this.encrypt = encrypt;
		setTripleIndexes(tripleIndexes);
	}

	/*---------*
	 * Methods *
	 *---------*/

	/**
	 * Sets the triple indexes for the native store, must be called before initialization.
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
	 * Specifiec whether updates should be synced to disk forcefully, must be called before initialization.
	 * Enabling this feature may prevent corruption in case of events like power loss, but can have a severe
	 * impact on write performance. By default, this feature is disabled.
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
	 * @return Returns the {@link EvaluationStrategy}.
	 */
	public synchronized EvaluationStrategyFactory getEvaluationStrategyFactory() {
		if (evalStratFactory == null) {
			evalStratFactory = new StrictEvaluationStrategyFactory(getFederatedServiceResolver());
		}
		evalStratFactory.setQuerySolutionCacheThreshold(getIterationCacheSyncThreshold());
		return evalStratFactory;
	}

	/**
	 * Sets the {@link EvaluationStrategy} to use.
	 */
	public synchronized void setEvaluationStrategyFactory(EvaluationStrategyFactory factory) {
		evalStratFactory = factory;
	}

	/**
	 * @return Returns the SERVICE resolver.
	 */
	public synchronized FederatedServiceResolver getFederatedServiceResolver() {
		if (serviceResolver == null) {
			if (dependentServiceResolver == null) {
				dependentServiceResolver = new SPARQLServiceResolver();
			}
			setFederatedServiceResolver(dependentServiceResolver);
		}
		return serviceResolver;
	}

	/**
	 * Overrides the {@link FederatedServiceResolver} used by this instance, but the given resolver is not
	 * shutDown when this instance is.
	 * 
	 * @param resolver
	 *        The SERVICE resolver to set.
	 */
	public synchronized void setFederatedServiceResolver(FederatedServiceResolver resolver) {
		this.serviceResolver = resolver;
		if (resolver != null && evalStratFactory instanceof FederatedServiceResolverClient) {
			((FederatedServiceResolverClient)evalStratFactory).setFederatedServiceResolver(resolver);
		}
	}

	/**
	 * Initializes this NativeStore.
	 * 
	 * @exception SailException
	 *            If this NativeStore could not be initialized using the parameters that have been set.
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
			File versionFile = new File(dataDir, "nativerdf.ver");
			String version = versionFile.exists() ? FileUtils.readFileToString(versionFile) : null;
			if (!VERSION.equals(version) && upgradeStore(dataDir, version)) {
				FileUtils.writeStringToFile(versionFile, VERSION);
			}
			final Collection2SailStore master = new Collection2SailStore(dataDir, tripleIndexes, forceSync,
					valueCacheSize, valueIDCacheSize, namespaceCacheSize, namespaceIDCacheSize, encrypt);
			this.store = new SnapshotSailStore(master, new ModelFactory() {

				public Model createEmptyModel() {
					return new MemoryOverflowModel() {

						@Override
						protected SailStore createSailStore(File dataDir)
							throws IOException, SailException
						{
							// Model can't fit into memory, use another Collection2SailStore to store delta
							return new Collection2SailStore(dataDir, getTripleIndexes());
						}
					};
				}
			}) {

				@Override
				public SailSource getExplicitSailSource() {
					if (isIsolationDisabled()) {
						// no isolation, use Collection2SailStore directly
						return master.getExplicitSailSource();
					}
					else {
						return super.getExplicitSailSource();
					}
				}

				@Override
				public SailSource getInferredSailSource() {
					if (isIsolationDisabled()) {
						// no isolation, use Collection2SailStore directly
						return master.getInferredSailSource();
					}
					else {
						return super.getInferredSailSource();
					}
				}
			};
		}
		catch (Throwable e) {
			// NativeStore initialization failed, release any allocated files
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
			store.close();

			logger.debug("NativeStore shut down");
		}
		finally {
			dirLock.release();
			if (dependentServiceResolver != null) {
				dependentServiceResolver.shutDown();
			}
			logger.debug("NativeStore shut down");
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
			return new Collection2StoreConnection(this);
		}
		catch (IOException e) {
			throw new SailException(e);
		}
	}

	public ValueFactory getValueFactory() {
		return store.getValueFactory();
	}

	/**
	 * This call will block when {@link IsolationLevels#NONE} is provided when there are active transactions
	 * with a higher isolation and block when a higher isolation is provided when there are active
	 * transactions with {@link IsolationLevels#NONE} isolation. Store is either exclusively in
	 * {@link IsolationLevels#NONE} isolation with potentially zero or more transactions, or exclusively in
	 * higher isolation mode with potentially zero or more transactions.
	 * 
	 * @param level
	 *        indicating desired mode {@link IsolationLevels#NONE} or higher
	 * @return Lock used to prevent Store from switching isolation modes
	 * @throws SailException
	 */
	protected Lock getTransactionLock(IsolationLevel level)
		throws SailException
	{
		txnLockManager.lock();
		try {
			if (IsolationLevels.NONE.isCompatibleWith(level)) {
				// make sure no isolated transaction are active
				isolatedLockManager.waitForActiveLocks();
				// mark isolation as disabled
				return disabledIsolationLockManager.createLock(level.toString());
			}
			else {
				// make sure isolation is not disabled
				disabledIsolationLockManager.waitForActiveLocks();
				// mark isolated transaction as active
				return isolatedLockManager.createLock(level.toString());
			}
		}
		catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new SailException(e);
		}
		finally {
			txnLockManager.unlock();
		}
	}

	/**
	 * Checks if any {@link IsolationLevels#NONE} isolation transactions are active.
	 * 
	 * @return <code>true</code> if at least one transaction has direct access to the indexes
	 */
	boolean isIsolationDisabled() {
		return disabledIsolationLockManager.isActiveLock();
	}

	SailStore getSailStore() {
		return store;
	}

	protected boolean upgradeStore(File dataDir, String version)
		throws IOException, SailException
	{
		if (version == null) {
			// either a new store or a pre-2.8.2 store
			ValueStore valueStore = new ValueStore(dataDir);
			try {
				valueStore.checkConsistency();
				return true; // good enough
			}
			catch (SailException e) {
				// valueStore is not consistent - possibly contains two entries for
				// string-literals with the same lexical value (e.g. "foo" and
				// "foo"^^xsd:string). Log an error and indicate upgrade should
				// not be executed.
				logger.error(
						"VALUE INCONSISTENCY: could not automatically upgrade native store to RDF 1.1-compatibility: {}. Failure to upgrade may result in inconsistent query results when comparing literal values.",
						e.getMessage());
				return false;
			}
			finally {
				valueStore.close();
			}
		}
		else {
			return false; // no upgrade needed
		}
	}
}

