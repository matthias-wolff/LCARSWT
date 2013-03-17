package de.tucottbus.kt.lcars.al;

import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.TimerTask;

import javax.sound.sampled.FloatControl;

import de.tucottbus.kt.lcars.contributors.EPositioner;

/**
 * A gain slider.
 * 
 * @author Matthias Wolff
 */
public class EGainSlider extends EPositioner
{
  private FloatControl control; 
  
  private float minGain = -96;
  
  private float maxGain = 6;
  
  public static final String TT_GAINUPDATE = "EGainSlider.GAINUPDATE";
  
  public EGainSlider(int x, int y, int w, int h)
  {
    super
    (
      new Rectangle(x,y,w,h),
      new Rectangle2D.Float(-100,-1,120,2),
      "dB"
    );
    Rectangle2D.Float constraints = new Rectangle2D.Float(-96,0,102,0);
    setConstraints(constraints,true);
    setGrid(new Point2D.Float(12,10),null,true);
    
    control = null;
    setActualPos(null);
    scheduleTimerTask(new UpdateTask(),TT_GAINUPDATE,40,40);
  }
  
  public void setControl(FloatControl control)
  {
    if (this.control==control) return;
    if (this.control==null)
    {
      minGain = control.getMinimum();
      maxGain = control.getMaximum();
      float min = minGain-(maxGain-minGain)*0.05f;
      float max = maxGain+(maxGain-minGain)*0.10f;
      setPhysicalBounds(new Rectangle2D.Float(min,-1,max-min,2),"dB",true);
      setConstraints(new Rectangle2D.Float(minGain,0,maxGain-minGain,0),true);
      setGrid(new Point2D.Float(12,10),null,true);
      if (getTargetPos()!=null)
        setGain(getTargetPos().x);
    }
    this.control = control;
  }

  public float getGain()
  {
    if (control==null) return java.lang.Float.NaN;
    return control.getValue();
  }
  
  public void setGain(float level)
  {
    if (level<minGain) level = minGain;
    if (level>maxGain) level = maxGain;
    setTargetPos(new Point2D.Float(level,0),true);
  }

  // -- Overrides --
  
  @Override
  protected void fireTargetChanged(boolean dragging)
  {
    super.fireTargetChanged(dragging);
    setGain(getTargetPos().x);
  }

  @Override
  protected String makeCursorLabel()
  {
    return "GAIN\n"+makePositionLabel(getTargetPos(),false);
  }
  
  // -- Nested classes --

  /**
   * The slider update timer task.
   */
  class UpdateTask extends TimerTask
  {
    @Override
    public void run()
    {
      if (control==null)
        setActualPos(null);
      else
      {
        if (getTargetPos()!=null && control.getValue()!=getTargetPos().x)
          control.setValue(getTargetPos().x);
        setActualPos(new Point2D.Float(control.getValue(),0));
      }
    }
  }
  
}

// EOF
