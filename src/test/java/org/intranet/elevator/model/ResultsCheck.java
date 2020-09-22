/*
 * Copyright 2004 Neil McKellar and Chris Dailey
 * All rights reserved.
 */
package org.intranet.elevator.model;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;

import org.intranet.elevator.RandomElevatorSimulator;
import org.intranet.elevator.model.operate.controller.Controller;
import org.intranet.elevator.model.operate.controller.SimpleController;
import org.intranet.sim.clock.RealTimeClock;
import org.junit.Test;

/**
 * @author Neil McKellar and Chris Dailey
 */
public class ResultsCheck {
	/**
	 * Smoke test of sim. Runs for 10^6 seconds simulated time.
	 */
	@Test
	public final void testRandomElevatorSimulator() {
		final ArrayList<Controller> controllers = new ArrayList<>();
		controllers.add(new SimpleController());
		final RandomElevatorSimulator res = new RandomElevatorSimulator(controllers);

		res.getParameter("Number of floors").setValueFromUI("3");
		res.getParameter("Number of Cars").setValueFromUI("1");
		res.getParameter("Number of People").setValueFromUI("11");
		res.getParameter("Rider insertion time (ms)").setValueFromUI("500");
		res.initialize(new RealTimeClock.RealTimeClockFactory());
		// accelerate real time by a factor 2^20 ~ 10^6
		((RealTimeClock) res.getClock()).setTimeConversion(20);
		res.getClock().start();
		// limit to 10^6 sec simulation time. The sim does not stop anymore so
		// we have to set a time deadline.
		while (res.getClock().isRunning() && res.getClock().getSimulationTime() < 1000000000L) {
			try {
				Thread.sleep(100);
			} catch (final InterruptedException e) {
				e.printStackTrace();
			}
		}
		assertEquals(0, res.getEventQueue().getEventList().size());
	}
}