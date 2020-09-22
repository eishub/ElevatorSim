/*
 * Copyright 2003 Neil McKellar and Chris Dailey
 * All rights reserved.
 */
package org.intranet.elevator.model.operate.controller;

public final class Direction {
	private final String name;

	private Direction(final String aName) {
		this.name = aName;
	}

	public static final Direction UP = new Direction("UP");
	public static final Direction DOWN = new Direction("DOWN");
	public static final Direction NONE = new Direction("NONE");

	public boolean isUp() {
		return this.name.equals(Direction.UP.name);
	}

	@Override
	public String toString() {
		return this.name;
	}
}