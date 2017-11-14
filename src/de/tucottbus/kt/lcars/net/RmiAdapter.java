package de.tucottbus.kt.lcars.net;

import java.net.MalformedURLException;
import java.rmi.ConnectException;
import java.rmi.ConnectIOException;
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
  private IRmiAdapterRemote peer;
  
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
   * Shuts this RMI network adapter down.
   */
  public void shutDown()
  {
    if (connection!=null)
    {
      connection.end();
      try
      { 
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
    return NetUtils.getRmiName()+".ScreenAdapter."+panelHostName.toUpperCase();
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
    return "//"+screenHostName+":"+NetUtils.getRmiPort()+
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
    return NetUtils.getRmiName()+".PanelAdapter."+screenHostName.toUpperCase();
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
    return "//"+panelHostName+":"+NetUtils.getRmiPort()+
           "/" +makePanelAdapterRmiName(screenHostName,screenID);
  }
  
  // -- Getters and setters --
  
  /**
   * Returns the name of the host serving the peer of this adapter.
   */
  public final String getPeerHostName()
  {
    return peerHostName;
  }

  /**
   * Returns the peer this adapter is connected to. The return value is <code>null</code> if there
   * is no network connection.
   */
  public final IRmiAdapterRemote getPeer()
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
      run = true;
      Log.info("Running RMI connection");
      while (run)
      {
      
      String serverURL = "//"+getPeerHostName()+":"+NetUtils.getRmiPort()+"/"+NetUtils.getRmiName();      
      
      // Start connection
      Log.info("Starting");
      Log.info("- self: "+getRmiName());
      Log.info("- peer: "+getRmiPeerUrl());
      try
      {
        Log.info("Exporting remote stub ...");
        Remote stub = UnicastRemoteObject.exportObject(self,0);
        Log.info("... Exported");
        Log.info("Binding to \""+getRmiName()+"\" ...");
        Naming.rebind(getRmiName(),stub);
        Log.info("... Bound");
      }
      catch (Exception e)
      {
        Log.info("... FAILED ("+e.toString()+")");
        //Log.err("FATAL ERROR: Screen adapter not started.");
        //return;
      }      
      Log.info("Running");
      peerMsg = null;
      
      // Maintain connection ...
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
            IRmiLcarsServerRemote server = (IRmiLcarsServerRemote)Naming.lookup(serverURL);
            server.serveRmiPanelAdapter(NetUtils.getHostName(),0,LCARS.getArg("--panel="));
            msg = "Connection to \""+serverURL+"\" established";
          }
          catch (MalformedURLException e)
          {
            msg = "\""+serverURL+"\" is not a valid URL";
            peer = null;
          }
					catch (ConnectException|ConnectIOException e)
					{
            msg = "No connection to \""+serverURL+"\"\nreason: "+e.getMessage();
            peer = null;
					}
          catch (RemoteException e)
          {
            e.printStackTrace();
            msg = "No connection to \""+serverURL+"\"\nreason: "+e.getMessage();
            peer = null;
          }
          catch (NotBoundException e)
          {
            msg = "Server is down";
            peer = null;
          }
          if (!msg.equals(serverMsg))
          {
            Log.info(msg);
            serverMsg = msg;
          }
        }
        
        // - ... to peer
        String msg = "";
        try
        {
          if (peer==null)
          {
            peer = (IRmiAdapterRemote)Naming.lookup(getRmiPeerUrl());
            msg = "Connection to peer established";
          }
          if (peer!=null)
          {
            peer.ping();
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
          msg = "Peer is down: " + e.getMessage();
          peer = null;
        }
        updatePeer();
        if (!msg.equals(peerMsg))
        {
          Log.info(msg);
          peerMsg = msg;
        }
        
        if (peer==null) break;
      }
      
      // End connection
      Log.info("Terminating ...");
      try
      {
        Log.info("Unbinding from \""+getRmiName()+"\" ...");        
        Naming.unbind(getRmiName());
        Log.info("... Unbound");
      }
      catch (Exception e)
      {
        Log.info("... FAILED ("+e.toString()+")");
      }
      try
      {
        Log.info("Unexporting ...");        
        UnicastRemoteObject.unexportObject(self,true);
        Log.info("... Unexported");
      }
      catch (Exception e)
      {
        Log.info("... FAILED ("+e.toString()+")");
      }
      if (self instanceof RmiScreenAdapter)
      {
        try
        {
          Log.info("Disconnecting from server ...");        
          IRmiLcarsServerRemote server = (IRmiLcarsServerRemote)Naming.lookup(serverURL);
          server.destroyRmiPanelAdapter(NetUtils.getHostName(),0);
          Log.info("... Disconnected");
        }
        catch (Exception e)
        {
          Log.info("... FAILED ("+e.toString()+")");
        }
      }
      Log.info("... Terminated");
    }
      Log.info("End of RMI connection");
    }
    
    public void end()
    {
      run = false;
      interrupt();
    }    
  }
  
}

// EOF

