/*
 * Copyright 2003 Neil McKellar and Chris Dailey
 * All rights reserved.
 */
package org.intranet.sim.event;

import java.util.Comparator;

/**
 *
 * An event to happen at some point in time. In the elevator simulator, these
 * events are created BEFORE the event should happen, and then
 * {@link #perform()} is called when the time has come (or passed) to trigger
 * the event.
 *
 * <p>
 * At this point, the time is an abstract integer. However the RealTimeClock
 * uses {@link System#currentTimeMillis()}
 * </p>
 *
 * @author Neil McKellar and Chris Dailey
 */
public abstract class Event {
	private static long maxId;
	private final long id;
	private final long time;

	/**
	 * Create an event that will happen at the given simulated time. At that time,
	 * perform() will be called.
	 *
	 * @param newTime the time (ms since start) when the event will be performed.
	 */
	public Event(final long newTime) {
		super();
		this.id = getNextId();
		this.time = newTime;
	}

	private static synchronized long getNextId() {
		return maxId++;
	}

	public long getId() {
		return this.id;
	}

	@Override
	public String toString() {
		final String fullClassName = getClass().getName();
		final String shortClassName = fullClassName.substring(fullClassName.lastIndexOf('.') + 1);
		return shortClassName + ": " + Long.toString(this.time);
	}

	public long getTime() {
		return this.time;
	}

	public abstract void perform();

	public static final class EventTimeComparator implements Comparator<Event> {
		@Override
		public int compare(final Event o1, final Event o2) {
			final Event e1 = o1;
			final Event e2 = o2;

			final long diff = (e1.getTime() - e2.getTime());
			if (diff == 0) {
				final long idDiff = e1.getId() - e2.getId();
				return idDiff == 0 ? 0 : idDiff > 0 ? 1 : -1;
			}
			return (diff > 0) ? 1 : -1;
		}
	}

	/**
	 * Create copy of Event, with new given time.
	 *
	 * @param newTime the new time to use
	 * @return {@link Event} caused by the setTime.
	 */
	public Event setTime(final long newTime) {
		throw new UnsupportedOperationException();
	}
}