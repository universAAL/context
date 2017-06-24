/*
	Copyright 2012-2016 ITACA-TSB, http://www.tsb.upv.es
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

import java.util.ArrayList;

import org.universAAL.context.prof.serv.SCaller.CHEDeniedException;
import org.universAAL.context.prof.serv.SCaller.CHEErrorException;
import org.universAAL.context.prof.serv.SCaller.CHENotFoundException;
import org.universAAL.context.prof.serv.SCaller.CHETimeOutException;
import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.container.utils.LogUtils;
import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.service.CallStatus;
import org.universAAL.middleware.service.ServiceCall;
import org.universAAL.middleware.service.ServiceCallee;
import org.universAAL.middleware.service.ServiceResponse;
import org.universAAL.middleware.service.owls.process.ProcessOutput;
import org.universAAL.middleware.service.owls.profile.ServiceProfile;
import org.universAAL.ontology.profile.Profilable;
import org.universAAL.ontology.profile.Profile;
import org.universAAL.ontology.profile.SubProfile;
import org.universAAL.ontology.profile.User;
import org.universAAL.ontology.profile.UserProfile;
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
	private static final ServiceResponse ERROR_EMPTY = new ServiceResponse(CallStatus.serviceSpecificFailure);
	/**
	 * Default namespace root.
	 */
	protected static final String NAMESPACE = "http://ontology.universAAL.org/ProfilingServer.owl#";
	/**
	 * Namespace for Profilable services.
	 */
	private static final String NAMESPACE_PROFILABLE = NAMESPACE + "profilable";
	/**
	 * Namespace for Profile services.
	 */
	private static final String NAMESPACE_PROFILE = NAMESPACE + "profile";
	/**
	 * Namespace for subprofile services.
	 */
	private static final String NAMESPACE_SUBPROFILE = NAMESPACE + "subprofile";
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
		ERROR_EMPTY.addOutput(
				new ProcessOutput(ServiceResponse.PROP_SERVICE_SPECIFIC_ERROR, "Result from CHE is empty or invalid"));
	}

	/**
	 * Default constructor.
	 *
	 * @param context
	 *            uAAL module context
	 */
	public SCallee(ModuleContext context) {
		super(context, SCalleeProvidedService.profiles);
		this.addNewServiceProfiles(SCalleeProvidedService.getServiceProfiles(NAMESPACE_PROFILABLE,
				ProfilingService.MY_URI, new String[] { ProfilingService.PROP_CONTROLS }, User.MY_URI));
		this.addNewServiceProfiles(SCalleeProvidedService.getServiceProfiles(NAMESPACE_PROFILE, ProfilingService.MY_URI,
				new String[] { ProfilingService.PROP_CONTROLS, Profilable.PROP_HAS_PROFILE }, UserProfile.MY_URI));
		this.addNewServiceProfiles(
				SCalleeProvidedService
						.getServiceProfiles(NAMESPACE_SUBPROFILE,
								ProfilingService.MY_URI, new String[] { ProfilingService.PROP_CONTROLS,
										Profilable.PROP_HAS_PROFILE, Profile.PROP_HAS_SUB_PROFILE },
								SubProfile.MY_URI));
		// this.addNewServiceProfiles(SCalleeProvidedService.profiles);
		ERROR_INPUT.addOutput(new ProcessOutput(ServiceResponse.PROP_SERVICE_SPECIFIC_ERROR, "Invalid input"));
		ERROR_EMPTY.addOutput(
				new ProcessOutput(ServiceResponse.PROP_SERVICE_SPECIFIC_ERROR, "Result from CHE is empty or invalid"));
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

		try {
			// TODO: All get/add/... seem to do the same (except operation URI).
			// Do something about it?
			// :::::::::::::PROFILABLE GET/ADD/CHANGE/REMOVE:::::::::::::::::
			// Because Users have double inheritance from PhThing and
			// Profilable, we cannot simply cast to Profilable (their Java
			// class extends from phThing). Instead they are casted to the
			// smallest common type: Resource. This is only for Java. When
			// de/serialized from/to client they are treated as RDF with all
			// their properties, including type, and in the client part they
			// are parsed to the most specialized class, which is supposed to
			// be known by the client.
			if (operation.startsWith(NAMESPACE_PROFILABLE + SCalleeProvidedService.SRV_GET_X)) {
				LogUtils.logDebug(mc, SCallee.class, "handleCall", new String[] { "CALLED: GET_PROFILABLE_DETAILS" },
						null);
				Object input = call.getInputValue(NAMESPACE_PROFILABLE + SCalleeProvidedService.INP_GET_X);
				if (input == null) {
					return ERROR_INPUT;
				}
				Resource result = Hub.scaller.getUser((Resource) input);
				ServiceResponse response = new ServiceResponse(CallStatus.succeeded);
				if (result != null) {
					response.addOutput(
							new ProcessOutput(NAMESPACE_PROFILABLE + SCalleeProvidedService.OUT_GET_X, result));
				} else {
					return ERROR_EMPTY;
				}
				return response;
			}

			if (operation.startsWith(NAMESPACE_PROFILABLE + SCalleeProvidedService.SRV_ADD_X)) {
				LogUtils.logDebug(mc, SCallee.class, "handleCall", new String[] { "CALLED: ADD_PROFILABLE" }, null);
				Object input = call.getInputValue(NAMESPACE_PROFILABLE + SCalleeProvidedService.INP_ADD_X);
				if (input == null) {
					return ERROR_INPUT;
				}
				Hub.scaller.addUser((Resource) input);
				ServiceResponse response = new ServiceResponse(CallStatus.succeeded);
				return response;
			}

			if (operation.startsWith(NAMESPACE_PROFILABLE + SCalleeProvidedService.SRV_CHN_X)) {
				LogUtils.logDebug(mc, SCallee.class, "handleCall", new String[] { "CALLED: CHANGE_PROFILABLE" }, null);
				Object input = call.getInputValue(NAMESPACE_PROFILABLE + SCalleeProvidedService.INP_CHN_X);
				if (input == null) {
					return ERROR_INPUT;
				}
				Hub.scaller.changeUser((Resource) input);
				ServiceResponse response = new ServiceResponse(CallStatus.succeeded);
				return response;
			}

			if (operation.startsWith(NAMESPACE_PROFILABLE + SCalleeProvidedService.SRV_REM_X)) {
				LogUtils.logDebug(mc, SCallee.class, "handleCall", new String[] { "CALLED: REMOVE_PROFILABLE" }, null);
				Object input = call.getInputValue(NAMESPACE_PROFILABLE + SCalleeProvidedService.INP_REM_X);
				if (input == null) {
					return ERROR_INPUT;
				}
				Hub.scaller.removeUser((Resource) input);
				ServiceResponse response = new ServiceResponse(CallStatus.succeeded);
				return response;
			}

			// :::::::::::::PROFILE GET/ADD/CHANGE/REMOVE:::::::::::::::::

			if (operation.startsWith(NAMESPACE_PROFILE + SCalleeProvidedService.SRV_GET_X)) {
				LogUtils.logDebug(mc, SCallee.class, "handleCall", new String[] { "CALLED: GET_PROFILE_DETAILS" },
						null);
				Object input = call.getInputValue(NAMESPACE_PROFILE + SCalleeProvidedService.INP_GET_X);
				if (input == null) {
					return ERROR_INPUT;
				}
				Resource result = Hub.scaller.getProfile((Resource) input);
				ServiceResponse response = new ServiceResponse(CallStatus.succeeded);
				if (result != null) {
					response.addOutput(new ProcessOutput(NAMESPACE_PROFILE + SCalleeProvidedService.OUT_GET_X, result));
				} else {
					return ERROR_EMPTY;
				}
				return response;
			}

			if (operation.startsWith(NAMESPACE_PROFILE + SCalleeProvidedService.SRV_ADD_X)) {
				LogUtils.logDebug(mc, SCallee.class, "handleCall", new String[] { "CALLED: ADD_PROFILE" }, null);
				Object input = call.getInputValue(NAMESPACE_PROFILE + SCalleeProvidedService.INP_ADD_X);
				if (input == null) {
					return ERROR_INPUT;
				}
				Hub.scaller.addProfile((Resource) input);
				ServiceResponse response = new ServiceResponse(CallStatus.succeeded);
				return response;
			}

			if (operation.startsWith(NAMESPACE_PROFILE + SCalleeProvidedService.SRV_CHN_X)) {
				LogUtils.logDebug(mc, SCallee.class, "handleCall", new String[] { "CALLED: CHANGE_PROFILE" }, null);
				Object input = call.getInputValue(NAMESPACE_PROFILE + SCalleeProvidedService.INP_CHN_X);
				if (input == null) {
					return ERROR_INPUT;
				}
				Hub.scaller.changeProfile((Resource) input);
				ServiceResponse response = new ServiceResponse(CallStatus.succeeded);
				return response;
			}

			if (operation.startsWith(NAMESPACE_PROFILE + SCalleeProvidedService.SRV_REM_X)) {
				LogUtils.logDebug(mc, SCallee.class, "handleCall", new String[] { "CALLED: REMOVE_PROFILE" }, null);
				Object input = call.getInputValue(NAMESPACE_PROFILE + SCalleeProvidedService.INP_REM_X);
				if (input == null) {
					return ERROR_INPUT;
				}
				Hub.scaller.removeProfile((Resource) input);
				ServiceResponse response = new ServiceResponse(CallStatus.succeeded);
				return response;
			}

			// :::::::::::::SUBPROFILE GET/ADD/CHANGE/REMOVE:::::::::::::::::

			if (operation.startsWith(NAMESPACE_SUBPROFILE + SCalleeProvidedService.SRV_GET_X)) {
				LogUtils.logDebug(mc, SCallee.class, "handleCall", new String[] { "CALLED: GET_SUBPROFILE_DETAILS" },
						null);
				Object input = call.getInputValue(NAMESPACE_SUBPROFILE + SCalleeProvidedService.INP_GET_X);
				if (input == null) {
					return ERROR_INPUT;
				}
				Resource result = Hub.scaller.getSubProfile((Resource) input);
				ServiceResponse response = new ServiceResponse(CallStatus.succeeded);
				if (result != null) {
					response.addOutput(
							new ProcessOutput(NAMESPACE_SUBPROFILE + SCalleeProvidedService.OUT_GET_X, result));
				} else {
					return ERROR_EMPTY;
				}
				return response;
			}

			if (operation.startsWith(NAMESPACE_SUBPROFILE + SCalleeProvidedService.SRV_ADD_X)) {
				LogUtils.logDebug(mc, SCallee.class, "handleCall", new String[] { "CALLED: ADD_SUBPROFILE" }, null);
				Object input = call.getInputValue(NAMESPACE_SUBPROFILE + SCalleeProvidedService.INP_ADD_X);
				if (input == null) {
					return ERROR_INPUT;
				}
				Hub.scaller.addSubProfile((Resource) input);
				ServiceResponse response = new ServiceResponse(CallStatus.succeeded);
				return response;
			}

			if (operation.startsWith(NAMESPACE_SUBPROFILE + SCalleeProvidedService.SRV_CHN_X)) {
				LogUtils.logDebug(mc, SCallee.class, "handleCall", new String[] { "CALLED: CHANGE_SUBPROFILE" }, null);
				Object input = call.getInputValue(NAMESPACE_SUBPROFILE + SCalleeProvidedService.INP_CHN_X);
				if (input == null) {
					return ERROR_INPUT;
				}
				Hub.scaller.changeSubProfile((Resource) input);
				ServiceResponse response = new ServiceResponse(CallStatus.succeeded);
				return response;
			}

			if (operation.startsWith(NAMESPACE_SUBPROFILE + SCalleeProvidedService.SRV_REM_X)) {
				LogUtils.logDebug(mc, SCallee.class, "handleCall", new String[] { "CALLED: REMOVE_SUBPROFILE" }, null);
				Object input = call.getInputValue(NAMESPACE_SUBPROFILE + SCalleeProvidedService.INP_REM_X);
				if (input == null) {
					return ERROR_INPUT;
				}
				Hub.scaller.removeSubProfile((Resource) input);
				ServiceResponse response = new ServiceResponse(CallStatus.succeeded);
				return response;
			}

			// :::::::::::::OTHER GETS:::::::::::::::::

			if (operation.startsWith(SCalleeProvidedService.SRV_GET_USRS)) {
				LogUtils.logDebug(mc, SCallee.class, "handleCall", new String[] { "CALLED: SRV_GET_USRS" }, null);
				ArrayList result = Hub.scaller.getUsers();
				ServiceResponse response = new ServiceResponse(CallStatus.succeeded);
				if (result != null) {
					response.addOutput(new ProcessOutput(SCalleeProvidedService.OUT_GET_USRS, result));
				} else {
					return ERROR_EMPTY;
				}
				return response;
			}

			if (operation.startsWith(SCalleeProvidedService.SRV_GET_PRF_OF_USR)) {
				LogUtils.logDebug(mc, SCallee.class, "handleCall", new String[] { "CALLED: SRV_GET_PRF_OF_USR" }, null);
				Object input = call.getInputValue(SCalleeProvidedService.INP_GET_PRF_OF_USR);
				if (input == null) {
					return ERROR_INPUT;
				}
				Resource result = Hub.scaller.getProfileOfUser((Resource) input);
				ServiceResponse response = new ServiceResponse(CallStatus.succeeded);
				if (result != null) {
					response.addOutput(new ProcessOutput(SCalleeProvidedService.OUT_GET_PRF_OF_USR, result));
				} else {
					return ERROR_EMPTY;
				}
				return response;
			}

			// if
			// (operation.startsWith(SCalleeProvidedService.SRV_GET_SECPRF_OF_USR))
			// {
			// LogUtils.logDebug(mc, SCallee.class, "handleCall",
			// new String[] { "CALLED: SRV_GET_SECPRF_OF_USR" }, null);
			// Object input = call
			// .getInputValue(SCalleeProvidedService.INP_GET_SECPRF_OF_USR);
			// if (input == null) {
			// return ERROR_INPUT;
			// }
			// Resource result = Hub.scaller.getSecProfileOfUser((Resource)
			// input);
			// ServiceResponse response = new
			// ServiceResponse(CallStatus.succeeded);
			// if (result != null) {
			// response.addOutput(new ProcessOutput(
			// SCalleeProvidedService.OUT_GET_SECPRF_OF_USR, result));
			// } else {
			// return ERROR_EMPTY;
			// }
			// return response;
			// }

			if (operation.startsWith(SCalleeProvidedService.SRV_GET_SUBS_OF_USR)) {
				LogUtils.logDebug(mc, SCallee.class, "handleCall", new String[] { "CALLED: SRV_GET_SUBS_OF_USR" },
						null);
				Object input = call.getInputValue(SCalleeProvidedService.INP_GET_SUBS_OF_USR);
				if (input == null) {
					return ERROR_INPUT;
				}
				ArrayList result = Hub.scaller.getSubProfilesOfUser((Resource) input);
				ServiceResponse response = new ServiceResponse(CallStatus.succeeded);
				if (result != null) {
					response.addOutput(new ProcessOutput(SCalleeProvidedService.OUT_GET_SUBS_OF_USR, result));
				} else {
					return ERROR_EMPTY;
				}
				return response;
			}

			if (operation.startsWith(SCalleeProvidedService.SRV_GET_SUB_OF_USR)) {
				LogUtils.logDebug(mc, SCallee.class, "handleCall", new String[] { "CALLED: SRV_GET_SUB_OF_USR" }, null);
				Object input = call.getInputValue(SCalleeProvidedService.INP_GET_SUB_OF_USR);
				if (input == null) {
					return ERROR_INPUT;
				}
				// Resource result = Hub.scaller
				// .getSubProfileOfUser((Resource) input);
				// ServiceResponse response = new
				// ServiceResponse(CallStatus.succeeded);
				// if (result != null) {
				// response.addOutput(new ProcessOutput(
				// SCalleeProvidedService.OUT_GET_SUB_OF_USR, result));
				// } else {
				return ERROR_EMPTY;
				// }
				// return response;
			}

			if (operation.startsWith(SCalleeProvidedService.SRV_GET_SUBS_OF_PRF)) {
				LogUtils.logDebug(mc, SCallee.class, "handleCall", new String[] { "CALLED: SRV_GET_SUBS_OF_PRF" },
						null);
				Object input = call.getInputValue(SCalleeProvidedService.INP_GET_SUBS_OF_PRF);
				if (input == null) {
					return ERROR_INPUT;
				}
				ArrayList result = Hub.scaller.getSubProfilesOfProfile((Resource) input);
				ServiceResponse response = new ServiceResponse(CallStatus.succeeded);
				if (result != null) {
					response.addOutput(new ProcessOutput(SCalleeProvidedService.OUT_GET_SUBS_OF_PRF, result));
				} else {
					return ERROR_EMPTY;
				}
				return response;
			}

			// :::::::::::::OTHER ADDS:::::::::::::::::

			if (operation.startsWith(SCalleeProvidedService.SRV_ADD_PRF_TO_USR)) {
				LogUtils.logDebug(mc, SCallee.class, "handleCall", new String[] { "CALLED: SRV_ADD_PRF_TO_USR" }, null);
				Object inWhere = call.getInputValue(SCalleeProvidedService.IN_ADD_PRF_TO_USR_WHERE);
				Object inWhat = call.getInputValue(SCalleeProvidedService.IN_ADD_PRF_TO_USR_WHAT);
				if (inWhere == null || inWhat == null) {
					return ERROR_INPUT;
				}
				Hub.scaller.addProfileToUser((Resource) inWhere, (Resource) inWhat);
				ServiceResponse response = new ServiceResponse(CallStatus.succeeded);
				return response;
			}

			if (operation.startsWith(SCalleeProvidedService.SRV_ADD_SUB_TO_USR)) {
				LogUtils.logDebug(mc, SCallee.class, "handleCall", new String[] { "CALLED: SRV_ADD_SUB_TO_USR" }, null);
				Object inWhere = call.getInputValue(SCalleeProvidedService.IN_ADD_SUB_TO_USR_WHERE);
				Object inWhat = call.getInputValue(SCalleeProvidedService.IN_ADD_SUB_TO_USR_WHAT);
				if (inWhere == null || inWhat == null) {
					return ERROR_INPUT;
				}
				Hub.scaller.addSubProfileToUser((Resource) inWhere, (Resource) inWhat);
				ServiceResponse response = new ServiceResponse(CallStatus.succeeded);
				return response;
			}

			if (operation.startsWith(SCalleeProvidedService.SRV_ADD_SUB_TO_PRF)) {
				LogUtils.logDebug(mc, SCallee.class, "handleCall", new String[] { "CALLED: SRV_ADD_SUB_TO_PRF" }, null);
				Object inWhere = call.getInputValue(SCalleeProvidedService.IN_ADD_SUB_TO_PRF_WHERE);
				Object inWhat = call.getInputValue(SCalleeProvidedService.IN_ADD_SUB_TO_PRF_WHAT);
				if (inWhere == null || inWhat == null) {
					return ERROR_INPUT;
				}
				Hub.scaller.addSubProfileToProf((Resource) inWhere, (Resource) inWhat);
				ServiceResponse response = new ServiceResponse(CallStatus.succeeded);
				return response;
			}

			if (operation.startsWith(NAMESPACE)) {
				ServiceResponse response = new ServiceResponse(CallStatus.serviceSpecificFailure);
				response.addOutput(
						new ProcessOutput(ServiceResponse.PROP_SERVICE_SPECIFIC_ERROR, "Service not implemented yet"));
				return response;
			}

		} catch (CHENotFoundException e) {
			ServiceResponse fail = new ServiceResponse(CallStatus.serviceSpecificFailure);
			fail.addOutput(new ProcessOutput(ServiceResponse.PROP_SERVICE_SPECIFIC_ERROR,
					"Internal call to CHE service was not resolved: noMatchingServiceFound"));
			return fail;
		} catch (CHETimeOutException e) {
			ServiceResponse fail = new ServiceResponse(CallStatus.serviceSpecificFailure);
			fail.addOutput(new ProcessOutput(ServiceResponse.PROP_SERVICE_SPECIFIC_ERROR,
					"Internal call to CHE service timed out: responseTimedOut"));
			return fail;
		} catch (CHEErrorException e) {
			ServiceResponse fail = new ServiceResponse(CallStatus.serviceSpecificFailure);
			fail.addOutput(new ProcessOutput(ServiceResponse.PROP_SERVICE_SPECIFIC_ERROR,
					"Internal call to CHE got an error: serviceSpecificFailure"));
			return fail;
		} catch (CHEDeniedException e) {
			ServiceResponse fail = new ServiceResponse(CallStatus.serviceSpecificFailure);
			fail.addOutput(new ProcessOutput(ServiceResponse.PROP_SERVICE_SPECIFIC_ERROR,
					"Internal call to CHE was denied: denied"));
			return fail;
		}
		return new ServiceResponse(CallStatus.serviceSpecificFailure);
	}

}
