/*
	Copyright 2007-2014 Fraunhofer IGD, http://www.igd.fraunhofer.de
	Fraunhofer Gesellschaft - Institut f�r Graphische Datenverarbeitung 
	
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

import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.container.osgi.uAALBundleContainer;
import org.universAAL.ontology.phThing.PhysicalThing;
import org.universAAL.ontology.location.Place;
import org.universAAL.ontology.location.Way;
import org.universAAL.ontology.location.position.OriginedMetric;
import org.universAAL.ontology.shape.Box;
import org.universAAL.ontology.shape.Shape;

public class Activator implements BundleActivator {

	private static JenaConverter jenaConv = null;
	public static ModuleContext mc;
	
	static JenaConverter getModelConverter() {
		return jenaConv;
	}
	
	private static BundleContext context;
	
	/**
	 * the logger for the provider bundle
	 */
	//private static Logger logger = LoggerFactory.getLogger("de.fhg.igd.ima.persona.space.conf");
	

	public void start(final BundleContext context) throws Exception {
		Activator.context = context;
		mc = uAALBundleContainer.THE_CONTAINER
			.registerModule(new Object[] { context });
		
		Way.getClassRestrictionsOnProperty(null);
		PhysicalThing.getClassRestrictionsOnProperty(null);
		Place.getClassRestrictionsOnProperty(null);
		Shape.getClassRestrictionsOnProperty(null);
		Box.getClassRestrictionsOnProperty(null);
		OriginedMetric.getClassRestrictionsOnProperty(null);
		
		jenaConv = (JenaConverter) context.getService(
				context.getServiceReference(JenaConverter.class.getName()));
		
		new Thread() {
			public void run() {
				
				new WorldConfigurationProvider(mc);
				
			}
		}.start();
	}
	
	public static BundleContext getContext(){
		return context;
	}
	
//	public static Logger getLogger(){
//		return logger;
//	}
	

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
	}
}