/*
 * Copyright 2004 Neil McKellar and Chris Dailey
 * All rights reserved.
 */
package org.intranet.elevator.model;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Neil McKellar and Chris Dailey
 *
 */
public class FloorRequestPanel {
	private final List<Floor> floors = new LinkedList<>();
	private final List<Floor> requestedFloors = new LinkedList<>();
	private final List<Listener> listeners = new LinkedList<>();

	public final void addServicedFloor(final Floor floor) {
		this.floors.add(floor);
	}

	public final Iterator<Floor> getServicedFloorsI() {
		return this.floors.iterator();
	}

	public final Floor getFloorAt(final float height) {
		for (final Floor f : this.floors) {
			if (f.getHeight() == height) {
				return f;
			}
		}
		return null;
	}

	public List<Floor> getServicedFloors() {
		return this.floors;
	}

	public List<Floor> getRequestedFloors() {
		return this.requestedFloors;
	}

	Floor getMaxFloor() {
		Floor maxFloor = null;
		float floorHeight = Float.MIN_VALUE;
		for (final Floor f : this.floors) {
			if (f.getHeight() > floorHeight) {
				floorHeight = f.getHeight();
				maxFloor = f;
			}
		}
		return maxFloor;
	}

	Floor getMinFloor() {
		Floor minFloor = null;
		float floorHeight = Float.MAX_VALUE;
		for (final Floor f : this.floors) {
			if (f.getHeight() < floorHeight) {
				floorHeight = f.getHeight();
				minFloor = f;
			}
		}
		return minFloor;
	}

	// called by Person
	public final void requestFloor(final Floor floor) {
		if (!this.floors.contains(floor)) {
			throw new IllegalArgumentException("Cannot request unreachable floors.");
		}
		if (!this.requestedFloors.contains(floor)) {
			this.requestedFloors.add(floor);
			for (final Listener listener : this.listeners) {
				listener.floorRequested(floor);
			}
		}
	}

	final void requestFulfilled(final Floor floor) {
		if (!this.floors.contains(floor)) {
			throw new IllegalArgumentException("Cannot fulfill request for unreachable floor.  " + floor);
		}
		this.requestedFloors.remove(floor);
	}

	public interface Listener {
		void floorRequested(Floor floor);
	}

	public void addListener(final Listener l) {
		this.listeners.add(l);
	}
}
