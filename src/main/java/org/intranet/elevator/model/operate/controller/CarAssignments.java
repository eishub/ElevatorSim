/*
 * Copyright 2003-2004 Neil McKellar and Chris Dailey
 * All rights reserved.
 */
package org.intranet.elevator.model.operate.controller;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import org.intranet.elevator.model.Floor;

class CarAssignments {
	private List<Assignment> list = new LinkedList<>();
	private final String carName;

	CarAssignments() {
		this("noname");
	}

	CarAssignments(final String name) {
		super();
		this.carName = name;
	}

	private CarAssignments(final CarAssignments ca) {
		this(ca.carName + "-clone");
		this.list = new ArrayList<>(ca.list);
	}

	void printAssignments(final String prefix) {
		if (!this.log) {
			return;
		}
		System.out.flush();
		System.err.flush();
		System.out.print(prefix + " ");
		for (final Object element : this.list) {
			final Assignment a = (Assignment) element;
			System.out.print(a.toString() + ",");
		}
		System.out.println();
		System.out.flush();
		System.err.flush();
	}

	private boolean log = false;

	void activateLog() {
		this.log = true;
	}

	private void log(final String message) {
		if (this.log) {
			System.err.flush();
			System.out.flush();
			System.out.println("[" + this.carName + "]: " + message);
			System.out.flush();
			System.err.flush();
		}
	}

	void addAssignment(final List<Floor> floors, final Assignment base, final Assignment newAssignment) {
		log("addAssignment(floor, " + base + ", " + newAssignment + ")");
		printAssignments("addAssignment");
		if (this.list.isEmpty()) {
			log("Add to empty list.");
			System.out.flush();
			this.list.add(newAssignment);
			return;
		}
		if (this.list.contains(newAssignment)) {
			return;
		}
		final int newNumStops = maxStops(floors, base, newAssignment);
		log("max stops with new: " + newNumStops);

		// Iterate through the list of destinations and determine the best place
		// to put the new assignment.
		// It should go just before the destination that has a greater 'maxStops'
		// value or at the end if there is no greater value.
		for (final ListIterator<Assignment> l = this.list.listIterator(); l.hasNext();) {
			final Assignment a = l.next();
			final int aNumStops = maxStops(floors, base, a);
			log("stops for: " + a + " = " + aNumStops);
			if (aNumStops > newNumStops) {
				l.previous();
				l.add(newAssignment);
				log("add new assignment here");
				return;
			}
		}
		// There were no assignments after our new assignment
		this.list.add(newAssignment);
		log("append new assignment");
	}

	/*
	 * Determine an ordinal value for the destination relative to a starting
	 * position in the list. 'UP' direction destinations are simply their positions
	 * in the list of floors. 'DOWN' direction destinations are counted as
	 * "wrapped around" and so are treated as their position plus the total number
	 * of possible floors. The return value can be between 0 and two times the total
	 * number of floors.
	 */
	private int maxStops(final List<Floor> floors, final Assignment base, final Assignment destination) {
		final int topFloorNumber = floors.size();

		int baseNumber = floors.indexOf(base.getDestination());
		int destNumber = floors.indexOf(destination.getDestination());

		if (base.getDirection() == Direction.DOWN) {
			baseNumber = topFloorNumber + (topFloorNumber - baseNumber - 1);
		}
		if (destination.getDirection() == Direction.DOWN) {
			destNumber = topFloorNumber + (topFloorNumber - destNumber - 1);
		}

		// If dest comes after base in the list of destinations,
		// Then the ordinal value is just the difference between the two
		// positions.
		// Otherwise, the ordinal value wraps around the complete list of floors
		// to come up "behind" the base value. Hence, 2 x total floors is added
		// to the difference.
		final int difference = destNumber - baseNumber;
		if (destNumber >= baseNumber) {
			return difference;
		}

		return 2 * topFloorNumber + difference;
	}

	void removeAssignment(final Assignment assignment) {
		if (!this.list.remove(assignment)) {
			throw new IllegalStateException("Can't remove non-existant assignment.");
		}
	}

	Assignment getCurrentAssignment() {
		if (this.list.isEmpty()) {
			return null;
		}
		return this.list.get(0);
	}

	Iterator<Assignment> iteratorIncluding(final List<Floor> floors, final Assignment base, final Assignment endPoint) {
		final CarAssignments clone = new CarAssignments(this);
		clone.addAssignment(floors, base, endPoint);
		return clone.iterator();
	}

	Iterator<Assignment> iterator() {
		return this.list.iterator();
	}

	boolean contains(final Assignment assignment) {
		return this.list.contains(assignment);
	}

	@Override
	public String toString() {
		final StringBuffer sb = new StringBuffer();
		for (final Iterator<Assignment> assignments = iterator(); assignments.hasNext();) {
			final Assignment a = assignments.next();
			if (sb.length() > 0) {
				sb.append(",");
			}
			sb.append(a.toString());
		}
		return sb.toString();
	}
}