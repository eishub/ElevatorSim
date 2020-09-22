/*
 * Copyright 2005 Neil McKellar and Chris Dailey
 * All rights reserved.
 */
package org.intranet.elevator.model.operate.controller;

import java.util.LinkedList;
import java.util.List;

import org.intranet.elevator.model.Car;
import org.intranet.elevator.model.CarRequestPanel;
import org.intranet.elevator.model.Floor;
import org.intranet.sim.event.EventQueue;

/**
 * Goal 1: (Complete) Elevators should stop when there are no car requests and
 * no floor requests.
 *
 * Goal 2: (Incomplete) Optimization - skip unnecessary floors If a button has
 * been pressed, the elevator goes to that floor. If there is a person in the
 * car, then we travel to the appropriate floor.
 *
 * @author Neil McKellar and Chris Dailey
 */
public class SimpleController implements Controller {
	private final List<Car> cars = new LinkedList<>();
	private boolean up = true;
	private boolean carsMoving = false;

	public SimpleController() {
		super();
	}

	@Override
	public void initialize(final EventQueue eQ) {
		this.cars.clear();
		this.carsMoving = false;
		this.up = true;
	}

	@Override
	public void requestCar(final Floor newFloor, final Direction d) {
		moveCars();
	}

	private void moveCars() {
		if (!this.carsMoving) {
			for (final Car car2 : this.cars) {
				final Car car = car2;
				sendToNextFloor(car);
			}
		}
		this.carsMoving = true;
	}

	@Override
	public void addCar(final Car car, final float stoppingDistance) {
		this.cars.add(car);
	}

	// TODO: Reduce code duplication between isUp(), getCurrentIndex(), and
	// sendToNextFloor()
	@Override
	public boolean arrive(final Car car) {
		final List<Floor> floors = car.getFloorRequestPanel().getServicedFloors();
		final int idx = getCurrentIndex(car);
		// At the top floor, go down; at the bottom floor go up
		this.up = (idx == floors.size() - 1) ? false : idx == 0 ? true : this.up;
		return this.up;
	}

	private int getCurrentIndex(final Car car) {
		Floor currentFloor = car.getLocation();
		if (currentFloor == null) {
			currentFloor = car.getFloorAt();
		}
		final List<Floor> floors = car.getFloorRequestPanel().getServicedFloors();
		return floors.indexOf(currentFloor);
	}

	private void sendToNextFloor(final Car car) {
		int idx = getCurrentIndex(car);
		// Next floor depends on the direction
		idx += arrive(car) ? 1 : -1;
		final List<Floor> floors = car.getFloorRequestPanel().getServicedFloors();
		final Floor nextFloor = floors.get(idx);
		car.setDestination(nextFloor);
	}

	@Override
	public String toString() {
		return "SimpleController";
	}

	private void evaluateCarsMoving(final Car car) {
		this.carsMoving = false;
		for (final Floor floor : car.getFloorRequestPanel().getServicedFloors()) {
			final Floor f = floor;
			final CarRequestPanel crp = f.getCallPanel();
			if (crp.isUp() || crp.isDown()) {
				this.carsMoving = true;
				break;
			}
		}
		if (car.getFloorRequestPanel().getRequestedFloors().size() > 0) {
			this.carsMoving = true;
		}
	}

	@Override
	public void setNextDestination(final Car car) {
		evaluateCarsMoving(car);
		// The end-condition of the simulation is roughly here. If
		// carsMoving is false, there will not be new events created
		// for cars. At this point, only events from other sources
		// (either the Simulation itself or a Person) will cause the
		// simulation to continue.
		if (this.carsMoving) {
			sendToNextFloor(car);
		}
	}
}
