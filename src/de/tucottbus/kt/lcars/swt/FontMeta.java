package de.tucottbus.kt.lcars.swt;

import java.io.Serializable;

import org.eclipse.swt.graphics.Font;

import de.tucottbus.kt.lcars.LCARS;

public abstract class FontMeta implements Serializable
{
  /**
   * Automatically generated serial version UID.
   */
  private static final long serialVersionUID = 4180056533608151385L;

  /**
   * The SWT display Y resolution in DPI (used to convert font sizes to LCARS
   * panel pixels).
   */
  private static int displayDpiY = 0;
  
  /**
   * Private implicit super-constructor.
   */
  private FontMeta() 
  {
  }
  
  /**
   * Returns the system font defined by the meta informations
   * @return
   */
  public abstract Font getFont();  

  /**
   * Converts a font height from LCARS panel pixels to points on the LCARS SWT 
   * display.
   * 
   * @param pixels
   *          The font height in LCARS panel pixels.
   * @return
   *    The font height in points.
   */
  protected final int pxToPt(int pixels)
  {
    if (displayDpiY<=0)
      LCARS.getDisplay().syncExec(()->
      {
        displayDpiY = LCARS.getDisplay().getDPI().y;
      });
      
    return (int)Math.round((float)pixels/(float)displayDpiY*72.);
  }
  
  //-- Nested classes --//
  
  /**
   * Descriptor of a standard LCARS font. Standard LCARS font have defined type
   * faces and sizes.
   * 
   * @author Christian Borck
   * @author Matthias Wolff
   */
  public static final class Implicit extends FontMeta 
  {
    private static final long serialVersionUID = 4180056533608151386L;
    
    /**
     * The LCARS font style, one of the {@link LCARS}{@code .ES_XXX} constants.
     */
    public final int style;
    
    /**
     * Creates a new standard font descriptor.
     * 
     * @param style 
     *          The LCARS font style, one of the {@link LCARS}{@code .ES_XXX} 
     *          constants.
     */
    public Implicit(int style) 
    {
      this.style = style;
    }
    
    @Override
    public Font getFont()
    {
      return LCARS.getFontMeta(style).getFont();
    }

    public String toString() 
    {
      return FontMeta.class.getSimpleName() + "." + Implicit.class.getSimpleName() + " style=" + Integer.toHexString(style);
    }

  }
  
  /**
   * Descriptor for a custom font.
   * 
   * @author Christian Borck
   * @author Matthias Wolff
   */
  public static final class Explicit extends FontMeta 
  {
    private static final long serialVersionUID = 4180056533608151387L;

    /**
     * The font name.
     */
    public final String name;
    
    /**
     * The font height in LCARS panel pixels.
     */
    public final int height;
    
    /**
     * The SWT font style.
     */
    public final int style;
        
    /**
     * Creates new custom font descriptor.
     * 
     * @param meta
     *          The meta data to derive this instance from.
     * @param height
     *          The height of the font in LCARS panel pixels.
     */
    public Explicit(Explicit meta, int height) 
    {
      super();
      this.name = meta.name;
      this.height = height;
      this.style = meta.style;
    }

    /**
     * Creates a new custom font descriptor.
     * 
     * @param name
     *          The name of the new font.
     * @param height
     *          The height of the new font in LCARS panel pixels.
     * @param style
     *          The font style, a bitwise combination of SWT font style 
     *          constants.
     */
    public Explicit(String name, int height, int style) 
    {
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
      return SWTResourceManager.getFont(name, pxToPt(height), style);
    }
    
    @Override
    public String toString() 
    {
      return FontMeta.class.getSimpleName() + "." 
        + Explicit.class.getSimpleName() + " name=\"" + name + "\" height=" 
        + height + " style=" + style;
    }
  }
}
