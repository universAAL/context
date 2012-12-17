/*	
	Copyright 2008-2014 Fraunhofer IGD, http://www.igd.fraunhofer.de
	Fraunhofer Gesellschaft - Institut für Graphische Datenverarbeitung 
	
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
package org.universAAL.reasoner.server;

import java.util.ArrayList;

import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.container.osgi.util.BundleConfigHome;
import org.universAAL.middleware.service.CallStatus;
import org.universAAL.middleware.service.ServiceCall;
import org.universAAL.middleware.service.ServiceCallee;
import org.universAAL.middleware.service.ServiceResponse;
import org.universAAL.middleware.service.owls.process.ProcessOutput;
import org.universAAL.reasoner.ont.ElementModel;
import org.universAAL.reasoner.ont.Persistent;
import org.universAAL.reasoner.ont.Query;
import org.universAAL.reasoner.ont.Situation;
import org.universAAL.reasoner.server.osgi.Activator;

/**
 * The main task of the ReasoningProvider is the handle Situations, Queries and
 * Rules (please have a look also at the description in the Activator). The
 * according objects are handled in a class called "ElementModel" that is able
 * the manage objects of the kind like given by the generic attribute. To manage
 * does mean to add/remove/get objects and also save/load them at the
 * file-system.
 * 
 * @author amarinc
 * 
 */
public class ReasoningProvider extends ServiceCallee {

	private static final ServiceResponse invalidInput = new ServiceResponse(
			CallStatus.serviceSpecificFailure);

	static {
		invalidInput.addOutput(new ProcessOutput(
				ServiceResponse.PROP_SERVICE_SPECIFIC_ERROR, "Invalid input!"));
	}

	private static final String bundleHomePath = new BundleConfigHome(
			"ctxt.situation.reasoner").getAbsolutePath();

	private final ElementModel<Situation> situations = new ElementModel<Situation>(
			Situation.class, Activator.serializer, bundleHomePath);
	private final ElementModel<Query> queries = new ElementModel<Query>(
			Query.class, Activator.serializer, bundleHomePath);
	private final RuleModel rules = new RuleModel(bundleHomePath);

	public ReasoningProvider(ModuleContext context) {
		super(context, ProvidedReasoningService.profiles);
		situations.loadElements();
		queries.loadElements();
	}

	@Override
	public void communicationChannelBroken() {

	}

	public void saveAllData() {
		situations.saveElements();
		queries.saveElements();
		rules.saveElements();
	}

	public void deleteContextSubscriptions() {
		rules.deleteContextSubscriptions();
	}

	/**
	 * 
	 * This methods seems to be some kind of strange because of the usage from
	 * the handleRequest method. This is a result of the simple fact the all
	 * services managed by this class are only used to add/remove/get objects of
	 * the ontology. Since the differences here are only in the used URI's I
	 * saved a lot of code by modularize this part.
	 * 
	 */
	@Override
	public ServiceResponse handleCall(ServiceCall call) {
		if (call == null)
			return null;

		String operation = call.getProcessURI();
		if (operation == null)
			return null;

		ServiceResponse response = null;

		response = handleRequest(call, situations,
				ProvidedReasoningService.SERVICE_GET_SITUATIONS,
				ProvidedReasoningService.SERVICE_ADD_SITUATION,
				ProvidedReasoningService.SERVICE_REMOVE_SITUATION,
				ProvidedReasoningService.OUTPUT_SITUATIONS,
				ProvidedReasoningService.INPUT_SITUATION);

		if (response == null) {
			response = handleRequest(call, queries,
					ProvidedReasoningService.SERVICE_GET_QUERIES,
					ProvidedReasoningService.SERVICE_ADD_QUERY,
					ProvidedReasoningService.SERVICE_REMOVE_QUERY,
					ProvidedReasoningService.OUTPUT_QUERIES,
					ProvidedReasoningService.INPUT_QUERY);
			if (response == null) {
				response = handleRequest(call, rules,
						ProvidedReasoningService.SERVICE_GET_RULES,
						ProvidedReasoningService.SERVICE_ADD_RULE,
						ProvidedReasoningService.SERVICE_REMOVE_RULE,
						ProvidedReasoningService.OUTPUT_RULES,
						ProvidedReasoningService.INPUT_RULE);
			}
		}

		return response;
	}

	/**
	 * 
	 * @SuppressWarnings("unchecked")
	 * 
	 * @param <M>
	 *            A class based on Persistent from the Reasoner ontology
	 * @param call
	 *            The ServiceCall like given by the middleware
	 * @param model
	 *            The ElementModel object that is used to handle the objects of
	 *            type M
	 * @param getService
	 *            URI of the service to get the elements of type M
	 * @param addService
	 *            URI of the service to add elements of type M
	 * @param removeService
	 *            URI of the service to remove elements of type M
	 * @param output
	 *            output-param in case the request is to get elements of type M
	 * @param input
	 *            input-param in case an element of type M need to be added or
	 *            removed
	 * @return ServiceRequest according to the call. Null if there was no match.
	 */
	@SuppressWarnings("unchecked")
	private <M extends Persistent> ServiceResponse handleRequest(
			ServiceCall call, ElementModel<M> model, String getService,
			String addService, String removeService, String output, String input) {
		String operation = call.getProcessURI();
		if (operation == null)
			return null;

		if (operation.startsWith(getService))
			return getResponse(output, model.getElements());

		if (operation.startsWith(addService)
				|| operation.startsWith(removeService)) {
			M element = (M) call.getInputValue(input);
			if (element == null)
				return invalidInput;
			if (operation.startsWith(addService))
				model.add(element);
			else
				model.remove(element);
			return new ServiceResponse(CallStatus.succeeded);
		}

		return null;
	}

	/**
	 * Creates a ServiceResponse with the given output-param and the given list
	 * as value
	 * 
	 * @param output_param
	 *            URI of the output-paramter to be added
	 * @param output
	 *            Value of the output-parameter where the
	 * @return
	 */
	private ServiceResponse getResponse(String output_param, ArrayList<?> output) {
		ServiceResponse sr = new ServiceResponse(CallStatus.succeeded);
		sr.addOutput(new ProcessOutput(output_param, output));
		return sr;
	}
}
