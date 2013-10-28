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
package org.universAAL.context.reasoner;

import org.universAAL.context.reasoner.osgi.Activator;
import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.context.ContextEvent;
import org.universAAL.middleware.context.ContextEventPattern;
import org.universAAL.middleware.context.ContextSubscriber;

/**
 * Class used to register multiple subscribers, each of them using just a single
 * mcontext event pattern, and having an associated SPARQL CONSTRUCT query. Each
 * patter is given by a restriction for the subject, the predicate and the
 * object for a mcontext-event at the bus, where the last two are optional. See
 * also the class Situation in the ontology for the reasoner.
 * 
 * @author alfiva
 * @author amarinc
 * 
 */
public class CSubsMulti extends ContextSubscriber {

    private String theQuery;

    /**
     * Create the subscriber
     * 
     * @param mcontext
     *            the module mcontext
     * @param initialSubscriptions
     *            the mcontext event pattern to react to
     * @param query
     *            the query to execute when receiving an event
     */
    protected CSubsMulti(ModuleContext context,
	    ContextEventPattern[] initialSubscriptions, String query) {
	super(context, initialSubscriptions);
	theQuery = query;
    }

    public void communicationChannelBroken() {

    }

    public void handleContextEvent(ContextEvent event) {
	Activator.scaller.executeQuery(theQuery);
    }

}
