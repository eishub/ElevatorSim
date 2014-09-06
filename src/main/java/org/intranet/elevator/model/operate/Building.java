/*
 * Copyright 2003-2005 Neil McKellar and Chris Dailey
 * All rights reserved.
 */
package org.intranet.elevator.model.operate;

import java.util.ArrayList;
import java.util.Iterator;
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
	private List floors = new ArrayList();
	private List cars = new ArrayList();
	private Controller metaController;

	private List<Person> people = new ArrayList<Person>();

	private Building() {
		super(null);
	}

	public Building(EventQueue eQ, Controller controller) {
		super(eQ);
		metaController = controller;
		controller.initialize(eQ);
	}

	public Building(EventQueue eQ, int numFloors, int numCars, int carCapacity,
			Controller controller) {
		this(eQ, controller);
		createFloors(numFloors);
		createCars(numCars, carCapacity);
	}

	public Building(EventQueue eQ, int numFloors, int numCars,
			Controller controller) {
		this(eQ, numFloors, numCars, 8, controller);
	}

	public void createFloors(int x) {
		for (int i = 0; i < x; i++) {
			// Units are feet in this example.
			final Floor newFloor = new Floor(eventQueue, i + 1, 10 * i, 9);
			floors.add(newFloor);
			height = 10 * (i + 1);

			CarRequest carRequest = new CarRequest(metaController, newFloor);
			newFloor.getCallPanel().addButtonListener(carRequest);
		}
	}

	public void createCars(int x, int capacity) {
		for (int i = 0; i < x; i++) {
			final Car car = new Car(eventQueue, Integer.toString(i), 0.0f,
					capacity);
			cars.add(car);
			metaController.addCar(car, 3.0f);

			// SOON: Move this to Floor or maybe CarEntrance or elsewhere
			car.addListener(new Car.Listener() {
				public void docked() {
					Floor location = car.getLocation();
					final CarEntrance entrance = location
							.getCarEntranceForCar(car);
					final Door door = entrance.getDoor();
					if (door.getState() != Door.State.CLOSED)
						throw new IllegalStateException(
								"How could the door not be closed if we're only now docking with it?");
					door.open();
					// LATER : This relies on the door state not changing
					// directly to CLOSED
					// because we add the listener after the call to open

					final boolean isUp = metaController.arrive(car);

					// set the up/down light on the car entrance
					if (isUp)
						entrance.setUp(true);
					else
						entrance.setDown(true);

					Door.Listener doorListener = new Door.Listener() {
						public void doorOpened() {
						}

						public void doorClosed() {
							door.removeListener(this);
							if (isUp)
								entrance.setUp(false);
							else
								entrance.setDown(false);

							metaController.setNextDestination(car);
							car.undock();
						}
					};
					door.addListener(doorListener, true);
				}
			});

			for (Iterator j = floors.iterator(); j.hasNext();) {
				Floor floor = (Floor) j.next();
				car.getFloorRequestPanel().addServicedFloor(floor);
				floor.createCarEntrance(car);
			}
		}
	}

	public float getHeight() {
		return height;
	}

	public int getNumFloors() {
		return floors.size();
	}

	public int getNumCars() {
		return cars.size();
	}

	public Iterator getFloors() {
		return floors.iterator();
	}

	public Iterator getCars() {
		return cars.iterator();
	}

	public Floor getFloor(int index) {
		return (Floor) floors.get(index);
	}

	/**
	 * create new person starting at given location.
	 * 
	 * @param startLocation
	 *            start location, usually a {@link Floor}
	 * @return new person.
	 */
	public Person createPerson(Location startLocation) {
		Person person = new Person(eventQueue, startLocation);
		people.add(person);
		return person;
	}

	public List getStatistics() {
		// TODO : Update existing tables instead of creating new ones.
		List tables = new ArrayList();
		tables.add(generatePersonTable());
		tables.add(generateCarTable());

		return tables;
	}

	private Table generateCarTable() {
		float[] travelDistances = new float[cars.size()];
		int[] numTravels = new int[cars.size()];

		int carNum = 0;
		String[] carRows = new String[cars.size()];
		for (Iterator carI = cars.iterator(); carI.hasNext(); carNum++) {
			Car car = (Car) carI.next();
			carRows[carNum] = "Car " + (carNum + 1);
			travelDistances[carNum] = car.getTotalDistance();
			numTravels[carNum] = car.getNumTravels();
		}

		Table carTable = new Table(carRows, "Car");
		carTable.addColumn(new FloatColumn("Travel Distances", travelDistances));
		carTable.addColumn(new IntColumn("Number of Stops", numTravels));
		return carTable;
	}

	private Table generatePersonTable() {
		long[] waitingTimes = new long[people.size()];
		long[] travelTimes = new long[people.size()];
		long[] totalTimes = new long[people.size()];

		int personNum = 0;
		String[] peopleRows = new String[people.size()];
		for (Iterator<Person> peopleI = people.iterator(); peopleI.hasNext(); personNum++) {
			Person person = peopleI.next();
			peopleRows[personNum] = "Person " + (personNum + 1);
			waitingTimes[personNum] = person.getTotalWaitingTime();
			travelTimes[personNum] = person.getTotalTravelTime();
			totalTimes[personNum] = person.getTotalTime();
		}
		Table personTable = new Table(peopleRows, "Person");
		personTable.addColumn(new LongColumn("Waiting Time", waitingTimes));
		personTable.addColumn(new LongColumn("Travel Time", travelTimes));
		personTable.addColumn(new LongColumn("Total Time", totalTimes));
		return personTable;
	}
}