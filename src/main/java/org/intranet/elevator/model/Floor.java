/*
* Copyright 2003-2005 Neil McKellar and Chris Dailey
* All rights reserved.
*/
package org.intranet.elevator.model;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.intranet.sim.event.EventQueue;

/**
 * @author Neil McKellar and Chris Dailey
 */
public final class Floor extends Location {
	private final int number;
	// distance from the ground
	private final float ceiling; // relative to the floor's height
	private final CarRequestPanel callPanel = new CarRequestPanel();
	private final List<CarEntrance> carEntrances = new LinkedList<>();

	// TODO: Make a sequence diagram with all the passing off of notification
	private final CarEntrance.CarEntranceListener carEntranceListener = new CarEntrance.CarEntranceListener() {
		@Override
		public void arrivedUp(final CarEntrance entrance) {
			Floor.this.callPanel.arrivedUp(entrance);
		}

		@Override
		public void arrivedDown(final CarEntrance entrance) {
			Floor.this.callPanel.arrivedDown(entrance);
		}
	};

	public Floor(final EventQueue eQ, final int number, final float height, final float ceiling) {
		super(eQ, height, 500);
		this.number = number;
		this.ceiling = ceiling;
	}

	public int getFloorNumber() {
		return this.number;
	}

	public float getCeiling() {
		return this.ceiling;
	}

	public float getAbsoluteCeiling() {
		return getHeight() + this.ceiling;
	}

	public CarRequestPanel getCallPanel() {
		return this.callPanel;
	}

	public void createCarEntrance(final Location destination) {
		this.carEntrances.add(new CarEntrance(this.eventQueue, this, destination, this.carEntranceListener));
	}

	public Iterator<CarEntrance> getCarEntrances() {
		return this.carEntrances.iterator();
	}

	public CarEntrance getOpenCarEntrance(final boolean up) {
		for (final CarEntrance carEntrance2 : this.carEntrances) {
			final CarEntrance carEntrance = carEntrance2;
			if (carEntrance.getDoor().isOpen()) {
				if (up && carEntrance.isUp()) {
					return carEntrance;
				}
				if (!up && carEntrance.isDown()) {
					return carEntrance;
				}
			}
		}
		return null;
	}

	public CarEntrance getCarEntranceForCar(final Location destination) {
		for (final CarEntrance carEntrance2 : this.carEntrances) {
			final CarEntrance carEntrance = carEntrance2;
			if (carEntrance.getDoor().getTo() == destination) {
				return carEntrance;
			}
		}
		return null;
	}

	@Override
	public String toString() {
		return "Floor" + this.number + "@" + getHeight();
	}
}