/*
 * Copyright 2005 Neil McKellar and Chris Dailey
 * All rights reserved.
 */
package org.intranet.elevator.model.operate.controller;

import org.intranet.elevator.model.Car;
import org.intranet.elevator.model.Floor;
import org.intranet.sim.event.EventQueue;

/**
 * @author Neil McKellar and Chris Dailey
 * @author W.Pasman added documentation.
 */
public interface Controller {
	/**
	 * called when controller is initialized, just before it is inserted into the
	 * {@link org.intranet.elevator.model.operate.Building}.
	 *
	 * @param eQ the event queue that the Controller works on.
	 */
	void initialize(EventQueue eQ);

	/**
	 * called when some person wants a car to get to another floor
	 *
	 * @param newFloor is the pickup floor of the person
	 * @param d        is the direction that the person wants to travel in
	 */
	void requestCar(Floor newFloor, Direction d);

	/**
	 * called by the {@link org.intranet.elevator.model.operate.Building} to add
	 * extra cars.
	 *
	 * @param car              is the car to be inserted
	 * @param stoppingDistance UNKNOWN what this is but usually 3.0
	 */
	void addCar(Car car, float stoppingDistance);

	/**
	 * To be called only once when a car arrives at a location. This is done by the
	 * {@link org.intranet.elevator.model.operate.Building}. Allows the controller
	 * to update any internal data structures that were keeping track of where the
	 * car was going to.
	 *
	 * @param car The car that is arriving.
	 * @return Whether the car is going up after this arrival. Can be used to set
	 *         the direction light on the entrance.
	 */
	boolean arrive(Car car);

	/**
	 * Called by the {@link org.intranet.elevator.model.operate.Building} when the
	 * door was closed. Gives the controller the hint that it is time to set a
	 * target floor.
	 *
	 * @param car is the car that has closed doors now.
	 */
	void setNextDestination(Car car);
}
