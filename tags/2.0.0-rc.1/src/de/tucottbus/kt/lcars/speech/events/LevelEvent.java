
package de.tucottbus.kt.lcars.speech.events;

import de.tucottbus.kt.lcars.speech.ISpeechEngine;

/**
 * A speech audio input level event. 
 * 
 * @author Matthias Wolff
 */
public class LevelEvent extends SpeechEvent
{
  /**
   * The speech input level.
   */
  public float level;

  /**
   * Creates a new level event.
   * 
   * @param spe
   *          The speech engine which caused this event.
   * @param frame
   *          The zero-based speech frame index since the start-up of the speech engine.
   * @param level
   *          The speech input level.
   */
  public LevelEvent(ISpeechEngine spe, long frame, float level)
  {
    super(spe,frame);
    this.level = level;
  }
  
  /**
   * Returns the amplitude for the speech input level represented by this message.
   */
  public int getAmp()
  {
    return (int)(Math.pow(10,level/20)*32768);
  }
}

// EOF
