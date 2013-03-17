package de.tucottbus.kt.lcars.contributors;

import java.awt.geom.Point2D;

/**
 * Interface for listeners to position changes of {@link EPositioner} contributors.
 * 
 * @author Matthias Wolff
 */
public interface IPositionListener
{
  /**
   * Called when the position is changing. The method is repeatedly called ehile the user drags the
   * position toggle.
   * 
   * @param position
   *          The current position
   */
  public void positionChanging(Point2D.Float position);

  /**
   * Called when the position has changed. The method is invoked if the user has released the
   * position toggle.
   * 
   * @param position
   *          The current position
   */
  public void positionChanged(Point2D.Float position);
}

// EOF

