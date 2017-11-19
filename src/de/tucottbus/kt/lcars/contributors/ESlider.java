package de.tucottbus.kt.lcars.contributors;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collection;

import de.tucottbus.kt.lcars.IScreen;
import de.tucottbus.kt.lcars.LCARS;
import de.tucottbus.kt.lcars.elements.EElement;
import de.tucottbus.kt.lcars.elements.EEvent;
import de.tucottbus.kt.lcars.elements.EEventListener;
import de.tucottbus.kt.lcars.elements.EEventListenerAdapter;
import de.tucottbus.kt.lcars.elements.ELabel;
import de.tucottbus.kt.lcars.elements.ERect;
import de.tucottbus.kt.lcars.test.ATestPanel;

/**
 * A lightweight slider.
 * 
 * @author Matthias Wolff
 */
public class ESlider extends ElementContributor
{
  // -- Constants --
  
  /**
   * Style constant for a vertical slider.
   */
  public static final int ES_VERTICAL = 0x00000000;
  
  /**
   * Style constant for a horizontal slider.
   */
  public static final int ES_HORIZONTAL = 0x10000000;
  
  /**
   * Style constant for a linear scale.
   */
  public static final int ES_LINEAR = 0x00000000;
  
  /**
   * Style constant for a logarithmic scale.
   */
  public static final int ES_LOGARITHMIC = 0x20000000;

  /**
   * If set, the slider knob will snap to the scale ticks, i.e. only values
   * corresponding to scale ticks can be selected.
   * 
   * @see #snapToTicks
   * @see #add(ScaleTick)
   */
  public static final int EB_SNAPTOTICKS = 0x40000000;

  /**
   * Style constant for rotating the slider knob by 90 degrees.
   */
  public static final int ES_ROTATE_KNOB = 0x80000000;
  
  protected static final int SNAPBYHOLD_TIMEOUT = 1000;
  
  // -- Public fields --
  
  /**
   * If <code>true</code> the slider is horizontal, if <code>false</code> it is
   * vertical.
   */
  public final boolean horiz;

  /**
   * If <code>true</code> the slider is logarithmic, if <code>false</code> it is
   * linear.
   */
  public final boolean log;

  /**
   * If <code>true</code> the slider knob will snap to scale ticks, i.e. only
   * values corresponding to scale ticks can be selected.
   * 
   * @see #EB_SNAPTOTICKS
   * @see #add(ScaleTick)
   */
  public boolean snapToTicks;

  /**
   * If <code>true</code> the slider knob is rotated by 90 degrees.
   * 
   * @see #ES_ROTATE_KNOB
   */
  private final boolean rotateKnob;
  
  /**
   * The slider background {@link EElement}.
   */
  public final ERect eBack;
  
  /**
   * The touch-sensitive area of the slider.
   * <ul>
   *   <li>You can modify the area by invoking
   *   {@link EElement#addGeometryModifier(de.tucottbus.kt.lcars.elements.modify.EGeometryModifier)
   *   eSens.addGeometryModifier(EGeometryModifier)}.</li>
   *   <li>You can make the area visible by invoking
   *   {@link EElement#setAlpha(float) eSens.setAlpha(0.2f)}</li>
   * </ul>
   */
  public final ERect eSens;
  
  /**
   * The slider knob.
   */
  public final ERect eKnob;
  
  // -- Protected fields --
  
  protected final ArrayList<ScaleTick> lScaleTicks;
  protected final ScaleTick scaleTickMin;
  protected final ScaleTick scaleTickMax;
  protected final int       style;
  protected       float     min;
  protected       float     max;
  protected       int       dragOffset;
  protected       int       dragLastPos;
  protected       boolean   snapByHold;
  protected       long      snapByHoldMillis;
  
  // -- Life cycle --
  
