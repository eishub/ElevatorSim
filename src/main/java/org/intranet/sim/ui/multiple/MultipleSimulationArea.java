/*
 * Copyright 2004 Neil McKellar and Chris Dailey
 * All rights reserved.
 */
package org.intranet.sim.ui.multiple;

import java.awt.BorderLayout;
import java.awt.Window;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import org.intranet.sim.SimulationApplication;
import org.intranet.sim.Simulator;
import org.intranet.sim.clock.Clock;
import org.intranet.sim.clock.ClockFactory;
import org.intranet.sim.clock.RealTimeClock;
import org.intranet.sim.event.Event;
import org.intranet.sim.event.EventQueue;
import org.intranet.sim.ui.realtime.SimulationArea;
import org.intranet.statistics.Column;
import org.intranet.statistics.Table;
import org.intranet.ui.ChoiceParameter;
import org.intranet.ui.ExceptionDialog;
import org.intranet.ui.MultipleChoiceParameter;
import org.intranet.ui.MultipleValueInputPanel;
import org.intranet.ui.MultipleValueParameter;
import org.intranet.ui.Parameter;
import org.intranet.ui.RangeParameter;
import org.intranet.ui.SingleValueParameter;

/**
 * @author Neil McKellar and Chris Dailey
 */
public class MultipleSimulationArea extends JComponent {
	private static final long serialVersionUID = 1L;
	private boolean foundError;
	private final Simulator sim;
	private final SimulationApplication simApp;
	private final JComponent topPanel = new JPanel();
	private ResultsSelection centerPanel;
	private ResultsTable bottomPanel;
	private List<Parameter> multiValueParams;
	private Map<List<Parameter>, List<Table>> results;
	private final Box topBox = new Box(BoxLayout.Y_AXIS);

	public MultipleSimulationArea(final Simulator simulator, final SimulationApplication app) {
		this.sim = simulator;
		this.simApp = app;

		setLayout(new BorderLayout());

		add(this.topBox, BorderLayout.NORTH);
		createTopPanel();
	}

	private void createTopPanel() {
		this.topPanel.setLayout(new BorderLayout());
		final List<Parameter> simParams = this.sim.getParameters();
		this.multiValueParams = createMultiValueParameters(simParams);
		final MultipleValueInputPanel ip = new MultipleValueInputPanel(this.multiValueParams, () -> {
			MultipleSimulationArea.this.foundError = false;
			MultipleSimulationArea.this.results = new HashMap<>();
			for (final Iterator<List<Parameter>> i = createParameterSetIterator(
					MultipleSimulationArea.this.multiValueParams); i.hasNext();) {
				final List<Parameter> params = i.next();
				updateSimulationParameters(MultipleSimulationArea.this.sim, params);
				final List<Table> statistics = startSimulation(params);
				if (MultipleSimulationArea.this.foundError) {
					break;
				}
				MultipleSimulationArea.this.results.put(params, statistics);
			}
			if (!MultipleSimulationArea.this.foundError) {
				createCenterPanel();
			}
		});
		this.topPanel.add(ip, BorderLayout.CENTER);
		this.topBox.add(this.topPanel);
	}

	private void createCenterPanel() {
		final List<StatisticVariable> statisticsVariables = new ArrayList<>();
		// Only fill the statistics variable with the headers of one set of tables,
		// otherwise the list will contain duplicates of each statistics variable
		// from each of the set of table results
		final List<Table> tables = this.results.values().iterator().next();
		for (final Table table : tables) {
			final Table t = table;
			final String tableName = t.getName();
			for (int colNum = 0; colNum < t.getColumnCount(); colNum++) {
				final Column c = t.getColumn(colNum);
				final String name = c.getHeading();
				statisticsVariables.add(new StatisticVariable(tableName, "Avg", name));
				statisticsVariables.add(new StatisticVariable(tableName, "Min", name));
				statisticsVariables.add(new StatisticVariable(tableName, "Max", name));
			}
		}

		if (this.centerPanel != null) {
			this.topBox.remove(this.centerPanel);
		}

		this.centerPanel = new ResultsSelection(this.multiValueParams, statisticsVariables, this::createBottomPanel);

		this.topBox.add(this.centerPanel);
		revalidate();
	}

