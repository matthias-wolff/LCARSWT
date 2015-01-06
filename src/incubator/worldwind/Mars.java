package incubator.worldwind; /* gov.nasa.worldwind.globes */
/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.globes.EllipsoidalGlobe;

public class Mars extends EllipsoidalGlobe
{
  public static final double WGS84_EQUATORIAL_RADIUS = 3396200.0; // ellipsoid equatorial getRadius, in meters
  public static final double WGS84_POLAR_RADIUS = 3376200.0; // ellipsoid polar getRadius, in meters
  public static final double WGS84_ES = 0.00589; // eccentricity squared, semi-major axis

  //public static final double ELEVATION_MIN = -800d; // Depth of Hellas Basin
  //public static final double ELEVATION_MAX = 22000d; // Height of Olympus Mons

  public Mars()
  {
    super(WGS84_EQUATORIAL_RADIUS, WGS84_POLAR_RADIUS, WGS84_ES,
        EllipsoidalGlobe.makeElevationModel(
            AVKey.MARS_ELEVATION_MODEL_CONFIG_FILE,
            "incubator/worldwind/MarsElevationModel.xml"));
  }

  public String toString()
  {
    return "Mars";
  }
}