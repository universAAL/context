package org.universAAL.context.che.ontology;

import javax.xml.datatype.Duration;

import org.universAAL.middleware.context.owl.ContextProvider;
import org.universAAL.middleware.owl.BoundingValueRestriction;
import org.universAAL.middleware.owl.DataRepOntology;
import org.universAAL.middleware.owl.ManagedIndividual;
import org.universAAL.middleware.owl.MergedRestriction;
import org.universAAL.middleware.owl.OntClassInfoSetup;
import org.universAAL.middleware.owl.Ontology;
import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.rdf.TypeMapper;
import org.universAAL.middleware.service.owl.Service;

public class ContextHistoryOntology extends Ontology {

    private static ContextHistoryFactory factory = new ContextHistoryFactory();

    public static final String NAMESPACE = "http://ontology.universAAL.org/ContextHistory.owl#";

    public ContextHistoryOntology(String ontURI) {
	super(ontURI);
    }

    public ContextHistoryOntology() {
	super(NAMESPACE);
    }

    @Override
    public void create() {
	Resource r = getInfo();
	r.setResourceComment("The ontology defining the CHE concepts and services");
	r.setResourceLabel("CHE");
	addImport(DataRepOntology.NAMESPACE);

	OntClassInfoSetup oci;

	// load ContextEvent______________
	oci = createNewOntClassInfo(ContextEvent.MY_URI, factory, 0);
	oci.setResourceComment("Replicates ContextEvent as a ManagedIndividual");
	oci.setResourceLabel("Context Event (CHE)");
	oci.addSuperClass(ManagedIndividual.MY_URI);

	oci.addObjectProperty(ContextEvent.PROP_RDF_SUBJECT).setFunctional();
	// oci.addRestriction(MergedRestriction
	// .getAllValuesRestrictionWithCardinality(
	// ContextEvent.PROP_RDF_SUBJECT, Resource.MY_URI, 0, 1));

	oci.addObjectProperty(ContextEvent.PROP_RDF_PREDICATE).setFunctional();
	// oci.addRestriction(MergedRestriction
	// .getAllValuesRestrictionWithCardinality(
	// ContextEvent.PROP_RDF_PREDICATE, Property.MY_URI, 0, 1));

	oci.addObjectProperty(ContextEvent.PROP_RDF_OBJECT).setFunctional();
	// oci.addRestriction(MergedRestriction
	// .getAllValuesRestrictionWithCardinality(
	// ContextEvent.PROP_RDF_OBJECT, Resource.MY_URI, 0, 1));

	oci.addDatatypeProperty(ContextEvent.PROP_CONTEXT_TIMESTAMP)
		.setFunctional();
	oci.addRestriction(MergedRestriction
		.getAllValuesRestrictionWithCardinality(
			ContextEvent.PROP_CONTEXT_TIMESTAMP,
			TypeMapper.getDatatypeURI(Long.class), 0, 1));

	oci.addDatatypeProperty(ContextEvent.PROP_CONTEXT_EXPIRATION_TIME)
		.setFunctional();
	oci.addRestriction(MergedRestriction
		.getAllValuesRestrictionWithCardinality(
			ContextEvent.PROP_CONTEXT_EXPIRATION_TIME,
			TypeMapper.getDatatypeURI(Long.class), 0, 1));

	oci.addDatatypeProperty(ContextEvent.PROP_CONTEXT_CONFIDENCE)
		.setFunctional();
	oci.addRestriction(MergedRestriction
		.getAllValuesRestrictionWithCardinality(
			ContextEvent.PROP_CONTEXT_CONFIDENCE,
			TypeMapper.getDatatypeURI(Long.class), 0, 1)
		.addRestriction(
			new BoundingValueRestriction(
				ContextEvent.PROP_CONTEXT_CONFIDENCE,
				new Integer(0), true, new Integer(100), true)));

	oci.addDatatypeProperty(ContextEvent.PROP_CONTEXT_PROVIDER)
		.setFunctional();
	oci.addRestriction(MergedRestriction
		.getAllValuesRestrictionWithCardinality(
			ContextEvent.PROP_CONTEXT_PROVIDER,
			ContextProvider.MY_URI, 0, 1));

	// load ContextHistoryService_________
	oci = createNewOntClassInfo(ContextHistoryService.MY_URI, factory, 1);
	oci.setResourceComment("The class of services managing Context Events in CHE");
	oci.setResourceLabel("Lighting");
	oci.addSuperClass(Service.MY_URI);

	oci.addObjectProperty(ContextHistoryService.PROP_MANAGES);
	oci.addRestriction(MergedRestriction.getAllValuesRestriction(
		ContextHistoryService.PROP_MANAGES, ContextEvent.MY_URI));

	oci.addObjectProperty(ContextHistoryService.PROP_PROCESSES);
	oci.addRestriction(MergedRestriction.getAllValuesRestriction(
		ContextHistoryService.PROP_PROCESSES,
		TypeMapper.getDatatypeURI(String.class)));

	oci.addObjectProperty(ContextHistoryService.PROP_TIMESTAMP_FROM);
	oci.addRestriction(MergedRestriction.getAllValuesRestriction(
		ContextHistoryService.PROP_TIMESTAMP_FROM,
		TypeMapper.getDatatypeURI(Long.class)));

	oci.addObjectProperty(ContextHistoryService.PROP_TIMESTAMP_TO);
	oci.addRestriction(MergedRestriction.getAllValuesRestriction(
		ContextHistoryService.PROP_TIMESTAMP_TO,
		TypeMapper.getDatatypeURI(Long.class)));

	oci.addObjectProperty(ContextHistoryService.PROP_DURATION_FROM);
	oci.addRestriction(MergedRestriction.getAllValuesRestriction(
		ContextHistoryService.PROP_DURATION_FROM,
		TypeMapper.getDatatypeURI(Duration.class)));

	oci.addObjectProperty(ContextHistoryService.PROP_DURATION_TO);
	oci.addRestriction(MergedRestriction.getAllValuesRestriction(
		ContextHistoryService.PROP_DURATION_TO,
		TypeMapper.getDatatypeURI(Duration.class)));

	oci.addObjectProperty(ContextHistoryService.PROP_RETURNS);
	oci.addRestriction(MergedRestriction.getAllValuesRestriction(
		ContextHistoryService.PROP_RETURNS,
		TypeMapper.getDatatypeURI(String.class)));

    }

}
