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
package org.universAAL.context.che.database.impl;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.StringWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Properties;

import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;


import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.db.DBConnection;
import com.hp.hpl.jena.db.ModelRDB;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ModelMaker;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.ReifiedStatement;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.expr.E_GreaterThanOrEqual;
import com.hp.hpl.jena.sparql.expr.E_LessThanOrEqual;
import com.hp.hpl.jena.sparql.expr.E_LogicalAnd;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.expr.ExprVar;
import com.hp.hpl.jena.sparql.expr.NodeValue;
import com.hp.hpl.jena.sparql.syntax.ElementFilter;
import com.hp.hpl.jena.sparql.syntax.ElementGroup;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.reflection.Sun14ReflectionProvider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.universAAL.context.che.Activator;
import org.universAAL.context.che.database.Backend;
import org.universAAL.middleware.context.ContextEvent;
import org.universAAL.middleware.context.owl.ContextProvider;
import org.universAAL.middleware.rdf.Resource;

/**
 * @author <a href="mailto:alfiva@itaca.upv.es">Alvaro Fides Valero</a>
 * 
 *         Implementation of
 *         {@link org.universAAL.context.che.database.Backend} that uses Jena
 *         DB to store and retrieve the context events in/from an underlying
 *         database server.
 *
 */
public class JenaDBBackend implements Backend {
	
	private static Model m_model = ModelFactory.createDefaultModel();
	public static final String  NS = "http://purl.org/dc/elements/1.1/";
	public static final com.hp.hpl.jena.rdf.model.Resource NAMESPACE = m_model.createResource( NS );
	public static final Property rdfsubject = m_model.createProperty( "http://www.w3.org/1999/02/22-rdf-syntax-ns#subject" );
	public static final Property rdfpredicate = m_model.createProperty( "http://www.w3.org/1999/02/22-rdf-syntax-ns#predicate" );
	public static final Property rdfobject = m_model.createProperty( "http://www.w3.org/1999/02/22-rdf-syntax-ns#object" );
	public static final Property rdftype = m_model.createProperty( "http://www.w3.org/1999/02/22-rdf-syntax-ns#type" );
	public static final Property rdfstatement = m_model.createProperty( "http://www.w3.org/1999/02/22-rdf-syntax-ns#Statement" );
	public static final Property xsdinteger = m_model.createProperty( "http://www.w3.org/2001/XMLSchema#integer" );
	public static final Property xsdfloat = m_model.createProperty( "http://www.w3.org/2001/XMLSchema#float" );
	public static final Property xsdlong = m_model.createProperty( "http://www.w3.org/2001/XMLSchema#long" );
	public static final Property xsddouble = m_model.createProperty( "http://www.w3.org/2001/XMLSchema#double" );
	public static final Property xsdboolean = m_model.createProperty( "http://www.w3.org/2001/XMLSchema#boolean" );
	public static final Property xsdstring = m_model.createProperty( "http://www.w3.org/2001/XMLSchema#string" );
	public static final Property xsdduration = m_model.createProperty( "http://www.w3.org/2001/XMLSchema#duration" );
	public static final Property xsddatetime = m_model.createProperty( "http://www.w3.org/2001/XMLSchema#dateTime" );
	public static final Property xsdlanguage = m_model.createProperty( "http://www.w3.org/2001/XMLSchema#language" );
	public static final Property ctxtcontextevent = m_model.createProperty( "http://ontology.universAAL.org/Context.owl#ContextEvent" );
	public static final Property ctxthastimestamp = m_model.createProperty( "http://ontology.universAAL.org/Context.owl#hasTimestamp" );
	public static final Property ctxthasaccuracy = m_model.createProperty( "http://ontology.universAAL.org/Context.owl#hasAccuracy" );
	public static final Property ctxthasconfidence = m_model.createProperty( "http://ontology.universAAL.org/Context.owl#hasConfidence" );
	public static final Property ctxthasexpirationtime = m_model.createProperty( "http://ontology.universAAL.org/Context.owl#hasExpirationTime" );
	public static final Property ctxthasprovider = m_model.createProperty( "http://ontology.universAAL.org/Context.owl#hasProvider" );
	
	private static final boolean DEBUG_DB = false;//Set to true to refill the DB every time (for debugging)
	private String DB_URL;
	private String DB_USER;
	private String DB_PWD;
	private String DB_TYPE;
	private String MODEL_NAME="ContextHistoryModel";//TODO CHANGE FOR URI IN THE FUTURE
	
