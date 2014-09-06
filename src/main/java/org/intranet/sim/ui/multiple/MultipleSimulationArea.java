/*
 * Copyright 2004 Neil McKellar and Chris Dailey
 * All rights reserved.
 */
package org.intranet.sim.ui.multiple;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Frame;
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
import org.intranet.ui.InputPanel;
import org.intranet.ui.MultipleChoiceParameter;
import org.intranet.ui.MultipleValueInputPanel;
import org.intranet.ui.MultipleValueParameter;
import org.intranet.ui.RangeParameter;
import org.intranet.ui.SingleValueParameter;

/**
 * @author Neil McKellar and Chris Dailey
 *
 */
public class MultipleSimulationArea extends JComponent
{
  private boolean foundError;
  private Simulator sim;
  private SimulationApplication simApp;
  private JComponent topPanel = new JPanel();
  private ResultsSelection centerPanel;
  private ResultsTable bottomPanel;
  private List multiValueParams;
  private Map results;
  private Box topBox = new Box(BoxLayout.Y_AXIS); 

  public MultipleSimulationArea(Simulator simulator, SimulationApplication app)
  {
    super();
    sim = simulator;
    simApp = app;
    
    setLayout(new BorderLayout());

    add(topBox, BorderLayout.NORTH);
    createTopPanel();
//    createBottomPanel();
  }

  private void createTopPanel()
  {
    topPanel.setLayout(new BorderLayout());
    List simParams = sim.getParameters();
    multiValueParams = createMultiValueParameters(simParams);
    MultipleValueInputPanel ip = new MultipleValueInputPanel(multiValueParams,
        new InputPanel.Listener()
    {
      public void parametersApplied()
      {
        foundError = false;
        results = new HashMap();
        for (Iterator i = createParameterSetIterator(multiValueParams); i.hasNext(); )
        {
          List params = (List)i.next();
          updateSimulationParameters(sim, params);
          List statistics = startSimulation(params);
          if (foundError) break;
          results.put(params, statistics);
        }
        if (!foundError)
          createCenterPanel();
      }
    });
    topPanel.add(ip, BorderLayout.CENTER);
    topBox.add(topPanel);
  }

  private void createCenterPanel()
  {
    final List statisticsVariables = new ArrayList();
    // Only fill the statistics variable with the headers of one set of tables,
    // otherwise the list will contain duplicates of each statistics variable
    // from each of the set of table results
    List tables = (List)results.values().iterator().next();
    for (Iterator tablesI = tables.iterator(); tablesI.hasNext();)
    {
      Table t = (Table)tablesI.next();
      String tableName = t.getName();
      for (int colNum = 0; colNum < t.getColumnCount(); colNum++)
      {
        Column c = t.getColumn(colNum);
        String name = c.getHeading();
        statisticsVariables.add(new StatisticVariable(tableName, "Avg", name));
        statisticsVariables.add(new StatisticVariable(tableName, "Min", name));
        statisticsVariables.add(new StatisticVariable(tableName, "Max", name));
      }
    }

    if (centerPanel != null)
      topBox.remove(centerPanel);

    centerPanel = new ResultsSelection(multiValueParams, statisticsVariables,
      new ResultsSelection.ResultsSelectionListener()
      {
        public void resultsSelected(MultipleValueParameter primaryVar,
            MultipleValueParameter secondaryVar,
            MultipleValueParameter averageVar, List otherVariables,
            StatisticVariable statistic)
        {
          createBottomPanel(primaryVar, secondaryVar, averageVar,
            otherVariables, statistic);
        }
      });

    topBox.add(centerPanel);
    revalidate();
  }

