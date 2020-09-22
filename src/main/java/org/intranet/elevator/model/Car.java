/*
 * Copyright 2003-2005 Neil McKellar and Chris Dailey
 * All rights reserved.
 */
package org.intranet.elevator.model;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.intranet.sim.clock.RealTimeClock;
import org.intranet.sim.event.EventQueue;

/**
 * The states of Car are substates of MovableLocation:IDLE. Valid states:
 * <table border="1" cellspacing="0" cellpadding="2" summary="">
 * <tr>
 * <th rowspan="2">State</th>
 * <th colspan="2">Variables</th>
 * <th colspan="10">Transitions</th>
 * </tr>
 * <tr>
 * <th>destination</th>
 * <th>location</th>
 * <th>setDestination()</th>
 * <th>undock()</th>
 * <th>[MovableLocation.arrive()]</th>
 * </tr>
 * <tr>
 * <td>IDLE:UNDOCKED</td>
 * <td>null</td>
 * <td>null</td>
 * <td>MOVING or arrive(): DOCKED</td>
 * <td><i>Illegal</i></td>
 * <td><i>Impossible</i></td>
 * </tr>
 * <tr>
 * <td>MOVING</td>
 * <td>Set</td>
 * <td>null</td>
 * <td>MOVING or arrive(): DOCKED</td>
 * <td><i>Illegal</i></td>
 * <td>DOCKED<br>
 * [docked()]</td>
 * </tr>
 * <tr>
 * <td>IDLE:UNDOCKING</td>
 * <td>Set</td>
 * <td>Set</td>
 * <td>UNDOCKING</td>
 * <td>MOVING</td>
 * <td><i>Impossible</i></td>
 * </tr>
 * <tr>
 * <td>IDLE:DOCKED</td>
 * <td>null</td>
 * <td>Set</td>
 * <td>UNDOCKING</td>
 * <td>UNDOCKED</td>
 * <td><i>Impossible</i></td>
 * </tr>
 * </table>
 *
 * @author Neil McKellar and Chris Dailey
 */
public final class Car extends MovableLocation {
	private final String name;
	private Floor location;
	private Floor destination;
	private final FloorRequestPanel panel = new FloorRequestPanel();
	private final List<Listener> listeners = new LinkedList<>();

	public interface Listener {
		void docked();
	}

	public Car(final EventQueue eQ, final String name, final float height, final int capacity) {
		super(eQ, height, capacity);
		this.name = name;
	}

	/**
	 * Not thread safe. Only call from {@link RealTimeClock} thread.
	 *
	 * @param destination new {@link Floor} to go to.
	 */
	public void setDestination(final Floor destination) {
		this.destination = destination;
		if (this.location == null) {
			setDestinationHeight(destination.getHeight());
		}
	}

	public float getTravelTime(final Floor floor) {
		final float floorHeight = floor.getHeight();
		final float travelDistance = floorHeight - getHeight();
		return getTravelTime(travelDistance);
	}

	/**
	 * Not thread safe. Only call from {@link RealTimeClock} thread.
	 */
	public void undock() {
		if (this.location == null) {
			throw new IllegalStateException("Must be docked to undock");
		}

		this.location = null;
		if (this.destination != null) {
			setDestinationHeight(this.destination.getHeight());
		}
	}

	public Floor getDestination() {
		return this.destination;
	}

	public Floor getLocation() {
		return this.location;
	}

	public void addListener(final Listener listener) {
		this.listeners.add(listener);
	}

	public void removeListener(final Listener listener) {
		this.listeners.remove(listener);
	}

	public String getName() {
		return this.name;
	}

	public FloorRequestPanel getFloorRequestPanel() {
		return this.panel;
	}

	public Floor getFloorAt() {
		if (this.destination == null && this.location == null) {
			return this.panel.getFloorAt(getHeight());
		}
		return null;
	}

	@Override
	public float getRatePerSecond() {
		return (float) (1000 * 10.0 / 4030.0);
	}

	@Override
	protected void arrive() {
		if (this.destination == null) {
			throw new IllegalStateException("already arrived, destination=null");
		}
		this.location = this.destination;
		this.destination = null;
		this.panel.requestFulfilled(this.location);
		fireDockedEvent();
	}

	private void fireDockedEvent() {
		final List<Listener> listenersCopy = new ArrayList<>(this.listeners);
		for (final Listener listener : listenersCopy) {
			final Listener l = listener;
			l.docked();
		}
	}

	@Override
	public String toString() {
		return this.name;
	}
}