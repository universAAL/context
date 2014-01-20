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

//import org.apache.log4j.Logger;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import org.drools.event.rule.ActivationCancelledEvent;
import org.drools.event.rule.ActivationCreatedEvent;
import org.drools.event.rule.AfterActivationFiredEvent;
import org.drools.event.rule.AgendaEventListener;
import org.drools.event.rule.AgendaGroupPoppedEvent;
import org.drools.event.rule.AgendaGroupPushedEvent;
import org.drools.event.rule.BeforeActivationFiredEvent;

public class CustomAgendaEventListener implements AgendaEventListener {
	private Logger logger = Logger.getLogger("AgendaListener");
	FileHandler fh;

	public CustomAgendaEventListener() {
		try {
			logger.setLevel(Level.INFO);
			System.out
					.println("SYSOUT-------------------->>>>SETTING UP LOGGER");
			logger.info("SETTING UP LOGGER");
			fh = new FileHandler("Agenda.log");
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

	public void activationCancelled(ActivationCancelledEvent event) {
		logger.info("Activation Cancelled: " + event.getActivation());
	}

	public void activationCreated(ActivationCreatedEvent event) {
		logger.info("Activation Created: " + event.getActivation());
	}

	public void beforeActivationFired(BeforeActivationFiredEvent event) {
		logger.info("Before Activation Fired: " + event.getActivation());
	}

	public void afterActivationFired(AfterActivationFiredEvent event) {
		logger.info("After Activation Fired: " + event.getActivation());
	}

	public void agendaGroupPopped(AgendaGroupPoppedEvent event) {
		// logger.info("Agenda Group Popped: " + event.getAgendaGroup());
	}

	public void agendaGroupPushed(AgendaGroupPushedEvent event) {
		// logger.info("Agenda Group Pushed: " + event.getAgendaGroup());
	}

}
