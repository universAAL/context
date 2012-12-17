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
*	       �2012
*/

import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.context.ContextEvent;
import org.universAAL.middleware.context.ContextEventPattern;
import org.universAAL.middleware.context.ContextSubscriber;
import org.universAAL.middleware.context.owl.ContextProvider;
import org.universAAL.middleware.context.owl.ContextProviderType;
import org.universAAL.middleware.owl.ClassExpression;
import org.universAAL.middleware.owl.MergedRestriction;
import org.universAAL.middleware.owl.Restriction;
import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.sodapop.msg.Message;
import org.universAAL.ontology.dependability.ErrorDetector;
import org.universAAL.ontology.dependability.Fault;

public class ReliabilityReasonerSubscriber extends ContextSubscriber {

    protected ReliabilityReasonerSubscriber(ModuleContext context,
	    ContextEventPattern[] initialSubscriptions) {
	super(context, initialSubscriptions);
	
	ContextEventPattern cep = new ContextEventPattern();
	Resource r = new Restriction();
	r = Restriction.getAllValuesRestriction(ContextEvent.PROP_RDF_SUBJECT, Fault.MY_URI);
	cep.addRestriction((MergedRestriction) r);
	
	
	String myID = null;//this ID has to be mapped to my prod
	
	
	/*r = Restriction.getAllValuesRestriction(myID, MergedRestriction.getFixedValueRestriction(
		    ContextProvider.PROP_CONTEXT_PROVIDER_TYPE,ContextProviderType.reasoner).appendTo(
			        MergedRestriction.getAllValuesRestriction(
			            ContextEvent.PROP_CONTEXT_PROVIDER, ContextProvider.MY_URI),
			            new String[] { ContextEvent.PROP_CONTEXT_PROVIDER,
			                ContextProvider.PROP_CONTEXT_PROVIDER_TYPE }));
	cep.addRestriction((MergedRestriction) r);*/
	
	//HashMap contextMap = (HashMap) context.getAttribute(myID);//depends on the incoming context events
	
    }

    protected ReliabilityReasonerSubscriber(ModuleContext context) {
	super(context, getPermanentSubscriptions());
	String contextID= (String)context.getAttribute(getMyID());
	String location = (String)context.getAttribute(Fault.PROP_LOCATION);
	String timestamp = (String)context.getAttribute(Fault.PROP_TIMESTAMP);
	String faultType = (String)context.getAttribute(Fault.PROP_FAULT_DECISION);
	
	
    }

    private static ContextEventPattern[] getPermanentSubscriptions() {
	// TODO Auto-generated method stub
	return null;
    }

    public void communicationChannelBroken() {
	// TODO Auto-generated method stub

    }

    public void handleContextEvent(ContextEvent event) {
	// TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.universAAL.middleware.context.ContextSubscriber#close()
     */
    @Override
    public void close() {
	// TODO Auto-generated method stub
	super.close();
    }

    /* (non-Javadoc)
     * @see org.universAAL.middleware.context.ContextSubscriber#getAllProvisions()
     */
    @Override
    public ContextEventPattern[] getAllProvisions() {
	Message m = null;
	// TODO Auto-generated method stub
	super.handleEvent(m);
	m.getSource();
	return super.getAllProvisions();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
	// TODO Auto-generated method stub
	return super.hashCode();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
	// TODO Auto-generated method stub
	return super.toString();
    }

}