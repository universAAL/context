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

    // TODO: All GET/ADD/CHANGE/REMOVE queries are the same, but keep them just
    // in case...
    // :::::::::::::PROFILABLE GET/ADD/CHANGE/REMOVE:::::::::::::::::
    protected static final String Q_GET_PROFILABLE = "DESCRIBE <"
	    + ARG1 + "> WHERE { <"+ARG1+"> a <"+Profilable.MY_URI+"> } ";
    protected static final String Q_ADD_PROFILABLE = "INSERT DATA { "
	    + ARGTURTLE + " }";
    protected static final String Q_CHANGE_PROFILABLE = "DELETE { <" + ARG1
	    + "> ?p ?o } INSERT { " + ARGTURTLE + " } WHERE { <" + ARG1
	    + "> ?p ?o ; a <"+Profilable.MY_URI+"> }";
    protected static final String Q_REMOVE_PROFILABLE = "DELETE { <" + ARG1
	    + "> ?p ?o . ?ss ?pp <" + ARG1 + "> } WHERE { <" + ARG1
	    + "> ?p ?o ; a <"+Profilable.MY_URI+"> . OPTIONAL { ?ss ?pp <" + ARG1 + "> } }";

    // :::::::::::::PROFILE GET/ADD/CHANGE/REMOVE:::::::::::::::::
    protected static final String Q_GET_PROFILE = "DESCRIBE <"
	    + ARG1 + "> WHERE { <"+ARG1+"> a <"+Profile.MY_URI+"> } ";
    protected static final String Q_ADD_PROFILE = "INSERT DATA { "
	    + ARGTURTLE + " }";
    protected static final String Q_CHANGE_PROFILE = "DELETE { <" + ARG1
	    + "> ?p ?o } INSERT { " + ARGTURTLE + " } WHERE { <" + ARG1
	    + "> ?p ?o ; a <"+Profile.MY_URI+"> }";
    protected static final String Q_REMOVE_PROFILE = "DELETE { <" + ARG1
	    + "> ?p ?o . ?ss ?pp <" + ARG1 + "> } WHERE { <" + ARG1
	    + "> ?p ?o ; a <"+Profile.MY_URI+"> . OPTIONAL { ?ss ?pp <" + ARG1 + "> } }";

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

    // :::::::::::::OTHERS:::::::::::::::::
    protected static final String Q_GET_IN_PROFILABLE_OUT_PROFILE = "DESCRIBE ?p WHERE {<"
	    + ARG1 + "> <" + Profilable.PROP_HAS_PROFILE + "> ?p}";
    protected static final String Q_ADD_IN_PROFILABLE_PROFILE = "INSERT DATA { <"
	    + ARG1
	    + "> <"
	    + Profilable.PROP_HAS_PROFILE
	    + "> <"
	    + ARG2
	    + "> . " + ARGTURTLE + " }";
    protected static final String Q_REPLACE_IN_PROFILABLE_PROFILE = "DELETE { ?s ?p ?old . ?old ?pp ?o . <"
	    + ARG1
	    + "> <"
	    + Profilable.PROP_HAS_PROFILE
	    + "> ?old } INSERT { <"
	    + ARG1
	    + "> <"
	    + Profilable.PROP_HAS_PROFILE
	    + "> <"
	    + ARG2
	    + "> . "
	    + ARGTURTLE
	    + " } WHERE { <"
	    + ARG1
	    + "> <"
	    + Profilable.PROP_HAS_PROFILE
	    + "> ?old . ?s ?p ?old . ?old ?pp ?o }";

    protected static final String Q_GET_IN_PROFILABLE_OUT_SUBPROFILES = "";

    protected static final String Q_GET_USERS = "CONSTRUCT { <"
	    + AUXBAG
	    + "> <"
	    + AUXBAGPROP
	    + "> ?y } WHERE { ?y a <http://ontology.universAAL.org/Profile.owl#User> } ";

    protected static final String Q_AUX_ASK_EXISTS = "ASK <" + ARG1 + " ?p ?o>";

}
