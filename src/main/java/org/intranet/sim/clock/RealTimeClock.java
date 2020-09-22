/*
 * Copyright 2004-2005 Neil McKellar and Chris Dailey
 * All rights reserved.
 */
package org.intranet.sim.clock;

import org.intranet.sim.Simulator;

/**
 * A clock that actually runs on a real time thread.
 *
 * @author Neil McKellar and Chris Dailey
 * @author W.Pasman 11nov2010 to use preferred time factor.
 */
public class RealTimeClock extends Clock {
	public static class RealTimeClockFactory implements ClockFactory {
		@Override
		public Clock createClock(final FeedbackListener cl) {
			return new RealTimeClock(cl);
		}
	}

	private AnimationLoop animationLoop;
	private Thread animationThread;
	private double realTime2SimTime = 1;

	/**
	 * Modified W.Pasman 11nov2010 to use preferred accelFactor.
	 *
	 * @param c a listener for the clock
	 */
	public RealTimeClock(final FeedbackListener c) {
		super(c);
		setTimeConversion(Simulator.simulatorprefs.getInt(Simulator.Keys.TIMEFACTOR.toString(), 0));
	}

	@Override
	public synchronized void start() {
		if (isRunning()) {
			throw new IllegalStateException("Can't start while already running");
		}
		setRunningState(true);
		this.animationLoop = new AnimationLoop();
		this.animationThread = new Thread(this.animationLoop);
		this.animationThread.start();
	}

	@Override
	public synchronized void pause() {
		if (!isRunning()) {
			throw new IllegalStateException("Can't pause while not running");
		}
		this.animationLoop.stop();
		if (this.animationThread != Thread.currentThread()) {
			try {
				this.animationThread.join();
			} catch (final InterruptedException e) {
				e.printStackTrace();
			}
		}
		this.animationThread = null;
		this.animationLoop = null;
		setRunningState(false);
	}

	@Override
	public void setTimeConversion(final int factor) {
		super.setTimeConversion(factor);
		this.realTime2SimTime = Math.pow(2, this.accelFactor);

	}

	class AnimationLoop implements Runnable {
		private boolean running = true;

		@Override
		public void run() {
			// The primary concept here is that the ideal real-time intervals
			// are always the same. The code increments the real-time values to
			// ensure that this is true.
			// The secondary concept is that the simulation time is always in
			// direct proportion to the ideal real-time intervals.
			final long realTimeIncrement = 100;
			long targetRealTime = System.currentTimeMillis();
			long simulationTime = getSimulationTime();
			while (this.running) {
				setSimulationTime(simulationTime);
				targetRealTime += realTimeIncrement;

				final long sleepTime = targetRealTime - System.currentTimeMillis();
				if (sleepTime > 0) {
					sleep(sleepTime);
				}

				simulationTime += (long) (realTimeIncrement * RealTimeClock.this.realTime2SimTime);
			}
			setSimulationTime(simulationTime);
		}

		private void sleep(final long sleepTime) {
			try {
				Thread.sleep(sleepTime);
			} catch (final InterruptedException ie) {
				// Ignore it and let the loop go around again
			}
		}

		public void stop() {
			this.running = false;
		}
	}

	@Override
	public synchronized void dispose() {
		if (isRunning()) {
			pause();
		}
	}
}
