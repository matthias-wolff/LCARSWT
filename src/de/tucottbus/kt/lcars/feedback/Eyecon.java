package de.tucottbus.kt.lcars.feedback;

import de.tucottbus.kt.lcars.swt.ColorMeta;

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
  private ColorMeta[] samples;
  
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
  public ColorMeta[] getSamples()
  {
    return samples!=null ? samples.clone() : new ColorMeta[0];
  }
  
  /**
   * Sets the sequence of color values.
   * 
   * @param samples
   *          The new sequence.
   * @see #getSamples()
   */
  public void setSamples(ColorMeta[] samples)
  {
    this.samples = samples != null ? samples.clone() : null;
  }
}

// EOF

