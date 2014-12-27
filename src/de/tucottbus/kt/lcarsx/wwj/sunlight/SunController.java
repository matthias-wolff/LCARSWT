/*
 * Copyright © 2014, Terramenta. All rights reserved.
 *
 * This work is subject to the terms of either
 * the GNU General Public License Version 3 ("GPL") or 
 * the Common Development and Distribution License("CDDL") (collectively, the "License").
 * You may not use this work except in compliance with the License.
 * 
 * You can obtain a copy of the License at
 * http://opensource.org/licenses/CDDL-1.0
 * http://opensource.org/licenses/GPL-3.0
 */
package de.tucottbus.kt.lcarsx.wwj.sunlight;

import gov.nasa.worldwind.Model;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.layers.SkyGradientLayer;
import gov.nasa.worldwind.terrain.Tessellator;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 *
 * @author Chris.Heidt
 */
public class SunController implements PropertyChangeListener 
{

  private static final Logger logger = Logger.getLogger(SunController.class.getName());
  private final AtmosphereLayer atmosphereLayer;
  private final RectangularNormalTessellator suntessellator;
  private final SunLayer sunLayer;
  private final Model model;
  private final Tessellator originalTessellator;
  private final SkyGradientLayer originalAtmosphere;

  static
  {
    logger.setLevel(Level.WARNING);
  }

  public SunController(Model model, SunLayer sunLayer, AtmosphereLayer atmosphereLayer)
  {
    this.sunLayer = sunLayer;
    this.model = model;
    this.originalTessellator = model.getGlobe().getTessellator();
    this.suntessellator = new RectangularNormalTessellator();
    this.atmosphereLayer = new AtmosphereLayer();

    List<Layer> atmos = model.getLayers().getLayersByClass(SkyGradientLayer.class);
    if (!atmos.isEmpty())
    {
      originalAtmosphere = (SkyGradientLayer) atmos.get(0);
    }
    else
    {
      originalAtmosphere = new SkyGradientLayer();
      model.getLayers().add(originalAtmosphere);
    }
    sunLayer.addPropertyChangeListener(this);
  }

  @Override
  public void propertyChange(PropertyChangeEvent evt)
  {
    if (evt.getPropertyName().equals("Enabled"))
    {
      boolean enabled = (Boolean) evt.getNewValue();
      if (enabled)
      { // enable shading, use AtmosphereLayer
        model.getGlobe().setTessellator(suntessellator);
        for (int i = 0; i < this.model.getLayers().size(); i++)
        {
          Layer l = this.model.getLayers().get(i);
          if (l instanceof SkyGradientLayer)
          {
            this.atmosphereLayer.setEnabled(l.isEnabled());
            this.model.getLayers().set(i, this.atmosphereLayer);
            break;
          }
        }
      }
      else
      { // disable lighting, use SkyGradientLayer
        model.getGlobe().setTessellator(originalTessellator);
        for (int i = 0; i <this.model.getLayers().size(); i++)
        {
          Layer l = this.model.getLayers().get(i);
          if (l instanceof AtmosphereLayer)
          {
            this.originalAtmosphere.setEnabled(l.isEnabled());
            this.model.getLayers().set(i, this.originalAtmosphere);
            break;
          }
        }
      }
    }
  }

  /**
   *
   * @param date
   */
  public void update(Date date)
  {
    double JD = calcJulianDate(date);
    double[] ll = subsolarPoint(JD);
    Position sunPosition = new Position(LatLon.fromRadians(ll[0], ll[1]), 0);
    logger.log(Level.FINE, "SUN Position @ {0} ({1}): {2}", new Object[]
    { date, JD, sunPosition });
    Vec4 sunVector = model.getGlobe().computePointFromPosition(sunPosition).normalize3();

    this.sunLayer.setSunDirection(sunVector);
    this.suntessellator.setLightDirection(sunVector.getNegative3());
    this.atmosphereLayer.setSunDirection(sunVector);
  }

