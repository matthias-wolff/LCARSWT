package de.tucottbus.kt.lcars.net;

import java.rmi.RemoteException;

import de.tucottbus.kt.lcars.Panel;

/**
 * Remote interface of {@linkplain RmiPanelAdapter RMI panel adapters}.
 * 
 * @author Matthias Wolff
 */
public interface IRmiPanelAdapterRemote extends IRmiAdapterRemote
{
  /**
   * Displays a new LCARS panel on this panel adapter. A previously displayed panel will be stopped
   * and discarded.
   * 
   * @param className
   *          The new {@link Panel}'s class name.
   * @throws ClassNotFoundException
   *           If <code>className</code> is invalid.
   */
  public void setPanel(String className)
  throws ClassNotFoundException, RemoteException;

  public int serialNo()
      throws RemoteException;
}

// EOF
