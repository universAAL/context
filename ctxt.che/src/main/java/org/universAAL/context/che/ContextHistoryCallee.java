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
package org.universAAL.context.che;

import java.util.ArrayList;
import java.util.List;

import org.universAAL.context.che.Hub.Log;
import org.universAAL.context.che.database.Backend;
import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.context.owl.ContextProvider;
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
    private static final ServiceResponse INVALID_INPUT = new ServiceResponse(
	    CallStatus.serviceSpecificFailure);
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
     * @param db
     *            The store
     */
    ContextHistoryCallee(ModuleContext context, Backend db) {
	super(context, ContextHistoryServices.PROFILES);
	this.db = db;
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
	    INVALID_INPUT
		    .addOutput(new ProcessOutput(
			    ServiceResponse.PROP_SERVICE_SPECIFIC_ERROR,
			    "Corrupt call"));
	    return INVALID_INPUT;
	}

	String operation = call.getProcessURI();
	if (operation == null) {
	    INVALID_INPUT
		    .addOutput(new ProcessOutput(
			    ServiceResponse.PROP_SERVICE_SPECIFIC_ERROR,
			    "Corrupt call"));
	    return INVALID_INPUT;
	}

	if (operation
		.startsWith(ContextHistoryServices.SERVICE_DO_SPARQL_QUERY)) {
	    log.info("handleCall", "Received call was SERVICE_DO_SPARQL_QUERY");

	    Object input = call
		    .getInputValue(ContextHistoryServices.INPUT_QUERY);
	    if (input == null) {
		INVALID_INPUT.addOutput(new ProcessOutput(
			ServiceResponse.PROP_SERVICE_SPECIFIC_ERROR,
			"Invalid input"));
		return INVALID_INPUT;
	    }
	    return execSPARQLQuery((String) input);

	} else if (operation
		.startsWith(ContextHistoryServices.SERVICE_GET_EVENTS_BY_SPARQL)) {
	    log.info("handleCall",
		    "Received call was SERVICE_GET_EVENTS_BY_SPARQL");

	    Object input = call
		    .getInputValue(ContextHistoryServices.INPUT_QUERY);
	    if (input == null) {
		INVALID_INPUT.addOutput(new ProcessOutput(
			ServiceResponse.PROP_SERVICE_SPECIFIC_ERROR,
			"Invalid input"));
		return INVALID_INPUT;
	    }

	    return execSPARQLQueryForEvents((String) input);

	} else {

	    Object input = call
		    .getInputValue(ContextHistoryServices.INPUT_EVENT);
	    ContextEvent inputevent;

	    if ((input == null) || (!(input instanceof ContextEvent))) {
		INVALID_INPUT.addOutput(new ProcessOutput(
			ServiceResponse.PROP_SERVICE_SPECIFIC_ERROR,
			"Invalid input (ContextEvent)"));
		return INVALID_INPUT;
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
		    INVALID_INPUT.addOutput(new ProcessOutput(
			    ServiceResponse.PROP_SERVICE_SPECIFIC_ERROR,
			    "Invalid input (Timestamp)"));
		    return INVALID_INPUT;
		} else {
		    tstinputValue = (Long) tstinput;
		}
		List results = db.retrieveEventsFromTstmp(sub, typ, pre, obj,
			con, exp, cop, tst, tstinputValue);
		ServiceResponse response = new ServiceResponse(
			CallStatus.succeeded);
		response.addOutput(new ProcessOutput(
			ContextHistoryServices.OUTPUT_EVENTS, results));
		return response;
	    }

	    else if (operation
		    .startsWith(ContextHistoryServices.SERVICE_GET_EVENTS_TO_TIMESTAMP)) {
		log.info("handleCall",
			"Received call was SERVICE_GET_EVENTS_TO_TIMESTAMP");
		Object tstinput = call
			.getInputValue(ContextHistoryServices.INPUT_TIMESTAMP_TO);
		Long tstinputValue = Long.valueOf("0");
		if ((tstinput == null) || (!(tstinput instanceof Long))) {
		    INVALID_INPUT.addOutput(new ProcessOutput(
			    ServiceResponse.PROP_SERVICE_SPECIFIC_ERROR,
			    "Invalid input (Timestamp)"));
		    return INVALID_INPUT;
		} else {
		    tstinputValue = (Long) tstinput;
		}
		List results = db.retrieveEventsToTstmp(sub, typ, pre, obj,
			con, exp, cop, tst, tstinputValue);
		ServiceResponse response = new ServiceResponse(
			CallStatus.succeeded);
		response.addOutput(new ProcessOutput(
			ContextHistoryServices.OUTPUT_EVENTS, results));
		return response;
	    }

	    else if (operation
		    .startsWith(ContextHistoryServices.SERVICE_GET_EVENTS_BETWEEN_TIMESTAMPS)) {
		log.info("handleCall",
			"Received call was SERVICE_GET_EVENTS_BETWEEN_TIMESTAMPS");
		Object tstinput1 = call
			.getInputValue(ContextHistoryServices.INPUT_TIMESTAMP_FROM);
		Object tstinput2 = call
			.getInputValue(ContextHistoryServices.INPUT_TIMESTAMP_TO);
		Long tstinput1Value = Long.valueOf("0");
		if ((tstinput1 == null) || (!(tstinput1 instanceof Long))) {
		    INVALID_INPUT.addOutput(new ProcessOutput(
			    ServiceResponse.PROP_SERVICE_SPECIFIC_ERROR,
			    "Invalid input (Timestamp)"));
		    return INVALID_INPUT;
		} else {
		    tstinput1Value = (Long) tstinput1;
		}
		Long tstinput2Value = Long.valueOf("0");
		if ((tstinput2 == null) || (!(tstinput2 instanceof Long))) {
		    INVALID_INPUT.addOutput(new ProcessOutput(
			    ServiceResponse.PROP_SERVICE_SPECIFIC_ERROR,
			    "Invalid input (Timestamp)"));
		    return INVALID_INPUT;
		} else {
		    tstinput2Value = (Long) tstinput2;
		}
		List results = db
			.retrieveEventsBetweenTstmp(sub, typ, pre, obj, con,
				exp, cop, tst, tstinput1Value, tstinput2Value);
		ServiceResponse response = new ServiceResponse(
			CallStatus.succeeded);
		response.addOutput(new ProcessOutput(
			ContextHistoryServices.OUTPUT_EVENTS, results));
		return response;
	    }

	}
	INVALID_INPUT.addOutput(new ProcessOutput(
		ServiceResponse.PROP_SERVICE_SPECIFIC_ERROR, "Invalid call"));
	return INVALID_INPUT;
    }

    /**
     * Perform SPARQL query.
     * 
     * @param input
     *            The query
     * @return Response
     */
    private ServiceResponse execSPARQLQuery(String input) {
	try {
	    String results = db.queryBySPARQL(input);
	    ServiceResponse response = new ServiceResponse(CallStatus.succeeded);
	    response.addOutput(new ProcessOutput(
		    ContextHistoryServices.OUTPUT_RESULT, results));
	    return response;
	} catch (Exception e) {
	    log.error("execSPARQLQuery",
		    "Error executing specific SPARQL: {} ", e);
	    INVALID_INPUT.addOutput(new ProcessOutput(
		    ServiceResponse.PROP_SERVICE_SPECIFIC_ERROR,
		    "Error executing specific SPARQL"));
	    return INVALID_INPUT;
	}
    }

    /**
     * Perform SPARQL query.
     * 
     * @param input
     *            The query
     * @return Response with events
     */
    private ServiceResponse execSPARQLQueryForEvents(String input) {
	try {
	    ServiceResponse response = new ServiceResponse(CallStatus.succeeded);
	    ArrayList results = db.retrieveEventsBySPARQL(input);
	    response.addOutput(new ProcessOutput(
		    ContextHistoryServices.OUTPUT_EVENTS, results));
	    return response;
	} catch (Exception e) {
	    log.error("execSPARQLQueryForEvents",
		    "Error executing SPARQL for events: {} ", e);
	    INVALID_INPUT.addOutput(new ProcessOutput(
		    ServiceResponse.PROP_SERVICE_SPECIFIC_ERROR,
		    "Error executing specific SPARQL"));
	    return INVALID_INPUT;
	}
    }

}
