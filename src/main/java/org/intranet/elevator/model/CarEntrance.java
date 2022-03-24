/*
* Copyright 2003,2005 Neil McKellar and Chris Dailey
* All rights reserved.
*/
package org.intranet.elevator.model;

import java.util.ArrayList;
import java.util.List;

import org.intranet.sim.event.EventQueue;

/**
 * @author Neil McKellar and Chris Dailey
 */
public final class CarEntrance {
	public interface CarEntranceListener {
		void arrivedUp(CarEntrance entrance);

		void arrivedDown(CarEntrance entrance);
	}

	public interface DoorWaitListener {
		void doorAvailable();
	}

	private boolean up;
	private boolean down;
	private final Door door;
	private final DoorSensor sensor;
	private final CarEntranceListener entranceListener;

	CarEntrance(final EventQueue eQ, final Location fromLocation, final Location toLocation,
			final CarEntranceListener listener) {
		this.door = new Door(eQ, fromLocation, toLocation);
		this.sensor = new DoorSensor(eQ);
		this.entranceListener = listener;
		final Door.Listener doorListener = new Door.Listener() {
			@Override
			public void doorOpened() {
				if (CarEntrance.this.up) {
					CarEntrance.this.entranceListener.arrivedUp(CarEntrance.this);
				} else if (CarEntrance.this.down) {
					CarEntrance.this.entranceListener.arrivedDown(CarEntrance.this);
				}
				if (CarEntrance.this.sensor.getState() == DoorSensor.State.CLEAR) {
					CarEntrance.this.sensor.unobstruct();
				}
			}

			@Override
			public void doorClosed() {
			}
		};
		this.door.addListener(doorListener, false);
		this.sensor.addListener(new DoorSensor.Listener() {
			@Override
			public void sensorCleared() {
				CarEntrance.this.door.close();
			}

			@Override
			public void sensorObstructed() {
				if (CarEntrance.this.door.getState() == Door.State.CLOSING) {
					CarEntrance.this.door.open();
				}
			}

			@Override
			public void sensorUnobstructed() {
			}
		});
	}

	private final List<DoorWaitListener> waiters = new ArrayList<>();

	public void waitToEnterDoor(final DoorWaitListener listener) {
		this.waiters.add(listener);
		chooseSomeoneFromList();
	}

	public boolean arePeopleWaitingToGetOut() {
		return this.waiters.size() > 0;
	}

	private void chooseSomeoneFromList() {
		boolean isSensorAvailable;
		do {
			if (this.waiters.size() == 0) {
				return;
			}
			isSensorAvailable = (this.door.getState() != Door.State.OPENING
					&& (this.sensor.getState() == DoorSensor.State.UNOBSTRUCTED
							|| this.sensor.getState() == DoorSensor.State.CLEAR));
			if (isSensorAvailable) {
				final DoorWaitListener listener = this.waiters.remove(0);
				listener.doorAvailable();
			}
			if (!isSensorAvailable) {
				this.sensor.addListener(new DoorSensor.Listener() {
					@Override
					public void sensorCleared() {
						CarEntrance.this.sensor.removeListener(this);
						chooseSomeoneFromList();
					}

					@Override
					public void sensorObstructed() {
					}

					@Override
					public void sensorUnobstructed() {
						CarEntrance.this.sensor.removeListener(this);
						chooseSomeoneFromList();
					}
				});
			}
		} while (this.waiters.size() > 0 && isSensorAvailable);
	}

	public boolean isUp() {
		return this.up;
	}

	public void setUp(final boolean up) {
		this.up = up;
	}

	public boolean isDown() {
		return this.down;
	}

	public void setDown(final boolean down) {
		this.down = down;
	}

	public DoorSensor getDoorSensor() {
		return this.sensor;
	}

	public Door getDoor() {
		return this.door;
	}
}