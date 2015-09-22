package de.tucottbus.kt.lcars;

import java.awt.Dimension;
import java.io.Serializable;

import de.tucottbus.kt.lcars.util.Objectt;

/**
 * The state of an {@linkplain Panel LCARS panel}.
 * 
 * @author Matthias Wolff
 */
public class PanelState implements Serializable
{
  /**
   * The default serial version ID.
   */
  private static final long serialVersionUID = -6737109958392273399L;

  // only use the first byte for flags, all other digits are reserved for flags of ElementData or ElementState
  
  /**flag mask**/
  public static final int FLAG_MASK    = 0xFF0000;      
  
  /**flag for changed dimension**/
  public static final int DIMENSION    = 0x010000;
  
  /**flag for changed color scheme**/
  public static final int COLOR_SCHEME = 0x020000;

  /**flag for changed blink state**/
  public static final int BLINK        = 0x040000;
  
  /**flag for changed modal**/
  public static final int MODAL        = 0x080000;
  
  /**flag for changed silent state**/
  public static final int SILENT       = 0x100000;
  
  /**flag for changed lock state**/
  public static final int LOCKED       = 0x200000;
  
  /**flag for changed transparency**/
  public static final int ALPHA        = 0x400000;
  
  /**flag for changed background source**/
  public static final int BACKGROUND   = 0x800000;
     

  //-- field --//
  
  /**
   * The panel dimension.
   */
  public Dimension dimension = null;

  /**
   * The color scheme.
   */
  public int colorScheme = LCARS.CS_MULTIDISP;

  /**
   * The blinking state.
   */
  public int blink = 0;

  /**
   * The modal state.
   */
  public boolean modal = false;

  /**
   * The silent state.
   */
  public boolean silent = false;

  /**
   * The locked state.
   */
  public boolean locked = false;

  /**
   * Interval of GUI idle time after which an unlocked panel will be re-locked
   * automatically. Values &le;0 are ineffective, i.&nbsp;e. the panel will not 
   * automatically re-lock.
   */
  public transient int autoRelockTime = 0;
  
  /**
   * Remaining GUI idle time until automatic re-locking.
   */
  public transient int autoRelock = 0;
  
  /**
   * The master opacity.
   */
  public float alpha = 1.f;

  /**
   * The resource name of the background image file or <code>null</code> for no
   * background image.
   */
  public String bgImageRes = null;

  /**
   * Creates a new panel state instance.
   */
  public PanelState(Dimension dimension)
  {
    this.dimension = dimension;
  }

  /**
   * Returns the flags that indicates the changed parts of this PanelState and another.
   */  
  public int getUpdateFlags(PanelState other) {
    if (other == null) throw new NullPointerException();
    int result = !Objectt.equals(dimension,     other.dimension) ?           DIMENSION : 0;    
    if (         !Objectt.equals(bgImageRes,    other.bgImageRes)) result |= BACKGROUND;
    if (                         colorScheme != other.colorScheme) result |= COLOR_SCHEME;
    if (                         blink       != other.blink)       result |= BLINK;
    if (                         modal       != other.modal)       result |= MODAL;
    if (                         silent      != other.silent)      result |= SILENT;
    if (                         alpha       != other.alpha)       result |= ALPHA;
    return                       locked      != other.locked ?     result | LOCKED : result;
  }
    
  @Override
  public boolean equals(Object o)
  {
    return o != null
        && (o instanceof PanelState)
        && equals((PanelState) o);
  }

  public boolean equals(PanelState other)
  {
    return other != null
        && Objectt.equals(dimension, other.dimension)
        && Objectt.equals(bgImageRes, other.bgImageRes)
        && colorScheme == other.colorScheme
        && blink       == other.blink
        && modal       == other.modal
        && silent      == other.silent
        && alpha       == other.alpha
        && locked      == other.locked;
  }
  
}

// EOF
