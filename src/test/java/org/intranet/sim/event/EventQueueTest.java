package org.intranet.sim.event;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.intranet.elevator.model.Car;
import org.intranet.sim.event.EventQueue.Listener;
import org.junit.Test;

import elevatorenv.GOALController;

/*
 * Tests for the Event mechanism. 
 * @author W.Pasman 10nov15
 */
public class EventQueueTest {

	/**
	 * Tries to test if two simultaneous events can interfere. Repeats the test
	 * many times, to increase changes to show the bug
	 * 
	 * @throws Throwable
	 */
	@Test
	public final void testMultiThreading() throws Throwable {
		for (int n = 0; n < 100; n++) {
			testMultiThreadingOnce();
			Thread.sleep(50);
		}
	}

	/**
	 * Test that two simultaneous events can not interfere with each other. In
	 * practice this appears to fail with about 50% chance if there is a
	 * multithreading bug.
	 * 
	 * <p>
	 * This tries to call {@link EventQueue#processEventsUpTo(long)} and at the
	 * same time {@link EventQueue#removeEvent(Event)}. What should never happen
	 * is that the event is executed while it already was removed from the
	 * queue.
	 * 
	 * <p>
	 * It would be nice if we could test with ArrivalEvent (
	 * {@link EventQueue#removeEvent(Event)} is called from
	 * {@link Car#setDestinationHeight} which is called from
	 * {@link GOALController}). however ArrivalEvent is private so we use some
	 * other event instead.
	 * 
	 * @throws Throwable
	 */
	public void testMultiThreadingOnce() throws Throwable {
		// the object under test.
		final EventQueue eQ = new EventQueue();

		final TestEvent event = new TestEvent(500);
		final List<Throwable> errors = new ArrayList<Throwable>();
		eQ.addEvent(event);

		Callable<Integer> removeTask = new Callable<Integer>() {
			@Override
			public Integer call() {
				eQ.removeEvent(event);
				event.disable();
				return 0;
			}
		};

		Callable<Integer> timerTask = new Callable<Integer>() {
			@Override
			public Integer call() {
				eQ.processEventsUpTo(1000);
				return 0;
			}
		};

		eQ.addListener(new Listener() {

			@Override
			public void eventRemoved(Event e) {
			}

			@Override
			public void eventError(Exception ex) {
				errors.add(ex);
			}

			@Override
			public void eventAdded(Event e) {
			}
		});

		List<Callable<Integer>> tasks = new ArrayList<Callable<Integer>>();
		tasks.add(timerTask);
		tasks.add(removeTask);
		ExecutorService executorService = Executors.newFixedThreadPool(2);
		executorService.invokeAll(tasks);
		if (!errors.isEmpty()) {
			throw (errors.get(0));
		}

	}

}
