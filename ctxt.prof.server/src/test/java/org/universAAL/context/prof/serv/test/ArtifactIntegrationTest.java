package org.universAAL.context.prof.serv.test;

import java.util.Iterator;
import java.util.List;

import org.springframework.util.Assert;
import org.universAAL.context.prof.serv.Activator;
import org.universAAL.itests.IntegrationTest;
import org.universAAL.middleware.container.utils.LogUtils;
import org.universAAL.middleware.owl.MergedRestriction;
import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.service.CallStatus;
import org.universAAL.middleware.service.DefaultServiceCaller;
import org.universAAL.middleware.service.ServiceCaller;
import org.universAAL.middleware.service.ServiceRequest;
import org.universAAL.middleware.service.ServiceResponse;
import org.universAAL.middleware.service.owls.process.ProcessOutput;
import org.universAAL.ontology.profile.AssistedPerson;
import org.universAAL.ontology.profile.AssistedPersonProfile;
import org.universAAL.ontology.profile.Profilable;
import org.universAAL.ontology.profile.Profile;
import org.universAAL.ontology.profile.SubProfile;
import org.universAAL.ontology.profile.User;
import org.universAAL.ontology.profile.UserProfile;
import org.universAAL.ontology.profile.service.ProfilingService;

/**
 * Here developer's of this artifact should code their integration tests.
 * 
 * @author rotgier
 * 
 */
public class ArtifactIntegrationTest extends IntegrationTest {

    private static final String NAMESPACE = "http://ontology.itaca.es/ProfileTest.owl#";
    private static final String NOTHING = "nothting";
    private static final String ARG_OUT = NAMESPACE+"argout";
    private ServiceCaller caller;

    public void testComposite() {
	logAllBundles();
	caller = new DefaultServiceCaller(Activator.context);
    }

    public void testProfilable() {
	caller = new DefaultServiceCaller(Activator.context);
	LogUtils.logInfo(Activator.context, ArtifactIntegrationTest.class,
		"testProfilable", new String[] { "-Test 1-" }, null);
	User user1 = new User(NAMESPACE + "user1");
	AssistedPerson user2 = new AssistedPerson(NAMESPACE + "ap2");

	Assert.isTrue(getProfilable(user1).equals(NOTHING),
		"Should have retruned nothing");
	Assert.isTrue(getUsers().equals(NOTHING),
		"Should have retruned nothing");

	addProfilable(user1);
	Assert.isTrue(getProfilable(user1).equals(user1.getURI()),
		"Should have retruned a User");

	addProfilable(user2);
	Assert.isTrue(getProfilable(user2).equals(user2.getURI()),
		"Should have retruned a AP");

	Assert.isTrue(getUsers().contains(","),
		"Should have retruned several users");

	changeProfilable(user1);
	Assert.isTrue(getProfilable(user1).equals(user1.getURI()),
		"Should have retruned a User");

	removeProfilable(user1);
	Assert.isTrue(getProfilable(user1).equals(NOTHING),
		"Should have retruned nothing");
    }

    public void testProfile() {
	caller = new DefaultServiceCaller(Activator.context);
	LogUtils.logInfo(Activator.context, ArtifactIntegrationTest.class,
		"testProfile", new String[] { "-Test 2-" }, null);
	UserProfile prof1 = new UserProfile(NAMESPACE + "userProf1");
	AssistedPersonProfile prof2 = new AssistedPersonProfile(NAMESPACE
		+ "apProf2");

	Assert.isTrue(getProfile(prof1).equals(NOTHING),
		"Should have returned nothing");

	addProfile(prof1);
	Assert.isTrue(getProfile(prof1).equals(prof1.getURI()),
		"Should have returned a UserProfile");

	addProfile(prof2);
	Assert.isTrue(getProfile(prof2).equals(prof2.getURI()),
		"Should have returned a APProfile");

	changeProfile(prof1);
	Assert.isTrue(getProfile(prof1).equals(prof1.getURI()),
		"Should have returned a UserProfile");

	removeProfile(prof1);
	Assert.isTrue(getProfile(prof1).equals(NOTHING),
		"Should have returned nothing");
    }

