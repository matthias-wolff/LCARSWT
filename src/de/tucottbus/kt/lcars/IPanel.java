package de.tucottbus.kt.lcars;

import java.awt.event.KeyEvent;
import java.rmi.Remote;
import java.rmi.RemoteException;

import de.tucottbus.kt.lcars.elements.EEvent;

/**
 * The interface of an LCARS panel. A panel represents the contents and semantics of a graphical,
 * haptic, acoustic and speech user interface. Panels are physically displayed at {@link IScreen
 * LCARS screens}. Screen and panel do not necessarily run on the same virtual or physical machine.
 * 
 * @see IScreen
 */
public interface IPanel extends Remote
{
  /**
   * Called when the panel is started.
   */
  public void start() throws RemoteException;

  /**
   * Called when the panel is stopped.
   */
  public void stop() throws RemoteException;

  /**
   * Called by the {@linkplain Screen LCARS screen} displaying this panel when touch events occur.
   * The contract of this method is to translate touch events into {@link EEvent element events} and
   * to dispatch the latter.
   * 
   * @param event
   *          The event.
   */
  public void processTouchEvents(TouchEvent[] events) throws RemoteException;

  /**
   * Called by the {@linkplain Screen LCARS screen} displaying this panel when keyboard input events
   * occur. The contract of this method is to dispatch the event.
   * 
   * @param event
   *          The event.
   */
  public void processKeyEvent(KeyEvent event) throws RemoteException;

  // -- Questionable interfaces --
  
  /**
   * Displays a dialog allowing the user to select a panel.
   */
  public void panelSelectionDialog() throws RemoteException;
  
  /**
   * The unique serial number of the {@link IPanel} described by this
   * instance.
   */
  public int serialNo();
}

// EOF
