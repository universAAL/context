/*
	Copyright 2008-2011 ITACA-TSB, http://www.tsb.upv.es
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
import java.util.Timer;
import java.util.TimerTask;

import org.universAAL.context.che.Activator;

/**
 * This class is used to remove events from the store periodically, to avoid the
 * uncontrolled growth of the history
 * 
 * @author <a href="mailto:alfiva@itaca.upv.es">Alvaro Fides Valero</a>
 * 
 */
public class Cleaner extends TimerTask {

    /**
     * Auxiliar class used to perform the removal at a specified hour
     * 
     * @author alfiva
     */
    private class Punctual extends TimerTask {

	private Backend db;

	public Punctual(Backend db) {
	    this.db = db;
	}

	public void run() {
	    long tst = Long.parseLong(Activator.getProperties().getProperty(
		    "RECYCLE.KEEP", "0"));
	    db.removeOldEvents(System.currentTimeMillis() - tst);
	}

    }

    private Backend db;
    private Timer t;

    public Cleaner(Backend db) {
	this.db = db;
    }

    public void run() {
	t = new Timer();
	Calendar now = Calendar.getInstance();
	now.set(Calendar.HOUR_OF_DAY,
		Integer.parseInt(Activator.getProperties().getProperty(
			"RECYCLE.HOUR", "22")));
	if (now.getTimeInMillis() < System.currentTimeMillis())
	    now.add(Calendar.DAY_OF_YEAR, 1);
	t.schedule(new Punctual(db), new Date(now.getTimeInMillis()));
    }

}
