/*
 * Copyright 2003 Neil McKellar and Chris Dailey
 * All rights reserved.
 */
package org.intranet.elevator.model.operate.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.intranet.elevator.model.Floor;
import org.intranet.sim.event.EventQueue;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Neil McKellar and Chris Dailey
 */
public class CarAssignmentsTest {
	private EventQueue eQ;
	private List<Floor> floors;
	private Floor floor1;
	private Floor floor2;
	private Floor floor3;
	private Floor floor4;
	private Assignment base;
	private Assignment up1;
	private Assignment up2;
	private Assignment up3;
	private Assignment up4;
	private Assignment down1;
	private Assignment down2;
	private Assignment down3;
	private Assignment down4;

	@Before
	public void setUp() throws Exception {
		this.eQ = new EventQueue();

		this.floor1 = new Floor(this.eQ, 1, 0, 10);
		this.floor2 = new Floor(this.eQ, 2, 10, 20);
		this.floor3 = new Floor(this.eQ, 3, 20, 30);
		this.floor4 = new Floor(this.eQ, 4, 30, 40);

		this.floors = new ArrayList<>(4);
		this.floors.add(this.floor1);
		this.floors.add(this.floor2);
		this.floors.add(this.floor3);
		this.floors.add(this.floor4);

		this.base = new Assignment(this.floor1, Direction.NONE);

		this.up1 = new Assignment(this.floor1, Direction.UP);
		this.up2 = new Assignment(this.floor2, Direction.UP);
		this.up3 = new Assignment(this.floor3, Direction.UP);
		this.up4 = new Assignment(this.floor4, Direction.UP);

		this.down1 = new Assignment(this.floor1, Direction.DOWN);
		this.down2 = new Assignment(this.floor2, Direction.DOWN);
		this.down3 = new Assignment(this.floor3, Direction.DOWN);
		this.down4 = new Assignment(this.floor4, Direction.DOWN);
	}

	@Test
	final public void testCarAssignments() {
		final CarAssignments a = new CarAssignments();
		assertNotNull(a);
	}

	@Test
	final public void testAddToNull() {
		final CarAssignments a = new CarAssignments();
		a.addAssignment(this.floors, this.base, this.up1);

		// confirm that list contains up1
		assertTrue(a.contains(this.up1));
		// confirm that it *only* contains up1
		final Iterator<Assignment> i = a.iterator();
		assertTrue(i.hasNext());
		assertEquals(this.up1, i.next());
		assertFalse(i.hasNext());
	}

	@Test
	final public void testAddTwice() {
		final CarAssignments a = new CarAssignments();
		a.addAssignment(this.floors, this.base, this.up1);
		// confirm that list contains up1
		assertTrue(a.contains(this.up1));
		// add it again - should have no effect
		a.addAssignment(this.floors, this.base, this.up1);
		// confirm that it *only* contains up1 (once)
		final Iterator<Assignment> i = a.iterator();
		assertTrue(i.hasNext());
		assertEquals(this.up1, i.next());
		assertFalse(i.hasNext());
	}

	@Test
	final public void testAddNoneUpUp() {
		// base = 1NONE
		// add: 3UP, 2UP
		// list order should be: 2UP, 3UP
		final CarAssignments a = new CarAssignments();
		a.addAssignment(this.floors, this.base, this.up3);
		// add in front of up3
		a.addAssignment(this.floors, this.base, this.up2);
		final Iterator<Assignment> i = a.iterator();
		assertEquals(this.up2, i.next());
		assertEquals(this.up3, i.next());
	}

	@Test
	final public void testAddNoneDownDown() {
		// betcha this one doesn't work
		// maxStops() only checks base.Direction and math is biased to UP

		// base = 1NONE
		// add: 2DOWN, 3DOWN
		// list order should be: 3DOWN, 2DOWN
		final CarAssignments a = new CarAssignments();
		a.addAssignment(this.floors, this.base, this.down2);
		// add in front of down2
		a.addAssignment(this.floors, this.base, this.down3);
		final Iterator<Assignment> i = a.iterator();
		assertEquals(this.down3, i.next());
		assertEquals(this.down2, i.next());
	}

	@Test
	final public void testAddDownUpDown() {
		// base = 3DOWN
		// add: 4UP, 2DOWN
		// list order should be: 2DOWN, 4UP
		final CarAssignments a = new CarAssignments();
		a.addAssignment(this.floors, this.down3, this.up4);
		// add in front of up4
		a.addAssignment(this.floors, this.down3, this.down2);
		final Iterator<Assignment> i = a.iterator();
		assertEquals(this.down2, i.next());
		assertEquals(this.up4, i.next());
	}

	@Test
	final public void testAddUpDownUp() {
		// base: 2UP
		// add: 1DOWN, 3UP
		// list order should be: 3UP, 1DOWN
		final CarAssignments a = new CarAssignments();
		a.addAssignment(this.floors, this.up2, this.down1);
		// add in front of down1
		a.addAssignment(this.floors, this.up2, this.up3);
		final Iterator<Assignment> i = a.iterator();
		assertEquals(this.up3, i.next());
		assertEquals(this.down1, i.next());
	}

	@Test
	final public void testAddDownUpUp() {
		// base = 3DOWN
		// add: 2UP, 1UP
		// list order should be: 1UP, 2UP
		final CarAssignments a = new CarAssignments();
		a.addAssignment(this.floors, this.down3, this.up2);
		// add in front of up2
		a.addAssignment(this.floors, this.down3, this.up1);
		final Iterator<Assignment> i = a.iterator();
		assertEquals(this.up1, i.next());
		assertEquals(this.up2, i.next());
	}

