/*
	Copyright 2008-2010 Fraunhofer IGD, http://www.igd.fraunhofer.de
	Fraunhofer-Gesellschaft - Institute of Computer Graphics Research 
	
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
package org.universAAL.context.conversion.jena.impl;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import javax.xml.XMLConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.universAAL.context.conversion.jena.JenaConverter;
import org.universAAL.middleware.context.ContextEvent;
import org.universAAL.middleware.context.ContextEventPattern;
import org.universAAL.middleware.input.InputEvent;
import org.universAAL.middleware.io.rdf.*;
import org.universAAL.middleware.output.OutputEvent;
import org.universAAL.middleware.output.OutputEventPattern;
import org.universAAL.middleware.owl.ClassExpression;
import org.universAAL.middleware.owl.ManagedIndividual;
import org.universAAL.middleware.owl.Restriction;
import org.universAAL.middleware.rdf.PropertyPath;
import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.rdf.TypeMapper;
import org.universAAL.middleware.service.AggregatingFilter;
import org.universAAL.middleware.service.ServiceCall;
import org.universAAL.middleware.service.ServiceRequest;
import org.universAAL.middleware.service.ServiceResponse; //import org.universAAL.middleware.service.impl.ServiceRealization;//not exported by bus.service
import org.universAAL.middleware.service.owls.profile.ServiceProfile;
import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.container.utils.LogUtils;

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

/**
 * This is the reference implementation of the
 * {@link org.universAAL.context.conversion.jena.JenaConverter}.
 * 
 * @author mtazari
 * 
 */
public class JenaModelConverter implements JenaConverter {

