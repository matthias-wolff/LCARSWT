package de.tucottbus.kt.lcars.elements;

import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Area;
import java.util.ArrayList;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

import org.jfree.util.Log;

import de.tucottbus.kt.lcars.LCARS;
import de.tucottbus.kt.lcars.Panel;
import de.tucottbus.kt.lcars.PanelState;
import de.tucottbus.kt.lcars.Screen;
import de.tucottbus.kt.lcars.elements.modify.EGeometryModifier;
import de.tucottbus.kt.lcars.feedback.UserFeedback;
import de.tucottbus.kt.lcars.geometry.AGeometry;
import de.tucottbus.kt.lcars.geometry.GArea;
import de.tucottbus.kt.lcars.swt.AwtSwt;
import de.tucottbus.kt.lcars.swt.ColorMeta;
import de.tucottbus.kt.lcars.util.Objectt;

/**
 * The base class of all LCARS GUI element (i.e. buttons, elbos, etc.)
 * 
 * @author Matthias Wolff
 */
public abstract class EElement
{
  public static final int INV_BACKGR    = 0x0001;
  public static final int INV_FOREGR    = 0x0002;
  public static final int INV_SHAPE     = 0x0004;
  
  /** indicates a recompute of the geometries at the next cycle **/
  public static final int GEO_RECOMPUTE = 0x0010;
  
  /** indicates that the geometries has been updated **/
  public static final int GEO_UPDATED   = 0x0020;
  
  // -- Static fields --
  private static final AtomicLong serialNumber     = new AtomicLong(0);

  // -- Fields --
  ElementData data;
  
  // -- Transient fields --
  protected transient String                    label      = null;
  private   transient int                       holdf      = 50;
  private   transient EEvent                    holde      = null;
  private   transient HoldThread                holdt      = null;
  protected transient Vector<EEventListener>    tlist      = new Vector<EEventListener>(); 
  protected transient Vector<EGeometryModifier> modifiers  = new Vector<EGeometryModifier>(); 
  private   transient Object                    userData   = null;
  private   transient int                       geoState   = GEO_RECOMPUTE;
  private   final transient long                serialNo;
  
  // -- Constructors --

  /**
   * Invoked by the constructors of derived classes.
   *  
   * @param panel
   *          The LCARS panel to place the GUI element on.
   * @param x
   *          The x-coordinate of the upper left corner (in LCARS panel pixels).
   * @param y
   *          The y-coordinate of the upper left corner (in LCARS panel pixels).
   * @param w
   *          The width (in LCARS panel pixels).
   * @param h
   *          The height (in LCARS panel pixels).
   * @param style
   *          The style (see class {@link LCARS}).
   * @param label
   *          The label.
   */
  public EElement(Panel panel, int x, int y, int w, int h, int style, String label)
  {
    this.panel         = panel;
    this.label         = label;
    this.serialNo      = serialNumber.getAndIncrement();
    this.data          = new ElementData(this, new Rectangle(x,y,w,h),style);
  }
  
  // -- Geometry getters and setters --
  
  /**
   * Returns the {@link Area area} covered by all background geometries.
   */
  public void getArea(Area area)
  {
    validateGeometry();
    data.getArea(area);
  }
  
  /**
   * Returns the rectangular bounds of this LCARS GUI element.
   * 
   * @see #setBounds(Rectangle)
   */
  public Rectangle getBounds()
  {
    return data.state.getBounds();
  }

  /**
   * Sets the bounding rectangle of this LCARS GUI element.
   * 
   * @param bounds
   *          The new bounding rectangle in LCARS panel coordinates.
   * @see #getBounds()
   */
  public void setBounds(Rectangle bounds)
  {
    synchronized (data)
    {
      //if(Objectt.equals(bounds, data.state.getBounds())) return;
      data.state.setBounds(bounds);
      invalidate(true);
    }    
  }
  
  // -- Color getters and setters --
  
