/*
	Copyright 2008-2014 ITACA-TSB, http://www.tsb.upv.es
	Instituto Tecnologico de Aplicaciones de Comunicacion 
	Avanzadas - Grupo Tecnologias para la Salud y el 
	Bienestar (TSB)
	
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
package org.universAAL.context.chemobile;

import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.sodapop.msg.MessageContentSerializer;

public class Hub {

    private MobileHistorySubscriber hc;

    public void start(ModuleContext context) throws Exception {
	hc = new MobileHistorySubscriber(context);
    }

    public void stop(ModuleContext context) throws Exception {
	hc.close();
    }

    public void setuAALParser(MessageContentSerializer service) {
	hc.setuAALParser(service);
    }

}
