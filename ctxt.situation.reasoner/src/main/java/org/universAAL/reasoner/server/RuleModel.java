package org.universAAL.reasoner.server;

import java.util.ArrayList;

import org.universAAL.middleware.context.ContextEvent;
import org.universAAL.middleware.context.ContextEventPattern;
import org.universAAL.middleware.owl.ManagedIndividual;
import org.universAAL.middleware.owl.MergedRestriction;
import org.universAAL.reasoner.ont.ElementModel;
import org.universAAL.reasoner.ont.Rule;
import org.universAAL.reasoner.server.osgi.Activator;

/**
 * In opposite the Situations and Queries Rules need some special handling. This
 * is only when adding a new rule and following the according method is
 * overwritten here. Every rule that is added need to register a
 * ContextSubscriber that matches the Situation given by the rule. This
 * subscriber is created in "registerRule".
 * 
 * @author amarinc
 * 
 */
public class RuleModel extends ElementModel<Rule> {

	private static final Object WILDCARD = "*";

	private ArrayList<CSubsMulti> subs = new ArrayList<CSubsMulti>();

	/**
	 * 
	 * @param confFolder
	 *            Folder where elements of this type should saved.
	 */
	public RuleModel(String confFolder) {
		super(Rule.class, Activator.serializer, confFolder);
		loadElements();
	}

	@Override
	public Rule add(Rule rule) {
		Rule addedRule = super.add(rule);
		if (addedRule == rule)
			registerRule(rule);
		return addedRule;
	}

	public void deleteContextSubscriptions() {
		for (CSubsMulti sub : (CSubsMulti[]) subs.toArray(new CSubsMulti[] {})) {
			if (sub != null)
				sub.close();
		}
		subs.clear();
	}

	/**
	 * Every Situation is described by at least one URI for the SUBJECT of a
	 * Contex-Event and optional also one for the Predicate and one for the
	 * Object. For Subject and Object it can be either a TypeURI or the URI of
	 * an instance.
	 * 
	 * @param rule
	 *            Rule to be registered
	 */
	private void registerRule(Rule rule) {
		// at least subject
		ContextEventPattern cep = new ContextEventPattern();
		String subject = rule.getSituation().getRdfSubject();
		String predicate = rule.getSituation().getRdfPredicate();
		String object = rule.getSituation().getRdfObject();

		if (subject == null || subject.equals("") || WILDCARD.equals(subject)) {
			return;
		}

		if (ManagedIndividual.isRegisteredClassURI(subject)) {
			cep.addRestriction(MergedRestriction.getAllValuesRestriction(
					ContextEvent.PROP_RDF_SUBJECT, subject));
		} else {
			cep.addRestriction(MergedRestriction.getFixedValueRestriction(
					ContextEvent.PROP_RDF_SUBJECT, subject));
		}

		if (predicate != null && !predicate.equals("")) {
			// at least subj & pred
			if (!WILDCARD.equals(predicate)) {
				cep.addRestriction(MergedRestriction.getFixedValueRestriction(
						ContextEvent.PROP_RDF_PREDICATE, predicate));
			}

			if (object != null && !object.equals("")) {
				// subj & pred & obj
				if (!WILDCARD.equals(object)) {
					if (ManagedIndividual.isRegisteredClassURI(object)) {
						cep.addRestriction(MergedRestriction
								.getAllValuesRestriction(
										ContextEvent.PROP_RDF_OBJECT, object));
					} else {
						cep.addRestriction(MergedRestriction
								.getFixedValueRestriction(
										ContextEvent.PROP_RDF_OBJECT, object));
					}
				}
			}
		}

		String query = rule.getQuery().getResultingQuery();
		subs.add(new CSubsMulti(Activator.mcontext,
				new ContextEventPattern[] { cep }, query));
	}

}
