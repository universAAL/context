package org.universAAL.context.sysinfo;

import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.container.SharedObjectListener;
import org.universAAL.middleware.container.utils.LogUtils;
import org.universAAL.middleware.context.ContextEvent;
import org.universAAL.middleware.interfaces.PeerCard;
import org.universAAL.middleware.interfaces.aalspace.AALSpaceDescriptor;
import org.universAAL.middleware.interfaces.aalspace.AALSpaceStatus;
import org.universAAL.middleware.managers.api.AALSpaceListener;
import org.universAAL.middleware.managers.api.AALSpaceManager;
import org.universAAL.middleware.util.Constants;
import org.universAAL.ontology.sysinfo.SystemInfo;

public class SystemEventsListener implements SharedObjectListener,
	AALSpaceListener {

    private ModuleContext context;
    private AALSpaceManager aalSpaceManager;
    private boolean initialized;
    private SystemInfo sys = new SystemInfo(
	    Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX + "sysInfoPublisher");

    public SystemEventsListener(ModuleContext context) {
	this.context = context;
	init();

    }

    public boolean init() {
	if (!initialized) {
	    logDebug("Initializing the System Info Provider...");
	    Object[] aalManagers = context.getContainer()
		    .fetchSharedObject(
			    context,
			    new Object[] { AALSpaceManager.class.getName()
				    .toString() }, this);
	    if (aalManagers != null) {
		aalSpaceManager = (AALSpaceManager) aalManagers[0];
		aalSpaceManager.addAALSpaceListener(this);
	    } else {
		logDebug("No AALSpaceManagers found");
		initialized = false;
		return initialized;
	    }
	    logDebug("System Info Provider initialized");
	} else {
	    logDebug("System Info Provider already initialized");
	}
	return initialized;
    }

    public void sharedObjectAdded(Object sharedObj, Object arg1) {
	if (sharedObj instanceof AALSpaceManager) {
	    logDebug("AALSpaceManager service added detected > updating");
	    aalSpaceManager = (AALSpaceManager) sharedObj;
	    aalSpaceManager.addAALSpaceListener(this);
	}
    }

    public void sharedObjectRemoved(Object sharedObj) {
	if (sharedObj instanceof AALSpaceManager) {
	    logDebug("AALSpaceManager service removed detected > updating");
	    aalSpaceManager = null;
	    initialized = false;
	}
    }

    private void logDebug(String msg) {
	if (context != null) {
	    LogUtils.logDebug(context, SystemEventsListener.class,
		    "SystemInfoProvider", new Object[] { msg }, null);
	} else {
	    System.out.println(msg);
	}
    }

    private void logInfo(String msg) {
	if (context != null) {
	    LogUtils.logInfo(context, SystemEventsListener.class,
		    "SystemInfoProvider", new Object[] { msg }, null);
	} else {
	    System.out.println(msg);
	}
    }

    public void aalSpaceJoined(AALSpaceDescriptor spaceDescriptor) {
	logInfo("AALSPACEJOINED");
	org.universAAL.ontology.sysinfo.AALSpaceDescriptor des = new org.universAAL.ontology.sysinfo.AALSpaceDescriptor(
		Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX
			+ spaceDescriptor.getSpaceCard().getSpaceID());
	des.setSerializedValue(spaceDescriptor.toString());
	sys.setSpaceJoined(des);
	ContextEvent ev = new ContextEvent(sys, SystemInfo.PROP_SPACE_JOINED);
	if (Activator.cpublisher != null)
	    Activator.cpublisher.publish(ev);
    }

    public void aalSpaceLost(AALSpaceDescriptor spaceDescriptor) {
	logInfo("AALSPACELOST");
	org.universAAL.ontology.sysinfo.AALSpaceDescriptor des = new org.universAAL.ontology.sysinfo.AALSpaceDescriptor(
		Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX
			+ spaceDescriptor.getSpaceCard().getSpaceID());
	des.setSerializedValue(spaceDescriptor.toString());
	sys.setSpaceLost(des);
	ContextEvent ev = new ContextEvent(sys, SystemInfo.PROP_SPACE_LOST);
	if (Activator.cpublisher != null)
	    Activator.cpublisher.publish(ev);
    }

    public void newPeerJoined(PeerCard peer) {
	logInfo("PEERJOINED");
	org.universAAL.ontology.sysinfo.PeerCardDescriptor des = new org.universAAL.ontology.sysinfo.PeerCardDescriptor(
		Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX + peer.getPeerID());
	des.setSerializedValue(peer.toString());
	sys.setPeerJoined(des);
	ContextEvent ev = new ContextEvent(sys, SystemInfo.PROP_PEER_JOINED);
	if (Activator.cpublisher != null)
	    Activator.cpublisher.publish(ev);
    }

    public void peerLost(PeerCard peer) {
	logInfo("PEERLOST");

	org.universAAL.ontology.sysinfo.PeerCardDescriptor des = new org.universAAL.ontology.sysinfo.PeerCardDescriptor(
		Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX + peer.getPeerID());
	des.setSerializedValue(peer.toString());
	sys.setPeerLost(des);
	ContextEvent ev = new ContextEvent(sys, SystemInfo.PROP_PEER_LOST);
	if (Activator.cpublisher != null)
	    Activator.cpublisher.publish(ev);
    }

    public void aalSpaceStatusChanged(AALSpaceStatus status) {
	logInfo("AALSPACECHANGED");
	org.universAAL.ontology.sysinfo.AALSpaceStatusDescriptor des = new org.universAAL.ontology.sysinfo.AALSpaceStatusDescriptor(
		Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX + status.toString());
	des.setSerializedValue(status.toString());
	sys.setSpaceChanged(des);
	ContextEvent ev = new ContextEvent(sys, SystemInfo.PROP_SPACE_CHANGED);
	if (Activator.cpublisher != null)
	    Activator.cpublisher.publish(ev);
    }

}
