package de.tucottbus.kt.lcars.j2d;

import java.awt.Font;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;

import de.tucottbus.kt.lcars.ScreenGraphics2D;

/**
 * A geometry representing a text.
 * 
 * @author Matthias Wolff
 */
public class GText extends Geometry
{
  private static final long serialVersionUID = 1L;
  protected Font font;
  protected Point2D.Float pos;
  protected GeneralPath shape;
  protected String text;
  protected float descent;
  protected Shape cachedTextShape;
  
  /**
   * Creates a new text geometry. A text geometry provides information and
   * 2D-rendering for a single line of text
   * 
   * @param text
   *          the text
   * @param pos
   *          the position to draw the text on
   * @param shape
   *          the bounding rectangle for touch detection (can be
   *          <code>null</code> for static texts); note that upper left corner
   *          of the bounding rectangle is <em>not</em> identical with the
   *          drawing position {@link pos}
   * @param font
   *          the font to render the text with
   * @param foreground
   *          foreground/background flag
   */
  public GText(String text, Point2D.Float pos, Rectangle shape, Font font,
      boolean foreground)
  {
    super(foreground);
    this.text = text;
    this.font = font;
    this.pos = pos;
    this.shape = new GeneralPath(shape);
  }

  public Font getFont()
  {
    return this.font;
  }

  public Point2D.Float getPos()
  {
    return this.pos;
  }

  @Override
  public Area getArea()
  {
    return new Area(shape);
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
  public void paint2D(ScreenGraphics2D g2d)
  {
//    if (textShape == null)
//      synchronized (this)
//      {
//        if (textShape == null)
//        {
//          FontRenderContext frc = g2d.getFontRenderContext();
//          textLayout = new TextLayout(this.text, this.font, frc);
//          textShape = font.createGlyphVector(frc, text).getOutline(pos.x, pos.y);
//        }
//      }
//
//    g2d.fill(textShape);
    //textLayout.draw(g2d, pos.x, pos.y);

    if (cachedTextShape == null) {
      g2d.setFont(this.font);
      g2d.drawString(this.text, pos.x, pos.y);
    }
    else
      g2d.draw(cachedTextShape);    
  }
}

// EOF
