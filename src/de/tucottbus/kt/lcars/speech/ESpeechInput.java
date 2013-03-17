package de.tucottbus.kt.lcars.speech;

import java.awt.Color;
import java.awt.Rectangle;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import de.tucottbus.kt.lcars.LCARS;
import de.tucottbus.kt.lcars.Panel;
import de.tucottbus.kt.lcars.contributors.ElementContributor;
import de.tucottbus.kt.lcars.elements.EElbo;
import de.tucottbus.kt.lcars.elements.ELabel;
import de.tucottbus.kt.lcars.elements.ERect;
import de.tucottbus.kt.lcars.elements.EValue;
import de.tucottbus.kt.lcars.speech.events.RecognitionEvent;

/**
 * EXPERIMENTAL: Display of speech level and recognition result details.
 *  
 * @author Matthias Wolff
 */
public class ESpeechInput extends ElementContributor
{
  private EElbo[] eFrame  = new EElbo[7];
  private ERect[] eCursor = new ERect[2];
  private ERect   eResultL;
  private EValue  eResult;
  private EValue  eAccept;
  private EValue  eNad;
  private EValue  eNed;
  private EValue  eTnad;
  private EValue  eTned;
  private EValue  eScore;
  private EValue  eRscore;
  private EValue  eXrt;
  private EValue  eRresFrame;
  private ELabel  eRres;
  private ERect   eConfirm;
  private ERect   ePoll;
  private int     width;
  private int     height;
  private int     numBars;
  private int     barHeight;
  private Timer   runt;
  private int     hilightCtr;
  private long    lvlCount;
  private float   lvlValue;

