package de.tucottbus.kt.lcars.al;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.util.Locale;
import java.util.Vector;

import de.tucottbus.kt.lcars.LCARS;
import de.tucottbus.kt.lcars.Panel;
import de.tucottbus.kt.lcars.contributors.ElementContributor;
import de.tucottbus.kt.lcars.elements.EElbo;
import de.tucottbus.kt.lcars.elements.EElement;
import de.tucottbus.kt.lcars.elements.ELabel;
import de.tucottbus.kt.lcars.elements.ERect;
import de.tucottbus.kt.lcars.elements.EValue;
import de.tucottbus.kt.lcars.j2d.EPerspective;

/**
 * An animated audio levels display for stereo signals. The display contains a
 * number of bars whose length and horizontal position indicate past, present
 * and future levels of the signal.
 * 
 * @author Matthias Wolff
 */
public class ELevelsDisplay extends ElementContributor
{
  private int            barW;
  private int            barH;
  private int            barC;
  private int            barZ;
  private int            barF;
  private float          minL;
  private String         title;
  private EPerspective   perspective;
  private ELabel         eTimecode;
  private ELabel         eDuration;
  private Vector<ELabel> eHistory;
  private Vector<ELabel> eFuture;
  private Color          cBars;
  private Color          cGrid;

  // -- Constructors --
  
  /**
   * EXPERIMENTAL: Creates a new levels display.
   * 
   * @param x
   *          The x-coordinate of the center of the topmost level bar (in panel coordinates).
   * @param y
   *          The y-coordinate of the top of the topmost level bar (in panel coordinates).
   * @param barLength
   *          The length of one level bar (in panel coordinates).
   * @param barCount
   *          The number of level bars, preferably an integer multiple of 25.
   * @param zeroBar
   *          The index of the bar representing the current time code. Bars with indexes smaller
   *          than <code>zeroBar</code> represent audio frames in the past, bars with indexes
   *          greater than <code>zeroBar</code> represent audio frames in the future.
   */
  public ELevelsDisplay(int x, int y, int barLength, int barCount, int zeroBar)
  {
    super(x,y);
    barW = barLength/2;
    barH = 3;
    barC = barCount;
    barZ = barC-zeroBar;
    minL = -36;
    float[][] matrix = {{2.25f,0,0,0},{0,2.25f,0,0},{0,0,1,0},{0,-0.01f,6f,0}};
    perspective = new EPerspective(matrix,x,y);
  }

  // -- Getters and setters --
  
  /**
   * EXPERIMENTAL: Sets a perspective projection.
   * 
   * @param perspective
   *          The perspective. 
   */
  public void setPerspective(EPerspective perspective)
  {
    if (perspective==null) return;
    this.perspective = perspective;
    if (isDisplayed()) layout();
  }
  
  /**
   * EXPERIMENTAL: Sets the title of the display.
   * 
   * @param title
   *          The title.
   */
  public void setTitle(String title)
  {
    if (this.title==null && title==null) return;
    if (this.title!=null && this.title.equals(title)) return;
    this.title = title;
    if (isDisplayed()) layout();
  }

  /**
   * EXPERIMENTAL: Sets the duration label of the display.
   * 
   * @param duration
   *          The duration in seconds.
   */
  public void setDuration(float duration)
  {
    setTimeLabel(eDuration,duration);
  }
  
  /**
   * EXPERIMENTAL: Sets the color of the level bars.
   * 
   * @param color
   *          The color, can be <code>null</code> in order to select the default color.
   */
  public void setBarColor(Color color)
  {
    if (this.cBars==null && color==null) return;
    if (this.cBars!=null && this.cBars.equals(color)) return;
    this.cBars = color;
    if (isDisplayed()) layout();
  }

  /**
   * EXPERIMENTAL: Sets the color of the grid.
   * 
   * @param color
   *          The color, can be <code>null</code> in order to select the default color.
   */
  public void setGridColor(Color color)
  {
    if (this.cGrid==null && color==null) return;
    if (this.cGrid!=null && this.cGrid.equals(color)) return;
    this.cGrid = color;
    if (isDisplayed()) layout();
  }
  
