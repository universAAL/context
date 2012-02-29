package org.universAAL.context.prof.serv;

import org.universAAL.middleware.owl.ManagedIndividual;
import org.universAAL.middleware.owl.MergedRestriction;
import org.universAAL.middleware.owl.OntologyManagement;
import org.universAAL.middleware.owl.SimpleOntology;
import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.rdf.impl.ResourceFactoryImpl;
import org.universAAL.middleware.service.owl.Service;
import org.universAAL.middleware.service.owls.process.ProcessInput;
import org.universAAL.middleware.service.owls.process.ProcessOutput;
import org.universAAL.middleware.service.owls.profile.ServiceProfile;
import org.universAAL.ontology.profile.Profilable;
import org.universAAL.ontology.profile.Profile;
import org.universAAL.ontology.profile.service.ProfilingService;

public class SCalleeProvidedService extends ProfilingService {

    public static final String NAMESPACE = "http://ontology.universAAL.org/ProfilingServer.owl#";
    public static final String MY_URI = NAMESPACE+"ProvidedServices";
    // VALUES FOR STATIC NON-TYPICAL SERVICE PROFILES
    public static final String FAKE_URI = NAMESPACE+"placeholder";
    protected static final String ADD_PROFILE = NAMESPACE + "servA";
    protected static final String CHANGE_PROFILE = NAMESPACE + "servB";
    protected static final String GET_USERS = NAMESPACE + "servC";
    protected static final String GET_SUBPROFILES = NAMESPACE + "servD";
    protected static final String INPUT_ADD_PROFILE = NAMESPACE + "arg1";
    protected static final String INPUT2_ADD_PROFILE = NAMESPACE + "arg2";
    protected static final String INPUT_CHANGE_PROFILE = NAMESPACE + "arg3";
    protected static final String INPUT2_CHANGE_PROFILE = NAMESPACE + "arg4";
    protected static final String OUTPUT_GET_USERS = NAMESPACE + "arg5";
    protected static final String INPUT_GET_SUBPROFILES = NAMESPACE + "arg6";
    protected static final String OUT_GET_SUBPROFILES = NAMESPACE + "arg7";
    // VALUES FOR DYNAMICE TYPICAL SERVICE PROFILES
    public static final String SERVICE_GET = "servEditorGet";
    public static final String SERVICE_ADD = "servEditorAdd";
    public static final String SERVICE_CHANGE = "servEditorChange";
    public static final String SERVICE_REMOVE = "servEditorRemove";
    public static final String IN_GET = "inputEditorGet";
    public static final String OUT_GET = "outputEditorGet";
    public static final String IN_ADD = "inputEditorAdd";
    public static final String IN_CHANGE = "inputEditorChange";
    public static final String IN_REMOVE = "inputEditorRemove";

    public SCalleeProvidedService(String uri) {
	super(uri);
    }

    public static ServiceProfile[] profiles=new ServiceProfile[4];

