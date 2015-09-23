package de.tucottbus.kt.lcars.swt;

import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Stack;
import java.util.TreeSet;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

import de.tucottbus.kt.lcars.PanelData;
import de.tucottbus.kt.lcars.PanelState;
import de.tucottbus.kt.lcars.elements.ElementData;
import de.tucottbus.kt.lcars.j2d.GImage;
import de.tucottbus.kt.lcars.logging.Log;

/**
 * 
 * @author Christian Borck
 *
 */
public abstract class PanelDataComposite extends Composite
{
  private final static int disabledCapacity = 100;
    
  private final Stack<ElementDataComposite> disabledControls = new Stack<ElementDataComposite>();
    
  private PanelState ps;
  
  private final Display display;
  
  private final HashMap<Long, ElementDataComposite> elements = new HashMap<Long, ElementDataComposite>(100);
  
  private final int edCanvasStyle;
  
  private String bgRes;
    
  private final PanelDataComposite _this = this;
  
  public PanelDataComposite(Composite parent, int style) {
    super (parent, style);
    edCanvasStyle = style | SWT.TRANSPARENT;
    display = getParent().getDisplay();
  }
  
  public void applyUpdate(PanelData panelData) {    
    if (panelData == null)
    {
      clear();
      return;
    }
    PanelState ps = panelData.panelState;
    PanelState currPs = ps != null ? ps : this.ps; //panel state for those who have not the previous state
    if (currPs.equals(ps))
      ps = null;
    
    ArrayList<ElementData> eds = panelData.elementData;
    ArrayList<Runnable> updateQueue = new ArrayList<Runnable>(eds != null ? eds.size()*3 : 10);
    
    if (ps != null) {
      this.ps = ps;
      
      String  nbgr = ps.bgImageRes;
      
      boolean newBg = bgRes != nbgr;
      if (newBg) // update background
      {   
        ImageData imgDt = GImage.getImage(nbgr);
        if (imgDt != null)
        {
          Image image = new Image(display, imgDt);
          
          updateQueue.add(() -> {
            Image oldImg = _this.getBackgroundImage();            
            _this.setBackgroundImage(image);
            if (oldImg != null)
              oldImg.dispose();
          }); 
        }
      }                  
      else
        updateQueue.add(() -> {
          _this.setBackgroundImage(null);
        });                    
      bgRes = nbgr;
    }
    
    if (eds == null)
      clear(); //TODO: reset all or only remove elements?
    else
      synchronized (this)
      {
        TreeSet<Long> oSerNo = new TreeSet<Long>(elements.keySet());
              
        Composite lower = null;            
        for(ElementData edu : eds) {
          
          assert(edu != null) : "edu != null";      
          long serNo = edu.serialNo;
          
          ElementDataComposite canvas = elements.get(serNo);
  
          if (canvas != null) // update existing canvas
          {
            canvas.applyUpdate(edu, ps, updateQueue);
            oSerNo.remove(serNo);
          }
          else // create new canvas
          {
            assert(edu.geometry != null) : "edu.geometry != null";                  
            elements.put(serNo, canvas = withdraw(currPs, edu, updateQueue));
          }
          
          final Composite c = canvas;        
          final Composite low = lower;
          updateQueue.add( low == null
              ? () -> {
                c.moveBelow(null);
                c.setVisible(true);
                c.setEnabled(true);}
              : ()->{
                c.moveAbove(low);
                c.setVisible(true);
                c.setEnabled(true);}
              );        
          lower = canvas;
        }
        
        //if (Log.DebugMode)
        //  Log.debug("Elements: "+ elements.keySet().toString());
        
        for (Iterator<Long> iterator = oSerNo.iterator(); iterator.hasNext();)
          deposit(elements.remove(iterator.next()), updateQueue);
      }
    applyUpdate(updateQueue);    
  }
  
  /**
   * Deactivates and clears the canvas and stores it to the disabledControls stack or if its full, it disposes the canvas
   * @param canvas
   */
  private void deposit(ElementDataComposite canvas, ArrayList<Runnable> updateQueue) {
    assert(canvas != null);
            
    synchronized (canvas)
    {
      assert(!disabledControls.contains(canvas));
      if (disabledControls.size() < disabledCapacity) { // disable and store
        updateQueue.add(() -> {
          canvas.setVisible(false);
          canvas.setEnabled(false);
        });
        canvas.clear();
        disabledControls.push(canvas);
        return;
      }      
      updateQueue.add(()-> { canvas.dispose(); });
    }
  }
  
  
  /**
   * Tries to recycle an ElementDataCanvas from the disabledControls stack or creates a new instance if the stack is empty. The canvas will be set to visible
   * @param ps
   * @param ed
   * @return
   */
  private ElementDataComposite withdraw(PanelState ps, ElementData ed, ArrayList<Runnable> updateQueue) {    
    if (!disabledControls.empty())
    {
      try
      {
        ElementDataComposite  result = disabledControls.pop();
        result.applyUpdate(ed, ps, updateQueue);
        return result;
      } catch (EmptyStackException e)
      {
        // create new canvas
      }      
    }
    // create new canvas
    ElementDataComposite[] result = new ElementDataComposite[1];
    display.syncExec(() -> {      
      result[0] = createElementDataCanvas(edCanvasStyle);
    });
    result[0].applyUpdate(ed, ps, updateQueue);
    return result[0];
  }
  
  protected abstract ElementDataComposite createElementDataCanvas(int style);
      
  public void clear() {
    if (elements.isEmpty()) return;
    synchronized (this)
    {
      ArrayList<Runnable> updateQueue = new ArrayList<Runnable>(elements.size());
      elements.forEach((serNo, canvas) -> {
        deposit(canvas,updateQueue);
      });      
      elements.clear();
      applyUpdate(updateQueue);
    }    
    Log.info("resetted");    
  }
  
  protected void applyUpdate(ArrayList<Runnable> updateQueue) {
    display.asyncExec(() -> {
      updateQueue.forEach((update) -> {update.run();});
    });
  }
}
