/*
 * Copyright 2005 Neil McKellar and Chris Dailey
 * All rights reserved.
 */
package org.intranet.ui;

import java.util.ArrayList;
import java.util.List;

import org.intranet.sim.Simulator;

/**
 * @author Neil McKellar and Chris Dailey
 */
public class FloatParameter extends SingleValueParameter {
	private static final long serialVersionUID = 1L;
	private float value;

	public FloatParameter(final Simulator.Keys key, final float defaultValue) {
		super(key);
		this.value = defaultValue;
	}

	@Override
	public void setValueFromUI(final Object param) {
		this.value = Float.parseFloat((String) param);
	}

	@Override
	public Object getUIValue() {
		return String.valueOf(this.value);
	}

	public float getFloatValue() {
		return this.value;
	}

	void setFloatValue(final float newValue) {
		this.value = newValue;
	}

	@Override
	public List<Object> getValues(final String min, final String max, final String inc) {
		float minFloat = Float.parseFloat(min);
		float maxFloat = Float.parseFloat(max);
		final float incFloat = Float.parseFloat(inc);

		if (minFloat > maxFloat) {
			final float temp = minFloat;
			minFloat = maxFloat;
			maxFloat = temp;
		}

		final int capacity = (int) ((maxFloat - minFloat) / incFloat + 1);
		final List<Object> intValues = new ArrayList<>(capacity);

		// populate the array list with strings representing the values
		for (float val = minFloat; val <= maxFloat; val += incFloat) {
			intValues.add(Float.toString(val));
		}

		return intValues;
	}
}
