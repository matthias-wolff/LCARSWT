package de.tucottbus.kt.lcars.j2d;

import java.awt.Shape;
import java.awt.geom.Area;
import java.io.Serializable;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Path;

import de.tucottbus.kt.lcars.j2d.rendering.AwtSwtUtil;

/**
 * A geometry representing a {@link Shape}.
 * 
 * @author Matthias Wolff
 */
public class GArea extends Geometry implements Serializable
{
  private static final long serialVersionUID = 1L;
  protected Shape           _shape;
  protected Path            _cachedPath;
  protected boolean         outline;

  public GArea(Shape shape, boolean foreground)
  {
    super(foreground);    
    _shape = new Area(shape);
  }
  
  @Override
  public Area getArea()
  {
    return new Area(_shape);
  }
  
  public void setShape(Shape shape)
  {
    _shape = new Area(shape);
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
  public void paint2D(GC gc)
  {
    if (_cachedPath == null)
      _cachedPath = AwtSwtUtil.toSwtPath(_shape, gc.getDevice());
    
    if (outline)
      gc.drawPath(_cachedPath);
    else
      gc.fillPath(_cachedPath);
  }  
}

// EOF
