package de.tucottbus.kt.lcars.j2d.rendering;

import java.awt.Dimension;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.jfree.experimental.swt.SWTUtils;

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
public class Renderer
{
  /**
   * The Default background color is black.
   */
  public final Color DEFAULT_BG_COLOR;
  
  
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
  
  /**
   * Context for the next repaint. Contains all element and the region of the
   * screen that has to be updated.
   */
  private FrameData context;
  
  private final org.eclipse.swt.graphics.Rectangle initialBounds;
  
  /**
   * 
   * @param initialSize
   */
  public Renderer(Device device, int initialWidth, int initialHeight) {
    this.initialBounds = new org.eclipse.swt.graphics.Rectangle(0, 0, initialWidth, initialHeight);
    DEFAULT_BG_COLOR = device.getSystemColor(SWT.COLOR_BLACK);
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
   * Returns the dimension defined by the panel data. If panel data is not set,
   * it returns null.
   * 
   * @return
   */
  public Dimension getDimension()
  {
    FrameData fd = context;
    return fd != null ? fd.getRenderSize() : new Dimension(initialBounds.width, initialBounds.height);
  }

  /**
   * Updates the data for rendering.
   * @param data
   * @param incremental
   */
  public void applyUpdate(PanelData data, boolean incremental) {
    synchronized (this)
    {
      FrameData nextContext = FrameData.create(data, incremental, this.selectiveRepaint);    
      nextContext.apply(context);      
      context = nextContext;      
    }
    onUpdate();
  }
  
  /**
   * Paints the panel elements of this screen on a {@link Graphics2D} context. 
   * 
   * @param gc the graphics context
   * @see #elements
   */
  public void paint2D(GC gc)
  {
    onPaint();
    FrameData context = this.context;
    if (context == null) // null stands for reset
    {
      gc.setClipping(initialBounds);
      gc.setBackground(DEFAULT_BG_COLOR);
      gc.drawRectangle(initialBounds);
      return;
    }
    
    // clipping setup
    org.eclipse.swt.graphics.Rectangle dirtyArea = SWTUtils.toSwtRectangle(context.getDirtyArea().getBounds());
    if(context.getFullRepaint()) {
      Dimension size = context.getRenderSize();
      gc.setClipping(0,0,size.width,size.height);
    }
    else
      gc.setClipping(dirtyArea);

    // background setup
    ImageData bgImg = context.getBackgroundImage();
    if (bgImg == null)
    {
      gc.setBackground(DEFAULT_BG_COLOR);
      gc.fillRectangle(dirtyArea);
    } else {
      Image img = new Image(gc.getDevice(), bgImg);
      gc.drawImage(img, 0, 0);
    }
    // TODO possible problem with clipping when drawing

    PanelState state = context.getPanelState();

    // GImage.beginCacheRun();
    try
    {
      for (ElementData el : context.getElementsToPaint())
        el.render2D(gc, state);
      
//      gc.setForeground(gc.getDevice().getSystemColor(SWT.COLOR_RED));
//      for (ElementData el : context.getElementsToPaint())
//        gc.drawRectangle(SWTUtils.toSwtRectangle(el.getBounds()));
      
      // LCARS.log(CLASSKEY, context.getElementsToPaint().size() +
      // " elements are rendered");
    } catch (Throwable e)
    {
      Log.err("error drawing elements to the screen", e);
    }
    // GImage.endCacheRun();

    // g2d.setColor(Color.red);
    // g2d.draw(dirtyArea);
  }

  /**
   * Clears the painter and fills the screen with the default background color (
   * {@value #DEFAULT_BG_COLOR}).
   */
  public synchronized void clear()
  {
    Log.info("Renderer cleared");
    context = null;    
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
  
  protected void finalize() {
    DEFAULT_BG_COLOR.dispose();
  }
}
