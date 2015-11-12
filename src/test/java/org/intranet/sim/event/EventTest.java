package org.intranet.sim.event;

import static org.junit.Assert.assertFalse;

import org.junit.Test;

public class EventTest {

	@Test
	public void equalsTest() {
		TestEvent e1 = new TestEvent(1000);
		TestEvent e2 = new TestEvent(1000);
		// they get different ids
		assertFalse(e1.equals(e2));
	}

}
