/*
 * Copyright 2004 Neil McKellar and Chris Dailey
 * All rights reserved.
 */
package org.intranet.sim.ui.multiple;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import org.intranet.ui.MultipleValueParameter;
import org.intranet.ui.SingleValueParameter;

/**
 * @author Neil McKellar and Chris Dailey
 *
 */
class ResultsTable
  extends JPanel
{
  private JScrollPane jsp;
  final private TableCellRenderer tcRenderer;

  static interface ResultsTableListener
  {
    void cellSelected(List params);
  }

  private List listeners = new ArrayList();
  void addResultsTableListener(ResultsTableListener rtl)
  {
    listeners.add(rtl);
  }

  public ResultsTable(final MultipleValueParameter primaryVar,
    final MultipleValueParameter secondaryVar, final ResultsGrid grid)
  {
    super(new BorderLayout());
    jsp = new JScrollPane(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
      ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    final JTable jtable = new JTable();
    tcRenderer = jtable.getDefaultRenderer(Float.class);
    jsp.setViewportView(jtable);
    add(jsp, BorderLayout.CENTER);
    if (primaryVar == null)
      jtable.setTableHeader(null);

    final List primaryParameters = primaryVar == null ? new ArrayList() :
      primaryVar.getParameterList();
    final List secondaryParameters = secondaryVar == null ? new ArrayList() :
      secondaryVar.getParameterList();

    add(new JLabel(primaryVar == null ? "" : primaryVar.getDescription(),
        SwingConstants.CENTER), BorderLayout.NORTH);
    Icon icon = new VTextIcon(this,
      secondaryVar == null ? "" : secondaryVar.getDescription(),
      VTextIcon.ROTATE_LEFT);
    add(new JLabel(icon), BorderLayout.WEST);

    TableModel dtm = new ResultsGridTableModel(primaryParameters, grid, secondaryParameters);

    jtable.getSelectionModel().addListSelectionListener(new ListSelectionListener()
    {
      public void valueChanged(ListSelectionEvent listSelEvt)
      {
        int row = jtable.getSelectedRow();
        int column = jtable.getSelectedColumn();
        if (!listSelEvt.getValueIsAdjusting() || row == -1 || column == -1)
          return;
        List parameters = grid.getParameters(column, row);
        for (Iterator i = listeners.iterator(); i.hasNext(); )
        {
          ResultsTableListener rtl = (ResultsTableListener)i.next();
          rtl.cellSelected(parameters);
        }
      }
    });

    jtable.setModel(dtm);

    TableCellRenderer customRenderer = new ResultsTableRenderer(tcRenderer,
        grid.getMin(), grid.getMax());
    TableColumnModel colModel = jtable.getColumnModel();
    for (int i = 0; i < colModel.getColumnCount(); i++)
      colModel.getColumn(i).setCellRenderer(customRenderer);

    if (!secondaryParameters.isEmpty())
    {
      JTable rowTable = new JTable(new ResultsRowTableModel(secondaryParameters));
      TableColumn column = rowTable.getColumnModel().getColumn(0);
      column.setPreferredWidth(50);
      column.setMaxWidth(50);
      rowTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
      rowTable.setBackground(jsp.getBackground());
      JViewport jvp = new JViewport();
      jvp.setPreferredSize(rowTable.getMaximumSize());
      jvp.setMaximumSize(rowTable.getMaximumSize());
      jvp.setView(rowTable);
      jsp.setRowHeader(jvp);
    }
    // TODO: Label for statistic choice
  }

  static private int interpolate(float percent, int min, int max)
  {
    int result;
    result = (int)(((max - min) * percent) + min);
    return result;
  }

  static private float computePercentage(float f, float min, float max)
  {
    if (max - min == 0)
      return 0.0f;
    return (f - min) / (max - min);
  }
  
  private static final class ResultsRowTableModel
    extends AbstractTableModel
  {
    private List params;
    public ResultsRowTableModel(List secondaryParameters)
    {
      super();
      params = secondaryParameters;
    }

    public int getRowCount()
    {
      return params.size();
//      return params.isEmpty() ? 1 : params.size();
    }

    public int getColumnCount()
    {
      return params.isEmpty() ? 0 : 1;
    }

    public Object getValueAt(int row, int column)
    {
      SingleValueParameter p = (SingleValueParameter)params.get(row);
      return p.getUIValue();
    }
  
  }

  private static final class ResultsGridTableModel
    extends AbstractTableModel
  {
    private final List primaryParameters;
    private final ResultsGrid grid;
    private final List secondaryParameters;

    private ResultsGridTableModel(List primaryParameters, ResultsGrid grid,
        List secondaryParameters)
    {
      super();
      this.primaryParameters = primaryParameters;
      this.grid = grid;
      this.secondaryParameters = secondaryParameters;
    }

    public int getColumnCount()
    {
      return primaryParameters.isEmpty() ? 1 : primaryParameters.size();
    }

    public int getRowCount()
    {
      return secondaryParameters.isEmpty() ? 1 : secondaryParameters.size();
    }

    public Object getValueAt(int row, int column)
    {
      return grid.getResult(column, row);
    }

    public String getColumnName(int col)
    {
      return primaryParameters.isEmpty() ? "Value" : grid.getColumnName(col);
    }
  }

  private static class ResultsTableRenderer implements TableCellRenderer
  {
    private Color minColor = Color.WHITE;
    private Color maxColor = Color.RED;
    private TableCellRenderer tcRenderer;
    private float min;
    private float max;
    
    public ResultsTableRenderer(TableCellRenderer defaultRenderer,
        float min, float max)
    {
      super();
      tcRenderer = defaultRenderer;
      this.min = min;
      this.max = max;
    }

    public Component getTableCellRendererComponent(JTable jtable, Object value,
        boolean isSelected, boolean hasFocus, int row, int column)
    {
      Component c = tcRenderer.getTableCellRendererComponent(jtable, value,
          isSelected, hasFocus, row, column);
      Number floatValue = (Number)value;
      float percent = computePercentage(floatValue.floatValue(), min, max);
      int red = interpolate(percent, minColor.getRed(), maxColor.getRed());
      int green = interpolate(percent, minColor.getGreen(), maxColor.getGreen());
      int blue = interpolate(percent, minColor.getBlue(), maxColor.getBlue());
      Color valueColor = new Color(red, green, blue);
      c.setBackground(valueColor);
      return c;
    }
  } 
}