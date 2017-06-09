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
package org.universAAL.context.space.serv;

import org.universAAL.middleware.owl.MergedRestriction;
import org.universAAL.middleware.owl.OntologyManagement;
import org.universAAL.middleware.owl.SimpleOntology;
import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.rdf.ResourceFactory;
import org.universAAL.middleware.service.owl.Service;
import org.universAAL.middleware.service.owls.process.ProcessInput;
import org.universAAL.middleware.service.owls.process.ProcessOutput;
import org.universAAL.middleware.service.owls.profile.ServiceProfile;
import org.universAAL.ontology.phThing.Device;
import org.universAAL.ontology.profile.AALAppSubProfile;
import org.universAAL.ontology.profile.AALService;
import org.universAAL.ontology.profile.AALServiceProfile;
import org.universAAL.ontology.profile.AALSpace;
import org.universAAL.ontology.profile.AALSpaceProfile;
import org.universAAL.ontology.profile.HRSubProfile;
import org.universAAL.ontology.profile.HWSubProfile;
import org.universAAL.ontology.profile.OntologyEntry;
import org.universAAL.ontology.profile.Profilable;
import org.universAAL.ontology.profile.User;
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
	protected static final String SRV_GET_SERVS = NAMESPACE + "servGetServices";
	protected static final String OUT_GET_SERVS = NAMESPACE + "outServices";
	protected static final String SRV_GET_SPACES = NAMESPACE + "servGetSpaces";
	protected static final String OUT_GET_SPACES = NAMESPACE + "outSpaces";
	protected static final String SRV_GET_SERVS_OF_SPACE = NAMESPACE + "servGetServicesOfSpace";
	protected static final String INP_GET_SERVS_OF_SPACE = NAMESPACE + "argDi";
	protected static final String OUT_GET_SERVS_OF_SPACE = NAMESPACE + "argDo";
	protected static final String SRV_GET_DEVS_OF_SPACE = NAMESPACE + "servGetDevicesOfSpace";
	protected static final String INP_GET_DEVS_OF_SPACE = NAMESPACE + "argEi";
	protected static final String OUT_GET_DEVS_OF_SPACE = NAMESPACE + "argEo";
	protected static final String SRV_GET_ONTS_OF_SPACE = NAMESPACE + "servGetOntsOfSpace";
	protected static final String INP_GET_ONTS_OF_SPACE = NAMESPACE + "argFi";
	protected static final String OUT_GET_ONTS_OF_SPACE = NAMESPACE + "argFo";
	protected static final String SRV_GET_HR_OF_SERV = NAMESPACE + "servGetHumanResourceOfService";
	protected static final String INP_GET_HR_OF_SERV = NAMESPACE + "servGi";
	protected static final String OUT_GET_HR_OF_SERV = NAMESPACE + "servGo";
	protected static final String SRV_GET_HW_OF_SERV = NAMESPACE + "servGetHardwareOfService";
	protected static final String INP_GET_HW_OF_SERV = NAMESPACE + "servHi";
	protected static final String OUT_GET_HW_OF_SERV = NAMESPACE + "servHo";
	protected static final String SRV_GET_APP_OF_SERV = NAMESPACE + "servGetAppOfService";
	protected static final String INP_GET_APP_OF_SERV = NAMESPACE + "servIi";
	protected static final String OUT_GET_APP_OF_SERV = NAMESPACE + "servIo";
	protected static final String SRV_ADD_SERV_TO_SPACE = NAMESPACE + "servAddServiceToSpace";
	protected static final String INP_ADD_SERV_TO_SPACE_WHERE = NAMESPACE + "servJ1";
	protected static final String INP_ADD_SERV_TO_SPACE_WHAT = NAMESPACE + "servJ2";
	protected static final String SRV_ADD_DEV_TO_SPACE = NAMESPACE + "servAddDeviceToSpace";
	protected static final String INP_ADD_DEV_TO_SPACE_WHERE = NAMESPACE + "servK1";
	protected static final String INP_ADD_DEV_TO_SPACE_WHAT = NAMESPACE + "servK2";
	protected static final String SRV_ADD_ONT_TO_SPACE = NAMESPACE + "servAddOntologyToSpace";
	protected static final String INP_ADD_ONT_TO_SPACE_WHERE = NAMESPACE + "servL1";
	protected static final String INP_ADD_ONT_TO_SPACE_WHAT = NAMESPACE + "servL2";
	protected static final String SRV_ADD_PROF_TO_SPACE = NAMESPACE + "servAddProfileToSpace";
	protected static final String INP_ADD_PROF_TO_SPACE_WHERE = NAMESPACE + "servM1";
	protected static final String INP_ADD_PROF_TO_SPACE_WHAT = NAMESPACE + "servM2";
	protected static final String SRV_ADD_PROF_TO_SERV = NAMESPACE + "servAddProfileToService";
	protected static final String INP_ADD_PROF_TO_SERV_WHERE = NAMESPACE + "servN1";
	protected static final String INP_ADD_PROF_TO_SERV_WHAT = NAMESPACE + "servN2";
	protected static final String SRV_GET_OWNERS_OF_SPACE = NAMESPACE + "servGetOwnersOfSpace";
	protected static final String INP_GET_OWNERS_OF_SPACE = NAMESPACE + "argOi";
	protected static final String OUT_GET_OWNERS_OF_SPACE = NAMESPACE + "argOo";
	protected static final String SRV_GET_OWNERS_OF_SERV = NAMESPACE + "servGetOwnersOfService";
	protected static final String INP_GET_OWNERS_OF_SERV = NAMESPACE + "argPi";
	protected static final String OUT_GET_OWNERS_OF_SERV = NAMESPACE + "argPo";
	protected static final String SRV_ADD_OWNER_TO_SPACE = NAMESPACE + "servAddOwnerToSpace";
	protected static final String INP_ADD_OWNER_TO_SPACE_WHERE = NAMESPACE + "servQ1";
	protected static final String INP_ADD_OWNER_TO_SPACE_WHAT = NAMESPACE + "servQ2";
	protected static final String SRV_ADD_OWNER_TO_SERV = NAMESPACE + "servAddOwnerToService";
	protected static final String INP_ADD_OWNER_TO_SERV_WHERE = NAMESPACE + "servR1";
	protected static final String INP_ADD_OWNER_TO_SERV_WHAT = NAMESPACE + "servR2";
	protected static final String SRV_GET_PROF_OF_SPACE = NAMESPACE + "servGetProfileOfSpace";
	protected static final String INP_GET_PROF_OF_SPACE = NAMESPACE + "argSi";
	protected static final String OUT_GET_PROF_OF_SPACE = NAMESPACE + "argSo";
	protected static final String SRV_GET_PROF_OF_SERV = NAMESPACE + "servGetProfileOfService";
	protected static final String INP_GET_PROF_OF_SERV = NAMESPACE + "argTi";
	protected static final String OUT_GET_PROF_OF_SERV = NAMESPACE + "argTo";

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
	 * @param uri
	 *            Instance URI
	 */
	public SCalleeProvidedService(String uri) {
		super(uri);
	}

	/**
	 * Where the service profiles are stored. Only the non-typical ones. The
	 * typical get/add/change/remove are extracted from method
	 * getServiceProfiles.
	 */
	protected static ServiceProfile[] profiles = new ServiceProfile[19];

	static {
		OntologyManagement.getInstance().register(Activator.context,
				new SimpleOntology(MY_URI, ProfilingService.MY_URI, new ResourceFactory() {
					public Resource createInstance(String classURI, String instanceURI, int factoryIndex) {
						return new SCalleeProvidedService(instanceURI);
					}
				}));

		// STATIC NON-TYPICAL SERVICE PROFILES

		// GET_AALSERVICES() -> [AALService]
		// Gets all AAL Services present in the system.
		SCalleeProvidedService prof0 = new SCalleeProvidedService(SRV_GET_SERVS);
		prof0.addOutput(OUT_GET_SERVS, AALService.MY_URI, 0, -1, new String[] { ProfilingService.PROP_CONTROLS });
		profiles[0] = prof0.getProfile();

		// GET_AALSPACES() -> [AALSpace]
		// Gets all AAL Spaces present in the system.
		SCalleeProvidedService prof1 = new SCalleeProvidedService(SRV_GET_SPACES);
		prof1.addOutput(OUT_GET_SPACES, AALSpace.MY_URI, 0, -1, new String[] { ProfilingService.PROP_CONTROLS });
		profiles[1] = prof1.getProfile();

		// GET_AALSERVICES(AALSpace) -> [AALService]
		// Returns the AAL Services (only their URIs) installed in a AALSpace
		// which URI matches the one of the passed parameter
		SCalleeProvidedService prof2 = new SCalleeProvidedService(SRV_GET_SERVS_OF_SPACE);
		prof2.addFilteringInput(INP_GET_SERVS_OF_SPACE, AALSpace.MY_URI, 1, 1,
				new String[] { ProfilingService.PROP_CONTROLS });
		prof2.addOutput(OUT_GET_SERVS_OF_SPACE, AALService.MY_URI, 0, -1, new String[] { ProfilingService.PROP_CONTROLS,
				Profilable.PROP_HAS_PROFILE, AALSpaceProfile.PROP_INSTALLED_SERVICES });
		profiles[2] = prof2.getProfile();

		// GET_DEVICES(AALSpace) -> [Device]
		// Returns the Devices (only their URIs) installed in a AALSpace
		// which URI matches the one of the passed parameter
		SCalleeProvidedService prof3 = new SCalleeProvidedService(SRV_GET_DEVS_OF_SPACE);
		prof3.addFilteringInput(INP_GET_DEVS_OF_SPACE, AALSpace.MY_URI, 1, 1,
				new String[] { ProfilingService.PROP_CONTROLS });
		prof3.addOutput(OUT_GET_DEVS_OF_SPACE, Device.MY_URI, 0, -1, new String[] { ProfilingService.PROP_CONTROLS,
				Profilable.PROP_HAS_PROFILE, AALSpaceProfile.PROP_INSTALLED_HARDWARE });
		profiles[3] = prof3.getProfile();

		// GET_ONTS(AALSpace) -> [Ont]
		// Returns the Devices (only their URIs) installed in a AALSpace
		// which URI matches the one of the passed parameter
		SCalleeProvidedService prof4 = new SCalleeProvidedService(SRV_GET_ONTS_OF_SPACE);
		prof4.addFilteringInput(INP_GET_ONTS_OF_SPACE, AALSpace.MY_URI, 1, 1,
				new String[] { ProfilingService.PROP_CONTROLS });
		prof4.addOutput(OUT_GET_ONTS_OF_SPACE, OntologyEntry.MY_URI, 0, -1,
				new String[] { ProfilingService.PROP_CONTROLS, Profilable.PROP_HAS_PROFILE,
						AALSpaceProfile.PROP_INSTALLED_ONTOLOGIES });
		profiles[4] = prof4.getProfile();

		// GET_HR(AALService) -> [HRSubP]
		// Returns the HRSubProfile, with all its properties (that can be
		// serialized) belonging to the AALService which URI matches the one of
		// the passed parameter
		SCalleeProvidedService prof5 = new SCalleeProvidedService(SRV_GET_HR_OF_SERV);
		prof5.addFilteringInput(INP_GET_HR_OF_SERV, AALService.MY_URI, 1, 1,
				new String[] { ProfilingService.PROP_CONTROLS });
		prof5.addOutput(OUT_GET_HR_OF_SERV, HRSubProfile.MY_URI, 0, -1, new String[] { ProfilingService.PROP_CONTROLS,
				Profilable.PROP_HAS_PROFILE, AALServiceProfile.PROP_HUMAN_RESOURCE_SUBPROFILE });
		profiles[5] = prof5.getProfile();

		// GET_HW(AALService) -> [HWSubP]
		// Returns the HWSubProfile, with all its properties (that can be
		// serialized) belonging to the AALService which URI matches the one of
		// the passed parameter
		SCalleeProvidedService prof6 = new SCalleeProvidedService(SRV_GET_HW_OF_SERV);
		prof6.addFilteringInput(INP_GET_HW_OF_SERV, AALService.MY_URI, 1, 1,
				new String[] { ProfilingService.PROP_CONTROLS });
		prof6.addOutput(OUT_GET_HW_OF_SERV, HWSubProfile.MY_URI, 0, -1, new String[] { ProfilingService.PROP_CONTROLS,
				Profilable.PROP_HAS_PROFILE, AALServiceProfile.PROP_HARDWARE_SUBPROFILE });
		profiles[6] = prof6.getProfile();

		// GET_APP(AALService) -> [AppSubP]
		// Returns the AppSubProfile, with all its properties (that can be
		// serialized) belonging to the AALService which URI matches the one of
		// the passed parameter
		SCalleeProvidedService prof7 = new SCalleeProvidedService(SRV_GET_APP_OF_SERV);
		prof7.addFilteringInput(INP_GET_APP_OF_SERV, AALService.MY_URI, 1, 1,
				new String[] { ProfilingService.PROP_CONTROLS });
		prof7.addOutput(OUT_GET_APP_OF_SERV, AALAppSubProfile.MY_URI, 0, -1,
				new String[] { ProfilingService.PROP_CONTROLS, Profilable.PROP_HAS_PROFILE,
						AALServiceProfile.PROP_APPLICATION_SUBPROFILE });
		profiles[7] = prof7.getProfile();

		// ADD_SERV(AALSpace, AALService) -> void
		// Adds the passed AALService to the store, with all its properties
		// (that can be serialized), and associates it to the given AALSpace.
		// Currently works like CHANGE SERVICE, but does not remove previous
		// value of an associated AALService to the AALSpace, if any
		SCalleeProvidedService prof8 = new SCalleeProvidedService(SRV_ADD_SERV_TO_SPACE);
		prof8.addFilteringInput(INP_ADD_SERV_TO_SPACE_WHERE, AALSpace.MY_URI, 1, 1,
				new String[] { ProfilingService.PROP_CONTROLS });
		prof8.addInputWithAddEffect(INP_ADD_SERV_TO_SPACE_WHAT, AALService.MY_URI, 1, 1, new String[] {
				ProfilingService.PROP_CONTROLS, Profilable.PROP_HAS_PROFILE, AALSpaceProfile.PROP_INSTALLED_SERVICES });
		profiles[8] = prof8.getProfile();

		// ADD_DEV(AALSpace, Device) -> void
		// Adds the passed Device to the store, with all its properties
		// (that can be serialized), and associates it to the given AALSpace.
		// Currently works like CHANGE DEVICE, but does not remove previous
		// value of an associated Device to the AALSpace, if any
		SCalleeProvidedService prof9 = new SCalleeProvidedService(SRV_ADD_DEV_TO_SPACE);
		prof9.addFilteringInput(INP_ADD_DEV_TO_SPACE_WHERE, AALSpace.MY_URI, 1, 1,
				new String[] { ProfilingService.PROP_CONTROLS });
		prof9.addInputWithAddEffect(INP_ADD_DEV_TO_SPACE_WHAT, Device.MY_URI, 1, 1, new String[] {
				ProfilingService.PROP_CONTROLS, Profilable.PROP_HAS_PROFILE, AALSpaceProfile.PROP_INSTALLED_HARDWARE });
		profiles[9] = prof9.getProfile();

		// ADD_ONT(AALSpace, Ont) -> void
		// Adds the passed Ont to the store, with all its properties
		// (that can be serialized), and associates it to the given AALSpace.
		// Currently works like CHANGE ONT, but does not remove previous
		// value of an associated Ont to the AALSpace, if any
		SCalleeProvidedService prof10 = new SCalleeProvidedService(SRV_ADD_ONT_TO_SPACE);
		prof10.addFilteringInput(INP_ADD_ONT_TO_SPACE_WHERE, AALSpace.MY_URI, 1, 1,
				new String[] { ProfilingService.PROP_CONTROLS });
		prof10.addInputWithAddEffect(INP_ADD_ONT_TO_SPACE_WHAT, OntologyEntry.MY_URI, 1, 1,
				new String[] { ProfilingService.PROP_CONTROLS, Profilable.PROP_HAS_PROFILE,
						AALSpaceProfile.PROP_INSTALLED_ONTOLOGIES });
		profiles[10] = prof10.getProfile();

		// ADD_PROF(AALSpace, AALSpaceProf) -> void
		// Adds the passed AALSPaceProf to the store, with all its properties
		// (that can be serialized), and associates it to the given AALSpace.
		// Currently works like CHANGE PROF, but does not remove previous
		// value of an associated profile to the AALSpace, if any
		SCalleeProvidedService prof11 = new SCalleeProvidedService(SRV_ADD_PROF_TO_SPACE);
		prof11.addFilteringInput(INP_ADD_PROF_TO_SPACE_WHERE, AALSpace.MY_URI, 1, 1,
				new String[] { ProfilingService.PROP_CONTROLS });
		prof11.addInputWithAddEffect(INP_ADD_PROF_TO_SPACE_WHAT, AALSpaceProfile.MY_URI, 1, 1,
				new String[] { ProfilingService.PROP_CONTROLS, Profilable.PROP_HAS_PROFILE });
		profiles[11] = prof11.getProfile();

		// ADD_PROF(AALServ, AALServProf) -> void
		// Adds the passed AALServProf to the store, with all its properties
		// (that can be serialized), and associates it to the given AALServ.
		// Currently works like CHANGE PROF, but does not remove previous
		// value of an associated profile to the AALServ, if any
		SCalleeProvidedService prof12 = new SCalleeProvidedService(SRV_ADD_PROF_TO_SERV);
		prof12.addFilteringInput(INP_ADD_PROF_TO_SERV_WHERE, AALService.MY_URI, 1, 1,
				new String[] { ProfilingService.PROP_CONTROLS });
		prof12.addInputWithAddEffect(INP_ADD_PROF_TO_SERV_WHAT, AALServiceProfile.MY_URI, 1, 1,
				new String[] { ProfilingService.PROP_CONTROLS, Profilable.PROP_HAS_PROFILE });
		profiles[12] = prof12.getProfile();

		// GET_OWNERS(AALSpace) -> [User]
		// Returns the Users (only their URIs) owning a AALSpace
		// which URI matches the one of the passed parameter
		SCalleeProvidedService prof13 = new SCalleeProvidedService(SRV_GET_OWNERS_OF_SPACE);
		prof13.addFilteringInput(INP_GET_OWNERS_OF_SPACE, AALSpace.MY_URI, 1, 1,
				new String[] { ProfilingService.PROP_CONTROLS });
		prof13.addOutput(OUT_GET_OWNERS_OF_SPACE, User.MY_URI, 0, -1, new String[] { ProfilingService.PROP_CONTROLS,
				Profilable.PROP_HAS_PROFILE, AALSpaceProfile.PROP_SPACE_OWNER });
		profiles[13] = prof13.getProfile();

		// GET_OWNERS(AALService) -> [User]
		// Returns the Users (only their URIs) owning a AALService
		// which URI matches the one of the passed parameter
		SCalleeProvidedService prof14 = new SCalleeProvidedService(SRV_GET_OWNERS_OF_SERV);
		prof14.addFilteringInput(INP_GET_OWNERS_OF_SERV, AALService.MY_URI, 1, 1,
				new String[] { ProfilingService.PROP_CONTROLS });
		prof14.addOutput(OUT_GET_OWNERS_OF_SERV, User.MY_URI, 0, -1, new String[] { ProfilingService.PROP_CONTROLS,
				Profilable.PROP_HAS_PROFILE, AALServiceProfile.PROP_SERVICE_OWNER });
		profiles[14] = prof14.getProfile();

		// ADD_OWNER(AALSpace, User) -> void
		// Adds the passed User to the store, with all its properties
		// (that can be serialized), and associates it to the given AALSpace.
		// Does not remove previous value of an associated User to the AALSpace
		SCalleeProvidedService prof15 = new SCalleeProvidedService(SRV_ADD_OWNER_TO_SPACE);
		prof15.addFilteringInput(INP_ADD_OWNER_TO_SPACE_WHERE, AALSpace.MY_URI, 1, 1,
				new String[] { ProfilingService.PROP_CONTROLS });
		prof15.addInputWithAddEffect(INP_ADD_OWNER_TO_SPACE_WHAT, User.MY_URI, 1, 1, new String[] {
				ProfilingService.PROP_CONTROLS, Profilable.PROP_HAS_PROFILE, AALSpaceProfile.PROP_SPACE_OWNER });
		profiles[15] = prof15.getProfile();

		// ADD_OWNER(AALService, User) -> void
		// Adds the passed User to the store, with all its properties
		// (that can be serialized), and associates it to the given AALService.
		// Does not remove previous value of an associated User to the AALServ
		SCalleeProvidedService prof16 = new SCalleeProvidedService(SRV_ADD_OWNER_TO_SERV);
		prof16.addFilteringInput(INP_ADD_OWNER_TO_SERV_WHERE, AALService.MY_URI, 1, 1,
				new String[] { ProfilingService.PROP_CONTROLS });
		prof16.addInputWithAddEffect(INP_ADD_OWNER_TO_SERV_WHAT, User.MY_URI, 1, 1, new String[] {
				ProfilingService.PROP_CONTROLS, Profilable.PROP_HAS_PROFILE, AALServiceProfile.PROP_SERVICE_OWNER });
		profiles[16] = prof16.getProfile();

		// GET_PROF(AALService) -> AALServiceProfile
		// Returns the AALServiceProfile of a AALService
		// which URI matches the one of the passed parameter
		SCalleeProvidedService prof17 = new SCalleeProvidedService(SRV_GET_PROF_OF_SERV);
		prof17.addFilteringInput(INP_GET_PROF_OF_SERV, AALService.MY_URI, 1, 1,
				new String[] { ProfilingService.PROP_CONTROLS });
		prof17.addOutput(OUT_GET_PROF_OF_SERV, AALServiceProfile.MY_URI, 0, -1,
				new String[] { ProfilingService.PROP_CONTROLS, Profilable.PROP_HAS_PROFILE });
		profiles[17] = prof17.getProfile();

		// GET_PROF(AALService) -> AALServiceProfile
		// Returns the AALServiceProfile of a AALService
		// which URI matches the one of the passed parameter
		SCalleeProvidedService prof18 = new SCalleeProvidedService(SRV_GET_PROF_OF_SPACE);
		prof18.addFilteringInput(INP_GET_PROF_OF_SPACE, AALSpace.MY_URI, 1, 1,
				new String[] { ProfilingService.PROP_CONTROLS });
		prof18.addOutput(OUT_GET_PROF_OF_SPACE, AALSpaceProfile.MY_URI, 0, -1,
				new String[] { ProfilingService.PROP_CONTROLS, Profilable.PROP_HAS_PROFILE });
		profiles[18] = prof18.getProfile();

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
	public static ServiceProfile[] getServiceProfiles(String namespace, String ontologyURI, String[] path,
			String editedURI) {

		ServiceProfile[] profiles = new ServiceProfile[4];

		// Get
		Service prof1 = (Service) OntologyManagement.getInstance().getResource(ontologyURI, namespace + SRV_GET_X);
		ProcessInput input1 = new ProcessInput(namespace + INP_GET_X);
		input1.setParameterType(editedURI);
		input1.setCardinality(1, 1);
		MergedRestriction restr1 = MergedRestriction.getFixedValueRestriction(path[path.length - 1],
				input1.asVariableReference());
		prof1.addInstanceLevelRestriction(restr1, path);
		prof1.getProfile().addInput(input1);
		ProcessOutput output = new ProcessOutput(namespace + OUT_GET_X);
		output.setParameterType(editedURI);
		prof1.getProfile().addOutput(output);
		prof1.getProfile().addSimpleOutputBinding(output, path);
		prof1.addInstanceLevelRestriction(MergedRestriction.getAllValuesRestriction(path[path.length - 1], editedURI),
				path);
		profiles[0] = prof1.getProfile();

		// Add
		Service prof2 = ((Service) OntologyManagement.getInstance().getResource(ontologyURI, namespace + SRV_ADD_X));
		ProcessInput input2 = new ProcessInput(namespace + INP_ADD_X);
		input2.setParameterType(editedURI);
		input2.setCardinality(1, 1);
		prof2.getProfile().addInput(input2);
		prof2.getProfile().addAddEffect(path, input2.asVariableReference());
		profiles[1] = prof2.getProfile();

		// Change
		Service prof3 = ((Service) OntologyManagement.getInstance().getResource(ontologyURI, namespace + SRV_CHN_X));
		ProcessInput input3 = new ProcessInput(namespace + INP_CHN_X);
		input3.setCardinality(1, 1);
		input3.setParameterType(editedURI);
		prof3.getProfile().addInput(input3);
		prof3.getProfile().addChangeEffect(path, input3.asVariableReference());
		profiles[2] = prof3.getProfile();

		// Remove
		Service prof4 = ((Service) OntologyManagement.getInstance().getResource(ontologyURI, namespace + SRV_REM_X));
		ProcessInput input4 = new ProcessInput(namespace + INP_REM_X);
		input4.setParameterType(editedURI);
		input4.setCardinality(1, 1);
		prof4.getProfile().addInput(input4);
		MergedRestriction restr4 = MergedRestriction.getFixedValueRestriction(path[path.length - 1],
				input4.asVariableReference());
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
