package de.tucottbus.kt.lcars.elements;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Area;
import java.io.Serializable;
import java.util.Vector;

import de.tucottbus.kt.lcars.LCARS;
import de.tucottbus.kt.lcars.PanelState;
import de.tucottbus.kt.lcars.Screen;
import de.tucottbus.kt.lcars.j2d.ElementState;
import de.tucottbus.kt.lcars.j2d.Geometry;

/**
 * The serializable data of an {@linkplain EElement LCARS GUI element}. An element data instance
 * stores the {@link #state} and the {@link #geometry} of a GUI element. Element data instances are
 * also used to transfer rendering information from the {@linkplain de.tucottbus.kt.lcars.Panel LCARS
 * panel} to the {@linkplain Screen screen}.
 * 
 * @author Matthias Wolff
 */
public class ElementData implements Serializable
{
  // -- Constants

  /**
   * Bit in the return value of {@link #applyUpdate(ElementData)} indicating that the
   * {@linkplain #state state} has been updated.
   */
  public static final int UPDATE_STATE = 0x00000001;
  
  /**
   * Bit in the return value of {@link #applyUpdate(ElementData)} indicating that the
   * {@linkplain #geometry geometry} has been updated.
   */
  public static final int UPDATE_GEOMETRY = 0x00000002;
  
  // -- Fields --
  
  /**
   * The default serial version ID.
   */
  private static final long serialVersionUID = 1L;

  /**
   * The unique serial number of the {@link EElement} described by this instance.
   */
  public long serialNo;
  
  /**
   * The state  of the {@link EElement} described by this instance.
   */
  public ElementState state;
  
  /**
   * The graphical representation of the {@link EElement} described by this instance.
   */
  public Vector<Geometry> geometry;

  // -- Constructors --
  
  /**
   * Creates a new element data instance.
   * 
   * @param serialNo
   *          The unique serial number of the {@link EElement} described by this instance.
   */
  public ElementData(long serialNo)
  {
    this.serialNo = serialNo;
    this.state    = null;
    this.geometry = null;
  }

  /**
   * Creates a copy of the element data stored in an {@linkplain EElement LCARS GUI element}.
   * 
   * @param element
   *          The GUI element to copy the data from
   */
  public ElementData(EElement element)
  {
    this.serialNo = element.data.serialNo;
    this.state    = new ElementState(element.data.state);
    this.geometry = new Vector<Geometry>(element.data.geometry);
  }
  
  // -- Getters and setters --

  /**
   * Determines if the {@link #state element state} has changed since its creation or the last call
   * to {@link ElementState#clearChanged()}.
   */
  public boolean isStateChanged()
  {
    return state.isChanged();
  }

  /**
   * Determines if the {@link #geometry geometry} (i.e. the graphical representation of this
   * element) is valid or needs to be recomputed.
   * 
   * @see EElement#invalidate(boolean)
   * @see EElement#validateGeometry()
   */
  public boolean isGeometryValid()
  {
    return geometry!=null;
  }
  
  /**
   * Returns the {@link Area area} covered by all background geometries.
   */
  public synchronized Area getArea()
  {
    try
    {
      Area area = new Area();
      for (Geometry gi : geometry)
      {
        if (gi.isForeground()) continue;
        area.add(new Area(gi.getArea()));
      }
      return area;
    }
    catch (NullPointerException e)
    {
      return new Area(state.getBounds());
    }
  }
  
  // -- Operations --

  /**
   * Returns a copy of this instance which is suitable for a {@linkplain Screen screen} update.
   * 
   * @param incremental
   *          Get copy for incremental or full update.
   * @param updateGeometry
   *          Include {@link #geometry geometry} in the update (always included if
   *          <code>incremental</code> is <code>false</code>).
   */
  public synchronized ElementData getUpdate(boolean incremental, boolean updateGeometry)
  {
    ElementData other = new ElementData(serialNo);
    if (isStateChanged() || !incremental)
      other.state = new ElementState(this.state);
    if (updateGeometry || !incremental)
      try
      {
        other.geometry = new Vector<Geometry>(this.geometry);
      }
      catch (Exception e)
      {
        // TODO: synchronization problem, exception should never occur 
        other.geometry = new Vector<Geometry>();
        LCARS.err("INT","Caught exception "+e.toString()+" at "+e.getStackTrace()[0].toString());
      }
    state.clearChanged();
    return other;
  }

  /**
   * Updates this instance with information taken from another one.
   * 
   * @param other
   *          The other instance.
   * @return A combination of {@link #UPDATE_STATE} and {@link #UPDATE_GEOMETRY} indicating the
   *         which data have actually been updated. A return value of 0 indicates that no updates
   *         have been performed.
   * @throws IllegalArgumentException
   *           <ol>
   *           <li>If the serial number of <code>other</code> is not equal to the serial number of
   *           <code>this</code> (i.e. the two instances describe different {@linkplain EElement
   *           LCARS GUI elements}).</li>
   *           <li>If the {@link #state} of this instance needs to be updated (i.e.
   *           <code>this.</code>{@link #state} is <code>null</code>) and <code>other</code> does
   *           not contain the information (i.e. <code>other.</code>{@link #state} is
   *           <code>null</code>).
   *           </ol>
   */
  public synchronized int applyUpdate(ElementData other)
  {
    if (this.serialNo!=other.serialNo)
      throw new IllegalArgumentException("Wrong serial numbers");
    
    int ret = 0;
    if (this.state==null)
    {
      if (other.state==null)
        throw new IllegalArgumentException("No state data present");
      this.state = new ElementState(other.state);
    }
    else
      ret |= UPDATE_STATE;
    if (this.geometry==null)
    {
      if (other.geometry==null)
        throw new IllegalArgumentException("No geometry data present");
      this.geometry = new Vector<Geometry>(other.geometry);
    }
    else
      ret |= UPDATE_GEOMETRY;
    return ret;
  }
  
  /**
   * Renders the graphical representation of the {@link EElement} described by this instance on a 2D
   * graphics context.
   * 
   * @param g2d
   *          The graphics context.
   * @param panelState
   *          The panel state.
   */
  public synchronized void render2D(Graphics2D g2d, PanelState panelState)
  {
    //if (geometry==null) return;
    if (state==null) LCARS.err("ELD","Invalid state #"+serialNo);
    if (!state.isVisible()) return;

    for (Geometry gi : geometry)
    {
      float alpha = gi.isForeground()?state.getFgAlpha():state.getBgAlpha(panelState);
      Color color = gi.isForeground()?state.getFgColor():state.getBgColor(panelState);
      g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,alpha));
      g2d.setColor(color);
      gi.paint2D(g2d);
    }
  }
}

// EOF
