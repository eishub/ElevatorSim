/*
 * Copyright 2003 Neil McKellar and Chris Dailey
 * All rights reserved.
 */
package org.intranet.elevator.model.operate.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.intranet.elevator.model.Floor;
import org.intranet.sim.event.EventQueue;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Neil McKellar and Chris Dailey
 *
 */
public class AssignmentTest {

	private Assignment a;
	private EventQueue eQ;
	private Floor floorA;
	private Floor floorB;

	@Before
	public void setUp() throws Exception {
		eQ = new EventQueue();
		floorA = new Floor(eQ, 1, 0.0f, 10.0f);
		floorB = new Floor(eQ, 2, 11.0f, 21.0f);
		a = new Assignment(floorA, Direction.UP);
	}

	@Test
	final public void testAssignment() {
		Assignment a = new Assignment(floorA, Direction.UP);
		assertNotNull(a);
	}

	@Test
	final public void testGetDestination() {
		assertEquals(floorA, a.getDestination());
	}

	@Test
	final public void testGetDirection() {
		assertEquals(Direction.UP, a.getDirection());
	}

	/*
	 * Test for boolean equals(Object)
	 */
	@Test
	final public void testEqualsTrue() {
		Assignment b = new Assignment(floorA, Direction.UP);
		assertTrue(a.equals(b));
		assertTrue(b.equals(a));
	}

	@Test
	final public void testNullEqualsFalse() {
		assertFalse(a.equals(null));
	}

	@Test
	final public void testInstanceEqualsFalse() {
		assertFalse(a.equals("a"));
	}

	@Test
	final public void testDestinationEqualsFalse() {
		Assignment b = new Assignment(floorB, Direction.UP);
		assertFalse(a.equals(b));
	}

	@Test
	final public void testDirectionEqualsFalse() {
		Assignment b = new Assignment(floorA, Direction.DOWN);
		assertFalse(a.equals(b));
	}

}
