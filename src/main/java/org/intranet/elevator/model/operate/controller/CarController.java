/*
 * Copyright 2003-2005 Neil McKellar and Chris Dailey
 * All rights reserved.
 */
package org.intranet.elevator.model.operate.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.intranet.elevator.model.Car;
import org.intranet.elevator.model.CarEntrance;
import org.intranet.elevator.model.Floor;

/**
 * @author Neil McKellar and Chris Dailey SOON : Still confusing, keep
 *         refactoring.
 */
public class CarController {
	private final Car car;
	private final float stoppingDistance;
	private final CarAssignments assignments;

	public CarController(final Car c, final float stoppingDist) {
		super();
		this.car = c;
		this.stoppingDistance = stoppingDist;
		this.assignments = new CarAssignments(this.car.getName());

		this.car.getFloorRequestPanel().addListener(destinationFloor -> {
			final float currentHeight = CarController.this.car.getHeight();
			if (destinationFloor.getHeight() > currentHeight) {
				addDestination(destinationFloor, Direction.UP);
			} else if (destinationFloor.getHeight() < currentHeight) {
				addDestination(destinationFloor, Direction.DOWN);
			} else {
				throw new RuntimeException("Do we really want to go to the current floor?");
			}
		});
	}

	Car getCar() {
		return this.car;
	}

	Floor getDestination() {
		final Assignment current = this.assignments.getCurrentAssignment();
		if (current == null) {
			return null;
		}
		return current.getDestination();
	}

	float getCost(final Floor floor, final Direction destinationDirection) {
		// TODO: In morning simulation, cars get stuck on the top floor
		final Assignment a = new Assignment(floor, destinationDirection);

		if (this.assignments.getCurrentAssignment() == null) {
			// don't care about direction
			final float time = this.car.getTravelTime(floor);
			return time;
		}

		// Don't send another elevator to do the work if this elevator is already
		// doing it.
		if (this.assignments.contains(a)) {
			return 0.0F;
		}

		// factors:
		// 1. how much will it slow down the elevator in processing existing
		// tasks?
		// 2. how long will it take for elevator to arrive *******
		// 3. how does this affect the distribution of elevators in the system?
		// (probably would eventually be in Building)
		float cost = 0.0F;

		// For now, only #2 above is implemented.
		float currentHeight = this.car.getHeight();
		for (final Iterator<Assignment> allDestinations = this.assignments.iteratorIncluding(
				this.car.getFloorRequestPanel().getServicedFloors(), getNearestBase(), a); allDestinations.hasNext();) {
			final Assignment nextAssignment = allDestinations.next();
			final Floor nextDestination = nextAssignment.getDestination();
			final float nextHeight = nextDestination.getHeight();

			// accumulator for number of stops
			cost += floor.getCarEntranceForCar(this.car).getDoor().getMinimumCycleTime();

			// accumulator for total distance
			cost += this.car.getTravelTime(nextHeight - currentHeight);

			currentHeight = nextHeight;
		}

		// all destinations have been accumulated, and we did not add this stop.
		// So now the stop must be added specifically from the last stop.
		cost += this.car.getTravelTime(floor.getHeight() - currentHeight);
		return cost;
	}

	/**
	 * The nearest base is the nearest floor we could reasonably stop at.
	 */
	private Assignment getNearestBase() {
		final Assignment current = this.assignments.getCurrentAssignment();
		final Direction currAssignmentDirection = (current == null) ? Direction.NONE : current.getDirection();

		// The first case is the car is docked
		final Floor carLocation = this.car.getLocation();
		if (carLocation != null) {
			final CarEntrance entrance = carLocation.getCarEntranceForCar(this.car);
			final Direction dockedDirection = entrance.isUp() ? Direction.UP
					: entrance.isDown() ? Direction.DOWN : currAssignmentDirection;
			return new Assignment(carLocation, dockedDirection);
		}

		// The second case is the car is idle
		final Floor f = this.car.getFloorAt();
		if (f != null) {
			return new Assignment(f, currAssignmentDirection);
		}

		// Finally, the third case is the car is travelling
		final float currentHeight = this.car.getHeight();
		final Direction carDirection = (current.getDestination().getHeight() < currentHeight) ? Direction.DOWN
				: Direction.UP;

		final List<Floor> floors = this.car.getFloorRequestPanel().getServicedFloors();
		for (final Iterator<FloorContext> i = createFloorContexts(floors, carDirection); i.hasNext();) {
			final FloorContext context = i.next();
			if (context.contains(currentHeight)) {
				final float distance = Math.abs(context.getNext().getHeight() - currentHeight);
				final boolean canCarStop = distance >= this.stoppingDistance;
				if (canCarStop || context.getNext() == getDestination()) {
					return new Assignment(context.getNext(), carDirection);
				}
				return new Assignment(context.getSuccessor(), carDirection);
			}
		}
		throw new IllegalStateException("The car is somehow not between two floors.");
	}

