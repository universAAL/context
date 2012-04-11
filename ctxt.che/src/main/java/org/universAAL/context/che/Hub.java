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
//import org.universAAL.context.che.ontology.ContextHistoryOntology;
import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.container.osgi.util.BundleConfigHome;
import org.universAAL.middleware.container.utils.LogUtils;
import org.universAAL.middleware.context.ContextEvent;
//import org.universAAL.middleware.owl.OntologyManagement;
import org.universAAL.middleware.sodapop.msg.MessageContentSerializer;

/**
 * Central class that takes care of starting and stopping application. It used
 * to be the Activator class before splitting the OSGi logic.
 * 
 * @author alfiva
 * 
 */
public class Hub {
    private static Log log = Hub.getLog(SesameBackend.class);

    public static final String PROPS_FILE = "CHe.properties";
    public static final String COMMENTS = "This file stores configuration "
	    + "parameters for the " + "Context History Entrepot";

    private static File confHome = new File(
	    new BundleConfigHome("ctxt.che").getAbsolutePath());
    private static ModuleContext moduleContext = null;

    /**
     * The store.
     */
    private Backend db;
    /**
     * Context subscriber.
     */
    private ContextHistorySubscriber hc;
    /**
     * Service callee.
     */
    private ContextHistoryCallee chc;
    /**
     * Timer for autoremoval.
     */
    private Timer t;
    /**
     * Lock for sync file access.
     */
    private Object fileLock = new Object();
    /**
     * Turtle-uAAL parser
     */
    private MessageContentSerializer uAALParser;

    /**
     * To be called when application starts. Used to be Activator.start().
     * 
     * @param context
     *            uaal module context
     * @throws Exception
     *             If anything goes wrong
     */
    public void start(ModuleContext context) throws Exception {
	moduleContext = context;
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
	this.hc = new ContextHistorySubscriber(moduleContext, db);
	this.chc = new ContextHistoryCallee(moduleContext, db);
	// Start the removal timer
	t = new Timer();
	// Every 24 hours do the "Cleaner thing" (see Cleaner class)
	t.scheduleAtFixedRate(new Cleaner(db), 86400000, 86400000);
	log.info("start", "Removal period will be checked in 24 hours from now");
    }

    /**
     * To be called when application stops. Used to be Activator.stop().
     * 
     * @throws Exception
     */
    public final void stop() throws Exception {
	// Stop the store and wrappers and deregister ont
	this.db.close();
	this.chc.close();
	this.hc.close();
    }

    /**
     * Set the turtle-uaal parser. Make sure it´s called after start().
     * 
     * @param service
     *            The parser
     */
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
	    if(!confHome.exists()){
		confHome.mkdir();
	    }
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
	    prop.setProperty("STORE.LOCATION", confHome.getAbsolutePath()+"/store");
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

    /**
     * Start the mobile events synchronization.
     */
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
		"Synchronizing with Mobile events");
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
		String turtleIn = "";//TODO: Use string builder
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
		log.warn("synchronizeMobileTurtle",
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

    /**
     * Gets a Log helper class
     * 
     * @param cl
     *            Class that asks for a logger
     * @return the logger
     */
    public static Log getLog(Class cl) {
	return new Log(cl);
    }

    /**
     * helper class to simplify the calls to LogUtils of MW.
     * 
     * @author alfiva
     */
    public static class Log {
	private Class logclass;

	/**
	 * Main constructor.
	 * 
	 * @param cl
	 *            Class that asks for a logger
	 */
	public Log(Class cl) {
	    this.logclass = cl;
	}

	/**
	 * LogUtils.info.
	 * 
	 * @param method
	 *            Method that logs
	 * @param msg
	 *            Message to log
	 */
	public void info(String method, String msg) {
	    LogUtils.logInfo(moduleContext, logclass, method,
		    new Object[] { msg }, null);
	}

	/**
	 * LogUtils.debug.
	 * 
	 * @param method
	 *            Method that logs
	 * @param msg
	 *            Message to log
	 */
	public void debug(String method, String msg) {
	    LogUtils.logDebug(moduleContext, logclass, method,
		    new Object[] { msg }, null);
	}

	/**
	 * LogUtils.warn.
	 * 
	 * @param method
	 *            Method that logs
	 * @param msg
	 *            Message to log
	 */
	public void warn(String method, String msg) {
	    LogUtils.logWarn(moduleContext, logclass, method,
		    new Object[] { msg }, null);
	}

	/**
	 * LogUtils.error.
	 * 
	 * @param method
	 *            Method that logs
	 * @param msg
	 *            Message to log
	 */
	public void error(String method, String msg) {
	    LogUtils.logError(moduleContext, logclass, method,
		    new Object[] { msg }, null);
	}

	/**
	 * LogUtils.info.
	 * 
	 * @param method
	 *            Method that logs
	 * @param msg
	 *            Message to log
	 * @param e
	 *            Throwable
	 */
	public void info(String method, String msg, Throwable e) {
	    LogUtils.logInfo(moduleContext, logclass, method,
		    new Object[] { msg }, e);
	}

	/**
	 * LogUtils.debug.
	 * 
	 * @param method
	 *            Method that logs
	 * @param msg
	 *            Message to log
	 * @param e
	 *            Throwable
	 */
	public void debug(String method, String msg, Throwable e) {
	    LogUtils.logDebug(moduleContext, logclass, method,
		    new Object[] { msg }, e);
	}

	/**
	 * LogUtils.warn.
	 * 
	 * @param method
	 *            Method that logs
	 * @param msg
	 *            Message to log
	 * @param e
	 *            Throwable
	 */
	public void warn(String method, String msg, Throwable e) {
	    LogUtils.logWarn(moduleContext, logclass, method,
		    new Object[] { msg }, e);
	}

	/**
	 * LogUtils.error.
	 * 
	 * @param method
	 *            Method that logs
	 * @param msg
	 *            Message to log
	 * @param e
	 *            Throwable
	 */
	public void error(String method, String msg, Throwable e) {
	    LogUtils.logError(moduleContext, logclass, method,
		    new Object[] { msg }, e);
	}
    }
}
