/*
	Copyright 2008-2014 ITACA-TSB, http://www.tsb.upv.es
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

import java.util.Calendar;
import java.util.Date;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

import org.universAAL.context.che.Hub;

/**
 * This class is used to remove events from the store periodically, to avoid the
 * uncontrolled growth of the history
 * 
 * @author <a href="mailto:alfiva@itaca.upv.es">Alvaro Fides Valero</a>
 * 
 */
public class Cleaner extends TimerTask {

    private Backend db;
    private Timer t;

    public Cleaner(Backend db) {
	this.db = db;
    }

    public void run() {
	// This happens every 24 hours
	// Get the date for which auto-removal is scheduled (default: a long
	// time ago)
	long itstime = Long.parseLong(Hub.getProperties().getProperty(
		"RECYCLE.DATE", "0"));
	Calendar now = Calendar.getInstance();
	// If the time for removal has come (that is, has passed)...
	if (now.getTimeInMillis() > itstime) {
	    // schedule the removal for tonight (or whenever)
	    t = new Timer();
	    // Turn "now" into the exact hour and date
	    now.set(Calendar.HOUR_OF_DAY,
		    Integer.parseInt(Hub.getProperties().getProperty(
			    "RECYCLE.HOUR", "22")));
	    // If it has passed, do it tomorrow
	    if (now.getTimeInMillis() < System.currentTimeMillis())
		now.add(Calendar.DAY_OF_YEAR, 1);// TODO: What if Dec 31st!? It
						 // should work...
	    t.schedule(new Punctual(db), new Date(now.getTimeInMillis()));
	}

    }

    /**
     * Auxiliary class used to perform the removal at a specified hour
     * 
     * @author alfiva
     */
    private class Punctual extends TimerTask {

	private Backend db;

	public Punctual(Backend db) {
	    this.db = db;
	}

	public void run() {
	    // This happens only when it´s time to remove
	    int keep = Integer.parseInt(Hub.getProperties().getProperty(
		    "RECYCLE.KEEP", "2"));
	    if (keep <= 0) {
		keep = 1;// At least 1 month. 0 not allowed.
	    }
	    long keepl = keep * 2592000000l;// Months in ms
	    // DB removes values prior to the argument passed
	    db.removeOldEvents(System.currentTimeMillis() - keepl);
	    // This will keep info that arrived in the last "keep" ms
	    // And now update the time of the last removal to "now"
	    Properties props = Hub.getProperties();
	    props.setProperty("RECYCLE.DATE",
		    String.valueOf(System.currentTimeMillis()));
	    Hub.setProperties(props);
	}

    }

}
