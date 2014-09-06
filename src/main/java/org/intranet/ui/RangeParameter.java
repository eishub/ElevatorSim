/*
 * Copyright 2004 Neil McKellar and Chris Dailey
 * All rights reserved.
 */
package org.intranet.ui;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Neil McKellar and Chris Dailey
 * 
 */
public final class RangeParameter extends MultipleValueParameter {
	private String baseValue;
	private String maxValue;
	private String incrementValue;
	private SingleValueParameter param;

	public RangeParameter(SingleValueParameter p) {
		super(p.getKey());
		param = p;
		setBaseValueFromString((String) p.getUIValue());
		setMaxValueFromString((String) p.getUIValue());
		setIncrementValueFromString("1");
	}

	public void setBaseValueFromString(String base) {
		baseValue = base;
	}

	public String getBaseValueAsString() {
		return baseValue;
	}

	public void setMaxValueFromString(String max) {
		maxValue = max;
	}

	public String getMaxValueAsString() {
		return maxValue;
	}

	public void setIncrementValueFromString(String incr) {
		incrementValue = incr;
	}

	public String getIncrementValueAsString() {
		return incrementValue;
	}

	public List getParameterList() {
		List params = new ArrayList();
		if (!isMultiple) {
			param.setValueFromUI(baseValue);
			params.add(param);
			return params;
		}
		for (Iterator values = getValues(baseValue, maxValue, incrementValue)
				.iterator(); values.hasNext();) {
			String value = (String) values.next();
			SingleValueParameter p = (SingleValueParameter) param.clone();
			p.setValueFromUI(value);
			params.add(p);
		}
		return params;
	}

	public List getValues(String min, String max, String inc) {
		return param.getValues(min, max, inc);
	}

	public Object getSingleValue() {
		return getBaseValueAsString();
	}
}
