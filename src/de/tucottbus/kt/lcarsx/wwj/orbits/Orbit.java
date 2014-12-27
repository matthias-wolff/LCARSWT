package de.tucottbus.kt.lcarsx.wwj.orbits;

import gov.nasa.worldwind.View;
import gov.nasa.worldwind.event.RenderingEvent;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Position;

import java.util.Date;

import de.tucottbus.kt.lcarsx.wwj.contributors.EWorldWind;

/**
 * <p><i><b style="color:red">Experimental API.</b></i></p>
 * 
 * An orbit around a globe. Orbits model the trajectory of some satellite and
 * supply eye {@linkplain Position positions}, a heading, and a pitch angle. 
 * Orbits deliberately enforce a zero degree roll angle.
 * 
 * @author Matthias Wolff, BTU Cottbus
 * @see #getEyePosition()
 * @see #getHeading()
 * @see #getPitch()
 */
public abstract class Orbit
{
  /**
   * The eye position to be used if the orbit does not control latitude,
   * longitude, or altitude, <code>null</code> indicates a default position.
   * 
   * @see #getEyePosition()
   */
  protected Position eyePosition = null;
  
  /**
   * The view heading angle to be used if the orbit does not control the 
   * heading, <code>null</code> indicates a default angle.
   * 
   * @see #getHeading()
   */
  protected Angle heading = null;

  /**
   * The view pitch angle to be used if the orbit does not control the pitch,
   * <code>null</code> indicates a default angle.
   * 
   * @see #getPitch()
   */
  protected Angle pitch = null;
  
  /**
   * The view provided by this orbit.
   */
  protected LcarsOrbitView view = new LcarsOrbitView(); 
  
  // -- Getters and setters --

  /**
   * Returns the view provided by this orbit.
   * 
   * <p><i><b style="color:red">Experimental API.</b></i></p>
   */
  public View getView()
  {
    return view;
  }
  
  /**
   * Sets the eye position of this orbit. The new latitude, longitude, and
   * altitude become effective only if the respective values are not controlled
   * be the orbit.
   * 
   * <p><i><b style="color:red">Experimental API.</b></i></p>
   * 
   * @param eyePosition
   *          The new eye position (<code>null</code> for a default position).
   * @see #getEyePosition()
   * @see #controlsLatitude()
   * @see #controlsLongitude()
   * @see #controlsAltitude()
   */
  public void setEyePosition(Position eyePosition)
  {
    this.eyePosition = eyePosition==null?getDefaultEyePosition():eyePosition;
  }
  
  /**
   * Returns the current eye position of this orbit.
   * 
   * <p><i><b style="color:red">Experimental API.</b></i></p>
   * 
   * @see #setEyePosition(Position)
   */
  public Position getEyePosition()
  {
    OrbitState os = getState();
    Position pos = eyePosition!=null?eyePosition:getDefaultEyePosition();
    Angle  lat = controlsLatitude() ?os.lat:pos.latitude;
    Angle  lon = controlsLongitude()?os.lon:pos.longitude;
    double alt = controlsAltitude() ?os.alt:pos.elevation;
    return new Position(lat,lon,alt);
  }

  /**
   * Returns the default eye position of this orbit. Derived classes may 
   * override this method to provide an own standard eye position.
   * 
   * <p><i><b style="color:red">Experimental API.</b></i></p>
   * 
   * @return Latitude 0°, longitude 0°, and altitude 100 km.
   */
  public Position getDefaultEyePosition()
  {
    return new Position(Angle.ZERO, Angle.ZERO, 10000000);
  }

  /**
   * Sets the view heading angle of this orbit. The new value becomes effective
   * only if the view heading is not controlled by the orbit.
   * 
   * <p><i><b style="color:red">Experimental API.</b></i></p>
   * 
   * @param heading
   *          The heading angle (<code>null</code> for direction of trajectory).
   * @see #getHeading()
   * @see #controlsHeading()
   */
  public void setHeading(Angle heading)
  {
    this.heading = heading;
  }

  /**
   * Returns the current heading {@link Angle angle}.
   * 
   * <p><i><b style="color:red">Experimental API.</b></i></p>
   */
  public Angle getHeading()
  {
    Angle hdg = controlsHeading()?getState().hdg:this.heading;
    if (hdg==null) hdg = getState().hdg;
    if (hdg==null) hdg = Angle.ZERO;
    return hdg;
  }

  /**
   * Sets the view pitch angle of this orbit. The new value becomes effective
   * only if the pitch angle is not controlled by the orbit.
   * 
   * <p><i><b style="color:red">Experimental API.</b></i></p>
   * 
   * @param pitch
   *          The pitch angle (<code>null</code> to look 5 degrees below the
   *          horizon).
   * @see #getPitch()
   * @see #controlsPitch()
   */
  public void setPitch(Angle pitch)
  {
    this.pitch = pitch;
  }
  
  /**
   * Returns the current pitch {@link Angle angle}.
   * 
   * <p><i><b style="color:red">Experimental API.</b></i></p>
   */
  public Angle getPitch()
  {
    Angle pitch = controlsPitch()?getState().pitch:this.pitch;
    if (pitch==null) pitch = getState().pitch;
    if (pitch==null) pitch = Angle.ZERO;
    return pitch;
  }
  
