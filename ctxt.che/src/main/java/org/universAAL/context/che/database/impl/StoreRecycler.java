/*
	Copyright 2026 the universAAL contributors

	See the NOTICE file distributed with this work for additional
	information regarding copyright ownership

	Licensed under the Apache License, Version 2.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at

	  http://www.apache.org/licenses/LICENSE-2.0

	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.
 */
package org.universAAL.context.che.database.impl;

import java.io.File;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryResult;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.repository.sail.SailRepositoryConnection;
import org.eclipse.rdf4j.sail.inferencer.InferencerConnection;
import org.eclipse.rdf4j.sail.inferencer.fc.ForwardChainingRDFSInferencer;
import org.eclipse.rdf4j.sail.nativerdf.NativeStore;
import org.universAAL.context.che.database.RecycleLog;

/**
 * Removes old context events from the store, including the blank nodes that
 * belong to them (e.g. the situation or the user described in the event).
 * Removing only the statements of the event itself leaves these blank nodes
 * behind, and the store keeps growing.
 * <p>
 * The removal is done in batches, one transaction per batch, on the store
 * opened without inferencer ({@link #recycleOffline}, the CHe must not have the
 * store open): with the RDFS inferencer, every transaction that removes
 * statements makes the inferencer recompute all inferred statements of the
 * store. So the inferred statements are recomputed only once, at the end. One
 * big transaction instead is no option: reading within a transaction gets
 * slower with every change in it.
 */
public final class StoreRecycler {

	private static final String CE = "http://ontology.universAAL.org/Context.owl#";

	/**
	 * Number of events removed per transaction.
	 */
	public static final int BATCH = 1000;

	private StoreRecycler() {
	}

	/**
	 * Counters of a recycling run.
	 */
	public static class Result {
		/** removed events */
		public long events;
		/** removed (explicit) statements */
		public long statements;

		@Override
		public String toString() {
			return events + " events (" + statements + " statements)";
		}
	}

	/**
	 * Removes the events received until tst from a store that is not open.
	 *
	 * @param dataDir
	 *            the store folder
	 * @param indexes
	 *            the triple indexes of the store
	 * @param forceSync
	 *            NativeStore forceSync
	 * @param inference
	 *            whether the store is used with the RDFS inferencer. If so,
	 *            the inferred statements are recomputed once at the end,
	 *            otherwise all inferred statements are removed (they are left
	 *            from a time when the inferencer was used).
	 * @param tst
	 *            timestamp in milliseconds until which events are removed
	 * @return the counters
	 */
	public static Result recycleOffline(File dataDir, String indexes, boolean forceSync, boolean inference,
			long tst) {
		long start = System.currentTimeMillis();
		Result res;
		NativeStore ns = new NativeStore(dataDir, indexes);
		ns.setForceSync(forceSync);
		Repository plain = new SailRepository(ns);
		plain.init();
		try {
			SailRepositoryConnection con = (SailRepositoryConnection) plain.getConnection();
			try {
				res = removeEvents(con, tst);
				if (!inference) {
					long t = System.currentTimeMillis();
					con.begin();
					((InferencerConnection) con.getSailConnection()).clearInferred();
					con.commit();
					RecycleLog.log("inferred statements removed (STORE.INFERENCE=false) in "
							+ (System.currentTimeMillis() - t) + " ms");
				}
			} finally {
				con.close();
			}
		} finally {
			plain.shutDown();
		}
		if (inference && res.events > 0) {
			recomputeInferred(dataDir, indexes, forceSync);
		}
		RecycleLog.log("recycling done: " + res + " in " + (System.currentTimeMillis() - start) + " ms");
		return res;
	}