  /**
   * Creates a new slider.
   * 
   * @param x
   *          The x-coordinate of the upper left corner (in LCARS panel pixels).
   * @param y
   *          The y-coordinate of the upper left corner (in LCARS panel pixels).
   * @param w
   *          The width (in LCARS panel pixels).
   * @param h
   *          The height (in LCARS panel pixels).
   * @param style
   *          A combination of color style ({@link LCARS}<code>.EC_XXX</code>),
   *          {@link ESlider}<code>.ES_XXX</code> and
   *          {@link ESlider}<code>.EB_XXX</code> constants. Add
   *          {@link LCARS#ES_STATIC} if the cursor shall not be movable by the
   *          user.
   * @param fatFingerMargin
   *          Margin of touch-sensitive area around the bounding rectangle (in
   *          LCARS panel pixels).
   */
  public ESlider(int x, int y, int w, int h, int style, int fatFingerMargin)
  {
    super(x, y);

    this.horiz = (style & ES_HORIZONTAL)!=0;
    this.log = (style & ES_LOGARITHMIC)!=0;
    this.snapToTicks = (style & EB_SNAPTOTICKS)!=0;
    this.rotateKnob = (style & ES_ROTATE_KNOB)!=0;
    this.style = (style & LCARS.ES_STYLE);
    int ffm = Math.max(0,fatFingerMargin);
    lScaleTicks = new ArrayList<ESlider.ScaleTick>();
    
    // Drag listener
    EEventListener dragListener = new EEventListener()
    {
      @Override
      public void touchDown(EEvent ee)
      {
        snapByHoldMillis = System.currentTimeMillis();
        eBack.setSelected(!eBack.isSelected());
        if (ee.el!=eKnob)
          eKnob.setSelected(eBack.isSelected());
        
        dragOffset = 0;
        dragLastPos = Integer.MIN_VALUE;
        if (ee.el==eKnob)
          if (horiz)
            dragOffset = ee.pt.x - eKnob.getBounds().width/2;
          else
            dragOffset = ee.pt.y - eKnob.getBounds().height/2;
        int pos;
        if (horiz)
          pos = ee.el.getBounds().x-eBack.getBounds().x+ee.pt.x;
        else
          pos = ee.el.getBounds().y-eBack.getBounds().y+ee.pt.y;
        pos -= dragOffset;
        if (snapToTicks)
        {
          ScaleTick tick = getClosestScaleTick(pos); 
          if(setKnobPos(tick.getPos()))
            fireSelectionChanged(tick.getValue());
        }
        else
        {
          if (setKnobPos(pos))
            fireSelectionChanged(getValue());
        }
      }

      @Override
      public void touchUp(EEvent ee)
      {
        int pos;
        eBack.setSelected(!eBack.isSelected());
        if (ee.el!=eKnob)
          eKnob.setSelected(eBack.isSelected());
        if (snapByHold)
          return;
        if (horiz)
          pos = ee.el.getBounds().x-eBack.getBounds().x+ee.pt.x;
        else
          pos = ee.el.getBounds().y-eBack.getBounds().y+ee.pt.y;
        pos -= dragOffset;
        if (snapToTicks)
        {
          ScaleTick tick = getClosestScaleTick(pos); 
          if(setKnobPos(tick.getPos()))
            fireSelectionChanged(tick.getValue());
        }
        else
        {
          if (setKnobPos(pos))
            fireSelectionChanged(getValue());
        }
      }
      
      @Override
      public void touchDrag(EEvent ee)
      {
        int pos;
        if (horiz)
          pos = ee.el.getBounds().x-eBack.getBounds().x+ee.pt.x;
        else
          pos = ee.el.getBounds().y-eBack.getBounds().y+ee.pt.y;
        pos -= dragOffset;
        if (pos==dragLastPos)
          return;

        snapByHoldMillis = System.currentTimeMillis();
        dragLastPos=pos;
        if (snapToTicks)
        {
          ScaleTick tick = getClosestScaleTick(pos); 
          if(setKnobPos(tick.getPos()))
            fireSelectionChanged(tick.getValue());
        }
        else
        {
          if (setKnobPos(pos))
            fireSelectionChanged(getValue());
        }
      }

      @Override
      public void touchHold(EEvent ee)
      {
        if (System.currentTimeMillis()-snapByHoldMillis<SNAPBYHOLD_TIMEOUT)
          return;
        
        ScaleTick tick = getClosestScaleTick(getKnobPos()); 
        if (setKnobPos(tick.getPos()))
        {
          eBack.setHighlighted(true);
          LCARS.invokeLater(()->
          {
            eBack.setHighlighted(false);
          },200);
          snapByHold = true;
          fireSelectionChanged(tick.getValue());
        }
      }
    }; 

    if (!rotateKnob)
      if (horiz)
      {
        eBack = new ERect(null,0,h/8,w,h*3/4,this.style|LCARS.ES_STATIC,null);
        eKnob = new ERect(null,w/2-h/4,0,h/2,h,style|LCARS.ES_RECT_RND|LCARS.EB_OVERDRAG|LCARS.EF_SMALL|LCARS.ES_LABEL_C,null);
      }
      else
      {
        eBack = new ERect(null,w/8,0,w*3/4,h,this.style|LCARS.ES_STATIC,null);
        eKnob = new ERect(null,0,h/2-w/4,w,w/2,style|LCARS.ES_RECT_RND|LCARS.EB_OVERDRAG|LCARS.EF_SMALL|LCARS.ES_LABEL_C,null);
      }
    else
      if (horiz)
      {
        eBack = new ERect(null,0,0,w,h,this.style|LCARS.ES_STATIC,null);
        eKnob = new ERect(null,w/2-h,0,2*h,h,style|LCARS.ES_RECT_RND|LCARS.EB_OVERDRAG|LCARS.EF_SMALL|LCARS.ES_LABEL_C,null);
      }
      else
      {
        eBack = new ERect(null,0,0,w,h,this.style|LCARS.ES_STATIC,null);
        eKnob = new ERect(null,0,h/2-w,w,2*w,style|LCARS.ES_RECT_RND|LCARS.EB_OVERDRAG|LCARS.EF_SMALL|LCARS.ES_LABEL_C,null);
      }
    eBack.setAlpha(0.2f);
    eSens = new ERect(null,-ffm,-ffm,w+2*ffm,h+2*ffm,LCARS.EB_OVERDRAG,null);
    eSens.setColor(LCARS.getColor(LCARS.CS_REDALERT,LCARS.EC_HEADLINE));
    eSens.setAlpha(0.f);
    eSens.addEEventListener(dragListener);
    eKnob.addEEventListener(dragListener);
    add(eBack);
    add(eSens);
    scaleTickMin = new ScaleTick(0,null,LCARS.ES_NONE,true,true);
    scaleTickMax = new ScaleTick(this.horiz?w:h,null,LCARS.ES_NONE,true,true); 
    add(scaleTickMin);
    add(scaleTickMax);
    add(eKnob);
    
    setMinMaxValue(0,1);
    setStatic((style & LCARS.ES_STATIC)!=0);
  }

