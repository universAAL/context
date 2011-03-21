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
package org.universAAL.context.che.database;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import javax.xml.XMLConstants;

import org.universAAL.context.conversion.jena.JenaConverter;
import org.universAAL.middleware.context.ContextEvent;
import org.universAAL.middleware.context.ContextEventPattern;
import org.universAAL.middleware.input.InputEvent;
import org.universAAL.middleware.io.rdf.ChoiceItem;
import org.universAAL.middleware.io.rdf.ChoiceList;
import org.universAAL.middleware.io.rdf.Form;
import org.universAAL.middleware.io.rdf.Group;
import org.universAAL.middleware.io.rdf.InputField;
import org.universAAL.middleware.io.rdf.Label;
import org.universAAL.middleware.io.rdf.MediaObject;
import org.universAAL.middleware.io.rdf.Range;
import org.universAAL.middleware.io.rdf.Select;
import org.universAAL.middleware.io.rdf.Select1;
import org.universAAL.middleware.io.rdf.SimpleOutput;
import org.universAAL.middleware.io.rdf.SubdialogTrigger;
import org.universAAL.middleware.io.rdf.Submit;
import org.universAAL.middleware.io.rdf.TextArea;
import org.universAAL.middleware.output.OutputEvent;
import org.universAAL.middleware.output.OutputEventPattern;
import org.universAAL.middleware.owl.ClassExpression;
import org.universAAL.middleware.owl.ManagedIndividual;
import org.universAAL.middleware.rdf.PropertyPath;
import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.rdf.TypeMapper;
import org.universAAL.middleware.service.AggregatingFilter;
import org.universAAL.middleware.service.ServiceCall;
import org.universAAL.middleware.service.ServiceRequest;
import org.universAAL.middleware.service.ServiceResponse; //import org.universAAL.middleware.service.impl.ServiceRealization;//Not exported by bus.service
import org.universAAL.middleware.service.owls.profile.ServiceProfile;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.datatypes.xsd.impl.XMLLiteralType;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFList;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;
import com.hp.hpl.jena.vocabulary.XSD;

/**
 * Implementation of {@link org.persona.conversion.jena.ModelConverter} tailored
 * specifically for CHe. It avoids to check the well-formedness of PResources
 * due to the lack of this check when events are stored. The method
 * <code>updateDBResource</code> is empty and always returns false, since this
 * converter is not used for accessing the database. In addition, modification
 * of the history would not be allowed. What´s more: this converter is not
 * published to the OSGi platform, and therefore is not available for any other
 * bundle.
 * 
 * @author <a href="mailto:alfiva@itaca.upv.es">Alvaro Fides Valero</a>
 * 
 */
public class Converter implements JenaConverter {

    private TypeMapper tm;

    private RDFNode addDescription(Model m, Resource pr, boolean reduced,
	    Hashtable resources) {
	Object o = resources.get(pr);
	if (o instanceof com.hp.hpl.jena.rdf.model.Resource)
	    return (com.hp.hpl.jena.rdf.model.Resource) o;

	com.hp.hpl.jena.rdf.model.Resource jr = pr.isAnon() ? m
		.createResource() : m.createResource(pr.getURI());
	resources.put(pr, jr);
	for (Enumeration e = pr.getPropertyURIs(); e.hasMoreElements();) {
	    String propURI = e.nextElement().toString();
	    int reductionType = RDF.type.getURI().equals(propURI) ? Resource.PROP_SERIALIZATION_REDUCED
		    : pr.getPropSerializationType(propURI);
	    if (reductionType == Resource.PROP_SERIALIZATION_UNDEFINED)
		// a reduced unmanaged resource should be serialized only by
		// its URI skipping all properties
		reductionType = reduced ? Resource.PROP_SERIALIZATION_OPTIONAL
			: Resource.PROP_SERIALIZATION_FULL;
	    if (reduced
		    && reductionType == Resource.PROP_SERIALIZATION_OPTIONAL)
		continue;
	    Property prop = m.createProperty(propURI);
	    o = pr.getProperty(propURI);
	    if (o instanceof List)
		if (pr.isClosedCollection(propURI)) {
		    List l = new ArrayList(((List) o).size());
		    for (Iterator i = ((List) o).iterator(); i.hasNext();)
			l
				.add(getRDFNode(
					m,
					i.next(),
					(reductionType == Resource.PROP_SERIALIZATION_REDUCED),
					resources));
		    jr.addProperty(prop, m.createList(l.iterator()));
		} else
		    for (Iterator i = ((List) o).iterator(); i.hasNext();)
			jr
				.addProperty(
					prop,
					getRDFNode(
						m,
						i.next(),
						(reductionType == Resource.PROP_SERIALIZATION_REDUCED),
						resources));
	    else
		jr.addProperty(prop, getRDFNode(m, o,
			(reductionType == Resource.PROP_SERIALIZATION_REDUCED),
			resources));
	}
	return jr;
    }

