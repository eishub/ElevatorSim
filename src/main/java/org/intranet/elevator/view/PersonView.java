/*
 * Copyright 2003 Neil McKellar and Chris Dailey
 * All rights reserved.
 */
package org.intranet.elevator.view;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JComponent;

import org.intranet.elevator.model.Floor;
import org.intranet.elevator.model.operate.Person;

/**
 * @author Neil McKellar and Chris Dailey
 *
 */
class PersonView extends JComponent {
	private static final long serialVersionUID = 1L;
	private Person person;

	public PersonView() {
		super();
	}

	void initialize(final Person person, final int width, final int height) {
		this.person = person;
		setSize(width, height);
		setOpaque(false);
	}

	@Override
	public void paintComponent(final Graphics g) {
		final int centerX = getWidth() / 2;
		final int hipsHeight = getHeight() / 3;
		final int hipsY = getHeight() - hipsHeight;
		final int neckY = hipsY - getHeight() / 3;
		final int armsY = getHeight() - getHeight() / 2;

		g.setColor(Color.gray);
		// draw legs
		g.drawLine(0, getHeight(), centerX, hipsY);
		g.drawLine(centerX, hipsY, getWidth(), getHeight());
		// draw torso
		g.drawLine(centerX, hipsY, centerX, neckY);
		// draw arms
		g.drawLine(0, armsY, getWidth(), armsY);
		// draw head
		final int headX = getWidth() / 2 - getHeight() / 6;
		g.fillOval(headX, getHeight() - getHeight(), getHeight() / 3, getHeight() / 3);
		// draw destination
		final Floor destination = this.person.getDestination();
		if (destination != null) {
			final int floorNumber = destination.getFloorNumber();
			final int numberWidth = g.getFontMetrics().stringWidth(Integer.toString(floorNumber));
			final int numberHeight = g.getFontMetrics().getHeight();
			g.setColor(Color.cyan);
			g.drawString(Integer.toString(floorNumber), headX + getHeight() / 6 - numberWidth / 2,
					getHeight() / 6 + numberHeight / 2);
		}
	}
}