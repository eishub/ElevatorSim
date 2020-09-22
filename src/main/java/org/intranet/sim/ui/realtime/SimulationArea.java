/*
 * Copyright 2003 Neil McKellar and Chris Dailey
 * All rights reserved.
 */
package org.intranet.sim.ui.realtime;

import java.awt.BorderLayout;
import java.awt.Graphics;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.SpinnerNumberModel;

import org.intranet.sim.Model;
import org.intranet.sim.SimulationApplication;
import org.intranet.sim.Simulator;
import org.intranet.sim.clock.Clock;
import org.intranet.sim.clock.RealTimeClock;
import org.intranet.ui.SingleValueInputPanel;

import eis.iilang.EnvironmentState;
import elevatorenv.EnvironmentInterface;
import elevatorenv.GOALController;

/**
 * @author Neil McKellar and Chris Dailey
 * @author W.Pasman to use simulator preferences. #958
 * @author W.Pasman to make separate Time Factor spinner listening to the actual
 *         clock. #1354
 */
public class SimulationArea extends JComponent {
	private static final long serialVersionUID = 1L;
	public JButton startButton = new JButton(); // TRAC 824
	private Simulator sim = null;
	private JComponent bView;
	private Statistics statistics;
	private final JComponent leftPane = new JPanel();
	private final JSplitPane rightSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
	private final JPanel bottomPanel = new JPanel(new BorderLayout());
	private final ClockDisplay clockDisplay = new ClockDisplay();
	public Clock clock = null; // HACK 5jan09 to get access to clock to enable
								// EIScontrol of pause mode.
	private EventQueueDisplay eventQueueDisplay;
	private TimeFactorDial timeFactorPanel;

	public SimulationArea(final Simulator simulator, final SimulationApplication simApp) {
		super();
		this.sim = simulator;

		setLayout(new BorderLayout());

		createRightPane();

		createBottomPanel(simApp);

		createLeftPane(simApp);

		this.sim.addListener(time -> SimulationArea.this.bView.repaint());
	}

	public Clock getClock() {
		return this.sim.getClock();
	}

	private void createLeftPane(final SimulationApplication simApp) {
		this.leftPane.setLayout(new BorderLayout());
		final SingleValueInputPanel ip = new SingleValueInputPanel(this.sim.getParameters(),
				() -> applyParameters(simApp));
		this.leftPane.add(ip, BorderLayout.NORTH);
		this.statistics = new Statistics();
		this.leftPane.add(this.statistics, BorderLayout.CENTER);

		add(this.leftPane, BorderLayout.WEST);
	}

	/**
	 * Apply button was pressed (or otherwise the parameters have been set up).
	 * Apply them to the sim.
	 *
	 * @param simApp the {@link SimulationApplication} to apply the parameters to
	 */
	public void applyParameters(final SimulationApplication simApp) {
		this.sim.initialize(new RealTimeClock.RealTimeClockFactory());
		reconfigureSimulation(simApp);
	}

	private void createRightPane() {
		add(this.rightSplitPane, BorderLayout.CENTER);
		this.rightSplitPane.setResizeWeight(1.0);
		this.rightSplitPane.setDividerLocation(425);
		this.eventQueueDisplay = new EventQueueDisplay();
		this.rightSplitPane.setRightComponent(this.eventQueueDisplay);
	}

	/**
	 * create the bottom panel for the sim area, containing the time factor dial and
	 * a START button.
	 *
	 * @param simApp              is the SimulationApplication.
	 * @param preferredTimeFactor is the initial factor to be used.
	 */
	private void createBottomPanel(final SimulationApplication simApp) {
		this.timeFactorPanel = new TimeFactorDial(this.sim);

		this.bottomPanel.add(this.timeFactorPanel, BorderLayout.EAST);

		final JPanel startButtonPanel = new JPanel();
		startButtonPanel.add(this.startButton);
		this.bottomPanel.add(startButtonPanel, BorderLayout.CENTER);

		this.startButton.addActionListener(ae -> startPauseSimulation(simApp));
		this.startButton.setEnabled(false);
		add(this.bottomPanel, BorderLayout.SOUTH);
		updateButtonText(false);
		this.bottomPanel.add(this.clockDisplay, BorderLayout.WEST);
	}

	/**
	 * Change of run mode. Notify parent if that is a GOAL environmentinterface.
	 *
	 * @param state
	 */
	private void stateChange(final SimulationApplication simApp, final EnvironmentState state) {
		if (simApp instanceof EnvironmentInterface) {
			((EnvironmentInterface) simApp).notifyEvent(state);
		}
	}

