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
package org.universAAL.context.sysinfo;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.container.osgi.OSGiContainer;

import org.universAAL.middleware.context.ContextEvent;
import org.universAAL.middleware.context.ContextEventPattern;
import org.universAAL.middleware.context.ContextPublisher;
import org.universAAL.middleware.context.DefaultContextPublisher;
import org.universAAL.middleware.context.owl.ContextProvider;
import org.universAAL.middleware.context.owl.ContextProviderType;
import org.universAAL.middleware.owl.MergedRestriction;
import org.universAAL.ontology.sysinfo.Descriptor;
import org.universAAL.ontology.sysinfo.SystemInfo;

public class Activator implements BundleActivator {
	public static BundleContext osgiContext = null;
	public static ModuleContext context = null;

	public static ContextPublisher cpublisher = null;
	SystemEventsListener listener = null;

	public void start(BundleContext bcontext) throws Exception {
		Activator.osgiContext = bcontext;
		Activator.context = OSGiContainer.THE_CONTAINER.registerModule(new Object[] { bcontext });
		// Provided events
		ContextEventPattern cep = new ContextEventPattern();
		MergedRestriction r1 = MergedRestriction.getAllValuesRestriction(ContextEvent.PROP_RDF_SUBJECT,
				SystemInfo.MY_URI);
		MergedRestriction r2 = MergedRestriction.getAllValuesRestriction(ContextEvent.PROP_RDF_OBJECT,
				Descriptor.MY_URI);
		cep.addRestriction((MergedRestriction) r1);
		cep.addRestriction((MergedRestriction) r2);
		// Provider info
		ContextProvider info = new ContextProvider("http://org.universAAL.context/Sysinfo.owl#SysInfoProvider");
		info.setType(ContextProviderType.gauge);
		info.setProvidedEvents(new ContextEventPattern[] { cep });
		// Start publisher and listener
		cpublisher = new DefaultContextPublisher(context, info);
		listener = new SystemEventsListener(context);
		listener.init();
	}

	public void stop(BundleContext arg0) throws Exception {
		cpublisher.close();
		listener = null;
	}

}
