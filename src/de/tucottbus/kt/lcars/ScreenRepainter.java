package de.tucottbus.kt.lcars;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Toolkit;
import java.awt.geom.Area;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;
import java.util.concurrent.Semaphore;
import java.util.function.BiFunction;

import de.tucottbus.kt.lcars.elements.ElementData;
import de.tucottbus.kt.lcars.util.BlockingBoundedBuffer;

/**
 * This Class organizes the screen updates by using asynchronous pre
 * calculations to relieve the paint process.
 * 
 * @author Christian Borck
 *
 */
class ScreenRepainter
{
  public static final String CLASSKEY = "SCR";

  // -- CONSTANTS --
  public static final int PREPAINT_BUFFER_SIZE = 100;

  /**
   * The Default background color is black.
   */
  public static final Color DEFAULT_BG_COLOR = Color.BLACK;

  /**
   * Synchronized buffer stores the incoming update data for the prepaint
   * calculations
   */
  BlockingBoundedBuffer<FrameData> buffer = new BlockingBoundedBuffer<FrameData>(
      PREPAINT_BUFFER_SIZE, FrameData.class);

  /**
   * Synchronizes the paint of the frame context with the pre calculations
   * process
   */
  Semaphore semaPaint = new Semaphore(0);

  /**
   * Synchronizes the pre calculations of the frame context with the paint
   * process
   */
  Semaphore semaContext = new Semaphore(1);

  /**
   * Context for the next repaint. Contains all element and the region of the
   * screen that has to be updated.
   */
  FrameData context;

  /**
   * Current size of the parent screen.
   */
  Dimension size;

  boolean selectiveRepaint;

  public ScreenRepainter(Dimension initialSize)
  {
    this.size = initialSize;
    this.selectiveRepaint = false;

    Thread worker = new Thread(new Runnable()
    {
      @Override
      public void run()
      {
        while (true)
        {
          try
          {
            // read the next context and precalculate the updates
            FrameData[] nextContexts = buffer.takeAll();
            Dimension nextScreenSize;
            int count = nextContexts.length;
            FrameData nextContext = nextContexts[count - 1];
            FrameData currContext = context;
            if (nextContext != null)
            {
              if (count > 1)
              {
                LCARS.log(CLASSKEY, (count - 1) + " Frames skipped");

                // collapse with previous contexts
                ArrayList<FrameData> skipped = new ArrayList<FrameData>(
                    count - 1);
                int iCollapse = count - 2;
                for (; iCollapse >= 0; iCollapse--)
                {
                  FrameData pred = nextContexts[iCollapse];
                  if (pred == null) // find resets
                  {
                    currContext = null;
                    break;
                  }
                  skipped.add(0, pred);
                }
                nextContext.collapse(skipped.toArray(new FrameData[count - 2
                    - iCollapse]));
              }

              nextContext.apply(currContext);
              nextScreenSize = nextContext.getRenderSize();
            } else
              nextScreenSize = size;

            // update data
            // System.out.println("(Worker) Update ... [SCtxt "+semaContext.availablePermits()+"|SPaint "+
            // semaPaint.availablePermits()+"]");
            semaContext.acquire();
            context = nextContext;
            size = nextScreenSize;

            // System.out.println("(Worker) done with "+ (context == null ?
            // "null" : (context.getElementsToPaint().size() + " elements"))
            // +".");
            semaPaint.release();
          } catch (InterruptedException e)
          {
            LCARS.err(CLASSKEY,
                "Sychronisation error while reading panel data buffer.");
            e.printStackTrace();
          }
        }
      }
    });

    worker.setName("ScreenRepainter worker");
    worker.start();
  }

  public void update(PanelData data, boolean incremental)
  {
    try
    {
      // System.out.println("(Input) update ... ");
      buffer.put(FrameData.create(data, incremental, this.selectiveRepaint));
      // System.out.println("(Input) done with "+(data == null ? "null" :
      // (data.elementData.size() + " elements")) +".");
    } catch (InterruptedException e)
    {
      LCARS.err(CLASSKEY, "Sychronisation error while update panel data.");
    }
  }

