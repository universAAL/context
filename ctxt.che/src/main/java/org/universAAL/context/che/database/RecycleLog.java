/*
	Copyright 2026 the universAAL contributors

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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Separate log of the recycling (removal of old events), so that it can be
 * followed independently of the platform log. The file is placed next to the
 * store folder (e.g. data/ctxt.che/recycle.log for the store
 * data/ctxt.che/store). It is only appended to, there is no rotation.
 */
public final class RecycleLog {

	/**
	 * Name of the log file.
	 */
	public static final String FILE_NAME = "recycle.log";

	/**
	 * The log file, null if logging is not possible.
	 */
	private static File file;

	private RecycleLog() {
	}

	/**
	 * Sets the log file next to the store folder.
	 *
	 * @param storeLocation
	 *            the store folder as configured in STORE.LOCATION
	 */
	public static synchronized void init(String storeLocation) {
		if (storeLocation == null) {
			file = null;
			return;
		}
		File parent = new File(storeLocation).getAbsoluteFile().getParentFile();
		file = parent == null ? null : new File(parent, FILE_NAME);
	}

	/**
	 * @return the log file, or null
	 */
	public static synchronized File getFile() {
		return file;
	}

	/**
	 * Appends a line with a time stamp.
	 *
	 * @param msg
	 *            the message
	 */
	public static void log(String msg) {
		write(msg, null);
	}

	/**
	 * Appends a line with a time stamp and the stack trace of an exception.
	 *
	 * @param msg
	 *            the message
	 * @param t
	 *            the exception
	 */
	public static void log(String msg, Throwable t) {
		write(msg, t);
	}

	private static synchronized void write(String msg, Throwable t) {
		if (file == null) {
			return;
		}
		StringBuilder sb = new StringBuilder();
		sb.append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date())).append("  ").append(msg)
				.append('\n');
		if (t != null) {
			StringWriter sw = new StringWriter();
			t.printStackTrace(new PrintWriter(sw));
			sb.append(sw);
		}
		FileWriter out = null;
		try {
			if (!file.getParentFile().exists()) {
				file.getParentFile().mkdirs();
			}
			out = new FileWriter(file, true);
			out.write(sb.toString());
		} catch (IOException e) {
			// nothing else we can do, the platform log has the important messages
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
				}
			}
		}
	}
}