  // -- Public API --

  /**
   * Adds a scale tick.
   * 
   * @param pos
   *          The position relative to the top (vertical sliders) or left side
   *          (horizontal sliders), in LCARS panel pixels.
   * @param label
   *          The tick label, can be <code>null</code>.
   * @param fontStyle
   *          One of the {@link LCARS}<code>.EF_XXX</code> constants.
   * @return The scale tick.
   */
  public ScaleTick addScaleTick(int pos, String label, int fontStyle)
  {
    return add(new ScaleTick(pos,label,fontStyle,false,true));
  }

  /**
   * Adds a scale tick.
   * 
   * @param value
   *          The value if the tick.
   * @param label
   *          The tick label, can be <code>null</code>.
   * @param fontStyle
   *          One of the {@link LCARS}<code>.EF_XXX</code> constants.
   * @return The scale tick.
   */
  public ScaleTick addScaleTick(float value, String label, int fontStyle)
  {
    ScaleTick tick = new ScaleTick(valueToPos(value),label,fontStyle,false,true);
    tick.value = value;
    return add(tick);
  }
  
  /**
   * Adds a scale label.
   * 
   * @param pos
   *          The position relative to the top (vertical sliders) or left side
   *          (horizontal sliders), in LCARS panel pixels.
   * @param label
   *          The label.
   * @param fontStyle
   *          One of the {@link LCARS}<code>.EF_XXX</code> constants.
   * @return A scale tick object representing the label.
   */
  public ScaleTick addScaleLabel(int pos, String label, int fontStyle)
  {
    return add(new ScaleTick(pos,label,fontStyle,true,false));
  }
  
