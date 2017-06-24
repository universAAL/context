/*
	Copyright 2008-2015 ITACA-TSB, http://www.tsb.upv.es
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
package org.universAAL.context.che;

import org.universAAL.context.che.Hub.Log;
import org.universAAL.context.che.database.Backend;
import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.context.ContextEvent;
import org.universAAL.middleware.context.ContextEventPattern;
import org.universAAL.middleware.context.ContextSubscriber;

/**
 * The CHe subscriber subscribes for all context events in order to save them to
 * the store.
 *
 * @author <a href="mailto:alfiva@itaca.upv.es">Alvaro Fides Valero</a>
 *
 */
public class ContextHistorySubscriber extends ContextSubscriber {
	/**
	 * The instance of the underlying store.
	 */
	private Backend db;
	/**
	 * Logger.
	 */
	private static Log log = Hub.getLog(ContextHistorySubscriber.class);
	/**
	 * Used to ignore keep-alive events (matches SystemInfo.PROP_ALIVE)
	 */
	private static final String SYSINFOPRED = "http://ontology.universAAL.org/SysInfo#alive";
	/**
	 * Determines if keep-alive events should be logged.
	 */
	private static boolean logAlive = Boolean.parseBoolean(Hub.getProperties().getProperty("STORE.LOGALIVE", "false"));

	/**
	 * Main constructor.
	 *
	 * @param context
	 *            The universAAL module context
	 * @param dbstore
	 *            The store
	 */
	public ContextHistorySubscriber(ModuleContext context, Backend dbstore) {
		// My context event pattern is zero-restrictions (ALL)
		super(context, new ContextEventPattern[] { new ContextEventPattern() });
		this.db = dbstore;
		log.info("init", "CHe: Subscriber Ready");
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.universAAL.middleware.context.ContextSubscriber#
	 * communicationChannelBroken()
	 */
	public void communicationChannelBroken() {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.universAAL.middleware.context.ContextSubscriber#handleContextEvent
	 * (org.universAAL.middleware.context.ContextEvent)
	 */
	public void handleContextEvent(ContextEvent event) {
		if (!logAlive && SYSINFOPRED.equals(event.getRDFPredicate()))
			return;
		db.storeEvent(event);
		log.info("handleContextEvent", "CHe: Stored a Context Event");
	}

	// public void close() {
	// // db.close();//Already closed by Activator
	// }

}
