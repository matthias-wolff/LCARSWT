package de.tucottbus.kt.lcarsx.al;

import java.awt.EventQueue;
import java.awt.Window;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.tritonus.share.sampled.file.TAudioFileFormat;

/**
 * An audio track based on an audio file. See source code of methods {@link #play1()} and
 * {@link #play2()} for play-back examples.
 * 
 * <p>
 * The class is capable of parsing MP3 audio and meta data using mp3spi (see <a target="_blank"
 * href="http://www.javazoom.net/mp3spi/mp3spi.html">http
 * ://www.javazoom.net/mp3spi/mp3spi.html</a>). For this to work make sure to include
 * <code>jl1.0.1.jar</code>, <code>mp3spi.9.5.jar</code> and <code>tritonus_share.jar</code> in the
 * class path.
 * </p>
 * 
 * @author Matthias Wolff
 */
public class AudioTrack
{
  /**
   * The audio file.
   */
  private File file; 
  
  /**
   * The number of audio channels.
   */
  private int channels;
  
  /**
   * The sample rate in Hertz.
   */
  private float sampleRate;
  
  /**
   * The duration in seconds.
   */
  private float length;

  /**
   * Track is excluded from a play list.
   */
  private boolean excluded;
  
  /**
   * The song's MP3 meta data.
   */
  private Map<?,?> mp3MetaData;
  
  /**
   * The meta data guessed from the file name of the song.
   */
  private HashMap<String,String> guessedMetaData;
  
  // -- Static API --
  
