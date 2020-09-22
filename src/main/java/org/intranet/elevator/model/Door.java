/*
* Copyright 2003, 2005 Neil McKellar and Chris Dailey
* All rights reserved.
*/
package org.intranet.elevator.model;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.intranet.sim.ModelElement;
import org.intranet.sim.event.Event;
import org.intranet.sim.event.EventQueue;
import org.intranet.sim.event.TrackingUpdateEvent;

/**
 * A door that opens and closes. Valid states:
 * <table border="1" cellspacing="0" cellpadding="2" summary="">
 * <tr>
 * <th rowspan="2">State</th>
 * <th colspan="1">Variables</th>
 * <th colspan="10">Transitions</th>
 * </tr>
 * <tr>
 * <th>state</th>
 * <th>open()</th>
 * <th>[OpenEvent]</th>
 * <th>close()</th>
 * <th>[CloseEvent]</th>
 * </tr>
 * <tr>
 * <td>CLOSED</td>
 * <td></td>
 * <td>OPENING</td>
 * <td><i>Impossible</i></td>
 * <td><i>Illegal</i></td>
 * <td><i>Impossible</i></td>
 * </tr>
 * <tr>
 * <td>OPENING</td>
 * <td></td>
 * <td><i>Illegal</i></td>
 * <td>opened(): OPENED<br>
 * [doorOpened()]</td>
 * <td><i>Illegal</i></td>
 * <td><i>Impossible</i></td>
 * </tr>
 * <tr>
 * <td>OPENED</td>
 * <td></td>
 * <td><i>Illegal</i></td>
 * <td><i>Impossible</i></td>
 * <td>close(): CLOSING</td>
 * <td><i>Impossible</i></td>
 * </tr>
 * <tr>
 * <td>CLOSING</td>
 * <td></td>
 * <td>OPENING</td>
 * <td><i>Impossible</i></td>
 * <td><i>Illegal</i></td>
 * <td>closed(): CLOSED<br>
 * [doorClosed()]</td>
 * </tr>
 * </table>
 *
 * @author Neil McKellar and Chris Dailey
 */
public class Door extends ModelElement {
	private State state = State.CLOSED;
	/**
	 * Varies from 0 to 100. The door starts closed.
	 */
	private int percentClosed = 100;
	private final Location from;
	private final Location to;
	private final List<Listener> listeners = new LinkedList<>();
	private final List<Listener> priorityListeners = new LinkedList<>();
	private Event event;
	private static final long CLOSE_TIME = 2000;
	private static final long CLOSE_WAIT_TIME = 3000;

	public static final class State {
		private final String name;

		private State(final String value) {
			this.name = value;
		}

		public static final State OPENED = new State("Opened");
		public static final State OPENING = new State("Opening");
		public static final State CLOSED = new State("Closed");
		public static final State CLOSING = new State("Closing");

		@Override
		public String toString() {
			return this.name;
		}
	}

	public interface Listener {
		void doorOpened();

		void doorClosed();
	}

	private class OpenEvent extends TrackingUpdateEvent {
		public OpenEvent() {
			super(Door.this.eventQueue.getCurrentTime(), Door.this.percentClosed,
					Door.this.eventQueue.getCurrentTime() + (long) (Door.this.percentClosed / 100F * CLOSE_TIME), 0);
		}

		@Override
		public void updateTime() {
			Door.this.percentClosed = (int) currentValue(Door.this.eventQueue.getCurrentTime());
		}

		@Override
		public void perform() {
			Door.this.event = null;
			Door.this.percentClosed = 0;
			opened();
		}
	}

	private class CloseEvent extends TrackingUpdateEvent {
		public CloseEvent() {
			super(Door.this.eventQueue.getCurrentTime(), Door.this.percentClosed, Door.this.eventQueue.getCurrentTime()
					+ (long) ((100 - Door.this.percentClosed) / 100F * CLOSE_TIME), 100);
		}

		@Override
		public void updateTime() {
			Door.this.percentClosed = (int) currentValue(Door.this.eventQueue.getCurrentTime());
		}

		@Override
		public void perform() {
			Door.this.event = null;
			Door.this.percentClosed = 100;
			closed();
		}
	}

	Door(final EventQueue eQ, final Location fromLocation, final Location toLocation) {
		super(eQ);
		this.from = fromLocation;
		this.to = toLocation;
	}

	public State getState() {
		return this.state;
	}

	private void setState(final State state) {
		this.state = state;
	}

	/**
	 * Returns the percentage the door is closed in its current state.
	 *
	 * @return An int between 0 and 100.
	 */
	public int getPercentClosed() {
		return this.percentClosed;
	}

	public Location getFrom() {
		return this.from;
	}

	public Location getTo() {
		return this.to;
	}

	public boolean isOpen() {
		return (this.percentClosed == 0);
	}

	public void open() {
		if (this.state == State.OPENED || this.state == State.OPENING) {
			throw new IllegalStateException();
		}
		if (this.state == State.CLOSING) {
			this.eventQueue.removeEvent(this.event);
			this.event = null;
		}
		setState(State.OPENING);
		// The starting percentClosed is how much we have left to open
		if (this.event != null) {
			throw new IllegalStateException("Already handling an event!");
		}
		this.event = new OpenEvent();
		this.eventQueue.addEvent(this.event);
	}

	void close() {
		if (this.state == State.CLOSED || this.state == State.CLOSING || this.state == State.OPENING) {
			throw new IllegalStateException("Can't close while in " + this.state);
		}
		setState(State.CLOSING);
		if (this.event != null) {
			throw new IllegalStateException("Already handling an event!");
		}
		this.event = new CloseEvent();
		this.eventQueue.addEvent(this.event);
	}

	private void closed() {
		setState(State.CLOSED);
		this.percentClosed = 100;
		// Notification occurs with the high priority listeners first.
		final List<Listener> listenersCopy = new ArrayList<>(this.priorityListeners);
		listenersCopy.addAll(this.listeners);
		for (final Listener listener : listenersCopy) {
			final Listener l = listener;
			l.doorClosed();
		}
	}

	private void opened() {
		setState(State.OPENED);
		this.percentClosed = 0;
		final List<Listener> listenersCopy = new ArrayList<>(this.priorityListeners);
		listenersCopy.addAll(this.listeners);
		for (final Listener listener : listenersCopy) {
			final Listener l = listener;
			l.doorOpened();
		}
		if (this.event != null) {
			throw new IllegalStateException("Already handling an event!");
		}
	}

	public void addListener(final Listener l, final boolean highPriority) {
		if (highPriority) {
			this.priorityListeners.add(l);
		} else {
			this.listeners.add(l);
		}
	}

	public void removeListener(final Listener l) {
		this.listeners.remove(l);
		this.priorityListeners.remove(l);
	}

	public long getMinimumCycleTime() {
		return 2 * CLOSE_TIME + CLOSE_WAIT_TIME;
	}
}