/*
 * Copyright 2003 Neil McKellar and Chris Dailey
 * All rights reserved.
 */
package org.intranet.statistics;

/**
 * @author Neil McKellar and Chris Dailey
 *
 */
public class LongColumn
  implements Column
{
  private long[] values;
  private String heading;
  private long total;
  private float average;
  private long min;
  private long max;
  private int minIndex;
  private int maxIndex;

  public LongColumn(String hdr, long[] vals)
  {
    values = vals;
    heading = hdr;
    calculate();
  }

  public String getHeading() { return heading; }
  public Number getTotal() { return new Long(total); }
  public Number getAverage() { return new Float(average); }
  public Number getMin() { return new Long(min); }
  public Number getMax() { return new Long(max); }
  public int getValueCount() { return values.length; }
  public Number getValue(int x) { return new Long(values[x]); }
  public int getMinIndex() { return minIndex; }
  public int getMaxIndex() { return maxIndex; }
  public boolean isMin(int index)
  {
    return (values[index] == values[minIndex]);
  }
  public boolean isMax(int index)
  {
    return (values[index] == values[maxIndex]);
  }

  private void calculate()
  {
    total = 0;
    min = Long.MAX_VALUE;
    max = Long.MIN_VALUE;
    for (int i = 0; i < values.length; i++)
    {
      total += values[i];
      if (values[i] < min)
      {
        min = values[i];
        minIndex = i;
      }
      if (values[i] > max)
      {
        max = values[i];
        maxIndex = i;
      }
    }
    average = (1.0F * total) / values.length;
  }
}