/**
 * Copyright 2008 W.Pasman
 * @author W.Pasman 18dec08
 * All rights reserved.
 */
package elevatorenv;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.intranet.elevator.model.Car;
import org.intranet.elevator.model.CarEntrance;
import org.intranet.elevator.model.Floor;
import org.intranet.elevator.model.operate.controller.Controller;
import org.intranet.elevator.model.operate.controller.Direction;
import org.intranet.sim.event.EventQueue;

import eis.exceptions.EntityException;
import eis.iilang.Identifier;
import eis.iilang.Numeral;
import eis.iilang.Percept;

/**
 * Glue between elevator simulator and GOAL To use this, select the
 * "GOALController" from the controller list. You can NOT directly use this as
 * environment though, because the controller is just a sub-module of the
 * elevator simulator. Instead you need to use the ElevatorEnv object which will
 * initialize the elevator simulator and hook up this controller (but only if
 * you select it for use) To use it, see ElevatorEnv. The lowest floor is number
 * 1.
 * 
 * @author W.Pasman
 * @author KH
 * @author W.Pasman nov2010 moved out of the original code base, fixed docu
 */
public class GOALController implements Controller {

	EventQueue evtQueue; // used to get time stamps.
	private List<Car> cars = new ArrayList<Car>();

	/**
	 * The direction light of a car, to be shown when the car arrives and door
	 * opens. true means up, false means down. The String is the car name, as
	 * returned by car.getName().
	 **/
	Hashtable<String, Boolean> nextDirOfCar = new Hashtable<String, Boolean>();

	/**
	 * For each car a list of percepts is buffered. (String is the car name, as
	 * returned by car.getName().) The percepts are created when an elevator
	 * event occurs, and are buffered here until the agent handling this car
	 * asks for its percepts. removed trac715 One problem with polling is that
	 * elevator seems not entirely thread safe. Let's see how serious that is...
	 * In theory the worst case would be a wrong signal (eg button on while it
	 * is in fact off) But Java might throw a ConcurrentModificationException if
	 * it takes this too seriously...
	 * 
	 **/
	EnvironmentInterface env = null;

	/**
	 * send floorCount percept only once. Here we keep the elevator names that
	 * we sent the percept to.
	 */
	private ArrayList<String> floorCountSentTo = new ArrayList<String>();

	/**
	 * Create new GOAL controller.
	 * 
	 * @param theenv
	 *            is the link to the environment interface, allows us to do
	 *            callbacks and pass percept information
	 */
	public GOALController(EnvironmentInterface theenv) {
		super();
		env = theenv;
	}

	/**
	 * get a car given a name.
	 * 
	 * @param carname
	 *            is the name of the car
	 * @return car with given name, or null if no such car
	 */
	public Car getCar(String carname) {
		for (Car car : cars) {
			if (carname.equals(car.getName()))
				return car;
		}
		return null;
	}

