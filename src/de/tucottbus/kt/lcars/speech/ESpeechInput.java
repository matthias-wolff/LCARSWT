package de.tucottbus.kt.lcars.speech;

import java.awt.Color;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Area;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import de.tucottbus.kt.lcars.LCARS;
import de.tucottbus.kt.lcars.Panel;
import de.tucottbus.kt.lcars.contributors.ElementContributor;
import de.tucottbus.kt.lcars.elements.EElbo;
import de.tucottbus.kt.lcars.elements.EElement;
import de.tucottbus.kt.lcars.elements.ELabel;
import de.tucottbus.kt.lcars.elements.ERect;
import de.tucottbus.kt.lcars.elements.EValue;
import de.tucottbus.kt.lcars.j2d.GArea;
import de.tucottbus.kt.lcars.j2d.Geometry;
import de.tucottbus.kt.lcars.speech.events.RecognitionEvent;

/**
 * EXPERIMENTAL: Display of speech level and recognition result details.
 *  
 * @author Matthias Wolff
 */
public class ESpeechInput extends ElementContributor
{
  private EElbo[]           eFrame  = new EElbo[7];
  private ERect[]           eCursor = new ERect[2];

  private EElement[]        eFvrFrame = new EElement[5];
  private EFvrValue         eFvrVal;
  private EValue            eLex;
  private EValue            eAccept;
  private EValue            eConf;
  private EValue            eNad;
  private EValue            eNed;

  private int               height;
  private int               numBars;
  private int               barHeight;
  private Timer             runt;
  private int               hilightCtr;
  private long              lvlCount;
  private float             lvlValue;
  private int               marqueeCtr;
  private ArrayList<String> lexValue = new ArrayList<String>();

  private static final int  LEX_MAXLENGTH = 60;
  