	private final static Logger log=LoggerFactory.getLogger(JenaDBBackend.class);
	
	private DBConnection conn;
	private ModelRDB CHModel;
	private Object theLock;
	private Object theFileLock;
	
	/**
	 * Constructor that establishes the connection to the database with the
	 * given parameters. Also initiates synchronization with the PMD history, if
	 * available.
	 * 
	 * @param url
	 *            The URL to the database
	 * @param user
	 *            User for authentication
	 * @param pwd
	 *            Password for authentication
	 * @param dbType
	 *            Type of database (MySQL, HSQL, Derby, Oracle or others)
	 */
	public JenaDBBackend(String url, String user, String pwd, String dbType){
		Properties props=Activator.getProperties();
		this.DB_URL=url;
		props.setProperty("DB.URL", DB_URL);
		this.DB_USER=user;
		props.setProperty("DB.USER", DB_USER);
		this.DB_PWD=pwd;
		props.setProperty("DB.PWD", DB_PWD);
		this.DB_TYPE=dbType;
		props.setProperty("DB.TYPE", DB_TYPE);
		this.MODEL_NAME=props.getProperty("MODEL.NAME");
		Activator.setProperties(props);
		this.theLock=new Object();
		this.theFileLock=new Object();
		connect();
		synchronizePMDDB();
	}
	
	/**
	 * Constructor that establishes the connection to the database with the
	 * default parameters. Also initiates synchronization with the PMD history,
	 * if available.
	 */
	public JenaDBBackend(){
		Properties prop=Activator.getProperties();
		this.DB_URL=prop.getProperty("DB.URL");
		this.DB_USER=prop.getProperty("DB.USER");
		this.DB_PWD=prop.getProperty("DB.PWD");
		this.DB_TYPE=prop.getProperty("DB.TYPE");
		this.MODEL_NAME=prop.getProperty("MODEL.NAME");
		this.theLock=new Object();
		this.theFileLock=new Object();
		connect();
		synchronizePMDDB();
	}
	
	public Object getLock(){
		return theLock;
	}
	
	public Object getFileLock(){
		return theFileLock;
	}
	
	public void connect(){
		try{
			Class.forName("com.mysql.jdbc.Driver");
			conn=new DBConnection(DB_URL,DB_USER,DB_PWD,DB_TYPE); 
			log.info("CHe connects to {} ",DB_URL);
			initDB();
		}catch(Exception e){
			log.error("Exception trying to get connection to database: {} ",e);
		}
	}
	
	/**
	 * Checks if the database is already formatted for Jena and if not, creates
	 * all the needed tables.
	 * 
	 * @throws SQLException
	 */
	private void initDB() throws SQLException{
		ModelMaker maker=ModelFactory.createModelRDBMaker(conn, ModelFactory.Standard);
		if(!conn.containsModel(MODEL_NAME)){
			if(!conn.isFormatOK()){
				//DB initialization options here, if any
			}
			log.debug("Database not formatted, creating a new one");
			CHModel=(ModelRDB) maker.createModel(MODEL_NAME);
		}else{
			if(DEBUG_DB){
				log.debug("DEBUG MODE: Cleaning and resetting database");
				conn.cleanDB();
				//DB access options here, if any
				CHModel=(ModelRDB) maker.createModel(MODEL_NAME);
			}else{
				log.debug("Database OK, opening CHe model: {} ",MODEL_NAME);
				//DB access options here, if any
				try{
					CHModel=(ModelRDB) maker.openModel(MODEL_NAME);
					log.info("Database Model open. CHe ready to store events");
				} catch (Exception e) {
					log.error("Could not open the model {}. CHe will not store events. Solve the problem and then restart CHe.\n" +
							"Make sure the model in the database was created by the CHe. " +
							"If it was created by other means, it should have been confuigured to reify statements, " +
							"and the system table in Jena DB should contain the JENA_DEFAULT_GRAPH_PROPERTIES model. " +
							"This will have probably been the error: {} ",new Object[]{MODEL_NAME,e});
				}
			}
		}
	}
	
	/**
	 * Closes the connection to the database. It is recommended to use
	 * <code>close()</code> instead.
	 */
	public void disconnect(){
		try {
			conn.close();
		}catch (SQLException e) {
			log.error("Exception trying to close connection to database: {} ",e);
		}
	}

