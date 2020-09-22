/*
 * Copyright 2003 Neil McKellar and Chris Dailey
 * All rights reserved.
 */
package org.intranet.elevator.model.operate.controller;

import org.intranet.elevator.model.Floor;

class Assignment {
	private final Floor destination;
	private final Direction direction;

	Assignment(final Floor floor, final Direction dir) {
		this.destination = floor;
		this.direction = dir;
	}

	Floor getDestination() {
		return this.destination;
	}

	Direction getDirection() {
		return this.direction;
	}

	@Override
	public boolean equals(final Object o) {
		if (o == null || !(o instanceof Assignment)) {
			return false;
		}

		final Assignment a = (Assignment) o;
		return (a.destination == this.destination && a.direction == this.direction);
	}

	@Override
	public int hashCode() {
		return this.destination.hashCode() + this.direction.hashCode();
	}

	@Override
	public String toString() {
		return this.destination.getFloorNumber() + this.direction.toString();
	}
}