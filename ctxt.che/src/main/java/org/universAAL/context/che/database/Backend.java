/*
	Copyright 2008 ITACA-SABIEN, http://www.sabien.upv.es
	Instituto Tecnologico de Aplicaciones de Comunicacion
	Avanzadas - Grupo Tecnologias para la Salud y el
	Bienestar (SABIEN)

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

import java.io.IOException;
import java.util.ArrayList;

import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.rio.RDFParseException;
import org.universAAL.middleware.context.ContextEvent;
import org.universAAL.middleware.context.owl.ContextProvider;
import org.universAAL.middleware.serialization.MessageContentSerializer;

/**
 * Interface that represents a store back end where the context history is
 * stored.
 *
 * @author <a href="mailto:alfiva@itaca.upv.es">Alvaro Fides Valero</a>
 *
 */
public interface Backend {

	/**
	 * Establishes the connection to the store.
	 */
	void connect();

	/**
	 * Closes the connection to the store.
	 */
	void close();

	/**
	 * Stores a {@link org.universAAL.middleware.context.ContextEvent} in the
	 * underlying store.
	 *
	 * @param e
	 *            The context event to be stored.
	 */
	void storeEvent(ContextEvent e);

	/**
	 * Retrieves a list of
	 * {@link org.universAAL.middleware.context.ContextEvent} from the
	 * underlying store, which members are the context events that match the
	 * parameters passed. A parameter can be passed a value of <code>null</code>
	 * for wildcarding.
	 * <p>
	 * This is not used in the current version. Will probably be deprecated.
	 *
	 * @param subject
	 *            The URI of the subject of the event to be matched
	 * @param subjecttype
	 *            The URI of the type of subject of the event to be matched
	 * @param predicate
	 *            The URI of the predicate of the event to be matched
	 * @param object
	 *            The Object of the event to be matched. It depends on the
	 *            implementer of this method how to match the object.
	 * @param confidence
	 *            The confidence of the event to be matched
	 * @param expiration
	 *            The expiration time of the event to be matched
	 * @param provider
	 *            The context provider of the event to be matched
	 * @param tstamp
	 *            The timestamp in milliseconds of the event to be matched
	 * @param scopeArray
	 *            Optional argument: scopes defining the origin tenants of the
	 *            requested info
	 * @return The list of context events that matched the values passed as
	 *         parameters.
	 */
	ArrayList retrieveEvent(String subject, String subjecttype, String predicate, Object object, Integer confidence,
			Long expiration, Object provider, Long tstamp, String... scopeArray);

	/**
	 * Retrieves a list of
	 * {@link org.universAAL.middleware.context.ContextEvent} from the
	 * underlying store, which members are the context events that match the
	 * parameters passed, and were received after the specified timestamp. A
	 * parameter can be passed a value of <code>null</code> for wildcarding.
	 *
	 * @param subject
	 *            The URI of the subject of the event to be matched
	 * @param subjecttype
	 *            The URI of the type of subject of the event to be matched
	 * @param predicate
	 *            The URI of the predicate of the event to be matched
	 * @param object
	 *            The Object of the event to be matched. It depends on the
	 *            implementer of this method how to match the object.
	 * @param confidence
	 *            The confidence of the event to be matched
	 * @param expiration
	 *            The expiration time of the event to be matched
	 * @param provider
	 *            The context provider of the event to be matched
	 * @param tstamp
	 *            The timestamp in milliseconds of the event to be matched. If
	 *            not <code>null</code>, only events with this specified
	 *            timestamp will be returned, as long as it is inside the time
	 *            range specified by <code>tstinputfrom</code>
	 * @param tstfrom
	 *            The timestamp in milliseconds from which events are requested.
	 *            Only events stored after this timestamp will be returned.
	 * @param scopeArray
	 *            Optional argument: scopes defining the origin tenants of the
	 *            requested info
	 * @return The list of context events that matched the values passed as
	 *         parameters.
	 */
	ArrayList retrieveEventsFromTstmp(String subject, String subjecttype, String predicate, Object object,
			Integer confidence, Long expiration, ContextProvider provider, Long tstamp, Long tstfrom,
			String... scopeArray);

	/**
	 * Retrieves a list of
	 * {@link org.universAAL.middleware.context.ContextEvent} from the
	 * underlying store, which members are the context events that match the
	 * parameters passed, and were received before the specified timestamp. A
	 * parameter can be passed a value of <code>null</code> for wildcarding.
	 *
	 * @param subject
	 *            The URI of the subject of the event to be matched
	 * @param subjecttype
	 *            The URI of the type of subject of the event to be matched
	 * @param predicate
	 *            The URI of the predicate of the event to be matched
	 * @param object
	 *            The Object of the event to be matched. It depends on the
	 *            implementer of this method how to match the object.
	 *
	 * @param confidence
	 *            The confidence of the event to be matched
	 * @param expiration
	 *            The expiration time of the event to be matched
	 * @param provider
	 *            The context provider of the event to be matched
	 * @param tstamp
	 *            The timestamp in milliseconds of the event to be matched. If
	 *            not <code>null</code>, only events with this specified
	 *            timestamp will be returned, as long as it is inside the time
	 *            range specified by <code>tstinputto</code>
	 * @param tstto
	 *            The timestamp in milliseconds until which events are
	 *            requested. Only events stored before this timestamp will be
	 *            returned.
	 * @param scopeArray
	 *            Optional argument: scopes defining the origin tenants of the
	 *            requested info
	 * @return The list of context events that matched the values passed as
	 *         parameters.
	 */
	ArrayList retrieveEventsToTstmp(String subject, String subjecttype, String predicate, Object object,
			Integer confidence, Long expiration, ContextProvider provider, Long tstamp, Long tstto,
			String... scopeArray);