    static {
	OntologyManagement.getInstance().register(
		new SimpleOntology(MY_URI, ProfilingService.MY_URI,
			new ResourceFactoryImpl() {
			    public Resource createInstance(String classURI,
				    String instanceURI, int factoryIndex) {
				return new SCalleeProvidedService(instanceURI);
			    }
			}));
	
	//STATIC NON-TYPICAL SERVICE PROFILES
	
	// GET_PROFILE(Profilable) -> Profile
	// Returns the profile (with all its stored properties) associated to the profile whose URI matches the one of the passed parameter

	// ADD_PROFILE(Profilable, Profile) -> void
	// Adds the passed profile to the store, with all its properties (that
	// can be serialized), and associates it to the given profilable.
	// Currently works like CHANGE PROFILE, but does not remove previous
	// value of an associated profile to the profilable, if any
	Service prof6=new ProfilingService(ADD_PROFILE);
	ProcessInput input1 = new ProcessInput(INPUT_ADD_PROFILE);
	input1.setParameterType(ManagedIndividual.getTypeURI(Profilable.MY_URI));
	MergedRestriction restr1 = MergedRestriction.getFixedValueRestriction(
		ProfilingService.PROP_CONTROLS, input1.asVariableReference());
	prof6.addInstanceLevelRestriction(restr1, new String[]{ProfilingService.PROP_CONTROLS});
	prof6.getProfile().addInput(input1);
	ProcessInput input2 = new ProcessInput(INPUT2_ADD_PROFILE);
	input2.setParameterType(ManagedIndividual.getTypeURI(Profile.MY_URI));
	prof6.getProfile().addInput(input2);
	prof6.getProfile().addAddEffect(new String[]{ProfilingService.PROP_CONTROLS,Profilable.PROP_HAS_PROFILE},
	input2.asVariableReference());
	profiles[0] = prof6.getProfile();
	
	// CHANGE_PROFILE(Profilable, Profile) -> void
	// Removes ALL REFERENCE to the previous profile of the passed
	// profilable, and then adds the new profile with its properties. The
	// properties of old profile (including subprofiles) are not saved,
	// although their values are.
	Service prof7=new ProfilingService(CHANGE_PROFILE);
	ProcessInput input3 = new ProcessInput(INPUT_CHANGE_PROFILE);
	input3.setParameterType(ManagedIndividual.getTypeURI(Profilable.MY_URI));
	MergedRestriction restr2 = MergedRestriction.getFixedValueRestriction(
		ProfilingService.PROP_CONTROLS, input3.asVariableReference());
	prof7.addInstanceLevelRestriction(restr2, new String[]{ProfilingService.PROP_CONTROLS});
	prof7.getProfile().addInput(input3);
	ProcessInput input4 = new ProcessInput(INPUT2_CHANGE_PROFILE);
	input4.setParameterType(ManagedIndividual.getTypeURI(Profile.MY_URI));
	prof7.getProfile().addInput(input4);
	prof7.getProfile().addChangeEffect(new String[]{ProfilingService.PROP_CONTROLS,Profilable.PROP_HAS_PROFILE},
		input4.asVariableReference());
	profiles[1] = prof7.getProfile();
	
	// GET_USERS() -> [User]
	// Not working yet
	Service prof9=new ProfilingService(GET_USERS);
//	prof9.put(Path.at(ProfilingService.PROP_CONTROLS), Arg.out(new Profilable(FAKE_URI)), OUTPUT_GET_USERS);
	ProcessOutput output1 = new ProcessOutput(OUTPUT_GET_USERS);
	output1.setParameterType(Profilable.MY_URI);
	prof9.getProfile().addOutput(output1);
	prof9.getProfile().addSimpleOutputBinding(output1, new String[]{ProfilingService.PROP_CONTROLS});
	profiles[2] = prof9.getProfile();
	
	// GET_SUBPROFILES(Profile) -> [Subprofile]
	// Not working yet
	Service prof10=new ProfilingService(GET_SUBPROFILES);
	ProcessInput input5 = new ProcessInput(INPUT_GET_SUBPROFILES);
	input5.setParameterType(ManagedIndividual.getTypeURI(Profile.MY_URI));
	MergedRestriction restr3 = MergedRestriction.getFixedValueRestriction(
		Profilable.PROP_HAS_PROFILE, input5.asVariableReference());
	prof10.addInstanceLevelRestriction(restr3, new String[]{ProfilingService.PROP_CONTROLS,Profilable.PROP_HAS_PROFILE});
	prof10.getProfile().addInput(input5);
	ProcessOutput output3 = new ProcessOutput(OUT_GET_SUBPROFILES);
	output3.setParameterType(Profile.MY_URI);
	prof10.getProfile().addOutput(output3);
	prof10.getProfile().addSimpleOutputBinding(output3, new String[]{ProfilingService.PROP_CONTROLS,Profilable.PROP_HAS_PROFILE,Profile.PROP_HAS_SUB_PROFILE});
	profiles[3] = prof10.getProfile();
	
    }
    
    //DYNAMIC TYPICAL SERVICE PROFILES
    public static ServiceProfile[] getServiceProfiles(String namespace,
	    String ontologyURI, String[] path, String editedURI) {

	ServiceProfile[] profiles = new ServiceProfile[4];

	// Get
	Service prof1 = (Service) OntologyManagement
		.getInstance()
		.getResource(ontologyURI, namespace + SERVICE_GET);
	ProcessInput input1 = new ProcessInput(namespace + IN_GET);
	input1.setParameterType(editedURI);
	input1.setCardinality(1, 1);
	MergedRestriction restr1 = MergedRestriction.getFixedValueRestriction(
		path[path.length - 1], input1.asVariableReference());
	prof1.addInstanceLevelRestriction(restr1, path);
	prof1.getProfile().addInput(input1);
	ProcessOutput output = new ProcessOutput(namespace + OUT_GET);
	output.setParameterType(editedURI);
	prof1.getProfile().addOutput(output);
	prof1.getProfile().addSimpleOutputBinding(output, path);
	profiles[0] = prof1.getProfile();

	// Add
	Service prof2 = ((Service) OntologyManagement
		.getInstance()
		.getResource(ontologyURI, namespace + SERVICE_ADD));
	ProcessInput input2 = new ProcessInput(namespace + IN_ADD);
	input2.setParameterType(editedURI);
	input2.setCardinality(1, 1);
	prof2.getProfile().addInput(input2);
	prof2.getProfile().addAddEffect(path,
		input2.asVariableReference());
	profiles[1] = prof2.getProfile();

	// Change
	Service prof3 = ((Service) OntologyManagement
		.getInstance().getResource(ontologyURI,
			namespace + SERVICE_CHANGE));
	ProcessInput input3 = new ProcessInput(namespace + IN_CHANGE);
	input3.setCardinality(1, 1);
	input3.setParameterType(editedURI);
	prof3.getProfile().addInput(input3);
	prof3.getProfile().addChangeEffect(path,
		input3.asVariableReference());
	profiles[2] = prof3.getProfile();

	// Remove
	Service prof4 = ((Service) OntologyManagement
		.getInstance().getResource(ontologyURI,
			namespace + SERVICE_REMOVE));
	ProcessInput input4 = new ProcessInput(namespace + IN_REMOVE);
	input4.setParameterType(editedURI);
	input4.setCardinality(1, 1);
	prof4.getProfile().addInput(input4);
	MergedRestriction restr4 = MergedRestriction.getFixedValueRestriction(
		path[path.length - 1], input4.asVariableReference());
	prof4.addInstanceLevelRestriction(restr4, path);
	prof4.getProfile().addRemoveEffect(path);
	profiles[3] = prof4.getProfile();

	return profiles;
    }

}
