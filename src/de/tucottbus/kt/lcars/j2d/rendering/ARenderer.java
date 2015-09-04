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
public abstract class ARenderer
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

  private int width;
  
  private int height;
  
  /**
   * 
   * @param initialSize
   */
  public ARenderer(Dimension initialSize)
  {
    if (initialSize == null)
      throw new NullPointerException("initialSize");
    width = initialSize.width;
    height = initialSize.height;
  }

  /**
   * 
   * @param renderer
   */
  public ARenderer(ARenderer renderer)
  {
    context = renderer.context;
    selectiveRepaint = renderer.selectiveRepaint;
    width = renderer.width;
    height = renderer.height;
  }

  /**
   * Sets a hint for selective repaints where only dirty areas on the screen
   * will be repainted. Dirty areas are defined by elements that has been added,
   * remove or changed.
   * 
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
  public Dimension getSize()
  {
    return new Dimension(width, height);
  }

  /**
   * Updates the data for rendering.
   * 
   * @param data
   * @param incremental
   */
  public abstract void update(PanelData data, boolean incremental);

  /**
   * Paints the panel elements of this screen on a {@link Graphics2D} context.
   * 
   * @param g2d
   *          the graphics context
   * @see #elements
   */
  public void paint2D(AdvGraphics2D g2d)
  {
    onPaint();
    FrameData context = getContext();
    Dimension size = getSize();
    if (context == null) // null stands for reset
    {
      g2d.setClip(0, 0, size.width, size.height);
      Shape scrRect = new Rectangle(size);
      g2d.setColor(DEFAULT_BG_COLOR);
      g2d.draw(scrRect);
      return;
    }

    // clipping setup
    Shape dirtyArea = context.getDirtyArea();
    if (context.getFullRepaint())
      g2d.setClip(0, 0, size.width, size.height);
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
    } catch (Exception e)
    {
      Log.err("Error drawing elements to the screen", e);
    }
    // GImage.endCacheRun();

    // g2d.setColor(Color.red);
    // g2d.draw(dirtyArea);
  }

  protected void setContext(FrameData context)
  {
    this.context = context;
    return;
//    if (context != null) {
//      //TODO: correct replacement?
//      Dimension size = context.getRenderSize();
//      checkOffScreenImages(size.width, size.height);
//      //this.size = context.getRenderSize();
//    }
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
  private void onPaint()
  {
    if (++this.paintCount == DEBUG_INTERVAL)
    {
      int updateCount = this.updateCount;
      int paintCount = this.paintCount;
      this.updateCount = 0;
      this.paintCount = 0;
      if (updateCount > paintCount)
      {
        int skipped = (updateCount - paintCount);
        Log.debug(skipped + " of " + DEBUG_INTERVAL + " Frame(s) skipped ("
            + (skipped * 100.0f / updateCount) + "%)");
      }
    }
  }

  /**
   * Increments the update counter
   */
  protected void onUpdate()
  {
    this.updateCount++;
  }

  protected FrameData getContext()
  {
    return this.context;
  }

  /**
   * Prepare to render on a SWT graphics context.
   */
}