  /**
   * Removes a scale tick. If the scale tick does not exist, the method does
   * nothing.
   * 
   * @param scaleTick
   *          The scale tick
   */
  public void removeScaleTick(ScaleTick scaleTick)
  {
    // Do not remove default scale ticks
    if (scaleTick==scaleTickMin || scaleTick==scaleTickMax)
      return;

    // Remove scale tick
    remove(scaleTick.eLine);
    remove(scaleTick.eLabel);
    lScaleTicks.remove(scaleTick);
  }

  /**
   * Removes all scale ticks except the default invisible scale ticks at both
   * ends of the slider.
   */
  public void removeAllScaleTicks()
  {
    for (ScaleTick scaleTick : getScaleTicks()) 
      // Note: This is safe because getScaleTicks() returns a copy of the list! 
      removeScaleTick(scaleTick);
  }
  
  /**
   * Returns the list of scale ticks and scale labels. The returned object is a
   * copy, modifying it has no effect on the slider. You can, however, modify the
   * {@link EElement}s in the scale ticks.
   * 
   * <h3>Remarks:</h3>
   * <ul>
   *   <li>The list does not include the default invisible scale ticks at both
   *   ends of the slider. It does, however, include scale labels added by {@link 
   *   #addScaleLabel(int, String, int)}. An empty return value means, that there 
   *   were no scale ticks or scale labels added by the application.</li>
   * </ul>
   * 
   * @see #addScaleTick(int, String, int)
   * @see #addScaleTick(float, String, int)
   * @see #addScaleLabel(int, String, int)
   */
  public Collection<ScaleTick> getScaleTicks()
  {
    ArrayList<ScaleTick> l = new ArrayList<ScaleTick>(lScaleTicks);
    l.remove(scaleTickMin);
    l.remove(scaleTickMax);
    return l;
  }

  /**
   * Returns the scale tick closest to a knob position.
   * 
   * @param pos
   *          The knob position relative to the top (vertical sliders) or left
   *          side (horizontal sliders), in LCARS panel pixels.
   * @param The
   *          closest scale tick.
   */
  public ScaleTick getClosestScaleTick(int pos)
  {
    ScaleTick closest = null;
    int minDist = Integer.MAX_VALUE;
    for (ScaleTick tick : lScaleTicks)
      if (Math.abs(pos-tick.getPos())<minDist)
      {
        minDist = Math.abs(pos-tick.getPos());
        closest = tick;
      }
    return closest;
  }
  
  /**
   * Returns the scale tick closest to a value.
   * 
   * @param pos
   *          The value.
   * @param The
   *          closest scale tick.
   */
  public ScaleTick getNearestScaleTick(float value)
  {
    return getClosestScaleTick(valueToPos(value));
  }
  
  /**
   * Sets the minimum and maximum values for the slider. The default interval is [0,1].
   * 
   * @param min
   *          The minimum.
   * @param max
   *          The maximum.
   */
  public void setMinMaxValue(float min, float max)
  {
    this.min = logValue(min);
    this.max = logValue(max);
  }
  
  /**
   * Sets the slider knob to a value.
   * 
   * @param value
   *          The value.
   */
  public void setValue(float value)
  {
    setKnobPos(valueToPos(value));
  }
  
  /**
   * Returns the current value of the slider, i.e. the value represented by the
   * current knob position.
   */
  public float getValue()
  {
    return posToValue(getKnobPos());
  }

