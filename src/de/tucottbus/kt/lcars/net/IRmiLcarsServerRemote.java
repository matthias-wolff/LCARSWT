package de.tucottbus.kt.lcars.net;

import java.rmi.Remote;
import java.rmi.RemoteException;

import de.tucottbus.kt.lcars.Screen;

/**
 * The LCARS panel server remote interface.
 * 
 * @author Matthias Wolff
 */
public interface IRmiLcarsServerRemote extends Remote
{
  /**
   * Assures that a remote panel adapter (and the respective panel) for the specified screen is
   * served by the local machine. If the panel adapter is already existing the method does nothing.
   * If the panel adapter is not existing the method creates it. If this instance was not started in
   * server mode (command line option <code>--server</code>) the method does nothing.
   * 
   * <p>
   * <b>Note:</b> The current implementation supports only one remote screen per host!
   * </p>
   * 
   * @param screenHostName
   *          The name of the remote {@linkplain Screen LCARS screen} requesting the panel adapter.
   *          If <code>null</code> the method does nothing.
   * @param screenID
   *          Reserved for identifying multiple screens on a single host, must be <code>0</code>.
   * @param panelClassName
   *          -- Experimental -- If a new panel adapter is created, this panel is initially
   *          displayed (can be <code>null</code>).
   * @return <code>true</code> if the specified panel adapter is being served, <code>false</code>
   *         otherwise.
   */
  public boolean serveRmiPanelAdapter(String screenHostName, int screenID, String panelClassName)
  throws RemoteException;

  /**
   * Destroys the remote panel adapter (and the respective panel) of the specified screen on the
   * local machine. If no such adapter exists, the method does nothing. If this instance was not
   * started in server mode (command line option <code>--server</code>) the method does nothing.
   * 
   * <p>
   * <b>Note:</b> The current implementation supports only one remote screen per host!
   * </p>
   * 
   * @param screenHostName
   *          The name of the remote {@linkplain Screen LCARS screen} requesting the panel adapter.
   *          If <code>null</code> the method does nothing.
   * @param screenID
   *          Reserved for identifying multiple screens on a single host, must be <code>0</code>.
   */
  public void destroyRmiPanelAdapter(String screenHostName, int screenID)
  throws RemoteException;

}

// EOF

