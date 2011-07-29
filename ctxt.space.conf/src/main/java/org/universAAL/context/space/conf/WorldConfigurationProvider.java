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

import java.util.ArrayList;
import java.util.List;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.universAAL.middleware.service.CallStatus;
import org.universAAL.middleware.service.ServiceCall;
import org.universAAL.middleware.service.ServiceCallee;
import org.universAAL.middleware.service.ServiceResponse;
import org.universAAL.middleware.service.owls.process.ProcessOutput;
import org.universAAL.middleware.util.Constants;
import org.universAAL.middleware.util.LogUtils;
import org.universAAL.middleware.owl.ManagedIndividual;

import org.universAAL.ontology.phThing.PhysicalThing;
import org.universAAL.ontology.shape.Box;
import org.universAAL.ontology.furniture.Furniture;
import org.universAAL.ontology.furniture.FurnitureType;
import org.universAAL.ontology.location.Location;
import org.universAAL.ontology.location.Place;
import org.universAAL.ontology.location.Way;
import org.universAAL.ontology.location.indoor.Room;
import org.universAAL.ontology.location.indoor.RoomFunction;
import org.universAAL.ontology.location.outdoor.Building;
import org.universAAL.ontology.location.outdoor.City;
import org.universAAL.ontology.location.position.CoordinateSystem;
import org.universAAL.ontology.location.position.OriginedMetric;
import org.universAAL.ontology.location.position.Point;

public class WorldConfigurationProvider extends ServiceCallee {
	static final String LOCATION_URI_PREFIX = ProvidedWorldConfigurator.LOCATION_SERVER_NAMESPACE
			+ "Location";

	private static final ServiceResponse failureResponse = new ServiceResponse(
			CallStatus.serviceSpecificFailure);
	
	static {
		failureResponse.addOutput(new ProcessOutput(
				ServiceResponse.PROP_SERVICE_SPECIFIC_ERROR, "Invalid input or internal failure!"));
	}

	private JenaPersistenceAccessor theServer;
	
	private Location world;
	
