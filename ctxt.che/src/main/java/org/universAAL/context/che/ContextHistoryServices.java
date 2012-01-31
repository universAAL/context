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

import org.universAAL.context.che.ontology.ContextEvent;
import org.universAAL.context.che.ontology.ContextHistoryService;
import org.universAAL.middleware.owl.MergedRestriction;
import org.universAAL.middleware.rdf.PropertyPath;
import org.universAAL.middleware.rdf.TypeMapper;
import org.universAAL.middleware.service.owls.process.ProcessInput;
import org.universAAL.middleware.service.owls.process.ProcessOutput;
import org.universAAL.middleware.service.owls.profile.ServiceProfile;

/**
 * Here are described the provided services that are registered by the CHe in
 * the service bus
 * 
 * @author <a href="mailto:alfiva@itaca.upv.es">Alvaro Fides Valero</a>
 * 
 */
public class ContextHistoryServices extends ContextHistoryService {

    public static final String CHE_NAMESPACE = "http://ontology.universAAL.org/CHE.owl#";
    public static final String MY_URI = CHE_NAMESPACE + "CHeService";

    static final String SERVICE_GET_EVENTS_FROM_TIMESTAMP = CHE_NAMESPACE
	    + "getEventsFromTimestamp";
    static final String SERVICE_GET_EVENTS_TO_TIMESTAMP = CHE_NAMESPACE
	    + "getEventsToTimestamp";
    static final String SERVICE_GET_EVENTS_BETWEEN_TIMESTAMPS = CHE_NAMESPACE
	    + "getEventsBetweenTimestamps";
    static final String SERVICE_DO_SPARQL_QUERY = CHE_NAMESPACE
	    + "doSparqlQuery";
    static final String SERVICE_GET_EVENTS_BY_SPARQL = CHE_NAMESPACE
	    + "getEventsBySparql";

    static final String INPUT_EVENT = CHE_NAMESPACE + "matchContextEvent";
    static final String INPUT_QUERY = CHE_NAMESPACE + "sparqlQuery";
    static final String INPUT_TIMESTAMP_FROM = CHE_NAMESPACE + "timestampFrom";
    static final String INPUT_TIMESTAMP_TO = CHE_NAMESPACE + "timestampTo";

    static final String OUTPUT_EVENTS = CHE_NAMESPACE + "matchingContextEvents";
    static final String OUTPUT_RESULT = CHE_NAMESPACE + "sparqlResult";

    static final ServiceProfile[] profiles = new ServiceProfile[5];

