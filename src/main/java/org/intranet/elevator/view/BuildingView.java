/*
 * Copyright 2003 Neil McKellar and Chris Dailey
 * All rights reserved.
 */
package org.intranet.elevator.view;

import java.awt.Color;
import java.awt.Graphics;
import java.util.Iterator;

import javax.swing.JComponent;

import org.intranet.elevator.model.Car;
import org.intranet.elevator.model.CarEntrance;
import org.intranet.elevator.model.Floor;
import org.intranet.elevator.model.operate.Building;
import org.intranet.elevator.model.operate.Person;

/**
 * @author Neil McKellar and Chris Dailey
 */
public class BuildingView extends JComponent {
	private static final long serialVersionUID = 1L;
	private final Building building;

	public BuildingView(final Building building) {
		super();
		this.building = building;
	}

	@Override
	public void paintComponent(final Graphics g) {
		synchronized (this.building) {
			final float pixelConv = getHeight() / this.building.getHeight();

			final float minFloorHeight = calcMinFloorHeight();
			final int personHeight = (int) (5.5 * pixelConv);

			for (final Iterator<Floor> i = this.building.getFloors(); i.hasNext();) {
				// Draw lines for floor and ceiling and number floors
				final Floor floor = i.next();
				final int shaftWidth = (int) (minFloorHeight * pixelConv);
				drawFloor(g, floor, pixelConv, shaftWidth, personHeight);
			}

			// Draw elevator cars
			final int carDimension = (int) (minFloorHeight * pixelConv);
			drawCars(g, pixelConv, personHeight, carDimension, carDimension);
		}
	}

	private float calcMinFloorHeight() {
		float minFloorCeiling = this.building.getHeight();
		for (final Iterator<Floor> i = this.building.getFloors(); i.hasNext();) {
			final Floor floor = i.next();
			if (floor.getCeiling() < minFloorCeiling) {
				minFloorCeiling = floor.getCeiling();
			}
		}
		return minFloorCeiling;
	}

	private void drawFloor(final Graphics g, final Floor floor, final float pixelConv, final int shaftWidthPixels,
			final int personHeight) {
		g.setColor(Color.gray);
		final int floorY = getHeight() - (int) (floor.getHeight() * pixelConv);
		g.drawLine(0, floorY, getWidth(), floorY);
		final int ceilingY = getHeight() - (int) (floor.getAbsoluteCeiling() * pixelConv);
		g.drawLine(0, ceilingY, getWidth(), ceilingY);
		g.drawString(Integer.toString(floor.getFloorNumber()), 5, floorY - 4);

		// Draw entrances
		final int entranceNum = drawEntrances(g, floor, ceilingY, shaftWidthPixels, floorY - ceilingY);

		// Draw request buttons
		drawRequestIndicators(g, floor, floorY, ceilingY, entranceNum);

		final int entrancesXLocation = getWidth() - shaftWidthPixels * this.building.getNumCars();
		drawFloorPersons(g, floor, personHeight, floorY, entrancesXLocation);
		drawCarMovingPersons(g, floor, personHeight, floorY, entrancesXLocation);
	}

	private void drawRequestIndicators(final Graphics g, final Floor floor, final int floorY, final int ceilingY,
			final int entranceNum) {
		final int floorHeight = floorY - ceilingY;
		final int widthOfEntrances = floorHeight * entranceNum; // proportional
		final int wallCenter = floorHeight / 2;
		final int buttonHeight = floorHeight / 10;
		final int buttonWidth = floorHeight / 10;

		if (floor.getCallPanel().isUp()) {
			g.setColor(Color.yellow);
		} else {
			g.setColor(Color.gray);
		}
		g.fillRect(getWidth() - widthOfEntrances - buttonWidth, ceilingY + wallCenter, buttonWidth, buttonHeight);

		if (floor.getCallPanel().isDown()) {
			g.setColor(Color.yellow);
		} else {
			g.setColor(Color.gray);
		}
		g.fillRect(getWidth() - widthOfEntrances - buttonWidth, ceilingY + wallCenter + buttonHeight + 1, buttonWidth,
				buttonHeight);
	}

	private void drawFloorPersons(final Graphics g, final Floor floor, final int personHeight, final int floorY,
			final int entrancesXLocation) {
		int personNumber = 0;
		final PersonView personView = new PersonView();
		for (final Iterator<Person> j = floor.getPeople(); j.hasNext();) {
			final Person person = j.next();
			final boolean isMoving = person.getPercentMoved() != -1;
			final int floorXPosition = 20 + personNumber * personHeight / 2;
			final int distanceToElevator = entrancesXLocation - floorXPosition;
			final int distanceMoved = (int) (distanceToElevator * person.getPercentMoved() / 100.0);
			final int personX = isMoving ? floorXPosition + distanceMoved : floorXPosition;
			final int personWidth = personHeight / 2;
			final int personY = floorY - personHeight;
			personView.initialize(person, personWidth, personHeight);
			final Graphics g2 = g.create(personX, personY, personWidth, personHeight);
			personView.paint(g2);
			personNumber++;
		}
	}

	private void drawCarMovingPersons(final Graphics g, final Floor floor, final int personHeight, final int floorY,
			final int elevatorLocation) {
		final PersonView personView = new PersonView();
		for (final Iterator<Car> cars = this.building.getCars(); cars.hasNext();) {
			final Car car = cars.next();
			if (car.getHeight() != floor.getHeight()) {
				continue;
			}
			for (final Iterator<Person> j = car.getPeople(); j.hasNext();) {
				final Person person = j.next();
				final boolean isMoving = person.getPercentMoved() != -1;
				if (!isMoving) {
					continue;
				}
				final int floorXPosition = 20;
				final int distanceToElevator = elevatorLocation - floorXPosition;
				final int distanceMoved = (int) (distanceToElevator * (100 - person.getPercentMoved()) / 100.0);
				final int personX = floorXPosition + distanceMoved;
				final int personWidth = personHeight / 2;
				final int personY = floorY - personHeight;
				personView.initialize(person, personWidth, personHeight);
				final Graphics g2 = g.create(personX, personY, personWidth, personHeight);
				personView.paint(g2);
			}
		}
	}

	private int drawEntrances(final Graphics g, final Floor floor, final int ceY, final int ceW, final int ceH) {
		final EntranceView entranceView = new EntranceView();
		int entranceNum = 0;
		final int allEntrancesWidth = this.building.getNumCars() * ceW;
		for (final Iterator<CarEntrance> j = floor.getCarEntrances(); j.hasNext(); entranceNum++) {
			final CarEntrance carEntrance = j.next();
			entranceView.initialize(carEntrance, ceW, ceH);
			final int ceX = getWidth() - allEntrancesWidth + (entranceNum * ceW);
			final Graphics g2 = g.create(ceX, ceY, ceW, ceH);
			entranceView.paint(g2);
		}
		return entranceNum;
	}

	private void drawCars(final Graphics g, final float pixelConv, final int personHeight, final int carHeight,
			final int carWidth) {
		int carNumber = 0;
		final CarView carView = new CarView();
		final int allCarsWidth = this.building.getNumCars() * carWidth;
		for (final Iterator<Car> i = this.building.getCars(); i.hasNext(); carNumber++) {
			final Car car = i.next();
			final int floorY = getHeight() - (int) (car.getHeight() * pixelConv);

			final int x = getWidth() - allCarsWidth + (carNumber * carWidth);
			final int y = floorY - carHeight;

			carView.initialize(car, carWidth, carHeight, personHeight);
			final Graphics carG = g.create(x, y, carWidth, carHeight);
			carView.paint(carG);
		}
	}
}