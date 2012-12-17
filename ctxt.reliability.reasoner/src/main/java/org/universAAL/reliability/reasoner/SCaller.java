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
*
*@author alviva
*/

import java.util.Iterator;
import java.util.List;

import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.container.utils.LogUtils;
import org.universAAL.middleware.container.utils.StringUtils;
import org.universAAL.middleware.context.ContextEvent;
import org.universAAL.middleware.owl.MergedRestriction;
import org.universAAL.middleware.rdf.PropertyPath;
import org.universAAL.middleware.service.CallStatus;
import org.universAAL.middleware.service.ServiceCaller;
import org.universAAL.middleware.service.ServiceRequest;
import org.universAAL.middleware.service.ServiceResponse;
import org.universAAL.middleware.service.owls.process.ProcessOutput;
import org.universAAL.middleware.sodapop.msg.MessageContentSerializerEx;
import org.universAAL.mini.reasoner.Activator;
import org.universAAL.ontology.che.ContextHistoryService;


public class SCaller extends ServiceCaller {
    private static final String HISTORY_CLIENT_NAMESPACE = "http://ontology.itaca.es/Reasoner.owl#";
    private static final String OUTPUT_RESULT_STRING = HISTORY_CLIENT_NAMESPACE
	    + "resultString";
    private MessageContentSerializerEx uAALParser;
    private static final String GENERIC_EVENT = "urn:org.universAAL.middleware.context.rdf:ContextEvent#_:0000000000000000:00";

    protected SCaller(ModuleContext context) {
	super(context);
	// TODO Auto-generated constructor stub
    }

    public void communicationChannelBroken() {
	// TODO Auto-generated method stub

    }

    public void handleResponse(String reqID, ServiceResponse response) {
	// TODO Auto-generated method stub

    }

    /**
     * Execute a SPARQL CONSTRUCT query on the CHE that will return a reasoned
     * event. That event will be published.
     * 
     * @param theQuery
     *            The SPARQL CONSTRUCT query
     */
    public void executeQuery(String theQuery) {
	// TODO: Try to improve this. Also replace ALWAYS the URI with new one
	String newUri = ContextEvent.CONTEXT_EVENT_URI_PREFIX
		+ StringUtils.createUniqueID();
	String query = theQuery.replace(GENERIC_EVENT, newUri);
	String ser = callSPARQL(query);
	if (!ser.isEmpty()) {
	    ContextEvent event = (ContextEvent) uAALParser.deserialize(ser,
		    newUri);
	    if (event != null) {
		event.setTimestamp(new Long(System.currentTimeMillis()));
		if (event.isWellFormed() && event.getSubjectTypeURI() != null) {
		    Activator.cpublisher.publish(event);
		} else {
		    LogUtils.logError(
			    Activator.context,
			    SCaller.class,
			    "executeQuery",
			    new Object[] { "Invalid CONSTRUCT query associated to "
				    + "situation. CONSTRUCT queries must build "
				    + "graphs with a well-formed Context Event in"
				    + " the root." }, null);
		}
	    } else {
		LogUtils.logError(
			Activator.context,
			SCaller.class,
			"executeQuery",
			new Object[] { "Invalid CONSTRUCT query associated to "
				+ "situation. The CONSTRUCT query might not " +
				"be properly built." },
			null);
	    }
	} else {
	    LogUtils.logWarn(Activator.context, SCaller.class, "executeQuery",
		    new Object[] { "Triggered evaluation of situation, "
			    + "but not found" }, null);
	}
    }

    /**
     * Call the CHE service
     * 
     * @param query
     *            The CONSTRUCT query
     * @return Serialized event constructed
     */
    public String callSPARQL(String query) {
	ServiceResponse response = this.call(getSPARQLRequest(query));
	if (response.getCallStatus() == CallStatus.succeeded) {
	    try {
		String result = (String) getReturnValue(response.getOutputs(),
			OUTPUT_RESULT_STRING);
		// Uncomment this line if you want to show the raw results. Do
		// this for CONSTRUCT, ASK or DESCRIBE
		LogUtils.logInfo(Activator.context, SCaller.class,
			"callDoSPARQL",
			new Object[] { "Result of SPARQL query was:\n"
				+ result }, null);
		return result;
	    } catch (Exception e) {
		LogUtils.logInfo(Activator.context, SCaller.class,
			"callDoSPARQL",
			new Object[] { "Mini Reasoner: Result corrupt!" }, e);
		return "";
	    }
	} else
	    LogUtils.logInfo(
		    Activator.context,
		    SCaller.class,
		    "callDoSPARQL",
		    new Object[] { "Mini Reasoner: status of callDoSPARQL(): "
			    + response.getCallStatus() }, null);
	return "";
    }

    /**
     * Prepare the call for CHE
     * 
     * @param query
     *            The CONSTRUCT query
     * @return The request for the call
     */
    private ServiceRequest getSPARQLRequest(String query) {
	ServiceRequest getQuery = new ServiceRequest(new ContextHistoryService(
		null), null);

	MergedRestriction r = MergedRestriction.getFixedValueRestriction(
		ContextHistoryService.PROP_PROCESSES, query);

	getQuery.getRequestedService().addInstanceLevelRestriction(r,
		new String[] { ContextHistoryService.PROP_PROCESSES });
	getQuery.addSimpleOutputBinding(
		new ProcessOutput(OUTPUT_RESULT_STRING), new PropertyPath(null,
			true,
			new String[] { ContextHistoryService.PROP_RETURNS })
			.getThePath());
	return getQuery;
    }

    /**
     * Process service call response
     * 
     * @param outputs
     *            The outputs of the response
     * @param expectedOutput
     *            The URI of the desired output
     * @return The desired output value
     */
    private Object getReturnValue(List outputs, String expectedOutput) {
	Object returnValue = null;
	if (outputs == null)
	    LogUtils.logInfo(Activator.context, SCaller.class,
		    "getReturnValue",
		    new Object[] { "History Client: No events found!" }, null);
	else
	    for (Iterator i = outputs.iterator(); i.hasNext();) {
		ProcessOutput output = (ProcessOutput) i.next();
		if (output.getURI().equals(expectedOutput))
		    if (returnValue == null)
			returnValue = output.getParameterValue();
		    else
			LogUtils.logInfo(
				Activator.context,
				SCaller.class,
				"getReturnValue",
				new Object[] { "History Client: redundant return value!" },
				null);
		else
		    LogUtils.logInfo(Activator.context, SCaller.class,
			    "getReturnValue",
			    new Object[] { "History Client - output ignored: "
				    + output.getURI() }, null);
	    }

	return returnValue;
    }

    /**
     * Set a MessageContentSerializer to be used when parsing the serialized
     * returned event
     * 
     * @param service
     *            the parser
     */
    public void setuAALParser(MessageContentSerializerEx service) {
	this.uAALParser = service;
    }

}
