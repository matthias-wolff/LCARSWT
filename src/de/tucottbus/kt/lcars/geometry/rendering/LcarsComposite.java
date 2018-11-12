package de.tucottbus.kt.lcars.geometry.rendering;

import java.awt.Graphics2D;
import java.awt.Point;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Transform;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.jfree.experimental.swt.SWTUtils;

import de.tucottbus.kt.lcars.LCARS;
import de.tucottbus.kt.lcars.PanelData;
import de.tucottbus.kt.lcars.PanelState;
import de.tucottbus.kt.lcars.elements.ElementData;
import de.tucottbus.kt.lcars.logging.Log;

/**
 * This Class organizes the screen updates to relieve the paint process.
 * 
 * @author Christian Borck, Matthias Wolff
 */
public class LcarsComposite extends Composite implements PaintListener
{
  /**
   * If <code>true</code>, the area that would be redrawn in {@link 
   * LcarsComposite#selectiveRepaint} mode is marked in red. Note that selective 
   * repainting itself is disabled in DEBUG mode!
   */
  protected boolean DEBUG = false;

  /**
   * Enables selective rendering where only dirty regions will be updated
   */
  protected boolean selectiveRepaint = false;

  /**
   * Context for the next repaint. Contains all element and the region of the
   * screen that has to be updated.
   */
  private FrameData context;
  
  /**
   * The SWT rendering transform.
   */
  protected Transform transform;
  
  /**
   * The SWT display this composite is placed on.
   */
  private final Display display;
  
  /**
   * Creates a new LCARS composite.
   * 
   * @param parent
   *          The parent composite.
   */
  public LcarsComposite(Composite parent) 
  {
    super(parent, SWT.DOUBLE_BUFFERED|SWT.NO_BACKGROUND);    
    display = parent.getDisplay();
    transform = new Transform(display);
    addPaintListener(this);
  }

  @Override
  public void dispose()
  {
    if (transform!=null && !transform.isDisposed())
      transform.dispose();
    super.dispose();
  }

  @Override
  public org.eclipse.swt.graphics.Point computeSize(int wHint, int hHint, boolean changed)
  {
    return getShell().getSize();
  }

  /**
   * Sets a hint for selective repaints where only dirty areas on the screen
   * will be repainted. Dirty areas are defined by elements that has been added,
   * remove or changed.
   * 
   * @param selectiveRepaint
   *          The new selective repaint mode.
   */
  public void setSelectiveRenderingHint(boolean selectiveRepaint)
  {
    this.selectiveRepaint = selectiveRepaint;
  }

  /**
   * Updates the rendering data.
   * 
   * @param data
   *          The panel data update.
   * @param incremental
   *          If <code>true</code> the update data are incremental.
   */
  public void applyUpdate(PanelData data, boolean incremental)
  {
    FrameData context = FrameData.create(data, incremental, selectiveRepaint);    
    context.apply(this.context);
    synchronized (this)
    {
      this.context = context;
    }
    
    if (context.isBgChanged())
    {      
      Image bg = context.isBgChanged() ? context.getBackgroundImage().getImage() : null;
      display.asyncExec(() -> {
        setBackgroundImage(bg);
      });
    }
  }
  
  /**
   * Updates and returns the SWT rendering transform.
   */
  protected Transform updateRenderingTransform()
  {
    transform.identity();
    
    if (context!=null)
    {
      Rectangle b = getBounds();
      float scrw = b.width;
      float scrh = b.height;
      float pnlw = context.getPanelWidth();
      float pnlh = context.getPanelHeight();
      
      float scl  = Math.min(scrw/pnlw,scrh/pnlh);
      float ofsx = (scrw-pnlw*scl)/2;
      float ofsy = (scrh-pnlh*scl)/2;
      
      transform.translate(ofsx,ofsy);
      transform.scale(scl,scl);
    }
    
    return transform;
  }

  /**
   * Converts LCARS panel to LCARS composite coordinates.
   * 
   * @param pt
   *          The panel coordinates.
   * @return
   *    The composite coordinates.
   */
  public Point panelToComposite(Point pt)
  {
    float[] pointArray = new float[] {pt.x, pt.y};
    transform.transform(pointArray);
    return new Point(Math.round(pointArray[0]),Math.round(pointArray[1]));
  }

  /**
   * Converts LCARS composite to LCARS panel coordinates.
   * 
   * @param pt
   *          The composite coordinates.
   * @return
   *    The panel coordinates.
   */
  public Point compositeToPanel(Point pt)
  {
    float[] elements = new float[6];
    transform.getElements(elements);
    Transform itransform = new Transform(display,elements);
    itransform.invert();
    float[] pointArray = new float[] {pt.x, pt.y};
    itransform.transform(pointArray);
    itransform.dispose();
    return new Point(Math.round(pointArray[0]),Math.round(pointArray[1]));
  }
  
  /**
   * Paints the panel elements of this screen on a {@link Graphics2D} context. 
   * 
   * @param gc the graphics context
   * @see #elements
   */
  @Override
  public void paintControl(PaintEvent e)
  {
    GC gc = e.gc;

    Transform transform = updateRenderingTransform();
    gc.setTransform(transform);
    if (this.context==null)
      return;

    FrameData context;
    synchronized (this)
    {
      context = this.context.clone();
    }
    
    // Clipping setup
    final Rectangle dirtyArea = SWTUtils.toSwtRectangle(context.getDirtyArea().getBounds());
    final Rectangle maxArea = getBounds();

    // Erase background
    gc.setBackground(getBackground());
    if (context.getFullRepaint() || DEBUG)
    {
      gc.setTransform(null);
      gc.setClipping((Rectangle)null);
      gc.fillRectangle(0,0,maxArea.width,maxArea.height);
      gc.setTransform(transform);
    }
    else
    {
      gc.setClipping(dirtyArea);
      gc.fillRectangle(0,0,maxArea.width,maxArea.height);
    }

    // Draw elements
    PanelState state = context.getPanelState();
    for (ElementData el : context.getElementsToPaint())
    {
      if (el==null)
        continue;
      if (el.serialNo == -1)
        Log.debug(el.toString());
      try
      {
        el.render2D(gc, state);
      }
      catch (Throwable ex)
      {
        Log.err("error drawing elements to the screen", ex);
      }
    }
    
    // -- DEBUG: Show repainted area -->
    if (DEBUG)
    {
      if (context.getFullRepaint())
        gc.setClipping(0,0,context.getPanelWidth(),context.getPanelHeight());
      else
        gc.setClipping(dirtyArea);
      int alpha = gc.getAlpha();
      gc.setAlpha(64);
      gc.setBackground(LCARS.getColor(LCARS.CS_REDALERT,LCARS.EC_ELBOLO).getColor());
      gc.fillRectangle(0,0,context.getPanelWidth(),context.getPanelHeight());
      gc.setAlpha(alpha);
    }
    // <--
  }
  
  /**
   * Clears the painter and fills the screen with the default background color (
   * {@value #DEFAULT_BG_COLOR}).
   */
  public synchronized void clear()
  {
    if (LCARS.SCREEN_DEBUG)
      Log.debug("Renderer cleared");
    context = null;    
  }

}
