package org.intranet.sim.event;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(value = Parameterized.class)
public class EventQueueSortOrderTest {

	private int[] times;

	public EventQueueSortOrderTest(int... t) {
		times = t;
	}

	@Test
	public void sortOrder() throws Throwable {

		// the object under test.
		final EventQueue eQ = new EventQueue();

		for (Integer t : times) {
			TestEvent event = new TestEvent(t);
			eQ.addEvent(event);
		}

		/**
		 * Check that we get back a sorted order
		 */
		List<Event> list = eQ.getEventList();
		System.out.println("" + list);
		long t = 0;
		for (Event e : list) {
			if (e.getTime() < t) {
				throw new IllegalStateException("list is not sorted!" + list);
			}
			t = e.getTime();
		}

	}

	// @Parameters
	// public static List<Integer[]> data1() {
	// return Arrays.asList(new Integer[][] { { 1, 1, 2 }, { 2, 2, 4 },
	// { 8, 2, 10 }, { 4, 5, 9 } });
	// }

	@Parameters
	public static Collection<Object[]> data() {
		return Arrays.asList(new Object[][] {

		{ new int[] { 1, 1, 2 } },

		{ new int[] { 2, 2, 4 } },

		{ new int[] { 3, 2, 4 } },

		{ new int[] { 4, 3, 3, 3, 2, 1 } } });
	}
}
