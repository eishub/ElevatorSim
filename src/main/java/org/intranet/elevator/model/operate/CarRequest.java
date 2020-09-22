/*
 * Copyright 2004-2005 Neil McKellar and Chris Dailey
 * All rights reserved.
 */
package org.intranet.elevator.model.operate;

import org.intranet.elevator.model.CarRequestPanel;
import org.intranet.elevator.model.Floor;
import org.intranet.elevator.model.operate.controller.Controller;
import org.intranet.elevator.model.operate.controller.Direction;

/**
 * Assigns a request from the CarRequestPanel for this Floor to the best
 * CarController.
 *
 * @author Neil McKellar and Chris Dailey
 */
public final class CarRequest implements CarRequestPanel.ButtonListener {
	private final Controller megaController;
	private final Floor floor;

	public CarRequest(final Controller m, final Floor floor) {
		super();
		this.megaController = m;
		this.floor = floor;
	}

	@Override
	public void pressedUp() {
		this.megaController.requestCar(this.floor, Direction.UP);
	}

	@Override
	public void pressedDown() {
		this.megaController.requestCar(this.floor, Direction.DOWN);
	}
}