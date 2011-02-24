/**
 * 
 */
package org.universAAL.context.conversion.jena.impl;

import java.util.Locale;

import org.universAAL.context.conversion.jena.impl.JenaModelConverter;
import org.universAAL.middleware.io.owl.PrivacyLevel;
import org.universAAL.middleware.io.rdf.*;
import org.universAAL.middleware.output.OutputEvent;
import org.universAAL.middleware.owl.OrderingRestriction;
import org.universAAL.middleware.owl.Restriction;
import org.universAAL.middleware.owl.supply.LevelRating;
import org.universAAL.middleware.rdf.PropertyPath;
import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.rdf.TypeMapper;
import org.universAAL.middleware.util.Constants;
import org.universAAL.middleware.util.ResourceComparator;

import junit.framework.TestCase;

/**
 * @author mtazari
 * 
 */
public class FormTest extends TestCase {
	
	private static final String DUMMY_PROP_1 = "urn:dummy#dummyProp1";
	private static final String DUMMY_PROP_2 = "urn:dummy#dummyProp2";
	private static final String DUMMY_PROP_3 = "urn:dummy#dummyProp3";
	static final Resource testUser = 
		new Resource(Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX + "saied");

	public FormTest(String name) {
		super(name);
	}

	public void testForm() {
		PropertyPath pp1 = new PropertyPath(null, false,
				new String[] { DUMMY_PROP_1 });
		PropertyPath pp2 = new PropertyPath(null, false,
				new String[] { DUMMY_PROP_2 });
		PropertyPath pp3 = new PropertyPath(null, false,
				new String[] { DUMMY_PROP_3 });
		
		Restriction restr = Restriction.getAllValuesRestriction(
				DUMMY_PROP_2, TypeMapper.getDatatypeURI(String.class));
		Restriction selectRestr = Restriction.getAllValuesRestriction(
				DUMMY_PROP_3, TypeMapper.getDatatypeURI(Integer.class));
		OrderingRestriction ordrRestr = OrderingRestriction.newOrderingRestriction(
				new Integer(30), new Integer(0), true, true,
				Restriction.getAllValuesRestriction(DUMMY_PROP_1,
						TypeMapper.getDatatypeURI(Integer.class)));

		Form f = Form.newDialog("Test Form", testUser);
		f.setDialogCreator("Test Component");
		Group controls = f.getIOControls();
		
		Group controls2 = new Group(controls, new Label("child group1", null),
				null, null, null);
		MediaObject mo1 = new MediaObject(controls2, null, "image", "/img/meal.jpg");
		mo1.setPreferredResolution(100, 100);
		new SubdialogTrigger(controls2, new Label("Edit", null), "edit1");

		Group controls3 = new Group(controls, new Label("child group2", null),
				null, null, null);
		MediaObject mo2 = new MediaObject(controls3, null, "image", "/img/meal.jpg");
		mo2.setPreferredResolution(100, 100);
		new SubdialogTrigger(controls3, new Label("Edit", null), "edit2");
		
		Group controls4 = new Group(controls, new Label("child group3", null),
				null, null, null);
		MediaObject mo3 = new MediaObject(controls4, null, "image", "/img/meal.jpg");
		mo3.setPreferredResolution(100, 100);
		new SubdialogTrigger(controls4, new Label("Edit", null), "edit3");

		MediaObject mo = new MediaObject(controls, null, "image", "/img/meal.jpg");
		mo.setPreferredResolution(100, 100);
		new TextArea(controls, new Label("Dummy2", null), pp2, restr,
				"Coffee with milk \nToast with butter and marmelade\nFruit or juice");
		Select testSelect = new Select(controls, new Label("Dummy3", null),
				pp3, selectRestr, new Integer(0));
		for (int t = 0; t < 7; t++)
			testSelect.addChoiceItem(new ChoiceItem("myLabel"+t, null, new Integer(t)));
		new Range(controls, new Label("Dummy", null), pp1,
				ordrRestr, new Integer(10));
		
		Group mySubmits = f.getSubmits();
		new Submit(mySubmits, new Label("TODAY", null), "todayMenu");
		new Submit(mySubmits, new Label("WEEK", null), "weekMenu");
		new Submit(mySubmits, new Label("My PROFILE", null), "myProfile");
		new Submit(mySubmits, new Label("HOME", null), "startPage");

		OutputEvent oe = new OutputEvent(testUser, f,
				LevelRating.middle, Locale.ENGLISH, PrivacyLevel.insensible);

		JenaModelConverter jmc = new JenaModelConverter();
		jmc.setTypeMapper(TypeMapper.getTypeMapper());

		new ResourceComparator().printDiffs(oe,
				jmc.toPersonaResource(jmc.toJenaResource(oe)));
	}
}
