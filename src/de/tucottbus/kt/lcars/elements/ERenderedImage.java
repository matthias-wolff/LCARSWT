package de.tucottbus.kt.lcars.elements;

import java.awt.Rectangle;
import java.util.ArrayList;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;

import de.tucottbus.kt.lcars.LCARS;
import de.tucottbus.kt.lcars.Panel;
import de.tucottbus.kt.lcars.geometry.AGeometry;
import de.tucottbus.kt.lcars.geometry.GRenderedImage;

/**
 * An SWT image rendered at the {@linkplain Screen LCARS screen} side.
 * 
 * @author Matthias Wolff
 */
public abstract class ERenderedImage extends EElement
{

  /**
   * Creates a new rendered image element.
   * 
   * @param panel
   *          The LCARS panel to place the GUI element on.
   * @param x
   *          The x-coordinate of the upper left corner (in LCARS panel pixels).
   * @param y
   *          The y-coordinate of the upper left corner (in LCARS panel pixels).
   * @param w
   *          The width (in LCARS panel pixels).
   * @param h
   *          The height (in LCARS panel pixels).
   * @param style
   *          The style (see class {@link LCARS}).
   */
  public ERenderedImage(Panel panel, int x, int y, int w, int h, int style)
  {
    super(panel,x,y,w,h,style,null);
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
  protected ArrayList<AGeometry> createGeometriesInt()
  {
    ArrayList<AGeometry> geos = new ArrayList<AGeometry>();
    final ERenderedImage _this = this;
    Rectangle b = getBounds();
    GRenderedImage geo = new GRenderedImage(b.x,b.y,b.width,b.height)
    {
      private static final long serialVersionUID = 1L;

      @Override
      protected Image renderImage(GC gc, int w, int h)
      {
        return _this.renderImage(gc,w,h);
      }
    };
    geos.add(geo);    
    return geos;
  }

}

// EOF
