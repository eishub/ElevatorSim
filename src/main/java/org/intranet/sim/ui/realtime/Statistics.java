/*
 * Copyright 2003-2005 Neil McKellar and Chris Dailey
 * All rights reserved.
 */
package org.intranet.sim.ui.realtime;

import java.awt.Color;
import java.awt.Component;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

import org.intranet.sim.Model;
import org.intranet.statistics.Column;
import org.intranet.statistics.Table;

/**
 * @author Neil McKellar and Chris Dailey
 *
 */
public final class Statistics
  extends JComponent
{
  private static final Color max = new Color(0xff, 0xcc, 0xcc);
  private static final Color min = new Color(0xdd, 0xee, 0xff);

  private final class StatisticsTableCellRenderer extends DefaultTableCellRenderer
  {
    private final Table t;
    private StatisticsTableCellRenderer(Table t)
    {
      super();
      this.t = t;
    }
    public Component getTableCellRendererComponent(JTable table,
                      Object value, boolean isSelected, boolean hasFocus,
                      int row, int column)
    {
      JLabel jl = (JLabel) super.getTableCellRendererComponent(table, value,
                                 isSelected, hasFocus, row, column);
      jl.setBackground(Color.white);
      if (column > 0)
      {
        Column c = t.getColumn(column - 1);
        // Avoid setting color on Total, Min, Max, or Average
        if (row < c.getValueCount())
        {
          if (c.isMax(row))
            jl.setBackground(max);
          if (c.isMin(row))
            jl.setBackground(min);
        }
        else
          jl.setBackground(Color.lightGray);
      }
      return jl;
    }
  }

  private final class StatisticsTableModel extends AbstractTableModel
  {
    private final Table t;
    String[] tmma = { "Total", "Min", "Max", "Avg" };
    private StatisticsTableModel(Table t)
    {
      // TODO : detect when the table changes (e.g. Real-time statistics updates)
      super();
      this.t = t;
    }
    public Object getValueAt(int row, int col)
    {
      if (col == 0)
      {
        if (row < t.getRowCount())
          return t.getRowName(row);
        return tmma[row - t.getRowCount()];
      }
      Column c = t.getColumn(col - 1);
      if (row < t.getRowCount())
        return c.getValue(row);
      int over = row - t.getRowCount();
      if (over == 0)
        return c.getTotal();
      if (over == 1)
        return c.getMin();
      if (over == 2)
        return c.getMax();
      if (over == 3)
        return c.getAverage();
      return null;
    }
    public int getColumnCount() { return 1 + t.getColumnCount(); }
    public int getRowCount() { return t.getRowCount() + 4; }
    public String getColumnName(int i)
    {
      if (i == 0) return "Object";
      Column c = t.getColumn(i - 1);
      return c.getHeading();
    }
  }

  private Model model;
  private JTabbedPane jtp;
  private List jtables = new ArrayList();

  public Statistics()
  {
    super();
    setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
    jtp = new JTabbedPane();
    add(jtp);
    setModel(null);
  }

  void setModel(Model mdl)
  {
    model = mdl;
    updateStatistics();
  }

  void updateStatistics()
  {
    if (model == null)
    {
      while (jtp.getTabCount() > 0)
        jtp.removeTabAt(0);
      return;
    }
    final List tables = model.getStatistics();
    // updateStatistics is called from the simulation runner thread, and
    // as a result it needs to update the user interface.  The user interface
    // update must happen within the AWT event queue, so we will invoke it
    // later in its own Runnable.
    SwingUtilities.invokeLater(new Runnable()
    {
      public void run()
      {
        if (jtp.getTabCount() == tables.size())
        {
          int tblNum = 0;
          for (Iterator tblI = tables.iterator(); tblI.hasNext(); tblNum++)
          {
            // TODO : if table is different, create a new statistics table model, else no change
            final Table t = (Table)tblI.next();
            JTable jt = (JTable)jtables.get(tblNum);
            jt.setModel(new StatisticsTableModel(t));
            jt.setDefaultRenderer(Object.class, new StatisticsTableCellRenderer(t));
          }
        }
        else
        {
          while (jtp.getTabCount() > 0)
            jtp.removeTabAt(0);
          int tblNum = 0;
          jtables.clear();
          for (Iterator tblI = tables.iterator(); tblI.hasNext(); tblNum++)
          {
            final Table t = (Table)tblI.next();
            JTable jt = new JTable(new StatisticsTableModel(t));
            jtables.add(jt);
            jt.setDefaultRenderer(Object.class, new StatisticsTableCellRenderer(t));
            JScrollPane jsp = new JScrollPane(jt,
              ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
              ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            jtp.addTab(t.getName(), jsp);
          }
        }
      }
    });
  }
}