package de.tucottbus.kt.lcars.net;

import java.rmi.RemoteException;

/**
 * Remote interface of {@linkplain RmiScreenAdapter RMI screen adapters}.
 * 
 * @author Matthias Wolff
 */
public interface IRmiScreenAdapterRemote extends IRmiAdapterRemote
{
  /**
   * The memory statistics of the screen update data.
   */
  public int getMemStat() throws RemoteException;
}

// EOF

