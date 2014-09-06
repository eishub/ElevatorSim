/*
 * Copyright 2005 Neil McKellar and Chris Dailey
 * All rights reserved.
 */
package org.intranet.ui;

import java.awt.GridBagConstraints;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;

/**
 * @author Neil McKellar and Chris Dailey
 *
 */
public final class SingleValueInputPanel
    extends InputPanel
{
  public SingleValueInputPanel(List parameters, Listener l)
  {
    super(parameters, l);
  }

  protected void addParameter(Parameter p)
  {
    SingleValueParameter param = (SingleValueParameter)p;
    JLabel inputLabel = new JLabel(param.getDescription());
    JComponent inputField;
    if (param instanceof ChoiceParameter)
    {
      ChoiceParameter listParam = (ChoiceParameter)param;
      JList list = new JList(listParam.getLegalValues().toArray());
      list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      list.setSelectedValue(listParam.getChoiceValue(), true);
      inputField = list;
    }
    else
      inputField = new JTextField((String)param.getUIValue(), 10);

    members.addStuffToArrays(param, inputField);

    GridBagConstraints gbc = new GridBagConstraints();
    gbc.gridy = centerRow++;
    center.add(inputLabel, gbc);
    gbc.gridx = 1;
    center.add(inputField, gbc);
  }

  protected void copyUIToParameter(int memberIndex, JComponent field,
    Parameter param)
  {
    Object value = (param instanceof ChoiceParameter) ?
        ((JList)field).getSelectedValue() :
        ((JTextField)field).getText();
    ((SingleValueParameter)param).setValueFromUI(value);
  }
}
