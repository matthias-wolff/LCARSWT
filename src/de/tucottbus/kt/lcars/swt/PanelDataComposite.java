package de.tucottbus.kt.lcars.swt;

import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Stack;
import java.util.TreeSet;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.widgets.Canvas;
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
  
  private final Stack<ElementDataCanvas> disabledControls = new Stack<ElementDataCanvas>();
    
  private PanelState ps;
  
  private final Display display;
  
  private final HashMap<Long, ElementDataCanvas> elements = new HashMap<Long, ElementDataCanvas>(100);
  
  private final int edCanvasStyle;
  
  private String bgRes;
  
  private final PanelDataComposite _this = this;
  
  public PanelDataComposite(Composite parent, int style) {
    super (parent, style);    
    this.edCanvasStyle = style | SWT.NO_BACKGROUND;
    this.display = getParent().getDisplay();    
  }
  
  public void applyUpdate(PanelData panelData) {        
    if (panelData == null) {
      clear();
      return;
    }
    PanelState ps = panelData.panelState;
    PanelState currPs = ps != null ? ps : this.ps; //panel state for those who have not the previous state
    if (ps != null) {
      this.ps = ps;
      
      String  nbgr = ps.bgImageRes;
      
      boolean newBg = bgRes != nbgr;
      if (newBg) {   // update background
        ImageData imgDt = GImage.getImage(nbgr);
        if (imgDt != null)
        {
          Image image = new Image(display, imgDt);
          
          display.asyncExec(() -> {
            Image oldImg = _this.getBackgroundImage();            
            _this.setBackgroundImage(image);
            if (oldImg != null)
              oldImg.dispose();
          }); 
        }
      }                  
      else
        display.asyncExec(() -> {
          _this.setBackgroundImage(null);
        });                    
      bgRes = nbgr;
    }
    
    ArrayList<ElementData> eds = panelData.elementData;

    if (eds == null) {
      clear(); //TODO: reset all or only remove elements?
      return;
    }
        
    synchronized (elements)
    {
      TreeSet<Long> oSerNo = new TreeSet<Long>(elements.keySet());
            
      Canvas lower = null;            
      for(ElementData edu : eds) {
        assert(edu != null) : "edu != null";      
        long serNo = edu.serialNo;
        ElementDataCanvas canvas = elements.get(serNo);
        Canvas lowerr = lower;
        
        if (canvas != null) // update existing canvas
        {
          canvas.applyUpdate(edu, ps);
          oSerNo.remove(serNo);
          
          display.asyncExec(lowerr == null
              ? () -> {canvas.moveBelow(null);}
              : () -> {canvas.moveAbove(lowerr);});                
          lower = canvas;
        }
        else // create new canvas
        {
          assert(edu.geometry != null) : "edu.geometry != null";
                  
          ElementDataCanvas nCanvas = withdraw(currPs, edu);        
          elements.put(serNo, nCanvas);
          display.asyncExec( lowerr == null
              ? () -> {
                nCanvas.moveBelow(null);
                nCanvas.setVisible(true);
                nCanvas.setEnabled(true);}
              : ()->{
                nCanvas.moveAbove(lowerr);
                nCanvas.setVisible(true);
                nCanvas.setEnabled(true);}
              );        
          lower = nCanvas;
        }
      }
      
      //if (Log.DebugMode)
      //  Log.debug("Elements: "+ elements.keySet().toString());
      
      for (Iterator<Long> iterator = oSerNo.iterator(); iterator.hasNext();)
        deposit(elements.remove(iterator.next()));         
    }    
  }
  
  /**
   * Deactivates and clears the canvas and stores it to the disabledControls stack or if its full, it disposes the canvas
   * @param canvas
   */
  private void deposit(ElementDataCanvas canvas) {
    assert(canvas != null);
            
    synchronized (canvas)
    {
      int count = disabledControls.size();
      assert(!disabledControls.contains(canvas));
      if (count < disabledCapacity) { // disable and store
        display.asyncExec(() -> {
          canvas.setVisible(false);
          canvas.setEnabled(false);
        });
        canvas.clear();
        disabledControls.push(canvas);
        return;
      }
      
      // else dispose canvas
      canvas.dispose();                                    
    }    
  }
  
  
  /**
   * Tries to recycle an ElementDataCanvas from the disabledControls stack or creates a new instance if the stack is empty. The canvas will be set to visible
   * @param ps
   * @param ed
   * @return
   */
  private ElementDataCanvas withdraw(PanelState ps, ElementData ed) {
    final PanelDataComposite _this = this;
    
    if (!disabledControls.empty())
    {
      try
      {
        ElementDataCanvas  result = disabledControls.pop();
        result.applyUpdate(ed, ps);
        return result;
      } catch (EmptyStackException e)
      {
        // create new canvas
      }      
    }
    // create new canvas
    ElementDataCanvas[] result = new ElementDataCanvas[1];
    display.syncExec(() -> {      
      result[0] = new ElementDataCanvas(this, edCanvasStyle)
      {        
        @Override
        public void prePaint(GC gc)
        {
          _this.prePaint(gc);
        }
        
        @Override
        public void postPaint(GC gc)
        {
          _this.postPaint(gc);
        }
      };
    });
    result[0].applyUpdate(ed, ps);
    return result[0];
  }
  
  public void clear() {
    Log.info("resetted");    
    throw new UnsupportedOperationException();
  }
  
  public abstract void prePaint(GC gc);
  public abstract void postPaint(GC gc);  
}
