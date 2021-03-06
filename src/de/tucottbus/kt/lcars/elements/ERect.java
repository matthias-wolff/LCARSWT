package de.tucottbus.kt.lcars.elements;

import java.awt.Rectangle;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;

import de.tucottbus.kt.lcars.LCARS;
import de.tucottbus.kt.lcars.Panel;
import de.tucottbus.kt.lcars.geometry.GArea;
import de.tucottbus.kt.lcars.geometry.AGeometry;

public class ERect extends EElement
{
  int arc;
  
  /**
   * Constructs a new rectangular element (button or static).
   * 
   * <p><img src="ERect.png"></p>
   * 
   * @param panel the panel to display the element in (may be <code>null</code>)
   * @param x     the x-coordinate of the upper-left corner
   * @param y     the y-coordinate of the upper-left corner
   * @param w     the width
   * @param h     the height
   * @param style a combination of {@link LCARS}<code>.XX_XXX</code> constants
   * @param label the label text (may be <code>null</code>)
   */
  public ERect(Panel panel, int x, int y, int w, int h, int style, String label)
  {
    super(panel,x,y,w,h,style,label);
  }

  public void setArc(int arc)
  {
    this.arc = arc;
  }
  
  protected int computeArc()
  {
    return (getStyle()&LCARS.ES_RECT_RND)!=0
        ? (this.arc>0?this.arc:Math.min(getBounds().width,getBounds().height))
        : 0;
  }

  @Override
  public ArrayList<AGeometry> createGeometriesInt()
  {
    ArrayList<AGeometry> geos = new ArrayList<AGeometry>(); 
    
    // Create background geometry
    Rectangle bounds = getBounds();
    int       style  = getStyle();
    int       x      = bounds.x;
    int       y      = bounds.y;
    int       w      = bounds.width;
    int       h      = bounds.height;
    int       arc    = computeArc();
    Area      area   = new Area(new RoundRectangle2D.Float(x,y,w,h,arc,arc));
    if ((style&LCARS.ES_RECT_RND)!=0)
    {
      if ((style&LCARS.ES_RECT_RND_E)==0)
        area.add(new Area(new Rectangle2D.Float(x+w-w/2,y,w/2,h)));
      if ((style&LCARS.ES_RECT_RND_W)==0)
        area.add(new Area(new Rectangle2D.Float(x,y,w/2,h)));
    }
    geos.add(new GArea(area,false));

    // Create label geometries
    geos.addAll(LCARS.createTextGeometry2D(label,bounds,style,computeLabelInsets(),true));
    
    // This is it
    return geos;
  }

}
