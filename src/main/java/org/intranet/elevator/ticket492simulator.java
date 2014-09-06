/*
 * Copyright 2003 Neil McKellar and Chris Dailey
 * All rights reserved.
 */
package org.intranet.elevator;

import java.util.ArrayList;

import org.intranet.elevator.model.operate.Building;
import org.intranet.elevator.model.operate.Person;
import org.intranet.elevator.model.operate.controller.Controller;
import org.intranet.sim.Model;
import org.intranet.sim.Simulator;
import org.intranet.sim.event.Event;
import org.intranet.ui.ChoiceParameter;
import org.intranet.ui.IntegerParameter;

/**
 * @author W.Pasman
 * 
 */
public class ticket492simulator extends Simulator {
	private IntegerParameter floorsParameter;
	private IntegerParameter carsParameter;
	private IntegerParameter capacityParameter;
	private ChoiceParameter controllerParameter;

	private Building building;

	ArrayList<Controller> the_controllers; // all pickable controllers

	/**
	 * Currently selected controller. set only after initializeModel() was done.
	 */
	private Controller controller = null;

	public ticket492simulator(ArrayList<Controller> controllers) {
		super();
		the_controllers = controllers;

		floorsParameter = Simulator.Keys.FLOORS.getDefaultIntegerParameter();
		parameters.add(floorsParameter);
		// preferred num of cars is 1 in this case...
		carsParameter = Simulator.Keys.CARS.getDefaultIntegerParameter();
		parameters.add(carsParameter);
		capacityParameter = Simulator.Keys.CAPACITY
				.getDefaultIntegerParameter();
		parameters.add(capacityParameter);

		controllerParameter = new ChoiceParameter(Simulator.Keys.CONTROLLER,
				the_controllers, preferredController(the_controllers),
				Controller.class);
		parameters.add(controllerParameter);

	}

	public void initializeModel() {
		int numFloors = floorsParameter.getIntegerValue();
		int numCars = carsParameter.getIntegerValue();
		int carCapacity = capacityParameter.getIntegerValue();
		controller = (Controller) controllerParameter.getChoiceValue();

		// copy the latest settings into the global simulatorsettings.
		// that way we can recall them the next run (if not overridden by MAS)
		Simulator.simulatorprefs.putInt(Simulator.Keys.FLOORS.toString(),
				numFloors);
		Simulator.simulatorprefs.putInt(Simulator.Keys.CARS.toString(),
				numCars);
		Simulator.simulatorprefs.putInt(Simulator.Keys.CAPACITY.toString(),
				carCapacity);
		Simulator.simulatorprefs.put(Simulator.Keys.CONTROLLER.toString(),
				controller.toString());
		// init the building and simulator.

		building = new Building(getEventQueue(), numFloors, numCars,
				carCapacity, controller);

		final Person a = building.createPerson(building.getFloor(0));
		Event eventa = new Event(0) {
			public void perform() {
				a.setDestination(building.getFloor(1));
			}
		};
		getEventQueue().addEvent(eventa);

		final Person b = building.createPerson(building.getFloor(1));
		Event eventb = new Event(18000) {
			public void perform() {
				b.setDestination(building.getFloor(2));
			}
		};
		getEventQueue().addEvent(eventb);

		final Person c = building.createPerson(building.getFloor(1));
		Event eventc = new Event(22000) {
			public void perform() {
				c.setDestination(building.getFloor(2));
			}
		};
		getEventQueue().addEvent(eventc);

	}

	public final Model getModel() {
		return building;
	}

	public String getDescription() {
		return "ticket 492 test scenario";
	}

	public Simulator duplicate() {
		return new UpToFourThenDownSimulator(the_controllers);
	}

	@Override
	public Controller getCurrentController() {
		return controller;
	}
}
