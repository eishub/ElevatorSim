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

	private Map sensorListenerMap = new HashMap();
	private Map doorListenerMap = new HashMap();
	private CarRequestPanel.ArrivalListener arrivalListener;

	private long totalWaitingTime;
	private long startWaitTime = -1;
	private long totalTravelTime;
	private long startTravelTime = -1;

	Person(EventQueue eQ, Location startLocation) {
		super(eQ);
		// TODO: Deal with the start location being at capacity.
		movePerson(startLocation);
	}

	public void setDestination(Floor newDestination) {
		if (newDestination == currentLocation) {
			destination = null;
			return;
		}
		destination = newDestination;
		int currentFloorNumber = ((Floor) currentLocation).getFloorNumber();
		int destinationFloorNumber = newDestination.getFloorNumber();
		boolean up = destinationFloorNumber > currentFloorNumber;

		beginWaiting();
		startPayingAttention(up);
		tryToEnterCar(up);
	}

	/**
	 * Check each arriving car, to see if it can bring us in <up> direction
	 * Connect listener to {@link CarRequestPanel.ArrivalListener} and checks
	 * each arrival. Note that we can not then enter right away, as car may be
	 * full, people have to get out, others will want to get in,etc. Therefore
	 * an arrival triggers setup of another listener to wait for our chance.
	 * <p>
	 * Note, this function does NOT let the person press a call button, it only
	 * watches arriving cars and tries to enter them.
	 * </p>
	 * 
	 * @param up
	 *            is true if we want to go up, false if down.
	 */
	private void startPayingAttention(final boolean up) {
		Floor here = (Floor) currentLocation;
		final CarRequestPanel callButton = here.getCallPanel();
		arrivalListener = new CarRequestPanel.ArrivalListener() {
			public void arrivedUp(CarEntrance entrance) {
				if (!up)
					return;
				payAttentionToEntrance(entrance, up);
				tryToEnterCar(up);
			}

			public void arrivedDown(CarEntrance entrance) {
				if (up)
					return;
				payAttentionToEntrance(entrance, up);
				tryToEnterCar(up);
			}
		};
		callButton.addArrivalListener(arrivalListener);

		/** hack to handle already open doors. See #492 */
		for (Iterator i = ((Floor) currentLocation).getCarEntrances(); i
				.hasNext();) {
			CarEntrance thisEntrance = (CarEntrance) i.next();
			if (thisEntrance.getDoor().getState() != Door.State.CLOSED
					&& ((thisEntrance.isUp() && up) || (!up && thisEntrance
							.isDown()))) {
				payAttentionToEntrance(thisEntrance, up);
			}
		}
	}

	/**
	 * Check if there is some car in given direction. This call is normally
	 * triggered by a car arriving on this floor, which is connected to the
	 * {@link CarRequestPanel.ArrivalListener} in {@link #startPayingAttention}
	 * 
	 * @param entrance
	 *            is the car entrance to be checked
	 * @param up
	 *            is true if we want up, false if down.
	 */
	private void payAttentionToEntrance(CarEntrance entrance, final boolean up) {
		final DoorSensor sensor = entrance.getDoorSensor();
		final Door door = entrance.getDoor();
		// pay attention to unobstructed
		final DoorSensor.Listener sensorListener = new DoorSensor.Listener() {
			public void sensorCleared() {
			}

			public void sensorObstructed() {
			}

			public void sensorUnobstructed() {
				tryToEnterCar(up);
			}
		};
		sensor.addListener(sensorListener);
		sensorListenerMap.put(sensorListener, sensor);
		// pay attention to doorClosed
		Door.Listener doorListener = new Door.Listener() {
			public void doorOpened() {
				tryToEnterCar(up);
			}

			public void doorClosed() {
				// stop paying attention to this entrance
				sensor.removeListener(sensorListener);
				sensorListenerMap.remove(sensorListener);
				door.removeListener(this);
				doorListenerMap.remove(this);
			}
		};
		door.addListener(doorListener, false);
		doorListenerMap.put(doorListener, door);
	}

	private void stopPayingAttention() {
		Floor here = (Floor) currentLocation;
		final CarRequestPanel callButton = here.getCallPanel();
		callButton.removeArrivalListener(arrivalListener);
		arrivalListener = null;

		for (Iterator i = sensorListenerMap.keySet().iterator(); i.hasNext();) {
			DoorSensor.Listener listener = (DoorSensor.Listener) i.next();
			DoorSensor sensor = (DoorSensor) sensorListenerMap.get(listener);
			sensor.removeListener(listener);
		}
		sensorListenerMap.clear();

		for (Iterator i = doorListenerMap.keySet().iterator(); i.hasNext();) {
			Door.Listener listener = (Door.Listener) i.next();
			Door door = (Door) doorListenerMap.get(listener);
			door.removeListener(listener);
		}
		doorListenerMap.clear();
	}

	/**
	 * Try to enter the given car. Just fails if there is someone blocking the
	 * entrance, door not open, car full, etc. If there is no car where we can
	 * enter in (maybe after letting some people out), then the call panel is
	 * operated to call a new car.
	 * <p>
	 * The idea is to keep calling this. This is done from
	 * {@link #payAttentionToEntrance(CarEntrance, boolean)} where
	 * {@link DoorSensor.Listener#sensorUnobstructed()} is used to trigger these
	 * calls.
	 * </p>
	 * 
	 * @param up
	 *            the direction we want to travel in. true if we want to go up,
	 *            false if down.
	 */
	private void tryToEnterCar(boolean up) {
		Floor here = (Floor) currentLocation;
		final CarRequestPanel callButton = here.getCallPanel();
		int numCandidateEntrances = 0;

		for (Iterator i = here.getCarEntrances(); i.hasNext();) {
			CarEntrance entrance = (CarEntrance) i.next();
			Door door = entrance.getDoor();
			DoorSensor sensor = entrance.getDoorSensor();
			Car car = (Car) door.getTo();
			// if door is open && sensor is !obstructed && not at capacity
			if (door.getState() != Door.State.CLOSED
					&& sensor.getState() != DoorSensor.State.OBSTRUCTED
					&& !car.isAtCapacity()
					&& !entrance.arePeopleWaitingToGetOut()
					&& up == entrance.isUp()) {
				stopPayingAttention();
				beginEnterCar(entrance);
				return;
			} else if (sensor.getState() == DoorSensor.State.OBSTRUCTED
					|| entrance.arePeopleWaitingToGetOut()) {
				numCandidateEntrances++;
			}
		}

		if (numCandidateEntrances > 0)
			return;

		if (up && !callButton.isUp())
			callButton.pressUp();
		if (!up && !callButton.isDown())
			callButton.pressDown();
	}

	/**
	 * Perform the entering of the car by this person. Places the movement event
	 * on the event queue and the action to be performed when the event has been
	 * played out.
	 * 
	 * @param entrance
	 *            is the CarEntrance to take.
	 */
	private void beginEnterCar(final CarEntrance entrance) {
		entrance.getDoorSensor().obstruct();
		long currentTime = eventQueue.getCurrentTime();
		Event enteringCarEvent = new TrackingUpdateEvent(currentTime, 0.0f,
				currentTime + 2000, 100.0f) {
			public void updateTime() {
				percentMoved = (int) currentValue(eventQueue.getCurrentTime());
			}

			public void perform() {
				percentMoved = -1;
				enterCar(entrance);
				entrance.getDoorSensor().unobstruct();
			}
		};
		eventQueue.addEvent(enteringCarEvent);
	}

	private void beginWaiting() {
		if (startWaitTime == -1)
			startWaitTime = eventQueue.getCurrentTime();
	}

	private void endWaiting() {
		if (startWaitTime == -1)
			throw new IllegalStateException(
					"Can't end waiting when not already waiting.");
		totalWaitingTime += eventQueue.getCurrentTime() - startWaitTime;
		startWaitTime = -1;
	}

	private void beginTravel() {
		if (startTravelTime != -1)
			throw new IllegalStateException(
					"Can't begin travelling while already travelling");
		startTravelTime = eventQueue.getCurrentTime();
	}

	private void endTravel() {
		if (startTravelTime == -1)
			throw new IllegalStateException(
					"Can't end travel when not already travelling.");
		totalTravelTime += eventQueue.getCurrentTime() - startTravelTime;
		startTravelTime = -1;
	}

	public long getTotalWaitingTime() {
		return totalWaitingTime;
	}

	public long getTotalTravelTime() {
		return totalTravelTime;
	}

	public long getTotalTime() {
		return totalWaitingTime + totalTravelTime;
	}

	public Floor getDestination() {
		return destination;
	}

	/**
	 * When a person arrives at the destination, this is all the processing that
	 * has to happen.
	 */
	private void leaveCar() {
		endTravel();
	}

	/**
	 * The person enters the car through the specified entrance. This method is
	 * called after the TrackingUpdateEvent that animates moving the person to
	 * the elevator car has completed.
	 * 
	 * @param carEntrance
	 *            The entrance to the car.
	 */
	private void enterCar(CarEntrance carEntrance) {
		final Door departureDoor = carEntrance.getDoor();
		final Car car = (Car) departureDoor.getTo();

		movePerson(car);

		endWaiting();
		beginTravel();
		car.getFloorRequestPanel().requestFloor(destination);
		// setup for getting out of the car
		car.addListener(new Car.Listener() {
			public void docked() {
				if (destination == car.getLocation()) {
					final Door arrivalDoor = destination.getCarEntranceForCar(
							car).getDoor();
					car.removeListener(this);
					waitForDoorOpen(arrivalDoor);
				}
			}
		});
	}

	/**
	 * Move the person to the specified destination.
	 * 
	 * @param destination
	 *            Where the person moves to.
	 */
	private void movePerson(Location destination) {
		if (destination == null)
			throw new IllegalArgumentException(
					"Cannot move to a null destination.");

		if (destination.isAtCapacity())
			throw new IllegalStateException("Cannot move person when "
					+ destination.getClass().getSimpleName()
					+ " is at capacity: " + destination.getCapacity() + ".");

		if (currentLocation != null)
			currentLocation.personLeaves(this);
		// personEnters() is guaranteed to succeed
		// because capacity was checked above.
		destination.personEnters(this);
		currentLocation = destination;
	}

	private void waitForDoorOpen(final Door arrivalDoor) {
		final CarEntrance entrance = destination
				.getCarEntranceForCar(currentLocation);
		entrance.waitToEnterDoor(new CarEntrance.DoorWaitListener() {
			public void doorAvailable() {
				entrance.getDoorSensor().obstruct();
				// TODO: Deal with the floor being at capacity.
				percentMoved = 0;
				eventQueue.addEvent(new TrackingUpdateEvent(eventQueue
						.getCurrentTime(), 0,
						eventQueue.getCurrentTime() + 2000, 100) {
					public void updateTime() {
						percentMoved = (int) currentValue(eventQueue
								.getCurrentTime());
					}

					public void perform() {
						percentMoved = -1;
						movePerson(destination);
						entrance.getDoorSensor().unobstruct();
						destination = null;
					}
				});
			}
		});
		Door.Listener doorListener = new Door.Listener() {
			public void doorOpened() {
				arrivalDoor.removeListener(this);
				leaveCar();
			}

			public void doorClosed() {
			}
		};
		arrivalDoor.addListener(doorListener, true);
	}

	public int getPercentMoved() {
		return percentMoved;
	}
}