  private void createBottomPanel(MultipleValueParameter primaryVar,
      MultipleValueParameter secondaryVar, MultipleValueParameter averageVar,
      List otherVariables, StatisticVariable statistic)
  {
    if (bottomPanel != null)
      remove(bottomPanel);
    ResultsGrid grid = new ResultsGrid(results, primaryVar, secondaryVar,
        averageVar, otherVariables, statistic);
    bottomPanel = new ResultsTable(primaryVar, secondaryVar, grid);
    bottomPanel.addResultsTableListener(new ResultsTable.ResultsTableListener()
    {
      public void cellSelected(List params)
      {
        Simulator newSim = sim.duplicate();
        // Parameters must be set before initializing the model.
        updateSimulationParameters(newSim, params);
        newSim.initialize(new RealTimeClock.RealTimeClockFactory());
        JFrame simFrame = new JFrame("Real Time Simulation Run");
        simFrame.setIconImage(simApp.getImageIcon());
        simFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        SimulationArea simulationArea = new SimulationArea(newSim, simApp);
        simFrame.getContentPane().add(simulationArea, BorderLayout.CENTER);
        simFrame.setSize(800, 600);
        simFrame.setVisible(true);
      }
    });
    add(bottomPanel, BorderLayout.CENTER);
    revalidate();
  }
  
  private void errorDialog(List params, Exception e)
  {
    foundError = true;
    Window window = SwingUtilities.windowForComponent(this);
    new ExceptionDialog(window, params, e);
  }

  private void updateSimulationParameters(Simulator updateSim, List params)
  {
    for (Iterator paramsI = params.iterator(); paramsI.hasNext(); )
    {
      SingleValueParameter p = (SingleValueParameter)paramsI.next();
      SingleValueParameter simParameter = updateSim.getParameter(p.getDescription());
      simParameter.setValueFromUI(p.getUIValue());
    }
  }

  private List startSimulation(final List params)
  {
    // TODO : reconsider how to determine the endtime in a multiple simulation
    final long endTime = 99000000;
    ClockFactory clockFactory = new ClockFactory()
    {
      public Clock createClock(final Clock.FeedbackListener cl)
      {
        return new Clock(cl)
        {
          public void dispose()
          {
          }

          public void pause()
          {
            setRunningState(false);
          }

          public void start()
          {
            if (isRunning())
              throw new IllegalStateException("Can't start while already running");
            setRunningState(true);
            setSimulationTime(endTime);
          }
        };
      }
    };
    //    initialize the sim
    try
    {
      sim.initialize(clockFactory);
    }
    catch (Exception e)
    {
      errorDialog(params, e);
      return null;
    }
    sim.getEventQueue().addListener(new EventQueue.Listener()
    {
      public void eventAdded(Event e) {}
      public void eventRemoved(Event e) {}

      public void eventError(Exception ex)
      {
        if (!foundError)
          errorDialog(params, ex);
      }
    });
    //    run the sim
    sim.getClock().start();
    return sim.getModel().getStatistics();
  }

  protected Iterator createParameterSetIterator(List rangeParams)
  {
    final List paramListList = new ArrayList();
    for (Iterator rangeParamsI = rangeParams.iterator(); rangeParamsI.hasNext(); )
    {
      MultipleValueParameter rp = (MultipleValueParameter)rangeParamsI.next();
      paramListList.add(rp.getParameterList());
    }

    final int[] positions = new int[paramListList.size()];
    return new Iterator()
    {
      boolean done = paramListList.size() == 0;
      public Object next()
      {
        if (done)
          throw new NoSuchElementException("Can't next after end");
        increment();
        return getCurrent();
      }

      private void increment()
      {
        for (int i = 0; i < paramListList.size(); i++)
        {
          List paramList = (List)paramListList.get(i);
          positions[i]++;
          if (positions[i] < paramList.size())
            return;  // not done yet
          positions[i] = 0;
        }
        // When the odometer rolls over, the iterator is done with
        done = true;
      }
      private List getCurrent()
      {
        List l = new ArrayList();
        for (int i = 0; i < paramListList.size(); i++)
        {
          List paramList = (List)paramListList.get(i);
          SingleValueParameter p = (SingleValueParameter)paramList.get(positions[i]);
          l.add(p);
        }
        return l;
      }

      public boolean hasNext()
      {
        return !done;
      }

      public void remove()
      {
        throw new IllegalStateException("Can't remove from this iterator!");
      }
    }; 
  }

  private List createMultiValueParameters(List simParams)
  {
    List newParams = new ArrayList(simParams.size());
    for (Iterator i = simParams.iterator(); i.hasNext();)
    {
      SingleValueParameter p = (SingleValueParameter)i.next();
      if (p instanceof ChoiceParameter)
        newParams.add(new MultipleChoiceParameter((ChoiceParameter)p));
      else
        newParams.add(new RangeParameter(p));
    }
    return newParams;
  }
}
