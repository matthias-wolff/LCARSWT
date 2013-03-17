package de.tucottbus.kt.lcars.speech.events;

import java.util.Properties;

import de.tucottbus.kt.lcars.speech.ISpeechEngine;

/**
 * A speech recognition event.
 * 
 * @author Matthias Wolff
 */
public class RecognitionEvent extends SpeechEvent
{
  /**
   * The recognition result.
   */
  public String result;

  /**
   * Recognition result accepted flag.
   */
  public boolean accepted;

  /**
   * The recognizer confidence, a value between 0 (most uncertain) through 1 (sure).
   */
  public float confidence;
  
  /**
   * A list of recognizer specific details on the recognition result.
   */
  public Properties details;
  
  /**
   * Creates a new speech recognition event.
   * 
   * @param spe
   *          The speech engine which caused this event.
   */
  public RecognitionEvent(ISpeechEngine spe)
  {
    super(spe,-1);
    details = new Properties();
  }
  
  /**
   * Returns the value of a recognition result detail.
   * 
   * @param key
   *          The key.
   * @param def
   *          The default value.
   * @return The value for key or <code>def</code> if the key was not found.
   *
   * @see #getDetailFloat(String, float)
   * @see #details
   */
  public String getDetail(String key, String def)
  {
    if (details==null) return def;
    return details.getProperty(key,def);
  }

  /**
   * Returns the floating point value of a recognition result detail.
   * 
   * @param key
   *          The key.
   * @param def
   *          The default value.
   * @return The value for key or <code>def</code> if the key was not found.
   *
   * @see #getDetail(String, String)
   * @see #details
   */
  public float getDetailFloat(String key, float def)
  {
    if (details==null) return def;
    String val = details.getProperty(key);
    if (val==null) return def;
    if ("nan".equals(val)) return Float.NaN;
    try
    {
      return Float.valueOf(val);
    }
    catch (Exception e)
    {
      e.printStackTrace();
      return def;
    }
  }
  
}

// EOF