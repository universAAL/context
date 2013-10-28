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
package org.universAAL.context.prof.serv;

import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.serialization.MessageContentSerializerEx;

/**
 * Central class that takes care of starting and stopping application. It used
 * to be the Activator class before splitting the OSGi logic.
 * 
 * @author alfiva
 * 
 */
public class Hub {
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
    /**
     * uAAL Module context.
     */
    protected static ModuleContext moduleContext = null;

    /**
     * Default constructor.
     */
    public Hub() {

    }

    /**
     * To be called when application starts. Used to be Activator.start().
     * 
     * @param context
     *            uaal module context
     */
    public void start(ModuleContext context) {
	moduleContext=context;
	scallee = new SCallee(context);
	scaller = new SCaller(context);
    }

    /**
     * To be called when application stops. Used to be Activator.stop().
     * 
     * @throws Exception
     */
    public final void stop() throws Exception {
	moduleContext=null;
	scallee.close();
	scaller.close();
    }

    /**
     * Set the turtle-uaal parser. Make sure it's set at least once before
     * start().
     * 
     * @param service
     *            The parser
     */
    public void setuAALParser(MessageContentSerializerEx service) {
	parser = service;
    }

}
