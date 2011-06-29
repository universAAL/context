/*
	Copyright 2008-2010 Fraunhofer IGD, http://www.igd.fraunhofer.de
	Fraunhofer-Gesellschaft - Institute of Computer Graphics Research 
	
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

package org.universAAL.context.reasoner;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Properties;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.universAAL.context.conversion.jena.JenaConverter;
import org.universAAL.middleware.context.ContextEvent;
import org.universAAL.middleware.context.ContextPublisher;
import org.universAAL.middleware.context.DefaultContextPublisher;
import org.universAAL.middleware.context.owl.ContextProvider;
import org.universAAL.middleware.context.owl.ContextProviderType;
import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.util.Constants;

import com.hp.hpl.jena.db.DBConnection;
import com.hp.hpl.jena.db.ModelRDB;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.rdf.model.Model;
import com.mysql.jdbc.Driver;

/**
 * @author <a href="mailto:Saied.Tazari@igd.fraunhofer.de">Saied Tazari</a>
 * 
 *         The basic idea of the reasoner (or reasoner in general) is to analyze
 *         all context-events within the system and create higher order events
 *         out of previous defined rules. Both, events and rules, are saved
 *         within a MYSQL database with a scheme for RDF-storage like used by
 *         the CHE. The context-events are saved by the CHE itself. All
 *         additional needed elements for the reasoner in the database can be
 *         created with the script available in the resources folder of this
 *         project. First of interest here are two tables:
 *         <ul>
 *         <li>Situation</li>
 *         <li>Situation_Index</li>
 *         </ul>
 *         Here the first one contains the rules in form of SPARQL queries. The
 *         second one contains statements that are used to detect events that
 *         will trigger a query (where id of the query is a foreign key in
 *         Situation_Index).<br/>
 *         One additional important element of the Reasoner is a plug-in for
 *         MYSQL. This is mainly a short C based DLL (source is available in
 *         src/main/c) that opens a socket and pushes a passed string.<br>
 *         Triggers are used to call a stored-procedure
 *         "examine_situation_index" every time a new event is
 *         inserted/deleted/updated in the CHE. This checks if the event matches
 *         to one of the statements in "Situation_index". If this is fulfilled
 *         it send the according query out of table "Situation" to the reasoner
 *         (using the plug-in). The Reasoner finally reads out this query and
 *         push the resulting statements to the context bus.<br>
 * <br>
 *         The rules in table "Situation" are SPAQRL queries. In the current
 *         version of the Reasoner there are restrictions how this query have to
 *         be designed. First it must contain a SPARQL "CONSTRUCT". The result
 *         need to have exact one root element (the one where no other statement
 *         is pointing on), where several predicates are allowed. To give an
 *         easy example we imagine several different sensors that pushes
 *         context-events. Now we create a query that read out from all
 *         measurement events that a user is 1. in the living room and 2.
 *         currently reading a book. So we have one root (the user) and two
 *         predicates (inLivingroom, isReading) and therefore two events.
 * 
 */
public class Activator extends Thread implements BundleActivator {

	/**
	 * The properties file is mainly used to save connect information to the
	 * database and config times for refreshment of the needed data.
	 */
	public static final String PROPS_FILE = "CHe.properties";
	private static File confHome = new File(new File(Constants
			.getSpaceConfRoot()), "ctxt.che");

	static final String JENA_DB_URL = getProperties().getProperty("DB.URL",
			"jdbc:mysql://localhost:3306/universaal_history");
	static final String JENA_DB_USER = getProperties().getProperty("DB.USER",
			"uaal_ctxt_sr");
	static final String JENA_DB_PASSWORD = getProperties().getProperty(
			"DB.PWD", "uaal_ctxt_sr");
	static final String JENA_MODEL_NAME = getProperties().getProperty(
			"MODEL.NAME", "universAAL_Context_History");

	public static final String uAAL_SITUATION_REASONER_NAMESPACE = Resource.uAAL_NAMESPACE_PREFIX
			+ "GenericReasoner.owl#";

	private int port = 3309; // Port for MySQL plugin with default value 3309
	private ContextPublisher cp; // Standard context-publisher
	private JenaConverter mc; // Jena interface
	private boolean continueListening; // used to control the runtime of the
	// listener thread

	private final static Logger logger = LoggerFactory
			.getLogger(Activator.class);

	/**
	 * 
	 * @author amarinc
	 * 
	 *         This internal class is used to handle queries that are passed to
	 *         the Reasoner and publish resulting events on the context-bus.
	 * 
	 */
	private class QueryHandler extends Thread {
		private Socket s;

		QueryHandler(Socket s) {
			this.s = s;
		}

		/**
		 * Take an object based on Resource and create a list with all of its
		 * properties (except of its type).
		 * 
		 * @param pr
		 *            Resource the properties should be listed
		 * @return List of all properties except of the type
		 */
		private ArrayList<String> getPreds(Resource pr) {
			ArrayList<String> result = new ArrayList<String>();
			for (Enumeration<?> e = pr.getPropertyURIs(); e.hasMoreElements();) {
				String uri = e.nextElement().toString();
				if (!Resource.PROP_RDF_TYPE.equals(uri))
					result.add(uri);
			}
			return result;
		}

