/*
	Copyright 2008-2014 TSB, http://www.tsbtecnologias.es
	TSB - Tecnolog�as para la Salud y el Bienestar
	
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
package org.universAAL.drools.engine;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import org.drools.FactHandle;
import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseConfiguration;
import org.drools.KnowledgeBaseFactory;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderConfiguration;
import org.drools.builder.KnowledgeBuilderError;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.conf.AssertBehaviorOption;
import org.drools.conf.EventProcessingOption;
import org.drools.definition.KnowledgePackage;
import org.drools.io.Resource;
import org.drools.io.ResourceFactory;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.rule.ConsequenceException;
import org.osgi.framework.BundleContext;
import org.universAAL.drools.DevelopingRulesUI;
import org.universAAL.drools.models.RuleModel;
import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.container.osgi.uAALBundleContainer;
import org.universAAL.middleware.container.osgi.util.BundleConfigHome;
import org.universAAL.middleware.context.ContextEvent;
import org.universAAL.middleware.context.ContextEventPattern;
import org.universAAL.middleware.context.ContextPublisher;
import org.universAAL.middleware.context.DefaultContextPublisher;
import org.universAAL.middleware.context.owl.ContextProvider;
import org.universAAL.middleware.context.owl.ContextProviderType;
import org.universAAL.ontology.activityhub.ActivityHubSensor;
import org.universAAL.ontology.device.Sensor;
import org.universAAL.ontology.drools.Consequence;
import org.universAAL.ontology.drools.ConsequenceProperty;
import org.universAAL.ontology.drools.DroolsReasoning;
import org.universAAL.ontology.phThing.Device;

//import bitronix.tm.TransactionManagerServices;
//import bitronix.tm.resource.jdbc.PoolingDataSource;
//import bitronix.tm.utils.ClassLoaderUtils;

//import org.dynamicjava.osgi.dynamic_jpa.DynamicJpaConstants;
//import org.dynamicjava.osgi.service_binding_utils.OsgiServiceBinder;
//import org.dynamicjava.osgi.service_binding_utils.ServiceFilter;

/**
 * Rules engine based on Drools technology. Provides the funcionalities of the
 * rules engine and the access to the working memory where is stored the
 * knowledge.
 * 
 * @author Miguel Llorente (mllorente)
 */

public final class RulesEngine {

	private static StatefulKnowledgeSession ksession;
	private static KnowledgeBase kbase;
	private static boolean testMode = false;
	// TODO get test mode out to a config file
	private static BundleContext rulesEngineBundleContext = null;
	private static ModuleContext rulesEngineModuleContext = null;
	private static RulesEngine INSTANCE;
	private static final UUID uuid = UUID.randomUUID();
	private static final String HOME_FOLDER = "drools.reasoner";
	private static final String PROPERTIES_FILE = "reasoner.properties";

	// private PoolingDataSource dataSource;
	// private EntityManagerFactory emf;
	// private Environment env;
	// private static final Class[] parameters = new Class[]{URL.class};
	// Messages messages;

	/**
	 * Private constructor. Needs a BundleContext for accessing the resources
	 * embedded in the bundle.
	 * 
	 * @param context
	 *            The bundle context.
	 */
	private RulesEngine(BundleContext context) {
		super();
		rulesEngineBundleContext = context;
		rulesEngineModuleContext = uAALBundleContainer.THE_CONTAINER
				.registerModule(new Object[] { rulesEngineBundleContext });
		try {
			initializeDrools();
		} catch (Exception e) {
			e.printStackTrace();
		}
		INSTANCE = this;
		System.out
				.println("\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\n\nSTARTING WINDOW\n\n\n\n\n\n\n\n\n\n");
		if (getDevelopingStatus()) {
			new DevelopingRulesUI(context);
		}
		setTestStatus();
	}

