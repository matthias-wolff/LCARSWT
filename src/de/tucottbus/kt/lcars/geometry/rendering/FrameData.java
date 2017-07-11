package de.tucottbus.kt.lcars.geometry.rendering;

import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Area;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import de.tucottbus.kt.lcars.PanelData;
import de.tucottbus.kt.lcars.PanelState;
import de.tucottbus.kt.lcars.elements.ElementData;
import de.tucottbus.kt.lcars.logging.Log;
import de.tucottbus.kt.lcars.swt.ImageMeta;
import de.tucottbus.kt.lcars.util.Objectt;

/**
 * Represents a context containing all data required to render a frame.
 * Instances of it can be applied each other to realize incremental and
 * selective updates.
 * 
 * @author Christian Borck
 *
 */
class FrameData
{
  public static final String CLASSKEY = "FrmDt";
  
  private boolean selectiveRepaint;
  private final boolean incremental;
  private PanelState panelState;
  private ElementData[] elements;
  private ArrayList<ElementData> elementsToPaint;
  private Shape dirtyArea;
  private boolean fullRepaint;
  private boolean bgChanged = true;

  private FrameData(PanelData panelData, boolean incremental,
      boolean selectiveRepaint)
  {
    this.incremental = incremental;
    this.selectiveRepaint = selectiveRepaint;
    this.panelState = panelData.panelState;
    this.elements = panelData.elementData;
    
    if (Log.DebugMode) //Debug: check if all elements not null
      for (ElementData ed : this.elements)
        if (ed == null){
          Log.warn("Illegal null ElementData found");
          return;
        }
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
   * Indicates if the background has changed comparing to previous {@link FrameData}.
   * 
   * @param pred
   *          - previous {@link FrameData}
   * @return true if background changed, otherwise false
   */
  private boolean updateBgImage(FrameData pred)
  {
    ImageMeta thisRes = panelState.bgImage;

    if (pred != null && Objectt.equals(thisRes, pred.panelState.bgImage))
      return false;

    if (thisRes == null)
      return true;

    // Dimension d = updateData.panelState.dimension;
    // bgImg.getScaledInstance(d.width,d.height,Image.SCALE_DEFAULT);
    return true;
  }


  /**
   * Applies missing data from the previous {@link FrameData} and calculates changes
   * from previous to this {@link FrameData}.
   * 
   * @param pred
   *          - previous {@link FrameData}
   * @param applyUpdate
   *          - Function class that applies missing data and return the changes
   *          flag. The signature is similar to
   *          "Integer applyUpdate(ElementData this, ElementData pred);"
   */
  public void apply(FrameData pred)
  {
    if (pred == null)
    {
      dirtyArea = new Rectangle(getPanelWidth(), getPanelHeight());
      elementsToPaint = new ArrayList<ElementData>(Arrays.asList(elements));
      bgChanged = updateBgImage(null);
      return;
    }
    
    bgChanged = updateBgImage(pred);
    
    this.fullRepaint = !panelState.equals(pred.panelState)
                    || !incremental
                    || !selectiveRepaint;
    //fullRepaint = true;

    int elCount = elements.length;

    // 1. Create a hash map of the current ElementData
    HashMap<Long, ElementData> hPred = incremental ? createHashMap(pred.elements) : null;
    if (incremental)
      for (ElementData edp : pred.elements)
        hPred.put(edp.serialNo, edp);
    
    // 2. Complete the received ElementData with the present information
    //
    if (this.fullRepaint)
    {
      elementsToPaint = new ArrayList<ElementData>(Arrays.asList(elements));
      if (incremental)
        for (ElementData edu : elements)
          try
          {
            ElementData edp = hPred.get(edu.serialNo);
            if (edp != null)
              hPred.remove(edp);
            edu.applyUpdate(edp);          
          } catch (Exception e)
          {
            if (edu != null)
              Log.err("Cannot apply frame update on " + edu + ".", e);
            else
              Log.err("Cannot apply frame update because of illegal null ElementData.");
          }
      dirtyArea = new Area(new Rectangle(getPanelWidth(), getPanelHeight()));
    } else
    {
      ArrayList<ElementData> elementsToPaint = new ArrayList<ElementData>(elCount);
      ArrayList<ElementData> elsWithoutChanges = new ArrayList<ElementData>(elCount);
      Area dirtyArea = new Area();

      for (ElementData edu : elements)
        try
        {
            if (incremental) {
              ElementData edp = hPred.get(edu.serialNo);
              if (edp != null)
                hPred.remove(edp);
              
              if (edu.applyUpdate(edp) == 0)
              {
                elsWithoutChanges.add(edu);
                continue;                
              }
              
              dirtyArea.add(new Area(edp.getBounds()));
            }
            dirtyArea.add(new Area(edu.getBounds()));
            elementsToPaint.add(edu);

        } catch (Exception e)
        {
          Log.err("Update failed on element #" + edu.serialNo + ": "
                  + e.getMessage());
        }
      // Add removed elements to the dirtyArea
      try
      {
        hPred
            .forEach((serialNo, edp) -> dirtyArea.add(new Area(edp.getBounds())));
      } catch (Exception e)
      {
        Log.err("Cannot create dirty area.", e);
      }
      dirtyArea.intersect(new Area(new Rectangle(getPanelWidth(), getPanelHeight())));
      this.dirtyArea = dirtyArea;

      for (ElementData edu : elsWithoutChanges)
        if (dirtyArea.intersects(edu.getBounds()))
          elementsToPaint.add(edu);
      this.elementsToPaint = elementsToPaint;
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
        if (thisEl == null) continue;
        thisEl.applyUpdate(predEl);
        if (thisEl.getMissing() == 0)
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
      ElementData[] elements)
  {
    HashMap<Long, ElementData> result = new HashMap<Long, ElementData>(
        elements.length);
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
      ElementData[] elements)
  {
    HashMap<Long, ElementData> result = new HashMap<Long, ElementData>(
        elements.length);
    for (ElementData edu : elements)
      if (edu.getMissing() != 0)
        result.put(edu.serialNo, edu);
    return result;
  }

  public Shape getDirtyArea()
  {
    return dirtyArea;
  }

  public int getPanelWidth()
  {
    return panelState.width;
  }

  public int getPanelHeight()
  {
    return panelState.height;
  }
  
  public PanelState getPanelState()
  {
    return panelState;
  }

  public ArrayList<ElementData> getElementsToPaint()
  {
    return elementsToPaint;
  }

  public boolean isBgChanged()
  {
    return bgChanged;
  }
  
  public ImageMeta getBackgroundImage()
  {
    return panelState.bgImage;
  }
  
  public Boolean getFullRepaint() {
    return fullRepaint;
  }
}
