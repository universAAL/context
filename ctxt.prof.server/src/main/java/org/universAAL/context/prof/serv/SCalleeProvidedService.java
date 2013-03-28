/*
	Copyright 2012-2014 ITACA-TSB, http://www.tsb.upv.es
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
package org.universAAL.context.prof.serv;

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
import org.universAAL.ontology.profile.SubProfile;
import org.universAAL.ontology.profile.User;
import org.universAAL.ontology.profile.UserProfile;
import org.universAAL.ontology.profile.service.ProfilingService;

/**
 * The class where all service profiles are built.
 * 
 * @author alfiva
 * 
 */
public class SCalleeProvidedService extends ProfilingService {

    /**
     * Default namespace root. Must be the same as in SCallee.
     */
    public static final String NAMESPACE = SCallee.NAMESPACE;
    /**
     * Ontological URI of the class (because it extends and ontology class>
     * ProfilingService)
     */
    public static final String MY_URI = NAMESPACE + "ProvidedServices";
    // VALUES FOR STATIC NON-TYPICAL SERVICE PROFILES
    protected static final String SRV_GET_USRS = NAMESPACE + "servA";
    protected static final String OUT_GET_USRS = NAMESPACE + "argAo";
    protected static final String SRV_GET_SUBS_OF_USR = NAMESPACE + "servB";
    protected static final String INP_GET_SUBS_OF_USR = NAMESPACE + "argBi";
    protected static final String OUT_GET_SUBS_OF_USR = NAMESPACE + "argBo";
    protected static final String SRV_GET_PRF_OF_USR = NAMESPACE + "servC";
    protected static final String INP_GET_PRF_OF_USR = NAMESPACE + "servCi";
    protected static final String OUT_GET_PRF_OF_USR = NAMESPACE + "servCo";
    protected static final String SRV_GET_SUBS_OF_PRF = NAMESPACE + "servD";
    protected static final String INP_GET_SUBS_OF_PRF = NAMESPACE + "servDi";
    protected static final String OUT_GET_SUBS_OF_PRF = NAMESPACE + "servDo";
    protected static final String SRV_ADD_PRF_TO_USR = NAMESPACE + "servE";
    protected static final String IN_ADD_PRF_TO_USR_WHERE = NAMESPACE + "servE1";
    protected static final String IN_ADD_PRF_TO_USR_WHAT = NAMESPACE + "servE2";
    protected static final String SRV_ADD_SUB_TO_USR = NAMESPACE + "servF";
    protected static final String IN_ADD_SUB_TO_USR_WHERE = NAMESPACE + "servF1";
    protected static final String IN_ADD_SUB_TO_USR_WHAT = NAMESPACE + "servF2";
    protected static final String SRV_ADD_SUB_TO_PRF = NAMESPACE + "servG";
    protected static final String IN_ADD_SUB_TO_PRF_WHERE = NAMESPACE + "servG1";
    protected static final String IN_ADD_SUB_TO_PRF_WHAT = NAMESPACE + "servG2";
    
    // VALUES FOR DYNAMIC TYPICAL SERVICE PROFILES
    public static final String SRV_GET_X = "servEditorGet";
    public static final String INP_GET_X = "inputEditorGet";
    public static final String OUT_GET_X = "outputEditorGet";
    public static final String SRV_ADD_X = "servEditorAdd";
    public static final String INP_ADD_X = "inputEditorAdd";
    public static final String SRV_CHN_X = "servEditorChn";
    public static final String INP_CHN_X = "inputEditorChn";
    public static final String SRV_REM_X = "servEditorRem";
    public static final String INP_REM_X = "inputEditorRem";

    /**
     * Default constructor.
     * 
     * @param uri Instance URI
     */
    public SCalleeProvidedService(String uri) {
	super(uri);
    }

    /**
     * Where the service profiles are stored.
     */
    protected static ServiceProfile[] profiles = new ServiceProfile[7];