	private void setTestStatus() {
		String homePath = new BundleConfigHome(HOME_FOLDER).getAbsolutePath();

		Properties properties = new Properties();
		try {
			properties.load(new FileInputStream(homePath + "/"
					+ PROPERTIES_FILE));
			if (Boolean.parseBoolean(properties.getProperty("mode.test"))) {
				enableTestMode();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private boolean getDevelopingStatus() {

		String homePath = new BundleConfigHome(HOME_FOLDER).getAbsolutePath();

		Properties properties = new Properties();
		try {
			properties.load(new FileInputStream(homePath + "/"
					+ PROPERTIES_FILE));
			return Boolean.parseBoolean(properties
					.getProperty("developing.rules"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * Gets an instance of the rules engine for using it in universAAL. It
	 * allows the interaction with the rules engine: insertion an retraction of
	 * facts, rules and evens; extraction of information related to the runtime
	 * session for monitoring proposals and consequences publication.
	 * 
	 * @param context
	 *            The bundle context.
	 * @return Instance of the rules engine.
	 */
	public static RulesEngine getInstance(BundleContext context) {
		System.out.println("THIS RULE ENGINE: " + uuid);
		if (INSTANCE == null)
			return new RulesEngine(context);
		else
			return INSTANCE;
	}

	/**
	 * Instance of the rules engine for using it in non-osgi environments. It
	 * must not be used in a typical universAAL platform exectuion. (Method
	 * created for debugging and auto-testing purposes).
	 * 
	 * @return An instance of the RulesEngine ready to be used out of the OSGI
	 *         runtime. This instance is not valid over universAAL and OSGi.
	 */
	public static RulesEngine getInstance() {
		// testMode = true;
		if (INSTANCE == null) {
			return new RulesEngine(null);
		} else {
			return INSTANCE;
		}
	}

	/**
	 * Returns an UUID of the rules engine. Useful for validating the singleton
	 * condition of this class.
	 * 
	 * @return
	 */
	public static UUID getUUID() {
		return uuid;
	}

	/**
	 * Performs the initialization process in order to create a stateful
	 * knowledge session with event processing mode.
	 * 
	 * @throws Exception
	 */
	private static void initializeDrools() throws Exception {
		// //Brand new
		// setUpPersistence();
		try {
			kbase = loadKnowledgeBaseFromPackages();
		} catch (final Throwable e) {
			e.printStackTrace();
			throw new Exception(
					"Errors found during the creation of the Knowledge Base", e);
		}

		ksession = kbase.newStatefulKnowledgeSession();
		ksession.addEventListener(new CustomAgendaEventListener());
		ksession.addEventListener(new CustomWorkingMemoryEventListener());
		// //Brand new
		// initKnowledgeSession();
		Runnable runnable = new Runnable() {
			public void run() {
				try {
					// run forever
					ksession.fireUntilHalt();
				} catch (ConsequenceException e) {
					throw e;
				}
			}
		};
		Thread thread = new Thread(runnable); // In java 6 use Executors instead
		thread.start();
		/**
		 * I've discovered it's not a good idea using "entry-points" if it isn't
		 * completely needed
		 */
		// try {
		// traceEntryPoint = setNewEntryPoint("trace");
		// } catch (final Throwable e) {
		// e.printStackTrace();
		// throw new Exception("Could not create WorkingMemoryEntryPoint", e);
		// }
	}

	/**
	 * Initialize the knowledgde base using the packages method.
	 * 
	 * @return Knowledge Base
	 */
	private static KnowledgeBase loadKnowledgeBaseFromPackages()
			throws Exception, Throwable {
		// KnowledgeBuilderConfiguration cfg =
		// KnowledgeBuilderFactory.newKnowledgeBuilderConfiguration();
		// cfg.setProperty("drools.dialect.java.compiler", "JANINO");
		// KnowledgeBuilder kbuilder =
		// KnowledgeBuilderFactory.newKnowledgeBuilder(cfg);

		Properties props = new Properties();
		KnowledgeBuilder kbuilder;
		if (!testMode) {
			System.setProperty("drools.dialect.mvel.strict", "false");
			System.setProperty("drools.dialect.java.compiler", "JANINO");
			props.setProperty("drools.dialect.mvel.strict", "false");
			props.setProperty("drools.dialect.java.compiler", "JANINO");
			KnowledgeBuilderConfiguration cfg = KnowledgeBuilderFactory
					.newKnowledgeBuilderConfiguration(props,
							ClassLoader.getSystemClassLoader());

			// DROOLS 5.3.0
			kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder(cfg);
			// DROOLS 6
			// kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();

			// System.out.println(ResourceFactory.newClassPathResource(".//").toString());
			// System.out.println(rulesEngineBundleContext.getBundle()
			// .getResource("reasoner.drl").getPath());

			// kbuilder.add(ResourceFactory.newUrlResource(rulesEngineBundleContext.getBundle().getResource("uAALrules.drl")),
			// ResourceType.DRL);

			// System.out
			// .println("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<LOS PATHS DEL METAAL>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
			// System.out.println(ResourceFactory
			// .newUrlResource(rulesEngineBundleContext.getBundle()
			// .getResource("uAALrules.drl")));
			String home = new BundleConfigHome("drools.reasoner")
					.getAbsolutePath();
			// String modHome = home.replace("\\", "/");
			// String pathToHome = "path=file:/"
			// + modHome.substring(modHome.indexOf(":") + 1)
			// + "/uAALrules.drl";
			// System.out.println(pathToHome);
			// System.out.println(new BundleConfigHome("drools.reasoner")
			// .getAbsolutePath());
			kbuilder.add(
					ResourceFactory.newFileResource(home + "/uAALrules.drl"),
					ResourceType.DRL);

		} else {
			// props.setProperty("drools.dialect.java.compiler", "JANINO");
			kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
			// System.out.println(ResourceFactory.newClassPathResource(".//").toString());
			// System.out.println(rulesEngineContext.getBundle().getResource("reasoner.drl"));
			kbuilder.add(ResourceFactory.newClassPathResource("reasoner.drl"),
					ResourceType.DRL);
		}
		if (kbuilder.hasErrors()) {
			int nErrors = kbuilder.getErrors().toArray().length;
			StringBuilder sb = new StringBuilder();
			sb.append("Knowledge builder has errors in its inicialization. Do you have a *.drl rule base file in the resources folder?");
			sb.append("\n" + nErrors + " detected:");

			for (int i = 0; i < nErrors; i++) {

				sb.append("\n"
						+ (i + 1)
						+ ") "
						+ ((KnowledgeBuilderError) kbuilder.getErrors()
								.toArray()[i]).getMessage());

			}
			throw new Exception(sb.toString());
		}
		final KnowledgeBaseConfiguration config = KnowledgeBaseFactory
				.newKnowledgeBaseConfiguration();
		config.setOption(EventProcessingOption.STREAM);
		config.setOption(AssertBehaviorOption.EQUALITY);
		config.setProperty("drools.dialect.java.compiler", "JANINO");
		config.setProperty("drools.dialect.mvel.strict", "false");
		final Collection<KnowledgePackage> kpkgs = kbuilder
				.getKnowledgePackages();
		final KnowledgeBase kb = KnowledgeBaseFactory.newKnowledgeBase(config);
		kb.addKnowledgePackages(kpkgs);
		return kb;
	}

	/**
	 * Sends a context events to the rules engine and fire the rules to check if
	 * any rule in the working memory fits with the new conditions.
	 * 
	 * @param event
	 *            ContextEvent to be inserted.
	 */
	public void insertContextEvent(Object event) {
		if (event instanceof ContextEvent) {
			if (((ContextEvent) event).getRDFSubject() instanceof ActivityHubSensor
					|| ((ContextEvent) event).getRDFSubject() instanceof Device) {
				ksession.insert(event);
			} else {
				ContextEvent c = null;
				int a = (Integer)c.getRDFObject();
				
				// c.setpro
				// Not a sensor (genius)
			}
		} else {
			// Not a contextEvent
		}

		// It has been decided to insert always. The rule ALWAYS must check if
		// it's being
		// received the correct class in order to made a cast from Object to a
		// more specific class. This can be easily implemented by placing
		// eval(myReceivedObject instanceof TheClassToBeCast).

	}

	/**
	 * Check if the rules engine was created correctly. TODO Must be improved to
	 * offer richer information if the response is negative.
	 * 
	 * @return true if the rules engine was correctly created; false if not.
	 */
	public boolean isWellFormed() {
		return (ksession != null && kbase != null);

	}

	/**
	 * Setter for the bundle context.
	 * 
	 * @param bc
	 *            The new bundle context.
	 */
	public void setBundleContext(BundleContext bc) {
		// TODO Auto-generated method stub
	}

	/**
	 * Activates the test mode for avoiding using special configuration related
	 * to the OSGi environment. Used to perform JUnit tests.
	 */
	public void enableTestMode() {
		testMode = true;
	}

	/**
	 * Deactivates the test mode for avoiding using special configuration
	 * related to the OSGi environment. Used to perform JUnit tests.
	 */
	public void disableTestMode() {
		testMode = false;
	}

	/**
	 * Publishes a consequence in the ContextBus. This method must be called
	 * from the consequence part of the rules (the part started by "THEN"
	 * statement in the DRL language).
	 * 
	 * @param uri
	 *            URI for the new consequence
	 * @param keys
	 *            Array containing all the keys of the properties. Each key must
	 *            have a value associated. According to this, keys.lenght() and
	 *            values.lenght() must equivalent.
	 * @param values
	 *            Array containing all the values of the properties.
	 * @throws Exception
	 */
	public static void publishConsequence(String uri, String[] keys,
			String[] values) throws Exception {
		if (keys.length != values.length) {
			throw new Exception(
					"keys and value arrays must be of the same length");
		}
		// LogUtils.logInfo(rulesEngineModuleContext, RulesEngine.class,
		// "publishConsequence",
		// new String[] { "Publishing a consequence." }, null);
		ContextPublisher cp;
		ContextProvider info = new ContextProvider();
		ArrayList<ConsequenceProperty> properties = new ArrayList<ConsequenceProperty>();

		Consequence cons = new Consequence(uri);// Commented for test TODO
		// Uncomment
		// Consequence cons = new Consequence();

		for (int i = 0; i < keys.length; i++) {
			// System.out.println("[PUBLISHING_CONSEQUENCE]Adding key" +
			// keys[i]+ "...and value..." + values[i]);
			properties.add(new ConsequenceProperty(
					"http://www.tsbtecnologias.es/ConsequenceProperty.owl#ReasonerConsequence"
							+ i, keys[i], values[i]));
			cons.addProperty(
					"http://www.tsbtecnologias.es/Property.owl#ReasonerConsequence"
							+ i, keys[i], values[i]);
		}
		info = new ContextProvider(
				"http://www.tsbtecnologias.es/ContextProvider.owl#ReasonerNotifier");

		info.setType(ContextProviderType.reasoner);
		info.setProvidedEvents(new ContextEventPattern[] { new ContextEventPattern() });
		cp = new DefaultContextPublisher(rulesEngineModuleContext, info);
		DroolsReasoning dr = new DroolsReasoning(
				"http://www.tsbtecnologias.es/DroolsReasoning.owl#ReasoningConsequence");

		// for (ConsequenceProperty consequenceProperty : properties) {
		// cons.setProperty(Consequence.PROP_HAS_PROPERTIES,
		// consequenceProperty);
		// }

		dr.setProperty(DroolsReasoning.PROP_PRODUCES_CONSEQUENCES, cons);
		cp.publish(new ContextEvent(dr,
				DroolsReasoning.PROP_PRODUCES_CONSEQUENCES));
	}

	public static long getNumberOfElementsInWorkingMemory() {
		return ksession.getFactCount();
	}

	public static Collection<FactHandle> getElementsInWorkingMemory() {
		return ksession.getFactHandles();
	}

	public static void startRulesEngine() {
		try {
			initializeDrools();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Rules engine started from method!!!");
	}

	public static void stopRulesEngine() {
		if (ksession != null) {
			ksession.halt();
		}
		ksession = null;
		System.out.println("Rules engine stopped from method!!!");
	}

	public void restartRulesEngine() {
		stopRulesEngine();
		startRulesEngine();
	}

	/**
	 * Inserts a single rule from a string.
	 * 
	 * @param st
	 *            The rule codification.
	 * @throws Exception
	 */
	private void insertSingleRule(final String st) {
		Properties props = new Properties();
		KnowledgeBuilder auxkbuilder;
		props.setProperty("drools.dialect.java.compiler", "JANINO");
		KnowledgeBuilderConfiguration cfg = KnowledgeBuilderFactory
				.newKnowledgeBuilderConfiguration(props,
						ClassLoader.getSystemClassLoader());
		auxkbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder(cfg);

		final Resource res = ResourceFactory
				.newReaderResource(new StringReader(st));
		auxkbuilder.add(res, ResourceType.DRL);
		// auxkbuilder.add(ResourceFactory.newUrlResource(rulesEngineBundleContext
		// .getBundle().getResource("reasoner.drl")), ResourceType.DRL);

		if (auxkbuilder.hasErrors()) {
			rulesEngineModuleContext.logError("",
					"An error happened when inserting a rule.", null);
			rulesEngineModuleContext.logError("", auxkbuilder.getErrors()
					.toString(), null);

		}
		// Adding rules dynamically
		kbase.addKnowledgePackages(auxkbuilder.getKnowledgePackages());
		System.out.println("Rules added dynamically.");
	}

	/**
	 * Inserts a rule from a string.
	 * 
	 * @param ruleIn
	 *            The rule codification.
	 * @throws Exception
	 */
	public void insertRule(final String ruleIn) {
		System.out.println("INSERTING THE RULE IN THE CALLS RULE ENGINE "
				+ uuid);
		System.out.println(ruleIn);
		ksession.halt();
		// final List<String> descriptors = new ArrayList<String>();

		// final List<String> humanIds = ParseRule.extractHumanIds(ruleIn);
		// for (final String humanId : humanIds) {
		// descriptors.add(humanId);
		// }
		try {

			insertSingleRule(ruleIn);
			ksession.fireUntilHalt();
		} catch (final Exception e) {
			System.out.println(e);
			// Rollback
			// for (final String humanIdRollback : descriptors) {
			// try {
			// removeRule(humanIdRollback);
			// rulesEngineModuleContext.logError("", "Rule removed : "
			// + humanIdRollback, null);
			// } catch (final Exception e1) {
			// rulesEngineModuleContext.logError("",
			// "Error trying to remove rule: " + humanIdRollback,
			// null);
			// }
			// }
			rulesEngineModuleContext.logError("",
					"Error inserting a list of rules. Error in the rules group: "
							+ ruleIn + e, null);
		}

	}

	/**
	 * Inserts a list of rules. In any rule fails,
	 * 
	 * @param rules
	 *            ArrayList
	 * @throws DroolsException
	 *             drools exception.
	 */
	public void insertRuleList(final List<RuleModel> rules) {

		final List<String> descriptors = new ArrayList<String>();
		for (final RuleModel rule : rules) {
			final List<String> humanIds = ParseRule.extractHumanIds(rule
					.getRuleDefinition());
			for (final String humanId : humanIds) {
				descriptors.add(humanId);
			}
			try {
				insertSingleRule(rule.getRuleDefinition());
			} catch (final Exception e) {
				// Rollback
				for (final String humanIdRollback : descriptors) {
					try {
						removeRule(humanIdRollback);
						rulesEngineModuleContext.logDebug("", "Rule removed : "
								+ humanIdRollback, null);
					} catch (final Exception e1) {
						rulesEngineModuleContext.logError("",
								"Error trying to remove rule: "
										+ humanIdRollback, null);
					}
				}
				rulesEngineModuleContext.logError("",
						"Error inserting a list of rules. Error in the rules group: "
								+ rule.getRuleDefinition() + "\n" + e, null);
			}

		}

	}

	/**
	 * Removes a rule from a a specified package.
	 * 
	 * @param name
	 *            Name of the rule
	 * @param pckg
	 *            Package of the rule
	 */
	public void removeRule(final String name, final String pckg) {
		try {
			kbase.removeRule(pckg, name);
		} catch (final Exception e) {
			rulesEngineModuleContext.logError("",
					"Error removing a rule. Have you correctly specified its identifier?\n"
							+ e, null);
		}
	}

	/**
	 * Removes a rule from the default package.
	 * 
	 * @param name
	 *            Name of the rule
	 */
	public void removeRule(final String name) {
		try {
			kbase.removeRule("drools", name);
		} catch (final Exception e) {
			// e.printStackTrace();
			rulesEngineModuleContext.logError("",
					"Error removing a rule. Is you rule in the default package \"drools\"?\n"
							+ e, null);
		}
	}

	public void shutdown() {
		ksession.halt();
		ksession.dispose();
		INSTANCE = null;
	}

	// private void setUpPersistence() {
	//
	// dataSource = new PoolingDataSource();
	// dataSource.setUniqueName("jdbc/testDatasource");
	// dataSource.setMaxPoolSize(5);
	// dataSource.setAllowLocalTransactions(true);
	// try {
	// System.out.println(rulesEngineBundleContext.getBundle().getResource("persistence.xml").getContent().toString());
	// } catch (IOException e1) {
	// e1.printStackTrace();
	// }
	// FileReader entrada1=null;
	// StringBuffer str1=new StringBuffer();
	// try {
	// entrada1=new
	// FileReader(rulesEngineBundleContext.getBundle().getResource("persistence.xml").toExternalForm());
	// int c;
	// while((c=entrada1.read())!=-1){
	// str1.append((char)c);
	// }
	// }catch (IOException ex) {ex.printStackTrace();}
	// System.out.println(str1);
	//
	// ClassLoader tccl = Thread.currentThread().getContextClassLoader();
	// Thread.currentThread().setContextClassLoader(MysqlXADataSource.class.getClassLoader());
	// try {
	// ClassLoaderUtils.loadClass("com.mysql.jdbc.jdbc2.optional.MysqlXADataSource");
	// } catch (ClassNotFoundException e) {
	// System.out.println("No se encuentra");
	// e.printStackTrace();
	// }
	//
	// dataSource.setClassName("com.mysql.jdbc.jdbc2.optional.MysqlXADataSource");
	// dataSource.setMaxPoolSize(3);
	// dataSource.getDriverProperties().put("user", "root");
	// dataSource.getDriverProperties().put("password", "Soluciones_TSB");
	// dataSource.getDriverProperties().put("databaseName", "uaal_ltba");
	// dataSource.getDriverProperties().put("serverName", "localhost");
	//
	//
	// dataSource.init();
	//
	// FileReader entrada=null;
	// StringBuffer str=new StringBuffer();
	// try {
	// entrada=new
	// FileReader("C:\\Desarrollo\\universAAL\\svn\\new_services\\sandboxes\\mllorente\\drools.reasoner\\META-INF\\persistence.xml");
	// int c;
	// while((c=entrada.read())!=-1){
	// str.append((char)c);
	// }
	// }catch (IOException ex) {ex.printStackTrace();}
	// System.out.println(str);
	//
	//
	// Properties prop = System.getProperties();
	// //prop.setProperty("java.class.path", prop.getProperty("java.class.path",
	// null));
	//
	// System.out.println(prop.getProperty("java.class.path", null));
	// try {
	// addFile("C:\\Desarrollo\\universAAL\\svn\\new_services\\sandboxes\\mllorente\\drools.reasoner\\META-INF");
	// } catch (IOException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	// try {
	// addFile("C:\\Desarrollo\\universAAL\\svn\\new_services\\sandboxes\\mllorente\\drools.reasoner\\META-INF\\persistence.xml");
	// } catch (IOException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	//
	// //Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
	//
	// System.out.println(prop.getProperty("java.class.path", null));
	//
	//
	// env = KnowledgeBaseFactory.newEnvironment();
	// emf = Persistence.createEntityManagerFactory("o");
	// env.set(EnvironmentName.ENTITY_MANAGER_FACTORY, emf);
	// env.set(EnvironmentName.TRANSACTION_MANAGER,
	// TransactionManagerServices.getTransactionManager());
	// Thread.currentThread().setContextClassLoader(tccl);
	// }
	//
	// private void initKnowledgeSession(){
	//
	// try{
	// ksession = JPAKnowledgeService.loadStatefulKnowledgeSession(1, kbase,
	// null, env);
	// }catch (Exception e) {
	// System.out.println("No KnowledgeSession previously created. Creating one from scratch.");
	// ksession = kbase.newStatefulKnowledgeSession();
	// }
	//
	// }
	//
	// public static void addFile(String s) throws IOException {
	// File f = new File(s);
	// addFile(f);
	// }//end method
	//
	// public static void addFile(File f) throws IOException {
	// addURL(f.toURL());
	// }//end method
	//
	//
	// public static void addURL(URL u) throws IOException {
	//
	// URLClassLoader sysloader = (URLClassLoader)
	// ClassLoader.getSystemClassLoader();
	// Class sysclass = URLClassLoader.class;
	//
	// try {
	// Method method = sysclass.getDeclaredMethod("addURL", parameters);
	// method.setAccessible(true);
	// method.invoke(sysloader, new Object[]{u});
	// } catch (Throwable t) {
	// t.printStackTrace();
	// throw new IOException("Error, could not add URL to system classloader");
	// }//end try catch
	//
	// }//end method
	//

}
