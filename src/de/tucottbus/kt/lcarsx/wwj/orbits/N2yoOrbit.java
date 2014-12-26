package de.tucottbus.kt.lcarsx.wwj.orbits;

import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Position;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.Locale;
import java.util.Vector;

import de.tucottbus.kt.lcars.LCARS;

/**
 * Instances of this class provide real time tracking of earth satellites
 * including the international space station. Orbit data are obtained from <a
 * href="http://www.n2yo.com">http://www.n2yo.com</a>.
 * 
 * @author Matthias Wolff, BTU Cottbus
 * @author helloworld922@www.javaprogrammingforums.com (spline interpolation)
 */
public abstract class N2yoOrbit extends Orbit
{ 
  private static final double EARTH_RADIUS = 6371000;
  //private static final double MOON_RADIUS = 1738000;
  //private static final double MARS_RADIUS = 3400000;
  
  /**
   * The HTTP-GET query retrieving a satellite orbit.
   */
  private static final String Q_ORBIT = "http://www.n2yo.com/sat/gg.php?s=%s";

  /**
   * The name of the satellite traveling this orbit. This field is initialized by
   * {@link #queryOrbit()}.
   */
  private String name;

  /**
   * The orbit.
   */
  protected Vector<OrbitState> orbit;

  /**
   * Creates a new satellite orbit.
   */
  public N2yoOrbit()
  {
    // Load orbit
    this.orbit = queryOrbit();
  }

  /**
   * Must be overridden by implementations an return the N2YO satellite ID.
   */
  public abstract String getSatID(); 
  
  @Override
  public String getName()
  {
    return name;
  }

  @Override
  public Position getDefaultEyePosition()
  {
    return new Position(Angle.ZERO, Angle.ZERO, 27000000);
  }

  @Override
  public boolean controlsLatitude()
  {
    return true;
  }

  @Override
  public boolean controlsLongitude()
  {
    return true;
  }