	private void createBottomPanel(final MultipleValueParameter primaryVar, final MultipleValueParameter secondaryVar,
			final MultipleValueParameter averageVar, final List<Parameter> otherVariables,
			final StatisticVariable statistic) {
		if (this.bottomPanel != null) {
			remove(this.bottomPanel);
		}
		final ResultsGrid grid = new ResultsGrid(this.results, primaryVar, secondaryVar, averageVar, otherVariables,
				statistic);
		this.bottomPanel = new ResultsTable(primaryVar, secondaryVar, grid);
		this.bottomPanel.addResultsTableListener(params -> {
			final Simulator newSim = MultipleSimulationArea.this.sim.duplicate();
			// Parameters must be set before initializing the model.
			updateSimulationParameters(newSim, params);
			newSim.initialize(new RealTimeClock.RealTimeClockFactory());
			final JFrame simFrame = new JFrame("Real Time Simulation Run");
			simFrame.setIconImage(MultipleSimulationArea.this.simApp.getImageIcon());
			simFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
			final SimulationArea simulationArea = new SimulationArea(newSim, MultipleSimulationArea.this.simApp);
			simFrame.getContentPane().add(simulationArea, BorderLayout.CENTER);
			simFrame.setSize(800, 600);
			simFrame.setVisible(true);
		});
		add(this.bottomPanel, BorderLayout.CENTER);
		revalidate();
	}

	private void errorDialog(final List<Parameter> params, final Exception e) {
		this.foundError = true;
		final Window window = SwingUtilities.windowForComponent(this);
		new ExceptionDialog(window, params, e);
	}

	private void updateSimulationParameters(final Simulator updateSim, final List<Parameter> params) {
		for (final Parameter parameter : params) {
			final SingleValueParameter p = (SingleValueParameter) parameter;
			final SingleValueParameter simParameter = updateSim.getParameter(p.getDescription());
			simParameter.setValueFromUI(p.getUIValue());
		}
	}

	private List<Table> startSimulation(final List<Parameter> params) {
		// TODO : reconsider how to determine the endtime in a multiple simulation
		final long endTime = 99000000;
		final ClockFactory clockFactory = cl -> new Clock(cl) {
			@Override
			public void dispose() {
			}

			@Override
			public void pause() {
				setRunningState(false);
			}

			@Override
			public void start() {
				if (isRunning()) {
					throw new IllegalStateException("Can't start while already running");
				}
				setRunningState(true);
				setSimulationTime(endTime);
			}
		};
		// initialize the sim
		try {
			this.sim.initialize(clockFactory);
		} catch (final Exception e) {
			errorDialog(params, e);
			return null;
		}
		this.sim.getEventQueue().addListener(new EventQueue.Listener() {
			@Override
			public void eventAdded(final Event e) {
			}

			@Override
			public void eventRemoved(final Event e) {
			}

			@Override
			public void eventError(final Exception ex) {
				if (!MultipleSimulationArea.this.foundError) {
					errorDialog(params, ex);
				}
			}
		});
		// run the sim
		this.sim.getClock().start();
		return this.sim.getModel().getStatistics();
	}

	protected Iterator<List<Parameter>> createParameterSetIterator(final List<Parameter> rangeParams) {
		final List<List<Parameter>> paramListList = new ArrayList<>(rangeParams.size());
		for (final Parameter parameter : rangeParams) {
			final MultipleValueParameter rp = (MultipleValueParameter) parameter;
			paramListList.add(rp.getParameterList());
		}

		final int[] positions = new int[paramListList.size()];
		return new Iterator<List<Parameter>>() {
			boolean done = paramListList.size() == 0;

			@Override
			public List<Parameter> next() {
				if (this.done) {
					throw new NoSuchElementException("Can't next after end");
				}
				increment();
				return getCurrent();
			}

			private void increment() {
				for (int i = 0; i < paramListList.size(); i++) {
					final List<Parameter> paramList = paramListList.get(i);
					positions[i]++;
					if (positions[i] < paramList.size()) {
						return; // not done yet
					}
					positions[i] = 0;
				}
				// When the odometer rolls over, the iterator is done with
				this.done = true;
			}

			private List<Parameter> getCurrent() {
				final List<Parameter> l = new ArrayList<>(paramListList.size());
				for (int i = 0; i < paramListList.size(); i++) {
					final List<Parameter> paramList = paramListList.get(i);
					final SingleValueParameter p = (SingleValueParameter) paramList.get(positions[i]);
					l.add(p);
				}
				return l;
			}

			@Override
			public boolean hasNext() {
				return !this.done;
			}

			@Override
			public void remove() {
				throw new IllegalStateException("Can't remove from this iterator!");
			}
		};
	}

	private List<Parameter> createMultiValueParameters(final List<Parameter> simParams) {
		final List<Parameter> newParams = new ArrayList<>(simParams.size());
		for (final Parameter parameter : simParams) {
			final SingleValueParameter p = (SingleValueParameter) parameter;
			if (p instanceof ChoiceParameter) {
				newParams.add(new MultipleChoiceParameter((ChoiceParameter) p));
			} else {
				newParams.add(new RangeParameter(p));
			}
		}
		return newParams;
	}
}
