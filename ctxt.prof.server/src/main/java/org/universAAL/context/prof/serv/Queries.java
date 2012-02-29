package org.universAAL.context.prof.serv;

import org.universAAL.ontology.profile.Profilable;

public class Queries {
    protected static final String ARG1="<ARG1>";
    protected static final String ARG2="<ARG2>";
    protected static final String ARGTURTLE="<TURTLE>";
    protected static final String AUXBAG=SCallee.NAMESPACE+"auxBag";
    protected static final String AUXBAGPROP=SCallee.NAMESPACE+"auxBagProp";
    //TODO: All GET/ADD/CHANGE/REMOVE queries are the same, but keep them just in case...
    //:::::::::::::PROFILABLE GET/ADD/CHANGE/REMOVE:::::::::::::::::
    protected static final String Q_GET_IN_PROFILABLE_OUT_PROFILABLE="DESCRIBE <"+ARG1+">";
    protected static final String Q_ADD_IN_PROFILABLE="INSERT DATA { "+ARGTURTLE+" }";
    protected static final String Q_CHANGE_IN_PROFILABLE="DELETE { <"+ARG1+"> ?p ?o } INSERT { "+ARGTURTLE+" } WHERE { <"+ARG1+"> ?p ?o }";
    protected static final String Q_REMOVE_IN_PROFILABLE="DELETE { <"+ARG1+"> ?p ?o . ?ss ?pp <"+ARG1+"> } WHERE { <"+ARG1+"> ?p ?o . OPTIONAL { ?ss ?pp <"+ARG1+"> } }";
    
    //:::::::::::::PROFILE GET/ADD/CHANGE/REMOVE:::::::::::::::::
    protected static final String Q_GET_IN_PROFILE_OUT_PROFILE = "DESCRIBE <"+ARG1+">";
    protected static final String Q_ADD_IN_PROFILE="INSERT DATA { "+ARGTURTLE+" }";
    protected static final String Q_CHANGE_IN_PROFILE="DELETE { <"+ARG1+"> ?p ?o } INSERT { "+ARGTURTLE+" } WHERE { <"+ARG1+"> ?p ?o }";
    protected static final String Q_REMOVE_IN_PROFILE="DELETE { <"+ARG1+"> ?p ?o . ?ss ?pp <"+ARG1+"> } WHERE { <"+ARG1+"> ?p ?o . OPTIONAL { ?ss ?pp <"+ARG1+"> } }";
    
    //:::::::::::::SUBPROFILE GET/ADD/CHANGE/REMOVE:::::::::::::::::
    protected static final String Q_GET_IN_SUBPROFILE_OUT_SUBPROFILE="DESCRIBE <"+ARG1+">";
    protected static final String Q_ADD_IN_SUBPROFILE="INSERT DATA { "+ARGTURTLE+" }";
    protected static final String Q_CHANGE_IN_SUBPROFILE="DELETE { <"+ARG1+"> ?p ?o } INSERT { "+ARGTURTLE+" } WHERE { <"+ARG1+"> ?p ?o }";
    protected static final String Q_REMOVE_IN_SUBPROFILE="DELETE { <"+ARG1+"> ?p ?o . ?ss ?pp <"+ARG1+"> } WHERE { <"+ARG1+"> ?p ?o . OPTIONAL { ?ss ?pp <"+ARG1+"> } }";
    
    //:::::::::::::OTHERS:::::::::::::::::
    protected static final String Q_GET_IN_PROFILABLE_OUT_PROFILE="DESCRIBE ?p WHERE {<"+ARG1+"> <"+Profilable.PROP_HAS_PROFILE+"> ?p}";
    protected static final String Q_ADD_IN_PROFILABLE_PROFILE="INSERT DATA { <"+ARG1+"> <"+Profilable.PROP_HAS_PROFILE+"> <"+ARG2+"> . "+ARGTURTLE+" }";
    protected static final String Q_REPLACE_IN_PROFILABLE_PROFILE="DELETE { ?s ?p ?old . ?old ?pp ?o . <"+ARG1+"> <"+Profilable.PROP_HAS_PROFILE+"> ?old } INSERT { <"+ARG1+"> <"+Profilable.PROP_HAS_PROFILE+"> <"+ARG2+"> . "+ARGTURTLE+" } WHERE { <"+ARG1+"> <"+Profilable.PROP_HAS_PROFILE+"> ?old . ?s ?p ?old . ?old ?pp ?o }";

    protected static final String Q_GET_IN_PROFILABLE_OUT_SUBPROFILES="";
    
    protected static final String Q_GET_OUT_USERS="CONSTRUCT { <"+AUXBAG+"> <"+AUXBAGPROP+"> ?y } WHERE { ?y a <http://ontology.universAAL.org/Profile.owl#User> } ";
    
    protected static final String Q_AUX_ASK_EXISTS="ASK <"+ARG1+" ?p ?o>";
    
    
}
