/**
 * Copyright 2008 W.Pasman
 * @author W.Pasman 18dec08
 * All rights reserved.
 */
package org.intranet.elevator.model.operate.controller;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;

import org.intranet.elevator.model.Car;
import org.intranet.elevator.model.Floor;
import org.intranet.sim.event.EventQueue;

/**
 * Interface to GOAL
 * 
 * @author W.Pasman
 */
public class ManualController implements Controller {
	private List<Car> cars = new ArrayList<Car>();
	private boolean up = true;
	private boolean carsMoving = false;

	public ManualController() {
		super();
	}

	public void initialize(EventQueue eQ) {
		cars.clear();
		carsMoving = false;
		up = true;
	}

	public void requestCar(Floor newFloor, Direction d) {
		System.out.println("car requested for floor " + newFloor
				+ " direction " + d);
	}

	/*
	 * private void moveCars() { if (!carsMoving) for (Iterator carsI =
	 * cars.iterator(); carsI.hasNext();) { Car car = (Car)carsI.next();
	 * sendToNextFloor(car); } carsMoving = true; }
	 */

	public void addCar(final Car car, float stoppingDistance) {
		cars.add(car);
		new ManualControlPanel(car);
	}

	// TODO: Reduce code duplication between isUp(), getCurrentIndex(), and
	// sendToNextFloor()
	/** Wouter: this returns the next direction of the elevator: up or down */
	public boolean arrive(Car car) {
		// AskNextFloor(car);
		List floors = car.getFloorRequestPanel().getServicedFloors();
		int idx = getCurrentIndex(car);
		// At the top floor, go down; at the bottom floor go up
		up = (idx == floors.size() - 1) ? false : idx == 0 ? true : up;
		return up;
	}

	private int getCurrentIndex(Car car) {
		Floor currentFloor = car.getLocation();
		if (currentFloor == null)
			currentFloor = car.getFloorAt();
		List floors = car.getFloorRequestPanel().getServicedFloors();
		return floors.indexOf(currentFloor);
	}

	/*
	 * private void sendToNextFloor(Car car) { int idx = getCurrentIndex(car);
	 * // Next floor depends on the direction idx += arrive(car) ? 1 : -1; List
	 * floors = car.getFloorRequestPanel().getServicedFloors(); Floor nextFloor
	 * = (Floor)floors.get(idx); car.setDestination(nextFloor); }
	 */

	public String toString() {
		return "Manual Controller";
	}

	/*
	 * private void evaluateCarsMoving(final Car car) { carsMoving = false; for
	 * (Iterator floorI =
	 * car.getFloorRequestPanel().getServicedFloors().iterator();
	 * floorI.hasNext();) { Floor f = (Floor)floorI.next(); CarRequestPanel crp
	 * = f.getCallPanel(); if (crp.isUp() || crp.isDown()) { carsMoving = true;
	 * break; } } if (car.getFloorRequestPanel().getRequestedFloors().size() >
	 * 0) carsMoving = true; }
	 */

	public void setNextDestination(Car car) {
		// AskNextFloor(car);
	}

	public void AskNextFloor(final Car car) {
		System.out.println("asking for floor for car " + car);
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				String line = (String) JOptionPane.showInputDialog(
						"Give next floor for car " + car, "Go To Floor");
				if (line == null)
					return;
				int floorNumber = Integer.parseInt(line) - 1;
				List floors = car.getFloorRequestPanel().getServicedFloors();
				Floor nextFloor = (Floor) floors.get(floorNumber);
				car.setDestination(nextFloor);
				System.out.println("floor setting finished. ");
			}
		});

	}

}

class ManualControlPanel implements ActionListener {
	JSpinner spinner;
	SpinnerNumberModel model;

	Car car;

	public ManualControlPanel(Car thecar) {
		car = thecar;
		createManualController(car);
	}

	public void createManualController(Car car) {
		JFrame frame = new JFrame(car.getName() + " controller");
		frame.setLayout(new BorderLayout());

		// problem is that following does not work, there are no floors yet.
		// List floors = car.getFloorRequestPanel().getServicedFloors();
		// therefore 9 is just a guess on how many floors there are...
		model = new SpinnerNumberModel(1, 0, 9, 1);
		spinner = new JSpinner(model);
		frame.add(spinner, BorderLayout.WEST);
		JButton gotoButton = new JButton("go!");
		frame.add(gotoButton, BorderLayout.CENTER);

		gotoButton.addActionListener(this);
		frame.pack();
		frame.setVisible(true);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		System.out.println("car " + car.getName() + "go to "
				+ spinner.getValue());
		int floorNumber = model.getNumber().intValue();
		List floors = car.getFloorRequestPanel().getServicedFloors();
		Floor nextFloor = (Floor) floors.get(floorNumber);
		car.setDestination(nextFloor);
	}
}
