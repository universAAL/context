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

import java.util.ArrayList;

import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.container.utils.LogUtils;
import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.service.CallStatus;
import org.universAAL.middleware.service.ServiceCall;
import org.universAAL.middleware.service.ServiceCallee;
import org.universAAL.middleware.service.ServiceResponse;
import org.universAAL.middleware.service.owls.process.ProcessOutput;
import org.universAAL.middleware.service.owls.profile.ServiceProfile;
import org.universAAL.ontology.phThing.Device;
import org.universAAL.ontology.profile.AALService;
import org.universAAL.ontology.profile.AALServiceProfile;
import org.universAAL.ontology.profile.AALSpace;
import org.universAAL.ontology.profile.AALSpaceProfile;
import org.universAAL.ontology.profile.OntologyEntry;
import org.universAAL.ontology.profile.Profilable;
import org.universAAL.ontology.profile.service.ProfilingService;

/**
 * The service callee receives all service calls issued to the profiling server
 * through the service bus. Converts them to requests to CHE and relays the
 * result.
 * 
 * @author alfiva
 * 
 */
public class SCallee extends ServiceCallee {

	/**
	 * Default error for invalid input.
	 */
	private static final ServiceResponse ERROR_INPUT = new ServiceResponse(CallStatus.serviceSpecificFailure);
	/**
	 * Default namespace root.
	 */
	protected static final String NAMESPACE = "http://ontology.universAAL.org/SpaceServer.owl#";
	/**
	 * Namespace for AALSPACE services.
	 */
	private static final String NAMESPACE_AALSPACE = NAMESPACE + "space";
	/**
	 * Namespace for AALSPACE PROFILE services.
	 */
	private static final String NAMESPACE_AALSPACEPROF = NAMESPACE + "profspace";
	/**
	 * Namespace for AALSERVICE services.
	 */
	private static final String NAMESPACE_AALSERVICE = NAMESPACE + "serv";
	/**
	 * Namespace for AALSERVICE PROFILE services.
	 */
	private static final String NAMESPACE_AALSERVICEPROF = NAMESPACE + "profserv";
	/**
	 * Namespace for Device services.
	 */
	private static final String NAMESPACE_DEVICE = NAMESPACE + "dev";
	/**
	 * Namespace for Ont services.
	 */
	private static final String NAMESPACE_ONT = NAMESPACE + "ont";
	/**
	 * The uAAL module context.
	 */
	private ModuleContext mc;

	/**
	 * Default 2-argument constructor. Must not be used.
	 * 
	 * @param context
	 *            uAAL module context
	 * @param realizedServices
	 *            provided service profiles
	 */
	protected SCallee(ModuleContext context, ServiceProfile[] realizedServices) {
		super(context, realizedServices);
		this.mc = context;
		ERROR_INPUT.addOutput(new ProcessOutput(ServiceResponse.PROP_SERVICE_SPECIFIC_ERROR, "Invalid input"));
	}

