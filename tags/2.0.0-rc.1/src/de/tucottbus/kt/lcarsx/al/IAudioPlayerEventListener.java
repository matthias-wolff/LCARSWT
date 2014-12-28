package de.tucottbus.kt.lcarsx.al;

/**
 * Implementations can listen to {@linkplain AudioPlayerEvent audio player events}.
 * 
 * @author Matthias Wolff
 */
public interface IAudioPlayerEventListener
{
  /**
   * Invoked on listeners when audio player events occur.
   * 
   * @param event
   *          The event.
   */
  public void processEvent(AudioPlayerEvent event);
}

// EOF

