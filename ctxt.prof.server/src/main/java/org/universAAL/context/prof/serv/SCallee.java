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
import org.universAAL.ontology.profile.Profilable;
import org.universAAL.ontology.profile.Profile;
import org.universAAL.ontology.profile.SubProfile;
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
    private static final ServiceResponse ERROR_INPUT = new ServiceResponse(
	    CallStatus.serviceSpecificFailure);
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
	ERROR_INPUT.addOutput(new ProcessOutput(
		ServiceResponse.PROP_SERVICE_SPECIFIC_ERROR, "Invalid input"));
    }

    /**
     * Default constructor.
     * 
     * @param context
     *            uAAL module context
     */
    protected SCallee(ModuleContext context) {
	super(context, SCalleeProvidedService.getServiceProfiles(
		NAMESPACE_PROFILABLE, ProfilingService.MY_URI,
		new String[] { ProfilingService.PROP_CONTROLS },
		Profilable.MY_URI));
	this.addNewRegParams(SCalleeProvidedService.getServiceProfiles(
		NAMESPACE_PROFILE, ProfilingService.MY_URI, new String[] {
			ProfilingService.PROP_CONTROLS,
			Profilable.PROP_HAS_PROFILE }, Profile.MY_URI));
	this.addNewRegParams(SCalleeProvidedService.getServiceProfiles(
		NAMESPACE_SUBPROFILE, ProfilingService.MY_URI, new String[] {
			ProfilingService.PROP_CONTROLS,
			Profilable.PROP_HAS_PROFILE,
			Profile.PROP_HAS_SUB_PROFILE }, SubProfile.MY_URI));
	this.addNewRegParams(SCalleeProvidedService.profiles);
	ERROR_INPUT.addOutput(new ProcessOutput(
		ServiceResponse.PROP_SERVICE_SPECIFIC_ERROR, "Invalid input"));
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.universAAL.middleware.service.ServiceCallee#communicationChannelBroken
     * ()
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

	// TODO: All get/add/... seem to do the same (except operation URI). Do
	// something about it?
	// :::::::::::::PROFILABLE GET/ADD/CHANGE/REMOVE:::::::::::::::::
	// Because Users have double inheritance from PhThing and Profilable, we
	// cannot simply cast to Profilable (their Java class extends from
	// phThing). Instead they are casted to the smallest common type:
	// Resource. This is only for Java. When de/serialized from/to client
	// they are treated as RDF with all their properties, including type,
	// and in the client part they are parsed to the most specialized class,
	// which is supposed to be known by the client.
	if (operation.startsWith(NAMESPACE_PROFILABLE
		+ SCalleeProvidedService.SERVICE_GET)) {
	    LogUtils.logDebug(mc, SCallee.class, "handleCall",
		    new String[] { "CALLED: GET_PROFILABLE_DETAILS" }, null);
	    Object input = call.getInputValue(NAMESPACE_PROFILABLE
		    + SCalleeProvidedService.IN_GET);
	    if (input == null) {
		return ERROR_INPUT;
	    }
	    Resource result = Activator.scaller
		    .getProfilableDetails((Resource) input);
	    ServiceResponse response = new ServiceResponse(CallStatus.succeeded);
	    response.addOutput(new ProcessOutput(NAMESPACE_PROFILABLE
		    + SCalleeProvidedService.OUT_GET, result));
	    return response;
	}

	if (operation.startsWith(NAMESPACE_PROFILABLE
		+ SCalleeProvidedService.SERVICE_ADD)) {
	    LogUtils.logDebug(mc, SCallee.class, "handleCall",
		    new String[] { "CALLED: ADD_PROFILABLE" }, null);
	    Object input = call.getInputValue(NAMESPACE_PROFILABLE
		    + SCalleeProvidedService.IN_ADD);
	    if (input == null) {
		return ERROR_INPUT;
	    }
	    Activator.scaller.addProfilable((Resource) input);
	    ServiceResponse response = new ServiceResponse(CallStatus.succeeded);
	    return response;
	}

	if (operation.startsWith(NAMESPACE_PROFILABLE
		+ SCalleeProvidedService.SERVICE_CHANGE)) {
	    LogUtils.logDebug(mc, SCallee.class, "handleCall",
		    new String[] { "CALLED: CHANGE_PROFILABLE" }, null);
	    Object input = call.getInputValue(NAMESPACE_PROFILABLE
		    + SCalleeProvidedService.IN_CHANGE);
	    if (input == null) {
		return ERROR_INPUT;
	    }
	    Activator.scaller.changeProfilable((Resource) input);
	    ServiceResponse response = new ServiceResponse(CallStatus.succeeded);
	    return response;
	}

	if (operation.startsWith(NAMESPACE_PROFILABLE
		+ SCalleeProvidedService.SERVICE_REMOVE)) {
	    LogUtils.logDebug(mc, SCallee.class, "handleCall",
		    new String[] { "CALLED: REMOVE_PROFILABLE" }, null);
	    Object input = call.getInputValue(NAMESPACE_PROFILABLE
		    + SCalleeProvidedService.IN_REMOVE);
	    if (input == null) {
		return ERROR_INPUT;
	    }
	    Activator.scaller.removeProfilable((Resource) input);
	    ServiceResponse response = new ServiceResponse(CallStatus.succeeded);
	    return response;
	}

	// :::::::::::::PROFILE GET/ADD/CHANGE/REMOVE:::::::::::::::::

	if (operation.startsWith(NAMESPACE_PROFILE
		+ SCalleeProvidedService.SERVICE_GET)) {
	    LogUtils.logDebug(mc, SCallee.class, "handleCall",
		    new String[] { "CALLED: GET_PROFILE_DETAILS" }, null);
	    Object input = call.getInputValue(NAMESPACE_PROFILE
		    + SCalleeProvidedService.IN_GET);
	    if (input == null) {
		return ERROR_INPUT;
	    }
	    Resource result = Activator.scaller
		    .getProfileDetails((Resource) input);
	    ServiceResponse response = new ServiceResponse(CallStatus.succeeded);
	    response.addOutput(new ProcessOutput(NAMESPACE_PROFILE
		    + SCalleeProvidedService.OUT_GET, result));
	    return response;
	}

	if (operation.startsWith(NAMESPACE_PROFILE
		+ SCalleeProvidedService.SERVICE_ADD)) {
	    LogUtils.logDebug(mc, SCallee.class, "handleCall",
		    new String[] { "CALLED: ADD_PROFILE" }, null);
	    Object input = call.getInputValue(NAMESPACE_PROFILE
		    + SCalleeProvidedService.IN_ADD);
	    if (input == null) {
		return ERROR_INPUT;
	    }
	    Activator.scaller.addProfile((Resource) input);
	    ServiceResponse response = new ServiceResponse(CallStatus.succeeded);
	    return response;
	}

	if (operation.startsWith(NAMESPACE_PROFILE
		+ SCalleeProvidedService.SERVICE_CHANGE)) {
	    LogUtils.logDebug(mc, SCallee.class, "handleCall",
		    new String[] { "CALLED: CHANGE_PROFILE" }, null);
	    Object input = call.getInputValue(NAMESPACE_PROFILE
		    + SCalleeProvidedService.IN_CHANGE);
	    if (input == null) {
		return ERROR_INPUT;
	    }
	    Activator.scaller.changeProfile((Resource) input);
	    ServiceResponse response = new ServiceResponse(CallStatus.succeeded);
	    return response;
	}

	if (operation.startsWith(NAMESPACE_PROFILE
		+ SCalleeProvidedService.SERVICE_REMOVE)) {
	    LogUtils.logDebug(mc, SCallee.class, "handleCall",
		    new String[] { "CALLED: REMOVE_PROFILE" }, null);
	    Object input = call.getInputValue(NAMESPACE_PROFILE
		    + SCalleeProvidedService.IN_REMOVE);
	    if (input == null) {
		return ERROR_INPUT;
	    }
	    Activator.scaller.removeProfile((Resource) input);
	    ServiceResponse response = new ServiceResponse(CallStatus.succeeded);
	    return response;
	}

	// :::::::::::::SUBPROFILE GET/ADD/CHANGE/REMOVE:::::::::::::::::

	if (operation.startsWith(NAMESPACE_SUBPROFILE
		+ SCalleeProvidedService.SERVICE_GET)) {
	    LogUtils.logDebug(mc, SCallee.class, "handleCall",
		    new String[] { "CALLED: GET_SUBPROFILE_DETAILS" }, null);
	    Object input = call.getInputValue(NAMESPACE_SUBPROFILE
		    + SCalleeProvidedService.IN_GET);
	    if (input == null) {
		return ERROR_INPUT;
	    }
	    Resource result = Activator.scaller
		    .getSubProfileDetails((Resource) input);
	    ServiceResponse response = new ServiceResponse(CallStatus.succeeded);
	    response.addOutput(new ProcessOutput(NAMESPACE_SUBPROFILE
		    + SCalleeProvidedService.OUT_GET, result));
	    return response;
	}

	if (operation.startsWith(NAMESPACE_SUBPROFILE
		+ SCalleeProvidedService.SERVICE_ADD)) {
	    LogUtils.logDebug(mc, SCallee.class, "handleCall",
		    new String[] { "CALLED: ADD_SUBPROFILE" }, null);
	    Object input = call.getInputValue(NAMESPACE_SUBPROFILE
		    + SCalleeProvidedService.IN_ADD);
	    if (input == null) {
		return ERROR_INPUT;
	    }
	    Activator.scaller.addSubProfile((Resource) input);
	    ServiceResponse response = new ServiceResponse(CallStatus.succeeded);
	    return response;
	}

	if (operation.startsWith(NAMESPACE_SUBPROFILE
		+ SCalleeProvidedService.SERVICE_CHANGE)) {
	    LogUtils.logDebug(mc, SCallee.class, "handleCall",
		    new String[] { "CALLED: CHANGE_SUBPROFILE" }, null);
	    Object input = call.getInputValue(NAMESPACE_SUBPROFILE
		    + SCalleeProvidedService.IN_CHANGE);
	    if (input == null) {
		return ERROR_INPUT;
	    }
	    Activator.scaller.changeSubProfile((Resource) input);
	    ServiceResponse response = new ServiceResponse(CallStatus.succeeded);
	    return response;
	}

	if (operation.startsWith(NAMESPACE_SUBPROFILE
		+ SCalleeProvidedService.SERVICE_REMOVE)) {
	    LogUtils.logDebug(mc, SCallee.class, "handleCall",
		    new String[] { "CALLED: REMOVE_SUBPROFILE" }, null);
	    Object input = call.getInputValue(NAMESPACE_SUBPROFILE
		    + SCalleeProvidedService.IN_REMOVE);
	    if (input == null) {
		return ERROR_INPUT;
	    }
	    Activator.scaller.removeSubProfile((Resource) input);
	    ServiceResponse response = new ServiceResponse(CallStatus.succeeded);
	    return response;
	}

	// :::::::::::::OTHERS:::::::::::::::::

	// if (operation.startsWith(SCalleeProvidedService.GET_PROFILE)) {
	// LogUtils.logDebug(mc, SCallee.class, "handleCall", new
	// String[]{"CALLED: GET_PROFILE"}, null);
	// Object input =
	// call.getInputValue(SCalleeProvidedService.INPUT_GET_PROFILE);
	// if (input == null) {
	// return errorInput;
	// }
	// Resource result=Activator.scaller.getProfile((Resource) input);
	// ServiceResponse response = new ServiceResponse(CallStatus.succeeded);
	// response.addOutput(new
	// ProcessOutput(SCalleeProvidedService.OUT_GET_PROFILE,result));
	// return response;
	// }

	if (operation.startsWith(SCalleeProvidedService.ADD_PROFILE)) {
	    LogUtils.logDebug(mc, SCallee.class, "handleCall",
		    new String[] { "CALLED: ADD_PROFILE" }, null);
	    Object input = call
		    .getInputValue(SCalleeProvidedService.INPUT_ADD_PROFILE);
	    Object input2 = call
		    .getInputValue(SCalleeProvidedService.INPUT2_ADD_PROFILE);
	    if (input == null || input2 == null) {
		return ERROR_INPUT;
	    }
	    Activator.scaller.addProfile((Resource) input, (Resource) input2);
	    ServiceResponse response = new ServiceResponse(CallStatus.succeeded);
	    return response;
	}

	if (operation.startsWith(SCalleeProvidedService.CHANGE_PROFILE)) {
	    LogUtils.logDebug(mc, SCallee.class, "handleCall",
		    new String[] { "CALLED: CHANGE_PROFILE" }, null);
	    Object input = call
		    .getInputValue(SCalleeProvidedService.INPUT_CHANGE_PROFILE);
	    Object input2 = call
		    .getInputValue(SCalleeProvidedService.INPUT2_CHANGE_PROFILE);
	    if (input == null || input2 == null) {
		return ERROR_INPUT;
	    }
	    Activator.scaller
		    .changeProfile((Resource) input, (Resource) input2);
	    ServiceResponse response = new ServiceResponse(CallStatus.succeeded);
	    return response;
	}

	if (operation.startsWith(SCalleeProvidedService.GET_USERS)) {
	    LogUtils.logDebug(mc, SCallee.class, "handleCall",
		    new String[] { "CALLED: GET_USERS" }, null);
	    ArrayList result = Activator.scaller.getUsers();
	    ServiceResponse response = new ServiceResponse(CallStatus.succeeded);
	    response.addOutput(new ProcessOutput(
		    SCalleeProvidedService.OUTPUT_GET_USERS, result));
	    return response;
	}

	if (operation.startsWith(SCalleeProvidedService.GET_SUBPROFILES)) {
	    LogUtils.logDebug(mc, SCallee.class, "handleCall",
		    new String[] { "CALLED: GET_SUBPROFILES" }, null);
	    Object input = call
		    .getInputValue(SCalleeProvidedService.INPUT_GET_SUBPROFILES);
	    if (input == null) {
		return ERROR_INPUT;
	    }
	    Resource[] result = Activator.scaller
		    .getSubprofiles((Resource) input);
	    ServiceResponse response = new ServiceResponse(CallStatus.succeeded);
	    response.addOutput(new ProcessOutput(
		    SCalleeProvidedService.OUT_GET_SUBPROFILES, result));
	    return response;
	}

	if (operation.startsWith(NAMESPACE)) {
	    ServiceResponse response = new ServiceResponse(
		    CallStatus.serviceSpecificFailure);
	    response.addOutput(new ProcessOutput(
		    ServiceResponse.PROP_SERVICE_SPECIFIC_ERROR,
		    "Service not implemented yet"));
	    return response;
	}

	return new ServiceResponse(CallStatus.serviceSpecificFailure);
    }

}
