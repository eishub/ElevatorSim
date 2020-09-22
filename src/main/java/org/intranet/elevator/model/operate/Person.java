/*
 * Copyright 2003,2005 Neil McKellar and Chris Dailey
 * All rights reserved.
 */
package org.intranet.elevator.model.operate;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.intranet.elevator.model.Car;
import org.intranet.elevator.model.CarEntrance;
import org.intranet.elevator.model.CarRequestPanel;
import org.intranet.elevator.model.Door;
import org.intranet.elevator.model.DoorSensor;
import org.intranet.elevator.model.Floor;
import org.intranet.elevator.model.Location;
import org.intranet.sim.ModelElement;
import org.intranet.sim.event.Event;
import org.intranet.sim.event.EventQueue;
import org.intranet.sim.event.TrackingUpdateEvent;

/**
 * A Person moves around the building, calling elevators, entering elevators,
 * and leaving elevators. Person states:
 * <table border="1" cellspacing="0" cellpadding="2" summary="">
 * <tr>
 * <th rowspan="2">State</th>
 * <th colspan="2">Variables</th>
 * <th colspan="11">Transitions</th>
 * </tr>
 * <tr>
 * <th>destination</th>
 * <th>currentLocation</th>
 * <th>setDestination()</th>
 * <th>CarRequestPanel<br>
 * [arrivedUp/Down]</th>
 * <th>Door<br>
 * [doorClosed]</th>
 * <th>Car<br>
 * [docked]</th>
 * <th>Door<br>
 * [doorOpened]</th>
 * <th>[leftCar]</th>
 * </tr>
 * <tr>
 * <td>Idle</td>
 * <td>null</td>
 * <td>Set</td>
 * <td>Idle if destination is same<br>
 * pressButton(): Waiting if elevator is elsewhere<br>
 * enterCar(): Travelling if elevator is there</td>
 * <td><i>Impossible</i></td>
 * <td><i>Impossible</i></td>
 * <td><i>Impossible</i></td>
 * <td><i>Impossible</i></td>
 * <td><i>Impossible</i></td>
 * </tr>
 * <tr>
 * <td>Waiting</td>
 * <td>Set</td>
 * <td>Set to Floor</td>
 * <td><i>Illegal?</i></td>
 * <td>enterCar(): Travelling if success<br>
 * waitForDoorClose(): DoorClosing if car is full</td>
 * <td><i>Impossible</i></td>
 * <td><i>Impossible</i></td>
 * <td><i>Impossible</i></td>
 * <td><i>Impossible</i></td>
 * </tr>
 * <tr>
 * <td>DoorClosing</td>
 * <td>Set</td>
 * <td>Set to Floor</td>
 * <td><i>Illegal?</i></td>
 * <td><i>Impossible</i></td>
 * <td>doorClosed(): Waiting via pressedUp/Down()</td>
 * <td><i>Impossible</i></td>
 * <td><i>Impossible</i></td>
 * <td><i>Impossible</i></td>
 * </tr>
 * <tr>
 * <td>Travelling</td>
 * <td>Set</td>
 * <td>Set to Car</td>
 * <td><i>Illegal?</i></td>
 * <td><i>Impossible</i></td>
 * <td><i>Impossible</i></td>
 * <td>waitForDoorOpen(): DoorOpening</td>
 * <td><i>Impossible</i></td>
 * <td><i>Impossible</i></td>
 * </tr>
 * <tr>
 * <td>DoorOpening</td>
 * <td>Set</td>
 * <td>Set to Car</td>
 * <td><i>Illegal?</i></td>
 * <td><i>Impossible</i></td>
 * <td><i>Impossible</i></td>
 * <td><i>Impossible</i></td>
 * <td>leaveCar(): LeavingCar</td>
 * <td><i>Impossible</i></td>
 * </tr>
 * <tr>
 * <td>LeavingCar</td>
 * <td>Set</td>
 * <td>Set to Car</td>
 * <td><i>Illegal?</i></td>
 * <td><i>Impossible</i></td>
 * <td><i>Impossible</i></td>
 * <td><i>Impossible</i></td>
 * <td><i>Impossible</i></td>
 * <td>Idle</td>
 * </tr>
 * <tr>
 * <td>Moving</td>
 * <td>Set</td>
 * <td>??</td>
 * <td><i>Illegal</i></td>
 * <td><i>Impossible</i></td>
 * <td><i>Impossible</i>: Door is obstructed</td>
 * <td><i>Impossible</i></td>
 * <td><i>Impossible</i></td>
 * <td><i>Impossible</i></td>
 * </tr>
 * </table>
 *
 * @author Neil McKellar and Chris Dailey
 */
