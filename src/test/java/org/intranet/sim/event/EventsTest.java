package org.intranet.sim.event;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.intranet.elevator.model.Car;
import org.junit.Test;

import elevatorenv.GOALController;

/*
 * Tests for the Event mechanism. 
 * @author W.Pasman 10nov15
 */
public class EventsTest {

	/**
	 * Test that two simultaneous events can not interfere with each other.
	 * 
	 * 
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
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	@Test
	public final void testMultiThreading() throws InterruptedException,
			ExecutionException {
		final EventQueue eQ = new EventQueue();
		final TestEvent event = new TestEvent(500);
		eQ.addEvent(event);

		Callable<Integer> timerTask = new Callable<Integer>() {
			@Override
			public Integer call() {
				eQ.processEventsUpTo(1000);
				return 0;
			}
		};

		Callable<Integer> removeTask = new Callable<Integer>() {
			@Override
			public Integer call() {
				eQ.removeEvent(event);
				event.disable();
				return 0;
			}
		};

		List<Callable<Integer>> tasks = new ArrayList<Callable<Integer>>();
		tasks.add(timerTask);
		tasks.add(removeTask);
		ExecutorService executorService = Executors.newFixedThreadPool(2);
		List<Future<Integer>> futures = executorService.invokeAll(tasks);
		List<Integer> resultList = new ArrayList<Integer>(futures.size());
		for (Future<Integer> future : futures) {
			// Throws an exception if an exception was thrown by the task.
			resultList.add(future.get());
		}

	}

}

class TestEvent extends Event {

	private boolean disabled = false;
	private EventQueue queue;

	public TestEvent(long newTime) {
		super(newTime);
	}

	/**
	 * Called after the task was removed from the stack by the removeTask.
	 */
	public void disable() {
		disabled = true;
	}

	@Override
	public void perform() {
		if (disabled) {
			throw new IllegalStateException(
					"executing event that is not on the queue!");
		}
	}

}