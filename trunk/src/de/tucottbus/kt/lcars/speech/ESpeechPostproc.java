package de.tucottbus.kt.lcars.speech;

import java.awt.Color;
import java.util.Locale;

import de.tucottbus.kt.lcars.LCARS;
import de.tucottbus.kt.lcars.contributors.ElementContributor;
import de.tucottbus.kt.lcars.elements.EElbo;
import de.tucottbus.kt.lcars.elements.EEvent;
import de.tucottbus.kt.lcars.elements.EEventListenerAdapter;
import de.tucottbus.kt.lcars.elements.ELabel;
import de.tucottbus.kt.lcars.elements.ERect;
import de.tucottbus.kt.lcars.elements.EValue;
import de.tucottbus.kt.lcars.speech.events.PostprocEvent;

/**
 * EXPERIMENTAL: Display of recognition post-processing information.
 *  
 * @author Matthias Wolff
 */
public class ESpeechPostproc extends ElementContributor
{
  private final Color cLsr = new Color(0xCC9999FF,true);
  
  private final Color cNll = new Color(0xCCFF0066,true);
  
  private final Color c1 = new Color(137,101,70);

  private final Color c2 = new Color(137,101,70,64);

  private final Color c3 = new Color(0xFF9900);
  
  /**
   * The number of NLL display bars.
   */
  private int numBars;
  
  /**
   * The width of one NLL display bar in LCARS panel pixels.
   */
  private int barWidth;
  
  /**
   * The height of one NLL display bar in LCARS panel pixels.
   */
  private int barHeight;

  /**
   * The maximal neglog likelihood difference to display.
   */
  private float maxNll = 21.9f;
  
  /**
   * The minimal neglog likelihood difference to display.
   */
  private float minNll = -11.9f;

  /**
   * The number of elements to remain on {@link #clear()}.
   */
  private int clearSize;

  /**
   * The currently displayed data.
   */
  private PostprocEvent data;
  
  /**
   * Display offset time in seconds.
   */
  private float offset;
  
  // -- Fields: GUI Elements --
  
  private ERect  eBwd;
  private EValue eLen;
  private ERect  eFwd;
  private ERect  eLock;
  
  // -- Constructors --
  
