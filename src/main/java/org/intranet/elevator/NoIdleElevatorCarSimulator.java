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
public class NoIdleElevatorCarSimulator extends Simulator {
	private IntegerParameter downDestParameter;
	private IntegerParameter upDestParameter;
	private IntegerParameter floorsParameter;
	private IntegerParameter carsParameter;
	private ChoiceParameter controllerParameter;

	ArrayList<Controller> the_controllers; // all pickable controllers
	/**
	 * Currently selected controller. set only after initializeModel() was done.
	 */
	private Controller controller = null;

	private Building building;

	public NoIdleElevatorCarSimulator(ArrayList<Controller> controllers) {
		super();
		the_controllers = controllers;
		floorsParameter = Simulator.Keys.FLOORS.getDefaultIntegerParameter();
		parameters.add(floorsParameter);
		// in this case, default would have been better at 1
		carsParameter = Simulator.Keys.CARS.getDefaultIntegerParameter();
		parameters.add(carsParameter);
		upDestParameter = Simulator.Keys.UPDESTINATION
				.getDefaultIntegerParameter();
		parameters.add(upDestParameter);
		downDestParameter = Simulator.Keys.DOWNDESTINATION
				.getDefaultIntegerParameter();
		parameters.add(downDestParameter);
		controllerParameter = new ChoiceParameter(Simulator.Keys.CONTROLLER,
				the_controllers, preferredController(the_controllers),
				Controller.class);
		parameters.add(controllerParameter);
	}

	public void initializeModel() {
		int numFloors = floorsParameter.getIntegerValue();
		int numCars = carsParameter.getIntegerValue();
		final int upDest = upDestParameter.getIntegerValue() - 1;
		final int downDest = downDestParameter.getIntegerValue() - 1;
		controller = (Controller) controllerParameter.getChoiceValue();

		// copy the latest settings into the global simulatorsettings.
		// that way we can recall them the next run (if not overridden by MAS)
		Simulator.simulatorprefs.putInt(Simulator.Keys.FLOORS.toString(),
				numFloors);
		Simulator.simulatorprefs.putInt(Simulator.Keys.CARS.toString(),
				numCars);
		Simulator.simulatorprefs.putInt(
				Simulator.Keys.UPDESTINATION.toString(), upDest);
		Simulator.simulatorprefs.putDouble(
				Simulator.Keys.DOWNDESTINATION.toString(), downDest);
		Simulator.simulatorprefs.put(Simulator.Keys.CONTROLLER.toString(),
				controller.toString());

		building = new Building(getEventQueue(), numFloors, numCars, controller);
		final Person a = building.createPerson(building.getFloor(1));
		Event eventA = new Event(0) {
			public void perform() {
				a.setDestination(building.getFloor(upDest));
			}
		};
		getEventQueue().addEvent(eventA);

		final Person c = building.createPerson(building.getFloor(3));
		Event eventC = new Event(0) {
			public void perform() {
				c.setDestination(building.getFloor(downDest));
			}
		};
		getEventQueue().addEvent(eventC);
	}

	public final Model getModel() {
		return building;
	}

	public String getDescription() {
		return "People Going Different Directions, Only One Car";
	}

	public Simulator duplicate() {
		return new NoIdleElevatorCarSimulator(the_controllers);
	}

	@Override
	public Controller getCurrentController() {
		return controller;
	}
}
