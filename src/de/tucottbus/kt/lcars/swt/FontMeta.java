package de.tucottbus.kt.lcars.swt;

import java.io.Serializable;

import org.eclipse.swt.graphics.Font;

import de.tucottbus.kt.lcars.LCARS;

public abstract class FontMeta implements Serializable
{
  /**
   * 
   */
  private static final long serialVersionUID = 4180056533608151385L;

  private FontMeta() {}
  
  /**
   * Returns the system font defined by the meta informations
   * @return
   */
  public abstract Font getFont();  
  
  
  //-- nested classes --//
  
  /**
   * Represents meta data of a font which is dependent on the local system style.
   * @author Christian Borck
   *
   */
  public static final class Implicit extends FontMeta {
    private static final long serialVersionUID = 4180056533608151386L;
    
    public final int style;
    
    /**
     * 
     * @param style LCARS style, see {@link LCARS#ES_FONT}
     */
    public Implicit(int style) {
      this.style = style;
    }
    
    @Override
    public Font getFont()
    {
      return LCARS.getFontMeta(style).getFont();
    }
    public String toString() {
      return FontMeta.class.getSimpleName() + "." + Implicit.class.getSimpleName() + " style=" + Integer.toHexString(style);
    }

  }
  
  public static final class Explicit extends FontMeta {
    private static final long serialVersionUID = 4180056533608151387L;

    public final String name;
    public final int height;
    public final int style;
        
    public Explicit(Explicit meta, int height) {
      super();
      this.name = meta.name;
      this.height = height;
      this.style = meta.style;
    }
    
    public Explicit(String name, int height, int style) {
      super();
      if (name == null)
        throw new IllegalArgumentException("name == null");
      
      this.name = name;
      this.height = height;
      this.style = style;
    }
    
    @Override
    public Font getFont()
    {
      return SWTResourceManager.getFont(name, height, style);
    }
    
    @Override
    public String toString() {
      return FontMeta.class.getSimpleName() + "." + Explicit.class.getSimpleName() + " name=\"" + name + "\" height=" + height + " style=" + style;
    }    
  }
}
