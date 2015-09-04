package de.tucottbus.kt.lcars.j2d.rendering;

import java.awt.Font;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;

import org.apache.commons.collections4.map.LRUMap;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Path;

/**
 * Wrapper class for {@link java.awt.Graphics2D} contains caching strategies.
 * @author Christian Borck
 *
 */
public class AdvGraphics2D extends SWTGraphics2D
{
  // -- Field --  
  
  /**
   * Cached font render context of Graphics
   */
  private FontRenderContext frc;
  
  /**
   * Glyph cache
   */
  private LRUMap<TextKey, GlyphVector> glyphCache;

  // -- Constructors
  
  public AdvGraphics2D(GC gc, int textCacheSize)
  {
    super(gc);
    FontRenderContext nextFrc = getFontRenderContext();
    glyphCache = new LRUMap<TextKey, GlyphVector>(textCacheSize);
  }

  /* (non-Javadoc)
   * @see java.awt.Graphics2D#setColor(java.awt.Color)
   */
  public void setColor(Color c)
  {
    gc.setBackground(c);
  }

  public void fill(Path path) {
    gc.fillPath(path);
  }
  
  /* (non-Javadoc)
   * @see java.awt.Graphics2D#drawImage(java.awt.Image, java.awt.geom.AffineTransform, java.awt.image.ImageObserver)
   */
  public void drawImage(Image image, int x, int y)
  {
    gc.drawImage(image, x, y);
  }
  
  /* (non-Javadoc)
   * @see java.awt.Graphics2D#drawImage(java.awt.Image, int, int, java.awt.image.ImageObserver)
   */
  public void drawImage(Image image, int srcX, int srcY, int srcWidth, int srcHeight, int destX, int destY, int destWidth, int destHeight)
  {
    gc.drawImage(image, srcX, srcY, srcWidth, srcHeight, destX, destY, destWidth, destHeight);  
  }

  /* (non-Javadoc)
   * @see java.awt.Graphics2D#drawString(java.lang.String, int, int)
   */
//  public Shape drawString(String text, int x, int y) {
//    Shape textShape = getGlyphVector(frc, gc.getFont(), text).getOutline(x, y);
//    gc.fill(textShape);
//    return textShape;
//  }
  
//  /* (non-Javadoc)
//   * @see java.awt.Graphics2D#drawString(java.lang.String, float, float)
//   */
//  public Shape drawString(String text, float x, float y) {
//    Shape textShape = getGlyphVector(frc, gc.getFont(), text).getOutline(x, y);    
//    gc.fill(textShape);
//    return textShape;
//  }
//
//  /* (non-Javadoc)
//   * @see java.awt.Graphics2D#setFont(java.awt.Font)
//   */
//  public void setFont(Font font)
//  {
//    this.gc.setFont(font);
//  }
//  
//  
//  // -- Fonts and Glyphs
//  
//  private GlyphVector getGlyphVector(FontRenderContext frc, Font font, String text) {
//    TextKey key = new TextKey(/*frc,*/ font, text);
//    GlyphVector gv = glyphCache.get(key);
//    if(gv == null) {
//      gv = createGlyphVector(frc, font, text);
//      glyphCache.put(key, gv);
//    }
//    
//    return gv;    
//  }
//  
//  private static GlyphVector createGlyphVector (FontRenderContext frc, Font font, String text) {
//    // TODO: check for capacity
//    return font.createGlyphVector(frc, text);
//  }
  
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