public final class Person extends ModelElement {
	private Floor destination;
	private Location currentLocation;
	private int percentMoved = -1;
	private final Map<DoorSensor.Listener, DoorSensor> sensorListenerMap = new HashMap<>();
	private final Map<Door.Listener, Door> doorListenerMap = new HashMap<>();
	private CarRequestPanel.ArrivalListener arrivalListener;
	private long totalWaitingTime;
	private long startWaitTime = -1;
	private long totalTravelTime;
	private long startTravelTime = -1;

	Person(final EventQueue eQ, final Location startLocation) {
		super(eQ);
		// TODO: Deal with the start location being at capacity.
		movePerson(startLocation);
	}

	public void setDestination(final Floor newDestination) {
		if (newDestination == this.currentLocation) {
			this.destination = null;
			return;
		}
		this.destination = newDestination;
		final int currentFloorNumber = ((Floor) this.currentLocation).getFloorNumber();
		final int destinationFloorNumber = newDestination.getFloorNumber();
		final boolean up = destinationFloorNumber > currentFloorNumber;

		beginWaiting();
		startPayingAttention(up);
		tryToEnterCar(up);
	}

	/**
	 * Check each arriving car, to see if it can bring us in <up> direction Connect
	 * listener to {@link CarRequestPanel.ArrivalListener} and checks each arrival.
	 * Note that we can not then enter right away, as car may be full, people have
	 * to get out, others will want to get in,etc. Therefore an arrival triggers
	 * setup of another listener to wait for our chance.
	 * <p>
	 * Note, this function does NOT let the person press a call button, it only
	 * watches arriving cars and tries to enter them.
	 * </p>
	 *
	 * @param up is true if we want to go up, false if down.
	 */
	private void startPayingAttention(final boolean up) {
		final Floor here = (Floor) this.currentLocation;
		final CarRequestPanel callButton = here.getCallPanel();
		this.arrivalListener = new CarRequestPanel.ArrivalListener() {
			@Override
			public void arrivedUp(final CarEntrance entrance) {
				if (!up) {
					return;
				}
				payAttentionToEntrance(entrance, up);
				tryToEnterCar(up);
			}

			@Override
			public void arrivedDown(final CarEntrance entrance) {
				if (up) {
					return;
				}
				payAttentionToEntrance(entrance, up);
				tryToEnterCar(up);
			}
		};
		callButton.addArrivalListener(this.arrivalListener);

		/** hack to handle already open doors. See #492 */
		for (final Iterator<CarEntrance> i = ((Floor) this.currentLocation).getCarEntrances(); i.hasNext();) {
			final CarEntrance thisEntrance = i.next();
			if (thisEntrance.getDoor().getState() != Door.State.CLOSED
					&& ((thisEntrance.isUp() && up) || (!up && thisEntrance.isDown()))) {
				payAttentionToEntrance(thisEntrance, up);
			}
		}
	}

	/**
	 * Check if there is some car in given direction. This call is normally
	 * triggered by a car arriving on this floor, which is connected to the
	 * {@link CarRequestPanel.ArrivalListener} in {@link #startPayingAttention}
	 *
	 * @param entrance is the car entrance to be checked
	 * @param up       is true if we want up, false if down.
	 */
	private void payAttentionToEntrance(final CarEntrance entrance, final boolean up) {
		final DoorSensor sensor = entrance.getDoorSensor();
		final Door door = entrance.getDoor();
		// pay attention to unobstructed
		final DoorSensor.Listener sensorListener = new DoorSensor.Listener() {
			@Override
			public void sensorCleared() {
			}

			@Override
			public void sensorObstructed() {
			}

			@Override
			public void sensorUnobstructed() {
				tryToEnterCar(up);
			}
		};
		sensor.addListener(sensorListener);
		this.sensorListenerMap.put(sensorListener, sensor);
		// pay attention to doorClosed
		final Door.Listener doorListener = new Door.Listener() {
			@Override
			public void doorOpened() {
				tryToEnterCar(up);
			}

			@Override
			public void doorClosed() {
				// stop paying attention to this entrance
				sensor.removeListener(sensorListener);
				Person.this.sensorListenerMap.remove(sensorListener);
				door.removeListener(this);
				Person.this.doorListenerMap.remove(this);
			}
		};
		door.addListener(doorListener, false);
		this.doorListenerMap.put(doorListener, door);
	}