  /**
   * Paints the panel elements of this screen on a {@link Graphics2D} context.
   * 
   * @param g2d
   *          the graphics context
   * @see #elements
   */
  public void paint2D(ScreenGraphics2D g2d)
  {
    FrameData context;
    try
    {
      semaPaint.acquire();
      context = this.context;
      semaContext.release();
    } catch (InterruptedException e1)
    {
      LCARS.err(CLASSKEY, "Synchronization error while painting on sreen.");
      e1.printStackTrace();
      return;
    }

    if (context == null) // null stands for reset
    {
      Shape scrRect = new Rectangle(size);
      g2d.setClip(scrRect);
      g2d.setColor(DEFAULT_BG_COLOR);
      g2d.draw(scrRect);
      return;
    }

    // clipping setup
    Shape dirtyArea = context.getDirtyArea();
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
      LCARS.err("SCR", "error drawing elements to the screen");
      System.out.println();
      e.printStackTrace();
    }
    // GImage.endCacheRun();

    // g2d.setColor(Color.red);
    // g2d.draw(dirtyArea);
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
   * Resets the painter and fills the screen with the default background color (
   * {@value #DEFAULT_BG_COLOR}).
   */
  public void reset()
  {
    try
    {
      buffer.put(null);
    } catch (InterruptedException e)
    {
      LCARS.err(CLASSKEY, "Synchronization error while resetting screen.");
    }
  }

  /**
   * Represents a context containing all data required to render a frame.
   * Instances of it can be applied each other to realize incremental and
   * selective updates.
   * 
   * @author Christian Borck
   *
   */
  private static class FrameData
  {
    private boolean selectiveRepaint;
    private boolean incremental;
    private PanelState panelState;
    private Vector<ElementData> elements;
    private Vector<ElementData> elementsToPaint;
    private Image bgImg;
    private Shape dirtyArea;

    private FrameData(PanelData panelData, boolean incremental,
        boolean selectiveRepaint)
    {
      this.incremental = incremental;
      this.selectiveRepaint = selectiveRepaint;
      this.panelState = panelData.panelState;
      this.elements = panelData.elementData;
    }

    /**
     * Creates frame context.
     * 
     * @param panelData
     * @param incremental
     * @return
     */
    public static FrameData create(PanelData panelData, boolean incremental,
        boolean selectiveRepaint)
    {
      return panelData == null || panelData.panelState == null
          || panelData.elementData == null ? null : new FrameData(panelData,
          incremental, selectiveRepaint);
    }

    /**
     * Called to update the background image of the screen.
     * 
     * @param pred
     *          - previous context
     * @return has background changed since the previous context
     */
    private boolean updateBgImage(FrameData pred)
    {
      String thisRes = panelState.bgImageRes;

      if (pred != null)
      {
        String predRes = pred.panelState.bgImageRes;
        if (thisRes == predRes || (thisRes != null && thisRes.equals(predRes)))
        {
          this.bgImg = pred.bgImg;
          return false;
        }
      }

      if (thisRes == null)
      {
        bgImg = null;
        return true;
      }

      ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
      LCARS.log("SCR", "background=" + thisRes);
      URL resource = classLoader.getResource(thisRes);
      if (resource == null)
      {
        bgImg = null;
        return true;
      }
      bgImg = Toolkit.getDefaultToolkit().createImage(resource.getFile());
      // Dimension d = updateData.panelState.dimension;
      // bgImg.getScaledInstance(d.width,d.height,Image.SCALE_DEFAULT);
      return true;
    }

    /**
     * Applies changes from previous to this context. See
     * {@link #apply(FrameData, BiFunction)} method.
     * 
     * @param pred
     */
    public void apply(FrameData pred)
    {
      apply(pred, incremental ? ((edu, edp) -> edu.applyUpdate(edp)) : ((edu,
          __) -> edu.updateFlag));
    }

    /**
     * Applies missing data from the previous frame context and calculates
     * changes from previous to this context.
     * 
     * @param pred
     *          - previous context
     * @param applyUpdate
     *          - Function class that applies missing data and return the
     *          changes flag. The signature is similar to
     *          "Integer applyUpdate(ElementData this, ElementData pred);"
     */
    private void apply(FrameData pred,
        BiFunction<ElementData, ElementData, Integer> applyUpdate)
    {
      if (pred == null)
      {
        dirtyArea = new Rectangle(getRenderSize());
        elementsToPaint = elements;
        updateBgImage(null);
        return;
      }

      boolean fullRepaint = updateBgImage(pred)
          || !panelState.equals(pred.panelState) || !incremental
          || !selectiveRepaint;
      // fullRepaint = true;

      int elCount = elements.size();
      this.dirtyArea = new Rectangle(panelState.dimension);

      Area dirtyArea = new Area();
      // 1. Create a hash map of the current ElementData
      HashMap<Long, ElementData> hPred = createHashMap(pred.elements);

      for (ElementData edp : pred.elements)
        hPred.put(edp.serialNo, edp);

      // 2. Complete the received ElementData with the present information
      //
      if (fullRepaint)
      {
        elementsToPaint = elements;
        for (ElementData edu : elements)
          try
          {
            ElementData edp = hPred.get(edu.serialNo);
            if (edp != null)
              applyUpdate.apply(edu, edp);
          } catch (Exception e)
          {
            LCARS.err("SCR", "Update failed on element #" + edu.serialNo + ": "
                + e.getMessage());
          }

      } else
      {
        elementsToPaint = new Vector<ElementData>(elCount);
        Vector<ElementData> elsWithoutChanges = new Vector<ElementData>(elCount);

        for (ElementData edu : elements)
          try
          {
            ElementData edp = hPred.get(edu.serialNo);
            if (edp != null)
            {
              if (applyUpdate.apply(edu, edp) == 0)
              {
                elsWithoutChanges.addElement(edu);
                continue;
              }
              hPred.remove(edp);
              dirtyArea.add(new Area(edp.getBounds()));
            }

            dirtyArea.add(new Area(edu.getBounds()));
            elementsToPaint.addElement(edu);
          } catch (Exception e)
          {
            LCARS.err("SCR", "Update failed on element #" + edu.serialNo + ": "
                + e.getMessage());
          }
        // Add removed elements to the dirtyArea
        try
        {
          hPred.forEach((serialNo, edp) -> dirtyArea.add(new Area(edp
              .getBounds())));
        } catch (Exception e)
        {
          e.printStackTrace();
        }
        dirtyArea.intersect(new Area(this.dirtyArea));
        this.dirtyArea = dirtyArea;

        for (ElementData edu : elsWithoutChanges)
          if (dirtyArea.intersects(edu.getBounds()))
            elementsToPaint.add(edu);
      }
    }

    /**
     * Collapse this and all previous frames as they had added as one frame.
     * 
     * @param preds
     * @return Indicates if all elements has complete data
     */
    public boolean collapse(FrameData[] preds)
    {
      // TODO: bounded buffer testen
      // TODO:

      if (!incremental) // not if background changed because incremental apply
        return true;

      HashMap<Long, ElementData> hThis = createHashMapOnlyIncomplete(elements);
      if (hThis.isEmpty())
        return true;

      for (int i = preds.length - 1; i >= 0; i--)
        for (ElementData predEl : preds[i].elements)
        {
          ElementData thisEl = hThis.get(predEl.serialNo);

          if (thisEl == null)
            continue;

          thisEl.applyUpdate(predEl);
          thisEl.updateFlag |= predEl.updateFlag;

          if (thisEl.getMissingFlag() == 0)
          {
            hThis.remove(thisEl.serialNo);
            if (hThis.isEmpty())
              return true;
          }
        }

      return hThis.isEmpty();
    }

    /**
     * 
     * @param elements
     * @return
     */
    private static HashMap<Long, ElementData> createHashMap(
        Vector<ElementData> elements)
    {
      HashMap<Long, ElementData> result = new HashMap<Long, ElementData>(
          elements.size());
      for (ElementData edp : elements)
        result.put(edp.serialNo, edp);
      return result;
    }

    /**
     * 
     * @param elements
     * @return
     */
    private static HashMap<Long, ElementData> createHashMapOnlyIncomplete(
        Vector<ElementData> elements)
    {
      HashMap<Long, ElementData> result = new HashMap<Long, ElementData>(
          elements.size());
      for (ElementData edu : elements)
        if (edu.getMissingFlag() != 0)
          result.put(edu.serialNo, edu);
      return result;
    }

    public Shape getDirtyArea()
    {
      return dirtyArea;
    }

    private Dimension getRenderSize()
    {
      return panelState.dimension;
    }

    public PanelState getPanelState()
    {
      return panelState;
    }

    public Vector<ElementData> getElementsToPaint()
    {
      return elementsToPaint;
    }

    public Image getBackgroundImage()
    {
      return bgImg;
    }
  }

}
