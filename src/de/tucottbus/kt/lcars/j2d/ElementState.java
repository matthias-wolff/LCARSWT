package de.tucottbus.kt.lcars.j2d;

import java.awt.Color;
import java.awt.Rectangle;
import java.io.Serializable;

import de.tucottbus.kt.lcars.LCARS;
import de.tucottbus.kt.lcars.PanelState;
import de.tucottbus.kt.lcars.elements.EElement;
import de.tucottbus.kt.lcars.swt.SwtColor;
import de.tucottbus.kt.lcars.util.Objectt;

/**
 * The serializable state of an {@linkplain EElement LCARS GUI element}. Contains style and state
 * information as well as a set of {@link Geometry}s defining the graphical representation of the
 * element.
 * 
 * @author Matthias Wolff
 */
// TODO: Re-integrate into EElement?
public class ElementState implements Serializable
{
  // only use the second byte for flags, all other digits are reserved for flags of ElementData or PanelState
  
  /**flag mask**/
  public static final int FLAG_MASK    = 0xFF00;
      
  /**flag for changed bounds**/
  public static final int BOUNDS       = 0x0100;
  
  /**flag for changed color**/
  public static final int COLOR        = 0x0200;

  /**flag for changed transparency**/
  public static final int ALPHA        = 0x0400;

  /**flag for changed style**/
  public static final int STYLE        = 0x0800;
  
  /**flag for changed visibility**/
  public static final int VISIBLE      = 0x1000;
  
  /**flag for changed highlight**/
  public static final int HIGHLIGHT    = 0x2000;
  
  /**flag for changed touch state**/
  public static final int TOUCH        = 0x4000;
    
  // -- Fields --
  
  /**
   * The default serial version ID.
   */
  private static final long serialVersionUID = 1L;
  
  /**
   * Flag indicating that this instance has been changed.
   */
  private transient boolean changed;

  /**
   * The bounding rectangle of the element.
   */
  private Rectangle bounds;

  /**
   * The custom color of background {@link Geometry}s. If <code>null</code> the color is
   * obtained from the {@link #style} of the geometry through {@link LCARS#getColor(int, int)}.
   */
  private SwtColor color;
  
  /**
   * The custom opacity of background {@link Geometry}s.
   */
  private float alpha;
  
  /**
   * The style, a combination of {@link LCARS}<code>.XX_XXX</code> constants.
   */
  private int style;
  
  /**
   * The visibility.
   */
  private boolean visible;
  
  /**
   * The highlighted state.
   */
  private boolean highlighted;

  /**
   * The touch state.
   */
  private int touch;
  
  // -- Constructors --

  /**
   * Creates a new element state.
   * 
   * @param bounds
   *          The bounding rectangle.
   * @param style
   *          The style, a combination of {@link LCARS}<code>.XX_XXX</code> constants.
   */
  public ElementState(Rectangle bounds, int style)
  {
    this.changed     = true;
    this.bounds      = bounds;
    this.color       = null;
    this.alpha       = 1.f;
    this.style       = style;
    this.visible     = true;
    this.highlighted = false;
    this.touch       = 0;
  }
  
  /**
   * Copy constructor. Does not copy transient fields.
   * 
   * @param other
   *          The element state to copy.
   */
  public ElementState(ElementState other)
  {
    this.changed     = other.changed;
    this.bounds      = other.bounds!=null?new Rectangle(other.bounds):null;
    this.color       = other.color;
    this.alpha       = other.alpha;
    this.style       = other.style;
    this.visible     = other.visible;
    this.highlighted = other.highlighted;
    this.touch       = other.touch;
  }
  
  // -- Geometry getters and setters --

  /**
   * Sets the bounding rectangle of the graphical representation.
   * 
   * @param bounds
   *          The new bounding rectangle in LCARS panel coordinates.
   */
  public void setBounds(Rectangle bounds)
  {
    this.bounds = bounds;
  }

  /**
   * Returns the bounding rectangle of this graphical representation in LCARS panel coordinates.
   */
  public Rectangle getBounds()
  {
    return this.bounds;
  }
  
  // -- Color getters and setters --

  /**
   * Returns the custom opacity of background {@linkplain Geometry geometries}. Note that the return
   * value is not necessarily the opacity actually used for painting. The obtain the actual opacity
   * call {@link #getBgAlpha(PanelState)}.
   */
  public float getAlpha()
  {
    return this.alpha;
  }

  /**
   * Sets custom the opacity of background {@linkplain Geometry geometries}.
   * 
   * @param alpha
   *          The new opacity (0: transparent ... 1: opaque).
   */
  public void setAlpha(float alpha)
  {
    changed |= (this.alpha!=alpha);
    this.alpha = alpha;
  }

  /**
   * Returns the opacity actually used for painting foreground {@linkplain Geometry geometries}.
   */
  public float getFgAlpha()
  {
    return 1.f;
  }

  /**
   * Returns the opacity actually used for painting background {@linkplain Geometry geometries}. The
   * value depends on the custom opacity set through {@link #setAlpha(float)}, the
   * {@linkplain #style style}, the <code>panelState</code>, and internal states.
   * 
   * @param panelState
   *          The current panel state.
   */
  public float getBgAlpha(PanelState panelState)
  {
    return (getStyle(LCARS.ES_MODAL)==0 && panelState!=null) ? alpha * panelState.alpha : alpha;
  }

