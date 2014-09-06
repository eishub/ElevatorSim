/*
* Copyright 2003 Neil McKellar and Chris Dailey
* All rights reserved.
*/
package org.intranet.elevator.view;

import java.awt.Color;
import java.awt.Graphics;
import java.util.Iterator;
import java.util.List;

import javax.swing.JComponent;

import org.intranet.elevator.model.Car;
import org.intranet.elevator.model.Floor;
import org.intranet.elevator.model.operate.Person;

/**
* @author Neil McKellar and Chris Dailey
*/
public class CarView
  extends JComponent
{
  private Car car;
  private int personHeight;

  CarView()
  {
    super();
  }

  void initialize(Car car, int width, int height, int personHeight)
  {
    this.car = car;
    this.personHeight = personHeight;
    setSize(width, height);
    setOpaque(false);
  }

  public void paintComponent(Graphics g)
  {
    // Draw door entrance border
    int margin = (int)(getWidth() * 0.05);
    int margin2 = 2 * margin;
    g.setColor(Color.yellow);
    g.drawRect(margin, margin,
      getWidth() - margin2, getHeight() - margin2);

    // Draw floor requests
    int requestNumber = 0;
    List requestedFloors = car.getFloorRequestPanel().getRequestedFloors();
    for (Iterator i = requestedFloors.iterator(); i.hasNext();)
    {
      Floor floor = (Floor)i.next();
      String floorNumber = Integer.toString(floor.getFloorNumber());
      int numberWidth = g.getFontMetrics().stringWidth(floorNumber);
      int numberX = getWidth() - numberWidth - 2;
      int numberY =
          getHeight() - requestNumber * (g.getFontMetrics().getHeight()) - 2;
      requestNumber++;
      g.setColor(Color.lightGray);
      g.fillRect(numberX,numberY-g.getFontMetrics().getHeight(),
                 numberWidth,g.getFontMetrics().getHeight());
      g.setColor(Color.black);
      g.drawString(floorNumber,numberX,numberY); 
    }

    int personNumber = 0;
    PersonView personView = new PersonView();
    for (Iterator j = car.getPeople(); j.hasNext();)
    {
      Person person = (Person) j.next();

      if (person.getPercentMoved() != -1)
        continue;
      int personX = 4 + personNumber * personHeight / 2;
      int personWidth = personHeight / 2;
      int personY = getHeight() - margin - personHeight;
      personView.initialize(person, personWidth, personHeight);
      Graphics g2 = g.create(personX, personY, personWidth, personHeight);
      personView.paint(g2);
      personNumber++;
    }
  }
}