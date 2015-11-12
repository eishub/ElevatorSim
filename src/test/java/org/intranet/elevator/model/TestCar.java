/*
 * Copyright 2004 Neil McKellar and Chris Dailey
 * All rights reserved.
 */
package org.intranet.elevator.model;

import junit.framework.TestCase;

import org.intranet.sim.event.Event;
import org.intranet.sim.event.EventQueue;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Neil McKellar and Chris Dailey
 * 
 */
public class TestCar extends TestCase {
	private EventQueue eQ;
	private Car car;
	private Floor floorTwo;
	private Floor floorThree;
	private boolean hasError;

	@Before
	protected void setUp() throws Exception {
		super.setUp();
		hasError = false;
		eQ = new EventQueue();
		eQ.addListener(new EventQueue.Listener() {
			public void eventAdded(Event e) {
			}

			public void eventRemoved(Event e) {
			}

			public void eventError(Exception ex) {
				hasError = true;
			}
		});
		car = new Car(eQ, "testCar", 0.0f, 10);
		floorTwo = new Floor(eQ, 2, 2.0f, 1.0f);
		floorThree = new Floor(eQ, 3, 3.0f, 1.0f);
		car.getFloorRequestPanel().addServicedFloor(floorTwo);
		car.getFloorRequestPanel().addServicedFloor(floorThree);
	}

	@After
	protected void tearDown() throws Exception {
		eQ = null;
		car = null;
		floorTwo = null;
		floorThree = null;
		super.tearDown();
	}

	@Test
	public final void testDockedToIdle() {
		idleToDocked();

		car.undock();
		assertCar(2.0f, null, null);
	}

	@Test
	public final void testDockedToTravelling() {
		idleToDocked();

		setDestination(floorThree, 1201);
		undock(1202);
		eQ.processEventsUpTo(1203);
		assertCar(2.002f, null, floorThree);

		eQ.processEventsUpTo(1800);
		assertCar(3.0f, floorThree, null);
	}

	@Test
	public final void testBadDirectionChange() {
		idleToDocked();

		setDestination(floorThree, 1201);
		undock(1202);
		setDestination(floorTwo, 1300);
		assertFalse(hasError);
		eQ.processEventsUpTo(1400);
		// assertTrue(hasError);
		// It's now allowed to change direction while traveling.
	}

	@Test
	public final void testDockedToDocked() {
		idleToDocked();

		setDestination(floorTwo, 1201);
		eQ.processEventsUpTo(1203);
		assertCar(2.0f, floorTwo, floorTwo);
		undock(1205);
		eQ.processEventsUpTo(1206);
		assertCar(2.0f, floorTwo, null);
	}

	private void assertCar(float height, Floor location, Floor destination) {
		assertNotNull(car);
		assertEquals(height, car.getHeight(), 0.001);
		assertEquals(location, car.getLocation());
		assertEquals(destination, car.getDestination());
	}

	private void setDestination(final Floor destination, long time) {
		eQ.addEvent(new Event(time) {
			public void perform() {
				car.setDestination(destination);
			}
		});
	}

	private void undock(long time) {
		eQ.addEvent(new Event(time) {
			public void perform() {
				car.undock();
			}
		});
	}

	private void idleToDocked() {
		assertCar(0.0f, null, null);

		setDestination(floorTwo, 0);
		eQ.processEventsUpTo(1);
		assertCar(0.002f, null, floorTwo);

		eQ.processEventsUpTo(1200);
		assertCar(2.0f, floorTwo, null);
	}
}