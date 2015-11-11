package org.intranet.sim.event;

import java.util.ConcurrentModificationException;

import org.intranet.elevator.model.Car;
import org.intranet.elevator.model.Floor;
import org.intranet.sim.clock.RealTimeClock;

/**
 * Event to change the destination of a {@link Car}. Use this instead of
 * {@link Car#setDestination(org.intranet.elevator.model.Floor)} to avoid
 * {@link ConcurrentModificationException}s. These cab happen if another event ,
 * triggered by the {@link RealTimeClock}, fire at the same time as your call to
 * {@link Car#setDestination(org.intranet.elevator.model.Floor)}.
 *
 */
public class NewDestinationEvent extends Event {

	private Floor destination;
	private Car car;

	public NewDestinationEvent(Car car, Floor dest, long newTime) {
		super(newTime);
		this.destination = dest;
		this.car = car;
	}

	@Override
	public void perform() {
		car.setDestination(destination);
	}

	@Override
	public Event setTime(long newTime) {
		return new NewDestinationEvent(car, destination, newTime);
	}
}
