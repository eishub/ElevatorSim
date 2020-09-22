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
public class LongParameter extends SingleValueParameter {
	private static final long serialVersionUID = 1L;
	private long value;

	public LongParameter(final Simulator.Keys key, final long defaultValue) {
		super(key);
		this.value = defaultValue;
	}

	public long getLongValue() {
		return this.value;
	}

	void setLongValue(final long newValue) {
		this.value = newValue;
	}

	@Override
	public void setValueFromUI(final Object param) {
		setLongValue(Long.parseLong((String) param));
	}

	@Override
	public Object getUIValue() {
		return Long.toString(this.value);
	}

	@Override
	public List<Object> getValues(final String min, final String max, final String inc) {
		long minLong = Long.parseLong(min);
		long maxLong = Long.parseLong(max);
		final long incLong = Long.parseLong(inc);

		if (minLong > maxLong) {
			final long temp = minLong;
			minLong = maxLong;
			maxLong = temp;
		}

		final int capacity = (int) ((maxLong - minLong) / incLong + 1);
		final List<Object> longValues = new ArrayList<>(capacity);

		// populate the array list with strings representing the values
		for (long val = minLong; val <= maxLong; val += incLong) {
			longValues.add(Long.toString(val));
		}

		return longValues;
	}
}
