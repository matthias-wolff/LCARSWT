package de.tucottbus.kt.lcars.j2d.rendering;

import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.image.ImageObserver;

import org.apache.commons.collections4.map.LRUMap;

/**
 * Wrapper class for {@link java.awt.Graphics2D} contains caching strategies.
 * @author Christian Borck
 *
 */
public class AdvGraphics2D
{
  // -- Field --
  
  /**
   * Graphics to draw on
   */
  private Graphics2D g;  
  
  /**
   * Cached font render context of Graphics
   */
  private FontRenderContext frc;
  
  /**
   * Glyph cache
   */
  private LRUMap<TextKey, GlyphVector> glyphCache;

  // -- Constructors
  
  public AdvGraphics2D(int textCacheSize)
  {
    glyphCache = new LRUMap<TextKey, GlyphVector>(textCacheSize);
  }

  // -- Graphics methods --
  
  public void setGraphics(Graphics2D g2d)
  {
    g = g2d;
    FontRenderContext nextFrc = g2d.getFontRenderContext();
    if(frc!=null && !frc.equals(nextFrc))
      glyphCache.clear();
    
    frc = g2d.getFontRenderContext();
  }

  /* (non-Javadoc)
   * @see java.awt.Graphics2D#setComposite(java.awt.Composite)
   */
  public void setComposite(Composite comp)
  {
    g.setComposite(comp);
  }

  /* (non-Javadoc)
   * @see java.awt.Graphics2D#setColor(java.awt.Color)
   */
  public void setColor(Color c)
  {
    g.setColor(c);
  }

  /* (non-Javadoc)
   * @see java.awt.Graphics2D#setClip(java.awt.Shape)
   */
  public void setClip(Shape clip)
  {
    g.setClip(clip);
  }

  /* (non-Javadoc)
   * @see java.awt.Graphics2D#setClip(int, int, int, int)
   */
  public void setClip(int x, int y, int width, int height)
  {
    g.setClip(x, y, width, height);
  }

  /* (non-Javadoc)
   * @see java.awt.Graphics2D#fill(java.awt.Shape)
   */
  public void fill(Shape s)
  {
    g.fill(s);
  }

  /* (non-Javadoc)
   * @see java.awt.Graphics2D#getTransform()
   */
  public AffineTransform getTransform()
  {
    return g.getTransform();
  }

  /* (non-Javadoc)
   * @see java.awt.Graphics2D#draw(java.awt.Shape)
   */
  public void draw(Shape s)
  {
    g.draw(s);
  }

  /* (non-Javadoc)
   * @see java.awt.Graphics2D#drawImage(java.awt.Image, java.awt.geom.AffineTransform, java.awt.image.ImageObserver)
   */
  public void drawImage(Image img, AffineTransform xform, ImageObserver obs)
  {
    g.drawImage(img, xform, obs);
  }
  
  /* (non-Javadoc)
   * @see java.awt.Graphics2D#drawImage(java.awt.Image, int, int, java.awt.image.ImageObserver)
   */
  public void drawImage(Image image, int x, int y, ImageObserver obs)
  {
    g.drawImage(image, x, y, obs);    
  }

  /* (non-Javadoc)
   * @see java.awt.Graphics2D#drawString(java.lang.String, int, int)
   */
  public Shape drawString(String text, int x, int y) {
    Shape textShape = getGlyphVector(frc, g.getFont(), text).getOutline(x, y);
    g.fill(textShape);
    return textShape;
  }
  
  /* (non-Javadoc)
   * @see java.awt.Graphics2D#drawString(java.lang.String, float, float)
   */
  public Shape drawString(String text, float x, float y) {
    Shape textShape = getGlyphVector(frc, g.getFont(), text).getOutline(x, y);    
    g.fill(textShape);
    return textShape;
  }

  /* (non-Javadoc)
   * @see java.awt.Graphics2D#setFont(java.awt.Font)
   */
  public void setFont(Font font)
  {
    this.g.setFont(font);
  }
  
  
  // -- Fonts and Glyphs
  
  private GlyphVector getGlyphVector(FontRenderContext frc, Font font, String text) {
    TextKey key = new TextKey(/*frc,*/ font, text);
    GlyphVector gv = glyphCache.get(key);
    if(gv == null) {
      gv = createGlyphVector(frc, font, text);
      glyphCache.put(key, gv);
    }
    
    return gv;    
  }
  
  private static GlyphVector createGlyphVector (FontRenderContext frc, Font font, String text) {
    // TODO: check for capacity
    return font.createGlyphVector(frc, text);
  }
  
  // -- Nested Class
  
  private class TextKey {
    /*public FontRenderContext frc;*/
    public Font font;
    public String text;
    
    public TextKey(/*FontRenderContext frc,*/ Font font, String text) {
      /*this.frc = frc;*/
      this.font = font;
      this.text = text;
      this.equals(this);
    }
    
    @Override
    public boolean equals(Object object) {
      return object instanceof TextKey && equals((TextKey)object);
    }
    
    public boolean equals(TextKey key) {
      return /*frc.equals(key.frc) &&*/ font.equals(key.font) && text.equals(key.text);
    }
    
    @Override
    public int hashCode() {
      return /*frc.hashCode() ^*/ font.hashCode() ^ text.hashCode();
    }
    
  }

}
