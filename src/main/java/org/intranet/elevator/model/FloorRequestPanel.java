/*
 * Copyright 2004 Neil McKellar and Chris Dailey
 * All rights reserved.
 */
package org.intranet.elevator.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Neil McKellar and Chris Dailey
 * 
 */
public class FloorRequestPanel {
	
	private final List<Floor> floors = new ArrayList<Floor>();
	private final List<Floor> requestedFloors = new ArrayList<Floor>();
	private final List<Listener> listeners = new ArrayList<Listener>();

	public final void addServicedFloor(final Floor floor) {
		floors.add(floor);
	}

	public final Iterator<Floor> getServicedFloorsI() {
		return floors.iterator();
	}

	public final Floor getFloorAt(float height) {
		for (Iterator<Floor> i = floors.iterator(); i.hasNext();) {
			Floor f = i.next();
			if (f.getHeight() == height)
				return f;
		}
		return null;
	}

	public List<Floor> getServicedFloors() {
		return floors;
	}

	public List<Floor> getRequestedFloors() {
		return requestedFloors;
	}

	Floor getMaxFloor() {
		Floor maxFloor = null;
		float floorHeight = Float.MIN_VALUE;
		for (Iterator<Floor> i = floors.iterator(); i.hasNext();) {
			Floor f = i.next();
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
		for (Iterator<Floor> i = floors.iterator(); i.hasNext();) {
			Floor f = i.next();
			if (f.getHeight() < floorHeight) {
				floorHeight = f.getHeight();
				minFloor = f;
			}
		}
		return minFloor;
	}

	// called by Person
	public final void requestFloor(Floor floor) {
		if (!floors.contains(floor))
			throw new IllegalArgumentException(
					"Cannot request unreachable floors.");
		if (!requestedFloors.contains(floor)) {
			requestedFloors.add(floor);
			for (Iterator<Listener> i = listeners.iterator(); i.hasNext();) {
				Listener listener = i.next();
				listener.floorRequested(floor);
			}
		}
	}

	final void requestFulfilled(Floor floor) {
		if (!floors.contains(floor))
			throw new IllegalArgumentException(
					"Cannot fulfill request for unreachable floor.  " + floor);
		requestedFloors.remove(floor);
	}

	public static interface Listener {
		void floorRequested(Floor floor);
	}

	public void addListener(Listener l) {
		listeners.add(l);
	}
}
