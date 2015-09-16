package de.tucottbus.kt.lcars.j2d;

import java.awt.geom.Area;
import java.awt.geom.Point2D;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Path;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;

import de.tucottbus.kt.lcars.LCARS;
import de.tucottbus.kt.lcars.logging.Log;

/**
 * A geometry representing a text.
 * 
 * @author Matthias Wolff, Christian Borck
 */
public class GText extends Geometry
{
  private static final long serialVersionUID = -1724627622898883028L;  
  protected Rectangle bounds;
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
  public GText(String text, Rectangle bounds, FontData font,
      boolean foreground)
  {    
    super(foreground);
    if (bounds == null)
      throw new NullPointerException("bounds");

    this.text = text;
    this.bounds = bounds;
    this.font = font;
  }
  
  public Point2D.Float getPos()
  {
    return new Point2D.Float(bounds.x, bounds.y);
  }

  @Deprecated
  public int getStyle() {
    return 0;
  }
  
  @Override
  public void getArea(Area area)
  {
    area.add(new Area(new java.awt.Rectangle(bounds.x, bounds.y, bounds.width, bounds.height)));
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

  private Path getTextPath() {
    if(textPath == null) {
      Display display = LCARS.getDisplay();
      Font font = new Font(display, this.font);
      textPath = LCARS.getTextShape(new Font(display, this.font), text, new java.awt.Rectangle(bounds.x, bounds.y, bounds.width, bounds.height));
      font.dispose();
    }
    return textPath;
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see de.tucottbus.kt.lcars.j2d.EGeometry2D#paint2D(java.awt.Graphics2D)
   */
  @Override
  public void paint2D(GC gc)
  {
    gc.setForeground(new Color(gc.getDevice(), new RGB(255, 255, 255)));
    
    gc.drawString(text, bounds.x, bounds.y, true);
    
//    Log.debug(isForeground() + "");
//    Font font = new Font(gc.getDevice(), this.font);
//    gc.setFont(font);
//    gc.drawText(text, x, y - gc.getFontMetrics().getAscent());
//    font.dispose();
    
    Path textPath = getTextPath();

    float[] b = new float[4];
    textPath.getBounds(b);
    //Log.debug((int)b[0]+","+(int)b[1]+","+(int)b[2]+","+(int)b[3]+"|"+x+","+y);
    
    gc.setBackground(new Color(gc.getDevice(), 255, 255, 255));
    gc.setForeground(new Color(gc.getDevice(), 255, 255, 255));
    gc.fillPath(textPath);        
  }
  
  
  @Override
  protected void finalize() throws Throwable
  {
    try
    {
      textPath.isDisposed();
    } catch (Exception e)
    {
      // ignored
    }
          
    super.finalize();
  }
  
}

// EOF
