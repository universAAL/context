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
    private static final String OUTPUT_RESULT_STRING = SCallee.NAMESPACE
	    + "outputfromCHE";
    /**
     * Actual SCaller that issues the calls.
     */
    private DefaultServiceCaller defaultCaller;

    /**
     * Default constructor.
     * 
     * @param mcontext
     *            uAAL Module mcontext.
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

    // TODO: All get/add/... seem to do the same. Do something about it?
    // :::::::::::::USER GET/ADD/CHANGE/REMOVE:::::::::::::::::

    protected Resource getUser(Resource input) {
	String result = getResult(defaultCaller
		.call(getDoSPARQLRequest(Queries.Q_GET_USER.replace(
			Queries.ARG1, input.getURI()))));
	return (Resource) Hub.parser.deserialize(result, input.getURI());
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
    protected void addUser(Resource input) {
	String serialized = Hub.parser.serialize(input);
	String[] split = splitPrefixes(serialized);
	defaultCaller.call(getDoSPARQLRequest(split[0] + " "
		+ Queries.Q_ADD_USER.replace(Queries.ARGTURTLE, split[1])));
    }

    protected void changeUser(Resource input) {
	String serialized = Hub.parser.serialize(input);
	String[] split = splitPrefixes(serialized);
	defaultCaller.call(getDoSPARQLRequest(split[0]
		+ " "
		+ Queries.Q_CHANGE_USER.replace(Queries.ARG1, input.getURI())
			.replace(Queries.ARGTURTLE, split[1])));
    }

    protected void removeUser(Resource input) {
	defaultCaller.call(getDoSPARQLRequest(Queries.Q_REMOVE_USER.replace(
		Queries.ARG1, input.getURI())));
    }

    // :::::::::::::PROFILE GET/ADD/CHANGE/REMOVE:::::::::::::::::

    protected Resource getProfile(Resource input) {
	String result = getResult(defaultCaller
		.call(getDoSPARQLRequest(Queries.Q_GET_UPROFILE.replace(
			Queries.ARG1, input.getURI()))));
	return (Resource) Hub.parser.deserialize(result, input.getURI());
    }

    protected void addProfile(Resource input) {
	String serialized = Hub.parser.serialize(input);
	String[] split = splitPrefixes(serialized);
	defaultCaller.call(getDoSPARQLRequest(split[0] + " "
		+ Queries.Q_ADD_UPROFILE.replace(Queries.ARGTURTLE, split[1])));
    }

    protected void changeProfile(Resource input) {
	String serialized = Hub.parser.serialize(input);
	String[] split = splitPrefixes(serialized);
	defaultCaller.call(getDoSPARQLRequest(split[0]
		+ " "
		+ Queries.Q_CHANGE_UPROFILE.replace(Queries.ARG1,
			input.getURI()).replace(Queries.ARGTURTLE, split[1])));
    }

    protected void removeProfile(Resource input) {
	defaultCaller.call(getDoSPARQLRequest(Queries.Q_REMOVE_UPROFILE
		.replace(Queries.ARG1, input.getURI())));
    }

    // :::::::::::::SUBPROFILE GET/ADD/CHANGE/REMOVE:::::::::::::::::

    protected Resource getSubProfile(Resource input) {
	String result = getResult(defaultCaller
		.call(getDoSPARQLRequest(Queries.Q_GET_SUBPROFILE.replace(
			Queries.ARG1, input.getURI()))));
	return (Resource) Hub.parser.deserialize(result, input.getURI());
    }

    protected void addSubProfile(Resource input) {
	String serialized = Hub.parser.serialize(input);
	String[] split = splitPrefixes(serialized);
	defaultCaller
		.call(getDoSPARQLRequest(split[0]
			+ " "
			+ Queries.Q_ADD_SUBPROFILE.replace(Queries.ARGTURTLE,
				split[1])));
    }

    protected void changeSubProfile(Resource input) {
	String serialized = Hub.parser.serialize(input);
	String[] split = splitPrefixes(serialized);
	defaultCaller.call(getDoSPARQLRequest(split[0]
		+ " "
		+ Queries.Q_CHANGE_SUBPROFILE.replace(Queries.ARG1,
			input.getURI()).replace(Queries.ARGTURTLE, split[1])));
    }

    protected void removeSubProfile(Resource input) {
	defaultCaller.call(getDoSPARQLRequest(Queries.Q_REMOVE_SUBPROFILE
		.replace(Queries.ARG1, input.getURI())));
    }

    // :::::::::::::OTHER GETS:::::::::::::::::

    // For getting several results maybe it´s better to issue a SELECT query to
    // CHE. The result will be in SPARQL result XML
    // format, so it will have to be processed here. It will have to use
    // Sesame... although any RDF framework should do.
    // First convert to framework specific, then list the results, parse each to
    // Turtle and then to uAAL. That´s 3 parses.
    // What about: CONSTRUCT { <http://ontology.itaca.upv.es/Test.owl#testBag>
    // <http://ontology.itaca.upv.es/Test.owl#testProp> ?y }
    // WHERE { ?y a <http://ontology.universAAL.org/Profile.owl#User> }
    // It works OK, but the type is not returned so you can´t get the most
    // specialized class and you have to cast manually here to User. And you
    // have no way to know if it´s an AP or a caregiver. Unless you create
    // methods for each.
    // Another solution is to issue a DESCRIBE to each of them. So in the end
    // there is a tradeof you have to choose:
    // 1: Import Sesame and spend time in the 3-way parsing of SELECT
    // 2: Create a get* for all kind of current and future types of User
    // 3: Use this getUser and then call a DESCRIBE on each
    // ...Or a final choice: construct a bag with the results and a bag with the
    // types. Then combine the RDF in a single string and deserialize. It´s
    // cheating but it works. And it only uses 2 calls and a serialize.
    protected ArrayList getUsers() {
	String result = getResult(defaultCaller
		.call(getDoSPARQLRequest(Queries.Q_GET_USRS)));
	String result2 = getResult(defaultCaller
		.call(getDoSPARQLRequest(Queries.Q_GET_USRS_XTRA)));
	Resource bag = (Resource) Hub.parser.deserialize(result + " "
		+ result2, Queries.AUXBAG);
	if (bag != null) {
	    Object content = bag.getProperty(Queries.AUXBAGPROP);
	    ArrayList list = new ArrayList();
	    OntologyManagement mng = OntologyManagement.getInstance();
	    if (content instanceof List) {
		Iterator iter = ((ArrayList) content).iterator();
		while (iter.hasNext()) {
		    Resource res = (Resource) iter.next();
		    list.add(mng.getResource(
			    mng.getMostSpecializedClass(res.getTypes()),
			    res.getURI()));
		}
	    } else {
		Resource res = (Resource) content;
		list.add(mng.getResource(
			mng.getMostSpecializedClass(res.getTypes()),
			res.getURI()));
	    }
	    return list;
	} else {
	    return null;
	}
    }

    protected Resource getProfileOfUser(Resource user) {
	// I need this extra query to get only the URI of profile to deserialize
	// it correctly later.
	String resultx = getResult(defaultCaller
		.call(getDoSPARQLRequest(Queries.Q_GET_PRF_OF_USR_XTRA.replace(
			Queries.ARG1, user.getURI()))));
	Object objx = Hub.parser.deserialize(resultx);
	if (objx == null)
	    return null;
	String result = getResult(defaultCaller
		.call(getDoSPARQLRequest(Queries.Q_GET_PRF_OF_USR.replace(
			Queries.ARG1, user.getURI()))));
	String uri = ((Resource) objx).getURI();
	return (Resource) Hub.parser.deserialize(result, uri);
    }

    protected ArrayList getSubProfilesOfUser(Resource user) {
	String result1 = getResult(defaultCaller
		.call(getDoSPARQLRequest(Queries.Q_GET_SUBS_OF_USR.replace(
			Queries.ARG1, user.getURI()))));
	String result2 = getResult(defaultCaller
		.call(getDoSPARQLRequest(Queries.Q_GET_SUBS_OF_USR_XTRA
			.replace(Queries.ARG1, user.getURI()))));
	Resource bag = (Resource) Hub.parser.deserialize(result1 + " "
		+ result2, Queries.AUXBAG);
	if (bag != null) {
	    Object content = bag.getProperty(Queries.AUXBAGPROP);
	    ArrayList list = new ArrayList();
	    OntologyManagement mng = OntologyManagement.getInstance();
	    if (content instanceof List) {
		Iterator iter = ((ArrayList) content).iterator();
		while (iter.hasNext()) {
		    Resource res = (Resource) iter.next();
		    list.add(mng.getResource(
			    mng.getMostSpecializedClass(res.getTypes()),
			    res.getURI()));
		}
	    } else {
		Resource res = (Resource) content;
		list.add(mng.getResource(
			mng.getMostSpecializedClass(res.getTypes()),
			res.getURI()));
	    }
	    return list;
	} else {
	    return null;
	}
    }

    protected ArrayList getSubProfilesOfProfile(Resource profile) {
	String result1 = getResult(defaultCaller
		.call(getDoSPARQLRequest(Queries.Q_GET_SUBS_OF_PRF.replace(
			Queries.ARG1, profile.getURI()))));
	String result2 = getResult(defaultCaller
		.call(getDoSPARQLRequest(Queries.Q_GET_SUBS_OF_PRF_XTRA
			.replace(Queries.ARG1, profile.getURI()))));
	Resource bag = (Resource) Hub.parser.deserialize(result1 + " "
		+ result2, Queries.AUXBAG);
	if (bag != null) {
	    Object content = bag.getProperty(Queries.AUXBAGPROP);
	    ArrayList list = new ArrayList();
	    OntologyManagement mng = OntologyManagement.getInstance();
	    if (content instanceof List) {
		Iterator iter = ((ArrayList) content).iterator();
		while (iter.hasNext()) {
		    Resource res = (Resource) iter.next();
		    list.add(mng.getResource(
			    mng.getMostSpecializedClass(res.getTypes()),
			    res.getURI()));
		}
	    } else {
		Resource res = (Resource) content;
		list.add(mng.getResource(
			mng.getMostSpecializedClass(res.getTypes()),
			res.getURI()));
	    }
	    return list;
	} else {
	    return null;
	}
    }


//    public Resource getSubProfileOfUser(Resource user) {
//	String resultx = getResult(defaultCaller
//		.call(getDoSPARQLRequest(Queries.Q_GET_PRF_OF_USR_XTRA.replace(
//			Queries.ARG1, user.getURI()))));
//	Object objx = Hub.parser.deserialize(resultx);
//	if (objx == null)
//	    return null;
//	String result = getResult(defaultCaller
//		.call(getDoSPARQLRequest(Queries.Q_GET_PRF_OF_USR.replace(
//			Queries.ARG1, user.getURI()))));
//	String uri = ((Resource) objx).getURI();
//	return (Resource) Hub.parser.deserialize(result, uri);
//    }
    
    // :::::::::::::OTHER ADDS:::::::::::::::::

    protected void addProfileToUser(Resource user, Resource profile) {
	String serialized = Hub.parser.serialize(profile);
	String[] split = splitPrefixes(serialized);
	defaultCaller.call(getDoSPARQLRequest(split[0]
		+ " "
		+ Queries.Q_ADD_PRF_TO_USR.replace(Queries.ARG1, user.getURI())
			.replace(Queries.ARG2, profile.getURI())
			.replace(Queries.ARGTURTLE, split[1])));
    }

    protected void addSubProfileToUser(Resource user, Resource subprofile) {
	String serialized = Hub.parser.serialize(subprofile);
	String[] split = splitPrefixes(serialized);
	defaultCaller.call(getDoSPARQLRequest(split[0]
		+ " "
		+ Queries.Q_ADD_SUB_TO_USR.replace(Queries.ARG1, user.getURI())
			.replace(Queries.ARG2, subprofile.getURI())
			.replace(Queries.ARGTURTLE, split[1])));
    }

    protected void addSubProfileToProf(Resource profile, Resource subprofile) {
	String serialized = Hub.parser.serialize(subprofile);
	String[] split = splitPrefixes(serialized);
	defaultCaller.call(getDoSPARQLRequest(split[0]
		+ " "
		+ Queries.Q_ADD_SUB_TO_PRF
			.replace(Queries.ARG1, profile.getURI())
			.replace(Queries.ARG2, subprofile.getURI())
			.replace(Queries.ARGTURTLE, split[1])));
    }

    /**
     * Prepares the call to the Do SPARQL service of CHE.
     * 
     * @param query
     *            The SPARQL query
     * @return The prepared request
     */
    private ServiceRequest getDoSPARQLRequest(String query) {
	ServiceRequest getQuery = new ServiceRequest(new ContextHistoryService(
		null), null);

	MergedRestriction r = MergedRestriction.getFixedValueRestriction(
		ContextHistoryService.PROP_PROCESSES, query);

	getQuery.getRequestedService().addInstanceLevelRestriction(r,
		new String[] { ContextHistoryService.PROP_PROCESSES });
	getQuery.addSimpleOutputBinding(
		new ProcessOutput(OUTPUT_RESULT_STRING), new PropertyPath(null,
			true,
			new String[] { ContextHistoryService.PROP_RETURNS })
			.getThePath());
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
		if (output.getURI().equals(OUTPUT_RESULT_STRING)
			&& returnValue == null) {
		    returnValue = output.getParameterValue();
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
	    lastprefixdot = serialized.substring(lastprefix + lastprefixuri)
		    .indexOf(".");
	}
	String[] result = new String[2];
	result[0] = serialized
		.substring(0, lastprefixuri + lastprefixdot + lastprefix + 1)
		.replace("@", " ").replace(">.", "> ").replace(" .", " ")
		.replace(". ", " ");
	result[1] = serialized.substring(lastprefixuri + lastprefixdot
		+ lastprefix + 1);
	return result;
    }


}
