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
 * @author Neil McKellar and Chris Dailey
 * 
 */
public class ThreePersonBugSimulator extends Simulator {
	// private IntegerParameter floorsParameter;
	private IntegerParameter carsParameter;
	private ChoiceParameter controllerParameter;

	private Building building;
	ArrayList<Controller> the_controllers; // all pickable controllers

	/**
	 * Currently selected controller. set only after initializeModel() was done.
	 */
	private Controller controller = null;

	public ThreePersonBugSimulator(ArrayList<Controller> controllers) {
		super();
		the_controllers = controllers;

		carsParameter = Simulator.Keys.INSERT2NDREQAT
				.getDefaultIntegerParameter();
		parameters.add(carsParameter);
		controllerParameter = new ChoiceParameter(Simulator.Keys.CONTROLLER,
				the_controllers, preferredController(the_controllers),
				Controller.class);
		parameters.add(controllerParameter);
	}

	public void initializeModel() {
		int numCars = carsParameter.getIntegerValue();
		controller = (Controller) controllerParameter.getChoiceValue();

		// copy the latest settings into the global simulatorsettings.
		// that way we can recall them the next run (if not overridden by MAS)

		Simulator.simulatorprefs.putInt(Simulator.Keys.CARS.toString(),
				numCars);
		Simulator.simulatorprefs.put(Simulator.Keys.CONTROLLER.toString(),
				controller.toString());

		building = new Building(getEventQueue(), 6, 1, controller);

		createPerson(3, 0, 0);
		createPerson(1, 2, numCars);
		// createPerson(4, 8, 20000);
	}

	private void createPerson(int start, final int dest, long simTime) {
		final Person person = building.createPerson(building.getFloor(start));
		// insertion event for destination at time
		Event event = new Event(simTime) {
			public void perform() {
				person.setDestination(building.getFloor(dest));
			}
		};
		getEventQueue().addEvent(event);
	}

	public final Model getModel() {
		return building;
	}

	public String getDescription() {
		return "Three Person Trip Bug";
	}

	public Simulator duplicate() {
		return new ThreePersonBugSimulator(the_controllers);
	}

	@Override
	public Controller getCurrentController() {
		return controller;
	}
}