  /**
   * Computes the Julian date from the given date/time.
   *
   * From "Practical Astronomy with Your Calculator" by Peter Duffet-Smith
   *
   * @param utc
   *          The date and time to convert.
   * @return The Julian date..
   */
  public static double calcJulianDate(Date utc)
  {
    final long SECONDS_IN_FULL_DAY = 86400;
    final long SECONDS_IN_HALF_DAY = 43200;
    // Create Gregorian start date used for comparison
    Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
    cal.set(1592, 10, 15);
    Date startGregorian = cal.getTime();

    // Convert incoming date to UTC
    cal.setTime(utc);

    long y = cal.get(Calendar.YEAR);
    long m = cal.get(Calendar.MONTH) + 1;
    long d = cal.get(Calendar.DAY_OF_MONTH);
    if (m <= 2)
    {
      y -= 1;
      m += 12;
    }
    long A = y / 100;
    long B = utc.after(startGregorian) ? 2 - A + (A / 4) : 0;
    long C = (y < 0) ? (long) ((365.25 * y) - 0.75) : (long) (365.25 * y);
    long D = (long) (30.6001 * (m + 1));
    double offsetFromNoon = ((double) (cal.get(Calendar.HOUR_OF_DAY) * 60 * 60
        + cal.get(Calendar.MINUTE) * 60 + cal.get(Calendar.SECOND) - SECONDS_IN_HALF_DAY))
        / SECONDS_IN_FULL_DAY;
    return B + C + D + d + 1720995 + offsetFromNoon;
  }

  /**
   * Calculates the subsolar latitude/longitude coordinates of sun at a given
   * time. The subsolar point on the earth is where the sun is perceived to be
   * directly overhead (in zenith), that is where the sun's rays are hitting the
   * planet exactly perpendicular to its surface.
   *
   * Original c++ source found here: http://www.psa.es/sdg/archive/SunPos.cpp.
   * Algorithm changed to use alternative greenwich mean sidereal time
   * calculation.
   *
   * @param julianDate
   *          The date/time used to compute the sun's position.
   * @return The latitude and longitude of the subsolar point. [radians]
   */
  public static double[] subsolarPoint(double julianDate)
  {
    // Main variables
    double elapsedJulianDays;
    double eclipticLongitude;
    double eclipticObliquity;
    double rightAscension, declination;
    double longitude;
    // Calculate difference in days between the current Julian Day
    // and JD 2451545.0, which is noon 1 January 2000 Universal Time
    {
      elapsedJulianDays = julianDate - 2451545.0;
    }
    // Calculate ecliptic coordinates (ecliptic longitude and obliquity of the
    // ecliptic in radians but without limiting the angle to be less than 2*Pi
    // (i.e., the result may be greater than 2*Pi)
    {
      double omega = 2.1429 - 0.0010394594 * elapsedJulianDays;
      double meanLongitude = 4.8950630 + 0.017202791698 * elapsedJulianDays; // Radians
      double meanAnomaly = 6.2400600 + 0.0172019699 * elapsedJulianDays;
      eclipticLongitude = meanLongitude + 0.03341607 * Math.sin(meanAnomaly)
          + 0.00034894 * Math.sin(2 * meanAnomaly) - 0.0001134 - 0.0000203
          * Math.sin(omega);
      eclipticObliquity = 0.4090928 - 6.2140e-9 * elapsedJulianDays + 0.0000396
          * Math.cos(omega);
    }
    // Calculate celestial coordinates ( right ascension and declination ) in
    // radians but without limiting the angle to be less than 2*Pi (i.e., the
    // result may be greater than 2*Pi)
    {
      double sinEclipticLongitude = Math.sin(eclipticLongitude);
      double dY = Math.cos(eclipticObliquity) * sinEclipticLongitude;
      double dX = Math.cos(eclipticLongitude);
      rightAscension = Math.atan2(dY, dX);
      if (rightAscension < 0.0)
      {
        rightAscension = rightAscension + Math.PI * 2.0;
      }
      declination = Math.asin(Math.sin(eclipticObliquity)
          * sinEclipticLongitude);
    }
    // Convert from celestial coordinates to horizontal coordinates; solar
    // latitude and declination are identical.
    {
      // Alternative from: see http://aa.usno.navy.mil/faq/docs/GAST.php
      double greenwichMeanSiderealTime = (18.697374558 + 24.06570982441908 * elapsedJulianDays) % 24;
      longitude = rightAscension
          - Math.toRadians(greenwichMeanSiderealTime * 15);
    }

    while (declination > Math.PI / 2.0)
    {
      declination -= Math.PI;
    }
    while (declination <= -Math.PI / 2.0)
    {
      declination += Math.PI;
    }
    while (longitude > Math.PI)
    {
      longitude -= Math.PI * 2.0;
    }
    while (longitude <= -Math.PI)
    {
      longitude += Math.PI * 2.0;
    }
    // Return latitude and longitude [radians]
    return new double[] { declination, longitude };
  }

}
