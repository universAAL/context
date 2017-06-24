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
import java.util.Iterator;
import java.util.List;

import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.owl.MergedRestriction;
import org.universAAL.middleware.owl.OntologyManagement;
import org.universAAL.middleware.rdf.PropertyPath;
import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.service.DefaultServiceCaller;
import org.universAAL.middleware.service.ServiceRequest;
import org.universAAL.middleware.service.ServiceResponse;
import org.universAAL.middleware.service.owls.process.ProcessOutput;
import org.universAAL.ontology.che.ContextHistoryService;
import org.universAAL.ontology.phThing.Device;
import org.universAAL.ontology.profile.AALService;
import org.universAAL.ontology.profile.AALServiceProfile;
import org.universAAL.ontology.profile.AALSpace;
import org.universAAL.ontology.profile.AALSpaceProfile;
import org.universAAL.ontology.profile.OntologyEntry;
import org.universAAL.ontology.profile.Profilable;
import org.universAAL.ontology.profile.User;

/**
 * Takes care of asking the CHE the equivalent request to the call received by
 * the profiling server.
 *
 * @author alfiva
 *
 */
public class SCaller {

	/**
	 * Where the result of the CHE will be placed.
	 */
	private static final String OUTPUT_RESULT_STRING = SCallee.NAMESPACE + "outputfromCHE";
	/**
	 * Actual SCaller that issues the calls.
	 */
	private DefaultServiceCaller defaultCaller;

	/**
	 * Default constructor.
	 *
	 * @param context
	 *            uAAL Module context.
	 */
	protected SCaller(ModuleContext context) {
		defaultCaller = new DefaultServiceCaller(context);
	}

	/**
	 * Relay for the actual SCaller.
	 */
	public void close() {
		defaultCaller.close();
	}

	// SPARQL UPDATE queries would include serialized uAAL data, but this
	// serialization is only possible currently to turtle
	// The turtle serialization could be in theory usable into the SPARUL query
	// as is, as long as:
	// 1: The prefixes are properly separated and placed before the query itself
	// 2: There are no subgraphs, because notation for subgraphs is different
	// between SPARQL and TURTLE
	// Another solution could be to implement a "storeResource" service in CHE
	// (which would work like storeEvent)
	// Then the whole data "payload" can be stored in the CHE without
	// serializing (CHE would take care of that)
	// and then use SPARUL to simply make the connection between owner of data
	// and data (in scenarios like adding 'User hasProf Profile')

	// :::::::::::::SPACE GET/ADD/CHANGE/REMOVE:::::::::::::::::

	protected Resource getAALSpace(Resource input) {
		return genericGet(input, Queries.GET.replace(Queries.ARGTYPE, AALSpace.MY_URI));
	}

	protected void addAALSpace(Resource input) {
		genericAdd(input, Queries.ADD);
	}

	protected void changeAALSpace(Resource input) {
		genericChange(input, Queries.CHANGE.replace(Queries.ARGTYPE, AALSpace.MY_URI));
	}

	protected void removeAALSpace(Resource input) {
		genericRemove(input, Queries.REMOVE.replace(Queries.ARGTYPE, AALSpace.MY_URI));
	}

	// :::::::::::::SPACE PROFILE GET/ADD/CHANGE/REMOVE:::::::::::::::::

	protected Resource getAALSpaceProfile(Resource input) {
		return genericGet(input, Queries.GET.replace(Queries.ARGTYPE, AALSpaceProfile.MY_URI));
	}

	protected void addAALSpaceProfile(Resource input) {
		genericAdd(input, Queries.ADD);
	}

	protected void changeAALSpaceProfile(Resource input) {
		genericChange(input, Queries.CHANGE.replace(Queries.ARGTYPE, AALSpaceProfile.MY_URI));
	}

	protected void removeAALSpaceProfile(Resource input) {
		genericRemove(input, Queries.REMOVE.replace(Queries.ARGTYPE, AALSpaceProfile.MY_URI));
	}

	// :::::::::::::SERVICE GET/ADD/CHANGE/REMOVE:::::::::::::::::

	protected Resource getAALService(Resource input) {
		return genericGet(input, Queries.GET.replace(Queries.ARGTYPE, AALService.MY_URI));
	}

