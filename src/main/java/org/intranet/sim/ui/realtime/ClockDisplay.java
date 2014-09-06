/*
 * Copyright 2003,2005 Neil McKellar and Chris Dailey
 * All rights reserved.
 */
package org.intranet.sim.ui.realtime;

import javax.swing.JLabel;

import org.intranet.sim.clock.Clock;

/**
 * @author Neil McKellar and Chris Dailey
 */
public class ClockDisplay extends JLabel {
	private static final int NUMERIC_FORMAT = 0;
	private static final int DURATION_FORMAT = 1;
	private int format = DURATION_FORMAT;

	// LATER: May need a way to remove the listener
	private Clock.Listener listener = new Clock.Listener() {
		@Override
		public void timeUpdate(long time) {
			updateTime(time);
		}

		@Override
		public void stateUpdate(boolean running) {
		}

		@Override
		public void timeFactorUpdate(int timeFactor) {
		}
	};

	public ClockDisplay() {
		super();
	}

	void setClock(Clock clock) {
		clock.addListener(listener);
		updateTime(clock.getSimulationTime());
	}

	private void updateTime(long time) {
		if (format == NUMERIC_FORMAT)
			setText(Long.toString(time));
		else if (format == DURATION_FORMAT)
			setText(Duration.format(time));
		else
			throw new IllegalArgumentException("bad duration format");
	}
}
