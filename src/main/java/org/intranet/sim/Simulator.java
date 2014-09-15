/*
 * Copyright 2003 Neil McKellar and Chris Dailey
 * All rights reserved.
 */
package org.intranet.sim;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.prefs.Preferences;

import org.intranet.elevator.model.operate.controller.Controller;
import org.intranet.sim.clock.Clock;
import org.intranet.sim.clock.ClockFactory;
import org.intranet.sim.event.EventQueue;
import org.intranet.ui.FloatParameter;
import org.intranet.ui.IntegerParameter;
import org.intranet.ui.LongParameter;
import org.intranet.ui.Parameter;
import org.intranet.ui.SingleValueParameter;

/**
 * @author Neil McKellar and Chris Dailey
 * 
 */
public abstract class Simulator {
	static public Preferences simulatorprefs = Preferences
			.userNodeForPackage(Simulator.class);

	private ClockFactory clockFactory;
	private boolean initialized;
	// TODO : Write tests
	private EventQueue eventQueue;
	private Clock clock;
	protected List<Parameter> parameters = new ArrayList<Parameter>();
	private List listeners = new ArrayList();
	Clock.FeedbackListener cc = new Clock.FeedbackListener() {
		public long timeUpdate(long time) {
			synchronized (getModel()) {
				try {
					if (eventQueue.processEventsUpTo(time)) {
						for (Iterator i = listeners.iterator(); i.hasNext();) {
							SimulatorListener l = (SimulatorListener) i.next();
							l.modelUpdate(time);
						}
					}
					// Wouter: removed code that stopped sim after last event.
					// We never know now when to stop. GOAL may always call
					// another goto()
					return time;
				} catch (Exception e) {
					// left in place, this may occur if something is not thread
					// safe, #1340
					System.out
							.println("Warning: Elevator environment unexpected exception in timeUpdate:"
									+ e);
					e.printStackTrace();
					return time;
				}
			}
		}
	};

	public interface SimulatorListener {
		void modelUpdate(long time);
	}

	public final void addListener(SimulatorListener sl) {
		listeners.add(sl);
	}

	public final void removeListener(SimulatorListener sl) {
		listeners.remove(sl);
	}

	/**
	 * Initialize simulator.
	 */
	protected Simulator() {
		super();
	}

	/**
	 * get the preferred settings.
	 * 
	 * @return Preferences object containing all preferred settings.
	 */
	public Preferences getPreferences() {
		return simulatorprefs;
	}

	public final EventQueue getEventQueue() {
		if (eventQueue == null)
			throw new IllegalStateException(
					"initialize() must be called before eventQueue is valid");
		return eventQueue;
	}

	public final Clock getClock() {
		if (clock == null)
			throw new IllegalStateException(
					"initialize() must be called before clock is valid");
		return clock;
	}

	public final void initialize(ClockFactory cf) {
		// Stop the clock, we're starting over
		if (clock != null)
			clock.dispose();

		clockFactory = cf;
		eventQueue = new EventQueue();
		clock = clockFactory.createClock(cc);
		initializeModel();
		initialized = true;
	}

	public final boolean isInitializied() {
		return initialized;
	}

	protected abstract void initializeModel();

	public abstract Model getModel();

	public final SingleValueParameter getParameter(String description) {
		for (Iterator i = getParameters().iterator(); i.hasNext();) {
			SingleValueParameter p = (SingleValueParameter) i.next();
			if (p.getDescription().equals(description))
				return p;
		}
		return null;
	}

	public abstract String getDescription();

	public final List<Parameter> getParameters() {
		return parameters;
	}

	public abstract Simulator duplicate();

	/**
	 * get the current controller associated with this Simulator. Added
	 * W.Pasman, 4nov2010, see #711
	 * 
	 * @return currently selected controller. null if no such controller
	 *         available (yet)
	 */
	public abstract Controller getCurrentController();

	/**
	 * get all parameters of this simulator. This function can be used only
	 * after parameters have been added, which usually happens in the
	 * constructor
	 * 
	 * @return the set of the names of the parameters that this simulator uses.
	 */
	public ArrayList<Simulator.Keys> getParameterKeys() {
		ArrayList<Simulator.Keys> names = new ArrayList<Simulator.Keys>();
		for (Parameter p : parameters) {
			names.add(p.getKey());
		}
		return names;
	}

	/**
	 * Look up the preferred controller name in the preferences and finding that
	 * name in the given controller list.
	 * 
	 * @param the_controllers
	 *            the list of available controllers
	 * @return the preferred controller.
	 */
	public Controller preferredController(ArrayList<Controller> the_controllers) {
		String controllername = simulatorprefs.get(
				Simulator.Keys.CONTROLLER.toString(), "");
		for (Controller c : the_controllers) {
			if (c.toString().equals(controllername)) {
				return c;
			}
		}
		System.out.println("WARNING: ElevatorSimulator: unknown controller "
				+ controllername);
		return the_controllers.get(0); // default
	}

