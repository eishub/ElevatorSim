/*
* Copyright 2003 Neil McKellar and Chris Dailey
* All rights reserved.
*/
package org.intranet.elevator.view;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JComponent;

import org.intranet.elevator.model.CarEntrance;

/**
* @author Neil McKellar and Chris Dailey
*/
public class EntranceView
  extends JComponent
{
  private CarEntrance entrance;
  void initialize(CarEntrance entrance, int width, int height)
  {
    this.entrance = entrance;
    setSize(width, height);
    setOpaque(false);
  }

  EntranceView()
  {
    super();
  }

  public void paintComponent(Graphics g)
  {
    int marginX = (int)(getWidth()*0.10);
    int marginY = (int)(getHeight()*0.10);
    int entranceWidth = getWidth() - (marginX * 2);
    int entranceHeight = getHeight() - (marginY * 2);
    int pctClosed = entrance.getDoor().getPercentClosed();
    int doorWidth = entranceWidth * pctClosed / 200; 
    
    // Draw door entrance border
    g.setColor(Color.white);
    g.drawRect(marginX, marginY, entranceWidth, entranceHeight);

    // Draw elevator doors
    g.setColor(Color.black);
    g.fillRect(marginX, marginY, doorWidth, entranceHeight);
    g.fillRect(marginX+entranceWidth-doorWidth, marginY,
               doorWidth, entranceHeight);
    
    // Up indicator next to elevator door
    if (entrance.isUp()) g.setColor(Color.green);
    else g.setColor(Color.gray);          
    g.fillRect(0, 0, marginX, marginY/2);

    // Down indicator next to elevator door
    if (entrance.isDown()) g.setColor(Color.green);
    else g.setColor(Color.gray);          
    g.fillRect(0, marginY/2, marginX, marginY/2);

    g.setColor(Color.black);
  }
}