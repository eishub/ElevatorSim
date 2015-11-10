package org.intranet.sim.event;

/**
 * Test event. This event can be disabled, which we do after removing it
 * succesfully from the stack.
 *
 */
public class TestEvent extends Event {

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