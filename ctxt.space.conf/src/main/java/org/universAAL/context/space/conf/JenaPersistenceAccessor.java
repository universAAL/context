/*
	Copyright 2007-2014 Fraunhofer IGD, http://www.igd.fraunhofer.de
	Fraunhofer Gesellschaft - Institut für Graphische Datenverarbeitung 
	
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
package org.universAAL.context.space.conf;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.util.Properties;
import java.util.Vector;

import org.osgi.framework.Bundle;
import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.util.LogUtils;
import org.universAAL.middleware.owl.ManagedIndividual;

import com.hp.hpl.jena.db.DBConnection;
import com.hp.hpl.jena.db.IDBConnection;
import com.hp.hpl.jena.db.ModelRDB;
import com.hp.hpl.jena.rdf.model.Model;
//import com.hp.hpl.jena.rdf.model.Resource;

import org.universAAL.ontology.phThing.PhysicalThing;
import org.universAAL.ontology.location.Location;
import org.universAAL.ontology.location.Place;
import org.universAAL.ontology.location.Way;
import org.universAAL.ontology.location.position.CoordinateSystem;
import org.universAAL.ontology.location.position.Point;
import org.universAAL.ontology.shape.Shape;
import org.universAAL.ontology.shape.Shape3D;
import org.universAAL.ontology.shape.Sphere;



public class JenaPersistenceAccessor {

	class DriverWrap implements Driver {
		
		private Driver driver;
		
		DriverWrap(Driver d) {
			this.driver = d;
		}
		
		public boolean acceptsURL(String u) throws SQLException {
			return this.driver.acceptsURL(u);
		}
		
		public Connection connect(String u, Properties p) throws SQLException {
			return this.driver.connect(u, p);
		}
		
		public int getMajorVersion() {
			return this.driver.getMajorVersion();
		}
		
		public int getMinorVersion() {
			return this.driver.getMinorVersion();
		}
		
		public DriverPropertyInfo[] getPropertyInfo(String u, Properties p) throws SQLException {
			return this.driver.getPropertyInfo(u, p);
		}
		
		public boolean jdbcCompliant() {
			return this.driver.jdbcCompliant();
		}
	}

	Model model = null;

	public JenaPersistenceAccessor(Bundle mysqlconnectorjava) {
		
		// database URL
		String M_DB_URL         = System.getProperty("org.persona.platform.jena_db.url",
		"jdbc:mysql://localhost:3306/persona_aal_space");
		
		// User name
		String M_DB_USER        = System.getProperty("org.persona.platform.ui.dm.db_user", "space_conf");
		
		// Password
		String M_DB_PASSWD      = System.getProperty("org.persona.platform.ui.dm.db_passwd", "space_conf");
		
		// Jena model name
		String MODEL_NAME = System.getProperty("org.persona.platform.jena_db.model_name", "PERSONA_AAL_Space");
		
		// Database engine name
		String M_DB = "MySQL";
		
		// JDBC driver
		String M_DBDRIVER_CLASS = "com.mysql.jdbc.Driver";
		
		Connection sqlconn = null;
		try {
			Driver d = (Driver) mysqlconnectorjava.loadClass(M_DBDRIVER_CLASS).newInstance();
			DriverManager.registerDriver(new DriverWrap(d));
			sqlconn = DriverManager.getConnection(M_DB_URL,M_DB_USER,M_DB_PASSWD);
		} catch (Exception e) {
			LogUtils.logInfo(Activator.getLogger(), "JenaPersistanceAccessor", "JenaPersistanceAccessor", new Object[] {
			"Error occured while connecting to Database! " },
			e);
			e.printStackTrace();
		}
		
		// create a database connection
		IDBConnection conn = new DBConnection(sqlconn,M_DB);
		
		// or open a previously created named model
		model = ModelRDB.open(conn,	MODEL_NAME);
		((ModelRDB)model).setDoDuplicateCheck(true);
	}

	public void clear() {
		model.removeAll();
	}

	public void close() {
		model.close();
	}

	/** adda a location to the peristent jena db
	 * 
	 * @param loc
	 * @return
	 */
	public com.hp.hpl.jena.rdf.model.Resource addResource(ManagedIndividual loc) {
		com.hp.hpl.jena.rdf.model.Resource r = model.createResource(loc.getURI());
		model.remove(r.getModel());
		r = Activator.getModelConverter().toJenaResource(loc);
		model.add(r.getModel());
		model.commit();
		return r;
	}
	
	/** Adds a location to the persistent jena db and sets the containing/contained
	 *  properties for all locations under masterLoc. This property is calculated with the bounding volume
	 *  and is currently possible for the bounding volumes sphere and box.
	 * 
	 * @param loc the location to add
	 * @param masterLoc any location uri containing the loc
	 */
	public void addContainedResource(Place loc, String masterLoc) {
		Place master = (Place) getByURI(masterLoc);
		if(master == null) throw new IllegalArgumentException("given master location is not valid");
		CoordinateSystem common = CoordinateSystem.findCommonParentSystem(loc.getShape().getLocalCoordinateSystem(), master.getShape().getLocalCoordinateSystem());
		if(common == null) throw new IllegalArgumentException("location are not within the same coordinate system tree");
		createContainProperty(loc,master);
		addResource(loc);
	}
	
	private void createContainProperty(Place loc,Place currMaster) {
		Location[] locs = currMaster.getContainedLocations();
		Shape mybnds = loc.getShape().getBoundingVolume();
		boolean hasContainingChild = false;
		for(int i=0;i<locs.length;i++) {
			if(locs[i] instanceof Place) {
				Shape bnds = ((Place)locs[i]).getShape().getBoundingVolume();
				if(bnds instanceof Sphere && ((Sphere)bnds).contains((Shape3D)mybnds)) {
					hasContainingChild = true;
					createContainProperty(loc,(Place)locs[i]);
				}
			}
		}
		if(!hasContainingChild) {
			currMaster.addContainedLocation(loc);
			loc.setContainingLocation(currMaster);
		}
	}
	/**
	 * Returns the Java Object defined by the given uri, according to the jena location database
	 * @param uri
	 * @return
	 */
	public Resource getByURI(String uri) {
		com.hp.hpl.jena.rdf.model.Resource r = model.createResource(uri);
		return Activator.getModelConverter().toPersonaResource(r);
	}
	
	/**
	 * 
	 * @param way
	 * @return
	 */
	public PhysicalThing[] getByWay(Way way) {
		Location loc = way.getContainingLocation();
		Vector v = getByWayDirect(way,loc);
		PhysicalThing[] pts = new PhysicalThing[v.size()];
		for(int i=0;i<v.size();i++)
			pts[i] = (PhysicalThing) v.get(i);
		return pts;
	}
	
	
	/**
	 * This method returns a Vector of FHPhysicalThing instances,
	 * which are marked by the specified way object.
	 * 
	 * 
	 * @param way The way object concerning to the recognized gesture
	 * @param loc the location object where the marked physical things come from
	 *            besides it is the location object, which contains the way object
	 * @return a Vector with the marked physical things
	 */
	private Vector getByWayDirect(Way way, Location loc) {
		Vector pts = new Vector();
		Location[] containedLocations = loc.getContainedLocations();
		
		if(containedLocations != null)	
			for(int i=0;i<containedLocations.length;i++) {
				
				// Either containedLocations[i] is an object of the type Place and at its position is a physical thing
			    // --> then check if contained[i] is near enough to the way object and put the concerning physical thing to the answers
			    if(containedLocations[i] instanceof Place && ((Place)containedLocations[i]).getPhysicalThingofLocation() != null) {
				    float dist = way.getDistanceTo(containedLocations[i]);
				    if((int)dist == Point.INTERSECTING)
				    	pts.add(((Place)containedLocations[i]).getPhysicalThingofLocation());
			    } else {
				
				    // or there is not a physical thing at containedLocations[i]'s position
				    // --> then do the same check like getByWaydirect for the way object and contained[i]
				    // which may be a location with some sublocations
				    pts.addAll(getByWayDirect(way,containedLocations[i]));
		    	}
		    }
		return pts;
	}
		
}
