/*
* Copyright 2003-2005 Neil McKellar and Chris Dailey
* All rights reserved.
*/
package org.intranet.elevator.model;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

import org.intranet.sim.ModelElement;
import org.intranet.sim.event.EventQueue;

/**
* @author Neil McKellar and Chris Dailey
*/
public class Location
  extends ModelElement
{
  private float height;
  private int capacity;
  private List people = new ArrayList();
  
  Location(EventQueue eQ, float height, int capacity)
  {
    super(eQ);
    this.height = height;
    this.capacity = capacity;
  }

  public final float getHeight()
  {
    return height;
  }

  protected void setHeight(float newHeight)
  {
    height = newHeight; 
  }
  
  public final void personEnters(Object person)
  {
    if (isAtCapacity())
      throw new IllegalStateException("Location is at capacity: " + capacity);
    people.add(person);
  }
  
  public final Iterator getPeople()
  {
    return people.iterator();
  }
  
  public final void personLeaves(Object person)
  {
    if (!people.remove(person))
    {
      throw new IllegalStateException("Person is not in this location.");
    }
  }
  
  public final boolean isAtCapacity()
  {
    return (people.size() == capacity);
  }
  
  public final int getCapacity()
  {
    return capacity;
  }
}