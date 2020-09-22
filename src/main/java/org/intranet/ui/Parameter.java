/*
 * Copyright 2003 Neil McKellar and Chris Dailey
 * All rights reserved.
 */
package org.intranet.ui;

import java.io.Serializable;

import org.intranet.sim.Simulator;

/**
 * @author Neil McKellar and Chris Dailey
 *
 *         LATER: add a way to validate inputs
 */
public abstract class Parameter implements Serializable {
	private static final long serialVersionUID = 1L;
	private final Simulator.Keys simkey;

	public Parameter(final Simulator.Keys key) {
		super();
		this.simkey = key;
	}

	public final String getDescription() {
		return this.simkey.getDescription();
	}

	@Override
	public String toString() {
		return getDescription();
	}

	public Simulator.Keys getKey() {
		return this.simkey;
	}
}
