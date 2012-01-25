/**
 * 
 */
package org.universAAL.context.conversion.jena.impl;

import junit.framework.TestCase;

/**
 * @author mtazari
 *
 */
public class UIFrameworkTest extends TestCase {	
	
	JenaModelConverter jmc;
	
	public UIFrameworkTest(String name) {
		super(name);

		jmc = new JenaModelConverter();
	}
	
	public void testMenuRequest() {
//		InputEvent ie = new InputEvent(
//				new Resource(
//						Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX
//						+ "saied"),
//				null,
//				InputEvent.uAAL_MAIN_MENU_REQUEST);
//
//		new ResourceComparator().printDiffs(ie,
//				jmc.toPersonaResource(jmc.toJenaResource(ie)));
	}
	
	public void testOBusSubscription() {
//		OutputEventPattern oep = new OutputEventPattern();
//		oep.addRestriction(Restriction.getAllValuesRestriction(
//				OutputEvent.PROP_HAS_ACCESS_IMPAIRMENT, new Enumeration(
//						new AccessImpairment[] {
//								new HearingImpairment(LevelRating.low),
//								new HearingImpairment(LevelRating.middle),
//								new HearingImpairment(LevelRating.high),
//								new HearingImpairment(LevelRating.full),
//								new SightImpairment(LevelRating.low),
//								new PhysicalImpairment(LevelRating.low)})));
//		oep.addRestriction(Restriction.getFixedValueRestriction(
//				OutputEvent.PROP_OUTPUT_MODALITY, Modality.gui));
//		Resource pr = new Resource();
//		pr.addType(Resource.uAAL_VOCABULARY_NAMESPACE + "Subscription", true);
//		pr.setProperty(Resource.uAAL_VOCABULARY_NAMESPACE + "theSubscriber", "urn:org.persona.aal_space:tes_env#123cc35472e@PC1581+f2ed514f_1");
//		pr.setProperty(Resource.uAAL_VOCABULARY_NAMESPACE + "theSubscription", oep);
//
//		new ResourceComparator().printDiffs(pr,
//				jmc.toPersonaResource(jmc.toJenaResource(pr)));
	}
	
	public void testInitialDialogRequest() {
	    // TODO
//		ServiceRequest sr = InitialServiceDialog.getInitialDialogRequest(
//				"http://ontology.persona.ima.igd.fhg.de/Nutritional.owl#Nutritional",
//				"http://www.tsb.upv.es",
//				new Resource(Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX + "saied"));


		// TODO
//		new ResourceComparator().printDiffs(sr,
//				jmc.toPersonaResource(jmc.toJenaResource(sr)));
	}
}

