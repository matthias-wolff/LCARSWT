package de.tucottbus.kt.lcars.j2d;

import java.awt.Shape;
import java.awt.geom.Area;
import java.io.Serializable;

import org.eclipse.swt.graphics.GC;

import de.tucottbus.kt.lcars.swt.AwtSwt;

/**
 * A geometry representing a {@link Shape}.
 * 
 * @author Matthias Wolff
 */
public class GArea extends Geometry implements Serializable
{
  private static final long serialVersionUID = 1L;
  protected Area            area;
  protected boolean         outline;
  
  public GArea(Area area, boolean foreground)
  {
    super(foreground);
    this.area = new Area(area);
  }
  
  @Override
  public void getArea(Area area)
  {
    area.add(this.area);
  }
  
  public Area getArea()
  {
    return new Area(this.area);
  }
  
  public boolean isOutline()
  {
    return this.outline;
  }
  
  public void setOutline(boolean outline)
  {
    this.outline = outline;
  }
  
  public void setShape(Shape shape) {
    area = new Area(shape);
  }
    
  /*
   * (non-Javadoc)
   * @see de.tucottbus.kt.lcars.j2d.EGeometry2D#paint2D(java.awt.Graphics2D)
   */
  @Override
  public void paint2D(GC gc)
  {
    if (outline)
      gc.drawPath(AwtSwt.toSwtPath(area, gc.getDevice()));
    else
      gc.fillPath(AwtSwt.toSwtPath(area, gc.getDevice()));
  }  
}

// EOF