    private Logger logger = LoggerFactory.getLogger(JenaModelConverter.class);

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
	    Property prop = RDF.type.getURI().equals(propURI) ? RDF.type : m
		    .createProperty(propURI);
	    int reductionType = (prop == RDF.type) ? Resource.PROP_SERIALIZATION_REDUCED
		    : pr.getPropSerializationType(propURI);
	    if (reductionType == Resource.PROP_SERIALIZATION_UNDEFINED)
		// a reduced unmanaged resource should be serialized only by
		// its URI skipping all properties
		reductionType = reduced ? Resource.PROP_SERIALIZATION_OPTIONAL
			: Resource.PROP_SERIALIZATION_FULL;
	    if (reduced
		    && reductionType == Resource.PROP_SERIALIZATION_OPTIONAL)
		continue;
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
		} else {
		    if (prop == RDF.type) {
			ArrayList aux = new ArrayList((List) o);
			for (Iterator i = ((List) o).iterator(); i.hasNext();) {
			    String[] types = ManagedIndividual
				    .getNonabstractSuperClasses(i.next()
					    .toString());
			    if (types != null)
				for (int j = 0; j < types.length; j++)
				    aux.add(new Resource(types[j]));
			}
			o = aux;
		    }
		    for (Iterator i = ((List) o).iterator(); i.hasNext();)
			jr
				.addProperty(
					prop,
					getRDFNode(
						m,
						i.next(),
						(reductionType == Resource.PROP_SERIALIZATION_REDUCED),
						resources));
		}
	    else
		jr.addProperty(prop, getRDFNode(m, o,
			(reductionType == Resource.PROP_SERIALIZATION_REDUCED),
			resources));
	}
	return jr;
    }

    private boolean checkEquality(int maxCard,
	    com.hp.hpl.jena.rdf.model.Resource sink,
	    com.hp.hpl.jena.rdf.model.Resource src) {
	// the 'equals' method of Resource can also check the equality of two
	// anon resources
	// but here we say that if the ontology allows only 1 value then the two
	// resources must be equal
	return maxCard == 1 || sink.equals(src);
    }

    // public Object deserialize(String serialized) {
    // return deserialize(serialized, false);
    // }

    /**
     * Deserializes a complex object that is not an XML literal, that is, a
     * {@link org.universAAL.middleware.rdf.Resource} root element.
     * 
     * @param serialized
     *            The serialized object in RDF
     * @return The deserialized <code>Object</code>. Returns <code>null</code>
     *         if there was an error.
     */
    private Object deserialize(String serialized, boolean wasXMLLiteral) {
	if (serialized == null)
	    return null;

	try {
	    Model m = ModelFactory.createDefaultModel();
	    m.read(new StringReader(serialized), null);
	    Resource result = getResource(getJenaRootResource(m),
		    new Hashtable(), wasXMLLiteral);
	    if (Resource.TYPE_RDF_LIST.equals(result.getType())) {
		Object first = result.getProperty(Resource.PROP_RDF_FIRST);
		if (first == null)
		    return null;
		Object rest = result.getProperty(Resource.PROP_RDF_REST);
		if (rest != null
			&& rest.toString().equals(Resource.RDF_EMPTY_LIST))
		    rest = new ArrayList(1);
		else if (!(rest instanceof List))
		    return null;
		((List) rest).add(0, first);
		return rest;
	    } else
		return result;
	} catch (Exception e) {
	    LogUtils.logError((ModuleContext) logger, JenaModelConverter.class, "deserialize",
		    new Object[] { "Jena-Serializer: Failed to parse\n   ",
			    serialized, "   returning null!" }, e);
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
	return processProperties(result, r, resources) ? result : null;
    }

    public com.hp.hpl.jena.rdf.model.Resource getJenaRootResource(Model m) {
	if (m == null)
	    return null;
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
	String classURI = getType(r);
	if (classURI == null)
	    return getUnmanagedResource(r, resources, wasXMLLiteral);

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
	return processProperties(pr, r, resources) ? pr : null;
    }

    private RDFNode getRDFNode(Model m, Object o, boolean reduced,
	    Hashtable resources) {
	if (o instanceof List) {
	    List l = new ArrayList(((List) o).size());
	    for (Iterator i = ((List) o).iterator(); i.hasNext();)
		l.add(getRDFNode(m, i.next(), reduced, resources));
	    return m.createList(l.iterator());
	} else if (o instanceof Resource) {
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
		String par[] = TypeMapper.getXMLInstance(o);
		return m.createTypedLiteral(par[0], new XSDDatatype(
			par[1].substring("http://www.w3.org/2001/XMLSchema"
				.length() + 1)));
	}//was XMLConstants.W3C_XML_SCHEMA_NS_URI

	//return null;

	/*
	 * old code refactored return (o instanceof Resource)? (((Resource)
	 * o).serializesAsXMLLiteral()? ((((Resource) o).isAnon() || ((Resource)
	 * o).numberOfProperties() > 0)? m.createTypedLiteral(serialize(o),
	 * XMLLiteralType.theXMLLiteralType) : m.createTypedLiteral(((Resource)
	 * o).getURI(), XSDDatatype.XSDanyURI)) : addDescription(m, (Resource)
	 * o, reduced, resources)) : m.createTypedLiteral(o);
	 */
    }

    private String getType(com.hp.hpl.jena.rdf.model.Resource r) {
	ArrayList al = new ArrayList();
	for (StmtIterator i = r.listProperties(RDF.type); i.hasNext();)
	    al.add(i.nextStatement().getResource().getURI());
	if (al.size() == 0)
	    return null;
	String result = ManagedIndividual.getMostSpecializedClass((String[]) al
		.toArray(new String[al.size()]));
	return (result == null) ? (String) al.get(0) : result;
    }

    private Resource getUnmanagedResource(com.hp.hpl.jena.rdf.model.Resource r,
	    Hashtable resources, boolean wasXMLLiteral) {
	String uri = r.isAnon() ? r.getId().getLabelString() : r.getURI();
	Resource result = (r.isAnon() ? new Resource(wasXMLLiteral)
		: new Resource(uri, wasXMLLiteral));
	resources.put(uri, result);
	return processProperties(result, r, resources) ? result : null;
    }

    private boolean handleObjectValue(RDFNode n, List l, Hashtable resources) {
	if (n.isLiteral()) {
	    Object o = null;
	    Literal literal = (Literal) n;
	    if (XMLLiteralType.theXMLLiteralType.equals(literal.getDatatype()))
		o = deserialize(literal.getLexicalForm(), true);
	    else
		o = TypeMapper.getJavaInstance(literal.getLexicalForm(), literal
			.getDatatypeURI());
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
		l.add(Resource.RDF_EMPTY_LIST);
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
	HashSet done = new HashSet();
	for (StmtIterator i1 = jr.listProperties(); i1.hasNext();) {
	    Property p = i1.nextStatement().getPredicate();
	    if (!done.add(p))
		// the case of properties used in several statements
		// we have already handled those statements in the loop below
		continue;
	    ArrayList l = new ArrayList();
	    for (StmtIterator i2 = jr.listProperties(p); i2.hasNext();)
		handleObjectValue(i2.nextStatement().getObject(), l, resources);
	    switch (l.size()) {
	    case 0:
		break;
	    case 1:
		if (l.remove(Resource.RDF_EMPTY_LIST))
		    pr.setProperty(p.getURI(), Resource.RDF_EMPTY_LIST);
		else
		    pr.setProperty(p.getURI(), l.get(0));
		break;
	    default:
		l.remove(Resource.RDF_EMPTY_LIST);
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
    private String serialize(Object messageContent) {
	if (messageContent instanceof Resource) {
	    Model m = toJenaResource((Resource) messageContent).getModel();
	    StringWriter sw = new StringWriter(4096);
	    // sw.write("<?xml version='1.0'?>\r\n");
	    m.write(sw, "RDF/XML-ABBREV");
	    return sw.toString();
	}

	return null;
    }

    public com.hp.hpl.jena.rdf.model.Resource toJenaResource(Resource r) {
	if (r == null)
	    return null;
	Model m = ModelFactory.createDefaultModel();
	m.setNsPrefix("", Resource.uAAL_VOCABULARY_NAMESPACE);
	Hashtable temp = new Hashtable();
	addDescription(m, r, false, temp);
	return (com.hp.hpl.jena.rdf.model.Resource) temp.get(r);
    }

    public Resource toPersonaResource(com.hp.hpl.jena.rdf.model.Resource r) {
	return (r == null) ? null : getResource(r, new Hashtable(), false);
    }

    public boolean updateDBResource(com.hp.hpl.jena.rdf.model.Resource dbRes,
	    com.hp.hpl.jena.rdf.model.Resource updater) {
	String snkType = getType(dbRes), srcType = getType(updater), sigType;
	if (snkType == null)
	    if (srcType == null)
		sigType = null;
	    else
		sigType = srcType;
	else if (srcType == null || snkType.equals(srcType)
		|| ManagedIndividual.checkCompatibility(srcType, snkType))
	    sigType = snkType;
	else
	    sigType = srcType;
	Model m = dbRes.getModel();
	// TODO: this is a simple implementation that ignores the xml:lang
	// attribute
	// and handles only the case where each property is always associated
	// with only one RDFNode as object
	for (StmtIterator i1 = updater.listProperties(); i1.hasNext();) {
	    Property p = i1.nextStatement().getPredicate();
	    ArrayList l1 = new ArrayList(), l2 = new ArrayList();
	    for (StmtIterator i2 = updater.listProperties(p); i2.hasNext();)
		l1.add(i2.nextStatement().getObject());
	    for (StmtIterator i2 = dbRes.listProperties(p); i2.hasNext();)
		l2.add(i2.nextStatement());
	    if (l2.isEmpty())
		// the only case, where we can handle all RDFNodes
		for (int i = 0; i < l1.size(); i++) {
		    RDFNode aux = (RDFNode) l1.get(i);
		    dbRes.addProperty(p, aux);
		    if (aux instanceof com.hp.hpl.jena.rdf.model.Resource)
			updateDBResource(dbRes.getProperty(p).getResource(),
				(com.hp.hpl.jena.rdf.model.Resource) aux);
		}
	    else if (l1.size() == 1 && l2.size() == 1) {
		RDFNode src = (RDFNode) l1.get(0);
		Statement s = (Statement) l2.get(0);
		if (src.isLiteral())
		    s.changeObject(src);
		else {
		    RDFNode sink = s.getObject();
		    if (src.canAs(RDFList.class)) {
			RDFList srcList = (RDFList) src.as(RDFList.class);
			if (sink.canAs(RDFList.class)) {
			    // TODO: what to do here? The alternatives are:
			    // 1. the whole srcList replaces the whole snkList
			    // without any check
			    // 2. like 1, but for equal members use the old one
			    // as basis and update
			    // it with the newer one -> delete old members with
			    // no equal
			    // 3. merge and update: keep snkList as basis, add
			    // fully new members
			    // from srcList and update equal members like in 2
			    // we go for the second variant!
			    RDFList snkList = (RDFList) sink.as(RDFList.class);
			    // if (RDF.nil.getURI().equals(snkList.getURI())) {
			    // if (RDF.nil.getURI().equals(srcList.getURI()))
			    if (snkList.isEmpty()) {
				if (srcList.isEmpty())
				    continue;
				snkList = m.createList(srcList.iterator());
				s.changeObject(snkList);
			    }
			    updateList(snkList, srcList);
			} else {
			    RDFList snkList = m.createList(srcList.iterator());
			    s.changeObject(snkList);
			    updateList(snkList, srcList);
			    if (sink.isResource()) {
				int n = srcList.size();
				for (int i = 0; i < n; i++) {
				    RDFNode lm = srcList.get(i);
				    if (lm.isResource()
					    && checkEquality(
						    -1,
						    (com.hp.hpl.jena.rdf.model.Resource) sink,
						    (com.hp.hpl.jena.rdf.model.Resource) lm)) {
					srcList.replace(i, sink);
					updateDBResource(
						(com.hp.hpl.jena.rdf.model.Resource) sink,
						(com.hp.hpl.jena.rdf.model.Resource) lm);
				    }
				}
			    }
			}
		    } else if (sink.canAs(RDFList.class)) {
			RDFList snkList = (RDFList) sink.as(RDFList.class);
			boolean found = false;
			for (Iterator i = snkList.iterator(); !found
				&& i.hasNext();) {
			    RDFNode lm = (RDFNode) i.next();
			    if (lm.isResource()
				    && checkEquality(
					    -1,
					    (com.hp.hpl.jena.rdf.model.Resource) lm,
					    (com.hp.hpl.jena.rdf.model.Resource) src)) {
				found = true;
				s.changeObject(lm);
			    }
			}
			if (!found)
			    s.changeObject(src.inModel(m));
			snkList.removeList();
			updateDBResource(s.getResource(),
				(com.hp.hpl.jena.rdf.model.Resource) src);
		    } else if (sink.isLiteral())
			s.changeObject(src.inModel(m));
		    else {
			Restriction r = ManagedIndividual
				.getClassRestrictionsOnProperty(sigType, p
					.getURI());
			if (!checkEquality(((r == null) ? -1 : r
				.getMaxCardinality()),
				(com.hp.hpl.jena.rdf.model.Resource) sink,
				(com.hp.hpl.jena.rdf.model.Resource) src))
			    s.changeObject(src.inModel(m));
			updateDBResource(s.getResource(),
				(com.hp.hpl.jena.rdf.model.Resource) src);
		    }
		}
	    } else {
		// TODO: see the todo entry at the beginning of this method
		return false;
	    }
	}
	return true;
    }

    private void updateList(RDFList sink, RDFList src) {
	int srcN = src.size();
	if (srcN == 0) {
	    sink.removeList();
	    sink.getModel().removeAll(null, null, sink);
	    return;
	}

	int snkN = sink.size();
	ArrayList al = new ArrayList(snkN);
	for (Iterator i = sink.iterator(); i.hasNext();) {
	    RDFNode n = (RDFNode) i.next();
	    if (n.isResource())
		al.add(n);
	}

	Model m = sink.getModel();
	RDFList curList = sink, parList = null;
	for (int i = 0; i < srcN; i++) {
	    RDFNode next = src.getHead();
	    src = src.getTail();
	    if (next.isLiteral())
		if (i < snkN) {
		    curList.setHead(next);
		    parList = curList;
		    curList = parList.getTail();
		} else {
		    parList.add(next);
		    parList = parList.getTail();
		}
	    else {
		com.hp.hpl.jena.rdf.model.Resource found = null;
		for (Iterator it = al.iterator(); it.hasNext();) {
		    found = (com.hp.hpl.jena.rdf.model.Resource) it.next();
		    if (checkEquality(-1, found,
			    (com.hp.hpl.jena.rdf.model.Resource) next)) {
			it.remove();
			break;
		    } else
			found = null;
		}
		if (found == null)
		    found = (com.hp.hpl.jena.rdf.model.Resource) next
			    .inModel(m);
		if (i < snkN) {
		    curList.setHead(found);
		    parList = curList;
		    curList = parList.getTail();
		} else {
		    parList.add(found);
		    parList = parList.getTail();
		}
		updateDBResource(found,
			(com.hp.hpl.jena.rdf.model.Resource) next);
	    }
	}

	// trim the list if necessary
	if (srcN < snkN) {
	    RDFList tmp = parList.getTail();
	    parList.getProperty(RDF.rest).changeObject(RDF.nil);
	    tmp.removeList();
	}

	// remove all not referenced resources
	for (Iterator it = al.iterator(); it.hasNext();) {
	    RDFNode aux = (RDFNode) it.next();
	    if (aux.isResource() && !m.contains(null, null, aux))
		m.removeAll((com.hp.hpl.jena.rdf.model.Resource) aux, null,
			null);
	}
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
