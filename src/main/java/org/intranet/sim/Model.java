/*
 * Copyright 2003 Neil McKellar and Chris Dailey
 * All rights reserved.
 */
package org.intranet.sim;

import java.util.List;

import org.intranet.sim.event.EventQueue;

/**
 * @author Neil McKellar and Chris Dailey
 *
 */
public abstract class Model extends ModelElement
{
  /**
   * @param eQ Event Queue
   */
  public Model(EventQueue eQ)
  {
    super(eQ);
  }

  /**
   * 
   * @return a list of Tables
   */
  public abstract List getStatistics();
}