package de.tucottbus.kt.lcars.elements;

import java.awt.Font;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.util.Vector;

import de.tucottbus.kt.lcars.LCARS;
import de.tucottbus.kt.lcars.Panel;
import de.tucottbus.kt.lcars.j2d.GArea;
import de.tucottbus.kt.lcars.j2d.Geometry;

/**
 * The LCARS elbo element.
 * 
 * @author Matthias Wolff
 */
public class EElbo extends EElement
{
  // -- Fields --
  
  /**
   * Width of the horizontal arm.
   */
  protected int armH = -1;
  
  /**
   * Width of the vertical arm.
   */
  protected int armV = -1;
  
  /**
   * Width of the outer arc.
   */
  protected int arcO = -1;
  
  /**
   * Width of the inner arc.
   */
  protected int arcI = -1;  
  
  // -- Constructors --

  /**
   * Creates a new LCARS elbo element.
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
   * @param label
   *          The label.
   */
  public EElbo(Panel panel, int x, int y, int w, int h, int style, String label)
  {
    super(panel,x,y,w,h,style,label);
  }
  
  // -- Getters an setters --
  
  /**
   * Returns the width of the horizontal arm.
   */
  protected int getArmHWidth()
  {
    if (armH>0) return armH;
    return (int)(0.45*(double)getBounds().width);
  }
  
  /**
   * Returns the width of the vertical arm.
   */
  protected int getArmVWidth()
  {
    if (armV>0) return armV;
    return (int)(0.19*(double)getBounds().height);
  }
  
  /**
   * Sets the widths of the two arms of the elbo.
   * 
   * @param armH
   *          The width of the horizontal arm (in LCARS panel pixels, -1 for the default width).
   * @param armV
   *          The width of the vertical arm (in LCARS panel pixels, -1 for the default width).
   */
  public void setArmWidths(int armH, int armV)
  {
    this.armH = armH;
    this.armV = armV;
    invalidate(true);
  }

  /**
   * Returns the width of the outer arc.
   */
  protected int getArcOWidth()
  {
    if (arcO>=0) return arcO;
    return (int)(0.78*(double)Math.max(getArmHWidth(),getArmVWidth()));
  }

  /**
   * Returns the width of the inner arc.
   */
  protected int getArcIWidth()
  {
    if (arcI>=0) return arcI;
    return (int)(2.32*(double)Math.min(getArmHWidth(),getArmVWidth()));
  }

  /**
   * Sets the widths of the two arcs of the elbo.
   * 
   * @param arcO
   *          The width of the outer arc (in LCARS panel pixels, -1 for the default width).
   * @param arcI
   *          The width of the inner arc (in LCARS panel pixels, -1 for the default width).
   */
  public void setArcWidths(int arcO, int arcI)
  {
    this.arcO = arcO;
    this.arcI = arcI;
    invalidate(true);
  }

  // -- Overrides --
  
  @Override
  protected Point computeLabelInsets()
  {
    Point ins = super.computeLabelInsets();
    int   ss  = getStyle() & LCARS.ES_SHAPE & ~LCARS.ES_OUTLINE;
    int   ls  = getStyle() & LCARS.ES_LABEL;
    switch (ss)
    {
    case LCARS.ES_SHAPE_SW:
      if (ls==LCARS.ES_LABEL_E || ls==LCARS.ES_LABEL_NE)
        ins.x+=getBounds().width-getArmHWidth();
      break;
    case LCARS.ES_SHAPE_NW:
      if (ls==LCARS.ES_LABEL_E || ls==LCARS.ES_LABEL_SE)
        ins.x+=getBounds().width-getArmHWidth();
      break;
    case LCARS.ES_SHAPE_SE:
      if (ls==LCARS.ES_LABEL_W || ls==LCARS.ES_LABEL_NW)
        ins.x+=getBounds().width-getArmHWidth();
      break;
    case LCARS.ES_SHAPE_NE:
      if (ls==LCARS.ES_LABEL_W || ls==LCARS.ES_LABEL_SW)
        ins.x+=getBounds().width-getArmHWidth();
      break;
    }
    return ins;
  }

  @Override
  public Vector<Geometry> createGeometriesInt()
  {
    Vector<Geometry> geos = new Vector<Geometry>(); 
    
    // Create elbo geometry
    Rectangle bounds = getBounds();
    int ess  = getStyle() & LCARS.ES_SHAPE & ~LCARS.ES_OUTLINE;
    int x    = bounds.x;
    int y    = bounds.y;
    int w    = bounds.width;
    int h    = bounds.height;
    int armH = getArmHWidth();
    int armV = getArmVWidth();
    int arcO = getArcOWidth();
    int arcI = getArcIWidth();
    Area area = new Area(new RoundRectangle2D.Float(x,y-h,2*w,2*h,arcO,arcO));
    area.subtract(new Area(new RoundRectangle2D.Float(x+armH,y-(h-armV),2*(w-armH),2*(h-armV),arcI,arcI)));
    AffineTransform tx = new AffineTransform();
    switch (ess)
    {
    case LCARS.ES_SHAPE_NE: tx.translate(-w,h); break;
    case LCARS.ES_SHAPE_NW: tx.translate( 0,h); break;
    case LCARS.ES_SHAPE_SE: tx.translate(-w,0); break;
    case LCARS.ES_SHAPE_SW: tx.translate( 0,0); break;
    }
    area.transform(tx);
    area.intersect(new Area(new Rectangle2D.Float(x,y,w,h)));
    geos.add(new GArea(area,false));

    // Create label geometries
    Font  font   = LCARS.getFont(getStyle());
    Point insets = computeLabelInsets();
    geos.addAll(LCARS.createTextGeometry2D(font,label,bounds,getStyle(),insets,true));
    
    // This is it
    return geos;
  }

}

// EOF


