package de.tucottbus.kt.lcars.j2d;

import java.awt.geom.Area;
import java.awt.geom.Point2D;

import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Path;
import org.eclipse.swt.graphics.Rectangle;

import de.tucottbus.kt.lcars.LCARS;


/**
 * A geometry representing a text.
 * 
 * @author Matthias Wolff, Christian Borck
 */
public class GText extends Geometry
{
  private static final long serialVersionUID = -1724627622898883028L;  
  protected int x;
  protected int y;
  protected Rectangle touchBounds;
  protected String text;
  protected float descent;
  protected FontData font;
  
  protected transient Path textPath;
  
  /**
   * Creates a new text geometry. A text geometry provides information and
   * 2D-rendering for a single line of text
   * 
   * @param text
   *          the text
   * @param pos
   *          the position to draw the text on
   * @param touchBounds
   *          the bounding rectangle for touch detection (can be
   *          <code>null</code> for static texts); note that upper left corner
   *          of the bounding rectangle is <em>not</em> identical with the
   *          drawing position {@link pos}
   * @param font
   *          the font to render the text with
   * @param foreground
   *          foreground/background flag
   */
  public GText(String text, Point2D.Float pos, Rectangle touchBounds, FontData font,
      boolean foreground)
  {
    super(foreground);
    this.text = text;
    x = (int)pos.x;
    y = (int)pos.y;
    this.touchBounds = touchBounds;
    this.font = font;
  }
  
  public Point2D.Float getPos()
  {
    return new Point2D.Float(x, y);
  }

  @Deprecated
  public int getStyle() {
    return 0;
  }
  
  @Override
  public void getArea(Area area)
  {
    area.add(new Area(new java.awt.Rectangle(touchBounds.x, touchBounds.y, touchBounds.width, touchBounds.height)));
  }
  
  public String getText()
  {
    return this.text;
  }

  public float getDescent()
  {
    return this.descent;
  }

  public void setDescent(float ascent)
  {
    this.descent = ascent;
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.tucottbus.kt.lcars.j2d.EGeometry2D#paint2D(java.awt.Graphics2D)
   */
  @Override
  public void paint2D(GC gc)
  {
    if(textPath == null) {
      Font font = new Font(gc.getDevice(), this.font);
      textPath = LCARS.getTextShape(new Font(gc.getDevice(), this.font), text, x, y);
      font.dispose();
    }    
    gc.fillPath(textPath);
    
    
  }  
}

// EOF
