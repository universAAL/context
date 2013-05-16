/*	
	Copyright 2008-2014 Fraunhofer IGD, http://www.igd.fraunhofer.de
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
package org.universAAL.reasoner.server.osgi;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.container.osgi.uAALBundleContainer;
import org.universAAL.middleware.context.ContextEventPattern;
import org.universAAL.middleware.context.ContextPublisher;
import org.universAAL.middleware.context.DefaultContextPublisher;
import org.universAAL.middleware.context.owl.ContextProvider;
import org.universAAL.middleware.context.owl.ContextProviderType;
import org.universAAL.middleware.serialization.MessageContentSerializer;
import org.universAAL.reasoner.server.ReasoningProvider;
import org.universAAL.reasoner.server.CHECaller;

/**
 * Activator of the reasoner. Basically it is used to prepare the
 * the ReasoningProvider. The reasoner is basically a manager for situations,
 * queries and rules. A Situation is currently (need to be improved) represented
 * by three URI's: Subject, Predicate and Object. Subject and Object can be a
 * concrete instances or also types. The subject is mandatory but the
 * other two are optional. A Query is either a SPARQL Construct-Query or created
 * out of a given Context-Event and a search-string. A Rule combines a situation
 * with a query. If a context-event is posted on the context-bus that matches
 * the parameters given by the situation in a rule, then the according query is
 * performed at the CHE and the resulting ContextEvent of the Construct-Query is
 * posted at the Context-bus.
 * 
 * @author alfiva
 * @author amarinc
 * 
 */
public class Activator implements BundleActivator, ServiceListener {
    public static BundleContext osgiContext = null;
    public static ModuleContext mcontext = null;
    public static ContextPublisher cpublisher = null;
    public static CHECaller scaller = null;
    public static MessageContentSerializer serializer = null;
    
    private ReasoningProvider provider = null;

    public void start(BundleContext bcontext) throws Exception {
	Activator.osgiContext = bcontext;
	Activator.mcontext = uAALBundleContainer.THE_CONTAINER
		.registerModule(new Object[] { osgiContext });
	ContextProvider info = new ContextProvider(
		"http://ontology.itaca.es/Reasoner.owl#ReasonerPublisher");
	info.setType(ContextProviderType.reasoner);
	info.setProvidedEvents(new ContextEventPattern[] { new ContextEventPattern() });
	cpublisher = new DefaultContextPublisher(mcontext, info);
	scaller = new CHECaller(mcontext);
	
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
	while (serializer == null)
	    Thread.sleep(100);
	    
	provider = new ReasoningProvider(mcontext);
    }

    public void stop(BundleContext arg0) throws Exception {
	// stopIt = true;
	osgiContext = null;
	mcontext = null;
	cpublisher.close();
	scaller.close();
	provider.saveAllData();
	provider.deleteContextSubscriptions();
	provider = null;
    }

    public void serviceChanged(ServiceEvent event) {
	// Update the MessageContentSerializer
	switch (event.getType()) {
	case ServiceEvent.REGISTERED:
	case ServiceEvent.MODIFIED: {
	    serializer = (MessageContentSerializer) osgiContext
		    .getService(event.getServiceReference());
	    scaller.setuAALParser(serializer);
	    break;
	}
	case ServiceEvent.UNREGISTERING:
	    scaller.setuAALParser(null);
	    break;
	default:
	    break;
	}
    }

}