    public void testSubProfile() {
	caller = new DefaultServiceCaller(Activator.context);
	LogUtils.logInfo(Activator.context, ArtifactIntegrationTest.class,
		"testSubProfile", new String[] { "-Test 3-" }, null);
	SubProfile subprof1 = new SubProfile(NAMESPACE + "userSubProf1");

	Assert.isTrue(getSubProfile(subprof1).equals(NOTHING),
		"Should have returned nothing");

	addSubProfile(subprof1);
	Assert.isTrue(getSubProfile(subprof1).equals(subprof1.getURI()),
		"Should have returned a SubProfile");

	changeSubProfile(subprof1);
	Assert.isTrue(getSubProfile(subprof1).equals(subprof1.getURI()),
		"Should have returned a UserProfile");

	removeSubProfile(subprof1);
	Assert.isTrue(getSubProfile(subprof1).equals(NOTHING),
		"Should have returned nothing");
    }

    // :::::::::::::PROFILABLE GET/ADD/CHANGE/REMOVE:::::::::::::::::
    private String removeProfilable(User profilable) {
	ServiceRequest req = new ServiceRequest(new ProfilingService(null),
		null);
	MergedRestriction r1 = MergedRestriction.getFixedValueRestriction(
		ProfilingService.PROP_CONTROLS, profilable);
	req.getRequestedService().addInstanceLevelRestriction(r1,
		new String[] { ProfilingService.PROP_CONTROLS });
	req.addRemoveEffect(new String[] { ProfilingService.PROP_CONTROLS });
	ServiceResponse resp = caller.call(req);
	return resp.getCallStatus().name();
    }

    private String changeProfilable(User profilable) {
	ServiceRequest req = new ServiceRequest(new ProfilingService(null),
		null);
	req.addChangeEffect(new String[] { ProfilingService.PROP_CONTROLS },
		profilable);
	ServiceResponse resp = caller.call(req);
	return resp.getCallStatus().name();
    }

    private String addProfilable(Resource profilable) {
	ServiceRequest req = new ServiceRequest(new ProfilingService(null),
		null);
	req.addAddEffect(new String[] { ProfilingService.PROP_CONTROLS },
		profilable);
	ServiceResponse resp = caller.call(req);
	return resp.getCallStatus().name();
    }

    private String getProfilable(Resource profilable) {
	ServiceRequest req = new ServiceRequest(new ProfilingService(null),
		null);
	req.addValueFilter(new String[] { ProfilingService.PROP_CONTROLS },
		profilable);
	req.addRequiredOutput(ARG_OUT,
		new String[] { ProfilingService.PROP_CONTROLS });
	ServiceResponse resp = caller.call(req);
	if (resp.getCallStatus() == CallStatus.succeeded) {
	    Object out = getReturnValue(resp.getOutputs(), ARG_OUT);
	    if (out != null) {
		return out.toString();
	    } else {
		return NOTHING;
	    }
	} else {
	    return resp.getCallStatus().name();
	}
    }

    // :::::::::::::PROFILE GET/ADD/CHANGE/REMOVE:::::::::::::::::
    private String removeProfile(UserProfile profile) {
	ServiceRequest req = new ServiceRequest(new ProfilingService(null),
		null);
	MergedRestriction r1 = MergedRestriction.getFixedValueRestriction(
		Profilable.PROP_HAS_PROFILE, profile);
	req.getRequestedService().addInstanceLevelRestriction(
		r1,
		new String[] { ProfilingService.PROP_CONTROLS,
			Profilable.PROP_HAS_PROFILE });
	req.addRemoveEffect(new String[] { ProfilingService.PROP_CONTROLS,
		Profilable.PROP_HAS_PROFILE });
	ServiceResponse resp = caller.call(req);
	return resp.getCallStatus().name();
    }

    private String changeProfile(UserProfile profile) {
	ServiceRequest req = new ServiceRequest(new ProfilingService(null),
		null);
	req.addChangeEffect(new String[] { ProfilingService.PROP_CONTROLS,
		Profilable.PROP_HAS_PROFILE }, profile);
	ServiceResponse resp = caller.call(req);
	return resp.getCallStatus().name();
    }

    private String addProfile(UserProfile profile) {
	ServiceRequest req = new ServiceRequest(new ProfilingService(null),
		null);
	req.addAddEffect(new String[] { ProfilingService.PROP_CONTROLS,
		Profilable.PROP_HAS_PROFILE }, profile);
	ServiceResponse resp = caller.call(req);
	return resp.getCallStatus().name();
    }