	public void close() {
		log.info("Closing connection to database");
		CHModel.close();
		disconnect();
	}
	
	public void storeEvent(ContextEvent e) {
		Model m=Activator.converter.toJenaResource(e).getModel();
		//Lock the access (better with transaction?). Might not be needed for SINGLE GRAPH MODE
		synchronized(this.getLock()){
			try {
				log.debug("Adding event to database");
				CHModel.add(m);
				log.debug("Successfully added event to database");
			} catch (Exception ex) {//TODO Specialize to CommunicationsException
				log.error("Error trying to get connection to database while trying to store an event: {} \n " +
						"Che will try to reconnect in case the connection was lost for some reason (like inactivity) ",ex);
				try {
					if(!conn.getConnection().isValid(0))connect();//Test if connection is open and connect
					log.debug("Adding event to database after reconnection");
					CHModel.add(m);
					log.debug("Successfully added event to database after reconnection");
				} catch (SQLException e1) {
					log.error("Reconnection attempt failed due to {} ",e1);
				} catch (Exception e2) {
					log.error("Reconnection MAY have succeeded but could not store the event due to {} ",e2);
				}
			}
		}
		
	}

	//Not used in current version. Its functionality is provided by "From" and "To" methods
	//with timestamp=0.
	public ArrayList retrieveEvent(String sub, String subType, String pred,
			Object obj, Integer acc, Integer conf, Long ex, Object cp,
			Long tstamp) {

		Query query = prepareQuery(sub,subType,pred,obj,acc,conf,ex,cp,tstamp) ;

		return queryEvents(query);
	}
	
	public String queryBySPARQL(String queryStr) {
		Model resultsModel=ModelFactory.createDefaultModel();
		synchronized(getLock()){
			log.info("Querying by SPARQL");
			try {
				if(!conn.getConnection().isValid(0))connect();//Test if connection is open and connect
			} catch (SQLException e) {
				log.error("Error trying to get connection to database while trying to execute SPARQL query: {} ",e.getStackTrace());
				return null;
			}
			Query query = QueryFactory.create(queryStr) ;
			QueryExecution qexec = QueryExecutionFactory.create(query, CHModel) ;
			try {
				if(query.isSelectType()){
					ResultSet results = qexec.execSelect() ;
					ResultSetFormatter.asRDF(resultsModel, results);
				}else if(query.isConstructType()){
					resultsModel = qexec.execConstruct() ;
				}else if(query.isDescribeType()){
					resultsModel = qexec.execDescribe();
				}else if(query.isAskType()){
					boolean results = qexec.execAsk() ;
					ResultSetFormatter.asRDF(resultsModel, results);
				}
			} finally { qexec.close() ; }
		}
		StringWriter sw = new StringWriter(4096);
		resultsModel.write(sw, "RDF/XML-ABBREV");//TO DO: Analyze convenience of other formats
		return sw.toString();
	}
	
	public ArrayList retrieveEventsBetweenTstmp(String sub, String typ,
			String pre, Object obj, Integer acc, Integer con, Long exp,
			ContextProvider cop, Long tst, Long tstinput1, Long tstinput2) {
		
		Query query = prepareQuery(sub,typ,pre,obj,acc,con,exp,cop,tst) ;
		ElementGroup elg=(ElementGroup)query.getQueryPattern();
		Var varT = Var.alloc("t") ;
		Var varC = Var.alloc("c") ;
		Triple t = new Triple(varC, ctxthastimestamp.asNode(), varT) ;
		elg.addTriplePattern(t) ;

        Expr exprGreat = new E_GreaterThanOrEqual(new ExprVar(varT), NodeValue.makeDecimal(((Long)tstinput1).longValue())) ;
        Expr exprLess = new E_LessThanOrEqual(new ExprVar(varT), NodeValue.makeDecimal(((Long)tstinput2).longValue())) ;
        Expr exprAnd = new E_LogicalAnd(exprGreat,exprLess);
        ElementFilter filter = new  ElementFilter(exprAnd) ;
        elg.addElementFilter(filter) ;
        query.setQueryPattern(elg);

        log.debug("CHe built the query: {} ",query.serialize());
		return queryEvents(query);
	}

