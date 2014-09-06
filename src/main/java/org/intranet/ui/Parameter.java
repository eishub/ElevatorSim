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
	private Simulator.Keys simkey;

	private Parameter() {
		super();
	}

	public Parameter(Simulator.Keys key) {
		super();
		simkey = key;
	}

	public final String getDescription() {
		return simkey.getDescription();
	}

	public String toString() {
		return getDescription();
	}

	public Simulator.Keys getKey() {
		return simkey;
	}
}
