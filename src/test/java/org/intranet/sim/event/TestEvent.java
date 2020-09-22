package org.intranet.sim.event;

/**
 * Test event. This event can be disabled, which we do after removing it
 * succesfully from the stack.
 */
public class TestEvent extends Event {
	private boolean disabled = false;

	public TestEvent(final long newTime) {
		super(newTime);
	}

	/**
	 * Called after the task was removed from the stack by the removeTask.
	 */
	public void disable() {
		this.disabled = true;
	}

	@Override
	public void perform() {
		if (this.disabled) {
			throw new IllegalStateException("executing event that is not on the queue!");
		}
	}
}