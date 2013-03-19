package de.tucottbus.kt.lcars.ge.orbits;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import de.tucottbus.kt.lcars.LCARS;
import de.tucottbus.kt.lcars.ge.GE;

/**
 * Instances of this class provide real time tracking of earth satellites including the
 * international space station. Orbit data are obtained from <a
 * href="http://www.n2yo.com">http://www.n2yo.com</a>.
 * 
 * @author Matthias Wolff, BTU Cottbus
 * @author helloworld922@www.javaprogrammingforums.com (spline interpolation)
 */
@SuppressWarnings("unused")
public class N2yoOrbit extends GEOrbit
{ 
  /**
   * The orbit of the international space station.
   */
  public static final String ID_ISS = "25544";

  /**
   * The HTTP-GET query retrieving a satellite orbit.
   */
  private static final String Q_ORBIT = "http://www.n2yo.com/sat/gg.php?s=%s";

  /**
   * The n2yo satellite ID, one of the <code>ID_XXX</code> constants.
   */
  private String satID;

  /**
   * The name of the satellite traveling this orbit. This field is initialized by
   * {@link #queryOrbit()}.
   */
  private String name;

  /**
   * The orbit.
   */
  protected Vector<Pos> orbit;

  /**
   * Creates a new satellite orbit.
   * 
   * @param satID
   *          The n2yo satellite ID, one of the <code>ID_XXX</code> constants.
   */
  public N2yoOrbit(String satID)
  {
    this.satID = satID;
    this.orbit = queryOrbit();
  }

  @Override
  public String getName()
  {
    return name;
  }

  @Override
  public String getWorld()
  {
    return GE.EARTH;
  }
  
  @Override
  public Pos getPosition(Date date)
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
      Pos pos1 = orbit.get(step-1);
      Pos pos2 = orbit.get(step  );
      Pos pos3 = orbit.get(step+1);
      Pos pos4 = orbit.get(step+2);
      if 
      (
        date.getTime()>=pos2.date.getTime() &&
        date.getTime()<=pos3.date.getTime()
      )
      {
        float f
          = (float)(date.getTime()-pos2.date.getTime())
          / (float)(pos3.date.getTime()-pos2.date.getTime());
        
        //Pos pos = getPositionLI(pos2,pos3,f);
        Pos pos = getPositionSI(pos1,pos2,pos3,pos4,f);
        pos.date = date;
        return pos;
      }
    }
    return null;
  }
  
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
  private Pos getPositionLI(Pos posA, Pos posB, float f)
  {
    // Compute latitude delta
    float dlat = posB.lat-posA.lat;

    // Compute longitude delta
    float dlon = posB.lon-posA.lon;
    if (Math.abs(dlon)>300)
    {
      if (posB.lon<0) dlon = posB.lon+360-posA.lon;
      else dlon = posB.lon-360-posA.lon;
    }

    // Compute interpolated position
    Pos pos = new Pos();
    pos.lat = posA.lat+f*dlat;
    pos.lon = posA.lon+f*dlon;
    pos.alt = (posA.alt+f*(posB.alt-posA.alt))*1000;
    pos.spd = posA.spd+f*(posB.spd-posA.spd);
    pos.hdg = dlon==0?0:(float)(Math.atan(dlon/dlat)/Math.PI*180f);
    if (pos.lon>180) pos.lon -= 360;
    if (dlon>0&&pos.hdg<0) pos.hdg += 180;
    else if (dlon<0&&pos.hdg>0) pos.hdg -= 180;
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
  private Pos getPositionSI(Pos pos1, Pos pos2, Pos pos3, Pos pos4, float f)
  {
    // Unwrap longitudes
    float lon1 = pos1.lon;
    float lon2 = lon_unwrap(lon1,pos2.lon);
    float lon3 = lon_unwrap(lon2,pos3.lon);
    float lon4 = lon_unwrap(lon3,pos4.lon);

    // Compute interpolated position
    Pos pos = new Pos();
    double[] x = {-1,0,1,2};
    double[] y = {pos1.lat,pos2.lat,pos3.lat,pos4.lat};
    pos.lat    = (float)poly_interpolate(x,y,f,3);
    float dlat = (float)poly_interpolate(x,y,f+0.01,3)-pos.lat;
    y          = new double[] {lon1,lon2,lon3,lon4};
    pos.lon    = (float)poly_interpolate(x,y,f,3);
    float dlon = (float)poly_interpolate(x,y,f+0.01,3)-pos.lon;
    pos.alt    = (pos2.alt+f*(pos3.alt-pos2.alt))*1000; // Linear!
    pos.spd    = pos2.spd+f*(pos3.spd-pos2.spd); // Linear!
    pos.hdg    = dlon==0?0:(float)(Math.atan(dlon/dlat)/Math.PI*180f);
    
    // Finish position
    if (pos.lon>180) pos.lon -= 360;
    if (lon3-lon2>0&&pos.hdg<0) pos.hdg += 180;
    else if (lon3-lon2<0&&pos.hdg>0) pos.hdg -= 180;
    return pos;
  }

  /**
   * Returns 10 (i.&nbsp;e. "teleport").
   */
  @Override
  public float getFlytoSpeed()
  {
    return 10; // i.e. teleport
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
   * Gets the orbit from <code>www.n2yo.com</code>.
   * 
   * @return
   *    The orbit.
   */
  protected Vector<N2yoOrbit.Pos> queryOrbit()
  {
    Vector<Pos> orbit = new Vector<Pos>();
    
    if (satID==null) satID = ID_ISS;
    String url = String.format(Locale.US,Q_ORBIT,satID);
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
            Pos pos  = new Pos();
            pos.lat  = Float.valueOf(tokens[0]);
            pos.lon  = Float.valueOf(tokens[1]);
            pos.alt  = Float.valueOf(tokens[2]);
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

  /**
   * Unwraps longitude values so that meaningful differences can be computed. 
   * 
   * @param lon1
   *          The first longitude.
   * @param lon2
   *          The second longitude.
   * @return The second longitude unwrapped with respect to the first one.
   */
  private static float lon_unwrap(float lon1, float lon2)
  {
    if (Math.abs(lon2-lon1)>300)
    {
      if (lon2<0) return lon2+360;
      else return lon2-360;
    }
    return lon2;
  }
  
  // -- Spline interpolation --

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
  
  // -- Main method --
  
  /**
   * DEBUGGING: Main method.
   * 
   * @param args
   *          The method does not use any command line arguments.
   */
  public static void main(String[] args)
  {
    final N2yoOrbit iss = new N2yoOrbit(ID_ISS);
    
    (new Timer()).schedule(new TimerTask()
    {
      
      @Override
      public void run()
      {
        System.out.print("\n"+iss.getPosition());
      }
    },1000,1000);
  }
}