    static {
	OntologyManagement.getInstance().register(Hub.moduleContext,
		new SimpleOntology(MY_URI, ProfilingService.MY_URI,
			new ResourceFactoryImpl() {
			    public Resource createInstance(String classURI,
				    String instanceURI, int factoryIndex) {
				return new SCalleeProvidedService(instanceURI);
			    }
			}));

	// STATIC NON-TYPICAL SERVICE PROFILES
	
	// GET_USERS() -> [User]
	//Gets all Users in the system (including subclasses like AP).
	Service prof0 = new ProfilingService(SRV_GET_USRS);
	ProcessOutput output0 = new ProcessOutput(OUT_GET_USRS);
	output0.setParameterType(User.MY_URI);
	prof0.getProfile().addOutput(output0);
	prof0.getProfile().addSimpleOutputBinding(output0,
		new String[] { ProfilingService.PROP_CONTROLS });
	profiles[0] = prof0.getProfile();

	// GET_PROFILE(User) -> UserProfile
	// Returns the profile (with all its stored properties) associated to
	// the user whose URI matches the one of the passed parameter
	Service prof1 = new ProfilingService(SRV_GET_PRF_OF_USR);
	ProcessInput input1 = new ProcessInput(INP_GET_PRF_OF_USR);
	input1.setParameterType(User.MY_URI);
	input1.setCardinality(1, 1);//
	MergedRestriction restr1 = MergedRestriction.getFixedValueRestriction(
		ProfilingService.PROP_CONTROLS, input1.asVariableReference());
	prof1.addInstanceLevelRestriction(restr1, new String[] {
		ProfilingService.PROP_CONTROLS });
	prof1.getProfile().addInput(input1);
	ProcessOutput output1 = new ProcessOutput(OUT_GET_PRF_OF_USR);
	output1.setParameterType(UserProfile.MY_URI);
	output1.setCardinality(1, 1);//
	prof1.getProfile().addOutput(output1);
	prof1.getProfile().addSimpleOutputBinding(
		output1,
		new String[] { ProfilingService.PROP_CONTROLS,
			Profilable.PROP_HAS_PROFILE });
	profiles[1] = prof1.getProfile();
	
	// GET_SUBPROFILES(User) -> [SubProfile]
	// Returns the subprofiles (only their URIs) associated to
	// the user whose URI matches the one of the passed parameter
	Service prof2 = new ProfilingService(SRV_GET_SUBS_OF_USR);
	ProcessInput input2 = new ProcessInput(INP_GET_SUBS_OF_USR);
	input2.setParameterType(User.MY_URI);
	input2.setCardinality(1, 1);//
	MergedRestriction restr2 = MergedRestriction.getFixedValueRestriction(
		ProfilingService.PROP_CONTROLS, input2.asVariableReference());
	prof2.addInstanceLevelRestriction(restr2, new String[] {
		ProfilingService.PROP_CONTROLS });
	prof2.getProfile().addInput(input2);
	ProcessOutput output2 = new ProcessOutput(OUT_GET_SUBS_OF_USR);
	output2.setParameterType(SubProfile.MY_URI);
	prof2.getProfile().addOutput(output2);
	prof2.getProfile().addSimpleOutputBinding(
		output2,
		new String[] { ProfilingService.PROP_CONTROLS,
			Profilable.PROP_HAS_PROFILE,
			Profile.PROP_HAS_SUB_PROFILE });
	profiles[2] = prof2.getProfile();
	
	// GET_SUBPROFILES(UserProfile) -> [SubProfile]
	// Returns the subprofiles (only their URIs) associated to
	// the user profile whose URI matches the one of the passed parameter
	Service prof3 = new ProfilingService(SRV_GET_SUBS_OF_PRF);
	ProcessInput input3 = new ProcessInput(INP_GET_SUBS_OF_PRF);
	input3.setParameterType(UserProfile.MY_URI);
	input3.setCardinality(1, 1);//
	MergedRestriction restr3 = MergedRestriction.getFixedValueRestriction(
		Profilable.PROP_HAS_PROFILE, input3.asVariableReference());
	prof3.addInstanceLevelRestriction(restr3, new String[] {
		ProfilingService.PROP_CONTROLS, Profilable.PROP_HAS_PROFILE });
	prof3.getProfile().addInput(input3);
	ProcessOutput output3 = new ProcessOutput(OUT_GET_SUBS_OF_PRF);
	output3.setParameterType(SubProfile.MY_URI);
	prof3.getProfile().addOutput(output3);
	prof3.getProfile().addSimpleOutputBinding(
		output3,
		new String[] { ProfilingService.PROP_CONTROLS,
			Profilable.PROP_HAS_PROFILE,
			Profile.PROP_HAS_SUB_PROFILE });
	profiles[3] = prof3.getProfile();

	// ADD_PROFILE(User, UserProfile) -> void
	// Adds the passed profile to the store, with all its properties (that
	// can be serialized), and associates it to the given user.
	// Currently works like CHANGE PROFILE, but does not remove previous
	// value of an associated profile to the user, if any
	Service prof4 = new ProfilingService(SRV_ADD_PRF_TO_USR);
	ProcessInput input4a = new ProcessInput(IN_ADD_PRF_TO_USR_WHERE);
	input4a.setParameterType(User.MY_URI);
	input4a.setCardinality(1, 1);//
	MergedRestriction restr4 = MergedRestriction.getFixedValueRestriction(
		ProfilingService.PROP_CONTROLS, input4a.asVariableReference());
	prof4.addInstanceLevelRestriction(restr4,
		new String[] { ProfilingService.PROP_CONTROLS });
	prof4.getProfile().addInput(input4a);
	ProcessInput input4b = new ProcessInput(IN_ADD_PRF_TO_USR_WHAT);
	input4b.setParameterType(UserProfile.MY_URI);
	input4b.setCardinality(1, 1);//
	prof4.getProfile().addInput(input4b);
	prof4.getProfile().addAddEffect(
		new String[] { ProfilingService.PROP_CONTROLS,
			Profilable.PROP_HAS_PROFILE },
		input4b.asVariableReference());
	profiles[4] = prof4.getProfile();

	// ADD_SUBPROFILE(User, SubProfile) -> void
	// Adds the passed subprofile to the store, with all its properties
	// (that can be serialized), and associates it to the user profile of
	// the given user.
	Service prof5 = new ProfilingService(SRV_ADD_SUB_TO_USR);
	ProcessInput input5a = new ProcessInput(IN_ADD_SUB_TO_USR_WHERE);
	input5a.setParameterType(User.MY_URI);
	input5a.setCardinality(1, 1);//
	MergedRestriction restr5 = MergedRestriction.getFixedValueRestriction(
		ProfilingService.PROP_CONTROLS, input5a.asVariableReference());
	prof5.addInstanceLevelRestriction(restr5,
		new String[] { ProfilingService.PROP_CONTROLS });
	prof5.getProfile().addInput(input5a);
	ProcessInput input5b = new ProcessInput(IN_ADD_SUB_TO_USR_WHAT);
	input5b.setParameterType(SubProfile.MY_URI);
	input5b.setCardinality(1, 1);//
	prof5.getProfile().addInput(input5b);
	prof5.getProfile().addAddEffect(
		new String[] { ProfilingService.PROP_CONTROLS,
			Profilable.PROP_HAS_PROFILE,
			Profile.PROP_HAS_SUB_PROFILE },
		input5b.asVariableReference());
	profiles[5] = prof5.getProfile();

	// ADD_SUBPROFILE(UserProfile, SubProfile) -> void
	// Adds the passed subprofile to the store, with all its properties
	// (that can be serialized), and associates it to the given user
	// profile.
	Service prof6 = new ProfilingService(SRV_ADD_SUB_TO_PRF);
	ProcessInput input6a = new ProcessInput(IN_ADD_SUB_TO_PRF_WHERE);
	input6a.setParameterType(UserProfile.MY_URI);
	input6a.setCardinality(1, 1);//
	MergedRestriction restr6 = MergedRestriction.getFixedValueRestriction(
		Profilable.PROP_HAS_PROFILE, input6a.asVariableReference());
	prof6.addInstanceLevelRestriction(restr6,
		new String[] { ProfilingService.PROP_CONTROLS, Profilable.PROP_HAS_PROFILE });
	prof6.getProfile().addInput(input6a);
	ProcessInput input6b = new ProcessInput(IN_ADD_SUB_TO_PRF_WHAT);
	input6b.setParameterType(SubProfile.MY_URI);
	input6b.setCardinality(1, 1);//
	prof6.getProfile().addInput(input6b);
	prof6.getProfile().addAddEffect(
		new String[] { ProfilingService.PROP_CONTROLS,
			Profilable.PROP_HAS_PROFILE,
			Profile.PROP_HAS_SUB_PROFILE },
		input6b.asVariableReference());
	profiles[6] = prof6.getProfile();
    }

