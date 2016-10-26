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
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Timer;

import org.universAAL.context.che.database.Backend;
import org.universAAL.context.che.database.Cleaner;
import org.universAAL.middleware.container.ModuleActivator;
import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.container.utils.LogUtils;
import org.universAAL.middleware.context.ContextEvent;
import org.universAAL.middleware.owl.Ontology;
import org.universAAL.middleware.owl.OntologyManagement;
import org.universAAL.middleware.serialization.MessageContentSerializer;
import org.universAAL.middleware.util.OntologyListener;

/**
 * Central class that takes care of starting and stopping application. It used
 * to be the Activator class before splitting the OSGi logic.
 * 
 * @author alfiva
 * 
 */
public class Hub implements OntologyListener, ModuleActivator {
    /**
     * Logger.
     */
    private static Log log = Hub.getLog(Hub.class);

    /**
     * Name of the config properties file.
     */
    public static final String PROPS_FILE = "CHe.properties";
    /**
     * This is prepended to the above file.
     */
    public static final String COMMENTS = "This file stores configuration "
	    + "parameters for the Context History Entrepot";

    /**
     * Milliseconds in 24 hours.
     */
    private static final long HOURS24 = 86400000;
    /**
     * Config folder.
     */
    private static File confHome;
    /**
     * uAAL Module context.
     */
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
     * Turtle-uAAL parser.
     */
    private static MessageContentSerializer uAALParser;

    /**
     * Flag for knowing when store is connected, used only for ontology updates
     */
    private boolean connected = false;

    /**
     * Default constructor.
     */
    public Hub(File configHome) {
    	Hub.confHome = configHome;
	// Instantiate the store you want
	try {
	    String storeclass = getProperties().getProperty("STORE.IMPL",
		    "org.universAAL.context.che.database.impl.SesameBackend");
	    this.db = (Backend) Class.forName(storeclass)
		    .getConstructor(new Class[] {})
		    .newInstance(new Object[] {});
	} catch (RuntimeException ex){
	    ex.printStackTrace();
	} catch (Exception e) {
	    // If we cannot get the Backend, abort.
	    String cause = "The store implementation passed as configuration"
		    + " parameter could not be used. Make sure it is a "
		    + "class that implements "
		    + "org.universAAL.context.che.database.Backend or "
		    + "remove that configuration parameter to use the "
		    + "default engine.";
	    log.error("init", cause);
	}	
    }
    
    /**
     * To be called when application starts. Used to be Activator.start().
     * 
     * @param context
     *            uaal module context
     */
    public void start(ModuleContext context) {
	moduleContext = context;
	confHome = moduleContext.getConfigHome();
	createOWLFiles();
	// Start the store and wrappers
	this.db.connect();
	this.connected=true;
	//connect before listening, otherwise we may miss onts (but only 1st execution)
	OntologyManagement.getInstance().addOntologyListener(context, this);
	this.hc = new ContextHistorySubscriber(moduleContext, db);
	this.chc = new ContextHistoryCallee(moduleContext, db);
	// Every 24 hours do the "Cleaner thing" (see Cleaner class)
	t = new Timer();
	t.scheduleAtFixedRate(new Cleaner(db), HOURS24, HOURS24);
	log.info("start", "Removal period will be checked"
		+ " in 24 hours from now");
	// Sync Mobile Events. When platform can, sync when mobile arrives
	log.debug("start", "Looking for mobile events to synchronize");
	if (synchronizeMobileTurtle()) {
	    log.info("start", "Synchronized Mobile Events!!!");
	} else {
	    log.warn("start", "Could not Synchronize Mobile Events!!!");
	}
    }

    /**
     * Create the OWL files for the registered ontologies, and put them in the
     * config folder.
     */
    private synchronized void createOWLFiles() {
	OntologyManagement manager = OntologyManagement.getInstance();
	String[] ontURIs = manager.getOntoloyURIs();
	for (int i = 0; i < ontURIs.length; i++) {
	    createOWLFile(ontURIs[i],manager);
	}
    }
    