	private void stopPayingAttention() {
		final Floor here = (Floor) this.currentLocation;
		final CarRequestPanel callButton = here.getCallPanel();
		callButton.removeArrivalListener(this.arrivalListener);
		this.arrivalListener = null;

		for (final Object element : this.sensorListenerMap.keySet()) {
			final DoorSensor.Listener listener = (DoorSensor.Listener) element;
			final DoorSensor sensor = this.sensorListenerMap.get(listener);
			sensor.removeListener(listener);
		}
		this.sensorListenerMap.clear();

		for (final Object element : this.doorListenerMap.keySet()) {
			final Door.Listener listener = (Door.Listener) element;
			final Door door = this.doorListenerMap.get(listener);
			door.removeListener(listener);
		}
		this.doorListenerMap.clear();
	}

	/**
	 * Try to enter the given car. Just fails if there is someone blocking the
	 * entrance, door not open, car full, etc. If there is no car where we can enter
	 * in (maybe after letting some people out), then the call panel is operated to
	 * call a new car.
	 * <p>
	 * The idea is to keep calling this. This is done from
	 * {@link #payAttentionToEntrance(CarEntrance, boolean)} where
	 * {@link DoorSensor.Listener#sensorUnobstructed()} is used to trigger these
	 * calls.
	 * </p>
	 *
	 * @param up the direction we want to travel in. true if we want to go up, false
	 *           if down.
	 */
	private void tryToEnterCar(final boolean up) {
		final Floor here = (Floor) this.currentLocation;
		final CarRequestPanel callButton = here.getCallPanel();
		int numCandidateEntrances = 0;

		for (final Iterator<CarEntrance> i = here.getCarEntrances(); i.hasNext();) {
			final CarEntrance entrance = i.next();
			final Door door = entrance.getDoor();
			final DoorSensor sensor = entrance.getDoorSensor();
			final Car car = (Car) door.getTo();
			// if door is open && sensor is !obstructed && not at capacity
			if (door.getState() != Door.State.CLOSED && sensor.getState() != DoorSensor.State.OBSTRUCTED
					&& !car.isAtCapacity() && !entrance.arePeopleWaitingToGetOut() && up == entrance.isUp()) {
				stopPayingAttention();
				beginEnterCar(entrance);
				return;
			} else if (sensor.getState() == DoorSensor.State.OBSTRUCTED || entrance.arePeopleWaitingToGetOut()) {
				numCandidateEntrances++;
			}
		}

		if (numCandidateEntrances > 0) {
			return;
		}

		if (up && !callButton.isUp()) {
			callButton.pressUp();
		}
		if (!up && !callButton.isDown()) {
			callButton.pressDown();
		}
	}

	/**
	 * Perform the entering of the car by this person. Places the movement event on
	 * the event queue and the action to be performed when the event has been played
	 * out.
	 *
	 * @param entrance is the CarEntrance to take.
	 */
	private void beginEnterCar(final CarEntrance entrance) {
		entrance.getDoorSensor().obstruct();
		final long currentTime = this.eventQueue.getCurrentTime();
		final Event enteringCarEvent = new TrackingUpdateEvent(currentTime, 0.0f, currentTime + 2000, 100.0f) {
			@Override
			public void updateTime() {
				Person.this.percentMoved = (int) currentValue(Person.this.eventQueue.getCurrentTime());
			}

			@Override
			public void perform() {
				Person.this.percentMoved = -1;
				enterCar(entrance);
				entrance.getDoorSensor().unobstruct();
			}
		};
		this.eventQueue.addEvent(enteringCarEvent);
	}