	public WorldConfigurationProvider(BundleContext context) {
		super(context, ProvidedWorldConfigurator.profiles);

		Bundle[] bundles = context.getBundles();
		int i = 0;
		while (i < bundles.length && !bundles[i].toString().contains("mysql-connector-java"))
			i++;
		
		if (i >= bundles.length) {
			try {
				throw new Exception("No Mysql Connector Bundle found");
			} catch (Exception e) {
				LogUtils.logInfo(Activator.getLogger(), "LocationServiceProvider", "LocationServiceProvider", new Object[] {
				"No Mysql Connector Bundle found" },
				e);
				e.printStackTrace();
			}
		} else {
			// TODO: Duplicate Check
		    	try {
		    	    	theServer = new JenaPersistenceAccessor(bundles[i]);
		    	} catch (Exception e) {
		    	}

			LogUtils.logInfo(Activator.getLogger(), "LocationServiceProvider", "LocationServiceProvider", new Object[] {
			"LocationServiceProvider started!" },
			null);
		}
		
		createWorld();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.persona.middleware.service.ServiceCallee#communicationChannelBroken()
	 */
	public void communicationChannelBroken() {
	}
	
	// TODO: has to be corrected and customized
	private void createWorld(){
		//##################################################################################
		//place creation
		// world
		Box worldShape = new Box(Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX+"worldShape",0,0,0,CoordinateSystem.WGS84);
		world = new Place(Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX+"world",worldShape);
		
		// darmstadt
		Box cityShape = new Box(Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX+"cityShape", 0, 0, 0);
		City darmstadt = new City(Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX+"darmstadt","Darmstadt",cityShape);
		((Place)world).addContainedLocation(darmstadt,0f,0f,0f);
		
		// lab
		Box labShape = new Box(Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX+"labShape", 7.18, 2.5, 11.96);
		Building lab = new Building(Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX+"lab",labShape);
		darmstadt.addContainedLocationRelativeToCorner(lab, 0, 0, 0);
		
		// workroom
		Box workroomShape = new Box(Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX+"workroomShape",7.18,2.5,3.78);
		Room workroom = new Room(Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX+"workroom",RoomFunction.WorkRoom,workroomShape);
		lab.addContainedLocationRelativeToCorner(workroom,0,0,0);
		
		// kitchen
		Box kitchenShape = new Box(Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX+"kitchenShape",3.48,2.5,3.94);
		Room kitchen = new Room(Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX+"kitchen",RoomFunction.Kitchen,kitchenShape);
		lab.addContainedLocationRelativeToCorner(kitchen,0f,3.9f,0f);
		
		// livingroom
		Box livingroomShape = new Box(Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX+"livingroomShape",3.58,2.5,6.26);
		Room livingroom = new Room(Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX+"livingroom",RoomFunction.SleepingRoom,livingroomShape);
		lab.addContainedLocationRelativeToCorner(livingroom,3.6f,3.9f,0f);
		
		// sleeping room
		Box bedroomShape = new Box(Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX+"bedroomShape",3.48,2.5,4.01);
		Room bedroom = new Room(Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX+"bedroom",RoomFunction.SleepingRoom,bedroomShape);
		lab.addContainedLocationRelativeToCorner(bedroom, 0f,7.94f,0f);
		
		// wardrobe
		Box wardrobeShape = new Box(Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX+"wardrobeShape",3.58,2.5,1.70);
		Room wardrobe = new Room(Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX+"wardrobe",RoomFunction.Wardrobe,wardrobeShape);
		lab.addContainedLocationRelativeToCorner(wardrobe, 3.6f,10.25f,0f);
		
		
		//##################################################################################
		//Furniture Sleepingroom
		Box sleepingroomLocker1Shape = new Box(Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX+"sleepingroomLocker1Shape",1.16f,2f,0.4f);
		Furniture sleepingroomLocker1 = new Furniture(Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX+"sleepingroomLocker1",FurnitureType.Locker,sleepingroomLocker1Shape);
		sleepingroomLocker1.setLocationRelativeToCorner(bedroom, 0.0f, 3.61f, 0f);
		
		Box sleepingroomLocker2Shape = new Box(Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX+"sleepingroomLocker2Shape",1.16f,2f,0.4f);
		Furniture sleepingroomLocker2 = new Furniture(Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX+"sleepingroomLocker2",FurnitureType.Locker,sleepingroomLocker2Shape);
		sleepingroomLocker2.setLocationRelativeToCorner(bedroom, 1.16f, 3.61f, 0f);
		
		Box sleepingroomLocker3Shape = new Box(Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX+"sleepingroomLocker3Shape",1.16f,2f,0.4f);
		Furniture sleepingroomLocker3 = new Furniture(Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX+"sleepingroomLocker3",FurnitureType.Locker,sleepingroomLocker3Shape);
		sleepingroomLocker3.setLocationRelativeToCorner(bedroom, 2.32f, 3.61f, 0f);
		
		Box sleepingroomHeaterShape = new Box(Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX+"sleepingroomHeaterShape",0.06f,1.8f,0.9f);
		Furniture sleepingroomHeater = new Furniture(Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX+"sleepingroomHeater",FurnitureType.Other,sleepingroomHeaterShape);
		sleepingroomHeater.setLocationRelativeToCorner(bedroom, 0f, 0.46f, 0.15f);
		
		Box sleepingroomShelfShape = new Box(Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX+"shelfShape",3f,0.74f,0.4f);
		Furniture sleepingroomShelf = new Furniture(Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX+"sleepingroomShelf",FurnitureType.Shelf,sleepingroomShelfShape);
		sleepingroomShelf.setLocationRelativeToCorner(bedroom, 0.24f, 0f, 0f);
		
		Box sleepingroomBedShape = new Box(Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX+"sleepingroomBedShape",2.1f,0.5f,1.55f);
		Furniture sleepingroomBed = new Furniture(Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX+"sleepingroomBed",FurnitureType.Bed,sleepingroomBedShape);
		sleepingroomBed.setLocationRelativeToCorner(bedroom, 1.38f, 3.23f, 0f);
		
		Box sleepingroomTVShape = new Box(Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX+"sleepingroomTVShape",0.79f,0.51f,0.15f);
		Furniture sleepingroomTV = new Furniture(Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX+"sleepingroomTV",FurnitureType.Other,sleepingroomTVShape);
		sleepingroomTV.setLocationRelativeToCorner(bedroom, 1.44f, 0.06f, 0.74f);
	
		
		//##################################################################################
		//Furniture wardrobe
		Box wardrobeLockerShape = new Box(Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX+"wardrobeLockerShape",1.25f,2f,0.4f);
		Furniture wardrobeLocker = new Furniture(Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX+"wardrobeLocker",FurnitureType.Locker,wardrobeLockerShape);
		wardrobeLocker.setLocationRelativeToCorner(wardrobe, 2.33f, 1.3f, 0f);
		
		Box wardrobeShelfShape = new Box(Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX+"wardrobeShelfShape",1.2f,0.4f,0.65f);
		Furniture wardrobeShelf = new Furniture(Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX+"wardrobeShelf",FurnitureType.Shelf,wardrobeShelfShape);
		wardrobeShelf.setLocationRelativeToCorner(wardrobe, 1.1f, 0f, 0f);
		
		
		//##################################################################################
		//Furniture livingroom 3.58/6.26
		Box livingroomShelf1Shape = new Box(Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX+"livingroomShelf1Shape",0.65f,0.4f,1.2f);
		Furniture livingroomShelf1 = new Furniture(Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX+"livingroomShelf1",FurnitureType.Shelf,livingroomShelf1Shape);
		livingroomShelf1.setLocationRelativeToCorner(livingroom, 2.93f, 5.06f, 0f);
		
		Box livingroomTV1Shape = new Box(Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX+"livingroomTV1Shape",0.15f,0.51f,0.79f);
		Furniture livingroomTV1 = new Furniture(Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX+"livingroomTV1",FurnitureType.Other,livingroomTV1Shape);
		livingroomTV1.setLocationRelativeToCorner(livingroom, 3.23f, 5.38f, 0.65f);
		
		Box livingroomSofa1Shape = new Box(Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX+"livingroomSofa1Shape",2.2f,0.75f,1.2f);
		Furniture livingroomSofa1 = new Furniture(Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX+"livingroomSofa1",FurnitureType.Sofa,livingroomSofa1Shape);
		livingroomSofa1.setLocationRelativeToCorner(livingroom, 1.21f, 4.12f, 0f);
		
		Box livingroomTableShape = new Box(Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX+"livingroomTableShape",1.2f,0.4f,0.58f);
		Furniture livingroomTable = new Furniture(Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX+"livingroomTable",FurnitureType.Table,livingroomTableShape);
		livingroomTable.setLocationRelativeToCorner(livingroom, 1.92f, 3.34f, 0f);
		
		Box livingroomSofa2Shape = new Box(Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX+"livingroomSofa2Shape",1.7f,0.75f,1f);
		Furniture livingroomSofa2 = new Furniture(Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX+"livingroomSofa2",FurnitureType.Sofa,livingroomSofa2Shape);
		livingroomSofa2.setLocationRelativeToCorner(livingroom, 1.63f, 1.33f, 0f);
		
		Box livingroomLockerShape = new Box(Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX+"livingroomLockerShape",2.4f,0.75f,0.42f);
		Furniture livingroomLocker = new Furniture(Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX+"livingroomLocker",FurnitureType.Locker,livingroomLockerShape);
		livingroomLocker.setLocationRelativeToCorner(livingroom, 1.09f, 0f, 0f);
		
		Box livingroomShelf2Shape = new Box(Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX+"livingroomShelf2Shape",2.4f,0.38f,0.38f);
		Furniture livingroomShelf2 = new Furniture(Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX+"livingroomShelf2",FurnitureType.Shelf,livingroomShelf2Shape);
		livingroomShelf2.setLocationRelativeToCorner(livingroom, 1.09f, 0f, 1.95f);
		
		Box livingroomTVShape = new Box(Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX+"livingroomTVShape",1.4f,0.98f,0.12f);
		Furniture livingroomTV = new Furniture(Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX+"LivingroomTV",FurnitureType.Other,livingroomTVShape);
		livingroomTV.setLocationRelativeToCorner(livingroom, 1.67f, 0.15f, 0.75f);
		
		
		//##################################################################################
		//Furniture kitchen 3.48/3.94
		Box kitchenTableShape = new Box(Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX+"kitchenTableShape",0.65f,1.1f,1.5f);
		Furniture kitchenTable = new Furniture(Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX+"kitchenTable",FurnitureType.Table,kitchenTableShape);
		kitchenTable.setLocationRelativeToCorner(kitchen, 1.85f, 1.5f, 0f);
		
		Box kitchenStoveShape = new Box(Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX+"stoveShape",0.66f,0.9f,0.61f);
		Furniture kitchenStove = new Furniture(Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX+"kitchenStove",FurnitureType.Other,kitchenStoveShape);
		kitchenStove.setLocationRelativeToCorner(kitchen, 0f, 2.28f, 0f);
		
		Box kitchenSinkShape = new Box(Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX+"kitchenSinkShape",0.66f,0.9f,0.62f);
		Furniture kitchenSink = new Furniture(Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX+"kitchenSink",FurnitureType.Other,kitchenSinkShape);
		kitchenSink.setLocationRelativeToCorner(kitchen, 0f, 1.18f, 0f);
		
		Box kitchenUnit1Shape = new Box(Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX+"kitchenKitchenunit1Shape",0.66f,0.9f,3.28f);
		Furniture kitchenUnit1 = new Furniture(Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX+"kitchenKitchenunit1",FurnitureType.Other,kitchenUnit1Shape);
		kitchenUnit1.setLocationRelativeToCorner(kitchen, 0f, 0.66f, 0f);
		
		Box kitchenUnit2Shape = new Box(Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX+"kitchenKitchenunit2Shape",1.19f,0.9f,0.61f);
		Furniture kitchenUnit2 = new Furniture(Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX+"kitchenKitchenunit2",FurnitureType.Other,kitchenUnit2Shape);
		kitchenUnit2.setLocationRelativeToCorner(kitchen, 0.66f, 3.33f, 0f);
		
		Box kitchenWindowShape = new Box(Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX+"kitchenWindowShape",1f,1f,0.01f);
		Furniture kitchenWindow = new Furniture(Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX+"kitchenWindow",FurnitureType.Other,kitchenWindowShape);
		kitchenWindow.setLocationRelativeToCorner(kitchen, 2.0f, 0f, 1.2f);
		
		
		//##################################################################################
		// creating the camera
		// TODO: the correctness of the camera data has to be checked
		String uriPrefix = Constants.uAAL_MIDDLEWARE_LOCAL_ID_PREFIX
		+ "Camera01";
	
	    // creating the coordinate system
	    // getting the device coordinates concerning the middle of the living room
	    float posX = Float.parseFloat("0.1");
	    float posY = Float.parseFloat("-2.75");
	    float posZ = Float.parseFloat("1.08");
	    OriginedMetric om = new OriginedMetric(uriPrefix + "CS", posX, posY,
		posZ, livingroom);
	
	    // setting the rotation values
	    // TODO: possibly, the rotation value has to be 340 degrees
	    // TODO: change the rotation values, because the common coordinate system has been changed
	    om.setRotateX(Float.parseFloat("-20"));
	    om.setRotateY(Float.parseFloat("0"));
	    om.setRotateZ(Float.parseFloat("0"));
	
	    // creating the position of the device
	    Point cameraPosition = new Point(uriPrefix + "Pos", 0, 0, 0, om);
	    livingroom.addContainedLocation(cameraPosition);
	}
	
	
	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.persona.middleware.service.ServiceCallee#handleCall(org.persona.
	 * middleware.service.ServiceCall)
	 */
	public ServiceResponse handleCall(ServiceCall call) {
		if (call == null)
			return null;
	
		String operation = call.getProcessURI();
		if (operation == null)
			return null;
	
		// wait till the server is fully initialized
		// TODO: reactivate when the server is integrated
//		while (theServer == null)
//			try {Thread.sleep(50);} catch (InterruptedException e) {e.printStackTrace();}
			
		if (operation.startsWith(ProvidedWorldConfigurator.SERVICE_GET_LOCATION)) {
			Object input = call
					.getInputValue(ProvidedWorldConfigurator.INPUT_LOCATION_OBJECT);
			return getLocation(input.toString());
		}
		if (operation
				.startsWith(ProvidedWorldConfigurator.SERVICE_GET_PHYSICAL_THING)) {
			Object input = call
					.getInputValue(ProvidedWorldConfigurator.INPUT_PHYSICAL_THING_OBJECT);
			return getPhysicalThing(input.toString());
		}
		if (operation.startsWith(ProvidedWorldConfigurator.SERVICE_ADD_LOCATION)) {
			Object input = call
					.getInputValue(ProvidedWorldConfigurator.INPUT_LOCATION_OBJECT);
			return addResource((Location) input);
		}
		if (operation
				.startsWith(ProvidedWorldConfigurator.SERVICE_ADD_PHYSICAL_THING)) {
			Object input = call
					.getInputValue(ProvidedWorldConfigurator.INPUT_PHYSICAL_THING_OBJECT);
			return addResource((PhysicalThing) input);
		}
		if (operation
				.startsWith(ProvidedWorldConfigurator.SERVICE_GET_THINGS_ON_WAY)) {
			Object inputWay = call
					.getInputValue(ProvidedWorldConfigurator.INPUT_WAY);
			return getByWay((Way) inputWay);
		}
		if (operation
				.startsWith(ProvidedWorldConfigurator.SERVICE_GET_WORLD)) {
			
			return getWorldServiceResponse();
		}
	
		return null;
	}
	
