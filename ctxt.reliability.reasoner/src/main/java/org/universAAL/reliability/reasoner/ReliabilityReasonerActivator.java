package org.universAAL.reliability.reasoner;


/**Copyright [2011-2014] [University of Siegen, Embedded System Instiute]

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

* @author <a href="mailto:abu.sadat@uni-siegen.de">Rubaiyat Sadat</a>
*	       ©2012
*@author alviva
*/

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.container.osgi.uAALBundleContainer;
import org.universAAL.middleware.context.ContextPublisher;
import org.universAAL.middleware.context.ContextSubscriber;
import org.universAAL.middleware.context.DefaultContextPublisher;
import org.universAAL.middleware.context.owl.ContextProvider;
import org.universAAL.middleware.context.owl.ContextProviderType;
import org.universAAL.middleware.sodapop.msg.MessageContentSerializerEx;


public class ReliabilityReasonerActivator implements BundleActivator, ServiceListener {
    public static BundleContext osgiContext = null;
    public static ModuleContext context = null;
    public static ContextPublisher cpublisher = null;
    public static ContextSubscriber csubscriber = null;
    public static SCaller scaller = null;
    private static Object theFileLock = new Object();
//    private static final String FILENAME = "Situations.txt";
//    private static final String FILENAME2 = "Query.txt";
//    private static final Object WILDCARD = "*";
//    private static File confHome = new File(new BundleConfigHome(
//	    "ctxt.reliability.reasoner").getAbsolutePath());
//    public static ArrayList<CSubsMulti> subs = new ArrayList<CSubsMulti>();

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext
     * )
     */
    public void start(BundleContext bcontext) throws Exception {
	osgiContext = bcontext;
	context = uAALBundleContainer.THE_CONTAINER
		.registerModule(new Object[] { osgiContext });
	ContextProvider info = new ContextProvider(
		"http://ontology.itaca.es/Reasoner.owl#ReasonerPublisher");
	info.setType(ContextProviderType.reasoner);
	cpublisher = new DefaultContextPublisher(context, info);
	scaller = new SCaller(context);
	
	// Look for MessageContentSerializer of mw.data.serialization
	String filter = "(objectclass="
		+ MessageContentSerializerEx.class.getName() + ")";
	osgiContext.addServiceListener(this, filter);
	ServiceReference[] references = osgiContext.getServiceReferences(null,
		filter);
	for (int i = 0; references != null && i < references.length; i++) {
	    this.serviceChanged(new ServiceEvent(ServiceEvent.REGISTERED,
		    references[i]));
	}
	
	// Start all subscribers (heavy, do in thread)
//	new Thread(){
//	    @Override
//	    public void run() {
//		initializeSubscribers();
//	    }
//	}.start();
	
	csubscriber=new ReliabilityReasonerSubscriber(context);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
     */
    public void stop(BundleContext arg0) throws Exception {
	cpublisher.close();
	scaller.close();
//	for (CSubsMulti sub : (CSubsMulti[]) subs.toArray(new CSubsMulti[] {})) {
//	    if (sub != null)
//		sub.close();
//	}
//	subs.clear();
	csubscriber.close();
    }

//    /**
//     * Reads a file looking for "situations", described as statements just like
//     * in the original reasoner. The format of the file must be as follows
//     * <p>
//     * File: Situations.txt
//     * <p>
//     * ID subjectURI predicateURI objectURI
//     * <P>
//     * ...
//     * <p>
//     * Predicate and object URI are optional. This statement describes loosely a
//     * Context Event Pattern. When an event matching the pattern is received,
//     * the file with the corresponding ID is read. The name of the file is
//     * IDQuery.txt, being ID the id expressed in the previous file. The content
//     * must be a SPARQL CONSTRUCT query that builds a valid ContextEvent.
//     * 
//     */
//    private static void initializeSubscribers() {
//	// TODO Improve management of situation patterns. Enhance situation
//	// description.
//	synchronized (getFileLock()) {
//	    try {
//		String readline = "";
//		BufferedReader br = new BufferedReader(new FileReader(new File(
//			confHome, FILENAME)));
//		readline = br.readLine();
//		while (readline != null) {
//		    if (!readline.isEmpty()) {
//			String situation[] = readline.split(" ", 4);
//			
//			if (situation.length > 1) {
//			    // at least subject
//			    ContextEventPattern cep = new ContextEventPattern();
//			    String id = situation[0];
//			    if (!WILDCARD.equals(situation[1])) {
//				if (ManagedIndividual
//					.isRegisteredClassURI(situation[1])) {
//				    cep.addRestriction(MergedRestriction
//					    .getAllValuesRestriction(
//						    ContextEvent.PROP_RDF_SUBJECT,
//						    situation[1]));
//				} else {
//				    cep.addRestriction(MergedRestriction
//					    .getFixedValueRestriction(
//						    ContextEvent.PROP_RDF_SUBJECT,
//						    situation[1]));
//				}
//			    }
//			    
//			    if (situation.length > 2) {
//				// at least subj & pred
//				if (!WILDCARD.equals(situation[2])) {
//				    cep.addRestriction(MergedRestriction
//					    .getFixedValueRestriction(
//						    ContextEvent.PROP_RDF_PREDICATE,
//						    situation[2]));
//				}
//				
//				if (situation.length > 3) {
//				    // subj & pred & obj
//				    if (!WILDCARD.equals(situation[3])) {
//					if (ManagedIndividual
//						.isRegisteredClassURI(situation[3])) {
//					    cep.addRestriction(MergedRestriction
//						    .getAllValuesRestriction(
//							    ContextEvent.PROP_RDF_OBJECT,
//							    situation[3]));
//					} else {
//					    cep.addRestriction(MergedRestriction
//						    .getFixedValueRestriction(
//							    ContextEvent.PROP_RDF_OBJECT,
//							    situation[3]));
//					}
//				    }
//				}
//			    }
//			    
//			    // Now read the query for the situation
//			    String query = "";
//			    String readline2 = "";
//			    BufferedReader br2 = new BufferedReader(
//				    new FileReader(new File(confHome,
//					    (id + FILENAME2))));
//			    readline2 = br2.readLine();
//			    while (readline2 != null) {
//				query += readline2;
//				readline2 = br2.readLine();
//			    }
//			    br2.close();
//			    // Register a subscriber for each pair of
//			    // situation/query
//			    subs.add(new CSubsMulti(context,
//				    new ContextEventPattern[] { cep }, query));
//			}
//		    }
//		    readline = br.readLine();
//		}
//		br.close();
//	    } catch (FileNotFoundException e) {
//		LogUtils.logError(context, ReliabilityReasonerActivator.class,
//			"getInitialSubscriptions",
//			new Object[] { "Could not find file" }, e);
//	    } catch (Exception e) {
//		LogUtils.logError(context, ReliabilityReasonerActivator.class,
//			"getInitialSubscriptions",
//			new Object[] { "Could not open file" }, e);
//	    }
//	}
//    }

    /**
     * For synchronization
     * 
     * @return A generic object serving as lock
     */
    public static Object getFileLock() {
	return theFileLock;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.osgi.framework.ServiceListener#serviceChanged(org.osgi.framework.
     * ServiceEvent)
     */
    public void serviceChanged(ServiceEvent event) {
	// Update the MessageContentSerializer
	switch (event.getType()) {
	case ServiceEvent.REGISTERED:
	case ServiceEvent.MODIFIED:
	    scaller.setuAALParser((MessageContentSerializerEx) osgiContext
		    .getService(event.getServiceReference()));
	    break;
	case ServiceEvent.UNREGISTERING:
	    scaller.setuAALParser(null);
	    break;
	default:
	    break;
	}
    }

}
