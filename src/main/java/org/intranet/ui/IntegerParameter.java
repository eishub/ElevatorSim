/*
 * Copyright 2003 Neil McKellar and Chris Dailey
 * All rights reserved.
 */
package org.intranet.ui;

import java.util.ArrayList;
import java.util.List;

import org.intranet.sim.Simulator;

/**
 * @author Neil McKellar and Chris Dailey
 */
public class IntegerParameter extends SingleValueParameter {
	private static final long serialVersionUID = 1L;
	private int value;

	public IntegerParameter(final Simulator.Keys simkey, final int defaultValue) {
		super(simkey);
		this.value = defaultValue;
	}

	public int getIntegerValue() {
		return this.value;
	}

	void setIntegerValue(final int newValue) {
		this.value = newValue;
	}

	@Override
	public void setValueFromUI(final Object param) {
		setIntegerValue(Integer.parseInt((String) param));
	}

	@Override
	public Object getUIValue() {
		return Integer.toString(this.value);
	}

	@Override
	public List<Object> getValues(final String min, final String max, final String inc) {
		int minInteger = Integer.parseInt(min);
		int maxInteger = Integer.parseInt(max);
		final int incInteger = Integer.parseInt(inc);

		if (minInteger > maxInteger) {
			final int temp = minInteger;
			minInteger = maxInteger;
			maxInteger = temp;
		}

		final int capacity = (maxInteger - minInteger) / incInteger + 1;
		final List<Object> intValues = new ArrayList<>(capacity);

		// populate the array list with strings representing the values
		for (int val = minInteger; val <= maxInteger; val += incInteger) {
			intValues.add(Integer.toString(val));
		}

		return intValues;
	}
}
