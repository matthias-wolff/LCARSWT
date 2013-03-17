package de.tucottbus.kt.lcars.speech;

import java.util.Properties;

import de.tucottbus.kt.lcars.feedback.UserFeedbackPlayer;

/**
 * Interface of lightweight speech recognition and synthesis engines. This interface may be
 * replaced by the Java Speech API.
 * 
 * @author Matthias Wolff
 */
public interface ISpeechEngine
{
  /**
   * Speech engine is started.
   */
  public static final int STARTED = 0x0001;

  /**
   * Speech engine is listening.
   */
  public static final int LISTENING = 0x0002;
 
  /**
   * Starts the speech engine.
   */
  public void start();

  /**
   * Stops the speech engine.
   */
  public void stop();
  
  /**
   * Determines if the speech engine is started.
   */
  public boolean isStarted();

  /**
   * Determines if the speech engine is currently busy.
   */
  public boolean isBusy();

  /**
   * Determines if the speech engine has terminally failed. If the method returns <code>true</code>,
   * speech input and output is nonfunctional and {@link ISpeechEngine#start()} will not fix the
   * problem. The previous standard and/or error output should contain information on what went wrong.
   */
  public boolean hasFailed();
  
  /**
   * Sets the listening mode of the speech recognizer.
   * 
   * @param mode
   *          The listening mode: &lt;0 to put the recognizer in the off-line
   *          state, 0 to put the recognizer in the sleeping state (i.e. waiting
   *          for voice activation), and &gt;0 to put the recognizer in the
   *          active state. Implementations may interpret positive values as
   *          dialog state indices and switch the grammar and vocabulary
   *          accordingly.
   * @see #getListenMode()
   */
  public void setListenMode(int mode);

  /**
   * Returns the listening mode of the speech recognizer.
   * 
   * @return The listening mode: &lt;0 if the recognizer is off-line, 0 if the
   *         recognizer is sleeping (i.e. waiting for voice-activation), and
   *         &gt;0 if the recognizer is active (i.e. waiting for speech input).
   *         Implementations may, but do not have to, return the (positive)
   *         dialog state index in the last case.
   * @see #setListenMode(int)
   */
  public int getListenMode();

  /**
   * Activates a new configuration.
   * 
   * @param config
   *          The new configuration.
   */
  public void setConfiguration(Properties config);
  
  /**
   * Returns the current voice activity state.
   */
  public boolean getVoiceActivity();
  
  /**
   * Adds an {@link ISpeechEventListener} to this speech engine.
   * 
   * @param listener the listener
   */
  public void addSpeechEventListener(ISpeechEventListener listener);
  
  /**
   * Removes a {@link ISpeechEventListener} from this speech engine.
   * 
   * @param listener the listener
   */
  public void removeSpeechEventListener(ISpeechEventListener listener);

  /**
   * Adds a player to give audio-visual user feedback on speech engine events and state changes.
   * 
   * @param player
   *          The player.
   * @see #removeUserFeedbackPlayer(UserFeedbackPlayer)
   */
  public void addUserFeedbackPlayer(UserFeedbackPlayer player);

  /**
   * Removes a user feedback player.
   * 
   * @param player
   *          The player.
   * @see #addUserFeedbackPlayer(UserFeedbackPlayer)
   */
  public void removeUserFeedbackPlayer(UserFeedbackPlayer player);

  /**
   * Sets the user feedback mode of this speech engine.
   * 
   * @param mode
   *          The new mode (see {@link UserFeedbackPlayer#setMode(int)} for
   *          possible values). 
   * @see #getUserFeedbackMode()
   */
  public void setUserFeedbackMode(int mode);

  /**
   * Returns the current user feedback mode of this speech engine.
   * 
   * @return The mode (see {@link UserFeedbackPlayer#setMode(int)} for possible
   *         values).
   * @see #setUserFeedbackMode()
   */
  public int getUserFeedbackMode();
}
