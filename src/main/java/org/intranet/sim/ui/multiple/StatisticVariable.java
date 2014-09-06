/*
 * Copyright 2004 Neil McKellar and Chris Dailey
 * All rights reserved.
 */
package org.intranet.sim.ui.multiple;

/**
 * @author Neil McKellar and Chris Dailey
 *
 */
public class StatisticVariable
{
  private String tableName;
  private String functionName;
  private String statisticName;

  StatisticVariable(String table, String function, String statistic)
  {
    tableName = table;
    functionName = function;
    statisticName = statistic;
  }

  public String toString()
  {
    return tableName + " " + functionName + " " + statisticName;
  }

  public String getTableName() { return tableName; }
  public String getStatisticName() { return statisticName; }
  public String getFunctionName() { return functionName; }
}
