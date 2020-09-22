/*
 * Copyright 2003 Neil McKellar and Chris Dailey
 * All rights reserved.
 */
package org.intranet.elevator.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.intranet.elevator.RandomElevatorSimulator;
import org.intranet.elevator.model.operate.Building;
import org.intranet.elevator.model.operate.controller.Controller;
import org.intranet.elevator.model.operate.controller.SimpleController;
import org.intranet.sim.Simulator;
import org.intranet.sim.clock.Clock;
import org.intranet.sim.clock.ClockFactory;
import org.intranet.ui.IntegerParameter;
import org.intranet.ui.LongParameter;
import org.intranet.ui.Parameter;
import org.junit.Test;

/**
 * @author Neil McKellar and Chris Dailey
 */
public class ChangeDirectionTest {

	/* This is more of a system test than a unit test. */
	@Test
	final public void testChangeAssignmentOnLongSimulation() {
		final long endTime = 99000;
		final ClockFactory clockFactory = cl -> new Clock(cl) {
			@Override
			public void dispose() {
			}

			@Override
			public void pause() {
				setRunningState(false);
			}

			@Override
			public void start() {
				if (isRunning()) {
					throw new IllegalStateException("Can't start while already running");
				}
				setRunningState(true);
				setSimulationTime(endTime);
			}
		};

		final ArrayList<Controller> controllers = new ArrayList<>();
		controllers.add(new SimpleController());
		final Simulator sim = new RandomElevatorSimulator(controllers);

		final List<Parameter> params = sim.getParameters();
		assertEquals(7, params.size());
		final IntegerParameter floorsParameter = (IntegerParameter) params.get(0);
		floorsParameter.setValueFromUI("10");
		final IntegerParameter carsParameter = (IntegerParameter) params.get(1);
		carsParameter.setValueFromUI("3");
		final IntegerParameter capacityParameter = (IntegerParameter) params.get(2);
		capacityParameter.setValueFromUI("8");
		final IntegerParameter ridersParameter = (IntegerParameter) params.get(3);
		ridersParameter.setValueFromUI("40");
		final LongParameter durationParameter = (LongParameter) params.get(4);
		durationParameter.setValueFromUI("100000");
		final LongParameter seedParameter = (LongParameter) params.get(5);
		seedParameter.setValueFromUI("635359");

		sim.initialize(clockFactory);

		sim.getClock().start();

		final Building building = (Building) sim.getModel();
		assertTrue(building.getNumFloors() == 10);
		assertTrue(building.getNumCars() == 3);
	}
}