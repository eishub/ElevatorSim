/*
 * Copyright 2003 Neil McKellar and Chris Dailey
 * All rights reserved.
 */
package org.intranet.sim;

import java.util.ArrayList;
import java.util.LinkedList;
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
 */
public abstract class Simulator {
	public static Preferences simulatorprefs = Preferences.userNodeForPackage(Simulator.class);

	private ClockFactory clockFactory;
	private boolean initialized;
	private EventQueue eventQueue;
	private Clock clock;
	protected List<Parameter> parameters = new LinkedList<>();
	private final List<Object> listeners = new LinkedList<>();
	private final Clock.FeedbackListener cc = time -> {
		synchronized (getModel()) {
			try {
				if (Simulator.this.eventQueue.processEventsUpTo(time)) {
					for (final Object element : Simulator.this.listeners) {
						final SimulatorListener l = (SimulatorListener) element;
						l.modelUpdate(time);
					}
				}
				// Wouter: removed code that stopped sim after last event.
				// We never know now when to stop. GOAL may always call
				// another goto()
				return time;
			} catch (final Exception e) {
				// left in place, this may occur if something is not thread
				// safe, #1340
				System.out.println("Warning: Elevator environment unexpected exception in timeUpdate:" + e);
				e.printStackTrace();
				return time;
			}
		}
	};

	public interface SimulatorListener {
		void modelUpdate(long time);
	}

	public final void addListener(final SimulatorListener sl) {
		this.listeners.add(sl);
	}

	public final void removeListener(final SimulatorListener sl) {
		this.listeners.remove(sl);
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
		if (this.eventQueue == null) {
			throw new IllegalStateException("initialize() must be called before eventQueue is valid");
		}
		return this.eventQueue;
	}

	public final Clock getClock() {
		if (this.clock == null) {
			throw new IllegalStateException("initialize() must be called before clock is valid");
		}
		return this.clock;
	}

	public final void initialize(final ClockFactory cf) {
		// Stop the clock, we're starting over
		if (this.clock != null) {
			this.clock.dispose();
		}

		this.clockFactory = cf;
		this.eventQueue = new EventQueue();
		this.clock = this.clockFactory.createClock(this.cc);
		initializeModel();
		this.initialized = true;
	}

	public final boolean isInitializied() {
		return this.initialized;
	}

	protected abstract void initializeModel();

	public abstract Model getModel();

	public final SingleValueParameter getParameter(final String description) {
		for (final Object element : getParameters()) {
			final SingleValueParameter p = (SingleValueParameter) element;
			if (p.getDescription().equals(description)) {
				return p;
			}
		}
		return null;
	}

	public abstract String getDescription();

	public final List<Parameter> getParameters() {
		return this.parameters;
	}

	public abstract Simulator duplicate();

	/**
	 * get the current controller associated with this Simulator. Added W.Pasman,
	 * 4nov2010, see #711
	 *
	 * @return currently selected controller. null if no such controller available
	 *         (yet)
	 */
	public abstract Controller getCurrentController();

	/**
	 * get all parameters of this simulator. This function can be used only after
	 * parameters have been added, which usually happens in the constructor
	 *
	 * @return the set of the names of the parameters that this simulator uses.
	 */
	public ArrayList<Simulator.Keys> getParameterKeys() {
		final ArrayList<Simulator.Keys> names = new ArrayList<>();
		for (final Parameter p : this.parameters) {
			names.add(p.getKey());
		}
		return names;
	}

	/**
	 * Look up the preferred controller name in the preferences and finding that
	 * name in the given controller list.
	 *
	 * @param the_controllers the list of available controllers
	 * @return the preferred controller.
	 */
	public Controller preferredController(final List<Controller> the_controllers) {
		final String controllername = simulatorprefs.get(Simulator.Keys.CONTROLLER.toString(), "");
		for (final Controller c : the_controllers) {
			if (c.toString().equals(controllername)) {
				return c;
			}
		}
		System.out.println("WARNING: ElevatorSimulator: unknown controller " + controllername);
		return the_controllers.get(0); // default
	}

