package de.tucottbus.kt.lcars;

import java.awt.Point;
import java.io.Serializable;


/**
 * A touch event to be transferred from the {@linkplain IScreen LCARS screen} to
 * the {@linkplain IPanel LCARS panel}.
 * 
 * @author Matthias Wolff
 */
public class TouchEvent implements Serializable
{
  private static final long serialVersionUID = 1L;

  public static final int DOWN = 1;
  public static final int UP   = 2;
  public static final int DRAG = 3;

  public static final String[] TYPE_NAMES =
  { "", "down", "up", "drag" };

  /**
   * The event type: {@link #DOWN}, {@link #UP}, or {@link #DRAG}.
   */
  public final int type;

  /**
   * The x-coordinate of the touch event (in panel coordinates).
   */
  public final int x;

  /**
   * The y-coordinate of the touch event (in panel coordinates).
   */
  public final int y;

  /**
   * Indicates if this event was introduced by a mouse event
   */
  public final boolean isMouseEvent;
  
  /**
   * Indicates if this event is a primary event of the current chain of touch events
   */
  public final boolean primary;
  
  

  public TouchEvent(int type, int x, int y, boolean isMouseEvent, boolean primary)
  {
    this.type = type;
    this.x = x;
    this.y = y;
    this.isMouseEvent = isMouseEvent;
    this.primary = primary;
  }

  public TouchEvent(int type, Point position, boolean isMouseEvent, boolean primary)
  {
    this(type, position.x, position.y, isMouseEvent, primary);
  }

  @Override
  public String toString()
  {
    return TouchEvent.class.getSimpleName() + "@(" + x + "," + y + ")"
        + " type=" + TYPE_NAMES[type] + " isMouseEvent="
        + Boolean.toString(isMouseEvent);
  }
}

// EOF
