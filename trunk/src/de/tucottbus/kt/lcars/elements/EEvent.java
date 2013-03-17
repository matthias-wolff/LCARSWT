package de.tucottbus.kt.lcars.elements;

import java.awt.Point;
import java.awt.event.MouseEvent;

/**
 * An event in an LCARS {@link EElement}.
 * 
 * @author Matthias Wolff
 */
public class EEvent
{
  /**
   * Touch down event (possible value of field {@link #id}).
   */
  public static final int TOUCH_DOWN = 1;
  
  /**
   * Touch released event (possible value of field {@link #id}).
   */
  public static final int TOUCH_UP = 2;
  
  /**
   * Drag event (possible value of field {@link #id}).
   */
  public static final int TOUCH_DRAG = 3;
  
  /**
   * Hold down event (possible value of field {@link #id}).
   */
  public static final int TOUCH_HOLD = 4;
  
  /**
   * The element in which this event occurred.
   */
  public EElement el;
  
  /**
   * The event type
   */
  public int id = 0;
  
  /**
   * The position of the event (in panel coordinates relative to the upper left
   * corner of the element's bounding rectangle).
   */
  public Point pt;
  
  /**
   * The repetition counter (if {@link #id} is {@link #TOUCH_HOLD}).
   */
  public int ct;

  /**
   * Creates an LCARS element event from a mouse event.
   * 
   * @param me
   *          The mouse event.
   * @param el
   *          The LCARS element.
   * @param pt
   *          The position of the event (in panel coordinates).
   * @return The event.
   */
  public static EEvent fromMouseEvent(MouseEvent me, EElement el, Point pt)
  {
    EEvent ee = new EEvent();
    ee.el = el;
    ee.pt = el.panelToElement(pt);
    switch (me.getID())
    {
    case MouseEvent.MOUSE_PRESSED : ee.id = TOUCH_DOWN; break;
    case MouseEvent.MOUSE_RELEASED: ee.id = TOUCH_UP;   break;
    }
    return ee;
  }

  /**
   * Creates a copy of an LCARS element.
   * 
   * @param ee
   *          The event.
   * @return The copy.
   */
  public static EEvent fromEEvent(EEvent ee)
  {
    EEvent ee2 = new EEvent();
    ee2.ct = ee.ct;
    ee2.el = ee.el;
    ee2.id = ee.id;
    ee2.pt = ee.pt;
    return ee2;
  }
}

// EOF

