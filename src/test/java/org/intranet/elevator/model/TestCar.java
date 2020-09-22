/*
 * Copyright 2004 Neil McKellar and Chris Dailey
 * All rights reserved.
 */
package org.intranet.elevator.model;

import org.intranet.sim.event.Event;
import org.intranet.sim.event.EventQueue;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import junit.framework.TestCase;

/**
 * @author Neil McKellar and Chris Dailey
 */
public class TestCar extends TestCase {
	private EventQueue eQ;
	private Car car;
	private Floor floorTwo;
	private Floor floorThree;
	private boolean hasError;

	@Override
	@Before
	protected void setUp() throws Exception {
		super.setUp();
		this.hasError = false;
		this.eQ = new EventQueue();
		this.eQ.addListener(new EventQueue.Listener() {
			@Override
			public void eventAdded(final Event e) {
			}

			@Override
			public void eventRemoved(final Event e) {
			}

			@Override
			public void eventError(final Exception ex) {
				TestCar.this.hasError = true;
			}
		});
		this.car = new Car(this.eQ, "testCar", 0.0f, 10);
		this.floorTwo = new Floor(this.eQ, 2, 2.0f, 1.0f);
		this.floorThree = new Floor(this.eQ, 3, 3.0f, 1.0f);
		this.car.getFloorRequestPanel().addServicedFloor(this.floorTwo);
		this.car.getFloorRequestPanel().addServicedFloor(this.floorThree);
	}

	@Override
	@After
	protected void tearDown() throws Exception {
		this.eQ = null;
		this.car = null;
		this.floorTwo = null;
		this.floorThree = null;
		super.tearDown();
	}

	@Test
	public final void testDockedToIdle() {
		idleToDocked();

		this.car.undock();
		assertCar(2.0f, null, null);
	}

	@Test
	public final void testDockedToTravelling() {
		idleToDocked();

		setDestination(this.floorThree, 1201);
		undock(1202);
		this.eQ.processEventsUpTo(1203);
		assertCar(2.002f, null, this.floorThree);

		this.eQ.processEventsUpTo(1800);
		assertCar(3.0f, this.floorThree, null);
	}

	@Test
	public final void testBadDirectionChange() {
		idleToDocked();

		setDestination(this.floorThree, 1201);
		undock(1202);
		setDestination(this.floorTwo, 1300);
		assertFalse(this.hasError);
		this.eQ.processEventsUpTo(1400);
		// assertTrue(hasError);
		// It's now allowed to change direction while traveling.
	}

	@Test
	public final void testDockedToDocked() {
		idleToDocked();

		setDestination(this.floorTwo, 1201);
		this.eQ.processEventsUpTo(1203);
		assertCar(2.0f, this.floorTwo, this.floorTwo);
		undock(1205);
		this.eQ.processEventsUpTo(1206);
		assertCar(2.0f, this.floorTwo, null);
	}

	private void assertCar(final float height, final Floor location, final Floor destination) {
		assertNotNull(this.car);
		assertEquals(height, this.car.getHeight(), 0.001);
		assertEquals(location, this.car.getLocation());
		assertEquals(destination, this.car.getDestination());
	}

	private void setDestination(final Floor destination, final long time) {
		this.eQ.addEvent(new Event(time) {
			@Override
			public void perform() {
				TestCar.this.car.setDestination(destination);
			}
		});
	}

	private void undock(final long time) {
		this.eQ.addEvent(new Event(time) {
			@Override
			public void perform() {
				TestCar.this.car.undock();
			}
		});
	}

	private void idleToDocked() {
		assertCar(0.0f, null, null);

		setDestination(this.floorTwo, 0);
		this.eQ.processEventsUpTo(1);
		assertCar(0.002f, null, this.floorTwo);

		this.eQ.processEventsUpTo(1200);
		assertCar(2.0f, this.floorTwo, null);
	}
}