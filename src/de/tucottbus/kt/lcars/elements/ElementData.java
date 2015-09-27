package de.tucottbus.kt.lcars.elements;

import java.awt.Rectangle;
import java.awt.geom.Area;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;

import de.tucottbus.kt.lcars.PanelState;
import de.tucottbus.kt.lcars.Screen;
import de.tucottbus.kt.lcars.geometry.AHeavyGeometry;
import de.tucottbus.kt.lcars.geometry.AGeometry;
import de.tucottbus.kt.lcars.logging.Log;

/**
 * The serializable data of an {@linkplain EElement LCARS GUI element}. An
 * element data instance stores the {@link #state} and the {@link #geometry} of
 * a GUI element. Element data instances are also used to transfer rendering
 * information from the {@linkplain Panel LCARS panel} to the {@linkplain Screen
 * screen}.
 * 
 * @author Matthias Wolff, Christian Borck
 */
public final class ElementData implements Serializable
{
  // -- Constants

  // only use the first byte for flags, all other digits are reserved for flags
  // of PanelState or ElementState

  /** flag mask **/
  public static final int FLAG_MASK = 0xFF;

  /**
   * Bit in the return value of {@link #applyUpdate(ElementData)} indicating
   * that the {@linkplain #geometry geometry} has been updated.
   */
  public static final int GEOMETRY_FLAG = 0x01;

  // -- Fields --

  /**
   * The default serial version ID.
   */
  private static final long serialVersionUID = 1L;

  /**
   * The unique serial number of the {@link EElement} described by this
   * instance.
   */
  public long serialNo;

  /**
   * The state of the {@link EElement} described by this instance.
   */
  public ElementState state;

  /**
   * The graphical representation of the {@link EElement} described by this
   * instance.
   */
  public ArrayList<AGeometry> geometry;

  /**
   * Bounds of the area of this element.
   */
  private transient Area cachedArea;

  // -- Constructors --

  /**
   * Creates a new element data instance.
   * 
   * @param serialNo
   *          The unique serial number of the {@link EElement} described by this
   *          instance.
   */
  public ElementData(long serialNo)
  {
    this.serialNo = serialNo;
    this.state = null;
    this.geometry = null;
  }

  /**
   * Creates a copy of the element data stored in an {@linkplain EElement LCARS
   * GUI element}.
   * 
   * @param element
   *          The GUI element to copy the data from
   */
  public ElementData(EElement element)
  {
    this.serialNo = element.data.serialNo;
    this.state = new ElementState(element.data.state);
    this.geometry = new ArrayList<AGeometry>(element.data.geometry);
  }

  // -- Getters and setters --

  /**
   * Determines if the {@link #state element state} has changed since its
   * creation or the last call to {@link ElementState#clearChanged()}.
   */
  public boolean isStateChanged()
  {
    return state.isChanged();
  }

  /**
   * Determines if the {@link #geometry geometry} (i.e. the graphical
   * representation of this element) is valid or needs to be recomputed.
   * 
   * @see #invalidateGeometry()
   * @see #validateGeometry()
   */
  public boolean isGeometryValid()
  {
    return geometry != null;
  }

  /**
   * Returns the {@link Area area} covered by all background geometries.
   */
  public void getArea(Area area)
  {
    if (area == null)
      return;
    if (cachedArea == null)
    {
      Area ar = new Area();
      try
      {
        for (AGeometry gi : geometry)
          if (!gi.isForeground())
            ar.add(gi.getArea());
        cachedArea = ar;
      } catch (NullPointerException e)
      {
        cachedArea = new Area(state.getBounds());
        Log.warn("Missing geometries in ElementData #" + serialNo);
      }
    }
    area.add((Area) cachedArea.clone());
    return;
  }

  // -- Operations --

  /**
   * Returns a copy of this instance which is suitable for a {@linkplain Screen
   * screen} update.
   * 
   * @param incremental
   *          Get copy for incremental or full update.
   * @param updateGeometry
   *          Include {@link #geometry geometry} in the update (always included
   *          if <code>incremental</code> is <code>false</code>).
   */
  public ElementData getUpdate(boolean incremental, boolean updateGeometry)
  {
    ElementData other = new ElementData(serialNo);
    boolean stateChanged = isStateChanged();
    if (stateChanged || !incremental)
      other.state = new ElementState(this.state);
    if (updateGeometry || !incremental)
      try
      {
        other.geometry = new ArrayList<AGeometry>(geometry.size());
        for (AGeometry geom : geometry)
          other.geometry.add(geom instanceof AHeavyGeometry
              ? ((AHeavyGeometry) geom).getUpdate(incremental) : geom);
      } catch (Exception e)
      {
        // TODO: synchronization problem, exception should never occur
        other.geometry = new ArrayList<AGeometry>();
        Log.err("Error while extracting updated data from ElementData with #"
            + serialNo, e);
      }

    state.clearChanged();
    return other;
  }