	private void beginWaiting() {
		if (this.startWaitTime == -1) {
			this.startWaitTime = this.eventQueue.getCurrentTime();
		}
	}

	private void endWaiting() {
		if (this.startWaitTime == -1) {
			throw new IllegalStateException("Can't end waiting when not already waiting.");
		}
		this.totalWaitingTime += this.eventQueue.getCurrentTime() - this.startWaitTime;
		this.startWaitTime = -1;
	}

	private void beginTravel() {
		if (this.startTravelTime != -1) {
			throw new IllegalStateException("Can't begin travelling while already travelling");
		}
		this.startTravelTime = this.eventQueue.getCurrentTime();
	}

	private void endTravel() {
		if (this.startTravelTime == -1) {
			throw new IllegalStateException("Can't end travel when not already travelling.");
		}
		this.totalTravelTime += this.eventQueue.getCurrentTime() - this.startTravelTime;
		this.startTravelTime = -1;
	}

	public long getTotalWaitingTime() {
		return this.totalWaitingTime;
	}

	public long getTotalTravelTime() {
		return this.totalTravelTime;
	}

	public long getTotalTime() {
		return this.totalWaitingTime + this.totalTravelTime;
	}

	public Floor getDestination() {
		return this.destination;
	}

	/**
	 * When a person arrives at the destination, this is all the processing that has
	 * to happen.
	 */
	private void leaveCar() {
		endTravel();
	}

	/**
	 * The person enters the car through the specified entrance. This method is
	 * called after the TrackingUpdateEvent that animates moving the person to the
	 * elevator car has completed.
	 *
	 * @param carEntrance The entrance to the car.
	 */
	private void enterCar(final CarEntrance carEntrance) {
		final Door departureDoor = carEntrance.getDoor();
		final Car car = (Car) departureDoor.getTo();

		movePerson(car);

		endWaiting();
		beginTravel();
		car.getFloorRequestPanel().requestFloor(this.destination);
		// setup for getting out of the car
		car.addListener(new Car.Listener() {
			@Override
			public void docked() {
				if (Person.this.destination == car.getLocation()) {
					final Door arrivalDoor = Person.this.destination.getCarEntranceForCar(car).getDoor();
					car.removeListener(this);
					waitForDoorOpen(arrivalDoor);
				}
			}
		});
	}

	/**
	 * Move the person to the specified destination.
	 *
	 * @param destination Where the person moves to.
	 */
	private void movePerson(final Location destination) {
		if (destination == null) {
			throw new IllegalArgumentException("Cannot move to a null destination.");
		}

		if (destination.isAtCapacity()) {
			throw new IllegalStateException("Cannot move person when " + destination.getClass().getSimpleName()
					+ " is at capacity: " + destination.getCapacity() + ".");
		}

		if (this.currentLocation != null) {
			this.currentLocation.personLeaves(this);
		}
		// personEnters() is guaranteed to succeed
		// because capacity was checked above.
		destination.personEnters(this);
		this.currentLocation = destination;
	}

	private void waitForDoorOpen(final Door arrivalDoor) {
		final CarEntrance entrance = this.destination.getCarEntranceForCar(this.currentLocation);
		entrance.waitToEnterDoor(() -> {
			entrance.getDoorSensor().obstruct();
			// TODO: Deal with the floor being at capacity.
			Person.this.percentMoved = 0;
			Person.this.eventQueue.addEvent(new TrackingUpdateEvent(Person.this.eventQueue.getCurrentTime(), 0,
					Person.this.eventQueue.getCurrentTime() + 2000, 100) {
				@Override
				public void updateTime() {
					Person.this.percentMoved = (int) currentValue(Person.this.eventQueue.getCurrentTime());
				}

				@Override
				public void perform() {
					Person.this.percentMoved = -1;
					movePerson(Person.this.destination);
					entrance.getDoorSensor().unobstruct();
					Person.this.destination = null;
				}
			});
		});
		final Door.Listener doorListener = new Door.Listener() {
			@Override
			public void doorOpened() {
				arrivalDoor.removeListener(this);
				leaveCar();
			}

			@Override
			public void doorClosed() {
			}
		};
		arrivalDoor.addListener(doorListener, true);
	}

	public int getPercentMoved() {
		return this.percentMoved;
	}
}