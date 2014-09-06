/*
* Copyright 2004 Neil McKellar and Chris Dailey
* All rights reserved.
*/
package org.intranet.sim.ui.multiple;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import org.intranet.ui.MultipleValueParameter;
import org.intranet.ui.SingleValueParameter;

/**
 * @author Neil McKellar and Chris Dailey
 */
public class ResultsSelection
  extends JPanel
{
  private JComboBox secondaryChooser;
  private JComboBox primaryChooser;
  private JComboBox statisticsChooser;
  private JComboBox averageChooser;
  private List statisticsVariables;
  private List valueSelectors = new ArrayList();
  private List listeners = new ArrayList(); 

  static interface ResultsSelectionListener
  {
    void resultsSelected(MultipleValueParameter primaryVar,
        MultipleValueParameter secondaryVar,
        MultipleValueParameter averageVar,
        List otherVariables, StatisticVariable statistic);
  }

  void addResultsSelectionListener(ResultsSelectionListener l)
  {
    listeners.add(l);
  }

  public ResultsSelection(final List rangeParams,
    List statisticsVariables, ResultsSelectionListener l)
  {
    super();
    this.statisticsVariables = statisticsVariables;
    if (l != null)
      addResultsSelectionListener(l);

    setLayout(new BorderLayout());

    Box selectionPanel = new Box(BoxLayout.Y_AXIS);

    Box chooserPanel = new Box(BoxLayout.X_AXIS);
    JComponent chooserBox = createChoosers(rangeParams);
    chooserPanel.add(chooserBox);

    // TODO: Add a color chooser for the graph
    JComponent spinnerPanel = createSpinners(rangeParams);
    chooserPanel.add(spinnerPanel);
    selectionPanel.add(chooserPanel);
    updateValueSelectors();

    add(selectionPanel, BorderLayout.NORTH);
  }

  /**
   * Notifies ResultsSelectionLister (which is the spinners on the right
   * hand side of the UI) that the primary/secondary/average selections have
   * changed.
   */
  private void variablesUpdated()
  {
    MultipleValueParameter primaryVar =
      (MultipleValueParameter)primaryChooser.getSelectedItem();
    MultipleValueParameter secondaryVar =
      (MultipleValueParameter)secondaryChooser.getSelectedItem();
    MultipleValueParameter averageVar =
      averageChooser.getSelectedItem() instanceof String ? null :
      (MultipleValueParameter)averageChooser.getSelectedItem();
    List otherParameters = new ArrayList();
    for (Iterator i = valueSelectors.iterator(); i.hasNext(); )
    {
      ValueSelector vs = (ValueSelector)i.next();
      boolean isPrimaryVariable = primaryVar != null &&
        vs.getParameter().getDescription().equals(primaryVar.getDescription());
      boolean isSecondaryVariable = secondaryVar != null &&
        vs.getParameter().getDescription().equals(secondaryVar.getDescription());
      boolean isAverageVariable = averageVar != null &&
        vs.getParameter().getDescription().equals(averageVar.getDescription());
      if (isPrimaryVariable || isSecondaryVariable)
        continue;
      otherParameters.add(vs.getSelectedParameter());
    }
    for (Iterator i = listeners.iterator(); i.hasNext(); )
    {
      ResultsSelectionListener l = (ResultsSelectionListener)i.next();
      l.resultsSelected(primaryVar, secondaryVar, averageVar,
          otherParameters,
          (StatisticVariable)statisticsChooser.getSelectedItem());
    }
  }

  private JComponent createSpinners(final List rangeParams)
  {
    // make spinners for all the parameters
    JPanel spinnerPanel = new JPanel(new GridBagLayout());
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.gridy = 0;

    for (Iterator i = rangeParams.iterator(); i.hasNext();)
    {
      MultipleValueParameter p = (MultipleValueParameter)i.next();
      final List paramList = p.getParameterList();
      final Map paramMap = new HashMap();
      for (Iterator paramI = paramList.iterator(); paramI.hasNext();)
      {
        SingleValueParameter param = (SingleValueParameter)paramI.next();
        paramMap.put(param.getUIValue(), param);
      }
      JLabel pName = new JLabel(p.getDescription());
      ValueSelector pValue = new ValueSelector(p, paramList, paramMap,
        new ValueSelector.Listener()
        {
          public void valueChanged()
          {
            variablesUpdated();
          }
        });
      valueSelectors.add(pValue);
      gbc.gridx = 0;
      gbc.anchor = GridBagConstraints.WEST;
      spinnerPanel.add(pName, gbc);
      gbc.gridx = 1;
      gbc.anchor = GridBagConstraints.EAST;
      spinnerPanel.add(pValue, gbc);
      gbc.gridy++;
    }
    return spinnerPanel;
  }

  private JComponent createChoosers(List rangeParamsInitial)
  {
    final List multiValueParams = new ArrayList();
    for (Iterator i = rangeParamsInitial.iterator(); i.hasNext(); )
    {
      MultipleValueParameter p = (MultipleValueParameter)i.next();
      if (p.isMultiple())
        multiValueParams.add(p);
    }

    JPanel chooserBox = new JPanel(new GridBagLayout());
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.gridx = 0; gbc.gridy = 0;
    gbc.anchor = GridBagConstraints.WEST;
    JLabel primaryLabel = new JLabel("Primary Variable");
    chooserBox.add(primaryLabel, gbc);
    ComboBoxModel primaryComboBoxModel =
      new DefaultComboBoxModel(multiValueParams.toArray());
    if (multiValueParams.size() > 0)
      primaryComboBoxModel.setSelectedItem(multiValueParams.get(0));
    
    primaryChooser = new JComboBox(primaryComboBoxModel);
    gbc.gridx = 1;
    gbc.anchor = GridBagConstraints.EAST;
    chooserBox.add(primaryChooser, gbc);
    if (multiValueParams.size() == 0)
    {
      primaryChooser.setVisible(false);
      primaryLabel.setVisible(false);
    }
    if (multiValueParams.size() == 1)
    {
      primaryChooser.setVisible(false);
      String description = ((MultipleValueParameter)multiValueParams.get(0)).getDescription();
      chooserBox.add(new JLabel(description), gbc);
    }

    gbc.gridx = 0; gbc.gridy++;
    gbc.anchor = GridBagConstraints.WEST;
    JLabel secondaryLabel = new JLabel("Secondary Variable");
    chooserBox.add(secondaryLabel, gbc);
    final SecondaryComboBoxModel secondaryComboBoxModel =
      new SecondaryComboBoxModel(
        multiValueParams, primaryChooser.getSelectedItem());
    secondaryChooser = new JComboBox(secondaryComboBoxModel);
    gbc.gridx = 1;
    gbc.anchor = GridBagConstraints.EAST;
    chooserBox.add(secondaryChooser, gbc);
    if (multiValueParams.size() < 2)
    {
      secondaryChooser.setVisible(false);
      secondaryLabel.setVisible(false);
    }

    // make stats chooser and button

    gbc.gridx = 0; gbc.gridy++;
    gbc.anchor = GridBagConstraints.WEST;
    chooserBox.add(new JLabel("Statistics Measurement"), gbc);
    statisticsChooser = new JComboBox(new ComboBoxModel()
    {
      Object selected = (statisticsVariables.size() > 0) ? statisticsVariables
          .get(0) : null;

      public Object getSelectedItem()
      {
        return selected;
      }

      public void setSelectedItem(Object value)
      {
        selected = value;
        variablesUpdated();
      }

      public int getSize()
      {
        return statisticsVariables.size();
      }

      public Object getElementAt(int index)
      {
        return statisticsVariables.get(index);
      }

      public void addListDataListener(ListDataListener arg0)
      {
      }

      public void removeListDataListener(ListDataListener arg0)
      {
      }
    });
    gbc.gridx = 1;
    gbc.anchor = GridBagConstraints.EAST;
    chooserBox.add(statisticsChooser, gbc);

    gbc.gridx = 0; gbc.gridy++;
    gbc.anchor = GridBagConstraints.WEST;
    chooserBox.add(new JLabel("Averaging"), gbc);
    gbc.gridx = 1;
    gbc.anchor = GridBagConstraints.EAST;
    final AverageComboBoxModel averageComboBoxModel = new AverageComboBoxModel(
        multiValueParams, primaryChooser.getSelectedItem(),
        secondaryChooser.getSelectedItem());
    averageChooser = new JComboBox(averageComboBoxModel);
    chooserBox.add(averageChooser, gbc);

    averageChooser.addItemListener(new ItemListener()
    {
      public void itemStateChanged(ItemEvent arg0)
      {
        updateValueSelectors();
      }
    });

    secondaryChooser.addItemListener(new ItemListener()
    {
      public void itemStateChanged(ItemEvent arg0)
      {
        MultipleValueParameter primary =
          (MultipleValueParameter)primaryChooser.getSelectedItem();
        MultipleValueParameter secondary =
          (MultipleValueParameter)secondaryChooser.getSelectedItem();
        averageComboBoxModel.validateValues(primary, secondary);
        updateValueSelectors();
      }
    });

    primaryChooser.addItemListener(new ItemListener()
    {
      public void itemStateChanged(ItemEvent arg0)
      {
        MultipleValueParameter primary =
          (MultipleValueParameter)primaryChooser.getSelectedItem();
        MultipleValueParameter secondary =
          (MultipleValueParameter)secondaryChooser.getSelectedItem();
        secondaryComboBoxModel.validateValues(primary);
        averageComboBoxModel.validateValues(primary, secondary);
        updateValueSelectors();
      }
    });

    return chooserBox;
  }
  
  private void updateValueSelectors()
  {
    MultipleValueParameter primary = (MultipleValueParameter)primaryChooser.getSelectedItem();
    MultipleValueParameter secondary = (MultipleValueParameter)secondaryChooser.getSelectedItem();
    MultipleValueParameter average =
      averageChooser.getSelectedItem() instanceof String ?
          null : (MultipleValueParameter)averageChooser.getSelectedItem();
    for(Iterator i = valueSelectors.iterator(); i.hasNext();)
    {
      ValueSelector selector = (ValueSelector)i.next();
      if (selector.getParameter() == primary)
        selector.setPrimary();
      else if (selector.getParameter() == secondary)
        selector.setSecondary();
      else if (selector.getParameter() == average)
        selector.setAverage();
      else
        selector.setTertiary();
    }
    variablesUpdated();
  }

  /**
   * @param spinnerValues Maps Parameter to JSpinner
   * @param parameterDescription The description for the parameter
   * @return the value of the spinner if the parameterDescription matches, else
   *         null meaning the parameterDescription does not match.
   */
  static String getSpinnerValue(Map spinnerValues, String parameterDescription)
  {
    for (Iterator entryIterator = spinnerValues.entrySet().iterator();
         entryIterator.hasNext(); )
    {
      Map.Entry entry = (Entry)entryIterator.next();
      SingleValueParameter param = (SingleValueParameter)entry.getKey();
      if (parameterDescription.equals(param.getDescription()))
      {
        JSpinner spinner = (JSpinner)entry.getValue();
        return (String)spinner.getValue();  
      }
    }
    return null;
  }

  private class AverageComboBoxModel
    implements ComboBoxModel
  {
    private List rangeParams;

    private List currentParams = new ArrayList();

    private Object selected;

    private List listeners = new ArrayList();

    // TODO: handle the case where no secondary var is selected
    public AverageComboBoxModel(List params, Object primarySelected,
        Object secondarySelected)
    {
      super();
      rangeParams = params;
      updateCurrentList(primarySelected, secondarySelected);
      selected = "None";
//      if (currentParams.size() > 0)
//        selected = currentParams.get(0);
    }

    private void updateCurrentList(Object primarySelected,
        Object secondarySelected)
    {
      currentParams.clear();
      for (Iterator i = rangeParams.iterator(); i.hasNext();)
      {
        Object o = i.next();
        if (!o.equals(primarySelected) && !o.equals(secondarySelected))
          currentParams.add(o);
      }
    }

    public Object getSelectedItem()
    {
      return selected;
    }

    public void setSelectedItem(Object index)
    {
      selected = index;
      // At least the secondary (and maybe the primary) has changed; update the selectors 
    }

    public int getSize()
    {
      return currentParams.size() + 1;
    }

    public Object getElementAt(int index)
    {
      if (index == 0)
        return "None";
      return currentParams.get(index - 1);
    }

    public void addListDataListener(ListDataListener l)
    {
      listeners.add(l);
    }

    public void removeListDataListener(ListDataListener l)
    {
      listeners.remove(l);
    }

    public void validateValues(MultipleValueParameter primary,
      MultipleValueParameter secondary)
    {
      updateCurrentList(primary, secondary);
      // if the current selection is no longer in the list, default to the first
      if (!currentParams.contains(selected))
        averageChooser.setSelectedIndex(0);
      for (Iterator i = listeners.iterator(); i.hasNext();)
      {
        ListDataListener l = (ListDataListener)i.next();
        l.contentsChanged(new ListDataEvent(this,
            ListDataEvent.CONTENTS_CHANGED, 0, currentParams.size()));
      }
    }
  }

  private class SecondaryComboBoxModel
    implements ComboBoxModel
  {
    private List rangeParams;

    private List currentParams = new ArrayList();

    private Object selected;

    private List listeners = new ArrayList();

    // TODO: handle the case where no secondary var is selected
    public SecondaryComboBoxModel(List params, Object primarySelected)
    {
      super();
      rangeParams = params;
      updateCurrentList(primarySelected);
      if (currentParams.size() > 0)
        selected = currentParams.get(0);
    }

    private void updateCurrentList(Object primarySelected)
    {
      currentParams.clear();
      for (Iterator i = rangeParams.iterator(); i.hasNext();)
      {
        Object o = i.next();
        if (!o.equals(primarySelected)) currentParams.add(o);
      }
    }

    public Object getSelectedItem()
    {
      return selected;
    }

    public void setSelectedItem(Object index)
    {
      selected = index;
      // At least the secondary (and maybe the primary) has changed; update the selectors 
      updateValueSelectors();
    }

    public int getSize()
    {
      return currentParams.size();
    }

    public Object getElementAt(int index)
    {
      return currentParams.get(index);
    }

    public void addListDataListener(ListDataListener l)
    {
      listeners.add(l);
    }

    public void removeListDataListener(ListDataListener l)
    {
      listeners.remove(l);
    }

    public void validateValues(MultipleValueParameter primaryParameter)
    {
      if (currentParams.contains(primaryParameter))
      {
        updateCurrentList(primaryParameter);
        for (Iterator i = listeners.iterator(); i.hasNext();)
        {
          ListDataListener l = (ListDataListener)i.next();
          l.contentsChanged(new ListDataEvent(this,
              ListDataEvent.CONTENTS_CHANGED, 0, currentParams.size()));
        }
        if (!currentParams.contains(selected))
          secondaryChooser.setSelectedIndex(0);
      }
    }
  }
}
