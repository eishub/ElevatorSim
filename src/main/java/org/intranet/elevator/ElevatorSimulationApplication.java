/*
 * Copyright 2004-2005 Neil McKellar and Chris Dailey
 * All rights reserved.
 */
package org.intranet.elevator;

import java.awt.Image;
import java.awt.Toolkit;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

import javax.swing.JComponent;

import org.intranet.elevator.model.operate.Building;
import org.intranet.elevator.model.operate.controller.Controller;
import org.intranet.elevator.model.operate.controller.ManualController;
import org.intranet.elevator.model.operate.controller.MetaController;
import org.intranet.elevator.model.operate.controller.SimpleController;
import org.intranet.elevator.view.BuildingView;
import org.intranet.sim.Model;
import org.intranet.sim.SimulationApplication;
import org.intranet.sim.Simulator;
import org.intranet.sim.ui.ApplicationUI;

/**
 * @author Neil McKellar and Chris Dailey
 * @author W.Pasman 9nov2010
 * 
 */
public class ElevatorSimulationApplication implements SimulationApplication {
	private Image iconImage;

	/**
	 * Fix, to store the new preferences.
	 */
	static Preferences myPreferences = Preferences
			.userNodeForPackage(ElevatorSimulationApplication.class);

	public static void main(String[] args) {

		ElevatorSimulationApplication sc = new ElevatorSimulationApplication();

		// new SimulationSelection(sc);
		new ApplicationUI(sc);
	}

	public ElevatorSimulationApplication() {
		super();
	}

	public JComponent createView(Model m) {
		return new BuildingView((Building) m);
	}

	public List<Simulator> getSimulations() {
		ArrayList<Controller> controllers = new ArrayList<Controller>();
		controllers.add(new MetaController());
		controllers.add(new SimpleController());
		controllers.add(new ManualController());

		List<Simulator> simulations = new ArrayList<Simulator>();
		simulations.add(new RandomElevatorSimulator(controllers));
		simulations.add(new MorningTrafficElevatorSimulator(controllers));
		simulations.add(new EveningTrafficElevatorSimulator(controllers));
		simulations.add(new ThreePersonBugSimulator(controllers));
		simulations.add(new ThreePersonElevatorSimulator(controllers));
		simulations.add(new UpToFourThenDownSimulator(controllers));
		simulations.add(new NoIdleElevatorCarSimulator(controllers));
		simulations.add(new ThreePersonTwoElevatorSimulator(controllers));
		return simulations;
	}

	public String getApplicationName() {
		return "Elevator Simulator";
	}

	public String getCopyright() {
		return "Copyright 2004-2005 Chris Dailey & Neil McKellar";
	}

	public String getVersion() {
		return "0.4";
	}

	public Image getImageIcon() {
		if (iconImage == null) {
			URL iconImageURL = ElevatorSimulationApplication.class
					.getResource("icon.gif");
			iconImage = Toolkit.getDefaultToolkit().createImage(iconImageURL);
		}
		return iconImage;
	}
}