  /**
   * Returns the meta data contained an MP3 file of the song using Mp3Spi.
   * 
   * @see AudioTrack#guessMetaData(File)
   */
  public static Map<?,?> getMetaData(File file)
  {
    try
    {
      AudioFileFormat baseFileFormat = null;
      baseFileFormat = AudioSystem.getAudioFileFormat(file);
      if (baseFileFormat instanceof TAudioFileFormat)
        return ((TAudioFileFormat)baseFileFormat).properties();
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
    return null;
  }

  /**
   * Guesses the song's meta data from its file name. For this to work, file names are expected to
   * have the format
   * <pre>
   * .../&lt;artist&gt;/&lt;year&gt; - &lt;album&gt;/&lt;trackno&gt; - &lt;songname&gt;.mp3
   * </pre>
   * 
   * @return The guessed meta data.
   * @see AudioTrack#getMetaData(File)
   */
  public static HashMap<String,String> guessMetaData(File file)
  {
    HashMap<String,String> md = new HashMap<String,String>();
    File f = file.getAbsoluteFile();
    
    // Guess track number and song name
    String s = f.getName();
    s = s.substring(0,s.lastIndexOf('.'));
    int i = s.indexOf('-');
    if (i>=0 && i<5)
    {
      md.put("track",s.substring(0,i).trim());
      md.put("title",s.substring(i+1).trim());
    }
    else
      md.put("title",s.trim());
    
    // Guess year and album
    f = f.getParentFile();
    if (f==null) return md;
    s = f.getName();
    i = s.indexOf('-');
    if (i>=3 && i<6)
    {
      md.put("date",s.substring(0,i).trim());
      md.put("album",s.substring(i+1).trim());
    }
    else
      md.put("album",s.trim());
    if (f.getParentFile()==null) return md;

    // Guess author
    f = f.getParentFile();
    if (f==null) return md;
    md.put("author",f.getName().trim());
    
    return md;
  }
  
  // -- Constructors --

  /**
   * Creates a new audio track object from an audio file.
   * 
   * @param file
   *          The audio file.
   * @throws IllegalArgumentException
   *           If <code>file</code> is <code>null</code> or not denoting an existing file.
   * @throws IOException
   *           If an I/O error occurs on <code>file</code>.
   * @throws UnsupportedAudioFileException
   *           If <code>file</code> does not denote a recognized audio file.
   */
  public AudioTrack(File file)
  throws UnsupportedAudioFileException, IOException
  {
    if (file==null || !file.exists() || !file.isFile())
      throw new IllegalArgumentException();
    
    this.file     = file;
    this.length = Float.NaN;
    
    // Get the song's audio stream format
    AudioInputStream ais = AudioSystem.getAudioInputStream(this.file);
    AudioFormat      af  = ais.getFormat();
    this.channels        = af.getChannels();
    this.sampleRate      = af.getSampleRate();
    ais.close();
    
    // Get the song's meta data
    guessedMetaData = guessMetaData(this.file);
    mp3MetaData     = getMetaData(file);
  }
  
  // -- Getters and setters --
  
  /**
   * Returns the MP3 file described by this song info.
   */
  public File getFile()
  {
    return file;
  }

  /**
   * Returns the audio format of this track.
   */
  public AudioFormat getAudioFormat()
  {
    return new AudioFormat
    (
      AudioFormat.Encoding.PCM_SIGNED,
      getSampleRate(),
      16,
      getChannels(),
      getChannels() * 2,
      getSampleRate(),
      false
    );
  }

  /**
   * Returns an audio input stream for the song.
   * <p>
   * <b>Note:</b> Applications must close the returned stream when it is no longer needed.
   * </p>
   * 
   * @throws UnsupportedAudioFileException
   *           If the underlying {@linkplain #file file} has no recognized audio format.
   * @throws IOException
   *           On I/O errors on the underlying {@linkplain #file file}.
   */
  public AudioInputStream getAudioInputStream()
  throws UnsupportedAudioFileException, IOException
  {
    AudioInputStream ais = AudioSystem.getAudioInputStream(file);
    ais = AudioSystem.getAudioInputStream(getAudioFormat(),ais);
    return ais; 
  }
  
  /**
   * Returns the number of audio channels.
   */
  public int getChannels()
  {
    return channels;
  }

  /**
   * Returns the sample rate.
   */
  public float getSampleRate()
  {
    return sampleRate;
  }
  
  /**
   * Returns the length of the track in milliseconds.
   */
  public float getLength()
  {
    if (Float.isNaN(length) && mp3MetaData!=null)
      if (mp3MetaData.containsKey("duration"))
        length = ((Long)mp3MetaData.get("duration"))/1.E6f;

    return length;
  }

  /**
   * Returns the excluded flag of this track. For details see {@link #setExcluded(boolean)}.
   */
  public boolean isExcluded()
  {
    return excluded;
  }
  
  /**
   * Marks the track as excluded from a play list. This property is just stored with the track, it
   * has no influence on its behavior. Particularly is does not prevent from playing the track with
   * an {@link AudioPlayer}.
   * 
   * @param excluded
   *          The new excluded flag.
   * @see #isExcluded()
   */
  public void setExcluded(boolean excluded)
  {
    this.excluded = excluded;
  }
  
  /**
   * Returns a property from the song's meta data.
   * 
   * @param key
   *          The key.
   * @return The property value.
   */
  public String getProperty(String key)
  {
    if (mp3MetaData!=null && mp3MetaData.containsKey(key))
      try
      {
        return (String)mp3MetaData.get(key);
      }
      catch (Exception e) {}
   if (guessedMetaData!=null && guessedMetaData.containsKey(key))
      try
      {
        return guessedMetaData.get(key);
      }
      catch (Exception e) {}
   return null;
  }
  
  /**
   * Returns the title of the song.
   */
  public String getTitle()
  {
    return getProperty("title");
  }

  /**
   * Returns the artist(s) performing the song.
   */
  public String getArtist()
  {
    return getProperty("author");
  }

  /**
   * Returns the album the song is part of.
   */
  public String getAlbum()
  {
    return getProperty("album");
  }

  /**
   * Returns the track number of the song on the album.
   */
  public String getTrackNumber()
  {
    String trackNumber = null;
    if (mp3MetaData!=null)
    {
      if (mp3MetaData.containsKey("mp3.id3tag.track"))
        trackNumber = (String)mp3MetaData.get("mp3.id3tag.track");
    }
    if (guessedMetaData!=null)
    {
      if (guessedMetaData.containsKey("track"))
        trackNumber = guessedMetaData.get("track");
    }
    if (trackNumber!=null)
      while (trackNumber.length()<2)
        trackNumber = "0"+trackNumber;
    return trackNumber;
  }

  /**
   * Returns the year in which the song was created.
   */
  public String getYear()
  {
    return getProperty("date");
  }

  /**
   * Determines if two audio tracks are compatible. Compatible tracks can be
   * played at the same {@link SourceDataLine}.
   * 
   * @param other
   *          The other audio track.
   */
  public boolean isCompatible(AudioTrack other)
  {
    if (other==null) return false;
    if (this.channels  !=other.channels  ) return false;
    if (this.sampleRate!=other.sampleRate) return false;
    return true;
  }
  
  // -- Operations --
  
  /**
   * Prints all meta information.
   */
  public void dump()
  {
    System.out.print("\nMP3 FILE META DATA:");
    if (mp3MetaData!=null)
      for (Object key : mp3MetaData.keySet())
      {
        Object value = mp3MetaData.get(key);
        System.out.print("\n- "+key+" ["+value.getClass().getSimpleName()+"] = "+value);
      }
    else
      System.out.print("\n- NONE");
    
    System.out.print("\nMP3 GUESSED META DATA:");
    if (guessedMetaData!=null)
      for (String key : guessedMetaData.keySet())
        System.out.print(String.format("\n- %s = \"%s\"",key,guessedMetaData.get(key)));
    else
      System.out.print("\n- NONE");    
  }

  /**
   * Returns all string properties of this track.
   */
  public Map<String,String> getProperties()
  {
    HashMap<String,String> props = new HashMap<String,String>();
    
    if (mp3MetaData!=null)
      for (Object key : mp3MetaData.keySet())
      {
        Object value = mp3MetaData.get(key);
        props.put((String)key,""+value);
      }
    
    if (guessedMetaData!=null)
      for (String key : guessedMetaData.keySet())
        if (!props.containsKey(key))
          props.put(key,guessedMetaData.get(key));

    return props;
  }
  
  // -- Overrides --
  
  @Override
  protected void finalize() throws Throwable
  {
    reset();
    super.finalize();
  }

  // -- EXPERIMENTAL: Advanced Play-back API --
  
  /**
   * EXPERIMENTAL: The audio input stream used by {@link #fetch()}.
   */
  AudioInputStream ais;

  /**
   * EXPERIMENTAL: A window of 40 milliseconds audio frames representing a certain
   * {@linkplain #history history} and {@linkplain #future future} of audio data.
   */
  Vector<AudioBuffer> win;
  
  /**
   * EXPERIMENTAL: Index in {@link #win} of the next buffer to deliver by {@link #fetch()}.
   */
  int winPtr;

  /**
   * EXPERIMENTAL: The number of bytes required to store 40 milliseconds of audio data from the {@linkplain #ais
   * audio input stream}.
   */
  int bufsize;

  /**
   * EXPERIMENTAL: The seconds of past audio data to keep in the current {@linkplain Window window}
   * in seconds.
   */
  float history;

  /**
   * EXPERIMENTAL: The seconds of audio data to prefetch and keep in the current {@linkplain Window
   * window}.
   */
  float future;

  /**
   * EXPERIMENTAL: The media time of the last buffer in {@link #win}. 
   */
  float prefetch;
  
  /**
   * EXPERIMENAL: Converts seconds to number of buffers. A buffer contains (as accurately as
   * possible) 1/25 s of audio data.
   * 
   * @param s
   *          Seconds.
   * @return Number of audio buffers.
   */
  protected int s2b(float s)
  {
    return (int)(s*25);
  }

  /**
   * EXPERIMENTAL: Reads a buffer of samples from the audio input stream.
   * 
   * @param bufsize
   *          The desired buffer size.
   * @return The buffer (may be shorter than desired or <code>null</code> if no data could be read).
   */
  protected byte[] readBuffer(int bufsize)
  {
    if (ais==null)
    {
      prefetch = 0;
      return null;
    }

    byte[] buffer = new byte[bufsize];
    int m = 0;
    while (m<bufsize)
      try
      {
        int n = ais.read(buffer,m,Math.min(buffer.length,bufsize-m));
        if (n<0) break;
        m+=n;
      }
      catch (IOException e)
      {
        e.printStackTrace();
      }
    if (m==0) return null;
    prefetch += (float)m/2/channels/sampleRate;
    if (m==bufsize)
      return buffer;
    else
    {
      byte[] buffer2 = new byte[m];
      System.arraycopy(buffer,0,buffer2,0,m);
      return buffer2;
    }
  }
  
  /**
   * EXPERIMENTAL: Starts fetching audio data. If fetching has already been started, the method
   * resets the audio input.
   * 
   * @param history
   *          The audio history length in seconds.
   * @param future
   *          The prefetch time in seconds.
   * @see #fetch()
   * @see #reset()
   */
  protected void prefetch(float history, float future)
  {
    // Initialize
    reset();
    this.history = history;
    this.future  = future;
    this.bufsize = (int)(sampleRate/25)*channels*2; // 0.04 s
    
    // Prefetch
    try
    {
      ais = getAudioInputStream();
      win = new Vector<AudioBuffer>();
      synchronized (win)
      {
        while (win.size()<s2b(this.future))
        {
          byte[] buffer = readBuffer(bufsize);
          if (buffer==null) break;
          win.add(new AudioBuffer(ais.getFormat(),buffer));
        }
      }
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }

  /**
   * EXPERIMENTAL: Returns the next buffer of audio data. Application may (but do not have to)
   * invoke {@link #prefetch(float, float)} in advance to specify audio history and prefetch times
   * and to prefetch audio data.
   * 
   * @return A buffer filled with audio data or <code>null</code> if there are no more data. After
   *         the method once has returned <code>null</code> it will keep returning <code>null</code>
   *         until the track is reset by invoking {@link #reset()}.
   * 
   * @see #prefetch(float, float)
   * @see #reset()
   */
  public AudioBuffer fetch()
  {
    // First call: initialize and prefetch if not yet done
    if (ais==null) prefetch(1,4);   
    if (ais==null) return null;

    // All calls: remove first and fetch next buffer
    synchronized (win)
    {
      if (winPtr>s2b(history+0.5f))
      {
        win.remove(0);
        winPtr--;
      }
    }
    EventQueue.invokeLater(new Runnable() // CHECK: EventQueue too fat? 
    {
      @Override
      public void run()
      {
        byte[] buffer = readBuffer(bufsize);
        if (buffer!=null)
          synchronized (win)
          {
            win.add(new AudioBuffer(ais.getFormat(),buffer));
          }
      }
    });
    
    // Return the current audio buffer or null if there is no more audio data
    synchronized (win)
    {
      if (winPtr<win.size())
        return win.get(winPtr++);
      else return null;
    }
  }

  /**
   * EXPERIMENTAL: Resets and rewinds this track. The method releases all system resources acquired
   * for the track.
   * 
   * @see #prefetch(float, float)
   * @see #fetch()
   */
  public void reset()
  {
    if (ais!=null)
      try
      {
        ais.close();
      }
      catch (Exception e)
      {
        e.printStackTrace();
      }
    ais      = null;
    win      = null;
    winPtr   = 0;
    prefetch = 0;
  }

  /**
   * EXPERIMENTAL: Returns the current window of audio data containing history and future. The
   * current audio buffer is at position {@link #s2b(float) s2b}<code>(</code>{@link #history}
   * <code>)</code>.
   * 
   * @param mediaTime
   *          The current media time. The value is needed to ensure that the currently played audio
   *          buffer is exactly at the stated position in the window. If such accuracy is not
   *          required use {@link #getWindow()} as it is significantly faster.
   * 
   * @see #prefetch(float, float)
   * @see #fetch()
   */
  public Vector<AudioBuffer> getWindow(float mediaTime)
  {
    if (mediaTime<0) return win;

    synchronized (win)
    {
      int tail = Math.round((prefetch-mediaTime)*25);
      int len  = win.size();
      int size = s2b(history+future);
      int first = len-tail-s2b(history);
  
      Vector<AudioBuffer> ret = new Vector<AudioBuffer>(size);
      for (int i = first; i<0; i++) ret.add(null);
      for (int i = 0; i<win.size(); i++)
      {
        ret.add(win.get(i));
        if (ret.size()==size) break;
      }
      while (ret.size()<size) ret.add(null);
      return ret;
    }
  }

  /**
   * EXPERIMENTAL: Returns the current window of audio data containing history and future. The
   * current audio buffer is approximately at position {@link #s2b(float) s2b}<code>(</code>
   * {@link #history} <code>)</code>.
   * 
   * @see #getWindow(float)
   * @see #prefetch(float, float)
   * @see #fetch()
   */
  public Vector<AudioBuffer> getWindow()
  {
    return getWindow(-1);
  }
  
  /**
   * EXPERIMENTAL: Returns the media time until which audio data have been prefetched.
   */
  public float getPrefetchTime()
  {
    return prefetch;
  }
  
  // -- Debugging and testing --
  
  /**
   * DEBUGGING ONLY: Simple play-back method.
   */
  private void play1()
  {
    try
    {
      byte[]           buf  = new byte[1024];
      AudioInputStream ais  = getAudioInputStream();
      DataLine.Info    dli  = new DataLine.Info(SourceDataLine.class,ais.getFormat());
      SourceDataLine   line = (SourceDataLine)AudioSystem.getLine(dli);
      line.open(ais.getFormat());
      line.start();

      while (true)
      {
        int n = ais.read(buf,0,buf.length);
        if (n<0) break;
        line.write(buf,0,n);
      }
      
      line.drain();
      line.stop();
      line.close();
      ais.close();
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }
  
  /**
   * DEBUGGING ONLY: Play-back method with prefetching.
   */
  private void play2()
  {
    try
    {
      AudioFormat    af   = getAudioFormat();
      DataLine.Info  dli  = new DataLine.Info(Clip.class,af);
      SourceDataLine line = (SourceDataLine)AudioSystem.getLine(dli);
      line.open(af);
      line.start();
      
      prefetch(1,4);
      while (true)
      {
        AudioBuffer abuf = fetch();
        if (abuf==null || abuf.data==null) break;
        line.write(abuf.data,0,abuf.data.length);
        float[] levels = abuf.getLevels();
        System.out.print(String.format(Locale.ENGLISH,
          "\nWINLEN=%3d, BUFLEN=%4d/%4d, LEVEL=(%4.1f,%4.1f) dB",
          getWindow().size(),abuf.data.length,bufsize,levels[0],levels[1]));
      }
      reset();
  
      line.drain();
      line.stop();
      line.close();
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }
  
  private void play3()
  {
    
    try
    {
      AudioInputStream ais = getAudioInputStream();
      DataLine.Info info = new DataLine.Info(Clip.class, ais.getFormat());
      Clip clip = (Clip)AudioSystem.getLine(info);
      clip.open(ais);

      // due to bug in Java Sound, explicitly exit the VM when
      // the sound has stopped.
      clip.addLineListener(new LineListener() 
      {
        public void update(LineEvent event) 
        {
          System.out.println(event);
        }
      });

      (new Thread()
      {
        @Override
        public void run()
        {
          while (true)
          {
            if (clip.getMicrosecondPosition()>10e6)
              clip.setMicrosecondPosition((long) 1e6);
            System.out.println(clip.getMicrosecondPosition());
            try { Thread.sleep(1000); } catch (InterruptedException e) {}
          }
        }
      }).start();
      
      // play the sound clip
      clip.start();
      Thread.sleep(100000);
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }
  
  /**
   * Main method for testing.
   * <p><b>Usage:</b>
   * <pre>  java [-cp ...] de.tucottbus.kt.lcars.al.AudioTrack &lt;audiofile&gt;</pre>
   * </p>
   * 
   * @param args
   *          The command line arguments (see above).
   */
  public static void main(String[] args)
  {
    try
    {
      AudioTrack track = new AudioTrack(new File(args[0]));
      System.out.println("play3 ..."); track.play3();
      System.out.println("play2 ..."); track.play2();
      System.out.println("play1 ..."); track.play1();
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }
  
}

// EOF