  /**
   * Returns the current {@linkplain OrbitState orbit state}. Shortcut for
   * {@link #getState(Date) getState(null)}.
   * 
   * <p><i><b style="color:red">Experimental API.</b></i></p>
   * 
   * @return The state or <code>null</code> if no orbit data are available.
   * @see #getState(Date)
   */
  protected final OrbitState getState()
  {
    return getState(null);
  }
  
  // -- View operations --

  /**
   * Do not use! The method is invoked by 
   * {@link EWorldWind#stageChanged(RenderingEvent)} to update the view provided
   * by this orbit with the internal orbit data.
   * 
   * <p><i><b style="color:red">Experimental API.</b></i></p>
   */
  public final void updateView()
  {
    if (getState()==null) return;
    view.setEyePosition(getEyePosition());
    view.setHeading(getHeading());
    view.setPitch(getPitch());
    view.setRoll(Angle.ZERO);
  }
  
  // -- Abstract API --
  
  /**
   * Returns the name of this orbit.
   * 
   * <p><i><b style="color:red">Experimental API.</b></i></p>
   */
  public abstract String getName();
  
  /**
   * Determines if this orbit controls the latitude of its eye position. If not,
   * the latitude of the provided view can be freely adjusted.
   * 
   * <p><i><b style="color:red">Experimental API.</b></i></p>
   * 
   * @see #getView()
   */
  public abstract boolean controlsLatitude();

  /**
   * Determines if this orbit controls the longitude of its eye position. If 
   * not, the longitude of the provided view can be freely adjusted.
   * 
   * <p><i><b style="color:red">Experimental API.</b></i></p>
   * 
   * @see #getView()
   */
  public abstract boolean controlsLongitude();

  /**
   * Determines if this orbit controls the altitude of its eye position. If 
   * not, the altitude of the provided view can be freely adjusted.
   * 
   * <p><i><b style="color:red">Experimental API.</b></i></p>
   * 
   * @see #getView()
   */
  public abstract boolean controlsAltitude();

  /**
   * Determines if this orbit controls the heading of its view. If not, the 
   * heading can be freely adjusted.
   * 
   * <p><i><b style="color:red">Experimental API.</b></i></p>
   * 
   * @see #getView()
   */
  public abstract boolean controlsHeading();

  /**
   * Determines if this orbit controls the pitch of its view. If not, the pitch 
   * can be freely adjusted.
   * 
   * <p><i><b style="color:red">Experimental API.</b></i></p>
   * 
   * @see #getView()
   */
  public abstract boolean controlsPitch();

  /**
   * Returns the orbit position at a given date.
   * 
   * <p><i><b style="color:red">Experimental API.</b></i></p>
   * 
   * @param date
   *          The date, can be <code>null</code> for "now". Possible values
   *          depend on the implementation. The only safe choice is
   *          <code>null</code>.
   * @return The position or <code>null</code> if no orbit data are available
   *         for the given date.
   * @see #getState()
   */
  protected abstract OrbitState getState(Date date);

  //-- Nested classes --

  /**
   * An item  of list of orbits.
   * 
   * <p><i><b style="color:red">Experimental API.</b></i></p>
   * @author Matthias Wolff, BTU Cottbus-Senftenberg
   */
  public static final class ListItem
  {
    public final String name;
    
    public final Class<? extends Orbit> clazz;
    
    public ListItem(Class<? extends Orbit> orbitClass, String name)
    {
      this.clazz = orbitClass;
      this.name       = name;
    }
  }
  
  /**
   * Position, heading, pitch, and speed on an orbit at a given time.
   * 
   * <p><i><b style="color:red">Experimental API.</b></i></p>
   * @author Matthias Wolff, BTU Cottbus-Senftenberg
   */
  protected final class OrbitState
  {
    /**
     * The latitude.
     */
    public Angle lat;
    
    /**
     * The longitude.
     */
    public Angle lon;
    
    /**
     * The altitude in m.
     */
    public double alt;
    
    /**
     * The view heading.
     */
    public Angle hdg;

    /**
     * The view pitch.
     */
    public Angle pitch;
    
    /**
     * The date.
     */
    public Date date;
    
    /**
     * The orbiting speed in m/s.
     */
    public double spd;

    /**
     * Creates an empty orbit state.
     */
    public OrbitState()
    {
      lat   = Angle.ZERO;
      lon   = Angle.ZERO;
      alt   = Double.NaN;
      hdg   = Angle.ZERO;
      pitch = Angle.ZERO;
      spd   = Double.NaN;
    }
    
    /**
     * Creates an orbit state.
     * 
     * @param lat
     *          The latitude in degrees.
     * @param lon
     *          The longitude in degrees.
     * @param alt
     *          The altitude in m.
     * @param spd
     *          The speed in m/s.
     * @param hdg
     *          The heading in degrees. 
     * @param pitch
     *          The pitch in degrees.
     */
    public OrbitState(double lat, double lon, double alt, double spd,
        double hdg, double pitch)
    {
      this();
      this.lat   = Angle.fromDegreesLatitude(lat);
      this.lon   = Angle.fromDegreesLongitude(lon);
      this.alt   = alt;
      this.hdg   = Angle.fromDegrees(hdg);
      this.pitch = Angle.fromDegrees(pitch);
      this.spd   = spd;
    }
    
    @Override
    public String toString()
    {
      return 
        "lat=" +lat  +", "  +
        "lon=" +lon  +",  " +
        "alt=" +alt  +" m, "+
        "hdg= "+hdg  +", "  +
        "pit= "+pitch+", "  +
        "date="+date +", "  +
        "spd= "+spd  +" m/s";
    }
  }

}