		/**
		 * One thread is started for every query passed to the reasoner. First a
		 * connection to JenaDB is established and then the query will be read
		 * out of the socket.
		 */
		public void run() {
			logger.info("Starting the Query handler thread");
			InputStream is = null;
			try {
				// Open a connection to the DB
				DBConnection conn = new DBConnection(JENA_DB_URL, JENA_DB_USER,
						JENA_DB_PASSWORD, "MySQL");
				logger.debug("Connected to DB");

				// Ensure that JenaDB is active
				if (conn.containsModel(JENA_MODEL_NAME)) {
					// Connect to JenaDB Model
					ModelRDB CHModel = ModelRDB.open(conn, JENA_MODEL_NAME);
					logger.debug("Connected to model");

					// Get the query-string from the socket
					StringWriter sw = new StringWriter(2048);
					is = s.getInputStream();
					for (int i = 0; i < 2048 && is.available() > 0; i++)
						sw.append((char) is.read());
					String queryStr = sw.toString();

					// create and perform the query on the DB
					Query query = QueryFactory.create(queryStr);
					logger.info("Checking stored query: {}", query.toString());
					QueryExecution qexec = QueryExecutionFactory.create(query,
							CHModel);
					Model m = qexec.execConstruct();

					// using JenaConverter to make a Resource object out of the
					// root element of the model (see descriptions at the
					// beginning) --> convert from JenaOWL to uAAL OWL-Java
					// representation.
					Resource pr = mc.toPersonaResource(mc
							.getJenaRootResource(m));

					if (pr != null) {
						logger.info("Publishing events on: {}", pr.getURI());
						// Now publish a context-event for every subject (must
						// be exact one) / predicate pair
						for (String pred : getPreds(pr)) {
							logger.info("{} = {}", pred, pr.getProperty(pred));
							ContextEvent cev = new ContextEvent(pr, pred);
							cp.publish(cev);
						}
						logger.info("Event publishing finished!");
					}
					// Finally clear up
					qexec.close();
					CHModel.close();
				}
				logger.debug("Clossing connection to DB");
				conn.close();
			} catch (Exception e) {
				logger.warn("A query string could not be processed: {}", e);
			} finally {
				if (is != null) {
					try {
						is.close();
					} catch (Exception e2) {
						logger.warn(
								"A query input stream could not be closed: {}",
								e2);
					}
				}
				if (s != null && !s.isClosed()) {
					try {
						s.close();
					} catch (Exception e2) {
						logger.warn(
								"A query input socket could not be closed: {}",
								e2);
					}
				}
			}
		}
	}

	/**
	 * Standard start-method of the BundleActivator interface. Here used to get
	 * the port of MySQL, get the JenaDB interface class, create the
	 * ContextPublisher and start the listening for queries.
	 */
	public void start(BundleContext context) throws Exception {
		// get MySQL port
		String portParam = System
				.getProperty("org.universAAL.ctxt.sr.server_port");
		if (portParam != null) {
			try {
				port = Integer.parseInt(portParam);
			} catch (Exception e) {
			}
		}

		// Get JenaDB service
		mc = (JenaConverter) context.getService(context
				.getServiceReference(JenaConverter.class.getName()));

		// prepare for context publishing
		ContextProvider info = new ContextProvider(
				uAAL_SITUATION_REASONER_NAMESPACE + "genReasoner");
		info.setType(ContextProviderType.reasoner);
		cp = new DefaultContextPublisher(context, info);

		// start listening
		continueListening = true;
		start();

	}

	/**
	 * Run method of the Reasoner listening thread. First checks if MySQl is
	 * reachable and connect per socket to the MySQL plug-in. Then starts
	 * listening to incoming queries and starts a new QueryHandler for everyone.
	 */
	public void run() {
		logger.info("Starting the Reasoner thread");
		ServerSocket serverSocket = null;
		try {
			// Check DB-Driver
			Driver dr = new Driver();
			if (dr == null)
				logger
						.warn("No instance of the mysql jdbc driver could be built!");
			// Create socket connection to the MySQL plug-in
			serverSocket = new ServerSocket(port, 5);
			synchronized (this) {
				notify();
			}

			// start an infinite loop that always blocks by using
			// serverSocket.accept() until a new query comes in and endsafter
			// the bundle stops (continueListening is false).
			while (continueListening) {
				try {
					new QueryHandler(serverSocket.accept()).start();
				} catch (Exception e1) {
					logger
							.warn(
									"Retrying the Situation Reasoner DB listener due to: {}",
									e1);
				}
			}
		} catch (Exception e) {
			logger.error("The Situation Reasoner DB listener interrupted: {}",
					e);
		} finally {
			if (serverSocket != null)
				try {
					serverSocket.close();
				} catch (Exception e2) {
					logger
							.warn(
									"Problems while trying to close the server socket: {}",
									e2);
				}
		}
	}

	/**
	 * Standard stop-method of the BundleActivator interface that is here used
	 * to stop the Reasoner thread
	 */
	public void stop(BundleContext arg0) throws Exception {
		continueListening = false;
	}

	/**
	 * Gets the properties of the CHe
	 * 
	 * @return The properties of the CHe
	 * @see #setProperties(Properties)
	 */
	public static synchronized Properties getProperties() {
		Properties prop = new Properties();
		try {
			prop = new Properties();
			InputStream in = new FileInputStream(new File(confHome, PROPS_FILE));
			prop.load(in);
			in.close();
		} catch (Exception e) {
			logger.error("Could not access properties file: {} " + e);
		}
		return prop;
	}

}
