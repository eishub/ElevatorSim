/*
 * Copyright 2003 Neil McKellar and Chris Dailey
 * All rights reserved.
 */
package org.intranet.sim.event;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

/**
 * A queue that contains future {@link Event}s. These events are actually
 * happening when {@link #processEventsUpTo(long)} is called.
 * 
 * @author Neil McKellar and Chris Dailey
 * 
 */
public final class EventQueue {
	private long currentTime = -1; // Invalid time value initially

	private long lastTime;
	private long lastEventProcessTime;

	private EventQueueSet eventSet = new EventQueueSet(); // new TreeSet(new
															// Event.EventTimeComparator());

	/**
	 * Interface for listeners for events in the {@link EventQueue}.
	 *
	 */
	public interface Listener {
		void eventAdded(Event e);

		void eventRemoved(Event e);

		void eventError(Exception ex);
	}

	private List listeners = new ArrayList();

	public void addEvent(Event event) {
		// System.out.println("EventQueue event at currentTime=" + currentTime +
		// " for time="+event.getTime()+ ", class="+event.getClass().getName());
		if (event.getTime() < lastTime) {
			throw new IllegalArgumentException(
					"Event occurs *before* the last time we processed: "
							+ event.getTime() + " < " + lastTime);
		}
		if ((currentTime != -1) && (event.getTime() < currentTime)) {
			throw new IllegalArgumentException(
					"Event occurs *before* the current time: "
							+ event.getTime() + " < " + currentTime);
		}
		if (eventSet.contains(event)) {
			throw new IllegalArgumentException(
					"Cannot re-add an Event to the queue!");
		}
		eventSet.add(event);

		for (Iterator i = listeners.iterator(); i.hasNext();) {
			Listener listener = (Listener) i.next();
			listener.eventAdded(event);
		}
	}

	/**
	 * Remove event from the queue. Succeeds if the event is not in the queue to
	 * start with.
	 * 
	 * 
	 * @param event
	 *            is event to be removed.
	 */
	public void removeEvent(Event event) {
		/**
		 * modified, parents can not always be sure whether event is still in
		 * the queue by the time this function is called. And it seems not to
		 * matter anyway - if it was already removed (because event ended) then
		 * we can just proceed.
		 */
		eventSet.remove(event);
		for (Iterator i = listeners.iterator(); i.hasNext();) {
			Listener listener = (Listener) i.next();
			listener.eventRemoved(event);
		}
	}

	public List getEventList() {
		return eventSet.getEvents();
	}

	/**
	 * Processes events in the event list up to the requested time. The method
	 * throws an exception if the requested time is before the last processed
	 * time mark. The remainging pending events that require updates are also
	 * notified.
	 * 
	 * @param time
	 *            The requested time to process up to in the list of events.
	 * @throws RuntimeException
	 *             When the requested time is before the last time.
	 * @return true if events were processed
	 */
	public boolean processEventsUpTo(long time) {
		if (time < lastTime) {
			throw new RuntimeException(
					"Requested time is earlier than last time.");
		}

		int numEventsProcessed = 0;
		do {
			if (eventSet.isEmpty())
				break; // can't process events if there aren't any
			Event currentEvent = (Event) eventSet.first();
			// Since eventSet is ordered, and we're only interested in
			// processing events
			// up to 'time', if we find an event after 'time' then we stop
			// processing
			// the Set.
			if (currentEvent.getTime() > time)
				break;
			// Now we know the event needs to be processed
			removeEvent(currentEvent);
			long oldCurrentTime = currentTime;
			currentTime = currentEvent.getTime();
			try {
				// If the time has progressed, we must update the
				// TrackingUpdateEvents
				// so further calculations in Event.perform() are based on
				// up-to-date
				// values.
				if (oldCurrentTime != currentTime)
					numEventsProcessed += updateEventProgress();

				lastEventProcessTime = currentTime;
				currentEvent.perform();
				numEventsProcessed++;
			} catch (Exception e) {
				e.printStackTrace();
				for (Iterator i = listeners.iterator(); i.hasNext();) {
					Listener l = (Listener) i.next();
					l.eventError(e);
				}
			}
		} while (true);
		currentTime = time;
		numEventsProcessed += updateEventProgress();
		currentTime = -1;
		lastTime = eventSet.isEmpty() ? lastEventProcessTime : time;
		return (numEventsProcessed != 0);
	}

	private int updateEventProgress() {
		int numEventsProcessed = 0;
		// Update any events that have incremental progress between states
		// we draw a copy of the queue to avoid blocking the queue too long.
		for (Event evt : eventSet.getEvents()) {
			if (evt instanceof IncrementalUpdateEvent) {
				IncrementalUpdateEvent updateEvent = (IncrementalUpdateEvent) evt;
				try {
					updateEvent.updateTime();
					numEventsProcessed++;
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}
		return numEventsProcessed;
	}

	public void addListener(Listener listener) {
		listeners.add(listener);
	}

	public void removeListener(Listener listener) {
		listeners.remove(listener);
	}

	public long getCurrentTime() {
		if (currentTime == -1)
			return lastTime;
		// Wouter: hacked: things may get a bit asynchronous and we have to deal
		// with that somehow...
		// throw new
		// IllegalStateException("Current time is invalid when not processing events");
		return currentTime;
	}

	public long getLastEventProcessTime() {
		return lastEventProcessTime;
	}
}

/**
 * Added to implement thread safety. We don't extend the original class to make
 * sure that we don't forget to synchronize some call
 * 
 * @author W.Pasman 22nov2010 trac #1340
 * 
 */
class EventQueueSet {
	private TreeSet<Event> events = new TreeSet<Event>(
			new Event.EventTimeComparator());

	synchronized boolean contains(Object e) {
		return events.contains(e);
	}

	synchronized boolean add(Event e) {
		return events.add(e);
	}

	synchronized boolean remove(Event e) {
		return events.remove(e);
	}

	/**
	 * get copy of available events.
	 * 
	 * @return
	 */
	synchronized ArrayList<Event> getEvents() {
		return new ArrayList<Event>(events);
	}

	synchronized boolean isEmpty() {
		return events.isEmpty();
	}

	synchronized Event first() {
		return events.first();
	}
}