  /**
   * Sets this slider static, i.e. not accepting user input, or non-static.
   * 
   * @param stat
   *          <code>true</code> to make the slider static, <code>false</code>
   *          otherwise
   */
  public void setStatic(boolean stat)
  {
    eSens.setStatic(stat);
    eBack.setStatic(stat);
    eKnob.setStatic(stat);
  }
  
  /**
   * Determines if this slider is static, i.e. not accepting user input.
   * 
   * @return <code>true</code> if the slider is static, <code>false</code>
   *         otherwise
   */
  public boolean isStatic()
  {
    return eKnob.isStatic();
  }

  /**
   * Sets this slider disabled or enabled.
   * 
   * @param disabled
   *         The new disabled state.
   */
  public void setDisabled(boolean disabled)
  {
    forAllElements((el)-> { el.setDisabled(disabled); });
  }
  
  /**
   * Determines if this slider is disabled or enabled.
   */
  public boolean isDisabled()
  {
    return eKnob.isDisabled();
  }
  
  /**
   * Converts a value to a slider position.
   * 
   * @param value
   *          The value.
   * @return The position relative to the top (vertical sliders) or left side
   *         (horizontal sliders), in LCARS panel pixels.
   */
  public int valueToPos(float value)
  {
    value = logValue(value);
    Rectangle b = eBack.getBounds();
    if (horiz)
    {
      int pos = Math.round((value-min)/(max-min)*b.width);
      return Math.max(0,Math.min(b.width,pos));
    }
    else
    {
      int pos =  b.height-Math.round((value-min)/(max-min)*b.height);
      return Math.max(0,Math.min(b.height,pos));
    }
  }
  
  /**
   * Converts a slider position to a value.
   * 
   * @param pos
   *         The position relative to the top (vertical sliders) or left side
   *         (horizontal sliders), in LCARS panel pixels.
   * @return The value.
   */
  public float posToValue(int pos)
  {
    Rectangle b = eBack.getBounds();
    if (horiz)
    {
      pos = Math.max(0,Math.min(b.width,pos));
      return expValue((float)pos/(float)b.width*(max-min)+min);
    }
    else
    {
      pos = Math.max(0,Math.min(b.height,pos));
      return expValue((float)(b.height-pos)/(float)b.height*(max-min)+min);
    }
  }
  
  // -- Protected API --
  
  /**
   * @return <code>true</code> if the knob position was changed,
   *         <code>false</code> otherwise.
   */
  protected boolean setKnobPos(int pos)
  {
    Rectangle b = eKnob.getBounds();
    if (horiz)
    { 
      pos = Math.max(0,Math.min(eBack.getBounds().width,pos));
      b.x = eBack.getBounds().x+pos-b.width/2;
    }
    else
    {
      pos = Math.max(0,Math.min(eBack.getBounds().height,pos));
      b.y = eBack.getBounds().y+pos-b.height/2;
    }
    if (pos==getKnobPos())
      return false;
    eKnob.setBounds(b);
    return true;
  }
  
  protected int getKnobPos()
  {
    Rectangle b = eKnob.getBounds();
    if (horiz)
      return b.x + b.width/2 - eBack.getBounds().x;
    else
      return b.y + b.height/2 - eBack.getBounds().y;
  }
  
  protected float logValue(float value)
  {
    if (!log) 
      return value;
    return (float)Math.log(Math.max(Float.MIN_NORMAL,value));
  }
  
  protected float expValue(float value)
  {
    if (!log) 
      return value;
    return (float)Math.exp(value);
  }
  
  protected ScaleTick add(ScaleTick scaleTick)
  {
    if (lScaleTicks.contains(scaleTick))
      return scaleTick;
    
    remove(eSens);
    remove(eKnob);
    
    lScaleTicks.add(scaleTick);
    add(scaleTick.eLine,false);
    if (scaleTick.eLabel!=null)
      add(scaleTick.eLabel,false);

    add(eSens,false);
    add(eKnob,false);
    return scaleTick;
  }

