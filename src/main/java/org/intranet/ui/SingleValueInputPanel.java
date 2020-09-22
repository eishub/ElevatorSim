/*
 * Copyright 2005 Neil McKellar and Chris Dailey
 * All rights reserved.
 */
package org.intranet.ui;

import java.awt.GridBagConstraints;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;

/**
 * @author Neil McKellar and Chris Dailey
 */
public final class SingleValueInputPanel extends InputPanel {
	private static final long serialVersionUID = 1L;

	public SingleValueInputPanel(final List<Parameter> parameters, final Listener l) {
		super(parameters, l);
	}

	@Override
	protected void addParameter(final Parameter p) {
		final SingleValueParameter param = (SingleValueParameter) p;
		final JLabel inputLabel = new JLabel(param.getDescription());
		JComponent inputField;
		if (param instanceof ChoiceParameter) {
			final ChoiceParameter listParam = (ChoiceParameter) param;
			final List<?> legalValues = listParam.getLegalValues();
			final JList<Object> list = new JList<>(legalValues.toArray(new Object[legalValues.size()]));
			list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			list.setSelectedValue(listParam.getChoiceValue(), true);
			inputField = list;
		} else {
			inputField = new JTextField((String) param.getUIValue(), 10);
		}

		// #2257 disable editing
		inputField.setEnabled(false);

		this.members.addStuffToArrays(param, inputField);

		final GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridy = this.centerRow++;
		this.center.add(inputLabel, gbc);
		gbc.gridx = 1;
		this.center.add(inputField, gbc);
	}

	@Override
	protected void copyUIToParameter(final int memberIndex, final JComponent field, final Parameter param) {
		final Object value = (param instanceof ChoiceParameter) ? ((JList<?>) field).getSelectedValue()
				: ((JTextField) field).getText();
		((SingleValueParameter) param).setValueFromUI(value);
	}
}
