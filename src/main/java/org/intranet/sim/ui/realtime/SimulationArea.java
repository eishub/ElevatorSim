/*
 * Copyright 2003 Neil McKellar and Chris Dailey
 * All rights reserved.
 */
package org.intranet.sim.ui.realtime;

import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.intranet.sim.Model;
import org.intranet.sim.SimulationApplication;
import org.intranet.sim.Simulator;
import org.intranet.sim.Simulator.SimulatorListener;
import org.intranet.sim.clock.Clock;
import org.intranet.sim.clock.RealTimeClock;
import org.intranet.ui.InputPanel;
import org.intranet.ui.SingleValueInputPanel;

import eis.iilang.EnvironmentState;
import elevatorenv.EnvironmentInterface;
import elevatorenv.GOALController;

/**
 * @author Neil McKellar and Chris Dailey
 * @author W.Pasman to use simulator preferences. #958
 * @author W.Pasman to make separate Time Factor spinner listening to the
 *           actual clock. #1354
 */
public class SimulationArea extends JComponent {

	public JButton startButton = new JButton(); // TRAC 824
	private Simulator sim = null;
	private JComponent bView;
	private Statistics statistics;
	private JComponent leftPane = new JPanel();
	private JSplitPane rightSplitPane = new JSplitPane(
			JSplitPane.VERTICAL_SPLIT);
	private JPanel bottomPanel = new JPanel(new BorderLayout());
	ClockDisplay clockDisplay = new ClockDisplay();
	public Clock clock = null; // HACK 5jan09 to get access to clock to enable
								// EIScontrol of pause mode.

	private EventQueueDisplay eventQueueDisplay;
	private TimeFactorDial timeFactorPanel;

	public SimulationArea(Simulator simulator, SimulationApplication simApp) {
		super();
		sim = simulator;

		setLayout(new BorderLayout());

		createRightPane();

		createBottomPanel(simApp);

		createLeftPane(simApp);

		sim.addListener(new SimulatorListener() {
			public void modelUpdate(long time) {
				// TODO : The model should be responsible for telling the view
				// when it updates
				bView.repaint();
			}
		});
	}

	public Clock getClock() {
		return sim.getClock();
	}

	private void createLeftPane(final SimulationApplication simApp) {
		leftPane.setLayout(new BorderLayout());
		SingleValueInputPanel ip = new SingleValueInputPanel(
				sim.getParameters(), new InputPanel.Listener() {
					public void parametersApplied() {
						applyParameters(simApp);
					}
				});
		leftPane.add(ip, BorderLayout.NORTH);
		statistics = new Statistics();
		leftPane.add(statistics, BorderLayout.CENTER);

		add(leftPane, BorderLayout.WEST);
	}

	/**
	 * Apply button was pressed (or otherwise the parameters have been set up).
	 * Apply them to the sim.
	 * 
	 * @param simApp
	 */
	public void applyParameters(SimulationApplication simApp) {
		sim.initialize(new RealTimeClock.RealTimeClockFactory());
		reconfigureSimulation(simApp);
	}

	private void createRightPane() {
		add(rightSplitPane, BorderLayout.CENTER);
		rightSplitPane.setResizeWeight(1.0);
		rightSplitPane.setDividerLocation(425);
		eventQueueDisplay = new EventQueueDisplay();
		rightSplitPane.setRightComponent(eventQueueDisplay);
	}

