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
 * 
 * 
 */
public abstract class Event {
	private static long maxId;
	private long id;
	private long time;

	private Event() {
		super();
	}

	/**
	 * Create an event that will happen at the given simulated time. At that
	 * time, perform() will be called.
	 * 
	 * @param newTime
	 *            the time (ms since start) when the event will be performed.
	 */
	public Event(long newTime) {
		super();
		id = getNextId();
		time = newTime;
	}

	private static synchronized long getNextId() {
		return maxId++;
	}

	public long getId() {
		return id;
	}

	public String toString() {
		String fullClassName = getClass().getName();
		String shortClassName = fullClassName.substring(fullClassName
				.lastIndexOf('.') + 1);
		return shortClassName + ": " + Long.toString(time);
	}

	public long getTime() {
		return time;
	}

	public abstract void perform();

	public static final class EventTimeComparator implements Comparator<Event> {
		public int compare(Event o1, Event o2) {
			Event e1 = (Event) o1;
			Event e2 = (Event) o2;

			long diff = (e1.getTime() - e2.getTime());
			if (diff == 0) {
				long idDiff = e1.getId() - e2.getId();
				return idDiff == 0 ? 0 : idDiff > 0 ? 1 : -1;
			}
			return (diff > 0) ? 1 : -1;
		}
	}
}
