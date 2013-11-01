/**
 * 
 */
package org.universAAL.context.conversion.jena.impl;

import java.io.File;
import java.util.Locale;

import junit.framework.TestCase;

import org.universAAL.middleware.container.Container;
import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.owl.DataRepOntology;
import org.universAAL.middleware.owl.IntRestriction;
import org.universAAL.middleware.owl.MergedRestriction;
import org.universAAL.middleware.owl.OntologyManagement;
import org.universAAL.middleware.owl.supply.LevelRating;
import org.universAAL.middleware.rdf.PropertyPath;
import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.rdf.TypeMapper;
import org.universAAL.middleware.ui.UIRequest;
import org.universAAL.middleware.ui.owl.PrivacyLevel;
import org.universAAL.middleware.ui.owl.UIBusOntology;
import org.universAAL.middleware.ui.rdf.ChoiceItem;
import org.universAAL.middleware.ui.rdf.Form;
import org.universAAL.middleware.ui.rdf.Group;
import org.universAAL.middleware.ui.rdf.Label;
import org.universAAL.middleware.ui.rdf.MediaObject;
import org.universAAL.middleware.ui.rdf.Range;
import org.universAAL.middleware.ui.rdf.Select;
import org.universAAL.middleware.ui.rdf.SubdialogTrigger;
import org.universAAL.middleware.ui.rdf.Submit;
import org.universAAL.middleware.ui.rdf.TextArea;
import org.universAAL.middleware.util.Constants;

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
		ModuleContext mc= new ModuleContext() {
		    
		    public boolean uninstall(ModuleContext arg0) {
			// TODO Auto-generated method stub
			return false;
		    }
		    
		    public boolean stop(ModuleContext arg0) {
			// TODO Auto-generated method stub
			return false;
		    }
		    
		    public boolean start(ModuleContext arg0) {
			// TODO Auto-generated method stub
			return false;
		    }
		    
		    public void setAttribute(String arg0, Object arg1) {
			// TODO Auto-generated method stub
			
		    }
		    
		    public void registerConfigFile(Object[] arg0) {
			// TODO Auto-generated method stub
			
		    }
		    
		    public void logWarn(String arg0, String arg1, Throwable arg2) {
			// TODO Auto-generated method stub
			
		    }
		    
		    public void logTrace(String arg0, String arg1, Throwable arg2) {
			// TODO Auto-generated method stub
			
		    }
		    
		    public void logInfo(String arg0, String arg1, Throwable arg2) {
			// TODO Auto-generated method stub
			
		    }
		    
		    public void logError(String arg0, String arg1, Throwable arg2) {
			// TODO Auto-generated method stub
			
		    }
		    
		    public void logDebug(String arg0, String arg1, Throwable arg2) {
			// TODO Auto-generated method stub
			
		    }
		    
		    public File[] listConfigFiles(ModuleContext arg0) {
			// TODO Auto-generated method stub
			return null;
		    }
		    
		    public String getID() {
			// TODO Auto-generated method stub
			return null;
		    }
		    
		    public Container getContainer() {
			// TODO Auto-generated method stub
			return null;
		    }
		    
		    public Object getAttribute(String arg0) {
			// TODO Auto-generated method stub
			return null;
		    }
		    
		    public boolean canBeUninstalled(ModuleContext arg0) {
			// TODO Auto-generated method stub
			return false;
		    }
		    
		    public boolean canBeStopped(ModuleContext arg0) {
			// TODO Auto-generated method stub
			return false;
		    }
		    
		    public boolean canBeStarted(ModuleContext arg0) {
			// TODO Auto-generated method stub
			return false;
		    }

		    public Object getProperty(String arg0) {
			// TODO Auto-generated method stub
			return null;
		    }

		    public Object getProperty(String arg0, Object arg1) {
			// TODO Auto-generated method stub
			return null;
		    }
		};
		OntologyManagement.getInstance().register(mc, new DataRepOntology());
		OntologyManagement.getInstance().register(mc, new UIBusOntology());
	}

	public void testForm() {
//	    if (true)
//		return;
	    
		PropertyPath pp1 = new PropertyPath(null, false,
				new String[] { DUMMY_PROP_1 });
		PropertyPath pp2 = new PropertyPath(null, false,
				new String[] { DUMMY_PROP_2 });
		PropertyPath pp3 = new PropertyPath(null, false,
				new String[] { DUMMY_PROP_3 });
		
		MergedRestriction restr = MergedRestriction.getAllValuesRestriction(
				DUMMY_PROP_2, TypeMapper.getDatatypeURI(String.class));
		MergedRestriction selectRestr = MergedRestriction.getAllValuesRestriction(
				DUMMY_PROP_3, TypeMapper.getDatatypeURI(Integer.class));
		
	MergedRestriction ordrRestr = MergedRestriction
		.getAllValuesRestriction(DUMMY_PROP_1,
			new IntRestriction(new Integer(
				0), true, new Integer(30), true));

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

		UIRequest ur = new UIRequest(testUser, f, LevelRating.middle, Locale.ENGLISH, PrivacyLevel.insensible);

		JenaModelConverter jmc = new JenaModelConverter();

		// TODO
//		new ResourceComparator().printDiffs(ur,
//				jmc.toPersonaResource(jmc.toJenaResource(ur)));
	}
}