	/**
	 * Opens the store with the RDFS inferencer and makes it recompute all
	 * inferred statements: the inferencer does this after a transaction in
	 * which an existing statement was removed.
	 */
	private static void recomputeInferred(File dataDir, String indexes, boolean forceSync) {
		long t = System.currentTimeMillis();
		NativeStore ns = new NativeStore(dataDir, indexes);
		ns.setForceSync(forceSync);
		Repository repo = new SailRepository(new ForwardChainingRDFSInferencer(ns));
		repo.init();
		try {
			RepositoryConnection con = repo.getConnection();
			try {
				ValueFactory f = con.getValueFactory();
				IRI marker = f.createIRI("urn:org.universAAL.context.che:recycling");
				// added and removed in two transactions: a statement added and
				// removed in the same transaction does not count as removed
				con.begin();
				con.add(marker, marker, marker);
				con.commit();
				con.begin();
				con.remove(marker, marker, marker);
				con.commit();
			} finally {
				con.close();
			}
		} finally {
			repo.shutDown();
		}
		RecycleLog.log("inferred statements recomputed in " + (System.currentTimeMillis() - t) + " ms");
	}

	/**
	 * Removes the events received until tst, with their blank nodes, in
	 * batches of {@link #BATCH} events.
	 *
	 * @param con
	 *            connection to the store
	 * @param tst
	 *            timestamp in milliseconds until which events are removed
	 * @return the counters
	 */
	public static Result removeEvents(RepositoryConnection con, long tst) {
		Result res = new Result();
		String select = "SELECT ?e WHERE { ?e <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <" + CE
				+ "ContextEvent> ; <" + CE + "hasTimestamp> ?t . FILTER ( ?t <= \"" + tst
				+ "\"^^<http://www.w3.org/2001/XMLSchema#decimal> ) } LIMIT " + BATCH;
		RecycleLog.log("removing events until " + new java.util.Date(tst) + " (" + tst + ")");
		boolean ok = false;
		try {
			while (true) {
				long t = System.currentTimeMillis();
				List<Resource> events = new ArrayList<Resource>();
				TupleQuery q = con.prepareTupleQuery(QueryLanguage.SPARQL, select);
				q.setIncludeInferred(false);
				TupleQueryResult r = q.evaluate();
				try {
					while (r.hasNext()) {
						BindingSet b = r.next();
						Value e = b.getValue("e");
						if (e instanceof Resource) {
							events.add((Resource) e);
						}
					}
				} finally {
					r.close();
				}
				if (events.isEmpty()) {
					break;
				}
				con.begin();
				long statements = 0;
				for (Resource e : events) {
					List<Statement> st = describe(con, e);
					con.remove(st);
					statements += st.size();
				}
				con.commit();
				res.events += events.size();
				res.statements += statements;
				RecycleLog.log("  batch: " + events.size() + " events (" + statements + " statements) in "
						+ (System.currentTimeMillis() - t) + " ms");
			}
			ok = true;
		} finally {
			if (!ok && con.isActive()) {
				con.rollback();
				RecycleLog.log("  failed, the current batch is rolled back");
			}
		}
		RecycleLog.log("removed " + res);
		return res;
	}

	/**
	 * The explicit statements of a resource and of all blank nodes reachable
	 * from it. Blank nodes are not shared between events, as every event is
	 * parsed on its own when it is stored.
	 */
	static List<Statement> describe(RepositoryConnection con, Resource s) {
		List<Statement> out = new ArrayList<Statement>();
		Set<Resource> seen = new LinkedHashSet<Resource>();
		Deque<Resource> todo = new ArrayDeque<Resource>();
		todo.push(s);
		while (!todo.isEmpty()) {
			Resource r = todo.pop();
			if (!seen.add(r)) {
				continue;
			}
			RepositoryResult<Statement> it = con.getStatements(r, null, null, false);
			try {
				while (it.hasNext()) {
					Statement st = it.next();
					out.add(st);
					if (st.getObject() instanceof BNode) {
						todo.push((BNode) st.getObject());
					}
				}
			} finally {
				it.close();
			}
		}
		return out;
	}
}