	public ArrayList retrieveEventsFromTstmp(String sub, String typ,
			String pre, Object obj, Integer acc, Integer con, Long exp,
			ContextProvider cop, Long tst, Long tstinput) {

		Query query = prepareQuery(sub,typ,pre,obj,acc,con,exp,cop,tst) ;
		if (tstinput.compareTo(new Long(0))!=0) {
			ElementGroup elg = (ElementGroup) query.getQueryPattern();
			Var varT = Var.alloc("t");
			Var varC = Var.alloc("c");
			Triple t = new Triple(varC, ctxthastimestamp.asNode(), varT);
			elg.addTriplePattern(t);
			
			Expr expr = new E_GreaterThanOrEqual(
					new ExprVar(varT), NodeValue.makeDecimal((tstinput).longValue()));
			ElementFilter filter = new ElementFilter(expr);
			elg.addElementFilter(filter);
			query.setQueryPattern(elg);
		}
		log.debug("CHe built the query: {} ",query.serialize());

		return queryEvents(query);
	}

	public ArrayList retrieveEventsToTstmp(String sub, String typ, String pre,
			Object obj, Integer acc, Integer con, Long exp,
			ContextProvider cop, Long tst, Long tstinput) {
		
		Query query = prepareQuery(sub,typ,pre,obj,acc,con,exp,cop,tst) ;
		if (tstinput.compareTo(new Long(0))!=0) {
			ElementGroup elg=(ElementGroup)query.getQueryPattern();
			Var varT = Var.alloc("t") ;
			Var varC = Var.alloc("c") ;
			Triple t = new Triple(varC, ctxthastimestamp.asNode(), varT) ;
			elg.addTriplePattern(t) ;
	
	        Expr expr = new E_LessThanOrEqual(
	        		new ExprVar(varT), NodeValue.makeDecimal((tstinput).longValue())) ;
	        ElementFilter filter = new  ElementFilter(expr) ;
	        elg.addElementFilter(filter) ;
	        query.setQueryPattern(elg);
		}
		log.debug("CHe built the query: {} ",query.serialize());

		return queryEvents(query);
	}
	
	private Query prepareQuery(String sub, String typ, String pred,
			Object obj, Integer acc, Integer conf, Long ex,
			Object cp, Long tstamp){
		
		ContextProvider cProv;
		if(cp instanceof ContextProvider){
			cProv = (ContextProvider)cp;
		}else{
			cProv = null;
		}
		
		Query query = QueryFactory.make() ;

        query.setQueryType(Query.QueryTypeSelect) ;
        
        ElementGroup elg = new ElementGroup() ;
        
        Var varC = Var.alloc("c") ;
        
        
        Node objQuery;
		if(obj instanceof Resource){
			objQuery=Node.createURI(((Resource)obj).getType());
		}else if(obj instanceof Integer){
			objQuery=Node.createLiteral(((Integer)obj).toString(), null, XSDDatatype.XSDint);
		}else if(obj instanceof Float){
			objQuery=Node.createLiteral(((Float)obj).toString(), null, XSDDatatype.XSDfloat);
		}else if(obj instanceof Long){
			objQuery=Node.createLiteral(((Long)obj).toString(), null, XSDDatatype.XSDlong);
		}else if(obj instanceof Double){
			objQuery=Node.createLiteral(((Double)obj).toString(), null, XSDDatatype.XSDdouble);
		}else if(obj instanceof Boolean){
			objQuery=Node.createLiteral(((Boolean)obj).toString(), null, XSDDatatype.XSDboolean);
		}else if(obj instanceof String){
			objQuery=Node.createLiteral(((String)obj), null, XSDDatatype.XSDstring);
		}else if(obj instanceof Duration){
			objQuery=Node.createLiteral(((Duration)obj).toString(), null, XSDDatatype.XSDduration);
		}else if(obj instanceof XMLGregorianCalendar){
			objQuery=Node.createLiteral(((XMLGregorianCalendar)obj).toString(), null, XSDDatatype.XSDdateTime);
		}else if(obj instanceof Locale){
			objQuery=Node.createLiteral(((Locale)obj).toString(), null, XSDDatatype.XSDlanguage);
		}else{
			obj=null;
			objQuery=Node.createAnon();
		}
		
        Triple t = new Triple(varC, rdftype.asNode(),  ctxtcontextevent.asNode()) ;
        elg.addTriplePattern(t) ;
        
		if (sub!=null){
			t = new Triple(varC, rdfsubject.asNode(), Node.createURI(sub)) ;
			elg.addTriplePattern(t) ;
		}else if (typ!=null){
			Var varAux = Var.alloc("s") ;
			t = new Triple(varC, rdfsubject.asNode(), varAux) ;
			elg.addTriplePattern(t) ;
			t = new Triple(varAux, rdftype.asNode(), Node.createURI(typ)) ;
			elg.addTriplePattern(t) ;
		}
		if (pred!=null){
			t = new Triple(varC, rdfpredicate.asNode(), Node.createURI(pred)) ;
			elg.addTriplePattern(t) ;
		}
		if (obj!=null){
			if (obj instanceof Resource){
				Var varAux = Var.alloc("a") ;
				t = new Triple(varC, rdfobject.asNode(), varAux) ;
				elg.addTriplePattern(t) ;
				t = new Triple(varAux, rdftype.asNode(), objQuery) ;
				elg.addTriplePattern(t) ;
			}else{
				t = new Triple(varC, rdfobject.asNode(), objQuery) ;
				elg.addTriplePattern(t) ;
			}

		}
		if (tstamp!=null){
			t = new Triple(varC, ctxthastimestamp.asNode(), Node.createLiteral((tstamp).toString(), null, XSDDatatype.XSDlong)) ;
			elg.addTriplePattern(t) ;
		}
		if (acc!=null){
			t = new Triple(varC, ctxthasaccuracy.asNode(), Node.createLiteral((acc).toString(), null, XSDDatatype.XSDint)) ;
			elg.addTriplePattern(t) ;
		}
		if (conf!=null){
			t = new Triple(varC, ctxthasconfidence.asNode(), Node.createLiteral((conf).toString(), null, XSDDatatype.XSDint)) ;
			elg.addTriplePattern(t) ;
		}
		if (ex!=null){
			t = new Triple(varC, ctxthasexpirationtime.asNode(), Node.createLiteral((ex).toString(), null, XSDDatatype.XSDlong)) ;
			elg.addTriplePattern(t) ;
		}
		if (cProv!=null){
			t = new Triple(varC, rdfpredicate.asNode(), Node.createURI(cProv.getURI())) ;
			elg.addTriplePattern(t) ;
		}
        
        query.setQueryPattern(elg) ;
        query.addResultVar(varC) ;
        
        log.debug("CHe built the query: {} ",query.serialize());
        return query;
	}

