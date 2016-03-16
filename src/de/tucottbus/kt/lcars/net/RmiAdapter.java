package de.tucottbus.kt.lcars.net;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import de.tucottbus.kt.lcars.LCARS;
import de.tucottbus.kt.lcars.Panel;
import de.tucottbus.kt.lcars.Screen;
import de.tucottbus.kt.lcars.logging.Log;

/**
 * Subclasses keep a connection to a peer adapter on a remote machine. Instances of the
 * {@link RmiScreenAdapter} subclass connect to a {@link RmiPanelAdapter} instance an vice versa.
 * 
 * @see RmiScreenAdapter
 * @see RmiPanelAdapter
 * @author Matthias Wolff
 */
public abstract class RmiAdapter implements Remote
{
  /**
   * Copy of <code>this</code> for nested classes.
   */
  private RmiAdapter self;
  
  /**
   * The peer this adapter is connected to.
   */
  private Remote peer;
  
  /**
   * The name of the host serving the peer of this adapter.
   */
  private String peerHostName;
  
  /**
   * The connection daemon.
   */
  private ConnectionThread connection;

  // -- Constructors --
  
  /**
   * Creates a new RMI network adapter.
   *
   * @param peerHostName
   *          The name of the host serving the peer of this adapter.
   * @throws RemoteException
   *           If the RMI registry could not be contacted.
   * @throws MalformedURLException
   *           If <code>rmiSelfName</code> is not an appropriately formatted URL.
   */
  protected RmiAdapter(String peerHostName)
  throws RemoteException, MalformedURLException
  {
    super();
    this.self         = this;
    this.peerHostName = peerHostName;
    this.connection   = new ConnectionThread();
    this.connection.start();
  }
  
  // -- Feedback --
  
  /**
   * Returns true if it is connected
   * @return
   */
  public boolean isConnected()
  {
    return this.connection.run;
  }
  
  /**
   * Returns the last log message of peer/server
   * @return
   */
  public String getServerMsg()
  {
    return connection.serverMsg;
  }

  // -- Operations --
  
  /**
   * Prints a log message.
   * 
   * @param msg
   *          The message.
   */
  protected void log(String msg)
  {
    Log.info(getClass().getSimpleName()+"."+getPeerHostName()+": "+msg);
  }
  
  /**
   * Prints an error message.
   * 
   * @param msg
   *          The message.
   */
  protected void err(String msg)
  {
    Log.err(getClass().getSimpleName()+"."+getPeerHostName()+": "+msg);
  }

  /**
   * Shuts this RMI network adapter down.
   */
  public void shutDown()
  {
    if (connection!=null)
    {
      connection.end();
      try { 
        connection.join(1500);
      }
      catch (InterruptedException e)
      {
        // ignored
      }
      connection=null;
    }
  }

  // -- RMI names and URLs --

  /**
   * Returns the RMI name of an {@linkplain RmiScreenAdapter RMI screen adapter}.
   * 
   * <p><b>Note:</b> The current implementation supports only one remote screen per host!</p>
   * 
   * @param panelHostName
   *          The name of the host serving the {@link Panel}.
   * @param screenID
   *          Reserved for identifying multiple screens on a single host, must be <code>0</code>.
   */
  public static final String makeScreenAdapterRmiName
  (
    String panelHostName,
    int screenID
  )
  {
    return LCARS.getRmiName()+".ScreenAdapter."+panelHostName.toUpperCase();
  }
  
  /**
   * Returns the fully qualified RMI URL of an {@linkplain RmiScreenAdapter RMI screen adapter}.
   * 
   * <p><b>Note:</b> The current implementation supports only one remote screen per host!</p>
   * 
   * @param panelHostName
   *          The name of the host serving the {@link Panel}.
   * @param screenHostName
   *          The name of the host serving the {@link Screen}.
   * @param screenID
   *          Reserved for identifying multiple screens on a single host, must be <code>0</code>.
   */
  public static final String makeScreenAdapterUrl
  (
    String panelHostName,
    String screenHostName,
    int screenID
  )
  {
    return "//"+screenHostName+":"+LCARS.getRmiPort()+
           "/" + makeScreenAdapterRmiName(panelHostName,screenID);
  }

  /**
   * Returns the RMI name of an {@linkplain RmiPanelAdapter RMI panel adapter}.
   * 
   * <p><b>Note:</b> The current implementation supports only one remote screen per host!</p>
   * 
   * @param screenHostName
   *          The name of the host serving the {@link Screen}.
   * @param screenID
   *          Reserved for identifying multiple screens on a single host, must be <code>0</code>.
   */
  public static final String makePanelAdapterRmiName
  (
    String screenHostName,
    int screenID
  )
  {
    return LCARS.getRmiName()+".PanelAdapter."+screenHostName.toUpperCase();
  }

  /**
   * Returns the fully qualified RMI URL of an {@linkplain RmiPanelAdapter RMI panel adapter}.
   * 
   * <p><b>Note:</b> The current implementation supports only one remote screen per host!</p>
   * 
   * @param panelHostName
   *          The name of the host serving the {@link Panel}.
   * @param screenHostName
   *          The name of the host serving the {@link Screen}.
   * @param screenID
   *          Reserved for identifying multiple screens on a single host, must be <code>0</code>.
   */
  public static final String makePanelAdapterUrl
  (
    String panelHostName,
    String screenHostName,
    int screenID
  )
  {
    return "//"+panelHostName+":"+LCARS.getRmiPort()+
           "/" +makePanelAdapterRmiName(screenHostName,screenID);
  }
  
