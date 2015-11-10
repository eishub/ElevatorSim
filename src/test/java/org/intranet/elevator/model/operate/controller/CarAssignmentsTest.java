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
 *
 */
public class CarAssignmentsTest {
	private EventQueue eQ;
	private List floors;
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

		eQ = new EventQueue();

		floor1 = new Floor(eQ, 1, 0, 10);
		floor2 = new Floor(eQ, 2, 10, 20);
		floor3 = new Floor(eQ, 3, 20, 30);
		floor4 = new Floor(eQ, 4, 30, 40);

		floors = new ArrayList();
		floors.add(floor1);
		floors.add(floor2);
		floors.add(floor3);
		floors.add(floor4);

		base = new Assignment(floor1, Direction.NONE);

		up1 = new Assignment(floor1, Direction.UP);
		up2 = new Assignment(floor2, Direction.UP);
		up3 = new Assignment(floor3, Direction.UP);
		up4 = new Assignment(floor4, Direction.UP);

		down1 = new Assignment(floor1, Direction.DOWN);
		down2 = new Assignment(floor2, Direction.DOWN);
		down3 = new Assignment(floor3, Direction.DOWN);
		down4 = new Assignment(floor4, Direction.DOWN);
	}

	@Test
	final public void testCarAssignments() {
		CarAssignments a = new CarAssignments();
		assertNotNull(a);
	}

	@Test
	final public void testAddToNull() {
		CarAssignments a = new CarAssignments();
		a.addAssignment(floors, base, up1);

		// confirm that list contains up1
		assertTrue(a.contains(up1));
		// confirm that it *only* contains up1
		Iterator i = a.iterator();
		assertTrue(i.hasNext());
		assertEquals(up1, i.next());
		assertFalse(i.hasNext());
	}

	@Test
	final public void testAddTwice() {
		CarAssignments a = new CarAssignments();
		a.addAssignment(floors, base, up1);
		// confirm that list contains up1
		assertTrue(a.contains(up1));
		// add it again - should have no effect
		a.addAssignment(floors, base, up1);
		// confirm that it *only* contains up1 (once)
		Iterator i = a.iterator();
		assertTrue(i.hasNext());
		assertEquals(up1, i.next());
		assertFalse(i.hasNext());
	}

	@Test
	final public void testAddNoneUpUp() {
		// base = 1NONE
		// add: 3UP, 2UP
		// list order should be: 2UP, 3UP
		CarAssignments a = new CarAssignments();
		a.addAssignment(floors, base, up3);
		// add in front of up3
		a.addAssignment(floors, base, up2);
		Iterator i = a.iterator();
		assertEquals(up2, i.next());
		assertEquals(up3, i.next());
	}

	@Test
	final public void testAddNoneDownDown() {
		// betcha this one doesn't work
		// maxStops() only checks base.Direction and math is biased to UP

		// base = 1NONE
		// add: 2DOWN, 3DOWN
		// list order should be: 3DOWN, 2DOWN
		CarAssignments a = new CarAssignments();
		a.addAssignment(floors, base, down2);
		// add in front of down2
		a.addAssignment(floors, base, down3);
		Iterator i = a.iterator();
		assertEquals(down3, i.next());
		assertEquals(down2, i.next());
	}

	@Test
	final public void testAddDownUpDown() {
		// base = 3DOWN
		// add: 4UP, 2DOWN
		// list order should be: 2DOWN, 4UP
		CarAssignments a = new CarAssignments();
		a.addAssignment(floors, down3, up4);
		// add in front of up4
		a.addAssignment(floors, down3, down2);
		Iterator i = a.iterator();
		assertEquals(down2, i.next());
		assertEquals(up4, i.next());
	}

	@Test
	final public void testAddUpDownUp() {
		// base: 2UP
		// add: 1DOWN, 3UP
		// list order should be: 3UP, 1DOWN
		CarAssignments a = new CarAssignments();
		a.addAssignment(floors, up2, down1);
		// add in front of down1
		a.addAssignment(floors, up2, up3);
		Iterator i = a.iterator();
		assertEquals(up3, i.next());
		assertEquals(down1, i.next());
	}

	@Test
	final public void testAddDownUpUp() {
		// base = 3DOWN
		// add: 2UP, 1UP
		// list order should be: 1UP, 2UP
		CarAssignments a = new CarAssignments();
		a.addAssignment(floors, down3, up2);
		// add in front of up2
		a.addAssignment(floors, down3, up1);
		Iterator i = a.iterator();
		assertEquals(up1, i.next());
		assertEquals(up2, i.next());
	}

	@Test
	final public void testAddDownDownDown() {
		// base = 3DOWN
		// add: 1DOWN, 2DOWN
		// list order should be: 2DOWN, 1DOWN
		CarAssignments a = new CarAssignments();
		a.addAssignment(floors, down3, down1);
		// add in front of down1
		a.addAssignment(floors, down3, down2);
		Iterator i = a.iterator();
		assertEquals(down2, i.next());
		assertEquals(down1, i.next());
	}

	@Test
	final public void testAddUpUpUp() {
		// base = 1UP
		// add: 3UP, 2UP
		// list order should be: 2UP, 3UP
		CarAssignments a = new CarAssignments();
		a.addAssignment(floors, up1, up3);
		// add in front of up3
		a.addAssignment(floors, up1, up2);
		Iterator i = a.iterator();
		assertEquals(up2, i.next());
		assertEquals(up3, i.next());
	}

	@Test
	final public void testAddUpDownDown() {
		// base = 1UP
		// add: 2DOWN, 3DOWN
		// list order should be: 3DOWN, 2DOWN
		CarAssignments a = new CarAssignments();
		a.addAssignment(floors, up1, down2);
		// add in front of down2
		a.addAssignment(floors, up1, down3);
		Iterator i = a.iterator();
		assertEquals(down3, i.next());
		assertEquals(down2, i.next());
	}

	@Test
	final public void testTopEqualsBase() {
		// math in maxStops() checks value of topFloor when base.Direction =
		// DOWN
		// this test confirms that the math is correct
		CarAssignments a = new CarAssignments();
		a.addAssignment(floors, down4, down2);
		// add in front of down2
		a.addAssignment(floors, down4, down3);
		Iterator i = a.iterator();
		assertEquals(down3, i.next());
		assertEquals(down2, i.next());
	}

	@Test
	final public void testTopEqualsDest() {
		// math in maxStops() checks value of topFloor when dest.Direction =
		// DOWN
		// this test confirms that math is correct
		CarAssignments a = new CarAssignments();
		a.addAssignment(floors, up2, down3);
		// add in front of down3
		a.addAssignment(floors, up2, down4);
		Iterator i = a.iterator();
		assertEquals(down4, i.next());
		assertEquals(down3, i.next());
	}

	@Test
	final public void testWrapAroundUp() {
		// base = 3UP
		// add: 4UP, 2UP, 3DOWN, 1UP
		// list order should be: 4UP, 3DOWN, 1UP, 2UP
		CarAssignments a = new CarAssignments();
		a.addAssignment(floors, up3, up4);
		a.addAssignment(floors, up3, up2);
		// add in front of up2
		a.addAssignment(floors, up3, down3);
		a.addAssignment(floors, up3, up1);
		Iterator i = a.iterator();
		assertEquals(up4, i.next());
		assertEquals(down3, i.next());
		assertEquals(up1, i.next());
		assertEquals(up2, i.next());
	}

	@Test
	final public void testWrapAroundDown() {
		// base = 2DOWN
		// add: 1DOWN, 3DOWN, 2UP, 4DOWN
		// list order should be: 1DOWN, 2UP, 4DOWN, 3DOWN
		CarAssignments a = new CarAssignments();
		a.addAssignment(floors, down2, down1);
		a.addAssignment(floors, down2, down3);
		// add in front of down3
		a.addAssignment(floors, down2, up2);
		a.addAssignment(floors, down2, down4);
		Iterator i = a.iterator();
		assertEquals(down1, i.next());
		assertEquals(up2, i.next());
		assertEquals(down4, i.next());
		assertEquals(down3, i.next());
	}

	@Test
	final public void testGetCurrent() {
		CarAssignments a = new CarAssignments();
		assertEquals(null, a.getCurrentAssignment());
		a.addAssignment(floors, base, up1);
		assertEquals(up1, a.getCurrentAssignment());
	}

	@Test
	final public void testIncluding() {
		// test that iteratorIncluding() includes new assignment
		CarAssignments a = new CarAssignments();
		a.addAssignment(floors, base, up1);
		Iterator i = a.iteratorIncluding(floors, base, up2);
		assertEquals(up1, i.next());
		assertEquals(up2, i.next());
	}

	@Test
	final public void testRemove() {
		// add: 1UP, 2UP
		// after remove: *only* 2UP
		CarAssignments a = new CarAssignments();
		a.addAssignment(floors, base, up1);
		a.addAssignment(floors, base, up2);
		assertTrue(a.contains(up1));
		assertTrue(a.contains(up2));
		a.removeAssignment(up1);
		assertFalse(a.contains(up1));
		assertTrue(a.contains(up2));
	}
}
