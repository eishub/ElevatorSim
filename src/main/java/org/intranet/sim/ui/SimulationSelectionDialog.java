/*
 * Copyright 2003 Neil McKellar and Chris Dailey
 * All rights reserved.
 */
package org.intranet.sim.ui;

import java.awt.BorderLayout;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.WindowConstants;

import org.intranet.sim.SimulationApplication;
import org.intranet.sim.Simulator;

/**
 * @author Neil McKellar and Chris Dailey
 */
public class SimulationSelectionDialog extends JDialog {
	private static final long serialVersionUID = 1L;
	private Simulator selected_simulation = null; // will be set when user selected

	public SimulationSelectionDialog(final JFrame owner, final SimulationApplication simApp,
			final SimulationSelection.Listener l) {
		super(owner, "Select a Simulation");
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setModal(true); // Added W.Pasman 9nov2010, we need an answer before
						// proceeding.
		// We need to have a way of disposing the dialog when a simulator is
		// selected. This listener intercepts the SimulationSelection listener
		// to pass on the event and also add an extra action (dispose this
		// dialog).
		final SimulationSelection.Listener newListener = (sim, app, multiple) -> {
			SimulationSelectionDialog.this.selected_simulation = sim;
			l.simulationSelected(sim, app, multiple);
			dispose();
		};
		final SimulationSelection sel = new SimulationSelection(simApp, newListener);
		getContentPane().add(sel, BorderLayout.CENTER);
		setSize(320, 200);
		setVisible(true);
	}

	/**
	 * get the selected simulation
	 *
	 * @return {@link Simulator}
	 */
	public Simulator getSelectedSimulation() {
		return this.selected_simulation;
	}
}
