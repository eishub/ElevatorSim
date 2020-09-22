/*
 * Copyright 2003 Neil McKellar and Chris Dailey
 * All rights reserved.
 */
package org.intranet.elevator;

import java.util.List;

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
	private final IntegerParameter floorsParameter;
	private final IntegerParameter carsParameter;
	private final ChoiceParameter controllerParameter;
	private Building building;
	private final List<Controller> the_controllers; // all pickable controllers
	/**
	 * Currently selected controller. set only after initializeModel() was done.
	 */
	private Controller controller = null;

	public UpToFourThenDownSimulator(final List<Controller> controllers) {
		super();
		this.the_controllers = controllers;

		// preferred value is 5 in this case, but we work different now...
		this.floorsParameter = Simulator.Keys.FLOORS.getDefaultIntegerParameter();
		this.parameters.add(this.floorsParameter);
		// preferred num of cars is 1 in this case...
		this.carsParameter = Simulator.Keys.CARS.getDefaultIntegerParameter();
		this.parameters.add(this.carsParameter);
		this.controllerParameter = new ChoiceParameter(Simulator.Keys.CONTROLLER, this.the_controllers,
				preferredController(this.the_controllers), Controller.class);
		this.parameters.add(this.controllerParameter);

	}

	@Override
	public void initializeModel() {
		final int numFloors = this.floorsParameter.getIntegerValue();
		final int numCars = this.carsParameter.getIntegerValue();
		this.controller = (Controller) this.controllerParameter.getChoiceValue();

		// copy the latest settings into the global simulatorsettings.
		// that way we can recall them the next run (if not overridden by MAS)
		Simulator.simulatorprefs.putInt(Simulator.Keys.FLOORS.toString(), numFloors);
		Simulator.simulatorprefs.putInt(Simulator.Keys.CARS.toString(), numCars);
		Simulator.simulatorprefs.put(Simulator.Keys.CONTROLLER.toString(), this.controller.toString());

		this.building = new Building(getEventQueue(), numFloors, numCars, this.controller);

		final Person a = this.building.createPerson(this.building.getFloor(3));
		final Event event = new Event(0) {
			@Override
			public void perform() {
				a.setDestination(UpToFourThenDownSimulator.this.building.getFloor(1));
			}
		};
		getEventQueue().addEvent(event);
	}

	@Override
	public final Model getModel() {
		return this.building;
	}

	@Override
	public String getDescription() {
		return "Elevator Travels Up To Process A Down Request";
	}

	@Override
	public Simulator duplicate() {
		return new UpToFourThenDownSimulator(this.the_controllers);
	}

	@Override
	public Controller getCurrentController() {
		return this.controller;
	}
}
