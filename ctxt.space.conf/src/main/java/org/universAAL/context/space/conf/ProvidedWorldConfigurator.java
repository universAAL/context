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

import java.util.Hashtable;

import org.universAAL.middleware.service.owls.profile.ServiceProfile;
import org.universAAL.middleware.owl.Restriction;

import org.universAAL.ontology.phThing.PhysicalThing;
import org.universAAL.ontology.location.Location;
import org.universAAL.ontology.location.Way;
import org.universAAL.ontology.space.SpaceConfigurationService;

public class ProvidedWorldConfigurator extends SpaceConfigurationService {
	public static final String LOCATION_SERVER_NAMESPACE = "http://ontology.igd.fhg.de/FhIGD_AALLabConfigurator.owl#";
	public static final String MY_URI = LOCATION_SERVER_NAMESPACE + "LabManager";
	
	static final String SERVICE_GET_LOCATION = LOCATION_SERVER_NAMESPACE + "getLocationByURI";
	static final String SERVICE_GET_PHYSICAL_THING = LOCATION_SERVER_NAMESPACE + "getPhysicalThingByURI";
	static final String SERVICE_GET_THINGS_ON_WAY = LOCATION_SERVER_NAMESPACE + "getThingsOnWay";
	static final String SERVICE_ADD_LOCATION = LOCATION_SERVER_NAMESPACE + "addLocation";
	static final String SERVICE_ADD_PHYSICAL_THING = LOCATION_SERVER_NAMESPACE + "addPhysicalThing";
	static final String SERVICE_GET_WORLD = LOCATION_SERVER_NAMESPACE + "getWorld";
	
	static final String INPUT_LOCATION_OBJECT = LOCATION_SERVER_NAMESPACE + "inputLocationObject";
	static final String INPUT_PHYSICAL_THING_OBJECT = LOCATION_SERVER_NAMESPACE + "inputPhysicalThingObject";
	static final String INPUT_WAY = LOCATION_SERVER_NAMESPACE + "inputWayObject";
	
	static final String OUTPUT_LOCATION_OBJECT = LOCATION_SERVER_NAMESPACE + "locationObjectOutput";
	static final String OUTPUT_PHYSICAL_THING = LOCATION_SERVER_NAMESPACE + "physicalThingOutput";
	static final String OUTPUT_WORLD = LOCATION_SERVER_NAMESPACE + "world";
	
	static final ServiceProfile[] profiles = new ServiceProfile[6];
	private static Hashtable serverLocationRestrictions = new Hashtable();
	static {
		register(ProvidedWorldConfigurator.class);
		addRestriction((Restriction)
				SpaceConfigurationService.getClassRestrictionsOnProperty(SpaceConfigurationService.PROP_MANAGED_LOCATIONS).copy(),
				new String[] {SpaceConfigurationService.PROP_MANAGED_LOCATIONS},
				serverLocationRestrictions);
		addRestriction((Restriction)
				SpaceConfigurationService.getClassRestrictionsOnProperty(SpaceConfigurationService.PROP_MANAGED_PHYSICAL_THINGS).copy(),
				new String[] {SpaceConfigurationService.PROP_MANAGED_PHYSICAL_THINGS},
				serverLocationRestrictions);
		
		String[] ppLocations = new String[] {SpaceConfigurationService.PROP_MANAGED_LOCATIONS};
		String[] ppPhysicalThings = new String[] {SpaceConfigurationService.PROP_MANAGED_PHYSICAL_THINGS};
		String[] ppConnected = new String[] {SpaceConfigurationService.PROP_MANAGED_PHYSICAL_THINGS,PhysicalThing.PROP_PHYSICAL_LOCATION,Location.PROP_IS_CONNECTED_TO};
		
		ProvidedWorldConfigurator getLocation = new ProvidedWorldConfigurator(SERVICE_GET_LOCATION);
		getLocation.addFilteringInput(INPUT_LOCATION_OBJECT, Location.MY_URI, 1, 1, ppLocations);
		getLocation.addOutput(OUTPUT_LOCATION_OBJECT, Location.MY_URI, 0, 0, ppLocations);
		profiles[0] = getLocation.myProfile;
				
		ProvidedWorldConfigurator addLocation = new ProvidedWorldConfigurator(SERVICE_ADD_LOCATION);
		addLocation.addInputWithAddEffect(INPUT_LOCATION_OBJECT, Location.MY_URI, 1, 1, ppLocations);
		profiles[1] = addLocation.myProfile;

		ProvidedWorldConfigurator addPhys = new ProvidedWorldConfigurator(SERVICE_ADD_PHYSICAL_THING);
		addPhys.addInputWithAddEffect(INPUT_PHYSICAL_THING_OBJECT, PhysicalThing.MY_URI, 1, 1, ppPhysicalThings);
		profiles[2] = addPhys.myProfile;
		
		ProvidedWorldConfigurator getObjectsOnTheWay = new ProvidedWorldConfigurator(SERVICE_GET_THINGS_ON_WAY);
		getObjectsOnTheWay.addFilteringInput(INPUT_WAY, Way.MY_URI, 1, 1, ppConnected);
		getObjectsOnTheWay.addOutput(OUTPUT_PHYSICAL_THING, PhysicalThing.MY_URI, 0, -1,ppPhysicalThings); 		
		profiles[3] = getObjectsOnTheWay.myProfile;
		
		ProvidedWorldConfigurator getPhysicalThing = new ProvidedWorldConfigurator(SERVICE_GET_PHYSICAL_THING);
		getPhysicalThing.addFilteringInput(INPUT_PHYSICAL_THING_OBJECT, PhysicalThing.MY_URI, 1, 1, ppPhysicalThings);
		getPhysicalThing.addOutput(OUTPUT_PHYSICAL_THING, PhysicalThing.MY_URI, 0, 0, ppPhysicalThings);
		profiles[4] = getPhysicalThing.myProfile;
		
		ProvidedWorldConfigurator getWorld = new ProvidedWorldConfigurator(SERVICE_GET_WORLD);
		getWorld.addOutput(OUTPUT_WORLD, Location.MY_URI, 0, 0, ppLocations);
		profiles[0] = getWorld.myProfile;

	}
	
	public ProvidedWorldConfigurator(String uri) {
		super(uri);
	}
}
