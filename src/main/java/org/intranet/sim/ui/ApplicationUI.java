/*
 * Copyright 2004 Neil McKellar and Chris Dailey
 * All rights reserved.
 */
package org.intranet.sim.ui;

import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import org.intranet.sim.SimulationApplication;
import org.intranet.sim.Simulator;
import org.intranet.sim.ui.multiple.MultipleSimulationArea;
import org.intranet.sim.ui.realtime.SimulationArea;

import eis.exceptions.ManagementException;
import elevatorenv.EnvironmentInterface;

/**
 * @author Neil McKellar and Chris Dailey
 */
public class ApplicationUI extends JFrame {
	private static final long serialVersionUID = 1L;
	public JComponent simulationArea; // TRAC 824
	private final SimulationApplication simApp;

	public ApplicationUI(final SimulationApplication sa) {
		super(sa.getApplicationName());
		this.simApp = sa;
		setIconImage(sa.getImageIcon());
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(final WindowEvent e) {
				close();
			}
		});

		final JMenuBar mb = new JMenuBar();

		final JMenu helpMenu = new JMenu("Help");
		final JMenuItem helpAboutMenu = new JMenuItem("About");
		helpAboutMenu.addActionListener(e -> new AboutDialog(ApplicationUI.this, sa));
		helpMenu.add(helpAboutMenu);
		mb.add(Box.createHorizontalGlue()); // add help to the far right
		mb.add(helpMenu);

		setJMenuBar(mb);

		setSize(800, 600);
		setVisible(true);
	}

	/**
	 * returns null if user did not select a simulator.
	 *
	 * @param owner is the parent frame, used for centering
	 * @param sa    the simulation application. Seems not used
	 * @return simulator to be used
	 */
	public Simulator showSimulatorSelectionGUI(final JFrame owner, final SimulationApplication sa) {
		final SimulationSelectionDialog dialog = new SimulationSelectionDialog(owner, sa, (sim, app, isMultiple) -> {
		});
		return dialog.getSelectedSimulation();
	}

	/**
	 * Handle the selection of a Simulator. Sets up the main screen appropriately.
	 * After this call, the main screen shows the GUI that allows the user to select
	 * the specific settings for this simulator.
	 *
	 * @param sim        the selected Simulator.
	 * @param app        is the SimulationApplication.
	 * @param isMultiple true if simulation is Multiple (not yet handled properly)
	 */
	public void handleSimulationSelected(final Simulator sim, final SimulationApplication app,
			final boolean isMultiple) {
		disposeMainScreenComponents();
		createMainScreenComponents(sim, app, isMultiple);
		// TODO : Figure out why we need to validate()
		// before the simulation area will repaint
		validate();
	}

	protected void createMainScreenComponents(final Simulator sim, final SimulationApplication app,
			final boolean isMultiple) {
		if (isMultiple) {
			this.simulationArea = new MultipleSimulationArea(sim, app);
		} else {
			this.simulationArea = new SimulationArea(sim, app);
		}
		getContentPane().add(this.simulationArea, BorderLayout.CENTER);
	}

	protected void disposeMainScreenComponents() {
		if (this.simulationArea != null) {
			if (this.simulationArea instanceof SimulationArea) {
				((SimulationArea) this.simulationArea).dispose();
			}
			getContentPane().remove(this.simulationArea);
			this.simulationArea = null;
		}
	}

	/**
	 * Cleans up before closing down the elevator application. Invoked when user
	 * hits close button on window.
	 */
	private void close() {
		try {
			((EnvironmentInterface) this.simApp).kill();
		} catch (final ManagementException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
