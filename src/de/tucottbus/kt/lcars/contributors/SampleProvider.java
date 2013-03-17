package de.tucottbus.kt.lcars.contributors;

import java.awt.Color;

import de.tucottbus.kt.lcars.util.Range;

public interface SampleProvider
{
  public Range getSample();
  public float getLevel();
  public Color getSampleColor();
}
