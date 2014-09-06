/*
* Copyright 2003,2005 Neil McKellar and Chris Dailey
* All rights reserved.
*/
package org.intranet.elevator.model;

import java.util.ArrayList;
import java.util.List;

import org.intranet.sim.event.EventQueue;

/**
* @author Neil McKellar and Chris Dailey
*/
public final class CarEntrance
{
  public interface CarEntranceListener
  {
    void arrivedUp(CarEntrance entrance);
    void arrivedDown(CarEntrance entrance);
  }

  public interface DoorWaitListener
  {
    void doorAvailable();
  }

  private boolean up;
  private boolean down;
  private Door door;
  private DoorSensor sensor;
  private CarEntranceListener entranceListener;

  CarEntrance(final EventQueue eQ, Location fromLocation, Location toLocation,
              CarEntranceListener listener)
  {
    super();
    door = new Door(eQ, fromLocation, toLocation);
    sensor = new DoorSensor(eQ);
    entranceListener = listener;
    Door.Listener doorListener = new Door.Listener()
    {
      public void doorOpened()
      {
        if (up)
          entranceListener.arrivedUp(CarEntrance.this);
        else if (down)
          entranceListener.arrivedDown(CarEntrance.this);
        if (sensor.getState() == DoorSensor.State.CLEAR)
          sensor.unobstruct();
      }

      public void doorClosed() {}
    };
    door.addListener(doorListener, false);
    sensor.addListener(new DoorSensor.Listener()
    {
      public void sensorCleared()
      {
        door.close(); 
      }

      public void sensorObstructed()
      {
        if (door.getState() == Door.State.CLOSING)
          door.open();
      }

      public void sensorUnobstructed() {}
    });
  }

  private List waiters = new ArrayList();
  public void waitToEnterDoor(DoorWaitListener listener)
  {
    waiters.add(listener);
    chooseSomeoneFromList();
  }
  public boolean arePeopleWaitingToGetOut()
  {
    return waiters.size() > 0;
  }

  private void chooseSomeoneFromList()
  {
    boolean isSensorAvailable;
    do
    {
      if (waiters.size() == 0)
        return;
      isSensorAvailable =
        (door.getState() != Door.State.OPENING &&
          (sensor.getState() == DoorSensor.State.UNOBSTRUCTED ||
           sensor.getState() == DoorSensor.State.CLEAR));
      if (isSensorAvailable)
      {
        DoorWaitListener listener = (DoorWaitListener)waiters.remove(0);
        listener.doorAvailable();
      }
      if (!isSensorAvailable)
        sensor.addListener(new DoorSensor.Listener()
        {
          public void sensorCleared()
          {
            sensor.removeListener(this);
            chooseSomeoneFromList();
          }
          public void sensorObstructed()
          {}
          public void sensorUnobstructed()
          {
            sensor.removeListener(this);
            chooseSomeoneFromList();
          }
        });
    } while (waiters.size() > 0 && isSensorAvailable);
  }

  public boolean isUp()
  {
    return up;
  }
  
  public void setUp(boolean up)
  {
    this.up = up;
  }

  public boolean isDown()
  {
    return down;
  }

  public void setDown(boolean down)
  {
    this.down = down;
  }

  public DoorSensor getDoorSensor()
  {
    return sensor;
  }

  public Door getDoor()
  {
    return door;
  }
}