package de.tucottbus.kt.lcars.feedback;

import java.awt.Color;


/**
 * An audio-visual user feedback signal consisting of an {@link Earcon} and an {@link Eyecon}.
 * 
 * @author Matthas Wolff
 */
public class UserFeedback
{
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
    String earconFileBase = "de/tucottbus/kt/lcars/resources/audio";
    UserFeedback signal = new UserFeedback();
    
    switch (type)
    {
    case NONE:
    {
      Color[] samples = { null };
      signal.eyecon = new Eyecon();
      signal.eyecon.setSamples(samples);
      return signal;
    }
    case TOUCH:
      signal.earcon = new Earcon();
      signal.earcon.resourceFile = earconFileBase+"/earcon-touch.wav";
      return signal;
    case DENY:
      signal.earcon = new Earcon();
      signal.earcon.resourceFile = earconFileBase+"/earcon-deny.wav";
      return signal;
    case REC_LISTENING:
    {
      Color[] samples = { new Color(0x0099CC) };
      signal.eyecon = new Eyecon();
      signal.eyecon.setSamples(samples);
      signal.earcon = new Earcon();
      signal.earcon.resourceFile = earconFileBase+"/earcon-rec-listening.wav";
      return signal;
    }
    case REC_SLEEPING:
    {
      signal.eyecon = new Eyecon();
      Color[] samples = { new Color(0x0000FF) };
      signal.eyecon.setSamples(samples);
      signal.earcon = new Earcon();
      signal.earcon.resourceFile = earconFileBase+"/earcon-rec-sleeping.wav";
      return signal;
    }
    case REC_ACCEPTED:
    {
      Color c = new Color(0x00FF66);
      Color[] samples = new Color[13];
      for (int i=0; i<4; i++)
      {
        samples[i]=c;
        samples[i+8]=c;
      }
      signal.eyecon = new Eyecon();
      signal.eyecon.setSamples(samples);
      signal.earcon = new Earcon();
      signal.earcon.resourceFile = earconFileBase+"/earcon-rec-accepted.wav";
      return signal;
    }
    case REC_REJECTED:
    {
      Color c = new Color(0xFF0066);
      Color[] samples = new Color[25];
      for (int i=0; i<24; i++) samples[i]=c;
      signal.eyecon = new Eyecon();
      signal.eyecon.setSamples(samples);
      signal.earcon = new Earcon();
      signal.earcon.resourceFile = earconFileBase+"/earcon-rec-rejected.wav";
      return signal;
    }
    default:
      return null;
    }
  }
}

// EOF

