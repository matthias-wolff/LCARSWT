package de.tucottbus.kt.lcars.j2d.rendering;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.BiFunction;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Region;

import de.tucottbus.kt.lcars.PanelData;
import de.tucottbus.kt.lcars.PanelState;
import de.tucottbus.kt.lcars.elements.ElementData;
import de.tucottbus.kt.lcars.j2d.GImage;
import de.tucottbus.kt.lcars.logging.Log;
import de.tucottbus.kt.lcars.util.Object;

/**
 * Represents a context containing all data required to render a frame.
 * Instances of it can be applied each other to realize incremental and
 * selective updates.
 * 
 * @author Christian Borck
 *
 */
class FrameData //TODO: implements Disposable
{
  public static final String CLASSKEY = "FrmDt";
  
  private boolean selectiveRepaint;
  private boolean incremental;
  private PanelState panelState;
  private ArrayList<ElementData> elements;
  private ArrayList<ElementData> elementsToPaint;
  private Image bgImg;
  private Region dirtyArea;
  private boolean fullRepaint;

  private FrameData(PanelData panelData, boolean incremental,
      boolean selectiveRepaint)
  {
    this.incremental = incremental;
    this.selectiveRepaint = selectiveRepaint;
    this.panelState = panelData.panelState;
    this.elements = new ArrayList<ElementData>(panelData.elementData);
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
      if (Object.equals(thisRes, pred.panelState.bgImageRes))
      {
        this.bgImg = pred.bgImg;
        return false;
      }
    }

    bgImg = GImage.getImage(thisRes);
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
    apply(pred, incremental ? ((edu, edp) -> edu.applyUpdate(edp))
        : ((edu, __) -> edu.updateFlag));
  }

  /**
   * Applies missing data from the previous frame context and calculates changes
   * from previous to this context.
   * 
   * @param pred
   *          - previous context
   * @param applyUpdate
   *          - Function class that applies missing data and return the changes
   *          flag. The signature is similar to
   *          "Integer applyUpdate(ElementData this, ElementData pred);"
   */
  private void apply(FrameData pred,
      BiFunction<ElementData, ElementData, Integer> applyUpdate)
  {
    Dimension size = getRenderSize();
    Rectangle bounds = new Rectangle(0,0,size.width, size.height);
    dirtyArea = new Region();
    dirtyArea.add(bounds);

    if (pred == null)
    {
      elementsToPaint = elements;
      updateBgImage(null);
      return;
    }
    
    fullRepaint = updateBgImage(pred)
        || !panelState.equals(pred.panelState) || !incremental
        || !selectiveRepaint;
    //fullRepaint = true;

    int elCount = elements.size();

    Region dirtyArea = new Region();
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
          if (edp != null) {
            applyUpdate.apply(edu, edp);   
            hPred.remove(edu.serialNo);
          } else
            edu.onAddToScreen();
        } catch (Exception e)
        {
          Log.err("Applying update failed on element #" + edu.serialNo, e);
        }
      
      // notify element removed
      hPred.forEach((serialNo, edp) -> edp.onRemoveFromScreen());
    } else
    {
      elementsToPaint = new ArrayList<ElementData>(elCount);
      ArrayList<ElementData> validElements = new ArrayList<ElementData>(elCount);
      for (ElementData edu : elements)
        try
        {
          ElementData edp = hPred.get(edu.serialNo);
          if (edp != null) // update from existing element
          {
            if (applyUpdate.apply(edu, edp) == 0)
            {
              validElements.add(edu);
              continue;
            }
            hPred.remove(edp);
            dirtyArea.add(edp.getBounds());
          }
          else // added element
            edu.onAddToScreen();

          dirtyArea.add(edu.getBounds());
          elementsToPaint.add(edu);
        } catch (Exception e)
        {
          Log.err("Applying update failed on element #" + edu.serialNo, e);
        }
      // Add removed elements to the dirtyArea
      try
      {
        hPred
            .forEach((serialNo, edp) -> {
              dirtyArea.add(edp.getBounds());
              edp.onRemoveFromScreen();
            });
      } catch (Exception e)
      {
        e.printStackTrace();
      }
      dirtyArea.intersect(bounds);
      this.dirtyArea.dispose();
      this.dirtyArea = dirtyArea;

      for (ElementData edu : validElements)
        if (dirtyArea.intersects(edu.getBounds()))
          elementsToPaint.add(edu);
    }
//    if(elements.size() !=elementsToPaint.size())
//      Log.warn(CLASSKEY, "Element difference " + elements.size() + ":" + elementsToPaint.size());
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
      ArrayList<ElementData> elements)
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
      ArrayList<ElementData> elements)
  {
    HashMap<Long, ElementData> result = new HashMap<Long, ElementData>(
        elements.size());
    for (ElementData edu : elements)
      if (edu.getMissingFlag() != 0)
        result.put(edu.serialNo, edu);
    return result;
  }

  public Region getDirtyArea()
  {
    return dirtyArea;
  }

  public Dimension getRenderSize()
  {
    return panelState.dimension;
  }

  public PanelState getPanelState()
  {
    return panelState;
  }

  public ArrayList<ElementData> getElementsToPaint()
  {
    return elementsToPaint;
  }

  public Image getBackgroundImage()
  {
    return bgImg;
  }
  
  public Boolean getFullRepaint() {
    return fullRepaint;
  }
}
