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

import java.util.Calendar;
import java.util.Date;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

import org.universAAL.context.che.Hub;
import org.universAAL.context.che.database.impl.RDF4JBackend;

/**
 * This class is used to remove events from the store periodically, to avoid the
 * uncontrolled growth of the history.
 * <p>
 * Configuration in CHe.properties:
 * <ul>
 * <li>RECYCLE.DATE: time in ms from which the next removal is due.</li>
 * <li>RECYCLE.KEEP_DAYS: events of the last n days are kept. If not set,
 * RECYCLE.KEEP is used: months of 30 days (default 2).</li>
 * <li>RECYCLE.INTERVAL_DAYS: after a removal, the next one is due in n days.
 * Default: the time events are kept.</li>
 * <li>RECYCLE.ON_STARTUP: true: the removal is only done when the CHe starts
 * and the removal is due, before the store is opened, so that it does not
 * block storing and querying. false (default): the CHe checks every 24 hours
 * whether the removal is due and then removes at RECYCLE.HOUR (default 22) on
 * the open store.</li>
 * </ul>
 * The recycling is logged in {@link RecycleLog}.
 *
 * @author <a href="mailto:alfiva@itaca.upv.es">Alvaro Fides Valero</a>
 *
 */
public class Cleaner extends TimerTask {

	private static final long DAY = 86400000L;
	private static final long MONTH = 30 * DAY;

	/**
	 * The store.
	 */
	private Backend db;
	/**
	 * Secondary timer for the right hour.
	 */
	private Timer t;

	/**
	 * Main constructor.
	 *
	 * @param dbstore
	 *            The store
	 */
	public Cleaner(Backend dbstore) {
		this.db = dbstore;
	}

	/**
	 * @return how long events are kept, in ms
	 */
	public static long keepMillis(Properties props) {
		String days = props.getProperty("RECYCLE.KEEP_DAYS");
		if (days != null && days.trim().length() > 0) {
			long d = Long.parseLong(days.trim());
			return Math.max(d, 1) * DAY; // At least 1 day. 0 not allowed.
		}
		int keep = Integer.parseInt(props.getProperty("RECYCLE.KEEP", "2").trim());
		return Math.max(keep, 1) * MONTH; // At least 1 month. 0 not allowed.
	}

	/**
	 * @return the time from one removal to the next, in ms
	 */
	public static long intervalMillis(Properties props) {
		String days = props.getProperty("RECYCLE.INTERVAL_DAYS");
		if (days != null && days.trim().length() > 0) {
			return Math.max(Long.parseLong(days.trim()), 1) * DAY;
		}
		return keepMillis(props);
	}

	/**
	 * @return the time from which the next removal is due
	 */
	public static long dueDate(Properties props) {
		return Long.parseLong(props.getProperty("RECYCLE.DATE", "0").trim());
	}

	/**
	 * Sets the date of the next removal.
	 */
	private static void scheduleNext() {
		Properties props = Hub.getProperties();
		long next = System.currentTimeMillis() + intervalMillis(props);
		props.setProperty("RECYCLE.DATE", String.valueOf(next));
		Hub.setProperties(props);
		RecycleLog.log("next recycling due from " + new Date(next) + " (RECYCLE.DATE=" + next + ")");
	}

	private static String settings(Properties props) {
		return "keep " + keepMillis(props) / DAY + " days, interval " + intervalMillis(props) / DAY + " days, "
				+ "STORE.INFERENCE=" + props.getProperty("STORE.INFERENCE", "true");
	}

	/**
	 * To be called when the CHe starts, before the store is opened: removes
	 * the old events if the removal is due.
	 *
	 * @param db
	 *            the store, not yet connected
	 */
	public static void recycleAtStartup(Backend db) {
		Properties props = Hub.getProperties();
		long due = dueDate(props);
		if (System.currentTimeMillis() <= due) {
			RecycleLog.log("start: recycling not due before " + new Date(due));
			return;
		}
		if (!(db instanceof RDF4JBackend)) {
			RecycleLog.log("start: recycling at startup not supported by " + db.getClass().getName());
			return;
		}
		RecycleLog.log("start: recycling due since " + new Date(due) + "; " + settings(props));
		if (((RDF4JBackend) db).removeOldEventsBeforeConnect(System.currentTimeMillis() - keepMillis(props))) {
			scheduleNext();
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.util.TimerTask#run()
	 */
	public void run() {
		// This happens every 24 hours
		// Get the date for which auto-removal is scheduled (default: a long
		// time ago)
		long itstime = dueDate(Hub.getProperties());
		Calendar now = Calendar.getInstance();
		// If the time for removal has come (that is, has passed)...
		if (now.getTimeInMillis() > itstime) {
			// schedule the removal for tonight (or whenever)
			if (t != null) {
				t.cancel();
			}
			t = new Timer("ctxt.che recycling", true);
			// Turn "now" into the exact hour and date
			now.set(Calendar.HOUR_OF_DAY, Integer.parseInt(Hub.getProperties().getProperty("RECYCLE.HOUR", "22")));
			now.set(Calendar.MINUTE, 0);
			now.set(Calendar.SECOND, 0);
			// If it has passed, do it tomorrow
			if (now.getTimeInMillis() < System.currentTimeMillis()) {
				now.add(Calendar.DAY_OF_YEAR, 1);
			}
			t.schedule(new Punctual(db), new Date(now.getTimeInMillis()));
			RecycleLog.log("recycling scheduled for " + now.getTime());
		}

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.util.TimerTask#cancel()
	 */
	public boolean cancel() {
		if (t != null) {
			t.cancel();
		}
		return super.cancel();
	}

	/**
	 * Auxiliary class used to perform the removal at a specified hour.
	 *
	 * @author alfiva
	 */
	private class Punctual extends TimerTask {
		/**
		 * The store.
		 */
		private Backend db;

		/**
		 * Main constructor.
		 *
		 * @param db
		 *            the store
		 */
		public Punctual(Backend db) {
			this.db = db;
		}

		/*
		 * (non-Javadoc)
		 *
		 * @see java.util.TimerTask#run()
		 */
		public void run() {
			// This happens only when it's time to remove
			Properties props = Hub.getProperties();
			RecycleLog.log("recycling on the open store; " + settings(props));
			long start = System.currentTimeMillis();
			// DB removes values prior to the argument passed
			// This will keep info that arrived in the last "keep" ms
			db.removeOldEvents(System.currentTimeMillis() - keepMillis(props));
			RecycleLog.log("recycling done in " + (System.currentTimeMillis() - start) + " ms");
			// And now update the time of the next removal
			scheduleNext();
		}

	}

}
