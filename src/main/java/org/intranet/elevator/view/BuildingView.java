/*
 * Copyright 2003 Neil McKellar and Chris Dailey
 * All rights reserved.
 */
package org.intranet.elevator.view;

import java.awt.Color;
import java.awt.Graphics;
import java.util.Iterator;

import javax.swing.JComponent;

import org.intranet.elevator.model.Car;
import org.intranet.elevator.model.CarEntrance;
import org.intranet.elevator.model.Floor;
import org.intranet.elevator.model.operate.Building;
import org.intranet.elevator.model.operate.Person;

/**
 * @author Neil McKellar and Chris Dailey
 *
 */
public class BuildingView
  extends JComponent
{
  private Building building;
  
  public BuildingView(Building building)
  {
    super();
    this.building = building;
  }
  
  public void paintComponent(Graphics g)
  {
    synchronized (building)
    {
      float pixelConv = getHeight() / building.getHeight();

      float minFloorHeight = calcMinFloorHeight();
      int personHeight = (int)(5.5 * pixelConv);
    
      for (Iterator i = building.getFloors(); i.hasNext();)
      {
        // Draw lines for floor and ceiling and number floors
        Floor floor = (Floor) i.next();
        int shaftWidth = (int)(minFloorHeight * pixelConv);
        drawFloor(g, floor, pixelConv, shaftWidth, personHeight);
      }
    
      // Draw elevator cars
      int carDimension = (int)(minFloorHeight * pixelConv);
      drawCars(g, pixelConv, personHeight, carDimension, carDimension);
    }
  }

  private float calcMinFloorHeight()
  {
    float minFloorCeiling = building.getHeight();
    for (Iterator i = building.getFloors(); i.hasNext();)
    {
      Floor floor = (Floor) i.next();
      if (floor.getCeiling() < minFloorCeiling)
        minFloorCeiling = floor.getCeiling();
    }
    return minFloorCeiling;
  }

  private void drawFloor(Graphics g, Floor floor, float pixelConv,
    int shaftWidthPixels, int personHeight)
  {
    g.setColor(Color.gray);
    int floorY = getHeight() - (int)(floor.getHeight() * pixelConv);
    g.drawLine(0, floorY, getWidth(), floorY);
    int ceilingY = getHeight() - (int)(floor.getAbsoluteCeiling() * pixelConv);
    g.drawLine(0, ceilingY, getWidth(), ceilingY);
    g.drawString(Integer.toString(floor.getFloorNumber()), 5, floorY - 4);
    
    // Draw entrances
    int entranceNum = drawEntrances(g, floor, ceilingY,
      shaftWidthPixels, floorY - ceilingY);
    
    // Draw request buttons
    drawRequestIndicators(g, floor, floorY, ceilingY, entranceNum);
    
    int entrancesXLocation = getWidth() - shaftWidthPixels * building.getNumCars();
    drawFloorPersons(g, floor, personHeight, floorY, entrancesXLocation);
    drawCarMovingPersons(g, floor, personHeight, floorY, entrancesXLocation);
  }

  private void drawRequestIndicators(Graphics g, Floor floor, int floorY,
    int ceilingY, int entranceNum)
  {
    int floorHeight = floorY - ceilingY;
    int widthOfEntrances = floorHeight * entranceNum; // proportional
    int wallCenter = floorHeight / 2;
    int buttonHeight = floorHeight / 20;
    int buttonWidth = floorHeight / 10;
    
    if (floor.getCallPanel().isUp())
      g.setColor(Color.yellow);
    else
      g.setColor(Color.gray);
    g.fillRect(getWidth() - widthOfEntrances - buttonWidth,
               ceilingY + wallCenter,
               buttonWidth, buttonHeight);
    
    if (floor.getCallPanel().isDown())
      g.setColor(Color.yellow);
    else
      g.setColor(Color.gray);
    g.fillRect(getWidth() - widthOfEntrances - buttonWidth,
               ceilingY + wallCenter + buttonHeight + 1,
               buttonWidth, buttonHeight);
  }

  private void drawFloorPersons(Graphics g, Floor floor, int personHeight,
    int floorY, int entrancesXLocation)
  {
    int personNumber = 0;
    PersonView personView = new PersonView();
    for (Iterator j = floor.getPeople(); j.hasNext();)
    {
      Person person = (Person)j.next();
      boolean isMoving = person.getPercentMoved() != -1;
      int floorXPosition = 20 + personNumber * personHeight / 2;
      int distanceToElevator = entrancesXLocation - floorXPosition;
      int distanceMoved = (int)(distanceToElevator * person.getPercentMoved() / 100.0);
      int personX = isMoving ? floorXPosition + distanceMoved : floorXPosition;
      int personWidth = personHeight / 2;
      int personY = floorY - personHeight;
      personView.initialize(person, personWidth, personHeight);
      Graphics g2 = g.create(personX, personY, personWidth, personHeight);
      personView.paint(g2);
      personNumber++;
    }
  }

  private void drawCarMovingPersons(Graphics g, Floor floor, int personHeight,
      int floorY, int elevatorLocation)
    {
      PersonView personView = new PersonView();
      for (Iterator cars = building.getCars(); cars.hasNext(); )
      {
        Car car = (Car)cars.next();
        if (car.getHeight() != floor.getHeight())
          continue;
        for (Iterator j = car.getPeople(); j.hasNext();)
        {
          Person person = (Person)j.next();
          boolean isMoving = person.getPercentMoved() != -1;
          if (!isMoving)
            continue;
          int floorXPosition = 20;
          int distanceToElevator = elevatorLocation - floorXPosition;
          int distanceMoved = (int)(distanceToElevator * (100 - person.getPercentMoved()) / 100.0);
          int personX = floorXPosition + distanceMoved;
          int personWidth = personHeight / 2;
          int personY = floorY - personHeight;
          personView.initialize(person, personWidth, personHeight);
          Graphics g2 = g.create(personX, personY, personWidth, personHeight);
          personView.paint(g2);
        }
      }
    }

  private int drawEntrances(Graphics g, Floor floor, int ceY, int ceW, int ceH)
  {
    EntranceView entranceView = new EntranceView();
    int entranceNum = 0;
    int allEntrancesWidth = building.getNumCars() * ceW;
    for (Iterator j = floor.getCarEntrances();
         j.hasNext();
         entranceNum++)
    {
      CarEntrance carEntrance = (CarEntrance) j.next();
      entranceView.initialize(carEntrance, ceW, ceH);
      int ceX = getWidth() - allEntrancesWidth + (entranceNum * ceW);
      Graphics g2 = g.create(ceX, ceY, ceW, ceH);
      entranceView.paint(g2);
    }
    return entranceNum;
  }

  private void drawCars(Graphics g, float pixelConv, int personHeight,
    int carHeight, int carWidth)
  {
    int carNumber = 0;
    CarView carView = new CarView();
    int allCarsWidth = building.getNumCars() * carWidth;
    for (Iterator i = building.getCars(); i.hasNext(); carNumber++)
    {
      Car car = (Car) i.next();
      int floorY = getHeight() - (int)(car.getHeight() * pixelConv);
    
      int x = getWidth() - allCarsWidth + (carNumber * carWidth);
      int y = floorY - carHeight;
    
      carView.initialize(car, carWidth, carHeight, personHeight);
      Graphics carG = g.create(x, y, carWidth, carHeight);
      carView.paint(carG);
    }
  }
}