	/***********************************/
	/***** IMPLEMENTS CONTROLLER *******/
	/***********************************/
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void requestCar(Floor newFloor, Direction d) {
		// TODO make a nice event to sent to all agents.
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void initialize(EventQueue eQ) {
		evtQueue = eQ;
		env.deleteCars();
		cars.clear();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addCar(final Car car, float stoppingDistance) {
		cars.add(car);
		nextDirOfCar.put(car.getName(), true);
		try {
			env.newEntity(car.getName(), "car");
		} catch (EntityException e) {
			System.out.println("[GOAL Controller] " + e.getMessage());
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean arrive(Car car) {
		return nextDirOfCar.get(car.getName());
	}

	/**
	 * simple toString, used for creation of the menu item
	 */
	public String toString() {
		return "GOAL Controller";
	}

	/***********************************/
	/** GOAL Environment support */
	/***********************************/
	/**
	 * execute a goto action and light up the floor lights after arriving. If
	 * the up sign is lighted, only people wanting to go up will enter the
	 * elevator. Similarly if the down sign is lighted.
	 * 
	 * @param carname
	 *            is the name of the car performing the goto
	 * @param floor
	 *            is the target floor. Ground floor = 1.
	 * @param dir
	 *            is the direction light to turn on after the car has arrived.
	 *            if Dir="up", the elevator will light the "up" sign when it
	 *            arrived at N. Otherwise it will light the "down" sign.
	 * 
	 * @throws java.lang.IllegalArgumentException
	 *             if you give incorrect arguments to your command.
	 */

	public void executeGoto(String carname, int floor, String dir)
			throws IllegalArgumentException {
		if (!(dir.equals("up") || dir.equals("down")))
			throw new IllegalArgumentException("dir should be 'up' or 'down'");
		Car car = getCar(carname);
		if (car == null)
			throw new IllegalArgumentException("unknown car " + carname);
		List<Floor> floors = car.getFloorRequestPanel().getServicedFloors();
		if (floor < 1 || floor > floors.size())
			throw new IllegalArgumentException("floor " + floor
					+ " does not exist");

		Floor nextFloor = floors.get(floor - 1);
		nextDirOfCar.put(carname, dir.equals("up"));
		car.setDestination(nextFloor);
	}

	/***********************************/
	/******** Percept Handling *********/
	/***********************************/

	Hashtable<String, String> lastDoorState = new Hashtable<String, String>();

	// this list contains the entity names that already received the capacity of
	// the elevator.
	ArrayList<String> sentCapacity = new ArrayList<String>();

	// the last reported number of people in the elevator.
	Hashtable<String, Integer> lastNumPeople = new Hashtable<String, Integer>();

	/**
	 * create list of current percepts that can be passed through EIS. Floor
	 * numbers are integers [1,...].
	 * <p>
	 * Percepts that are generated:</p>
	 * <ul>
	 * <li>doorState(X) indicates that the car's doors are opening, open,
	 * closed, closing. Only sent on change.</li>
	 * <li>atFloor(L) indicates that the car is at floor number L. Only sent
	 * while at a floor.</li>
	 * <li>fButtonOn(L,Dir). Indicates that a person waiting on floor L pressed
	 * the 'up' or 'down' button D.</li>
	 * <li>eButtonOn(L). Indicates that a person in the car pressed the floor
	 * button L.</li>
	 * <li>people(N). Indicates that there are N people in the car now. This
	 * percept is sent only when N changes.</li>
	 * <li>capacity(N). Indicates that the car can hold at most N people. This
	 * percept is sent only 1 time at start.</li>
	 * </ul>
	 *
	 * 
	 * @return list of EIS percepts.
	 * @param carname
	 *            is the name of the car in the elevator simulator (typicall
	 *            same as entity name but without the leading "car").
	 * @param entity
	 *            is the name of the car in EIS.
	 * @param entity
	 *            is the current time factor of the realtime clock.
	 */
	public synchronized LinkedList<Percept> sendPercepts(String carname,
			String entity, int timefactor) {
		Car car = getCar(carname);
		Percept percept;
		String newDoorState;

		LinkedList<Percept> percepts = new LinkedList<Percept>();
		// figure out which floor we are. see also TRAC #715 and #1334

		// there are two ways to get floor. Sometimes one fails. Check why?
		Floor floor = car.getLocation();
		if (floor == null)
			floor = car.getFloorAt();

		// at a floor? Then give atFloor and doorState
		if (floor != null) {
			percept = new Percept("atFloor",
					new Numeral(floor.getFloorNumber()));
			percept.setSource(entity);
			percepts.add(percept);

			CarEntrance entrance = floor.getCarEntranceForCar(car);
			newDoorState = entrance.getDoor().getState().toString()
					.toLowerCase();
		} else {
			newDoorState = "closed";
		}

		String oldState = lastDoorState.get(entity);
		if (oldState == null || !oldState.equals(newDoorState)) {
			// update needed.
			percepts.add(new Percept("doorState", new Identifier(newDoorState)));
			lastDoorState.put(entity, newDoorState);
		}

		// HACK see #1357.
		percepts.add(new Percept("carPosition", new Numeral(1. + car
				.getHeight() / 10.)));

		// see also #767 and #763

		// find out which buttons are on now.
		for (Floor f : car.getFloorRequestPanel().getServicedFloors()) {
			if (f.getCallPanel().isUp()) {
				percept = new Percept("fButtonOn", new Numeral(
						f.getFloorNumber()), new Identifier("up"));
				percept.setSource(entity);
				percepts.add(percept);
			}
			if (f.getCallPanel().isDown()) {
				percept = new Percept("fButtonOn", new Numeral(
						f.getFloorNumber()), new Identifier("down"));
				percept.setSource(entity);
				percepts.add(percept);
			}
		}

		for (Floor f : car.getFloorRequestPanel().getRequestedFloors()) {
			percept = new Percept("eButtonOn", new Numeral(f.getFloorNumber()));
			percept.setSource(entity);
			percepts.add(percept);
		}

		if (!sentCapacity.contains(entity)) {
			sentCapacity.add(entity);
			percept = new Percept("capacity", new Numeral(car.getCapacity()));
			percept.setSource(entity);
			percepts.add(percept);
		}

		// count people now in elevator. Annoying but we can't just get the list
		Integer people = 0;
		Iterator peopleiterator = car.getPeople();
		while (peopleiterator.hasNext()) {
			people++;
			peopleiterator.next();
		}
		// update needed?
		Integer oldNumPeople = lastNumPeople.get(entity);
		if (oldNumPeople == null || !(oldNumPeople.equals(people))) {
			// update needed.
			percepts.add(new Percept("people", new Numeral(people)));
			lastNumPeople.put(entity, people);
		}

		if (!floorCountSentTo.contains(carname)) {
			// the num of floors = the number of buttons in the car, right?
			int n = car.getFloorRequestPanel().getServicedFloors().size();
			percepts.add(new Percept("floorCount", new Numeral(n)));
			floorCountSentTo.add(carname);
		}

		// the current time factor of the simulator #1357
		percepts.add(new Percept("timefactor", new Numeral(timefactor)));

		return percepts;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setNextDestination(Car car) {
		// We do not generate an event here since we already
		// have the doorState() percept.
		// But we could create an EIS event here.
	}

}
