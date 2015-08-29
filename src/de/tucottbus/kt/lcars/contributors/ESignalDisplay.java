package de.tucottbus.kt.lcars.contributors;

import java.awt.Color;
import java.awt.Rectangle;
import java.util.Timer;
import java.util.TimerTask;

import de.tucottbus.kt.lcars.LCARS;
import de.tucottbus.kt.lcars.Panel;
import de.tucottbus.kt.lcars.elements.EElbo;
import de.tucottbus.kt.lcars.elements.EElement;
import de.tucottbus.kt.lcars.elements.ELabel;
import de.tucottbus.kt.lcars.elements.ERect;
import de.tucottbus.kt.lcars.util.Range;

public class ESignalDisplay extends ElementContributor
{
  public static final int MODE_STATIC     = 0x0000;
  public static final int MODE_SINGLE     = 0x0001;
  public static final int MODE_CONTINUOUS = 0x0002;
  public static final int MODE_ANIMATION  = 0x000F;
  public static final int MODE_NOSAMPLES  = 0x0010;
  public static final int MODE_NOCURSOR   = 0x0020;
  
  private SampleProvider  sampleProvider;
  private int             curSample;
  private int             sampleCount;
  private int             sampleWidth;
  private float           sampleTime;
  private Color           sampleColor   = new Color(0.598f,0.598f,1f,1f);
  private float           timeGridMajor = 5f;
  private float           timeGridMinor = 1f;
  private long            period;
  private int             mode;
  private int             height;
  private Timer           animation;
  private boolean         locked;
  private ERect           cursor;
  private ELabel          cursorLab;
  private EElbo[]         eFrame;
  private int             frameStyle = LCARS.EC_ELBOUP|LCARS.ES_SELECTED|LCARS.ES_STATIC;

  private float displayToSeconds(int pos)
  {
    return (float)pos/(float)sampleWidth*(sampleTime/1000f);
  }

  private int secondsToDisplay(float time)
  {
    return (int)Math.round(time/sampleTime*1000f*sampleWidth);
  }
  
  @SuppressWarnings("unused")
  private int displayToAmp(int pos)
  {
    return -pos*65535/this.height;
  }

  private int ampToDisplay(int amp)
  {
    return -amp*this.height/65535;
  }
  
  /**
   * 
   * @param x
   * @param y
   * @param sampleCount
   * @param sampleWidth
   * @param sampleTime
   *          Time increment per sample in milliseconds.
   * @param height
   * @param mode
   */
  public ESignalDisplay(int x, int y, int sampleCount, int sampleWidth, float sampleTime, int height, int mode)
  {
    super(x,y);
    this.sampleCount = sampleCount;
    this.sampleWidth = sampleWidth;
    this.sampleTime  = sampleTime;
    this.mode        = mode;
    this.height      = height;
  }

