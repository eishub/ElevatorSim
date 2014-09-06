/*
 * Copyright 2003 Neil McKellar and Chris Dailey
 * All rights reserved.
 */
package org.intranet.elevator.view;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JComponent;

import org.intranet.elevator.model.Floor;
import org.intranet.elevator.model.operate.Person;

/**
 * @author Neil McKellar and Chris Dailey
 *
 */
class PersonView extends JComponent
{
  private Person person;

  public PersonView()
  {
    super();
  }
  void initialize(Person person, int width, int height)
  {
    this.person = person;
    setSize(width, height);
    setOpaque(false);
  }

  public void paintComponent(Graphics g)
  {
    int centerX = getWidth() / 2;
    int hipsHeight = getHeight() / 3;
    int hipsY = getHeight() - hipsHeight;
    int neckY = hipsY - getHeight() / 3;
    int armsY = getHeight() - getHeight() / 2;
    
    g.setColor(Color.gray);
    // draw legs
    g.drawLine(0, getHeight(), centerX, hipsY);
    g.drawLine(centerX, hipsY, getWidth(), getHeight());
    //draw torso
    g.drawLine(centerX, hipsY, centerX, neckY);
    // draw arms
    g.drawLine(0, armsY, getWidth(), armsY);
    // draw head
    int headX = getWidth()/2 - getHeight()/6;
    g.fillOval(headX, getHeight() - getHeight(), getHeight()/3, getHeight()/3);
    // draw destination
    Floor destination = person.getDestination();
    if (destination != null)
    {
      int floorNumber = destination.getFloorNumber();
      int numberWidth = g.getFontMetrics().stringWidth(
        Integer.toString(floorNumber));
      int numberHeight = g.getFontMetrics().getHeight();
      g.setColor(Color.cyan);
      g.drawString(Integer.toString(floorNumber),
                   headX + getHeight()/6 - numberWidth/2,
                   getHeight()/6 + numberHeight/2);
    }
  }
}