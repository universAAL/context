/*
	Copyright 2012-2014 ITACA-TSB, http://www.tsb.upv.es
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
package org.universAAL.context.prof.serv.osgi;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.universAAL.context.prof.serv.Hub;
import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.container.osgi.OSGiContainer;
import org.universAAL.middleware.serialization.MessageContentSerializerEx;

/**
 * OSGI Activator. Initializes provided services and SCallee.
 *
 * @author alfiva
 *
 */
public class Activator implements BundleActivator, ServiceListener {
	/**
	 * The OSGi Bundle context
	 */
	private static BundleContext osgiContext = null;
	/**
	 * The universAAL module context
	 */
	private static ModuleContext context = null;
	/**
	 * The application hub independent from OSGi.
	 */
	private Hub hub;

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext
	 * )
	 */
	public void start(BundleContext bcontext) throws Exception {
		Activator.osgiContext = bcontext;
		Activator.context = OSGiContainer.THE_CONTAINER.registerModule(new Object[] { bcontext });

		// Initialize the CHE hub (needed before setting parsers)
		this.hub = new Hub();

		String filter = "(objectclass=" + MessageContentSerializerEx.class.getName() + ")";
		osgiContext.addServiceListener(this, filter);
		ServiceReference[] references = osgiContext.getServiceReferences((String) null, filter);
		for (int i = 0; references != null && i < references.length; i++) {
			this.serviceChanged(new ServiceEvent(ServiceEvent.REGISTERED, references[i]));
		}

		// Start the hub. May be heavy, use thread.
		new Thread() {
			public void run() {
				hub.start(context);
			}
		}.start();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext arg0) throws Exception {
		hub.stop(Activator.context);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.osgi.framework.ServiceListener#serviceChanged(org.osgi.framework.
	 * ServiceEvent)
	 */
	public void serviceChanged(ServiceEvent event) {
		// Update the parser of Hub (& store)
		MessageContentSerializerEx parser;
		switch (event.getType()) {
		case ServiceEvent.REGISTERED:
		case ServiceEvent.MODIFIED:
		    parser = (MessageContentSerializerEx) osgiContext.getService(event.getServiceReference());
		    if(parser.getContentType().equals("text/turtle")){
			hub.setSerializer(parser);
		    }
			break;
		case ServiceEvent.UNREGISTERING:
		    parser = (MessageContentSerializerEx) osgiContext.getService(event.getServiceReference());
		    if(parser.getContentType().equals("text/turtle")){
			hub.setSerializer(null);
		    }
			break;
		default:
			break;
		}
	}

	/**
	 * Get the universAAL module context. This is only needed for integration test.
	 *
	 * @return the module context
	 */
	public static ModuleContext getModuleContext() {
		return context;
	}

}
