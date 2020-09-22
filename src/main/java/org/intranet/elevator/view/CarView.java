/*
* Copyright 2003 Neil McKellar and Chris Dailey
* All rights reserved.
*/
package org.intranet.elevator.view;

import java.awt.Color;
import java.awt.Graphics;
import java.util.Iterator;
import java.util.List;

import javax.swing.JComponent;

import org.intranet.elevator.model.Car;
import org.intranet.elevator.model.Floor;
import org.intranet.elevator.model.operate.Person;

/**
 * @author Neil McKellar and Chris Dailey
 */
public class CarView extends JComponent {
	private static final long serialVersionUID = 1L;
	private Car car;
	private int personHeight;

	CarView() {
		super();
	}

	void initialize(final Car car, final int width, final int height, final int personHeight) {
		this.car = car;
		this.personHeight = personHeight;
		setSize(width, height);
		setOpaque(false);
	}

	@Override
	public void paintComponent(final Graphics g) {
		// Draw door entrance border
		final int margin = (int) (getWidth() * 0.05);
		final int margin2 = 2 * margin;
		g.setColor(Color.yellow);
		g.drawRect(margin, margin, getWidth() - margin2, getHeight() - margin2);

		// Draw floor requests
		int requestNumber = 0;
		final List<Floor> requestedFloors = this.car.getFloorRequestPanel().getRequestedFloors();
		for (final Floor floor2 : requestedFloors) {
			final Floor floor = floor2;
			final String floorNumber = Integer.toString(floor.getFloorNumber());
			final int numberWidth = g.getFontMetrics().stringWidth(floorNumber);
			final int numberX = getWidth() - numberWidth - 2;
			final int numberY = getHeight() - requestNumber * (g.getFontMetrics().getHeight()) - 2;
			requestNumber++;
			g.setColor(Color.lightGray);
			g.fillRect(numberX, numberY - g.getFontMetrics().getHeight(), numberWidth, g.getFontMetrics().getHeight());
			g.setColor(Color.black);
			g.drawString(floorNumber, numberX, numberY);
		}

		int personNumber = 0;
		final PersonView personView = new PersonView();
		for (final Iterator<Person> j = this.car.getPeople(); j.hasNext();) {
			final Person person = j.next();
			if (person.getPercentMoved() != -1) {
				continue;
			}
			final int personX = 4 + personNumber * this.personHeight / 2;
			final int personWidth = this.personHeight / 2;
			final int personY = getHeight() - margin - this.personHeight;
			personView.initialize(person, personWidth, this.personHeight);
			final Graphics g2 = g.create(personX, personY, personWidth, this.personHeight);
			personView.paint(g2);
			personNumber++;
		}
	}
}