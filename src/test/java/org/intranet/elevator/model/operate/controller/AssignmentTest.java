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
 */
public class AssignmentTest {
	private Assignment a;
	private EventQueue eQ;
	private Floor floorA;
	private Floor floorB;

	@Before
	public void setUp() throws Exception {
		this.eQ = new EventQueue();
		this.floorA = new Floor(this.eQ, 1, 0.0f, 10.0f);
		this.floorB = new Floor(this.eQ, 2, 11.0f, 21.0f);
		this.a = new Assignment(this.floorA, Direction.UP);
	}

	@Test
	final public void testAssignment() {
		final Assignment a = new Assignment(this.floorA, Direction.UP);
		assertNotNull(a);
	}

	@Test
	final public void testGetDestination() {
		assertEquals(this.floorA, this.a.getDestination());
	}

	@Test
	final public void testGetDirection() {
		assertEquals(Direction.UP, this.a.getDirection());
	}

	/*
	 * Test for boolean equals(Object)
	 */
	@Test
	final public void testEqualsTrue() {
		final Assignment b = new Assignment(this.floorA, Direction.UP);
		assertTrue(this.a.equals(b));
		assertTrue(b.equals(this.a));
	}

	@Test
	final public void testNullEqualsFalse() {
		assertFalse(this.a.equals(null));
	}

	@Test
	final public void testInstanceEqualsFalse() {
		assertFalse(this.a.equals("a"));
	}

	@Test
	final public void testDestinationEqualsFalse() {
		final Assignment b = new Assignment(this.floorB, Direction.UP);
		assertFalse(this.a.equals(b));
	}

	@Test
	final public void testDirectionEqualsFalse() {
		final Assignment b = new Assignment(this.floorA, Direction.DOWN);
		assertFalse(this.a.equals(b));
	}

}
