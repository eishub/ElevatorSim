/*
* Copyright 2004 Neil McKellar and Chris Dailey
* All rights reserved.
*/
package org.intranet.sim.ui.multiple;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.intranet.statistics.Column;
import org.intranet.statistics.Table;
import org.intranet.ui.MultipleValueParameter;
import org.intranet.ui.SingleValueParameter;

/**
 * @author Neil McKellar and Chris Dailey
 */
public class ResultsGrid
{
  MultipleValueParameter primaryVar;
  MultipleValueParameter secondaryVar;
  MultipleValueParameter averageVar;
  List otherVariables;
  List[][] parameterLists;
  AverageNumber[][] statisticLists;

  ResultsGrid(Map results, MultipleValueParameter primaryVar,
    MultipleValueParameter secondaryVar, MultipleValueParameter averageVar,
    List otherVariables, StatisticVariable statisticsSelection)
  {
    this.primaryVar = primaryVar;
    this.secondaryVar = secondaryVar;
    this.otherVariables = otherVariables;
    this.averageVar = averageVar;

    int primarySize = primaryVar == null ? 1 :
      primaryVar.getParameterList().size();
    int secondarySize = secondaryVar == null ? 1 :
      secondaryVar.getParameterList().size();

    parameterLists = new List[primarySize][];
    statisticLists = new AverageNumber[primarySize][];
    for (int i = 0; i < primarySize; i++)
    {
      parameterLists[i] = new List[secondarySize];
      statisticLists[i] = new AverageNumber[secondarySize];
    }

    // Iterate through the statistics results to extract the values for the
    // variables we are interested in (based on primary and secondary selections).
    // Get column "names" (actually variable values) and row names (to do later)
    // along the way.
    for (Iterator i = results.entrySet().iterator(); i.hasNext(); )
    {
      Map.Entry entry = (Entry)i.next();
      List params = (List)entry.getKey();
      List statistics = (List)entry.getValue();

      if (!variablesMatch(params))
        continue;
      int column = primaryVar == null ? 0 : findPrimaryColumn(params);
      int row = secondaryVar == null ? 0 : findSecondaryRow(params);
      Number result = getStatistic(statistics, statisticsSelection);
      AverageNumber num = statisticLists[column][row];

      if (num == null)
        statisticLists[column][row] = new AverageNumber(result.doubleValue());
      else
        num.add(result.doubleValue());
      parameterLists[column][row] = params;
    }
  }

  private Number getStatistic(List statistics, StatisticVariable statisticsSelection)
  {
    // find the statistic that was requested from the statistics chooser
    for (Iterator tableIterator = statistics.iterator(); tableIterator.hasNext(); )
    {
      Table table = (Table)tableIterator.next();
      if (statisticsSelection.getTableName().equals(table.getName()))
      {
        for (int colNum = 0; colNum < table.getColumnCount(); colNum++)
        {
          Column column = table.getColumn(colNum);
          if (column.getHeading().equals(statisticsSelection.getStatisticName()))
          {
            // TODO: factor out explicit case analysis to classes
            if ("Avg".equals(statisticsSelection.getFunctionName()))
              return column.getAverage();
            else if ("Min".equals(statisticsSelection.getFunctionName()))
              return column.getMin();
            else if ("Max".equals(statisticsSelection.getFunctionName()))
              return column.getMax();
          }
        }
      }
    }
    throw new IllegalArgumentException("Couldn't find value for statistic " +
        statisticsSelection.getFunctionName());
  }

  private int findSecondaryRow(List params)
  {
    for (Iterator i = params.iterator(); i.hasNext(); )
    {
      SingleValueParameter p = (SingleValueParameter)i.next();
      if (secondaryVar.getDescription().equals(p.getDescription()))
      {
        int idx = 0;
        for (Iterator secI = secondaryVar.getParameterList().iterator(); secI.hasNext(); )
        {
          SingleValueParameter sec = (SingleValueParameter)secI.next();
          if (p.getUIValue().equals(sec.getUIValue()))
            return idx;
          idx++;
        }
      }
    }
    throw new IllegalArgumentException("Could not find parameter named " +
        secondaryVar.getDescription());
  }

  private int findPrimaryColumn(List params)
  {
    for (Iterator i = params.iterator(); i.hasNext(); )
    {
      SingleValueParameter p = (SingleValueParameter)i.next();
      if (primaryVar.getDescription().equals(p.getDescription()))
      {
        int idx = 0;
        for (Iterator primI = primaryVar.getParameterList().iterator(); primI.hasNext(); )
        {
          SingleValueParameter prim = (SingleValueParameter)primI.next();
          if (p.getUIValue().equals(prim.getUIValue()))
            return idx;
          idx++;
        }
      }
    }
    throw new IllegalArgumentException("Could not find parameter named " +
        primaryVar.getDescription());
  }

  private boolean variablesMatch(List params)
  {
    for (Iterator i = params.iterator(); i.hasNext(); )
    {
      SingleValueParameter p = (SingleValueParameter)i.next();
      String desc = p.getDescription();
      Object val = p.getUIValue();
      if (primaryVar != null && desc.equals(primaryVar.getDescription()))
        continue;
      if (secondaryVar != null && desc.equals(secondaryVar.getDescription()))
        continue;
      for (Iterator others = otherVariables.iterator(); others.hasNext(); )
      {
        SingleValueParameter other = (SingleValueParameter)others.next();
        // If this is an average variable, we want all instances of it to
        // match.  So just continue here and don't risk doing a test that
        // will result in returning false.
        if (averageVar != null &&
          averageVar.getDescription().equals(other.getDescription()))
        {
          continue;
        }
        if (desc.equals(other.getDescription()))
        {
          if (!val.equals(other.getUIValue()))
            return false;
          break;
        }
      }
    }
    return true;
  }

  List getParameters(int col, int row)
  {
    return parameterLists[col][row];
  }

  Object getResult(int col, int row)
  {
    return statisticLists[col][row];
  }

  String getColumnName(int col)
  {
    SingleValueParameter param = (SingleValueParameter)primaryVar.getParameterList().get(col);
    return param.getUIValue().toString();
  }

  String getRowName(int row)
  {
    SingleValueParameter param = (SingleValueParameter)secondaryVar.getParameterList().get(row);
    return param.getUIValue().toString();
  }

  float getMin()
  {
    int primarySize = primaryVar == null ? 1 :
      primaryVar.getParameterList().size();
    int secondarySize = secondaryVar == null ? 1 :
      secondaryVar.getParameterList().size();

    float min = Float.MAX_VALUE;
    for (int col = 0; col < primarySize; col++)
    {
      for (int row = 0; row < secondarySize; row++)
      {
        Number val = (Number)statisticLists[col][row];
        if (min > val.floatValue())
          min = val.floatValue();
      }
    }
    return min;
  }

  float getMax()
  {
    int primarySize = primaryVar == null ? 1 :
      primaryVar.getParameterList().size();
    int secondarySize = secondaryVar == null ? 1 :
      secondaryVar.getParameterList().size();

    float max = Float.MIN_VALUE;
    for (int col = 0; col < primarySize; col++)
    {
      for (int row = 0; row < secondarySize; row++)
      {
        Number val = (Number)statisticLists[col][row];
        if (max < val.floatValue())
          max = val.floatValue();
      }
    }
    return max;
  }
}
