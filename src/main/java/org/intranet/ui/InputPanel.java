/*
 * Copyright 2003 Neil McKellar and Chris Dailey
 * All rights reserved.
 */
package org.intranet.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * @author Neil McKellar and Chris Dailey
 */
public abstract class InputPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	protected JPanel center = new JPanel(new GridBagLayout());
	protected int centerRow = 1; // skip the header row
	protected MemberArrays members = new MemberArrays();

	protected static class MemberArrays {
		private final List<JComponent> baseInputFields = new ArrayList<>();
		private final List<JComponent> maxInputFields = new ArrayList<>();
		private final List<JComponent> incrementInputFields = new ArrayList<>();
		private final List<JComponent> checkboxInputFields = new ArrayList<>();
		private final List<Parameter> inputParams = new ArrayList<>();

		void addStuffToArrays(final Parameter p, final JComponent base, final JComponent max,
				final JComponent increment, final JComponent checkbox) {
			this.baseInputFields.add(base);
			this.maxInputFields.add(max);
			this.incrementInputFields.add(increment);
			this.checkboxInputFields.add(checkbox);
			this.inputParams.add(p);
		}

		void addStuffToArrays(final Parameter p, final JComponent base) {
			addStuffToArrays(p, base, null, null, null);
		}

		int getSize() {
			return this.inputParams.size();
		}

		Parameter getParameter(final int i) {
			return this.inputParams.get(i);
		}

		List<Parameter> getParameters() {
			return this.inputParams;
		}

		JComponent getBaseInputField(final int i) {
			return this.baseInputFields.get(i);
		}

		JTextField getMaxInputField(final int i) {
			return (JTextField) this.maxInputFields.get(i);
		}

		JTextField getIncrementInputField(final int i) {
			return (JTextField) this.incrementInputFields.get(i);
		}

		JCheckBox getCheckboxInputField(final int i) {
			return (JCheckBox) this.checkboxInputFields.get(i);
		}
	}

	protected abstract void copyUIToParameter(int memberIndex, JComponent field, Parameter param);

	private final List<Listener> listeners = new ArrayList<>();

	public interface Listener {
		void parametersApplied();
	}

	private void addListener(final Listener l) {
		this.listeners.add(l);
	}

	public void removeListener(final Listener l) {
		this.listeners.remove(l);
	}

	private InputPanel() {
		setLayout(new BorderLayout());
		final JPanel centered = new JPanel(new FlowLayout(FlowLayout.CENTER));
		add(centered, BorderLayout.SOUTH);
		add(this.center, BorderLayout.CENTER);
	}

	protected InputPanel(final List<Parameter> parameters, final Listener l) {
		this();
		for (final Parameter parameter : parameters) {
			final Parameter p = parameter;
			addParameter(p);
		}
		addListener(l);
	}

	protected abstract void addParameter(Parameter p);

	public void applyParameters() {
		for (final Listener listener : this.listeners) {
			final Listener l = listener;
			l.parametersApplied();
		}
	}
}
