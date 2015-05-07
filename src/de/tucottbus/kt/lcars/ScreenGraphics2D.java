package de.tucottbus.kt.lcars;

import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.image.ImageObserver;

import de.tucottbus.kt.lcars.j2d.caching.GTextCache;

public class ScreenGraphics2D
{
  private Graphics2D g;  
  
  private FontRenderContext frc;
  
  private GTextCache cache;

  public ScreenGraphics2D(int textCacheSize)
  {
    cache = new GTextCache(textCacheSize);
  }

  public void setGraphics(Graphics2D g2d)
  {
    g = g2d;
    frc = g2d.getFontRenderContext();
  }

  public Graphics2D getGraphics() {
    return g;
  }
  
  public void setComposite(Composite comp)
  {
    g.setComposite(comp);
  }

  public void setColor(Color c)
  {
    g.setColor(c);
  }

  public void setClip(Shape clip)
  {
    g.setClip(clip);
  }

  public void fill(Shape s)
  {
    g.fill(s);
  }

  public AffineTransform getTransform()
  {
    return g.getTransform();
  }

  public void draw(Shape s)
  {
    g.draw(s);
  }

  public void drawImage(Image img, AffineTransform xform, ImageObserver obs)
  {
    g.drawImage(img, xform, obs);
  }
  
  public void drawImage(Image image, int x, int y, ImageObserver obs)
  {
    g.drawImage(image, x, y, obs);    
  }

  public void drawString(String text, int x, int y) {
    g.fill(cache.getGlyphVector(frc, g.getFont(), text).getOutline(x, y));
  }
  
  public void drawString(String text, float x, float y) {
    g.fill(cache.getGlyphVector(frc, g.getFont(), text).getOutline(x, y));
  }

  public void setFont(Font font)
  {
    this.g.setFont(font);
  }
}
