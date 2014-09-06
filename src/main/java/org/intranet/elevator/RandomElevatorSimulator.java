/*
 * Copyright 2003-2005 Neil McKellar and Chris Dailey
 * All rights reserved.
 */
package org.intranet.elevator;

import java.util.ArrayList;
import java.util.Random;

import org.intranet.elevator.model.Floor;
import org.intranet.elevator.model.operate.Building;
import org.intranet.elevator.model.operate.Person;
import org.intranet.elevator.model.operate.controller.Controller;
import org.intranet.sim.Model;
import org.intranet.sim.Simulator;
import org.intranet.sim.event.Event;
import org.intranet.ui.ChoiceParameter;
import org.intranet.ui.IntegerParameter;
import org.intranet.ui.LongParameter;

/**
 * @author Neil McKellar and Chris Dailey
 * @author W.Pasman modifications to control parameters externally by
 *         Preferences
 */
public class RandomElevatorSimulator extends Simulator {
	private IntegerParameter floorsParameter;
	private IntegerParameter carsParameter;
	private IntegerParameter capacityParameter;
	private IntegerParameter ridersParameter;
	private LongParameter durationParameter;
	private LongParameter seedParameter;
	private ChoiceParameter controllerParameter;

	/**
	 * Currently selected controller. set only after initializeModel() was done.
	 */
	private Controller controller = null;

	private Building building;
	ArrayList<Controller> the_controllers; // all available controllers

	public RandomElevatorSimulator(ArrayList<Controller> controllers) {
		the_controllers = controllers;
		floorsParameter = Simulator.Keys.FLOORS.getDefaultIntegerParameter();
		parameters.add(floorsParameter);
		carsParameter = Simulator.Keys.CARS.getDefaultIntegerParameter();
		parameters.add(carsParameter);
		capacityParameter = Simulator.Keys.CAPACITY
				.getDefaultIntegerParameter();
		parameters.add(capacityParameter);
		ridersParameter = Simulator.Keys.PEOPLE.getDefaultIntegerParameter();
		parameters.add(ridersParameter);
		durationParameter = Simulator.Keys.INSERTIONTIME.getDefaultLongParameter();
		parameters.add(durationParameter);
		seedParameter = Simulator.Keys.RANDOMSEED.getDefaultLongParameter();
		parameters.add(seedParameter);

		controllerParameter = new ChoiceParameter(Simulator.Keys.CONTROLLER,
				the_controllers, preferredController(the_controllers),
				Controller.class);
		parameters.add(controllerParameter);
	}

	public void initializeModel() {
		int numFloors = floorsParameter.getIntegerValue();
		int numCars = carsParameter.getIntegerValue();
		int carCapacity = capacityParameter.getIntegerValue();
		int numRiders = ridersParameter.getIntegerValue();
		long duration = durationParameter.getLongValue();
		long seed = seedParameter.getLongValue();
		controller = (Controller) controllerParameter.getChoiceValue();

		// copy the latest settings into the global simulatorsettings.
		// that way we can recall them the next run (if not overridden by MAS)
		Simulator.simulatorprefs.putInt(Simulator.Keys.FLOORS.toString(),
				numFloors);
		Simulator.simulatorprefs.putInt(Simulator.Keys.CARS.toString(),
				numCars);
		Simulator.simulatorprefs.putInt(Simulator.Keys.CAPACITY.toString(),
				carCapacity);
		Simulator.simulatorprefs.putInt(Simulator.Keys.PEOPLE.toString(),
				numRiders);
		Simulator.simulatorprefs.putLong(Simulator.Keys.INSERTIONTIME.toString(),
				duration);
		Simulator.simulatorprefs.putLong(Simulator.Keys.RANDOMSEED.toString(),
				seed);
		Simulator.simulatorprefs.put(Simulator.Keys.CONTROLLER.toString(),
				controller.toString());

		// init the building and simulator.
		building = new Building(getEventQueue(), numFloors, numCars,
				carCapacity, controller);

		Random rand = new Random(seed);

		for (int i = 0; i < numRiders; i++) {
			// starting floor
			Floor startingFloor = building.getFloor(rand.nextInt(numFloors));
			final Person person = building.createPerson(startingFloor);
			// destination floor
			Floor floor = null;
			do {
				floor = building.getFloor(rand.nextInt(numFloors));
			} while (floor == startingFloor);
			final Floor destFloor = floor;
			// time to insert
			long insertTime = rand.nextInt((int) duration);
			// insertion event for destination at time
			Event event = new Event(insertTime) {
				public void perform() {
					person.setDestination(destFloor);
				}
			};
			getEventQueue().addEvent(event);
		}
	}

	public final Model getModel() {
		return building;
	}

	public String getDescription() {
		return "Random Rider Insertion";
	}

	public Simulator duplicate() {
		return new RandomElevatorSimulator(the_controllers);
	}

	@Override
	public Controller getCurrentController() {
		return controller;
	}
}
