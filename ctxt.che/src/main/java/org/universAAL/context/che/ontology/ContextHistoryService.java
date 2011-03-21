/*
	Copyright 2008-2010 ITACA-TSB, http://www.tsb.upv.es
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
package org.universAAL.context.che.ontology;

import java.util.Hashtable;

import javax.xml.datatype.Duration;

import org.universAAL.middleware.owl.Restriction;
import org.universAAL.middleware.rdf.TypeMapper;
import org.universAAL.middleware.service.owl.Service;

/**
 * The ontological description of the Context History related services. This
 * class can be used by other components that wish to implement and provide
 * their own Context History storage services, such as short term history
 * storage
 * 
 * @author <a href="mailto:alfiva@itaca.upv.es">Alvaro Fides Valero</a>
 * 
 */
public class ContextHistoryService extends Service {
    public static final String MY_URI;
    public static final String PROP_MANAGES;
    public static final String PROP_PROCESSES;
    public static final String PROP_RETURNS;
    public static final String PROP_TIMESTAMP_FROM;
    public static final String PROP_TIMESTAMP_TO;
    public static final String PROP_DURATION_FROM;
    public static final String PROP_DURATION_TO;
    public static Hashtable CHRestrictions = new Hashtable();
    static {
	MY_URI = "http://ontology.persona.tsb.itaca.es/ContextHistory.owl#ContextHistoryService";
	PROP_MANAGES = "http://ontology.persona.tsb.itaca.es/ContextHistory.owl#manages";
	PROP_PROCESSES = "http://ontology.persona.tsb.itaca.es/ContextHistory.owl#processes";
	PROP_RETURNS = "http://ontology.persona.tsb.itaca.es/ContextHistory.owl#returns";
	PROP_TIMESTAMP_FROM = "http://ontology.persona.tsb.itaca.es/ContextHistory.owl#timestampFrom";
	PROP_TIMESTAMP_TO = "http://ontology.persona.tsb.itaca.es/ContextHistory.owl#timestampTo";
	PROP_DURATION_FROM = "http://ontology.persona.tsb.itaca.es/ContextHistory.owl#durationFrom";
	PROP_DURATION_TO = "http://ontology.persona.tsb.itaca.es/ContextHistory.owl#durationTo";
	register(ContextHistoryService.class);
	addRestriction(Restriction.getAllValuesRestriction(PROP_MANAGES,
		ContextEvent.MY_URI), new String[] { PROP_MANAGES },
		CHRestrictions);
	addRestriction(Restriction.getAllValuesRestriction(PROP_PROCESSES,
		TypeMapper.getDatatypeURI(String.class)),
		new String[] { PROP_PROCESSES }, CHRestrictions);
	addRestriction(Restriction.getAllValuesRestriction(PROP_RETURNS,
		TypeMapper.getDatatypeURI(String.class)),
		new String[] { PROP_RETURNS }, CHRestrictions);
	addRestriction(Restriction.getAllValuesRestriction(PROP_TIMESTAMP_FROM,
		TypeMapper.getDatatypeURI(Long.class)),
		new String[] { PROP_TIMESTAMP_FROM }, CHRestrictions);
	addRestriction(Restriction.getAllValuesRestriction(PROP_TIMESTAMP_TO,
		TypeMapper.getDatatypeURI(Long.class)),
		new String[] { PROP_TIMESTAMP_TO }, CHRestrictions);
	addRestriction(Restriction.getAllValuesRestriction(PROP_DURATION_FROM,
		TypeMapper.getDatatypeURI(Duration.class)),
		new String[] { PROP_DURATION_FROM }, CHRestrictions);
	addRestriction(Restriction.getAllValuesRestriction(PROP_DURATION_TO,
		TypeMapper.getDatatypeURI(Duration.class)),
		new String[] { PROP_DURATION_TO }, CHRestrictions);
    }

    public static Restriction getClassRestrictionsOnProperty(String propURI) {
	if (propURI == null)
	    return null;
	Object r = CHRestrictions.get(propURI);
	if (r instanceof Restriction)
	    return (Restriction) r;
	return Service.getClassRestrictionsOnProperty(propURI);// WARNING. NOT
	// IN MW C.E.
    }

    public static String getRDFSComment() {
	return "The class of services for querying the Context History.";
    }

    public static String getRDFSLabel() {
	return "Context History";
    }

    public ContextHistoryService(String uri) {
	super(uri);
    }

    protected Hashtable getClassLevelRestrictions() {
	return CHRestrictions;
    }

    public int getPropSerializationType(String propURI) {
	return PROP_MANAGES.equals(propURI) || PROP_PROCESSES.equals(propURI)
		|| PROP_RETURNS.equals(propURI)
		|| PROP_TIMESTAMP_FROM.equals(propURI)
		|| PROP_TIMESTAMP_TO.equals(propURI)
		|| PROP_DURATION_FROM.equals(propURI)
		|| PROP_DURATION_TO.equals(propURI) ? PROP_SERIALIZATION_FULL
		: PROP_SERIALIZATION_OPTIONAL;
    }

    public boolean isWellFormed() {
	return true;
    }

}