	private ServiceResponse getWorldServiceResponse(){
		ServiceResponse sr = new ServiceResponse(CallStatus.succeeded);
		sr.addOutput(new ProcessOutput(
				ProvidedWorldConfigurator.OUTPUT_WORLD, world));
		return sr;
	}
	
	private ServiceResponse getLocation(String locationURI) {
		try {
			Location loc = (Location) theServer.getByURI(locationURI);
			ServiceResponse sr = new ServiceResponse(CallStatus.succeeded);
			sr.addOutput(new ProcessOutput(
					ProvidedWorldConfigurator.OUTPUT_LOCATION_OBJECT, loc));
			return sr;
		} catch (Exception e) {
			LogUtils.logInfo(Activator.getLogger(), "LocationServiceProvider", "getLocation", new Object[] {
			"An error occured while delivering a getLocation service" },
			e);
			e.printStackTrace();
			return failureResponse;
		}
	}

	private ServiceResponse getPhysicalThing(String thingURI) {
		try {
			PhysicalThing pt = (PhysicalThing) theServer.getByURI(thingURI);
			
			ServiceResponse sr = new ServiceResponse(CallStatus.succeeded);
			sr.addOutput(new ProcessOutput(ProvidedWorldConfigurator.OUTPUT_PHYSICAL_THING, pt));
			return sr;
		} catch (Exception e) {
			LogUtils.logInfo(Activator.getLogger(), "LocationServiceProvider", "getPhysicalThing", new Object[] {
			"An error occured while delivering a getPhysicalThing service" },
			e);
			e.printStackTrace();
			return failureResponse;
		}
	}

