/*
 * Copyright 2003-2005 Neil McKellar and Chris Dailey
 * All rights reserved.
 */
package org.intranet.elevator.model.operate.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.intranet.elevator.model.Car;
import org.intranet.elevator.model.CarEntrance;
import org.intranet.elevator.model.Floor;
import org.intranet.elevator.model.FloorRequestPanel;

/**
 * @author Neil McKellar and Chris Dailey
 * SOON : Still confusing, keep refactoring.
 */
public class CarController
{
  private final Car car;
  private final float stoppingDistance;
  private final CarAssignments assignments;
  
  public CarController(Car c, float stoppingDist)
  {
    super();
    car = c;
    stoppingDistance = stoppingDist;
    assignments = new CarAssignments(car.getName());

    car.getFloorRequestPanel().addListener(new FloorRequestPanel.Listener()
    {
      public void floorRequested(Floor destinationFloor)
      {
        float currentHeight = car.getHeight();
        if (destinationFloor.getHeight() > currentHeight)
        {
          addDestination(destinationFloor, Direction.UP);
        }
        else if (destinationFloor.getHeight() < currentHeight)
        {
          addDestination(destinationFloor, Direction.DOWN);
        }
        else
          throw new RuntimeException("Do we really want to go to the current floor?");
          // Maybe some day we can just turn off the button
//        getFloorRequest(floor).setRequested(false, currentTime);
      }
    });
  }

  Car getCar()
  {
    return car;
  }

  Floor getDestination()
  {
    Assignment current = assignments.getCurrentAssignment();
    if (current == null)
      return null;
    return current.getDestination();
  }

  float getCost(Floor floor, Direction destinationDirection)
  {
    // TODO: In morning simulation, cars get stuck on the top floor
    Assignment a = new Assignment(floor, destinationDirection);
    
    if (assignments.getCurrentAssignment() == null)
    {
      // don't care about direction
      float time = car.getTravelTime(floor);
      return time;
    }

    // Don't send another elevator to do the work if this elevator is already
    // doing it.
    if (assignments.contains(a))
        return 0.0F;

    // factors:
    // 1. how much will it slow down the elevator in processing existing
    //    tasks?
    // 2. how long will it take for elevator to arrive *******
    // 3. how does this affect the distribution of elevators in the system?
    //    (probably would eventually be in Building)
    float cost = 0.0F;

    // For now, only #2 above is implemented.
    float currentHeight = car.getHeight();
    for (Iterator allDestinations =
          assignments.iteratorIncluding(car.getFloorRequestPanel().getServicedFloors(),
              getNearestBase(), a);
           allDestinations.hasNext(); )
    {
      Assignment nextAssignment = (Assignment)allDestinations.next();
      Floor nextDestination = nextAssignment.getDestination();
      float nextHeight = nextDestination.getHeight();

      // stop condition when destination would be reached.
      // LATER: continue to evaluate total trip cost vs. partial trip cost
//      if (a.equals(nextAssignment))
//      {
//        cost += car.getTravelTime(nextDestination.getHeight() - currentHeight);
//        return cost;
//      }

      // accumulator for number of stops
      cost += floor.getCarEntranceForCar(car).getDoor().getMinimumCycleTime();

      // accumulator for total distance
      cost += car.getTravelTime(nextHeight - currentHeight);

      currentHeight = nextHeight;
    }

    // all destinations have been accumulated, and we did not add this stop.
    // So now the stop must be added specifically from the last stop.
    cost += car.getTravelTime(floor.getHeight() - currentHeight);
    return cost;
  }
  
