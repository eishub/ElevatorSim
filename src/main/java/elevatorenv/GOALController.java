/**
 * Copyright 2008 W.Pasman
 * @author W.Pasman 18dec08
 * All rights reserved.
 */
package elevatorenv;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.intranet.elevator.model.Car;
import org.intranet.elevator.model.CarEntrance;
import org.intranet.elevator.model.Floor;
import org.intranet.elevator.model.operate.Person;
import org.intranet.elevator.model.operate.controller.Controller;
import org.intranet.elevator.model.operate.controller.Direction;
import org.intranet.sim.event.Event;
import org.intranet.sim.event.EventQueue;
import org.intranet.sim.event.NewDestinationEvent;

import eis.PerceptUpdate;
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
	private EventQueue evtQueue; // used to get time stamps.
	private final List<Car> cars = new ArrayList<>();

	/**
	 * The direction light of a car, to be shown when the car arrives and door
	 * opens. true means up, false means down. The String is the car name, as
	 * returned by car.getName().
	 **/
	private final Map<String, Boolean> nextDirOfCar = new HashMap<>();

	/**
	 * For each car a list of percepts is buffered. (String is the car name, as
	 * returned by car.getName().) The percepts are created when an elevator event
	 * occurs, and are buffered here until the agent handling this car asks for its
	 * percepts. removed trac715 One problem with polling is that elevator seems not
	 * entirely thread safe. Let's see how serious that is... In theory the worst
	 * case would be a wrong signal (eg button on while it is in fact off) But Java
	 * might throw a ConcurrentModificationException if it takes this too
	 * seriously...
	 *
	 **/
	private final EnvironmentInterface env;

	/**
	 * Create new GOAL controller.
	 *
	 * @param theenv is the link to the environment interface, allows us to do
	 *               callbacks and pass percept information
	 */
	public GOALController(final EnvironmentInterface theenv) {
		this.env = theenv;
	}

	/**
	 * get a car given a name.
	 *
	 * @param carname is the name of the car
	 * @return car with given name, or null if no such car
	 */
	public Car getCar(final String carname) {
		for (final Car car : this.cars) {
			if (carname.equals(car.getName())) {
				return car;
			}
		}
		return null;
	}

	/**
	 * Get available cars.
	 *
	 * @return available cars
	 */
	public List<Car> getCars() {
		return this.cars;
	}

	/***********************************/
	/***** IMPLEMENTS CONTROLLER *******/
	/***********************************/
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void requestCar(final Floor newFloor, final Direction d) {
		// TODO make a nice event to sent to all agents.
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void initialize(final EventQueue eQ) {
		this.evtQueue = eQ;
		this.env.deleteCars();
		this.cars.clear();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void addCar(final Car car, final float stoppingDistance) {
		this.cars.add(car);
		this.nextDirOfCar.put(car.getName(), true);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean arrive(final Car car) {
		return this.nextDirOfCar.get(car.getName());
	}

	/**
	 * simple toString, used for creation of the menu item
	 */
	@Override
	public String toString() {
		return "EIS Controller";
	}

	/***********************************/
	/** GOAL Environment support */
	/***********************************/
	/**
	 * execute a goto action and light up the floor lights after arriving. If the up
	 * sign is lighted, only people wanting to go up will enter the elevator.
	 * Similarly if the down sign is lighted.
	 *
	 * @param carname is the name of the car performing the goto
	 * @param floor   is the target floor. Ground floor = 1.
	 * @param dir     is the direction light to turn on after the car has arrived.
	 *                if Dir="up", the elevator will light the "up" sign when it
	 *                arrived at N. Otherwise it will light the "down" sign.
	 *
	 * @throws java.lang.IllegalArgumentException if you give incorrect arguments to
	 *                                            your command.
	 */

	public void executeGoto(final String carname, final int floor, final String dir) throws IllegalArgumentException {
		if (!(dir.equals("up") || dir.equals("down"))) {
			throw new IllegalArgumentException("dir should be 'up' or 'down'");
		}
		final Car car = getCar(carname);
		if (car == null) {
			throw new IllegalArgumentException("unknown car " + carname);
		}
		final List<Floor> floors = car.getFloorRequestPanel().getServicedFloors();
		if (floor < 1 || floor > floors.size()) {
			throw new IllegalArgumentException("floor " + floor + " does not exist");
		}

		final Floor nextFloor = floors.get(floor - 1);
		this.nextDirOfCar.put(carname, dir.equals("up"));

		final Event event = new NewDestinationEvent(car, nextFloor, 0);
		this.evtQueue.insertEvent(event);
		// car.setDestination(nextFloor);
	}

	/***********************************/
	/******** Percept Handling *********/
	/***********************************/

	private final Map<String, List<Percept>> previousPercepts = new HashMap<>();

	private final Map<String, Identifier> lastDoorState = new HashMap<>();
	private final Map<String, Identifier> lastDoorStateSent = new HashMap<>();
	private final Map<String, Numeral> lastNumPeople = new HashMap<>();
	private final Map<String, Numeral> lastNumPeopleSent = new HashMap<>();
	private final Set<String> sentInitial = new HashSet<>();

	/**
	 * create list of current percepts that can be passed through EIS. Floor numbers
	 * are integers [1,...].
	 * <p>
	 * Percepts that are generated:
	 * </p>
	 * <ul>
	 * <li>doorState(X) indicates that the car's doors are opening, open, closed,
	 * closing. Only sent on change.</li>
	 * <li>atFloor(L) indicates that the car is at floor number L. Only sent while
	 * at a floor.</li>
	 * <li>fButtonOn(L,Dir). Indicates that a person waiting on floor L pressed the
	 * 'up' or 'down' button D.</li>
	 * <li>eButtonOn(L). Indicates that a person in the car pressed the floor button
	 * L.</li>
	 * <li>people(N). Indicates that there are N people in the car now. This percept
	 * is sent only when N changes.</li>
	 * <li>capacity(N). Indicates that the car can hold at most N people. This
	 * percept is sent only 1 time at start.</li>
	 * </ul>
	 *
	 *
	 * @return list of EIS percepts.
	 * @param carname    is the name of the car in the elevator simulator (typically
	 *                   the same as entity name but without the leading "car").
	 * @param entity     is the name of the car in EIS.
	 * @param timefactor is the current time factor of the realtime clock.
	 */
	public synchronized PerceptUpdate sendPercepts(final String carname, final String entity, final int timefactor) {
		final Car car = getCar(carname);

		final List<Percept> percepts = new ArrayList<>();
		final List<Percept> addList = new ArrayList<>();
		final List<Percept> delList = new ArrayList<>();

		if (!this.sentInitial.contains(entity)) {
			percepts.add(new Percept("capacity", new Numeral(car.getCapacity())));
			final int n = car.getFloorRequestPanel().getServicedFloors().size();
			percepts.add(new Percept("floorCount", new Numeral(n)));
			this.sentInitial.add(entity);
		}

		// figure out which floor we are. see also TRAC #715 and #1334
		// there are two ways to get floor. Sometimes one fails. Check why?
		Floor floor = car.getLocation();
		if (floor == null) {
			floor = car.getFloorAt();
		}

		// at a floor? Then give atFloor and doorState
		Identifier newDoorState;
		if (floor == null) {
			newDoorState = new Identifier("closed");
		} else {
			final CarEntrance entrance = floor.getCarEntranceForCar(car);
			newDoorState = new Identifier(entrance.getDoor().getState().toString().toLowerCase());

			percepts.add(new Percept("atFloor", new Numeral(floor.getFloorNumber())));
		}

		final Identifier oldDoorState = this.lastDoorState.get(entity);
		if (newDoorState.equals(oldDoorState)) {
			final Identifier sentDoorState = this.lastDoorStateSent.get(entity);
			if (sentDoorState != null) {
				delList.add(new Percept("doorState", sentDoorState));
				this.lastDoorStateSent.remove(entity);
			}
		} else {
			if (oldDoorState != null) {
				delList.add(new Percept("doorState", oldDoorState));
			}
			addList.add(new Percept("doorState", newDoorState));
			this.lastDoorState.put(entity, newDoorState);
			this.lastDoorStateSent.put(entity, newDoorState);
		}

		// HACK see #1357.
		percepts.add(new Percept("carPosition", new Numeral(1. + car.getHeight() / 10.)));

		// see also #767 and #763

		// find out which buttons are on now.
		for (final Floor f : car.getFloorRequestPanel().getServicedFloors()) {
			if (f.getCallPanel().isUp()) {
				percepts.add(new Percept("fButtonOn", new Numeral(f.getFloorNumber()), new Identifier("up")));
			}
			if (f.getCallPanel().isDown()) {
				percepts.add(new Percept("fButtonOn", new Numeral(f.getFloorNumber()), new Identifier("down")));
			}
		}

		for (final Floor f : car.getFloorRequestPanel().getRequestedFloors()) {
			percepts.add(new Percept("eButtonOn", new Numeral(f.getFloorNumber())));
		}

		// count people now in elevator. Annoying but we can't just get the list
		Integer people = 0;
		final Iterator<Person> peopleiterator = car.getPeople();
		while (peopleiterator.hasNext()) {
			people++;
			peopleiterator.next();
		}
		final Numeral newNumPeople = new Numeral(people);
		// update needed?
		final Numeral oldNumPeople = this.lastNumPeople.get(entity);
		if (newNumPeople.equals(oldNumPeople)) {
			final Numeral sentNumPeople = this.lastNumPeopleSent.get(entity);
			if (sentNumPeople != null) {
				delList.add(new Percept("people", sentNumPeople));
				this.lastNumPeopleSent.remove(entity);
			}
		} else {
			if (oldNumPeople != null) {
				delList.add(new Percept("people", oldNumPeople));
			}
			addList.add(new Percept("people", newNumPeople));
			this.lastNumPeople.put(entity, newNumPeople);
			this.lastNumPeopleSent.put(entity, newNumPeople);
		}

		// the current time factor of the simulator #1357
		percepts.add(new Percept("timefactor", new Numeral(timefactor)));

		// construct and return the PerceptUpdate
		List<Percept> previous = this.previousPercepts.get(entity);
		if (previous == null) {
			previous = new ArrayList<>(0);
		}
		addList.addAll(percepts);
		addList.removeAll(previous);
		delList.addAll(previous);
		delList.removeAll(percepts);
		this.previousPercepts.put(entity, percepts);

		return new PerceptUpdate(addList, delList);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setNextDestination(final Car car) {
		// We do not generate an event here since we already
		// have the doorState() percept.
		// But we could create an EIS event here.
	}
}
