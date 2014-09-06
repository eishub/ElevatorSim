/*
 * Copyright 2005 Neil McKellar and Chris Dailey
 * All rights reserved.
 */
package org.intranet.sim.ui.multiple;

class AverageNumber
  extends Number
{
  double sum = 0.0;
  int n = 0;

  public AverageNumber()
  {
    super();
  }

  public AverageNumber(double d)
  {
    super();
    add(d);
  }

  public void add(double d)
  {
    sum += d;
    n++;
  }

  public int intValue()
  {
    if (n == 0) return 0;
    return (int)(sum/n);
  }

  public long longValue()
  {
    if (n == 0) return 0;
    return (long)(sum/n);
  }

  public float floatValue()
  {
    if (n == 0) return 0;
    return (float)(sum/n);
  }

  public double doubleValue()
  {
    if (n == 0) return 0;
    return (sum/n);
  }
}
