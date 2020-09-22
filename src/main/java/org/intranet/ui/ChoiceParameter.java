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
public final class ChoiceParameter extends SingleValueParameter {
	private static final long serialVersionUID = 1L;
	private Object value;
	private final List<?> legalValues;
	private final Class<?> type;

	public ChoiceParameter(final Simulator.Keys key, final List<?> legalValues, final Object defaultValue,
			final Class<?> expectedType) {
		super(key);
		this.value = defaultValue;
		this.legalValues = legalValues;
		this.type = expectedType;
	}

	public List<?> getLegalValues() {
		return this.legalValues;
	}

	public Class<?> getType() {
		return this.type;
	}

	@Override
	public void setValueFromUI(final Object param) {
		for (final Object next : this.legalValues) {
			if (next.toString().equals(param.toString())) {
				this.value = next;
				return;
			}
		}
		throw new IllegalArgumentException("Parameter is not a legal value.");
	}

	@Override
	public Object getUIValue() {
		return this.value.toString();
	}

	public Object getChoiceValue() {
		return this.value;
	}

	@Override
	public List<Object> getValues(final String min, final String max, final String inc) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object clone() {
		final ChoiceParameter cp = new ChoiceParameter(getKey(), this.legalValues, this.value, this.type);
		return cp;
	}
}
