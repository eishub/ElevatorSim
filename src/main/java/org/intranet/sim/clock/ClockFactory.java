/*
 * Copyright 2003 Neil McKellar and Chris Dailey
 * All rights reserved.
 */
package org.intranet.sim.clock;

/**
 * @author Neil McKellar and Chris Dailey
 *
 */
public interface ClockFactory
{
  Clock createClock(Clock.FeedbackListener cl);
}