  public ESpeechPostproc(int x, int y, int numBars, int barWidth, int barHeight)
  {
    super(x,y);
    this.numBars   = numBars;
    this.barHeight = barHeight;
    this.barWidth  = barWidth;
    
    // Horizontal guidelines
    for (int nll=(int)Math.ceil(minNll/2)*2; nll<=maxNll; nll+=2)
    {
      int yNll = nllToDisplay(nll);
      if (nll%10==0)
      {
        ERect line = new ERect(null,0,yNll,(numBars+1)*barWidth+14,2,LCARS.ES_STATIC,null);
        line.setColor(c1);
        add(line);
        ERect mark = new ERect(null,(numBars+1)*barWidth,yNll-3,14,3,LCARS.ES_STATIC,null);
        mark.setColor(c1);
        add(mark);
        ELabel label = new ELabel(null,(numBars+1)*barWidth,yNll-17,14,14,LCARS.EF_TINY|LCARS.ES_LABEL_SW,nll+"");
        label.setColor(c1);
        add(label);
      }
      else
      {
        ERect line = new ERect(null,0,yNll,(numBars+1)*barWidth,1,LCARS.ES_STATIC,null);
        line.setColor(c2);
        add(line);
      }
    }
    
    // Label track backgrounds
    ERect e = new ERect(null,0,-18,numBars*barWidth,18,LCARS.ES_STATIC,null);
    e.setColor(c2);
    add(e);
    ELabel l = new ELabel(null,numBars*barWidth+3,-18,28,18,LCARS.ES_STATIC|LCARS.EF_SMALL|LCARS.ES_LABEL_W,"REC");
    l.setColor(c3);
    add(l);

    e = new ERect(null,0,barHeight,numBars*barWidth,18,LCARS.ES_STATIC,null);
    e.setColor(c2);
    add(e);
    l = new ELabel(null,numBars*barWidth+3,barHeight,28,18,LCARS.ES_STATIC|LCARS.EF_SMALL|LCARS.ES_LABEL_W,"REF");
    l.setColor(c1);
    add(l);

    // Frame
    int style = LCARS.EC_PRIMARY|LCARS.ES_SELECTED|LCARS.ES_STATIC;
    int h = nllToDisplay(0)+18;
    EElbo b = new EElbo(null,-21,-18,18,h,LCARS.ES_SHAPE_NW|style,null);
    b.setArcWidths(18,12); b.setArmWidths(6,18);
    add(b);
    e = new ERect(null,-15,nllToDisplay(0)-3,12,3,style,null);
    add(e);
    b = new EElbo(null,numBars*barWidth+27,-18,18,h,LCARS.ES_SHAPE_NE|style,null);
    b.setArcWidths(18,12); b.setArmWidths(6,18);
    add(b);
    e = new ERect(null,numBars*barWidth+30,nllToDisplay(0)-3,12,3,style,null);
    add(e);
    h = barHeight-nllToDisplay(0)+15;
    b = new EElbo(null,-21,nllToDisplay(0)+3,18,h,LCARS.ES_SHAPE_SW|style,null);
    b.setArcWidths(18,12); b.setArmWidths(6,18); b.setColor(c1);
    add(b);
    b = new EElbo(null,numBars*barWidth+27,nllToDisplay(0)+3,18,h,LCARS.ES_SHAPE_SE|style,null);
    b.setArcWidths(18,12); b.setArmWidths(6,18); b.setColor(c1);
    add(b);

    // Tool bar
    style = LCARS.EC_SECONDARY|LCARS.ES_DISABLED;
    eBwd = new ERect(null,-21,barHeight+42,76,38,style|LCARS.ES_RECT_RND_W|LCARS.ES_LABEL_W,"<");
    eBwd.addEEventListener(new EEventListenerAdapter()
    {
      @Override
      public void touchDown(EEvent ee)
      {
        setPostprocResult(data,offset-0.1f);
      }

      @Override
      public void touchHold(EEvent ee)
      {
        if (ee.ct>10)
          setPostprocResult(data,offset-0.1f);
      }
    });
    add(eBwd);

    EValue v = new EValue(null,58,barHeight+42,numBars*barWidth-530,38,style|LCARS.ES_STATIC|LCARS.ES_LABEL_W,null);
    v.setValue("PHONETIC/NLL"); v.setValueMargin(numBars*barWidth-705);
    add(v);

    eLen = new EValue(null,numBars*barWidth-462,barHeight+42,70,38,style|LCARS.ES_STATIC|LCARS.ES_LABEL_W,null);
    eLen.setValue("0.00");
    add(eLen);
    l = new ELabel(null,numBars*barWidth-411,barHeight+42,18,38,LCARS.ES_LABEL_C|LCARS.ES_STATIC,"s");
    l.setColor(Color.BLACK);
    add(l);
    
    eLock = new ERect(null,numBars*barWidth-389,barHeight+42,96,38,LCARS.EC_SECONDARY|LCARS.ES_LABEL_E,"LOCK");
    eLock.addEEventListener(new EEventListenerAdapter()
    {
      @Override
      public void touchDown(EEvent ee)
      {
        eLock.setBlinking(!eLock.isBlinking());
      }
    });
    add(eLock);
    
    e = new ERect(null,numBars*barWidth-290,barHeight+42,96,38,LCARS.EC_SECONDARY|LCARS.ES_LABEL_E,"CLEAR");
    e.addEEventListener(new EEventListenerAdapter()
    {
      @Override
      public void touchDown(EEvent ee)
      {
        setPostprocResult(null);
      }
    });
    add(e);
    
    v = new EValue(null,numBars*barWidth-195,barHeight+42,86,18,style|LCARS.ES_STATIC,null);
    v.setValue("NLL DIFFERENCE"); v.setValueMargin(0);
    add(v);
    e = new ERect(null,numBars*barWidth-109,barHeight+42,7,18,LCARS.ES_STATIC|LCARS.ES_RECT_RND,null);
    e.setColor(cLsr);
    add(e);
    v = new EValue(null,numBars*barWidth-89,barHeight+42,56,18,style|LCARS.ES_STATIC,null);
    v.setValue("POSTERIOR"); v.setValueMargin(11);
    add(v);
    e = new ERect(null,numBars*barWidth-191,barHeight+62,78,18,style|LCARS.ES_STATIC,null);
    add(e);
    e = new ERect(null,numBars*barWidth-109,barHeight+62,7,18,LCARS.ES_STATIC|LCARS.ES_RECT_RND,null);
    e.setColor(cNll);
    add(e);
    v = new EValue(null,numBars*barWidth-83,barHeight+62,53,18,style|LCARS.ES_STATIC,null);
    v.setValue("OBSERVATION"); v.setValueMargin(0);
    add(v);
    
    eFwd = new ERect(null,numBars*barWidth-31,barHeight+42,76,38,style|LCARS.ES_RECT_RND_E|LCARS.ES_LABEL_E,">");
    eFwd.addEEventListener(new EEventListenerAdapter()
    {
      @Override
      public void touchDown(EEvent ee)
      {
        setPostprocResult(data,offset+0.1f);
      }

      @Override
      public void touchHold(EEvent ee)
      {
        if (ee.ct>10)
          setPostprocResult(data,offset+0.1f);
      }
    });
    add(eFwd);
    
    // All elements instantiated until now are not to be cleared
    clearSize = getElements().size();
  }

  /**
   * Sets and display new recognition post-processing information.
   * 
   * @param pe
   *          A recognition post-processing event.
   */
  public void setPostprocResult(PostprocEvent pe)
  {
    setPostprocResult(pe,0);
  }
  
