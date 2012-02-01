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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.container.osgi.util.BundleConfigHome;
import org.universAAL.middleware.container.utils.LogUtils;
import org.universAAL.middleware.context.ContextEvent;
import org.universAAL.middleware.context.ContextEventPattern;
import org.universAAL.middleware.context.ContextSubscriber;
import org.universAAL.middleware.sodapop.msg.MessageContentSerializer;

/**
 * The CHe subscriber subscribes for all context events in order to save them.
 * 
 * @author <a href="mailto:alfiva@itaca.upv.es">Alvaro Fides Valero</a>
 * 
 */
public class MobileHistorySubscriber extends ContextSubscriber {

    private static final String FILE = "Mobile-Events.txt";
    private static File confHome = new File(new BundleConfigHome(
	    "ctxt.che.mobile").getAbsolutePath());

    /**
     * File lock to synchronize access to "store".
     * 
     */
    private Object fileLock = new Object();
    /**
     * Turtle-uaal parser.
     */
    private MessageContentSerializer uAALParser;
    /**
     * uaal module context.
     */
    private ModuleContext moduleContext;

    /**
     * Main constructor.
     * 
     * @param context
     *            uaal module context
     */
    protected MobileHistorySubscriber(ModuleContext context) {
	super(context, new ContextEventPattern[] { new ContextEventPattern() });
	this.moduleContext = context;
	synchronized (fileLock) {
	    try {
		BufferedWriter out = new BufferedWriter(new FileWriter(
			new File(confHome, FILE), false));
		out.close();
	    } catch (Exception e) {
		LogUtils.logError(moduleContext, this.getClass(), "init",
			new Object[] { "COULD NOT CREATE FILE " }, e);
	    }
	}
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
	LogUtils.logDebug(moduleContext, this.getClass(), "handleContextEvent",
		new Object[] { "Mobile CHe: Received a Context Event" }, null);
	synchronized (fileLock) {
	    try {
		BufferedWriter out = new BufferedWriter(new FileWriter(
			new File(confHome, FILE), true));
		String turtleOut = uAALParser.serialize(event);
		out.write(turtleOut);
		out.newLine();
		out.write("<!--CEv-->");
		out.newLine();
		out.close();
	    } catch (Exception e) {
		LogUtils.logError(moduleContext, this.getClass(), "init",
			new Object[] { "COULD NOT ACCESS FILE: " }, e);
	    }
	}
    }

    /**
     * Sets the uaal parser.
     * 
     * @param service
     *            the parser
     */
    public void setuAALParser(MessageContentSerializer service) {
	this.uAALParser = service;
    }

}
