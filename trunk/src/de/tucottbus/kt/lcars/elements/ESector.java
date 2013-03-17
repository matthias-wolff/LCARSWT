package de.tucottbus.kt.lcars.elements;

import java.awt.Font;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.util.Vector;

import de.tucottbus.kt.lcars.LCARS;
import de.tucottbus.kt.lcars.Panel;
import de.tucottbus.kt.lcars.j2d.GArea;
import de.tucottbus.kt.lcars.j2d.Geometry;

public class ESector extends EElement
{

  private int   r0;
  private int   r1;
  private float a0;
  private float a1;
  private int   b;
  private Point labelPos;
  private int   limitW;
  private int   limitH;
  private int   limitArc;
  
  /**
   * Creates a new sector element.
   * 
   * @param panel
   *          the panel
   * @param x
   *          x-coordinate of the center (LCARS panel pixels)
   * @param y
   *          y-coordinate of the center (LCARS panel pixels)
   * @param r0
   *          radius of the outer circle (LCARS panel pixels)
   * @param r1
   *          radius of the inner circle (LCARS panel pixels)
   * @param a0
   *          start angle of the sector (degrees)
   * @param a1
   *          end angle of the sector (degrees)
   * @param b
   *          width of the radial gap between two adjacent sectors (LCARS panel
   *          pixels, typically 3)
   * @param style
   *          the style
   * @param label
   *          the label
   */
  public ESector
  (
    Panel  panel,
    int    x,
    int    y,
    int    r0,
    int    r1,
    float  a0,
    float  a1,
    int    b,
    int    style,
    String label
  )
  {
    super(panel,x-r0,y-r0,2*r0,2*r0,style,label);
    if (a1<a0    ) throw new IllegalArgumentException("Angle range must be non-negative");
    if (a1-a0>180) throw new IllegalArgumentException("Angle range must smaller than 180°");
    if (r0<=0    ) throw new IllegalArgumentException("Outer radius must be non-negative");
    if (r0<=r1   ) throw new IllegalArgumentException("Inner radius must smaller than outer radius");
    if (b>=2*r0  ) throw new IllegalArgumentException("Radial gap must be smaller than outer diameter");
    this.r0 = r0;
    this.r1 = r1;
    this.a0 = a0;
    this.a1 = a1;
    this.b  = b;
  }

  /**
   * Sets a clipping rectangle for this {@link ESector}. The center of the
   * rectangle is the center of the sector.
   * 
   * @param w   the width (in LCARS panel pixels)
   * @param h   the height (in LCARS panel pixels)
   * @param arc the width of the arc to use to round off the corners.
   */
  public void setLimit(int w, int h, int arc)
  {
    this.limitW   = w;
    this.limitH   = h;
    this.limitArc = arc;
  }
  
  /**
   * Sets the label position relative to this {@link ESector}'s center. The
   * coordinates specify the label's anchor point. The actual position depends
   * on the {@link LCARS}<code>.ES_LABEL_XXX</code> style flags.
   * 
   * @param x the x-coordinate (in LCARS panel pixels)
   * @param y the y-coordinate (in LCARS panel pixels)
   */
  public void setLabelPos(int x, int y)
  {
    this.labelPos = new Point(x,y);
  }
  
  /**
   * Returns the center point of this {@link ESector}.
   * 
   * @return the center point (in LCARS panel pixels)
   */
  public Point getCenter()
  {
    Rectangle bounds = getBounds();
    int cx = bounds.x+bounds.width/2;
    int cy = bounds.y+bounds.height/2;
    return new Point(cx,cy);
  }
  
  @Override
  protected Vector<Geometry> createGeometriesInt()
  {
    Vector<Geometry> geos = new Vector<Geometry>(); 

    // Create sector geometry
    int  cx   = getCenter().x;
    int  cy   = getCenter().y;
    Area area = limit(sector(cx,cy,r0,r1,a0,a1,b),cx,cy,limitW,limitH,limitArc);    
    geos.add(new GArea(area,false));

    // Create label geometries
    Font      font   = LCARS.getFont(getStyle());
    Rectangle bounds = area.getBounds();
    Point     insets = computeLabelInsets();
    if (labelPos!=null)
    {
      bounds = new Rectangle(cx+labelPos.x,cy+labelPos.y,0,0);
      insets = new Point(0,0);
    }
    geos.addAll(LCARS.createTextGeometry2D(font,label,bounds,getStyle(),insets,true));
    
    return geos;
  }

  private static int getQuadrant(float angle)
  {
    while (angle<0) angle+=360;
    return (int)((angle%360)/90);
  }

  public static Area sector
  (
    int   x,
    int   y,
    int   r0,
    int   r1,
    float a0,
    float a1,
    int   b
  )
  {
    float ar0 = (float)(a0*Math.PI/180);
    float ar1 = (float)(a1*Math.PI/180);
    int   q0  = getQuadrant(a0);
    int   q1  = getQuadrant(a1);
    if (q1<q0) q1+=4;
    Point[] qp  = 
    {
      new Point(x+r0+1,y-r0-1),
      new Point(x-r0-1,y-r0-1),
      new Point(x-r0-1,y+r0+1),
      new Point(x+r0+1,y+r0+1)
    };
    
    // Create the background shape
    // - the ring
    Area area = new Area(new Ellipse2D.Float(x-r0,y-r0,2*r0,2*r0));
    if (r1>0)
      area.subtract(new Area(new Ellipse2D.Float(x-r1,y-r1,2*r1,2*r1)));

    // - sector trimming
    Polygon strim = new Polygon();
    strim.addPoint(x,y);
    strim.addPoint((int)Math.round(x+Math.cos(ar0)*r0),(int)Math.round(y-Math.sin(ar0)*r0));
    for (int q=q0; q<=q1; q++)
      strim.addPoint(qp[q%4].x,qp[q%4].y);
    strim.addPoint((int)Math.round(x+Math.cos(ar1)*r0),(int)Math.round(y-Math.sin(ar1)*r0));
    area.intersect(new Area(strim));
    
    // -- gap trimming
    Area btrim = new Area(new Rectangle2D.Float(x-r0-1,y-(float)b/2f,2*r0+2,b));
    AffineTransform bTrafo = new AffineTransform();
    bTrafo.rotate(-ar0,x,y); 
    btrim.transform(bTrafo);
    area.subtract(btrim);
    btrim = new Area(new Rectangle2D.Float(x-r0-1,y-(float)b/2f,2*r0+2,b));
    bTrafo = new AffineTransform();
    bTrafo.rotate(-ar1,x,y); 
    btrim.transform(bTrafo);
    area.subtract(btrim);
    
    return area;
  }

  public static Area limit(Area area, int cx, int cy, int limitW, int limitH, int limitArc)
  {
    if (limitW==0 || limitH==0) return area;
    int  x     = cx-limitW;
    int  y     = cy-limitH;
    int  w     = 2*limitW;
    int  h     = 2*limitH;
    int  arc   = limitArc;
    Area are   = new Area(area); 
    Area ltrim = new Area(new RoundRectangle2D.Float(x,y,w,h,arc,arc));
    are.intersect(ltrim);
    return are;
  }
  
}
