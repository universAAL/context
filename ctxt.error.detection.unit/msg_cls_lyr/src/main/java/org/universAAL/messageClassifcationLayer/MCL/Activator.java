package org.universAAL.messageClassifcationLayer.MCL;
/**Copyright [2011-2014] [University of Siegen, Embedded System Instiute]

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

* @author <a href="mailto:abu.sadat@uni-siegen.de">Rubaiyat Sadat</a>
*	       ©2012
*/
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.container.osgi.uAALBundleContainer;

/**
 * initialization of the EDUCore.
 * @author <a href="mailto:hamzah.dakheel@uni-siegen.de">Hamzah Dakheel</a>
* @author <a href="mailto:abu.sadat@uni-siegen.de">Rubaiyat Sadat</a>
 *
 */
public class Activator implements BundleActivator {
    public static BundleContext osgiContext = null;
    public static ModuleContext context = null;

	public static CPublisher cpublisher=null;
	public static MessageDelivery ms =null;

    public void start(BundleContext bcontext) throws Exception {
	Activator.osgiContext = bcontext;
	Activator.context = uAALBundleContainer.THE_CONTAINER
		.registerModule(new Object[] { bcontext });
	    ms =new MessageDelivery("init");
		//cpublisher=new CPublisher(context);
    }

    public void stop(BundleContext arg0) throws Exception {
		//cpublisher.close();
    }

}
