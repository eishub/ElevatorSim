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
import org.intranet.sim.event.EventQueue;
import org.intranet.ui.ChoiceParameter;
import org.intranet.ui.IntegerParameter;

/**
 * @author Neil McKellar and Chris Dailey
 * 
 */
public class ThreePersonTwoElevatorSimulator extends Simulator {
	private IntegerParameter floorsParameter;
	private IntegerParameter carsParameter;
	private ChoiceParameter controllerParameter;

	private Building building;

	ArrayList<Controller> the_controllers; // all pickable controllers

	/**
	 * Currently selected controller. set only after initializeModel() was done.
	 */
	private Controller controller = null;

	public ThreePersonTwoElevatorSimulator(ArrayList<Controller> controllers) {
		super();
		the_controllers = controllers;

		floorsParameter = Simulator.Keys.FLOORS.getDefaultIntegerParameter();
		parameters.add(floorsParameter);
		carsParameter = Simulator.Keys.CARS.getDefaultIntegerParameter();
		parameters.add(carsParameter);
		controllerParameter = new ChoiceParameter(Simulator.Keys.CONTROLLER,
				the_controllers, preferredController(the_controllers),
				Controller.class);
		parameters.add(controllerParameter);
	}

	public void initializeModel() {
		int numFloors = floorsParameter.getIntegerValue();
		int numCars = carsParameter.getIntegerValue();
		controller = (Controller) controllerParameter.getChoiceValue();

		// copy the latest settings into the global simulatorsettings.
		// that way we can recall them the next run (if not overridden by MAS)
		Simulator.simulatorprefs.putInt(Simulator.Keys.FLOORS.toString(),
				numFloors);
		Simulator.simulatorprefs.putInt(Simulator.Keys.CARS.toString(),
				numCars);
		Simulator.simulatorprefs.put(Simulator.Keys.CONTROLLER.toString(),
				controller.toString());

		EventQueue eQ = getEventQueue();

		building = new Building(getEventQueue(), numFloors, numCars, controller);

		final Person c = building.createPerson(building.getFloor(3));
		Event eventC = new Event(0) {
			public void perform() {
				c.setDestination(building.getFloor(1));
			}
		};
		eQ.addEvent(eventC);

		final Person a = building.createPerson(building.getFloor(1));
		Event eventA = new Event(0) {
			public void perform() {
				a.setDestination(building.getFloor(3));
			}
		};
		eQ.addEvent(eventA);

		final Person b = building.createPerson(building.getFloor(2));
		Event eventB = new Event(0) {
			public void perform() {
				b.setDestination(building.getFloor(4));
			}
		};
		eQ.addEvent(eventB);
	}

	public final Model getModel() {
		return building;
	}

	public String getDescription() {
		return "Three Person Two Elevator";
	}

	public Simulator duplicate() {
		return new ThreePersonTwoElevatorSimulator(the_controllers);
	}

	@Override
	public Controller getCurrentController() {
		return controller;
	}
}
