/*
	Copyright 2008-2010 ITACA-TSB, http://www.tsb.upv.es
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
import org.universAAL.context.che.database.Converter;
import org.universAAL.context.che.database.impl.JenaDBBackend;
import org.universAAL.context.conversion.jena.JenaConverter;
import org.universAAL.middleware.rdf.TypeMapper;


/**
 * @author <a href="mailto:alfiva@itaca.upv.es">Alvaro Fides Valero</a>
 *
 */
public class Activator implements BundleActivator, ServiceListener {
	
	public static final String PROPS_FILE="CHe.properties";
	public static final String COMMENTS="This file stores configuration parameters for the " +
			"Context History Entrepot";
	
	private final static Logger log=LoggerFactory.getLogger(Activator.class);
	public static BundleContext context=null;
	public static JenaConverter converter;
	private Backend db;
	private ContextHistorySubscriber HC;
	private ContextHistoryCallee CHC; 
	private Timer t;

	public void start(BundleContext context) throws Exception {
		Activator.context = context;
		//Converter provided by Jena Serializer must only be used once CHe realizes ontological restrictions
		//converter = (ModelConverter) context.getService(context.getServiceReference(ModelConverter.class.getName()));;
		
		//------Remove this section once CHe realizes ontological restrictions
		converter=new Converter();
		String filter = "(objectclass=" + TypeMapper.class.getName() + ")";
		context.addServiceListener(this, filter);
		ServiceReference references[] = context.getServiceReferences(null, filter);
		for (int i = 0; references != null && i < references.length; i++)
			this.serviceChanged(new ServiceEvent(ServiceEvent.REGISTERED, references[i]));
		//------
		//Use parameterized constructor to explicitly define connection params. Otherwise config file will be used
		//this.db=new JenaDBBackend("jdbc:mysql://localhost:3306/persona_aal_space","casf_che","casf_che","MySQL");
		this.db=new JenaDBBackend();
		this.HC=new ContextHistorySubscriber(context,db);
		this.CHC=new ContextHistoryCallee(context,db);
		t=new Timer();
		long tst=Long.parseLong(getProperties().getProperty("RECYCLE.PERIOD_MSEC"));
		t.scheduleAtFixedRate(new Cleaner(db), tst, tst);
		log.info("Removal scheduled for {} ms with a periodicity of {} ms ",
				new Object[] {
						Long.toString(Calendar.getInstance().getTimeInMillis()
								+ tst), Long.toString(tst) });
	}

	public void stop(BundleContext context) throws Exception {
		this.CHC.close();
		this.HC.close();
	}
	
	//------Remove this method (and implementation of ServiceListener) once CHe realizes ontological restrictions
	public void serviceChanged(ServiceEvent event) {
		switch (event.getType()) {
		case ServiceEvent.REGISTERED:
		case ServiceEvent.MODIFIED:
			((Converter) converter).setTypeMapper((TypeMapper) context.getService(event.getServiceReference()));
			break;
		case ServiceEvent.UNREGISTERING:
			((Converter) converter).setTypeMapper(null);
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
	public static synchronized void setProperties(Properties prop){
		try {
			FileWriter out;
			out = new FileWriter(PROPS_FILE);
			prop.store(out, COMMENTS);
			out.close();
		} catch (Exception e) {
			log.error("Could not set properties file: {} "+e);
		}
	}
	
	/**
	 * Gets the properties of the CHe
	 * 
	 * @return The properties of the CHe
	 * @see #setProperties(Properties)
	 */
	public static synchronized Properties getProperties(){
		Properties prop=new Properties();
		try {
			prop=new Properties();
			InputStream in = new FileInputStream(PROPS_FILE);
			prop.load(in);
			in.close();
		} catch (java.io.FileNotFoundException e) {
			log.warn("Properties file does not exist; generating default...");
			prop.setProperty("DB.URL","jdbc:mysql://localhost:3306/persona_aal_space");
			prop.setProperty("DB.USER","casf_che");
			prop.setProperty("DB.PWD","casf_che");
			prop.setProperty("DB.TYPE","MySQL");
			prop.setProperty("MODEL.NAME","PERSONA_AAL_Space");
			prop.setProperty("PMD.StorageFile","PMD-Events.txt");
			prop.setProperty("PMD.BorderFlag","<!--CEv-->");
			prop.setProperty("RECYCLE.KEEP_MSEC","15552000000");//6 months
			prop.setProperty("RECYCLE.PERIOD_MSEC","5184000000");//2 months
			prop.setProperty("RECYCLE.HOUR","22");//at 22:00
			setProperties(prop);
		}catch (Exception e) {
			log.error("Could not access properties file: {} "+e);
		}
		return prop;
	}

}
