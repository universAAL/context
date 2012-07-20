package org.universAAL.context.prof.serv;

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
    private static final String NOTHING = "nothing";
    private static final String ARG_OUT = NAMESPACE+"argout";
    private ServiceCaller caller;

    public void testComposite() {
	logAllBundles();
	try {
	    Thread.sleep(5000L);
	} catch (InterruptedException e) {
	    e.printStackTrace();
	}
	caller = new DefaultServiceCaller(Activator.context);
    }

    public void testProfilable() {
	caller = new DefaultServiceCaller(Activator.context);
	LogUtils.logInfo(Activator.context, ArtifactIntegrationTest.class,
		"testProfilable", new String[] { "-Test 1-" }, null);
	User user1 = new User(NAMESPACE + "user1");
	AssistedPerson user2 = new AssistedPerson(NAMESPACE + "ap2");

	String str1=getProfilable(user1);
	Assert.isTrue(str1.equals(NOTHING),
		"Should have retruned nothing (1), but not "+str1);
	String str2=getUsers();
	Assert.isTrue(str2.equals(NOTHING),
		"Should have retruned nothing (2), nut not "+str2);

	String out1=addProfilable(user1);
	String str3=getProfilable(user1);
	Assert.isTrue(str3.equals(user1.getURI()),
		"Should have retruned a User (1), but not "+str3+" with "+out1);

	String out2=addProfilable(user2);
	String str4=getProfilable(user2);
	Assert.isTrue(str4.equals(user2.getURI()),
		"Should have retruned a AP, but not "+str4+" with "+out2);

	String str5=getUsers();
	Assert.isTrue(str5.contains(","),
		"Should have retruned several users, but not "+str5);

	String out3=changeProfilable(user1);
	String str6=getProfilable(user1);
	Assert.isTrue(str6.equals(user1.getURI()),
		"Should have retruned a User (2), but not "+str6+" with "+out3);

	String out4=removeProfilable(user1);
	String str7=getProfilable(user1);
	Assert.isTrue(str7.equals(NOTHING),
		"Should have retruned nothing (3), but not "+str7+" with "+out4);
	//Clean
	removeProfilable(user2);
    }

    public void testProfile() {
	caller = new DefaultServiceCaller(Activator.context);
	LogUtils.logInfo(Activator.context, ArtifactIntegrationTest.class,
		"testProfile", new String[] { "-Test 2-" }, null);
	UserProfile prof1 = new UserProfile(NAMESPACE + "userProf1");
	AssistedPersonProfile prof2 = new AssistedPersonProfile(NAMESPACE
		+ "apProf2");

	String str1=getProfile(prof1);
	Assert.isTrue(str1.equals(NOTHING),
		"Should have returned nothing (1), but not "+str1);

	String out1=addProfile(prof1);
	String str2=getProfile(prof1);
	Assert.isTrue(str2.equals(prof1.getURI()),
		"Should have returned a UserProfile (1), but not "+str2+" with "+out1);

	String out2=addProfile(prof2);
	String str3=getProfile(prof2);
	Assert.isTrue(str3.equals(prof2.getURI()),
		"Should have returned a APProfile, but not "+str3+" with "+out2);

	String out3=changeProfile(prof1);
	String str4=getProfile(prof1);
	Assert.isTrue(str4.equals(prof1.getURI()),
		"Should have returned a UserProfile (2), but not "+str4+" with "+out3);

	String out4=removeProfile(prof1);
	String str5=getProfile(prof1);
	Assert.isTrue(str5.equals(NOTHING),
		"Should have returned nothing (2), but not "+str5+" with "+out4);
	//Clean
	removeProfile(prof2);
    }

    public void testSubProfile() {
	caller = new DefaultServiceCaller(Activator.context);
	LogUtils.logInfo(Activator.context, ArtifactIntegrationTest.class,
		"testSubProfile", new String[] { "-Test 3-" }, null);
	SubProfile subprof1 = new SubProfile(NAMESPACE + "userSubProf1");

	String str1=getSubProfile(subprof1);
	Assert.isTrue(str1.equals(NOTHING),
		"Should have returned nothing (1), but not "+str1);

	String out1=addSubProfile(subprof1);
	String str2=getSubProfile(subprof1);
	Assert.isTrue(str2.equals(subprof1.getURI()),
		"Should have returned a SubProfile, but not "+str2+" with "+out1);

	String out2=changeSubProfile(subprof1);
	String str3=getSubProfile(subprof1);
	Assert.isTrue(str3.equals(subprof1.getURI()),
		"Should have returned a UserProfile, but not "+str3+" with "+out2);

	String out3=removeSubProfile(subprof1);
	String str4=getSubProfile(subprof1);
	Assert.isTrue(str4.equals(NOTHING),
		"Should have returned nothing (2), but not "+str4+" with "+out3);
    }

    // :::::::::::::PROFILABLE GET/ADD/CHANGE/REMOVE:::::::::::::::::
    private String removeProfilable(Resource profilable) {
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
