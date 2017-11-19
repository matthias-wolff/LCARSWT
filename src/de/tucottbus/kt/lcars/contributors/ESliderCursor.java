package de.tucottbus.kt.lcars.contributors;

import java.awt.Rectangle;
import java.util.ArrayList;

import de.tucottbus.kt.lcars.IScreen;
import de.tucottbus.kt.lcars.LCARS;
import de.tucottbus.kt.lcars.elements.EEvent;
import de.tucottbus.kt.lcars.elements.EEventListenerAdapter;
import de.tucottbus.kt.lcars.elements.ERect;
import de.tucottbus.kt.lcars.elements.modify.EGeometryModifier;
import de.tucottbus.kt.lcars.geometry.AGeometry;
import de.tucottbus.kt.lcars.geometry.GArea;
import de.tucottbus.kt.lcars.test.ATestPanel;

/**
 * A {@link ESlider} with a cursor line attached to the slider knob and with 
 * grid lines extended to the cursor length.
 * 
 * @author Matthias Wolff
 */
public class ESliderCursor extends ESlider
{
  /**
   * The cursor line.
   */
  public final ERect eLine;
  
  // -- Constants --

  /**
   * Cursor line is right (vertical slider) or at the bottom (horizontal
   * slider). Default is the opposite side.
   */
  protected static final int ES_LINE_ES = 0x40000000;
  
  /**
   * Style constant for a vertical slider with a horizontal cursor line attached
   * to the left side of the slider knob.
   */
  public static final int ES_VERT_LINE_W = ES_VERTICAL | 0x00000000;

  /**
   * Style constant for a vertical slider with a horizontal cursor line attached
   * to the right side of the slider knob.
   */
  public static final int ES_VERT_LINE_E = ES_VERTICAL | ES_LINE_ES;

  /**
   * Style constant for a horizontal slider with a vertical cursor line attached
   * to the top of the slider knob.
   */
  public static final int ES_HORIZ_LINE_N = ES_HORIZONTAL | 0x00000000;

  /**
   * Style constant for a horizontal slider with a vertical cursor line attached
   * to the bottom of the slider knob.
   */
  public static final int ES_HORIZ_LINE_S = ES_HORIZONTAL | ES_LINE_ES;
  
  /**
   * Cursor line is east or south.
   */
  protected final boolean lineES;
  
  protected final int w;
  protected final int h;
  protected final int cl;
  protected final int cw;
  
  /**
   * Creates a new slider cursor. A slider cursor is a {@link ESlider} with a
   * cursor line attached to the slider knob.
   * 
   * <h3>Remarks:</h3>
   * <ul>
   *   <li>Using the {@link #ES_ROTATE_KNOB} style causes on offset of the bounding
   *   rectangle of <code>w</code>/4 for vertical sliders and <code>h</code>/4 for
   *   horizontal sliders.</li>
   *   <li>If possible, the smaller dimension, i.e. min(<code>w</code>, <code>h</code>),
   *   should be a multiple of 8. This ensures a precise layout. Otherwise, rounding 
   *   errors may lead to slight inaccuracies.</li>
   * </ul>
   *  
   * @param x
   *          The x-coordinate of the upper left corner of the slider's bounding
   *          rectangle (in LCARS panel pixels).
   * @param y
   *          The y-coordinate of the upper left corner of the slider's bounding
   *          rectangle (in LCARS panel pixels).
   * @param w
   *          The width of the slider's bounding rectangle (in LCARS panel
   *          pixels). 
   * @param h
   *          The height of the slider's bounding rectangle (in LCARS panel
   *          pixels). 
   * @param style
   *          A combination of color style ({@link LCARS}<code>.EC_XXX</code>),
   *          {@link ESliderCursor}<code>.ES_XXX</code>. Add
   *          {@link LCARS#ES_STATIC} if the cursor shall not be movable by the
   *          user.
   * @param fatFingerMargin
   *          Margin of touch-sensitive area around the slider's bounding
   *          rectangle (in LCARS panel pixels).
   * @param cl
   *          The length of the cursor line (in LCARS panel pixels). The cursor
   *          line will be place <em>outside</em> the bounding rectangle defined
   *          by <code>x</code>, <code>y</code>, <code>w</code>, and
   *          <code>h</code> in the direction indicated by the
   *          {@link ESliderCursor}<code>.ES_XXX</code>. constant used in
   *          <code>sty.e</code>.
   * @param cw
   *          The width of the cursor line (in LCARS panel pixels).
   */
  public ESliderCursor
  (
    int x, 
    int y, 
    int w, 
    int h, 
    int style,
    int fatFingerMargin,
    int cl,
    int cw
  )
  {
    super(x,y,w,h,getSliderStyle(style),fatFingerMargin);
    
    this.lineES = (style & 0x40000000)!=0;
    this.w = w;
    this.h = h;
    this.cl = cl;
    this.cw = cw;

    // Add cursor line
    Rectangle b = computeLineBounds();
    this.eLine = new ERect(null,b.x,b.y,b.width,b.height,eKnob.getStyle()|LCARS.ES_STATIC,null);
    add(this.eLine,false);
    eKnob.addEEventListener(new EEventListenerAdapter()
    {
      @Override
      public void touchUp(EEvent ee)
      {
        eLine.setSelected(false);
      }
      
      @Override
      public void touchDown(EEvent ee)
      {
        eLine.setSelected(true);
      }
    });
    eSens.addEEventListener(new EEventListenerAdapter()
    {
      @Override
      public void touchUp(EEvent ee)
      {
        eLine.setSelected(false);
      }
      
      @Override
      public void touchDown(EEvent ee)
      {
        eLine.setSelected(true);
      }
    });
  }

