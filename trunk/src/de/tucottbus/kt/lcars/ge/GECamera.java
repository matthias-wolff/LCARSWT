package de.tucottbus.kt.lcars.ge;

/**
 * A camera position in Google Earth.
 */
public class GECamera
{
  /**
   * The camera's latitude (in degrees)
   */
  public float latitude;
  
  /**
   * The camera's longitude (in degrees)
   */
  public float longitude;

  /**
   * The camera's altitude (in meters)
   */
  public float altitude;

  /**
   * The camera's heading angle (in degrees)
   */  
  public float heading;

  /**
   * The camera's tilt angle (in degrees)
   */  
  public float tilt;

  /**
   * The camera's roll angle (in degrees)
   */  
  public float roll;
  
  /**
   * The camera's name.
   */
  public String name;
  
  /**
   * Copy constructor.
   * 
   * @param source
   *          The source to copy.
   */
  public GECamera(GECamera source)
  {
    this.latitude  = source.latitude;
    this.longitude = source.longitude;
    this.altitude  = source.altitude;
    this.heading   = source.heading;
    this.tilt      = source.tilt;
    this.roll      = source.roll;
    this.name      = source.name;
  }
  
  /**
   * Creates a new default camera.
   * 
   * @param world
   *          The world: {@link GE#EARTH}, {@link GE#MOON}, or {@link GE#MARS}.
   */
  public GECamera(String world)
  {
    if (world == GE.MOON)
    {
      latitude = 0;
      longitude = 0;
      altitude = 5000000;
      heading = 0;
      tilt = 0;
    }
    else if (world == GE.MARS)
    {
      latitude = 0;
      longitude = 0;
      altitude = 10000000;
      heading = 0;
      tilt = 0;
    }
    else
    {
      latitude = 25;
      longitude = 0;
      altitude = 17000000;
      heading = 0;
      tilt = 0;
    }
    name = world;
  }

  /**
   * Creates a new camera from a text representation.
   * 
   * @param world
   *          the world, one of the {@link GECamera}<code>.XXXX</code> constants
   * @param text
   *          the camera to view this place in Google Eearth in the format
   *          "&lt;latitude&gt;, &lt;longitude&gt;, &lt;altitude&gt;, 
   *          &lt;heading&gt;, &lt;tilt&gt;, &lt;roll&gt;" where the spaces
   *          are optional and the fields enclosed in arrow brackets denote
   *          floating point values
   */
  public GECamera(String world, String text)
  {
    String[] fields = text.split(",");
    try { this.latitude = parseLatLon(fields[0].trim()); }
    catch (Exception e) {}
    try { this.longitude = parseLatLon(fields[1].trim()); }
    catch (Exception e) {}
    try { this.altitude = Float.parseFloat(fields[2].trim()); }
    catch (Exception e) {}
    try { this.heading = Float.parseFloat(fields[3].trim()); }
    catch (Exception e) {}
    try { this.tilt = Float.parseFloat(fields[4].trim()); }
    catch (Exception e) {}
    try { this.roll = Float.parseFloat(fields[5].trim()); }
    catch (Exception e) {}
  }

  /**
   * Moves the camera in north-south direction.
   * 
   * @param inc the increment in degrees
   */
  public void incLatitude(float inc)
  {
    latitude+=inc;
    if (latitude<-90) latitude = -90;
    if (latitude> 90) latitude =  90;      
  }
  
  /**
   * Moves the camera in east-west direction.
   * 
   * @param inc the increment in degrees
   */
  public void incLongitude(float inc)
  {
    longitude+=inc;
    if (longitude<=-180) longitude =  180;
    if (longitude>  180) longitude = -180;
  }
  
  @Override
  public boolean equals(Object obj)
  {
    if (obj==null) return false;
    if (!(obj instanceof GECamera)) return false;
    GECamera source = (GECamera)obj;

    if (this.latitude  != source.latitude ) return false;;
    if (this.longitude != source.longitude) return false;;
    if (this.altitude  != source.altitude ) return false;;
    if (this.heading   != source.heading  ) return false;;
    if (this.tilt      != source.tilt     ) return false;;
    if (this.roll      != source.roll     ) return false;;
    return true;
  }

  @Override
  public String toString()
  {
    return 
      latitude  + "," +
      longitude + "," +
      altitude  + "," +
      heading   + "," +
      tilt      + "," +
      roll;
  }
  
  public float parseLatLon(String text)
  {
    try
    {
      return Float.parseFloat(text);
    }
    catch (Exception e) {}
    try
    {
      text=text.trim();
      float sign = 1;
      if (text.endsWith("N") || text.endsWith("E"))
        sign = 1;
      else if (text.endsWith("S") || text.endsWith("W"))
        sign = -1;
      String[] fields = text.split("°");
      float d = Integer.parseInt(fields[0]);
      fields = fields[1].split("'");
      float m = Integer.parseInt(fields[0]);
      fields = fields[1].split("\"");
      float s = Float.parseFloat(fields[0]);
      return sign*(d+m/60+s/3600);
    }
    catch (Exception e)
    {
      throw new NumberFormatException();
    }
  }
  
}