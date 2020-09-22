/*
 * Copyright 2003,2005 Neil McKellar and Chris Dailey
 * All rights reserved.
 */
package org.intranet.elevator.model;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.intranet.sim.ModelElement;
import org.intranet.sim.event.Event;
import org.intranet.sim.event.EventQueue;

/**
 * Deals with obstructions. The state transitions look like this:
 * <table border="1" cellspacing="0" cellpadding="2" summary="">
 * <tr>
 * <th rowspan="2">State</th>
 * <th colspan="1">Variables</th>
 * <th colspan="10">Transitions</th>
 * </tr>
 * <tr>
 * <th>state</th>
 * <th>obstruct()</th>
 * <th>unobstruct()</th>
 * <th>[ClearEvent]</th>
 * </tr>
 * <tr>
 * <td>CLEAR</td>
 * <td></td>
 * <td>OBSTRUCTED<br>
 * [sensorObstructed()]</td>
 * <td>UNOBSTRUCTED</td>
 * <td><i>Impossible</i></td>
 * </tr>
 * <tr>
 * <td>OBSTRUCTED</td>
 * <td></td>
 * <td><i>Illegal</i></td>
 * <td>UNOBSTRUCTED</td>
 * <td><i>Impossible</i></td>
 * </tr>
 * <tr>
 * <td>UNOBSTRUCTED</td>
 * <td></td>
 * <td>OBSTRUCTED<br>
 * [sensorObstructed()]</td>
 * <td><i>Illegal</i></td>
 * <td>CLEAR<br>
 * [sensorCleared()]</td>
 * </tr>
 * </table>
 *
 * @author Neil McKellar and Chris Dailey
 */
public class DoorSensor extends ModelElement {
	private State state = State.CLEAR;
	private Event clearEvent = null;
	private final List<Listener> listeners = new LinkedList<>();

	private final class ClearEvent extends Event {
		private ClearEvent(final long newTime) {
			super(newTime);
		}

		@Override
		public void perform() {
			clear();
			DoorSensor.this.clearEvent = null;
		}
	}

	public interface Listener {
		void sensorCleared();

		void sensorObstructed();

		void sensorUnobstructed();
	}

	public static class State {
		private final String name;

		private State(final String state) {
			this.name = state;
		}

		/**
		 * The sensor is unobstructed and the door is available to be closed. The only
		 * way out of this state is to obstruct().
		 */
		public static final State CLEAR = new State("CLEAR");
		/**
		 * Someone is obstructing the way. The door cannot be closed. The only way out
		 * of this state is to unobstruct().
		 */
		public static final State OBSTRUCTED = new State("OBSTRUCTED");
		/**
		 * There is noone in the way of the door, and after a timeout the state will
		 * change to CLEAR unless another obstruct() occurs.
		 */
		public static final State UNOBSTRUCTED = new State("UNOBSTRUCTED");

		@Override
		public String toString() {
			return this.name;
		}
	}

	public DoorSensor(final EventQueue eQ) {
		super(eQ);
	}

	public State getState() {
		return this.state;
	}

	public void obstruct() {
		if (this.state == State.OBSTRUCTED) {
			throw new IllegalStateException("Can't reobstruct");
		}
		if (this.state == State.UNOBSTRUCTED) {
			// cancel the pending clearEvent, the door sensor is obstructed again
			this.eventQueue.removeEvent(this.clearEvent);
			this.clearEvent = null;
		}
		this.state = State.OBSTRUCTED;
		for (final Listener listener : this.listeners) {
			final Listener l = listener;
			l.sensorObstructed();
		}
	}

	public void unobstruct() {
		if (this.state == State.UNOBSTRUCTED) {
			throw new IllegalStateException("Can't unobstruct unless obstructed, state");
		}
		this.state = State.UNOBSTRUCTED;
		this.clearEvent = new ClearEvent(this.eventQueue.getCurrentTime() + 3000);
		this.eventQueue.addEvent(this.clearEvent);

		for (final Listener listener : new ArrayList<>(this.listeners)) {
			final Listener l = listener;
			l.sensorUnobstructed();
		}
	}

	private void clear() {
		this.state = State.CLEAR;
		for (final Listener listener : this.listeners) {
			final Listener l = listener;
			l.sensorCleared();
		}
	}

	public void addListener(final Listener l) {
		this.listeners.add(l);
	}

	public void removeListener(final Listener l) {
		this.listeners.remove(l);
	}
}
