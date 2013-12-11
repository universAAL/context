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