	@Test
	final public void testAddDownDownDown() {
		// base = 3DOWN
		// add: 1DOWN, 2DOWN
		// list order should be: 2DOWN, 1DOWN
		final CarAssignments a = new CarAssignments();
		a.addAssignment(this.floors, this.down3, this.down1);
		// add in front of down1
		a.addAssignment(this.floors, this.down3, this.down2);
		final Iterator<Assignment> i = a.iterator();
		assertEquals(this.down2, i.next());
		assertEquals(this.down1, i.next());
	}

	@Test
	final public void testAddUpUpUp() {
		// base = 1UP
		// add: 3UP, 2UP
		// list order should be: 2UP, 3UP
		final CarAssignments a = new CarAssignments();
		a.addAssignment(this.floors, this.up1, this.up3);
		// add in front of up3
		a.addAssignment(this.floors, this.up1, this.up2);
		final Iterator<Assignment> i = a.iterator();
		assertEquals(this.up2, i.next());
		assertEquals(this.up3, i.next());
	}

	@Test
	final public void testAddUpDownDown() {
		// base = 1UP
		// add: 2DOWN, 3DOWN
		// list order should be: 3DOWN, 2DOWN
		final CarAssignments a = new CarAssignments();
		a.addAssignment(this.floors, this.up1, this.down2);
		// add in front of down2
		a.addAssignment(this.floors, this.up1, this.down3);
		final Iterator<Assignment> i = a.iterator();
		assertEquals(this.down3, i.next());
		assertEquals(this.down2, i.next());
	}

	@Test
	final public void testTopEqualsBase() {
		// math in maxStops() checks value of topFloor when base.Direction =
		// DOWN
		// this test confirms that the math is correct
		final CarAssignments a = new CarAssignments();
		a.addAssignment(this.floors, this.down4, this.down2);
		// add in front of down2
		a.addAssignment(this.floors, this.down4, this.down3);
		final Iterator<Assignment> i = a.iterator();
		assertEquals(this.down3, i.next());
		assertEquals(this.down2, i.next());
	}

	@Test
	final public void testTopEqualsDest() {
		// math in maxStops() checks value of topFloor when dest.Direction =
		// DOWN
		// this test confirms that math is correct
		final CarAssignments a = new CarAssignments();
		a.addAssignment(this.floors, this.up2, this.down3);
		// add in front of down3
		a.addAssignment(this.floors, this.up2, this.down4);
		final Iterator<Assignment> i = a.iterator();
		assertEquals(this.down4, i.next());
		assertEquals(this.down3, i.next());
	}

	@Test
	final public void testWrapAroundUp() {
		// base = 3UP
		// add: 4UP, 2UP, 3DOWN, 1UP
		// list order should be: 4UP, 3DOWN, 1UP, 2UP
		final CarAssignments a = new CarAssignments();
		a.addAssignment(this.floors, this.up3, this.up4);
		a.addAssignment(this.floors, this.up3, this.up2);
		// add in front of up2
		a.addAssignment(this.floors, this.up3, this.down3);
		a.addAssignment(this.floors, this.up3, this.up1);
		final Iterator<Assignment> i = a.iterator();
		assertEquals(this.up4, i.next());
		assertEquals(this.down3, i.next());
		assertEquals(this.up1, i.next());
		assertEquals(this.up2, i.next());
	}

	@Test
	final public void testWrapAroundDown() {
		// base = 2DOWN
		// add: 1DOWN, 3DOWN, 2UP, 4DOWN
		// list order should be: 1DOWN, 2UP, 4DOWN, 3DOWN
		final CarAssignments a = new CarAssignments();
		a.addAssignment(this.floors, this.down2, this.down1);
		a.addAssignment(this.floors, this.down2, this.down3);
		// add in front of down3
		a.addAssignment(this.floors, this.down2, this.up2);
		a.addAssignment(this.floors, this.down2, this.down4);
		final Iterator<Assignment> i = a.iterator();
		assertEquals(this.down1, i.next());
		assertEquals(this.up2, i.next());
		assertEquals(this.down4, i.next());
		assertEquals(this.down3, i.next());
	}

	@Test
	final public void testGetCurrent() {
		final CarAssignments a = new CarAssignments();
		assertEquals(null, a.getCurrentAssignment());
		a.addAssignment(this.floors, this.base, this.up1);
		assertEquals(this.up1, a.getCurrentAssignment());
	}

	@Test
	final public void testIncluding() {
		// test that iteratorIncluding() includes new assignment
		final CarAssignments a = new CarAssignments();
		a.addAssignment(this.floors, this.base, this.up1);
		final Iterator<Assignment> i = a.iteratorIncluding(this.floors, this.base, this.up2);
		assertEquals(this.up1, i.next());
		assertEquals(this.up2, i.next());
	}

	@Test
	final public void testRemove() {
		// add: 1UP, 2UP
		// after remove: *only* 2UP
		final CarAssignments a = new CarAssignments();
		a.addAssignment(this.floors, this.base, this.up1);
		a.addAssignment(this.floors, this.base, this.up2);
		assertTrue(a.contains(this.up1));
		assertTrue(a.contains(this.up2));
		a.removeAssignment(this.up1);
		assertFalse(a.contains(this.up1));
		assertTrue(a.contains(this.up2));
	}
}