  /**
   * Sets the color of this element. This supersedes the color specified through the style. To
   * restore the natural color of the element, call this method with <code>color=null</code>.
   * 
   * @param color
   *          The color, can be <code>null</code>.
   * @see #setColorStyle(int)
   * @see #getColor()
   * @see #getBgColor()
   */
  public void setColor(ColorMeta color)
  {
    if (de.tucottbus.kt.lcars.util.Objectt.equals(color, data.state.getColor())) return;
    data.state.setColor(color);
    invalidate(false);
  }

  /**
   * Sets the color of this element. This supersedes the color specified through the style. To
   * restore the natural color of the element, call this method with <code>color=null</code>.
   * 
   * @param color
   *          The color, can be <code>null</code>.
   * @see #setColorStyle(int)
   * @see #getColor()
   * @see #getBgColor()
   */
  @Deprecated
  public void setColor(Color color)
  {
    ColorMeta dsc = data.state.getColor();
    if(color == null) {
      if (dsc == null) return;
      data.state.setColor(null);        
    }
    else {
      ColorMeta ndsc = AwtSwt.toSwtColor(color);
      if (ndsc.equals(dsc)) return;
      data.state.setColor(ndsc);
    }
    invalidate(false);
  }

  /**
   * Returns the custom color assigned to this LCARS GUI element by the last call of the
   * {@link #setColor(Color)} method.
   * <p>
   * Note: If this element displays its natural color (i.e. the color determined by its style), the
   * return value is <code>null</code>. Use {@link #data}.{@link ElementState#getBgColor(PanelState)
   * getBgColor(PanelState)} to determine the current background color anyway.
   * </p>
   * 
   * @see #getColorStyle()
   * @see #setColor(Color)
   */
  public ColorMeta getColor()
  {
    return data.state.getColor();
  }
  
  /**
   * @deprecated 
   */
  public ColorMeta getBgColor()
  {
    return data.state.getBgColor(new PanelState(null));
  }

  /**
   * Returns the custom opacity of background {@linkplain AGeometry geometries}.
   * 
   * @see #setAlpha(float)
   */
  public float getAlpha()
  {
    return data.state.getAlpha();
  }

  /**
   * Sets opacity of the element's background geometries.
   * 
   * @param alpha
   *          The new opacity (0: transparent ... 1: opaque).
   * @see #getAlpha()
   */
  public void setAlpha(float alpha)
  {
    if (alpha<0) alpha=0;
    if (alpha>1) alpha=1;
    if (data.state.getAlpha()==alpha) return;
    data.state.setAlpha(alpha);
    invalidate(false);
  }

  //-- Style getters and setters --
  
  /**
   * Returns the style of this LCARS GUI element.
   * 
   * @see #setStyle(int)
   */
  public int getStyle()
  {
    return data.state.getStyle();
  }
  
  /**
   * Sets the style of this LCARS GUI element.
   * 
   * @param style
   *          The new style.
   * @see #getStyle()          
   */
  public void setStyle(int style)
  {
    data.state.setStyle(style);
    invalidate(false);
  }

  /**
   * Determines if this LCARS GUI element is blinking.
   */
  public boolean isBlinking()
  {
    return data.state.getStyle(LCARS.ES_BLINKING)!=0;
  }

  /**
   * Sets this LCARS GUI element selected.
   * 
   * @param blinking
   *          <code>true</code> to set the element selected, <code>false</code>
   *          to set it unselected
   */
  public void setBlinking(boolean blinking)
  {
    if (isBlinking()==blinking) return;
    data.state.setStyle(LCARS.ES_BLINKING,blinking);
    invalidate(false);
  }
  
  /**
   * Sets the color style of this LCARS GUI element.
   * 
   * @param style
   *          One of the {@link LCARS}<code>.EC_XXX</code> constants.
   * @see #setColor(Color)
   * @see #getColorStyle()
   */
  public void setColorStyle(int style)
  {
    int s = (data.state.getStyle()&~LCARS.ES_STYLE) | (style&LCARS.ES_STYLE);
    if (s==data.state.getStyle()) return;
    data.state.setStyle(s);
    invalidate(false);
  }
  
