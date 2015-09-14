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
  private static final long serialVersionUID = 1L;

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