    /**
     * Deserializes a complex object that is not an XML literal, that is, a
     * {@link org.universAAL.middleware.rdf.Resource} root element.
     * 
     * @param serialized
     *            The serialized object in RDF
     * @return The deserialized <code>Object</code>. Returns <code>null</code>
     *         if there was an error.
     */
    public Object deserialize(String serialized) {
	return deserialize(serialized, false);
    }

    private Resource deserialize(String serialized, boolean wasXMLLiteral) {
	if (serialized == null)
	    return null;

	try {
	    Model m = ModelFactory.createDefaultModel();
	    m.read(new StringReader(serialized), null);
	    return getResource(getJenaRootResource(m), new Hashtable(),
		    wasXMLLiteral);
	} catch (Exception e) {
	    e.printStackTrace();
	}

	return null;
    }

    private ClassExpression getClassExpression(
	    com.hp.hpl.jena.rdf.model.Resource r, String uri) {
	ClassExpression result = null;
	for (StmtIterator i = r.listProperties(RDFS.subClassOf); i.hasNext();) {
	    com.hp.hpl.jena.rdf.model.Resource superclass = i.nextStatement()
		    .getResource();
	    if (!superclass.isAnon()) {
		result = ClassExpression.getClassExpressionInstance(superclass
			.getURI(), null, uri);
		if (result != null)
		    return result;
	    }
	}
	int num = 0;
	for (StmtIterator i = r.listProperties(); i.hasNext();) {
	    Property p = i.nextStatement().getPredicate();
	    if (!RDF.type.equals(p)) {
		result = ClassExpression.getClassExpressionInstance(null, p
			.getURI(), uri);
		num++;
	    }
	    if (result != null)
		return result;
	}
	return (r.isAnon() || num > 0) ? null : ClassExpression
		.getClassExpressionInstance(null, null, uri);
    }

    private ManagedIndividual getIndividual(
	    com.hp.hpl.jena.rdf.model.Resource r, String classURI,
	    Hashtable resources) {
	String uri = r.isAnon() ? r.getId().getLabelString() : r.getURI();
	ManagedIndividual result = ManagedIndividual.getInstance(classURI, (r
		.isAnon() ? null : uri));
	resources.put(uri, result);
	return (processProperties(result, r, resources) /*
							 * && result
							 * .isWellFormed()
							 */) ? result : null;
    }

    public com.hp.hpl.jena.rdf.model.Resource getJenaRootResource(Model m) {
	for (ResIterator ri = m.listSubjects(); ri.hasNext();) {
	    com.hp.hpl.jena.rdf.model.Resource r = ri.nextResource();
	    // check if it is a root node
	    if (!m.contains(null, null, r))
		return r;
	}
	return null;
    }

    private Resource getResource(com.hp.hpl.jena.rdf.model.Resource r,
	    Hashtable resources, boolean wasXMLLiteral) {
	Statement st = r.getProperty(RDF.type);
	if (st == null)
	    return getUnmanagedResource(r, resources, wasXMLLiteral);

	String classURI = st.getResource().getURI();
	String uri = r.isAnon() ? null : r.getURI();
	Resource pr = getResourceInstance(classURI, uri);
	if (pr == null) {
	    if (ManagedIndividual.isRegisteredClassURI(classURI))
		return getIndividual(r, classURI, resources);

	    pr = ClassExpression.getClassExpressionInstance(classURI, uri);

	    if (pr == null) {
		if (ClassExpression.OWL_CLASS.equals(classURI))
		    pr = getClassExpression(r, uri);
		if (pr == null)
		    return getUnmanagedResource(r, resources, wasXMLLiteral);
	    }
	}

	resources.put((uri == null ? r.getId().getLabelString() : uri), pr);
	return (processProperties(pr, r, resources) /* && pr.isWellFormed() */) ? pr
		: null;
    }

