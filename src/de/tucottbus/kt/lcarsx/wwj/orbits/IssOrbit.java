package de.tucottbus.kt.lcarsx.wwj.orbits;

import java.util.Timer;
import java.util.TimerTask;

/**
 * The actual orbit of the international space station.
 *
 * @author Matthias Wolff, BTU Cottbus-Senftenberg
 */
public class IssOrbit extends N2yoOrbit
{

  @Override
  public String getSatID()
  {
    return "25544";
  }

  // == MAIN METHOD ==
  
  /**
   * DEBUGGING: Main method.
   * 
   * @param args
   *          The method does not use any command line arguments.
   */
  public static void main(String[] args)
  {
    final IssOrbit iss = new IssOrbit();
    
    (new Timer()).schedule(new TimerTask()
    {
      
      @Override
      public void run()
      {
        System.out.print("\n"+iss.getState());
      }
    },1000,1000);
  }

}