  /**
   * Determines the color style of this LCARS GUI element.
   * 
   * @return One of the {@link LCARS}<code>.EC_XXX</code> constants.
   * @see #getColor()
   * @see #getColorStyle()
   */
  public int getColorStyle()
  {
    return data.state.getStyle(LCARS.ES_STYLE);
  }

  /**
   * Determines if this LCARS GUI element is disabled. Disabled elements are
   * displayed grayed or dimmed and do temporarily not accept user input.
   */
  public boolean isDisabled()
  {
    return data.state.getStyle(LCARS.ES_DISABLED)!=0;
  }

  /**
   * Sets this LCARS GUI disabled. Disabled elements are displayed grayed or 
   * dimmed and do temporarily not accept user input.
   * 
   * @param disabled
   *          <code>true</code> to set the element disabled, <code>false</code>
   *          to set it enabled
   */
  public void setDisabled(boolean disabled)
  {
    if (disabled)
    {
      clearTouch();
      holde=null;
    }
    if (isDisabled()==disabled) return;
    data.state.setStyle(LCARS.ES_DISABLED,disabled);
    invalidate(false);
  }

  /**
   * Determines if this LCARS GUI element is outline style. In this style background geometries are
   * painted as outlines rather than as filled shapes.
   */
  public boolean isOutline()
  {
    return data.state.getStyle(LCARS.ES_OUTLINE)!=0;
  }

  /**
   * Sets the outline style of this LCARS GUI element. In this style background geometries are
   * painted as outlines rather than as filled shapes.
   * 
   * @param outline
   *          The new outline style.
   */
  public void setOutline(boolean outline)
  {
    if (isOutline()==outline) return;
    data.state.setStyle(LCARS.ES_OUTLINE,outline);
    invalidate(false);
  }

  /**
   * Determines if this element realizes the over-drag behavior. Over-dragging means that the
   * element will keep firing {@link EEvent#TOUCH_DRAG} events until the touch panel is released
   * even if the touched position has moved outside the element's shape.
   * 
   * @see LCARS#EB_OVERDRAG
   */
  public boolean isOverDrag()
  {
     return data.state.getStyle(LCARS.EB_OVERDRAG)!=0;
  }
  
  /**
   * Set the over-drag style (for explanation see {@link #isOverDrag()}). 
   * 
   * @param overdrag
   *          The new over-drag style.
   */
  public void setOverdrag(boolean overdrag)
  {
    data.state.setStyle(LCARS.EB_OVERDRAG);
    invalidate(false);
  }
  
  /**
   * Determines if this LCARS GUI element is selected.
   */
  public boolean isSelected()
  {
    return data.state.getStyle(LCARS.ES_SELECTED)!=0;
  }

  /**
   * Sets this LCARS GUI element selected.
   * 
   * @param selected
   *          <code>true</code> to set the element selected, <code>false</code>
   *          to set it unselected
   */
  public void setSelected(boolean selected)
  {
    if (isSelected()==selected) return;
    data.state.setStyle(LCARS.ES_SELECTED,selected);
    invalidate(false);
  }

  /**
   * Determines if this LCARS GUI element is silent. Silent elements do not
   * play an earcon when touched.
   */
  public boolean isSilent()
  {
    return data.state.getStyle(LCARS.ES_SILENT)!=0;
  }
  
  /**
   * Sets the silent property of this LCARS GUI element. Silent elements do not
   * play an earcon when touched.
   * 
   * @param silent
   *          <code>true</code> to make the element silent, <code>false</code>
   *          otherwise
   */
  public void setSilent(boolean silent)
  {
    data.state.setStyle(LCARS.ES_SILENT,silent);
  }
  
  /**
   * Determines if this LCARS GUI element is static. Static elements do not
   * accept user input.
   */
  public boolean isStatic()
  {
    return data.state.getStyle(LCARS.ES_STATIC)!=0;
  }

