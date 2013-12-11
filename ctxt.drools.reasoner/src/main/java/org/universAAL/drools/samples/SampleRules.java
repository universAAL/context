/*
	Copyright 2008-2014 TSB, http://www.tsbtecnologias.es
	TSB - Tecnologías para la Salud y el Bienestar
	
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
package org.universAAL.drools.samples;

/**
 * Helper class for use examples of rules.
 * 
 * @author Miguel Llorente (mllorente)
 * 
 */
public class SampleRules {

	public static String SIMPL_RULE_2 = "package org.universAAL.AALapplication\r\n"

			+ "import org.universAAL.middleware.context.ContextEvent;\n"
			+ "import org.universAAL.drools.engine.RulesEngine;\n"
			+ "import org.universAAL.ontology.location.Location;\n"
			+ "import org.universAAL.middleware.rdf.Resource;\n"
			+ "import org.universAAL.ontology.phThing.Sensor;\n"
			+ "import java.util.Hashtable;\n"
			+ "import java.util.ArrayList;\n"
			+ "dialect \"java\" "
			// + "declare ContextEvent\n"
			// + "@role(event)\n"
			// + "@expires(2m)\n"
			// + "end\n"
			// + "declare Activity\n"
			// + "place : String @key\n"
			// + "intensity: String\n"
			// + "counter : int\n"
			// + "end\n"
			+ "rule \"SimpleRuleForTest\"\n"
			+ "when\n"
			+ "ContextEvent(  )\n"
			+ "then\n" + "System.out.println(\"Polo!\");\n" + "end\n";

}
