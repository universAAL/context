/*	
	Copyright 2008-2014 Fraunhofer IGD, http://www.igd.fraunhofer.de
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
package org.universaal2.reasoner.client.osgi;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.container.osgi.uAALBundleContainer;
import org.universAAL.middleware.container.utils.LogUtils;
import org.universAAL.middleware.serialization.MessageContentSerializer;

/**
 * 
 * This bundle offers classes to get access and make changes on the situation
 * reasoner. The basic idea behind this to not force a programmer to take use of
 * the basic universAAL functionality but use the reasoner with simple method
 * calls like provided by the ReasoningCaller. In addition there is the class
 * ContextEventRecorder that record all events at the context-bus (and therefore
 * possible Type-URI's, instances and its URI's in the system, ... ). This can
 * be used to offer possibilities for the elements of the Situations, Queries
 * and Rules to a user and therefore simplify the process of creating them. The
 * class CHECaller can be used to perform SPARQL-Queries at the CHE.
 * 
 * @author amarinc
 * 
 */
public class Activator implements BundleActivator, ServiceListener {
    public static BundleContext osgiContext = null;
    public static ModuleContext context = null;
    public static MessageContentSerializer serializer = null;

    public void start(BundleContext bcontext) throws Exception {
	Activator.osgiContext = bcontext;
	Activator.context = uAALBundleContainer.THE_CONTAINER
		.registerModule(new Object[] { bcontext });

	// Look for MessageContentSerializer of mw.data.serialization
	String filter = "(objectclass="
		+ MessageContentSerializer.class.getName() + ")";
	osgiContext.addServiceListener(this, filter);
	ServiceReference[] references = osgiContext.getServiceReferences(null,
		filter);
	for (int i = 0; references != null && i < references.length; i++) {
	    this.serviceChanged(new ServiceEvent(ServiceEvent.REGISTERED,
		    references[i]));
	}
    }

    public static void postInfo(Class<?> targetClass, String methodName,
	    String message) {
	LogUtils.logInfo(Activator.context, targetClass, methodName,
		new Object[] { message }, null);
    }

    public void stop(BundleContext arg0) throws Exception {
	Activator.osgiContext = null;
	Activator.context = null;
	Activator.osgiContext = null;
	Activator.context = null;
    }

    public void serviceChanged(ServiceEvent event) {
	// Update the MessageContentSerializer
	switch (event.getType()) {
	case ServiceEvent.REGISTERED:
	case ServiceEvent.MODIFIED: {
	    serializer = (MessageContentSerializer) osgiContext
		    .getService(event.getServiceReference());
	    break;
	}
	case ServiceEvent.UNREGISTERING:
	    break;
	default:
	    break;
	}
    }

}