    // DYNAMIC TYPICAL SERVICE PROFILES
    /**
     * Gives you the 4 typical service profiles of an editor service: Get, Add,
     * Change and Remove. When handling requests in you Callee, you can use the
     * references to services and arguments URIs prepending
     * <code>namespace</code> to SimpleEditor constants.
     * 
     * @param namespace
     *            The namespace of your server, ending with the character #. You
     *            can optionally add some prefix after the # if you use
     *            SimpleEditor more than once in the same Callee.
     * @param ontologyURI
     *            The MY_URI of the class of Service ontology you are going to
     *            implement
     * @param path
     *            The property path from the root of the Service ontology
     *            concept to the exact concept you want to manage
     * @param editedURI
     *            The MY_URI of the class of the concept ontology that you want
     *            to manage, which is at the end of the property path
     * @return An array with the 4 typical service profiles
     */
    public static ServiceProfile[] getServiceProfiles(String namespace,
	    String ontologyURI, String[] path, String editedURI) {

	ServiceProfile[] profiles = new ServiceProfile[4];

	// Get
	Service prof1 = (Service) OntologyManagement.getInstance().getResource(
		ontologyURI, namespace + SRV_GET_X);
	ProcessInput input1 = new ProcessInput(namespace + INP_GET_X);
	input1.setParameterType(editedURI);
	input1.setCardinality(1, 1);
	MergedRestriction restr1 = MergedRestriction.getFixedValueRestriction(
		path[path.length - 1], input1.asVariableReference());
	prof1.addInstanceLevelRestriction(restr1, path);
	prof1.getProfile().addInput(input1);
	ProcessOutput output = new ProcessOutput(namespace + OUT_GET_X);
	output.setParameterType(editedURI);
	prof1.getProfile().addOutput(output);
	prof1.getProfile().addSimpleOutputBinding(output, path);
	profiles[0] = prof1.getProfile();

	// Add
	Service prof2 = ((Service) OntologyManagement.getInstance()
		.getResource(ontologyURI, namespace + SRV_ADD_X));
	ProcessInput input2 = new ProcessInput(namespace + INP_ADD_X);
	input2.setParameterType(editedURI);
	input2.setCardinality(1, 1);
	prof2.getProfile().addInput(input2);
	prof2.getProfile().addAddEffect(path, input2.asVariableReference());
	profiles[1] = prof2.getProfile();

	// Change
	Service prof3 = ((Service) OntologyManagement.getInstance()
		.getResource(ontologyURI, namespace + SRV_CHN_X));
	ProcessInput input3 = new ProcessInput(namespace + INP_CHN_X);
	input3.setCardinality(1, 1);
	input3.setParameterType(editedURI);
	prof3.getProfile().addInput(input3);
	prof3.getProfile().addChangeEffect(path, input3.asVariableReference());
	profiles[2] = prof3.getProfile();

	// Remove
	Service prof4 = ((Service) OntologyManagement.getInstance()
		.getResource(ontologyURI, namespace + SRV_REM_X));
	ProcessInput input4 = new ProcessInput(namespace + INP_REM_X);
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
    
    @Override
    public String getClassURI() {
	return MY_URI;
    }

}
