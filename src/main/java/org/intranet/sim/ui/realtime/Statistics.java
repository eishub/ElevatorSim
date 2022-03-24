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
public final class Statistics extends JComponent {
	private static final long serialVersionUID = 1L;
	private static final Color max = new Color(0xff, 0xcc, 0xcc);
	private static final Color min = new Color(0xdd, 0xee, 0xff);

	private final class StatisticsTableCellRenderer extends DefaultTableCellRenderer {
		private static final long serialVersionUID = 1L;
		private final Table t;

		private StatisticsTableCellRenderer(final Table t) {
			this.t = t;
		}

		@Override
		public Component getTableCellRendererComponent(final JTable table, final Object value, final boolean isSelected,
				final boolean hasFocus, final int row, final int column) {
			final JLabel jl = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row,
					column);
			jl.setBackground(Color.white);
			if (column > 0) {
				final Column c = this.t.getColumn(column - 1);
				// Avoid setting color on Total, Min, Max, or Average
				if (row < c.getValueCount()) {
					if (c.isMax(row)) {
						jl.setBackground(max);
					}
					if (c.isMin(row)) {
						jl.setBackground(min);
					}
				} else {
					jl.setBackground(Color.lightGray);
				}
			}
			return jl;
		}
	}

	private static final class StatisticsTableModel extends AbstractTableModel {
		private static final long serialVersionUID = 1L;
		private final Table t;
		private final String[] tmma = { "Total", "Min", "Max", "Avg" };

		private StatisticsTableModel(final Table t) {
			// TODO : detect when the table changes (e.g. Real-time statistics updates)
			this.t = t;
		}

		@Override
		public Object getValueAt(final int row, final int col) {
			if (col == 0) {
				if (row < this.t.getRowCount()) {
					return this.t.getRowName(row);
				}
				return this.tmma[row - this.t.getRowCount()];
			}
			final Column c = this.t.getColumn(col - 1);
			if (row < this.t.getRowCount()) {
				return c.getValue(row);
			}
			final int over = row - this.t.getRowCount();
			switch (over) {
			case 0:
				return c.getTotal();
			case 1:
				return c.getMin();
			case 2:
				return c.getMax();
			case 3:
				return c.getAverage();
			default:
				break;
			}
			return null;
		}

		@Override
		public int getColumnCount() {
			return 1 + this.t.getColumnCount();
		}

		@Override
		public int getRowCount() {
			return this.t.getRowCount() + 4;
		}

		@Override
		public String getColumnName(final int i) {
			if (i == 0) {
				return "Object";
			}
			final Column c = this.t.getColumn(i - 1);
			return c.getHeading();
		}
	}

	private Model model;
	private final JTabbedPane jtp;
	private final List<JTable> jtables = new ArrayList<>();

	public Statistics() {
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		this.jtp = new JTabbedPane();
		add(this.jtp);
		setModel(null);
	}

	void setModel(final Model mdl) {
		this.model = mdl;
		updateStatistics();
	}

	void updateStatistics() {
		if (this.model == null) {
			while (this.jtp.getTabCount() > 0) {
				this.jtp.removeTabAt(0);
			}
			return;
		}
		final List<Table> tables = this.model.getStatistics();
		// updateStatistics is called from the simulation runner thread, and
		// as a result it needs to update the user interface. The user interface
		// update must happen within the AWT event queue, so we will invoke it
		// later in its own Runnable.
		SwingUtilities.invokeLater(() -> {
			if (Statistics.this.jtp.getTabCount() == tables.size()) {
				int tblNum1 = 0;
				for (final Iterator<Table> tblI1 = tables.iterator(); tblI1.hasNext(); tblNum1++) {
					// TODO : if table is different, create a new statistics table model, else no
					// change
					final Table t1 = tblI1.next();
					final JTable jt1 = Statistics.this.jtables.get(tblNum1);
					jt1.setModel(new StatisticsTableModel(t1));
					jt1.setDefaultRenderer(Object.class, new StatisticsTableCellRenderer(t1));
				}
			} else {
				while (Statistics.this.jtp.getTabCount() > 0) {
					Statistics.this.jtp.removeTabAt(0);
				}
				Statistics.this.jtables.clear();
				for (final Table t2 : tables) {
					final JTable jt2 = new JTable(new StatisticsTableModel(t2));
					Statistics.this.jtables.add(jt2);
					jt2.setDefaultRenderer(Object.class, new StatisticsTableCellRenderer(t2));
					final JScrollPane jsp = new JScrollPane(jt2, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
							ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
					Statistics.this.jtp.addTab(t2.getName(), jsp);
				}
			}
		});
	}
}