	private ArrayList queryEvents(Query query){
		ArrayList solution=new ArrayList();
		synchronized(getLock()){
			log.info("Querying for Events");
			try {
				if(!conn.getConnection().isValid(0))connect();//Test if connection is open and connect
			} catch (SQLException e) {
				log.error("Error trying to get connection to database while trying to execute query: {} ",e.getStackTrace());
				return null;
			}
			QueryExecution qexec = QueryExecutionFactory.create(query, CHModel) ;
			
			try {
				ResultSet results = qexec.execSelect() ;
				for ( ; results.hasNext() ; )    {
					QuerySolution soln = results.nextSolution() ;
					com.hp.hpl.jena.rdf.model.Resource x = soln.getResource("c") ;
					solution.add((ContextEvent) Activator.converter.toPersonaResource(x));	
				}  
			} finally { qexec.close() ; }
		}
		return solution;
	}

	public ArrayList retrieveEventsBySPARQL(String input) {
		Query query = QueryFactory.create(input) ;
		return queryEvents(query);
	}
	
	/**
	 * Performs the synchronization with the history gathered by the PMD while
	 * outside. It only works if the file from the PMD containing the history
	 * has already been downloaded to the running directory.
	 * 
	 * @return <code>true</code> if the synchronization succeeded.
	 */
	private boolean synchronizePMDDB() {
		log.info("Synchronizing with PMD events");
		XStream xs = new XStream(new Sun14ReflectionProvider());
		xs.setClassLoader(Activator.class.getClassLoader());
		ContextEvent ev=null;
		synchronized (getFileLock()) {
			try {
				Properties prop = Activator.getProperties();
				String lastKnownOf = prop.getProperty("PMD.LastKnownOfTst");
				String filePMD = prop.getProperty("PMD.StorageFile");
				String flag = prop.getProperty("PMD.BorderFlag");
				long lKO = 0;
				if (lastKnownOf != null) {
					log.info("PMD events were last synchronized in {} ",lastKnownOf);
					lKO = Long.parseLong(lastKnownOf);
				}
				String readline = "";
				String xmlIn = "";
				BufferedReader br = new BufferedReader(new FileReader(
						filePMD));
				
				readline=br.readLine();
				while (readline != null){
					while (readline!=null&&!readline.equals(flag)) {
						xmlIn += readline;
						readline = br.readLine();
					}
					if(!xmlIn.isEmpty()){
						ev = (ContextEvent) xs.fromXML(xmlIn);
						if (lKO < ev.getTimestamp().longValue())
							log.debug("Parsed an event from PMD file, storing in DB");
							storeEvent(ev);
					}
					xmlIn = "";
					readline=br.readLine();
				}
				if(ev!=null){
					log.info("PMD events are now synchronized up to {} ", ev.getTimestamp().toString());
					prop.setProperty("PMD.LastKnownOfTst", ev.getTimestamp().toString());
					Activator.setProperties(prop);
				}

			} catch (FileNotFoundException e) {
				log.error("Could not find the PMD events file, synchronization will not take place. {} ",e.getMessage());
				return false;
			}catch (Exception e) {
				log.error("Error processing the PMD events file: {}",e);
				return false;
			}
		}
		return true;
	}
	
