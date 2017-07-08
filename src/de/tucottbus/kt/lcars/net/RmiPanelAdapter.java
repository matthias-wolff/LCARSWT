package de.tucottbus.kt.lcars.net;

import java.awt.event.KeyEvent;
import java.net.MalformedURLException;
import java.rmi.RemoteException;

import de.tucottbus.kt.lcars.IPanel;
import de.tucottbus.kt.lcars.IScreen;
import de.tucottbus.kt.lcars.LCARS;
import de.tucottbus.kt.lcars.Panel;
import de.tucottbus.kt.lcars.Screen;
import de.tucottbus.kt.lcars.TouchEvent;
import de.tucottbus.kt.lcars.logging.Log;
import de.tucottbus.kt.lcars.util.LoadStatistics;

/**
 * Network adapter class wrapping an {@linkplain Panel LCARS panel} for remote service.
 *
 * @author Matthias Wolff
 */
public class RmiPanelAdapter
extends RmiAdapter
implements IPanel, IRmiPanelAdapterRemote
{
  /**
   * The wrapped panel.
   */
  protected Panel panel;

  // -- Constructors --
  
  /**
   * Creates a new RMI network adapter for a remote {@linkplain Screen LCARS screen}.
   * 
   * @param className
   *          The class name of the panel to wrap by this adapter.
   * @param screenHostName
   *          The name of the host serving the peer {@linkplain IPanel LCARS panel} to connect this
   *          adapter to.
   * @throws RemoteException
   *           If the RMI registry could not be contacted.
   * @throws MalformedURLException
   *           If <code>rmiSelfName</code> is not an appropriately formatted URL.
   */
  public RmiPanelAdapter(String className, String screenHostName)
  throws RemoteException, MalformedURLException, ClassNotFoundException
  {
    super(screenHostName);
    setPanel(className);
  }

  // -- Getters and setters --
  
  /**
   * Returns the class  of the {@linkplain Panel LCARS panel} wrapped by this adapter.
   */
  public Class<?> getPanelClass()
  {
    if (panel==null) return null;
    return panel.getClass();
  }
  
  /**
   * Returns a human readable name of the {@linkplain Panel LCARS panel} wrapped by this adapter.
   */
  public String getPanelTitle()
  {
    if (panel==null) return null;
    if (panel.getTitle()!=null) return panel.getTitle();
    return panel.getClass().getSimpleName();
  }

  /**
   * Returns the (server side) load statistics of the {@linkplain Panel LCARS panel} wrapped by this
   * adapter.
   */
  public LoadStatistics getLoadStatistics()
  {
    if (panel==null) return null;
    return panel.getLoadStatistics();
  }
  
  // -- Implementation of abstract methods --
  
  @Override
  protected void updatePeer()
  {
    try
    {
      panel.setScreen((IScreen)getPeer());
    } catch (NullPointerException e)
    {
      Log.err("Cannot set screen.", e); 
    }
  }


  @Override
  public String getRmiName()
  {
    return makePanelAdapterRmiName(getPeerHostName(),0);
  }

  @Override
  public String getRmiUrl()
  {
    return makePanelAdapterUrl(LCARS.getHostName(),getPeerHostName(),0);
  }
  
  @Override
  public String getRmiPeerUrl()
  {
    return makeScreenAdapterUrl(LCARS.getHostName(),getPeerHostName(),0);
  }

  // -- Implementation of the IRmiPanelAdapteRemote interface --
  
  @Override
  public void setPanel(String className) throws ClassNotFoundException
  {
    Log.info("Setting panel "+className+" ...");
    panel = Panel.createPanel(className,(IScreen)getPeer());
    if (panel==null)
    {
      IScreen src = (IScreen)getPeer();
      panel = Panel.createPanel(null,src);
      try
      {
        src.setPanelId(panel.serialNo);
        String s = "cannot be created on a remote screen.";
        panel.messageBox("ERROR",className+"\n"+s.toUpperCase(),"OK",null,null);
      } catch (RemoteException e)
      {
        Log.err("... Panel set failed",e);
        return;
      }
    }
    Log.info("... Panel set");
  }

  @Override
  public void ping()
  {
    // Called just to see if panel adapter is still connected. RMI will throw
    // a RemoteException if not.
  }
  
  // -- Panel wrapper methods / Implementation of the IPanel interface --

  @Override
  public void start()
  {
    panel.start();
  }

  @Override
  public void stop()
  {
    panel.stop();
  }

  @Override
  public void processTouchEvents(TouchEvent[] events)
  {
    panel.processTouchEvents(events);
  }

  @Override
  public void processKeyEvent(KeyEvent event)
  {
    panel.processKeyEvent(event);
  }

  @Override
  public void panelSelectionDialog()
  {
    panel.panelSelectionDialog();
  }

  @Override
  public int serialNo()
  {
    return (panel != null) ? panel.serialNo : -1;
  }

  @Override
  public String getElementInfo(int serialNo)
      throws RemoteException
  {
    return panel.getElementInfo(serialNo);
  }
  
  
}

// EOF