  // -- Selection listener implementation --
  
  public interface SelectionListener
  {
    public void selectionChanged(float value);
  }

  private ArrayList<SelectionListener> listeners;
  
  /**
   * Adds a selection listener to the slider.
   * 
   * @param listener
   *          The listener.
   */
  public void addSelectionListener(SelectionListener listener)
  {
    if (listener==null)
      return;
    if (listeners==null)
      listeners = new ArrayList<SelectionListener>();
    if (listeners.contains(listener))
      return;
    listeners.add(listener);
  }
  
  /**
   * Removes a selection listener from the slider.
   * 
   * @param listener
   *          The listener. If this listener is not registered, the method does
   *          nothing.
   */
  public void removeListener(SelectionListener listener)
  {
    if (listener==null || listeners==null)
      return;
    listeners.remove(listener);
  }
  
  /**
   * Removes all selection listeners from the slider.
   */
  public void removeAllListeners()
  {
    listeners.clear();
  }
  
  protected void fireSelectionChanged(float value)
  {
    if (listeners==null)
      return;
    for (SelectionListener listener : listeners)
      listener.selectionChanged(value);
  }

  // -- Nested classes --
  
  /**
   * A scale tick on the slider.
   */
  public class ScaleTick
  {
    /**
     * The tick line, can be <code>null</code>. 
     */
    public final ERect eLine;

    /**
     * The tick line, can be <code>null</code>. 
     */
    public final ELabel eLabel;
    
    /**
     * If <code>true</code>, the slider will snap to this scale tick.
     * 
     * <h3>Remarks:</h3>
     * <ul>
     *   <li>Tick snapping must be activated by the
     *   {@link ESlider#EB_SNAPTOTICKS} style or by setting
     *   {@link ESlider#snapToTicks}!</li>
     * </ul>
     */
    public boolean snapToTick;
    
    /**
     * The value of the tick.
     */
    protected float value;
    
    protected ScaleTick(int pos, String label, int fontStyle, boolean noLine, boolean snapToTick)
    {
      this.snapToTick = snapToTick;
      this.value = Float.NaN;

      fontStyle = (fontStyle & LCARS.ES_FONT) | style | LCARS.ES_STATIC;
      Rectangle b = eBack.getBounds();

      if (horiz)
      {
        this.eLine = new ERect(null,b.x+pos,b.y,1,b.height,style|LCARS.ES_STATIC,null);
        if (label!=null && label.length()>0)
          this.eLabel = new ELabel(null,b.x+pos+2,b.y,1,b.height,fontStyle|LCARS.ES_LABEL_W|fontStyle,label);
        else
          this.eLabel = null;
      }
      else
      {
        eLine = new ERect(null,b.x,b.y+pos,b.width,1,style|LCARS.ES_STATIC,null);
        if (label!=null && label.length()>0)
          this.eLabel = new ELabel(null,b.x,b.y+pos+1,b.width-3,1,fontStyle|LCARS.ES_LABEL_SE,label);
        else
          eLabel = null;
      }
      this.eLine.setAlpha(noLine?0.f:0.3f);
      if (this.eLabel!=null)
        this.eLabel.setAlpha(0.5f);
    }
    
    /**
     * Returns the position of the scale tick in LCARS panel pixels relative to
     * the top (vertical sliders) or left (horizontal sliders).
     */
    public int getPos()
    {
      if (horiz)
        return eLine.getBounds().x-eBack.getBounds().x;
      else
        return eLine.getBounds().y-eBack.getBounds().y;
    }
  
    public float getValue()
    {
      if (Float.isNaN(this.value))
        return posToValue(getPos());
      else
        return this.value;
    }
  }
  
  // == TESTING AND DEBUGGING ==

  protected static class CslSliderTestPanel extends ATestPanel
  {
    protected ERect        eBtnScales;
    protected ERect        eBtnSnapToScales;
    protected ESlider[] sliders;

    public CslSliderTestPanel(IScreen iscreen)
    {
      super(iscreen);
    }
    
