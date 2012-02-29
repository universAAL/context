package org.universAAL.context.prof.serv;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.universAAL.context.che.ontology.ContextHistoryService;
import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.owl.MergedRestriction;
import org.universAAL.middleware.rdf.PropertyPath;
import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.service.DefaultServiceCaller;
import org.universAAL.middleware.service.ServiceRequest;
import org.universAAL.middleware.service.ServiceResponse;
import org.universAAL.middleware.service.owls.process.ProcessOutput;
import org.universAAL.ontology.profile.User;
//import org.universAAL.ontology.profile.User;

public class SCaller {

    private static final String OUTPUT_RESULT_STRING = "http://ontology.universAAL.org/ProfilingServer.owl#outputfromCHE";
    private DefaultServiceCaller defaultCaller;

    protected SCaller(ModuleContext context) {
	defaultCaller = new DefaultServiceCaller(context);
    }
    // TODO: All get/add/... seem to do the same. Do something about it?
    //:::::::::::::PROFILABLE GET/ADD/CHANGE/REMOVE:::::::::::::::::
    
    protected Resource getProfilableDetails(Resource input) {
	String result=getResult(defaultCaller.call(getDoSPARQLRequest(Queries.Q_GET_IN_PROFILABLE_OUT_PROFILABLE.replace(Queries.ARG1, input.getURI()))));
	return (Resource)Activator.parser.deserialize(result);
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
    protected void addProfilable(Resource input) {
	String serialized=Activator.parser.serialize(input);
	String[] split=splitPrefixes(serialized);//[0]: prefixes. [1]: statements
	defaultCaller.call(getDoSPARQLRequest(split[0]+" "+Queries.Q_ADD_IN_PROFILABLE.replace(Queries.ARGTURTLE, split[1])));
    }

    protected void changeProfilable(Resource input) {
	String serialized=Activator.parser.serialize(input);
	String[] split=splitPrefixes(serialized);//[0]: prefixes. [1]: statements
	defaultCaller.call(getDoSPARQLRequest(split[0]+" "+Queries.Q_CHANGE_IN_PROFILABLE.replace(Queries.ARG1, input.getURI()).replace(Queries.ARGTURTLE, split[1])));
    }

    protected void removeProfilable(Resource input) {
	defaultCaller.call(getDoSPARQLRequest(Queries.Q_REMOVE_IN_PROFILABLE.replace(Queries.ARG1, input.getURI())));
    }

    //:::::::::::::PROFILE GET/ADD/CHANGE/REMOVE:::::::::::::::::
    
    protected Resource getProfileDetails(Resource input) {
	String result=getResult(defaultCaller.call(getDoSPARQLRequest(Queries.Q_GET_IN_PROFILE_OUT_PROFILE.replace(Queries.ARG1, input.getURI()))));
	return (Resource)Activator.parser.deserialize(result);
    }
    
    protected void addProfile(Resource input) {
   	String serialized=Activator.parser.serialize(input);
   	String[] split=splitPrefixes(serialized);//[0]: prefixes. [1]: statements
   	defaultCaller.call(getDoSPARQLRequest(split[0]+" "+Queries.Q_ADD_IN_PROFILE.replace(Queries.ARGTURTLE, split[1])));
    }

    protected void changeProfile(Resource input) {
   	String serialized=Activator.parser.serialize(input);
   	String[] split=splitPrefixes(serialized);//[0]: prefixes. [1]: statements
   	defaultCaller.call(getDoSPARQLRequest(split[0]+" "+Queries.Q_CHANGE_IN_PROFILE.replace(Queries.ARG1, input.getURI()).replace(Queries.ARGTURTLE, split[1])));
    }
    
    protected void removeProfile(Resource input) {
	defaultCaller.call(getDoSPARQLRequest(Queries.Q_REMOVE_IN_PROFILE.replace(Queries.ARG1, input.getURI())));
    }
    
    //:::::::::::::SUBPROFILE GET/ADD/CHANGE/REMOVE:::::::::::::::::
    
    protected Resource getSubProfileDetails(Resource input) {
	String result=getResult(defaultCaller.call(getDoSPARQLRequest(Queries.Q_GET_IN_SUBPROFILE_OUT_SUBPROFILE.replace(Queries.ARG1, input.getURI()))));
	return (Resource)Activator.parser.deserialize(result);
    }
    
    protected void addSubProfile(Resource input) {
   	String serialized=Activator.parser.serialize(input);
   	String[] split=splitPrefixes(serialized);//[0]: prefixes. [1]: statements
   	defaultCaller.call(getDoSPARQLRequest(split[0]+" "+Queries.Q_ADD_IN_SUBPROFILE.replace(Queries.ARGTURTLE, split[1])));
    }

    protected void changeSubProfile(Resource input) {
   	String serialized=Activator.parser.serialize(input);
   	String[] split=splitPrefixes(serialized);//[0]: prefixes. [1]: statements
   	defaultCaller.call(getDoSPARQLRequest(split[0]+" "+Queries.Q_CHANGE_IN_SUBPROFILE.replace(Queries.ARG1, input.getURI()).replace(Queries.ARGTURTLE, split[1])));
    }
    
    protected void removeSubProfile(Resource input) {
	defaultCaller.call(getDoSPARQLRequest(Queries.Q_REMOVE_IN_SUBPROFILE.replace(Queries.ARG1, input.getURI())));
    }
    
    //:::::::::::::OTHERS:::::::::::::::::
    
    protected Resource getProfile(Resource input) {
	String result=getResult(defaultCaller.call(getDoSPARQLRequest(Queries.Q_GET_IN_PROFILABLE_OUT_PROFILE.replace(Queries.ARG1, input.getURI()))));
	return (Resource)Activator.parser.deserialize(result);
    }

    protected void addProfile(Resource input, Resource input2) {
	String serialized=Activator.parser.serialize(input2);
	String[] split=splitPrefixes(serialized);//[0]: prefixes. [1]: statements
	defaultCaller.call(getDoSPARQLRequest(split[0]+" "+Queries.Q_ADD_IN_PROFILABLE_PROFILE.replace(Queries.ARG1, input.getURI()).replace(Queries.ARG2, input2.getURI()).replace(Queries.ARGTURTLE, split[1])));
    }

    // Take into account that since cardinality of hasProfile is only 1,
    // changeProfile removes the previous value of profile, no matter if the
    // URI of the old profile is not equal to the new profile
    protected void changeProfile(Resource input, Resource input2) {
	String serialized=Activator.parser.serialize(input2);
	String[] split=splitPrefixes(serialized);//[0]: prefixes. [1]: statements
	defaultCaller.call(getDoSPARQLRequest(split[0]+" "+Queries.Q_REPLACE_IN_PROFILABLE_PROFILE.replace(Queries.ARG1, input.getURI()).replace(Queries.ARG2, input2.getURI()).replace(Queries.ARGTURTLE, split[1])));
    }



    // For getting several results maybe it´s better to issue a SELECT query to
    // CHE. The result will be in SPARQL result XML
    // format, so it will have to be processed here. It will have to use
    // Sesame... although any RDF framework should do.
    // First convert to framework specific, then list the results, parse each to
    // Turtle and then to uAAL. That´s 3 parses.
    // What about: CONSTRUCT { <http://ontology.itaca.upv.es/Test.owl#testBag> <http://ontology.itaca.upv.es/Test.owl#testProp> ?y }
    //             WHERE { ?y a <http://ontology.universAAL.org/Profile.owl#User> } 
    // It works OK, but the type is not returned so you can´t get the most
    // specialized class and you have to cast manually here to User. And you
    // have no way to know if it´s an AP or a caregiver. Unless you create
    // methods for each.
    // Another solution is to issue a DESCRIBE to each of them. So in the end there is a tradeof you have to choose:
    // 1: Import Sesame and spend time in the 3-way parsing of  SELECT
    // 2: Create a get* for all kind of current and future types of User
    // 3: Use this getUser and then call a DESCRIBE on each
    protected ArrayList getUsers() {
	String result=getResult(defaultCaller.call(getDoSPARQLRequest(Queries.Q_GET_OUT_USERS)));
	Resource bag=(Resource)Activator.parser.deserialize(result);
	if(bag!=null){
	    Object content = bag.getProperty(Queries.AUXBAGPROP);
	    ArrayList list = new ArrayList();
	    if (content instanceof List) {
		Iterator iter = ((ArrayList) content).iterator();
		while (iter.hasNext()) {
		    Resource res=(Resource) iter.next();
		    list.add(new User(res.getURI()));
		}
	    } else {
		list.add(new User(((Resource)content).getURI()));
	    }
	    return list;
	}else{
	    return null;
	}
    }

    protected Resource[] getSubprofiles(Resource input) {
//	String result=getResult(defaultCaller.call(getDoSPARQLRequest(Queries.Q_GET_IN_PROFILABLE_OUT_SUBPROFILES.replace(Queries.ARG1, in.getURI()))));
	return null;
    }

    public void close() {
	defaultCaller.close();
    }
    
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
