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
import org.universAAL.middleware.context.ContextSubscriber;
import org.universAAL.middleware.context.owl.ContextProvider;
import org.universAAL.middleware.context.owl.ContextProviderType;
import org.universAAL.middleware.owl.MergedRestriction;
import org.universAAL.middleware.sodapop.msg.Message;
import org.universAAL.ontology.dependability.Fault;
import org.universAAL.reliability.reasoner.SCaller;

public class ReliabilityReasonerSubscriber extends ContextSubscriber {

    protected ReliabilityReasonerSubscriber(ModuleContext context,
	    ContextEventPattern[] initialSubscriptions) {
	super(context, initialSubscriptions);
    }

    protected ReliabilityReasonerSubscriber(ModuleContext context) {
	super(context, getPermanentSubscriptions());
	/*String contextID = (String) context.getAttribute(getMyID());
	String location = (String) context.getAttribute(Fault.PROP_LOCATION);
	String timestamp = (String) context.getAttribute(Fault.PROP_TIMESTAMP);
	String faultType = (String) context
		.getAttribute(Fault.PROP_FAULT_DECISION);*/
    }

    private static ContextEventPattern[] getPermanentSubscriptions() {
	ContextEventPattern cep = new ContextEventPattern();
	cep.addRestriction(MergedRestriction.getAllValuesRestriction(
		ContextEvent.PROP_RDF_SUBJECT, Fault.MY_URI));

	MergedRestriction r = 
		MergedRestriction.getFixedValueRestriction(
			ContextProvider.PROP_CONTEXT_PROVIDER_TYPE,
			ContextProviderType.gauge).appendTo(
			MergedRestriction.getAllValuesRestriction(
				ContextEvent.PROP_CONTEXT_PROVIDER,
				ContextProvider.MY_URI),
			new String[] { ContextEvent.PROP_CONTEXT_PROVIDER,
				ContextProvider.PROP_CONTEXT_PROVIDER_TYPE });
	cep.addRestriction((MergedRestriction) r);

	// HashMap contextMap = (HashMap) context.getAttribute(myID);//depends
	// on the incoming context events
	
	return new ContextEventPattern[]{cep};
    }

