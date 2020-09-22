/*
 * Copyright 2003 Neil McKellar and Chris Dailey
 * All rights reserved.
 */
package org.intranet.sim.event;

/**
 * @author Neil McKellar and Chris Dailey Does interpolation.
 */
public abstract class TrackingUpdateEvent extends IncrementalUpdateEvent {
	private long beginTime;
	private float beginValue;
	private float distance;

	private TrackingUpdateEvent() {
		super(0);
	}

	public TrackingUpdateEvent(final long beginTime, final float begin, final long endTime, final float end) {
		super(endTime);
		this.beginTime = beginTime;
		this.beginValue = begin;
		this.distance = end - begin;
	}

	private float percentDone(final long time) {
		return (float) (time - this.beginTime) / (getTime() - this.beginTime);
	}

	/**
	 * Gets the current tracking value as a straight interpolation between the
	 * beginValue and endValue.
	 *
	 * @param time The current time. Do not pass Event.getTime(), use
	 *             eventQueue.getCurrentTime().
	 * @return The current value being tracked by this event.
	 */
	public final float currentValue(final long time) {
		final float percent = percentDone(time);
		return this.beginValue + this.distance * percent;
	}
}