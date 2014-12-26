package de.tucottbus.kt.lcarsx.wwj;

import gov.nasa.worldwind.View;

public class Camera
{
  private double latitude  = 0.0;
  private double longitude = 0.0;
  private double altitude  = 0.0;
  private double heading   = 0.0;
  private double tilt      = 0.0;
  private double roll      = 0.0;
  private double range     = 0.0;
  
  public Camera(double latitude, double longitude, double altitude, double heading, double tilt, double roll, double range)
  {
    this.latitude  = latitude;
    this.longitude = longitude;
    this.altitude  = altitude;
    this.heading   = heading;
    this.tilt      = tilt;
    this.roll      = roll;
    this.range     = range;
  }

  static Camera fromString(String text)
  {
    Camera camera = new Camera(0,0,0,0,0,0,0);
    String[] fields = text.split(",");
    try { camera.latitude = parseLatLon(fields[0].trim()); }
    catch (Exception e) {}
    try { camera.longitude = parseLatLon(fields[1].trim()); }
    catch (Exception e) {}
    try { camera.altitude = Float.parseFloat(fields[2].trim()); }
    catch (Exception e) {}
    try { camera.heading = Float.parseFloat(fields[3].trim()); }
    catch (Exception e) {}
    try { camera.tilt = Float.parseFloat(fields[4].trim()); }
    catch (Exception e) {}
    try { camera.roll = Float.parseFloat(fields[5].trim()); }
    catch (Exception e) {}
    return camera;
  }
  
  public static Camera fromView(View view)
  {
    Camera camera = new Camera(0,0,0,0,0,0,0);
    camera.latitude  = view.getEyePosition().getLatitude().degrees;
    camera.longitude = view.getEyePosition().getLongitude().degrees;
    camera.altitude  = view.getEyePosition().getAltitude();
    camera.heading   = view.getHeading().degrees;
    camera.tilt      = view.getPitch().degrees;
    camera.roll      = view.getRoll().degrees;
    return camera;
  }
  
  public double getLatitude()
  {
    return latitude;
  }

  public double getLongitude()
  {
    return longitude;
  }

  public double getAltitude()
  {
    return altitude;
  }

  public double getHeading()
  {
    return heading;
  }

  public double getTilt()
  {
    return tilt;
  }

  public double getRange()
  {
    return range;
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
  
  public static double parseLatLon(String text)
  {
    try
    {
      return Float.parseFloat(text);
    }
    catch (Exception e) {}
    try
    {
      text=text.trim();
      double sign = 1;
      if (text.endsWith("N") || text.endsWith("E"))
        sign = 1;
      else if (text.endsWith("S") || text.endsWith("W"))
        sign = -1;
      String[] fields = text.split("°");
      double d = Integer.parseInt(fields[0]);
      fields = fields[1].split("'");
      double m = Integer.parseInt(fields[0]);
      fields = fields[1].split("\"");
      double s = Double.parseDouble(fields[0]);
      return sign*(d+m/60+s/3600);
    }
    catch (Exception e)
    {
      throw new NumberFormatException();
    }
  }
}
