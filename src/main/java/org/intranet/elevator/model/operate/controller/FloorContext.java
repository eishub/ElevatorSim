/*
 * Copyright 2003 Neil McKellar and Chris Dailey
 * All rights reserved.
 */
package org.intranet.elevator.model.operate.controller;

import org.intranet.elevator.model.Floor;

/**
 * FloorContext defines a range between floors, and includes enough information
 * to tell if a car is in that range, and what the stop at the end of the range
 * and the next stop after that range would be.
 *
 * @author Neil McKellar and Chris Dailey
 */
class FloorContext {
	private final Floor previous;
	private final Floor next;
	private final Floor successor;

	FloorContext(final Floor previous, final Floor next, final Floor successor) {
		super();
		this.previous = previous;
		this.next = next;
		this.successor = successor;
	}

	Floor getPrevious() {
		return this.previous;
	}

	Floor getNext() {
		return this.next;
	}

	Floor getSuccessor() {
		return this.successor;
	}

	boolean contains(final float height) {
		return (height >= this.previous.getHeight() && height <= this.next.getHeight())
				|| (height >= this.next.getHeight() && height <= this.previous.getHeight());
	}

	@Override
	public String toString() {
		return "previous = " + this.previous + ", next = " + this.next + ", succ = " + this.successor;
	}
}