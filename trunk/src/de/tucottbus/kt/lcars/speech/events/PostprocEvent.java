package de.tucottbus.kt.lcars.speech.events;

import java.util.Vector;

import de.tucottbus.kt.lcars.speech.ISpeechEngine;

/**
 * A recognition post-processing event.
 * 
 * @author Matthias Wolff
 */
public class PostprocEvent extends SpeechEvent
{
  /**
   * Per-frame information on the recognition result.
   */
  public class Frame
  {
    public String recPhn;
    public String refPhn;
    public float  dlsr;
    public float  dnll;
  }
  
  /**
   * A vector containing per-frame information on the recognition result. 
   */
  public Vector<Frame> frames;
   
  /**
   * Creates a new recognition post-processing event.
   * 
   * @param spe
   *          The speech engine which caused this event.
   */  
  public PostprocEvent(ISpeechEngine spe)
  {
    super(spe,-1);
    frames = new Vector<Frame>();
  }
  
  /**
   * Adds information on one speech frame.
   * 
   * @param frame
   *          The frame information.
   */
  public void addFrame(Frame frame)
  {
    if (frame!=null)
      frames.add(frame);
  }
}

// EOF