	/**
	 * @param floors
	 * @param carDirection
	 */
	private Iterator<FloorContext> createFloorContexts(final List<Floor> floors, final Direction carDirection) {
		final List<Floor> sortedFloors = new ArrayList<>(floors);
		Collections.sort(sortedFloors, (arg0, arg1) -> {
			final Floor floor0 = (Floor) arg0;
			final Floor floor1 = (Floor) arg1;
			float difference = floor0.getHeight() - floor1.getHeight();
			if (!carDirection.isUp()) {
				difference = -difference;
			}
			if (difference > 0) {
				return 1;
			}
			if (difference < 0) {
				return -1;
			}
			return 0;
		});
		final List<FloorContext> floorContexts = new LinkedList<>();
		for (int floorNum = 0; floorNum < sortedFloors.size() - 1; floorNum++) {
			final Floor previous = sortedFloors.get(floorNum);
			final Floor next = sortedFloors.get(floorNum + 1);
			Floor successor;
			if (floorNum == sortedFloors.size() - 2) {
				successor = next;
			} else {
				successor = sortedFloors.get(floorNum + 2);
			}
			final FloorContext set = new FloorContext(previous, next, successor);
			floorContexts.add(set);
		}
		return floorContexts.iterator();
	}

	void addDestination(final Floor d, final Direction direction) {
		final Assignment newAssignment = new Assignment(d, direction);
		final Assignment baseAssignment = getNearestBase();

		final List<Floor> floorList = this.car.getFloorRequestPanel().getServicedFloors();
		this.assignments.addAssignment(floorList, baseAssignment, newAssignment);
//  LATER: Can we delete the commented out check for DOCKED in addDestination()?
//    if (car.getState() != Car.State.DOCKED)
		this.car.setDestination(this.assignments.getCurrentAssignment().getDestination());
	}

	public boolean arrive() {
		final Floor location = this.car.getLocation();
		final List<Floor> serviceFloors = this.car.getFloorRequestPanel().getServicedFloors();
		Floor topFloor = serviceFloors.get(serviceFloors.size() - 1);
		Floor bottomFloor = serviceFloors.get(0);

		// remove from up/down list
		final Assignment currentAssignment = this.assignments.getCurrentAssignment();
		this.assignments.removeAssignment(currentAssignment);
		// If the next assignment is on the same floor but going the other way
		// the doors would close and re-open.
		// To prevent this, we can remove that assignment and indicate that
		// we're at the "extreme" position, ready to go the other direction.
		final Assignment newAssignment = this.assignments.getCurrentAssignment();
		if (newAssignment != null && newAssignment.getDestination() == location) {
			this.assignments.removeAssignment(newAssignment);
			if (currentAssignment.getDirection() == Direction.UP) {
				topFloor = location;
			} else {
				bottomFloor = location;
			}
		}
		final boolean wasUp = currentAssignment.getDirection().isUp();
		final boolean atExtreme = (wasUp && location == topFloor) || (!wasUp && location == bottomFloor);
		final boolean isUp = atExtreme ? !wasUp : wasUp;
		return isUp;
	}

	public void setNextDestination() {
		final Assignment current = this.assignments.getCurrentAssignment();
		if (current != null) {
			this.car.setDestination(current.getDestination());
		}
	}
}
