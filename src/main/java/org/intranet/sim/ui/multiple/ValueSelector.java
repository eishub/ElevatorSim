/*
 * Copyright 2004 Neil McKellar and Chris Dailey
 * All rights reserved.
 */
package org.intranet.sim.ui.multiple;

import java.awt.FlowLayout;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractSpinnerModel;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JSpinner;

import org.intranet.ui.MultipleValueParameter;
import org.intranet.ui.Parameter;
import org.intranet.ui.SingleValueParameter;

class ValueSelector extends JComponent {
	private static final long serialVersionUID = 1L;
	private MultipleValueParameter param;
	private final JLabel label = new JLabel("selector");
	private JSpinner spinner;
	private SingleValueParameter selected;
	private Listener listener;

	interface Listener {
		void valueChanged();
	}

	public ValueSelector(final MultipleValueParameter rangeParam, final List<Parameter> paramList,
			final Map<Object, Parameter> paramMap, final Listener l) {
		super();
		setLayout(new FlowLayout());
		this.param = rangeParam;
		this.listener = l;
		this.selected = (SingleValueParameter) paramList.get(0);
		if (this.param.isMultiple()) {
			this.spinner = new JSpinner(new AbstractSpinnerModel() {
				private static final long serialVersionUID = 1L;

				@Override
				public Object getNextValue() {
					int currentIndex = paramList.indexOf(ValueSelector.this.selected);
					if (currentIndex == -1) {
						currentIndex = 0;
					} else if (currentIndex >= paramList.size() - 1) {
						return null;
					} else {
						currentIndex++;
					}
					return ((SingleValueParameter) paramList.get(currentIndex)).getUIValue();
				}

				@Override
				public Object getPreviousValue() {
					int currentIndex = paramList.indexOf(ValueSelector.this.selected);
					if (currentIndex == -1) {
						currentIndex = 0;
					} else if (currentIndex == 0) {
						return null;
					} else {
						currentIndex--;
					}
					return ((SingleValueParameter) paramList.get(currentIndex)).getUIValue();
				}

				@Override
				public Object getValue() {
					return ValueSelector.this.selected.getUIValue();
				}

				@Override
				public void setValue(final Object value) {
					ValueSelector.this.selected = (SingleValueParameter) paramMap.get(value);
					fireStateChanged();
					ValueSelector.this.listener.valueChanged();
				}
			});
			add(this.spinner);
		}
		add(this.label);
		setTertiary(); // by default
	}

	public void setPrimary() {
		if (!this.param.isMultiple()) {
			throw new IllegalStateException("Primary parameter must be a range parameter.");
		}
		this.label.setText("Primary");
		this.label.setVisible(true);
		this.spinner.setVisible(false);
	}

	public void setSecondary() {
		if (!this.param.isMultiple()) {
			throw new IllegalStateException("Secondary parameter must be a range parameter.");
		}
		this.label.setText("Secondary");
		this.label.setVisible(true);
		this.spinner.setVisible(false);
	}

	public void setTertiary() {
		this.label.setText(this.param.getSingleValue().toString());
		if (this.param.isMultiple()) {
			this.label.setVisible(false);
			this.spinner.setVisible(true);
		}
	}

	public void setAverage() {
		if (!this.param.isMultiple()) {
			throw new IllegalStateException("Can't setAverage on a single-value parameter");
		}
		this.label.setText("Average");
		this.label.setVisible(true);
		this.spinner.setVisible(false);
	}

	public MultipleValueParameter getParameter() {
		return this.param;
	}

	public SingleValueParameter getSelectedParameter() {
		return this.selected;
	}
}