/*
 * Copyright 2005 Neil McKellar and Chris Dailey
 * All rights reserved.
 */
package org.intranet.ui;

import java.util.Iterator;
import java.util.List;

import org.intranet.sim.Simulator;

/**
 * @author Neil McKellar and Chris Dailey
 * 
 */
public final class ChoiceParameter extends SingleValueParameter {
	private Object value;
	private List legalValues;
	private Class type;

	public ChoiceParameter(Simulator.Keys key, List legalValues,
			Object defaultValue, Class expectedType) {
		super(key);
		value = defaultValue;
		this.legalValues = legalValues;
		type = expectedType;
	}

	public List getLegalValues() {
		return legalValues;
	}

	public Class getType() {
		return type;
	}

	public void setValueFromUI(Object param) {
		for (Iterator i = legalValues.iterator(); i.hasNext();) {
			Object next = i.next();
			if (next.toString().equals(param.toString())) {
				value = next;
				return;
			}
		}
		throw new IllegalArgumentException("Parameter is not a legal value.");
	}

	public Object getUIValue() {
		return value.toString();
	}

	public Object getChoiceValue() {
		return value;
	}

	public List getValues(String min, String max, String inc) {
		throw new UnsupportedOperationException();
	}

	public Object clone() {
		ChoiceParameter cp = new ChoiceParameter(getKey(), legalValues, value,
				type);
		return cp;
	}
}