    private RDFNode getRDFNode(Model m, Object o, boolean reduced,
	    Hashtable resources) {
	if (o instanceof Resource) {
	    Resource p = (Resource) o;
	    if (p.serializesAsXMLLiteral())
		if (p.isAnon() || p.numberOfProperties() > 0)
		    return m.createTypedLiteral(serialize(p),
			    XMLLiteralType.theXMLLiteralType);
		else
		    return m.createTypedLiteral(p.getURI(),
			    XSDDatatype.XSDanyURI);
	    else
		return addDescription(m, (Resource) o, reduced, resources);
	} else {
	    if (tm != null) {
		String par[] = tm.getXMLInstance(o);
		return m.createTypedLiteral(par[0], new XSDDatatype(
			par[1].substring(XMLConstants.W3C_XML_SCHEMA_NS_URI
				.length() + 1)));
	    }
	}

	return null;
    }

    private Resource getUnmanagedResource(com.hp.hpl.jena.rdf.model.Resource r,
	    Hashtable resources, boolean wasXMLLiteral) {
	String uri = r.isAnon() ? r.getId().getLabelString() : r.getURI();
	Resource result = (r.isAnon() ? new Resource(wasXMLLiteral)
		: new Resource(uri));
	resources.put(uri, result);
	return processProperties(result, r, resources) ? result : null;
    }

    private boolean handleObjectValue(RDFNode n, List l, Hashtable resources) {
	if (n.isLiteral()) {
	    Object o = null;
	    Literal literal = (Literal) n;
	    if (XMLLiteralType.theXMLLiteralType.equals(literal.getDatatype()))
		o = deserialize(literal.getLexicalForm(), true);
	    else if (XSD.anyURI.getURI().equals(literal.getDatatypeURI()))
		o = new Resource(literal.getLexicalForm(), true);
	    if (o == null) {
		// o = ((Literal) n).getValue();
		o = tm.getJavaInstance(literal.getLexicalForm(), literal
			.getDatatypeURI());
	    }
	    l.add(o);
	} else {
	    String uri = ((com.hp.hpl.jena.rdf.model.Resource) n).isAnon() ? ((com.hp.hpl.jena.rdf.model.Resource) n)
		    .getId().getLabelString()
		    : ((com.hp.hpl.jena.rdf.model.Resource) n).getURI();
	    Object o = resources.get(uri);
	    if (o instanceof Resource)
		l.add(o);
	    else if (n.canAs(RDFList.class)) {
		for (Iterator i = ((RDFList) n.as(RDFList.class)).iterator(); i
			.hasNext();)
		    if (!handleObjectValue((RDFNode) i.next(), l, resources))
			return false;
	    } else {
		o = getResource(((com.hp.hpl.jena.rdf.model.Resource) n),
			resources, false);
		if (o instanceof Resource)
		    l.add(o);
		else
		    return false;
	    }
	}
	return true;
    }

    private boolean processProperties(Resource pr,
	    com.hp.hpl.jena.rdf.model.Resource jr, Hashtable resources) {
	for (StmtIterator i1 = jr.listProperties(); i1.hasNext();) {
	    Property p = i1.nextStatement().getPredicate();
	    ArrayList l = new ArrayList();
	    for (StmtIterator i2 = jr.listProperties(p); i2.hasNext();)
		handleObjectValue(i2.nextStatement().getObject(), l, resources);
	    switch (l.size()) {
	    case 0:
		break;
	    case 1:
		pr.setProperty(p.getURI(), l.get(0));
		break;
	    default:
		pr.setProperty(p.getURI(), l);
		break;
	    }
	}
	return true;
    }

    /**
     * Serializes an object into RDF format. The object to serialize must be a
     * {@link org.universAAL.middleware.rdf.Resource}
     * 
     * @param messageContent
     *            The object to serialize
     * @return The <String> representing the serialized object. Returns
     *         <code>null</code> if could not serialize or the argument is not a
     *         {@link org.universAAL.middleware.rdf.Resource}
     */
    public String serialize(Object messageContent) {
	if (messageContent instanceof Resource) {
	    Model m = toJenaResource((Resource) messageContent).getModel();
	    StringWriter sw = new StringWriter(4096);
	    // sw.write("<?xml version='1.0'?>\r\n");
	    m.write(sw, "RDF/XML-ABBREV");
	    return sw.toString();
	}

	return null;
    }

