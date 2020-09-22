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
 */
public class ThreePersonBugSimulator extends Simulator {
	private final IntegerParameter carsParameter;
	private final ChoiceParameter controllerParameter;
	private Building building;
	private final List<Controller> the_controllers; // all pickable controllers
	/**
	 * Currently selected controller. set only after initializeModel() was done.
	 */
	private Controller controller = null;

	public ThreePersonBugSimulator(final List<Controller> controllers) {
		super();
		this.the_controllers = controllers;

		this.carsParameter = Simulator.Keys.INSERT2NDREQAT.getDefaultIntegerParameter();
		this.parameters.add(this.carsParameter);
		this.controllerParameter = new ChoiceParameter(Simulator.Keys.CONTROLLER, this.the_controllers,
				preferredController(this.the_controllers), Controller.class);
		this.parameters.add(this.controllerParameter);
	}

	@Override
	public void initializeModel() {
		final int numCars = this.carsParameter.getIntegerValue();
		this.controller = (Controller) this.controllerParameter.getChoiceValue();

		// copy the latest settings into the global simulatorsettings.
		// that way we can recall them the next run (if not overridden by MAS)
		Simulator.simulatorprefs.putInt(Simulator.Keys.CARS.toString(), numCars);
		Simulator.simulatorprefs.put(Simulator.Keys.CONTROLLER.toString(), this.controller.toString());

		this.building = new Building(getEventQueue(), 6, 1, this.controller);

		createPerson(3, 0, 0);
		createPerson(1, 2, numCars);
	}

	private void createPerson(final int start, final int dest, final long simTime) {
		final Person person = this.building.createPerson(this.building.getFloor(start));
		// insertion event for destination at time
		final Event event = new Event(simTime) {
			@Override
			public void perform() {
				person.setDestination(ThreePersonBugSimulator.this.building.getFloor(dest));
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
		return "Three Person Trip Bug";
	}

	@Override
	public Simulator duplicate() {
		return new ThreePersonBugSimulator(this.the_controllers);
	}

	@Override
	public Controller getCurrentController() {
		return this.controller;
	}
}