  // -- Getters and setters --
  
  /**
   * Returns the name of the host serving the peer of this adapter.
   */
  protected final String getPeerHostName()
  {
    return peerHostName;
  }

  /**
   * Returns the peer this adapter is connected to. The return value is <code>null</code> if there
   * is no network connection.
   */
  protected final Remote getPeer()
  {
    return peer;
  }
  
  // -- Abstract methods --

  /**
   * Called when the peer has changed (including the case that there is no peer anymore). The
   * current peer can be retrieved using {@link #getPeer()}.
   */
  protected abstract void updatePeer();
  
  /**
   * Returns the RMI name this adapter is bound to.
   */
  public abstract String getRmiName();

  /**
   * Returns the RMI URL of this adapter.
   */
  public abstract String getRmiUrl();
  
  /**
   * Returns the RMI URL of the peer adapter this adapter is connected to.
   */
  public abstract String getRmiPeerUrl();

  // -- Nested classes --
  
  /**
   * Network connection thread.
   */
  private class ConnectionThread extends Thread
  {
    private boolean run       = false;
    private String  serverMsg = null;
    private String  peerMsg   = null;

    protected ConnectionThread()
    {
      super();
      setDaemon(true);
    }
    
    @Override
    public void run()
    {
      String serverURL = "//"+getPeerHostName()+":"+LCARS.getRmiPort()+"/"+LCARS.getRmiName();      
      
      // Start connection
      log("Starting");
      log("- self: "+getRmiName());
      log("- peer: "+getRmiPeerUrl());
      try
      {
        log("Exporting remote stub ...");
        Remote stub = UnicastRemoteObject.exportObject(self,0);
        log("... Exported");
        log("Binding to \""+getRmiName()+"\" ...");
        Naming.rebind(getRmiName(),stub);
        log("... Bound");
      }
      catch (Exception e)
      {
        log("... FAILED ("+e.toString()+")");
        err("FATAL ERROR: Screen adapter not started.");
        return;
      }      
      log("Running");
      
      // Maintain connection ...
      run = true;
      while (run)
      {
        // Wait
        try { Thread.sleep(1000); } catch (InterruptedException e){}        
        if (!run) break;
        
        // - ... to LCARS server
        //       HACK: It's not best practice to implement a derived classes' function in the base class!
        if (self instanceof RmiScreenAdapter)
        {
          String msg = "";
          try
          {
            ILcarsRemote server = (ILcarsRemote)Naming.lookup(serverURL);
            server.serveRmiPanelAdapter(LCARS.getHostName(),0,LCARS.getArg("--panel="));
            msg = "Connection to server \""+serverURL+"\" established";
          }
          catch (MalformedURLException e)
          {
            msg = "\""+serverURL+"\" is not a valid URL";
            peer = null;
          }
          catch (RemoteException e)
          {
            e.printStackTrace();
            msg = "No connection to server \""+serverURL+"\"\nreason: "+e.getMessage();
            peer = null;
          }
          catch (NotBoundException e)
          {
            msg = "Server is down";
            peer = null;
          }
          if (!msg.equals(serverMsg))
          {
            log(msg);
            serverMsg = msg;
          }
        }
        
        // - ... to peer
        String msg = "";
        try
        {
          if (peer==null)
          {
            peer = Naming.lookup(getRmiPeerUrl());
            msg = "Connection to peer established";
          }
          if (peer!=null)
          {
            ((IRmiAdapterRemote)peer).ping();
            msg = "Connection to peer ok";
          }
        }
        catch (RemoteException e)
        {
          msg = peer!=null ? "Connection to peer broke down" : "Peer not found";
          peer = null;
        }
        catch (MalformedURLException e)
        {
          msg = "\""+getRmiPeerUrl()+"\" is not a valid URL";
          peer = null;
        }
        catch (NotBoundException e)
        {
          msg = "Peer is down";
          peer = null;
        }
        updatePeer();
        if (!msg.equals(peerMsg))
        {
          log(msg);
          peerMsg = msg;
        }
      }
      
      // End connection
      log("Terminating ...");
      try
      {
        log("Unbinding from \""+getRmiName()+"\" ...");        
        Naming.unbind(getRmiName());
        UnicastRemoteObject.unexportObject(self,true);
        log("... Unbound");
      }
      catch (Exception e)
      {
        log("... FAILED ("+e.toString()+")");
      }
      if (self instanceof RmiScreenAdapter)
      {
        try
        {
          log("Disconnecting from server ...");        
          ILcarsRemote server = (ILcarsRemote)Naming.lookup(serverURL);
          server.destroyRmiPanelAdapter(LCARS.getHostName(),0);
          log("... Disconnected");
        }
        catch (Exception e)
        {
          log("... FAILED ("+e.toString()+")");
        }
      }
      log("... Terminated");
    }
    
    public void end()
    {
      run = false;
      interrupt();
    }    
  }
  
}

// EOF

