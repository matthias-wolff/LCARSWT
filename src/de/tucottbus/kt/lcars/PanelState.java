package de.tucottbus.kt.lcars;

import java.awt.Dimension;
import java.io.Serializable;

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
    if (o == null)
      return false;

    if (!(o instanceof PanelState))
      return false;

    PanelState other = (PanelState) o;

    if (!((dimension == other.dimension) || ((dimension != null) && dimension
        .equals(other.dimension))))
      return false;

    if (!((bgImageRes == other.bgImageRes) || ((bgImageRes != null) && bgImageRes
        .equals(other.bgImageRes))))
      return false;

    return colorScheme == other.colorScheme && blink == other.blink
        && modal == other.modal && silent == other.silent
        && alpha == other.alpha;
  }

}

// EOF
