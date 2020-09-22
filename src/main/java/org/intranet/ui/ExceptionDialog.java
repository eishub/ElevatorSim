/*
 * Copyright 2004 Neil McKellar and Chris Dailey
 * All rights reserved.
 */
package org.intranet.ui;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

/**
 * @author Neil McKellar and Chris Dailey
 */
public class ExceptionDialog extends JDialog {
	private static final long serialVersionUID = 1L;

	private ExceptionDialog(final Frame owner, final List<Parameter> params, final Exception e) {
		super(owner, "Error");
		createContents(params, e);
	}

	private ExceptionDialog(final Dialog owner, final List<Parameter> params, final Exception e) {
		super(owner, "Error");
		createContents(params, e);
	}

	public ExceptionDialog(final Window window, final List<Parameter> params, final Exception e) {
		e.printStackTrace(System.err);
		if (window instanceof Frame) {
			new ExceptionDialog((Frame) window, params, e);
		} else if (window instanceof Dialog) {
			new ExceptionDialog((Dialog) window, params, e);
		} else {
			throw new IllegalStateException("Invalid parent window type: " + window.getClass().getName());
		}
	}

	private void createContents(final List<Parameter> params, final Exception e) {
		// display the parameters (name, value)
		final JPanel paramPanel = displayParams(params);
		getContentPane().add(paramPanel, BorderLayout.NORTH);

		// display the exeception (class, message?)

		final JPanel exceptionPanel = displayError(e);
		getContentPane().add(exceptionPanel, BorderLayout.CENTER);

		// ok button
		final JPanel okPanel = displayButton();
		getContentPane().add(okPanel, BorderLayout.SOUTH);
		pack();
		setVisible(true);
	}

	private JPanel displayParams(final List<Parameter> params) {
		final JPanel paramPanel = new JPanel(new GridBagLayout());
		final GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridy = 1;
		for (final Parameter parameter : params) {
			final SingleValueParameter p = (SingleValueParameter) parameter;
			final JLabel desc = new JLabel(p.getDescription());
			final JLabel value = new JLabel(p.getUIValue().toString());
			gbc.gridx = 0;
			paramPanel.add(desc, gbc);
			gbc.gridx = 1;
			paramPanel.add(value, gbc);
			gbc.gridy++;
		}

		return paramPanel;
	}

	private JPanel displayError(final Exception e) {
		final JPanel errorPanel = new JPanel(new BorderLayout());
		final JLabel type = new JLabel(e.getClass().getName());
		final JLabel error = new JLabel(e.getMessage());

		errorPanel.add(type, BorderLayout.NORTH);
		errorPanel.add(error, BorderLayout.SOUTH);

		errorPanel.setBorder(new EmptyBorder(5, 5, 5, 5));

		return errorPanel;
	}

	private JPanel displayButton() {
		final JPanel okPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));

		final JButton apply = new JButton("OK");
		okPanel.add(apply);
		getContentPane().add(okPanel, BorderLayout.SOUTH);
		apply.addActionListener(ae -> dispose());

		return okPanel;
	}
}
