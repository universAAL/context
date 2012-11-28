package org.universAAL.context.sesame.sail;

import java.io.File;
import java.io.IOException;

import org.openrdf.sail.NotifyingSailConnection;
import org.openrdf.sail.SailException;

public class CardinCollectNativeStore extends CardinalityNativeStore{

    public CardinCollectNativeStore(File dataDir, String indexes, boolean encrypt) {
	super(dataDir,indexes,encrypt);
    }

    @Override
    protected NotifyingSailConnection getConnectionInternal()
	    throws SailException {
	try {
		return new CardinCollectNativeStoreConnection(this);
	}
	catch (IOException e) {
		throw new SailException(e);
	}
    }

}
