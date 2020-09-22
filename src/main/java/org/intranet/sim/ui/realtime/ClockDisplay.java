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
	private static final long serialVersionUID = 1L;
	private static final int NUMERIC_FORMAT = 0;
	private static final int DURATION_FORMAT = 1;
	private final int format = DURATION_FORMAT;

	// LATER: May need a way to remove the listener
	private final Clock.Listener listener = new Clock.Listener() {
		@Override
		public void timeUpdate(final long time) {
			updateTime(time);
		}

		@Override
		public void stateUpdate(final boolean running) {
		}

		@Override
		public void timeFactorUpdate(final int timeFactor) {
		}
	};

	public ClockDisplay() {
		super();
	}

	void setClock(final Clock clock) {
		clock.addListener(this.listener);
		updateTime(clock.getSimulationTime());
	}

	private void updateTime(final long time) {
		if (this.format == NUMERIC_FORMAT) {
			setText(Long.toString(time));
		} else if (this.format == DURATION_FORMAT) {
			setText(Duration.format(time));
		} else {
			throw new IllegalArgumentException("bad duration format");
		}
	}
}
