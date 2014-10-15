package org.universAAL.context.chemobile.test;

import java.io.File;

import org.springframework.util.Assert;
import org.universAAL.context.chemobile.osgi.Activator;
import org.universAAL.itests.IntegrationTest;
import org.universAAL.middleware.container.osgi.util.BundleConfigHome;
import org.universAAL.middleware.container.utils.LogUtils;
import org.universAAL.middleware.context.ContextEvent;
import org.universAAL.middleware.context.ContextEventPattern;
import org.universAAL.middleware.context.ContextPublisher;
import org.universAAL.middleware.context.DefaultContextPublisher;
import org.universAAL.middleware.context.owl.ContextProvider;
import org.universAAL.middleware.context.owl.ContextProviderType;
import org.universAAL.middleware.owl.MergedRestriction;
import org.universAAL.middleware.rdf.Resource;

public class ArtifactIT extends IntegrationTest {

    public static final String NAMESPACE = "http://ontology.universAAL.org/Test.owl#";
    public static final String USER = NAMESPACE + "User";
    public static final String DUMMYUSER = NAMESPACE + "dummyUser";
    public static final String HAS_LOCATION = NAMESPACE + "hasLocation";
    public static final String LOCATION = NAMESPACE + "dummyLocation";
    public static final String DUMMYEVENT = "urn:org.universAAL.middleware.context.rdf:ContextEvent#_:0000000000000000:00";

    /**
     * Test 1: Check all artifacts in the log.
     */
    public void testComposite() {
	logAllBundles();
    }

    public void testStore() {
	LogUtils.logInfo(Activator.getModuleContext(),
		ArtifactIT.class, "testStore",
		new String[] { "-Test 2-" }, null);

	ContextProvider info = new ContextProvider();
	info.setType(ContextProviderType.gauge);
	ContextEventPattern cep = new ContextEventPattern();
	cep.addRestriction(MergedRestriction.getFixedValueRestriction(
		ContextEvent.PROP_RDF_SUBJECT, new Resource(DUMMYUSER)));
	cep.addRestriction(MergedRestriction.getFixedValueRestriction(
		ContextEvent.PROP_RDF_PREDICATE, new Resource(HAS_LOCATION)));
	info.setProvidedEvents(new ContextEventPattern[] { cep });

	ContextPublisher pub = new DefaultContextPublisher(
		Activator.getModuleContext(), info);
	LogUtils.logInfo(
		Activator.getModuleContext(),
		ArtifactIT.class,
		"testStore",
		new String[] { "Created Default Context Publisher with full Provider Info" },
		null);
	// Create and send first event
	org.universAAL.middleware.context.ContextEvent cevA = org.universAAL.middleware.context.ContextEvent
		.constructSimpleEvent(DUMMYUSER, USER, HAS_LOCATION,
			new Resource(LOCATION));
	pub.publish(cevA);
	LogUtils.logInfo(Activator.getModuleContext(),
		ArtifactIT.class, "testStore",
		new String[] { "Published event 1: " + cevA }, null);
	
	// Wait for the event to be stored...
	try {
	    Thread.sleep(3000L);
	} catch (InterruptedException e) {
	    e.printStackTrace();
	}
	
	File fileref = new File(
		new BundleConfigHome("ctxt.che.mobile").getAbsolutePath(),
		"Mobile-Events.txt");
	 fileref.deleteOnExit();

	if (fileref.exists()) {
	    if (!(fileref.length() > 0)) {
		Assert.notNull(null, "The file with stored events is empty!");
	    }
	} else {
	    Assert.notNull(null, "The file with stored events does not exist!");
	}
    }

}
