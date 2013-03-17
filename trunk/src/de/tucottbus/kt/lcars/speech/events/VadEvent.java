package de.tucottbus.kt.lcars.speech.events;

import de.tucottbus.kt.lcars.speech.ISpeechEngine;

/**
 * A voice activity detection (VAD) message.
 * 
 * @author Matthias Wolff, BTU Cottbus
 */
public class VadEvent extends SpeechEvent
{
  /**
   * Voice activity (&lt;0: off-line, 0: silence, &gt;0: speech)
   */
  public int activity;
  
  /**
   * Creates a new voice activity detection (VAD) message.
   * 
   * @param spe
   *          The speech engine which caused this event.
   * @param frame
   *          The zero-based speech frame index since the start-up of the speech engine.
   * @param activity
   *          The voice activity flag.
   */
  public VadEvent(ISpeechEngine spe, long frame, int activity)
  {
    super(spe,frame);
    this.activity = activity;
  }

}

// EOF

