package de.tucottbus.kt.lcars.swt;

import java.io.Serializable;

public final class SwtColor implements Serializable
{
  private static final long serialVersionUID = 7144575098466224086L;
  
  /** Constant for the color white: R=255, G=255, B=255. */
  public static final SwtColor white = new SwtColor(0xFFFFFF);
  
  /** Constant for the color white: R=255, G=255, B=255. */
  public static final SwtColor WHITE = white;
  
  /** Constant for the color light gray: R=192, G=192, B=192. */
  public static final SwtColor lightGray = new SwtColor(0xc0c0c0);
  
  /** Constant for the color light gray: R=192, G=192, B=192. */
  public static final SwtColor LIGHT_GRAY = lightGray;
  
  /** Constant for the color gray: R=128, G=128, B=128. */
  public static final SwtColor gray = new SwtColor(0x808080);
  
  /** Constant for the color gray: R=128, G=128, B=128. */
  public static final SwtColor GRAY = gray;
  
  /** Constant for the color dark gray: R=64, G=64, B=64. */
  public static final SwtColor darkGray = new SwtColor(0x404040);

  /** Constant for the color dark gray: R=64, G=64, B=64. */
  public static final SwtColor DARK_GRAY = darkGray;
  
  /** Constant for the color red: R=255, G=0, B=0. */
  public static final SwtColor black = new SwtColor(0x000000);
  
  /** Constant for the color red: R=255, G=0, B=0. */
  public static final SwtColor BLACK = black;

  /** Constant for the color red: R=255, G=0, B=0. */
  public static final SwtColor red = new SwtColor(0xff0000);
  
  public static final SwtColor RED = red;
  
  /** Constant for the color pink: R=255, G=175, B=175. */
  public static final SwtColor pink = new SwtColor(0xffafaf);
  
  public static final SwtColor PINK = pink;
  
  /** Constant for the color orange: R=255, G=200, B=0. */
  public static final SwtColor orange = new SwtColor(0xffc800);
  
  public static final SwtColor ORANGE = orange;
  
  /** Constant for the color yellow: R=255, G=255, B=0. */
  public static final SwtColor yellow = new SwtColor(0xffff00);
  
  public static final SwtColor YELLOW = yellow;
  
  /** Constant for the color green: R=0, G=255, B=0. */
  public static final SwtColor green = new SwtColor(0x00ff00);
  
  public static final SwtColor GREEN = green;
  
  /** Constant for the color magenta: R=255, G=0, B=255. */
  public static final SwtColor magenta = new SwtColor(0xff00ff);
  
  public static final SwtColor MAGENTA = magenta;
  
  /** Constant for the color cyan: R=0, G=255, B=255. */
  public static final SwtColor cyan = new SwtColor(0x00ffff);
  
  public static final SwtColor CYAN = cyan;
  
  /** Constant for the color blue: R=0, G=0, B=255. */
  public static final SwtColor blue = new SwtColor(0x0000ff);
  
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
  
  private final byte Red;
  private final byte Green;
  private final byte Blue;
  private final byte Alpha;
  
  public final boolean HasAlpha;
  
  public SwtColor(SwtColor color, float alpha) {
    Red = color.Red;
    Green = color.Green;
    Blue = color.Blue;
    Alpha = alpha <= 0 ? 0 : (alpha >= 1 ? -1 : (byte)(alpha*0xFF));
    HasAlpha = true;
  }
  
  public SwtColor(int argb, boolean hasTransparence) {
    Red = (byte)((argb >> R_SHIFT) & 0xFF);
    Green = (byte)((argb >> G_SHIFT) & 0xFF);
    Blue = (byte)((argb >> B_SHIFT) & 0xFF);
    Alpha = hasTransparence ? (byte)(argb >> A_SHIFT) : -1;
    HasAlpha = hasTransparence;
  }
  
