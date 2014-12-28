package de.tucottbus.kt.lcars.net;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Common remote interface of {@linkplain RmiAdapter RMI adapters}.
 * 
 * @author Matthias Wolff
 */
public interface IRmiAdapterRemote extends Remote
{  
  /**
   * Network keep-alive method.
   */
  public void ping() throws RemoteException;
}

// EOF