  public synchronized void layout()
  {
    // Remove all current elements
    getElements().removeAllElements();

    // The Samples
    if ((mode&MODE_NOSAMPLES)==0)
    {
      ERect er;
      for (int i=0; i<sampleCount; i++)
      {
        er = new ERect(null,sampleWidth*i,0,sampleWidth,1,LCARS.ES_STATIC|LCARS.ES_RECT_RND,null);
        er.setArc(4*sampleWidth);
        er.setColor(sampleColor);
        add(er);
      }
    }
    
    // The grid and the cursor
    ERect line;
    ERect mark;
    ELabel label;
    int   hh    = this.height/2;
    int   width = sampleWidth*sampleCount;
    int   style = LCARS.EC_ELBOLO|LCARS.ES_SELECTED|LCARS.ES_STATIC;
    Color c1    = new Color(137,101,70);
    Color c2    = new Color(137,101,70,64);
    int   aux   = (int)Math.pow(10,-Math.floor(Math.log10(timeGridMinor)));
    if (aux<1) aux=1;
    for (float time=0f; time<displayToSeconds(sampleCount*sampleWidth); time+=timeGridMinor)
    {
      time = Math.round(time*aux)/(float)aux;
      int i = secondsToDisplay(time);
      /* if ( time%timeGridMajor==0)*/
      if (Math.abs(Math.IEEEremainder(time,timeGridMajor))<displayToSeconds(sampleWidth))
      {
        line = new ERect(null,i,-hh,2,height+14,style,null);
        line.setColor(c1); add(line);
        mark = new ERect(null,i+2,hh,3,14,style,null);
        mark.setColor(c1); add(mark);
        label = new ELabel(null,i+7,hh+2,20,14,LCARS.EF_TINY|LCARS.ES_LABEL_SW,time+"");
        label.setColor(c1); add(label);
      }
      else
      {
        line = new ERect(null,i,-hh,2,height,style,null);
        line.setColor(c2);
        add(line);
      }
    }
    for (int i=-30000; i<=30000; i+=10000)
    {
      int pos = ampToDisplay(i);
      if (i%30000==0)
      {
        line = new ERect(null,0,pos,width+14,2,style,null);
        line.setColor(c1); add(line);
        mark = new ERect(null,width,pos-3,14,3,style,null);
        mark.setColor(c1); add(mark);
        label = new ELabel(null,width,pos-16,20,14,LCARS.EF_TINY|LCARS.ES_LABEL_SW,(i/1000)+(i==0?"":"K"));
        label.setColor(c1); add(label);
      }
      else if (i%10000==0)
      {
        line = new ERect(null,0,pos,width,2,style,null);
        line.setColor(c2);
        add(line);
      }
    }
    if ((mode&MODE_NOCURSOR)==0)
    {
      cursor = new ERect(null,0,hh,4,14,style,null); add(cursor);
    }
    cursorLab = new ELabel(null,width-1,hh+2,20,14,style|LCARS.EF_TINY|LCARS.ES_LABEL_SE,"s"); add(cursorLab);
    
    // The frame
    eFrame = new EElbo[4]; 
    style = frameStyle;
    eFrame[0] = new EElbo(null,-12,-hh,10,hh,style|LCARS.ES_SHAPE_NW,null);
    eFrame[0].setArmWidths(4,18); eFrame[0].setArcWidths(10,5); add(eFrame[0]);
    eFrame[1] = new EElbo(null,-12,0,10,hh+14,style|LCARS.ES_SHAPE_SW,null);
    eFrame[1].setArmWidths(4,18); eFrame[1].setArcWidths(10,5); add(eFrame[1]);
    eFrame[2] = new EElbo(null,width+22,-hh,10,hh,style|LCARS.ES_SHAPE_NE,null);
    eFrame[2].setArmWidths(4,18); eFrame[2].setArcWidths(10,5); add(eFrame[2]);
    eFrame[3] = new EElbo(null,width+22,0,10,hh+14,style|LCARS.ES_SHAPE_SE,null);
    eFrame[3].setArmWidths(4,18); eFrame[3].setArcWidths(10,5); add(eFrame[3]);
  }
  
  public void reset()
  {
    for (int i=0; i<sampleCount; i++)
      setSample(i,new Range(0,0),null);
    curSample = -1;
    addSample(new Range(0,0),null);
  }

  public void setTimeGrid(float major, float minor, boolean layout)
  {
    this.timeGridMajor = major;
    this.timeGridMinor = minor;
    if (layout) layout();
  }
  
  public void setFrameStyle(int frameStyle)
  {
    this.frameStyle = frameStyle|LCARS.ES_STATIC;
  }
  
  public boolean getLocked()
  {
    return this.locked;
  }
  
  public void setLocked(boolean locked)
  {
    if (this.locked==locked) return;
    this.locked = locked;
    if (!this.locked) reset();
  }

  public void setSampleProvider(SampleProvider sampleProvider)
  {
    this.sampleProvider = sampleProvider;
  }
  
