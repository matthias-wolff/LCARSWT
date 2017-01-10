package de.tucottbus.kt.lcarsx.wwj.orbits;

import gov.nasa.worldwind.animation.Animator;
import gov.nasa.worldwind.awt.AbstractViewInputHandler;
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
    this.viewInputHandler = new AbstractViewInputHandler() {
		
		@Override
		public void stopAnimators() {
			// ignored			
		}
		
		@Override
		public boolean isAnimating() {
			// ignored
			return false;
		}
		
		@Override
		public void goTo(Position lookAtPos, double elevation) {
			// ignored
		}
		
		@Override
		public void addAnimator(Animator animator) {
			// ignored
		}
	};
    
    this.viewLimits.setEyeElevationLimits(0,MAX_ELEVATION);
    setEyePosition(Position.fromDegrees(0,0,MAX_ELEVATION));
  }
}
