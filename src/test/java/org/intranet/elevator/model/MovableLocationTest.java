/*
 * Copyright 2005 Neil McKellar and Chris Dailey
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
public class MovableLocationTest extends TestCase {
	private EventQueue eQ;
	private MovableLocation movableLocation;
	private boolean hasError;
	private boolean hasArrived;

	@Override
	@Before
	protected void setUp() throws Exception {
		super.setUp();
		this.hasError = false;
		this.hasArrived = false;
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
				MovableLocationTest.this.hasError = true;
			}
		});
		this.movableLocation = new MovableLocation(this.eQ, 0.0f, 10) {
			@Override
			public float getRatePerSecond() {
				return 2.0f;
			}

			@Override
			protected void arrive() {
				MovableLocationTest.this.hasArrived = true;
			}
		};
	}

	@Override
	@After
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	@Test
	public void testBasic() {
		assertFalse(this.hasArrived);
		assertFalse(this.hasError);
		assertTrue(this.movableLocation.getTotalDistance() == 0.0f);
		assertTrue(this.movableLocation.getNumTravels() == 0);

		this.eQ.addEvent(new Event(0) {
			@Override
			public void perform() {
				MovableLocationTest.this.movableLocation.setDestinationHeight(3.0f);
			}
		});

		this.eQ.processEventsUpTo(1000);
		final float rememberHeight = this.movableLocation.getHeight();
		// prove that the loc has moved
		assertTrue(rememberHeight != 0.0f);
		assertFalse(this.hasArrived);
		assertFalse(this.hasError);
		assertTrue(this.movableLocation.getTotalDistance() != 0.0f);
		assertTrue(this.movableLocation.getNumTravels() == 0);

		this.eQ.processEventsUpTo(2000);
		// prove that loc has moved again
		assertTrue(this.movableLocation.getHeight() != rememberHeight);
		assertTrue(this.hasArrived);
		assertFalse(this.hasError);
		assertTrue(this.movableLocation.getTotalDistance() != 0.0f);
		assertTrue(this.movableLocation.getNumTravels() != 0);
	}

	@Test
	public void testMoveOppositeDirection() {
		assertFalse(this.hasArrived);
		assertFalse(this.hasError);
		assertTrue(this.movableLocation.getTotalDistance() == 0.0f);
		assertTrue(this.movableLocation.getNumTravels() == 0);

		this.eQ.addEvent(new Event(0) {
			@Override
			public void perform() {
				MovableLocationTest.this.movableLocation.setDestinationHeight(3.0f);
			}
		});
		this.eQ.addEvent(new Event(1001) {
			@Override
			public void perform() {
				MovableLocationTest.this.movableLocation.setDestinationHeight(1.0f);
			}
		});

		this.eQ.processEventsUpTo(1000);
		final float rememberHeight = this.movableLocation.getHeight();
		// prove that the loc has moved
		assertTrue(rememberHeight != 0.0f);
		assertFalse(this.hasError);
		assertFalse(this.hasArrived);

		this.eQ.processEventsUpTo(2000);
		// assertTrue(hasError);
		assertTrue(this.hasArrived);
		assertEquals(1.0f, this.movableLocation.getHeight());
	}
}
