package de.tucottbus.kt.lcars.speech;

import de.tucottbus.kt.lcars.speech.events.SpeechEvent;

/**
 * 
 * 
 * @author Matthias Wolff
 */
public interface ISpeechEventListener
{
  /**
   * Called upon {@link SpeechEvent}s.
   * 
   * @param event The speech event.
   */
  public void speechEvent(SpeechEvent event);
}