    static {
	ProcessInput eventInput = new ProcessInput(INPUT_EVENT);
	eventInput.setParameterType(ContextEvent.MY_URI);
	eventInput.setCardinality(1, 1);

	ProcessInput queryInput = new ProcessInput(INPUT_QUERY);
	queryInput.setParameterType(TypeMapper.getDatatypeURI(String.class));
	queryInput.setCardinality(1, 1);

	ProcessInput tstFromInput = new ProcessInput(INPUT_TIMESTAMP_FROM);
	tstFromInput.setParameterType(TypeMapper.getDatatypeURI(Long.class));
	tstFromInput.setCardinality(1, 1);

	ProcessInput tstToInput = new ProcessInput(INPUT_TIMESTAMP_TO);
	tstToInput.setParameterType(TypeMapper.getDatatypeURI(Long.class));
	tstToInput.setCardinality(1, 1);

	MergedRestriction eventRestr = MergedRestriction
		.getFixedValueRestriction(ContextHistoryService.PROP_MANAGES,
			eventInput.asVariableReference());

	MergedRestriction queryr = MergedRestriction.getFixedValueRestriction(
		ContextHistoryService.PROP_PROCESSES,
		queryInput.asVariableReference());

	MergedRestriction tstFromRestr = MergedRestriction
		.getFixedValueRestriction(
			ContextHistoryService.PROP_TIMESTAMP_FROM,
			tstFromInput.asVariableReference());

	MergedRestriction tstToRestr = MergedRestriction
		.getFixedValueRestriction(
			ContextHistoryService.PROP_TIMESTAMP_TO,
			tstToInput.asVariableReference());

	ProcessOutput output = new ProcessOutput(OUTPUT_EVENTS);
	output.setParameterType(ContextEvent.MY_URI);
	output.setCardinality(-1, 0);

	ProcessOutput resultoutput = new ProcessOutput(OUTPUT_RESULT);
	resultoutput.setParameterType(TypeMapper.getDatatypeURI(String.class));
	resultoutput.setCardinality(1, 1);

	PropertyPath managesPath = new PropertyPath(null, false,
		new String[] { ContextHistoryService.PROP_MANAGES });

	PropertyPath returnsPath = new PropertyPath(null, true,
		new String[] { ContextHistoryService.PROP_RETURNS });

	// SPARQL_QUERY
	ContextHistoryServices doSPARQL = new ContextHistoryServices(
		SERVICE_DO_SPARQL_QUERY);
	profiles[0] = doSPARQL.getProfile();
	profiles[0].addInput(queryInput);
	doSPARQL.addInstanceLevelRestriction(queryr,
		new String[] { ContextHistoryService.PROP_PROCESSES });
	profiles[0].addOutput(resultoutput);
	profiles[0].addSimpleOutputBinding(resultoutput,
		returnsPath.getThePath());

	// GET_EVENTS_FROM_TIMESTAMP
	ContextHistoryServices getEventsFromTimestamp = new ContextHistoryServices(
		SERVICE_GET_EVENTS_FROM_TIMESTAMP);
	profiles[1] = getEventsFromTimestamp.getProfile();
	profiles[1].addInput(eventInput);
	getEventsFromTimestamp.addInstanceLevelRestriction(eventRestr,
		new String[] { ContextHistoryService.PROP_MANAGES });
	profiles[1].addInput(tstFromInput);
	getEventsFromTimestamp.addInstanceLevelRestriction(tstFromRestr,
		new String[] { ContextHistoryService.PROP_TIMESTAMP_FROM });
	profiles[1].addOutput(output);
	profiles[1].addSimpleOutputBinding(output, managesPath.getThePath());

	// GET_EVENTS_TO_TIMESTAMP
	ContextHistoryServices getEventsToTimestamp = new ContextHistoryServices(
		SERVICE_GET_EVENTS_TO_TIMESTAMP);
	profiles[2] = getEventsToTimestamp.getProfile();
	profiles[2].addInput(eventInput);
	getEventsToTimestamp.addInstanceLevelRestriction(eventRestr,
		new String[] { ContextHistoryService.PROP_MANAGES });
	profiles[2].addInput(tstToInput);
	getEventsToTimestamp.addInstanceLevelRestriction(tstToRestr,
		new String[] { ContextHistoryService.PROP_TIMESTAMP_TO });
	profiles[2].addOutput(output);
	profiles[2].addSimpleOutputBinding(output, managesPath.getThePath());

	// GET_EVENTS_BETWEEN_TIMESTAMPS
	ContextHistoryServices getEventsBetweenTimestamp = new ContextHistoryServices(
		SERVICE_GET_EVENTS_BETWEEN_TIMESTAMPS);
	profiles[3] = getEventsBetweenTimestamp.getProfile();
	profiles[3].addInput(eventInput);
	getEventsBetweenTimestamp.addInstanceLevelRestriction(eventRestr,
		new String[] { ContextHistoryService.PROP_MANAGES });
	profiles[3].addInput(tstFromInput);
	getEventsBetweenTimestamp.addInstanceLevelRestriction(tstFromRestr,
		new String[] { ContextHistoryService.PROP_TIMESTAMP_FROM });
	profiles[3].addInput(tstToInput);
	getEventsBetweenTimestamp.addInstanceLevelRestriction(tstToRestr,
		new String[] { ContextHistoryService.PROP_TIMESTAMP_TO });
	profiles[3].addOutput(output);
	profiles[3].addSimpleOutputBinding(output, managesPath.getThePath());

	// GET_EVENTS_BY_SPARQL_QUERY
	ContextHistoryServices doSPARQLforEvents = new ContextHistoryServices(
		SERVICE_GET_EVENTS_BY_SPARQL);
	profiles[4] = doSPARQLforEvents.getProfile();
	profiles[4].addInput(queryInput);
	doSPARQLforEvents.addInstanceLevelRestriction(queryr,
		new String[] { ContextHistoryService.PROP_PROCESSES });
	profiles[4].addOutput(output);
	profiles[4].addSimpleOutputBinding(output, managesPath.getThePath());

    }

    protected ContextHistoryServices(String uri) {
	super(uri);
    }

}
