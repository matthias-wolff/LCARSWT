package de.tucottbus.kt.lcars.net;

import java.rmi.Naming;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;

import de.tucottbus.kt.lcars.logging.Log;

/**
 * Incubating - The LCARS panel server.
 * 
 * @author Matthias Wolff, BTU Cottbus - Senftenberg
 */
public class LcarsServer implements IRmiLcarsServerRemote
{
  // -- Fields --
  
  /**
   * List of panel adapters served.
   */
  protected HashMap<String,RmiPanelAdapter> rmiPanelAdapters;
  
  // -- Singleton implementation and constructors --
  
  /**
   * The LCARS panel server singleton.
   */
  private static LcarsServer singleton;

  /**
   * Returns the LCARS panel server singleton. If the singleton instance does not yet 
   * exist, it will be created. 
   * 
   * @return The LCARS panel server singleton.
   */
  public static LcarsServer getInstance()
  {
    if (singleton==null)
      singleton = new LcarsServer();
    return singleton;
  }
  
  /**
   * Creates the LCARS panel server singleton. 
   */
  protected LcarsServer()
  {
    rmiPanelAdapters = new HashMap<String,RmiPanelAdapter>();
  }

  // -- Static API --
  
  /**
   * Starts the LCARS panel server and creates the server singleton if necessary. If 
   * starting the server failed, the program will exit.
   */
  public static void start()
  {
    try
    {
      Log.info("Starting LCARS panel server @"+NetUtils.getHostName()+".");
      getInstance();
      NetUtils.getRmiRegistry();
      Remote stub = UnicastRemoteObject.exportObject(singleton,0);
      Naming.rebind(NetUtils.getRmiName(),stub);
    } catch (Exception e)
    {
      Log.err("FATAL ERROR: RMI setup failed.", e);
      System.exit(-1);
    }
  }
  
  /**
   * Shuts down the LCARS panel server. If there is not panel server running, the
   * method does nothing. The method destroy the server singleton.
   */
  public static void shutDown()
  {
    if (singleton==null)
      return;

    Log.info("Shutting down LCARS panel server.");

    // Shutdown remote panels being served
    for (RmiPanelAdapter rpa : singleton.rmiPanelAdapters.values())
      rpa.shutDown();
    singleton.rmiPanelAdapters.clear();

    // Terminate RMI
    try
    {
      Naming.unbind(NetUtils.getRmiName());
    } catch (Exception e)
    {
      Log.err("RMI unbinding failed.", e);
    }
    try
    {
      UnicastRemoteObject.unexportObject(singleton,true);
    } catch (Exception e)
    {
      Log.err("RMI unexporting failed.", e);
    }
    
    // Destroy singleton
    singleton = null;
  }

  /**
   * Returns the list of {@linkplain RmiPanelAdapter panel adapters} served by
   * this LCARS panel server.
   * 
   * @return The list of panel adapters currently served or <code>null</code> if
   *         no LCARS panel server is runnning.
   */
  public static HashMap<String,RmiPanelAdapter> getPanelAdapters()
  {
    if (singleton!=null)
      return singleton.rmiPanelAdapters;
    else
      return null;
  }
  
  // -- Implementation  of the ILcarsRemote interface --
  
  @Override
  public boolean serveRmiPanelAdapter
  (
    String screenHostName, 
    int    screenID,
    String panelClassName
  ) throws RemoteException
  {
    if (screenHostName==null) return false;
    
    if (rmiPanelAdapters.containsKey(screenHostName+"."+screenID))
      return true;
    
    try
    {
      String screenUrl = RmiAdapter.makeScreenAdapterUrl(NetUtils.getHostName(),screenHostName,0);
      Log.info("LCARS.server: Connection request from "+screenUrl);
      RmiPanelAdapter rpa = new RmiPanelAdapter(panelClassName,screenHostName);
      rmiPanelAdapters.put(screenHostName+"."+screenID,rpa);
      return true;
    }
    catch (Exception e)
    {
      e.printStackTrace();
      return false;
    }
  }

  @Override
  public void destroyRmiPanelAdapter(String screenHostName, int screenID)
  throws RemoteException
  {
    String screenUrl = RmiAdapter.makeScreenAdapterUrl(NetUtils.getHostName(),screenHostName,0);
    String key = screenHostName+"."+screenID;
    Log.info("LCARS.server: Disconnection request from "+screenUrl);
    
    RmiPanelAdapter rpa = rmiPanelAdapters.remove(key);
    if (rpa!=null) rpa.shutDown();
  }

}

// EOF