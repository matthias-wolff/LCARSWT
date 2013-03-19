package de.tucottbus.kt.lcars.util;

import java.io.Serializable;
import java.util.Vector;

// TODO: Write JavaDoc!
/**
 * A statistics of load factors. A load factor is the percentage of real time consumed by a
 * periodically executed program task.
 * 
 * @author Matthias Wolff
 */
public class LoadStatistics implements Serializable
{
  private static final long         serialVersionUID = 1L;
  private transient Vector<Integer> samples;
  private transient int             sampleSize;
  private transient int             eventCounter;
  private int                       load;
  private int                       eventsPerPeriod;

  public LoadStatistics(int sampleSize)
  {
    this.samples         = new Vector<Integer>();
    this.sampleSize      = sampleSize;
    this.eventCounter    = 0;
    this.load            = 0;
    this.eventsPerPeriod = 0;
  }
  
  public void add(int loadFactor)
  {
    samples.add(new Integer(loadFactor));
    if (samples.size()>sampleSize) samples.remove(0);
    load = 0;
    if (samples.size()>0)
    {      
      for (Integer time : samples) load += time;
      load /= samples.size();
    }
    eventCounter++;
  }

  public int getLoad()
  {
    return load;
  }
  
  public int getEventCount()
  {
    return eventCounter;
  }
  
  public int getEventsPerPeriod()
  {
    return eventsPerPeriod;
  }
  
  public void period()
  {
    eventsPerPeriod = eventCounter;
    eventCounter = 0;
  }
}

// EOF
