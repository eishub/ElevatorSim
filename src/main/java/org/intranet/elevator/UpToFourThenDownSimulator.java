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
public class UpToFourThenDownSimulator extends Simulator {
	private IntegerParameter floorsParameter;
	private IntegerParameter carsParameter;
	private ChoiceParameter controllerParameter;

	private Building building;

	ArrayList<Controller> the_controllers; // all pickable controllers

	/**
	 * Currently selected controller. set only after initializeModel() was done.
	 */
	private Controller controller = null;

	public UpToFourThenDownSimulator(ArrayList<Controller> controllers) {
		super();
		the_controllers = controllers;

		// preferred value is 5 in this case, but we work different now...
		floorsParameter = Simulator.Keys.FLOORS.getDefaultIntegerParameter();
		parameters.add(floorsParameter);
		// preferred num of cars is 1 in this case...
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

		building = new Building(getEventQueue(), numFloors, numCars, controller);

		final Person a = building.createPerson(building.getFloor(3));
		Event event = new Event(0) {
			public void perform() {
				a.setDestination(building.getFloor(1));
			}
		};
		getEventQueue().addEvent(event);
	}

	public final Model getModel() {
		return building;
	}

	public String getDescription() {
		return "Elevator Travels Up To Process A Down Request";
	}

	public Simulator duplicate() {
		return new UpToFourThenDownSimulator(the_controllers);
	}

	@Override
	public Controller getCurrentController() {
		return controller;
	}
}
