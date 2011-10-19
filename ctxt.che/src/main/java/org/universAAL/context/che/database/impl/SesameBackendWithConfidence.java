package org.universAAL.context.che.database.impl;

import java.io.IOException;
import java.io.StringReader;
import java.util.Iterator;

import org.openrdf.OpenRDFException;
import org.openrdf.model.Statement;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.helpers.StatementCollector;
import org.openrdf.rio.turtle.TurtleParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.universAAL.context.che.Activator;
import org.universAAL.middleware.context.ContextEvent;

/**
 * Extension of {@link org.universAAL.context.che.database.impl.SesameBackend}
 * that interprets the confidence value of received events before storing them.
 * If the confidence is greater than the threshold passed to this class in the
 * constructor or <code>setThreshold</code>, the event will be stored unchanged
 * as in {@link org.universAAL.context.che.database.impl.SesameBackend}.
 * Otherwise, only statements having the event as subject will be stored, but
 * not reified statements about its subject nor object.
 * 
 * Example:
 * 
 * An "event1" with "subject2" "predicate3" and "object4" with enough confidence
 * will result in having the statements in the store:
 * "event1" "hasSubject" "subject2"
 * "event1" "hasPredicate" "predicate3"
 * "event1" "hasObject" "object4"
 * "subject2" "predicate3" "object4"
 * 
 * But if the confidence is below the threshold, the last reified statement is
 * not stored.
 * 
 * @author alfiva
 * 
 */
public class SesameBackendWithConfidence extends SesameBackend{
    private final static Logger log = LoggerFactory
	    .getLogger(SesameBackendWithConfidence.class);
    private int threshold = 0;
    
    public SesameBackendWithConfidence(){
	super();
	String conf=Activator.getProperties().getProperty("STORE.CONFIDENCE");
	if(conf!=null){
	    try{
		setThreshold(Integer.parseInt(conf));
	    }catch (Exception e) {
		log.error("Invalid confidence threshold. Using 0.",e);
		setThreshold(0);
	    }
	    
	}else{
	    setThreshold(0);
	}
    }
    
    public SesameBackendWithConfidence(int confidence){
	super();
	this.setThreshold(confidence);
    }
    
    @Override
    public void storeEvent(ContextEvent e) {
	try {
	    RepositoryConnection con = myRepository.getConnection();
	    try {
		log.debug("Adding event to store, if enough confidence");
		Integer conf = e.getConfidence();
		if (conf != null) {
		    if (conf.intValue() < threshold) {
			TurtleParser sesameParser=new TurtleParser();
			StatementCollector stHandler=new StatementCollector();
			sesameParser.setRDFHandler(stHandler);
			sesameParser.parse(new StringReader(uAALParser.serialize(e)), e.getURI());
			Iterator<Statement> sts=stHandler.getStatements().iterator();
			while(sts.hasNext()){
			    Statement st=sts.next();
			    if(st.getSubject().stringValue().equals(e.getURI())){
				con.add(st);//store only stmts having event as subject
			    }
			}
			log.info("CHe: Stored a Context Event with low Confidence: Not reified.");
		    }else{
			con.add(new StringReader(uAALParser.serialize(e)), e.getURI(),
				RDFFormat.TURTLE);
			log.info("CHe: Stored a Context Event with high Confidence");
		    }
		} else {//TODO: What to do if events have no confidence?
		    con.add(new StringReader(uAALParser.serialize(e)), e.getURI(),
			RDFFormat.TURTLE);
		    log.info("CHe: Stored a Context Event without Confidence");
		}
		log.debug("Successfully added event to store");
	    } catch (IOException exc) {
		log.error("Error trying to add event to the store. "
			+ "In older versions this usually happened because "
			+ "of the underlying connection closing due to "
			+ "inactivity, but now it is because: {}", exc);
		exc.printStackTrace();
	    } finally {
		con.close();
	    }
	} catch (OpenRDFException exc) {
	    log.error("Error trying to get connection to store: {}",exc);
	    exc.printStackTrace();
	}
    }
    
    public int getThreshold() {
	return threshold;
    }

    public void setThreshold(int threshold) {
	if (threshold < 100) {
	    this.threshold = threshold;
	} else {
	    this.threshold = 100;
	}

    }

}