	/**
	 * Keys to store the simulator specific values.
	 *
	 * @author wouter
	 *
	 */
	public enum Keys {
		/**
		 * The simulation, eg "Random Rider Insertion. This string should exactly match
		 * the text in the " Select a Simulation" GUI.
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
		INSERTIONTIME("Rider insertion time (ms)", 50000L),
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
		RANDOMSEED("Random seed", 635359L),
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

		private Keys(final String desc, final Object defvalue) {
			this.description = desc;
			this.defaultValue = defvalue;
		}

		public String getDescription() {
			return this.description;
		}

		/**
		 * get the default value for this key and put it in a FloatParameter . Only
		 * usable for keys that have Float default value. do not use this for CONTROLLER
		 * or SIMULATION that require a combo box.
		 *
		 * @return {@link Parameter} containing default setting for the given key.
		 * @throws ClassCastException if you apply this to key with non-Float default
		 *                            value.
		 */
		public FloatParameter getDefaultFloatParameter() {
			return new FloatParameter(this, simulatorprefs.getFloat(toString(), (Float) this.defaultValue));
		}

		/**
		 * get the default value for this key and put it in a IntegerParameter . Only
		 * usable for keys that have Integer default value. do not use this for
		 * CONTROLLER or SIMULATION that require a combo box.
		 *
		 * @return {@link Parameter} containing default setting for the given key.
		 * @throws ClassCastException if you apply this to key with non-Integer default
		 *                            value.
		 */

		public IntegerParameter getDefaultIntegerParameter() {
			return new IntegerParameter(this, simulatorprefs.getInt(toString(), (Integer) this.defaultValue));

		}

		/**
		 * get the default value for this key and put it in a LongParameter . Only
		 * usable for keys that have Long default value. do not use this for CONTROLLER
		 * or SIMULATION that require a combo box.
		 *
		 * @return {@link Parameter} containing default setting for the given key.
		 * @throws ClassCastException if you apply this to key with non-Integer default
		 *                            value.
		 */

		public LongParameter getDefaultLongParameter() {
			return new LongParameter(this, simulatorprefs.getLong(toString(), (Long) this.defaultValue));

		}

		/**
		 * get Key with given keyname.
		 *
		 * @param keyname is name for which you want the associated Key
		 * @return Key with the given name, or null if no such key.
		 */
		public Keys getKey(final String keyname) {
			for (final Keys key : Keys.values()) {
				if (key.getDescription().equals(keyname)) {
					return key;
				}
			}
			return null;
		}

		/**
		 * save preferred value for this key. Value should match the original
		 * defaultValue, but if defaultValue is Float or Double, value can be Float or
		 * Double, and when defaultValue is Integer or Long, value can be Integer or
		 * Long.
		 *
		 * @param value is preferred value. Integer, Float, Double, or String.
		 *
		 * @throws IllegalArgumentException if the type of given value does not match
		 *                                  the defaultValue.
		 */
		public void setPreference(final Object value) {
			if ((this.defaultValue instanceof Integer || this.defaultValue instanceof Long)
					&& value instanceof Integer) {
				simulatorprefs.putInt(toString(), (Integer) value);
			} else if ((this.defaultValue instanceof Integer || this.defaultValue instanceof Long)
					&& value instanceof Long) {
				simulatorprefs.putLong(toString(), (Long) value);
			} else if ((this.defaultValue instanceof Double || this.defaultValue instanceof Float)
					&& (value instanceof Float)) {
				simulatorprefs.putFloat(toString(), (Float) value);
			} else if ((this.defaultValue instanceof Double || this.defaultValue instanceof Float)
					&& (value instanceof Double)) {
				simulatorprefs.putDouble(toString(), (Double) value);
			} else if (value instanceof String && this.defaultValue instanceof String) {
				simulatorprefs.put(toString(), (String) value);
			} else {
				throw new IllegalArgumentException(
						"Preference for " + this + " (" + this.description + ") is supposed to be of "
								+ this.defaultValue.getClass() + " but received " + value + " of " + value.getClass());
			}
		}
	}
}
