package de.tucottbus.kt.lcars.feedback;

import java.util.Timer;
import java.util.TimerTask;

import de.tucottbus.kt.lcars.logging.Log;
import de.tucottbus.kt.lcars.swt.ColorMeta;
import sun.audio.AudioPlayer;
import sun.audio.AudioStream;

/**
 * Derived classes play {@linkplain UserFeedback audio-visual feedback signals}.
 * 
 * @author Matthias Wolff
 */
public abstract class UserFeedbackPlayer
{
  // -- Fields --
  
  /**
   * Player gives auditory feedback.
   */
  private boolean auditory;
  
  /**
   * Player gives visual feedback.
   */
  private boolean visual;
  
  /**
   * The play-back timer.
   */
  private Timer timer;

  /**
   * The samples of the currently played eyecon.
   */
  private ColorMeta[] eyeconSamples;

  /**
   * The color to display instead of <code>null</code>.
   */
  private ColorMeta fallbackColor;
  
  /**
   * The current eyecon play-back position.
   */
  private int eyeconSample;

  /**
   * The current audio stream.
   */
  AudioStream stream;
  
  /**
   * No user feedback.
   */
  public static final int NONE = 0x0000;

  /**
   * Auditory user feedback (earcons). 
   */
  public static final int AUDITORY = 0x0001;

  /**
   * Visual user feedback ("viscons").
   */
  public static final int VISUAL = 0x0002;

  /**
   * -- Reserved -- Speech feedback.
   */
  public static final int SPEECH = 0x0004;

  /**
   * All user feedback modes.
   */
  public static final int ALL = 0xFFFF;
  
  // -- Constructors --
  
  /**
   * Creates a new user feedback player.
   * 
   * @param mode
   *          The feedback mode: {@link #NONE} for no feedback, {@link #ALL} for
   *          all feedback modes, or any bitwise-or combination of
   *          <ul>
   *            <li>{@link #AUDITORY}: Player gives auditory feedback,</li>
   *            <li>{@link #VISUAL}: Player gives visual feedback, or</li>
   *            <li>{@link #SPEECH}: -- Reserved -- for speech feedback.</li>
   *          </ul>
   */
  public UserFeedbackPlayer(int mode)
  {
    auditory = (mode&AUDITORY)!=0;
    visual   = (mode&VISUAL  )!=0;
  }

  // -- Getters and setters --

  /**
   * Sets the feedback mode.
   * 
   * @param mode
   *          The new mode: {@link #NONE} for no feedback, {@link #ALL} for all
   *          feedback modes, or any bitwise-or combination of
   *          <ul>
   *            <li>{@link #AUDITORY}: Player gives auditory feedback,</li>
   *            <li>{@link #VISUAL}: Player gives visual feedback, or</li>
   *            <li>{@link #SPEECH}: -- Reserved -- for speech feedback.</li>
   *          </ul>
   * @see #getMode()
   */
  public void setMode(int mode)
  {
    auditory = (mode&AUDITORY)!=0;
    visual   = (mode&VISUAL)  !=0;
  }

  /**
   * Returns the current feedback mode.
   * 
   * @return The mode: {@link #NONE} or any bitwise-or combination of
   *         {@link #AUDITORY}, {@link #VISUAL}, and {@link #SPEECH}.
   * @see #setMode(int)
   */
  public int getMode()
  {
    int mode = 0;
    if (auditory) mode |= AUDITORY;
    if (visual  ) mode |= VISUAL;
    return mode;
  }

  // -- Operations --

  /**
   * Plays a feedback signal.
   * 
   * @param signal
   *           The signal.
   * @see #play(UserFeedback, int)
   */
  public void play(UserFeedback signal)
  {
    play(signal,ALL);
  }

