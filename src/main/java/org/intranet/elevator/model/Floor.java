/*
* Copyright 2003-2005 Neil McKellar and Chris Dailey
* All rights reserved.
*/
package org.intranet.elevator.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.intranet.sim.event.EventQueue;

/**
* @author Neil McKellar and Chris Dailey
*/
public final class Floor
  extends Location
{
  private int number;
  // distance from the ground
  private float ceiling;  // relative to the floor's height
  private CarRequestPanel callPanel = new CarRequestPanel();
  private List carEntrances = new ArrayList();

  // TODO: Make a sequence diagram with all the passing off of notification
  private CarEntrance.CarEntranceListener carEntranceListener =
    new CarEntrance.CarEntranceListener()
  {
    public void arrivedUp(CarEntrance entrance)
    {
      callPanel.arrivedUp(entrance);
    }

    public void arrivedDown(CarEntrance entrance)
    {
      callPanel.arrivedDown(entrance);
    }
  };

  public Floor(EventQueue eQ, int number, float height, float ceiling)
  {
    super(eQ, height, 500);
    this.number = number;
    this.ceiling = ceiling;
  }
  
  public int getFloorNumber()
  {
    return number;
  }
  
  public float getCeiling()
  {
    return ceiling;
  }
  
  public float getAbsoluteCeiling()
  {
    return getHeight() + ceiling;
  }
  
  public CarRequestPanel getCallPanel()
  {
    return callPanel;
  }

  public void createCarEntrance(Location destination)
  {
    carEntrances.add(new CarEntrance(eventQueue, this, destination,
      carEntranceListener));
  }

  public Iterator getCarEntrances()
  {
    return carEntrances.iterator();
  }

  public CarEntrance getOpenCarEntrance(boolean up)
  {
    for (Iterator i = carEntrances.iterator(); i.hasNext(); )
    {
      CarEntrance carEntrance = (CarEntrance)i.next();
      if (carEntrance.getDoor().isOpen())
      {
        if (up && carEntrance.isUp())
          return carEntrance;
        if (!up && carEntrance.isDown())
          return carEntrance;
      }
    }
    return null;
  }

  public CarEntrance getCarEntranceForCar(Location destination)
  {
    for (Iterator i = carEntrances.iterator(); i.hasNext(); )
    {
      CarEntrance carEntrance = (CarEntrance)i.next();
      if (carEntrance.getDoor().getTo() == destination)
        return carEntrance;
    }
    return null;
  }
  
  public String toString()
  {
    return "Floor" + number + "@" + getHeight();
  }
}