    @Override
    public void init()
    {
      super.init();
      final int KNOB_SIZE = 44;
      int ex;
      int ey;
      
      // Create sliders
      sliders = new ESlider[4];
      ex = 600;
      ey = 170;
      sliders[0] = new ESlider(ex,ey,400,KNOB_SIZE,ESlider.ES_HORIZONTAL,5);
      sliders[0].addToPanel(this);
      ey += sliders[0].eKnob.getBounds().height+10;
      sliders[1] = new ESlider(ex,ey,400,KNOB_SIZE/2,ESlider.ES_HORIZONTAL|ESlider.ES_ROTATE_KNOB,5);
      sliders[1].addToPanel(this);
      ey += sliders[1].eKnob.getBounds().height+10;
      sliders[0].addSelectionListener((value)->
      {
        System.out.println("slider0.value="+value);
        sliders[1].setValue(value);
      });
      sliders[1].addSelectionListener((value)->
      {
        System.out.println("slider1.value="+value);
        sliders[0].setValue(value);
      });

      ey += 20;
      sliders[2] = new ESlider(ex,ey,KNOB_SIZE,400,LCARS.ES_NONE,5);
      sliders[2].addToPanel(this);
      ex += sliders[2].eKnob.getBounds().width+10;
      sliders[3] = new ESlider(ex,ey,KNOB_SIZE/2,400,ESlider.ES_ROTATE_KNOB,5);
      sliders[3].addToPanel(this);

      sliders[2].addSelectionListener((value)->
      {
        System.out.println("slider2.value="+value);
        sliders[3].setValue(value);
      });
      sliders[3].addSelectionListener((value)->
      {
        System.out.println("slider3.value="+value);
        sliders[2].setValue(value);
      });
    }
    
    @Override
    protected int createToolBar(int x, int y, int w, int h)
    {
      int ey = y;
      int style = LCARS.ES_RECT_RND|LCARS.ES_LABEL_E;
      
      eBtnScales = new ERect(this,x,ey,w,h,style,"SCALES");
      eBtnScales.addEEventListener(new EEventListenerAdapter()
      {
        @Override
        public void touchUp(EEvent ee)
        {
          boolean hasScales = hasScales();
          for (ESlider slider : sliders)
            slider.removeAllScaleTicks();
          if (!hasScales)
            for (ESlider slider : sliders)
            {
              slider.addScaleTick(0.2f,"0.2",LCARS.EF_TINY);
              slider.addScaleTick(0.4f,"0.4",LCARS.EF_TINY);
              slider.addScaleTick(0.6f,"0.6",LCARS.EF_TINY);
              slider.addScaleTick(0.8f,"0.8",LCARS.EF_TINY);
            }
        }
      });
      add(eBtnScales);
      ey += getElements().get(getElements().size()-1).getBounds().height +3;

      eBtnSnapToScales = new ERect(this,x,ey,w,h,style,"SNAP TO SCALES");
      eBtnSnapToScales.addEEventListener(new EEventListenerAdapter()
      {
        @Override
        public void touchUp(EEvent ee)
        {
          boolean snapToScales = isSnapToScales(); 
          for (ESlider slider : sliders)
            slider.snapToTicks = !snapToScales;
        }
      });
      add(eBtnSnapToScales);
      ey += getElements().get(getElements().size()-1).getBounds().height +3;

      return ey-y-3;
    }
    
    @Override
    protected void fps10()
    {
      eBtnScales.setSelected(hasScales());
      eBtnSnapToScales.setSelected(isSnapToScales());
    }
    
    protected boolean hasScales()
    {
      return !sliders[0].getScaleTicks().isEmpty();
    }
    
    protected boolean isSnapToScales()
    {
      return sliders[0].snapToTicks;
    }
    
  }
  
  public static void main(String[] args)
  {
    args = LCARS.setArg(args,"--panel=",CslSliderTestPanel.class.getName());
    args = LCARS.setArg(args,"--nospeech",null);
    LCARS.main(args);
  }
}

// EOF
