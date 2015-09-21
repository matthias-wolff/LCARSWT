package de.tucottbus.kt.lcars.swt;

import java.io.Serializable;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;

public final class SwtColor implements Serializable
{
  private static final long serialVersionUID = 7144575098466224086L;
  
  /** Constant for the color white: R=255, G=255, B=255. */
  public static final SwtColor white = new SwtColor(0xFFFFFF);
  
  /** Constant for the color white: R=255, G=255, B=255. */
  public static final SwtColor WHITE = white;
  
  /** Constant for the color light gray: R=192, G=192, B=192. */
  public static final SwtColor lightGray = new SwtColor(0xc0c0c0, false);
  
  /** Constant for the color light gray: R=192, G=192, B=192. */
  public static final SwtColor LIGHT_GRAY = lightGray;
  
  /** Constant for the color gray: R=128, G=128, B=128. */
  public static final SwtColor gray = new SwtColor(0x808080, false);
  
  /** Constant for the color gray: R=128, G=128, B=128. */
  public static final SwtColor GRAY = gray;
  
  /** Constant for the color dark gray: R=64, G=64, B=64. */
  public static final SwtColor darkGray = new SwtColor(0x404040, false);

  /** Constant for the color dark gray: R=64, G=64, B=64. */
  public static final SwtColor DARK_GRAY = darkGray;
  
  /** Constant for the color red: R=255, G=0, B=0. */
  public static final SwtColor black = new SwtColor(0x000000);
  
  /** Constant for the color red: R=255, G=0, B=0. */
  public static final SwtColor BLACK = black;

  /** Constant for the color red: R=255, G=0, B=0. */
  public static final SwtColor red = new SwtColor(0xff0000, false);
  
  public static final SwtColor RED = red;
  
  /** Constant for the color pink: R=255, G=175, B=175. */
  public static final SwtColor pink = new SwtColor(0xffafaf, false);
  
  public static final SwtColor PINK = pink;
  
  /** Constant for the color orange: R=255, G=200, B=0. */
  public static final SwtColor orange = new SwtColor(0xffc800, false);
  
  public static final SwtColor ORANGE = orange;
  
  /** Constant for the color yellow: R=255, G=255, B=0. */
  public static final SwtColor yellow = new SwtColor(0xffff00, false);
  
  public static final SwtColor YELLOW = yellow;
  
  /** Constant for the color green: R=0, G=255, B=0. */
  public static final SwtColor green = new SwtColor(0x00ff00, false);
  
  public static final SwtColor GREEN = green;
  
  /** Constant for the color magenta: R=255, G=0, B=255. */
  public static final SwtColor magenta = new SwtColor(0xff00ff, false);
  
  public static final SwtColor MAGENTA = magenta;
  
  /** Constant for the color cyan: R=0, G=255, B=255. */
  public static final SwtColor cyan = new SwtColor(0x00ffff, false);
  
  public static final SwtColor CYAN = cyan;
  
  /** Constant for the color blue: R=0, G=0, B=255. */
  public static final SwtColor blue = new SwtColor(0x0000ff, false);
  
  public static final SwtColor BLUE = blue;
  
  /** Internal mask for red. */
  private static final int R_SHIFT = 16;

  /** Internal mask for green. */
  private static final int G_SHIFT = 8;

  /** Internal mask for blue. */
  private static final int B_SHIFT = 0;

  /** Internal mask for alpha. Package visible for use in subclass. */
  private static final int A_SHIFT = 24;

  /** Amount to scale a color by when brightening or darkening. */
  private static final float BRIGHT_SCALE = 0.7f;
  
  
  // field //
  
  private final org.eclipse.swt.graphics.RGB RGB;
  private final byte Alpha;
  
  
  public SwtColor(SwtColor color) {
    org.eclipse.swt.graphics.RGB rgb = color.RGB;
    RGB = new org.eclipse.swt.graphics.RGB(rgb.red, rgb.green, rgb.blue);
    Alpha = color.Alpha;
  }
  
  public SwtColor(SwtColor color, float alpha) {
    org.eclipse.swt.graphics.RGB rgb = color.RGB;
    RGB = new org.eclipse.swt.graphics.RGB(rgb.red, rgb.green, rgb.blue);
    Alpha = alpha <= 0 ? Byte.MIN_VALUE : (alpha >= 1 ? Byte.MAX_VALUE : (byte)(alpha*Byte.MAX_VALUE));
  }
  
  public SwtColor(int argb, boolean hasTransparence) {
    RGB = new org.eclipse.swt.graphics.RGB((argb >> R_SHIFT) & 0xFF, (argb >> G_SHIFT) & 0xFF, (argb >> B_SHIFT) & 0xFF);
    Alpha = hasTransparence ? (byte)(argb >> A_SHIFT) : Byte.MAX_VALUE;
  }
  
  public SwtColor(int rgb) {
    RGB = new org.eclipse.swt.graphics.RGB((rgb >> R_SHIFT) & 0xFF, (rgb >> G_SHIFT) & 0xFF, (rgb >> B_SHIFT) & 0xFF);
    Alpha = Byte.MAX_VALUE;
  } 
  
