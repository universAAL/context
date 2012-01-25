/*
	Copyright 2008-2011 Fraunhofer IGD, http://www.igd.fraunhofer.de
	Fraunhofer-Gesellschaft - Institute of Computer Graphics Research
	
	Copyright 2008-2011 ITACA-TSB, http://www.tsb.upv.es
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
 * This is a modification of org.universAAL.middleware.context.ContextEvent
 * redefined as extension of ManagedIdividual so it can be registered and used
 * in a service definition and call (specially, or solely, for the context history).
 * It allows for defining all of its properties as null (after construction), 
 * for match wildcarding.
 */
package org.universAAL.context.che.ontology;

import org.universAAL.middleware.context.owl.ContextProvider;
import org.universAAL.middleware.owl.ManagedIndividual;
import org.universAAL.middleware.rdf.Resource;

/**
 * Helper class that replicates
 * {@link org.universAAL.middleware.context.ContextEvent} as a ManagedIndividual
 * with the possibility of not defining some of its properties.
 * <p/>
 * This is the class that must be used by service callers that wish to call CHe
 * services, instead of the middleware one (
 * {@link org.universAAL.middleware.context.ContextEvent}), in the
 * ServiceRequests they build (as input or outputs). Then, the output events
 * received in a service response will have to be casted to
 * {@link org.universAAL.middleware.context.ContextEvent}, thus performing the
 * conversion from
 * <code>org.universAAL.platform.casf.che.ontology.ContextEvent</code> to
 * {@link org.universAAL.middleware.context.ContextEvent} in the casting.
 * 
 * @author <a href="mailto:alfiva@itaca.upv.es">Alvaro Fides Valero</a>
 * @author <a href="mailto:Saied.Tazari@igd.fraunhofer.de">Saied Tazari</a>
 * 
 */
public class ContextEvent extends ManagedIndividual {
    public static final String uAAL_CONTEXT_NAMESPACE = uAAL_NAMESPACE_PREFIX
	    + "Context.owl#";
    public static final String MY_URI = uAAL_CONTEXT_NAMESPACE
	    + "ContextEventCHE";
    public static final String CONTEXT_EVENT_URI_PREFIX = "urn:org.universAAL.middleware.context.rdf:ContextEvent#";
    public static final String LOCAL_NAME_SUBJECT = "subject";
    public static final String PROP_RDF_SUBJECT = RDF_NAMESPACE
	    + LOCAL_NAME_SUBJECT;
    public static final String LOCAL_NAME_PREDICATE = "predicate";
    public static final String PROP_RDF_PREDICATE = RDF_NAMESPACE
	    + LOCAL_NAME_PREDICATE;
    public static final String LOCAL_NAME_OBJECT = "object";
    public static final String PROP_RDF_OBJECT = RDF_NAMESPACE
	    + LOCAL_NAME_OBJECT;
    public static final String LOCAL_NAME_CONFIDENCE = "hasConfidence";
    public static final String PROP_CONTEXT_CONFIDENCE = uAAL_CONTEXT_NAMESPACE
	    + LOCAL_NAME_CONFIDENCE;
    public static final String LOCAL_NAME_PROVIDER = "hasProvider";
    public static final String PROP_CONTEXT_PROVIDER = uAAL_CONTEXT_NAMESPACE
	    + LOCAL_NAME_PROVIDER;
    public static final String LOCAL_NAME_EXPIRATION_TIME = "hasExpirationTime";
    public static final String PROP_CONTEXT_EXPIRATION_TIME = uAAL_CONTEXT_NAMESPACE
	    + LOCAL_NAME_EXPIRATION_TIME;
    public static final String LOCAL_NAME_TIMESTAMP = "hasTimestamp";
    public static final String PROP_CONTEXT_TIMESTAMP = uAAL_CONTEXT_NAMESPACE
	    + LOCAL_NAME_TIMESTAMP;

