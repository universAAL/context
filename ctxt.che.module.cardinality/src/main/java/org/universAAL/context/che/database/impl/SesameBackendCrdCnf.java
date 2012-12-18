/*
	Copyright 2014 ITACA-TSB, http://www.tsb.upv.es
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

import java.io.IOException;
import java.io.StringReader;
import java.util.Iterator;

import org.openrdf.OpenRDFException;
import org.openrdf.model.Statement;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.helpers.StatementCollector;
import org.openrdf.rio.turtle.TurtleParser;
import org.universAAL.context.che.Hub;
import org.universAAL.context.che.Hub.Log;
import org.universAAL.middleware.context.ContextEvent;

/**
 * Extension of
 * {@link org.universAAL.context.che.database.impl.SesameBackendCrd} that
 * interprets the confidence value of received events before storing them. If
 * the confidence is greater than the threshold passed to this class in the
 * constructor or <code>setThreshold</code>, the event will be stored unchanged
 * as in {@link org.universAAL.context.che.database.impl.SesameBackend}.
 * Otherwise, only statements having the event as subject will be stored, but
 * not reified statements about its subject nor object.
 * 
 * Example:
 * 
 * An "event1" with "subject2" "predicate3" and "object4" with enough confidence
 * will result in having the statements in the store: "event1" "hasSubject"
 * "subject2" "event1" "hasPredicate" "predicate3" "event1" "hasObject"
 * "object4" "subject2" "predicate3" "object4"
 * 
 * But if the confidence is below the threshold, the last reified statement is
 * not stored.
 * 
 * @author alfiva
 * 
 */
public class SesameBackendCrdCnf extends SesameBackendCrd {
    /**
     * Logger.
     */
    private static final Log log = Hub.getLog(SesameBackendCrdClc2Cnf.class);
    /**
     * Confidence threshold.
     */
    private int threshold = 0;

    /**
     * Default constructor.
     */
    public SesameBackendCrdCnf() {
	super();
	String conf = Hub.getProperties().getProperty("STORE.CONFIDENCE");
	if (conf != null) {
	    try {
		setThreshold(Integer.parseInt(conf));
	    } catch (Exception e) {
		log.error("init", "Invalid confidence threshold. Using 0.", e);
		setThreshold(0);
	    }

	} else {
	    setThreshold(0);
	}
    }

    /**
     * Constructor with confidence.
     * 
     * @param confidence
     *            0 to 100.
     */
    public SesameBackendCrdCnf(int confidence) {
	super();
	this.setThreshold(confidence);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.universAAL.context.che.database.impl.SesameBackend#storeEvent(org
     * .universAAL.middleware.context.ContextEvent)
     */
    @Override
    public void storeEvent(ContextEvent e) {
	try {
	    RepositoryConnection con = myRepository.getConnection();
	    try {
		log.debug("storeEvent",
			"Adding event to store, if enough confidence");
		Integer conf = e.getConfidence();
		if (conf != null) {
		    if (conf.intValue() < threshold) {
			TurtleParser sesameParser = new TurtleParser();
			StatementCollector stHandler = new StatementCollector();
			sesameParser.setRDFHandler(stHandler);
			sesameParser.parse(
				new StringReader(uAALParser.serialize(e)),
				e.getURI());
			Iterator<Statement> sts = stHandler.getStatements()
				.iterator();
			while (sts.hasNext()) {
			    Statement st = sts.next();
			    if (st.getSubject().stringValue()
				    .equals(e.getURI())) {
				con.add(st);
				// store only stmts having event as subject
			    }
			}
			log.info("storeEvent",
				"CHe: Stored a Context Event with "
					+ "low Confidence: Not reified.");
		    } else {
			con.add(new StringReader(uAALParser.serialize(e)),
				e.getURI(), RDFFormat.TURTLE);
			log.info("storeEvent", "CHe: Stored a Context Event"
				+ " with high Confidence");
		    }
		} else { // TODO: What to do if events have no confidence?
		    con.add(new StringReader(uAALParser.serialize(e)),
			    e.getURI(), RDFFormat.TURTLE);
		    log.info("storeEvent",
			    "CHe: Stored a Context Event without Confidence");
		}
		log.debug("storeEvent", "Successfully added event to store");
	    } catch (IOException exc) {
		log.error("storeEvent",
			"Error trying to add event to the store. "
				+ "In older versions this usually"
				+ " happened because of the underlying"
				+ " connection closing due to inactivity,"
				+ " but now it is because: {}", exc);
		exc.printStackTrace();
	    } finally {
		con.close();
	    }
	} catch (OpenRDFException exc) {
	    log.error("storeEvent",
		    "Error trying to get connection to store: {}", exc);
	    exc.printStackTrace();
	}
    }

    /**
     * Get confidence threshold.
     * 
     * @return Confidence.
     */
    public int getThreshold() {
	return threshold;
    }

    /**
     * Set confidence threshold.
     * 
     * @param threshold
     *            Confidence.
     */
    public void setThreshold(int thr) {
	if (threshold < 100) {
	    this.threshold = thr;
	} else {
	    this.threshold = 100;
	}

    }

}