  /**
   * EXPERIMENTAL: Sets new audio data.
   * 
   * @param audio
   *          The audio data (as obtained by {@link AudioTrack#getWindow()}).
   * @param time
   *          The current media time in seconds.
   */
  public void setAudioWindow(Vector<AudioBuffer> audio, float time)
  {
    if (!isDisplayed()) return;
    
    if (audio==null)
    {
      audio = new Vector<AudioBuffer>();
      time = 0;
    }
    else
      audio = new Vector<AudioBuffer>(audio);
    int b = barF;
    for (AudioBuffer abuf : audio)
    {
      float[] levels = { -96, -96 }; 
      if (abuf!=null) levels = abuf.getLevels();

      EElement e = getElements().get(b++);
      Rectangle r = e.getBounds();
      int wl = Math.max(barH,levelToPanel(levels[0])+3);
      int wr = Math.max(barH,levelToPanel(levels[1])+3);
      r.x = this.x-wl-2; r.width = wl+wr+3;
      e.setBounds(r);
      
      if (b==barC) break;
    }
    for (; b<barC; b++)
    {
      EElement e = getElements().get(b);
      Rectangle r = e.getBounds();
      int w = Math.max(barH,levelToPanel(-96)+3);
      r.x = this.x-w-2; r.width = 2*w+3;
      e.setBounds(r);
    }
    
    setTimeLabel(eTimecode,time);
    int offset = 1;
    for (ELabel eLabel : eHistory)
      setTimeLabel(eLabel,time+offset++);
    offset = 1;
    for (ELabel eLabel : eFuture)
      setTimeLabel(eLabel,time-offset++);
  }
  
  // -- Operations --

  private int levelToPanel(float level)
  {
    level = Math.max(level,minL);
    return Math.round((minL-level)/minL*barW);
  }
  
  protected void addGuideLine(float level, boolean right, String suffix)
  {
    ERect e;
    ELabel l;
    int x = (right?1:-1)*(levelToPanel(level)+3);
    e = new ERect(null,x-1,0,3,barC*barH,LCARS.EC_SECONDARY,null);
    e.addGeometryModifier(perspective); e.setAlpha(0.35f);
    e.setColor(cGrid);
    add(e);
    String s = String.format(Locale.ENGLISH,"%1.0f",level);
    if (suffix!=null && suffix.length()>0) s+=" "+suffix;
    int a = right?LCARS.ES_LABEL_NW:LCARS.ES_LABEL_NE;
    l = new ELabel(null,x,barC*barH,0,0,LCARS.EC_SECONDARY|LCARS.EF_TINY|a,s);
    l.addGeometryModifier(perspective); l.setAlpha(0.5f);
    l.setColor(cGrid);
    add(l);
  }
  
  protected Point2D.Float addScaleSection
  (
    int     y,
    int     h,
    String  label,
    int     labelOffset,
    float   alpha
  )
  {
    boolean left = labelOffset<0;
    int style = LCARS.EC_SECONDARY|LCARS.ES_STATIC|(left?LCARS.ES_SHAPE_NE:LCARS.ES_SHAPE_NW);
    int x     = left?-levelToPanel(0)-27:levelToPanel(0)+6;
    
    EElbo eElbo = new EElbo(null,x,y,24,h,style,null);
    eElbo.setArcWidths(1,1); eElbo.setArmWidths(3,barH);
    eElbo.setAlpha(alpha);
    eElbo.addGeometryModifier(perspective);
    eElbo.setColor(cGrid);
    add(eElbo);
    
    Point2D.Float pt = new Point2D.Float(this.x+x+(left?-3:27),this.y+y);
    pt = perspective.transform(pt);
    x = Math.round(pt.x)-this.x;
    y = Math.round(pt.y)-this.y;
    if (label!=null)
    {
      style |= LCARS.ES_LABEL_SW|LCARS.EF_TINY;
      ELabel eLabel = new ELabel(null,x+labelOffset,y-13,0,13,style,label);
      eLabel.setAlpha(alpha);
      eLabel.setColor(cGrid);
      add(eLabel);
    }
    return pt;
  }
  
  protected void setTimeLabel(ELabel eLabel, float time)
  {
    double sec = time;
    int    min = (int)sec/60;
    String s   = String.format(Locale.ENGLISH,"%02d:%04.1f",min,sec-min*60);
    eLabel.setLabel(s);
  }
  
