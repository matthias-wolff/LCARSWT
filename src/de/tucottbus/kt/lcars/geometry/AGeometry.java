package de.tucottbus.kt.lcars.geometry;

import java.awt.Composite;
import java.awt.Rectangle;
import java.awt.geom.Area;
import java.io.Serializable;

import org.eclipse.swt.graphics.GC;

import de.tucottbus.kt.lcars.elements.EElement;

/**
 * A 2D geometric shape. The graphical representation of an {@linkplain EElement LCARS GUI element}
 * is a set of geometries.
 * 
 * @author Matthias Wolff
 */
// TODO: rename to AGeometry
public abstract class AGeometry implements Serializable
{  
  private static final long serialVersionUID = -6704273422742875854L;
  protected boolean         foreground;
  
  /**
   * Creates a new geometry.
   * 
   * @param foreground
   *          <code>true</code> for foreground geometries, <code>false</code> for background geometries.
   */
  public AGeometry(boolean foreground)
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
   * 
   * @return
   */
  public abstract Rectangle getBounds();

  

  /**
   * Called to paint this geometry on a {@link GC} context. Implementations <b>must not</b>
   * change
   * <ul>
   *   <li>the foreground and background colors and</li>
   *   <li>the {@link Composite}.</li>
   * </ul>
   * <p>They may, however, change other attributes including the font.</p>
   * 
   * @param gc
   *          The graphics context.
   */
  public abstract void paint2D(GC gc);

  /**
   * Called when the visibility of the {@link GC} changed.
   * @param visibility
   */
  public void onVisibilityChanged(boolean visibility)
  {
    // ignored
  }     
}

// EOF