  public ESpeechInput(int x, int y, int width, int height)
  {
    super(x,y);
    this.height    = height;
    this.barHeight = 3;
    int hh         = height/2;
    
    // Add level meter bars
    this.numBars  = (hh-7)/barHeight*2;
    int numHi     = numBars*4/5;
    int numMid    = numBars*1/3;
    Color clrHi   = new Color(0x11FF0066,true);
    Color clrMid  = new Color(0x11CCCC66,true);
    Color clrLo   = new Color(0x110066FF/*0x3366FF00*/,true); 
    for (int i=0; i<this.numBars; i++)
    {
      ERect e = new ERect(null,width-40,+hh-i*barHeight-barHeight,52,barHeight,LCARS.ES_STATIC,null);
      if (i>numHi)
        e.setColor(LCARS.interpolateColors(clrMid,clrHi,(float)(i-numHi)/(float)(numBars-numHi)));
      else if (i>numMid)
        e.setColor(LCARS.interpolateColors(clrLo,clrMid,(float)(i-numMid)/(float)(numHi-numMid)));
      else
        e.setColor(clrLo);
      add(e);
    }
    
    // The level meter frame and cursor
    int style = LCARS.EC_ELBOUP|LCARS.ES_STATIC;
    int y1 = +hh-numBars*barHeight;
    int y2 = +hh;
    add(new ERect(null,width-41,y1,1,y2-y1,style,null));
    add(new ERect(null,width+12,y1,1,y2-y1,style,null));
    add(new ERect(null,width-41,y1,54,1,style,null));
    add(new ERect(null,width-41,y2,54,1,style,null));
    //eFrame[0] = new EElbo(null,width-80,-hh,14,hh,style|LCARS.ES_SHAPE_NW,null);
    //eFrame[0].setArmWidths(4,8); eFrame[0].setArcWidths(18,10); add(eFrame[0]);
    //eFrame[1] = new EElbo(null,width-80,0,14,hh+14,style|LCARS.ES_SHAPE_SW,null);
    //eFrame[1].setArmWidths(4,8); eFrame[1].setArcWidths(18,10); add(eFrame[1]);
    eFrame[2] = new EElbo(null,width+22,-hh,20,28,style|LCARS.ES_SHAPE_NE,null);
    eFrame[2].setArmWidths(12,8); eFrame[2].setArcWidths(18,10); add(eFrame[2]);
    add(new ELabel(null,width+50,-hh+2,50,14,LCARS.EF_TINY|LCARS.EC_PRIMARY|LCARS.ES_STATIC|LCARS.ES_LABEL_SW," LEVEL"));
    float shf = (float)(height-46f)/3f;
    for (int i=0; i<3; i++)
    {
      int sy = -hh+29+Math.round(i*shf);
      int sh = -hh+29+Math.round((i+1)*shf)-sy-1;
      eFrame[i+3] = new EElbo(null,width+30,sy,18,sh,style|LCARS.ES_SHAPE_NW,null);
      eFrame[i+3].setArmWidths(12,3); eFrame[i+3].setArcWidths(1,1);
      add(eFrame[i+3]);
      String llabel = (i>0?"-":" ")+i*20; 
      add(new ELabel(null,width+50,sy-6,25,14,LCARS.EF_TINY|LCARS.EC_PRIMARY|LCARS.ES_STATIC|LCARS.ES_LABEL_SW,llabel));
    }
    int sy = -hh+29+Math.round(3*shf);
    add(new ERect(null,width+30,sy,18,3,style|LCARS.ES_SHAPE_NW,null));
    add(new ELabel(null,width+50,sy-6,25,14,LCARS.EF_TINY|LCARS.EC_PRIMARY|LCARS.ES_STATIC|LCARS.ES_LABEL_SW,"-60"));
    add(new ELabel(null,width+50,sy+12,25,14,LCARS.EF_TINY|LCARS.EC_PRIMARY|LCARS.ES_STATIC|LCARS.ES_LABEL_SW," dB"));
    eFrame[6] = new EElbo(null,width+22,hh-14,20,28,style|LCARS.ES_SHAPE_SE,null);
    eFrame[6].setArmWidths(12,8); eFrame[6].setArcWidths(18,10); add(eFrame[6]);
    eCursor[0] = new ERect(null,width+17,hh-1,22,14,LCARS.ES_RECT_RND|LCARS.ES_STATIC,null);
    eCursor[0].setColor(Color.black); add(eCursor[0]);
    eCursor[1] = new ERect(null,width+18,hh,20,12,LCARS.ES_RECT_RND|LCARS.ES_STATIC,null);
    eCursor[1].setColor(Color.white); add(eCursor[1]);
    
    // The speech engine elements
    eFvrFrame[0] = new EElbo(null,0,-96,72,62,LCARS.EC_ELBOUP|LCARS.ES_SELECTED|LCARS.ES_STATIC|LCARS.ES_SHAPE_NW|LCARS.ES_LABEL_NE,"FVR");
    ((EElbo)eFvrFrame[0]).setArmWidths(72,62); ((EElbo)eFvrFrame[0]).setArcWidths(38,1);
    add(eFvrFrame[0]);
    eFvrFrame[1] = new EElbo(null,0,-34,72,62,LCARS.EC_ELBOUP|LCARS.ES_SELECTED|LCARS.ES_STATIC|LCARS.ES_SHAPE_SW,null);
    ((EElbo)eFvrFrame[1]).setArmWidths(9,6); ((EElbo)eFvrFrame[1]).setArcWidths(38,28);
    add(eFvrFrame[1]);
    eFvrFrame[2] = new EElbo(null,width-71,-96,19,62,LCARS.EC_ELBOUP|LCARS.ES_SELECTED|LCARS.ES_STATIC|LCARS.ES_SHAPE_NE,null);
    ((EElbo)eFvrFrame[2]).setArmWidths(72,62); ((EElbo)eFvrFrame[2]).setArcWidths(38,1);
    add(eFvrFrame[2]);
    eFvrFrame[3] = new EElbo(null,width-71,-34,19,62,LCARS.EC_ELBOUP|LCARS.ES_SELECTED|LCARS.ES_STATIC|LCARS.ES_SHAPE_SE,null);
    ((EElbo)eFvrFrame[3]).setArmWidths(72,62); ((EElbo)eFvrFrame[3]).setArcWidths(38,1);
    add(eFvrFrame[3]);
    eFvrFrame[4] = new ERect(null,75,22,width-149,6,LCARS.EC_ELBOUP|LCARS.ES_STATIC,null);
    add(eFvrFrame[4]);

    eFvrVal = new EFvrValue(null,75,-96,width-149,115,LCARS.EC_ELBOUP|LCARS.ES_SELECTED|LCARS.ES_STATIC,null);
    add(eFvrVal);

    eLex = new EValue(null,0,35,width-52,38,LCARS.EC_ELBOUP|LCARS.ES_RECT_RND|LCARS.ES_LABEL_E|LCARS.ES_VALUE_W|LCARS.ES_STATIC,"LEX");
    eLex.setValueWidth(width-143);
    add(eLex);

    eAccept = new EValue(null,width-147,76,95,38,LCARS.EC_ELBOUP|LCARS.ES_SELECTED|LCARS.ES_RECT_RND_E|LCARS.ES_STATIC,null);
    add(eAccept);
    eConf = new EValue(null,width-285,76,135,38,LCARS.EC_ELBOUP|LCARS.ES_SELECTED|LCARS.ES_RECT_RND_W|LCARS.ES_LABEL_E|LCARS.ES_STATIC,"CONF");
    eConf.setValueMargin(0); eConf.setValueWidth(66); eConf.setValue("0.00");
    add(eConf);
    eNed = new EValue(null,width-488,76,200,38,LCARS.EC_ELBOUP|LCARS.ES_RECT_RND_E|LCARS.ES_LABEL_E|LCARS.ES_STATIC,"NED");
    add(eNed);
    eNad = new EValue(null,width-701,76,210,38,LCARS.EC_ELBOUP|LCARS.ES_RECT_RND_W|LCARS.ES_LABEL_E|LCARS.ES_STATIC,"NAD");
    add(eNad);
  }

