/*	
	Copyright 2008-2014 Fraunhofer IGD, http://www.igd.fraunhofer.de
	Fraunhofer Gesellschaft - Institut f�r Graphische Datenverarbeitung 
	
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

import java.util.Iterator;
import java.util.List;

import org.universAAL.context.reasoner.osgi.Activator;
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
import org.universAAL.middleware.serialization.MessageContentSerializer;
import org.universAAL.ontology.che.ContextHistoryService;

/**
 * This class contains methods to perform SPARQL-Requests on the CHE.
 * 
 * @author alfiva
 * @author amarinc
 * 
 */
public class CHECaller extends ServiceCaller {
    private static final String HISTORY_CLIENT_NAMESPACE = "http://ontology.itaca.es/Reasoner.owl#";
    private static final String OUTPUT_RESULT_STRING = HISTORY_CLIENT_NAMESPACE
	    + "resultString";
    private MessageContentSerializer uAALParser;
    /**
     * GENERIC_EVENT is used to identify events created by a SPARQL CONSTRUCT
     * query.
     */
    private static final String GENERIC_EVENT = "urn:org.universAAL.middleware.context.rdf:ContextEvent#_:0000000000000000:00";

    public CHECaller(ModuleContext context) {
	super(context);
    }

    public void communicationChannelBroken() {

    }

    public void handleResponse(String reqID, ServiceResponse response) {

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
	String query = theQuery.replace(
		GENERIC_EVENT,
		ContextEvent.CONTEXT_EVENT_URI_PREFIX
			+ StringUtils.createUniqueID());
	String ser = callDoSPARQL(query);
	if (!ser.isEmpty()) {
	    ContextEvent event = (ContextEvent) uAALParser.deserialize(ser);
	    event.setTimestamp(new Long(System.currentTimeMillis()));
	    if (event.isWellFormed() && event.getSubjectTypeURI() != null) {
		Activator.cpublisher.publish(event);
	    } else {
		LogUtils.logError(Activator.mcontext, CHECaller.class,
			"executeQuery",
			new Object[] { "Invalid CONSTRUCT query associated to "
				+ "situation. CONSTRUCT queries must build "
				+ "graphs with a well-formed Context Event in"
				+ " the root." }, null);
	    }
	} else {
	    LogUtils.logWarn(Activator.mcontext, CHECaller.class,
		    "executeQuery",
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
    public String callDoSPARQL(String query) {
	ServiceResponse response = this.call(getDoSPARQLRequest(query));
	if (response.getCallStatus() == CallStatus.succeeded) {
	    try {
		String results = (String) getReturnValue(response.getOutputs(),
			OUTPUT_RESULT_STRING);
		// Uncomment this line if you want to show the raw results. Do
		// this for CONSTRUCT, ASK or DESCRIBE
		LogUtils.logInfo(Activator.mcontext, CHECaller.class,
			"callDoSPARQL",
			new Object[] { "Result of SPARQL query was:\n"
				+ results }, null);
		return results;
	    } catch (Exception e) {
		LogUtils.logInfo(Activator.mcontext, CHECaller.class,
			"callDoSPARQL",
			new Object[] { "History Client: Result corrupt!" }, e);
		return "";
	    }
	} else
	    LogUtils.logInfo(
		    Activator.mcontext,
		    CHECaller.class,
		    "callDoSPARQL",
		    new Object[] { "History Client - status of doSparqlQuery(): "
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
    private ServiceRequest getDoSPARQLRequest(String query) {
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
     * Handles the return-value of the CHE Service Request. This means to return
     * the value given in "expectedOutput" if such a result in the given list.
     * In case that there is one then more result for the same parameter the
     * first found will be returned.
     * 
     * @param outputs
     *            List of outputs like given by a ServiceResponse
     * @param expectedOutput
     *            URI of the expected output-parameter
     * @return Value for expectedOutput. Null if there is not such a value.
     */
    @SuppressWarnings("unchecked")
    private Object getReturnValue(List outputs, String expectedOutput) {
	Object returnValue = null;
	if (outputs == null)
	    LogUtils.logInfo(Activator.mcontext, CHECaller.class,
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
				Activator.mcontext,
				CHECaller.class,
				"getReturnValue",
				new Object[] { "History Client: redundant return value!" },
				null);
		else
		    LogUtils.logInfo(Activator.mcontext, CHECaller.class,
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
    public void setuAALParser(MessageContentSerializer service) {
	this.uAALParser = service;
    }

}
