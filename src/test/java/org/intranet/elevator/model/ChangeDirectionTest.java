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
import org.junit.Test;

/**
 * @author Neil McKellar and Chris Dailey
 *
 */
public class ChangeDirectionTest {

	/* This is more of a system test than a unit test. */
	@Test
	final public void testChangeAssignmentOnLongSimulation() {
		final long endTime = 99000;
		ClockFactory clockFactory = new ClockFactory() {
			public Clock createClock(final Clock.FeedbackListener cl) {
				return new Clock(cl) {
					public void dispose() {
					}

					public void pause() {
						setRunningState(false);
					}

					public void start() {
						if (isRunning())
							throw new IllegalStateException(
									"Can't start while already running");
						setRunningState(true);
						setSimulationTime(endTime);
					}
				};
			}
		};

		ArrayList<Controller> controllers = new ArrayList<Controller>();
		controllers.add(new SimpleController());
		final Simulator sim = new RandomElevatorSimulator(controllers);

		List params = sim.getParameters();
		assertEquals(7, params.size());
		IntegerParameter floorsParameter = (IntegerParameter) params.get(0);
		floorsParameter.setValueFromUI("10");
		IntegerParameter carsParameter = (IntegerParameter) params.get(1);
		carsParameter.setValueFromUI("3");
		IntegerParameter capacityParameter = (IntegerParameter) params.get(2);
		capacityParameter.setValueFromUI("8");
		IntegerParameter ridersParameter = (IntegerParameter) params.get(3);
		ridersParameter.setValueFromUI("40");
		LongParameter durationParameter = (LongParameter) params.get(4);
		durationParameter.setValueFromUI("100000");
		LongParameter seedParameter = (LongParameter) params.get(5);
		seedParameter.setValueFromUI("635359");

		sim.initialize(clockFactory);

		sim.getClock().start();

		Building building = (Building) sim.getModel();
		assertTrue(building.getNumFloors() == 10);
		assertTrue(building.getNumCars() == 3);
	}
}