    /**
     * Create the OWL file for a given ontology, and put it in the config
     * folder. If STORE.OVERWRITEOWL=true, the OWL file will be overwritten if it
     * already exists. If false nothing happens.
     * @return 
     */
    private synchronized String createOWLFile(String ontURI,OntologyManagement manager) {
	Boolean overwrite = Boolean.parseBoolean(Hub.getProperties().getProperty(
		"STORE.OVERWRITEOWL", "false"));
	String filename = ontURI.replaceAll("[:/#]", ".");
	if (!filename.endsWith(".owl")) {
	    filename += ".owl";
	}
	File destination = new File(confHome, filename);
	if (destination.exists()) {
	    if (overwrite) {
		if (!destination.delete()) {//TODO Delete triples from store too?
		    log.warn(
			    "createOWLFile",
			    "Could not replace old version of "
				    + ontURI
				    + " OWL file with new one. "
				    + "If there are no changes on it, it is OK. "
				    + "Otherwise take into account that the old "
				    + "version is still loaded, you will need to "
				    + "remove it manually.");
		    return null;
		}
	    } else {
		log.info("createOWLFile", "The OWL file for " + ontURI
			+ " alreday exists. Skipping its creation.");
		return null;
	    }
	}
	try {
	    BufferedWriter out = new BufferedWriter(new FileWriter(destination, false));
	    Ontology ont = manager.getOntology(ontURI);
	    String str = this.uAALParser.serialize(ont);
	    out.write(str);
	    out.close();
	} catch (IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
	return destination.getName();
    }

    /**
     * To be called when application stops. Used to be Activator.stop().
     * 
     * @throws Exception
     */
    public final void stop(ModuleContext mc) throws Exception {
	// Stop the store and wrappers
	if(moduleContext!=null){
	    OntologyManagement.getInstance().removeOntologyListener(moduleContext, this);
	}
	this.chc.close();
	this.hc.close();
	this.db.close();
	this.connected=false;
    }

    /**
     * Set the turtle-uaal parser. Make sure it's set at least once before
     * start().
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
	    if (!confHome.exists()) {
		if(!confHome.mkdir()){
		    log.error("setproperties", "Could not set properties file");
		}
	    }
	    out = new FileWriter(new File(confHome, PROPS_FILE));
	    prop.store(out, COMMENTS);
	    out.close();
	} catch (Exception e) {
	    log.error("setproperties", "Could not set properties file: {} ", e);
	}
    }

    private static Properties getSafeProperties(){
    	Properties prop = new Properties();
	    prop.setProperty("STORE.IMPL",
			    "org.universAAL.context.che.database.impl.SesameBackend");
	    prop.setProperty("MOBILE.FILE", "Mobile-Events.txt");
	    prop.setProperty("MOBILE.FLAG", "<!--CEv-->");
	    prop.setProperty("RECYCLE.KEEP", "2"); // 2 months
	    prop.setProperty("RECYCLE.DATE", "0"); // right now
	    prop.setProperty("RECYCLE.HOUR", "22"); // at 22:00
    	return prop;
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
    		InputStream in = new FileInputStream(new File(confHome, PROPS_FILE));
    		prop.load(in);
    		in.close();
    	} catch (java.io.FileNotFoundException e) {
    		log.warn("start",
    				"Properties file does not exist; generating default...");
    		prop = getSafeProperties();
    		try {
				prop.setProperty("STORE.LOCATION", confHome.getAbsolutePath()
						+ "/store");
				setProperties(prop);
			} catch (Exception e1) {
	    		prop.setProperty("STORE.LOCATION", "./store");
			}
    	} catch (Exception e) {
    		log.error("getproperties", "Could not access properties file: {} ",
    				e);
    		prop = getSafeProperties();
    		prop.setProperty("STORE.LOCATION", "./store");
    	}
    	return prop;
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
		StringBuffer turtleIn=new StringBuffer();
		int count = 0;
		long start = System.currentTimeMillis();
		File fileref = new File(
			confHome,
			fileMobile);
		BufferedReader br = new BufferedReader(new FileReader(fileref));
		readline = br.readLine();
		while (readline != null) {
		    while (readline != null && !readline.equals(flag)) {
			turtleIn.append(readline);
			readline = br.readLine();
		    }
		    if (turtleIn.length()>0) {
			ev = (ContextEvent) uAALParser.deserialize(turtleIn.toString());
			if (lKO < ev.getTimestamp().longValue()) {
			    log.debug("synchronizeMobileTurtle",
				    "Parsed an event from Mobile "
					    + "file, storing in DB");
			    this.db.storeEvent(ev);
			    count++;
			}
		    }
		    turtleIn =new StringBuffer();
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
		log.warn(
			"synchronizeMobileTurtle",
			"Could not find the Mobile events file,"
				+ " synchronization will not take place: "
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
     * Gets a Log helper class.
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
	/**
	 * Custom log helper.
	 */
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

    public void ontologyAdded(String ontURI) {
	String filename=createOWLFile(ontURI,OntologyManagement.getInstance());
	if (this.connected && filename!=null) {
	    try {
		this.db.populate(filename);
	    } catch (Exception e) {
		log.error("ontologyAdded",
			"Exception updating the store with new ontologies ", e);
		e.printStackTrace();
	    }
	}
    }

    public void ontologyRemoved(String ontURI) {
	// Do nothing, I cant just remove an owl from the backend 
    }
    
    public static File getConfigHome(){
    	return confHome;
    }
    
    static MessageContentSerializer getSerializer(){
    	return uAALParser;
    }
}