  protected void layout()
  {
    removeAll();

    ERect  eRect;
    EElbo  eElbo;
    EValue eValue;
    
    // The Grid
    addGuideLine(-3    ,false,null);
    addGuideLine(-3    ,true,"dB");
    addGuideLine(minL/2,false,null);
    addGuideLine(minL/2,true,null);
    ELabel l = new ELabel(null,0,barC*barH,0,0,LCARS.EC_SECONDARY|LCARS.EF_TINY|LCARS.ES_LABEL_N,"L  -36  R");
    l.addGeometryModifier(perspective);
    l.setAlpha(0.35f);
    l.setColor(cGrid);
    add(l);

    // The bars
    barF = getElements().size();
    for (int b=barC-1; b>=0; b--)
    {
      float alpha = 1;
      if (b-barZ>0)
        alpha = 1-(float)Math.pow((float)(b-barZ)/(float)(barC-barZ),1.0);
      else
        alpha = 1-(float)Math.pow((float)(barZ-b)/(float)(barZ),0.5);
     
      int style = b==barZ ? LCARS.EC_HEADLINE : LCARS.EC_ELBOUP;
      eRect = new ERect(null,-barH,b*barH,2*barH+3,barH,LCARS.ES_STATIC|LCARS.ES_RECT_RND|style,null);
      eRect.setArc(4*barH); eRect.setAlpha(alpha);
      eRect.addGeometryModifier(perspective);
      if (b!=barZ) eRect.setColor(cBars);
      add(eRect);
    }
    eRect = new ERect(null,-2,0,3,barC*barH,LCARS.ES_STATIC,null);
    eRect.addGeometryModifier(perspective); eRect.setColor(Color.BLACK);
    add(eRect);
    
    // The frame
    Point2D.Float p = perspective.transform(new Point2D.Float(this.x,this.y+barZ*barH));
    int y = Math.round(p.y)-this.y;
    eElbo = new EElbo(null,-barW-24,-44,24,y+47,LCARS.EC_HEADLINE|LCARS.ES_STATIC|LCARS.ES_SHAPE_NW,null);
    eElbo.setArmWidths(8,36); eElbo.setArcWidths(24,12);
    add(eElbo);
    eRect = new ERect(null,-barW-16,y,28,3,LCARS.EC_HEADLINE|LCARS.ES_STATIC,null);
    add(eRect);
    eElbo = new EElbo(null,-barW-24,y+8,24,barC*barH-y+7,LCARS.EC_SECONDARY|LCARS.ES_STATIC|LCARS.ES_SHAPE_SW,null);
    eElbo.setArmWidths(8,13); eElbo.setArcWidths(24,12);
    eElbo.setColor(cGrid);
    add(eElbo);

    eElbo = new EElbo(null,barW,-44,24,y+47,LCARS.EC_HEADLINE|LCARS.ES_STATIC|LCARS.ES_SHAPE_NE,null);
    eElbo.setArmWidths(8,36); eElbo.setArcWidths(24,12);
    add(eElbo);
    eRect = new ERect(null,barW-12,y,28,3,LCARS.EC_HEADLINE|LCARS.ES_STATIC,null);
    add(eRect);
    eElbo = new EElbo(null,+barW,y+8,24,barC*barH-y+7,LCARS.EC_SECONDARY|LCARS.ES_STATIC|LCARS.ES_SHAPE_SE,null);
    eElbo.setArmWidths(8,13); eElbo.setArcWidths(24,12);
    eElbo.setColor(cGrid);
    add(eElbo);
    
    eValue = new EValue(null,barW-3,-44,0,36,LCARS.EC_HEADLINE|LCARS.ES_STATIC|LCARS.ES_RECT_RND_W,null);
    eValue.setValueMargin(0); eValue.setValue(title);
    add(eValue);
    
    // The scale
    eHistory = new Vector<ELabel>();
    eFuture  = new Vector<ELabel>();
    Point2D.Float pt = null;
    for (int b=barZ-25; b>=0; b-=25)
    {
      float alpha = getElements().get((barC-b-25)+barF).getAlpha();
      addScaleSection(b*barH,24*barH,Integer.toString((barZ-b)/25),0,alpha);
      pt = addScaleSection(b*barH,24*barH,"",-34,alpha);
      eHistory.add((ELabel)getElements().get(getElements().size()-1));
    }
    int style = LCARS.EC_SECONDARY|LCARS.ES_STATIC|LCARS.EF_SMALL|LCARS.ES_LABEL_SW;
    eDuration = new ELabel(null,(int)pt.x-36,(int)pt.y-33,0,18,style,"00:00.0");
    eDuration.setColor(cGrid);
    add(eDuration,false);
    for (int b=barZ; b<barC; b+=25)
    {
      float alpha = getElements().get((barC-b)+barF).getAlpha();
      int c = Math.min(24,barC-b);
      String label = Integer.toString((barZ-b)/25);
      if ("0".equals(label)) label+=" s";
      addScaleSection(b*barH,c*barH,label,0,alpha);
      pt = addScaleSection(b*barH,c*barH,null,-34,alpha);
      if (b==barZ)
      {
        eTimecode = new ELabel(null,(int)pt.x-46,(int)pt.y-18,0,18,style,"00:00.0");
        add(eTimecode,false);
      }
      else
        eFuture.add((ELabel)getElements().get(getElements().size()-1));
    }
  }
  
  // -- Overrides --
  
  @Override
  public void addToPanel(Panel panel)
  {
    layout();
    super.addToPanel(panel);
  }
  
}

// EOF
