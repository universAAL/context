/*
	Copyright 2008-2015 ITACA-TSB, http://www.tsb.upv.es
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
package org.universAAL.context.che;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.universAAL.context.che.Hub.Log;
import org.universAAL.context.che.database.Backend;
import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.context.owl.ContextProvider;
import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.serialization.MessageContentSerializerEx;
import org.universAAL.middleware.service.CallStatus;
import org.universAAL.middleware.service.ServiceCall;
import org.universAAL.middleware.service.ServiceCallee;
import org.universAAL.middleware.service.ServiceResponse;
import org.universAAL.middleware.service.owls.process.ProcessOutput;
import org.universAAL.ontology.che.ContextEvent;

/**
 * The CHe service callee receives all service calls issued to the CHe through
 * the service bus.
 * 
 * @author <a href="mailto:alfiva@itaca.upv.es">Alvaro Fides Valero</a>
 * 
 */
public class ContextHistoryCallee extends ServiceCallee {
    private static final ServiceResponse FAILURE = new ServiceResponse(
	    CallStatus.serviceSpecificFailure);
    /**
     * Logger.
     */
    private static Log log = Hub.getLog(ContextHistoryCallee.class);

    /**
     * The DB of the store.
     */
    private Backend db;

    /**
     * Main constructor.
     * 
     * @param context
     *            The uaal module context
     * @param dbstore
     *            The store
     */
    ContextHistoryCallee(ModuleContext context, Backend dbstore) {
	super(context, ContextHistoryServices.PROFILES);
	this.db = dbstore;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.universAAL.middleware.service.ServiceCallee#communicationChannelBroken
     * ()
     */
    public void communicationChannelBroken() {
	// TODO Auto-generated method stub

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.universAAL.middleware.service.ServiceCallee#handleCall(org.universAAL
     * .middleware.service.ServiceCall)
     */
    public ServiceResponse handleCall(ServiceCall call) {
	log.info("handleCall", "CHe received a service call");
	if (call == null) {
	    FAILURE.addOutput(new ProcessOutput(
		    ServiceResponse.PROP_SERVICE_SPECIFIC_ERROR, "Corrupt call"));
	    return FAILURE;
	}
	String operation = call.getProcessURI();
	if (operation == null) {
	    FAILURE.addOutput(new ProcessOutput(
		    ServiceResponse.PROP_SERVICE_SPECIFIC_ERROR, "Corrupt call"));
	    return FAILURE;
	}
	List scopeList = call.getScopes();
	String[] scopeArray = (String[]) scopeList.toArray(new String[0]);
	if (operation
		.startsWith(ContextHistoryServices.SERVICE_DO_SPARQL_QUERY)) {
	    log.info("handleCall", "Received call was SERVICE_DO_SPARQL_QUERY");
	    Object input = call
		    .getInputValue(ContextHistoryServices.INPUT_QUERY);
	    if (input == null) {
		FAILURE.addOutput(new ProcessOutput(
			ServiceResponse.PROP_SERVICE_SPECIFIC_ERROR,
			"Invalid input"));
		return FAILURE;
	    }
	    return execSPARQLQuery((String) input, scopeArray);
	} else if (operation
		.startsWith(ContextHistoryServices.SERVICE_GET_EVENTS_BY_SPARQL)) {
	    log.info("handleCall",
		    "Received call was SERVICE_GET_EVENTS_BY_SPARQL");
	    Object input = call
		    .getInputValue(ContextHistoryServices.INPUT_QUERY);
	    if (input == null) {
		FAILURE.addOutput(new ProcessOutput(
			ServiceResponse.PROP_SERVICE_SPECIFIC_ERROR,
			"Invalid input"));
		return FAILURE;
	    }
	    return execSPARQLQueryForEvents((String) input, scopeArray);
	} else {
	    Object input = call
		    .getInputValue(ContextHistoryServices.INPUT_EVENT);
	    ContextEvent inputevent;
	    if ((input == null) || (!(input instanceof ContextEvent))) {
		FAILURE.addOutput(new ProcessOutput(
			ServiceResponse.PROP_SERVICE_SPECIFIC_ERROR,
			"Invalid input (ContextEvent)"));
		return FAILURE;
	    } else {
		inputevent = (ContextEvent) input;
	    }
	    String sub, typ, pre;
	    Object obj;
	    Integer con;
	    Long exp, tst;
	    ContextProvider cop;
	    sub = (inputevent.getSubjectURI() != null && !inputevent
		    .getRDFSubject().isAnon()) ? inputevent.getSubjectURI()
		    : null;
	    typ = (inputevent.getSubjectTypeURI() != null) ? inputevent
		    .getSubjectTypeURI() : null;
	    pre = (inputevent.getRDFPredicate() != null) ? inputevent
		    .getRDFPredicate() : null;
	    obj = (inputevent.getRDFObject() != null) ? inputevent
		    .getRDFObject() : null;
	    con = (inputevent.getConfidence() != null) ? inputevent
		    .getConfidence() : null;
	    exp = (inputevent.getExpirationTime() != null) ? inputevent
		    .getExpirationTime() : null;
	    tst = (inputevent.getTimestamp() != null) ? inputevent
		    .getTimestamp() : null;
	    cop = (inputevent.getProvider() != null) ? inputevent.getProvider()
		    : null;
	    if (operation
		    .startsWith(ContextHistoryServices.SERVICE_GET_EVENTS_FROM_TIMESTAMP)) {
		log.info("handleCall",
			"Received call was SERVICE_GET_EVENTS_FROM_TIMESTAMP");
		Object tstinput = call
			.getInputValue(ContextHistoryServices.INPUT_TIMESTAMP_FROM);
		Long tstinputValue = Long.valueOf("0");
		if ((tstinput == null) || (!(tstinput instanceof Long))) {
		    FAILURE.addOutput(new ProcessOutput(
			    ServiceResponse.PROP_SERVICE_SPECIFIC_ERROR,
			    "Invalid input (Timestamp)"));
		    return FAILURE;
		} else {
		    tstinputValue = (Long) tstinput;
		}
		List results = db.retrieveEventsFromTstmp(sub, typ, pre, obj,
			con, exp, cop, tst, tstinputValue, scopeArray);
		ServiceResponse response = new ServiceResponse(
			CallStatus.succeeded);
		response.addOutput(new ProcessOutput(
			ContextHistoryServices.OUTPUT_EVENTS, results));
		return response;
	    } else if (operation
		    .startsWith(ContextHistoryServices.SERVICE_GET_EVENTS_TO_TIMESTAMP)) {
		log.info("handleCall",
			"Received call was SERVICE_GET_EVENTS_TO_TIMESTAMP");
		Object tstinput = call
			.getInputValue(ContextHistoryServices.INPUT_TIMESTAMP_TO);
		Long tstinputValue = Long.valueOf("0");
		if ((tstinput == null) || (!(tstinput instanceof Long))) {
		    FAILURE.addOutput(new ProcessOutput(
			    ServiceResponse.PROP_SERVICE_SPECIFIC_ERROR,
			    "Invalid input (Timestamp)"));
		    return FAILURE;
		} else {
		    tstinputValue = (Long) tstinput;
		}
		List results = db.retrieveEventsToTstmp(sub, typ, pre, obj,
			con, exp, cop, tst, tstinputValue, scopeArray);
		ServiceResponse response = new ServiceResponse(
			CallStatus.succeeded);
		response.addOutput(new ProcessOutput(
			ContextHistoryServices.OUTPUT_EVENTS, results));
		return response;
	    } else if (operation
		    .startsWith(ContextHistoryServices.SERVICE_GET_EVENTS_BETWEEN_TIMESTAMPS)) {
		log.info("handleCall",
			"Received call was SERVICE_GET_EVENTS_BETWEEN_TIMESTAMPS");
		Object tstinput1 = call
			.getInputValue(ContextHistoryServices.INPUT_TIMESTAMP_FROM);
		Object tstinput2 = call
			.getInputValue(ContextHistoryServices.INPUT_TIMESTAMP_TO);
		Long tstinput1Value = Long.valueOf("0");
		if ((tstinput1 == null) || (!(tstinput1 instanceof Long))) {
		    FAILURE.addOutput(new ProcessOutput(
			    ServiceResponse.PROP_SERVICE_SPECIFIC_ERROR,
			    "Invalid input (Timestamp)"));
		    return FAILURE;
		} else {
		    tstinput1Value = (Long) tstinput1;
		}
		Long tstinput2Value = Long.valueOf("0");
		if ((tstinput2 == null) || (!(tstinput2 instanceof Long))) {
		    FAILURE.addOutput(new ProcessOutput(
			    ServiceResponse.PROP_SERVICE_SPECIFIC_ERROR,
			    "Invalid input (Timestamp)"));
		    return FAILURE;
		} else {
		    tstinput2Value = (Long) tstinput2;
		}
		List results = db.retrieveEventsBetweenTstmp(sub, typ, pre,
			obj, con, exp, cop, tst, tstinput1Value,
			tstinput2Value, scopeArray);
		ServiceResponse response = new ServiceResponse(
			CallStatus.succeeded);
		response.addOutput(new ProcessOutput(
			ContextHistoryServices.OUTPUT_EVENTS, results));
		return response;
	    }
	}
	FAILURE.addOutput(new ProcessOutput(
		ServiceResponse.PROP_SERVICE_SPECIFIC_ERROR, "Invalid call"));
	return FAILURE;
    }

    /**
     * Get the full Resource graph of a given URI.
     * @param uri
     * @param visited initially null, or containing the URIs to be ignored.
     * @return
     */
    public Resource getFullResourceGraph(String uri, Set visited){
    	if (visited == null ){
    		visited = new HashSet();
    	}
    	String query = "DESCRIBE <" + uri + ">";
    	String serialised = db.queryBySPARQL(query, null);
    	Object o = ((MessageContentSerializerEx)Hub.getSerializer()).deserialize(serialised,uri);
    	if (!(o instanceof Resource)){
    		return null;
    	}
    	if (visited.contains(uri)){
    		return (Resource) o;
    	}
    	visited.add(uri);
    	Resource r = (Resource) o;
    	Enumeration pe = r.getPropertyURIs();
    	while (pe.hasMoreElements()) {
			String prop = (String) pe.nextElement();
			Object pv = r.getProperty(prop);
			if (pv instanceof Resource){
				r.changeProperty(prop, getFullResourceGraph(((Resource) pv).getURI(),visited));
			}
		}
    	return r;
    }
    
    /**
     * Perform SPARQL query.
     * 
     * @param input
     *            The query
     * @param scopeArray
     * @return Response
     */
    private ServiceResponse execSPARQLQuery(String input, String[] scopeArray) {
	try {
	    String results = db.queryBySPARQL(input, scopeArray);
	    if (results == null) {
		log.error("execSPARQLQuery",
			"Error executing specific SPARQL: the backend returned null");
		FAILURE.addOutput(new ProcessOutput(
			ServiceResponse.PROP_SERVICE_SPECIFIC_ERROR,
			"Null response to SPARQL"));
		return FAILURE;
	    }
	    ServiceResponse response = new ServiceResponse(CallStatus.succeeded);
	    response.addOutput(new ProcessOutput(
		    ContextHistoryServices.OUTPUT_RESULT, results));
	    return response;
	} catch (Exception e) {
	    log.error("execSPARQLQuery",
		    "Error executing specific SPARQL: {} ", e);
	    FAILURE.addOutput(new ProcessOutput(
		    ServiceResponse.PROP_SERVICE_SPECIFIC_ERROR,
		    "Error executing specific SPARQL"));
	    return FAILURE;
	}
    }

    /**
     * Perform SPARQL query.
     * 
     * @param input
     *            The query
     * @param scopeArray
     * @return Response with events
     */
    private ServiceResponse execSPARQLQueryForEvents(String input,
	    String[] scopeArray) {
	try {
	    ServiceResponse response = new ServiceResponse(CallStatus.succeeded);
	    ArrayList results = db.retrieveEventsBySPARQL(input, scopeArray);
	    if (results == null) {
		log.error("execSPARQLQueryForEvents",
			"Error executing specific SPARQL for events: the backend returned null");
		FAILURE.addOutput(new ProcessOutput(
			ServiceResponse.PROP_SERVICE_SPECIFIC_ERROR,
			"Null response to SPARQL"));
		return FAILURE;
	    }
	    response.addOutput(new ProcessOutput(
		    ContextHistoryServices.OUTPUT_EVENTS, results));
	    return response;
	} catch (Exception e) {
	    log.error("execSPARQLQueryForEvents",
		    "Error executing SPARQL for events: {} ", e);
	    FAILURE.addOutput(new ProcessOutput(
		    ServiceResponse.PROP_SERVICE_SPECIFIC_ERROR,
		    "Error executing specific SPARQL"));
	    return FAILURE;
	}
    }

}
