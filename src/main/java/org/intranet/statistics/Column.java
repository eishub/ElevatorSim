/*
 * Copyright 2003 Neil McKellar and Chris Dailey
 * All rights reserved.
 */
package org.intranet.statistics;

/**
 * @author Neil McKellar and Chris Dailey
 *
 */
public interface Column
{
  String getHeading();
  Number getTotal();
  Number getAverage();
  Number getMin();
  Number getMax();
  int getMinIndex();
  int getMaxIndex();
  boolean isMin(int index);
  boolean isMax(int index);
  int getValueCount();
  Number getValue(int x);
}