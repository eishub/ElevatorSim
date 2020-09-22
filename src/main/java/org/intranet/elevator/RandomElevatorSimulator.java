/*
 * Copyright 2003-2005 Neil McKellar and Chris Dailey
 * All rights reserved.
 */
package org.intranet.elevator;

import java.util.List;
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
	private final IntegerParameter floorsParameter;
	private final IntegerParameter carsParameter;
	private final IntegerParameter capacityParameter;
	private final IntegerParameter ridersParameter;
	private final LongParameter durationParameter;
	private final LongParameter seedParameter;
	private final ChoiceParameter controllerParameter;
	/**
	 * Currently selected controller. set only after initializeModel() was done.
	 */
	private Controller controller = null;
	private Building building;
	private final List<Controller> the_controllers; // all available controllers

	public RandomElevatorSimulator(final List<Controller> controllers) {
		this.the_controllers = controllers;
		this.floorsParameter = Simulator.Keys.FLOORS.getDefaultIntegerParameter();
		this.parameters.add(this.floorsParameter);
		this.carsParameter = Simulator.Keys.CARS.getDefaultIntegerParameter();
		this.parameters.add(this.carsParameter);
		this.capacityParameter = Simulator.Keys.CAPACITY.getDefaultIntegerParameter();
		this.parameters.add(this.capacityParameter);
		this.ridersParameter = Simulator.Keys.PEOPLE.getDefaultIntegerParameter();
		this.parameters.add(this.ridersParameter);
		this.durationParameter = Simulator.Keys.INSERTIONTIME.getDefaultLongParameter();
		this.parameters.add(this.durationParameter);
		this.seedParameter = Simulator.Keys.RANDOMSEED.getDefaultLongParameter();
		this.parameters.add(this.seedParameter);

		this.controllerParameter = new ChoiceParameter(Simulator.Keys.CONTROLLER, this.the_controllers,
				preferredController(this.the_controllers), Controller.class);
		this.parameters.add(this.controllerParameter);
	}

	@Override
	public void initializeModel() {
		final int numFloors = this.floorsParameter.getIntegerValue();
		final int numCars = this.carsParameter.getIntegerValue();
		final int carCapacity = this.capacityParameter.getIntegerValue();
		final int numRiders = this.ridersParameter.getIntegerValue();
		final long duration = this.durationParameter.getLongValue();
		final long seed = this.seedParameter.getLongValue();
		this.controller = (Controller) this.controllerParameter.getChoiceValue();

		// copy the latest settings into the global simulatorsettings.
		// that way we can recall them the next run (if not overridden by MAS)
		Simulator.simulatorprefs.putInt(Simulator.Keys.FLOORS.toString(), numFloors);
		Simulator.simulatorprefs.putInt(Simulator.Keys.CARS.toString(), numCars);
		Simulator.simulatorprefs.putInt(Simulator.Keys.CAPACITY.toString(), carCapacity);
		Simulator.simulatorprefs.putInt(Simulator.Keys.PEOPLE.toString(), numRiders);
		Simulator.simulatorprefs.putLong(Simulator.Keys.INSERTIONTIME.toString(), duration);
		Simulator.simulatorprefs.putLong(Simulator.Keys.RANDOMSEED.toString(), seed);
		Simulator.simulatorprefs.put(Simulator.Keys.CONTROLLER.toString(), this.controller.toString());

		// init the building and simulator.
		this.building = new Building(getEventQueue(), numFloors, numCars, carCapacity, this.controller);

		final Random rand = new Random(seed);

		for (int i = 0; i < numRiders; i++) {
			// starting floor
			final Floor startingFloor = this.building.getFloor(rand.nextInt(numFloors));
			final Person person = this.building.createPerson(startingFloor);
			// destination floor
			Floor floor = null;
			do {
				floor = this.building.getFloor(rand.nextInt(numFloors));
			} while (floor == startingFloor);
			final Floor destFloor = floor;
			// time to insert
			final long insertTime = rand.nextInt((int) duration);
			// insertion event for destination at time
			final Event event = new Event(insertTime) {
				@Override
				public void perform() {
					person.setDestination(destFloor);
				}
			};
			getEventQueue().addEvent(event);
		}
	}

	@Override
	public final Model getModel() {
		return this.building;
	}

	@Override
	public String getDescription() {
		return "Random Rider Insertion";
	}

	@Override
	public Simulator duplicate() {
		return new RandomElevatorSimulator(this.the_controllers);
	}

	@Override
	public Controller getCurrentController() {
		return this.controller;
	}
}