  @Override
  protected ScaleTick add(ScaleTick scaleTick)
  {
    ScaleTick st = super.add(scaleTick);
    
    // Modify tick line
    if (st.eLine!=null)
      st.eLine.addGeometryModifier(new EGeometryModifier()
      {
        @Override
        public void modify(ArrayList<AGeometry> geos)
        {
          Rectangle backBounds = eBack.getBounds();
          Rectangle knobBounds = eKnob.getBounds();
          
          // Enlarge bounding rectangle
          GArea geo = (GArea)geos.get(0);
          Rectangle b = geo.getBounds();
          if (horiz)
          {
            int clx = lineES 
              ? knobBounds.y+knobBounds.height-backBounds.y-backBounds.height
              : backBounds.y-knobBounds.y;
            b.y += lineES ? 0 : -cl-clx;
            b.height += cl+clx;
          }
          else
          {
            int clx = lineES
              ? knobBounds.x+knobBounds.width-backBounds.x-backBounds.width
              : backBounds.x-knobBounds.x;
            b.x += lineES ? 0 : -cl-clx;
            b.width += cl+clx;
          }
          geo.setShape(b);
        }
      });
    
    return st;
  }
  
  @Override
  protected boolean setKnobPos(int pos)
  {
    boolean posChanged = super.setKnobPos(pos);
    eLine.setBounds(computeLineBounds());
    return posChanged;
  }
  
  protected Rectangle computeLineBounds()
  {
    Rectangle b = eKnob.getBounds();
    if (horiz)
    {
      if (lineES)
        b = new Rectangle(b.x+b.width/2-cw/2,b.y+b.height,cw,cl);
      else
        b = new Rectangle(b.x+b.width/2-cw/2,b.y-cl,cw,cl);
    }
    else
    {
      if (lineES)
        b = new Rectangle(b.x+b.width,b.y+b.height/2-cw/2,cl,cw);
      else
        b = new Rectangle(b.x-cl,b.y+b.height/2-cw/2,cl,cw);
    }
    return b;
  }
  
  private static int getSliderStyle(int style)
  {
    return (style & 0xBFFFFFFF);
  }

  // == TESTING AND DEBUGGING ==

  protected static class CslSliderCursorTestPanel extends ATestPanel
  {

    public CslSliderCursorTestPanel(IScreen iscreen)
    {
      super(iscreen);
    }
    
    @Override
    public void init()
    {
      super.init();
      final int CURSOR_WIDTH = 3; 
      final int KNOB_GAP = 3; 
      final int KNOB_SIZE = 44;
      final int ew = 440;
      final int eh = 440;
      int ex = 400;
      int ey = 120+KNOB_SIZE/2+KNOB_GAP;

      // Test area
      ERect eRect = new ERect(this,ex,ey,ew,eh,LCARS.ES_STATIC,null);
      eRect.setColor(LCARS.getColor(LCARS.CS_REDALERT,LCARS.ES_NONE));
      eRect.setAlpha(0.15f);
      add(eRect);

      // Add ECslSliderCursors at all sides of the test area
      ESliderCursor eCsc;

      // - Right
      eCsc = new ESliderCursor(ex+ew+KNOB_GAP,ey,KNOB_SIZE,eh,LCARS.EC_SECONDARY|ESliderCursor.ES_VERT_LINE_W,0,ew+KNOB_GAP,CURSOR_WIDTH);
      eCsc.eKnob.setLabel("000");
      eCsc.setStatic(true);
      eCsc.addScaleTick(0.45f,"0.45",LCARS.EF_SMALL);
      eCsc.addToPanel(this);

      // - Left
      eCsc = new ESliderCursor(ex+-KNOB_GAP-KNOB_SIZE,ey,KNOB_SIZE,eh,ESliderCursor.ES_VERT_LINE_E,0,ew+KNOB_GAP,CURSOR_WIDTH);
      eCsc.eKnob.setLabel("000");
      eCsc.addScaleTick(0.55f,"0.55",LCARS.EF_SMALL);
      eCsc.addToPanel(this);

      // - Top
      eCsc = new ESliderCursor(ex,ey-KNOB_GAP-KNOB_SIZE/2,ew,KNOB_SIZE/2,LCARS.EC_SECONDARY|ESliderCursor.ES_HORIZ_LINE_S|ESlider.ES_ROTATE_KNOB,0,eh+KNOB_GAP,CURSOR_WIDTH);
      eCsc.eKnob.setLabel("000");
      eCsc.addScaleTick(0.45f,"0.45",LCARS.EF_SMALL);
      eCsc.addToPanel(this);

      // - Bottom
      eCsc = new ESliderCursor(ex,ey+eh+KNOB_GAP,ew,KNOB_SIZE/2,ESliderCursor.ES_HORIZ_LINE_N|ESlider.ES_ROTATE_KNOB,0,eh+KNOB_GAP,CURSOR_WIDTH);
      eCsc.eKnob.setLabel("000");
      eCsc.addScaleTick(0.55f,"0.55",LCARS.EF_SMALL);
      eCsc.addToPanel(this);
    }
    
    @Override
    protected int createToolBar(int x, int y, int w, int h)
    {
      return 0;
    }
    
  }
  
  public static void main(String[] args)
  {
    args = LCARS.setArg(args,"--panel=",CslSliderCursorTestPanel.class.getName());
    args = LCARS.setArg(args,"--nospeech",null);
    LCARS.main(args);
  }

}

// EOF
