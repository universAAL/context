/*
	Copyright 2007-2014 Fraunhofer IGD, http://www.igd.fraunhofer.de
	Fraunhofer Gesellschaft - Institut für Graphische Datenverarbeitung 
	
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
package org.universAAL.context.space.conf;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.universAAL.context.conversion.jena.JenaConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.universAAL.ontology.phThing.PhysicalThing;
import org.universAAL.ontology.location.Place;
import org.universAAL.ontology.location.Way;
import org.universAAL.ontology.location.position.OriginedMetric;
import org.universAAL.ontology.shape.Box;
import org.universAAL.ontology.shape.Shape;

public class Activator implements BundleActivator {

	private static JenaConverter mc = null;
	
	static JenaConverter getModelConverter() {
		return mc;
	}
	
	private static BundleContext context;
	
	/**
	 * the logger for the provider bundle
	 */
	private static Logger logger = LoggerFactory.getLogger("de.fhg.igd.ima.persona.space.conf");
	
	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(final BundleContext context) throws Exception {
		Activator.context = context;
		
		Way.getClassRestrictionsOnProperty(null);
		PhysicalThing.getClassRestrictionsOnProperty(null);
		Place.getClassRestrictionsOnProperty(null);
		Shape.getClassRestrictionsOnProperty(null);
		Box.getClassRestrictionsOnProperty(null);
		OriginedMetric.getClassRestrictionsOnProperty(null);
		
		mc = (JenaConverter) context.getService(
				context.getServiceReference(JenaConverter.class.getName()));
		
		new Thread() {
			public void run() {
				
				new WorldConfigurationProvider(context);
				
			}
		}.start();
	}
	
	public static BundleContext getContext(){
		return context;
	}
	
	public static Logger getLogger(){
		return logger;
	}
	

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
	}
}