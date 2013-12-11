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
package org.universAAL.drools.engine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Class to parse rules and extract humanId's.
 * 
 * @author Miguel Llorente (mllorente)
 * 
 */
public final class ParseRule {

	private ParseRule() {
		/*
		 * DO NOT USE.
		 */
	}

	/**
	 * Returns the humanIds that are in the rule definition.
	 * 
	 * @param ruleDefinition
	 *            the rule definition.
	 * @return thehumanIds that are in the rule definition.
	 */
	public static List<String> extractHumanIds(final String ruleDefinition) {
		final List<String> humanIds = new ArrayList<String>();
		final String delim = "rule";

		final String[] parsedDefinition = ruleDefinition.split(delim);
		final List<String> parsedDefinitionList = Arrays
				.asList(parsedDefinition);

		for (final String parsed : parsedDefinitionList) {
			final String nextToken = parsed.trim();
			// System.out.println(nextToken);
			if (isRuleHumanIdPresent(nextToken)) {
				humanIds.add(extractRuleName(nextToken));
			}
		}

		return humanIds;
	}

	/**
	 * Returns the rule name contained in the string nextToken.
	 * 
	 * @param nextToken
	 *            the rule definition, from humanId till end.
	 * @return the rule name contained in the string nextToken.
	 */
	private static String extractRuleName(final String nextToken) {
		final int initialIndex = nextToken.indexOf("\"");
		final int finalIndex = nextToken.indexOf("\"", initialIndex + 1);
		return nextToken.substring(initialIndex + 1, finalIndex);
	}

	/**
	 * Returns true if in the string nextToken is contained a rule humanId.
	 * False otherwise.
	 * 
	 * @param nextToken
	 *            the rule definition, from humanId till end.
	 * @return true if in the string nextToken is contained a rule humanId.
	 *         False otherwise.
	 */
	private static boolean isRuleHumanIdPresent(final String nextToken) {
		if (nextToken.startsWith("\"")) {
			return true;
		}
		return false;

	}
}
