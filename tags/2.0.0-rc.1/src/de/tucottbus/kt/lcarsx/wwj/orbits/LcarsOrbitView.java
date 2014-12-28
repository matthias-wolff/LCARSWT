package de.tucottbus.kt.lcarsx.wwj.orbits;

import gov.nasa.worldwind.animation.Animator;
import gov.nasa.worldwind.awt.AbstractViewInputHandler;
import gov.nasa.worldwind.awt.ViewInputAttributes.ActionAttributes;
import gov.nasa.worldwind.awt.ViewInputAttributes.DeviceAttributes;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.view.firstperson.BasicFlyView;

/**
 * The orbiting view (actually a customized {@link BasicFlyView}).
 *  
 * @author Matthias Wolff, BTU Cottbus-Senftenberg
 */
public class LcarsOrbitView extends BasicFlyView
{
  private static final double MAX_ELEVATION = 40000000;

  public LcarsOrbitView()
  {
    super();
    this.viewInputHandler=new AbstractViewInputHandler()
    {
      @Override
      public void stopAnimators()
      {
      }
      
      @Override
      public boolean isAnimating()
      {
        return false;
      }
      
      @Override
      public void goTo(Position lookAtPos, double elevation)
      {
      }
      
      @Override
      public void addAnimator(Animator animator)
      {
      }
      
      @Override
      protected void onVerticalTranslate(double translateChange,
          double totalTranslateChange, DeviceAttributes deviceAttributes,
          ActionAttributes actionAttributes)
      {
      }
      
      @Override
      protected void onVerticalTranslate(double translateChange,
          ActionAttributes actionAttribs)
      {
      }
      
      @Override
      protected void onRotateView(double headingInput, double pitchInput,
          double totalHeadingInput, double totalPitchInput,
          DeviceAttributes deviceAttributes, ActionAttributes actionAttributes)
      {
      }
      
      @Override
      protected void onRotateView(Angle headingChange, Angle pitchChange,
          ActionAttributes actionAttribs)
      {
      }
      
      @Override
      protected void onResetHeadingPitchRoll(ActionAttributes actionAttribs)
      {
      }
      
      @Override
      protected void onResetHeading(ActionAttributes actionAttribs)
      {
      }
      
      @Override
      protected void onMoveTo(Position focalPosition,
          DeviceAttributes deviceAttributes, ActionAttributes actionAttribs)
      {
      }
      
      @Override
      protected void onMoveTo(Position focalPosition, ActionAttributes actionAttribs)
      {
      }
      
      @Override
      protected void onHorizontalTranslateRel(double sideInput,
          double forwardInput, double sideInputFromMouseDown,
          double forwardInputFromMouseDown, DeviceAttributes deviceAttributes,
          ActionAttributes actionAttributes)
      {
      }
      
      @Override
      protected void onHorizontalTranslateRel(Angle forwardChange,
          Angle sideChange, ActionAttributes actionAttribs)
      {
      }
      
      @Override
      protected void onHorizontalTranslateAbs(Angle latitudeChange,
          Angle longitudeChange, ActionAttributes actionAttribs)
      {
      }
    };
    this.viewLimits.setEyeElevationLimits(0,MAX_ELEVATION);
    setEyePosition(Position.fromDegrees(0,0,MAX_ELEVATION));
  }

}