  /**
   * Returns the custom color of background {@linkplain Geometry geometries}. If
   * the value is <code>null</code> the background geometries will be painted in
   * their "natural" color defined by the {@linkplain #style style}. Call
   * {@link #getBgColor(PanelState)} to obtain the color actually used for
   * painting the background geometries.
   */
  public SwtColor getColor()
  {
    return this.color;
  }

  /**
   * Sets the custom color of background {@linkplain Geometry geometries}.
   * 
   * @param color
   *          The new custom background color. If <code>null</code> the background geometries will
   *          be painted in their "natural" color defined by the {@linkplain #style style}.
   */
  public void setColor(SwtColor color)
  {    
    changed |= !Objectt.equals(this.color, color);
    this.color = color;// != null ? color : SwtColor.BLACK;
    if (color != null && color.HasAlpha)
      this.alpha = color.getAlpha()/255;
  }

  /**
   * Returns the color actually used for painting foreground {@linkplain Geometry geometries}.
   */
  public SwtColor getFgColor()
  {
    return SwtColor.BLACK;
  }

  /**
   * Returns the color actually used for painting background {@linkplain Geometry geometries}. The
   * color depends on the custom color set through {@link #setColor(Color)}, the {@linkplain #style
   * style}, the <code>panelState</code>, and internal states.
   * 
   * @param panelState
   *          The current panel state.
   */
  public SwtColor getBgColor(PanelState panelState)
  {
    boolean blinking = getStyle(LCARS.ES_BLINKING)!=0;
    int     blink    = panelState!=null?panelState.blink:0;

    if (this.color!=null && (!blinking || blink!=0)) return color;
    if (highlighted) return SwtColor.WHITE;
    return LCARS.getColor(panelState.colorScheme,style^touch^(blinking?blink:0));
  }

  // -- Style getters and setters --
  
  /**
   * Returns the style.
   */
  public int getStyle()
  {
    return this.style;
  }
  
  /**
   * Returns style bits.
   * 
   * @param mask
   *          The style bits to retrieve.
   */
  public int getStyle(int mask)
  {
    return this.style & mask;
  }
  
  /**
   * Sets the style.
   * 
   * @param style
   *          The new style.
   */
  public void setStyle(int style)
  {
    changed |= (this.style!=style);
    this.style = style;
  }

  /**
   * Sets or clears style bits.
   * 
   * @param mask
   *          The style bits to set or clear.
   * @param set
   *          <code>true</code> to set, <code>false</code> to clear.
   */
  public void setStyle(int mask, boolean set)
  {
    if (set)
      setStyle(this.style | mask);
    else
      setStyle(this.style & ~mask);
  }

  // -- State getters and setters --

  /**
   * Determines if this element state has been changed since its creation or the last call to
   * {@link #clearChanged()}.
   * 
   * @see #clearChanged()
   */
  public boolean isChanged()
  {
    return this.changed;
  }
  
  /**
   * Marks this element state changed.
   */
  public void setChanged()
  {
    this.changed = true;
  }
  
  /**
   * Sets this element state unchanged. It becomes changed again when any setter is invoked.
   * 
   * @see #isChanged()
   */
  public void clearChanged()
  {
    this.changed = false;
  }
  
  /**
   * Returns the highlight state.
   */
  public boolean isHighlighted()
  {
    return this.highlighted;
  }
  
  /**
   * Toggles the highlight state.
   * 
   * @param highlight
   *          The new highlight state.
   */
  public void setHighlighted(boolean highlight)
  {
    changed |= (this.highlighted!=highlight);
    this.highlighted = highlight;
  }

  /**
   * Sets the touch state.
   *
   * @param touch
   *          The new touch state.
   */
  public void setTouch(int touch)
  {
    changed |= (this.touch!=touch);
    this.touch = touch;
  }

  /**
   * Returns the visibility.
   */
  public boolean isVisible()
  {
    return this.visible;
  }
  
  /**
   * Toggles the visibility.
   * 
   * @param visible
   *          The visibility.
   */
  public void setVisible(boolean visible)
  {
    changed |= (this.visible!=visible); 
    this.visible = visible;
  }

  /**
   * Returns the flags that indicates the changed parts of this PanelState and another.
   */  
  public int getUpdateFlags(ElementState other) {
    if (other == null) throw new NullPointerException();
    int result = !Objectt.equals(bounds,        other.bounds) ?              BOUNDS : 0;    
    if (         !Objectt.equals(color,         other.color))      result |= COLOR;
    if (                         alpha       != other.alpha)       result |= ALPHA;
    if (                         style       != other.style)       result |= STYLE;
    if (                         visible     != other.visible)     result |= VISIBLE;
    if (                         highlighted != other.highlighted) result |= HIGHLIGHT;
    if (                         alpha       != other.alpha)       result |= ALPHA;
    return                       touch       != other.touch ?      result |  TOUCH : result;
  }
}

// EOF
