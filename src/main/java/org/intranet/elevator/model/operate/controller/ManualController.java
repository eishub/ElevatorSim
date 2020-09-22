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
	private final List<Car> cars = new ArrayList<>();
	private boolean up = true;

	public ManualController() {
		super();
	}

	@Override
	public void initialize(final EventQueue eQ) {
		this.cars.clear();
		this.up = true;
	}

	@Override
	public void requestCar(final Floor newFloor, final Direction d) {
		System.out.println("car requested for floor " + newFloor + " direction " + d);
	}

	@Override
	public void addCar(final Car car, final float stoppingDistance) {
		this.cars.add(car);
		new ManualControlPanel(car);
	}

	/** Wouter: this returns the next direction of the elevator: up or down */
	@Override
	public boolean arrive(final Car car) {
		// AskNextFloor(car);
		final List<Floor> floors = car.getFloorRequestPanel().getServicedFloors();
		final int idx = getCurrentIndex(car);
		// At the top floor, go down; at the bottom floor go up
		this.up = (idx == floors.size() - 1) ? false : idx == 0 ? true : this.up;
		return this.up;
	}

	private int getCurrentIndex(final Car car) {
		Floor currentFloor = car.getLocation();
		if (currentFloor == null) {
			currentFloor = car.getFloorAt();
		}
		final List<Floor> floors = car.getFloorRequestPanel().getServicedFloors();
		return floors.indexOf(currentFloor);
	}

	@Override
	public void setNextDestination(final Car car) {

	}

	@Override
	public String toString() {
		return "Manual Controller";
	}

	public void AskNextFloor(final Car car) {
		System.out.println("asking for floor for car " + car);
		SwingUtilities.invokeLater(() -> {
			final String line = JOptionPane.showInputDialog("Give next floor for car " + car, "Go To Floor");
			if (line == null) {
				return;
			}
			final int floorNumber = Integer.parseInt(line) - 1;
			final List<Floor> floors = car.getFloorRequestPanel().getServicedFloors();
			final Floor nextFloor = floors.get(floorNumber);
			car.setDestination(nextFloor);
			System.out.println("floor setting finished. ");
		});
	}
}

class ManualControlPanel implements ActionListener {
	private JSpinner spinner;
	private SpinnerNumberModel model;
	private final Car car;

	public ManualControlPanel(final Car thecar) {
		this.car = thecar;
		createManualController(this.car);
	}

	public void createManualController(final Car car) {
		final JFrame frame = new JFrame(car.getName() + " controller");
		frame.setLayout(new BorderLayout());

		// problem is that following does not work, there are no floors yet.
		// List floors = car.getFloorRequestPanel().getServicedFloors();
		// therefore 9 is just a guess on how many floors there are...
		this.model = new SpinnerNumberModel(1, 0, 9, 1);
		this.spinner = new JSpinner(this.model);
		frame.add(this.spinner, BorderLayout.WEST);
		final JButton gotoButton = new JButton("go!");
		frame.add(gotoButton, BorderLayout.CENTER);

		gotoButton.addActionListener(this);
		frame.pack();
		frame.setVisible(true);
	}

	@Override
	public void actionPerformed(final ActionEvent e) {
		System.out.println("car " + this.car.getName() + "go to " + this.spinner.getValue());
		final int floorNumber = this.model.getNumber().intValue();
		final List<Floor> floors = this.car.getFloorRequestPanel().getServicedFloors();
		final Floor nextFloor = floors.get(floorNumber);
		this.car.setDestination(nextFloor);
	}
}
