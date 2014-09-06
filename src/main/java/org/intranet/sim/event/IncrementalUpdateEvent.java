/*
 * Copyright 2003 Neil McKellar and Chris Dailey
 * All rights reserved.
 */
package org.intranet.sim.event;

/**
 * @author Neil McKellar and Chris Dailey
 *
 */
public abstract class IncrementalUpdateEvent extends Event
{
  private IncrementalUpdateEvent()
  {
    super(0);
  }
  
  public IncrementalUpdateEvent(long time)
  {
    super(time);
  }
  
  public abstract void updateTime();
}
