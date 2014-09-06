/*
 * Copyright 2003 Neil McKellar and Chris Dailey
 * All rights reserved.
 */
package org.intranet.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

/**
 * @author Neil McKellar and Chris Dailey
 *
 */
public abstract class InputPanel
  extends JPanel
{
  protected JPanel center = new JPanel(new GridBagLayout());
  protected int centerRow = 1; // skip the header row
  protected MemberArrays members = new MemberArrays();

  protected class MemberArrays
  {
    private List baseInputFields = new ArrayList();
    private List maxInputFields = new ArrayList();
    private List incrementInputFields = new ArrayList();
    private List checkboxInputFields = new ArrayList();
    private List inputParams = new ArrayList();

    void addStuffToArrays(Parameter p, JComponent base, JComponent max,
      JComponent increment, JComponent checkbox)
    {
      baseInputFields.add(base);
      maxInputFields.add(max);
      incrementInputFields.add(increment);
      checkboxInputFields.add(checkbox);
      inputParams.add(p);
    }

    void addStuffToArrays(Parameter p, JComponent base)
    {
      addStuffToArrays(p, base, null, null, null);
    }
    int getSize() { return inputParams.size(); }
    Parameter getParameter(int i) { return (Parameter)inputParams.get(i); }
    List getParameters() { return inputParams; }
    JComponent getBaseInputField(int i)
    { return (JComponent)baseInputFields.get(i); }
    JTextField getMaxInputField(int i)
    { return (JTextField)maxInputFields.get(i); }
    JTextField getIncrementInputField(int i)
    { return (JTextField)incrementInputFields.get(i); }
    JCheckBox getCheckboxInputField(int i)
    { return (JCheckBox)checkboxInputFields.get(i); }

    private void copyUIToParameters()
    {
      for (int i = 0; i < getSize(); i++)
      {
        JComponent field = getBaseInputField(i);
        Parameter param = getParameter(i);
        copyUIToParameter(i, field, param);
      }
    }
  }

  protected abstract void copyUIToParameter(int memberIndex, JComponent field,
    Parameter param);

  private List listeners = new ArrayList();

  public interface Listener
  {
    void parametersApplied();
  }
  private void addListener(Listener l)
  { listeners.add(l); }
  public void removeListener(Listener l)
  { listeners.remove(l); }
  
  private InputPanel()
  {
    super();
    setLayout(new BorderLayout());
    JPanel centered = new JPanel(new FlowLayout(FlowLayout.CENTER));
    JButton apply = new JButton("Apply");
    centered.add(apply);
    add(centered, BorderLayout.SOUTH);
    apply.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent ae)
      {
        members.copyUIToParameters();
        try
        {
          applyParameters();
        }
        catch (Exception e)
        {
          Window window = SwingUtilities.windowForComponent(InputPanel.this);
          new ExceptionDialog(window, members.getParameters(), e);
        }
      }
    });
    
    add(center, BorderLayout.CENTER);
  }
  
  protected InputPanel(List parameters, Listener l)
  {
    this();
    for (Iterator i = parameters.iterator(); i.hasNext();)
    {
      Parameter p = (Parameter) i.next();
      addParameter(p);
    }
    addListener(l);
  }

  protected abstract void addParameter(Parameter p);
  
  public void applyParameters()
  {
    for (Iterator i = listeners.iterator(); i.hasNext(); )
    {
      Listener l = (Listener)i.next();
      l.parametersApplied();
    }
  }
}
