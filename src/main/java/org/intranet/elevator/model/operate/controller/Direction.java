/*
 * Copyright 2003 Neil McKellar and Chris Dailey
 * All rights reserved.
 */
package org.intranet.elevator.model.operate.controller;


public final class Direction
{
  private String name;
  private Direction(String aName) { name = aName; }

  public static final Direction UP = new Direction("UP");
  public static final Direction DOWN = new Direction("DOWN");
  public static final Direction NONE = new Direction("NONE");
  
  public boolean isUp()
  {
    return name.equals(Direction.UP.name);
  }
  
  public String toString() { return name; }
}