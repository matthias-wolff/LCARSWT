package de.tucottbus.kt.lcars.elements;

import java.awt.Rectangle;
import java.awt.geom.Area;
import java.io.Serializable;
import java.util.ArrayList;

import org.eclipse.swt.graphics.GC;

import de.tucottbus.kt.lcars.PanelState;
import de.tucottbus.kt.lcars.Screen;
import de.tucottbus.kt.lcars.j2d.AHeavyGeometry;
import de.tucottbus.kt.lcars.j2d.ElementState;
import de.tucottbus.kt.lcars.j2d.Geometry;
import de.tucottbus.kt.lcars.logging.Log;

/**
 * The serializable data of an {@linkplain EElement LCARS GUI element}. An
 * element data instance stores the {@link #state} and the {@link #geometry} of
 * a GUI element. Element data instances are also used to transfer rendering
 * information from the {@linkplain Panel LCARS panel} to the
 * {@linkplain Screen screen}.
 * 
 * @author Matthias Wolff, Christian Borck
 */
public final class ElementData implements Serializable
{
  // -- Constants
  
  public static final String CLASSKEY = "ElementData";

  /**
   * Bit in the return value of {@link #applyUpdate(ElementData)} indicating
   * that the {@linkplain #state state} has been updated.
   */
  public static final int STATE_FLAG = 0x00000001;

  /**
   * Bit in the return value of {@link #applyUpdate(ElementData)} indicating
   * that the {@linkplain #geometry geometry} has been updated.
   */
  public static final int GEOMETRY_FLAG = 0x00000002;

  /**
   * Bits that indicates that all has changed.
   */
  public static final int ALL_FLAG = STATE_FLAG | GEOMETRY_FLAG;

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
  public ArrayList<Geometry> geometry;

  /**
   * Rendering hints for screen
   */
  public int updateFlag = 0;

  /**
   * Bounds of the area of this element.
   */
  private transient org.eclipse.swt.graphics.Rectangle bounds;
  
  /**
   * Bounds of the area of this element.
   */
  private transient Area area;

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
    this.geometry = new ArrayList<Geometry>(element.data.geometry);
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
  public Area getArea()
  {
    if (area == null)
      try
      {
        Area area = new Area();
        for (Geometry gi : geometry)
          if (!gi.isForeground())
            area.add(gi.getArea());
        this.area = area;
      } catch (NullPointerException e)
      {
        area = new Area(state.getBounds());
      }      
    return area;
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
        other.geometry = new ArrayList<Geometry>(this.geometry.size());
        for(Geometry geom : this.geometry)
          other.geometry.add(geom instanceof AHeavyGeometry
              ? ((AHeavyGeometry)geom).getUpdate(incremental)
              : geom);
      } catch (Exception e)
      {
        // TODO: synchronization problem, exception should never occur
        other.geometry = new ArrayList<Geometry>();
        Log.err("Error while extracting updated data from ElementData with #" + serialNo, e);
      }

    state.clearChanged();
    other.updateFlag = (stateChanged ? STATE_FLAG : 0)
        | (updateGeometry ? GEOMETRY_FLAG : 0);
    return other;
  }

  /**
   * Updates this instance with information taken from another one.
   * 
   * @param other
   *          The other instance.
   * @return A combination of {@link #STATE_FLAG} and {@link #GEOMETRY_FLAG}
   *         indicating the which data have actually been updated. A return
   *         value of 0 indicates that no updates have been performed.
   * @throws IllegalArgumentException
   *           <ol>
   *           <li>If the serial number of <code>other</code> is not equal to
   *           the serial number of <code>this</code> (i.e. the two instances
   *           describe different {@linkplain EElement LCARS GUI elements}).</li>
   *           <li>If the {@link #state} of this instance needs to be updated
   *           (i.e. <code>this.</code>{@link #state} is <code>null</code>) and
   *           <code>other</code> does not contain the information (i.e.
   *           <code>other.</code>{@link #state} is <code>null</code>).
   *           </ol>
   */
  public int applyUpdate(ElementData other)
  {
    if (this.serialNo != other.serialNo)
      throw new IllegalArgumentException("Wrong serial numbers");

    int ret = 0;
    if (this.state == null)
    {
      if (other.state != null)
        this.state = new ElementState(other.state);
    } else
      ret |= STATE_FLAG;
    if (this.geometry == null)
    {
      if (other.geometry != null)
      {
        this.geometry = new ArrayList<Geometry>(other.geometry);
        for(Geometry geom : this.geometry)
          if (geom instanceof AHeavyGeometry)
            ((AHeavyGeometry) geom).applyUpdate();        
        this.bounds = other.bounds;
        this.area = other.area;
        //TODO: make 
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
    if (state==null) {
      Log.err("Missing state in ElementData #"+serialNo);
      return;
    }
    if (!state.isVisible())
      return;

    int i = 0;
    int l = geometry.size();

    if (l <= 0)
      return;
    
    Geometry gi = geometry.get(i++);
    if (!gi.isForeground())
    {
      state.getBgColor(panelState).applyBackground(gc);
      gc.setAlpha((int)(state.getBgAlpha(panelState)*255));
      
      //TODO: set alpha composite?
      //render background elements
//      gc.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
//          state.getBgAlpha(panelState)));
      while(true) {
        gi.paint2D(gc);
        if(i==l)
          return;
        gi = geometry.get(i++);
        if(gi.isForeground())
          break;
      }
    }
    
    //TODO: set alpha composite?
    // render foreground elements
    // gc.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, state.getFgAlpha()));
    state.getFgColor().applyForeground(gc);
    gc.setAlpha((int)(state.getFgAlpha()*255));
    while(true) {
      gi.paint2D(gc);
      if(i==l)
        return;
      gi = geometry.get(i++);
    }    
  }

  /**
   * Returns the
   * 
   * @return
   */
  public org.eclipse.swt.graphics.Rectangle getBounds()
  {
    if (bounds == null)
    {
      Area area = new Area();
      for (Geometry gi : geometry)
        area.add(gi.getArea());
      Rectangle rect = area.getBounds();
      area.reset();
      bounds = new org.eclipse.swt.graphics.Rectangle(rect.x, rect.y, rect.width, rect.height);
    }

    return new org.eclipse.swt.graphics.Rectangle(bounds.x, bounds.y, bounds.width, bounds.height); 
  }

  /**
   * Returns a flags that indicates which data is currently missing
   * 
   * @return
   */
  public int getMissingFlag()
  {
    return (this.state == null ? STATE_FLAG : 0)
        | (this.geometry == null ? GEOMETRY_FLAG : 0);
  }
  
  public void onAddToScreen() {
    for(Geometry g : geometry)
      if (g instanceof AHeavyGeometry)
        ((AHeavyGeometry)g).onAddToScreen();
  }
  
  public void onRemoveFromScreen() {
    for(Geometry g : geometry)
      if (g instanceof AHeavyGeometry)
        ((AHeavyGeometry)g).onRemoveFromScreen();
  }
  
}

// EOF
