package org.universAAL.reliability.reasoner;

/**Copyright [2011-2014] [University of Siegen, Embedded System Instiute]

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.

 * @author <a href="mailto:abu.sadat@uni-siegen.de">Rubaiyat Sadat</a>
 *	       ©2012
 */
import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.context.ContextEvent;
import org.universAAL.middleware.context.ContextEventPattern;
import org.universAAL.middleware.context.ContextPublisher;
import org.universAAL.middleware.context.owl.ContextProvider;
import org.universAAL.middleware.context.owl.ContextProviderType;
import org.universAAL.middleware.owl.MergedRestriction;
import org.universAAL.ontology.dependability.Fault;

public class ReliabilityReasonerPublisher extends ContextPublisher {

    protected ReliabilityReasonerPublisher(ModuleContext context,
	    ContextProvider providerInfo) {
	super(context, getProviderInfo());

    }

    protected ReliabilityReasonerPublisher(ModuleContext context) {
	super(context, getProviderInfo());

    }

    public void publishContextEvent(Fault myFault) {
	ContextEvent myContextEvent = new ContextEvent(myFault,
		Fault.PROP_FAULT_DECISION);
	publish(myContextEvent);
    }

    private static ContextProvider getProviderInfo() {
	ContextProvider myContextProvider = new ContextProvider(
		"http://ontology.universAAL.org/Dependability.owl#Fault");
	ContextEventPattern myContextEventPattern = new ContextEventPattern();
	((ContextEventPattern) myContextEventPattern)
		.addRestriction(MergedRestriction.getAllValuesRestriction(
			ContextEvent.PROP_RDF_SUBJECT, Fault.MY_URI));
	myContextProvider.setType(ContextProviderType.controller);
	ContextEventPattern[] myEvents = null;
	myContextProvider.setProvidedEvents(myEvents);
	return myContextProvider;
    }

    public void communicationChannelBroken() {
	// TODO Auto-generated method stub

    }

}