	/**
	 * 
	 * @param way
	 *            - MUST have the CONTAINED_IN Property set
	 * @return
	 */
	private ServiceResponse getByWay(Way way) {
		try {
			PhysicalThing[] pts = theServer.getByWay(way);
			
			ServiceResponse sr = new ServiceResponse(CallStatus.succeeded);
	
			List things = new ArrayList();
			for (int i = 0; i < pts.length; i++)
				things.add(pts[i]);
			
			sr.addOutput(new ProcessOutput(ProvidedWorldConfigurator.OUTPUT_PHYSICAL_THING, things));
			
			return sr;
		} catch (Exception e) {
			LogUtils.logInfo(Activator.getLogger(), "LocationServiceProvider", "getByWay", new Object[] {
			"An error occured while delivering a getByWay service" },
			e);
			e.printStackTrace();
			return failureResponse;
		}
	}

	private ServiceResponse addResource(ManagedIndividual loc) {
		try {
			theServer.addResource(loc);
			ServiceResponse sr = new ServiceResponse(CallStatus.succeeded);
			return sr;
		} catch (Exception e) {
			LogUtils.logInfo(Activator.getLogger(), "LocationServiceProvider", "addResource", new Object[] {
			"An error occured while delivering an addResource service" },
			e);
			e.printStackTrace();
			return failureResponse;
		}
	}

}
