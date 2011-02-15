/*
	Copyright 2008-2010 Fraunhofer IGD, http://www.igd.fraunhofer.de
	Fraunhofer-Gesellschaft - Institute of Computer Graphics Research
	
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
/**
 * This is a modification of org.persona.middleware.context.ContextEvent
 * redefined as extension of ManagedIdividual so it can be registered and used
 * in a service definition and call (specially, or solely, for the context history).
 * It allows for defining all of its properties as null (after construction), 
 * for match wildcarding.
 */
package org.universAAL.context.che.ontology;

import org.universAAL.middleware.context.owl.ContextProvider;
import org.universAAL.middleware.owl.ManagedIndividual;
import org.universAAL.middleware.owl.supply.Rating;
import org.universAAL.middleware.rdf.Resource;


/**
 * @author <a href="mailto:alfiva@itaca.upv.es">Alvaro Fides Valero</a>
 * @author <a href="mailto:Saied.Tazari@igd.fraunhofer.de">Saied Tazari</a>
 * 
 *         Helper class that replicates
 *         {@link org.persona.middleware.context.ContextEvent} as a
 *         ManagedIndividual with the possibility of not defining some of its
 *         properties.
 *         <p>
 *         This is the class that must be used by service callers that wish to
 *         call CHe services, instead of the middleware one (
 *         {@link org.persona.middleware.context.ContextEvent}). The events
 *         received in a service response will have to be casted to
 *         {@link org.persona.middleware.context.ContextEvent} however, thus
 *         performing the conversion from
 *         <code>org.persona.platform.casf.che.ontology.ContextEvent</code> to
 *         {@link org.persona.middleware.context.ContextEvent} in the casting.
 * 
 */
public class ContextEvent extends ManagedIndividual {
	public static final String PERSONA_CONTEXT_NAMESPACE;
	public static final String MY_URI;
	public static final String CONTEXT_EVENT_URI_PREFIX ;
	public static final String LOCAL_NAME_SUBJECT;
	public static final String PROP_RDF_SUBJECT;
	public static final String LOCAL_NAME_PREDICATE;
	public static final String PROP_RDF_PREDICATE;
	public static final String LOCAL_NAME_OBJECT;
	public static final String PROP_RDF_OBJECT;
	public static final String LOCAL_NAME_CONFIDENCE;
	public static final String PROP_CONTEXT_CONFIDENCE;
	public static final String LOCAL_NAME_ACCURACY;
	public static final String PROP_CONTEXT_ACCURACY;
	public static final String LOCAL_NAME_PROVIDER;
	public static final String PROP_CONTEXT_PROVIDER;
	public static final String LOCAL_NAME_EXPIRATION_TIME;
	public static final String PROP_CONTEXT_EXPIRATION_TIME;
	public static final String LOCAL_NAME_TIMESTAMP;
	public static final String PROP_CONTEXT_TIMESTAMP;
	
	static{
		PERSONA_CONTEXT_NAMESPACE = uAAL_NAMESPACE_PREFIX + "Context.owl#";
		MY_URI = PERSONA_CONTEXT_NAMESPACE + "ContextEvent";
		CONTEXT_EVENT_URI_PREFIX = "urn:org.persona.ontology:ContextEvent#";
		LOCAL_NAME_SUBJECT = "subject";
		PROP_RDF_SUBJECT = RDF_NAMESPACE + LOCAL_NAME_SUBJECT;
		LOCAL_NAME_PREDICATE = "predicate";
		PROP_RDF_PREDICATE = RDF_NAMESPACE + LOCAL_NAME_PREDICATE;
		LOCAL_NAME_OBJECT = "object";
		PROP_RDF_OBJECT = RDF_NAMESPACE + LOCAL_NAME_OBJECT;
		LOCAL_NAME_CONFIDENCE = "hasConfidence";
		PROP_CONTEXT_CONFIDENCE = 
			PERSONA_CONTEXT_NAMESPACE + LOCAL_NAME_CONFIDENCE;
		LOCAL_NAME_ACCURACY = "hasAccuracy";
		PROP_CONTEXT_ACCURACY = 
			PERSONA_CONTEXT_NAMESPACE + LOCAL_NAME_ACCURACY;
		LOCAL_NAME_PROVIDER = "hasProvider";
		PROP_CONTEXT_PROVIDER = PERSONA_CONTEXT_NAMESPACE + LOCAL_NAME_PROVIDER;
		LOCAL_NAME_EXPIRATION_TIME = "hasExpirationTime";
		PROP_CONTEXT_EXPIRATION_TIME = 
			PERSONA_CONTEXT_NAMESPACE + LOCAL_NAME_EXPIRATION_TIME;
		LOCAL_NAME_TIMESTAMP = "hasTimestamp";
		PROP_CONTEXT_TIMESTAMP = 
			PERSONA_CONTEXT_NAMESPACE + LOCAL_NAME_TIMESTAMP;
		register(ContextEvent.class);
	}
	
