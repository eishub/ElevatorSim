/*
 * Copyright 2005 Neil McKellar and Chris Dailey
 * All rights reserved.
 */
package org.intranet.elevator.model.operate.controller;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.intranet.elevator.model.Car;
import org.intranet.elevator.model.Floor;
import org.intranet.sim.event.EventQueue;

/**
 * @author Neil McKellar and Chris Dailey
 */
public class MetaController
  implements Controller
{
  private List carControllers = new ArrayList();

  public void initialize(EventQueue eQ)
  {
    carControllers.clear();
  }

  public void addCar(Car car, float stoppingDistance)
  {
    CarController controller = new CarController(car, stoppingDistance);
    carControllers.add(controller);
  }

  public void requestCar(Floor newFloor, Direction d)
  {
    CarController controller = findBestCar(newFloor, d);
    controller.addDestination(newFloor, d);
  }

  private CarController findBestCar(Floor floor, Direction direction)
  {
    // if only one car, duh
    if (carControllers.size() == 1) return (CarController)carControllers.get(0);

    CarController c = null;
    float lowestCost = Float.MAX_VALUE;
    for (Iterator i = carControllers.iterator(); i.hasNext();)
    {
      CarController controller = (CarController)i.next();
      float cost = controller.getCost(floor, direction);
      if (cost < lowestCost)
      {
        c = controller;
        lowestCost = cost;
      }
      else if (cost == lowestCost)
      {
        // Previously, the simulation simply collected statistics.
        // With the addition of this comparison, the statistics being gathered
        // are affecting the outcome of the simulation.
        if (controller.getCar().getTotalDistance() < c.getCar().getTotalDistance())
          c = controller;
      }
    }

    return c;
  }

  public String toString()
  {
    return "Default MetaController";
  }

  public boolean arrive(Car car)
  {
    CarController c = getController(car);
    return c.arrive();
  }

  public void setNextDestination(Car car)
  {
    CarController c = getController(car);
    c.setNextDestination();
  }

  private CarController getController(Car car)
  {
    CarController c = null;
    for (Iterator i = carControllers.iterator(); i.hasNext();)
    {
      CarController controller = (CarController)i.next();
      if (controller.getCar() == car)
        c = controller;
    }
    return c;
  }
}
