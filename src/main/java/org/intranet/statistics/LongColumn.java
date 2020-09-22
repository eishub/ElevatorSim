/*
 * Copyright 2003 Neil McKellar and Chris Dailey
 * All rights reserved.
 */
package org.intranet.statistics;

/**
 * @author Neil McKellar and Chris Dailey
 */
public class LongColumn implements Column {
	private final long[] values;
	private final String heading;
	private long total;
	private float average;
	private long min;
	private long max;
	private int minIndex;
	private int maxIndex;

	public LongColumn(final String hdr, final long[] vals) {
		this.values = vals;
		this.heading = hdr;
		calculate();
	}

	@Override
	public String getHeading() {
		return this.heading;
	}

	@Override
	public Number getTotal() {
		return this.total;
	}

	@Override
	public Number getAverage() {
		return this.average;
	}

	@Override
	public Number getMin() {
		return this.min;
	}

	@Override
	public Number getMax() {
		return this.max;
	}

	@Override
	public int getValueCount() {
		return this.values.length;
	}

	@Override
	public Number getValue(final int x) {
		return this.values[x];
	}

	@Override
	public int getMinIndex() {
		return this.minIndex;
	}

	@Override
	public int getMaxIndex() {
		return this.maxIndex;
	}

	@Override
	public boolean isMin(final int index) {
		return (this.values[index] == this.values[this.minIndex]);
	}

	@Override
	public boolean isMax(final int index) {
		return (this.values[index] == this.values[this.maxIndex]);
	}

	private void calculate() {
		this.total = 0;
		this.min = Long.MAX_VALUE;
		this.max = Long.MIN_VALUE;
		for (int i = 0; i < this.values.length; i++) {
			this.total += this.values[i];
			if (this.values[i] < this.min) {
				this.min = this.values[i];
				this.minIndex = i;
			}
			if (this.values[i] > this.max) {
				this.max = this.values[i];
				this.maxIndex = i;
			}
		}
		this.average = (1.0F * this.total) / this.values.length;
	}
}