  /**
   * Sets and display new recognition post-processing information.
   * 
   * @param pe
   *          A recognition post-processing event.
   * @param offset
   *          Time offset of the display (in seconds).
   */
  public void setPostprocResult(PostprocEvent pe, float offset)
  {
    if (eLock.isBlinking() && pe!=null && data!=null && pe!=data) return;
    
    this.data   = pe;
    this.offset = Math.max(offset,0);
    
    // Clear display
    clear();
    
    // Add markers and bars
    String prevRecPhn = null;
    String prevRefPhn = null;
    float  dlsr       = 0;
    float  dnll       = 0;
    int    ofs        = (int)(100*this.offset);
    int    bar;
    for (bar=0; bar<numBars; bar++)
    {
      if (data==null) break;
      if (bar+ofs>=data.frames.size()) break;
      PostprocEvent.Frame f = data.frames.get(bar+ofs);
      dlsr = f.dlsr;
      dnll = f.dnll;
      
      if (prevRefPhn!=null && !prevRefPhn.equals(f.refPhn))
        addMarker(1,prevRefPhn,bar,Math.min(Math.min(dlsr,dnll),0));
      if (prevRecPhn!=null && !prevRecPhn.equals(f.recPhn))
        addMarker(0,prevRecPhn,bar,Math.max(Math.max(dlsr,dnll),0));
      prevRecPhn = f.recPhn;
      prevRefPhn = f.refPhn;

      addBar(bar,cLsr,dlsr);
      addBar(bar,cNll,dnll);
    }
    if (bar+ofs==data.frames.size())
    {
      addMarker(1,prevRefPhn,bar-1,Math.min(Math.min(dlsr,dnll),0));
      addMarker(0,prevRecPhn,bar-1,Math.max(Math.max(dlsr,dnll),0));
    }

    // Adjust tool bar buttons
    eBwd.setDisabled(data==null || ofs<=0);
    eFwd.setDisabled(data==null || ofs+numBars>=data.frames.size());
    eLen.setValue(data!=null?data.frames.size()/100f+"":"0.00");
    if (data==null) eLock.setBlinking(false);
    
    // Time scale
    if (data!=null)
    {
      ERect  e;
      ELabel l = null;
      for (bar = 0; bar<numBars; bar+=10)
      {
        int w = (bar+ofs)%50==0 ? 2 : 1;
        e = new ERect(null,bar*barWidth+barWidth/2,-18,w,barHeight+57,LCARS.ES_STATIC,null);
        e.setColor(c2);
        add(e);
        if ((bar+ofs)%50==0)
        {
          e = new ERect(null,bar*barWidth+barWidth/2,barHeight+21,3,18,LCARS.ES_STATIC,null);
          e.setColor(c1);
          add(e);
          String s = String.format(Locale.ENGLISH,"%.1f",(bar+ofs)/100f);
          l = new ELabel(null,bar*barWidth+barWidth/2+6,barHeight+21,18,18,LCARS.ES_STATIC|LCARS.EF_TINY|LCARS.ES_LABEL_SW,s);
          l.setColor(c1);
          add(l);
        }
      }
      if (l!=null)
        l.setLabel(l.getLabel()+" s");
    }
  }

  // -- Operations --
  
  protected void clear()
  {
    while (getElements().size()>clearSize)
      remove(getElements().get(clearSize));
  }
  
  protected void addMarker(int track, String label, int bar, float nll)
  {
    if (track!=0) track = 1;
    Color c  = track==0 ? c3 : c1;
    int   x  = bar*barWidth+barWidth/2;
    int   y  = track==0 ? 0 : Math.max(nllToDisplay(0),nllToDisplay(nll))+barWidth/2; 
    int   h  = track==0 ? nllToDisplay(nll)-barWidth/2 : barHeight-y;
    int   dy = track==0 ? -18 : h;
    ERect e  = new ERect(null,x,y,1,h,LCARS.ES_STATIC,null);
    e.setColor(c);
    add(e);
    e = new ERect(null,x-18,y+dy,19,18,LCARS.ES_STATIC|LCARS.EF_TINY|LCARS.ES_LABEL_C,label);
    e.setColor(c);
    add(e);
  }
  
  protected void addBar(int bar, Color color, float nll)
  {
    int yNull = nllToDisplay(0);
    int yNll  = nllToDisplay(nll);
    int x     = bar*barWidth;
    int y     = -barWidth/2+Math.min(yNull,yNll);
    int h     = Math.abs(yNll-yNull)+barWidth; 
    
    ERect e = new ERect(null,x,y,barWidth,h,LCARS.ES_STATIC|LCARS.ES_RECT_RND,null);
    e.setColor(color);
    e.setAlpha(color.getAlpha()/255f);
    add(e);
  }

  // -- Auxiliary methods --
  
  protected int nllToDisplay(float nll)
  {
    nll = Math.min(Math.max(nll,minNll),maxNll);
    return barHeight-(int)((nll-minNll)/(maxNll-minNll)*barHeight);
  }
  
}

// EOF

