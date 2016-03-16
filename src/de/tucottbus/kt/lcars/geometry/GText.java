package de.tucottbus.kt.lcars.geometry;

import java.awt.Rectangle;
import java.awt.geom.Area;
import java.awt.geom.Point2D;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;

import de.tucottbus.kt.lcars.LCARS;
import de.tucottbus.kt.lcars.swt.ColorMeta;
import de.tucottbus.kt.lcars.swt.FontMeta;

/**
 * A geometry representing a text.
 * 
 * @author Matthias Wolff, Christian Borck
 */
public class GText extends AGeometry
{
  private static final long serialVersionUID = -1724627622898883028L;

  protected String text;
  protected int descent;
  protected int indent;
  protected FontMeta fontMeta;

  protected int x;
  protected int y;
  protected int width;
  protected int height;

  /**
   * Creates a new text geometry. A text geometry provides information and
   * 2D-rendering for a single line of text
   * 
   * @param text
   *          the text
   * @param pos
   *          the position to draw the text on
   * @param bounds
   *          the bounding rectangle for touch detection (can be
   *          <code>null</code> for static texts); note that upper left corner
   *          of the bounding rectangle is <em>not</em> identical with the
   *          drawing position {@link pos}
   * @param font
   *          the font to render the text with
   * @param foreground
   *          foreground/background flag
   */
  public GText(String text, Rectangle bounds, FontMeta fontMeta,
      boolean foreground)
  {
    super(foreground);
    x = bounds.x;
    y = bounds.y;
    width = bounds.width;
    height = bounds.height;
    this.text = text;
    this.fontMeta = fontMeta;
  }

  /**
   * Returns the coordinates of the upper left corner where the text will draw using {@link paint2D(GC)}. 
   * @return
   */
  public Point2D.Float getPos()
  {
    return new Point2D.Float(x, y);
  }
  
  /**
   * Returns the style of the text.
   * @return always zero
   */
  @Deprecated
  public int getStyle()
  {
    return 0;
  }

  @Override
  public Area getArea()
  {
    return new Area(getBounds());
  }

  /**
   * Returns the smallest {@link java.awt.Rectangle} which contains the hole text ignoring indent and descent.
   */
  @Override
  public Rectangle getBounds()
  {
    return new Rectangle(x, y, width, height);
  }

  /**
   * Returns the text which will be drawn using {@link paint2D(GC)}.
   * @return
   */
  public String getText()
  {
    return this.text;
  }

  /**
   * Returns the distance from the baseline to the bottom of the lowest descenders on the glyphs.
   * @return
   */
  public int getDescent()
  {
    return this.descent;
  }

  /**
   * Sets the distance from the baseline to the bottom of the lowest descenders on the glyphs.
   */
  public void setDescent(int descent)
  {
    this.descent = descent;
  }

  /**
   * Returns the distance of the text from either the left or the right margin of the bounds
   * @return
   */
  public int getIndent()
  {
    return indent;
  }

  /**
   * Sets the distance of the text from either the left or the right margin of the bounds
   */
  public void setIndent(int indent)
  {
    this.indent = indent;
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.tucottbus.kt.lcars.geometry.AGeometry#paint2D(org.eclipse.swt.graphics.GC)
   */
  @Override
  public void paint2D(GC gc)
  {
    //org.eclipse.swt.graphics.Rectangle clip = gc.getClipping();
    Color cf = gc.getForeground();
    Color cb = gc.getBackground();
    gc.setFont(fontMeta.getFont());
    gc.setForeground(cb);
    //gc.setClipping(new org.eclipse.swt.graphics.Rectangle(x, y, width, height+2));
    gc.drawString(text, x + indent, y + descent, true);
    //gc.setClipping(clip);

    if (LCARS.SCREEN_DEBUG) // draw bounds
    {
      gc.setForeground(ColorMeta.RED.getColor());
      Rectangle b = getBounds();
      gc.drawRectangle(b.x, b.y, b.width - 1, b.height - 1);
    }
    gc.setForeground(cf);
    cf.dispose();
    cb.dispose();
  }

  @Override
  public String toString()
  {
    return getClass().getSimpleName() + " text=\"" + text + "\" bounds=(" + x
        + "," + y + "," + width + "," + height + ") fontmeta=" + fontMeta;
  }

}

// EOF
