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
public class SightImpairment extends AccessImpairment{
	public static final String MY_URI;
	
	static {
		MY_URI = uAAL_VOCABULARY_NAMESPACE + "SightImpairment";
		register(SightImpairment.class);
	}
	
	public static String getRDFSComment() {
		return "Represents the level of the user's difficulty in seeing GUI-based system output.";
	}
	
	public static String getRDFSLabel() {
		return "Sight Impairment";
	}
	
	/**
	 * The constructor for (de-)serializers.
	 */
	public SightImpairment() {
		super();
	}
	
	/**
	 * The constructor for use by applications.
	 */
	public SightImpairment(LevelRating impairmentLevel) {
		super(impairmentLevel);
	}
	
	public void setImpairment(LevelRating rating) {
		props.put(AccessImpairment.PROP_IMPAIRMENT_LEVEL, rating);
	}
	
	public static SightImpairment loadInstance() {
		return new SightImpairment(LevelRating.none);
	}
	
	public String toString() {
		return "Sight Imapirment: " + this.getImpaimentLevel().name();
	}
}