	/**
	 * Keys to store the simulator specific values.
	 * 
	 * @author wouter
	 * 
	 */
	public static enum Keys {
		/**
		 * The simulation, eg
		 * "Random Rider Insertion. This string should exactly match the text in the "
		 * Select a Simulation" GUI.
		 */
		SIMULATION("Simulation", "Random Rider Insertion"),
		/**
		 * Number of floors
		 */
		FLOORS("Number of floors", 10),
		/**
		 * Number of Cars
		 */
		CARS("Number of Cars", 3),
		/**
		 * Elevator Capacity
		 */
		CAPACITY("Elevator Capacity", 8),
		/**
		 * Number of People
		 */
		PEOPLE("Number of People", 20),
		/**
		 * Number of people per floor
		 */
		NPEOPLEPERFLOOR("Number of People per Floor", 20),
		/**
		 * Rider insertion time (ms)
		 */
		INSERTIONTIME("Rider insertion time (ms)", 50000l),
		/**
		 * Rider insertion time (hours)
		 */
		INSERTIONTIMEHR("Rider insertion time (hours)", 1.0f),
		/**
		 * Up destination
		 */
		UPDESTINATION("Up Destination", 3),
		/**
		 * down destination
		 */
		DOWNDESTINATION("Down Destination", 2),
		/**
		 * Standard deviation
		 */
		STANDARDDEV("Standard Deviation", 1),
		/**
		 * Random seed
		 */
		RANDOMSEED("Random seed", 635359l),
		/**
		 * insert second request at
		 */
		INSERT2NDREQAT("Insert second request at", 29000),
		/**
		 * Controller - the code that controls the behaviour of the cars.
		 */
		CONTROLLER("Controller", "EIS Controller"),
		/**
		 * Time factor, to speed up the simtime 2^n compared to real time.
		 */
		TIMEFACTOR("Time Factor", 0);

		private String description;
		private Object defaultValue;

		private Keys(String desc, Object defvalue) {
			description = desc;
			defaultValue = defvalue;
		}

		public String getDescription() {
			return description;
		}

		/**
		 * get the default value for this key and put it in a FloatParameter .
		 * Only usable for keys that have Float default value. do not use this
		 * for CONTROLLER or SIMULATION that require a combo box.
		 * 
		 * @return {@link Parameter} containing default setting for the given
		 *         key.
		 * @throws ClassCastException
		 *             if you apply this to key with non-Float default value.
		 */
		public FloatParameter getDefaultFloatParameter() {
			return new FloatParameter(this, simulatorprefs.getFloat(
					this.toString(), (Float) defaultValue));
		}

		/**
		 * get the default value for this key and put it in a IntegerParameter .
		 * Only usable for keys that have Integer default value. do not use this
		 * for CONTROLLER or SIMULATION that require a combo box.
		 * 
		 * @return {@link Parameter} containing default setting for the given
		 *         key.
		 * @throws ClassCastException
		 *             if you apply this to key with non-Integer default value.
		 */

		public IntegerParameter getDefaultIntegerParameter() {
			return new IntegerParameter(this, simulatorprefs.getInt(
					this.toString(), (Integer) defaultValue));

		}

		/**
		 * get the default value for this key and put it in a LongParameter .
		 * Only usable for keys that have Long default value. do not use this
		 * for CONTROLLER or SIMULATION that require a combo box.
		 * 
		 * @return {@link Parameter} containing default setting for the given
		 *         key.
		 * @throws ClassCastException
		 *             if you apply this to key with non-Integer default value.
		 */

		public LongParameter getDefaultLongParameter() {
			return new LongParameter(this, simulatorprefs.getLong(
					this.toString(), (Long) defaultValue));

		}

		/**
		 * get Key with given keyname.
		 * 
		 * @param keyname
		 *            is name for which you want the associated Key
		 * @return Key with the given name, or null if no such key.
		 */
		public Keys getKey(String keyname) {
			for (Keys key : Keys.values()) {
				if (key.getDescription().equals(keyname)) {
					return key;
				}
			}
			return null;
		}

		/**
		 * save preferred value for this key. Value should match the original
		 * defaultValue, but if defaultValue is Float or Double, value can be
		 * Float or Double, and when defaultValue is Integer or Long, value can
		 * be Integer or Long.
		 * 
		 * @param value
		 *            is preferred value. Integer, Float, Double, or String.
		 * 
		 * @throws IllegalArgumentException
		 *             if the type of given value does not match the
		 *             defaultValue.
		 */
		public void setPreference(Object value) {
			if ((defaultValue instanceof Integer || defaultValue instanceof Long)
					&& value instanceof Integer) {
				simulatorprefs.putInt(this.toString(), (Integer) value);
			} else if ((defaultValue instanceof Integer || defaultValue instanceof Long)
					&& value instanceof Long) {
				simulatorprefs.putLong(this.toString(), (Long) value);
			} else if ((defaultValue instanceof Double || defaultValue instanceof Float)
					&& (value instanceof Float)) {
				simulatorprefs.putFloat(this.toString(), (Float) value);
			} else if ((defaultValue instanceof Double || defaultValue instanceof Float)
					&& (value instanceof Double)) {
				simulatorprefs.putDouble(this.toString(), (Double) value);
			} else if (value instanceof String
					&& defaultValue instanceof String) {
				simulatorprefs.put(this.toString(), (String) value);
			} else {
				throw new IllegalArgumentException("Preference for " + this
						+ " (" + description + ") is supposed to be of "
						+ defaultValue.getClass() + " but received " + value
						+ " of " + value.getClass());
			}
		}
	}

}
