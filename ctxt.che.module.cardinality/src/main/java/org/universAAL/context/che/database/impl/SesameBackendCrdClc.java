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

import java.io.File;

import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.inferencer.fc.ForwardChainingRDFSInferencer;
import org.universAAL.context.che.Hub;
import org.universAAL.context.che.Hub.Log;
import org.universAAL.context.sesame.sail.CardinCollectNativeStore;

/**
 * Extension of
 * {@link org.universAAL.context.che.database.impl.SesameBackendCrd} that uses a
 * modified Sesame NativeStore that checks cardinality of stored statements,
 * according to OWL Lite (if maxCardinality is 1 only one single value object is
 * accepted) and also closed collections to avoid situations of collection
 * open-closed duplicity, like:.
 *
 * S P list1 S P list2
 *
 * @author alfiva
 *
 */
public class SesameBackendCrdClc extends SesameBackend {
	/**
	 * Logger.
	 */
	private static Log log = Hub.getLog(SesameBackendCrdClc.class);

	@Override
	public void connect() {
		String dataPath = Hub.getProperties().getProperty("STORE.LOCATION");
		boolean encrypt = Boolean.parseBoolean(Hub.getProperties().getProperty("STORE.ENCRYPT"));
		// I use C:\Proyectos\UNIVERSAAL\ContextStore\Stores\SAIL_FCRDFS_Native
		if (dataPath != null) {
			File dataDir = new File(dataPath);
			String indexes = "spoc,posc,cosp"; // TODO: Change indexes
			// (specially
			// if we dont use contexts)
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
					"No location specified for the store. " + "Add and specify the configuration"
							+ " parameter STORE.LOCATION to the " + "configuration file of the CHE pointing"
							+ " to a valid folder path.");
		}
	}

}
