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

import java.io.InputStream;
import java.io.StringWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Enumeration;

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
 */
public class Activator extends Thread implements BundleActivator {

	static final String JENA_DB_URL = System.getProperty("org.persona.platform.jena_db.url",
			"jdbc:mysql://localhost:3306/persona_aal_space");
	static final String JENA_DB_USER = System.getProperty("org.persona.platform.casf.sr.db_user", "casf_sr");
	static final String JENA_DB_PASSWORD = System.getProperty("org.persona.platform.casf.sr.db_passwd", "casf_sr");
	static final String JENA_MODEL_NAME = System.getProperty("org.persona.platform.jena_db.model_name", "PERSONA_AAL_Space");
	
//	private class SRTest extends Thread {
//		public void run() {
//			try {
//				DBConnection conn=new DBConnection(
//						JENA_DB_URL,
//						JENA_DB_USER,
//						JENA_DB_PASSWORD,
//						"MySQL"); 
//				if (conn.containsModel(JENA_MODEL_NAME)) {
//					ModelRDB CHModel = ModelRDB.open(conn, JENA_MODEL_NAME);
//					PResource lr = new PResource("urn:org.persona.aal_space:itaca_home_lab#livingRoom");
//					PResource os = new PResource("urn:org.persona.aal_space:itaca_home_lab#outside");
//					PResource at = new PResource();
//					at.addType("http://ontology.persona.ratio.it/action.owl#ActionType", true);
//					at.setProperty("http://ontology.persona.ratio.it/action.owl#description", "WatchingTV");
//					PResource ca = new PResource();
//					ca.addType("http://ontology.persona.ratio.it/action.owl#CompositeAction", true);
//					ca.setProperty("http://ontology.persona.ratio.it/action.owl#hasStatus", new Integer(1));
//					ca.setProperty("http://ontology.persona.ratio.it/action.owl#hasActionType", at);
//					PResource user = new PResource("urn:org.aal_persona.aal_space:itaca_living_lab#ella");
//					user.addType("http://ontology.persona.upm.es/User.owl#User", true);
//					user.setProperty(PhysicalThing.PROP_PHYSICAL_LOCATION, lr);
//					user.setProperty("http://ontology.persona.upm.es/User.owl#currentActivity", ca);
//					CHModel.add(mc.toJenaResource(user).getModel());
//					lr.setProperty("http://ontology.aal-persona.org/PERSONA.owl#hasTemperature", new Integer(18));
//					CHModel.add(mc.toJenaResource(lr).getModel());
//					os.setProperty("http://ontology.aal-persona.org/PERSONA.owl#hasTemperature", new Integer(8));
//					CHModel.add(mc.toJenaResource(os).getModel());
//					CHModel.close();
//				}
//				conn.close();
//			} catch (Exception e) {
//				logger.warn("A query string could not be processed: {}", e);
//			} finally {
//			}
//		}
//	}
	
	public static final String PERSONA_SITUATION_REASONER_NAMESPACE = 
		Resource.uAAL_NAMESPACE_PREFIX + "SituationReasoner.owl#";
	
	private class QueryHandler extends Thread {
		private Socket s;
		
		QueryHandler(Socket s) {
			this.s = s;
		}
		
		private ArrayList<String> getPreds(Resource pr) {
			ArrayList<String> result = new ArrayList<String>();
			for (Enumeration<?> e=pr.getPropertyURIs(); e.hasMoreElements();) {
				String uri = e.nextElement().toString();
				if (!Resource.PROP_RDF_TYPE.equals(uri))
					result.add(uri);
			}
			return result;
		}
		
		public void run() {
			InputStream is = null;
			try {
				DBConnection conn=new DBConnection(
						JENA_DB_URL,
						JENA_DB_USER,
						JENA_DB_PASSWORD,
						"MySQL"); 
				if (conn.containsModel(JENA_MODEL_NAME)) {
					ModelRDB CHModel = ModelRDB.open(conn, JENA_MODEL_NAME);

					StringWriter sw = new StringWriter(2048);
					is = s.getInputStream();
					for (int i=0; i<2048 && is.available() > 0; i++)
						sw.append((char) is.read());

					String queryStr = sw.toString();
					logger.info("Received notification: {}", queryStr);
					
					Query query = QueryFactory.create(queryStr);
					QueryExecution qexec = QueryExecutionFactory.create(query, CHModel) ;
					Model m = qexec.execConstruct();
					Resource pr = mc.toPersonaResource(mc.getJenaRootResource(m));
					if (pr != null) {
						logger.info("Publishing events on: {}", pr.getURI());
						for (String pred : getPreds(pr)) {
							logger.info("{} = {}", pred, pr.getProperty(pred));
							cp.publish(new ContextEvent(pr, pred));
						}
						logger.info("Event publishing finished!");
					}
					qexec.close();
					CHModel.close();
				}
				conn.close();
			} catch (Exception e) {
				logger.warn("A query string could not be processed: {}", e);
			} finally {
				if (is != null) {
					try {
						is.close();
					} catch (Exception e2) {
						logger.warn("A query input stream could not be closed: {}", e2);
					}
				}
				if (s != null && !s.isClosed()) {
					try {
						s.close();
					} catch (Exception e2) {
						logger.warn("A query input socket could not be closed: {}", e2);
					}
				}
			}
		}
	}
	
	private int port = 3309;
	private ContextPublisher cp;
	private JenaConverter mc;
	private Logger logger = LoggerFactory.getLogger(Activator.class);
	private boolean continueListening;
	
	public void start(BundleContext context) throws Exception {
		
		String portParam = System.getProperty("org.persona.platform.casf.sr.server_port");
		if (portParam != null) {
			try {
				port = Integer.parseInt(portParam);
			} catch (Exception e) {}
		}
		
		mc = (JenaConverter) context.getService(context.getServiceReference(JenaConverter.class.getName()));

		// prepare for context publishing
		ContextProvider info =  new ContextProvider(
				PERSONA_SITUATION_REASONER_NAMESPACE + "situReasoner");
		info.setType(ContextProviderType.reasoner);
		cp = new DefaultContextPublisher(context, info);
	
		continueListening = true;
		start();
		
		// test
		// new SRTest().start();
	}

	public void run() {
		ServerSocket serverSocket = null;
		try {
			Driver dr = new Driver();
			if (dr == null)
				logger.warn("No instance of the mysql jdbc driver could be built!");
			serverSocket = new ServerSocket(port, 5);
			synchronized (this) {
				notify();
			}
			while (continueListening) {
				try {
					new QueryHandler(serverSocket.accept()).start();
				} catch (Exception e1) {
					logger.warn("Retrying the Situation Reasoner DB listener due to: {}", e1);
				}
			}
		} catch (Exception e) {
			logger.error("The Situation Reasoner DB listener interrupted: {}", e);
		} finally {
			if (serverSocket != null)
				try {
					serverSocket.close();
				} catch (Exception e2) {
					logger.warn("Problems while trying to close the server socket: {}", e2);
				}
		}
	}
	
	public void stop(BundleContext arg0) throws Exception {
		continueListening = false;
	}

}
