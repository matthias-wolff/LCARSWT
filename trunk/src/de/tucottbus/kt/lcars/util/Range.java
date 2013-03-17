package de.tucottbus.kt.lcars.util;

public class Range
{
  public double min;
  public double max;
  
  public Range(double min, double max)
  {
    this.min = Math.min(min,max);
    this.max = Math.max(min,max);
  }

}