  /**
   * Determines if this LCARS GUI element is modal. Modal elements remain active
   * in the modal {@linkplain Panel panel} state.
   * 
   * @see Panel#isModal()
   * @see Panel#setModal(boolean)
   */
  public boolean isModal()
  {
    return data.state.getStyle(LCARS.ES_MODAL)!=0;
  }
  
  /**
   * Sets the static property of this LCARS GUI element. Static elements do not
   * accept user input.
   * 
   * @param stat
   *          <code>true</code> to make the element static, <code>false</code>
   *          otherwise
   */
  public void setStatic(boolean stat)
  {
    data.state.setStyle(LCARS.ES_STATIC,stat);
  }  

  // -- State getters and setters --  

  /**
   * Determines if this LCARS GUI element is highlighted. Highlighted elements
   * are painted with white background color.
   */
  public boolean isHighlighted()
  {
    return data.state.isHighlighted();
  }
  
  /**
   * Sets this LCARS GUI element highlighted. Highlighted elements are painted
   * with white background color.
   * 
   * @param highlighted
   *          <code>true</code> to highlight, <code>false</code> to clear
   */
  public void setHighlighted(boolean highlighted)
  {
    data.state.setHighlighted(highlighted);
    invalidate(false);
  }
  
  /**
   * Clears the touched flag. For use by the framework only!
   */
  public void clearTouch()
  {
    data.state.setTouch(0x00000000);
  }
  
  /**
   * Determines if this LCARS GUI element is visible.
   */
  public boolean isVisible()
  {
    return data.state.isVisible();
  }
  
  /**
   * Set this LCARS GUI element visible or invisible.
   * 
   * @param visible
   *          The new visibility.
   */
  public void setVisible(boolean visible)
  {
    data.state.setVisible(visible);
    invalidate(false);
  }

  // -- Other getters and setters --

  /**
   * Returns a copy of the serializable data of this LCARS GUI element which is suitable for a
   * {@linkplain Screen screen} update. 
   * 
   * @param incremental
   *          Get data for incremental or full update.
   */
  public synchronized ElementData getUpdateData(boolean incremental)
  {
    boolean updateGeometry = (validateGeometry() & GEO_UPDATED) != 0;
    geoState &= ~GEO_UPDATED;
    return data.getUpdate(incremental,updateGeometry);
  }
  
  /**
   * Retrieves the user defined data associated with this LCARS GUI element.
   * 
   * @see #setData(Object)
   * @return the data
   */
  public Object getData()
  {
    return this.userData;
  }

  /**
   * Associates user defined data with this LCARS GUI element.
   * 
   * @see #getData()
   * @param data the data
   */
  public void setData(Object data)
  {
    this.userData = data;
  }
  
  /**
   * Returns the label of this LCARS GUI element.
   * 
   * @see #setLabel(String)
   */
  public String getLabel()
  {
    return this.label;
  }

  /**
   * Sets a new label of this LCARS GUI element.
   * 
   * @param label
   *          The label.
   * @see #getLabel()
   */
  public void setLabel(String label)
  {
    if (Objectt.equals(this.label, label)) return;
    this.label = label;
    invalidate(true);
  }

  /**
   * Sets the firing interval of {@link EEvent#TOUCH_HOLD} events. The standard
   * interval is 50 ms.
   * 
   * @param time
   *          the firing interval (in ms); 0 or a negative value prevents the
   *          LCARS GUI element from firing hold events
   */
  public void setHoldTime(int time)
  {
    this.holdf = time;
  }

  // -- LCARS event dispatching --

  /**
   * Adds a new listener for {@link EEvent}s occurring in this element.
   *   
   * @param listener the listener
   */
  public final void addEEventListener(EEventListener listener)
  {
    tlist.add(listener);
  }
  
  /**
   * Removes a listener for {@link EEvent}s occurring in this element.
   * 
   * @param listener the listener
   */
  public final void removeEEventListener(EEventListener listener)
  {
    tlist.remove(listener);
  }
  
