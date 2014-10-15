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

import org.universAAL.ontology.profile.Profilable;
import org.universAAL.ontology.profile.Profile;
import org.universAAL.ontology.profile.SubProfile;
import org.universAAL.ontology.profile.User;
import org.universAAL.ontology.profile.UserProfile;
//import org.universAAL.ontology.profile.userid.UserIDProfile;

/**
 * Class that holds the constant representation of the SPARQL queries to be
 * forwarded to CHE.
 * 
 * @author alfiva
 * 
 */
public class Queries {

    protected static final String ARG1 = "<ARG1>";
    protected static final String ARG2 = "<ARG2>";
    protected static final String ARGTURTLE = "<TURTLE>";
    protected static final String AUXBAG = SCallee.NAMESPACE + "auxBag";
    protected static final String AUXBAGPROP = SCallee.NAMESPACE + "auxBagProp";

    // :::::::::::::USER GET/ADD/CHANGE/REMOVE:::::::::::::::::
    protected static final String Q_GET_USER = "DESCRIBE <"
	    + ARG1 + "> WHERE { <"+ARG1+"> a <"+User.MY_URI+"> } ";
    protected static final String Q_ADD_USER = "INSERT DATA { "
	    + ARGTURTLE + " }";
    protected static final String Q_CHANGE_USER = "DELETE { <" + ARG1
	    + "> ?p ?o } INSERT { " + ARGTURTLE + " } WHERE { <" + ARG1
	    + "> ?p ?o ; a <"+User.MY_URI+"> }";
    protected static final String Q_REMOVE_USER = "DELETE { <" + ARG1
	    + "> ?p ?o . ?ss ?pp <" + ARG1 + "> } WHERE { <" + ARG1
	    + "> ?p ?o ; a <"+User.MY_URI+"> . OPTIONAL { ?ss ?pp <" + ARG1 + "> } }";

    // :::::::::::::USER PROFILE GET/ADD/CHANGE/REMOVE:::::::::::::::::
    protected static final String Q_GET_UPROFILE = "DESCRIBE <"
	    + ARG1 + "> WHERE { <"+ARG1+"> a <"+UserProfile.MY_URI+"> } ";
    protected static final String Q_ADD_UPROFILE = "INSERT DATA { "
	    + ARGTURTLE + " }";
    protected static final String Q_CHANGE_UPROFILE = "DELETE { <" + ARG1
	    + "> ?p ?o } INSERT { " + ARGTURTLE + " } WHERE { <" + ARG1
	    + "> ?p ?o ; a <"+UserProfile.MY_URI+"> }";
    protected static final String Q_REMOVE_UPROFILE = "DELETE { <" + ARG1
	    + "> ?p ?o . ?ss ?pp <" + ARG1 + "> } WHERE { <" + ARG1
	    + "> ?p ?o ; a <"+UserProfile.MY_URI+"> . OPTIONAL { ?ss ?pp <" + ARG1 + "> } }";

    // :::::::::::::SUBPROFILE GET/ADD/CHANGE/REMOVE:::::::::::::::::
    protected static final String Q_GET_SUBPROFILE = "DESCRIBE <"
	    + ARG1 + "> WHERE { <"+ARG1+"> a <"+SubProfile.MY_URI+"> } ";
    protected static final String Q_ADD_SUBPROFILE = "INSERT DATA { "
	    + ARGTURTLE + " }";
    protected static final String Q_CHANGE_SUBPROFILE = "DELETE { <" + ARG1
	    + "> ?p ?o } INSERT { " + ARGTURTLE + " } WHERE { <" + ARG1
	    + "> ?p ?o ; a <"+SubProfile.MY_URI+"> }";
    protected static final String Q_REMOVE_SUBPROFILE = "DELETE { <" + ARG1
	    + "> ?p ?o . ?ss ?pp <" + ARG1 + "> } WHERE { <" + ARG1
	    + "> ?p ?o ; a <"+SubProfile.MY_URI+"> . OPTIONAL { ?ss ?pp <" + ARG1 + "> } }";