	public static ContextEvent constructSimpleEvent(String subjectURI, String subjectTypeURI, String predicate, Object object) {
		if (subjectURI == null  ||  subjectTypeURI == null  ||  predicate == null  ||  object == null)
			return null;
		
		Resource subject = null;
		if (ManagedIndividual.isRegisteredClassURI(subjectTypeURI))
			subject = ManagedIndividual.getInstance(subjectTypeURI, subjectURI);
		else {
			subject = new Resource(subjectURI);
			subject.addType(subjectTypeURI, false);
		}
		subject.setProperty(predicate, object);
		
		return new ContextEvent(subject, predicate);
	}
	
	public ContextEvent(){
		
	}
	
	/**
	 * This constructor is NOT for the exclusive usage by deserializers. Not
	 * anymore!! You can construct one of these ContextEvents without
	 * properties, only with a URI. Or without it.
	 */
	public ContextEvent(String uri) {
		super(uri);
		if (!uri.startsWith(CONTEXT_EVENT_URI_PREFIX))
			throw new RuntimeException("Invalid instance URI!");
		addType(MY_URI, true);
	}
	
	public ContextEvent(Resource subject, String predicate) {
		super(CONTEXT_EVENT_URI_PREFIX, 8);
		
		if (subject == null  ||  predicate == null)
			throw new RuntimeException("Invalid null value!");
		
		Object eventObject = subject.getProperty(predicate);
		if (eventObject == null)
			throw new RuntimeException("Event object not set!");

		addType(MY_URI, true);
		setRDFSubject(subject);
		setRDFPredicate(predicate);
		setRDFObject(eventObject);
		setTimestamp(new Long(System.currentTimeMillis()));
	}

	public Rating getAccuracy() {
		return (Rating) getProperty(PROP_CONTEXT_ACCURACY);
	}

	public Integer getConfidence() {
		return (Integer) getProperty(PROP_CONTEXT_CONFIDENCE);
	}

	public Long getExpirationTime() {
		return (Long) getProperty(PROP_CONTEXT_EXPIRATION_TIME);
	}
	
	public int getPropSerializationType(String propURI) {
		return (PROP_RDF_SUBJECT.equals(propURI)|| PROP_CONTEXT_PROVIDER.equals(propURI))?
				PROP_SERIALIZATION_REDUCED : PROP_SERIALIZATION_FULL;
	}

	public Object getRDFObject() {
		return getProperty(PROP_RDF_OBJECT);
	}

	public String getRDFPredicate() {
		Object o = getProperty(PROP_RDF_PREDICATE);
		return (o instanceof Resource)? o.toString() : null;
	}
	
	public ContextProvider getProvider() {
		return (ContextProvider) props.get(PROP_CONTEXT_PROVIDER);
	}
	
	public Resource getRDFSubject() {
		return (Resource) getProperty(PROP_RDF_SUBJECT);
	}

	public String getSubjectTypeURI() {
		Resource subject = (Resource) getProperty(PROP_RDF_SUBJECT);
		return (subject == null)? null : subject.getType();
	}

	public String getSubjectURI() {
		Resource subject = (Resource) getProperty(PROP_RDF_SUBJECT);
		return (subject == null)? null : subject.getURI();
	}

	public Long getTimestamp() {
		return (Long) getProperty(PROP_CONTEXT_TIMESTAMP);
	}

	public boolean isWellFormed() {
		return (getRDFSubject() != null && getRDFPredicate() != null
				&& getRDFObject() != null && getTimestamp() != null);
	}

