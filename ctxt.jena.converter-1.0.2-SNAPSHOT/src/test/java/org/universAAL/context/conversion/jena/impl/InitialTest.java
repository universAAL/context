package org.universAAL.context.conversion.jena.impl;

//import org.persona.middleware.MiddlewareConstants;
//import org.persona.ontology.AccessImpairment;
//import org.persona.ontology.Gender;
//import org.persona.ontology.LevelRating;
//import org.persona.ontology.Modality;
//import org.persona.ontology.PrivacyLevel;
import junit.framework.TestCase;

import org.universAAL.middleware.context.ContextEvent;
import org.universAAL.middleware.owl.AllValuesFromRestriction;
import org.universAAL.middleware.owl.Enumeration;
import org.universAAL.middleware.owl.ExactCardinalityRestriction;
import org.universAAL.middleware.owl.HasValueRestriction;
import org.universAAL.middleware.owl.MergedRestriction;
import org.universAAL.middleware.owl.SomeValuesFromRestriction;
import org.universAAL.middleware.owl.TypeURI;
import org.universAAL.middleware.owl.supply.Rating;
import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.util.ResourceComparator;

public class InitialTest extends TestCase {
	private static String TEST_PROP = "http://ontology.aal-persona.org/Context.owl#entersLocation";
	
	JenaModelConverter jmc;
	
	public InitialTest(String name) {
		super(name);

		jmc = new JenaModelConverter();
	}

	public void testContextEvent()
	{
		
		Resource subject = new Resource("urn:org.aal-persona.profiling:123456789:saied");
		subject.addType("http://ontology.aal-persona.org/User.owl#User", true);
		subject.setProperty(TEST_PROP, "Nowhere");
		ContextEvent ce = new ContextEvent(subject, TEST_PROP);

//		ce.setAccuracy(Rating.good);
		ce.setConfidence(new Integer(42));
		ce.setExpirationTime(new Long(System.currentTimeMillis()+100000));

		new ResourceComparator().printDiffs(ce,
				jmc.toPersonaResource(jmc.toJenaResource(ce)));
	}
	
	public void testDataRange() {
		Enumeration e1 = new Enumeration();
		e1.addValue(new Integer(0));
		e1.addValue(new Integer(1));
		e1.addValue(new Integer(2));
		e1.addValue(new Integer(3));
		e1.addValue(new Integer(4));

		new ResourceComparator().printDiffs(e1,
				jmc.toPersonaResource(jmc.toJenaResource(e1)));
	}
	
	public void testRestriction() {
		Integer one = new Integer(1);
		Enumeration e = new Enumeration();
		e.addValue(Rating.richSatisfying);
		e.addValue(Rating.almostGood);
		e.addValue(Rating.good);
        
        MergedRestriction r = new MergedRestriction(HasValueRestriction.PROP_OWL_HAS_VALUE);
        r.addRestriction(new AllValuesFromRestriction(HasValueRestriction.PROP_OWL_HAS_VALUE, new TypeURI(Rating.MY_URI, false)));
        r.addRestriction(new ExactCardinalityRestriction(HasValueRestriction.PROP_OWL_HAS_VALUE, one.intValue()));
        r.addRestriction(new SomeValuesFromRestriction(HasValueRestriction.PROP_OWL_HAS_VALUE, e));

        // TODO
//		new ResourceComparator().printDiffs(r,
//				jmc.toPersonaResource(jmc.toJenaResource(r)));
	}

//	public void testProfileData() {
//		UserIdentificationProfile uip = new UserIdentificationProfile();
//		uip.setName("Saied");
//		ElderlyProfile ep = new ElderlyProfile();
//		ep.setUserIdentificationProfile(uip);
//		HealthProfile hp = new HealthProfile();
//		hp.setDisability(new AccessImpairment[]{new HearingImpairment(LevelRating.middle)});
//		ep.setHealthProfile(hp);
//		PersonalPreferenceProfile ppp = new PersonalPreferenceProfile();
//		ppp.setInsensibleMaxX(new Integer(1024));
//		ppp.setInsensibleMaxY(new Integer(768));
//		ppp.setInsensibleVolumeLevel(new Integer(85));
//		ppp.setPersonalMinX(new Integer(176));
//		ppp.setPersonalMinY(new Integer(320));
//		ppp.setPersonalVolumeLevel(new Integer(60));
//		ppp.setPLsMappedToInsensible(new PrivacyLevel[]{PrivacyLevel.knownPeopleOnly});
//		ppp.setPLsMappedToPersonal(new PrivacyLevel[]{
//				PrivacyLevel.intimatesOnly, PrivacyLevel.homeMatesOnly});
//		ppp.setVoiceGender(Gender.female);
//		ppp.setXactionModality(Modality.gui);
//		ep.setPersonalPreferenceProfile(ppp);
//		ElderlyUser eu = new ElderlyUser(MiddlewareConstants.PERSONA_MIDDLEWARE_LOCAL_ID_PREFIX+"saied");
//		eu.setProfile(ep);
//		new ResourceComparator().printDiffs(eu,
//				jmc.toPersonaResource(jmc.toJenaResource(eu)));
//	}
}