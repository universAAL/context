/*
	Copyright 2008-2011 ITACA-TSB, http://www.tsb.upv.es
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.universAAL.context.che.database.Backend;
import org.universAAL.context.che.ontology.ContextEvent;
import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.context.owl.ContextProvider;
import org.universAAL.middleware.service.CallStatus;
import org.universAAL.middleware.service.ServiceCall;
import org.universAAL.middleware.service.ServiceCallee;
import org.universAAL.middleware.service.ServiceResponse;
import org.universAAL.middleware.service.owls.process.ProcessOutput;

/**
 * The CHe service callee receives all service calls issued to the CHe through
 * the service bus
 * 
 * @author <a href="mailto:alfiva@itaca.upv.es">Alvaro Fides Valero</a>
 * 
 */
public class ContextHistoryCallee extends ServiceCallee {
    private static final ServiceResponse invalidInput = new ServiceResponse(
	    CallStatus.serviceSpecificFailure);
    private final static Logger log = LoggerFactory
	    .getLogger(ContextHistoryCallee.class);

    static {
	invalidInput.addOutput(new ProcessOutput(
		ServiceResponse.PROP_SERVICE_SPECIFIC_ERROR, "Invalid input!"));
    }

    private Backend db;

    ContextHistoryCallee(ModuleContext context, Backend db) {
	super(context, ContextHistoryServices.profiles);
	this.db = db;
    }

    public void communicationChannelBroken() {
	// TODO Auto-generated method stub

    }

    public ServiceResponse handleCall(ServiceCall call) {
	log.info("CHe received a service call");
	if (call == null) {
	    invalidInput
		    .addOutput(new ProcessOutput(
			    ServiceResponse.PROP_SERVICE_SPECIFIC_ERROR,
			    "Corrupt call"));
	    return invalidInput;
	}

	String operation = call.getProcessURI();
	if (operation == null) {
	    invalidInput
		    .addOutput(new ProcessOutput(
			    ServiceResponse.PROP_SERVICE_SPECIFIC_ERROR,
			    "Corrupt call"));
	    return invalidInput;
	}

	if (operation
		.startsWith(ContextHistoryServices.SERVICE_DO_SPARQL_QUERY)) {
	    log.info("Received call was SERVICE_DO_SPARQL_QUERY");

	    Object input = call
		    .getInputValue(ContextHistoryServices.INPUT_QUERY);
	    if (input == null) {
		invalidInput.addOutput(new ProcessOutput(
			ServiceResponse.PROP_SERVICE_SPECIFIC_ERROR,
			"Invalid input"));
		return invalidInput;
	    }
	    return execSPARQLQuery((String) input);

	} else if (operation
		.startsWith(ContextHistoryServices.SERVICE_GET_EVENTS_BY_SPARQL)) {
	    log.info("Received call was SERVICE_GET_EVENTS_BY_SPARQL");

	    Object input = call
		    .getInputValue(ContextHistoryServices.INPUT_QUERY);
	    if (input == null) {
		invalidInput.addOutput(new ProcessOutput(
			ServiceResponse.PROP_SERVICE_SPECIFIC_ERROR,
			"Invalid input"));
		return invalidInput;
	    }

	    return execSPARQLQueryForEvents((String) input);

	} else {

	    Object input = call
		    .getInputValue(ContextHistoryServices.INPUT_EVENT);
	    ContextEvent inputevent;

	    if ((input == null) || (!(input instanceof ContextEvent))) {
		invalidInput.addOutput(new ProcessOutput(
			ServiceResponse.PROP_SERVICE_SPECIFIC_ERROR,
			"Invalid input (ContextEvent)"));
		return invalidInput;
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
		log.info("Received call was SERVICE_GET_EVENTS_FROM_TIMESTAMP");

		Object tstinput = call
			.getInputValue(ContextHistoryServices.INPUT_TIMESTAMP_FROM);
		Long tstinputValue = new Long("0");
		if ((tstinput == null) || (!(tstinput instanceof Long))) {
		    invalidInput.addOutput(new ProcessOutput(
			    ServiceResponse.PROP_SERVICE_SPECIFIC_ERROR,
			    "Invalid input (Timestamp)"));
		    return invalidInput;
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
		log.info("Received call was SERVICE_GET_EVENTS_TO_TIMESTAMP");
		Object tstinput = call
			.getInputValue(ContextHistoryServices.INPUT_TIMESTAMP_TO);
		Long tstinputValue = new Long("0");
		if ((tstinput == null) || (!(tstinput instanceof Long))) {
		    invalidInput.addOutput(new ProcessOutput(
			    ServiceResponse.PROP_SERVICE_SPECIFIC_ERROR,
			    "Invalid input (Timestamp)"));
		    return invalidInput;
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
		log
			.info("Received call was SERVICE_GET_EVENTS_BETWEEN_TIMESTAMPS");
		Object tstinput1 = call
			.getInputValue(ContextHistoryServices.INPUT_TIMESTAMP_FROM);
		Object tstinput2 = call
			.getInputValue(ContextHistoryServices.INPUT_TIMESTAMP_TO);
		Long tstinput1Value = new Long("0");
		if ((tstinput1 == null) || (!(tstinput1 instanceof Long))) {
		    invalidInput.addOutput(new ProcessOutput(
			    ServiceResponse.PROP_SERVICE_SPECIFIC_ERROR,
			    "Invalid input (Timestamp)"));
		    return invalidInput;
		} else {
		    tstinput1Value = (Long) tstinput1;
		}
		Long tstinput2Value = new Long("0");
		if ((tstinput2 == null) || (!(tstinput2 instanceof Long))) {
		    invalidInput.addOutput(new ProcessOutput(
			    ServiceResponse.PROP_SERVICE_SPECIFIC_ERROR,
			    "Invalid input (Timestamp)"));
		    return invalidInput;
		} else {
		    tstinput2Value = (Long) tstinput2;
		}
		List results = db.retrieveEventsBetweenTstmp(sub, typ, pre,
			obj, con, exp, cop, tst, tstinput1Value,
			tstinput2Value);
		ServiceResponse response = new ServiceResponse(
			CallStatus.succeeded);
		response.addOutput(new ProcessOutput(
			ContextHistoryServices.OUTPUT_EVENTS, results));
		return response;
	    }

	}
	invalidInput.addOutput(new ProcessOutput(
		ServiceResponse.PROP_SERVICE_SPECIFIC_ERROR, "Invalid call"));
	return invalidInput;
    }

    private ServiceResponse execSPARQLQuery(String input) {
	try {
	    String results = db.queryBySPARQL(input);
	    ServiceResponse response = new ServiceResponse(CallStatus.succeeded);
	    response.addOutput(new ProcessOutput(
		    ContextHistoryServices.OUTPUT_RESULT, results));
	    return response;
	} catch (Exception e) {
	    log.error("Error executing specific SPARQL: {} ", e);
	    return invalidInput;
	}
    }

    private ServiceResponse execSPARQLQueryForEvents(String input) {
	try {
	    ServiceResponse response = new ServiceResponse(CallStatus.succeeded);
	    ArrayList results = db.retrieveEventsBySPARQL(input);
	    response.addOutput(new ProcessOutput(
		    ContextHistoryServices.OUTPUT_EVENTS, results));
	    return response;
	} catch (Exception e) {
	    log.error("Error executing SPARQL for events: {} ", e);
	    return invalidInput;
	}
    }

}
