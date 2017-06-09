/*
    Copyright 2007-2014 TSB, http://www.tsbtecnologias.es
    Technologies for Health and Well-being - Valencia, Spain

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

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

//import org.apache.log4j.jmx.LoggerDynamicMBean;
import org.drools.event.rule.ObjectInsertedEvent;
import org.drools.event.rule.ObjectRetractedEvent;
import org.drools.event.rule.ObjectUpdatedEvent;
import org.drools.event.rule.WorkingMemoryEventListener;

public class CustomWorkingMemoryEventListener implements WorkingMemoryEventListener {
	private static final Logger logger = Logger.getLogger("WorkingMemory");
	FileHandler fh;

	public CustomWorkingMemoryEventListener() {

		try {
			logger.setLevel(Level.INFO);
			fh = new FileHandler("WorkingMemory.log");
			SimpleFormatter format = new SimpleFormatter();
			fh.setFormatter(format);
			logger.addHandler(fh);
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void objectInserted(ObjectInsertedEvent event) {
		logger.info("Object Inserted: " + event.getFactHandle() + " is a " + event.getObject());
	}

	public void objectRetracted(ObjectRetractedEvent event) {
		logger.info("Object Retracted: " + event.getFactHandle() + " is a " + event.getOldObject());
	}

	public void objectUpdated(ObjectUpdatedEvent event) {
		logger.info("Object Updated: " + event.getFactHandle() + " is a " + event.getObject());
	}
}
