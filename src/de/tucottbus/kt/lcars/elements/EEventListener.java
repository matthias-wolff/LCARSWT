package de.tucottbus.kt.lcars.elements;

/**
 * The listener interface for receiving touch events on an {@linkplain EElement LCARS GUI element}.
 * 
 * @author Matthias Wolff
 */
public interface EEventListener
{
  /**
   * Invoked when an {@linkplain EElement LCARS GUI element} has been touched.
   * 
   * @param ee
   *          The touch event.
   */
  public void touchDown(EEvent ee);
  
  /**
   * Invoked when an {@linkplain EElement LCARS GUI element} has been released.
   * 
   * @param ee
   *          The touch event.
   */
  public void touchUp(EEvent ee);
  
  /**
   * Invoked when an {@linkplain EElement LCARS GUI element} is being dragged.
   * 
   * @param ee
   *          The touch event.
   */
  public void touchDrag(EEvent ee);
  
  /**
   * Invoked when an {@linkplain EElement LCARS GUI element} is being held.
   * 
   * @param ee
   *          The touch event.
   */
  public void touchHold(EEvent ee);
}

// EOF