  /**
   * Plays a feedback signal.
   * 
   * @param signal
   *          The signal.
   * @param mode
   *          The modes to play (if respective data are contained in <code>signal</code>):
   *          <ul>
   *            <li>{@link #AUDITORY}: Play auditory feedback,</li>
   *            <li>{@link #VISUAL}: Play visual feedback, or</li>
   *            <li>{@link #SPEECH}: -- Reserved -- for speech feedback.</li>
   *          </ul>
   *          Note: Independently of the value of <code>mode</code> a particular
   *          mode is played <em>only</em>, if the player has been configured to
   *          play it through the {@linkplain #UserFeedbackPlayer(int) constructor}
   *          or {@link #setMode(int) setMode}!
   * @see #play(UserFeedback)
   */
  public void play(UserFeedback signal, int mode)
  {
    // Play eyecon
    if ((mode&VISUAL)!=0 && visual)
      try
      {
        // Initialize play-back
        synchronized (this) //TODO: WARNING: CA2002: Do not lock on objects with weak identity
        {
          // - Skip to last sample of currently played signal
          if (eyeconSamples!=null && eyeconSamples.length>0)
          {
            ColorMeta c = eyeconSamples[eyeconSamples.length-1];
            if (c!=null || eyeconSamples.length==1) fallbackColor = c;
            writeColor(c==null?fallbackColor:c);
          }
    
          // - Stop playing
          eyeconSamples = null;
          eyeconSample = 0;
    
          // - Start playing the new signal 
          if (signal!=null && signal.eyecon!=null)
            eyeconSamples = signal.eyecon.getSamples();
        }
        
        // Start play-back thread
        if (eyeconSamples!=null && timer==null)
        {
          long period = (long)(1000./Eyecon.getSampleRate());
          timer = new Timer(true);
          timer.scheduleAtFixedRate(new TimerTask()
          {
            @Override
            public void run()
            {
              try
              {
                synchronized (UserFeedbackPlayer.this) //TODO: WARNING: CA2002: Do not lock on objects with weak identity
                {
                  if (eyeconSamples!=null&&eyeconSample<eyeconSamples.length)
                  {
                    ColorMeta c = eyeconSamples[eyeconSample];
                    if (eyeconSample==eyeconSamples.length-1 && (c!=null||eyeconSamples.length==1))
                      fallbackColor = c;
                    eyeconSample++;
                    writeColor(c==null?fallbackColor:c);
                  }
                  else
                  {
                    timer.cancel();
                    timer.purge();
                    timer = null;
                  }
                }
              }
              catch (Exception e)
              {
                Log.err("Some error in " + UserFeedback.class.getSimpleName() + "occured while playing eyecon samples.", e);
              }
            }
          },period,period);
        }
      }
      catch (Exception e)
      {
        Log.err("Some error in " + UserFeedback.class.getSimpleName() + "occured while playing eyecon samples.", e);
      }
    
    // Play earcon
    if (signal.earcon!=null && (mode&AUDITORY)!=0 && auditory)
    {
      try
      {
        if (stream!=null)
          AudioPlayer.player.stop(stream);
        stream = signal.earcon.getAudioStream();
        AudioPlayer.player.start(stream);
      }
      catch (Exception e)
      {
        Log.err("Some error in " + UserFeedback.class.getSimpleName() + "occured while playing earcon samples.", e);
      }
    }
  }

  /**
   * Cancels all running feedbacks.
   */
  public void cancel()
  {
    if (timer!=null)
      try
      {
        eyeconSamples = null;
        eyeconSample = 0;
        timer.cancel();
        timer.purge();
        timer = null;
      } catch (Exception e)
      {
        Log.err("Failed to cancel visual UserFeedbackPlayer",e);
      }
    
    try
    {
      if (stream!=null)
        AudioPlayer.player.stop(stream);
    } catch (Exception e)
    {
      Log.err("Failed to cancel audio UserFeedbackPlayer",e);
    }
  }
  
  // -- Abstract API --
  
  /**
   * Presents a new color at the eyecon player's output device.
   * 
   * @param color
   *          The color. Implementations are expected to accept <code>null</code> as color value and
   *          display a default color in this case.
   */
  public abstract void writeColor(ColorMeta color);
  
}

// EOF

