/*
 * Copyright 2004 Neil McKellar and Chris Dailey
 * All rights reserved.
 */
package org.intranet.sim.ui.multiple;

/**
 * @author Neil McKellar and Chris Dailey
 */
public class StatisticVariable {
	private final String tableName;
	private final String functionName;
	private final String statisticName;

	StatisticVariable(final String table, final String function, final String statistic) {
		this.tableName = table;
		this.functionName = function;
		this.statisticName = statistic;
	}

	@Override
	public String toString() {
		return this.tableName + " " + this.functionName + " " + this.statisticName;
	}

	public String getTableName() {
		return this.tableName;
	}

	public String getStatisticName() {
		return this.statisticName;
	}

	public String getFunctionName() {
		return this.functionName;
	}
}
