package de.tucottbus.kt.lcars.feedback;

import de.tucottbus.kt.lcars.swt.SWTColor;

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
  private SWTColor[] samples;
  
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
  public SWTColor[] getSamples()
  {
    if (this.samples==null) return new SWTColor[0];
    SWTColor[] samples = new SWTColor[this.samples.length];
    System.arraycopy(this.samples,0,samples,0,this.samples.length);
    return samples;
  }
  
  /**
   * Sets the sequence of color values.
   * 
   * @param samples
   *          The new sequence.
   * @see #getSamples()
   */
  public void setSamples(SWTColor[] samples)
  {
    this.samples = samples;
  }
}

// EOF

