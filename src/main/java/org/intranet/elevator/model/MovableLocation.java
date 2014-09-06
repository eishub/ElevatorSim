/*
 * Copyright 2005 Neil McKellar and Chris Dailey
 * All rights reserved.
 */
package org.intranet.elevator.model;

import org.intranet.sim.event.Event;
import org.intranet.sim.event.EventQueue;
import org.intranet.sim.event.TrackingUpdateEvent;

/**
 * A location that can move. The state of movement is kept between height and
 * destinationHeight. If they are equal, then the MovableLocation is still. If
 * they are not equal, then the MovableLocation is moving. Valid states:
 * <table border="1" cellspacing="0" cellpadding="2">
 * <tr>
 * <th rowspan="2">State</th>
 * <th colspan="1">Variables</th>
 * <th colspan="10">Transitions</th>
 * </tr>
 * <tr>
 * <th>destinationHeight</th>
 * <th>setDestinationHeight()</th>
 * <th>[ArrivalEvent]</th>
 * </tr>
 * <tr>
 * <td>IDLE</td>
 * <td>height</td>
 * <td>IDLE:[arrive()] or travel(): MOVING</td>
 * <td><i>Impossible</i></td>
 * </tr>
 * <tr>
 * <td>MOVING</td>
 * <td><i>other</i></td>
 * <td>IDLE:[arrive()] or travel(): MOVING</td>
 * <td>IDLE:[arrive()]</td>
 * </tr>
 * </table>
 * <p>
 * Note that <code>arrive()</code> is like an event that is picked up by the
 * subclass, as it is an abstract method.
 * </p>
 * 
 * @author Neil McKellar and Chris Dailey
 */
public abstract class MovableLocation extends Location {
	MovableLocation(EventQueue eQ, float height, int capacity) {
		super(eQ, height, capacity);
		destinationHeight = height;
		eQ.addListener(new EventQueue.Listener() {
			public void eventAdded(Event e) {
			}

			public void eventError(Exception ex) {
			}

			public void eventRemoved(Event e) {
				if (e == arrivalEvent)
					arrivalEvent = null;
			}
		});
	}

	private float destinationHeight;
	private float totalDistance = 0.0F;
	private int numTravels = 0;
	private Event arrivalEvent;

	public final float getTotalDistance() {
		return totalDistance;
	}

	public final int getNumTravels() {
		return numTravels;
	}

	public final float getTravelTime(float distance) {
		return Math.abs(distance / getRatePerSecond());
	}

	public final void setHeight(float newHeight) {
		totalDistance += Math.abs(newHeight - getHeight());
		super.setHeight(newHeight);
	}

	protected final void setDestinationHeight(float h) {
		if (arrivalEvent != null) {
			eventQueue.removeEvent(arrivalEvent);
			arrivalEvent = null;
		}

		// checkDirectionChange(h);
		destinationHeight = h;

		if (getHeight() == h)
			arrive();
		else
			travel();
	}

	protected abstract void arrive();

	protected abstract float getRatePerSecond();

	private class ArrivalEvent extends TrackingUpdateEvent {
		public ArrivalEvent(float departureHeight, long departureTime,
				long arrivalTime) {
			super(departureTime, departureHeight, arrivalTime,
					destinationHeight);
		}

		public void perform() {
			setHeight(destinationHeight);
			numTravels++;
			arrive();
		}

		public void updateTime() {
			setHeight(currentValue(eventQueue.getCurrentTime()));
		}
	}

	private void checkDirectionChange(float h) {
		boolean areMoving = getHeight() != destinationHeight;
		if (areMoving) {
			boolean oldUp = destinationHeight > getHeight();
			boolean newUp = h > getHeight();
			if (oldUp != newUp) {
				System.err
						.println("MovableLocation.setDestination: destinationHeight="
								+ destinationHeight
								+ ", currentHeight="
								+ getHeight() + ", new destHeight=" + h);
				// TODO : Make fault-tolerant -- remove assignments from list
				// and give them back to the building for re-assignment
				// TODO : Or rejecting the destination
				throw new IllegalArgumentException(
						"Can't change directions in mid-travel.");
			}
		}
	}

	private void travel() {
		float ratePerMillisecond = getRatePerSecond() / 1000;
		long arrivalTime = eventQueue.getCurrentTime()
				+ (long) (Math.abs(getHeight() - destinationHeight) / ratePerMillisecond);
		long departureTime = eventQueue.getCurrentTime();
		float departureHeight = getHeight();
		// create and remember IncrementalUpdateEvent(arrivalTime)
		arrivalEvent = new ArrivalEvent(departureHeight, departureTime,
				arrivalTime);
		try {
			eventQueue.addEvent(arrivalEvent);
		} catch (IllegalArgumentException iae) {
			System.err
					.println("MovableLocation.travel():eventQueue.getCurrentTime()="
							+ eventQueue.getCurrentTime());
			System.err.println("departureTime=" + departureTime);
			System.err.println("arrivalTime  =" + arrivalTime);
			throw iae;
		}
	}
}
