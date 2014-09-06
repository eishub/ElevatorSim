/*
 * Copyright 2004-2005 Neil McKellar and Chris Dailey
 * All rights reserved.
 */
package org.intranet.sim.clock;

import org.intranet.sim.Simulator;

/**
 * @author Neil McKellar and Chris Dailey
 * @author W.Pasman 11nov2010 to use preferred time factor.
 */
public class RealTimeClock extends Clock {
	public static class RealTimeClockFactory implements ClockFactory {
		public Clock createClock(FeedbackListener cl) {
			return new RealTimeClock(cl);
		}
	}

	private AnimationLoop animationLoop;
	private Thread animationThread;
	private double realTime2SimTime = 1;

	/** Modified W.Pasman 11nov2010 to use preferred accelFactor */
	public RealTimeClock(FeedbackListener c) {
		super(c);
		setTimeConversion(Simulator.simulatorprefs.getInt(
				Simulator.Keys.TIMEFACTOR.toString(), 0));
	}

	public synchronized void start() {
		if (isRunning())
			throw new IllegalStateException("Can't start while already running");
		setRunningState(true);
		animationLoop = new AnimationLoop();
		animationThread = new Thread(animationLoop);
		animationThread.start();
	}

	public synchronized void pause() {
		if (!isRunning())
			throw new IllegalStateException("Can't pause while not running");
		animationLoop.stop();
		if (animationThread != Thread.currentThread()) {
			try {
				animationThread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		animationThread = null;
		animationLoop = null;
		setRunningState(false);
	}

	@Override
	public void setTimeConversion(int factor) {
		super.setTimeConversion(factor);
		realTime2SimTime = Math.pow(2, accelFactor);

	}

	class AnimationLoop implements Runnable {
		private boolean running = true;

		public void run() {
			// The primary concept here is that the ideal real-time intervals
			// are always the same. The code increments the real-time values to
			// ensure that this is true.
			// The secondary concept is that the simulation time is always in
			// direct proportion to the ideal real-time intervals.
			final long realTimeIncrement = 100;
			long targetRealTime = System.currentTimeMillis();
			long simulationTime = getSimulationTime();
			while (running) {
				setSimulationTime(simulationTime);
				targetRealTime += realTimeIncrement;

				long sleepTime = targetRealTime - System.currentTimeMillis();
				if (sleepTime > 0)
					sleep(sleepTime);

				simulationTime += (long) (realTimeIncrement * realTime2SimTime);
			}
			setSimulationTime(simulationTime);
		}

		private void sleep(long sleepTime) {
			try {
				Thread.sleep(sleepTime);
			} catch (InterruptedException ie) {
				// Ignore it and let the loop go around again
			}
		}

		public void stop() {
			running = false;
		}
	}

	public synchronized void dispose() {
		if (isRunning())
			pause();
	}
}
