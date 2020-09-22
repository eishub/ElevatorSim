/*
* Copyright 2004 Neil McKellar and Chris Dailey
* All rights reserved.
*/
package org.intranet.sim.ui.multiple;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.intranet.statistics.Column;
import org.intranet.statistics.Table;
import org.intranet.ui.MultipleValueParameter;
import org.intranet.ui.Parameter;
import org.intranet.ui.SingleValueParameter;

/**
 * @author Neil McKellar and Chris Dailey
 */
public class ResultsGrid {
	MultipleValueParameter primaryVar;
	MultipleValueParameter secondaryVar;
	MultipleValueParameter averageVar;
	List<Parameter> otherVariables;
	List<Parameter>[][] parameterLists;
	AverageNumber[][] statisticLists;

	@SuppressWarnings("unchecked")
	ResultsGrid(final Map<List<Parameter>, List<Table>> results, final MultipleValueParameter primaryVar,
			final MultipleValueParameter secondaryVar, final MultipleValueParameter averageVar,
			final List<Parameter> otherVariables, final StatisticVariable statisticsSelection) {
		this.primaryVar = primaryVar;
		this.secondaryVar = secondaryVar;
		this.otherVariables = otherVariables;
		this.averageVar = averageVar;

		final int primarySize = primaryVar == null ? 1 : primaryVar.getParameterList().size();
		final int secondarySize = secondaryVar == null ? 1 : secondaryVar.getParameterList().size();

		this.parameterLists = new List[primarySize][];
		this.statisticLists = new AverageNumber[primarySize][];
		for (int i = 0; i < primarySize; i++) {
			this.parameterLists[i] = new List[secondarySize];
			this.statisticLists[i] = new AverageNumber[secondarySize];
		}

		// Iterate through the statistics results to extract the values for the
		// variables we are interested in (based on primary and secondary selections).
		// Get column "names" (actually variable values) and row names (to do later)
		// along the way.
		for (final Entry<List<Parameter>, List<Table>> entry : results.entrySet()) {
			final List<Parameter> params = entry.getKey();
			final List<Table> statistics = entry.getValue();

			if (!variablesMatch(params)) {
				continue;
			}
			final int column = primaryVar == null ? 0 : findPrimaryColumn(params);
			final int row = secondaryVar == null ? 0 : findSecondaryRow(params);
			final Number result = getStatistic(statistics, statisticsSelection);
			final AverageNumber num = this.statisticLists[column][row];

			if (num == null) {
				this.statisticLists[column][row] = new AverageNumber(result.doubleValue());
			} else {
				num.add(result.doubleValue());
			}
			this.parameterLists[column][row] = params;
		}
	}

	private Number getStatistic(final List<Table> statistics, final StatisticVariable statisticsSelection) {
		// find the statistic that was requested from the statistics chooser
		for (final Table table2 : statistics) {
			final Table table = table2;
			if (statisticsSelection.getTableName().equals(table.getName())) {
				for (int colNum = 0; colNum < table.getColumnCount(); colNum++) {
					final Column column = table.getColumn(colNum);
					if (column.getHeading().equals(statisticsSelection.getStatisticName())) {
						// TODO: factor out explicit case analysis to classes
						if ("Avg".equals(statisticsSelection.getFunctionName())) {
							return column.getAverage();
						} else if ("Min".equals(statisticsSelection.getFunctionName())) {
							return column.getMin();
						} else if ("Max".equals(statisticsSelection.getFunctionName())) {
							return column.getMax();
						}
					}
				}
			}
		}
		throw new IllegalArgumentException(
				"Couldn't find value for statistic " + statisticsSelection.getFunctionName());
	}

	private int findSecondaryRow(final List<Parameter> params) {
		for (final Parameter parameter : params) {
			final SingleValueParameter p = (SingleValueParameter) parameter;
			if (this.secondaryVar.getDescription().equals(p.getDescription())) {
				int idx = 0;
				for (final Parameter parameter2 : this.secondaryVar.getParameterList()) {
					final SingleValueParameter sec = (SingleValueParameter) parameter2;
					if (p.getUIValue().equals(sec.getUIValue())) {
						return idx;
					}
					idx++;
				}
			}
		}
		throw new IllegalArgumentException("Could not find parameter named " + this.secondaryVar.getDescription());
	}

	private int findPrimaryColumn(final List<Parameter> params) {
		for (final Parameter parameter : params) {
			final SingleValueParameter p = (SingleValueParameter) parameter;
			if (this.primaryVar.getDescription().equals(p.getDescription())) {
				int idx = 0;
				for (final Object element : this.primaryVar.getParameterList()) {
					final SingleValueParameter prim = (SingleValueParameter) element;
					if (p.getUIValue().equals(prim.getUIValue())) {
						return idx;
					}
					idx++;
				}
			}
		}
		throw new IllegalArgumentException("Could not find parameter named " + this.primaryVar.getDescription());
	}

	private boolean variablesMatch(final List<Parameter> params) {
		for (final Parameter parameter : params) {
			final SingleValueParameter p = (SingleValueParameter) parameter;
			final String desc = p.getDescription();
			final Object val = p.getUIValue();
			if (this.primaryVar != null && desc.equals(this.primaryVar.getDescription())) {
				continue;
			}
			if (this.secondaryVar != null && desc.equals(this.secondaryVar.getDescription())) {
				continue;
			}
			for (final Parameter parameter2 : this.otherVariables) {
				final SingleValueParameter other = (SingleValueParameter) parameter2;
				// If this is an average variable, we want all instances of it to
				// match. So just continue here and don't risk doing a test that
				// will result in returning false.
				if (this.averageVar != null && this.averageVar.getDescription().equals(other.getDescription())) {
					continue;
				}
				if (desc.equals(other.getDescription())) {
					if (!val.equals(other.getUIValue())) {
						return false;
					}
					break;
				}
			}
		}
		return true;
	}

	List<Parameter> getParameters(final int col, final int row) {
		return this.parameterLists[col][row];
	}

	Object getResult(final int col, final int row) {
		return this.statisticLists[col][row];
	}

	String getColumnName(final int col) {
		final SingleValueParameter param = (SingleValueParameter) this.primaryVar.getParameterList().get(col);
		return param.getUIValue().toString();
	}

	String getRowName(final int row) {
		final SingleValueParameter param = (SingleValueParameter) this.secondaryVar.getParameterList().get(row);
		return param.getUIValue().toString();
	}

	float getMin() {
		final int primarySize = this.primaryVar == null ? 1 : this.primaryVar.getParameterList().size();
		final int secondarySize = this.secondaryVar == null ? 1 : this.secondaryVar.getParameterList().size();

		float min = Float.MAX_VALUE;
		for (int col = 0; col < primarySize; col++) {
			for (int row = 0; row < secondarySize; row++) {
				final Number val = this.statisticLists[col][row];
				if (min > val.floatValue()) {
					min = val.floatValue();
				}
			}
		}
		return min;
	}

	float getMax() {
		final int primarySize = this.primaryVar == null ? 1 : this.primaryVar.getParameterList().size();
		final int secondarySize = this.secondaryVar == null ? 1 : this.secondaryVar.getParameterList().size();

		float max = Float.MIN_VALUE;
		for (int col = 0; col < primarySize; col++) {
			for (int row = 0; row < secondarySize; row++) {
				final Number val = this.statisticLists[col][row];
				if (max < val.floatValue()) {
					max = val.floatValue();
				}
			}
		}
		return max;
	}
}
