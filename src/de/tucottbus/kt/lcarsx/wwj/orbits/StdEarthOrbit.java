package de.tucottbus.kt.lcarsx.wwj.orbits;

import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Position;

import java.util.Date;

/**
 * The standard orbit around earth
 * <ul>
 *   <li>at 25° N,</li>
 *   <li>ten revolutions per hour.</li>
 * </ul>
 * 
 * @author Matthias Wolff
 */
public class StdEarthOrbit extends Orbit
{
  private Date       start;
  private OrbitState state;
  
  public StdEarthOrbit()
  {
    // Initialize start time
    this.start = new Date();
    
    // Initialize eye position and view angles
    eyePosition = getDefaultEyePosition();
    heading     = null;
    pitch       = Angle.ZERO;
    
    // Initialize orbit state and view angles
    this.state       = new OrbitState();
    this.state.lat   = null;             // not controlled
    this.state.lon   = Angle.ZERO;
    this.state.alt   = Double.NaN;       // not controlled
    this.state.date  = start;
    this.state.spd   = Double.NaN;
    this.state.hdg   = null;             // not controlled
    this.state.pitch = Angle.ZERO;       // not controlled
  }
  
  @Override
  public String getName()
  {
    return "ORBIT";
  }

  @Override
  public Position getDefaultEyePosition()
  {
    return new Position(Angle.fromDegrees(25), Angle.ZERO, 27000000);
  }
  
  @Override
  protected OrbitState getState(Date date)
  {
    if (date==null) date = new Date();
    double lon = (date.getTime()-start.getTime())/360000.;
    lon -= Math.floor(lon);
    lon *= 360;
    if (lon>180) lon -= 360;
    state.lon  = Angle.fromDegreesLongitude(lon);
    state.date = date;
    return state;
  }

  @Override
  public boolean controlsLatitude()
  {
    return false;
  }

  @Override
  public boolean controlsLongitude()
  {
    return true;
  }

  @Override
  public boolean controlsAltitude()
  {
    return false;
  }

  @Override
  public boolean controlsHeading()
  {
    return false;
  }

  @Override
  public boolean controlsPitch()
  {
    return false;
  }

}