	protected void addAALService(Resource input) {
		genericAdd(input, Queries.ADD);
	}

	protected void changeAALService(Resource input) {
		genericChange(input, Queries.CHANGE.replace(Queries.ARGTYPE, AALService.MY_URI));
	}

	protected void removeAALService(Resource input) {
		genericRemove(input, Queries.REMOVE.replace(Queries.ARGTYPE, AALService.MY_URI));
	}

	// :::::::::::::SERVICE PROFILE GET/ADD/CHANGE/REMOVE:::::::::::::::::

	public Resource getAALServiceProf(Resource input) {
		return genericGet(input, Queries.GET.replace(Queries.ARGTYPE, AALServiceProfile.MY_URI));
	}

	public void addAALServiceProf(Resource input) {
		genericAdd(input, Queries.ADD);
	}

	public void changeAALServiceProf(Resource input) {
		genericChange(input, Queries.CHANGE.replace(Queries.ARGTYPE, AALServiceProfile.MY_URI));
	}

	public void removeAALServiceProf(Resource input) {
		genericRemove(input, Queries.REMOVE.replace(Queries.ARGTYPE, AALServiceProfile.MY_URI));
	}

	// :::::::::::::DEVICE GET/ADD/CHANGE/REMOVE:::::::::::::::::

	public Resource getDevice(Resource input) {
		return genericGet(input, Queries.GET.replace(Queries.ARGTYPE, Device.MY_URI));
	}

	public void addDevice(Resource input) {
		genericAdd(input, Queries.ADD);
	}

	public void changeDevice(Resource input) {
		genericChange(input, Queries.CHANGE.replace(Queries.ARGTYPE, Device.MY_URI));
	}

	public void removeDevice(Resource input) {
		genericRemove(input, Queries.REMOVE.replace(Queries.ARGTYPE, Device.MY_URI));
	}

	// :::::::::::::ONT GET/ADD/CHANGE/REMOVE:::::::::::::::::

	public Resource getOnt(Resource input) {
		return genericGet(input, Queries.GET.replace(Queries.ARGTYPE, OntologyEntry.MY_URI));
	}

	public void addOnt(Resource input) {
		genericAdd(input, Queries.ADD);
	}

	public void changeOnt(Resource input) {
		genericChange(input, Queries.CHANGE.replace(Queries.ARGTYPE, OntologyEntry.MY_URI));
	}

	public void removeOnt(Resource input) {
		genericRemove(input, Queries.REMOVE.replace(Queries.ARGTYPE, OntologyEntry.MY_URI));
	}

	// :::::::::::::OTHER GETS AND ADDS:::::::::::::::::

	protected ArrayList getAALServices() {
		return genericGetAll(Queries.GETALL.replace(Queries.ARGTYPE, AALService.MY_URI),
				Queries.GETALLXTRA.replace(Queries.ARGTYPE, AALService.MY_URI));
	}

	public ArrayList getAALSpaces() {
		return genericGetAll(Queries.GETALL.replace(Queries.ARGTYPE, AALSpace.MY_URI),
				Queries.GETALLXTRA.replace(Queries.ARGTYPE, AALSpace.MY_URI));
	}

	protected Resource getHROfAALService(Resource input) {
		return genericGetOnePropOf(input,
				Queries.GETONEOFXTRA.replace(Queries.ARGTYPE, AALServiceProfile.PROP_HUMAN_RESOURCE_SUBPROFILE),
				Queries.GETONEOF.replace(Queries.ARGTYPE, AALServiceProfile.PROP_HUMAN_RESOURCE_SUBPROFILE));
	}

	public Resource getHWOfAALService(Resource input) {
		return genericGetOnePropOf(input,
				Queries.GETONEOFXTRA.replace(Queries.ARGTYPE, AALServiceProfile.PROP_HARDWARE_SUBPROFILE),
				Queries.GETONEOF.replace(Queries.ARGTYPE, AALServiceProfile.PROP_HARDWARE_SUBPROFILE));
	}

	public Resource getAppOfAALService(Resource input) {
		return genericGetOnePropOf(input,
				Queries.GETONEOFXTRA.replace(Queries.ARGTYPE, AALServiceProfile.PROP_APPLICATION_SUBPROFILE),
				Queries.GETONEOF.replace(Queries.ARGTYPE, AALServiceProfile.PROP_APPLICATION_SUBPROFILE));
	}

