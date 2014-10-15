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
package org.universAAL.drools.models;

import java.util.Hashtable;

public class Fact {

	public String humanId;
	private Hashtable<String, String> attributes;

	public Fact() {

	}

	public Fact(String stID, FactProperty[] props) {
		super();
		humanId = stID;
		for (FactProperty factProperty : props) {
			putAttribute(factProperty.key, factProperty.value);
		}

	}

	public void putAttribute(String key, String val) {
		if (attributes == null) {
			attributes = new Hashtable<String, String>(1);
		}
		attributes.put(key, val);
	}

	public String getAttribute(String key) {
		return attributes.get(key);
	}
}
