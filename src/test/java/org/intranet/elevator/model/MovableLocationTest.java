/*
 * Copyright 2005 Neil McKellar and Chris Dailey
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
public class MovableLocationTest extends TestCase {

	private EventQueue eQ;
	private MovableLocation movableLocation;
	private boolean hasError;
	private boolean hasArrived;

	@Before
	protected void setUp() throws Exception {
		super.setUp();
		hasError = false;
		hasArrived = false;
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
		movableLocation = new MovableLocation(eQ, 0.0f, 10) {
			public float getRatePerSecond() {
				return 2.0f;
			}

			protected void arrive() {
				hasArrived = true;
			}
		};
	}

	@After
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	@Test
	public void testBasic() {
		assertFalse(hasArrived);
		assertFalse(hasError);
		assertTrue(movableLocation.getTotalDistance() == 0.0f);
		assertTrue(movableLocation.getNumTravels() == 0);

		eQ.addEvent(new Event(0) {
			public void perform() {
				movableLocation.setDestinationHeight(3.0f);
			}
		});

		eQ.processEventsUpTo(1000);
		float rememberHeight = movableLocation.getHeight();
		// prove that the loc has moved
		assertTrue(rememberHeight != 0.0f);
		assertFalse(hasArrived);
		assertFalse(hasError);
		assertTrue(movableLocation.getTotalDistance() != 0.0f);
		assertTrue(movableLocation.getNumTravels() == 0);

		eQ.processEventsUpTo(2000);
		// prove that loc has moved again
		assertTrue(movableLocation.getHeight() != rememberHeight);
		assertTrue(hasArrived);
		assertFalse(hasError);
		assertTrue(movableLocation.getTotalDistance() != 0.0f);
		assertTrue(movableLocation.getNumTravels() != 0);
	}

	@Test
	public void testMoveOppositeDirection() {
		assertFalse(hasArrived);
		assertFalse(hasError);
		assertTrue(movableLocation.getTotalDistance() == 0.0f);
		assertTrue(movableLocation.getNumTravels() == 0);

		eQ.addEvent(new Event(0) {
			public void perform() {
				movableLocation.setDestinationHeight(3.0f);
			}
		});
		eQ.addEvent(new Event(1001) {
			public void perform() {
				movableLocation.setDestinationHeight(1.0f);
			}
		});

		eQ.processEventsUpTo(1000);
		float rememberHeight = movableLocation.getHeight();
		// prove that the loc has moved
		assertTrue(rememberHeight != 0.0f);
		assertFalse(hasError);
		assertFalse(hasArrived);

		eQ.processEventsUpTo(2000);
		// assertTrue(hasError);
		assertTrue(hasArrived);
		assertEquals(1.0f, movableLocation.getHeight());
	}
}
