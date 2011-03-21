/*
	Copyright 2008-2010 ITACA-TSB, http://www.tsb.upv.es
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

import org.osgi.framework.BundleContext;
import org.universAAL.middleware.context.ContextEvent;
import org.universAAL.middleware.context.ContextEventPattern;
import org.universAAL.middleware.context.ContextSubscriber;
import org.universAAL.middleware.util.Constants;

import com.thoughtworks.xstream.XStream;

/**
 * @author <a href="mailto:alfiva@itaca.upv.es">Alvaro Fides Valero</a>
 * 
 */
public class HistoryConsumer extends ContextSubscriber {
    private static final String FILE = "PMD-Events.txt";
    private XStream xs;
    private File confHome = new File(new File(Constants.getSpaceConfRoot()),
	    "ctxt.che.mobile");

    public HistoryConsumer(BundleContext context) {
	super(context, new ContextEventPattern[] { new ContextEventPattern() });
	xs = new XStream(new WriteOnlyJavaReflectionProvider());
	synchronized (Activator.getLock()) {
	    try {
		BufferedWriter out = new BufferedWriter(new FileWriter(
			new File(confHome, FILE), false));
		out.close();
	    } catch (Exception e) {
		Activator.log.debug("COULD NOT CREATE FILE " + e.getMessage()
			+ "---" + e.toString());
	    }
	}
    }

    public void communicationChannelBroken() {
	// TODO Auto-generated method stub
    }

    public void handleContextEvent(ContextEvent event) {
	Activator.log.debug("PMD CHe: Received a Context Event");
	synchronized (Activator.getLock()) {
	    try {
		BufferedWriter out = new BufferedWriter(new FileWriter(
			new File(confHome, FILE), true));
		String xmlOut = xs.toXML(event);
		out.write(xmlOut);
		out.newLine();
		out.write("<!--CEv-->");
		out.newLine();
		out.close();
	    } catch (Exception e) {
		Activator.log.debug("COULD NOT ACCESS FILE: " + e.getMessage()
			+ "---" + e.toString());
	    }
	}
    }

    public void close() {
	xs = null;
    }

}