  public ESpeechInput(int x, int y, int width, int height)
  {
    super(x,y);
    this.width     = width;
    this.height    = height;
    this.barHeight = 3;
    int hh         = this.height/2;
    
    // Add level meter bars
    this.numBars  = (hh-7)/barHeight*2;
    int numHi     = numBars*4/5;
    int numMid    = numBars*1/3;
    Color clrHi   = new Color(0x11FF0066,true);
    Color clrMid  = new Color(0x11CCCC66,true);
    Color clrLo   = new Color(0x110066FF/*0x3366FF00*/,true); 
    for (int i=0; i<this.numBars; i++)
    {
      ERect e = new ERect(null,width-50,+hh-i*barHeight-barHeight,52,barHeight,LCARS.ES_STATIC,null);
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
    add(new ERect(null,width-51,y1,1,y2-y1,style,null));
    add(new ERect(null,width+2,y1,1,y2-y1,style,null));
    add(new ERect(null,width-51,y1,54,1,style,null));
    add(new ERect(null,width-51,y2,54,1,style,null));
    //eFrame[0] = new EElbo(null,width-80,-hh,14,hh,style|LCARS.ES_SHAPE_NW,null);
    //eFrame[0].setArmWidths(4,8); eFrame[0].setArcWidths(18,10); add(eFrame[0]);
    //eFrame[1] = new EElbo(null,width-80,0,14,hh+14,style|LCARS.ES_SHAPE_SW,null);
    //eFrame[1].setArmWidths(4,8); eFrame[1].setArcWidths(18,10); add(eFrame[1]);
    eFrame[2] = new EElbo(null,width+22,-hh,20,28,style|LCARS.ES_SHAPE_NE,null);
    eFrame[2].setArmWidths(12,8); eFrame[2].setArcWidths(18,10); add(eFrame[2]);
    add(new ELabel(null,width+50,-hh+2,50,14,LCARS.EF_TINY|LCARS.EC_PRIMARY|LCARS.ES_STATIC|LCARS.ES_LABEL_SW," LEVEL"));
    float shf = (float)(this.height-46f)/3f;
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
    eResultL = new ERect(null,0,-96,131,38,LCARS.EC_ELBOUP|LCARS.ES_SELECTED|LCARS.ES_RECT_RND_W|LCARS.ES_LABEL_E|LCARS.ES_STATIC,"RESULT");
    add(eResultL);
    
    eResult = new EValue(null,134,-96,this.width-308,38,LCARS.EC_ELBOUP|LCARS.ES_SELECTED|LCARS.ES_STATIC,null);
    eResult.setValueMargin(0); eResult.setValue("");
    add(eResult);

    eAccept = new EValue(null,this.width-174,-96,95,38,LCARS.EC_ELBOUP|LCARS.ES_SELECTED|LCARS.ES_RECT_RND_E|LCARS.ES_STATIC,null);
    eAccept.setValue("");
    add(eAccept);

    eXrt = new EValue(null,0,-47,216,38,LCARS.EC_ELBOUP|LCARS.ES_RECT_RND_W|LCARS.ES_LABEL_E|LCARS.ES_STATIC,"XRT");
    eXrt.setValueWidth(66); eXrt.setValue("0");
    add(eXrt);
    eScore = new EValue(null,0,-6,216,38,LCARS.EC_ELBOUP|LCARS.ES_RECT_RND_W|LCARS.ES_LABEL_E|LCARS.ES_STATIC,"NLL");
    eScore.setValueWidth(66); eScore.setValue("500");
    add(eScore);
    eRscore = new EValue(null,219,-6,178,38,LCARS.EC_ELBOUP|LCARS.ES_RECT_RND_E|LCARS.ES_LABEL_E|LCARS.ES_STATIC,"RNLL");
    eRscore.setValueWidth(66); eRscore.setValue("500");
    add(eRscore);
    eNad = new EValue(null,0,35,216,38,LCARS.EC_ELBOUP|LCARS.ES_RECT_RND_W|LCARS.ES_LABEL_E|LCARS.ES_STATIC,"NAD");
    eNad.setValueWidth(66); eNad.setValue("0.00");
    add(eNad);
    eNed = new EValue(null,219,35,178,38,LCARS.EC_ELBOUP|LCARS.ES_RECT_RND_E|LCARS.ES_LABEL_E|LCARS.ES_STATIC,"NED");
    eNed.setValueWidth(66); eNed.setValue("0.00");
    add(eNed);
    eTnad = new EValue(null,0,76,216,38,LCARS.EC_ELBOUP|LCARS.ES_RECT_RND_W|LCARS.ES_LABEL_E|LCARS.ES_STATIC,"TNAD");
    eTnad.setValueWidth(66); eTnad.setValue("0.00");
    add(eTnad);
    eTned = new EValue(null,219,76,178,38,LCARS.EC_ELBOUP|LCARS.ES_RECT_RND_E|LCARS.ES_LABEL_E|LCARS.ES_STATIC,"TNED");
    eTned.setValueWidth(66); eTned.setValue("0.00");
    add(eTned);
    eRresFrame = new EValue(null,219,-47,this.width-298,38,LCARS.EC_ELBOUP|LCARS.ES_RECT_RND_E|LCARS.ES_LABEL_E|LCARS.ES_STATIC,"RRES");
    eRresFrame.setValueWidth(this.width-410);
    add(eRresFrame);
    eRres = new ELabel(null,320,-42,485,28,LCARS.EC_ELBOUP|LCARS.ES_LABEL_W,null);
    add(eRres);
    eConfirm = new ERect(null,400,-6,(this.width-482)/2,120,LCARS.EC_SECONDARY|LCARS.ES_LABEL_SE,"CONFIRM");
    add(eConfirm);
    ePoll = new ERect(null,403+(this.width-482)/2,-6,(this.width-482)/2,120,LCARS.EC_SECONDARY|LCARS.ES_LABEL_SE,"POLL");
    add(ePoll);
  }

  public void setRecResult(RecognitionEvent event)
  {
    eResult.setValue(event.result);
    eRres  .setLabel(event.getDetail("reference",null));
    eAccept.setValue(event.accepted?"ACC":"REJ");
    eXrt   .setValue("?");
    eScore .setValue(String.format(Locale.US,"%4.0f",event.getDetailFloat("gw.res",0)));
    eRscore.setValue(String.format(Locale.US,"%4.0f",event.getDetailFloat("gw.ref",0)));
    eNad   .setValue(String.format(Locale.US,"%3.2f",event.getDetailFloat("nad"   ,0)));
    eTnad  .setValue(String.format(Locale.US,"%3.2f",event.getDetailFloat("tnad"  ,0)));
    eNed   .setValue(String.format(Locale.US,"%3.2f",event.getDetailFloat("ned"   ,0)));
    eTned  .setValue(String.format(Locale.US,"%3.2f",event.getDetailFloat("tned"  ,0)));
    
    Color color = event.accepted ? new Color(0x00FF66) : new Color (0xFF0066);
    eResultL.setColor(color);
    eResult .setColor(color);
    eAccept .setColor(color);
    hilightCtr = 50;
    
    if (panel!=null) panel.invalidate();
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
          eResultL.setColor(null);
          eResult.setColor(null);
          eAccept.setColor(null);
          if (panel!=null && panel instanceof SpeechEnginePanel)
            if (((SpeechEnginePanel)panel).getModeUAuto())
              ((SpeechEnginePanel)panel).switchModeU(0);
        }
      }
      
      if (panel!=null) panel.invalidate();
    }
  }

}

// EOF