    /**
     * Set the TypeMapper to use for resolving literals.
     * 
     * @param tm
     *            The {@link org.universAAL.middleware.rdf.TypeMapper} to use
     */
    public void setTypeMapper(TypeMapper tm) {
	this.tm = tm;
    }

    public com.hp.hpl.jena.rdf.model.Resource toJenaResource(Resource r) {
	Model m = ModelFactory.createDefaultModel();
	m.setNsPrefix("", Resource.uAAL_VOCABULARY_NAMESPACE);
	Hashtable temp = new Hashtable();
	addDescription(m, r, false, temp);
	return (com.hp.hpl.jena.rdf.model.Resource) temp.get(r);
    }

    public Resource toPersonaResource(com.hp.hpl.jena.rdf.model.Resource r) {
	return getResource(r, new Hashtable(), false);
    }

    public boolean updateDBResource(com.hp.hpl.jena.rdf.model.Resource dbRes,
	    com.hp.hpl.jena.rdf.model.Resource updater) {
	// No updates are allowed within the context history
	return false;
    }

    /**
     * Helper method that returns an instance of a
     * {@link org.universAAL.middleware.rdf.Resource} given its class and
     * individual URis
     * 
     * @param classURI
     *            URI of the class of the
     *            {@link org.universAAL.middleware.rdf.Resource} to get an
     *            instance of
     * @param instanceURI
     *            URI of the individual of the
     *            {@link org.universAAL.middleware.rdf.Resource} to get an
     *            instance of
     * @return The desired {@link org.universAAL.middleware.rdf.Resource}
     *         instance, or <code>null</code> if the instance could not be
     *         created
     */
    public static Resource getResourceInstance(String classURI,
	    String instanceURI) {
	Hashtable middlewareResources = new Hashtable();
	middlewareResources.put(ContextEvent.MY_URI, ContextEvent.class);
	middlewareResources.put(ContextEventPattern.MY_URI,
		ContextEventPattern.class);
	middlewareResources.put(AggregatingFilter.MY_URI,
		AggregatingFilter.class);
	middlewareResources.put(PropertyPath.TYPE_PROPERTY_PATH,
		PropertyPath.class);
	middlewareResources.put(ServiceCall.MY_URI, ServiceCall.class);
	middlewareResources.put(ServiceRequest.MY_URI, ServiceRequest.class);
	// middlewareResources.put(ServiceRealization.MY_URI,
	// ServiceRealization.class);
	middlewareResources.put(ServiceResponse.MY_URI, ServiceResponse.class);
	middlewareResources.put(ServiceProfile.MY_URI, ServiceProfile.class);
	middlewareResources.put(OutputEvent.MY_URI, OutputEvent.class);
	middlewareResources.put(OutputEventPattern.MY_URI,
		OutputEventPattern.class);
	middlewareResources.put(InputEvent.MY_URI, InputEvent.class);
	middlewareResources.put(Form.MY_URI, Form.class);
	middlewareResources.put(Label.MY_URI, Label.class);
	middlewareResources.put(ChoiceItem.MY_URI, ChoiceItem.class);
	middlewareResources.put(ChoiceList.MY_URI, ChoiceList.class);
	middlewareResources.put(Group.MY_URI, Group.class);
	middlewareResources.put(Submit.MY_URI, Submit.class);
	middlewareResources
		.put(SubdialogTrigger.MY_URI, SubdialogTrigger.class);
	middlewareResources.put(SimpleOutput.MY_URI, SimpleOutput.class);
	middlewareResources.put(MediaObject.MY_URI, MediaObject.class);
	middlewareResources.put(InputField.MY_URI, InputField.class);
	middlewareResources.put(TextArea.MY_URI, TextArea.class);
	middlewareResources.put(Select.MY_URI, Select.class);
	middlewareResources.put(Select1.MY_URI, Select1.class);
	middlewareResources.put(Range.MY_URI, Range.class);
	if (classURI == null)
	    return null;

	Class clz = (Class) middlewareResources.get(classURI);
	if (clz == null)
	    return null;

	try {
	    if (clz == ServiceCall.class
		    && ServiceCall.THIS_SERVICE_CALL.getURI().equals(
			    instanceURI))
		return ServiceCall.THIS_SERVICE_CALL;
	    if (Resource.isAnonymousURI(instanceURI))
		return (Resource) clz.newInstance();
	    else
		return (Resource) clz.getConstructor(
			new Class[] { String.class }).newInstance(
			new Object[] { instanceURI });
	} catch (Exception e) {
	    return null;
	}
    }

}
