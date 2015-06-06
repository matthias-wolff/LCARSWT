package de.tucottbus.kt.lcars.j2d.rendering;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.util.concurrent.Semaphore;

import de.tucottbus.kt.lcars.PanelData;

public class Renderer extends ARenderer
{
  /**
   * Context for the next repaint. Contains all element and the region of the
   * screen that has to be updated.
   */
  private FrameData nextContext;

  private Semaphore mutex = new Semaphore(1);
  
  public Renderer (Dimension initialSize) {
    super(initialSize);
  }
  
  public Renderer(ARenderer renderer)
  {
    super(renderer);
  }

  /**
   * Paints the panel elements of this screen on a {@link Graphics2D} context. 
   * 
   * @param g2d the graphics context
   * @see #elements
   */
  public void paint2D(AdvGraphics2D g2d) {
    if(nextContext != null)
      nextContext.apply(getContext());
    setContext(nextContext);
    super.paint2D(g2d);
  }

  /**
   * Updates the data for rendering.
   * @param data
   * @param incremental
   */
  public void update(PanelData data, boolean incremental)
  {
    try
    {
      mutex.acquire();
      doUpdate(data, incremental);
      mutex.release();
    } catch (InterruptedException e)
    {
      synchronized (this) {
        doUpdate(data, incremental);
      }
    }    
  }

  private void doUpdate(PanelData data, boolean incremental) {
    FrameData nextContext = FrameData.create(data, incremental, this.selectiveRepaint);
    nextContext.apply(this.nextContext);
    this.nextContext = nextContext;
    onUpdate();
  }
  
  /**
   * Resets the painter and fills the screen with the default background color (
   * {@value #DEFAULT_BG_COLOR}).
   */
  public void reset()
  {    
    try
    {
      mutex.acquire();
      super.reset();
      mutex.release();
    } catch (InterruptedException e)
    {
      synchronized (this) {
        super.reset();
      }
    }    
  }


}
