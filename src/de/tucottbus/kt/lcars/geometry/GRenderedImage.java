package de.tucottbus.kt.lcars.geometry;

import java.awt.Rectangle;
import java.awt.geom.Area;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;

/**
 * Geometry of an SWT image rendered at the {@linkplain Screen LCARS screen}
 * side.
 * 
 * <h3>Remarks:</h3>
 * <ul>
 *   <li>FIXME: Not serializable!</li>
 *   <li>TODO: Create <code>ERenderedImage extends EElement</code></li>
 * </ul>
 * 
 * @author Matthias Wolff
 */
public abstract class GRenderedImage extends AGeometry
{
  private static final long serialVersionUID = 1L;

  private int   x;
  private int   y;
  private int   w;
  private int   h;
  
  private transient Image img;

  /**
   * Creates a new rendered image.
   * 
   * @param x
   *          The x-coordinate of the upper left corner (in LCARS panel pixels).
   * @param y
   *          The y-coordinate of the upper left corner (in LCARS panel pixels).
   * @param w
   *          The width (in LCARS panel pixels).
   * @param h
   *          The height (in LCARS panel pixels).
   */
  public GRenderedImage(int x, int y, int w, int h)
  {
    super(false);
    this.x = x;
    this.y = y;
    this.w = w;
    this.h = h;
  }
  
  /**
   * Called in order to render the SWT image for this geometry.
   * 
   * @param gc
   *          The SWT graphics context.
   * @param w
   *          The width in pixels.
   * @param h
   *          The height in pixels.
   * @return The image.
   */
  protected abstract Image renderImage(GC gc, int w, int h);

  @Override
  protected void finalize() throws Throwable
  {
    if (img!=null)
      img.dispose();
    super.finalize();
  }
  
  @Override
  public Area getArea()
  {
    return new Area(getBounds());
  }

  @Override
  public Rectangle getBounds()
  {
    return new Rectangle(x,y,w,h);
  }

  @Override
  public void paint2D(GC gc)
  {
    if (img==null)
      img = renderImage(gc,w,h);
    gc.drawImage(img,x,y);
  }

}

// EOF