	/**
	 * create the bottom panel for the sim area, containing the time factor dial
	 * and a START button.
	 * 
	 * @param simApp
	 *            is the SimulationApplication.
	 * @param preferredTimeFactor
	 *            is the initial factor to be used.
	 */
	private void createBottomPanel(final SimulationApplication simApp) {
		timeFactorPanel = new TimeFactorDial(sim);

		bottomPanel.add(timeFactorPanel, BorderLayout.EAST);

		JPanel startButtonPanel = new JPanel();
		startButtonPanel.add(startButton);
		bottomPanel.add(startButtonPanel, BorderLayout.CENTER);

		startButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				startPauseSimulation(simApp);
			}
		});
		startButton.setEnabled(false);
		add(bottomPanel, BorderLayout.SOUTH);
		updateButtonText(false);
		bottomPanel.add(clockDisplay, BorderLayout.WEST);
	}

	/**
	 * Change of run mode. Notify parent if that is a GOAL environmentinterface.
	 * 
	 * @param state
	 */
	private void stateChange(SimulationApplication simApp,
			EnvironmentState state) {
		if (simApp instanceof EnvironmentInterface) {
			((EnvironmentInterface) simApp).notifyEvent(state);
		}

	}

	/**
	 * Start or Pause the simulation, after user pressed Start/Pause button.
	 */
	private void startPauseSimulation(SimulationApplication simApp) {
		clock = sim.getClock();
		synchronized (clock) {
			if (clock.isRunning()) {
				clock.pause();
				stateChange(simApp, EnvironmentState.PAUSED);
			} else {
				// CHECK ok? We now store factor in clock anyway.
				// int factor = ((Number) spinnerNumberModel.getValue())
				// .intValue();
				// ((RealTimeClock) clock).setTimeConversion(factor);
				clock.start();
				stateChange(simApp, EnvironmentState.RUNNING);
			}
		}
	}

	private void updateButtonText(boolean running) {
		startButton.setText(running ? "Pause" : "Go, Dude!");
	}

	public void paint(Graphics g) {
		Model model = sim.getModel();
		if (model == null) {
			super.paint(g);
		} else
			synchronized (model) {
				super.paint(g);
			}
	}

	/**
	 * This is called after the user pressed "Apply" to initialize the
	 * simulator.
	 * 
	 * @param simApp
	 */
	private void reconfigureSimulation(final SimulationApplication simApp) {
		startButton.setEnabled(true);
		bView = simApp.createView(sim.getModel());
		rightSplitPane.setLeftComponent(bView);

		final Clock clock = sim.getClock();
		clock.addListener(new Clock.Listener() {
			public void timeUpdate(long time) {
			}

			public void stateUpdate(boolean running) {
				updateButtonText(running);
				if (!running)
					statistics.updateStatistics();
			}

			@Override
			public void timeFactorUpdate(int timeFactor) {
			}
		});

		clock.addListener(timeFactorPanel);

		clockDisplay.setClock(sim.getClock());
		eventQueueDisplay.initialize(sim.getEventQueue());

		statistics.setModel(sim.getModel());
		validate();

		// if we get here, we need to notify GOAL about the new run state. #1591
		boolean readyToRun = sim.getCurrentController() instanceof GOALController;
		System.out.println(sim.getCurrentController());
		stateChange(simApp, readyToRun ? EnvironmentState.PAUSED
				: EnvironmentState.INITIALIZING);
	}

	public void dispose() {
		// If the sim has not been initialized, we can't get a clock (and also
		// don't
		// really need to worry about disposing of anything). bView will be set
		// by this component shortly after calling initialize on the simulator,
		// so we can check its value.
		if (bView == null)
			return;
		Clock clock = sim.getClock();
		if (clock.isRunning())
			clock.pause();
	}

	/**
	 * Get currently active simulator in the area. Added W.Pasman 4nov2010, see
	 * #711
	 * 
	 * @return currently active simulator in the area. Returns null if no
	 *         simulator selected yet.
	 */
	public Simulator getSimulator() {
		return sim;
	}
}

/**
 * A dial to control the RealTime clock speed factor. YOU need to connect this
 * panel when the clock is created, using
 * {@link RealTimeClock#addListener(org.intranet.sim.clock.Clock.Listener)}.
 * 
 * @author W.Pasman 11nov2010, see #1354
 * 
 */
class TimeFactorDial extends JPanel implements Clock.Listener {

	final SpinnerNumberModel spinnerNumberModel;

	/**
	 * @param sim
	 *            Simulator that was selected.
	 */
	public TimeFactorDial(final Simulator sim) {
		JLabel timeFactorLabel = new JLabel("Time Factor");

		spinnerNumberModel = new SpinnerNumberModel(0, -20, 20, 1);
		JSpinner timeFactor = new JSpinner(spinnerNumberModel);
		spinnerNumberModel.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				if (!sim.isInitializied())
					return;

				int factor = ((Number) spinnerNumberModel.getValue())
						.intValue();
				// CHECK should we save the new value? I am tempted to say no
				// as user probably prefers 0 as initial value
				RealTimeClock rtClock = (RealTimeClock) sim.getClock();
				synchronized (rtClock) {
					rtClock.setTimeConversion(factor);
				}
			}
		});

		add(timeFactorLabel);
		add(timeFactor);
	}

	@Override
	public void timeUpdate(long time) {
	}

	@Override
	public void stateUpdate(boolean running) {
	}

	@Override
	public void timeFactorUpdate(int timeFactor) {
		spinnerNumberModel.setValue(timeFactor);
	}
}
