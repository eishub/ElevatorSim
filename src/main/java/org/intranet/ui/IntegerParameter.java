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
 * 
 */
public class IntegerParameter extends SingleValueParameter {
	int value;

	public IntegerParameter(Simulator.Keys simkey, int defaultValue) {
		super(simkey);
		value = defaultValue;
	}

	public int getIntegerValue() {
		return value;
	}

	void setIntegerValue(int newValue) {
		value = newValue;
	}

	public void setValueFromUI(Object param) {
		setIntegerValue(Integer.parseInt((String) param));
	}

	public Object getUIValue() {
		return Integer.toString(value);
	}

	public List getValues(String min, String max, String inc) {
		int minInteger = Integer.parseInt(min);
		int maxInteger = Integer.parseInt(max);
		int incInteger = Integer.parseInt(inc);

		if (minInteger > maxInteger) {
			int temp = minInteger;
			minInteger = maxInteger;
			maxInteger = temp;
		}

		int capacity = (maxInteger - minInteger) / incInteger + 1;
		List intValues = new ArrayList(capacity);

		// populate the array list with strings representing the values
		for (int val = minInteger; val <= maxInteger; val += incInteger)
			intValues.add(Integer.toString(val));

		return intValues;
	}
}