	/**
	 * Start or Pause the simulation, after user pressed Start/Pause button.
	 */
	private void startPauseSimulation(final SimulationApplication simApp) {
		this.clock = this.sim.getClock();
		synchronized (this.clock) {
			if (this.clock.isRunning()) {
				this.clock.pause();
				stateChange(simApp, EnvironmentState.PAUSED);
			} else {
				// CHECK ok? We now store factor in clock anyway.
				// int factor = ((Number) spinnerNumberModel.getValue())
				// .intValue();
				// ((RealTimeClock) clock).setTimeConversion(factor);
				this.clock.start();
				stateChange(simApp, EnvironmentState.RUNNING);
			}
		}
	}

	private void updateButtonText(final boolean running) {
		this.startButton.setText(running ? "Pause" : "Go, Dude!");
	}

	@Override
	public void paint(final Graphics g) {
		final Model model = this.sim.getModel();
		if (model == null) {
			super.paint(g);
		} else {
			synchronized (model) {
				super.paint(g);
			}
		}
	}

	/**
	 * This is called after the user pressed "Apply" to initialize the simulator.
	 *
	 * @param simApp
	 */
	private void reconfigureSimulation(final SimulationApplication simApp) {
		this.startButton.setEnabled(true);
		this.bView = simApp.createView(this.sim.getModel());
		this.rightSplitPane.setLeftComponent(this.bView);

		final Clock clock = this.sim.getClock();
		clock.addListener(new Clock.Listener() {
			@Override
			public void timeUpdate(final long time) {
			}

			@Override
			public void stateUpdate(final boolean running) {
				updateButtonText(running);
				if (!running) {
					SimulationArea.this.statistics.updateStatistics();
				}
			}

			@Override
			public void timeFactorUpdate(final int timeFactor) {
			}
		});

		clock.addListener(this.timeFactorPanel);

		this.clockDisplay.setClock(this.sim.getClock());
		this.eventQueueDisplay.initialize(this.sim.getEventQueue());

		this.statistics.setModel(this.sim.getModel());
		validate();

		// if we get here, we need to notify GOAL about the new run state. #1591
		final boolean readyToRun = this.sim.getCurrentController() instanceof GOALController;
		System.out.println(this.sim.getCurrentController());
		stateChange(simApp, readyToRun ? EnvironmentState.PAUSED : EnvironmentState.INITIALIZING);
	}

	public void dispose() {
		// If the sim has not been initialized, we can't get a clock (and also
		// don't
		// really need to worry about disposing of anything). bView will be set
		// by this component shortly after calling initialize on the simulator,
		// so we can check its value.
		if (this.bView == null) {
			return;
		}
		final Clock clock = this.sim.getClock();
		if (clock.isRunning()) {
			clock.pause();
		}
	}

	/**
	 * Get currently active simulator in the area. Added W.Pasman 4nov2010, see #711
	 *
	 * @return currently active simulator in the area. Returns null if no simulator
	 *         selected yet.
	 */
	public Simulator getSimulator() {
		return this.sim;
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
	private static final long serialVersionUID = 1L;
	private final SpinnerNumberModel spinnerNumberModel;

	/**
	 * @param sim Simulator that was selected.
	 */
	public TimeFactorDial(final Simulator sim) {
		final JLabel timeFactorLabel = new JLabel("Time Factor");

		this.spinnerNumberModel = new SpinnerNumberModel(0, -20, 20, 1);
		final JSpinner timeFactor = new JSpinner(this.spinnerNumberModel);
		this.spinnerNumberModel.addChangeListener(e -> {
			if (!sim.isInitializied()) {
				return;
			}

			final int factor = ((Number) TimeFactorDial.this.spinnerNumberModel.getValue()).intValue();
			// CHECK should we save the new value? I am tempted to say no
			// as user probably prefers 0 as initial value
			final RealTimeClock rtClock = (RealTimeClock) sim.getClock();
			synchronized (rtClock) {
				rtClock.setTimeConversion(factor);
			}
		});

		add(timeFactorLabel);
		add(timeFactor);
	}

	@Override
	public void timeUpdate(final long time) {
	}

	@Override
	public void stateUpdate(final boolean running) {
	}

	@Override
	public void timeFactorUpdate(final int timeFactor) {
		this.spinnerNumberModel.setValue(timeFactor);
	}
}
