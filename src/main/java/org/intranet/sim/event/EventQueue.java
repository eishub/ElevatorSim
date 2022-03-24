/*
 * Copyright 2003 Neil McKellar and Chris Dailey
 * All rights reserved.
 */
package org.intranet.sim.event;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import org.intranet.sim.clock.RealTimeClock;

/**
 * A set containing future {@link Event}s. These events are actually happening
 * when {@link #processEventsUpTo(long)} is called from the
 * {@link RealTimeClock}. All elevator actions go through this queue. This
 * process ensures that all actions run on a single thread (the real time clock)
 * which is essential as the core code of the elevator is not thread safe (and
 * can't be made thread safe either, see #3738).
 *
 * This {@link EventQueue} is thread safe.
 *
 * @author Neil McKellar and Chris Dailey
 * @author W.Pasman modified for thread safety
 */
public final class EventQueue {
	private long currentTime = -1; // Invalid time value initially
	private long lastTime;
	private long lastEventProcessTime;
	private final TreeSet<Event> eventSet = new TreeSet<>(new Event.EventTimeComparator());

	/**
	 * Interface for listeners for events in the {@link EventQueue}.
	 *
	 */
	public interface Listener {
		void eventAdded(Event e);

		void eventRemoved(Event e);

		void eventError(Exception ex);
	}

	private final List<Listener> listeners = new ArrayList<>();

	/**
	 * Add new event to the event set.
	 *
	 * @param event new event. This event must be in the future (its time must be
	 *              &gt; {@link #getCurrentTime()} and not already be in the set.
	 */
	public synchronized void addEvent(final Event event) {
		// System.out.println("EventQueue event at currentTime=" + currentTime +
		// " for time="+event.getTime()+ ", class="+event.getClass().getName());
		if (event.getTime() < this.lastTime) {
			throw new IllegalArgumentException(
					"Event occurs *before* the last time we processed: " + event.getTime() + " < " + this.lastTime);
		}
		if ((this.currentTime != -1) && (event.getTime() < this.currentTime)) {
			throw new IllegalArgumentException(
					"Event occurs *before* the current time: " + event.getTime() + " < " + this.currentTime);
		}
		if (this.eventSet.contains(event)) {
			throw new IllegalArgumentException("Cannot re-add an Event to the queue!");
		}
		this.eventSet.add(event);

		for (final Listener listener2 : this.listeners) {
			final Listener listener = listener2;
			listener.eventAdded(event);
		}
	}

	/**
	 * Remove event from the queue. Succeeds if the event is not in the queue to
	 * start with.
	 *
	 * @param event is event to be removed.
	 */
	public synchronized void removeEvent(final Event event) {
		/**
		 * modified, parents can not always be sure whether event is still in the queue
		 * by the time this function is called. And it seems not to matter anyway - if
		 * it was already removed (because event ended) then we can just proceed.
		 */
		this.eventSet.remove(event);
		for (final Listener listener2 : this.listeners) {
			final Listener listener = listener2;
			listener.eventRemoved(event);
		}
	}

	/**
	 * @return COPY of event list
	 */
	public synchronized List<Event> getEventList() {
		return new ArrayList<>(this.eventSet);
	}

	/**
	 * Processes events in the event list up to the requested time. The method
	 * throws an exception if the requested time is before the last processed time
	 * mark. The remainging pending events that require updates are also notified.
	 *
	 * @param time The requested time to process up to in the list of events.
	 * @throws RuntimeException When the requested time is before the last time.
	 * @return true if events were processed
	 */
	public synchronized boolean processEventsUpTo(final long time) {
		if (time < this.lastTime) {
			throw new RuntimeException("Requested time is earlier than last time.");
		}

		int numEventsProcessed = 0;
		do {
			if (this.eventSet.isEmpty()) {
				break; // can't process events if there aren't any
			}
			final Event currentEvent = this.eventSet.first();
			// Since eventSet is ordered, and we're only interested in
			// processing events
			// up to 'time', if we find an event after 'time' then we stop
			// processing
			// the Set.
			if (currentEvent.getTime() > time) {
				break;
			}
			// Now we know the event needs to be processed
			removeEvent(currentEvent);
			final long oldCurrentTime = this.currentTime;
			this.currentTime = currentEvent.getTime();
			try {
				// If the time has progressed, we must update the
				// TrackingUpdateEvents
				// so further calculations in Event.perform() are based on
				// up-to-date
				// values.
				if (oldCurrentTime != this.currentTime) {
					numEventsProcessed += updateEventProgress();
				}

				this.lastEventProcessTime = this.currentTime;
				currentEvent.perform();
				numEventsProcessed++;
			} catch (final Exception e) {
				e.printStackTrace();
				for (final Listener listener : this.listeners) {
					final Listener l = listener;
					l.eventError(e);
				}
			}
		} while (true);
		this.currentTime = time;
		numEventsProcessed += updateEventProgress();
		this.currentTime = -1;
		this.lastTime = this.eventSet.isEmpty() ? this.lastEventProcessTime : time;
		return (numEventsProcessed != 0);
	}

	private int updateEventProgress() {
		int numEventsProcessed = 0;
		// Update any events that have incremental progress between states
		// we draw a copy of the queue to avoid blocking the queue too long.
		for (final Event evt : getEventList()) {
			if (evt instanceof IncrementalUpdateEvent) {
				final IncrementalUpdateEvent updateEvent = (IncrementalUpdateEvent) evt;
				try {
					updateEvent.updateTime();
					numEventsProcessed++;
				} catch (final Exception ex) {
					ex.printStackTrace();
				}
			}
		}
		return numEventsProcessed;
	}

	public void addListener(final Listener listener) {
		this.listeners.add(listener);
	}

	public void removeListener(final Listener listener) {
		this.listeners.remove(listener);
	}

	/**
	 * @return The current time, or the last time an event was processed.
	 */
	public long getCurrentTime() {
		if (this.currentTime == -1) {
			return this.lastTime;
		}
		// Wouter: hacked: things may get a bit asynchronous and we have to deal
		// with that somehow...
		// throw new
		// IllegalStateException("Current time is invalid when not processing
		// events");
		return this.currentTime;
	}

	public long getLastEventProcessTime() {
		return this.lastEventProcessTime;
	}

	/**
	 * as {@link #addEvent(Event)} but sets the time of the event so that it will be
	 * evaluated asap.
	 *
	 * @param event the event to be inserted
	 */
	public synchronized void insertEvent(final Event event) {
		final Event newTimedEvent = event.setTime(getCurrentTime() + 1);
		addEvent(newTimedEvent);
	}
}
