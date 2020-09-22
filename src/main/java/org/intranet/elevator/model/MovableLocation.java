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
 * <table border="1" cellspacing="0" cellpadding="2" summary="">
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
	MovableLocation(final EventQueue eQ, final float height, final int capacity) {
		super(eQ, height, capacity);
		this.destinationHeight = height;
		eQ.addListener(new EventQueue.Listener() {
			@Override
			public void eventAdded(final Event e) {
			}

			@Override
			public void eventError(final Exception ex) {
			}

			@Override
			public void eventRemoved(final Event e) {
				if (e == MovableLocation.this.arrivalEvent) {
					MovableLocation.this.arrivalEvent = null;
				}
			}
		});
	}

	private float destinationHeight;
	private float totalDistance = 0.0F;
	private int numTravels = 0;
	private Event arrivalEvent;

	public final float getTotalDistance() {
		return this.totalDistance;
	}

	public final int getNumTravels() {
		return this.numTravels;
	}

	public final float getTravelTime(final float distance) {
		return Math.abs(distance / getRatePerSecond());
	}

	@Override
	public final void setHeight(final float newHeight) {
		this.totalDistance += Math.abs(newHeight - getHeight());
		super.setHeight(newHeight);
	}

	protected final void setDestinationHeight(final float h) {
		if (this.arrivalEvent != null) {
			this.eventQueue.removeEvent(this.arrivalEvent);
			this.arrivalEvent = null;
		}

		this.destinationHeight = h;

		if (getHeight() == h) {
			arrive();
		} else {
			travel();
		}
	}

	protected abstract void arrive();

	protected abstract float getRatePerSecond();

	private class ArrivalEvent extends TrackingUpdateEvent {
		public ArrivalEvent(final float departureHeight, final long departureTime, final long arrivalTime) {
			super(departureTime, departureHeight, arrivalTime, MovableLocation.this.destinationHeight);
		}

		@Override
		public void perform() {
			setHeight(MovableLocation.this.destinationHeight);
			MovableLocation.this.numTravels++;
			arrive();
		}

		@Override
		public void updateTime() {
			setHeight(currentValue(MovableLocation.this.eventQueue.getCurrentTime()));
		}
	}

	private void travel() {
		final float ratePerMillisecond = getRatePerSecond() / 1000;
		final long arrivalTime = this.eventQueue.getCurrentTime()
				+ (long) (Math.abs(getHeight() - this.destinationHeight) / ratePerMillisecond);
		final long departureTime = this.eventQueue.getCurrentTime();
		final float departureHeight = getHeight();
		// create and remember IncrementalUpdateEvent(arrivalTime)
		this.arrivalEvent = new ArrivalEvent(departureHeight, departureTime, arrivalTime);
		try {
			this.eventQueue.addEvent(this.arrivalEvent);
		} catch (final IllegalArgumentException iae) {
			System.err.println(
					"MovableLocation.travel():eventQueue.getCurrentTime()=" + this.eventQueue.getCurrentTime());
			System.err.println("departureTime=" + departureTime);
			System.err.println("arrivalTime  =" + arrivalTime);
			throw iae;
		}
	}
}
