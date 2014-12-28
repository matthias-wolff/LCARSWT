package de.tucottbus.kt.lcars.speech.events;

import java.util.Vector;

import de.tucottbus.kt.lcars.speech.ISpeechEngine;

/**
 * <p><i><b style="color:red">Experimental.</b></i></p>
 *
 * A recognition post-processing event. The event can contain a
 * {@linkplain #result refined recognition result} and {@linkplain #frames
 * per-frame details}.
 * 
 * @author Matthias Wolff
 */
public class PostprocEvent extends SpeechEvent
{
  /**
   * Per-frame information on the recognition result. A frame is the smallest
   * signal portion processed by a speech recognizer, typically 10 milliseconds.
   */
  public class Frame
  {
    /**
     * The phoneme label.
     */
    public String recPhn;
    
    /**
     * The reference phoneme label (<code>null</code> or empty if no reference
     * recognition was performed).
     */
    public String refPhn;
    
    /**
     * The output label.
     */
    public String recOut;
    
    /**
     * The logarithmic weight (sum of negative logarithmic likelihood of the
     * feature vector and the negative logarithmic transition weight) or, if a
     * reference recognition was performed, the difference between the
     * logarithmic weights of the recognition result and the reference
     * recognition result.
     */
    public float lsr;
    
    /**
     * The negative logarithmic likelihood of the feature vector or, if a
     * reference recognition was performed, the difference between the
     * negative logarithmic likelihoods of the recognition result and the
     * reference recognition result.
     */
    public float nll;
  }
  
  /**
   * The (possibly refined) recognition result. Can be <code>null</code> or
   * empty if no refinement was done by the post-processing. In this case
   * {@link RecognitionEvent#result} is the final result.
   */
  public String result;
  
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