  public SwtColor(int rgb) {
    Red = (byte)((rgb >> R_SHIFT) & 0xFF);
    Green = (byte)((rgb >> G_SHIFT) & 0xFF);
    Blue = (byte)((rgb >> B_SHIFT) & 0xFF);
    Alpha = -1;
    HasAlpha = false;
  } 
  
  public SwtColor(float r, float g, float b, float a) {
    Red   = r <= 0 ? 0 : (r >= 1 ? -1 : (byte)(r*0xFF));
    Green = g <= 0 ? 0 : (g >= 1 ? -1 : (byte)(g*0xFF));
    Blue  = b <= 0 ? 0 : (b >= 1 ? -1 : (byte)(b*0xFF));
    Alpha = a <= 0 ? 0 : (a >= 1 ? -1 : (byte)(a*0xFF));
    HasAlpha = true;
  }
  
  public SwtColor(float r, float g, float b) {
    Red   = r <= 0 ? 0 : (r >= 1 ? -1 : (byte)(r*0xFF));
    Green = g <= 0 ? 0 : (g >= 1 ? -1 : (byte)(g*0xFF));
    Blue  = b <= 0 ? 0 : (b >= 1 ? -1 : (byte)(b*0xFF));
    Alpha = -1;
    HasAlpha = false;
  }
  
  public SwtColor(int r, int g, int b, int a) {
    Red   = r <= 0 ? 0 : (r >= 0xFF ? -1 : (byte)r);
    Green = g <= 0 ? 0 : (g >= 0xFF ? -1 : (byte)g);
    Blue  = b <= 0 ? 0 : (b >= 0xFF ? -1 : (byte)b);
    Alpha = a <= 0 ? 0 : (a >= 0xFF ? -1 : (byte)a);    
    HasAlpha = true;
  }
  
  public SwtColor(int r, int g, int b) {
    Red   = r <= 0 ? 0 : (r >= 0xFF ? -1 : (byte)r);
    Green = g <= 0 ? 0 : (g >= 0xFF ? -1 : (byte)g);
    Blue  = b <= 0 ? 0 : (b >= 0xFF ? -1 : (byte)b);
    Alpha = -1;    
    HasAlpha = false;
  }
    
  public int getRed() {
    return Red & 0xFF;
  }
  
  public int getGreen() {
    return Green & 0xFF;
  }
  
  public int getBlue() {
    return Blue & 0xFF;
  }
  
  public int getAlpha() {
    return HasAlpha ? Alpha & 0xFF : 0xFF;
  }
  
  public org.eclipse.swt.graphics.RGB getRGB() {
    return new org.eclipse.swt.graphics.RGB(Red & 0xFF, Green & 0xFF, Blue & 0xFF);
  }
  
  public float[] getHSB() {
    return getRGB().getHSB();
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
    int r = Red & 0xFF;
    int g = Green & 0xFF;
    int b = Blue & 0xFF;
        
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
    return new SwtColor(Math.max((int)((Red & 0xFF)*BRIGHT_SCALE), 0),
                        Math.max((int)((Green & 0xFF)*BRIGHT_SCALE), 0),
                        Math.max((int)((Blue & 0xFF) *BRIGHT_SCALE), 0),
                        Alpha);
  }
  
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (!(obj instanceof SwtColor))
      return false;    
    SwtColor c = (SwtColor)obj;
    return Red == c.Red
        && Green == c.Green
        && Blue == c.Blue
        && Alpha == c.Alpha;    
  }
  
  public boolean equals(SwtColor c) {
    return c != null && (this == c
        || (Red == c.Red
        && Green == c.Green
        && Blue == c.Blue
        && Alpha == c.Alpha));    
  }
  
  public int getValue() {
    return ((Red & 0xFF) << R_SHIFT)
         | ((Green & 0xFF) << G_SHIFT)
         | ((Blue & 0xFF) << B_SHIFT)
         | (HasAlpha ? ((Alpha & 0xFF) << A_SHIFT) : (0xFF << A_SHIFT));
  }
  
  public String toString() {
    return SwtColor.class.getSimpleName() + " rgba="+Integer.toHexString(getValue());
  }
}
