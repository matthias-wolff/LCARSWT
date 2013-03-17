package de.tucottbus.kt.lcars.al;

import javax.sound.sampled.AudioFormat;

/**
 * A self-contained buffer of audio data.
 * 
 * @author Matthias Wolff
 */
public class AudioBuffer
{
  /**
   * The audio format.
   */
  public AudioFormat format;
  
  /**
   * The audio data.
   */
  public byte[] data;
  
  /**
   * The levels of the audio channels in dB.
   */
  private float[] levels;

  /**
   * Creates a new audio buffer.
   * 
   * @param format
   *          The audio format.
   * @param data
   *          The audio data.
   */
  public AudioBuffer(AudioFormat format, byte[] data)
  {
    this.format = format;
    this.data   = data;
    this.levels = null; 
  }

  /**
   * Measures the levels in this audio buffer.
   * 
   * @return An array of levels in dB, one level value per channel.
   */
  protected float[] getLevels()
  {
    // NOTE: This implementation is specific for the hard-coded audio format
    //       returned by AudioTrack.getAudioFormat(): 16 bit per sample, signed!

    if (levels!=null) return levels;
    
    int c,k;
    int C = format.getChannels();
    levels = new float[C];
    if (data!=null)
    {
      for (c=0,k=0; k<data.length; k+=2,c++)
      {
        if (c>=C) c=0;
        float x = ((data[k+1]<<8) | (data[k]&0xFF)) / 32768.f;
        levels[c] = Math.max(levels[c],Math.abs(x));   
      }
      for (c=0; c<C; c++)
        levels[c] = (float)(20*Math.log10(levels[c]));
    }
    else
    {
      for (c=0; c<C; c++)
        levels[c] = Float.NEGATIVE_INFINITY;
    }

    return levels;
  }
}

// EOF