	public void setAccuracy(Rating accuracy) {
		if (accuracy==null){ props.remove(PROP_CONTEXT_ACCURACY);return;}
		if (accuracy != null  &&  !props.containsKey(PROP_CONTEXT_ACCURACY))
			props.put(PROP_CONTEXT_ACCURACY, accuracy);
	}

	public void setConfidence(Integer confidence) {
		if (confidence==null){ props.remove(PROP_CONTEXT_CONFIDENCE);return;}
		if (confidence != null  &&  confidence.intValue() >= 0
				&& confidence.intValue() <= 100
				&& !props.containsKey(PROP_CONTEXT_CONFIDENCE))
			props.put(PROP_CONTEXT_CONFIDENCE, confidence);
	}

	public void setExpirationTime(Long expirationTime) {
		if (expirationTime==null){ props.remove(PROP_CONTEXT_EXPIRATION_TIME);return;}
		if (expirationTime != null  &&  expirationTime.longValue() > 0
				&&  !props.containsKey(PROP_CONTEXT_EXPIRATION_TIME))
			props.put(PROP_CONTEXT_EXPIRATION_TIME, expirationTime);
	}
	
	public void setRDFObject(Object o) {
		if (o==null){ props.remove(PROP_RDF_OBJECT);return;}
		if (o != null  &&  !props.containsKey(PROP_RDF_OBJECT))
			props.put(PROP_RDF_OBJECT, o);
	}
	
	public void setRDFPredicate(String propURI) {
		if (propURI==null){ props.remove(PROP_RDF_PREDICATE);return;}
		if (propURI != null  &&  propURI.lastIndexOf('#') > 0
				&&  !props.containsKey(PROP_RDF_PREDICATE))
			props.put(PROP_RDF_PREDICATE, new Resource(propURI));
	}
	
	public void setProvider(ContextProvider src) {
		if (src==null){ props.remove(PROP_CONTEXT_PROVIDER);return;}
		if (src != null  &&  !props.containsKey(PROP_CONTEXT_PROVIDER))
			props.put(PROP_CONTEXT_PROVIDER, src);
	}
	
	public void setRDFSubject(Resource subj) {
		if (subj==null){ props.remove(PROP_RDF_SUBJECT);return;}
		if (subj != null  &&  !props.containsKey(PROP_RDF_SUBJECT))
			props.put(PROP_RDF_SUBJECT, subj);
	}

	public void setTimestamp(Long timestamp) {
		if (timestamp==null){ props.remove(PROP_CONTEXT_TIMESTAMP);return;}
		if (timestamp != null  &&  timestamp.longValue() > 0
				&&  !props.containsKey(PROP_CONTEXT_TIMESTAMP))
			props.put(PROP_CONTEXT_TIMESTAMP, timestamp);
	}
	
	public void setProperty(String propURI, Object value) {
		if (propURI == null)
			return;
		
		if (propURI.equals(PROP_RDF_OBJECT))
			setRDFObject(value);
		else if (value instanceof Rating) {
			if (propURI.equals(PROP_CONTEXT_ACCURACY))
				setAccuracy((Rating) value);
		} else if (value instanceof ContextProvider) {
			if (propURI.equals(PROP_CONTEXT_PROVIDER))
				setProvider((ContextProvider) value);
		} else if (value instanceof Resource) {
			if (propURI.equals(PROP_RDF_SUBJECT))
				setRDFSubject((Resource) value);
			else if (propURI.equals(PROP_RDF_PREDICATE))
				setRDFPredicate(((Resource) value).getURI());
		} else if (value instanceof String) {
			if (propURI.equals(PROP_RDF_PREDICATE))
				setRDFPredicate((String) value);
		} else if (value instanceof Long) {
			if (propURI.equals(PROP_CONTEXT_TIMESTAMP))
				setTimestamp((Long) value);
			else if (propURI.equals(PROP_CONTEXT_EXPIRATION_TIME))
				setExpirationTime((Long) value);
		} else if (value instanceof Integer) {
			if (propURI.equals(PROP_CONTEXT_CONFIDENCE))
				setConfidence((Integer) value);
		}
	}
}
