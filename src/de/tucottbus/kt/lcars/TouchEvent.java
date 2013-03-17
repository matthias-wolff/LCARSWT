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
  
}

// EOF
