/*
* Copyright 2003 Neil McKellar and Chris Dailey
* All rights reserved.
*/
package org.intranet.elevator.model;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Neil McKellar and Chris Dailey
 */
public final class CarRequestPanel {
	private boolean up;
	private boolean down;
	private final List<ButtonListener> buttonListeners = new ArrayList<>();
	private final List<ArrivalListener> arrivalListeners = new ArrayList<>();

	CarRequestPanel() {
	}

	public boolean isUp() {
		return this.up;
	}

	public boolean isDown() {
		return this.down;
	}

	public void pressUp() {
		if (this.up) {
			return;
		}
		this.up = true;
		final List<ButtonListener> buttonListeners = new ArrayList<>(this.buttonListeners);
		for (final ButtonListener buttonListener : buttonListeners) {
			final ButtonListener l = buttonListener;
			l.pressedUp();
		}
	}

	public void pressDown() {
		if (this.down) {
			return;
		}
		this.down = true;
		final List<ButtonListener> buttonListeners = new ArrayList<>(this.buttonListeners);
		for (final ButtonListener buttonListener : buttonListeners) {
			final ButtonListener l = buttonListener;
			l.pressedDown();
		}
	}

	void arrivedUp(final CarEntrance entrance) {
		this.up = false;
		final List<ArrivalListener> arrivalListeners = new ArrayList<>(this.arrivalListeners);
		for (final ArrivalListener arrivalListener : arrivalListeners) {
			final ArrivalListener l = arrivalListener;
			l.arrivedUp(entrance);
		}
	}

	void arrivedDown(final CarEntrance entrance) {
		this.down = false;
		final List<ArrivalListener> arrivalListeners = new ArrayList<>(this.arrivalListeners);
		for (final ArrivalListener arrivalListener : arrivalListeners) {
			final ArrivalListener l = arrivalListener;
			l.arrivedDown(entrance);
		}
	}

	public void addButtonListener(final ButtonListener listener) {
		this.buttonListeners.add(listener);
	}

	public void removeButtonListener(final ButtonListener listener) {
		this.buttonListeners.remove(listener);
	}

	public void addArrivalListener(final ArrivalListener listener) {
		this.arrivalListeners.add(listener);
	}

	public void removeArrivalListener(final ArrivalListener listener) {
		this.arrivalListeners.remove(listener);
	}

	public interface ButtonListener {
		void pressedUp();

		void pressedDown();
	}

	public interface ArrivalListener {
		void arrivedUp(CarEntrance entrance);

		void arrivedDown(CarEntrance entrance);
	}
}