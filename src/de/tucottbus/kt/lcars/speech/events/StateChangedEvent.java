package de.tucottbus.kt.lcars.speech.events;

import de.tucottbus.kt.lcars.speech.ISpeechEngine;

/**
 * A state changed event. The current state can be retrieved through field {@link SpeechEvent#spe}.
 * 
 * @author Matthias Wolff
 */
public class StateChangedEvent extends SpeechEvent
{
  
  public boolean listening;
  
  public boolean sleeping;

  /**
   * Creates a new state changed event.
   * 
    * @param spe
    *          The speech engine which caused this event.
    * @param frame
    *          The zero-based speech frame index since the start-up of the speech engine.
   */
  public StateChangedEvent(ISpeechEngine spe, long frame)
  {
    super(spe,frame);
  }

}

// EOF

