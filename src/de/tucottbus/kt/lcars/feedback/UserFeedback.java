package de.tucottbus.kt.lcars.feedback;

import de.tucottbus.kt.lcars.swt.SwtColor;


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
      signal.eyecon = new Eyecon();
      signal.eyecon.setSamples(new SwtColor[] { null });
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
      signal.eyecon = new Eyecon();
      signal.eyecon.setSamples(new SwtColor[]{ new SwtColor(0x0099CC) });
      signal.earcon = new Earcon();
      signal.earcon.resourceFile = earconFileBase+"/earcon-rec-listening.wav";
      return signal;
    }
    case REC_SLEEPING:
    {
      signal.eyecon = new Eyecon();
      signal.eyecon.setSamples(new SwtColor[] {new SwtColor(0x0000FF)});
      signal.earcon = new Earcon();
      signal.earcon.resourceFile = earconFileBase+"/earcon-rec-sleeping.wav";
      return signal;
    }
    case REC_ACCEPTED:
    {
      SwtColor c = new SwtColor(0x00FF66);
      SwtColor[] samples = new SwtColor[13];
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
      SwtColor c = new SwtColor(0xFF0066);
      SwtColor[] samples = new SwtColor[25];
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