  public SwtColor(float r, float g, float b, float a) {
    RGB = new org.eclipse.swt.graphics.RGB(
                    r <= 0 ? Byte.MIN_VALUE : (r >= 1 ? Byte.MAX_VALUE : (int)(r*Byte.MAX_VALUE)),
                    g <= 0 ? Byte.MIN_VALUE : (g >= 1 ? Byte.MAX_VALUE : (int)(g*Byte.MAX_VALUE)),
                    b <= 0 ? Byte.MIN_VALUE : (b >= 1 ? Byte.MAX_VALUE : (int)(b*Byte.MAX_VALUE)));
    Alpha = a <= 0 ? Byte.MIN_VALUE : (a >= 1 ? Byte.MAX_VALUE : (byte)(a*Byte.MAX_VALUE));
  }
  
  public SwtColor(int r, int g, int b, int a) {
    RGB = new org.eclipse.swt.graphics.RGB(r, g, b);
    Alpha = a < Byte.MIN_VALUE ? Byte.MIN_VALUE : (a > Byte.MAX_VALUE ? Byte.MAX_VALUE : (byte)a);    
  }
  
  public SwtColor(int r, int g, int b) {
    RGB = new org.eclipse.swt.graphics.RGB(r, g, b);
    Alpha = Byte.MAX_VALUE;    
  }
    
  public int getRed() {
    return RGB.red;
  }
  
  public int getGreen() {
    return RGB.green;
  }
  
  public int getBlue() {
    return RGB.blue;
  }
  
  public int getAlpha() {
    return Alpha & 0xFF;
  }
  
  public org.eclipse.swt.graphics.RGB getRGB() {
    return new org.eclipse.swt.graphics.RGB(RGB.red, RGB.green, RGB.blue);
  }
  
  public float[] getHSB() {
    return RGB.getHSB();
  }
  
  public void applyForeground(GC gc) {
    Color c = new Color(gc.getDevice(), RGB);
    gc.setForeground(c);
    c.dispose();
  }
  
  /**
  * Creates a new <code>Color</code> that is a brighter version of this
  * <code>Color</code>.
  * <p>
  * This method applies an arbitrary scale factor to each of the three RGB
  * components of this <code>Color</code> to create a brighter version
  * of this <code>Color</code>.
  * The {@code alpha} value is preserved.
  * Although <code>brighter</code> and
  * <code>darker</code> are inverse operations, the results of a
  * series of invocations of these two methods might be inconsistent
  * because of rounding errors.
  * @return     a new <code>Color</code> object that is
  *                 a brighter version of this <code>Color</code>
  *                 with the same {@code alpha} value.
  * @see        de.tucottbus.kt.lcars.swt.SwtColor#darker
  * @since      JDK1.0
  */
  public SwtColor brighter() {
    final float scale = 1/BRIGHT_SCALE;
    int r = RGB.red;
    int g = RGB.green;
    int b = RGB.blue;
        
    /* From 2D group:
     * 1. black.brighter() should return grey
     * 2. applying brighter to blue will always return blue, brighter
     * 3. non pure color (non zero rgb) will eventually return white
     */
    final int i = (int)(1/(1-BRIGHT_SCALE));
    if ( r == 0 && g == 0 && b == 0)
      return new SwtColor(i, i, i, Alpha);
    
    if ( r > 0 && r < i ) r = i;
    if ( g > 0 && g < i ) g = i;
    if ( b > 0 && b < i ) b = i;
    
    return new SwtColor(Math.min((int)(r*scale), 255),
                        Math.min((int)(g*scale), 255),
                        Math.min((int)(b*scale), 255),
                        Alpha);
  }

  /**
   * Creates a new <code>Color</code> that is a darker version of this
   * <code>Color</code>.
   * <p>
   * This method applies an arbitrary scale factor to each of the three RGB
   * components of this <code>Color</code> to create a darker version of
   * this <code>Color</code>.
   * The {@code alpha} value is preserved.
   * Although <code>brighter</code> and
   * <code>darker</code> are inverse operations, the results of a series
   * of invocations of these two methods might be inconsistent because
   * of rounding errors.
   * @return  a new <code>Color</code> object that is
   *                    a darker version of this <code>Color</code>
   *                    with the same {@code alpha} value.
   * @see        de.tucottbus.kt.lcars.swt.SwtColor#brighter
   */
  public SwtColor darker() {
    return new SwtColor(Math.max((int)(RGB.red  *BRIGHT_SCALE), 0),
                        Math.max((int)(RGB.green*BRIGHT_SCALE), 0),
                        Math.max((int)(RGB.blue *BRIGHT_SCALE), 0),
                        Alpha);
  }
  
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (!(obj instanceof SwtColor))
      return false;    
    SwtColor c = (SwtColor)obj;
    return RGB.red == c.RGB.red
        && RGB.green == c.RGB.green
        && RGB.blue == c.RGB.blue
        && Alpha == c.Alpha;    
  }
  
  public boolean equals(SwtColor c) {
    return c != null && (this == c
        || (RGB.red == c.RGB.red
        && RGB.green == c.RGB.green
        && RGB.blue == c.RGB.blue
        && Alpha == c.Alpha));    
  }
}
