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

import java.util.ArrayList;

import org.universAAL.middleware.context.ContextEvent;
import org.universAAL.middleware.context.owl.ContextProvider;

/**
 * @author <a href="mailto:alfiva@itaca.upv.es">Alvaro Fides Valero</a>
 * 
 *        Interface that represents a database back end where the context
 *         history is stored.
 */
public interface Backend {

    /**
     * Stores a {@link org.persona.middleware.context.ContextEvent} in the
     * underlying database.
     * 
     * @param e
     *            The context event to be stored.
     */
    public void storeEvent(ContextEvent e);

    // Not used in current version
    /**
     * Retrieves a list of {@link org.persona.middleware.context.ContextEvent}
     * from the underlying database, which members are the context events that
     * match the parameters passed. A parameter can be passed a value of
     * <code>null</code> for wildcarding.
     * <p>
     * This is not used in the current version. Will probably be deprecated.
     * 
     * @param sub
     *            The URI of the subject of the event to be matched
     * @param subType
     *            The URI of the type of subject of the event to be matched
     * @param pred
     *            The URI of the predicate of the event to be matched
     * @param obj
     *            The Object of the event to be matched. It depends on the
     *            implementer of this method how to match the object.
     * @param acc
     *            The accuracy of the event to be matched
     * @param conf
     *            The confidence of the event to be matched
     * @param ex
     *            The expiration time of the event to be matched
     * @param cp
     *            The context provider of the event to be matched
     * @param tstamp
     *            The timestamp in milliseconds of the event to be matched
     * @return The list of context events that matched the values passed as
     *         parameters.
     */
    public ArrayList retrieveEvent(String sub, String subType, String pred,
	    Object obj, Integer acc, Integer conf, Long ex, Object cp,
	    Long tstamp);

    /**
     * Retrieves a list of {@link org.persona.middleware.context.ContextEvent}
     * from the underlying database, which members are the context events that
     * match the parameters passed, and were received after the specified
     * timestamp. A parameter can be passed a value of <code>null</code> for
     * wildcarding.
     * 
     * @param sub
     *            The URI of the subject of the event to be matched
     * @param typ
     *            The URI of the type of subject of the event to be matched
     * @param pre
     *            The URI of the predicate of the event to be matched
     * @param obj
     *            The Object of the event to be matched. It depends on the
     *            implementer of this method how to match the object.
     * @param acc
     *            The accuracy of the event to be matched
     * @param con
     *            The confidence of the event to be matched
     * @param exp
     *            The expiration time of the event to be matched
     * @param cop
     *            The context provider of the event to be matched
     * @param tst
     *            The timestamp in milliseconds of the event to be matched. If
     *            not <code>null</code>, only events with this specified
     *            timestamp will be returned, as long as it is inside the time
     *            range specified by <code>tstinputfrom</code>
     * @param tstinputfrom
     *            The timestamp in milliseconds from which events are requested.
     *            Only events stored after this timestamp will be returned.
     * @return The list of context events that matched the values passed as
     *         parameters.
     */
    public ArrayList retrieveEventsFromTstmp(String sub, String typ,
	    String pre, Object obj, Integer acc, Integer con, Long exp,
	    ContextProvider cop, Long tst, Long tstinputfrom);

    /**
     * Retrieves a list of {@link org.persona.middleware.context.ContextEvent}
     * from the underlying database, which members are the context events that
     * match the parameters passed, and were received before the specified
     * timestamp. A parameter can be passed a value of <code>null</code> for
     * wildcarding.
     * 
     * @param sub
     *            The URI of the subject of the event to be matched
     * @param typ
     *            The URI of the type of subject of the event to be matched
     * @param pre
     *            The URI of the predicate of the event to be matched
     * @param obj
     *            The Object of the event to be matched. It depends on the
     *            implementer of this method how to match the object.
     * @param acc
     *            The accuracy of the event to be matched
     * @param con
     *            The confidence of the event to be matched
     * @param exp
     *            The expiration time of the event to be matched
     * @param cop
     *            The context provider of the event to be matched
     * @param tst
     *            The timestamp in milliseconds of the event to be matched. If
     *            not <code>null</code>, only events with this specified
     *            timestamp will be returned, as long as it is inside the time
     *            range specified by <code>tstinputto</code>
     * @param tstinputto
     *            The timestamp in milliseconds until which events are
     *            requested. Only events stored before this timestamp will be
     *            returned.
     * @return The list of context events that matched the values passed as
     *         parameters.
     */
    public ArrayList retrieveEventsToTstmp(String sub, String typ, String pre,
	    Object obj, Integer acc, Integer con, Long exp,
	    ContextProvider cop, Long tst, Long tstinputto);

    /**
     * Retrieves a list of {@link org.persona.middleware.context.ContextEvent}
     * from the underlying database, which members are the context events that
     * match the parameters passed, and were received within the specified time
     * range. A parameter can be passed a value of <code>null</code> for
     * wildcarding.
     * 
     * @param sub
     *            The URI of the subject of the event to be matched
     * @param typ
     *            The URI of the type of subject of the event to be matched
     * @param pre
     *            The URI of the predicate of the event to be matched
     * @param obj
     *            The Object of the event to be matched. It depends on the
     *            implementer of this method how to match the object.
     * @param acc
     *            The accuracy of the event to be matched
     * @param con
     *            The confidence of the event to be matched
     * @param exp
     *            The expiration time of the event to be matched
     * @param cop
     *            The context provider of the event to be matched
     * @param tst
     *            The timestamp in milliseconds of the event to be matched. If
     *            not <code>null</code>, only events with this specified
     *            timestamp will be returned, as long as it is inside the time
     *            range
     * @param tstinputfrom
     *            The timestamp in milliseconds from which events are requested.
     *            Only events stored after this timestamp will be returned.
     * @param tstinputto
     *            The timestamp in milliseconds until which events are
     *            requested. Only events stored before this timestamp will be
     *            returned.
     * @return The list of context events that matched the values passed as
     *         parameters.
     */
    public ArrayList retrieveEventsBetweenTstmp(String sub, String typ,
	    String pre, Object obj, Integer acc, Integer con, Long exp,
	    ContextProvider cop, Long tst, Long tstinputfrom, Long tstinputto);

    /**
     * Returns the result of a SPARQL query issued to the underlying database.
     * 
     * @param input
     *            The SPARQL query to be executed. All types of SPARQL queries
     *            are allowed.
     * @return The serialized form of the model resulting from the execution of
     *         the query, in RDF/XML-ABBREV format.
     */
    public String queryBySPARQL(String input);

    /**
     * Retrieves a list of {@link org.persona.middleware.context.ContextEvent}
     * from the underlying database, as a result of a SPARQL query.
     * 
     * @param input
     *            The SPARQL query that defines the events to be returned. The
     *            variable that represents the event MUST be named
     *            <code>c</code>. For instance: "SELECT ?c WHERE {...". Only
     *            SELECT queries are allowed.
     * @return The list of context events that matched the query.
     */
    public ArrayList retrieveEventsBySPARQL(String input);

    /**
     * Removes all events from the underlying database that were received until
     * the specified timestamp.
     * 
     * @param tst
     *            The timestamp in milliseconds until which events are removed.
     *            A value of 0 does nothing.
     */
    public void removeOldEvents(long tst);

    /**
     * Closes the connection to the database
     */
    public void close();

    /**
     * Establishes the connection to the database
     */
    public void connect();

}
