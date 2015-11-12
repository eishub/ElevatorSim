/*
 * Copyright 2003-2005 Neil McKellar and Chris Dailey
 * All rights reserved.
 */
package org.intranet.sim.clock;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Contains the basic Clock functionality of the simulator. Just stores the
 * actual time and handles listeners. See {@link RealTimeClock} for the actual
 * clock.
 * <p>
 * There are TWO types of listeners here.
 * <ul>
 * <li>One is the FeedbackListener, which is passed to the Constructor or set
 * with {@link #setFeedbackListener}. This FeedbackListener CONTROLS the time
 * with the timeUpdate callback.
 * <li>The other one is the {link #Listener} which allows you to listen to
 * changes but not to influence the clock in the listen process.
 * </ul>
 * TODO Link not working
 * 
 * @author Neil McKellar and Chris Dailey
 * 
 */
public abstract class Clock {
	private boolean isRunning = false;
	protected List<Listener> listeners = new ArrayList<Listener>();
	private FeedbackListener feedbackListener;
	protected int accelFactor;

	protected long simulationTime;

	public interface Listener {
		/**
		 * Informs about elapsed time since start
		 * 
		 * @param time
		 *            time in ms since start
		 */
		void timeUpdate(long time);

		void stateUpdate(boolean running);

		/**
		 * called when the time factor changes. 2^(time factor) is the ratio
		 * simulated time/real time.
		 * 
		 * @param timeFactor
		 *            Allowed integers in range [-20..20].
		 */
		void timeFactorUpdate(int timeFactor);
	}

	public interface FeedbackListener {
		/**
		 * Informs about elapsed time since start
		 * 
		 * @param time
		 *            time in ms since start
		 */
		long timeUpdate(long time);
	}

	public Clock(FeedbackListener c) {
		super();
		setFeedbackListener(c);
	}

	/**
	 * @return the simulation time in ms from start
	 */
	public final long getSimulationTime() {
		return simulationTime;
	}

	public final void addListener(Listener l) {
		listeners.add(l);
		// inform new listener of latest state of affairs
		l.timeFactorUpdate(accelFactor);
	}

	public final void setFeedbackListener(FeedbackListener l) {
		feedbackListener = l;
	}

	public final boolean isRunning() {
		return isRunning;
	}

	protected final void setRunningState(boolean newRunningState) {
		isRunning = newRunningState;
		for (Iterator i = listeners.iterator(); i.hasNext();) {
			Listener l = (Listener) i.next();
			l.stateUpdate(isRunning);
		}
	}

	/**
	 * 
	 * @param t
	 *            the simulation time in ms from start. Notice that the
	 *            simulation time is the real time multiplied with the
	 *            acceleration factor
	 */
	protected final void setSimulationTime(long t) {
		simulationTime = feedbackListener.timeUpdate(t);
		for (Iterator i = listeners.iterator(); i.hasNext();) {
			Listener l = (Listener) i.next();
			l.timeUpdate(simulationTime);
		}
	}

	/**
	 * time conversion is basic aspect of clock now. Added W.Pasman 11nov2010
	 * 
	 * @param factor
	 *            is time factor [-20..20]. This indicates that the ratio
	 *            simulated/real time = 2^timefactor.
	 */
	public void setTimeConversion(int factor) {
		accelFactor = factor;
		for (Listener l : listeners) {
			l.timeFactorUpdate(accelFactor);
		}

	}

	/**
	 * Get the time factor. See {@link #setTimeConversion(int)}
	 * 
	 * @return current time factor [-20..20]. This indicates that the current
	 *         ratio simulated/real time = 2^timefactor.
	 */
	public int getTimeConversion() {
		return accelFactor;
	}

	public abstract void dispose();

	public abstract void start();

	public abstract void pause();
}