    public void communicationChannelBroken() {
	// TODO Auto-generated method stub

    }

public void handleContextEvent(ContextEvent event) {
	
	String eventTimestamp = (String) event.getProperty(Fault.PROP_TIMESTAMP);
	String eventType = (String) event.getProperty(Fault.PROP_FAULT_DECISION);
	String eventLocation = (String) event.getProperty(Fault.PROP_LOCATION);
	    
	    
	    event.setRDFSubject(event);
	    event.setRDFPredicate(eventTimestamp);
	    event.setRDFObject(eventType);
	    
	    /*FileInputStream fis = null;
	    try {
		fis = new FileInputStream("Query.txt");
	    } catch (FileNotFoundException e) {
		
		e.printStackTrace();
	    }
	    try {
		InputStreamReader in = new InputStreamReader(fis, "UTF-8");
	    } catch (UnsupportedEncodingException e) {
		
		e.printStackTrace();
	    } */
	  
	    ModuleContext thisSubscriberContext = (ModuleContext) event.getProvider();
	    SCaller caller = new SCaller(thisSubscriberContext);
	    
	 if (eventType == "http://ontology.universAAL.org/Dependability#Fault" && eventLocation == "http://ontology.universAAL.org/Dependability#FCR")
	 {
	     String queryString =
		     "CONSTRUCT { <urn:org.universAAL.middleware.context.rdf:ContextEvent#_:0000000000000000:00>  <http://www.w3.org/1999/02/22-rdf-syntax-ns#type>  <http://ontology.universAAL.org/Context.owl#ContextEvent> "+
		 "<http://www.w3.org/1999/02/22-rdf-syntax-ns#subject> <http://ontology.universAAL.org/Dependability#FCR> "+
		 "<http://www.w3.org/1999/02/22-rdf-syntax-ns#predicate> <http://ontology.universAAL.org/Dependability#Fault>"+
		 "<http://www.w3.org/1999/02/22-rdf-syntax-ns#object> \"100\"^^<http://www.w3.org/2001/XMLSchema#int> .}"+
		 "WHERE		  {"+
		 "<http://ontology.universAAL.org/Dependability#FCR> <http://ontology.universAAL.org/Dependability#Fault> ?a ."+
		 "FILTER (?a > 100)"+
		  "} \n";
	     
		    
		    caller.executeQuery(queryString);
	
	 }
	 else if (eventType== "http://ontology.universAAL.org/Dependability#EarlyTimingFault" && eventLocation == "http://ontology.universAAL.org/Dependability#FCR")
	 {
	     String queryString =
		     "CONSTRUCT { <urn:org.universAAL.middleware.context.rdf:ContextEvent#_:0000000000000000:00>  <http://www.w3.org/1999/02/22-rdf-syntax-ns#type>  <http://ontology.universAAL.org/Context.owl#ContextEvent> "+
		 "<http://www.w3.org/1999/02/22-rdf-syntax-ns#subject> <http://ontology.universAAL.org/Dependability#FCR> "+
		 "<http://www.w3.org/1999/02/22-rdf-syntax-ns#predicate> <http://ontology.universAAL.org/Dependability#EarlyTimingFault>"+
		 "<http://www.w3.org/1999/02/22-rdf-syntax-ns#object> \"100\"^^<http://www.w3.org/2001/XMLSchema#int> .}"+
		 "WHERE		  {"+
		 "<http://ontology.universAAL.org/Dependability#FCR> <http://ontology.universAAL.org/Dependability#EarlyTimingFault> ?a ."+
		 "FILTER (?a > 100)"+
		  "} \n";
	     
		    
		    caller.executeQuery(queryString);
	 }
	 
	 else if (eventType== "http://ontology.universAAL.org/Dependability#LateTimingFault" && eventLocation == "http://ontology.universAAL.org/Dependability#FCR")
	 {
	     String queryString =
		     "CONSTRUCT { <urn:org.universAAL.middleware.context.rdf:ContextEvent#_:0000000000000000:00>  <http://www.w3.org/1999/02/22-rdf-syntax-ns#type>  <http://ontology.universAAL.org/Context.owl#ContextEvent> "+
		 "<http://www.w3.org/1999/02/22-rdf-syntax-ns#subject> <http://ontology.universAAL.org/Dependability#FCR> "+
		 "<http://www.w3.org/1999/02/22-rdf-syntax-ns#predicate> <http://ontology.universAAL.org/Dependability#LateTimingFault>"+
		 "<http://www.w3.org/1999/02/22-rdf-syntax-ns#object> \"100\"^^<http://www.w3.org/2001/XMLSchema#int> .}"+
		 "WHERE		  {"+
		 "<http://ontology.universAAL.org/Dependability#FCR> <http://ontology.universAAL.org/Dependability#LateTimingFault> ?a ."+
		 "FILTER (?a > 100)"+
		  "} \n";
	     
		    
		    caller.executeQuery(queryString);
	 }
	 
	 else if (eventType== "http://ontology.universAAL.org/Dependability#ValueFault" && eventLocation == "http://ontology.universAAL.org/Dependability#FCR")
	 {
	     String queryString =
		     "CONSTRUCT { <urn:org.universAAL.middleware.context.rdf:ContextEvent#_:0000000000000000:00>  <http://www.w3.org/1999/02/22-rdf-syntax-ns#type>  <http://ontology.universAAL.org/Context.owl#ContextEvent> "+
		 "<http://www.w3.org/1999/02/22-rdf-syntax-ns#subject> <http://ontology.universAAL.org/Dependability#FCR> "+
		 "<http://www.w3.org/1999/02/22-rdf-syntax-ns#predicate> <http://ontology.universAAL.org/Dependability#ValueFault>"+
		 "<http://www.w3.org/1999/02/22-rdf-syntax-ns#object> \"100\"^^<http://www.w3.org/2001/XMLSchema#int> .}"+
		 "WHERE		  {"+
		 "<http://ontology.universAAL.org/Dependability#FCR> <http://ontology.universAAL.org/Dependability#ValueFault> ?a ."+
		 "FILTER (?a > 100)"+
		  "} \n";
	     
		    
		    caller.executeQuery(queryString);
	 }
	 else
	     {
		     String queryString =
			     "CONSTRUCT { <urn:org.universAAL.middleware.context.rdf:ContextEvent#_:0000000000000000:00>  <http://www.w3.org/1999/02/22-rdf-syntax-ns#type>  <http://ontology.universAAL.org/Context.owl#ContextEvent> "+
			 "<http://www.w3.org/1999/02/22-rdf-syntax-ns#subject> <http://ontology.universAAL.org/Dependability#FCR> "+
			 "<http://www.w3.org/1999/02/22-rdf-syntax-ns#predicate> <http://ontology.universAAL.org/Dependability#TransientFault>"+
			 "<http://www.w3.org/1999/02/22-rdf-syntax-ns#object> "+eventTimestamp+"^^<http://www.w3.org/2001/XMLSchema#long> .}"+
			  "} \n";
		     
			    
			    caller.executeQuery(queryString);
		 }
	
	

    }

   /* 
     * (non-Javadoc)
     * 
     * @see
     * org.universAAL.middleware.context.ContextSubscriber#getAllProvisions()
     
    @Override
    public ContextEventPattern[] getAllProvisions() {
	Message m = null;
	// TODO Auto-generated method stub
	super.handleEvent(m);
	m.getSource();
	return super.getAllProvisions();
    }*/

}