	protected ArrayList getServicesOfSpace(Resource input) {
		return genericGetAllOf(input,
				Queries.GETALLOF.replace(Queries.ARG2, AALSpaceProfile.PROP_INSTALLED_SERVICES).replace(Queries.ARGTYPE,
						AALService.MY_URI),
				Queries.GETALLOFXTRA.replace(Queries.ARG2, AALSpaceProfile.PROP_INSTALLED_SERVICES)
						.replace(Queries.ARGTYPE, AALService.MY_URI));
	}

	public ArrayList getDevicesOfSpace(Resource input) {
		return genericGetAllOf(input,
				Queries.GETALLOF.replace(Queries.ARG2, AALSpaceProfile.PROP_INSTALLED_HARDWARE).replace(Queries.ARGTYPE,
						Device.MY_URI),
				Queries.GETALLOFXTRA.replace(Queries.ARG2, AALSpaceProfile.PROP_INSTALLED_HARDWARE)
						.replace(Queries.ARGTYPE, Device.MY_URI));
	}

	public ArrayList getOntsOfSpace(Resource input) {
		return genericGetAllOf(input,
				Queries.GETALLOF.replace(Queries.ARG2, AALSpaceProfile.PROP_INSTALLED_ONTOLOGIES)
						.replace(Queries.ARGTYPE, OntologyEntry.MY_URI),
				Queries.GETALLOFXTRA.replace(Queries.ARG2, AALSpaceProfile.PROP_INSTALLED_ONTOLOGIES)
						.replace(Queries.ARGTYPE, OntologyEntry.MY_URI));
	}

	public ArrayList getOwnsOfSpace(Resource input) {
		return genericGetAllOf(input,
				Queries.GETALLOF.replace(Queries.ARG2, AALSpaceProfile.PROP_SPACE_OWNER).replace(Queries.ARGTYPE,
						User.MY_URI),
				Queries.GETALLOFXTRA.replace(Queries.ARG2, AALSpaceProfile.PROP_SPACE_OWNER).replace(Queries.ARGTYPE,
						User.MY_URI));
	}

	public ArrayList getOwnsOfServ(Resource input) {
		return genericGetAllOf(input,
				Queries.GETALLOF.replace(Queries.ARG2, AALServiceProfile.PROP_SERVICE_OWNER).replace(Queries.ARGTYPE,
						User.MY_URI),
				Queries.GETALLOFXTRA.replace(Queries.ARG2, AALServiceProfile.PROP_SERVICE_OWNER)
						.replace(Queries.ARGTYPE, User.MY_URI));
	}

	public Resource getProfOfServ(Resource input) {
		String resultx = getResult(defaultCaller
				.call(getDoSPARQLRequest(Queries.GETPRFOFPROFILABLEXTRA.replace(Queries.ARG1, input.getURI()))));
		Object objx = Activator.parser.deserialize(resultx);
		if (objx == null)
			return null;
		String result = getResult(defaultCaller
				.call(getDoSPARQLRequest(Queries.GETPRFOFPROFILABLE.replace(Queries.ARG1, input.getURI()))));
		String uri = ((Resource) objx).getURI();
		return (Resource) Activator.parser.deserialize(result, uri);
	}

	public Resource getProfOfSpace(Resource input) {
		String resultx = getResult(defaultCaller
				.call(getDoSPARQLRequest(Queries.GETPRFOFPROFILABLEXTRA.replace(Queries.ARG1, input.getURI()))));
		Object objx = Activator.parser.deserialize(resultx);
		if (objx == null)
			return null;
		String result = getResult(defaultCaller
				.call(getDoSPARQLRequest(Queries.GETPRFOFPROFILABLE.replace(Queries.ARG1, input.getURI()))));
		String uri = ((Resource) objx).getURI();
		return (Resource) Activator.parser.deserialize(result, uri);
	}

	protected void addServiceToSpace(Resource input, Resource what) {
		genericAddToSpace(input, what,
				Queries.ADDTOPROFILABLE.replace(Queries.ARGTYPE, AALSpaceProfile.PROP_INSTALLED_SERVICES));
	}

