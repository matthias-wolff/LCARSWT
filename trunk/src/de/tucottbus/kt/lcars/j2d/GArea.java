package de.tucottbus.kt.lcars.j2d;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;

/**
 * A geometry representing a {@link Shape}.
 * 
 * @author Matthias Wolff
 */
public class GArea extends Geometry
{
  private static final long serialVersionUID = 1L;
  protected GeneralPath     shape;
  
  // the strategy to paint the area
  protected IPaintStrategy  painter;

  public GArea(Area area, boolean foreground)
  {
    super(foreground);
    this.shape = new GeneralPath(area);
    painter = new Filler();
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
    return this.painter instanceof Outliner;
  }
  
  public void setOutline(boolean outline)
  {
    this.painter = outline ? new Outliner() : new Filler();
  }
    
  /*
   * (non-Javadoc)
   * @see de.tucottbus.kt.lcars.j2d.EGeometry2D#paint2D(java.awt.Graphics2D)
   */
  @Override
  public void paint2D(Graphics2D g2d)
  {
    painter.paint(g2d, shape);
  }
  
  // -- Nested classes --
  
  /**
   * Interface to distribute a method to paint the shape
   * 
   * @author Christian Borck
   *
   */
  private interface IPaintStrategy
  {   
    /**
     * Paint a shape on a 2D-graphic.
     * 
     * @param graphic
     * @param shape
     */
    public void paint (Graphics2D g2d, Shape s);
  }

  /**
   * Distribute a method to fill the shape
   * 
   * @author Christian Borck
   *
   */
  private class Filler implements IPaintStrategy
  {
    @Override
    public void paint(Graphics2D g2d, Shape s)
    {
      g2d.fill(s);        
    }
    
  }
  
  /**
   * Distribute a method to outline the shape
   * 
   * @author Christian Borck
   *
   */
  private class Outliner implements IPaintStrategy
  {
    @Override
    public void paint(Graphics2D g2d, Shape s)
    {
      g2d.draw(s);        
    }    
  }  
}

// EOF
