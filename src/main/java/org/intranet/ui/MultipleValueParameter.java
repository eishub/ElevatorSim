/*
 * Copyright 2005 Neil McKellar and Chris Dailey
 * All rights reserved.
 */
package org.intranet.ui;

import java.util.List;

import org.intranet.sim.Simulator;

/**
 * @author Neil McKellar and Chris Dailey
 */
public abstract class MultipleValueParameter extends Parameter {
	private static final long serialVersionUID = 1L;

	public MultipleValueParameter(final Simulator.Keys key) {
		super(key);
		this.isMultiple = false;
	}

	public abstract Object getSingleValue();

	public abstract List<Parameter> getParameterList();

	protected boolean isMultiple;

	public void setMultiple(final boolean multiple) {
		this.isMultiple = multiple;
	}

	public boolean isMultiple() {
		return this.isMultiple;
	}
}