  /**
   * Updates this instance with information taken from another one.
   * 
   * @param other
   *          The other instance.
   * @return An bitwise OR combination of {@link #GEOMETRY_FLAG}, {@link ElementState#BOUNDS}, {@link ElementState#COLOR}, {@link ElementState#ALPHA}, {@link ElementState#STYLE}, {@link ElementState#VISIBLE}, {@link ElementState#HIGHLIGHT} or {@link ElementState#TOUCH} 
   *         indicating the which data have actually been updated. If
   *         {@link #STATE_FLAG} is in result, then element-wise state changes
   *         has been checked and added to the result too
   *         {@link ElementState#FLAG_MASK}. A return value of 0 indicates that
   *         no updates have been performed.
   * @throws IllegalArgumentException
   *           <ol>
   *           <li>If the serial number of <code>other</code> is not equal to
   *           the serial number of <code>this</code> (i.e. the two instances
   *           describe different {@linkplain EElement LCARS GUI elements}).
   *           </li>
   *           <li>If the {@link #state} of this instance needs to be updated
   *           (i.e. <code>this.</code>{@link #state} is <code>null</code>) and
   *           <code>other</code> does not contain the information (i.e.
   *           <code>other.</code>{@link #state} is <code>null</code>).
   *           </ol>
   */
  public int applyUpdate(ElementData other, boolean detailedFlags)
  {
    // if (serialNo == 25)
    // Log.debug("#" + serialNo + ": geometry is " + (geometry == null ? "NULL"
    // : "not null"));
    if (other == null)
    {
      if (geometry == null)
        throw new IllegalArgumentException("geometry required");
      if (state == null)
        throw new IllegalArgumentException("state required");
      return GEOMETRY_FLAG | ElementState.FLAG_MASK;
    }

    if (this.serialNo != other.serialNo)
      throw new IllegalArgumentException("Wrong serial numbers");

    int ret = 0;
    if (state == null)
    {
      if (other.state != null)
        state = new ElementState(other.state);
    } else
      ret |= detailedFlags ? state.getUpdateFlags(other.state) : ElementState.FLAG_MASK;

    if (geometry == null)
    {
      if (other.geometry != null)
      {
        this.geometry = new ArrayList<AGeometry>(other.geometry);
        for (AGeometry geom : this.geometry)
          if (geom instanceof AHeavyGeometry)
            ((AHeavyGeometry) geom).applyUpdate();
        this.cachedArea = other.cachedArea != null ? new Area(other.cachedArea)
            : null;
        // TODO: make a copy
      }
    } else
      ret |= GEOMETRY_FLAG;
    return ret;
  }

  /**
   * Renders the graphical representation of the {@link EElement} described by
   * this instance on a 2D graphics context.
   * 
   * @param gc
   *          The graphics context.
   * @param panelState
   *          The panel state.
   */
  public void render2D(GC gc, PanelState panelState)
  {
    // if (geometry==null) return;
    if (state == null)
    {
      Log.err("Missing state in ElementData #" + serialNo);
      return;
    }
    if (!state.isVisible())
      return;
    
    Iterator<AGeometry> geoIt = geometry.iterator();
    if (!geoIt.hasNext())
      return;

    final int fgAlpha = (int) (state.getFgAlpha() * 255);
    final int bgAlpha = (int) (state.getBgAlpha(panelState) * 255);
    final Color fgColor = state.getFgColor().getColor();
    final Color bgColor = state.getBgColor(panelState).getColor();

    // init with and render first geometry
    AGeometry gi = geoIt.next();
    boolean foreground = gi.isForeground();
    gc.setBackground(foreground ? fgColor : bgColor);
    gc.setForeground(foreground ? bgColor : fgColor);
    gc.setAlpha(foreground ? fgAlpha : bgAlpha);
    
    gi.paint2D(gc);
    
    // render all other geometry
    while (geoIt.hasNext())
    {
      gi = geoIt.next();
      if (foreground != gi.isForeground()) 
      {
        foreground = gi.isForeground();        
        gc.setBackground(foreground ? fgColor : bgColor);
        gc.setForeground(foreground ? bgColor : fgColor);        
        gc.setAlpha(foreground ? fgAlpha : bgAlpha);
      }
      gi.paint2D(gc);      
    }
  }

  /**
   * Returns the smallest rectangle that covers all geometries.
   * 
   * @return
   */
  public Rectangle getBounds()
  {
    if (geometry == null || geometry.isEmpty())
      return state.getBounds();
    Rectangle result = geometry.get(0).getBounds();
    int n = geometry.size();
    for (int i = 1; i < n; i++)
      result.add(geometry.get(i).getBounds());
    return result;
  }

  public void onVisibilityChanged(boolean visible)
  {
    for (AGeometry g : geometry)
      g.onVisibilityChanged(visible);
  }

  @Override
  public String toString()
  {
    return this.getClass().getSimpleName() + "#" + serialNo
        + (state != null && state.isVisible() ? " " : "!INVISIBLE ") + geometry;
  }

  /**
   * Indicates missing data by returning the flags {@link #GEOMETRY_FLAG} and {@link ElementState#FLAG_MASK} as xor combined integer.
   * @return
   */
  public int getMissing() {
    return geometry == null
        ? (state == null ? ElementState.FLAG_MASK | GEOMETRY_FLAG : GEOMETRY_FLAG) 
        : (state == null ? ElementState.FLAG_MASK                 : 0);
  }  
}

// EOF