    // :::::::::::::OTHER GETS:::::::::::::::::
    protected static final String Q_GET_USRS = "CONSTRUCT { <"
	    + AUXBAG
	    + "> <"
	    + AUXBAGPROP
	    + "> ?u } WHERE { ?u a <"+User.MY_URI+"> } ";
    protected static final String Q_GET_USRS_XTRA = "CONSTRUCT { <"
	    + AUXBAG
	    + "> <"
	    + AUXBAGPROP
	    + "> ?u . ?u a ?t . } WHERE { ?u a <"+User.MY_URI+"> ; a ?t . } ";
    protected static final String Q_GET_PRF_OF_USR = "DESCRIBE ?p WHERE {<"
	    + ARG1 + "> <" + Profilable.PROP_HAS_PROFILE + "> ?p}";
    protected static final String Q_GET_PRF_OF_USR_XTRA = "CONSTRUCT { ?p a ?t } WHERE {<"
	    + ARG1 + "> <" + Profilable.PROP_HAS_PROFILE + "> ?p . ?p a ?t}";
    public static final String Q_GET_SUBS_OF_USR = "CONSTRUCT { <"
	    + AUXBAG
	    + "> <"
	    + AUXBAGPROP
	    + "> ?s } WHERE { <"+ARG1+"> <"+Profilable.PROP_HAS_PROFILE+"> ?p . "
	    + " ?p <"+Profile.PROP_HAS_SUB_PROFILE+"> ?s . "
	    + " ?s a <"+SubProfile.MY_URI+"> } ";
    public static final String Q_GET_SUBS_OF_USR_XTRA = "CONSTRUCT { <"
	    + AUXBAG
	    + "> <"
	    + AUXBAGPROP
	    + "> ?s . ?s a ?t } WHERE { <"+ARG1+"> <"+Profilable.PROP_HAS_PROFILE+"> ?p . "
	    + " ?p <"+Profile.PROP_HAS_SUB_PROFILE+"> ?s . "
	    + " ?s a <"+SubProfile.MY_URI+"> ; a ?t } ";
    public static final String Q_GET_SUBS_OF_PRF = "CONSTRUCT { <"
	    + AUXBAG
	    + "> <"
	    + AUXBAGPROP
	    + "> ?s } WHERE { <"+ARG1+"> <"+Profile.PROP_HAS_SUB_PROFILE+"> ?s . "
	    + " ?s a <"+SubProfile.MY_URI+"> } ";
    public static final String Q_GET_SUBS_OF_PRF_XTRA = "CONSTRUCT { <"
	    + AUXBAG
	    + "> <"
	    + AUXBAGPROP
	    + "> ?s . ?s a ?t } WHERE { <"+ARG1+"> <"+Profile.PROP_HAS_SUB_PROFILE+"> ?s . "
	    + " ?s a <"+SubProfile.MY_URI+"> ; a ?t } ";
//    public static final String Q_GET_SECPRF_OF_USR="DESCRIBE ?sp WHERE {<"
//	    + ARG1 + "> <" + Profilable.PROP_HAS_PROFILE + "> ?p . " 
//	    + " ?p <"+Profile.PROP_HAS_SUB_PROFILE+"> ?sp . "
//	    + " ?sp a <"+UserIDProfile.MY_URI+"> }";
//    protected static final String Q_GET_SECPRF_OF_USR_XTRA = "CONSTRUCT { ?sp a ?t } WHERE {<"
//	    + ARG1 + "> <" + Profilable.PROP_HAS_PROFILE + "> ?p . " 
//	    + " ?p <"+Profile.PROP_HAS_SUB_PROFILE+"> ?sp . "
//	    + " ?sp a <"+UserIDProfile.MY_URI+">  . ?sp a ?t}";
    
    // :::::::::::::OTHER ADDS:::::::::::::::::
    protected static final String Q_ADD_PRF_TO_USR = "INSERT DATA { <"
	    + ARG1
	    + "> <"
	    + Profilable.PROP_HAS_PROFILE
	    + "> <"
	    + ARG2
	    + "> . " + ARGTURTLE + " }";
    protected static final String Q_ADD_SUB_TO_USR = "INSERT { ?p <"
	    + Profile.PROP_HAS_SUB_PROFILE 
	    + "> <" 
	    + ARG2 
	    + "> . " + ARGTURTLE + " } WHERE {<" 
	    + ARG1 
	    + "> <" 
	    + Profilable.PROP_HAS_PROFILE
	    + "> ?p}";
    protected static final String Q_ADD_SUB_TO_PRF = "INSERT DATA { <"
	    + ARG1
	    + "> <"
	    + Profile.PROP_HAS_SUB_PROFILE
	    + "> <"
	    + ARG2
	    + "> . " + ARGTURTLE + " }";
    
    protected static final String Q_AUX_ASK_EXISTS = "ASK <" + ARG1 + " ?p ?o>";

}
