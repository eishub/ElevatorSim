/*
 * Copyright 2005 Neil McKellar and Chris Dailey
 * All rights reserved.
 */
package org.intranet.sim.ui.multiple;

class AverageNumber extends Number {
	private static final long serialVersionUID = 1L;
	private double sum = 0.0;
	private int n = 0;

	public AverageNumber() {
		super();
	}

	public AverageNumber(final double d) {
		super();
		add(d);
	}

	public void add(final double d) {
		this.sum += d;
		this.n++;
	}

	@Override
	public int intValue() {
		if (this.n == 0) {
			return 0;
		}
		return (int) (this.sum / this.n);
	}

	@Override
	public long longValue() {
		if (this.n == 0) {
			return 0;
		}
		return (long) (this.sum / this.n);
	}

	@Override
	public float floatValue() {
		if (this.n == 0) {
			return 0;
		}
		return (float) (this.sum / this.n);
	}

	@Override
	public double doubleValue() {
		if (this.n == 0) {
			return 0;
		}
		return (this.sum / this.n);
	}
}
