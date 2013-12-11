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

public class CustomWorkingMemoryEventListener implements
		WorkingMemoryEventListener {
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
		logger.info("Object Inserted: " + event.getFactHandle() + " is a "
				+ event.getObject());
	}

	public void objectRetracted(ObjectRetractedEvent event) {
		logger.info("Object Retracted: " + event.getFactHandle() + " is a "
				+ event.getOldObject());
	}

	public void objectUpdated(ObjectUpdatedEvent event) {
		logger.info("Object Updated: " + event.getFactHandle() + " is a "
				+ event.getObject());
	}
}