    /*
     * static { uAAL_CONTEXT_NAMESPACE = uAAL_NAMESPACE_PREFIX + "Context.owl#";
     * MY_URI = uAAL_CONTEXT_NAMESPACE + "ContextEventCHE";
     * CONTEXT_EVENT_URI_PREFIX =
     * "urn:org.universAAL.middleware.context.rdf:ContextEvent#";
     * LOCAL_NAME_SUBJECT = "subject"; PROP_RDF_SUBJECT = RDF_NAMESPACE +
     * LOCAL_NAME_SUBJECT; LOCAL_NAME_PREDICATE = "predicate";
     * PROP_RDF_PREDICATE = RDF_NAMESPACE + LOCAL_NAME_PREDICATE;
     * LOCAL_NAME_OBJECT = "object"; PROP_RDF_OBJECT = RDF_NAMESPACE +
     * LOCAL_NAME_OBJECT; LOCAL_NAME_CONFIDENCE = "hasConfidence";
     * PROP_CONTEXT_CONFIDENCE = uAAL_CONTEXT_NAMESPACE + LOCAL_NAME_CONFIDENCE;
     * LOCAL_NAME_PROVIDER = "hasProvider"; PROP_CONTEXT_PROVIDER =
     * uAAL_CONTEXT_NAMESPACE + LOCAL_NAME_PROVIDER; LOCAL_NAME_EXPIRATION_TIME
     * = "hasExpirationTime"; PROP_CONTEXT_EXPIRATION_TIME =
     * uAAL_CONTEXT_NAMESPACE + LOCAL_NAME_EXPIRATION_TIME; LOCAL_NAME_TIMESTAMP
     * = "hasTimestamp"; PROP_CONTEXT_TIMESTAMP = uAAL_CONTEXT_NAMESPACE +
     * LOCAL_NAME_TIMESTAMP; register(ContextEvent.class); }
     */

