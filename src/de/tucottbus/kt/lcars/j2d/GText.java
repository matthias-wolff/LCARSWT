package de.tucottbus.kt.lcars.j2d;

import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;

import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.jfree.experimental.swt.SWTUtils;

/**
 * A geometry representing a text.
 * 
 * @author Matthias Wolff
 */
public class GText extends Geometry
{
  private static final long serialVersionUID = 1L;
  protected java.awt.Font awtFont;
  protected Font font;
  protected int x;
  protected int y;
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
  public GText(String text, Point2D.Float pos, Rectangle shape, java.awt.Font font,
      boolean foreground)
  {
    super(foreground);
    this.text = text;
    awtFont = font;
    x = (int)pos.x;
    y = (int)pos.y;
    this.shape = new GeneralPath(shape);
  }

  public Font getFont()
  {
    return this.font;
  }

  public Point2D.Float getPos()
  {
    return new Point2D.Float(x, y);
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
  public void paint2D(GC gc)
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

//    if (cachedTextShape == null) {
//      gc.setFont(this.font);
//      gc.drawString(this.text, pos.x, pos.y);
//    }
//    else
//      gc.draw(cachedTextShape);
    if (font == null)
      font = new org.eclipse.swt.graphics.Font( 
              gc.getDevice(), 
              SWTUtils.toSwtFontData(gc.getDevice(), awtFont, true));    
    gc.setFont(font);
    gc.drawText(text, x, y);
  }
}

// EOF
