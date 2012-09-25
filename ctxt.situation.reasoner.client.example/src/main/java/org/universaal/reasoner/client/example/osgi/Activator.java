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
package org.universaal.reasoner.client.example.osgi;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.container.osgi.uAALBundleContainer;
import org.universAAL.middleware.container.utils.LogUtils;
import org.universaal.reasoner.client.CHECaller;
import org.universaal.reasoner.client.ReasoningCaller;
import org.universaal.reasoner.client.example.ReasoningGUI;

/**
 * This bundle implements a graphical example client for the Reasoner. The base
 * is a list of Situations, Queries and Rule (see also the description in the
 * Reasoners Activator) where the according objects can be managed
 * (added/removed/shown). The process of adding elements is supported by a
 * individual graphical user interface for each type. To support this recorded
 * elements on the context-bus are offered. For further descriptions please have
 * a look at the descriptions of the gui-classes for each type.
 * 
 * @author amarinc
 * 
 */
public class Activator implements BundleActivator {
    public static BundleContext osgiContext = null;
    public static ModuleContext context = null;

    public static ReasoningCaller scaller = null;
    public static ReasoningGUI gui = null;
    public static CHECaller cheCaller = null;

    public void start(BundleContext bcontext) throws Exception {
	Activator.osgiContext = bcontext;
	Activator.context = uAALBundleContainer.THE_CONTAINER
		.registerModule(new Object[] { bcontext });
	scaller = new ReasoningCaller();
	gui = new ReasoningGUI(scaller);
	cheCaller = new CHECaller(context);
    }

    public static void postInfo(Class<?> targetClass, String methodName,
	    String message) {
	LogUtils.logInfo(Activator.context, targetClass, methodName,
		new Object[] { message }, null);
    }

    public void stop(BundleContext arg0) throws Exception {
	Activator.osgiContext = null;
	Activator.context = null;
	scaller.unregister();
	scaller = null;
	gui.close();
	gui = null;
	cheCaller.close();
	cheCaller = null;
    }
}