	protected void addDeviceToSpace(Resource input, Resource what) {
		genericAddToSpace(input, what,
				Queries.ADDTOPROFILABLE.replace(Queries.ARGTYPE, AALSpaceProfile.PROP_INSTALLED_HARDWARE));
	}

	protected void addOntToSpace(Resource input, Resource what) {
		genericAddToSpace(input, what,
				Queries.ADDTOPROFILABLE.replace(Queries.ARGTYPE, AALSpaceProfile.PROP_INSTALLED_ONTOLOGIES));
	}

	protected void addOwnToSpace(Resource input, Resource what) {
		genericAddToSpace(input, what,
				Queries.ADDTOPROFILABLE.replace(Queries.ARGTYPE, AALSpaceProfile.PROP_SPACE_OWNER));
	}

	protected void addOwnToServ(Resource input, Resource what) {
		genericAddToSpace(input, what,
				Queries.ADDTOPROFILABLE.replace(Queries.ARGTYPE, AALServiceProfile.PROP_SERVICE_OWNER));
	}

	protected void addProfToProfilable(Resource input, Resource what) {
		genericAddToSpace(input, what, Queries.ADDPROFTOPROFILABLE);
	}

	// :::::::::::::GENERIC GET/ADD/CHANGE/REMOVE:::::::::::::::::

	protected Resource genericGet(Resource input, String getquery) {
		String result = getResult(
				defaultCaller.call(getDoSPARQLRequest(getquery.replace(Queries.ARG1, input.getURI()))));
		return (Resource) Activator.parser.deserialize(result, input.getURI());
	}

	protected void genericAdd(Resource input, String addquery) {
		String serialized = Activator.parser.serialize(input);
		String[] split = splitPrefixes(serialized);
		defaultCaller.call(getDoSPARQLRequest(split[0] + " " + addquery.replace(Queries.ARGTURTLE, split[1])));
	}

	protected void genericChange(Resource input, String changequery) {
		String serialized = Activator.parser.serialize(input);
		String[] split = splitPrefixes(serialized);
		defaultCaller.call(getDoSPARQLRequest(split[0] + " "
				+ changequery.replace(Queries.ARG1, input.getURI()).replace(Queries.ARGTURTLE, split[1])));
	}

	protected void genericRemove(Resource input, String removequery) {
		defaultCaller.call(getDoSPARQLRequest(removequery.replace(Queries.ARG1, input.getURI())));
	}

	// :::::::::::::GENERIC OTHER GETS AND ADDS:::::::::::::::::

	protected ArrayList genericGetAll(String queryall, String queryallxtra) {
		// Or final choice: construct a bag with the results and a bag with the
		// types. Then combine the RDF in a single string and deserialize. It's
		// cheating but it works. And it only uses 2 calls and a serialize.
		String result = getResult(defaultCaller.call(getDoSPARQLRequest(queryall)));
		String result2 = getResult(defaultCaller.call(getDoSPARQLRequest(queryallxtra)));
		Resource bag = (Resource) Activator.parser.deserialize(result + " " + result2, Queries.AUXBAG);
		return getResultFromBag(bag);
	}

	protected Resource genericGetOnePropOf(Resource input, String queryaux, String queryfinal) {
		// First query gets only the URI of the requested prop
		// Second query builds the full req. prop, but it is not the clear root
		// (That's why I need to get only the URI first)
		String resultx = getResult(
				defaultCaller.call(getDoSPARQLRequest(queryaux.replace(Queries.ARG1, input.getURI()))));
		Object objx = Activator.parser.deserialize(resultx);
		if (objx == null)
			return null;
		String result = getResult(
				defaultCaller.call(getDoSPARQLRequest(queryfinal.replace(Queries.ARG1, input.getURI()))));
		String uri = ((Resource) objx).getURI();
		return (Resource) Activator.parser.deserialize(result, uri);
	}

	protected ArrayList genericGetAllOf(Resource input, String queryall, String queryallxtra) {
		String result1 = getResult(
				defaultCaller.call(getDoSPARQLRequest(queryall.replace(Queries.ARG1, input.getURI()))));
		String result2 = getResult(
				defaultCaller.call(getDoSPARQLRequest(queryallxtra.replace(Queries.ARG1, input.getURI()))));
		Resource bag = (Resource) Activator.parser.deserialize(result1 + " " + result2, Queries.AUXBAG);
		return getResultFromBag(bag);
	}

