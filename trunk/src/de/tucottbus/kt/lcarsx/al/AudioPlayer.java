package de.tucottbus.kt.lcarsx.al;

import java.io.File;
import java.util.Vector;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.SourceDataLine;

/**
 * A player for {@link AudioTrack}s. The class supports continuous play-back of multiple tracks. See
 * source code of the {@linkplain #main(String[]) main} method for an application example.
 * 
 * @author Matthias Wolff
 */
public class AudioPlayer implements Runnable
{
  // -- Fields --
  
  /**
   * The current audio track.
   */
  private AudioTrack track;

  /**
   * The next audio track.
   */
  private AudioTrack nextTrack;
  
  /**
   * The current media time in microseconds.
   */
  private long time;
  
  /**
   * The play-back tread.
   */
  private Thread playThread;

  /**
   * Run flag for the {@linkplain #playThread play-back thread}.
   */
  private boolean run;
  
  /**
   * Pause flag;
   */
  private boolean pause;
  
  /**
   * The list of song event listeners.
   */
  private Vector<IAudioPlayerEventListener> listeners;
  
  /**
   * The master gain control (only available while playing).
   */
  private FloatControl gainCtrl;
  
  // -- Constructors --
  
  /**
   * Internal constructor. Call {@link AudioPlayer#getInstance()} to get the
   * singleton.
   */
  private AudioPlayer()
  {
    this.time = 0;
    this.listeners = new Vector<IAudioPlayerEventListener>();
  }
  
  // -- Getters and setters --
  
  /**
   * Returns the current audio track.
   */
  public AudioTrack getCurrentTrack()
  {
    return track;
  }
  
  /**
   * Sets the current track of this audio player.
   * 
   * @param track
   *          The audio track.
   * @see #setNextTrack(AudioTrack)
   */
  public void setCurrentTrack(AudioTrack track)
  {
    stop();
    this.track = track;
  }

  /**
   * Returns the next audio track.
   */
  public AudioTrack getNextTrack()
  {
    return nextTrack;
  }
  
  /**
   * Sets the track to play after the current one.
   * 
   * @param track
   *          The audio track.
   * @see #setCurrentTrack(AudioTrack)
   */
  public void setNextTrack(AudioTrack track)
  {
    this.nextTrack = track;
  }
  
  /**
   * Returns the current song time in seconds.
   */
  public float getMediaTime()
  {
    return ((float)time)/1.E6f;
  }

  /**
   * Sets the current song time.
   * 
   * @param time
   *          The time in seconds.
   */
  public void setMediaTime(float time)
  {
    // TODO: implement it
  }
  
  /**
   * Returns the duration of the song.
   */
  public float getDuration()
  {
    return track.getDuration();
  }

  /**
   * Returns the current audio window.
   */
  public Vector<AudioBuffer> getAudioWindow()
  {
    if (track!=null) return track.getWindow(getMediaTime());
    return new Vector<AudioBuffer>();
  }
  
  /**
   * Determines if the song is playing. The method also returns <code>true</code> if the song is
   * paused.
   * 
   * @see #isPaused()
   */
  public boolean isPlaying()
  {
    return run;
  }
  
  /**
   * Determines if the song is paused.
   */
  public boolean isPaused()
  {
    return pause;
  }
  
  /**
   * Returns the master gain control of this player. The method will retrun <code>null</code>
   * no play-back is in progress (i. e. if {@link #isPlaying()} returns <code>false</code>.
   */
  public FloatControl getGainControl()
  {
    return gainCtrl;
  }
  
  // -- Operations --

  /**
   * Starts playing back the current audio track or resumes from pausing. If there is no current
   * track, the method does nothing.
   * 
   * @see #pause()
   * @see #stop()
   */
  public void play()
  {
    if (track==null) return;
    run   = true;
    pause = false;
    if (playThread!=null)
      synchronized (playThread) { playThread.notifyAll(); }
    if (playThread==null || !playThread.isAlive())
    {
      playThread = new Thread(this);
      playThread.setDaemon(true);
      playThread.start();
    }
  }

  /**
   * Pauses playing back. If the player is already paused or has not been started, the method does
   * nothing.
   * 
   * @see #play()
   * @see #stop()
   */
  public void pause()
  {
    pause = true;
  }

  /**
   * Stops playing back and clears the current and next audio track. You need to call
   * {@link #setCurrentTrack(AudioTrack)} before restarting the player!
   * 
   * @see #play()
   * @see #pause()
   */
  public void stop()
  {
    run = false;
    if (playThread!=null)
    {
      synchronized (playThread) { playThread.notifyAll(); }
      if (playThread.isAlive())
        try { playThread.join(); }
        catch (InterruptedException e) { e.printStackTrace(); }
    }
    playThread = null;
    track = null;
    nextTrack = null;
  }

  // -- Event handling --

  /**
   * Registers a new song event listener. If the listener is already registered, the method does
   * nothing. Listeners do not need to unregister. This is done automatically when they are
   * garbage-collected.
   * 
   * @param listener
   *          The listener.
   * @see #removeEventListener(IAudioPlayerEventListener)
   * @see #fireEvent(AudioPlayerEvent)
   */
  public void addEventListener(IAudioPlayerEventListener listener)
  {
    if (listener==null) return;
    if (listeners.contains(listener)) return;
    listeners.add(listener);
  }

