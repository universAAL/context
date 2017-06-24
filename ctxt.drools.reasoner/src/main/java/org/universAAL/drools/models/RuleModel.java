/*
    Copyright 2007-2014 TSB, http://www.tsbtecnologias.es
    Technologies for Health and Well-being - Valencia, Spain

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
package org.universAAL.drools.models;

/**
 * Drools rule description.
 *
 * @author mllorente
 * @version $Rev: 1037 $ $Date: 2012-09-19 17:25:45 +0200 (mié, 19 sep 2012) $
 */

@SuppressWarnings("serial")
public class RuleModel {

	/**
	 * Rule human ruleDefinition.
	 */
	protected String ruleDefinition;

	@SuppressWarnings("unused")
	private RuleModel() {
		// DO NOTHING. DO NOT USE.
	}

	/**
	 * Constructor for one Rule human id.
	 *
	 * @param ruleDefinition
	 *            string with the drools rule definition.
	 */
	public RuleModel(final String ruleDefinition) {
		this.ruleDefinition = ruleDefinition;
	}

	/**
	 * Returns the rule definition. The rule itself.
	 *
	 * @return the rule definition. The rule itself.
	 */
	public String getRuleDefinition() {
		return ruleDefinition;
	}

	/**
	 * Allows to specify the rule description.
	 *
	 * @param ruleDefinition
	 *            the rule description.
	 */
	public void setRuleDefinition(final String ruleDefinition) {
		this.ruleDefinition = ruleDefinition;
	}

}