  /**
   * The nearest base is the nearest floor we could reasonably stop at.
   */
  private Assignment getNearestBase()
  {
    Assignment current = assignments.getCurrentAssignment();
    Direction currAssignmentDirection = (current == null) ?
        Direction.NONE : current.getDirection();

    // The first case is the car is docked
    Floor carLocation = car.getLocation();
    if (carLocation != null)
    {
      final CarEntrance entrance = carLocation.getCarEntranceForCar(car);
      Direction dockedDirection = entrance.isUp() ? Direction.UP :
         entrance.isDown() ? Direction.DOWN :
         currAssignmentDirection;
      return new Assignment(carLocation, dockedDirection);
    }

    // The second case is the car is idle
    Floor f = car.getFloorAt();
    if (f != null)
      return new Assignment(f, currAssignmentDirection);

    // Finally, the third case is the car is travelling
    float currentHeight = car.getHeight();
    Direction carDirection =
      (current.getDestination().getHeight() < currentHeight)?
        Direction.DOWN : Direction.UP;

    List floors = car.getFloorRequestPanel().getServicedFloors();
    for (Iterator i = createFloorContexts(floors, carDirection); i.hasNext(); )
    {
      FloorContext context = (FloorContext)i.next();
      if (context.contains(currentHeight))
      {
        float distance = Math.abs(context.getNext().getHeight() - currentHeight);
        boolean canCarStop = distance >= stoppingDistance;
        if (canCarStop || context.getNext() == getDestination())
          return new Assignment(context.getNext(), carDirection);
        return new Assignment(context.getSuccessor(), carDirection);
      }
    }
    throw new IllegalStateException("The car is somehow not between two floors.");
  }

  /**
   * @param floors
   * @param carDirection
   */
  private Iterator createFloorContexts(List floors, final Direction carDirection)
  {
    List sortedFloors = new ArrayList(floors);
    Collections.sort(sortedFloors, new Comparator()
    {
      public int compare(Object arg0, Object arg1)
      {
        Floor floor0 = (Floor)arg0;
        Floor floor1 = (Floor)arg1;
        float difference = floor0.getHeight() - floor1.getHeight();
        if (!carDirection.isUp())
          difference = -difference;
        if (difference > 0)
          return 1;
        if (difference < 0)
          return -1;
        return 0;
      }
    });
    List floorContexts = new ArrayList();
    for (int floorNum = 0; floorNum < sortedFloors.size() - 1; floorNum++)
    {
      Floor previous = (Floor)sortedFloors.get(floorNum);
      Floor next = (Floor)sortedFloors.get(floorNum + 1);
      Floor successor;
      if (floorNum == sortedFloors.size() - 2)
        successor = next;
      else
        successor = (Floor)sortedFloors.get(floorNum + 2);
      FloorContext set = new FloorContext(previous, next, successor);
      floorContexts.add(set);
    }
    return floorContexts.iterator();
  }

  void addDestination(Floor d, Direction direction)
  {
    Assignment newAssignment = new Assignment(d, direction);
    Assignment baseAssignment = getNearestBase();

    List floorList = car.getFloorRequestPanel().getServicedFloors();
    assignments.addAssignment(floorList, baseAssignment, newAssignment);
//  LATER: Can we delete the commented out check for DOCKED in addDestination()?
//    if (car.getState() != Car.State.DOCKED)
      car.setDestination(assignments.getCurrentAssignment().getDestination());
  }

  public boolean arrive()
  {
    Floor location = car.getLocation();
    List serviceFloors = car.getFloorRequestPanel().getServicedFloors();
    Floor topFloor = (Floor)serviceFloors.get(serviceFloors.size()-1); 
    Floor bottomFloor = (Floor)serviceFloors.get(0);

    // remove from up/down list
    Assignment currentAssignment = assignments.getCurrentAssignment();
    assignments.removeAssignment(currentAssignment);
    // If the next assignment is on the same floor but going the other way
    // the doors would close and re-open.
    // To prevent this, we can remove that assignment and indicate that
    // we're at the "extreme" position, ready to go the other direction.
    Assignment newAssignment = assignments.getCurrentAssignment();
    if (newAssignment != null && newAssignment.getDestination() == location)
    {
      assignments.removeAssignment(newAssignment);
      if (currentAssignment.getDirection() == Direction.UP)
        topFloor = location;
      else
        bottomFloor = location;
    }
    boolean wasUp = currentAssignment.getDirection().isUp();
    boolean atExtreme = (wasUp && location == topFloor) ||
                        (!wasUp && location == bottomFloor);
    boolean isUp = atExtreme ? !wasUp : wasUp;
    return isUp;
  }

  public void setNextDestination()
  {
    Assignment current = assignments.getCurrentAssignment();
    if (current != null)
      car.setDestination(current.getDestination());
  }
}
