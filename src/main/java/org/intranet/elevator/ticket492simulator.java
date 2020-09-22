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
 * @author W.Pasman
 */
public class ticket492simulator extends Simulator {
	private final IntegerParameter floorsParameter;
	private final IntegerParameter carsParameter;
	private final IntegerParameter capacityParameter;
	private final ChoiceParameter controllerParameter;
	private Building building;
	private final List<Controller> the_controllers; // all pickable controllers

	/**
	 * Currently selected controller. set only after initializeModel() was done.
	 */
	private Controller controller = null;

	public ticket492simulator(final List<Controller> controllers) {
		super();
		this.the_controllers = controllers;

		this.floorsParameter = Simulator.Keys.FLOORS.getDefaultIntegerParameter();
		this.parameters.add(this.floorsParameter);
		// preferred num of cars is 1 in this case...
		this.carsParameter = Simulator.Keys.CARS.getDefaultIntegerParameter();
		this.parameters.add(this.carsParameter);
		this.capacityParameter = Simulator.Keys.CAPACITY.getDefaultIntegerParameter();
		this.parameters.add(this.capacityParameter);

		this.controllerParameter = new ChoiceParameter(Simulator.Keys.CONTROLLER, this.the_controllers,
				preferredController(this.the_controllers), Controller.class);
		this.parameters.add(this.controllerParameter);

	}

	@Override
	public void initializeModel() {
		final int numFloors = this.floorsParameter.getIntegerValue();
		final int numCars = this.carsParameter.getIntegerValue();
		final int carCapacity = this.capacityParameter.getIntegerValue();
		this.controller = (Controller) this.controllerParameter.getChoiceValue();

		// copy the latest settings into the global simulatorsettings.
		// that way we can recall them the next run (if not overridden by MAS)
		Simulator.simulatorprefs.putInt(Simulator.Keys.FLOORS.toString(), numFloors);
		Simulator.simulatorprefs.putInt(Simulator.Keys.CARS.toString(), numCars);
		Simulator.simulatorprefs.putInt(Simulator.Keys.CAPACITY.toString(), carCapacity);
		Simulator.simulatorprefs.put(Simulator.Keys.CONTROLLER.toString(), this.controller.toString());
		// init the building and simulator.

		this.building = new Building(getEventQueue(), numFloors, numCars, carCapacity, this.controller);

		final Person a = this.building.createPerson(this.building.getFloor(0));
		final Event eventa = new Event(0) {
			@Override
			public void perform() {
				a.setDestination(ticket492simulator.this.building.getFloor(1));
			}
		};
		getEventQueue().addEvent(eventa);

		final Person b = this.building.createPerson(this.building.getFloor(1));
		final Event eventb = new Event(18000) {
			@Override
			public void perform() {
				b.setDestination(ticket492simulator.this.building.getFloor(2));
			}
		};
		getEventQueue().addEvent(eventb);

		final Person c = this.building.createPerson(this.building.getFloor(1));
		final Event eventc = new Event(22000) {
			@Override
			public void perform() {
				c.setDestination(ticket492simulator.this.building.getFloor(2));
			}
		};
		getEventQueue().addEvent(eventc);

	}

	@Override
	public final Model getModel() {
		return this.building;
	}

	@Override
	public String getDescription() {
		return "ticket 492 test scenario";
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
