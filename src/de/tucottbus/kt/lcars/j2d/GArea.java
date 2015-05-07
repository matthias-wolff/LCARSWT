package de.tucottbus.kt.lcars.j2d;

import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.io.Serializable;

import de.tucottbus.kt.lcars.ScreenGraphics2D;

/**
 * A geometry representing a {@link Shape}.
 * 
 * @author Matthias Wolff
 */
public class GArea extends Geometry implements Serializable
{
  private static final long serialVersionUID = 1L;
  protected GeneralPath     shape;
  protected boolean         outline;

  public GArea(Area area, boolean foreground)
  {
    super(foreground);
    this.shape = new GeneralPath(area);
  }
  
  @Override
  public Area getArea()
  {
    return new Area(shape);
  }
  
  public void setShape(Area area)
  {
    this.shape = new GeneralPath(area);;
  }
  
  public boolean isOutline()
  {
    return this.outline;
  }
  
  public void setOutline(boolean outline)
  {
    this.outline = outline;
  }
    
  /*
   * (non-Javadoc)
   * @see de.tucottbus.kt.lcars.j2d.EGeometry2D#paint2D(java.awt.Graphics2D)
   */
  @Override
  public void paint2D(ScreenGraphics2D g2d)
  {
    if (outline)
      g2d.draw(shape);
    else
      g2d.fill(shape);
  }  
}

// EOF
