package de.tucottbus.kt.lcars;

import java.io.Serializable;

/**
 * A touch event to be transferred from the {@linkplain IScreen LCARS screen} to the
 * {@linkplain IPanel LCARS panel}.
 * 
 * @author Matthias Wolff
 */
public class TouchEvent implements Serializable
{
  private static final long serialVersionUID = 1L;
  
  public static final int DOWN = 1;
  public static final int UP   = 2;
  public static final int DRAG = 3;
  
  public static final String[] TYPE_NAMES = {"", "down", "up", "drag"};
  
  /**
   * The event type: {@link #DOWN}, {@link #UP}, or {@link #DRAG}.
   */
  public int type;
  
  /**
   * The x-coordinate of the touch event (in panel coordinates).
   */
  public int x;
  
  /**
   * The y-coordinate of the touch event (in panel coordinates).
   */
  public int y;
  
  @Override
  public String toString() {
    return TouchEvent.class.getSimpleName() +"@("+x+","+y+")"+ " type="+ TYPE_NAMES[type];        
  }
}

// EOF
