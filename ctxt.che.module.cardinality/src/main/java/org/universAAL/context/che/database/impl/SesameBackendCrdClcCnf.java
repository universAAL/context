/*
	Copyright 2015 ITACA-TSB, http://www.tsb.upv.es
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

import java.io.File;

import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.inferencer.fc.ForwardChainingRDFSInferencer;
import org.universAAL.context.che.Hub;
import org.universAAL.context.che.Hub.Log;
import org.universAAL.context.sesame.sail.CardinCollectNativeStore;

/**
 * Like a {@link org.universAAL.context.che.database.impl.SesameBackendCrdClc}
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
 * will result in having the statements in the store: "event1" "hasSubject"
 * "subject2", "event1" "hasPredicate" "predicate3", "event1" "hasObject"
 * "object4", "subject2" "predicate3" "object4"
 *
 * But if the confidence is below the threshold, the last reified statement is
 * not stored.
 *
 * @author alfiva
 *
 */
public class SesameBackendCrdClcCnf extends SesameBackendWithConfidence {
	/**
	 * Logger.
	 */
	private static final Log log = Hub.getLog(SesameBackendCrdClcCnf.class);

	@Override
	public void connect() {
		String dataPath = Hub.getProperties().getProperty("STORE.LOCATION");
		boolean encrypt = Boolean.parseBoolean(Hub.getProperties().getProperty("STORE.ENCRYPT"));
		// I use C:\Proyectos\UNIVERSAAL\ContextStore\Stores\SAIL_FCRDFS_Native
		if (dataPath != null) {
			File dataDir = new File(dataPath);
			String indexes = "spoc,posc,cosp"; // TODO: Change indexes
			log.info("CHe connects to {} ", dataDir.toString());
			// TODO: Evaluate the inference, and study other reasoners, if any
			try {
				myRepository = new SailRepository(
						new ForwardChainingRDFSInferencer(new CardinCollectNativeStore(dataDir, indexes, encrypt)));
				myRepository.initialize();
				con = myRepository.getConnection();
				if (Boolean.parseBoolean(Hub.getProperties().getProperty("STORE.PRELOAD"))) {
					this.populate();
				}
			} catch (Exception e) {
				log.error("connect", "Exception trying to initilaize the store: {} ", e);
				e.printStackTrace();
			}
		} else {
			log.error("connect",
					"No location specified for the store. " + "Add and specify the configuration parameter "
							+ "STORE.LOCATION to the configuration file of the CHE "
							+ "pointing to a valid folder path.");
		}
	}
}
