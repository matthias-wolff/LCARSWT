package de.tucottbus.kt.lcars.contributors;

import java.awt.Dimension;
import java.util.Vector;

import de.tucottbus.kt.lcars.LCARS;
import de.tucottbus.kt.lcars.Panel;
import de.tucottbus.kt.lcars.elements.EEvent;
import de.tucottbus.kt.lcars.elements.EEventListener;
import de.tucottbus.kt.lcars.elements.ELabel;
import de.tucottbus.kt.lcars.elements.ERect;
import de.tucottbus.kt.lcars.elements.EValue;
import de.tucottbus.kt.lcars.swt.ColorMeta;

/**
 * TODO: The message box concept does not fit well in the LCARS look and feel. Avoid message boxes!
 */
public class EMessageBox extends ElementContributor implements EEventListener
{
  
  private Vector<EMessageBoxListener> listeners = new Vector<EMessageBoxListener>();
  private int       w;
  private int       h;
  private EValue    eTit;
  private ELabel     eMsg;
  private ERect     eBn1;
  private ERect     eBn2;
  private final int istyle = LCARS.ES_BLINKING|LCARS.EC_ELBOUP|LCARS.ES_MODAL|LCARS.ES_RECT_RND|LCARS.ES_STATIC;
  private final int tstyle = LCARS.EC_TEXT|LCARS.ES_MODAL|LCARS.ES_LABEL_C|LCARS.EF_HEAD2|LCARS.ES_STATIC; 
  private final int bstyle = LCARS.EC_PRIMARY|LCARS.ES_MODAL|LCARS.ES_LABEL_E|LCARS.ES_RECT_RND|LCARS.ES_SELECTED; 
  
  public EMessageBox(int x, int y, int w, int h)
  {
    super(x,y);
    this.w = w;
    this.h = h;
    init();
  }
  
  protected void init()
  {
    int bWidth  = 174;
    int bHeight = 48;
    int xBn1    = (this.w-2*bWidth-15)/2;
    int xBn2    = xBn1+bWidth+15;
    this.eMsg   = new ELabel(null,0,41,this.w,h-56-bHeight,tstyle,null);
    this.eBn1   = new ERect(null,xBn1,this.h-15-bHeight,bWidth,bHeight,bstyle,null);
    this.eBn2   = new ERect(null,xBn2,this.h-15-bHeight,bWidth,bHeight,bstyle,null);

    eTit = new EValue(null,0,0,w,38,istyle,null);
    add(eTit);
    
    ERect e = new ERect(null,0,44,w,h-47,LCARS.ES_STATIC|LCARS.ES_MODAL,null);
    e.setColor(new ColorMeta(0x80000000,true));
    add(e);
    e = new ERect(null,0,h+3,w-75,3,LCARS.ES_BLINKING|LCARS.EC_ELBOUP|LCARS.ES_STATIC|LCARS.ES_MODAL,null);
    add(e);
    add(new ELabel(null,0,h+3,w,14,LCARS.ES_BLINKING|LCARS.EC_ELBOUP|LCARS.ES_STATIC|LCARS.ES_MODAL|LCARS.ES_LABEL_SE|LCARS.EF_TINY,"LCARS MESSAGE"));
    
    add(eMsg);
    eBn1.addEEventListener(this); add(eBn1);
    eBn2.addEEventListener(this); add(eBn2);
  }

  protected void fireAnswer(String answer)
  {
    Panel panel = getPanel();
    if (panel==null) return;
    panel.dim(1f);
    panel.setModal(false);
    removeFromPanel();
    if (answer!=null)
      for (int i=0; i<listeners.size(); i++)
        listeners.get(i).answer(answer);
    listeners = new Vector<EMessageBoxListener>();
  }
  
  protected Dimension getDimension()
  {
    return new Dimension(this.w,this.h);
  }
  
  /**
   * Executes the message box.
   * 
   * @param panel
   *          the panel
   * @param title
   *          the title of the message box
   * @param msg
   *          the message
   * @param bn1
   *          the label of the first button
   * @param bn2
   *          the label of the second button
   */
  public void open(Panel panel, String title, String msg, String bn1, String bn2)
  {
    if (panel==null) return;
    panel.dim(0.3f);
    panel.setModal(true);
    eTit.setValue(title);
    eMsg.setLabel(msg);
    eBn1.setLabel(bn1);
    eBn2.setLabel(bn2);
    addToPanel(panel);
  }
  
  public void addListener(EMessageBoxListener l)
  {
    if (l==null) return;
    listeners.add(l);
  }
  
  public void removeListener(EMessageBoxListener l)
  {
    listeners.remove(l);
  }
  
  public void touchDown(EEvent ee)
  {
    fireAnswer(ee.el.getLabel());
  }

  public void touchDrag(EEvent ee)
  {
  }

  public void touchHold(EEvent ee)
  {
  }

  public void touchUp(EEvent ee)
  {
  }

}
