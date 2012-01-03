package org.universAAL.context.che.ontology;

import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.rdf.impl.ResourceFactoryImpl;

public class ContextHistoryFactory extends ResourceFactoryImpl {

    public Resource createInstance(String classURI, String instanceURI,
	    int factoryIndex) {
	switch (factoryIndex) {
	case 0:
	    return new ContextEvent(instanceURI);
	case 1:
	    return new ContextHistoryService(instanceURI);
	}
	return null;
    }

}
