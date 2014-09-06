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
* The states of Car are substates of MovableLocation:IDLE. 
* Valid states:
* <table border="1" cellspacing="0" cellpadding="2" summary="">
*  <tr>
*   <th rowspan="2">State</th>
*   <th colspan="2">Variables</th>
*   <th colspan="10">Transitions</th>
*  </tr>
*  <tr>
*   <th>destination</th>
*   <th>location</th>
*   <th>setDestination()</th>
*   <th>undock()</th>
*   <th>[MovableLocation.arrive()]</th>
*  </tr>
*  <tr>
*   <td>IDLE:UNDOCKED</td>
*   <td>null</td>
*   <td>null</td>
*   <td>MOVING or arrive(): DOCKED</td>
*   <td><i>Illegal</i></td>
*   <td><i>Impossible</i></td>
*  </tr>
*  <tr>
*   <td>MOVING</td>
*   <td>Set</td>
*   <td>null</td>
*   <td>MOVING or arrive(): DOCKED</td>
*   <td><i>Illegal</i></td>
*   <td>DOCKED<br>[docked()]</td>
*  </tr>
*  <tr>
*   <td>IDLE:UNDOCKING</td>
*   <td>Set</td>
*   <td>Set</td>
*   <td>UNDOCKING</td>
*   <td>MOVING</td>
*   <td><i>Impossible</i></td>
*  </tr>
*  <tr>
*   <td>IDLE:DOCKED</td>
*   <td>null</td>
*   <td>Set</td>
*   <td>UNDOCKING</td>
*   <td>UNDOCKED</td>
*   <td><i>Impossible</i></td>
*  </tr>
* </table>
* @author Neil McKellar and Chris Dailey
*/
public final class Car
  extends MovableLocation
{
  private String name;
  private Floor location;
  private Floor destination;
  private FloorRequestPanel panel = new FloorRequestPanel();
  private List listeners = new ArrayList(); 
  
  public interface Listener
  {
    void docked();
  }

  public Car(EventQueue eQ, String name, float height, int capacity)
  {
    super(eQ, height, capacity);
    this.name = name;
  }

  public void setDestination(Floor destination)
  {
    this.destination = destination;
    if (location == null)
      setDestinationHeight(destination.getHeight());
  }

  public float getTravelTime(Floor floor)
  {
    float floorHeight = floor.getHeight();
    float travelDistance = floorHeight - getHeight();
    return getTravelTime(travelDistance);
  }

  public void undock()
  {
    if (location == null)
      throw new IllegalStateException("Must be docked to undock");

    location = null;
    if (destination != null)
      setDestinationHeight(destination.getHeight());
  }

  public Floor getDestination()
  {
    return destination;
  }

  public Floor getLocation()
  {
    return location;
  }
  
  public void addListener(Listener listener)
  {
    listeners.add(listener);
  }
  
  public void removeListener(Listener listener)
  {
    listeners.remove(listener);
  }

  public String getName()
  {
    return name;
  }

  public FloorRequestPanel getFloorRequestPanel()
  {
    return panel;
  }

  public Floor getFloorAt()
  {
    if (destination == null && location == null)
      return panel.getFloorAt(getHeight());
    return null;
  }

  public final float getRatePerSecond()
  {
    return (float)(1000 * 10.0/4030.0);
  }

  protected void arrive()
  {
    location = destination;
    destination = null;
    panel.requestFulfilled(location);
    fireDockedEvent();
  }

  private void fireDockedEvent()
  {
    List listenersCopy = new ArrayList(listeners);
    for (Iterator i = listenersCopy.iterator(); i.hasNext();)
    {
      Listener l = (Listener) i.next();
      l.docked();
    }
  }
  
  //Wouter: added to ease debugging. 
  public String toString() { return name; }
}