  public void setSample(int sample, Range value, Color color)
  {
    if (sample<0 || sample>=sampleCount) return;
    if ((mode&MODE_NOSAMPLES)!=0) return;
    EElement el = getElements().get(sample);
    Rectangle bounds = el.getBounds();
    int min = ampToDisplay((int)value.min);
    int max = ampToDisplay((int)value.max);
    bounds.y      = this.y+max - ((min-max)<3?1:0);
    bounds.height = Math.max((min-max),3);
    el.setBounds(bounds);
    el.setColor(color);    
  }

  @Override
  public void addToPanel(Panel panel)
  {
    if (getElements().size()==0) layout();
    super.addToPanel(panel);
    //reset();
    animate();
  }

  @Override
  public void removeFromPanel()
  {
    if (animation!=null) animation.cancel();
    super.removeFromPanel();
  }

  public void animate()
  {
    if (panel==null) return;
    if ((mode&MODE_NOSAMPLES)!=0) return;

    this.animation = new Timer("ESignalDisplay.timer");
    long period = (this.period>0)?this.period:(int)Math.max(sampleTime,1);
    animation.schedule(new AnimationTask(),period,period);
    
  }
  
  /**
   * Adds a new sample to this signal display.
   * 
   * @param value
   *          The sample (minimum and maximum value).
   * @param color
   *          The color of the sample, can be <code>null</code>.
   * @return The running sample index (starts over when display is full)
   */
  public synchronized int addSample(Range value, Color color)
  {
    curSample++;
    if (curSample>=sampleCount) curSample=0;

    getElements().get(curSample).setColor(new Color(1.f,1.f,1.f,1.f));
    for (int i=0; i<sampleCount; i++)
    {
      EElement el = getElements().get(i);
      if (i==curSample)
      {
        if (color==null)
          color=sampleColor;
        else
          for (int j=curSample-1; j>=0 && j>=curSample-6; j--)
          {
            EElement el2 = getElements().get(j);
            int alpha = el2.getBgColor().getAlpha();
            Color col2 = new Color(color.getRed(),color.getGreen(),color.getBlue(),alpha);
            el2.setColor(col2);
          }
        setSample(curSample,value,color);
      }
      else
      {
        float alpha = 1f;
        if (i<curSample) alpha = (float)(i+sampleCount-curSample)/(float)sampleCount;
        if (i>curSample) alpha = (float)(i-curSample)/(float)sampleCount;
        alpha = (float)Math.pow(Math.max(alpha-0.1,0.0),0.7);
        Color c = el.getBgColor();
        el.setColor(new Color(c.getRed(),c.getGreen(),c.getBlue(),(int)(255*alpha)));
      }
    }
    if (cursor!=null)
    {
      Rectangle bounds = cursor.getBounds();
      bounds.x=x+sampleWidth*curSample;
      cursor.setBounds(bounds);
      cursorLab.setLabel(Math.round(displayToSeconds(sampleWidth*curSample)*10)/10f+" s");
    }
    if (panel!=null) panel.invalidate();
    return curSample;
  }
  
  public synchronized EElement getSampleElement(int sample)
  {
    if (sample<0 || sample>=sampleCount) return null;
    if ((mode&MODE_NOSAMPLES)!=0) return null;
    return getElements().get(sample);
  }
  
  class AnimationTask extends TimerTask
  {
    public void run()
    {
      if (panel==null                       ) { cancel(); return; }
      if ((mode&MODE_NOSAMPLES)!=0          ) { cancel(); return; }
      if ((mode&MODE_ANIMATION)==MODE_STATIC) { cancel(); return; }
      if (locked) return;

      Range smp = new Range(0,0);
      Color col = null; 
      if (sampleProvider!=null)
      {
        smp = sampleProvider.getSample();
        col = sampleProvider.getSampleColor();
      }
      int i = addSample(smp,col);
      if (i==sampleCount-1 && (mode&MODE_ANIMATION)==MODE_SINGLE)
      {
        cancel();
        return;
      }
    }
  }

}