	public void removeOldEvents(long tst) {
		String query1 = "SELECT ?n WHERE {"
				+ "?db <http://jena.hpl.hp.com/2003/04/DB#StmtTable> ?n;"
				+ "<http://jena.hpl.hp.com/2003/04/DB#GraphName> \""
				+ MODEL_NAME + "\".}";
		String tablename = "jena_g1t1_stmt";
		synchronized (getLock()) {
			log.info("Getting name of table to remove events");
			try {
				if(!conn.getConnection().isValid(0))connect();//Test if connection is open and connect
			} catch (SQLException e) {
				log.error("Error trying to get connection to database while trying to remove events: {} ",e.getStackTrace());
				return;
			}
			QueryExecution qexec = QueryExecutionFactory.create(query1, conn
					.getDatabaseProperties());
			try {
				ResultSet results = qexec.execSelect();
				for (; results.hasNext();) {
					QuerySolution soln = results.nextSolution();
					Literal x = soln.getLiteral("n");
					tablename = x.getString();
				}
			} catch (Exception e) {
				log.error("An error occured while getting the name of the statements table. Using "
								+ tablename + " by default.");
			} finally {
				qexec.close();
			}
		}

		Query query = QueryFactory.make();
		query.setQueryType(Query.QueryTypeSelect);
		ElementGroup elg = new ElementGroup();
		Var varT = Var.alloc("t");
		Var varC = Var.alloc("c");
		Triple t1 = new Triple(varC, rdftype.asNode(), ctxtcontextevent
				.asNode());
		elg.addTriplePattern(t1);
		Triple t2 = new Triple(varC, ctxthastimestamp.asNode(), varT);
		elg.addTriplePattern(t2);
		query.addResultVar(varC);
		Expr expr = new E_LessThanOrEqual(new ExprVar(varT), NodeValue
				.makeDecimal(tst));
		ElementFilter filter = new ElementFilter(expr);
		elg.addElementFilter(filter);
		query.setQueryPattern(elg);
System.out.println(query.toString());
		synchronized (getLock()) {
			log.info("Removing Events prior to {} ",Long.toString(tst));
			try {
				if(!conn.getConnection().isValid(0))connect();//Test if connection is open and connect
			} catch (SQLException e) {
				log.error("Error trying to get connection to database while trying to remove events: {} ",e.getStackTrace());
				return;
			}
			QueryExecution qexec = QueryExecutionFactory.create(query, CHModel);
			try {
				ResultSet results = qexec.execSelect();
				for (; results.hasNext();) {
					QuerySolution soln = results.nextSolution();
					com.hp.hpl.jena.rdf.model.Resource x = soln.getResource("c");
					x.getProperty(rdftype).changeObject(rdfstatement);
					x.getProperty(rdftype);
					ReifiedStatement r = (ReifiedStatement) x
							.as(ReifiedStatement.class);
					CHModel.removeReification(r);
					java.sql.Statement s = conn.getConnection()
							.createStatement();
					s.execute("DELETE FROM " + tablename
							+ " WHERE Subj LIKE '%" + x.toString() + "%'");
				}
			} catch (SQLException e) {
				log.error("Exception trying to get connection to database: {} " ,e);
			} finally {
				qexec.close();
			}
		}
	}

}