    private String getProfile(UserProfile profile) {
	ServiceRequest req = new ServiceRequest(new ProfilingService(null),
		null);
	req.addValueFilter(new String[] { ProfilingService.PROP_CONTROLS,
		Profilable.PROP_HAS_PROFILE }, profile);
	req.addRequiredOutput(ARG_OUT, new String[] {
		ProfilingService.PROP_CONTROLS, Profilable.PROP_HAS_PROFILE });
	ServiceResponse resp = caller.call(req);
	if (resp.getCallStatus() == CallStatus.succeeded) {
	    Object out = getReturnValue(resp.getOutputs(), ARG_OUT);
	    if (out != null) {
		return out.toString();
	    } else {
		return NOTHING;
	    }
	} else {
	    return resp.getCallStatus().name();
	}
    }

    // :::::::::::::SUBPROFILE GET/ADD/CHANGE/REMOVE:::::::::::::::::
    private String removeSubProfile(SubProfile profile) {
	ServiceRequest req = new ServiceRequest(new ProfilingService(null),
		null);
	MergedRestriction r1 = MergedRestriction.getFixedValueRestriction(
		Profile.PROP_HAS_SUB_PROFILE, profile);
	req.getRequestedService().addInstanceLevelRestriction(
		r1,
		new String[] { ProfilingService.PROP_CONTROLS,
			Profilable.PROP_HAS_PROFILE,
			Profile.PROP_HAS_SUB_PROFILE });
	req.addRemoveEffect(new String[] { ProfilingService.PROP_CONTROLS,
		Profilable.PROP_HAS_PROFILE, Profile.PROP_HAS_SUB_PROFILE });
	ServiceResponse resp = caller.call(req);
	return resp.getCallStatus().name();
    }

    private String changeSubProfile(SubProfile profile) {
	ServiceRequest req = new ServiceRequest(new ProfilingService(null),
		null);
	req.addChangeEffect(new String[] { ProfilingService.PROP_CONTROLS,
		Profilable.PROP_HAS_PROFILE, Profile.PROP_HAS_SUB_PROFILE },
		profile);
	ServiceResponse resp = caller.call(req);
	return resp.getCallStatus().name();
    }

    private String addSubProfile(SubProfile profile) {
	ServiceRequest req = new ServiceRequest(new ProfilingService(null),
		null);
	req.addAddEffect(new String[] { ProfilingService.PROP_CONTROLS,
		Profilable.PROP_HAS_PROFILE, Profile.PROP_HAS_SUB_PROFILE },
		profile);
	ServiceResponse resp = caller.call(req);
	return resp.getCallStatus().name();
    }

    private String getSubProfile(SubProfile profile) {
	ServiceRequest req = new ServiceRequest(new ProfilingService(null),
		null);
	req.addValueFilter(new String[] { ProfilingService.PROP_CONTROLS,
		Profilable.PROP_HAS_PROFILE, Profile.PROP_HAS_SUB_PROFILE },
		profile);
	req.addRequiredOutput(ARG_OUT, new String[] {
		ProfilingService.PROP_CONTROLS, Profilable.PROP_HAS_PROFILE,
		Profile.PROP_HAS_SUB_PROFILE });
	ServiceResponse resp = caller.call(req);
	if (resp.getCallStatus() == CallStatus.succeeded) {
	    Object out = getReturnValue(resp.getOutputs(), ARG_OUT);
	    if (out != null) {
		return out.toString();
	    } else {
		return NOTHING;
	    }
	} else {
	    return resp.getCallStatus().name();
	}
    }

    // :::::::::::::OTHERS:::::::::::::::::

    private String getUsers() {
	ServiceRequest req = new ServiceRequest(new ProfilingService(null),
		null);
	req.addRequiredOutput(ARG_OUT,
		new String[] { ProfilingService.PROP_CONTROLS });
	ServiceResponse resp = caller.call(req);
	if (resp.getCallStatus() == CallStatus.succeeded) {
	    Object out = getReturnValue(resp.getOutputs(), ARG_OUT);
	    if (out != null) {
		return out.toString();
	    } else {
		return NOTHING;
	    }
	} else {
	    return resp.getCallStatus().name();
	}
    }

    private Object getReturnValue(List outputs, String expectedOutput) {
	Object returnValue = null;
	if (!(outputs == null)) {
	    for (Iterator i = outputs.iterator(); i.hasNext();) {
		ProcessOutput output = (ProcessOutput) i.next();
		if (output.getURI().equals(expectedOutput))
		    if (returnValue == null)
			returnValue = output.getParameterValue();
	    }
	}
	return returnValue;
    }

}
