/*
 * Copyright 2003-2005 Neil McKellar and Chris Dailey
 * All rights reserved.
 */
package org.intranet.elevator.model.operate;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.intranet.elevator.model.Car;
import org.intranet.elevator.model.CarEntrance;
import org.intranet.elevator.model.Door;
import org.intranet.elevator.model.Floor;
import org.intranet.elevator.model.Location;
import org.intranet.elevator.model.operate.controller.Controller;
import org.intranet.sim.Model;
import org.intranet.sim.event.EventQueue;
import org.intranet.statistics.FloatColumn;
import org.intranet.statistics.IntColumn;
import org.intranet.statistics.LongColumn;
import org.intranet.statistics.Table;

/**
 * The building is a factory for other domain objects that also links up
 * CarRequests with the RequestIndicators for each Floor.
 *
 * @author Neil McKellar and Chris Dailey
 */
public class Building extends Model {
	private float height; // external height of building
	private final List<Floor> floors = new LinkedList<>();
	private final List<Car> cars = new LinkedList<>();
	private Controller metaController;
	private final List<Person> people = new LinkedList<>();

	private Building() {
		super(null);
	}

	public Building(final EventQueue eQ, final Controller controller) {
		super(eQ);
		this.metaController = controller;
		controller.initialize(eQ);
	}

	public Building(final EventQueue eQ, final int numFloors, final int numCars, final int carCapacity,
			final Controller controller) {
		this(eQ, controller);
		createFloors(numFloors);
		createCars(numCars, carCapacity);
	}

	public Building(final EventQueue eQ, final int numFloors, final int numCars, final Controller controller) {
		this(eQ, numFloors, numCars, 8, controller);
	}

	public void createFloors(final int x) {
		for (int i = 0; i < x; i++) {
			// Units are feet in this example.
			final Floor newFloor = new Floor(this.eventQueue, i + 1, 10 * i, 9);
			this.floors.add(newFloor);
			this.height = 10 * (i + 1);

			final CarRequest carRequest = new CarRequest(this.metaController, newFloor);
			newFloor.getCallPanel().addButtonListener(carRequest);
		}
	}

	public void createCars(final int x, final int capacity) {
		for (int i = 0; i < x; i++) {
			final Car car = new Car(this.eventQueue, Integer.toString(i), 0.0f, capacity);
			this.cars.add(car);
			this.metaController.addCar(car, 3.0f);

			// SOON: Move this to Floor or maybe CarEntrance or elsewhere
			car.addListener(() -> {
				final Floor location = car.getLocation();
				final CarEntrance entrance = location.getCarEntranceForCar(car);
				final Door door = entrance.getDoor();
				if (door.getState() != Door.State.CLOSED) {
					throw new IllegalStateException(
							"How could the door not be closed if we're only now docking with it?");
				}
				door.open();
				// LATER : This relies on the door state not changing
				// directly to CLOSED
				// because we add the listener after the call to open

				final boolean isUp = Building.this.metaController.arrive(car);

				// set the up/down light on the car entrance
				if (isUp) {
					entrance.setUp(true);
				} else {
					entrance.setDown(true);
				}

				final Door.Listener doorListener = new Door.Listener() {
					@Override
					public void doorOpened() {
					}

					@Override
					public void doorClosed() {
						door.removeListener(this);
						if (isUp) {
							entrance.setUp(false);
						} else {
							entrance.setDown(false);
						}

						Building.this.metaController.setNextDestination(car);
						car.undock();
					}
				};
				door.addListener(doorListener, true);
			});

			for (final Floor floor2 : this.floors) {
				final Floor floor = floor2;
				car.getFloorRequestPanel().addServicedFloor(floor);
				floor.createCarEntrance(car);
			}
		}
	}

	public float getHeight() {
		return this.height;
	}

	public int getNumFloors() {
		return this.floors.size();
	}

	public int getNumCars() {
		return this.cars.size();
	}

	public Iterator<Floor> getFloors() {
		return this.floors.iterator();
	}

	public Iterator<Car> getCars() {
		return this.cars.iterator();
	}

	public Floor getFloor(final int index) {
		return this.floors.get(index);
	}

	/**
	 * create new person starting at given location.
	 *
	 * @param startLocation start location, usually a {@link Floor}
	 * @return new person.
	 */
	public Person createPerson(final Location startLocation) {
		final Person person = new Person(this.eventQueue, startLocation);
		this.people.add(person);
		return person;
	}

	@Override
	public List<Table> getStatistics() {
		// TODO : Update existing tables instead of creating new ones.
		final List<Table> tables = new ArrayList<>(2);
		tables.add(generatePersonTable());
		tables.add(generateCarTable());

		return tables;
	}

	private Table generateCarTable() {
		final float[] travelDistances = new float[this.cars.size()];
		final int[] numTravels = new int[this.cars.size()];

		int carNum = 0;
		final String[] carRows = new String[this.cars.size()];
		for (final Iterator<Car> carI = this.cars.iterator(); carI.hasNext(); carNum++) {
			final Car car = carI.next();
			carRows[carNum] = "Car " + (carNum + 1);
			travelDistances[carNum] = car.getTotalDistance();
			numTravels[carNum] = car.getNumTravels();
		}

		final Table carTable = new Table(carRows, "Car");
		carTable.addColumn(new FloatColumn("Travel Distances", travelDistances));
		carTable.addColumn(new IntColumn("Number of Stops", numTravels));
		return carTable;
	}

	private Table generatePersonTable() {
		final long[] waitingTimes = new long[this.people.size()];
		final long[] travelTimes = new long[this.people.size()];
		final long[] totalTimes = new long[this.people.size()];

		int personNum = 0;
		final String[] peopleRows = new String[this.people.size()];
		for (final Iterator<Person> peopleI = this.people.iterator(); peopleI.hasNext(); personNum++) {
			final Person person = peopleI.next();
			peopleRows[personNum] = "Person " + (personNum + 1);
			waitingTimes[personNum] = person.getTotalWaitingTime();
			travelTimes[personNum] = person.getTotalTravelTime();
			totalTimes[personNum] = person.getTotalTime();
		}
		final Table personTable = new Table(peopleRows, "Person");
		personTable.addColumn(new LongColumn("Waiting Time", waitingTimes));
		personTable.addColumn(new LongColumn("Travel Time", travelTimes));
		personTable.addColumn(new LongColumn("Total Time", totalTimes));
		return personTable;
	}
}