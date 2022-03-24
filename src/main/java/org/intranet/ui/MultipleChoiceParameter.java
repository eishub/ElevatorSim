/*
 * Copyright 2005 Neil McKellar and Chris Dailey
 * All rights reserved.
 */
package org.intranet.ui;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Neil McKellar and Chris Dailey
 */
public final class MultipleChoiceParameter extends MultipleValueParameter {
	private static final long serialVersionUID = 1L;
	private final ChoiceParameter choiceParam;
	private Object[] selectedValues;

	public MultipleChoiceParameter(final ChoiceParameter param) {
		super(param.getKey());
		this.choiceParam = param;
	}

	@Override
	public List<Parameter> getParameterList() {
		final List<Parameter> params = new ArrayList<>();
		if (!this.isMultiple) {
			final ChoiceParameter p = new ChoiceParameter(this.choiceParam.getKey(), this.choiceParam.getLegalValues(),
					this.choiceParam.getUIValue(), this.choiceParam.getType());
			p.setValueFromUI(this.selectedValues[0]);
			params.add(p);
			return params;
		}
		for (final Object value : this.selectedValues) {
			final ChoiceParameter p = (ChoiceParameter) this.choiceParam.clone();
			p.setValueFromUI(value);
			params.add(p);
		}
		return params;
	}

	public List<?> getLegalValues() {
		return this.choiceParam.getLegalValues();
	}

	public void setChoice(final Object[] selectedValues) {
		this.selectedValues = selectedValues;
	}

	@Override
	public Object getSingleValue() {
		return this.selectedValues[0];
	}
}
