/*
 * Copyright 2003 Neil McKellar and Chris Dailey
 * All rights reserved.
 */
package org.intranet.statistics;

import java.util.LinkedList;
import java.util.List;

/**
 * @author Neil McKellar and Chris Dailey
 */
public class Table {
	private final String name;
	private final List<Column> columns = new LinkedList<>();

	public Table(final String[] rows, final String tblName) {
		super();
		this.rowNames = rows;
		this.name = tblName;
	}

	public void addColumn(final Column c) {
		this.columns.add(c);
	}

	private final String rowNames[];

	public int getRowCount() {
		return this.rowNames.length;
	}

	public String getRowName(final int i) {
		return this.rowNames[i];
	}

	public String getName() {
		return this.name;
	}

	public int getColumnCount() {
		return this.columns.size();
	}

	public Column getColumn(final int i) {
		return this.columns.get(i);
	}
}