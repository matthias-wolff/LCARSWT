package de.tucottbus.kt.lcars.geometry;

import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Area;
import java.io.Serializable;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Path;

import de.tucottbus.kt.lcars.swt.AwtSwt;

/**
 * A geometry representing a {@link Shape}.
 * 
 * @author Matthias Wolff
 */
public class GArea extends AGeometry implements Serializable
{
  private static final long serialVersionUID = 254038909353270177L;

  protected Area            area;
  protected boolean         outline;  
  private transient Path    cachedPath;
  
  public GArea(Area area, boolean foreground)
  {
    super(foreground);
    this.area = new Area(area);
  }
  
  @Override
  public Area getArea()
  {
    return new Area(this.area);
  }
  
  public Rectangle getBounds() {
    return area.getBounds();
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
    if (cachedPath == null)
      cachedPath = AwtSwt.toSwtPath(area, gc.getDevice());
    
    if (outline) {
      Color bgc = gc.getBackground();
      Color fgc = gc.getForeground();
      gc.setForeground(bgc);
      gc.drawPath(cachedPath);
      gc.setForeground(fgc);
      bgc.dispose();
      fgc.dispose();
    }
    else
      gc.fillPath(cachedPath);
  }  

  @Override
  public String toString()
  {
    return getClass().getSimpleName() + " outline=" + outline;
  }
  
  @Override
  protected void finalize() throws Throwable
  {
    if (cachedPath != null)
      cachedPath.dispose();
    super.finalize();
  }
}

// EOF