	/**
	 * Default constructor.
	 * 
	 * @param context
	 *            uAAL module context
	 */
	protected SCallee(ModuleContext context) {
		super(context, SCalleeProvidedService.profiles);
		this.addNewServiceProfiles(SCalleeProvidedService.getServiceProfiles(NAMESPACE_AALSPACE,
				ProfilingService.MY_URI, new String[] { ProfilingService.PROP_CONTROLS }, AALSpace.MY_URI));
		this.addNewServiceProfiles(SCalleeProvidedService.getServiceProfiles(NAMESPACE_AALSPACEPROF,
				ProfilingService.MY_URI, new String[] { ProfilingService.PROP_CONTROLS, Profilable.PROP_HAS_PROFILE },
				AALSpaceProfile.MY_URI));
		this.addNewServiceProfiles(SCalleeProvidedService.getServiceProfiles(NAMESPACE_AALSERVICE,
				ProfilingService.MY_URI, new String[] { ProfilingService.PROP_CONTROLS }, AALService.MY_URI));
		this.addNewServiceProfiles(SCalleeProvidedService.getServiceProfiles(NAMESPACE_AALSERVICEPROF,
				ProfilingService.MY_URI, new String[] { ProfilingService.PROP_CONTROLS, Profilable.PROP_HAS_PROFILE },
				AALServiceProfile.MY_URI));
		this.addNewServiceProfiles(
				SCalleeProvidedService
						.getServiceProfiles(NAMESPACE_DEVICE,
								ProfilingService.MY_URI, new String[] { ProfilingService.PROP_CONTROLS,
										Profilable.PROP_HAS_PROFILE, AALSpaceProfile.PROP_INSTALLED_HARDWARE },
								Device.MY_URI));
		this.addNewServiceProfiles(SCalleeProvidedService.getServiceProfiles(
				NAMESPACE_ONT, ProfilingService.MY_URI, new String[] { ProfilingService.PROP_CONTROLS,
						Profilable.PROP_HAS_PROFILE, AALSpaceProfile.PROP_INSTALLED_ONTOLOGIES },
				OntologyEntry.MY_URI));
		// this.addNewRegParams(SCalleeProvidedService.profiles);
		ERROR_INPUT.addOutput(new ProcessOutput(ServiceResponse.PROP_SERVICE_SPECIFIC_ERROR, "Invalid input"));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.universAAL.middleware.service.ServiceCallee#
	 * communicationChannelBroken ()
	 */
	public void communicationChannelBroken() {
		// TODO Auto-generated method stub
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.universAAL.middleware.service.ServiceCallee#handleCall(org.universAAL
	 * .middleware.service.ServiceCall)
	 */
	public ServiceResponse handleCall(ServiceCall call) {
		if (call == null)
			return null;

		String operation = call.getProcessURI();
		if (operation == null)
			return null;

		if (operation.endsWith("Process"))
			operation = operation.substring(0, operation.length() - "Process".length());

		LogUtils.logDebug(mc, SCallee.class, "handleCall", new String[] { operation }, null);

		// TODO: All get/add/... seem to do the same (except operation URI). Do
		// something about it?
		// When de/serialized from/to client inputs
		// are treated as RDF with all their properties, including type,
		// and in the client part they are parsed to the most specialized class,
		// which is supposed to be known by the client.

		// :::::::::::::AALSPACE GET/ADD/CHANGE/REMOVE:::::::::::::::::

		if (operation.equals(NAMESPACE_AALSPACE + SCalleeProvidedService.SRV_GET_X)) {
			Object input = call.getInputValue(NAMESPACE_AALSPACE + SCalleeProvidedService.INP_GET_X);
			if (input == null) {
				return ERROR_INPUT;
			}
			Resource result = Activator.scaller.getAALSpace((Resource) input);
			ServiceResponse response = new ServiceResponse(CallStatus.succeeded);
			response.addOutput(new ProcessOutput(NAMESPACE_AALSPACE + SCalleeProvidedService.OUT_GET_X, result));
			return response;
		}

		if (operation.equals(NAMESPACE_AALSPACE + SCalleeProvidedService.SRV_ADD_X)) {
			Object input = call.getInputValue(NAMESPACE_AALSPACE + SCalleeProvidedService.INP_ADD_X);
			if (input == null) {
				return ERROR_INPUT;
			}
			Activator.scaller.addAALSpace((Resource) input);
			return new ServiceResponse(CallStatus.succeeded);
		}

		if (operation.equals(NAMESPACE_AALSPACE + SCalleeProvidedService.SRV_CHN_X)) {
			Object input = call.getInputValue(NAMESPACE_AALSPACE + SCalleeProvidedService.INP_CHN_X);
			if (input == null) {
				return ERROR_INPUT;
			}
			Activator.scaller.changeAALSpace((Resource) input);
			return new ServiceResponse(CallStatus.succeeded);
		}

		if (operation.equals(NAMESPACE_AALSPACE + SCalleeProvidedService.SRV_REM_X)) {
			Object input = call.getInputValue(NAMESPACE_AALSPACE + SCalleeProvidedService.INP_REM_X);
			if (input == null) {
				return ERROR_INPUT;
			}
			Activator.scaller.removeAALSpace((Resource) input);
			return new ServiceResponse(CallStatus.succeeded);
		}

		// :::::::::::::AALSPACEPROFILE GET/ADD/CHANGE/REMOVE:::::::::::::::::

		if (operation.equals(NAMESPACE_AALSPACEPROF + SCalleeProvidedService.SRV_GET_X)) {
			Object input = call.getInputValue(NAMESPACE_AALSPACEPROF + SCalleeProvidedService.INP_GET_X);
			if (input == null) {
				return ERROR_INPUT;
			}
			Resource result = Activator.scaller.getAALSpaceProfile((Resource) input);
			ServiceResponse response = new ServiceResponse(CallStatus.succeeded);
			response.addOutput(new ProcessOutput(NAMESPACE_AALSPACEPROF + SCalleeProvidedService.OUT_GET_X, result));
			return response;
		}

		if (operation.equals(NAMESPACE_AALSPACEPROF + SCalleeProvidedService.SRV_ADD_X)) {
			Object input = call.getInputValue(NAMESPACE_AALSPACEPROF + SCalleeProvidedService.INP_ADD_X);
			if (input == null) {
				return ERROR_INPUT;
			}
			Activator.scaller.addAALSpaceProfile((Resource) input);
			return new ServiceResponse(CallStatus.succeeded);
		}

		if (operation.equals(NAMESPACE_AALSPACEPROF + SCalleeProvidedService.SRV_CHN_X)) {
			Object input = call.getInputValue(NAMESPACE_AALSPACEPROF + SCalleeProvidedService.INP_CHN_X);
			if (input == null) {
				return ERROR_INPUT;
			}
			Activator.scaller.changeAALSpaceProfile((Resource) input);
			return new ServiceResponse(CallStatus.succeeded);
		}

		if (operation.equals(NAMESPACE_AALSPACEPROF + SCalleeProvidedService.SRV_REM_X)) {
			Object input = call.getInputValue(NAMESPACE_AALSPACEPROF + SCalleeProvidedService.INP_REM_X);
			if (input == null) {
				return ERROR_INPUT;
			}
			Activator.scaller.removeAALSpaceProfile((Resource) input);
			return new ServiceResponse(CallStatus.succeeded);
		}

		// :::::::::::::AALSERVICE GET/ADD/CHANGE/REMOVE:::::::::::::::::

		if (operation.equals(NAMESPACE_AALSERVICE + SCalleeProvidedService.SRV_GET_X)) {
			Object input = call.getInputValue(NAMESPACE_AALSERVICE + SCalleeProvidedService.INP_GET_X);
			if (input == null) {
				return ERROR_INPUT;
			}
			Resource result = Activator.scaller.getAALService((Resource) input);
			ServiceResponse response = new ServiceResponse(CallStatus.succeeded);
			response.addOutput(new ProcessOutput(NAMESPACE_AALSERVICE + SCalleeProvidedService.OUT_GET_X, result));
			return response;
		}

		if (operation.equals(NAMESPACE_AALSERVICE + SCalleeProvidedService.SRV_ADD_X)) {
			Object input = call.getInputValue(NAMESPACE_AALSERVICE + SCalleeProvidedService.INP_ADD_X);
			if (input == null) {
				return ERROR_INPUT;
			}
			Activator.scaller.addAALService((Resource) input);
			return new ServiceResponse(CallStatus.succeeded);
		}

		if (operation.equals(NAMESPACE_AALSERVICE + SCalleeProvidedService.SRV_CHN_X)) {
			Object input = call.getInputValue(NAMESPACE_AALSERVICE + SCalleeProvidedService.INP_CHN_X);
			if (input == null) {
				return ERROR_INPUT;
			}
			Activator.scaller.changeAALService((Resource) input);
			return new ServiceResponse(CallStatus.succeeded);
		}

		if (operation.equals(NAMESPACE_AALSERVICE + SCalleeProvidedService.SRV_REM_X)) {
			Object input = call.getInputValue(NAMESPACE_AALSERVICE + SCalleeProvidedService.INP_REM_X);
			if (input == null) {
				return ERROR_INPUT;
			}
			Activator.scaller.removeAALService((Resource) input);
			return new ServiceResponse(CallStatus.succeeded);
		}

		// :::::::::::::AALSERVICEPROF GET/ADD/CHANGE/REMOVE:::::::::::::::::

		if (operation.equals(NAMESPACE_AALSERVICEPROF + SCalleeProvidedService.SRV_GET_X)) {
			Object input = call.getInputValue(NAMESPACE_AALSERVICEPROF + SCalleeProvidedService.INP_GET_X);
			if (input == null) {
				return ERROR_INPUT;
			}
			Resource result = Activator.scaller.getAALServiceProf((Resource) input);
			ServiceResponse response = new ServiceResponse(CallStatus.succeeded);
			response.addOutput(new ProcessOutput(NAMESPACE_AALSERVICEPROF + SCalleeProvidedService.OUT_GET_X, result));
			return response;
		}

		if (operation.equals(NAMESPACE_AALSERVICEPROF + SCalleeProvidedService.SRV_ADD_X)) {
			Object input = call.getInputValue(NAMESPACE_AALSERVICEPROF + SCalleeProvidedService.INP_ADD_X);
			if (input == null) {
				return ERROR_INPUT;
			}
			Activator.scaller.addAALServiceProf((Resource) input);
			return new ServiceResponse(CallStatus.succeeded);
		}

		if (operation.equals(NAMESPACE_AALSERVICEPROF + SCalleeProvidedService.SRV_CHN_X)) {
			Object input = call.getInputValue(NAMESPACE_AALSERVICEPROF + SCalleeProvidedService.INP_CHN_X);
			if (input == null) {
				return ERROR_INPUT;
			}
			Activator.scaller.changeAALServiceProf((Resource) input);
			return new ServiceResponse(CallStatus.succeeded);
		}

		if (operation.equals(NAMESPACE_AALSERVICEPROF + SCalleeProvidedService.SRV_REM_X)) {
			Object input = call.getInputValue(NAMESPACE_AALSERVICEPROF + SCalleeProvidedService.INP_REM_X);
			if (input == null) {
				return ERROR_INPUT;
			}
			Activator.scaller.removeAALServiceProf((Resource) input);
			return new ServiceResponse(CallStatus.succeeded);
		}

		// :::::::::::::DEVICE GET/ADD/CHANGE/REMOVE:::::::::::::::::

		if (operation.equals(NAMESPACE_DEVICE + SCalleeProvidedService.SRV_GET_X)) {
			Object input = call.getInputValue(NAMESPACE_DEVICE + SCalleeProvidedService.INP_GET_X);
			if (input == null) {
				return ERROR_INPUT;
			}
			Resource result = Activator.scaller.getDevice((Resource) input);
			ServiceResponse response = new ServiceResponse(CallStatus.succeeded);
			response.addOutput(new ProcessOutput(NAMESPACE_DEVICE + SCalleeProvidedService.OUT_GET_X, result));
			return response;
		}

		if (operation.equals(NAMESPACE_DEVICE + SCalleeProvidedService.SRV_ADD_X)) {
			Object input = call.getInputValue(NAMESPACE_DEVICE + SCalleeProvidedService.INP_ADD_X);
			if (input == null) {
				return ERROR_INPUT;
			}
			Activator.scaller.addDevice((Resource) input);
			return new ServiceResponse(CallStatus.succeeded);
		}

		if (operation.equals(NAMESPACE_DEVICE + SCalleeProvidedService.SRV_CHN_X)) {
			Object input = call.getInputValue(NAMESPACE_DEVICE + SCalleeProvidedService.INP_CHN_X);
			if (input == null) {
				return ERROR_INPUT;
			}
			Activator.scaller.changeDevice((Resource) input);
			return new ServiceResponse(CallStatus.succeeded);
		}

		if (operation.equals(NAMESPACE_DEVICE + SCalleeProvidedService.SRV_REM_X)) {
			Object input = call.getInputValue(NAMESPACE_DEVICE + SCalleeProvidedService.INP_REM_X);
			if (input == null) {
				return ERROR_INPUT;
			}
			Activator.scaller.removeDevice((Resource) input);
			return new ServiceResponse(CallStatus.succeeded);
		}

		// :::::::::::::ONT GET/ADD/CHANGE/REMOVE:::::::::::::::::

		if (operation.equals(NAMESPACE_ONT + SCalleeProvidedService.SRV_GET_X)) {
			Object input = call.getInputValue(NAMESPACE_ONT + SCalleeProvidedService.INP_GET_X);
			if (input == null) {
				return ERROR_INPUT;
			}
			Resource result = Activator.scaller.getOnt((Resource) input);
			ServiceResponse response = new ServiceResponse(CallStatus.succeeded);
			response.addOutput(new ProcessOutput(NAMESPACE_ONT + SCalleeProvidedService.OUT_GET_X, result));
			return response;
		}

		if (operation.equals(NAMESPACE_ONT + SCalleeProvidedService.SRV_ADD_X)) {
			Object input = call.getInputValue(NAMESPACE_ONT + SCalleeProvidedService.INP_ADD_X);
			if (input == null) {
				return ERROR_INPUT;
			}
			Activator.scaller.addOnt((Resource) input);
			return new ServiceResponse(CallStatus.succeeded);
		}

		if (operation.equals(NAMESPACE_ONT + SCalleeProvidedService.SRV_CHN_X)) {
			Object input = call.getInputValue(NAMESPACE_ONT + SCalleeProvidedService.INP_CHN_X);
			if (input == null) {
				return ERROR_INPUT;
			}
			Activator.scaller.changeOnt((Resource) input);
			return new ServiceResponse(CallStatus.succeeded);
		}

		if (operation.equals(NAMESPACE_ONT + SCalleeProvidedService.SRV_REM_X)) {
			Object input = call.getInputValue(NAMESPACE_ONT + SCalleeProvidedService.INP_REM_X);
			if (input == null) {
				return ERROR_INPUT;
			}
			Activator.scaller.removeOnt((Resource) input);
			return new ServiceResponse(CallStatus.succeeded);
		}

		// :::::::::::::OTHER GETS:::::::::::::::::

		if (operation.equals(SCalleeProvidedService.SRV_GET_SERVS)) {
			ArrayList result = Activator.scaller.getAALServices();
			ServiceResponse response = new ServiceResponse(CallStatus.succeeded);
			response.addOutput(new ProcessOutput(SCalleeProvidedService.OUT_GET_SERVS, result));
			return response;
		}

		if (operation.equals(SCalleeProvidedService.SRV_GET_SPACES)) {
			ArrayList result = Activator.scaller.getAALSpaces();
			ServiceResponse response = new ServiceResponse(CallStatus.succeeded);
			response.addOutput(new ProcessOutput(SCalleeProvidedService.OUT_GET_SPACES, result));
			return response;
		}

		if (operation.equals(SCalleeProvidedService.SRV_GET_HR_OF_SERV)) {
			Object input = call.getInputValue(SCalleeProvidedService.INP_GET_HR_OF_SERV);
			if (input == null) {
				return ERROR_INPUT;
			}
			Resource result = Activator.scaller.getHROfAALService((Resource) input);
			ServiceResponse response = new ServiceResponse(CallStatus.succeeded);
			response.addOutput(new ProcessOutput(SCalleeProvidedService.OUT_GET_HR_OF_SERV, result));
			return response;
		}

		if (operation.equals(SCalleeProvidedService.SRV_GET_HW_OF_SERV)) {
			Object input = call.getInputValue(SCalleeProvidedService.INP_GET_HW_OF_SERV);
			if (input == null) {
				return ERROR_INPUT;
			}
			Resource result = Activator.scaller.getHWOfAALService((Resource) input);
			ServiceResponse response = new ServiceResponse(CallStatus.succeeded);
			response.addOutput(new ProcessOutput(SCalleeProvidedService.OUT_GET_HW_OF_SERV, result));
			return response;
		}

		if (operation.equals(SCalleeProvidedService.SRV_GET_APP_OF_SERV)) {
			Object input = call.getInputValue(SCalleeProvidedService.INP_GET_APP_OF_SERV);
			if (input == null) {
				return ERROR_INPUT;
			}
			Resource result = Activator.scaller.getAppOfAALService((Resource) input);
			ServiceResponse response = new ServiceResponse(CallStatus.succeeded);
			response.addOutput(new ProcessOutput(SCalleeProvidedService.OUT_GET_APP_OF_SERV, result));
			return response;
		}

		if (operation.equals(SCalleeProvidedService.SRV_GET_SERVS_OF_SPACE)) {
			Object input = call.getInputValue(SCalleeProvidedService.INP_GET_SERVS_OF_SPACE);
			if (input == null) {
				return ERROR_INPUT;
			}
			ArrayList result = Activator.scaller.getServicesOfSpace((Resource) input);
			ServiceResponse response = new ServiceResponse(CallStatus.succeeded);
			response.addOutput(new ProcessOutput(SCalleeProvidedService.OUT_GET_SERVS_OF_SPACE, result));
			return response;
		}

		if (operation.equals(SCalleeProvidedService.SRV_GET_DEVS_OF_SPACE)) {
			Object input = call.getInputValue(SCalleeProvidedService.INP_GET_DEVS_OF_SPACE);
			if (input == null) {
				return ERROR_INPUT;
			}
			ArrayList result = Activator.scaller.getDevicesOfSpace((Resource) input);
			ServiceResponse response = new ServiceResponse(CallStatus.succeeded);
			response.addOutput(new ProcessOutput(SCalleeProvidedService.OUT_GET_DEVS_OF_SPACE, result));
			return response;
		}

		if (operation.equals(SCalleeProvidedService.SRV_GET_ONTS_OF_SPACE)) {
			Object input = call.getInputValue(SCalleeProvidedService.INP_GET_ONTS_OF_SPACE);
			if (input == null) {
				return ERROR_INPUT;
			}
			ArrayList result = Activator.scaller.getOntsOfSpace((Resource) input);
			ServiceResponse response = new ServiceResponse(CallStatus.succeeded);
			response.addOutput(new ProcessOutput(SCalleeProvidedService.OUT_GET_ONTS_OF_SPACE, result));
			return response;
		}

		if (operation.equals(SCalleeProvidedService.SRV_GET_OWNERS_OF_SPACE)) {
			Object input = call.getInputValue(SCalleeProvidedService.INP_GET_OWNERS_OF_SPACE);
			if (input == null) {
				return ERROR_INPUT;
			}
			ArrayList result = Activator.scaller.getOwnsOfSpace((Resource) input);
			ServiceResponse response = new ServiceResponse(CallStatus.succeeded);
			response.addOutput(new ProcessOutput(SCalleeProvidedService.OUT_GET_OWNERS_OF_SPACE, result));
			return response;
		}

		if (operation.equals(SCalleeProvidedService.SRV_GET_OWNERS_OF_SERV)) {
			Object input = call.getInputValue(SCalleeProvidedService.INP_GET_OWNERS_OF_SERV);
			if (input == null) {
				return ERROR_INPUT;
			}
			ArrayList result = Activator.scaller.getOwnsOfServ((Resource) input);
			ServiceResponse response = new ServiceResponse(CallStatus.succeeded);
			response.addOutput(new ProcessOutput(SCalleeProvidedService.OUT_GET_OWNERS_OF_SERV, result));
			return response;
		}

		if (operation.equals(SCalleeProvidedService.SRV_GET_PROF_OF_SERV)) {
			Object input = call.getInputValue(SCalleeProvidedService.INP_GET_PROF_OF_SERV);
			if (input == null) {
				return ERROR_INPUT;
			}
			Resource result = Activator.scaller.getProfOfServ((Resource) input);
			ServiceResponse response = new ServiceResponse(CallStatus.succeeded);
			response.addOutput(
					new ProcessOutput(SCalleeProvidedService.OUT_GET_PROF_OF_SERV, (AALServiceProfile) result));
			return response;
		}

		if (operation.equals(SCalleeProvidedService.SRV_GET_PROF_OF_SPACE)) {
			Object input = call.getInputValue(SCalleeProvidedService.INP_GET_PROF_OF_SPACE);
			if (input == null) {
				return ERROR_INPUT;
			}
			Resource result = Activator.scaller.getProfOfSpace((Resource) input);
			ServiceResponse response = new ServiceResponse(CallStatus.succeeded);
			if (result != null)//
				response.addOutput(
						new ProcessOutput(SCalleeProvidedService.OUT_GET_PROF_OF_SPACE, (AALSpaceProfile) result));
			return response;
		}

		// :::::::::::::OTHER ADDS:::::::::::::::::

		if (operation.equals(SCalleeProvidedService.SRV_ADD_SERV_TO_SPACE)) {
			Object inWhere = call.getInputValue(SCalleeProvidedService.INP_ADD_SERV_TO_SPACE_WHERE);
			Object inWhat = call.getInputValue(SCalleeProvidedService.INP_ADD_SERV_TO_SPACE_WHAT);
			if (inWhere == null || inWhat == null) {
				return ERROR_INPUT;
			}
			Activator.scaller.addServiceToSpace((Resource) inWhere, (Resource) inWhat);
			return new ServiceResponse(CallStatus.succeeded);
		}

		if (operation.equals(SCalleeProvidedService.SRV_ADD_DEV_TO_SPACE)) {
			Object inWhere = call.getInputValue(SCalleeProvidedService.INP_ADD_DEV_TO_SPACE_WHERE);
			Object inWhat = call.getInputValue(SCalleeProvidedService.INP_ADD_DEV_TO_SPACE_WHAT);
			if (inWhere == null || inWhat == null) {
				return ERROR_INPUT;
			}
			Activator.scaller.addDeviceToSpace((Resource) inWhere, (Resource) inWhat);
			return new ServiceResponse(CallStatus.succeeded);
		}

		if (operation.equals(SCalleeProvidedService.SRV_ADD_ONT_TO_SPACE)) {
			Object inWhere = call.getInputValue(SCalleeProvidedService.INP_ADD_ONT_TO_SPACE_WHERE);
			Object inWhat = call.getInputValue(SCalleeProvidedService.INP_ADD_ONT_TO_SPACE_WHAT);
			if (inWhere == null || inWhat == null) {
				return ERROR_INPUT;
			}
			Activator.scaller.addOntToSpace((Resource) inWhere, (Resource) inWhat);
			return new ServiceResponse(CallStatus.succeeded);
		}

		if (operation.equals(SCalleeProvidedService.SRV_ADD_OWNER_TO_SPACE)) {
			Object inWhere = call.getInputValue(SCalleeProvidedService.INP_ADD_OWNER_TO_SPACE_WHERE);
			Object inWhat = call.getInputValue(SCalleeProvidedService.INP_ADD_OWNER_TO_SPACE_WHAT);
			if (inWhere == null || inWhat == null) {
				return ERROR_INPUT;
			}
			Activator.scaller.addOwnToSpace((Resource) inWhere, (Resource) inWhat);
			return new ServiceResponse(CallStatus.succeeded);
		}

		if (operation.equals(SCalleeProvidedService.SRV_ADD_OWNER_TO_SERV)) {
			Object inWhere = call.getInputValue(SCalleeProvidedService.INP_ADD_OWNER_TO_SERV_WHERE);
			Object inWhat = call.getInputValue(SCalleeProvidedService.INP_ADD_OWNER_TO_SERV_WHAT);
			if (inWhere == null || inWhat == null) {
				return ERROR_INPUT;
			}
			Activator.scaller.addOwnToServ((Resource) inWhere, (Resource) inWhat);
			return new ServiceResponse(CallStatus.succeeded);
		}

		if (operation.equals(SCalleeProvidedService.SRV_ADD_PROF_TO_SPACE)) {
			Object inWhere = call.getInputValue(SCalleeProvidedService.INP_ADD_PROF_TO_SPACE_WHERE);
			Object inWhat = call.getInputValue(SCalleeProvidedService.INP_ADD_PROF_TO_SPACE_WHAT);
			if (inWhere == null || inWhat == null) {
				return ERROR_INPUT;
			}
			Activator.scaller.addProfToProfilable((Resource) inWhere, (Resource) inWhat);
			return new ServiceResponse(CallStatus.succeeded);
		}

		if (operation.equals(SCalleeProvidedService.SRV_ADD_PROF_TO_SERV)) {
			Object inWhere = call.getInputValue(SCalleeProvidedService.INP_ADD_PROF_TO_SERV_WHERE);
			Object inWhat = call.getInputValue(SCalleeProvidedService.INP_ADD_PROF_TO_SERV_WHAT);
			if (inWhere == null || inWhat == null) {
				return ERROR_INPUT;
			}
			Activator.scaller.addProfToProfilable((Resource) inWhere, (Resource) inWhat);
			return new ServiceResponse(CallStatus.succeeded);
		}

		// :::::::::::::REMAINDER:::::::::::::::::

		if (operation.equals(NAMESPACE)) {
			ServiceResponse response = new ServiceResponse(CallStatus.serviceSpecificFailure);
			response.addOutput(
					new ProcessOutput(ServiceResponse.PROP_SERVICE_SPECIFIC_ERROR, "Service not implemented yet"));
			return response;
		}

		return new ServiceResponse(CallStatus.serviceSpecificFailure);
	}

}
