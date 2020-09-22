/*
 * Copyright 2004 Neil McKellar and Chris Dailey
 * All rights reserved.
 */
package org.intranet.ui;

import java.util.LinkedList;
import java.util.List;

/**
 * @author Neil McKellar and Chris Dailey
 */
public final class RangeParameter extends MultipleValueParameter {
	private static final long serialVersionUID = 1L;
	private String baseValue;
	private String maxValue;
	private String incrementValue;
	private final SingleValueParameter param;

	public RangeParameter(final SingleValueParameter p) {
		super(p.getKey());
		this.param = p;
		setBaseValueFromString((String) p.getUIValue());
		setMaxValueFromString((String) p.getUIValue());
		setIncrementValueFromString("1");
	}

	public void setBaseValueFromString(final String base) {
		this.baseValue = base;
	}

	public String getBaseValueAsString() {
		return this.baseValue;
	}

	public void setMaxValueFromString(final String max) {
		this.maxValue = max;
	}

	public String getMaxValueAsString() {
		return this.maxValue;
	}

	public void setIncrementValueFromString(final String incr) {
		this.incrementValue = incr;
	}

	public String getIncrementValueAsString() {
		return this.incrementValue;
	}

	@Override
	public List<Parameter> getParameterList() {
		final List<Parameter> params = new LinkedList<>();
		if (!this.isMultiple) {
			this.param.setValueFromUI(this.baseValue);
			params.add(this.param);
			return params;
		}
		for (final Object object : getValues(this.baseValue, this.maxValue, this.incrementValue)) {
			final String value = object.toString();
			final SingleValueParameter p = (SingleValueParameter) this.param.clone();
			p.setValueFromUI(value);
			params.add(p);
		}
		return params;
	}

	public List<Object> getValues(final String min, final String max, final String inc) {
		return this.param.getValues(min, max, inc);
	}

	@Override
	public Object getSingleValue() {
		return getBaseValueAsString();
	}
}
