package de.tucottbus.kt.lcars.speech.events;

import de.tucottbus.kt.lcars.speech.ISpeechEngine;

/**
 * Instances of this class represent events occurring in speech engines.
 * 
 * @author Matthias Wolff
 */
public abstract class SpeechEvent
{   
   /**
    * The speech engine which caused this event.
    */
   public ISpeechEngine spe;

   /**
    * The zero-based speech frame index since the start-up of the speech engine.
    */
   public long frame;
   
   /**
    * Creates a new speech event.
    * 
    * @param spe
    *          The speech engine which caused this event.
    * @param frame
    *          The zero-based speech frame index since the start-up of the speech engine.
    */
   public SpeechEvent(ISpeechEngine spe, long frame)
   {
     this.spe   = spe;
     this.frame = frame;
   }
}

// EOF

