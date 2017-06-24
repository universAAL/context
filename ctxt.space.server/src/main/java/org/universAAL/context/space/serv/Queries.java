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

import org.universAAL.ontology.profile.Profilable;

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
	protected static final String ARGTYPE = "<TYPE>";
	protected static final String AUXBAG = SCallee.NAMESPACE + "auxBag";
	protected static final String AUXBAGPROP = SCallee.NAMESPACE + "auxBagProp";

	// :::::::::::::GENERIC GET/ADD/CHANGE/REMOVE:::::::::::::::::
	protected static final String GET = "DESCRIBE <" + ARG1 + "> WHERE { <" + ARG1 + "> a <" + ARGTYPE + "> } ";
	protected static final String ADD = "INSERT DATA { " + ARGTURTLE + " }";
	protected static final String CHANGE = "DELETE { <" + ARG1 + "> ?p ?o } INSERT { " + ARGTURTLE + " } WHERE { <"
			+ ARG1 + "> ?p ?o ; a <" + ARGTYPE + "> }";
	protected static final String REMOVE = "DELETE { <" + ARG1 + "> ?p ?o . ?ss ?pp <" + ARG1 + "> } WHERE { <" + ARG1
			+ "> ?p ?o ; a <" + ARGTYPE + "> . OPTIONAL { ?ss ?pp <" + ARG1 + "> } }";

	// :::::::::::::OTHER GENERICS:::::::::::::::::
	public static final String GETALLOF = "CONSTRUCT { <" + AUXBAG + "> <" + AUXBAGPROP + "> ?s } WHERE { <" + ARG1
			+ "> <" + Profilable.PROP_HAS_PROFILE + "> ?p . " + " ?p <" + ARG2 + "> ?s . " + " ?s a <" + ARGTYPE
			+ "> } ";
	public static final String GETALLOFXTRA = "CONSTRUCT { <" + AUXBAG + "> <" + AUXBAGPROP
			+ "> ?s . ?s a ?t } WHERE { <" + ARG1 + "> <" + Profilable.PROP_HAS_PROFILE + "> ?p . " + " ?p <" + ARG2
			+ "> ?s . " + " ?s a <" + ARGTYPE + "> ; a ?t } ";

	protected static final String GETALL = "CONSTRUCT { <" + AUXBAG + "> <" + AUXBAGPROP + "> ?u } WHERE { ?u a <"
			+ ARGTYPE + "> } ";
	protected static final String GETALLXTRA = "CONSTRUCT { <" + AUXBAG + "> <" + AUXBAGPROP
			+ "> ?u . ?u a ?t . } WHERE { ?u a <" + ARGTYPE + "> ; a ?t . } ";

	protected static final String GETONEOF = "DESCRIBE ?p WHERE {<" + ARG1 + "> <" + Profilable.PROP_HAS_PROFILE
			+ "> ?ap . ?ap <" + ARGTYPE + "> ?p}";
	protected static final String GETONEOFXTRA = "CONSTRUCT { ?p a ?t } WHERE {<" + ARG1 + "> <"
			+ Profilable.PROP_HAS_PROFILE + "> ?ap . ?ap <" + ARGTYPE + "> ?p . ?p a ?t}";

	protected static final String ADDTOPROFILABLE = "INSERT { ?p <" + ARGTYPE + "> <" + ARG2 + "> . " + ARGTURTLE
			+ " } WHERE {<" + ARG1 + "> <" + Profilable.PROP_HAS_PROFILE + "> ?p}";

	protected static final String ADDPROFTOPROFILABLE = "INSERT DATA { <" + ARG1 + "> <" + Profilable.PROP_HAS_PROFILE
			+ "> <" + ARG2 + "> . " + ARGTURTLE + " }";

	protected static final String GETPRFOFPROFILABLE = "DESCRIBE ?p WHERE {<" + ARG1 + "> <"
			+ Profilable.PROP_HAS_PROFILE + "> ?p}";
	protected static final String GETPRFOFPROFILABLEXTRA = "CONSTRUCT { ?p a ?t } WHERE {<" + ARG1 + "> <"
			+ Profilable.PROP_HAS_PROFILE + "> ?p . ?p a ?t}";

}
