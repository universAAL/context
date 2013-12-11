/*
	Copyright 2008-2014 TSB, http://www.tsbtecnologias.es
	TSB - Tecnolog�as para la Salud y el Bienestar
	
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
package org.universAAL.drools;

import org.universAAL.middleware.owl.MergedRestriction;
import org.universAAL.middleware.owl.OntologyManagement;
import org.universAAL.middleware.owl.SimpleOntology;
import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.rdf.TypeMapper;
import org.universAAL.middleware.rdf.ResourceFactory;
import org.universAAL.middleware.service.owls.process.ProcessInput;
import org.universAAL.middleware.service.owls.profile.ServiceProfile;
import org.universAAL.ontology.drools.DroolsReasoning;
import org.universAAL.ontology.drools.Fact;
import org.universAAL.ontology.drools.Rule;

/**
 * Service descriptor for the rule engine. It defines the access method to the
 * rules engine in order to add, modify or remove rules, or facts.
 * 
 * @author Miguel Llorente (mllorente)
 */
public class ProvidedDroolsReasonerService extends DroolsReasoning {

	// Naming
	public static final String DROOLS_REASONER_NAMESPACE = "http://www.tsbtecnologias.es/DroolsReasoner.owl#";
	public static final String MY_URI = DROOLS_REASONER_NAMESPACE
			+ "ProvidedDroolsReasonerService";
	// Rule managing
	public static final String SERVICE_ADD_RULE = DROOLS_REASONER_NAMESPACE
			+ "addRule";
	public static final String SERVICE_REMOVE_RULE = DROOLS_REASONER_NAMESPACE
			+ "removeRule";
	public static final String SERVICE_MODIFY_RULE = DROOLS_REASONER_NAMESPACE
			+ "modifyRule";
	// Fact managing
	public static final String SERVICE_ADD_FACT = DROOLS_REASONER_NAMESPACE
			+ "addFact";
	public static final String SERVICE_REMOVE_FACT = DROOLS_REASONER_NAMESPACE
			+ "removeFact";
	public static final String SERVICE_MODIFY_FACT = DROOLS_REASONER_NAMESPACE
			+ "modifyFact";
	// Consequence managing
	public static final String SERVICE_FIRE_CONSEQUENCE = DROOLS_REASONER_NAMESPACE
			+ "fireConsequence";
	// LTBA ON
	public static final String SERVICE_SWITCH_ON = DROOLS_REASONER_NAMESPACE
			+ "switchOn";
	// LTBA OFF
	public static final String SERVICE_SWITCH_OFF = DROOLS_REASONER_NAMESPACE
			+ "switchOff";
	// Input rule
	public static final String INPUT_RULE = DROOLS_REASONER_NAMESPACE
			+ "inputRule";
	// Input fact
	public static final String INPUT_FACT = DROOLS_REASONER_NAMESPACE
			+ "inputFact";
	// Input ruleID
	public static final String INPUT_RULE_ID = DROOLS_REASONER_NAMESPACE
			+ "inputRuleId";
	// Input factID
	public static final String INPUT_FACT_ID = DROOLS_REASONER_NAMESPACE
			+ "inputFactId";
	// Classes for storing info
	static final ServiceProfile[] profiles = new ServiceProfile[8];
	// private static Hashtable droolsReasonerRestrictions = new Hashtable();
	// Registration -- not so sure of what the following code actually does.
	static {
		OntologyManagement.getInstance().register(
				Activator.mc,
				new SimpleOntology(MY_URI, DroolsReasoning.MY_URI,
						new ResourceFactory() {
							public Resource createInstance(String classURI,
									String instanceURI, int factoryIndex) {
								return new ProvidedDroolsReasonerService(
										instanceURI);
							}
						}));
		// AddRule

		ProvidedDroolsReasonerService addRule = new ProvidedDroolsReasonerService(
				SERVICE_ADD_RULE);

		// Lighting l = new Lighting(SERVICE_ADD_RULE);
		// ProcessInput input1 = new ProcessInput(INPUT_RULE);
		// input1.setParameterType(LightSource.MY_URI);
		// input1.setCardinality(1, 1);
		// l.getProfile().addInput(input1);
		// l.getProfile().addChangeEffect(new String[]{Lighting.PROP_CONTROLS},
		// input1.asVariableReference() );
		// profiles[0] = l.getProfile();
		ProcessInput input1 = new ProcessInput(INPUT_RULE);
		input1.setParameterType(Rule.MY_URI);
		// input1.setCardinality(1, 1);
		// addRule.getProfile().addInput(input1);
		addRule.addFilteringInput(INPUT_RULE, Rule.MY_URI, 1, 1,
				new String[] { DroolsReasoning.PROP_KNOWS_RULES });
		// MergedRestriction restr1 =
		// MergedRestriction.getFixedValueRestriction(DroolsReasoning.PROP_KNOWS_RULES,
		// input1.asVariableReference());
		// addRule.addInstanceLevelRestriction(restr1, new
		// String[]{DroolsReasoning.PROP_KNOWS_RULES});
		// Not sure if the next line works as I supose
		addRule.myProfile.addAddEffect(
				new String[] { DroolsReasoning.PROP_KNOWS_RULES }, input1);
		profiles[0] = addRule.myProfile;
		// RemoveRule
		ProvidedDroolsReasonerService removeRule = new ProvidedDroolsReasonerService(
				SERVICE_REMOVE_RULE);
		// removeRule.addFilteringInput(INPUT_RULE_ID ,
		// TypeMapper.getDatatypeURI(String.class), 1, 1, new
		// String[]{DroolsReasoning.PROP_KNOWS_RULES});
		// removeRule.myProfile.addRemoveEffect(new
		// String[]{DroolsReasoning.PROP_KNOWS_RULES});
		ProcessInput input2 = new ProcessInput(INPUT_RULE_ID);
		input2.setParameterType(TypeMapper.getDatatypeURI(String.class));
		input2.setCardinality(1, 1);
		removeRule.getProfile().addInput(input2);
		MergedRestriction restr2 = MergedRestriction.getFixedValueRestriction(
				Rule.PROP_HAS_IDENTIFIER, input2.asVariableReference());
		removeRule.addInstanceLevelRestriction(restr2, new String[] {
				DroolsReasoning.PROP_KNOWS_RULES, Rule.PROP_HAS_IDENTIFIER });
		removeRule.getProfile().addRemoveEffect(
				new String[] { DroolsReasoning.PROP_KNOWS_RULES,
						Rule.PROP_HAS_IDENTIFIER });
		profiles[1] = removeRule.myProfile;

		// ModifyRule
		ProvidedDroolsReasonerService modifyRule = new ProvidedDroolsReasonerService(
				SERVICE_MODIFY_RULE);
		ProcessInput input3 = new ProcessInput(INPUT_RULE);
		input3.setParameterType(Rule.MY_URI);
		modifyRule.addFilteringInput(INPUT_RULE, Rule.MY_URI, 1, 1,
				new String[] { DroolsReasoning.PROP_KNOWS_RULES });
		modifyRule.myProfile.addChangeEffect(
				new String[] { DroolsReasoning.PROP_KNOWS_RULES }, input3);
		profiles[2] = modifyRule.myProfile;

		// AddFact
		ProvidedDroolsReasonerService addFact = new ProvidedDroolsReasonerService(
				SERVICE_ADD_FACT);
		ProcessInput input4 = new ProcessInput(INPUT_FACT);
		input4.setParameterType(Fact.MY_URI);
		addFact.addFilteringInput(INPUT_FACT, Fact.MY_URI, 1, 1,
				new String[] { DroolsReasoning.PROP_KNOWS_FACTS });
		addFact.myProfile.addAddEffect(
				new String[] { DroolsReasoning.PROP_KNOWS_FACTS }, input4);
		profiles[3] = addFact.myProfile;

		// ModifyFact
		ProvidedDroolsReasonerService modifyFact = new ProvidedDroolsReasonerService(
				SERVICE_MODIFY_FACT);
		ProcessInput input6 = new ProcessInput(INPUT_FACT);
		input6.setParameterType(Fact.MY_URI);
		modifyFact.addFilteringInput(INPUT_FACT, Fact.MY_URI, 1, 1,
				new String[] { DroolsReasoning.PROP_KNOWS_FACTS });
		modifyFact.myProfile.addChangeEffect(
				new String[] { DroolsReasoning.PROP_KNOWS_FACTS }, input6);
		profiles[4] = modifyFact.myProfile;

		// RemoveFact
		ProvidedDroolsReasonerService removeFact = new ProvidedDroolsReasonerService(
				SERVICE_REMOVE_FACT);
		ProcessInput input7 = new ProcessInput(INPUT_FACT_ID);
		input7.setParameterType(TypeMapper.getDatatypeURI(String.class));
		input7.setCardinality(1, 1);
		removeFact.getProfile().addInput(input7);
		MergedRestriction restr3 = MergedRestriction.getFixedValueRestriction(
				Fact.PROP_HAS_IDENTIFIER, input7.asVariableReference());
		removeFact.addInstanceLevelRestriction(restr3, new String[] {
				DroolsReasoning.PROP_KNOWS_FACTS, Fact.PROP_HAS_IDENTIFIER });
		removeFact.getProfile().addRemoveEffect(
				new String[] { DroolsReasoning.PROP_KNOWS_FACTS,
						Fact.PROP_HAS_IDENTIFIER });
		profiles[5] = removeFact.myProfile;
		// Switch ON
		// TODO Add property for avoiding passing null parameters to the efect
		// (e.g. LTBA_STATE)
		ProvidedDroolsReasonerService switchOn = new ProvidedDroolsReasonerService(
				SERVICE_SWITCH_ON);
		switchOn.myProfile.addAddEffect(null, null);
		profiles[6] = switchOn.myProfile;
		// Switch OFF
		// TODO Add property for avoiding passing null parameters to the efect
		// (e.g. LTBA_STATE)
		ProvidedDroolsReasonerService switchOff = new ProvidedDroolsReasonerService(
				SERVICE_SWITCH_OFF);
		switchOff.getProfile().addRemoveEffect(null);
		profiles[7] = switchOff.myProfile;

		// input3.setCardinality(1, 1);
		// modifyRule.getProfile().addInput(input3);
		// MergedRestriction restr3 =
		// MergedRestriction.getFixedValueRestriction(Rule.PROP_HAS_IDENTIFIER,
		// input3.asVariableReference());
		// removeRule.addInstanceLevelRestriction(restr3, new
		// String[]{DroolsReasoning.PROP_KNOWS_RULES,
		// Rule.PROP_HAS_IDENTIFIER});

		// TODO End the profiles declaration, currently stopped for checking the
		// working of the whole structure
	}

	public ProvidedDroolsReasonerService(String instanceURI) {
		super(instanceURI);
		// TODO Auto-generated constructor stub
	}

}
