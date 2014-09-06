/*
 * Copyright 2003 Neil McKellar and Chris Dailey
 * All rights reserved.
 */
package org.intranet.sim.event;

/**
 * @author Neil McKellar and Chris Dailey
 * Does interpolation.
 */
public abstract class TrackingUpdateEvent
  extends IncrementalUpdateEvent
{
  private long beginTime;
  private float beginValue;
  private float distance;
  
  private TrackingUpdateEvent()
  {
    super(0);
  }

  public TrackingUpdateEvent(long beginTime, float begin, long endTime, float end)
  {
    super(endTime);
    this.beginTime = beginTime;
    beginValue = begin;
    distance = end - begin;
  }
  
  private float percentDone(long time)
  {
    return (float)(time - beginTime) / (getTime() - beginTime);
  }

  /**
   * Gets the current tracking value as a straight interpolation between the
   * beginValue and endValue.
   * @param time The current time.  Do not pass Event.getTime(), use
   *             eventQueue.getCurrentTime().
   * @return The current value being tracked by this event.
   */
  public final float currentValue(long time)
  {
    float percent = percentDone(time);
    return beginValue + distance * percent;
  }
}