  @Override
  public boolean controlsAltitude()
  {
    return true;
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

  @Override
  protected OrbitState getState(Date date)
  {
    if (date==null) date = new Date();
    if (date.before(getFirstDate())) return null;
    if (date.after(getLastDate()))
    {
      orbit = queryOrbit();
      if (date.after(getLastDate())) return null;
    }
    
    for (int step=1; step<orbit.size()-2; step++)
    {
      OrbitState pos1 = orbit.get(step-1);
      OrbitState pos2 = orbit.get(step  );
      OrbitState pos3 = orbit.get(step+1);
      OrbitState pos4 = orbit.get(step+2);
      if 
      (
        date.getTime()>=pos2.date.getTime() &&
        date.getTime()<=pos3.date.getTime()
      )
      {
        float f
          = (float)(date.getTime()-pos2.date.getTime())
          / (float)(pos3.date.getTime()-pos2.date.getTime());
        
        //OrbitState pos = getPositionLI(pos2,pos3,f);
        OrbitState pos = getPositionSI(pos1,pos2,pos3,pos4,f);

        // Standard pitch: five degrees below the horizon
        double a = Math.asin(EARTH_RADIUS/(EARTH_RADIUS+pos.alt))/Math.PI*180-5;
        pos.pitch = Angle.fromDegrees(a); 

        pos.date = date;
        return pos;
      }
    }
    return null;
  }

  // -- Operations --
  
  /**
   * Gets the orbit from <code>www.n2yo.com</code>.
   * 
   * @return
   *    The orbit.
   */
  protected Vector<N2yoOrbit.OrbitState> queryOrbit()
  {
    Vector<OrbitState> orbit = new Vector<OrbitState>();
    
    String url = String.format(Locale.US,Q_ORBIT,getSatID());
    LCARS.log("N2Y","HTTP-GET \""+url+"\"");
    HttpURLConnection conn;
    try
    {
      conn = (HttpURLConnection)(new URL(url)).openConnection();
      conn.setRequestMethod("GET");
      conn.connect();
      InputStream in = conn.getInputStream();
      BufferedReader reader = new BufferedReader(new InputStreamReader(in));
      for (int line=1; true; line++)
      {
        String text = reader.readLine();
        if (text==null) break;
        //LCARS.log("N2Y",line+": "+text);
        try
        {
          String[] tokens = text.split("\\|");
          if (line==1)
          {
            name = tokens[2];
            //LCARS.log("N2Y","("+line+") satname=\""+satName+"\"");
          }
          else
          {
            OrbitState pos  = new OrbitState();
            pos.lat  = Angle.fromDegreesLatitude(Double.valueOf(tokens[0]));
            pos.lon  = Angle.fromDegreesLongitude(Double.valueOf(tokens[1]));
            pos.alt  = Double.valueOf(tokens[2]);
            pos.date = new Date(Long.valueOf(tokens[3])*1000);
            pos.spd  = Float.valueOf(tokens[4]);
            orbit.add(pos);
            //LCARS.log("N2Y","("+line+") "+pos.toString());
          }
        }
        catch (Exception e)
        {
          LCARS.err("N2Y","Cannot parse line "+line+" \""+text+"\", reason:");
          e.printStackTrace();
        }
      }
      conn.disconnect();
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
    return orbit;
  }
  
  // -- Private API --

  /**
   * Get position by linear interpolation.
   * 
   * @param posA
   *          The left position.
   * @param posB
   *          The right position.
   * @param f
   *          The interpolation factor, 0 (left position) ... 1 (right position).
   * @return The interpolated position.
   */
  @SuppressWarnings("unused")
  private OrbitState getPositionLI(OrbitState posA, OrbitState posB, float f)
  {
    // Compute latitude delta
    double dlat = posB.lat.degrees-posA.lat.degrees;

    // Compute longitude delta
    double dlon = posB.lon.degrees-posA.lon.degrees;
    if (Math.abs(dlon)>300)
    {
      if (posB.lon.degrees<0) dlon = posB.lon.degrees+360-posA.lon.degrees;
      else dlon = posB.lon.degrees-360-posA.lon.degrees;
    }

    // Compute interpolated position
    double lat = posA.lat.degrees+f*dlat;
    double lon = posA.lon.degrees+f*dlon;
    double alt = (posA.alt+f*(posB.alt-posA.alt))*1000;
    double spd = posA.spd+f*(posB.spd-posA.spd);
    double hdg = dlon==0?0:Math.atan(dlon/dlat)/Math.PI*180;
    if (lon>180) lon -= 360;
    if (dlon>0&&hdg<0) hdg += 180;
    else if (dlon<0&&hdg>0) hdg -= 180;

    OrbitState pos = new OrbitState();
    pos.lat = Angle.fromDegreesLatitude(lat);
    pos.lon = Angle.fromDegreesLongitude(lon);
    pos.alt = alt;
    pos.spd = spd;
    pos.hdg = Angle.fromDegrees(hdg);
    return pos;
  }

  /**
   * Get position by spline interpolation.
   * 
   * @param pos1
   *          The first position.
   * @param pos2
   *          The second position.
   * @param pos3
   *          The third position.
   * @param pos4
   *          The forth position.
   * @param f
   *          The interpolation factor between the 2nd and 3rd position, 0 (2nd position) ... 1 (3rd
   *          position).
   * @return The interpolated position.
   */
  private OrbitState getPositionSI(OrbitState pos1, OrbitState pos2, OrbitState pos3, OrbitState pos4, float f)
  {
    // Unwrap longitudes
    double lon1 = pos1.lon.degrees;
    double lon2 = lon_unwrap(lon1,pos2.lon.degrees);
    double lon3 = lon_unwrap(lon2,pos3.lon.degrees);
    double lon4 = lon_unwrap(lon3,pos4.lon.degrees);

    // Compute interpolated position
    double[] x  = {-1,0,1,2};
    double[] y  = {pos1.lat.degrees,pos2.lat.degrees,pos3.lat.degrees,pos4.lat.degrees};
    double lat  = poly_interpolate(x,y,f,3);
    double dlat = poly_interpolate(x,y,f+0.1,3)-poly_interpolate(x,y,f-0.1,3);;
    y           = new double[] {lon1,lon2,lon3,lon4};
    double lon  = poly_interpolate(x,y,f,3);
    double dlon = poly_interpolate(x,y,f+0.1,3)-poly_interpolate(x,y,f-0.1,3);
    double alt  = (pos2.alt+f*(pos3.alt-pos2.alt))*1000; // Linear!
    double spd  = pos2.spd+f*(pos3.spd-pos2.spd); // Linear!
    double hdg  = dlon==0?0:Math.atan(dlon/dlat)/Math.PI*180;
    
    // Finish position
    if (lon>180) lon -= 360;
    if (lon3-lon2>0&&hdg<0) hdg += 180;
    else if (lon3-lon2<0&&hdg>0) hdg -= 180;

    // Make orbit state
    return new OrbitState(lat,lon,alt,spd,hdg,0);
  }

  /**
   * Returns the first date for which orbit positions are available.
   */
  private Date getFirstDate()
  {
    return orbit.get(1).date;
  }

  /**
   * Returns the last date for which orbit positions are available.
   */
  private Date getLastDate()
  {
    return orbit.get(orbit.size()-2).date;
  }

  /**
   * Unwraps longitude values so that meaningful differences can be computed. 
   * 
   * @param lon1
   *          The first longitude.
   * @param lon2
   *          The second longitude.
   * @return The second longitude unwrapped with respect to the first one.
   */
  private static double lon_unwrap(double lon1, double lon2)
  {
    if (Math.abs(lon2-lon1)>300)
    {
      if (lon2<0) return lon2+360;
      else return lon2-360;
    }
    return lon2;
  }
  
  // -- Private API -- Spline interpolation --

  /**
   * The Gaussian elimination algorithm.
   * <p><b>Author:</b> helloworld922@www.javaprogrammingforums.com</p>
   * 
   * @param matrix
   *          The coefficient matrix.
   * @return The solution vector.
   */
  private static double[] lin_solve(double[][] matrix)
  {
    double[] results = new double[matrix.length];
    int[] order = new int[matrix.length];
    for (int i = 0; i<order.length; ++i)
    {
      order[i] = i;
    }
    for (int i = 0; i<matrix.length; ++i)
    {
      // partial pivot
      int maxIndex = i;
      for (int j = i+1; j<matrix.length; ++j)
      {
        if (Math.abs(matrix[maxIndex][i])<Math.abs(matrix[j][i]))
        {
          maxIndex = j;
        }
      }
      if (maxIndex!=i)
      {
        // swap order
        {
          int temp = order[i];
          order[i] = order[maxIndex];
          order[maxIndex] = temp;
        }
        // swap matrix
        for (int j = 0; j<matrix[0].length; ++j)
        {
          double temp = matrix[i][j];
          matrix[i][j] = matrix[maxIndex][j];
          matrix[maxIndex][j] = temp;
        }
      }
      if (Math.abs(matrix[i][i])<1e-15) { throw new RuntimeException(
          "Singularity detected"); }
      for (int j = i+1; j<matrix.length; ++j)
      {
        double factor = matrix[j][i]/matrix[i][i];
        for (int k = i; k<matrix[0].length; ++k)
        {
          matrix[j][k] -= matrix[i][k]*factor;
        }
      }
    }
    for (int i = matrix.length-1; i>=0; --i)
    {
      // back substitute
      results[i] = matrix[i][matrix.length];
      for (int j = i+1; j<matrix.length; ++j)
      {
        results[i] -= results[j]*matrix[i][j];
      }
      results[i] /= matrix[i][i];
    }
    double[] correctResults = new double[results.length];
    for (int i = 0; i<order.length; ++i)
    {
      // switch the order around back to the original order
      correctResults[order[i]] = results[i];
    }
    return results;
  }

  /**
   * Spline interpolation.
   * <p><b>Author:</b> helloworld922@www.javaprogrammingforums.com</p>
   * 
   * @param dataX
   *          Set of x values (power+1 elements)
   * @param dataY
   *          Set of y values (power+1 elements)
   * @param x
   *          The x value to interpolate at.
   * @param power
   *          The interpolation order (preferably 3).
   * @return The interpolated y value
   */
  private static double poly_interpolate
  (
    double[] dataX,
    double[] dataY,
    double x,
    int power
  )
  {
    int xIndex = 0;
    while (xIndex<dataX.length-(1+power+(dataX.length-1)%power)
        &&dataX[xIndex+power]<x)
    {
      xIndex += power;
    }

    double matrix[][] = new double[power+1][power+2];
    for (int i = 0; i<power+1; ++i)
    {
      for (int j = 0; j<power; ++j)
      {
        matrix[i][j] = Math.pow(dataX[xIndex+i],(power-j));
      }
      matrix[i][power] = 1;
      matrix[i][power+1] = dataY[xIndex+i];
    }
    double[] coefficients = lin_solve(matrix);
    double answer = 0;
    for (int i = 0; i<coefficients.length; ++i)
    {
      answer += coefficients[i]*Math.pow(x,(power-i));
    }
    return answer;
  }
}
