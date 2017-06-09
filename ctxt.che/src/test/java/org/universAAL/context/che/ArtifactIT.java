package org.universAAL.context.che;

import java.util.Iterator;
import java.util.List;

import org.springframework.util.Assert;
import org.universAAL.context.che.Hub;
import org.universAAL.context.che.Hub.Log;
import org.universAAL.context.che.osgi.Activator;
import org.universAAL.itests.IntegrationTest;
import org.universAAL.middleware.context.ContextEventPattern;
import org.universAAL.middleware.context.ContextPublisher;
import org.universAAL.middleware.context.DefaultContextPublisher;
import org.universAAL.middleware.context.owl.ContextProvider;
import org.universAAL.middleware.context.owl.ContextProviderType;
import org.universAAL.middleware.owl.MergedRestriction;
import org.universAAL.middleware.rdf.PropertyPath;
import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.service.CallStatus;
import org.universAAL.middleware.service.DefaultServiceCaller;
import org.universAAL.middleware.service.ServiceCaller;
import org.universAAL.middleware.service.ServiceRequest;
import org.universAAL.middleware.service.ServiceResponse;
import org.universAAL.middleware.service.owls.process.ProcessOutput;
import org.universAAL.ontology.che.ContextEvent;
import org.universAAL.ontology.che.ContextHistoryService;

/**
 * Here developer's of this artifact should code their integration tests.
 * 
 * @author rotgier
 * 
 */
public class ArtifactIT extends IntegrationTest {

	public static final String NAMESPACE = "http://ontology.universAAL.org/Test.owl#";
	public static final String USER = NAMESPACE + "User";
	public static final String DUMMYUSER = NAMESPACE + "dummyUser";
	public static final String HAS_LOCATION = NAMESPACE + "hasLocation";
	public static final String LOCATION = NAMESPACE + "dummyLocation";
	public static final String DUMMYEVENT = "urn:org.universAAL.middleware.context.rdf:ContextEvent#_:0000000000000000:00";

	private static final String HISTORY_CLIENT_NAMESPACE = "http://ontology.itaca.es/HistoryClient.owl#";
	private static final String OUTPUT_LIST_OF_EVENTS = HISTORY_CLIENT_NAMESPACE + "listOfEvents";
	private static final String OUTPUT_RESULT_STRING = HISTORY_CLIENT_NAMESPACE + "resultString";

	ServiceCaller caller;
	private static Log log = Hub.getLog(ArtifactIT.class);

	/**
	 * Test 1: Check all artifacts in the log
	 */
	public void testComposite() {
		logAllBundles();
	}

	public void testOntology() {
		log.info("testOntology", "-Test 1-");
		// Test all possible constructions of the special CHE ContextEvent
		ContextEvent cev1 = ContextEvent.constructSimpleEvent(DUMMYUSER, USER, HAS_LOCATION, LOCATION);
		ContextEvent cev2 = new ContextEvent();
		ContextEvent cev3 = new ContextEvent(DUMMYEVENT);
		Resource res = new Resource(DUMMYUSER);
		res.setProperty(HAS_LOCATION, LOCATION);
		ContextEvent cev4 = new ContextEvent(res, HAS_LOCATION);
		cev2.setConfidence(50);
		cev2.setExpirationTime(Long.parseLong("0"));
		cev2.setTimestamp(Long.parseLong("0"));
		cev2.setProperty(ContextEvent.LOCAL_NAME_SUBJECT, new Resource(DUMMYUSER));
		cev2.setRDFSubject(new Resource(DUMMYUSER));
		cev2.setRDFPredicate(HAS_LOCATION);
		cev2.setRDFObject(LOCATION);
	}

	// public void testTransform(){
	// ContextEvent cevT = ContextEvent.constructSimpleEvent(DUMMYUSER, USER,
	// HAS_LOCATION, LOCATION);
	// cevT -> parser -> RDF -> parser -> cevF
	// org.universAAL.middleware.context.ContextEvent
	// cevF=(org.universAAL.middleware.context.ContextEvent)cevT;
	// }

