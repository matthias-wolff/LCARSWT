package de.tucottbus.kt.lcars.ge.orbits;

import java.util.Date;
import java.util.Locale;

import de.tucottbus.kt.lcars.ge.GE;
import de.tucottbus.kt.lcars.ge.GECamera;

/**
 * An orbit around earth, moon or mars. Orbits model the trajectory of some satellite and supply
 * {@linkplain #getPosition() positions} or {@linkplain #getCamera() cameras}.
 * 
 * @author Matthias Wolff, BTU Cottbus
 */
public abstract class GEOrbit
{
  /**
   * Value indicating automatic choice.
   */
  public static final float AUTO = Float.NaN;

  /**
   * The camera heading angle in degrees.
   * 
   * @see #getCamera()
   */
  protected float heading = AUTO;

  /**
   * The camera tilt angle in degrees.
   * 
   * @see #getCamera()
   */
  protected float tilt = AUTO;

  /**
   * The camera roll angle in degrees.
   * 
   * @see #getCamera()
   */
  protected float roll = AUTO;
  
  // -- Getters and setters --
  
  /**
   * Sets the camera view angle of this orbit.
   * 
   * @param heading
   *          The heading angle (in degrees, {@link #AUTO} for direction of trajectory).
   * @param tilt
   *          The tilt angle (in degrees, {@link #AUTO} to look at the horizon).
   * @param roll
   *          The roll angle (in degrees, {@link #AUTO} for 0).
   * @see #getCamera()
   */
  public void setViewAngle(float heading, float tilt, float roll)
  {
    this.heading = heading;
    this.tilt    = tilt;
    this.roll    = roll;
  }
  
  /**
   * Returns the name of this orbit.
   */
  public abstract String getName();

  /**
   * Returns the orbited world.
   * 
   * @return {@link GE#EARTH}, {@link GE#MOON}, {@link GE#MARS}, or {@link GE#SKY}. 
   */
  public abstract String getWorld(); 
  
  /**
   * Returns a camera viewing from the current orbit position.
   * 
   * @see #setViewAngle(float, float, float)
   */
  public GECamera getCamera()
  {
    float heading = this.heading;
    float tilt    = this.tilt;
    float roll    = this.roll;
    Pos   pos     = getPosition();
    
    if (Float.isNaN(heading))
    {
      heading = pos.hdg;
    }
    if (Float.isNaN(tilt))
    {
      float r = 0; // Radius in m
      if      (GE.EARTH.equals(getWorld())) r = 6371000;
      else if (GE.MOON .equals(getWorld())) r = 1738000;
      else if (GE.MARS .equals(getWorld())) r = 3400000;
      if (r==0)
        tilt = 0;
      else
        tilt = (float)(Math.asin(r/(r+pos.alt))/Math.PI*180);
    }
    if (Float.isNaN(roll)) roll = 0;
    
    GECamera cam = new GECamera(GE.EARTH);
    cam.latitude  = pos.lat;
    cam.longitude = pos.lon;
    cam.altitude  = pos.alt;
    cam.heading   = heading;
    cam.tilt      = tilt;
    cam.roll      = roll;
    cam.name      = getName();
    return cam;
  }
  
  /**
   * Returns the current orbit position.
   * 
   * @return The position or <code>null</code> if no orbit data are available.
   * @see #getPosition(Date)
   */
  public Pos getPosition()
  {
    return getPosition(null);
  }

  /**
   * Returns the orbit position at a given date.
   * 
   * @param date
   *          The date, can be <code>null</code> for "now". Possible values depend on the
   *          implementation. The only safe choice is <code>null</code>.
   * @return The position or <code>null</code> if no orbit data are available for the given date.
   * @see #getPosition()
   */
  public abstract Pos getPosition(Date date);

  /**
   * Returns the fly-to speed on this orbit. Values range between 0.0 and 5.0, a negative value
   * indicates automatic speed adjustment, a value larger than 5 indicates teleport speed. The
   * default implementation return -1 (i.&nbsp;e. "automatic").
   */
  public float getFlytoSpeed()
  {
    return -1; // i.e. automatic
  }

  //-- Nested classes --

  /**
   * One position of the orbit.
   */
  public class Pos
  {
    float lat;
    float lon;
    float alt;
    Date  date;
    float spd;
    float hdg;

    @Override
    public String toString()
    {
      return String.format(Locale.US,
          "lat=%f°, lon=%f°, alt=%f m, date=%s, spd=%f m/s",lat,lon,alt,
          date.toString(),spd);
    }
  }

}
