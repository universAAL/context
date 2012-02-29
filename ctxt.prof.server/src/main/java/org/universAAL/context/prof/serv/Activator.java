package org.universAAL.context.prof.serv;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.container.osgi.uAALBundleContainer;
import org.universAAL.middleware.sodapop.msg.MessageContentSerializer;

public class Activator implements BundleActivator, ServiceListener {
    public static BundleContext osgiContext = null;
    public static ModuleContext context = null;
    public static SCallee scallee = null;
    public static SCaller scaller = null;
    protected static MessageContentSerializer parser=null;

    public void start(BundleContext bcontext) throws Exception {
	Activator.osgiContext = bcontext;
	Activator.context = uAALBundleContainer.THE_CONTAINER
		.registerModule(new Object[] { bcontext });
	scallee = new SCallee(context);
	scaller = new SCaller(context);
	String filter = "(objectclass="
		+ MessageContentSerializer.class.getName() + ")";
	osgiContext.addServiceListener(this, filter);
	ServiceReference[] references = osgiContext.getServiceReferences(null,
		filter);
	for (int i = 0; references != null && i < references.length; i++) {
	    this.serviceChanged(new ServiceEvent(ServiceEvent.REGISTERED,
		    references[i]));
	}
    }

    public void stop(BundleContext arg0) throws Exception {
	scallee.close();
	scaller.close();
    }

    public void serviceChanged(ServiceEvent event) {
	// Update the MessageContentSerializer
	switch (event.getType()) {
	case ServiceEvent.REGISTERED:
	case ServiceEvent.MODIFIED:
	    parser = (MessageContentSerializer) osgiContext
		    .getService(event.getServiceReference());
	    break;
	case ServiceEvent.UNREGISTERING:
	    parser = null;
	    break;
	default:
	    break;
	}
    }

}