  public void setRecResult(RecognitionEvent event)
  {
    if (event.incremenral==false)
    {
      // Final result
      eFvrVal.setLabel(event.result!=null?event.result:"?");
      eAccept.setValue(event.accepted?"ACC":"REJ");
      eConf.setValue(String.format(Locale.US,"%3.2f",event.confidence));
      
      lexValue = makeLexValue(event.text!=null ? event.text.toUpperCase() : "?");
      eLex.setValue(lexValue.size()==1?lexValue.get(0):"");
      
      Color color = event.accepted ? new Color(0x00FF66) : new Color (0xFF0066);
      for (EElement e : eFvrFrame) e.setColor(color);
      eFvrVal.setColor(color);
      eAccept.setColor(color);
      eConf.setColor(color);
  
      float f1 = event.getDetailFloat("nad" ,0);
      float f2 = event.getDetailFloat("tnad",0);
      eNad.setValue(String.format(Locale.US,"%3.2f/%3.2f",f1,f2));
  
      f1 = event.getDetailFloat("ned" ,0);
      f2 = event.getDetailFloat("tned",0);
      eNed.setValue(String.format(Locale.US,"%3.2f/%3.2f",f1,f2));
  
      marqueeCtr = 0;
      hilightCtr = 50;
    }
    else
    {
      // Incremental result
      lexValue = makeLexValue(event.text!=null ? event.text.toUpperCase() : "?");
      eLex.setValue(lexValue.size()==1?lexValue.get(0):"");
      marqueeCtr = 0;
    }

    /* Other information in event:
    eRres  .setLabel(event.getDetail("reference",null));
    eXrt   .setValue("?");
    eScore .setValue(String.format(Locale.US,"%4.0f",event.getDetailFloat("gw.res",0)));
    eRscore.setValue(String.format(Locale.US,"%4.0f",event.getDetailFloat("gw.ref",0)));
    */
    
    if (panel!=null) panel.invalidate();
  }

  private ArrayList<String> makeLexValue(String string)
  {
    ArrayList<String> result = new ArrayList<String>();
    if (string==null) string = "";

    while (string.length()>0)
    {
      int endIndex = Math.min(LEX_MAXLENGTH,string.length());
      if (endIndex==LEX_MAXLENGTH)
        for (; endIndex>LEX_MAXLENGTH-20; endIndex--)
          if (endIndex>=0 && endIndex<string.length() && Character.isWhitespace(string.charAt(endIndex)))
            break;
      result.add(string.substring(0,endIndex).trim());
      string = string.substring(endIndex).trim();
    }
    
    if (result.isEmpty()) result.add("");
    return result;
  }
  
