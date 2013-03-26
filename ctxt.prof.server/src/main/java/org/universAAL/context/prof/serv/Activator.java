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
package org.universAAL.context.prof.serv;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.container.osgi.uAALBundleContainer;
import org.universAAL.middleware.serialization.MessageContentSerializerEx;

/**
 * OSGI Activator. Initializes provided services and SCallee.
 * 
 * @author alfiva
 * 
 */
public class Activator implements BundleActivator, ServiceListener {
    /**
     * The OSGi Bundle mcontext
     */
    protected static BundleContext osgiContext = null;
    /**
     * The uAAL module mcontext
     */
    protected static ModuleContext mcontext = null;
    /**
     * Service Callee
     */
    protected static SCallee scallee = null;
    /**
     * Service Caller
     */
    protected static SCaller scaller = null;
    /**
     * Turtle parser
     */
    protected static MessageContentSerializerEx parser = null;

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext
     * )
     */
    public void start(BundleContext bcontext) throws Exception {
	Activator.osgiContext = bcontext;
	Activator.mcontext = uAALBundleContainer.THE_CONTAINER
		.registerModule(new Object[] { bcontext });
	scallee = new SCallee(mcontext);
	scaller = new SCaller(mcontext);
	String filter = "(objectclass="
		+ MessageContentSerializerEx.class.getName() + ")";
	osgiContext.addServiceListener(this, filter);
	ServiceReference[] references = osgiContext.getServiceReferences(null,
		filter);
	for (int i = 0; references != null && i < references.length; i++) {
	    this.serviceChanged(new ServiceEvent(ServiceEvent.REGISTERED,
		    references[i]));
	}
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
     */
    public void stop(BundleContext arg0) throws Exception {
	scallee.close();
	scaller.close();
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
	    parser = (MessageContentSerializerEx) osgiContext.getService(event
		    .getServiceReference());
	    break;
	case ServiceEvent.UNREGISTERING:
	    parser = null;
	    break;
	default:
	    break;
	}
    }

}
