package de.tucottbus.kt.lcarsx.al;

/**
 * An {@linkplain AudioPlayer audio player} event.
 * 
 * @author Matthas Wolff
 */
public class AudioPlayerEvent
{
  // -- Static fields --
  
  /**
   * Play-back has started.
   */
  public static final int STARTED = 1;

  /**
   * Play-back has stopped.
   */
  public static final int STOPPED = 2;

  /**
   * Beginning of an audio track.
   */
  public static final int TRACKSTART = 3;

  /**
   * End of an audio track.
   */
  public static final int TRACKEND = 4;
  
  // -- Fields --
  
  /**
   * The audio player in which the event occurred.
   */
  public AudioPlayer player;
  
  /**
   * The track in which the event occurred (can be <code>null</code>).
   */
  public AudioTrack track;

  /**
   * The media time.
   */
  public long time;
  
  /**
   * The event type (see static fields).
   */
  public int type;
  
  // -- Constructors --
  
  /**
   * Creates a new audio player event.
   * 
   * @param player
   *          The audio player in which the event occurred.
   * @param track
   *          The track in which the event occurred (can be <code>null</code>).
   * @param time
   *          The media time.
   * @param type
   *          The event type (see static fields).
   */
  public AudioPlayerEvent(AudioPlayer player, AudioTrack track, long time, int type)
  {
    this.player = player;
    this.track  = track;
    this.time   = time;
    this.type   = type;
  }
  
  // -- Overrides --
  
  @Override
  public String toString()
  {
    String s = getClass().getSimpleName()+",";
    switch (type)
    {
    case STARTED   : s+="STARTED";     break;
    case STOPPED   : s+="STOPPED";     break;
    case TRACKSTART: s+="TRACK START"; break;
    case TRACKEND  : s+="TRACK END";   break;
    default        : s+=type;          break;
    }
    s += ","+(track==null?"null":("\""+track.getTitle()+"\""));
    return s;
  }
  
}

// EOF