    /**
     * Constructs a CHe stub ContextEvent according to the parameters passed
     * 
     * @param subjectURI
     *            URI of the subject. Must not be null.
     * @param subjectTypeURI
     *            URI of the subject type. Must not be null.
     * @param predicate
     *            URI of the predicate. Must not be null.
     * @param object
     *            The object of the event. Must not be null.
     * @return
     */
    public static ContextEvent constructSimpleEvent(String subjectURI,
	    String subjectTypeURI, String predicate, Object object) {
	if (subjectURI == null || subjectTypeURI == null || predicate == null
		|| object == null)
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

    /**
     * Empty constructor, needed because this is a ManagedIndividual
     */
    public ContextEvent() {
	super();
    }

    /**
     * This constructor is NOT for the exclusive usage by deserializers. Not
     * anymore!! You can construct one of these ContextEvents without
     * properties, only with a URI. Or without it. Because this is a
     * ManagedIndividual
     */
    public ContextEvent(String uri) {
	super(uri);
	if (!uri.startsWith(CONTEXT_EVENT_URI_PREFIX))
	    throw new RuntimeException("Invalid instance URI!");
	addType(MY_URI, true);
    }

    /**
     * Construct a CHe stub ContextEvent inferring the object from the predicate
     * which URI is present in the propeorties of the subject
     * 
     * @param subject
     *            The Resource representing the subject of the event. Must
     *            include the property specified in the second parameter, and
     *            must have a certain value
     * @param predicate
     *            The property of the subject that will be used as object in the
     *            event
     */
    public ContextEvent(Resource subject, String predicate) {
	super(CONTEXT_EVENT_URI_PREFIX, 8);

	if (subject == null || predicate == null)
	    throw new RuntimeException("Invalid null value!");

	Object eventObject = subject.getProperty(predicate);
	if (eventObject == null)
	    throw new RuntimeException("Event object not set!");

	addType(MY_URI, true);
	setRDFSubject(subject);
	setRDFPredicate(predicate);
	setRDFObject(eventObject);
	// setTimestamp(new Long(System.currentTimeMillis()))
	// The timestamp can in this case be unset for wildcarding, and set
	// later
    }

    /**
     * Get the confidence of the event
     * 
     * @return The confidence represented as a percentage (0 to 100)
     */
    public Integer getConfidence() {
	return (Integer) getProperty(PROP_CONTEXT_CONFIDENCE);
    }

    /**
     * Get the expiration time
     * 
     * @return The amount of milliseconds after reception from which the
     *         information in the event is no longer valid
     */
    public Long getExpirationTime() {
	return (Long) getProperty(PROP_CONTEXT_EXPIRATION_TIME);
    }

    public int getPropSerializationType(String propURI) {
	return (PROP_RDF_SUBJECT.equals(propURI) || PROP_CONTEXT_PROVIDER
		.equals(propURI)) ? PROP_SERIALIZATION_REDUCED
		: PROP_SERIALIZATION_FULL;
    }

    /**
     * Get the object of the event
     * 
     * @return The object of the event (a Resource)
     */
    public Object getRDFObject() {
	return getProperty(PROP_RDF_OBJECT);
    }

    /**
     * Get the predicate of the event
     * 
     * @return The URI of the predicate of the event
     */
    public String getRDFPredicate() {
	Object o = getProperty(PROP_RDF_PREDICATE);
	return (o instanceof Resource) ? o.toString() : null;
    }

    /**
     * Get the ContextProvider of the event
     * 
     * @return The {@link org.universAAL.middleware.context.owl.ContextProvider}
     *         representing the provider that originated the event
     */
    public ContextProvider getProvider() {
	return (ContextProvider) props.get(PROP_CONTEXT_PROVIDER);
    }

    /**
     * Get the subject of the event
     * 
     * @return The {@link org.universAAL.middleware.rdf.Resource} that is the
     *         subject to the event
     */
    public Resource getRDFSubject() {
	return (Resource) getProperty(PROP_RDF_SUBJECT);
    }

    /**
     * Get the type of the subject of the event
     * 
     * @return The URI of the type of the subject to the event
     */
    public String getSubjectTypeURI() {
	Resource subject = (Resource) getProperty(PROP_RDF_SUBJECT);
	return (subject == null) ? null : subject.getType();
    }

    /**
     * Get the URI of the subject of the event
     * 
     * @return The URI of the individual that is the subject to the event
     */
    public String getSubjectURI() {
	Resource subject = (Resource) getProperty(PROP_RDF_SUBJECT);
	return (subject == null) ? null : subject.getURI();
    }

    /**
     * Get the timestamp of the event
     * 
     * @return The timestamp, in UNIX format, associated to the event
     */
    public Long getTimestamp() {
	return (Long) getProperty(PROP_CONTEXT_TIMESTAMP);
    }

    public boolean isWellFormed() {
	return (getRDFSubject() != null && getRDFPredicate() != null && getRDFObject() != null);
	// We do not evaluate timestamp in this case
    }

    /**
     * Set the confidence
     * 
     * @param confidence
     *            The confidence in percentage (0 to 100)
     */
    public void setConfidence(Integer confidence) {
	if (confidence == null) {
	    props.remove(PROP_CONTEXT_CONFIDENCE);
	    return;
	}
	if (confidence != null && confidence.intValue() >= 0
		&& confidence.intValue() <= 100
		&& !props.containsKey(PROP_CONTEXT_CONFIDENCE))
	    props.put(PROP_CONTEXT_CONFIDENCE, confidence);
    }

    /**
     * Set the expiration time
     * 
     * @param expirationTime
     *            The amount of millisecond after which the event is not valid
     *            afer reception
     */
    public void setExpirationTime(Long expirationTime) {
	if (expirationTime == null) {
	    props.remove(PROP_CONTEXT_EXPIRATION_TIME);
	    return;
	}
	if (expirationTime != null && expirationTime.longValue() > 0
		&& !props.containsKey(PROP_CONTEXT_EXPIRATION_TIME))
	    props.put(PROP_CONTEXT_EXPIRATION_TIME, expirationTime);
    }

    /**
     * Set the object
     * 
     * @param o
     */
    public void setRDFObject(Object o) {
	if (o == null) {
	    props.remove(PROP_RDF_OBJECT);
	    return;
	}
	if (o != null && !props.containsKey(PROP_RDF_OBJECT))
	    props.put(PROP_RDF_OBJECT, o);
    }

    /**
     * Set the predicate
     * 
     * @param propURI
     *            The URI of the predicate
     */
    public void setRDFPredicate(String propURI) {
	if (propURI == null) {
	    props.remove(PROP_RDF_PREDICATE);
	    return;
	}
	if (propURI != null && propURI.lastIndexOf('#') > 0
		&& !props.containsKey(PROP_RDF_PREDICATE))
	    props.put(PROP_RDF_PREDICATE, new Resource(propURI));
    }

    /**
     * Set the Context Provider
     * 
     * @param src
     */
    public void setProvider(ContextProvider src) {
	if (src == null) {
	    props.remove(PROP_CONTEXT_PROVIDER);
	    return;
	}
	if (src != null && !props.containsKey(PROP_CONTEXT_PROVIDER))
	    props.put(PROP_CONTEXT_PROVIDER, src);
    }

    /**
     * Set the subject
     * 
     * @param subj
     */
    public void setRDFSubject(Resource subj) {
	if (subj == null) {
	    props.remove(PROP_RDF_SUBJECT);
	    return;
	}
	if (subj != null && !props.containsKey(PROP_RDF_SUBJECT))
	    props.put(PROP_RDF_SUBJECT, subj);
    }

    /**
     * Set the timestamp
     * 
     * @param timestamp
     *            The timestamp in UNIX format
     */
    public void setTimestamp(Long timestamp) {
	if (timestamp == null) {
	    props.remove(PROP_CONTEXT_TIMESTAMP);
	    return;
	} else if (timestamp.longValue() > 0
		&& !props.containsKey(PROP_CONTEXT_TIMESTAMP))
	    props.put(PROP_CONTEXT_TIMESTAMP, timestamp);
    }

    public void setProperty(String propURI, Object value) {
	if (propURI == null)
	    return;

	if (propURI.equals(PROP_RDF_OBJECT))
	    setRDFObject(value);
	else if (value instanceof ContextProvider) {
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

    public String getClassURI() {
	return MY_URI;
    }
}
