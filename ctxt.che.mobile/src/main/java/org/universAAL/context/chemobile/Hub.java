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
package org.universAAL.context.chemobile;

import java.io.File;

import org.universAAL.context.chemobile.osgi.Activator;
import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.serialization.MessageContentSerializer;

/**
 * Central class that takes care of starting and stopping application. It used
 * to be the Activator class before splitting the OSGi logic.
 *
 * @author alfiva
 *
 */
public class Hub {

	/**
	 * The context subscriber.
	 */
	private MobileHistorySubscriber hc;
	/**
	 * Config folder.
	 */
	protected static File confHome = new File(Activator.osgiConfigPath);

	/**
	 * To be called when application starts. Used to be Activator.start().
	 *
	 * @param context
	 *            universAAL module context
	 * @throws Exception
	 *             If anything goes wrong
	 */
	public void start(ModuleContext context) throws Exception {
		hc = new MobileHistorySubscriber(context);
	}

	/**
	 * To be called when application stops. Used to be Activator.stop().
	 *
	 * @throws Exception
	 */
	public void stop() throws Exception {
		hc.close();
	}

	/**
	 * Set the turtle parser. Make sure it's called after start().
	 *
	 * @param service
	 *            The parser
	 */
	public void setSerializer(MessageContentSerializer service) {
		hc.setSerializer(service);
	}

}
