/**
 * 
 */
package org.universAAL.context.conversion.jena.impl.util;

import org.universAAL.middleware.io.owl.AccessImpairment;
import org.universAAL.middleware.owl.supply.LevelRating;


/**
 * @author mtazari
 *
 */
public class HearingImpairment extends AccessImpairment {
	public static final String MY_URI;
	
	static {
		MY_URI = uAAL_VOCABULARY_NAMESPACE + "HearingImpairment";
		register(HearingImpairment.class);
	}
	
	public static String getRDFSComment() {
		return "Represents the level of the user's difficulty in hearing voice-based system output.";
	}
	
	public static String getRDFSLabel() {
		return "Hearing Impairment";
	}
	
	/**
	 * The constructor for (de-)serializers.
	 */
	public HearingImpairment() {
		super();
	}
	
	/**
	 * The constructor for use by applications.
	 */
	public HearingImpairment(LevelRating impairmentLevel) {
		super(impairmentLevel);
	}
	
	public void setImpairment(LevelRating rating) {
		props.put(AccessImpairment.PROP_IMPAIRMENT_LEVEL, rating);
	}
	
	public String toString() {
		return "Hearing Impairment: " + this.getImpaimentLevel().name();
	}
}
