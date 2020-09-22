/*
 * Copyright 2003 Neil McKellar and Chris Dailey
 * All rights reserved.
 */
package org.intranet.statistics;

/**
 * @author Neil McKellar and Chris Dailey
 */
public class FloatColumn implements Column {
	private final float[] values;
	private final String heading;
	private double total;
	private float average;
	private float min;
	private float max;
	private int minIndex;
	private int maxIndex;

	public FloatColumn(final String hdr, final float[] vals) {
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
		this.total = 0.0;
		this.min = Float.POSITIVE_INFINITY;
		this.max = Float.NEGATIVE_INFINITY;
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
		this.average = (float) (this.total / this.values.length);
	}
}