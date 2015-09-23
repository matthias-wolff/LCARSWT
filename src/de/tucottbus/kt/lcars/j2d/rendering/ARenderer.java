package de.tucottbus.kt.lcars.j2d.rendering;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;

import org.eclipse.swt.SWT;
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
public abstract class ARenderer
{
  /**
   * The Default background color is black.
   */
  public static final Color DEFAULT_BG_COLOR = Color.BLACK;
  
  
  /**
   * Number of updates between two debug logs 
   */
  public static final int DEBUG_INTERVAL = 500; // 1500 updates ~ 60 sec

  /**
   * Current size of the parent screen.
   */
  protected Dimension size;


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

  
  /**
   * 
   * @param initialSize
   */
  public ARenderer(Dimension initialSize) {
    if(initialSize == null)
      throw new NullPointerException("initialSize");
    
    this.size = initialSize;
  }
  
  /**
   * 
   * @param renderer
   */
  public ARenderer(ARenderer renderer) {
    this.size = renderer.size;
    this.context = renderer.context;
    this.selectiveRepaint = renderer.selectiveRepaint;
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
    return new Dimension(size);
  }

  /**
   * Updates the data for rendering.
   * @param data
   * @param incremental
   */
  public abstract void applyUpdate(PanelData data, boolean incremental);
  
  /**
   * Paints the panel elements of this screen on a {@link Graphics2D} context. 
   * 
   * @param gc the graphics context
   * @see #elements
   */
  public void paint2D(GC gc)
  {
    onPaint();
    FrameData context = getContext();
    if (context == null) // null stands for reset
    {
      gc.setClipping(0,0,size.width,size.height);
      gc.setBackground(gc.getDevice().getSystemColor(SWT.COLOR_BLACK));
      gc.drawRectangle(SWTUtils.toSwtRectangle(new Rectangle(size)));
      return;
    }

    // clipping setup
    org.eclipse.swt.graphics.Rectangle dirtyArea = SWTUtils.toSwtRectangle(context.getDirtyArea().getBounds());
    if(context.getFullRepaint())
      gc.setClipping(0,0,size.width,size.height);
    else
      gc.setClipping(dirtyArea);

    // background setup
    ImageData bgImg = context.getBackgroundImage();
    if (bgImg == null)
    {
      gc.setBackground(gc.getDevice().getSystemColor(SWT.COLOR_BLACK));
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

  protected void setContext(FrameData context) {
    this.context = context;
    if(context != null)
      this.size = context.getRenderSize();
  }
  
  /**
   * Clears the painter and fills the screen with the default background color (
   * {@value #DEFAULT_BG_COLOR}).
   */
  public void clear()
  {
    setContext(null);
  }

  /**
   * Checks paint count and 
   */
  private void onPaint() {
    if(++this.paintCount == DEBUG_INTERVAL) {
      int updateCount = this.updateCount;
      int paintCount = this.paintCount;
      this.updateCount = 0;
      this.paintCount = 0;
      if(updateCount > paintCount) {
        int skipped = (updateCount - paintCount);
        Log.debug(skipped + " of " + DEBUG_INTERVAL +  " Frame(s) skipped (" + (skipped*100.0f/updateCount) + "%)");        
      }         
    }
  }
  
  /**
   * Increments the update counter
   */
  protected void onUpdate() {
    this.updateCount++;
  }
  
  protected FrameData getContext() {
    return this.context;
  }
  
  
}
