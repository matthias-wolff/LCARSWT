package de.tucottbus.kt.lcars.j2d.rendering;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Shape;

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
  public static final String CLASSKEY = "RENDER";

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
   * Context for the next repaint. Contains all element and the region of the
   * screen that has to be updated.
   */
  private FrameData context;

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
   * 
   * @param initialSize
   */
  public Renderer(Dimension initialSize) {
    if(initialSize == null)
      throw new NullPointerException("initialSize");
    
    this.size = initialSize;
  }
  
  /**
   * 
   * @param renderer
   */
  public Renderer(Renderer renderer) {
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
  public synchronized void update(PanelData data, boolean incremental)
  {
    FrameData newContext = FrameData.create(data, incremental, this.selectiveRepaint);
    newContext.apply(context);
    setContext(newContext);
    onUpdate();
  }
  
  /**
   * Paints the panel elements of this screen on a {@link Graphics2D} context. 
   * 
   * @param g2d the graphics context
   * @see #elements
   */
  public void paint2D(AdvGraphics2D g2d)
  {
    onPaint();
    FrameData context = getContext();
    if (context == null) // null stands for reset
    {
      g2d.setClip(0,0,size.width,size.height);
      Shape scrRect = new Rectangle(size);
      g2d.setColor(DEFAULT_BG_COLOR);
      g2d.draw(scrRect);
      return;
    }

    // clipping setup
    Shape dirtyArea = context.getDirtyArea();
    if(context.getFullRepaint())
      g2d.setClip(0,0,size.width,size.height);
    else
      g2d.setClip(dirtyArea);

    // background setup
    Image bgImg = context.getBackgroundImage();
    if (bgImg == null)
    {
      g2d.setColor(Color.black);
      g2d.fill(dirtyArea);
    } else
      g2d.drawImage(bgImg, g2d.getTransform(), null);
    // TODO possible problem with clipping when drawing

    PanelState state = context.getPanelState();

    // GImage.beginCacheRun();
    try
    {
      for (ElementData el : context.getElementsToPaint())
        el.render2D(g2d, state);

      // LCARS.log(CLASSKEY, context.getElementsToPaint().size() +
      // " elements are rendered");
    } catch (Throwable e)
    {
      Log.err("SCR", "error drawing elements to the screen", e);
    }
    // GImage.endCacheRun();

    // g2d.setColor(Color.red);
    // g2d.draw(dirtyArea);
  }

  /**
   * Resets the painter and fills the screen with the default background color (
   * {@value #DEFAULT_BG_COLOR}).
   */
  public void reset()
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
        Log.debug(CLASSKEY, skipped + " of " + DEBUG_INTERVAL +  " Frame(s) skipped (" + (skipped*100.0f/updateCount) + "%)");        
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
  
  protected void setContext(FrameData context) {
    this.context = context;
    if(context != null)
      this.size = context.getRenderSize();
  }
  
}