  /**
   * Removes all listeners for {@link EEvent}s occurring in this element.
   */
  public final void removeAllEEventListeners()
  {
    tlist.clear();
  }

  /**
   * Dispatches an {@link EEvent} to all registered listeners
   * 
   * @param ee
   *          The event.
   * @return The appropriate user feedback type.
   */
  public final UserFeedback.Type fireEEvent(EEvent ee)
  {
    UserFeedback.Type fbt = ee.id==EEvent.TOUCH_DOWN?UserFeedback.Type.DENY:UserFeedback.Type.NONE;
    if (isDisabled()) return fbt;
    if (isStatic  ()) return fbt;
    if (!isVisible()) return fbt;

    // Modal mode
    if (panel!=null && panel.isModal() && data.state.getStyle(LCARS.ES_MODAL)==0)
      return fbt;

    // Locked state
    if (panel!=null)
    {
      if (panel.isLocked() && data.state.getStyle(LCARS.ES_MODAL|LCARS.ES_NOLOCK)==0)
        return fbt;
      panel.breakAutoRelock();
    }
    
    // Highlight and hold control
    switch (ee.id)
    {
    case EEvent.TOUCH_DOWN:
      data.state.setTouch(LCARS.ES_SELECTED);
      invalidate(false);
      runHoldThread(ee);
      fbt = UserFeedback.Type.TOUCH;
      break;
    case EEvent.TOUCH_UP:
      data.state.setTouch(0x00000000);
      invalidate(false);
      runHoldThread(null);
      break;
    case EEvent.TOUCH_DRAG:
      //runHoldThread(ee);
      break;
    }
    
    // Dispatch event
    for (int i=0; i<tlist.size(); i++)
      switch (ee.id)
      {
      case EEvent.TOUCH_DOWN: tlist.get(i).touchDown(ee); break;
      case EEvent.TOUCH_UP  : tlist.get(i).touchUp  (ee); break;
      case EEvent.TOUCH_DRAG: tlist.get(i).touchDrag(ee); break;
      case EEvent.TOUCH_HOLD: tlist.get(i).touchHold(ee); break;
      }
    
    return fbt;
  }
  
  /**
   * Re-fires an {@link EEvent} as long as the user touches ("holds down") this
   * element. 
   * 
   * @param ee the event 
   */
  private void runHoldThread(EEvent ee)
  {
    holde = (ee==null ? null : EEvent.fromEEvent(ee));
    if ((holde==null) || (holdf<=0)) return;
    holde.id = EEvent.TOUCH_HOLD;
    holde.ct = 0;
    if (holdt!=null && holdt.isAlive()) return;
    holdt = new HoldThread();
    holdt.start();
  }

  /**
   * A thread firing touch events when the element is "held down" (i. e. touched for a longer time).
   */
  class HoldThread extends Thread
  {
    public HoldThread()
    {
      super(HoldThread.class.getSimpleName() + " of EElement#" + data.serialNo);
      setDaemon(true);
    }

    @Override
    public void run()
    {
      while (holde!=null)
      {
        try { Thread.sleep(holdf); } catch (InterruptedException e) {}
        if (holde!=null)
        {
          holde.ct++;
          fireEEvent(holde);
        }
      }
    }
  }
  
  // -- Geometry and painting --

  /**
   * Registers a geometry modifier with this <code>EElement</code>.
   *  
   * @param gm the modifier
   */
  public void addGeometryModifier(EGeometryModifier gm)
  {
    modifiers.add(gm);
  }

  /**
   * Unregisters a geometry modifier with this <code>EElement</code>.
   *  
   * @param gm the modifier
   */
  public void removeGeometryModifier(EGeometryModifier gm)
  {
    modifiers.remove(gm);
  }
  
  /**
   * Computes the insets of the label text from the GUI element's bounding
   * rectangle(!). Derived classes may override this method. The returned insets
   * may depend on the label position flags {@link LCARS}.<code>ES_LABEL_XXX</code>
   * contained in the {@link #data} field.
   * 
   * @return the insets
   */
  // TODO: Move to Geometry?
  protected Point computeLabelInsets()
  {
    //Dimension pd = panel.getDimension();
    //int ins = Math.max(pd.width,pd.height)/200;
    return new Point(10,10);
  }
  
