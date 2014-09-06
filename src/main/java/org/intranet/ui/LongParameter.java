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
 * 
 */
public class LongParameter extends SingleValueParameter {
	long value;

	public LongParameter(Simulator.Keys key, long defaultValue) {
		super(key);
		value = defaultValue;
	}

	public long getLongValue() {
		return value;
	}

	void setLongValue(long newValue) {
		value = newValue;
	}

	public void setValueFromUI(Object param) {
		setLongValue(Long.parseLong((String) param));
	}

	public Object getUIValue() {
		return Long.toString(value);
	}

	public List getValues(String min, String max, String inc) {
		long minLong = Long.parseLong(min);
		long maxLong = Long.parseLong(max);
		long incLong = Long.parseLong(inc);

		if (minLong > maxLong) {
			long temp = minLong;
			minLong = maxLong;
			maxLong = temp;
		}

		int capacity = (int) ((maxLong - minLong) / incLong + 1);
		List longValues = new ArrayList(capacity);

		// populate the array list with strings representing the values
		for (long val = minLong; val <= maxLong; val += incLong)
			longValues.add(Long.toString(val));

		return longValues;
	}
}
