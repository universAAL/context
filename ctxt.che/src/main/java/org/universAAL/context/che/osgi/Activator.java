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
package org.universAAL.context.che.osgi;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.universAAL.context.che.Hub;
import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.container.osgi.uAALBundleContainer;
import org.universAAL.middleware.sodapop.msg.MessageContentSerializer;

/**
 * OSGI activator. Relays start and stop to Hub.
 * 
 * @author alfiva
 * 
 */
public class Activator implements BundleActivator, ServiceListener {

    /**
     * uAAL module context
     */
    private ModuleContext moduleContext;
    /**
     * OSGI bundle context
     */
    private BundleContext osgiContext;
    /**
     * The application hub independent from OSGi
     */
    private Hub hub = new Hub();

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext
     * )
     */
    public void start(BundleContext context) throws Exception {
	osgiContext = context;
	// create the context
	moduleContext = uAALBundleContainer.THE_CONTAINER
		.registerModule(new Object[] { context });
	// Start the core
	hub.start(moduleContext);
	// Look for MessageContentSerializer of mw.data.serialization
	// And set parser
	String filter = "(objectclass="
		+ MessageContentSerializer.class.getName() + ")";
	context.addServiceListener(this, filter);
	ServiceReference[] references = context.getServiceReferences(null,
		filter);
	for (int i = 0; references != null && i < references.length; i++) {
	    this.serviceChanged(new ServiceEvent(ServiceEvent.REGISTERED,
		    references[i]));
	}
	// Sync mobile (after setting parser)
	hub.synchronizeMobile();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
     */
    public void stop(BundleContext arg0) throws Exception {
	hub.stop();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.osgi.framework.ServiceListener#serviceChanged(org.osgi.framework.
     * ServiceEvent)
     */
    public void serviceChanged(ServiceEvent event) {
	// Update the MessageContentSerializer
	switch (event.getType()) {
	case ServiceEvent.REGISTERED:
	case ServiceEvent.MODIFIED:
	    hub.setuAALParser((MessageContentSerializer) osgiContext
		    .getService(event.getServiceReference()));
	    break;
	case ServiceEvent.UNREGISTERING:
	    hub.setuAALParser(null);
	    break;
	default:
	    break;
	}
    }

}
