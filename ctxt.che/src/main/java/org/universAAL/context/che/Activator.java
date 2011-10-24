/*
	Copyright 2008-2011 ITACA-TSB, http://www.tsb.upv.es
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Properties;
import java.util.Timer;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.universAAL.context.che.database.Backend;
import org.universAAL.context.che.database.Cleaner;
import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.container.osgi.uAALBundleContainer;
import org.universAAL.middleware.container.osgi.util.BundleConfigHome;
import org.universAAL.middleware.sodapop.msg.MessageContentSerializer;

/**
 * @author <a href="mailto:alfiva@itaca.upv.es">Alvaro Fides Valero</a>
 * 
 */
public class Activator implements BundleActivator, ServiceListener {

    public static final String PROPS_FILE = "CHe.properties";
    public static final String COMMENTS = "This file stores configuration parameters for the "
	    + "Context History Entrepot";
    private static File confHome = new File(new BundleConfigHome("ui.handler.gui").getAbsolutePath());

    private final static Logger log = LoggerFactory.getLogger(Activator.class);
    public static BundleContext context = null;
    private static ModuleContext moduleContext=null;
    private Backend db;
    private ContextHistorySubscriber HC;
    private ContextHistoryCallee CHC;
    private Timer t;

    public void start(BundleContext context) throws Exception {
	Activator.context = context;
	
	//Start the store you want
	try {
	    String storeclass = getProperties().getProperty("STORE.IMPL",
		    "org.universAAL.context.che.database.impl.SesameBackend");
	    this.db = (Backend) Class.forName(storeclass)
		    .getConstructor(new Class[] {}).newInstance(new Object[]{});
	} catch (Exception e) {
	    //If we cannot get the Backend, abort.
	    String cause="The store implementation passed as configuration parameter could not be used. "
		    + "Make sure it is a class that implements org.universAAL.context.che.database.Backend "
		    + "or remove that configuration parameter to use the default engine.";
	    throw new Exception(cause,e);//TODO: Create a new kind of exception?
	}
	this.db.connect();
	
	//Look for MessageContentSerializer of mw.data.serialization
	String filter = "(objectclass=" + MessageContentSerializer.class.getName() + ")";
	context.addServiceListener(this, filter);
	ServiceReference references[] = context.getServiceReferences(null,
		filter);
	for (int i = 0; references != null && i < references.length; i++)
	    this.serviceChanged(new ServiceEvent(ServiceEvent.REGISTERED,
		    references[i]));
	
	//Start uAAL wrappers
	Activator.moduleContext = uAALBundleContainer.THE_CONTAINER
		.registerModule(new Object[] { context });
	this.HC = new ContextHistorySubscriber(moduleContext, db);
	this.CHC = new ContextHistoryCallee(moduleContext, db);
	
	//Start the removal timer
	t = new Timer();
	long tst = Long.parseLong(getProperties().getProperty(
		"RECYCLE.PERIOD_MSEC"));
	t.scheduleAtFixedRate(new Cleaner(db), tst, tst);
	log.info("Removal scheduled for {} ms with a periodicity of {} ms ",
		new Object[] {
			Long.toString(Calendar.getInstance().getTimeInMillis()
				+ tst), Long.toString(tst) });
    }

    public void stop(BundleContext context) throws Exception {
	// Stop the store and wrappers
	this.db.close();
	this.CHC.close();
	this.HC.close();
    }

    public void serviceChanged(ServiceEvent event) {
	//Update the MessageContentSerializer
	switch (event.getType()) {
	case ServiceEvent.REGISTERED:
	case ServiceEvent.MODIFIED:
	    this.db.setuAALParser((MessageContentSerializer) context.getService(event.getServiceReference()));
	    break;
	case ServiceEvent.UNREGISTERING:
	    this.db.setuAALParser(null);
	    break;
	}
    }

    /**
     * Sets the properties of the CHe
     * 
     * @param prop
     *            The Properties object containing ALL of the properties of the
     *            CHe
     * @see #getProperties()
     */
    public static synchronized void setProperties(Properties prop) {
	try {
	    FileWriter out;
	    out = new FileWriter(new File(confHome, PROPS_FILE));
	    prop.store(out, COMMENTS);
	    out.close();
	} catch (Exception e) {
	    log.error("Could not set properties file: {} " + e);
	}
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
	} catch (java.io.FileNotFoundException e) {
	    log.warn("Properties file does not exist; generating default...");
	    prop.setProperty("STORE.IMPL", "org.universAAL.context.che.database.impl.SesameBackend");
	    prop.setProperty("MOBILE.FILE", "PMD-Events.txt");
	    prop.setProperty("MOBILE.FLAG", "<!--CEv-->");
	    prop.setProperty("RECYCLE.KEEP_MSEC", "15552000000");// 6 months
	    prop.setProperty("RECYCLE.PERIOD_MSEC", "5184000000");// 2 months
	    prop.setProperty("RECYCLE.HOUR", "22");// at 22:00
	    setProperties(prop);
	} catch (Exception e) {
	    log.error("Could not access properties file: {} " + e);
	}
	return prop;
    }

}
