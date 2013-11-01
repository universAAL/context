/*	
	Copyright 2008-2014 Fraunhofer IGD, http://www.igd.fraunhofer.de
	Fraunhofer Gesellschaft - Institut für Graphische Datenverarbeitung 
	
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

import java.util.Hashtable;

import org.universAAL.context.reasoner.osgi.Activator;
import org.universAAL.middleware.owl.MergedRestriction;
import org.universAAL.middleware.owl.OntologyManagement;
import org.universAAL.middleware.owl.SimpleOntology;
import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.rdf.ResourceFactory;
import org.universAAL.middleware.service.owls.profile.ServiceProfile;
import org.universAAL.ontology.reasoner.Query;
import org.universAAL.ontology.reasoner.ReasoningService;
import org.universAAL.ontology.reasoner.Rule;
import org.universAAL.ontology.reasoner.Situation;

/**
 * This implementation of ReasoningService contains all methods currently
 * supported by the Reasoner. This basically means to add/delete/get the
 * Situations, Queries and Rules handled by the reasoner. In fact all of the
 * methods are quite similar, since in every case only one object of the
 * ontology have to be handled.
 * 
 * @author amarinc
 * 
 */
public class ProvidedReasoningService extends ReasoningService {

    public static final String REASONER_SERVER_NAMESPACE = "http://ontology.igd.fhg.de/ReasonerSituationService.owl#";
    public static final String MY_URI = REASONER_SERVER_NAMESPACE
	    + "ReasonerSituationService";

    static final String SERVICE_GET_SITUATIONS = REASONER_SERVER_NAMESPACE
	    + "getSituations";
    static final String SERVICE_ADD_SITUATION = REASONER_SERVER_NAMESPACE
	    + "addSituation";
    static final String SERVICE_REMOVE_SITUATION = REASONER_SERVER_NAMESPACE
	    + "removeSituation";
    static final String SERVICE_GET_QUERIES = REASONER_SERVER_NAMESPACE
	    + "getQueries";
    static final String SERVICE_ADD_QUERY = REASONER_SERVER_NAMESPACE
	    + "addQuerie";
    static final String SERVICE_REMOVE_QUERY = REASONER_SERVER_NAMESPACE
	    + "removeQuery";
    static final String SERVICE_GET_RULES = REASONER_SERVER_NAMESPACE
	    + "getRule";
    static final String SERVICE_ADD_RULE = REASONER_SERVER_NAMESPACE
	    + "addRule";
    static final String SERVICE_REMOVE_RULE = REASONER_SERVER_NAMESPACE
	    + "removeRule";

    static final String INPUT_SITUATION = REASONER_SERVER_NAMESPACE
	    + "oneSituation";
    static final String INPUT_QUERY = REASONER_SERVER_NAMESPACE + "oneQuery";
    static final String INPUT_RULE = REASONER_SERVER_NAMESPACE + "oneRule";

    static final String OUTPUT_SITUATIONS = REASONER_SERVER_NAMESPACE
	    + "situations";
    static final String OUTPUT_QUERIES = REASONER_SERVER_NAMESPACE + "queries";
    static final String OUTPUT_RULES = REASONER_SERVER_NAMESPACE + "rules";

    static final ServiceProfile[] profiles = new ServiceProfile[9];

    @SuppressWarnings("unchecked")
    private static Hashtable serverLightingRestrictions = new Hashtable();
    static {
	OntologyManagement.getInstance().register(
		Activator.mcontext,
		new SimpleOntology(MY_URI, ReasoningService.MY_URI,
			new ResourceFactory() {
			    public Resource createInstance(String classURI,
				    String instanceURI, int factoryIndex) {
				return new ProvidedReasoningService(instanceURI);
			    }
			}));

	String[] ppSituations = new String[] { ReasoningService.PROP_SITUATIONS };
	String[] ppQueries = new String[] { ReasoningService.PROP_QUERIES };
	String[] ppRules = new String[] { ReasoningService.PROP_RULES };

	addRestriction(
		(MergedRestriction) ReasoningService
			.getClassRestrictionsOnProperty(
				ReasoningService.MY_URI,
				ReasoningService.PROP_SITUATIONS).copy(),
		ppSituations, serverLightingRestrictions);

	ProvidedReasoningService getSituations = new ProvidedReasoningService(
		SERVICE_GET_SITUATIONS);
	getSituations.addOutput(OUTPUT_SITUATIONS, ReasoningService.MY_URI, 0,
		0, ppSituations);
	profiles[0] = getSituations.myProfile;

	ProvidedReasoningService addSituation = new ProvidedReasoningService(
		SERVICE_ADD_SITUATION);
	addSituation.addInputWithAddEffect(INPUT_SITUATION, Situation.MY_URI,
		1, 1, ppSituations);
	profiles[1] = addSituation.myProfile;

	ProvidedReasoningService removeSituation = new ProvidedReasoningService(
		SERVICE_REMOVE_SITUATION);
	removeSituation.addInputWithRemoveEffect(INPUT_SITUATION,
		Situation.MY_URI, 1, 1, ppSituations);
	profiles[2] = removeSituation.myProfile;

	ProvidedReasoningService getQueries = new ProvidedReasoningService(
		SERVICE_GET_QUERIES);
	getQueries.addOutput(OUTPUT_QUERIES, ReasoningService.MY_URI, 0, 0,
		ppQueries);
	profiles[3] = getQueries.myProfile;

	ProvidedReasoningService addQuery = new ProvidedReasoningService(
		SERVICE_ADD_QUERY);
	addQuery.addInputWithAddEffect(INPUT_QUERY, Query.MY_URI, 1, 1,
		ppQueries);
	profiles[4] = addQuery.myProfile;

	ProvidedReasoningService removeQuery = new ProvidedReasoningService(
		SERVICE_REMOVE_QUERY);
	removeQuery.addInputWithRemoveEffect(INPUT_QUERY, Query.MY_URI, 1, 1,
		ppQueries);
	profiles[5] = removeQuery.myProfile;

	ProvidedReasoningService getRules = new ProvidedReasoningService(
		SERVICE_GET_RULES);
	getRules.addOutput(OUTPUT_RULES, ReasoningService.MY_URI, 0, 0, ppRules);
	profiles[6] = getRules.myProfile;

	ProvidedReasoningService addRule = new ProvidedReasoningService(
		SERVICE_ADD_RULE);
	addRule.addInputWithAddEffect(INPUT_RULE, Rule.MY_URI, 1, 1, ppRules);
	profiles[7] = addRule.myProfile;

	ProvidedReasoningService removeRule = new ProvidedReasoningService(
		SERVICE_REMOVE_RULE);
	removeRule.addInputWithRemoveEffect(INPUT_RULE, Rule.MY_URI, 1, 1,
		ppRules);
	profiles[8] = removeRule.myProfile;

    }

    private ProvidedReasoningService(String uri) {
	super(uri);
    }

    public String getClassURI() {
	return MY_URI;
    }
}
