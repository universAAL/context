/*
	Copyright 2008-2014 TSB, http://www.tsbtecnologias.es
	TSB - Tecnologías para la Salud y el Bienestar
	
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
package org.universAAL.drools;

import org.osgi.framework.BundleContext;
import org.universAAL.drools.engine.RulesEngine;
import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.container.osgi.uAALBundleContainer;
import org.universAAL.middleware.container.utils.LogUtils;
import org.universAAL.middleware.service.CallStatus;
import org.universAAL.middleware.service.ServiceCall;
import org.universAAL.middleware.service.ServiceCallee;
import org.universAAL.middleware.service.ServiceResponse;
import org.universAAL.ontology.drools.Rule;

/**
 * Provider to for the services of the rules engine.
 * 
 * @author Miguel Llorente(mllorente)
 */
public class DroolsReasonerProvider extends ServiceCallee {
	private ModuleContext mctx;
	private BundleContext bctx;

	protected DroolsReasonerProvider(ModuleContext context) {
		super(context, ProvidedDroolsReasonerService.profiles);
		mctx = context;
	}

	protected DroolsReasonerProvider(BundleContext context) {
		super(uAALBundleContainer.THE_CONTAINER
				.registerModule(new Object[] { context }),
				ProvidedDroolsReasonerService.profiles);
		bctx = context;
	}

	/**
	 * Whe
	 */
	@Override
	public void communicationChannelBroken() {
		// TODO Auto-generated method stub

	}

	@Override
	public ServiceResponse handleCall(ServiceCall call) {
		LogUtils.logDebug(mctx, getClass(), "ServiceProvided",
				new String[] { "Handling service call..." }, null);
		if (call == null) {
			return null;
		} else {
			String operation = call.getProcessURI();
			if (operation == null) {
				return null;
			} else if (operation
					.startsWith(ProvidedDroolsReasonerService.SERVICE_ADD_RULE)) {
				LogUtils.logTrace(mctx, getClass(), "ServiceProvided",
						new String[] { "ADDING A RULE..." }, null);
				Object input = call
						.getInputValue(ProvidedDroolsReasonerService.INPUT_RULE);// URI
				// DEL
				// INPUT
				// DEL
				// PROFILE
				LogUtils.logTrace(mctx, getClass(), "ServiceProvided",
						new String[] { "The body is:"
								+ ((Rule) input).getBody() }, null);
				RulesEngine.getInstance(bctx).insertRule(
						((Rule) input).getBody());
				return new ServiceResponse(CallStatus.succeeded);
			} else if (operation
					.startsWith(ProvidedDroolsReasonerService.SERVICE_REMOVE_RULE)) {
				LogUtils.logTrace(mctx, getClass(), "ServiceProvided",
						new String[] { "REMOVING A RULE..." }, null);
				return new ServiceResponse(CallStatus.succeeded);
			} else if (operation
					.startsWith(ProvidedDroolsReasonerService.SERVICE_MODIFY_RULE)) {
				LogUtils.logTrace(mctx, getClass(), "ServiceProvided",
						new String[] { "MODIFYING RULE..." }, null);
			} else if (operation
					.startsWith(ProvidedDroolsReasonerService.SERVICE_ADD_FACT)) {
				LogUtils.logTrace(mctx, getClass(), "ServiceProvided",
						new String[] { "ADDING FACT..." }, null);
			} else if (operation
					.startsWith(ProvidedDroolsReasonerService.SERVICE_MODIFY_FACT)) {
				LogUtils.logTrace(mctx, getClass(), "ServiceProvided",
						new String[] { "MODIFYING FACT..." }, null);
			} else if (operation
					.startsWith(ProvidedDroolsReasonerService.SERVICE_REMOVE_FACT)) {
				LogUtils.logTrace(mctx, getClass(), "ServiceProvided",
						new String[] { "REMOVING FACT..." }, null);
			} else if (operation
					.startsWith(ProvidedDroolsReasonerService.SERVICE_SWITCH_ON)) {
				LogUtils.logTrace(mctx, getClass(), "ServiceProvided",
						new String[] { "SWITCHING ON..." }, null);
			} else if (operation
					.startsWith(ProvidedDroolsReasonerService.SERVICE_SWITCH_OFF)) {
				LogUtils.logTrace(mctx, getClass(), "ServiceProvided",
						new String[] { "SWITCHING OFF..." }, null);
			}
		}
		return new ServiceResponse(CallStatus.serviceSpecificFailure);
	}
}
