package de.tucottbus.kt.lcars.j2d.caching;

import java.awt.Font;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;

import org.apache.commons.collections4.map.LRUMap;

public class GTextCache
{
  private LRUMap<Key, GlyphVector> hashmap;
  
  public GTextCache() {
    hashmap = new LRUMap<GTextCache.Key, GlyphVector>();
  }
  
  public GTextCache(int maxSize) {
    hashmap = new LRUMap<GTextCache.Key, GlyphVector>(maxSize);
  }
  
  public GlyphVector getGlyphVector(FontRenderContext frc, Font font, String text) {
    Key key = new Key(/*frc,*/ font, text);
    GlyphVector gv = hashmap.get(key);
    if(gv == null) {
      gv = createGlyphs(frc, font, text);
      hashmap.put(key, gv);
    }
    
    return gv;    
  }
  
  private static GlyphVector createGlyphs (FontRenderContext frc, Font font, String text) {
    // TODO: check for capacity
    return font.createGlyphVector(frc, text);
  }
  
  private class Key {
    /*public FontRenderContext frc;*/
    public Font font;
    public String text;
    
    public Key(/*FontRenderContext frc,*/ Font font, String text) {
      /*this.frc = frc;*/
      this.font = font;
      this.text = text;
      this.equals(this);
    }
    
    @Override
    public boolean equals(Object object) {      
      return object instanceof Key && equals((Key)object);
    }
    
    public boolean equals(Key key) {
      return /*frc.equals(key.frc) &&*/ font.equals(key.font) && text.equals(key.text);
    }
    
    @Override
    public int hashCode() {
      return /*frc.hashCode() ^*/ font.hashCode() ^ text.hashCode();
    }
    
  }
  
}
