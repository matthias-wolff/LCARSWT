package de.tucottbus.kt.lcars.ge.orbits;

import java.util.Date;

import de.tucottbus.kt.lcars.ge.GECamera;

/**
 * The standard orbit of earth, moon, or mars.
 * <ul>
 *   <li>25° N at earth, over equator at moon and mars</li>.
 *   <li>One revolution per hour.</li>
 * </ul>
 * 
 * @author Matthias Wolff
 */
public class StdOrbit extends GEOrbit
{
  private String world;
  private Date   start;
  private Pos    pos;
  
  public StdOrbit(String world)
  {
    this.world = world;
    this.start = new Date();
    this.pos = new Pos();
    GECamera stdCam = new GECamera(world);
    this.pos.lat  = stdCam.latitude; 
    this.pos.lon  = stdCam.longitude;
    this.pos.alt  = stdCam.altitude;
    this.pos.date = start;
    this.pos.spd  = Float.NaN;
    this.heading  = 0;
    this.tilt     = 0;
    this.roll     = 0;
  }
  
  @Override
  public String getName()
  {
    return world;
  }

  @Override
  public String getWorld()
  {
    return world;
  }
  
  @Override
  public Pos getPosition(Date date)
  {
    if (date==null) date = new Date();
    pos.lon = (float)(date.getTime()-start.getTime())/3600000f;
    pos.lon -= Math.floor(pos.lon);
    pos.lon *= 360f;
    if (pos.lon>180) pos.lon -= 360;
    pos.date = date;
    return pos;
  }

}