	public void testStoreAndService() {
		log.info("testStoreAndService", "-Test 2-");
		caller = new DefaultServiceCaller(Activator.getModuleContext());
		ContextProvider info = new ContextProvider();
		info.setType(ContextProviderType.gauge);
		ContextEventPattern cep = new ContextEventPattern();
		cep.addRestriction(
				MergedRestriction.getFixedValueRestriction(ContextEvent.PROP_RDF_SUBJECT, new Resource(DUMMYUSER)));
		cep.addRestriction(MergedRestriction.getFixedValueRestriction(ContextEvent.PROP_RDF_PREDICATE,
				new Resource(HAS_LOCATION)));
		info.setProvidedEvents(new ContextEventPattern[] { cep });
		// info.setContextSources(new ManagedIndividual[]{new
		// ManagedIndividual(SOURCE)}); //Until we can use outer classes...
		ContextPublisher pub = new DefaultContextPublisher(Activator.getModuleContext(), info);
		log.info("testStoreAndService", "Created Default Context Publisher with full Provider Info", null);
		// Create and send first event
		org.universAAL.middleware.context.ContextEvent cevA = org.universAAL.middleware.context.ContextEvent
				.constructSimpleEvent(DUMMYUSER, USER, HAS_LOCATION, new Resource(LOCATION));
		// Must wait for the CHE to start and load OWLs...
		try {
			Thread.sleep(5000L);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		pub.publish(cevA);
		log.info("testStoreAndService", "Published event 1: " + cevA);
		// Wait for the event to be stored...
		try {
			Thread.sleep(3000L);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		// Create CHE match for the event
		ContextEvent cevB = ContextEvent.constructSimpleEvent(DUMMYUSER, USER, HAS_LOCATION, new Resource(LOCATION));

		// Test service GET EVENT
		// log.info("testStoreAndService", "Test service GET EVENT");
		// Assert.notNull(callGetEvents(cevB, 0L, 0L));
		// Test service GET EVENT FROM T
		log.info("testStoreAndService", " Test service GET EVENT FROM T");
		Assert.notNull(callGetEvents(cevB, 10L, 0L));
		// Test service GET EVENT TO T
		log.info("testStoreAndService", "Test service GET EVENT TO T");
		Assert.notNull(callGetEvents(cevB, 0L, System.currentTimeMillis()));
		// Test service GET EVENT BETWEEN T
		log.info("testStoreAndService", "Test service GET EVENT BETWEEN T");
		Assert.notNull(callGetEvents(cevB, 10L, System.currentTimeMillis()));
		// Test service GET SPARQL
		log.info("testStoreAndService", "Test service GET SPARQL");
		String result = callDoSPARQL("SELECT  ?c " + "WHERE"
				+ "  { ?c  <http://www.w3.org/1999/02/22-rdf-syntax-ns#type>  <http://ontology.universAAL.org/Context.owl#ContextEvent> ;"
				+ "        <http://www.w3.org/1999/02/22-rdf-syntax-ns#subject> <" + DUMMYUSER + "> ; "
				+ "        <http://www.w3.org/1999/02/22-rdf-syntax-ns#predicate> " + "<" + HAS_LOCATION + "> ; "
				+ "        <http://www.w3.org/1999/02/22-rdf-syntax-ns#object>  " + "<" + LOCATION + "> ." + "  }");
		Assert.notNull(result);
		Assert.isTrue(!result.equals(""));
		// Test service GET EVENT SPARQL
		log.info("testStoreAndService", "Test service GET EVENT SPARQL");
		Assert.notNull(this.callGetEventsSPARQL("SELECT  ?c " + "WHERE"
				+ "  { ?c  <http://www.w3.org/1999/02/22-rdf-syntax-ns#type>  <http://ontology.universAAL.org/Context.owl#ContextEvent> ;"
				+ "        <http://www.w3.org/1999/02/22-rdf-syntax-ns#subject> <" + DUMMYUSER + "> ; "
				+ "        <http://www.w3.org/1999/02/22-rdf-syntax-ns#predicate> " + "<" + HAS_LOCATION + "> ; "
				+ "        <http://www.w3.org/1999/02/22-rdf-syntax-ns#object>  " + "<" + LOCATION + "> ." + "  }"));
	}

	private Object getReturnValue(List outputs, String expectedOutput) {
		Object returnValue = null;
		if (outputs == null)
			log.info("getReturnValue", "History Client: No events found!");
		else
			for (Iterator i = outputs.iterator(); i.hasNext();) {
				ProcessOutput output = (ProcessOutput) i.next();
				if (output.getURI().equals(expectedOutput))
					if (returnValue == null)
						returnValue = output.getParameterValue();
					else
						log.info("getReturnValue", "History Client: redundant return value!");
				else
					log.info("getReturnValue", "History Client - output ignored: " + output.getURI());
			}

		return returnValue;
	}

	public org.universAAL.middleware.context.ContextEvent callGetEvents(ContextEvent matchEvent, long tstFrom,
			long tstTo) {
		org.universAAL.middleware.context.ContextEvent event = null;
		ServiceResponse response = caller.call(getGetEventsRequest(matchEvent, tstFrom, tstTo));
		if (response.getCallStatus() == CallStatus.succeeded) {
			Object value = getReturnValue(response.getOutputs(), OUTPUT_LIST_OF_EVENTS);
			if (value instanceof Resource) {
				if (((Resource) value).getURI().contains("#nil")) {
					log.info("callGetEvents", "History Client - result is empty");
					return null;
				}
			} else if (value != null) {
				try {
					org.universAAL.middleware.context.ContextEvent[] events = (org.universAAL.middleware.context.ContextEvent[]) (((List) value)
							.toArray(new org.universAAL.middleware.context.ContextEvent[((List) value).size()]));

					for (int j = 0; j < events.length; j++) {
						log.info("callGetEvents",
								"Retrieved context event from CHe:\n" + "    Subject     =" + events[j].getSubjectURI()
										+ "\n" + "    Subject type=" + events[j].getSubjectTypeURI() + "\n"
										+ "    Predicate   =" + events[j].getRDFPredicate() + "\n" + "    Object      ="
										+ events[j].getRDFObject());
						event = events[j];
					}
				} catch (Exception e) {
					log.info("callGetEvents", "History Client: List of events corrupt!", e);
				}
			} else {
				log.warn("callGetEvents", "History Client: No returned events");
			}
		} else
			log.info("callGetEvents", "History Client - status of getEvents(): " + response.getCallStatus());
		return event;
	}

	private ServiceRequest getGetEventsRequest(org.universAAL.ontology.che.ContextEvent matchEvent, long tstFrom,
			long tstTo) {
		ServiceRequest getEvents = new ServiceRequest(new ContextHistoryService(), null);

		MergedRestriction r = MergedRestriction.getFixedValueRestriction(ContextHistoryService.PROP_MANAGES,
				matchEvent);

		getEvents.getRequestedService().addInstanceLevelRestriction(r,
				new String[] { ContextHistoryService.PROP_MANAGES });
		if (tstFrom > 0) {
			MergedRestriction tstr1 = MergedRestriction
					.getFixedValueRestriction(ContextHistoryService.PROP_TIMESTAMP_FROM, Long.valueOf(tstFrom));
			getEvents.getRequestedService().addInstanceLevelRestriction(tstr1,
					new String[] { ContextHistoryService.PROP_TIMESTAMP_FROM });

		}
		if (tstTo > 0) {
			MergedRestriction tstr2 = MergedRestriction
					.getFixedValueRestriction(ContextHistoryService.PROP_TIMESTAMP_TO, Long.valueOf(tstTo));
			getEvents.getRequestedService().addInstanceLevelRestriction(tstr2,
					new String[] { ContextHistoryService.PROP_TIMESTAMP_TO });
		}
		getEvents.addSimpleOutputBinding(new ProcessOutput(OUTPUT_LIST_OF_EVENTS),
				new PropertyPath(null, false, new String[] { ContextHistoryService.PROP_MANAGES }).getThePath());
		return getEvents;
	}

	public String callDoSPARQL(String query) {
		ServiceResponse response = caller.call(getDoSPARQLRequest(query));

		if (response.getCallStatus() == CallStatus.succeeded) {
			try {
				String results = (String) getReturnValue(response.getOutputs(), OUTPUT_RESULT_STRING);
				// Uncomment this line if you want to show the raw results. Do
				// this for CONSTRUCT, ASK or DESCRIBE
				log.info("callDoSPARQL", "Result of SPARQL query was:\n" + results);
				return results;
			} catch (Exception e) {
				log.error("callDoSPARQL", "History Client: Result corrupt!", e);
				return "";
			}
		} else
			log.info("callDoSPARQL", "History Client - status of doSparqlQuery(): " + response.getCallStatus());
		return "";
	}

	private ServiceRequest getDoSPARQLRequest(String query) {
		ServiceRequest getQuery = new ServiceRequest(new ContextHistoryService(), null);

		MergedRestriction r = MergedRestriction.getFixedValueRestriction(ContextHistoryService.PROP_PROCESSES, query);

		getQuery.getRequestedService().addInstanceLevelRestriction(r,
				new String[] { ContextHistoryService.PROP_PROCESSES });
		getQuery.addSimpleOutputBinding(new ProcessOutput(OUTPUT_RESULT_STRING),
				new PropertyPath(null, true, new String[] { ContextHistoryService.PROP_RETURNS }).getThePath());
		return getQuery;
	}

	public org.universAAL.middleware.context.ContextEvent callGetEventsSPARQL(String query) {
		org.universAAL.middleware.context.ContextEvent event = null;
		ServiceResponse response = caller.call(getGetEventsSPARQLRequest(query));
		if (response.getCallStatus() == CallStatus.succeeded) {
			Object value = getReturnValue(response.getOutputs(), OUTPUT_LIST_OF_EVENTS);
			if (value instanceof Resource) {
				if (((Resource) value).getURI().contains("#nil")) {
					log.info("callGetEventsSPARQL", "History Client - result is empty");
					return null;
				}
			} else if (value != null) {
				try {
					org.universAAL.middleware.context.ContextEvent[] events = (org.universAAL.middleware.context.ContextEvent[]) ((List) value)
							.toArray(new org.universAAL.middleware.context.ContextEvent[((List) value).size()]);

					for (int j = 0; j < events.length; j++) {
						log.info("callGetEventsSPARQL",
								"Retrieved context event from CHe:\n" + "    Subject     =" + events[j].getSubjectURI()
										+ "\n" + "    Subject type=" + events[j].getSubjectTypeURI() + "\n"
										+ "    Predicate   =" + events[j].getRDFPredicate() + "\n" + "    Object      ="
										+ events[j].getRDFObject());
						event = events[j];
					}
				} catch (Exception e) {
					log.info("callGetEventsSPARQL", "History Client: List of events corrupt!", e);
				}
			} else {
				log.warn("callGetEventsSPARQL", "History Client: No returned events");
			}
		} else
			log.info("callGetEventsSPARQL", "History Client - status of getEvents(): " + response.getCallStatus());
		return event;
	}

	private ServiceRequest getGetEventsSPARQLRequest(String query) {
		ServiceRequest getQuery = new ServiceRequest(new ContextHistoryService(), null);

		MergedRestriction r = MergedRestriction.getFixedValueRestriction(ContextHistoryService.PROP_PROCESSES, query);

		getQuery.getRequestedService().addInstanceLevelRestriction(r,
				new String[] { ContextHistoryService.PROP_PROCESSES });
		getQuery.addSimpleOutputBinding(new ProcessOutput(OUTPUT_LIST_OF_EVENTS),
				new PropertyPath(null, false, new String[] { ContextHistoryService.PROP_MANAGES }).getThePath());
		return getQuery;
	}

}
