/*
 * Copyright 2003-2005 Neil McKellar and Chris Dailey
 * All rights reserved.
 */
package org.intranet.elevator.model;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.intranet.elevator.model.operate.Person;
import org.intranet.sim.ModelElement;
import org.intranet.sim.event.EventQueue;

public class Location extends ModelElement {
	private float height;
	private final int capacity;
	private final List<Person> people = new LinkedList<>();

	Location(final EventQueue eQ, final float height, final int capacity) {
		super(eQ);
		this.height = height;
		this.capacity = capacity;
	}

	/**
	 * @return Current position of the elevator.
	 */
	public final float getHeight() {
		return this.height;
	}

	protected void setHeight(final float newHeight) {
		this.height = newHeight;
	}

	public final void personEnters(final Person person) {
		if (isAtCapacity()) {
			throw new IllegalStateException("Location is at capacity: " + this.capacity);
		}
		this.people.add(person);
	}

	public final Iterator<Person> getPeople() {
		return this.people.iterator();
	}

	public final void personLeaves(final Person person) {
		if (!this.people.remove(person)) {
			throw new IllegalStateException("Person is not in this location.");
		}
	}

	public final boolean isAtCapacity() {
		return (this.people.size() == this.capacity);
	}

	public final int getCapacity() {
		return this.capacity;
	}
}