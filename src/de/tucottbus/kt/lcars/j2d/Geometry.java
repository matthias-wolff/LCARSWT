package de.tucottbus.kt.lcars.j2d;

import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.geom.Area;
import java.io.Serializable;
import java.util.concurrent.atomic.AtomicLong;

import de.tucottbus.kt.lcars.elements.EElement;
import de.tucottbus.kt.lcars.j2d.rendering.AdvGraphics2D;

/**
 * A 2D geometric shape. The graphical representation of an {@linkplain EElement LCARS GUI element}
 * is a set of geometries.
 * 
 * @author Matthias Wolff
 */
// TODO: rename to AGeometry
public abstract class Geometry implements Serializable
{
  private static final long serialVersionUID = -6704273422742875854L;
  protected boolean         foreground;
  
  /**
   * Creates a new geometry.
   * 
   * @param foreground
   *          <code>true</code> for foreground geometries, <code>false</code> for background geometries.
   */
  public Geometry(boolean foreground)
  {
    this.foreground = foreground;
  }

  /**
   * Determines if this a foreground geometry.
   * 
   * @return <code>true</code> for foreground geometries, <code>false</code> for background
   *         geometries.
   */
  public boolean isForeground()
  {
    return foreground;
  }
  
  /**
   * Returns the {@link Area area} covered by this geometry.
   */
  public abstract Area getArea();

  /**
   * Called to paint this geometry on a {@link Graphics2D} context. Implementations <b>must not</b>
   * change
   * <ul>
   *   <li>the foreground and background colors and</li>
   *   <li>the {@link Composite}.</li>
   * </ul>
   * <p>They may, however, change other attributes including the font.</p>
   * 
   * @param g2d
   *          The graphics context.
   */
  public abstract void paint2D(AdvGraphics2D g2d); 

}

// EOF
