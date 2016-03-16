package de.tucottbus.kt.lcars.feedback;

import de.tucottbus.kt.lcars.swt.ColorMeta;


/**
 * An audio-visual user feedback signal consisting of an {@link Earcon} and an {@link Eyecon}.
 * 
 * @author Matthas Wolff
 */
public class UserFeedback
{
  //-- static final fields
  private static final String EARCON_FILE_BASE = "de/tucottbus/kt/lcars/resources/audio";
  private static final String EARCON_TOUCH = EARCON_FILE_BASE+"/earcon-touch.wav";
  private static final String EARCON_DENY = EARCON_FILE_BASE+"/earcon-deny.wav";
  private static final String EARCON_REC_LISTENING = EARCON_FILE_BASE+"/earcon-rec-listening.wav";
  private static final String EARCON_REC_SLEEPING= EARCON_FILE_BASE+"/earcon-rec-sleeping.wav";
  private static final String EARCON_REC_ACCEPTED= EARCON_FILE_BASE+"/earcon-rec-accepted.wav";
  private static final String EARCON_REC_REJECTED= EARCON_FILE_BASE+"/earcon-rec-rejected.wav";
  
  /**
   * The user feedback types.
   */
  public enum Type
  {
    NONE, TOUCH, DENY,
    REC_LISTENING, REC_SLEEPING, REC_ACCEPTED, REC_REJECTED
  };
  
  /**
   * The earcon.
   */
  public Earcon earcon;
  
  /**
   * The eyecon.
   */
  public Eyecon eyecon;
  
  // -- Instance getters --
  
  /**
   * Returns a predefined user feedback signal.
   * 
   * @param type
   *          The {@linkplain Type type}.
   */
  public static UserFeedback getInstance(UserFeedback.Type type)
  {
    UserFeedback signal = new UserFeedback();
    
    switch (type)
    {
    case NONE:
    {
      signal.eyecon = new Eyecon();
      signal.eyecon.setSamples(new ColorMeta[] { null });
      return signal;
    }
    case TOUCH:
      signal.earcon = new Earcon();
      signal.earcon.resourceFile = EARCON_TOUCH;
      return signal;
    case DENY:
      signal.earcon = new Earcon();
      signal.earcon.resourceFile = EARCON_DENY;
      return signal;
    case REC_LISTENING:
    {
      signal.eyecon = new Eyecon();
      signal.eyecon.setSamples(new ColorMeta[]{ new ColorMeta(0x0099CC) });
      signal.earcon = new Earcon();
      signal.earcon.resourceFile = EARCON_REC_LISTENING;
      return signal;
    }
    case REC_SLEEPING:
    {
      signal.eyecon = new Eyecon();
      signal.eyecon.setSamples(new ColorMeta[] {new ColorMeta(0x0000FF)});
      signal.earcon = new Earcon();
      signal.earcon.resourceFile = EARCON_REC_SLEEPING;
      return signal;
    }
    case REC_ACCEPTED:
    {
      ColorMeta c = new ColorMeta(0x00FF66);
      ColorMeta[] samples = new ColorMeta[13];
      for (int i=0; i<4; i++)
        samples[i+8]=samples[i]=c;

      signal.eyecon = new Eyecon();
      signal.eyecon.setSamples(samples);
      signal.earcon = new Earcon();
      signal.earcon.resourceFile = EARCON_REC_ACCEPTED;
      return signal;
    }
    case REC_REJECTED:
    {
      ColorMeta c = new ColorMeta(0xFF0066);
      ColorMeta[] samples = new ColorMeta[25];
      for (int i=0; i<24; i++) samples[i]=c;
      signal.eyecon = new Eyecon();
      signal.eyecon.setSamples(samples);
      signal.earcon = new Earcon();
      signal.earcon.resourceFile = EARCON_REC_REJECTED;
      return signal;
    }
    default:
      return null;
    }
  }
}

// EOF

