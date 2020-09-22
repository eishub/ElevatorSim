/*
 * Copyright 2003 Neil McKellar and Chris Dailey
 * All rights reserved.
 */
package org.intranet.sim.ui;

import java.awt.BorderLayout;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListModel;

import org.intranet.sim.SimulationApplication;
import org.intranet.sim.Simulator;

/**
 * @author Neil McKellar and Chris Dailey
 */
public final class SimulationSelection extends JPanel {
	private static final long serialVersionUID = 1L;
	private final JList<String> simulationJList = new JList<>();
	private List<Simulator> simulations;
	private SimulationApplication simulationApp;
	private Listener listener;

	interface Listener {
		void simulationSelected(Simulator sim, SimulationApplication app, boolean multiple);
	}

	public SimulationSelection(final SimulationApplication simApp, final Listener l) {
		super();
		setLayout(new BorderLayout());
		this.listener = l;
		this.simulationApp = simApp;
		this.simulations = simApp.getSimulations();

		final JScrollPane scrollPane = new JScrollPane(this.simulationJList);
		add(scrollPane, BorderLayout.CENTER);

		final JPanel buttonPanel = new JPanel();
		add(buttonPanel, BorderLayout.SOUTH);

		final JButton realtimeButton = new JButton("Real-Time");
		buttonPanel.add(realtimeButton);

		final JButton multipleButton = new JButton("Multiple");
		buttonPanel.add(multipleButton);

		final ListModel<String> listModel = new AbstractListModel<String>() {
			private static final long serialVersionUID = 9011825828997455707L;

			@Override
			public String getElementAt(final int arg0) {
				return SimulationSelection.this.simulations.get(arg0).getDescription();
			}

			@Override
			public int getSize() {
				return SimulationSelection.this.simulations.size();
			}
		};

		realtimeButton.addActionListener(arg0 -> {
			final int index = SimulationSelection.this.simulationJList.getSelectedIndex();
			apply(index, false);
		});

		multipleButton.addActionListener(arg0 -> {
			final int index = SimulationSelection.this.simulationJList.getSelectedIndex();
			apply(index, true);
		});

		this.simulationJList.setModel(listModel);
		this.simulationJList.setSelectedIndex(0);
	}

	private void apply(final int index, final boolean isMultiple) {
		if (index < 0) {
			return;
		}

		final Simulator sim = this.simulations.get(index);
		this.listener.simulationSelected(sim, this.simulationApp, isMultiple);
	}
}
