/*
	Copyright 2015 ITACA-TSB, http://www.tsb.upv.es
	Instituto Tecnologico de Aplicaciones de Comunicacion
	Avanzadas - Grupo Tecnologias para la Salud y el
	Bienestar (TSB)

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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;

import org.universAAL.context.che.Hub;
import org.universAAL.context.che.Hub.Log;
import org.universAAL.context.che.database.Backend;
import org.universAAL.middleware.container.utils.StringUtils;
import org.universAAL.middleware.context.ContextEvent;
import org.universAAL.middleware.context.owl.ContextProvider;
import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.serialization.MessageContentSerializer;
import org.universAAL.middleware.util.Constants;
import org.openrdf.OpenRDFException;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.BindingSet;
import org.openrdf.query.BooleanQuery;
import org.openrdf.query.Dataset;
import org.openrdf.query.GraphQuery;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.Update;
import org.openrdf.query.impl.DatasetImpl;
import org.openrdf.query.resultio.sparqlxml.SPARQLResultsXMLWriter;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.turtle.TurtleWriterFactory;
import org.openrdf.sail.nativerdf.NativeStore;
import org.openrdf.sail.inferencer.fc.ForwardChainingRDFSInferencer;

/**
 * Implementation of {@link org.universAAL.context.che.database.Backend} that
 * uses Sesame to store and retrieve the context events in/from an underlying
 * store server (a SAIL in Sesame). In this case it uses the Sesame native
 * Filesystem repository, interfaced with a forward chaining RDFS inferencer.
 *
 * @author <a href="mailto:alfiva@itaca.upv.es">Alvaro Fides Valero</a>
 *
 */
public class SesameBackend implements Backend {
	/**
	 * Logger.
	 */
	private static Log log = Hub.getLog(SesameBackend.class);
	/**
	 * The sesame store.
	 */
	Repository myRepository;
	/**
	 * The connection to the sesame store
	 */
	RepositoryConnection con;
	/**
	 * uaal-turtle parser.
	 */
	protected MessageContentSerializer uAALParser;

	// TODO: Remove this DEBUG
	/**
	 * If true cleans the store when stopped.
	 */
	private static final boolean DEBUG_DB = Boolean
			.parseBoolean(Hub.getProperties().getProperty("RECYCLE.DEBUG", "false"));
	/**
	 * Constants to identify SPARQL queries.
	 */
	private static final int SELECT = 0, CONSTRUCT = 1, DESCRIBE = 2, ASK = 3, UPDATE = 4, NONE = -1;
	/**
	 * Name of file holding the stored OWLs
	 */
	public static final String PRELOAD_FILE = "preload.properties";
	/**
	 * Determines if data is stored/queried in tenant-aware mode
	 */
	public static Boolean tenantAware = Boolean.parseBoolean(Hub.getProperties().getProperty("STORE.TENANT", "true"));

