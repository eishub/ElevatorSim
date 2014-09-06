/*
 * Copyright 2003 Neil McKellar and Chris Dailey
 * All rights reserved.
 */
package org.intranet.elevator.model.operate.controller;

import org.intranet.elevator.model.Floor;

/**
 * FloorContext defines a range between floors, and includes enough information
 * to tell if a car is in that range, and what the stop at the end of the range
 * and the next stop after that range would be.
 * 
 * @author Neil McKellar and Chris Dailey
 */
class FloorContext
{
  private Floor previous;
  private Floor next;
  private Floor successor;

  /**
   * 
   */
  FloorContext(Floor previous, Floor next, Floor successor)
  {
    super();
    this.previous = previous;
    this.next = next;
    this.successor = successor;
  }

  Floor getPrevious()
  {
    return previous;
  }

  Floor getNext()
  {
    return next;
  }

  Floor getSuccessor()
  {
    return successor;
  }

  boolean contains(float height)
  {
    return (height >= previous.getHeight() && height <= next.getHeight()) ||
      (height >= next.getHeight() && height <= previous.getHeight());
  }
  
  public String toString()
  {
    return "previous = " + previous + ", next = " + next +
           ", succ = " + successor;
  }
}