  /**
   * Translates panel to element coordinates (both logical LCARS coordinates).
   * 
   * @param pt the panel coordinates
   * @return the coordinates relative to this LCARS GUI element's top left
   *         corner
   */
  public Point panelToElement(Point pt)
  {
    Rectangle bounds = getBounds();
    return new Point(pt.x-bounds.x,pt.y-bounds.y);
  }

  /**
   * Called to (re)compute the elements geometries.
   * 
   * @return The geometries.
   */
  //TODO: replace Vector with ArrayList 
  protected abstract ArrayList<AGeometry> createGeometriesInt();
    
  /**
   * Marks the GUI representation of element invalid. 
   * 
   * @param geometryChanged
   *          If <code>true</code>, the geometry will be recomputed
   * @see #validateGeometry()
   */
  public final void invalidate(boolean geometryChanged)
  {    
    if (geometryChanged)
      geoState |= GEO_RECOMPUTE;
    if (panel!=null) panel.invalidate();
  }

  /**
   * Recomputes the {@link ElementData#geometry geometry} if necessary. If the geometry is still valid.
   * 
   * @return the current geoState
   *
   * @see #invalidate(boolean)
   */
  public final int validateGeometry()
  {
    synchronized (data)
    {
      int geoState = this.geoState;
      if ((geoState & GEO_RECOMPUTE) == 0) return geoState;// Unnecessary!
      ArrayList<AGeometry> geos = createGeometriesInt();
      final boolean isOutline = isOutline();
      for (AGeometry geo : geos)
        if (geo instanceof GArea)
          ((GArea)geo).setOutline(isOutline);
      for (EGeometryModifier gm : modifiers)
        gm.modify(geos);
      data.updateGeometries(geos);
      return this.geoState = GEO_UPDATED;
    }
  }
  
  // -- Deprecation candidates --
  
  /**
   * The panel this element is displayed on, <code>null</code> if the element is currently not displayed.
   */
  protected transient Panel panel = null;

  /**
   * Returns the LCARS {@link Panel} this element is displayed on. If the element is currently not
   * displayed, the method returns <code>null</code>.
   * 
   * @return The panel or <code>null</code>.
   * @see #setPanel(Panel)
   */
  public Panel getPanel()
  {
    return panel;
  }

  /**
   * Sets the panel on which this LCARS GUI element is displayed.
   * 
   * @param panel
   *          The panel, can be <code>null</code> if the element is not displayed any longer.
   * @see #getPanel()
   */
  public synchronized void setPanel(Panel panel)
  {
    if (this.panel == panel) return;
    this.panel = panel;
    if (panel == null) return;
    data.state.setChanged();
    geoState |= GEO_RECOMPUTE;
  }
  
  @Override
  public String toString() {
    Rectangle b = data.state.getBounds();
    return this.getClass().getSimpleName()+"#"+data.serialNo
        + (b != null ? " bounds=("+b.x + ","+b.y + "," + b.width + "," + b.height + ")" : "")
        + (label != null ? " label=\""+label + "\"" : "");
  }
  
  public boolean checkValidation() {
    Boolean[] valid = {true};
    Consumer<String> invalid = (msg) -> {
      Log.warn(toString()+": "+msg);
      valid[0] = false;
    };
    
    if (panel == null)
      invalid.accept("panel == null");
    
    if (data == null)
      invalid.accept("data == null");
    else
      if (data.geometry == null)
        invalid.accept("data.geometry == null");
      else {
        int i = 0;
        for(AGeometry g : data.geometry) {
          if (g == null)
            invalid.accept("geometry["+i+"] == null");   
          i++;
        }
        if (data.state == null)
          invalid.accept("data.state == null");
      }
    return valid[0];   
  }
  
  public final long getSerialNo() {
    return serialNo;
  }
}

// EOF
