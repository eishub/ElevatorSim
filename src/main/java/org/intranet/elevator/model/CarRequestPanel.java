/*
* Copyright 2003 Neil McKellar and Chris Dailey
* All rights reserved.
*/
package org.intranet.elevator.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
* @author Neil McKellar and Chris Dailey
*/
public final class CarRequestPanel
{
  private boolean up;
  private boolean down;
  private List buttonListeners = new ArrayList();
  private List arrivalListeners = new ArrayList();
  
  CarRequestPanel()
  {
    super();
  }
  
  public boolean isUp()
  {
    return up;
  }
  
  public boolean isDown()
  {
    return down;
  }
  
  public void pressUp()
  {
    if (up)
      return;
    up = true;
    List buttonListeners = new ArrayList(this.buttonListeners);
    for (Iterator i = buttonListeners.iterator(); i.hasNext(); )
    {
      ButtonListener l = (ButtonListener)i.next();
      l.pressedUp();
    }
  }
  
  public void pressDown()
  {
    if (down)
      return;
    down = true;
    List buttonListeners = new ArrayList(this.buttonListeners);
    for (Iterator i = buttonListeners.iterator(); i.hasNext(); )
    {
      ButtonListener l = (ButtonListener)i.next();
      l.pressedDown();
    }
  }
  
  void arrivedUp(CarEntrance entrance)
  {
    up = false;
    List arrivalListeners = new ArrayList(this.arrivalListeners);
    for (Iterator i = arrivalListeners.iterator(); i.hasNext(); )
    {
      ArrivalListener l = (ArrivalListener)i.next();
      l.arrivedUp(entrance);
    }
  }
  
  void arrivedDown(CarEntrance entrance)
  {
    down = false;
    List arrivalListeners = new ArrayList(this.arrivalListeners);
    for (Iterator i = arrivalListeners.iterator(); i.hasNext(); )
    {
      ArrivalListener l = (ArrivalListener)i.next();
      l.arrivedDown(entrance);
    }
  }
  
  public void addButtonListener(ButtonListener listener)
  {
    buttonListeners.add(listener);
  }
  
  public void removeButtonListener(ButtonListener listener)
  {
    buttonListeners.remove(listener);
  }
  
  public void addArrivalListener(ArrivalListener listener)
  {
    arrivalListeners.add(listener);
  }
  
  public void removeArrivalListener(ArrivalListener listener)
  {
    arrivalListeners.remove(listener);
  }
  
  public static interface ButtonListener
  {
    void pressedUp();
    
    void pressedDown();
  }
  
  public static interface ArrivalListener
  {
    void arrivedUp(CarEntrance entrance);
    
    void arrivedDown(CarEntrance entrance);
  }
}