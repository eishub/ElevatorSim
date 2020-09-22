/*
 * Copyright 2005 Neil McKellar and Chris Dailey
 * All rights reserved.
 */
package org.intranet.ui;

import java.awt.GridBagConstraints;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeListener;

/**
 * @author Neil McKellar and Chris Dailey
 *
 */
public final class MultipleValueInputPanel extends InputPanel {
	private static final long serialVersionUID = 1L;

	public MultipleValueInputPanel(final List<Parameter> parameters, final Listener l) {
		super(parameters, l);
		createRangeHeaders();
	}

	private void createRangeHeaders() {
		final GridBagConstraints gbc = new GridBagConstraints();
		gbc.ipadx = 10;
		gbc.gridy = 0;
		gbc.gridx = 1;
		this.center.add(new JLabel("Base"), gbc);
		gbc.gridx = 2;
		this.center.add(new JLabel("Max"), gbc);
		gbc.gridx = 3;
		this.center.add(new JLabel("Increment"), gbc);
	}

	@Override
	protected void addParameter(final Parameter p) {
		final MultipleValueParameter param = (MultipleValueParameter) p;
		final JCheckBox inputCheckbox = new JCheckBox(param.getDescription(), param.isMultiple());

		final GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridy = this.centerRow++;
		gbc.anchor = GridBagConstraints.WEST;
		this.center.add(inputCheckbox, gbc);
		gbc.gridx = 1;

		ChangeListener cl;

		if (param instanceof RangeParameter) {
			final RangeParameter rangeParam = (RangeParameter) param;
			final JTextField baseInputField = new JTextField(rangeParam.getBaseValueAsString(), 10);

			final JTextField maxInputField = new JTextField(rangeParam.getMaxValueAsString(), 10);
			final JTextField incrementInputField = new JTextField(rangeParam.getIncrementValueAsString(), 10);
			this.members.addStuffToArrays(param, baseInputField, maxInputField, incrementInputField, inputCheckbox);

			this.center.add(baseInputField, gbc);
			gbc.gridx = 2;
			this.center.add(maxInputField, gbc);
			gbc.gridx = 3;
			this.center.add(incrementInputField, gbc);
			cl = e -> {
				final boolean isChecked = inputCheckbox.isSelected();
				maxInputField.setVisible(isChecked);
				incrementInputField.setVisible(isChecked);
				MultipleValueInputPanel.this.center.revalidate();
			};
		} else if (param instanceof MultipleChoiceParameter) {
			final MultipleChoiceParameter multiChoiceParam = (MultipleChoiceParameter) param;
			final JList<Object> list = new JList<>(multiChoiceParam.getLegalValues().toArray());
			list.setSelectionMode(param.isMultiple() ? ListSelectionModel.MULTIPLE_INTERVAL_SELECTION
					: ListSelectionModel.SINGLE_SELECTION);
			list.setSelectedIndex(0);
			this.members.addStuffToArrays(param, list, null, null, inputCheckbox);

			cl = e -> {
				final boolean isChecked = inputCheckbox.isSelected();
				if (!isChecked) {
					// There might be multiple items checked. If so,
					// make only the first item checked.
					final int selectedIdx = list.getSelectedIndices()[0];
					list.setSelectedIndex(selectedIdx);
				}
				list.setSelectionMode(isChecked ? ListSelectionModel.MULTIPLE_INTERVAL_SELECTION
						: ListSelectionModel.SINGLE_SELECTION);
			};
			gbc.gridwidth = 2;
			this.center.add(list, gbc);
		} else {
			throw new UnsupportedOperationException();
		}

		inputCheckbox.addChangeListener(cl);
		cl.stateChanged(null);
	}

	@Override
	protected void copyUIToParameter(final int memberIndex, final JComponent field, final Parameter param) {
		final JCheckBox check = this.members.getCheckboxInputField(memberIndex);
		if (param instanceof RangeParameter) {
			final RangeParameter rangeParam = (RangeParameter) param;
			rangeParam.setBaseValueFromString(((JTextField) field).getText());
			rangeParam.setMultiple(check.isSelected());
			if (check.isSelected()) {
				final String maxValue = this.members.getMaxInputField(memberIndex).getText();
				rangeParam.setMaxValueFromString(maxValue);
				final String incrValue = this.members.getIncrementInputField(memberIndex).getText();
				rangeParam.setIncrementValueFromString(incrValue);
			}
		} else if (param instanceof MultipleChoiceParameter) {
			final MultipleChoiceParameter multiChoiceParam = (MultipleChoiceParameter) param;
			final JList<?> list = (JList<?>) field;
			multiChoiceParam.setMultiple(check.isSelected());
			final List<?> selectedValues = list.getSelectedValuesList();
			multiChoiceParam.setChoice(selectedValues.toArray(new Object[selectedValues.size()]));
		} else {
			throw new UnsupportedOperationException();
		}
	}
}