  /**
   * Removes a song event listener. If the listener is not registered, the method does nothing.
   *  
   * @param listener
   *          The listener.
   * @see #addEventListener(IAudioPlayerEventListener)
   * @see #fireEvent(AudioPlayerEvent)
   */
  public void removeEventListener(IAudioPlayerEventListener listener)
  {
    listeners.remove(listener);
  }
  
  /**
   * Fires a song event. The method invokes {@link IAudioPlayerEventListener#processEvent(AudioPlayerEvent)
   * processEvent(event)} on all registered listeners.
   * 
   * @param event
   *          The event.
   * @see #addEventListener(IAudioPlayerEventListener)
   * @see #removeEventListener(IAudioPlayerEventListener)
   */
  protected void fireEvent(AudioPlayerEvent event)
  {
    for (IAudioPlayerEventListener listener : listeners)
      listener.processEvent(event);
  }
  
  // -- Overrides --

  @Override
  protected void finalize() throws Throwable
  {
    try
    {
      stop();
      listeners.clear();
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
    super.finalize();
  }

  // -- Implementation of the Runnable interface --

  @Override
  public void run()
  {
    while (run && track!=null)
    {
      long marker = 0;
      
      // Play current track
      try
      {
        // - Initialize
        AudioFormat    af   = track.getAudioFormat();
        DataLine.Info  dli  = new DataLine.Info(SourceDataLine.class,af);
        SourceDataLine line = (SourceDataLine)AudioSystem.getLine(dli);
        line.open(af);
        line.start();
        gainCtrl = (FloatControl)line.getControl(FloatControl.Type.MASTER_GAIN);
        
        // - Play it
        synchronized (playThread)
        {
          fireEvent(new AudioPlayerEvent(this,track,time,AudioPlayerEvent.STARTED));
          fireEvent(new AudioPlayerEvent(this,track,time,AudioPlayerEvent.TRACKSTART));
          while (run)
          {
            // - Pause control
            if (pause)
            {
              if (line.isRunning()) line.stop();
              try { playThread.wait(); } catch (InterruptedException e) {}
            }
            if (!line.isRunning()) line.start();

            AudioBuffer abuf = track.fetch();
            
            // - End of track: Play next track on the same line (if possible) 
            if (abuf==null && nextTrack!=null && track.isCompatible(nextTrack))
            {
              track.reset();
              fireEvent(new AudioPlayerEvent(this,track,time,AudioPlayerEvent.TRACKEND));
              track = nextTrack;
              nextTrack = null;
              abuf = track.fetch();
              marker = line.getMicrosecondPosition();
              fireEvent(new AudioPlayerEvent(this,track,time,AudioPlayerEvent.TRACKSTART));
            }
            if (abuf==null) break;
            time = line.getMicrosecondPosition()-marker;
            line.write(abuf.data,0,abuf.data.length);
          }
          if (run) line.drain(); else line.flush(); 
          if (run) run = nextTrack!=null;
        }
        
        // - Clean up
        track.reset();
        gainCtrl = null;
        line.stop();
        line.close();
        fireEvent(new AudioPlayerEvent(this,track,time,AudioPlayerEvent.STOPPED));
      }
      catch (Exception e)
      {
        e.printStackTrace();
      }
      
      // Play next track on a new line
      if (run)
      {
        track = nextTrack;
        nextTrack = null;
      }
    }
  }
  
  // -- Statics --
  
  protected static AudioPlayer player;
  
  static
  {
    player = new AudioPlayer();
  }
  
  public static AudioPlayer getInstance()
  {
    return player;
  }
  
  /**
   * Main method used for testing only.
   * 
   * @param args
   *          The command line arguments.
   */
  public static void main(String[] args)
  {
    //String f1 = "C:/Users/wolff/Music/Tangerine Dream/1985 - Dream Sequence/A01 - The Dream Is Always the Same.mp3";
    //String f1 = "C:/Users/wolff/Music/Phil Collins/1990 - Serious Hits Live/09 - In the Air tonight.mp3";
    //String f1 = "C:/Users/wolff/Music/Tangerine Dream/2006 - Jeanne d'Arc/07 - Le Combat du Sang.mp3";
    //String f1 = "C:/Users/wolff/Music/Tangerine Dream/1990 - East/Tangerine Dream_East_007_Tangerine Dream_Nomad's Scale.mp3";
    //String f1 = "D:/private/Music/Klötzelmönch/Audio/Klötzelmönch.cpr.wav";

    String f1 = "C:/Users/wolff/Music/Tangerine Dream/1993 - Turn of the Tides/01 - Pictures at an Exhibition.mp3";
    String f2 = "C:/Users/wolff/Music/Tangerine Dream/1993 - Turn of the Tides/02 - Firetongues.mp3";    
    
    player = new AudioPlayer();
    
    try
    {
      AudioTrack song1 = new AudioTrack(new File(f1));
      AudioTrack song2 = new AudioTrack(new File(f2));
      
      player.addEventListener(new IAudioPlayerEventListener()
      {
        @Override
        public void processEvent(AudioPlayerEvent event)
        {
          System.out.print("\n["+event.toString()+"]");
        }
      });
      player.setCurrentTrack(song1);
      player.setNextTrack(song2);
      player.play();
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }  
}

// EOF
