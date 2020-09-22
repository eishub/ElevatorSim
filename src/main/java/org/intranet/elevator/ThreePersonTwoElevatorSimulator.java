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
import org.intranet.sim.event.EventQueue;
import org.intranet.ui.ChoiceParameter;
import org.intranet.ui.IntegerParameter;

/**
 * @author Neil McKellar and Chris Dailey
 *
 */
public class ThreePersonTwoElevatorSimulator extends Simulator {
	private final IntegerParameter floorsParameter;
	private final IntegerParameter carsParameter;
	private final ChoiceParameter controllerParameter;
	private Building building;
	private final List<Controller> the_controllers; // all pickable controllers
	/**
	 * Currently selected controller. set only after initializeModel() was done.
	 */
	private Controller controller = null;

	public ThreePersonTwoElevatorSimulator(final List<Controller> controllers) {
		super();
		this.the_controllers = controllers;

		this.floorsParameter = Simulator.Keys.FLOORS.getDefaultIntegerParameter();
		this.parameters.add(this.floorsParameter);
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

		final EventQueue eQ = getEventQueue();

		this.building = new Building(getEventQueue(), numFloors, numCars, this.controller);

		final Person c = this.building.createPerson(this.building.getFloor(3));
		final Event eventC = new Event(0) {
			@Override
			public void perform() {
				c.setDestination(ThreePersonTwoElevatorSimulator.this.building.getFloor(1));
			}
		};
		eQ.addEvent(eventC);

		final Person a = this.building.createPerson(this.building.getFloor(1));
		final Event eventA = new Event(0) {
			@Override
			public void perform() {
				a.setDestination(ThreePersonTwoElevatorSimulator.this.building.getFloor(3));
			}
		};
		eQ.addEvent(eventA);

		final Person b = this.building.createPerson(this.building.getFloor(2));
		final Event eventB = new Event(0) {
			@Override
			public void perform() {
				b.setDestination(ThreePersonTwoElevatorSimulator.this.building.getFloor(4));
			}
		};
		eQ.addEvent(eventB);
	}

	@Override
	public final Model getModel() {
		return this.building;
	}

	@Override
	public String getDescription() {
		return "Three Person Two Elevator";
	}

	@Override
	public Simulator duplicate() {
		return new ThreePersonTwoElevatorSimulator(this.the_controllers);
	}

	@Override
	public Controller getCurrentController() {
		return this.controller;
	}
}
