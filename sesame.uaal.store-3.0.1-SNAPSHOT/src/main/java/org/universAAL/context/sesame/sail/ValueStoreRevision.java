/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.universAAL.context.sesame.sail;

import java.io.Serializable;

/**
 * A {@link ValueStore ValueStore} revision for {@link NativeValue NativeValue}
 * objects. For a cached value ID of a NativeValue to be valid, the revision
 * object needs to be equal to the concerning ValueStore's revision object. The
 * ValueStore's revision object is changed whenever values are removed from it
 * or IDs are changed.
 * 
 * @author Arjohn Kampman
 */
public class ValueStoreRevision implements Serializable {

	/*-----------*
	 * Constants *
	 *-----------*/

	private static final long serialVersionUID = -2434063125560285009L;

	/*-----------*
	 * Variables *
	 *-----------*/

	transient private final ValueStore valueStore;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public ValueStoreRevision(ValueStore valueStore) {
		this.valueStore = valueStore;
	}

	/*---------*
	 * Methods *
	 *---------*/

	public ValueStore getValueStore() {
		return valueStore;
	}
}
