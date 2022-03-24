/*
 * Copyright 2005 Neil McKellar and Chris Dailey
 * All rights reserved.
 */
package org.intranet.elevator.model.operate.controller;

import java.util.ArrayList;
import java.util.List;

import org.intranet.elevator.model.Car;
import org.intranet.elevator.model.Floor;
import org.intranet.sim.event.EventQueue;

/**
 * @author Neil McKellar and Chris Dailey
 */
public class MetaController implements Controller {
	private final List<CarController> carControllers = new ArrayList<>();

	@Override
	public void initialize(final EventQueue eQ) {
		this.carControllers.clear();
	}

	@Override
	public void addCar(final Car car, final float stoppingDistance) {
		final CarController controller = new CarController(car, stoppingDistance);
		this.carControllers.add(controller);
	}

	@Override
	public void requestCar(final Floor newFloor, final Direction d) {
		final CarController controller = findBestCar(newFloor, d);
		controller.addDestination(newFloor, d);
	}

	private CarController findBestCar(final Floor floor, final Direction direction) {
		// if only one car, duh
		if (this.carControllers.size() == 1) {
			return this.carControllers.get(0);
		}

		CarController c = null;
		float lowestCost = Float.MAX_VALUE;
		for (final Object element : this.carControllers) {
			final CarController controller = (CarController) element;
			final float cost = controller.getCost(floor, direction);
			if (cost < lowestCost) {
				c = controller;
				lowestCost = cost;
			} else // Previously, the simulation simply collected statistics.
			// With the addition of this comparison, the statistics being gathered
			// are affecting the outcome of the simulation.
			if ((cost == lowestCost) && (controller.getCar().getTotalDistance() < c.getCar().getTotalDistance())) {
				c = controller;
			}
		}

		return c;
	}

	@Override
	public String toString() {
		return "Default MetaController";
	}

	@Override
	public boolean arrive(final Car car) {
		final CarController c = getController(car);
		return c.arrive();
	}

	@Override
	public void setNextDestination(final Car car) {
		final CarController c = getController(car);
		c.setNextDestination();
	}

	private CarController getController(final Car car) {
		CarController c = null;
		for (final Object element : this.carControllers) {
			final CarController controller = (CarController) element;
			if (controller.getCar() == car) {
				c = controller;
			}
		}
		return c;
	}
}
