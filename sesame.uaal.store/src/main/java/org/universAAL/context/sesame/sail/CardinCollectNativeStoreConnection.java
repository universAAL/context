package org.universAAL.context.sesame.sail;

import java.io.IOException;

import org.openrdf.OpenRDFUtil;
import org.openrdf.model.BNode;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.sail.SailException;

public class CardinCollectNativeStoreConnection extends CardinalityNativeStoreConnection{

    protected CardinCollectNativeStoreConnection(
	    CardinalityNativeStore nativeStore) throws IOException {
	super(nativeStore);
    }

    @Override
    protected boolean addStatement(Resource subj, URI pred, Value obj,
	    boolean explicit, Resource... contexts) throws SailException {
	OpenRDFUtil.verifyContextNotNull(contexts);

	boolean result = false;

	try {
		ValueStore valueStore = nativeStore.getValueStore();
		int subjID = valueStore.storeValue(subj);
		int predID = valueStore.storeValue(pred);
		int objID = valueStore.storeValue(obj);

		if (contexts.length == 0) {
			contexts = new Resource[] { null };
		}

		for (Resource context : contexts) {
			int contextID = 0;
			if (context != null) {
				contextID = valueStore.storeValue(context);
			}

			//START PATCH
			if(hasMaxCardinality1(pred, contexts)){
			    removeStatements(subj, pred, null, true, contexts);
			}else if(objIsClosedCollection(obj, contexts)){
			    removeStatements(subj, pred, null, true, contexts);
			}
			//END PATCH
			boolean wasNew = nativeStore.getTripleStore().storeTriple(subjID, predID, objID, contextID,
					explicit);
			result |= wasNew;

			if (wasNew) {
				// The triple was not yet present in the triple store
				sailChangedEvent.setStatementsAdded(true);

				if (hasConnectionListeners()) {
					Statement st;

					if (context != null) {
						st = valueStore.createStatement(subj, pred, obj, context);
					}
					else {
						st = valueStore.createStatement(subj, pred, obj);
					}

					notifyStatementAdded(st);
				}
			}
		}
	}
	catch (IOException e) {
		throw new SailException(e);
	}
	catch (RuntimeException e) {
		logger.error("Encountered an unexpected problem while trying to add a statement", e);
		throw e;
	}

	return result;
    }

    private boolean objIsClosedCollection(Value obj, Resource[] contexts)
	    throws SailException {
	if (obj instanceof BNode) {//TODO of Resource better? wait and see ClosedCollection, if it´s not blank...
	    return (size((BNode) obj, RDF.FIRST, null, true, contexts) > 0);
	} else {
	    return false;
	}
    }

}
