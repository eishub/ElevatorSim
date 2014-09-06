/*
 * Copyright 2004 Neil McKellar and Chris Dailey
 * All rights reserved.
 */
package org.intranet.ui;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;


/**
 * @author Neil McKellar and Chris Dailey
 *
 */
public class ExceptionDialog extends JDialog
{
  private ExceptionDialog(Frame owner, List params, Exception e)
  {
    super(owner, "Error");
    createContents(params, e);
  }
  
  private ExceptionDialog(Dialog owner, List params, Exception e)
  {
    super(owner, "Error");
    createContents(params, e);
  }
  
  public ExceptionDialog(Window window, List params, Exception e)
  {
    e.printStackTrace(System.err);
    if (window instanceof Frame)
    {
      new ExceptionDialog((Frame)window, params, e);
    }
    else if (window instanceof Dialog)
    {
      new ExceptionDialog((Dialog)window, params, e);
    }
    else
    {
      throw new IllegalStateException("Invalid parent window type: " +
                                      window.getClass().getName());
    }
  }

  private void createContents(List params, Exception e)
  {
    // display the parameters (name, value)
    JPanel paramPanel = displayParams(params);
    getContentPane().add(paramPanel, BorderLayout.NORTH);
    
    // display the exeception (class, message?)
    
    JPanel exceptionPanel = displayError(e);
    getContentPane().add(exceptionPanel, BorderLayout.CENTER);
    
    // ok button
    JPanel okPanel = displayButton();
    getContentPane().add(okPanel, BorderLayout.SOUTH);
    pack();
    setVisible(true);
  }
  
  private JPanel displayParams(List params)
  {
    JPanel paramPanel = new JPanel(new GridBagLayout());
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.gridy = 1;
    for (Iterator i = params.iterator(); i.hasNext(); )
    {
      SingleValueParameter p = (SingleValueParameter)i.next();
      JLabel desc = new JLabel(p.getDescription());
      JLabel value = new JLabel(p.getUIValue().toString());
      gbc.gridx = 0;
      paramPanel.add(desc, gbc);
      gbc.gridx = 1;
      paramPanel.add(value, gbc);
      gbc.gridy++;
    }
    
    return paramPanel;
  }
  
  private JPanel displayError(Exception e)
  {
    JPanel errorPanel = new JPanel(new BorderLayout());
    JLabel type = new JLabel(e.getClass().getName());
    JLabel error = new JLabel(e.getMessage());
    
    errorPanel.add(type, BorderLayout.NORTH);
    errorPanel.add(error, BorderLayout.SOUTH);
    
    errorPanel.setBorder(new EmptyBorder(5,5,5,5));
    
    return errorPanel;
  }

  private JPanel displayButton()
  {
    JPanel okPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
    
    JButton apply = new JButton("OK");
    okPanel.add(apply);
    getContentPane().add(okPanel, BorderLayout.SOUTH);
    apply.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae)
      {
        dispose();
      }
    });
    
    return okPanel;
  }
}
