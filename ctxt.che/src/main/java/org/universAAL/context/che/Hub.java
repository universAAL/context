/*
	Copyright 2008-2014 ITACA-TSB, http://www.tsb.upv.es
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
package org.universAAL.context.che;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.util.Properties;
import java.util.Timer;

import org.universAAL.context.che.database.Backend;
import org.universAAL.context.che.database.Cleaner;
import org.universAAL.context.che.database.impl.SesameBackend;
import org.universAAL.context.che.ontology.ContextHistoryOntology;
import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.container.osgi.util.BundleConfigHome;
import org.universAAL.middleware.container.utils.LogUtils;
import org.universAAL.middleware.context.ContextEvent;
import org.universAAL.middleware.owl.OntologyManagement;
import org.universAAL.middleware.sodapop.msg.MessageContentSerializer;

public class Hub {
    private final static Log log = Hub.getLog(SesameBackend.class);

    public static final String PROPS_FILE = "CHe.properties";
    public static final String COMMENTS = "This file stores configuration "
	    + "parameters for the " + "Context History Entrepot";

    private static File confHome = new File(
	    new BundleConfigHome("ctxt.che").getAbsolutePath());
    private static ModuleContext moduleContext = null;

    private Backend db;
    private ContextHistorySubscriber HC;
    private ContextHistoryCallee CHC;
    private Timer t;
    private ContextHistoryOntology ontology = new ContextHistoryOntology();
    private Object fileLock = new Object();
    protected MessageContentSerializer uAALParser;

    public void start(ModuleContext context) throws Exception {
	moduleContext = context;
	// Register ont
	OntologyManagement.getInstance().register(ontology);
	// Start the store you want
	try {
	    String storeclass = getProperties().getProperty("STORE.IMPL",
		    "org.universAAL.context.che.database.impl.SesameBackend");
	    this.db = (Backend) Class.forName(storeclass)
		    .getConstructor(new Class[] {})
		    .newInstance(new Object[] {});
	} catch (Exception e) {
	    // If we cannot get the Backend, abort.
	    String cause = "The store implementation passed as configuration"
		    + " parameter could not be used. Make sure it is a "
		    + "class that implements "
		    + "org.universAAL.context.che.database.Backend or "
		    + "remove that configuration parameter to use the "
		    + "default engine.";
	    throw new Exception(cause, e);// TODO: Create a new kind of
					  // exception?
	}
	this.db.connect();
	// Start the wrappers
	this.HC = new ContextHistorySubscriber(moduleContext, db);
	this.CHC = new ContextHistoryCallee(moduleContext, db);
	// Start the removal timer
	t = new Timer();
	// Every 24 hours do the "Cleaner thing" (see Cleaner class)
	t.scheduleAtFixedRate(new Cleaner(db), 86400000, 86400000);
	log.info("start", "Removal period will be checked in 24 hours from now");
    }

    public final void stop() throws Exception {
	// Stop the store and wrappers and deregister ont
	this.db.close();
	this.CHC.close();
	this.HC.close();
	OntologyManagement.getInstance().unregister(ontology);
    }

    public void setuAALParser(MessageContentSerializer service) {
	this.uAALParser = service;
	this.db.setuAALParser(service);
    }

    /**
     * Sets the properties of the CHe.
     * 
     * @param prop
     *            The Properties object containing ALL of the properties of the
     *            CHe
     * @see #getProperties()
     */
    public static synchronized void setProperties(final Properties prop) {
	try {
	    FileWriter out;
	    out = new FileWriter(new File(confHome, PROPS_FILE));
	    prop.store(out, COMMENTS);
	    out.close();
	} catch (Exception e) {
	    log.error("setproperties", "Could not set properties file: {} ", e);
	}
    }

    /**
     * Gets the properties of the CHe.
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
	} catch (java.io.FileNotFoundException e) {
	    log.warn("start",
		    "Properties file does not exist; generating default...");
	    prop.setProperty("STORE.IMPL",
		    "org.universAAL.context.che.database.impl.SesameBackend");
	    prop.setProperty("MOBILE.FILE", "Mobile-Events.txt");
	    prop.setProperty("MOBILE.FLAG", "<!--CEv-->");
	    prop.setProperty("RECYCLE.KEEP", "2"); // 2 months
	    prop.setProperty("RECYCLE.DATE", "0"); // right now
	    prop.setProperty("RECYCLE.HOUR", "22"); // at 22:00
	    setProperties(prop);
	} catch (Exception e) {
	    log.error("getproperties", "Could not access properties file: {} ",
		    e);
	}
	return prop;
    }

    public void synchronizeMobile() {
	// Sync Mobile Events. When platform can, sync when mobile arrives
	log.debug("start", "Looking for mobile events to synchronize");
	if (synchronizeMobileTurtle()) {
	    log.info("start", "Synchronized Mobile Events!!!");
	} else {
	    log.warn("start", "Could not Synchronize Mobile Events!!!");
	}
    }

    /**
     * Performs the synchronization with the history gathered by the Mobile
     * device while outside. It only works if the file from the Mobile
     * containing the history has already been downloaded to the CHE
     * configuration.
     * 
     * @return <code>true</code> if the synchronization succeeded.
     */
    private boolean synchronizeMobileTurtle() {
	log.info("synchronizeMobileTurtle",
		"Synchronizing with Mobile events - Parsed");
	ContextEvent ev = null;
	synchronized (fileLock) {
	    try {
		String lastKnownOf = Hub.getProperties().getProperty(
			"MOBILE.LAST", "0");
		String fileMobile = Hub.getProperties().getProperty(
			"MOBILE.FILE", "Mobile-Events.txt");
		String flag = Hub.getProperties().getProperty("MOBILE.FLAG",
			"<!--CEv-->");
		long lKO = Long.parseLong(lastKnownOf);
		log.info("synchronizeMobileTurtle",
			"Mobile events were last synchronized in "
				+ lastKnownOf);
		String readline = "";
		String turtleIn = "";
		int count = 0;
		long start = System.currentTimeMillis();
		File fileref = new File(
			new BundleConfigHome("ctxt.che").getAbsolutePath(),
			fileMobile);
		BufferedReader br = new BufferedReader(new FileReader(fileref));
		readline = br.readLine();
		while (readline != null) {
		    while (readline != null && !readline.equals(flag)) {
			turtleIn += readline;
			readline = br.readLine();
		    }
		    if (!turtleIn.isEmpty()) {
			ev = (ContextEvent) uAALParser.deserialize(turtleIn);
			if (lKO < ev.getTimestamp().longValue()) {
			    log.debug("synchronizeMobileTurtle",
				    "Parsed an event from Mobile file, storing in DB");
			    this.db.storeEvent(ev);
			    count++;
			}
		    }
		    turtleIn = "";
		    readline = br.readLine();
		}
		if (ev != null) {
		    Properties props = Hub.getProperties();
		    props.setProperty("MOBILE.LAST", ev.getTimestamp()
			    .toString());
		    Hub.setProperties(props);
		    log.info("synchronizeMobileTurtle",
			    "Synchronized " + count + " Mobile events up to "
				    + ev.getTimestamp().toString() + ", in "
				    + (System.currentTimeMillis() - start)
				    / 1000 + " seconds.");
		}
		br.close();
		if (!fileref.delete()) {
		    log.warn("synchronizeMobileTurtle",
			    "Could not delete the Mobile events file");
		}
	    } catch (FileNotFoundException e) {
		log.error("synchronizeMobileTurtle",
			"Could not find the Mobile events file, synchronization will not take place: "
				+ e.getMessage());
		return false;
	    } catch (Exception e) {
		log.error("synchronizeMobileTurtle",
			"Error processing the Mobile events file:", e);
		return false;
	    }
	}
	return true;
    }

    public static Log getLog(Class cl) {
	return new Log(cl);
    }

    public static class Log {
	private Class logclass;

	public Log(Class cl) {
	    this.logclass = cl;
	}

	public void info(String method, String msg) {
	    LogUtils.logInfo(moduleContext, logclass, method,
		    new Object[] { msg }, null);
	}

	public void debug(String method, String msg) {
	    LogUtils.logDebug(moduleContext, logclass, method,
		    new Object[] { msg }, null);
	}

	public void warn(String method, String msg) {
	    LogUtils.logWarn(moduleContext, logclass, method,
		    new Object[] { msg }, null);
	}

	public void error(String method, String msg) {
	    LogUtils.logError(moduleContext, logclass, method,
		    new Object[] { msg }, null);
	}

	public void info(String method, String msg, Throwable e) {
	    LogUtils.logInfo(moduleContext, logclass, method,
		    new Object[] { msg }, e);
	}

	public void debug(String method, String msg, Throwable e) {
	    LogUtils.logDebug(moduleContext, logclass, method,
		    new Object[] { msg }, e);
	}

	public void warn(String method, String msg, Throwable e) {
	    LogUtils.logWarn(moduleContext, logclass, method,
		    new Object[] { msg }, e);
	}

	public void error(String method, String msg, Throwable e) {
	    LogUtils.logError(moduleContext, logclass, method,
		    new Object[] { msg }, e);
	}
    }
}