  /**
   * Displays a new level.
   * 
   * @param level
   *          The level.
   */
  public void setLevel(float level)
  {
    lvlValue = level;
    lvlCount = 0;
    setLevelInt(level);
  }
  
  private void setLevelInt(float level)
  {
    level = Math.min(level,  3);
    level = Math.max(level,-63);
    int curLevel = (int)((level+63f)/66f*numBars);
    for (int i=0; i<numBars; i++)
    {
      ERect bar = (ERect)getElements().get(i);
      Color clr = bar.getBgColor();
      float alpha = 0.1f;
      if (i<curLevel)
        alpha = (float)Math.pow((float)(i)/(float)curLevel+0.3,1.2);
      if (alpha<0f) alpha=0f;
      if (alpha>1f) alpha=1f;
      bar.setColor(new Color(clr.getRed(),clr.getGreen(),clr.getBlue(),(int)(alpha*255)));
      Rectangle b0 = eCursor[0].getBounds(); b0.y=y+height/2-curLevel*barHeight-5;
      eCursor[0].setBounds(b0);
      Rectangle b1 = eCursor[1].getBounds(); b1.y=y+height/2-curLevel*barHeight-4;
      eCursor[1].setBounds(b1);
    }
    if (panel!=null) panel.invalidate();
  }
  
  @Override
  public void addToPanel(Panel panel)
  {
    super.addToPanel(panel);
    if (panel!=null)
    {
      this.runt = new Timer("ESpeechIo.timer");
      runt.schedule(new RunTask(),100,100);
    }
  }

  @Override
  public void removeFromPanel()
  {
    super.removeFromPanel();
  }  
  
  class RunTask extends TimerTask
  {
    public void run()
    {
      if (panel==null) { runt.cancel(); return; }
      
      // Softly clear level display
      lvlCount++;
      if (lvlCount>3) setLevelInt((lvlValue+96)/2-96);
      
      // Highlight off and auto mode
      if (hilightCtr>0)
      {
        hilightCtr--;
        if (hilightCtr==0)
        {
          for (EElement e : eFvrFrame) e.setColor(null);
          eFvrVal.setColor(null);
          eAccept.setColor(null);
          eConf.setColor(null);

          if (panel!=null && panel instanceof SpeechEnginePanel)
            if (((SpeechEnginePanel)panel).getModeUAuto())
              ((SpeechEnginePanel)panel).switchModeU(0);
        }
      }
      
      if (lexValue.size()>1)
      {
        
        if (marqueeCtr%LEX_MAXLENGTH ==0)
        {
          int line = ( marqueeCtr / LEX_MAXLENGTH ) % lexValue.size();
          String s = lexValue.get(line);
          if (line<lexValue.size()-1) s += "...";
          if (line>0) s = "..."+s;
          eLex.setValue(s);          
        }
        marqueeCtr++;
      }
      
      if (panel!=null) panel.invalidate();
    }
  }

  // -- Nested classes --

  /**
   * An LCARS {@link EElement} displaying feature-value relation strings. A
   * feature-value relation string represents tree structures with labeled nodes,
   * e.~g. <code>"[root[child1[grandchild]][child2]]"</code>.
   * 
   * @author Matthias Wolff
   */
  public static class EFvrValue extends EElement
  {
    
    /**
     * Create a new semantic value display.
     * 
     * @param panel
     *          The LCARS panel to place the GUI element on.
     * @param x
     *          The x-coordinate of the upper left corner (in LCARS panel pixels).
     * @param y
     *          The y-coordinate of the upper left corner (in LCARS panel pixels).
     * @param w
     *          The width (in LCARS panel pixels).
     * @param h
     *          The height (in LCARS panel pixels).
     * @param style
     *          The style (see class {@link LCARS}).
     * @param label
     *          The label (a feature-value relation string). A feature-value
     *          relation string represents tree structures with labeled nodes,
     *          e.~g. <code>"[root[child1[grandchild]][child2]]"</code>.
     */
    public EFvrValue
    (
      Panel  panel,
      int    x,
      int    y,
      int    w,
      int    h,
      int    style,
      String label
    )
    {
      super(panel,x,y,w,h,style,label);
    }
    
