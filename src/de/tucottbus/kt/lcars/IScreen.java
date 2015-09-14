package de.tucottbus.kt.lcars;

import java.awt.geom.Area;
import java.rmi.Remote;
import java.rmi.RemoteException;

import de.tucottbus.kt.lcars.feedback.UserFeedback;
import de.tucottbus.kt.lcars.util.LoadStatistics;

/**
 * The interface of an LCARS screen. A screen is the representation of a physical unit with a
 * display, a touch panel and/or mouse and keyboard, and a loudspeaker. A screen provides a
 * graphical, haptic and acoustic user interface for an {@linkplain IPanel LCARS panel}. Screen and
 * panel do not necessarily run on the same virtual or physical machine.
 *
 * @see IPanel
 */
public interface IScreen extends Remote
{

  /**
   * Returns the physical geometry of the screen (in pixels).
   */
  public Area getArea() throws RemoteException;
  
  /**
   * Sets the physical geometry of the screen (in pixels).
   */
  public void setArea(Area area) throws RemoteException;
  
  /**
   * Returns name of the host displaying the screen.
   */
  public String getHostName() throws RemoteException;
  
  /**
   * Displays a new LCARS panel on this screen. A previously displayed panel will be stopped and
   * discarded.
   * 
   * @param className
   *          The new {@link Panel}'s class name.
   * @throws ClassNotFoundException
   *           If <code>className</code> is invalid.
   */
  public void setPanel(String className)
  throws ClassNotFoundException, RemoteException;  

  /**
   * Returns an interface to the {@linkplain IPanel LCARS panel} currently running on this screen.
   */
  public IPanel getPanel() throws RemoteException;
  
  /**
   * Called to inform the screen that the panel content has changed and the graphical representation
   * needs to be updated.
   *
   * @param data
   *          The data transferred from the {@linkplain Panel LCARS panel} to the screen.
   * @param incremental
   *          Update contains differences only.
   */
  public void update(PanelData data, boolean incremental)
  throws RemoteException;

  /**
   * Performs an audio-visual user feedback.
   * 
   * @param type
   *          The {@linkplain de.tucottbus.kt.lcars.feedback.UserFeedback.Type user feedback type}.
   */
  public void userFeedback(UserFeedback.Type type) throws RemoteException;

  /**
   * Returns the computational load statistics of the screen. The load depends on the number and
   * complexity of the GUI elements.
   * <ul>
   * <li>Call {@link LoadStatistics#getLoad()} on the return value to determine the average
   * percentage of 40 milliseconds consumed by a screen repaint. A return value of more than 100 %
   * indicates possible display juddering and the consumption of the entire capacity of one
   * processor core if 25 frames per second are painted.</li>
   * <li>Call {@link LoadStatistics#getEventsPerPeriod()} on the return value to determine the
   * actual frame rate in the last complete second.</li>
   * </ul>
   */
  public LoadStatistics getLoadStatistics() throws RemoteException;

  /**
   * Terminates the LCARS session displaying the screen.
   */
  public void exit() throws RemoteException;
}

// EOF
