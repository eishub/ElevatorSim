/*
 * Copyright 2005 Neil McKellar and Chris Dailey
 * All rights reserved.
 */
package org.intranet.sim.ui.realtime;

import java.text.NumberFormat;
import java.util.Date;

/**
 * @author Neil McKellar and Chris Dailey
 *
 */
class Duration
{
  static NumberFormat threeDigitFormat = NumberFormat.getNumberInstance();
  static NumberFormat twoDigitFormat = NumberFormat.getNumberInstance();
  static NumberFormat oneDigitFormat = NumberFormat.getNumberInstance();
  static
  {
    threeDigitFormat.setMinimumIntegerDigits(3);
    threeDigitFormat.setMaximumIntegerDigits(3);

    twoDigitFormat.setMinimumIntegerDigits(2);
    twoDigitFormat.setMaximumIntegerDigits(2);

    oneDigitFormat.setMinimumIntegerDigits(1);
    oneDigitFormat.setMaximumIntegerDigits(5);
  }

  long duration;

  Duration(Date d1, Date d2)
  {
    duration = d2.getTime() - d1.getTime();
    if (duration < 0) duration = 0;
  }

  Duration (long time)
  {
    duration = time;
  }

  public static String format(long time)
  {
    final long msPerSecond = 1000;
    final long msPerMinute = 60 * msPerSecond;
    final long msPerHour = 60 * msPerMinute;
    final long msPerDay = 24 * msPerHour;
    final long msPerMonth = 30 * msPerDay;
    final long msPerYear = 365 * msPerDay;

    final long[] divisions = { 1, msPerSecond, msPerMinute, msPerHour,
        msPerDay, msPerMonth, msPerYear };
    final int numValues = 7;
    int[] timeValues = new int[numValues];
    String[] separators = { ".", ":", ":", " day ", " month ", " year " }; 
    NumberFormat[] formats = { threeDigitFormat, twoDigitFormat, twoDigitFormat, twoDigitFormat,
        oneDigitFormat, oneDigitFormat, oneDigitFormat };
    for (int i = numValues - 1; i >= 0; i--)
    {
      long division = divisions[i];
      timeValues[i] = (int)(time / division);
      // remove this amount of time
      time = time % division;
    }

    // Find the maximum index that has a nonzero value.  With this value,
    // cases such as "0 years 0 months 0 days 00:00:01.283" can be avoided.
    long maxIndex = 1;
    for (int i = 1; i < numValues; i++)
    {
      if (timeValues[i] > 0)
        maxIndex = i;
    }

    String formatString = "";
    for (int i = 0; i <= maxIndex; i++)
    {
      formatString = formats[i].format(timeValues[i]) + formatString;
      if (i < maxIndex)
        formatString = separators[i] + formatString;
    }

    return formatString;
  }

  public String toString()
  {
    return format(duration);
  }
}
