/*
 * Copyright 2003 Neil McKellar and Chris Dailey
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
import org.intranet.ui.FloatParameter;
import org.intranet.ui.IntegerParameter;
import org.intranet.ui.LongParameter;

/**
 * @author Neil McKellar and Chris Dailey
 * @author W.Pasman modifications to control parameters externally by
 *         Preferences
 */
public class MorningTrafficElevatorSimulator extends Simulator {
	private final IntegerParameter floorsParameter;
	private final IntegerParameter carsParameter;
	private final IntegerParameter ridersParameter;
	private final FloatParameter durationParameter;
	private final IntegerParameter stdDeviationParameter;
	private final LongParameter seedParameter;
	private final ChoiceParameter controllerParameter;
	private Building building;
	private final List<Controller> the_controllers; // all pickable controllers
	/**
	 * Currently selected controller. set only after initializeModel() was done.
	 */
	private Controller controller = null;

	public MorningTrafficElevatorSimulator(final List<Controller> controllers) {
		super();
		this.the_controllers = controllers;
		this.floorsParameter = Simulator.Keys.FLOORS.getDefaultIntegerParameter();
		this.parameters.add(this.floorsParameter);
		this.carsParameter = Simulator.Keys.CARS.getDefaultIntegerParameter();
		this.parameters.add(this.carsParameter);
		this.ridersParameter = Simulator.Keys.NPEOPLEPERFLOOR.getDefaultIntegerParameter();
		this.parameters.add(this.ridersParameter);
		this.durationParameter = Simulator.Keys.INSERTIONTIMEHR.getDefaultFloatParameter();
		this.parameters.add(this.durationParameter);
		this.stdDeviationParameter = Simulator.Keys.STANDARDDEV.getDefaultIntegerParameter();
		this.parameters.add(this.stdDeviationParameter);
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
		final int numRiders = this.ridersParameter.getIntegerValue();
		final float duration = this.durationParameter.getFloatValue();
		final double durationInMs = duration * 3600.0 * 1000.0;
		final int stdDeviation = this.stdDeviationParameter.getIntegerValue();
		final long seed = this.seedParameter.getLongValue();
		this.controller = (Controller) this.controllerParameter.getChoiceValue();

		// copy the latest settings into the global simulatorsettings.
		// that way we can recall them the next run (if not overridden by MAS)
		Simulator.simulatorprefs.putInt(Simulator.Keys.FLOORS.toString(), numFloors);
		Simulator.simulatorprefs.putInt(Simulator.Keys.CARS.toString(), numCars);
		Simulator.simulatorprefs.putInt(Simulator.Keys.NPEOPLEPERFLOOR.toString(), numRiders);
		Simulator.simulatorprefs.putDouble(Simulator.Keys.INSERTIONTIMEHR.toString(), duration);
		Simulator.simulatorprefs.putInt(Simulator.Keys.STANDARDDEV.toString(), stdDeviation);
		Simulator.simulatorprefs.putLong(Simulator.Keys.RANDOMSEED.toString(), seed);
		Simulator.simulatorprefs.put(Simulator.Keys.CONTROLLER.toString(), this.controller.toString());

		this.building = new Building(getEventQueue(), numFloors, numCars, this.controller);
		// starting floor is the ground floor
		final Floor startingFloor = this.building.getFloor(0);

		final Random rand = new Random(seed);

		for (int i = 1; i < numFloors; i++) {
			final Floor destFloor = this.building.getFloor(i);
			for (int j = 0; j < numRiders; j++) {
				final Person person = this.building.createPerson(startingFloor);
				// time to insert
				// Convert a gaussian[-1, 1] to a gaussian[0, 1]
				final double gaussian = (getGaussian(rand, stdDeviation) + 1) / 2;
				// Apply gaussian value to the duration (in hours)
				// and convert to milliseconds
				final long insertTime = (long) (gaussian * durationInMs);
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
	}

	@Override
	public final Model getModel() {
		return this.building;
	}

	@Override
	public String getDescription() {
		return "Morning Traffic Rider Insertion";
	}

	@Override
	public Simulator duplicate() {
		return new MorningTrafficElevatorSimulator(this.the_controllers);
	}

	private static double getGaussian(final Random rand, final int stddev) {
		while (true) {
			final double gaussian = rand.nextGaussian() / stddev;
			if (gaussian >= -1.0 && gaussian <= 1.0) {
				return gaussian;
			}
		}
	}

	@Override
	public Controller getCurrentController() {
		return this.controller;
	}
}