	/**
	 * Retrieves a list of
	 * {@link org.universAAL.middleware.context.ContextEvent} from the
	 * underlying store, which members are the context events that match the
	 * parameters passed, and were received within the specified time range. A
	 * parameter can be passed a value of <code>null</code> for wildcarding.
	 *
	 * @param subject
	 *            The URI of the subject of the event to be matched
	 * @param subjecttype
	 *            The URI of the type of subject of the event to be matched
	 * @param predicate
	 *            The URI of the predicate of the event to be matched
	 * @param object
	 *            The Object of the event to be matched. It depends on the
	 *            implementer of this method how to match the object.
	 * @param confidence
	 *            The confidence of the event to be matched
	 * @param expiration
	 *            The expiration time of the event to be matched
	 * @param provider
	 *            The context provider of the event to be matched
	 * @param tstamp
	 *            The timestamp in milliseconds of the event to be matched. If
	 *            not <code>null</code>, only events with this specified
	 *            timestamp will be returned, as long as it is inside the time
	 *            range
	 * @param tstfrom
	 *            The timestamp in milliseconds from which events are requested.
	 *            Only events stored after this timestamp will be returned.
	 * @param tstto
	 *            The timestamp in milliseconds until which events are
	 *            requested. Only events stored before this timestamp will be
	 *            returned.
	 * @param scopeArray
	 *            Optional argument: scopes defining the origin tenants of the
	 *            requested info
	 * @return The list of context events that matched the values passed as
	 *         parameters.
	 */
	ArrayList retrieveEventsBetweenTstmp(String subject, String subjecttype, String predicate, Object object,
			Integer confidence, Long expiration, ContextProvider provider, Long tstamp, Long tstfrom, Long tstto,
			String... scopeArray);

	/**
	 * Retrieves a list of
	 * {@link org.universAAL.middleware.context.ContextEvent} from the
	 * underlying store, as a result of a SPARQL query.
	 *
	 * @param input
	 *            The SPARQL query that defines the events to be returned. The
	 *            variable that represents the event MUST be named
	 *            <code>c</code>. For instance: "SELECT ?c WHERE {...". Only
	 *            SELECT queries are allowed.
	 * @param scopeArray
	 *            Optional argument: scopes defining the origin tenants of the
	 *            requested info
	 * @return The list of context events that matched the query.
	 */
	ArrayList retrieveEventsBySPARQL(String input, String... scopeArray);

	/**
	 * Returns the result of a SPARQL query issued to the underlying store.
	 *
	 * The types of SPARQL queries allowed depend on the underlying
	 * implementation, but these are mandated to include at least SELECT and
	 * DESCRIBE, and encouraged to also allow CONSTRUCT and ASK. SPARQL-Update
	 * is optional. Implementations should inform in case an unsupported query
	 * is received.
	 *
	 * The underlying implementations should return results in RDF/XML-ABBREV
	 * format encoded in UTF-8 in case of RDF models, and also if possible in
	 * case of lists of bindings for SELECT queries. Results for ASK and UPDATE
	 * queries (if implemented) should follow the format \"true\" or \"false\"
	 * Strings in case of ASK and \"true\" in case of a successful UPDATE query.
	 *
	 * @param input
	 *            The SPARQL query to be executed.
	 * @param scopeArray
	 *            Optional argument: scopes defining the origin tenants of the
	 *            requested info
	 * @return The serialized form of the result from the execution of the
	 *         query.
	 */
	String queryBySPARQL(String input, String... scopeArray);

	/**
	 * Removes all events from the underlying store that were received until the
	 * specified timestamp.
	 *
	 * @param tstamp
	 *            The timestamp in milliseconds until which events are removed.
	 *            A value of 0 does nothing.
	 */
	void removeOldEvents(long tstamp);

	/**
	 * Set the MessageContentSerializer universAAL Parser that can be used to
	 * serialize and parse RDF data to universAAL data and vice-versa. This
	 * might be helpful in many implementations, and allows the activator of the
	 * CHE to look for such service in OSGi, which should be provided by the
	 * mandatory component mw.data.serialization.
	 *
	 * There is no guarantee though that the parser is set before it is used in
	 * the implementation, so check for nulls. If an implementation of a Backend
	 * does not need to use such parser, leave its implementation of this method
	 * empty.
	 *
	 * @param service
	 *            The MessageContentSerializer service implementation found in
	 *            OSGi
	 */
	void setSerializer(MessageContentSerializer service);

	/**
	 * Fills the initial store with the OWL data of the ontologies from the OWL
	 * files in the config folder (or registered in the system). Also used for
	 * updates when new ontologies are installed, so this method shall take care
	 * of not duplicating new ontologies that are already stored.
	 *
	 * @throws RepositoryException
	 * @throws RDFParseException
	 * @throws IOException
	 */
	public void populate() throws RepositoryException, RDFParseException, IOException;

	/**
	 * Fills the store with the OWL data of a certain ontologies from the OWL
	 * file in the config folder (or registered in the system). Also used for
	 * updates when new ontologies are installed.
	 *
	 * @param owlFileName
	 *            The naem of the OWL file to store
	 * @throws RepositoryException
	 * @throws RDFParseException
	 * @throws IOException
	 */
	public void populate(String owlFileName) throws RepositoryException, RDFParseException, IOException;
}