	protected void genericAddToSpace(Resource input, Resource what, String queryadd) {
		String serialized = Activator.parser.serialize(what);
		String[] split = splitPrefixes(serialized);
		defaultCaller.call(getDoSPARQLRequest(split[0] + " " + queryadd.replace(Queries.ARG1, input.getURI())
				.replace(Queries.ARG2, what.getURI()).replace(Queries.ARGTURTLE, split[1])));
	}

	// :::::::::::::UTILITY METHODS:::::::::::::::::

	/**
	 * Gets all results from a RDF Bag resource and returns them as an ArrayList
	 * of uAAL ontologies.
	 *
	 * @param bag
	 *            The RDF Bag Resource
	 * @return The ArrayList with results
	 */
	private ArrayList getResultFromBag(Resource bag) {
		if (bag != null) {
			Object content = bag.getProperty(Queries.AUXBAGPROP);
			ArrayList list = new ArrayList();
			OntologyManagement mng = OntologyManagement.getInstance();
			if (content instanceof List) {
				Iterator iter = ((ArrayList) content).iterator();
				while (iter.hasNext()) {
					Resource res = (Resource) iter.next();
					list.add(mng.getResource(mng.getMostSpecializedClass(res.getTypes()), res.getURI()));
				}
			} else {
				Resource res = (Resource) content;
				list.add(mng.getResource(mng.getMostSpecializedClass(res.getTypes()), res.getURI()));
			}
			return list;
		} else {
			return null;
		}
	}

	/**
	 * Prepares the call to the Do SPARQL service of CHE.
	 *
	 * @param query
	 *            The SPARQL query
	 * @return The prepared request
	 */
	private ServiceRequest getDoSPARQLRequest(String query) {
		ServiceRequest getQuery = new ServiceRequest(new ContextHistoryService(null), null);

		MergedRestriction r = MergedRestriction.getFixedValueRestriction(ContextHistoryService.PROP_PROCESSES, query);

		getQuery.getRequestedService().addInstanceLevelRestriction(r,
				new String[] { ContextHistoryService.PROP_PROCESSES });
		getQuery.addSimpleOutputBinding(new ProcessOutput(OUTPUT_RESULT_STRING),
				new PropertyPath(null, true, new String[] { ContextHistoryService.PROP_RETURNS }).getThePath());
		return getQuery;
	}

	/**
	 * Helper method to get the result from the Service Response of CHE.
	 *
	 * @param call
	 *            The service response
	 * @return the result SPARQL string
	 */
	private String getResult(ServiceResponse call) {
		Object returnValue = null;
		List outputs = call.getOutputs();
		if (outputs == null) {
			return null;
		} else {
			for (Iterator i = outputs.iterator(); i.hasNext();) {
				ProcessOutput output = (ProcessOutput) i.next();
				if (output.getURI().equals(OUTPUT_RESULT_STRING)) {
					if (returnValue == null) {
						returnValue = output.getParameterValue();
					}
				}

			}
			if (returnValue instanceof String) {
				return (String) returnValue;
			} else {
				return null;
			}
		}
	}

	/**
	 * Splits a Turtle serialized string into prefixes and content, so it can be
	 * used inside SPARQL queries.
	 *
	 * @param serialized
	 *            The turtle string
	 * @return An array of length 2. The first item [0] is the string with the
	 *         prefixes, and the second [1] is the string with the triples
	 *         content
	 */
	public static String[] splitPrefixes(String serialized) {
		int lastprefix = 0, lastprefixdot = 0, lastprefixuri = 0;
		lastprefix = serialized.toLowerCase().lastIndexOf("@prefix");
		if (lastprefix >= 0) {
			lastprefixuri = serialized.substring(lastprefix).indexOf(">");
			lastprefixdot = serialized.substring(lastprefix + lastprefixuri).indexOf(".");
		}
		String[] result = new String[2];
		result[0] = serialized.substring(0, lastprefixuri + lastprefixdot + lastprefix + 1).replace("@", " ")
				.replace(">.", "> ").replace(" .", " ").replace(". ", " ");
		result[1] = serialized.substring(lastprefixuri + lastprefixdot + lastprefix + 1);
		return result;
	}

}
