package org.universAAL.drools.models;


/**
 * Drools rule description.
 * 
 * @author mllorente
 * @version $Rev: 1037 $ $Date: 2012-09-19 17:25:45 +0200 (mié, 19 sep 2012) $
 */


@SuppressWarnings("serial")
public class RuleModel{


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
     * @param ruleDefinition string with the drools rule definition.
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
     * @param ruleDefinition the rule description.
     */
    public void setRuleDefinition(final String ruleDefinition) {
        this.ruleDefinition = ruleDefinition;
    }

}
