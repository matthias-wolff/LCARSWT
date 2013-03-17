package de.tucottbus.kt.lcars.feedback;

import java.awt.Color;

/**
 * A short sequence of color values used to give visual feedback.
 * 
 * @see Earcon
 * @author Matthas Wolff
 */
public class Eyecon
{
  /**
   * The color sequence.
   */
  private Color[] samples;
  
  // -- Getters and setters --
  
  /**
   * Returns the sampling rate in Hz. Colors in the eyecon's sequence change at this rate during
   * play-back.
   */
  public static final float getSampleRate()
  {
    return 25;
  }

  /**
   * Returns the sequence of color values. The returned array is a copy. Modifying it has no effect
   * on the eyecon.
   * 
   * @see #setSamples(Color[])
   */
  public Color[] getSamples()
  {
    if (this.samples==null) return new Color[0];
    Color[] samples = new Color[this.samples.length];
    System.arraycopy(this.samples,0,samples,0,this.samples.length);
    return samples;
  }
  
  /**
   * Sets the sequence of color values.
   * 
   * @param color
   *          The new sequence.
   * @see #getSamples()
   */
  public void setSamples(Color[] samples)
  {
    this.samples = samples;
  }
}

// EOF

