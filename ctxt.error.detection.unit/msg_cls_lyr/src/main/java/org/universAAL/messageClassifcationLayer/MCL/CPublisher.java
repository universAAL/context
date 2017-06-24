package org.universAAL.messageClassifcationLayer.MCL;


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
/**
 * Cpublisher is responsible for receiving the faulty messages from the EDUCore,
 *  and publish them on the context bus.
 * @author <a href="mailto:hamzah.dakheel@uni-siegen.de">Hamzah Dakheel</a>
* @author <a href="mailto:abu.sadat@uni-siegen.de">Rubaiyat Sadat</a>
 *
 */
public class CPublisher extends ContextPublisher {
	public static int received_faults = 0;

    protected CPublisher(ModuleContext context, ContextProvider providerInfo) {
	super(context, providerInfo);
	// TODO Auto-generated constructor stub
    }

    protected CPublisher(ModuleContext context) {
	super(context, getProviderInfo());
    }

    private static ContextProvider getProviderInfo() {
	ContextProvider myContextProvider = new ContextProvider("http://ontology.universAAL.org/Dependability.owl#Fault");
	ContextEventPattern myContextEventPattern = new ContextEventPattern();
	((ContextEventPattern) myContextEventPattern).addRestriction(MergedRestriction.getAllValuesRestriction(ContextEvent.PROP_RDF_SUBJECT, Fault.MY_URI));
	myContextProvider.setType(ContextProviderType.gauge);
	ContextEventPattern[] myEvents=new ContextEventPattern[] { new ContextEventPattern() };
	myContextProvider.setProvidedEvents(myEvents);
	return myContextProvider;
    }

    public void communicationChannelBroken() {
	// TODO Auto-generated method stub

    }


    /**
     * this function is the publiusher of the EDU publisher,
     * using ontologcial implication by Fault class
     * @param ID
     * @param judgment
     * @param timeStamp
     * @param location
     */
    public void publishContextEvent(int ID, int judgment, long timeStamp, String location) {

	Fault myFault = new Fault();


	// setting error judgment

	if (judgment == 0)
	    myFault.setFaultDecision(false);
	else
	    myFault.setFaultDecision(true);

	//calculating threshold and timestamp
	myFault.setTimestamp(timeStamp);

	myFault.setLocation(location);


	ContextEvent myContextEvent = new ContextEvent(myFault, Fault.PROP_FAULT_DECISION);
	publish(myContextEvent);
	}




    /**
     * this function should be called by EDUCore to receive the faulty message parameters,
     * reforming the faulty message and publish it on the context bus
     * @param ID
     * @param judgment
     * @param process
     * @param sec
     * @param nsec
     */
    public static void handleFault(int ID, int judgment, String process, String sec, String nsec){
    	++received_faults;

    		System.out.println("Hi there I am from haandleFault" + received_faults );

    }


}
