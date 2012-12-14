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

import java.lang.Runnable;
import java.util.*;
import org.universAAL.middleware.context.ContextEvent;
/**
 * this class is using by Activator class to initialize EDUCore,
 * and by other uAAL components to deliver their events to the EDUCore.
 * @author <a href="mailto:hamzah.dakheel@uni-siegen.de">Hamzah Dakheel</a>
 ** @author <a href="mailto:abu.sadat@uni-siegen.de">Rubaiyat Sadat</a>

 */
public class MessageDelivery implements Runnable {
	Thread initialization;
	public static Map<Integer, Integer> m = new HashMap<Integer, Integer>();
static String pathToLibCore = "~/error.detection.unit/libEDUCore.so";
	
	public MessageDelivery(){
		
	}
	
	public MessageDelivery (String ThreadName){
		
		initialization = new Thread (this, ThreadName);
		initialization.start();
		
	}
	
	public void run() {
		//Display info about this particular thread
		System.out.println(Thread.currentThread());
		timer_trigger();
	}
	
	public native void timer_trigger();
	public native static synchronized void eventHandler(int ID, boolean count);
	
	static {
	    System.load(pathToLibCore);
	} /**/
	
	/*public int set_msg_count_table(int msg_Id){
		int result=0;
		Integer count = m.get(msg_Id);
		if (count == null)
		{
			m.put(msg_Id, 1);
			//result = 1;
		}
		else if (count == 5)
			result = 2;
		else if (count < 5)
		{
			m.put(msg_Id, ++count);
			if (count == 5)
			result = 1;
		}
		

		

		
		return result;
	}*/
	/**
	 * recognition of the first event, this recognition helps the EDUCore
	 * in make the initialization for some of its data structures.
	 * @param msg_Id
	 * @return
	 */
	
	public int get_first_msg (int msg_Id){
		int x =0;
		Integer count = m.get(msg_Id);
		if (count == null)
		{
			m.put(msg_Id, 1);
			x=1;
			//result = 1;
		}
		else
			x = 0;
		
		return x;
	}
	
	/**
	 * it receives the the event details from uAAL Comp. and delivers it
	 * to the EDUCore. 
	 * @param msg_Id
	 * @param msg
	 */
	
	public synchronized void deliverToEDU (int msg_Id, ContextEvent msg){
		
		//check the counter of the incoming message
		/*int msg_status;
		msg_status = set_msg_count_table(msg_Id);
		if (msg_status == 1)
			eventHandler(msg_Id, true);
		else if (msg_status == 2)
			eventHandler(msg_Id, false);*/
		
		if (this.get_first_msg(msg_Id)== 1)
			{
			this.eventHandler(msg_Id, true);
			}
		else
			this.eventHandler(msg_Id, false);
		
			
		
		}
	

}
