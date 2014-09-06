/*
 * Copyright 2003-2004 Neil McKellar and Chris Dailey
 * All rights reserved.
 */
package org.intranet.elevator.model.operate.controller;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

class CarAssignments
{
  private List list = new ArrayList();
  private String carName;
  
  CarAssignments()
  {
    this("noname");
  }
  
  CarAssignments(String name)
  {
    super();
    carName = name;
  }
  
  private CarAssignments(CarAssignments ca)
  {
    this(ca.carName + "-clone");
    this.list = new ArrayList(ca.list);
  }
  
  void printAssignments(String prefix)
  {
    if (!log)
      return;
    System.out.flush();
    System.err.flush();
    System.out.print(prefix + " ");
    for(Iterator i = list.iterator(); i.hasNext();)
    {
      Assignment a = (Assignment)i.next();
      System.out.print(a.toString() + ",");
    }
    System.out.println();
    System.out.flush();
    System.err.flush();
  }

  private boolean log = false;
  
  void activateLog()
  {
    log = true;
  }
  
  private void log(String message)
  {
    if (log)
    {
      System.err.flush();
      System.out.flush();
      System.out.println("[" + carName + "]: " + message);
      System.out.flush();
      System.err.flush();
    }
  }

  void addAssignment(List floors, Assignment base,
                     Assignment newAssignment)
  {
    log("addAssignment(floor, "+base+", "+newAssignment+")");
    printAssignments("addAssignment");
    if (list.isEmpty())
    {
      log("Add to empty list.");
      System.out.flush();
      list.add(newAssignment);
      return;
    }
    if (list.contains(newAssignment))
      return;
    int newNumStops = maxStops(floors, base, newAssignment);
    log("max stops with new: " + newNumStops);
    
    // Iterate through the list of destinations and determine the best place
    // to put the new assignment.
    // It should go just before the destination that has a greater 'maxStops'
    // value or at the end if there is no greater value.
    for (ListIterator l = list.listIterator(); l.hasNext();)
    {
      Assignment a = (Assignment)l.next();
      int aNumStops = maxStops(floors, base, a);
      log("stops for: " + a + " = " + aNumStops);
      if (aNumStops > newNumStops)
      {
        l.previous();
        l.add(newAssignment);
        log("add new assignment here");
        return;
      }
    }
    // There were no assignments after our new assignment
    list.add(newAssignment);
    log("append new assignment");
  }
  
  /*
   * Determine an ordinal value for the destination relative to
   * a starting position in the list.
   * 'UP' direction destinations are simply their positions in the list of
   * floors.
   * 'DOWN' direction destinations are counted as "wrapped around" and so
   * are treated as their position plus the total number of possible floors.
   * The return value can be between 0 and two times the total number of
   * floors.
   */
  private int maxStops(List floors, Assignment base,
                       Assignment destination)
  {
    int topFloorNumber = floors.size();

    int baseNumber = floors.indexOf(base.getDestination());
    int destNumber = floors.indexOf(destination.getDestination());

    if (base.getDirection() == Direction.DOWN)
    {
      baseNumber = topFloorNumber + (topFloorNumber - baseNumber - 1); 
    }
    if (destination.getDirection() == Direction.DOWN)
    {
      destNumber = topFloorNumber + (topFloorNumber - destNumber - 1);
    }
    
    // If dest comes after base in the list of destinations,
    // Then the ordinal value is just the difference between the two
    // positions.
    // Otherwise, the ordinal value wraps around the complete list of floors
    // to come up "behind" the base value.  Hence, 2 x total floors is added
    // to the difference.
    int difference = destNumber - baseNumber;
    if (destNumber >= baseNumber)
    {
      return difference;
    }

    return 2 * topFloorNumber + difference;
  }
  
  void removeAssignment(Assignment assignment)
  {
    if (!list.remove(assignment))
    {
      throw new IllegalStateException("Can't remove non-existant assignment.");
    }
  }
  
  Assignment getCurrentAssignment()
  {
    if (list.isEmpty())
      return null;
    return (Assignment)list.get(0);
  }
  
  Iterator iteratorIncluding(List floors, Assignment base,
                             Assignment endPoint)
  {
    CarAssignments clone = new CarAssignments(this);
    clone.addAssignment(floors, base, endPoint);
    return clone.iterator();
  }
  
  Iterator iterator()
  {
    return list.iterator();
  }
  
  boolean contains(Assignment assignment)
  {
    return list.contains(assignment);
  }

  public String toString()
  {
    StringBuffer sb = new StringBuffer();
    for (Iterator assignments = iterator(); assignments.hasNext(); )
    {
      Assignment a = (Assignment)assignments.next();
      if (sb.length() > 0)
        sb.append(",");
      sb.append(a.toString());
    }
    return sb.toString();
  }
}