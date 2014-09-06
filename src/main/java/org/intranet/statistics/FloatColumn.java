/*
 * Copyright 2003 Neil McKellar and Chris Dailey
 * All rights reserved.
 */
package org.intranet.statistics;

/**
 * @author Neil McKellar and Chris Dailey
 *
 */
public class FloatColumn
  implements Column
{
  private float[] values;
  private String heading;
  private double total;
  private float average;
  private float min;
  private float max;
  private int minIndex;
  private int maxIndex;
  
  public FloatColumn(String hdr, float[] vals)
  {
    values = vals;
    heading = hdr;
    calculate();
  }

  public String getHeading() { return heading; }
  public Number getTotal() { return new Double(total); }
  public Number getAverage() { return new Float(average); }
  public Number getMin() { return new Float(min); }
  public Number getMax() { return new Float(max); }
  public int getValueCount() { return values.length; }
  public Number getValue(int x) { return new Float(values[x]); }
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
    total = 0.0;
    min = Float.POSITIVE_INFINITY;
    max = Float.NEGATIVE_INFINITY;
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
    average = (float)(total / values.length);
  }
}