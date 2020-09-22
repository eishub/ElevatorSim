/*
 * Copyright 2003 Neil McKellar and Chris Dailey
 * All rights reserved.
 */
package org.intranet.elevator.view;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JComponent;

import org.intranet.elevator.model.CarEntrance;

/**
 * @author Neil McKellar and Chris Dailey
 */
public class EntranceView extends JComponent {
	private static final long serialVersionUID = 1L;
	private CarEntrance entrance;

	/**
	 *
	 * @param entrance the {@link CarEntrance} (the model)
	 * @param width    the entire width including buttons
	 * @param height   the entire height including buttons.
	 */
	void initialize(final CarEntrance entrance, final int width, final int height) {
		this.entrance = entrance;
		setSize(width, height);
		setOpaque(false);
	}

	EntranceView() {
		super();
	}

	@Override
	public void paintComponent(final Graphics g) {
		final int marginX = (int) (getWidth() * 0.10);
		final int marginY = (int) (getHeight() * 0.10);
		final int entranceWidth = getWidth() - (marginX * 2);
		final int entranceHeight = getHeight() - (marginY * 2);
		final int pctClosed = this.entrance.getDoor().getPercentClosed();
		final int doorWidth = entranceWidth * pctClosed / 200;

		// Draw door entrance border
		g.setColor(Color.white);
		g.drawRect(marginX, marginY, entranceWidth, entranceHeight);

		// Draw elevator doors
		g.setColor(Color.black);
		g.fillRect(marginX, marginY, doorWidth, entranceHeight);
		g.fillRect(marginX + entranceWidth - doorWidth, marginY, doorWidth, entranceHeight);

		// Up indicator next to elevator door
		if (this.entrance.isUp()) {
			g.setColor(Color.green);
		} else {
			g.setColor(Color.gray);
		}
		g.fillRect(0, 0, marginX, marginY);

		// Down indicator next to elevator door
		if (this.entrance.isDown()) {
			g.setColor(Color.green);
		} else {
			g.setColor(Color.gray);
		}
		g.fillRect(0, marginY, marginX, marginY);

		g.setColor(Color.black);
	}
}