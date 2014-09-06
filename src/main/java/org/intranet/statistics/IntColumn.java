/*
 * Copyright 2003 Neil McKellar and Chris Dailey
 * All rights reserved.
 */
package org.intranet.statistics;

/**
 * @author Neil McKellar and Chris Dailey
 *
 */
public class IntColumn
  implements Column
{
  private String heading;
  private int[] values;
  private long total;
  private float average;
  private int min;
  private int max;
  private int minIndex;
  private int maxIndex;

  public IntColumn(String hdr, int[] vals)
  {
    values = vals;
    heading = hdr;
    calculate();
  }
  
  public String getHeading() { return heading; }
  public Number getTotal() { return new Long(total); }
  public Number getAverage() { return new Float(average); }
  public Number getMin() { return new Integer(min); }
  public Number getMax() { return new Integer(max); }
  public int getValueCount() { return values.length; }
  public Number getValue(int x) { return new Integer(values[x]); }
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
    min = Integer.MAX_VALUE;
    max = Integer.MIN_VALUE;
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