	/*
	 * (non-Javadoc)
	 *
	 * @see org.universAAL.context.che.database.Backend#connect()
	 */
	synchronized public void connect() {
		String dataPath = Hub.getProperties().getProperty("STORE.LOCATION");
		// I use C:/Proyectos/UNIVERSAAL/ContextStore/Stores/SAIL_FCRDFS_Native
		if (dataPath != null) {
			File dataDir = new File(dataPath);
			String indexes = "spoc,posc,cosp";
			// TODO: Change indexes (specially if we dont use contexts)
			log.info("CHe connects to {} ", dataDir.toString());
			// TODO: Study other reasoners, if any
			try {
				myRepository = new SailRepository(new ForwardChainingRDFSInferencer(new NativeStore(dataDir, indexes)));
				myRepository.initialize();
				con = myRepository.getConnection();
				if (Boolean.parseBoolean(Hub.getProperties().getProperty("STORE.PRELOAD"))) {
					this.populate();
				}
			} catch (Exception e) {
				log.error("connect", "Exception trying to initilaize the store: {} ", e);
				e.printStackTrace();
			}
		} else {
			log.error("connect",
					"No location specified for the store. " + "Add and specify the configuration parameter "
							+ "STORE.LOCATION to the configuration file of "
							+ "the CHE pointing to a valid folder path.");
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.universAAL.context.che.database.Backend#populate()
	 */
	public void populate() throws RepositoryException, RDFParseException, IOException {
		// RepositoryConnection con = myRepository.getConnection();
		Properties stored = getProperties();
		try {
			File confHome = new File(Hub.getConfigHome().getAbsolutePath());
			File[] files = confHome.listFiles(new FilenameFilter() {
				public boolean accept(File dir, String name) {
					return name.toLowerCase().endsWith(".owl");
				}
			});
			for (int i = 0; i < files.length; i++) {
				// TODO: Guess the default namespace. Otherwise the file
				// should not use default namespace prefix : .
				String name = files[i].getName();
				if (!"true".equals(stored.getProperty(name))) {
					try { // TODO: Handle format
						con.add(files[i], null, RDFFormat.TURTLE);
					} catch (RDFParseException e) {
						con.add(files[i], null, RDFFormat.RDFXML);
					}
					stored.setProperty(name, "true");
					log.debug("populate", "populated store with: " + name);
				} else {
					log.info("populate", name + " is already populated");
				}
			}
		} finally {
			// con.close();
			setProperties(stored);
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.universAAL.context.che.database.Backend#populate(java.lang.String)
	 */
	public void populate(String filename) throws RepositoryException, RDFParseException, IOException {
		// RepositoryConnection con = myRepository.getConnection();
		Properties stored = getProperties();
		try {
			File file = new File(Hub.getConfigHome().getAbsolutePath(), filename);
			// TODO: Guess the default namespace. Otherwise the file
			// should not use default namespace prefix : .
			String name = file.getName();
			if (!"true".equals(stored.getProperty(name))) {
				try { // TODO: Handle format
					con.add(file, null, RDFFormat.TURTLE);
				} catch (RDFParseException e) {
					con.add(file, null, RDFFormat.RDFXML);
				}
				stored.setProperty(name, "true");
				log.debug("populate", "populated store with: " + name);
			} else {
				log.info("populate", name + " is already populated");
			}
		} finally {
			// con.close();
			setProperties(stored);
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.universAAL.context.che.database.Backend#close()
	 */
	public void close() {
		try {
			if (DEBUG_DB) { // TODO: Remove this
				// RepositoryConnection con = myRepository.getConnection();
				try {
					con.clear();
					Properties empty = new Properties();
					setProperties(empty);
				} finally {
					con.close();
				}
			}
			myRepository.shutDown();
		} catch (OpenRDFException exc) {
			log.error("close", "Exception trying to shutdown the store: {} ", exc);
			exc.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.universAAL.context.che.database.Backend#storeEvent(org.universAAL
	 * .middleware.context.ContextEvent)
	 */
	synchronized public void storeEvent(ContextEvent e) {
		try {
			// RepositoryConnection con = myRepository.getConnection();
			try {
				log.debug("storeEvent", "Adding event to store");
				if (tenantAware) {
					// Tenant-aware enabled: add tenants as RDF context
					List scopeList = e.getScopes();
					if (scopeList.isEmpty()) {
						// No tenants, do as always
						con.add(new StringReader(uAALParser.serialize(e)), e.getURI(), RDFFormat.TURTLE);
					} else {
						ValueFactory f = myRepository.getValueFactory();
						String[] scopeArray = (String[]) scopeList.toArray(new String[0]);
						URI[] contextArray = new URI[scopeArray.length];
						for (int i = 0; i < scopeArray.length; i++) {
							// Check that scope is valid URI
							contextArray[i] = f.createURI(Resource.isQualifiedName(scopeArray[i]) ? scopeArray[i]
									: Constants.MIDDLEWARE_LOCAL_ID_PREFIX + scopeArray[i]);
						}
						// store with associated tenants
						con.add(new StringReader(uAALParser.serialize(e)), e.getURI(), RDFFormat.TURTLE, contextArray);
					}
				} else {
					// Not tenant-aware, store in default RDF context
					con.add(new StringReader(uAALParser.serialize(e)), e.getURI(), RDFFormat.TURTLE);
				}
				log.debug("storeEvent", "Successfully added event to store");
			} catch (IOException exc) {
				log.error("storeEvent", "Error trying to add event to the store. " + " Because: {}", exc);
				exc.printStackTrace();
			} finally {
				// con.close();
			}
		} catch (OpenRDFException exc) {
			log.error("storeEvent", "Error trying to get connection to store: {}", exc);
			exc.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.universAAL.context.che.database.Backend#queryBySPARQL(java.lang.
	 * String )
	 */
	synchronized public String queryBySPARQL(String input, String... scopeArray) {
		log.debug("queryBySPARQL", "queryBySPARQL");
		String result = null;
		try {
			// RepositoryConnection con = myRepository.getConnection();
			try {
				ByteArrayOutputStream stream = new ByteArrayOutputStream();
				// Find out if the call has scope and set it as dataset
				Dataset ds = scopesToFroms(scopeArray);
				// Otherwise dont set empty dataset or else result is empty
				boolean setds = !ds.getDefaultGraphs().isEmpty();
				// Theres no way in Sesame to find out which type of query it is
				// We have to find out ourselves with get
				switch (getQueryType(input)) {
				case SELECT:// TODO Put a selector in uAAL service for XML,
					// because this is not possible with Turtle
					SPARQLResultsXMLWriter selectWriter = new SPARQLResultsXMLWriter(stream);
					TupleQuery tquery = con.prepareTupleQuery(QueryLanguage.SPARQL, input);
					if (setds)
						tquery.setDataset(ds);
					tquery.evaluate(selectWriter);
					result = stream.toString("UTF-8");
					break;
				case ASK:
					BooleanQuery bquery = con.prepareBooleanQuery(QueryLanguage.SPARQL, input);
					if (setds)
						bquery.setDataset(ds);
					result = bquery.evaluate() ? "true" : "false";
					break;
				case CONSTRUCT:// TODO: Put a selector in uAAL service for XML
					// results instead of Turtle
					TurtleWriterFactory factory1 = new TurtleWriterFactory();
					RDFWriter construtWriter = factory1.getWriter(stream);
					GraphQuery cquery = con.prepareGraphQuery(QueryLanguage.SPARQL, input);
					if (setds)
						cquery.setDataset(ds);
					cquery.evaluate(construtWriter);
					result = stream.toString("UTF-8");
					factory1 = null; // Just in case...
					break;
				case DESCRIBE:// TODO: Put a selector in uAAL service for XML
					// results instead of Turtle
					TurtleWriterFactory factory2 = new TurtleWriterFactory();
					RDFWriter describeWriter = factory2.getWriter(stream);
					GraphQuery dquery = con.prepareGraphQuery(QueryLanguage.SPARQL, input);
					if (setds)
						dquery.setDataset(ds);
					dquery.evaluate(describeWriter);
					result = stream.toString("UTF-8");
					factory2 = null; // Just in case...
					break;
				case UPDATE:
					Update uquery = con.prepareUpdate(QueryLanguage.SPARQL, input);
					if (setds)
						uquery.setDataset(ds);
					uquery.execute();
					result = "true";
					break;
				case NONE:
					throw new MalformedQueryException("A SPARQL query must contain one of SELECT, "
							+ "CONSTRUCT, DESCRIBE, ASK, or UPDATE in " + "case of SPARQL Updates.");
				default:
					throw new MalformedQueryException("Unknown SPARQL Query.");
				}
			} catch (UnsupportedEncodingException e) {
				log.error("queryBySPARQL", "Could not parse results to UTF-8 encoding");
				e.printStackTrace();
			} finally {
				// con.close();
			}
		} catch (OpenRDFException exc) {
			log.error("queryBySPARQL", "Error trying to get connection to store: {}", exc);
			exc.printStackTrace();
		} catch (Exception exc) {
			log.error("queryBySPARQL", "Unknown Error handling SPARQL: {}", exc);
			exc.printStackTrace();
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.universAAL.context.che.database.Backend#retrieveEventsBySPARQL(java
	 * .lang.String)
	 */
	synchronized public ArrayList retrieveEventsBySPARQL(String input, String... scopeArray) {
		log.debug("retrieveEventsBySPARQL", "retrieveEventsBySPARQL");
		ArrayList solution = new ArrayList();
		try {
			// RepositoryConnection con = myRepository.getConnection();
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			TurtleWriterFactory factory = new TurtleWriterFactory();
			RDFWriter writer = factory.getWriter(stream);
			try {
				// If there are scopes, add them as FROM before SELECT
				if (scopeArray != null && scopeArray.length > 0) {
					input = input.replace("SELECT", scopesToFroms(scopeArray) + "SELECT");
				}
				TupleQuery tquery = con.prepareTupleQuery(QueryLanguage.SPARQL, input);
				TupleQueryResult result = tquery.evaluate();
				try {
					while (result.hasNext()) {
						BindingSet bindingSet = result.next();
						Value valueOfC = bindingSet.getValue("c");
						// With the query we get the URI of events
						if (valueOfC instanceof URI) {
							// Then we must describe them fully
							con.exportStatements((URI) valueOfC, null, null, true, writer);
							// Instead of using a Converter, we parse the Turtle
							// result
							solution.add((ContextEvent) uAALParser.deserialize(stream.toString("UTF-8")));
							stream.reset();
						} else {
							log.error("retrieveEventsBySPARQL", "Returned value was not a Resource, "
									+ "and therefore not a Context Event. " + "When querying for Context Events, "
									+ "the SPARQL query must be a SELECT " + "of asingle value ?c where a resource of "
									+ "rdf:type Context Event must be placed " + "according to clauses like WHERE");
						}
					}
				} catch (UnsupportedEncodingException e) {
					log.error("retrieveEventsBySPARQL", "Could not parse results to UTF-8 encoding");
					e.printStackTrace();
				} finally {
					result.close();
				}
			} finally {
				// con.close();
			}
		} catch (OpenRDFException exc) {
			log.error("retrieveEventsBySPARQL", "Error trying to get connection to store: {}", exc);
			exc.printStackTrace();
		}
		return solution.isEmpty() ? null : solution;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.universAAL.context.che.database.Backend#retrieveEvent(java.lang.
	 * String , java.lang.String, java.lang.String, java.lang.Object,
	 * java.lang.Integer, java.lang.Long, java.lang.Object, java.lang.Long)
	 */
	public ArrayList retrieveEvent(String subject, String subjecttype, String predicate, Object object,
			Integer confidence, Long expiration, Object provider, Long tstamp, String... scopeArray) {
		log.debug("retrieveEvent", "retrieveEvent");
		return retrieveEventsBySPARQL(prepareQuery(subject, subjecttype, predicate, object, confidence, expiration,
				provider, tstamp, null, null), scopeArray);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.universAAL.context.che.database.Backend#retrieveEventsBetweenTstmp
	 * (java.lang.String, java.lang.String, java.lang.String, java.lang.Object,
	 * java.lang.Integer, java.lang.Long,
	 * org.universAAL.middleware.context.owl.ContextProvider, java.lang.Long,
	 * java.lang.Long, java.lang.Long)
	 */
	public ArrayList retrieveEventsBetweenTstmp(String subject, String subjecttype, String predicate, Object object,
			Integer confidence, Long expiration, ContextProvider provider, Long tstamp, Long tstfrom, Long tstto,
			String... scopeArray) {
		log.debug("retrieveEventsBetweenTstmp", "retrieveEventsBetweenTstmp");
		return retrieveEventsBySPARQL(prepareQuery(subject, subjecttype, predicate, object, confidence, expiration,
				provider, tstamp, tstfrom, tstto), scopeArray);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.universAAL.context.che.database.Backend#retrieveEventsFromTstmp(java
	 * .lang.String, java.lang.String, java.lang.String, java.lang.Object,
	 * java.lang.Integer, java.lang.Long,
	 * org.universAAL.middleware.context.owl.ContextProvider, java.lang.Long,
	 * java.lang.Long)
	 */
	public ArrayList retrieveEventsFromTstmp(String subject, String subjecttype, String predicate, Object object,
			Integer confidence, Long expiration, ContextProvider provider, Long tstamp, Long tstfrom,
			String... scopeArray) {
		log.debug("retrieveEventsFromTstmp", "retrieveEventsFromTstmp");
		return retrieveEventsBySPARQL(prepareQuery(subject, subjecttype, predicate, object, confidence, expiration,
				provider, tstamp, tstfrom, null), scopeArray);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.universAAL.context.che.database.Backend#retrieveEventsToTstmp(java
	 * .lang.String, java.lang.String, java.lang.String, java.lang.Object,
	 * java.lang.Integer, java.lang.Long,
	 * org.universAAL.middleware.context.owl.ContextProvider, java.lang.Long,
	 * java.lang.Long)
	 */
	public ArrayList retrieveEventsToTstmp(String subject, String subjecttype, String predicate, Object object,
			Integer confidence, Long expiration, ContextProvider provider, Long tstamp, Long tstto,
			String... scopeArray) {
		log.debug("retrieveEventsToTstmp", "retrieveEventsToTstmp");
		return retrieveEventsBySPARQL(prepareQuery(subject, subjecttype, predicate, object, confidence, expiration,
				provider, tstamp, null, tstto), scopeArray);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.universAAL.context.che.database.Backend#removeOldEvents(long)
	 */
	synchronized public void removeOldEvents(long tst) {
		log.debug("removeOldEvents", "removeOldEvents stored before: " + tst);
		String removeQuery = "DELETE { ?s ?p ?o } " + "WHERE" + "  { ?s "
				+ " <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> "
				+ " <http://ontology.universAAL.org/Context.owl#ContextEvent> ;" + "  ?p ?o ;"
				+ "  <http://ontology.universAAL.org/Context.owl#hasTimestamp> ?t ." + " FILTER ( ?t <= \"" + tst
				+ "\"^^<http://www.w3.org/2001/XMLSchema#decimal> )  }";
		try {
			// RepositoryConnection con = myRepository.getConnection();
			try {
				Update uquery = con.prepareUpdate(QueryLanguage.SPARQL, removeQuery);
				uquery.execute();
			} finally {
				// con.close();
			}
		} catch (OpenRDFException exc) {
			log.error("removeOldEvents", "Error trying to get connection to store: {}", exc);
			exc.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.universAAL.context.che.database.Backend#setuAALParser(org.universAAL
	 * .middleware.sodapop.msg.MessageContentSerializer)
	 */
	public void setuAALParser(MessageContentSerializer service) {
		this.uAALParser = service;
	}

	// --Helper Methods---

	/**
	 * Finds out what kind of SPARQL query the input is.
	 *
	 * @param input
	 *            The SPARQL query to analyze
	 * @return The value of the constant indicating the type of query. One of
	 *         <code>SesameBackend.SELECT</code>,
	 *         <code>SesameBackend.CONSTRUCT</code>,
	 *         <code>SesameBackend.DESCRIBE</code>,
	 *         <code>SesameBackend.ASK</code>, <code>SesameBackend.UPDATE</code>
	 *         or <code>SesameBackend.NONE</code>
	 */
	private int getQueryType(String input) {
		int[] indexes = { input.indexOf("SELECT"), input.indexOf("CONSTRUCT"), input.indexOf("DESCRIBE"),
				input.indexOf("ASK"), input.indexOf("INSERT"), input.indexOf("DELETE") };
		int value = input.length();
		int index = -1;
		for (int i = 0; i < indexes.length; i++) {
			int current = indexes[i];
			if (current > -1 && current < value) {
				value = current;
				index = i;
			}
		} // Finds out what SPARQL keyword goes first
		if (index > UPDATE) {
			return UPDATE; // If DELETE treat as INSERT
		}
		return index;
	}

	/**
	 * Given the uAAL values of all or some of the fields of a Context Event,
	 * prepares the appropriate SPARQL SELECT query that would return matching
	 * events.
	 *
	 * @param subject
	 *            Subject of the event. <code>null</code> for wildacrd.
	 * @param subjecttype
	 *            Type of the subject of the event. <code>null</code> for
	 *            wildacrd.
	 * @param predicate
	 *            Predicate of the event. <code>null</code> for wildacrd.
	 * @param object
	 *            Object of the event. <code>null</code> for wildacrd.
	 * @param confidence
	 *            Confidence of the event. <code>null</code> for wildacrd.
	 * @param expiration
	 *            Expiration time of the event. <code>null</code> for wildacrd.
	 * @param provider
	 *            Cintext Provider of the event. <code>null</code> for wildacrd.
	 * @param tstamp
	 *            Timestamp of the event. <code>null</code> for wildacrd.
	 * @param tstfrom
	 *            Timestamp from which events are requested. <code>null</code>
	 *            for none.
	 * @param tstto
	 *            Timestamp until which events are requested. <code>null</code>
	 *            for none.
	 * @return The prepared SPARQL query
	 */
	private String prepareQuery(String subject, String subjecttype, String predicate, Object object, Integer confidence,
			Long expiration, Object provider, Long tstamp, Long tstfrom, Long tstto) {

		// We could use Jena ARQ to build a query programatically and then
		// serialize it. But that is not going to happen.
		StringBuffer query = new StringBuffer("SELECT ?c WHERE { ?c <http://www.w3.org/1999/02/22-rdf-syntax-ns#type>"
				+ "  <http://ontology.universAAL.org/Context.owl#ContextEvent> ; \n");

		if (subject != null) {
			query.append(" <http://www.w3.org/1999/02/22-rdf-syntax-ns#subject> <" + subject + "> ; \n");
		}
		if (subjecttype != null) {
			query.append(" <http://www.w3.org/1999/02/22-rdf-syntax-ns#subject> ?s ; \n");
		}
		if (predicate != null) {
			query.append(" <http://www.w3.org/1999/02/22-rdf-syntax-ns#predicate> <" + predicate + "> ; \n");
		}
		if (object != null) {
			// Object can be a Resource or a literal
			String objExpr = getObjectExpression(object);
			if (objExpr != null) {
				query.append(" <http://www.w3.org/1999/02/22-rdf-syntax-ns#object> " + objExpr + " ; \n");
			}
		}
		if (confidence != null) {
			query.append(" <http://ontology.universAAL.org/Context.owl#hasConfidence> \"" + confidence
					+ "\"^^<http://www.w3.org/2001/XMLSchema#int> ; \n");
		}
		if (expiration != null) {
			query.append(" <http://ontology.universAAL.org/Context.owl#hasExpirationTime> \"" + expiration
					+ "\"^^<http://www.w3.org/2001/XMLSchema#decimal> ; \n");
		}
		if (provider != null) {
			// Provider must be a ContextProvider instance
			ContextProvider cProv;
			if (provider instanceof ContextProvider) {
				cProv = (ContextProvider) provider;
			} else {
				cProv = null;
			}
			if (cProv != null) {
				query.append(" <http://ontology.universAAL.org/Context.owl#hasProvider> <" + cProv.getURI() + "> ; \n");
			}
		}
		if (tstamp != null) {
			query.append(" <http://ontology.universAAL.org/Context.owl#hasTimestamp> \"" + tstamp
					+ "\"^^<http://www.w3.org/2001/XMLSchema#decimal> ; \n");
		}
		if (tstfrom != null || tstto != null) {
			query.append(" <http://ontology.universAAL.org/Context.owl#hasTimestamp> ?t ; \n");
		}

		query.delete(query.lastIndexOf(";"), query.length());
		query.append(". \n");

		if (subjecttype != null) {
			query.append(" ?s  <http://www.w3.org/1999/02/22-rdf-syntax-ns#type>  <" + subjecttype + "> . \n");
		}

		if (tstfrom != null || tstto != null) {
			if (tstfrom != null) {
				if (tstto != null) {
					query.append(
							" FILTER ( ?t > \"" + tstfrom + "\"^^<http://www.w3.org/2001/XMLSchema#decimal> && ?t < \""
									+ tstto + "\"^^<http://www.w3.org/2001/XMLSchema#decimal> ) \n");
				} else {
					query.append(" FILTER ( ?t > \"" + tstfrom + "\"^^<http://www.w3.org/2001/XMLSchema#decimal> ) \n");
				}
			} else {
				query.append(" FILTER ( ?t < \"" + tstto + "\"^^<http://www.w3.org/2001/XMLSchema#decimal> ) \n");
			}
		}
		query.append("}");
		return query.toString();
	}

	/**
	 * Because the Object of a Context Event can be either a Resource or a
	 * Literal, its String representation for the prepared query will vary. This
	 * method constructs the appropriate expression representation of an object
	 * for an SPARQL query.
	 *
	 * @param obj
	 *            The Object to get the SPARQL expression for
	 * @return The String expression describing the Object for the query
	 */
	private String getObjectExpression(Object obj) {
		if (obj instanceof Resource) {
			return "<" + ((Resource) obj).getURI() + ">";
		} else if (obj instanceof Integer) {
			return "\"" + ((Integer) obj).toString() + "\"^^<http://www.w3.org/2001/XMLSchema#int>";
		} else if (obj instanceof Float) {
			return "\"" + ((Float) obj).toString() + "\"^^<http://www.w3.org/2001/XMLSchema#float>";
		} else if (obj instanceof Long) {
			return "\"" + ((Long) obj).toString() + "\"^^<http://www.w3.org/2001/XMLSchema#decimal>";
		} else if (obj instanceof Double) {
			return "\"" + ((Double) obj).toString() + "\"^^<http://www.w3.org/2001/XMLSchema#double>";
		} else if (obj instanceof Boolean) {
			return "\"" + ((Boolean) obj).toString() + "\"^^<http://www.w3.org/2001/XMLSchema#boolean>";
		} else if (obj instanceof String) {
			return "\"" + ((String) obj).toString() + "\"^^<http://www.w3.org/2001/XMLSchema#string>";
		} else if (obj instanceof Duration) {
			return "\"" + ((Duration) obj).toString() + "\"^^<http://www.w3.org/2001/XMLSchema#duration>";
		} else if (obj instanceof XMLGregorianCalendar) {
			return "\"" + ((XMLGregorianCalendar) obj).toString() + "\"^^<http://www.w3.org/2001/XMLSchema#dateTime>";
		} else if (obj instanceof Locale) {
			return "\"" + ((Locale) obj).toString() + "\"^^<http://www.w3.org/2001/XMLSchema#language>";
		} else {
			return null;
		}
	}

	private static synchronized void setProperties(final Properties prop) {
		File confHome = Hub.getConfigHome();
		try {
			FileWriter out;
			if (!confHome.exists()) {
				if (!confHome.mkdir()) {
					log.error("setproperties", "Could not set properties file");
				}
			}
			out = new FileWriter(new File(confHome, PRELOAD_FILE));
			prop.store(out, "DO NOT REMOVE. This file holds the list of OWL "
					+ "stored in CHE. Removing it or setting one to false " + "forces the re-store of the OWL");
			out.close();
		} catch (Exception e) {
			log.error("setproperties", "Could not set preload file: {} ", e);
		}
	}

	private static synchronized Properties getProperties() {
		File confHome = Hub.getConfigHome();
		Properties prop = new Properties();
		try {
			prop = new Properties();
			InputStream in = new FileInputStream(new File(confHome, PRELOAD_FILE));
			prop.load(in);
			in.close();
		} catch (java.io.FileNotFoundException e) {
			log.warn("getProperties", "Properties file does not exist; generating default...");
			// TODO Find out which ones are stored already?
			setProperties(prop);
		} catch (Exception e) {
			log.error("getproperties", "Could not access preload file: {} ", e);
		}
		return prop;
	}

	/**
	 * Turns an array of tenant scopes into a collection of SPARQL FROM
	 * commands. If a scope is not formatted as URI, its prepended the
	 * Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX.
	 *
	 * @param scopes
	 *            Array of scopes
	 * @return a String with a FROM < scope > in different lines
	 */
	private Dataset scopesToFroms(String[] scopes) {
		DatasetImpl ds = new DatasetImpl();
		ValueFactory f = myRepository.getValueFactory();
		for (String scope : scopes) {
			ds.addDefaultGraph(f.createURI(StringUtils.startsWithURIScheme(scope) ? scope
					: Constants.MIDDLEWARE_LOCAL_ID_PREFIX + scope));
		}
		return ds;
	}

}
