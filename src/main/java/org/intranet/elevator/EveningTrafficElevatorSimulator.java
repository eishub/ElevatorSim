/*
 * Copyright 2003 Neil McKellar and Chris Dailey
 * All rights reserved.
 */
package org.intranet.elevator;

import java.util.ArrayList;
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
 * @author W.Pasman 8nov2010 added ControllerParameter
 */
public class EveningTrafficElevatorSimulator extends Simulator {
	private IntegerParameter floorsParameter;
	private IntegerParameter carsParameter;
	private IntegerParameter ridersParameter;
	private FloatParameter durationParameter;
	private IntegerParameter stdDeviationParameter;
	private LongParameter seedParameter;
	private ChoiceParameter controllerParameter;

	private Building building;

	ArrayList<Controller> the_controllers; // all pickable controllers

	/**
	 * Currently selected controller. set only after initializeModel() was done.
	 */
	private Controller controller = null;

	public EveningTrafficElevatorSimulator(ArrayList<Controller> controllers) {
		super();
		the_controllers = controllers;

		floorsParameter = Simulator.Keys.FLOORS.getDefaultIntegerParameter();
		parameters.add(floorsParameter);
		carsParameter = Simulator.Keys.CARS.getDefaultIntegerParameter();
		parameters.add(carsParameter);
		ridersParameter = Simulator.Keys.NPEOPLEPERFLOOR
				.getDefaultIntegerParameter();
		parameters.add(ridersParameter);
		durationParameter = Simulator.Keys.INSERTIONTIMEHR
				.getDefaultFloatParameter();
		parameters.add(durationParameter);
		stdDeviationParameter = Simulator.Keys.STANDARDDEV
				.getDefaultIntegerParameter();
		parameters.add(stdDeviationParameter);
		seedParameter = Simulator.Keys.RANDOMSEED.getDefaultLongParameter();
		parameters.add(seedParameter);

		controllerParameter = new ChoiceParameter(Simulator.Keys.CONTROLLER,
				the_controllers, preferredController(the_controllers),
				Controller.class);
		parameters.add(controllerParameter);
	}

	public void initializeModel() {
		int numFloors = floorsParameter.getIntegerValue();
		int numCars = carsParameter.getIntegerValue();
		int numRiders = ridersParameter.getIntegerValue();
		float duration = durationParameter.getFloatValue();
		int stdDeviation = stdDeviationParameter.getIntegerValue();
		long seed = seedParameter.getLongValue();
		controller = (Controller) controllerParameter.getChoiceValue();

		// copy the latest settings into the global simulatorsettings.
		// that way we can recall them the next run (if not overridden by MAS)
		Simulator.simulatorprefs.putInt(Simulator.Keys.FLOORS.toString(),
				numFloors);
		Simulator.simulatorprefs.putInt(Simulator.Keys.CARS.toString(),
				numCars);
		Simulator.simulatorprefs.putInt(
				Simulator.Keys.NPEOPLEPERFLOOR.toString(), numRiders);
		Simulator.simulatorprefs.putDouble(
				Simulator.Keys.INSERTIONTIMEHR.toString(), duration);
		Simulator.simulatorprefs.putInt(Simulator.Keys.STANDARDDEV.toString(),
				stdDeviation);
		Simulator.simulatorprefs.putLong(Simulator.Keys.RANDOMSEED.toString(),
				seed);
		Simulator.simulatorprefs.put(Simulator.Keys.CONTROLLER.toString(),
				controller.toString());

		building = new Building(getEventQueue(), numFloors, numCars, controller);
		// destination floor is the ground floor
		final Floor destFloor = building.getFloor(0);

		Random rand = new Random(seed);

		for (int i = 1; i < numFloors; i++) {
			Floor startingFloor = building.getFloor(i);
			for (int j = 0; j < numRiders; j++) {
				final Person person = building.createPerson(startingFloor);
				// time to insert
				// Convert a gaussian[-1, 1] to a gaussian[0, 1]
				float gaussian = (getGaussian(rand, stdDeviation) + 1) / 2;
				// Apply gaussian value to the duration (in hours)
				// and convert to milliseconds
				long insertTime = (long) (gaussian * duration * 3600 * 1000);

				// insertion event for destination at time
				Event event = new Event(insertTime) {
					public void perform() {
						person.setDestination(destFloor);
					}
				};
				getEventQueue().addEvent(event);
			}
		}
	}

	public final Model getModel() {
		return building;
	}

	public String getDescription() {
		return "Evening Traffic Rider Insertion";
	}

	public Simulator duplicate() {
		return new EveningTrafficElevatorSimulator(the_controllers);
	}

	private static float getGaussian(Random rand, int stddev) {
		while (true) {
			float gaussian = (float) (rand.nextGaussian() / stddev);
			if (gaussian >= -1.0f && gaussian <= 1.0f)
				return gaussian;
		}
	}

	@Override
	public Controller getCurrentController() {
		return controller;
	}

}