    @Override
    protected Vector<Geometry> createGeometriesInt()
    {
      Vector<Geometry> geos = new Vector<Geometry>();
      Rectangle        bnds = getBounds();
      Font             font = getFont();
      String           fvrs = label==null?"":label;
      int              xofs = 0;
      int              yofs = 0;
      int              linh = LCARS.getTextShape(font,"M").getBounds().height;
      int              yinc = linh/3;
  
      // Trim on leading and one tailing square brace from label 
      if (fvrs.startsWith("[") && fvrs.endsWith("]"))
        fvrs = fvrs.substring(1,fvrs.length()-1);
      
      // Count lines
      int lcnt = 0;
      int bcnt = 0;
      for (char c : fvrs.toCharArray())
        if (c=='[')
        {
          bcnt++;
          lcnt = Math.max(bcnt,lcnt);
        }
        else if (c==']')
          bcnt--;
      lcnt++;
  
      // Adjust line increment
      if (linh+lcnt*yinc>bnds.height)
        yinc = Math.max((bnds.height-linh)/lcnt,3);
      
      // Create lines
      Area[] lines = new Area[lcnt];
      for (int i=0; i<lines.length; i++)
        lines[i] = new Area(new Rectangle(bnds.x,bnds.y+i*yinc+linh+1,bnds.width,1));
      
      // Create node label geometries
      StringWriter sw = new StringWriter();
      for (int i=0; i<fvrs.length(); i++)
      {
        char c = fvrs.charAt(i);
        if (c=='[' || c==']' || i==fvrs.length()-1)
        {
          if (i==fvrs.length()-1 && c!='[' && c!=']')
            sw.append(c);
          if (sw.getBuffer().length()>0)
          {
            String    nlab = sw.toString();
            Rectangle tbnd = LCARS.getTextShape(font,nlab).getBounds();
            tbnd.x = bnds.x + xofs;
            tbnd.y = bnds.y + yofs - (int)(tbnd.height*0.15);
            tbnd.height = linh;
            for (Area area : lines)
            {
              Rectangle r = new Rectangle(tbnd);
              r.x-=2; r.y-=2; r.width+=4; r.height+=4;
              area.subtract(new Area(r));
            }
            geos.addAll(LCARS.createTextGeometry2D(font,nlab,tbnd,LCARS.ES_LABEL_NW,null,false));      
            xofs += tbnd.width + 6;
            sw = new StringWriter();
          }
          yofs += c=='[' ? yinc : -yinc;
        }
        else
          sw.append(c);
      }
  
      // Add line geometries
      for (Area area : lines)
        geos.add(new GArea(area,false));
      
      return geos;
    }
  
    /**
     * Returns a font which allows to display the semantic value within the bounds
     * of this element.
     */
    protected Font getFont()
    {
      Rectangle bnds  = getBounds();
      Font      font  = LCARS.getFont(LCARS.EF_LARGE);
      Shape     tshp  = LCARS.getTextShape(font,rawLabel(label));
      
      if (bnds==null || tshp==null) return font;
      if (tshp.getBounds().width<=bnds.width) return font;
  
      float size = font.getSize()*(float)bnds.width/(float)tshp.getBounds().width;
      return LCARS.getFont(LCARS.EF_LARGE,(int)(size));
    }
  
    /**
     * Removed heading and tailing braces, replaces every remaining sequence of
     * opening and closing square brackets by a space and returns the result of
     * this operation.
     * 
     * @param label
     *          The label string to be processed.
     */
    protected String rawLabel(String label)
    {
      if (label==null) return "";
      StringWriter sw = new StringWriter();
      boolean braceflag = true;
      for (char c : label.toCharArray())
        if (c!='[' && c!=']')
        {
          sw.append(c);
          braceflag = false;
        }
        else if (!braceflag)
        {
          sw.append(' ');
          braceflag = true;
        }
      return sw.toString().trim();
    }
  }  
  
}

// EOF
