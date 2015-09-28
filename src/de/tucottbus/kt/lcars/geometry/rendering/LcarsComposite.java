package de.tucottbus.kt.lcars.geometry.rendering;

import java.awt.geom.Point2D;

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
 * @author Christian Borck
 *
 */
public class LcarsComposite extends Composite implements PaintListener
{  
  /**
   * Number of updates between two debug logs 
   */
  public static final int DEBUG_INTERVAL = 500; // 1500 updates ~ 60 sec

  /**
   * Count of updates between to paints
   */
  private int updateCount = 0;
  
  /**
   * 
   */
  private int paintCount = 0;
   
  /**
   * Enables selective rendering where only dirty regions will be updated
   */
  protected boolean selectiveRepaint = false;
  
  protected Point2D.Float scale = new Point2D.Float(1, 1);
  
  /**
   * Context for the next repaint. Contains all element and the region of the
   * screen that has to be updated.
   */
  private FrameData context;
  
  private final Display display;
  
  /**
   * 
   * @param initialSize
   */
  public LcarsComposite(Composite parent, int style) {
    super(parent, style);    
    display = parent.getDisplay();
    addPaintListener(this);
  }
    
  /**
   * Sets a hint for selective repaints where only dirty areas on the screen will be repainted.
   * Dirty areas are defined by elements that has been added, remove or changed. 
   * @param selectiveRepaint
   */
  public void setSelectiveRenderingHint(boolean selectiveRepaint)
  {
    this.selectiveRepaint = selectiveRepaint;
  }

  /**
   * Updates the data for rendering.
   * @param data
   * @param incremental
   */
  public void applyUpdate(PanelData data, boolean incremental)
  {
    FrameData nextContext;
    synchronized (this)
    {
      nextContext = FrameData.create(data, incremental, this.selectiveRepaint);    
      nextContext.apply(context);
      context = nextContext;
    }
    
    Image bg = nextContext.isBgChanged() ? nextContext.getBackgroundImage().getImage() : null;
    display.asyncExec(() -> {
      if (nextContext.isBgChanged())
        setBackgroundImage(bg);
    });
    onUpdate();
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
    onPaint();
    FrameData context = this.context;
    if (context == null) // null -> reset
    {
      if (scale.x != 1 || scale.y != 1)
        scale = new Point2D.Float(1, 1);
      return;
    }
    
    Rectangle b = getBounds();
    float sx = (float)b.width /context.getRenderWidth();
    float sy = (float)b.height/context.getRenderHeight();
    if (scale.x != sx || scale.y != sy)
      scale = new Point2D.Float(sx, sy);    
    
    Transform t = new Transform(display, sx, 0, 0, sy, 0, 0);
    gc.setTransform(t);
    t.dispose();
    
    // clipping setup
    final Rectangle dirtyArea = SWTUtils.toSwtRectangle(context.getDirtyArea().getBounds());
       
    if(context.getFullRepaint())
      gc.setClipping(0,0,getBounds().width, getBounds().height);
    else
      gc.setClipping(dirtyArea);

    PanelState state = context.getPanelState();
    try
    {
      for (ElementData el : context.getElementsToPaint())
        el.render2D(gc, state);      
    } catch (Throwable ex)
    {
      Log.err("error drawing elements to the screen", ex);
    }
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

  public Point2D.Float getScale() {
    Point2D.Float scale = this.scale;
    return new Point2D.Float(scale.x, scale.y);
  }
  
  /**
   * Checks paint count and 
   */
  private void onPaint() {
    if(++this.paintCount == DEBUG_INTERVAL) {
      int updateCount = this.updateCount;
      int paintCount = this.paintCount;
      this.paintCount = this.updateCount = 0;
      if(updateCount > paintCount) {
        int skipped = updateCount-paintCount;
        Log.debug(skipped + " of " + updateCount +  " Frame(s) skipped (" + String.format("%.2f", skipped*100f/updateCount) + "%)");        
      }         
    }
  }
  
  /**
   * Increments the update counter
   */
  protected void onUpdate() {
    this.updateCount++;
  }  
}
