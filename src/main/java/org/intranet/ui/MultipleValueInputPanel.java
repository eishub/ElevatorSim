/*
 * Copyright 2005 Neil McKellar and Chris Dailey
 * All rights reserved.
 */
package org.intranet.ui;

import java.awt.GridBagConstraints;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * @author Neil McKellar and Chris Dailey
 *
 */
public final class MultipleValueInputPanel
    extends InputPanel
{
  public MultipleValueInputPanel(List parameters, Listener l)
  {
    super(parameters, l);
    createRangeHeaders();
  }

  private void createRangeHeaders()
  {
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.ipadx = 10;
    gbc.gridy = 0;
    gbc.gridx = 1;
    center.add(new JLabel("Base"), gbc);
    gbc.gridx = 2;
    center.add(new JLabel("Max"), gbc);
    gbc.gridx = 3;
    center.add(new JLabel("Increment"), gbc);
  }

  protected void addParameter(Parameter p)
  {
    MultipleValueParameter param = (MultipleValueParameter)p;
    final JCheckBox inputCheckbox =
      new JCheckBox(param.getDescription(), param.isMultiple());

    GridBagConstraints gbc = new GridBagConstraints();
    gbc.gridy = centerRow++;
    gbc.anchor = GridBagConstraints.WEST;
    center.add(inputCheckbox, gbc);
    gbc.gridx = 1;

    ChangeListener cl;

    if (param instanceof RangeParameter)
    {
      RangeParameter rangeParam = (RangeParameter)param;
      JTextField baseInputField =
        new JTextField(rangeParam.getBaseValueAsString(), 10);

      final JTextField maxInputField =
        new JTextField(rangeParam.getMaxValueAsString(), 10);
      final JTextField incrementInputField =
        new JTextField(rangeParam.getIncrementValueAsString(), 10);
      members.addStuffToArrays(param, baseInputField, maxInputField,
          incrementInputField, inputCheckbox);

      center.add(baseInputField, gbc);
      gbc.gridx = 2;
      center.add(maxInputField, gbc);
      gbc.gridx = 3;
      center.add(incrementInputField, gbc);
      cl = new ChangeListener()
      {
        public void stateChanged(ChangeEvent e)
        {
          boolean isChecked = inputCheckbox.isSelected();
          maxInputField.setVisible(isChecked);
          incrementInputField.setVisible(isChecked);
          center.revalidate();
        }
      };
    }
    else if (param instanceof MultipleChoiceParameter)
    {
      MultipleChoiceParameter multiChoiceParam = (MultipleChoiceParameter)param;
      final JList list = new JList(multiChoiceParam.getLegalValues().toArray());
      list.setSelectionMode(param.isMultiple() ?
        ListSelectionModel.MULTIPLE_INTERVAL_SELECTION :
        ListSelectionModel.SINGLE_SELECTION);
      list.setSelectedIndex(0);
      members.addStuffToArrays(param, list, null, null, inputCheckbox);

      cl = new ChangeListener()
      {
        public void stateChanged(ChangeEvent e)
        {
          boolean isChecked = inputCheckbox.isSelected();
          if (!isChecked)
          {
            // There might be multiple items checked.  If so,
            // make only the first item checked.
            int selectedIdx = list.getSelectedIndices()[0];
            list.setSelectedIndex(selectedIdx);
          }
          list.setSelectionMode(isChecked ?
              ListSelectionModel.MULTIPLE_INTERVAL_SELECTION :
              ListSelectionModel.SINGLE_SELECTION);
        }
      };
      gbc.gridwidth = 2;
      center.add(list, gbc);
    }
    else
      throw new UnsupportedOperationException();

    inputCheckbox.addChangeListener(cl);
    cl.stateChanged(null);
  }

  protected void copyUIToParameter(int memberIndex, JComponent field,
    Parameter param)
  {
    JCheckBox check = members.getCheckboxInputField(memberIndex);
    if (param instanceof RangeParameter)
    {
      RangeParameter rangeParam = (RangeParameter)param;
      rangeParam.setBaseValueFromString(((JTextField)field).getText());
      rangeParam.setMultiple(check.isSelected());
      if (check.isSelected())
      {
        String maxValue = members.getMaxInputField(memberIndex).getText();
        rangeParam.setMaxValueFromString(maxValue);
        String incrValue = members.getIncrementInputField(memberIndex).getText();
        rangeParam.setIncrementValueFromString(incrValue);
      }
    }
    else if (param instanceof MultipleChoiceParameter)
    {
      MultipleChoiceParameter multiChoiceParam = (MultipleChoiceParameter)param;
      JList list = (JList)field;
      multiChoiceParam.setMultiple(check.isSelected());
      
      multiChoiceParam.setChoice(list.getSelectedValues());
    }
    else
      throw new UnsupportedOperationException();
  }
}
