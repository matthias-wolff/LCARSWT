package de.tucottbus.kt.lcars.feedback;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;

import sun.audio.AudioStream;

/**
 * A short sound used to give auditory feedback.
 * 
 * @see Eyecon
 * @author Matthias Wolff
 */
public class Earcon
{
  /**
   * The resource path of the earcon's audio file, e.g.
   * "de/tucottbus/kt/lcars/resources/audio/earcon-touch.wav".
   */
  public String resourceFile;
  
  /**
   * Returns the earcon's audio stream. 
   * 
   * @throws IOException
   *           If an I/O exception occurs.
   */
  public AudioStream getAudioStream() throws IOException
  {
    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    URL resource = classLoader.getResource(resourceFile);
    if (resource==null)
      throw new FileNotFoundException(resourceFile);
    return new AudioStream(resource.openStream());
  }
}

// EOF

