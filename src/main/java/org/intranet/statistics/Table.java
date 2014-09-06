/*
 * Copyright 2003 Neil McKellar and Chris Dailey
 * All rights reserved.
 */
package org.intranet.statistics;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Neil McKellar and Chris Dailey
 *
 */
public class Table
{
  private String name;
  public Table(String[] rows, String tblName)
  {
    super();
    rowNames = rows;
    name = tblName;
  }
  public void addColumn(Column c)
  {
    columns.add(c);
  }
  private String rowNames[];
  public int getRowCount() { return rowNames.length; }
  public String getRowName(int i) { return rowNames[i]; }

  public String getName() { return name; }
  private List columns = new ArrayList();
  public int getColumnCount() { return columns.size(); }
  public Column getColumn(int i) { return (Column)columns.get(i); }
}