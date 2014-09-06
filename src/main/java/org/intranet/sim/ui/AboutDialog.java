/*
 * Copyright 2003 Neil McKellar and Chris Dailey
 * All rights reserved.
 */
package org.intranet.sim.ui;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;

import org.intranet.sim.SimulationApplication;

/**
 * @author Neil McKellar and Chris Dailey
 *
 */
public class AboutDialog extends JDialog
{

  public AboutDialog(JFrame owner, SimulationApplication simApp)
  {
    super(owner, "About " + simApp.getApplicationName());
    setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    
    JPanel aboutPanel = new JPanel();
    aboutPanel.setLayout(new GridLayout(0, 1));
    aboutPanel.add(new JLabel(simApp.getApplicationName(), SwingConstants.CENTER));
    aboutPanel.add(new JLabel("Version " + simApp.getVersion(), SwingConstants.CENTER));
    aboutPanel.add(new JLabel(simApp.getCopyright(), SwingConstants.CENTER));
    getContentPane().add(aboutPanel, BorderLayout.NORTH);
    
    JScrollPane scrollPane = createLicensePane();
    getContentPane().add(scrollPane, BorderLayout.CENTER);

    setSize(owner.getWidth()*3/4, owner.getHeight()*2/3);
    setLocationRelativeTo(owner); // centers the dialog in the parent window
    setVisible(true);
  }

  private JScrollPane createLicensePane()
  {
    // scrollable license text area
    JTextArea textArea = new JTextArea();
    textArea.setFont(new Font("Monospaced",Font.PLAIN,12));
    JScrollPane scrollPane = new JScrollPane(textArea);
    // get license text
    InputStream lgplStream = ClassLoader.getSystemResourceAsStream("lgpl.txt");
    InputStreamReader lgplReader = new InputStreamReader(lgplStream);
    BufferedReader bufferedReader = new BufferedReader(lgplReader);
    for (;;)
    {
      try
      {
        String line = bufferedReader.readLine();
        if (line == null) break;
        textArea.append(line);
        textArea.append("\n");
      } catch (IOException e)
      {
        textArea.append("Unable to find license file.");
        break;
      }
    }
    textArea.setCaretPosition(0);  // set us back at the top
    return scrollPane;
  }

}
