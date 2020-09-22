/*
 * Copyright 2004 Neil McKellar and Chris Dailey
 * All rights reserved.
 */
package org.intranet.sim.ui.multiple;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import org.intranet.ui.MultipleValueParameter;
import org.intranet.ui.Parameter;
import org.intranet.ui.SingleValueParameter;

/**
 * @author Neil McKellar and Chris Dailey
 */
class ResultsTable extends JPanel {
	private static final long serialVersionUID = 1L;
	private JScrollPane jsp;
	final private TableCellRenderer tcRenderer;

	interface ResultsTableListener {
		void cellSelected(List<Parameter> params);
	}

	private final List<ResultsTableListener> listeners = new LinkedList<>();

	void addResultsTableListener(final ResultsTableListener rtl) {
		this.listeners.add(rtl);
	}

	public ResultsTable(final MultipleValueParameter primaryVar, final MultipleValueParameter secondaryVar,
			final ResultsGrid grid) {
		super(new BorderLayout());
		this.jsp = new JScrollPane(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		final JTable jtable = new JTable();
		this.tcRenderer = jtable.getDefaultRenderer(Float.class);
		this.jsp.setViewportView(jtable);
		add(this.jsp, BorderLayout.CENTER);
		if (primaryVar == null) {
			jtable.setTableHeader(null);
		}

		final List<Parameter> primaryParameters = primaryVar == null ? new ArrayList<>(0)
				: primaryVar.getParameterList();
		final List<Parameter> secondaryParameters = secondaryVar == null ? new ArrayList<>(0)
				: secondaryVar.getParameterList();

		add(new JLabel(primaryVar == null ? "" : primaryVar.getDescription(), SwingConstants.CENTER),
				BorderLayout.NORTH);
		final Icon icon = new VTextIcon(this, secondaryVar == null ? "" : secondaryVar.getDescription(),
				VTextIcon.ROTATE_LEFT);
		add(new JLabel(icon), BorderLayout.WEST);

		final TableModel dtm = new ResultsGridTableModel(primaryParameters, grid, secondaryParameters);

		jtable.getSelectionModel().addListSelectionListener(listSelEvt -> {
			final int row = jtable.getSelectedRow();
			final int column = jtable.getSelectedColumn();
			if (!listSelEvt.getValueIsAdjusting() || row == -1 || column == -1) {
				return;
			}
			final List<Parameter> parameters = grid.getParameters(column, row);
			for (final ResultsTableListener rtl : ResultsTable.this.listeners) {
				rtl.cellSelected(parameters);
			}
		});

		jtable.setModel(dtm);

		final TableCellRenderer customRenderer = new ResultsTableRenderer(this.tcRenderer, grid.getMin(),
				grid.getMax());
		final TableColumnModel colModel = jtable.getColumnModel();
		for (int i = 0; i < colModel.getColumnCount(); i++) {
			colModel.getColumn(i).setCellRenderer(customRenderer);
		}

		if (!secondaryParameters.isEmpty()) {
			final JTable rowTable = new JTable(new ResultsRowTableModel(secondaryParameters));
			final TableColumn column = rowTable.getColumnModel().getColumn(0);
			column.setPreferredWidth(50);
			column.setMaxWidth(50);
			rowTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
			rowTable.setBackground(this.jsp.getBackground());
			final JViewport jvp = new JViewport();
			jvp.setPreferredSize(rowTable.getMaximumSize());
			jvp.setMaximumSize(rowTable.getMaximumSize());
			jvp.setView(rowTable);
			this.jsp.setRowHeader(jvp);
		}
		// TODO: Label for statistic choice
	}

	static private int interpolate(final float percent, final int min, final int max) {
		int result;
		result = (int) (((max - min) * percent) + min);
		return result;
	}

	static private float computePercentage(final float f, final float min, final float max) {
		if (max - min == 0) {
			return 0.0f;
		}
		return (f - min) / (max - min);
	}

	private static final class ResultsRowTableModel extends AbstractTableModel {
		private static final long serialVersionUID = 1L;
		private final List<Parameter> params;

		public ResultsRowTableModel(final List<Parameter> secondaryParameters) {
			super();
			this.params = secondaryParameters;
		}

		@Override
		public int getRowCount() {
			return this.params.size();
		}

		@Override
		public int getColumnCount() {
			return this.params.isEmpty() ? 0 : 1;
		}

		@Override
		public Object getValueAt(final int row, final int column) {
			final SingleValueParameter p = (SingleValueParameter) this.params.get(row);
			return p.getUIValue();
		}

	}

	private static final class ResultsGridTableModel extends AbstractTableModel {
		private static final long serialVersionUID = 1L;
		private final List<Parameter> primaryParameters;
		private final ResultsGrid grid;
		private final List<Parameter> secondaryParameters;

		private ResultsGridTableModel(final List<Parameter> primaryParameters, final ResultsGrid grid,
				final List<Parameter> secondaryParameters) {
			super();
			this.primaryParameters = primaryParameters;
			this.grid = grid;
			this.secondaryParameters = secondaryParameters;
		}

		@Override
		public int getColumnCount() {
			return this.primaryParameters.isEmpty() ? 1 : this.primaryParameters.size();
		}

		@Override
		public int getRowCount() {
			return this.secondaryParameters.isEmpty() ? 1 : this.secondaryParameters.size();
		}

		@Override
		public Object getValueAt(final int row, final int column) {
			return this.grid.getResult(column, row);
		}

		@Override
		public String getColumnName(final int col) {
			return this.primaryParameters.isEmpty() ? "Value" : this.grid.getColumnName(col);
		}
	}

	private static class ResultsTableRenderer implements TableCellRenderer {
		private final Color minColor = Color.WHITE;
		private final Color maxColor = Color.RED;
		private final TableCellRenderer tcRenderer;
		private final float min;
		private final float max;

		public ResultsTableRenderer(final TableCellRenderer defaultRenderer, final float min, final float max) {
			super();
			this.tcRenderer = defaultRenderer;
			this.min = min;
			this.max = max;
		}

		@Override
		public Component getTableCellRendererComponent(final JTable jtable, final Object value,
				final boolean isSelected, final boolean hasFocus, final int row, final int column) {
			final Component c = this.tcRenderer.getTableCellRendererComponent(jtable, value, isSelected, hasFocus, row,
					column);
			final Number floatValue = (Number) value;
			final float percent = computePercentage(floatValue.floatValue(), this.min, this.max);
			final int red = interpolate(percent, this.minColor.getRed(), this.maxColor.getRed());
			final int green = interpolate(percent, this.minColor.getGreen(), this.maxColor.getGreen());
			final int blue = interpolate(percent, this.minColor.getBlue(), this.maxColor.getBlue());
			final Color valueColor = new Color(red, green, blue);
			c.setBackground(valueColor);
			return c;
		}
	}
}