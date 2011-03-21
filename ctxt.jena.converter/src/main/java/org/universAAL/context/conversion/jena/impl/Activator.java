/*
	Copyright 2008-2010 Fraunhofer IGD, http://www.igd.fraunhofer.de
	Fraunhofer-Gesellschaft - Institute of Computer Graphics Research 
	
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
package org.universAAL.context.conversion.jena.impl;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.universAAL.context.conversion.jena.JenaConverter;
import org.universAAL.middleware.rdf.TypeMapper;

/**
 * The Jena ontology factory is an implementation of
 * {@link MessageContentSerializer} using JENA as the underlying tool.
 * 
 * @author mtazari
 * 
 */
public class Activator implements BundleActivator, ServiceListener {

    static BundleContext context = null;
    JenaModelConverter ser;

    public void start(BundleContext context) throws Exception {
	Activator.context = context;

	ser = new JenaModelConverter();

	context.registerService(new String[] { JenaConverter.class.getName() },
		ser, null);

	String filter = "(objectclass=" + TypeMapper.class.getName() + ")";
	context.addServiceListener(this, filter);
	ServiceReference references[] = context.getServiceReferences(null,
		filter);
	for (int i = 0; references != null && i < references.length; i++)
	    this.serviceChanged(new ServiceEvent(ServiceEvent.REGISTERED,
		    references[i]));

    }

    public void stop(BundleContext arg0) throws Exception {
	// TODO Auto-generated method stub
    }

    public void serviceChanged(ServiceEvent event) {
	switch (event.getType()) {
	case ServiceEvent.REGISTERED:
	case ServiceEvent.MODIFIED:
	    ser.setTypeMapper((TypeMapper) context.getService(event
		    .getServiceReference()));
	    break;
	case ServiceEvent.UNREGISTERING:
	    ser.setTypeMapper(null);
	    break;
	}
    }
}
