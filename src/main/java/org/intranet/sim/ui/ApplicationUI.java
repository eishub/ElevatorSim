/*
 * Copyright 2004 Neil McKellar and Chris Dailey
 * All rights reserved.
 */
package org.intranet.sim.ui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
 * 
 */
@SuppressWarnings("serial")
public class ApplicationUI extends JFrame {

	public JComponent simulationArea; // TRAC 824
	private SimulationApplication simApp;

	public ApplicationUI(final SimulationApplication sa) {
		super(sa.getApplicationName());
		simApp = sa;
		setIconImage(sa.getImageIcon());
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				close();
			}
		});

		JMenuBar mb = new JMenuBar();

		JMenu helpMenu = new JMenu("Help");
		JMenuItem helpAboutMenu = new JMenuItem("About");
		helpAboutMenu.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// Creating a dialog box with the license in it.
				// Stand-alone window, don'w worry about it.
				new AboutDialog(ApplicationUI.this, sa);
			}
		});
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
	 * @param owner
	 *            is the parent frame, used for centering
	 * @param sa
	 *            the simulation application. Seems not used
	 * @return simulator to be used
	 */
	public Simulator showSimulatorSelectionGUI(JFrame owner,
			SimulationApplication sa) {

		SimulationSelectionDialog dialog = new SimulationSelectionDialog(owner,
				sa, new SimulationSelection.Listener() {
					public void simulationSelected(Simulator sim,
							SimulationApplication app, boolean isMultiple) {
					}
				});
		return dialog.getSelectedSimulation();
	}

	/**
	 * Handle the selection of a Simulator. Sets up the main screen
	 * appropriately. After this call, the main screen shows the GUI that allows
	 * the user to select the specific settings for this simulator.
	 * 
	 * @param sim
	 *            the selected Simulator.
	 * @param app
	 *            is the SimulationApplication.
	 * @param isMultiple
	 *            true if simulation is Multiple (not yet handled properly)
	 */
	public void handleSimulationSelected(Simulator sim,
			SimulationApplication app, boolean isMultiple) {
		disposeMainScreenComponents();
		createMainScreenComponents(sim, app, isMultiple);
		// TODO : Figure out why we need to validate()
		// before the simulation area will repaint
		validate();
	}

	protected void createMainScreenComponents(Simulator sim,
			SimulationApplication app, boolean isMultiple) {
		if (isMultiple)
			simulationArea = new MultipleSimulationArea(sim, app);
		else
			simulationArea = new SimulationArea(sim, app);
		getContentPane().add(simulationArea, BorderLayout.CENTER);
	}

	protected void disposeMainScreenComponents() {
		if (simulationArea != null) {
			if (simulationArea instanceof SimulationArea)
				((SimulationArea) simulationArea).dispose();
			getContentPane().remove(simulationArea);
			simulationArea = null;
		}
	}

	/**
	 * Cleans up before closing down the elevator application. Invoked when user
	 * hits close button on window.
	 */
	private void close() {
		try {
			((EnvironmentInterface) simApp).kill();
		} catch (ManagementException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
