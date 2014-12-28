package de.tucottbus.kt.lcarsx.wwj.places;

import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Position;

/**
 * <p><i><b style="color:red">Experimental.</b></i></p>
 * 
 * One place comprising a name, a world, a camera position and a voice control 
 * grammar.
 * 
 * @author Matthias Wolff, BTU Cottbus-Senftenberg
 */
public class Place
{
  public static final String ONEARTH = "EARTH";
  public static final String ONMARS  = "MARS";
  public static final String ONMOON  = "MOON";
  public static final String ONSKY   = "SKY";
  
  public final String name;
  public final String world;
  public final Camera camera;
  public final String grammar;

  /**
   * <p><i><b style="color:red">Experimental.</b></i></p>
   * 
   * Creates a new place.
   * 
   * @param name
   *          The place name.
   * @param world
   *          The world this place is on: {@link #ONEARTH}, {@link #ONMOON}, 
   *          {@link #ONMARS}, or {@link #ONSKY}.
   */
  protected Place(String name, String world)
  {
    this.name    = name;
    this.world   = world;
    this.camera  = null;
    this.grammar = null;
  }
  
  /**
   * <p><i><b style="color:red">Experimental.</b></i></p>
   * 
   * Creates a new place.
   * 
   * @param name
   *          The place name.
   * @param world
   *          The world this place is on: {@link #ONEARTH}, {@link #ONMOON}, 
   *          {@link #ONMARS}, or {@link #ONSKY}.
   * @param camera
   *          The camera to view this place.
   * @param grammar
   *          Contribution to the speech dialog (UASR finite state grammar
   *          format).  
   */
  public Place(String name, String world, Camera camera, String grammar)
  {
    this.name    = name;
    this.world   = world;
    this.camera  = camera;
    this.grammar = grammar;
  }

  /**
   * <p><i><b style="color:red">Experimental.</b></i></p>
   * 
   * Creates a new place.
   * 
   * @param name
   *          The place name.
   * @param world
   *          The world this place is on: {@link #ONEARTH}, {@link #ONMOON}, 
   *          {@link #ONMARS}, or {@link #ONSKY}.
   * @param camera
   *          The camera to view this place in the format "&lt;latitude&gt;,
   *          &lt;longitude&gt;, &lt;altitude&gt;, &lt;heading&gt;,
   *          &lt;tilt&gt;, &lt;roll&gt;" where the fields enclosed in arrow
   *          brackets denote floating point values
   * @param grammar
   *          Contribution to the speech dialog (UASR finite state grammar
   *          format).
   */
  public Place(String name, String world, String camera, String grammar)
  {
    this.name    = name;
    this.world   = world;
    this.camera  = Camera.fromString(camera);
    this.grammar = grammar;
  }
  
  /**
   * <p><i><b style="color:red">Experimental.</b></i></p>
   * 
   * Creates a place from a geographical position.
   * 
   * @param name
   *          The place name.
   * @param world
   *          The world this place is on: {@link #ONEARTH}, {@link #ONMOON}, 
   *          {@link #ONMARS}, or {@link #ONSKY}.
   * @param lookAt
   *          The position to look at.
   */
  public static Place fromLookAt(String name, String world, Position lookAt)
  {
    Camera camera = new Camera(lookAt.getLatitude().degrees, 
        lookAt.getLongitude().degrees, lookAt.getAltitude(), 0, 0, 0, 0);
    Place place = new Place(name,world,camera,null);
    return place;
  }

  /**
   * <p><i><b style="color:red">Experimental.</b></i></p>
   * 
   * Creates a place from a geographical position.
   * 
   * @param name
   *          The place name.
   * @param world
   *          The world this place is on: {@link #ONEARTH}, {@link #ONMOON}, 
   *          {@link #ONMARS}, or {@link #ONSKY}.
   * @param latitude
   *          The latitude in degrees to look at.
   * @param longitude
   *          The longitude in degrees to look at.
   * @param altitude
   *          The altitude in meters to look at.
   */
  public static Place fromLookAt(String name, String world, double latitude, double longitude, double altitude)
  {
    Position lookAt = new Position(Angle.fromDegreesLatitude(latitude), 
        Angle.fromDegreesLongitude(longitude),altitude);
    return Place.fromLookAt(name,world,lookAt);
  }
  
  /**
   * <p><i><b style="color:red">Experimental.</b></i></p>
   * 
   * Creates a place from latitude-longitude box.
   * 
   * @param name
   *          The place name.
   * @param world
   *          The world this place is on: {@link #ONEARTH}, {@link #ONMOON}, 
   *          {@link #ONMARS}, or {@link #ONSKY}.
   * @param north
   *          The northern boundary in degrees latitude. 
   * @param south
   *          The southern boundary in degrees latitude. 
   * @param east
   *          The eastern boundary in degrees longitude. 
   * @param west
   *          The western boundary in degrees longitude. 
   */
  public static Place fromLatLonBox(String name, String world, double north, 
      double south, double east, double west)
  {
    Place place = new Place(name,world);
    if (east<west)
    {
      if (Math.abs(east)>Math.abs(west)) east += 360;
      else west -= 360;
    }
    if (north<south)
    {
      if (Math.abs(north)>Math.abs(south)) north += 180;
      else south -= 180;
    }
    double latAlpha = north-south;
    double lonAlpha = east-west;
    double alpha    = Math.max(latAlpha,lonAlpha);
    double lat      = (north+south)/2;
    double lon      = (west+east)/2;
    double alt      = Math.sin(Math.min(alpha,180)/360*Math.PI)*place.getFullAltitude();
    Camera camera   = new Camera(lat,lon,alt,0,0,0,0);
    return new Place(name,world,camera,null);
  }
  
  /**
   * <p><i><b style="color:red">Experimental.</b> This will change. Should
   * rather return the {@link gov.nasa.worldwind.Globe Globe} or the {@link
   * gov.nasa.worldwind.Model Model}.</i></p>
   * 
   * Returns the name of the world this place is on.
   */
  public String getWorld()
  {
    return world;
  }
  
  /**
   * <p><i><b style="color:red">Experimental.</b> This will change. The full
   * altitude can be computed from the {@linkplain gov.nasa.worldwind.Globe 
   * globe} and the current {@linkplain gov.nasa.worldwind.View#getFieldOfView()
   * field of view}.</i></p>
   * 
   * Returns the altitude in meters from which the entire world is visible.
   */
  public double getFullAltitude()
  {
    if (ONMARS.equals(world)) return 10000000;
    if (ONMOON.equals(world)) return 5000000;
    return 27000000;
  }
  
}