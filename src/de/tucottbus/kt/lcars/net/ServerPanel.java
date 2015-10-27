package de.tucottbus.kt.lcars.net;

import java.awt.Dimension;
import java.lang.ref.WeakReference;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import de.tucottbus.kt.lcars.IScreen;
import de.tucottbus.kt.lcars.LCARS;
import de.tucottbus.kt.lcars.Panel;
import de.tucottbus.kt.lcars.contributors.EElementArray;
import de.tucottbus.kt.lcars.elements.EElbo;
import de.tucottbus.kt.lcars.elements.EElement;
import de.tucottbus.kt.lcars.elements.EEvent;
import de.tucottbus.kt.lcars.elements.EEventListenerAdapter;
import de.tucottbus.kt.lcars.elements.ELabel;
import de.tucottbus.kt.lcars.elements.ERect;
import de.tucottbus.kt.lcars.elements.EValue;
import de.tucottbus.kt.lcars.logging.Log;
import de.tucottbus.kt.lcars.swt.ColorMeta;
import de.tucottbus.kt.lcars.util.LoadStatistics;

/**
 * The LCARS server panel.
 * 
 * @author Matthias Wolff
 */
public class ServerPanel extends Panel
{
  // -- Static fields --
  
  /**
   * The static list of all server panels instantiated on this JVM. Used for dispatching the server
   * log (see {@link #logMsg(String, String, boolean)}).
   */
  private static ArrayList<WeakReference<ServerPanel>> instances;
  
  // -- GUI element fields --
  private EElementArray eLog;
  private EElementArray eScreens;
  private EElbo         eLogSize;
  private EElbo         eScrCnt;
  private EElbo         eSrvLd;
  private EElbo         eCscLd;
  private ERect         eLogLock;
  private ERect         eLogPrev;
  private ERect         eLogNext;
  private ERect         eConfirm;
  private ERect         eScrLock;
  private ERect         eScrPrev;
  private ERect         eScrNext;
  private ERect         eCscShutdown;
  private EValue        eCsc;
  private ELabel        eCscPnlUrl;
  private ELabel        eCscScrUrl;
  private ELabel        eCscSize;
  private ERect         eCscUpdate;
  private ERect         eCscStart;
  private ERect         eCscStop;
  private ERect         eCscPnlSel;
  private final ColorMeta   cRed = new ColorMeta(0x00FF0066,false);
  
  // -- Constructors --
  
  /**
   * Creates a new LCARS server panel.
   * 
   * @param iscreen
   *          The screen to display the panel on. 
   */
  public ServerPanel(IScreen iscreen)
  {
    super(iscreen);
    if (instances==null) instances = new ArrayList<WeakReference<ServerPanel>>();
    instances.add(new WeakReference<ServerPanel>(this));
  }

  // -- Overrides --
  
  @Override
  public void init()
  {
    super.init();
    
    setTitleLabel(new ELabel(this,-100,-100,0,0,LCARS.ES_STATIC,"LCARS SERVER"));
    setColorScheme(LCARS.CS_SECONDARY);
    
    int    ce = LCARS.EC_ELBOUP;
    int    cp = LCARS.EC_PRIMARY;
    int    c1 = LCARS.EC_SECONDARY|LCARS.ES_SELECTED;
    int    c2 = LCARS.EC_SECONDARY;
    ColorMeta  cSpecial = new ColorMeta(0xFF95848C,true);
    ERect  eRect;
    EElbo  eElbo;
    EValue eValue;
    
    // Server elbo
    add(new ERect(this,23,23,76,76,ce|LCARS.ES_STATIC|LCARS.ES_RECT_RND_W,null));
    
    eValue = new EValue(this,110,23,445,76,c1|LCARS.ES_STATIC,"");
    eValue.setValue("LCARS PANEL SERVER");
    eValue.setValueMargin(0);
    add(eValue);

    eLogSize = new EElbo(this,570,23,560,146,ce|LCARS.ES_STATIC|LCARS.ES_SHAPE_NE|LCARS.ES_LABEL_SW,"000");
    eLogSize.setArmWidths(192,76); eLogSize.setArcWidths(238,98);
    add(eLogSize);

    eLogPrev = new ERect(this,938,174,192,96,c2|LCARS.ES_LABEL_NW,"PREV");
    add(eLogPrev);

    eLogNext = new ERect(this,938,275,192,96,c2|LCARS.ES_LABEL_NW,"NEXT");
    add(eLogNext);
    
    eRect = new ERect(this,938,376,192,221,cp|LCARS.ES_LABEL_NW,"LCARS");
    eRect.addEEventListener(new EEventListenerAdapter()
    {
      @Override
      public void touchDown(EEvent ee)
      {
        panelSelectionDialog();
      }
    });
    add(eRect);
    
    eLogLock = new ERect(this,938,602,192,96,c1|LCARS.ES_LABEL_NW,"FOLLOW");
    add(eLogLock);
    
    String s = String.format("%d/%s",LCARS.getRmiPort(),LCARS.getRmiName());
    eElbo = new EElbo(this,938,703,230,354,ce|LCARS.ES_STATIC|LCARS.ES_SHAPE_SW|LCARS.ES_LABEL_NW,s);
    eElbo.setArmWidths(192,68); eElbo.setArcWidths(238,98);
    add(eElbo);
    
    eValue = new EValue(this,1167,989,720,68,ce|LCARS.ES_STATIC|LCARS.ES_RECT_RND_E|LCARS.ES_VALUE_W,null);
    eValue.setValue(LCARS.getHostName().toUpperCase());
    eValue.setValueMargin(68);
    add(eValue); 

    eLog = new EElementArray(61,104,ELabel.class,new Dimension(839,35),25,1,LCARS.ES_STATIC|LCARS.ES_LABEL_W,null)
    {
      @Override
      public synchronized EElement add(String name)
      {
        if (name.length()>90) name = name.substring(0,90)+"...";
        EElement e = super.add(name);
        while (getItemCount()>226) remove(0);
        showItemsInt(((int)getItemCount()/25)*25,25);
        hiliteItem(getItemCount()-1,2000);
        return e;
      }

      @Override
      public void animate()
      {
        showItemsInt(getFirstVisibleItemIndex(),25);
      }
    };
    eLog.setLock(true);
    eLog.setPageControls(eLogPrev,eLogNext);
    eLog.addToPanel(this);
    
    // Screens elbo
    add(new ERect(this,1831,23,68,68,ce|LCARS.ES_STATIC|LCARS.ES_RECT_RND_E,null));

    eConfirm = new ERect(this,1626,23,192,68,LCARS.ES_LABEL_SE|LCARS.ES_STATIC,"CONFIRM");
    eConfirm.addEEventListener(new EEventListenerAdapter()
    {
      @Override
      public void touchDown(EEvent ee)
      {
        EElement e = (EElement)eConfirm.getData();
        if (e==null) return;
        if (e==eCscShutdown)
        {
          RmiPanelAdapter rpa = null;
          for (EElement e2 : eScreens.getItemElements())
            if (e2.isSelected())
            {
              rpa = (RmiPanelAdapter)e2.getData();
              try
              {
                ((IScreen)rpa.getPeer()).exit();
              }
              catch (RemoteException e1)
              {
                Log.err("Cannot exit peer.", e1);
              }
            }
          eCscShutdown.setData(null);
        }
        eConfirm.setData(null);
      }      
    });
    add(eConfirm);
    
    eScrCnt = new EElbo(this,1160,23,461,146,ce|LCARS.ES_STATIC|LCARS.ES_SHAPE_NW|LCARS.ES_LABEL_SE,null);
    eScrCnt.setArmWidths(192,68); eScrCnt.setArcWidths(177,90);
    add(eScrCnt);

    eScrLock = new ERect(this,1160,174,192,96,c1|LCARS.ES_LABEL_SE,"LOCK");
    add(eScrLock);

    eScrPrev = new ERect(this,1160,275,192,96,c2|LCARS.ES_LABEL_SE,"PREV");
    add(eScrPrev);

    eScrNext = new ERect(this,1160,376,192,96,c2|LCARS.ES_LABEL_SE,"NEXT");
    add(eScrNext);
    
    eSrvLd = new EElbo(this,1160,477,714,221,ce|LCARS.ES_STATIC|LCARS.ES_SHAPE_SW|LCARS.ES_LABEL_NE,"SERVER LD\n000-00");
    eSrvLd.setArmWidths(192,11); eSrvLd.setArcWidths(1,1);
    add(eSrvLd);
    
    eScreens = new EElementArray(1365,173,EValue.class,new Dimension(509,48),10,1,LCARS.ES_LABEL_W|LCARS.ES_RECT_RND_E|LCARS.EC_PRIMARY,"SCREENS");
    eScreens.setLockControl(eScrLock); eScreens.setPageControls(eScrPrev,eScrNext);
    eScreens.addEEventListener(new EEventListenerAdapter()
    {
      @Override
      public void touchDown(EEvent ee)
      {
        for (EElement e : eScreens.getItemElements())
          e.setSelected(e==ee.el);
      }
    });
    eScreens.addToPanel(this);
    
    // Screen control elbo
    eCscShutdown = new ERect(this,1160,703,192,145,cp|LCARS.ES_LABEL_SE,"SCREEN\nSHUTDOWN");
    eCscShutdown.addEEventListener(new EEventListenerAdapter()
    {
      @Override
      public void touchDown(EEvent ee)
      {
        eCscShutdown.setData(new Integer(0));
        eConfirm.setData(eCscShutdown);
      }
    });
    add(eCscShutdown);
    
    eCscLd = new EElbo(this,1160,853,664,126,ce|LCARS.ES_STATIC|LCARS.ES_SHAPE_SW|LCARS.ES_LABEL_NE,null);
    eCscLd.setArmWidths(192,43); eCscLd.setArcWidths(120,70);
    eCscLd.setColor(cSpecial);
    add(eCscLd);

    eRect = new ERect(this,1831,936,43,43,ce|LCARS.ES_STATIC|LCARS.ES_RECT_RND_E,null);
    eRect.setColor(cSpecial);
    add(eRect);
    
    eCsc = new EValue(this,1365,703,509,48,LCARS.EC_SECONDARY|LCARS.ES_STATIC|LCARS.ES_DISABLED|LCARS.ES_LABEL_W|LCARS.ES_RECT_RND_E,null);
    add(eCsc);
    
    add(new ELabel(this,1365,761,46,28,LCARS.EC_SECONDARY|LCARS.ES_STATIC|LCARS.ES_DISABLED|LCARS.ES_LABEL_E,"PNL"));
    eCscPnlUrl = new ELabel(this,1421,761,453,28,LCARS.EC_SECONDARY|LCARS.ES_STATIC|LCARS.ES_LABEL_W,null);
    add(eCscPnlUrl);
    
    add(new ELabel(this,1365,792,46,28,LCARS.EC_SECONDARY|LCARS.ES_STATIC|LCARS.ES_DISABLED|LCARS.ES_LABEL_E,"SCR"));
    eCscScrUrl = new ELabel(this,1421,792,453,28,LCARS.EC_SECONDARY|LCARS.ES_STATIC|LCARS.ES_LABEL_W,null);
    add(eCscScrUrl);
    
    eCscSize = new ELabel(this,1421,823,453,28,LCARS.EC_SECONDARY|LCARS.ES_STATIC|LCARS.ES_LABEL_W,null);
    add(eCscSize);

    eCscUpdate = new ERect(this,1365,861,123,65,LCARS.EC_SECONDARY|LCARS.ES_LABEL_S|LCARS.ES_RECT_RND_W,"UPDATE");
    eCscUpdate.addEEventListener(new EEventListenerAdapter()
    {
      @Override
      public void touchDown(EEvent ee)
      {
        RmiPanelAdapter rpa = getSelectedPanelAdapter();
        if (rpa!=null) rpa.updatePeer(); 
      }
    });
    add(eCscUpdate);

    eCscStart = new ERect(this,1493,861,123,65,LCARS.EC_PRIMARY|LCARS.ES_LABEL_SW,"START");
    eCscStart.addEEventListener(new EEventListenerAdapter()
    {
      @Override
      public void touchDown(EEvent ee)
      {
        RmiPanelAdapter rpa = getSelectedPanelAdapter();
        if (rpa!=null) rpa.start(); 
      }
    });
    add(eCscStart);

    eCscStop = new ERect(this,1621,861,123,65,LCARS.EC_PRIMARY|LCARS.ES_LABEL_SW,"STOP");
    eCscStop.addEEventListener(new EEventListenerAdapter()
    {
      @Override
      public void touchDown(EEvent ee)
      {
        RmiPanelAdapter rpa = getSelectedPanelAdapter();
        if (rpa!=null) rpa.stop(); 
      }
    });
    add(eCscStop);

    eCscPnlSel = new ERect(this,1749,861,123,65,LCARS.EC_SECONDARY|LCARS.ES_SELECTED|LCARS.ES_LABEL_SW|LCARS.ES_RECT_RND_E,"PNL SELECT");
    eCscPnlSel.addEEventListener(new EEventListenerAdapter()
    {
      @Override
      public void touchDown(EEvent ee)
      {
        RmiPanelAdapter rpa = getSelectedPanelAdapter();
        if (rpa!=null) rpa.panelSelectionDialog(); 
      }
    });
    add(eCscPnlSel);
  }

  @Override
  protected void fps2()
  {
    super.fps2();
    
    // Update log length display
    eLogSize.setLabel(String.format("%03d",eLog.getItemCount()));
    
    // Update connected screens display
    synchronized(this)
    {
      HashMap<String,RmiPanelAdapter> rpas = LCARS.getPanelAdapters();
      eScrCnt.setLabel(String.format("SERVING %03d",eScreens.getItemCount()));

      // Remove buttons
      for (int i=0; i<eScreens.getItemCount(); )
      {
        RmiPanelAdapter rpa = (RmiPanelAdapter)eScreens.getItemElement(i).getData();
        if (!rpas.containsValue(rpa))
          eScreens.remove(i);
        else
          i++;
      }
        
      // Add buttons
      int ld  = 0;
      int fps = 0;
      for (RmiPanelAdapter rpa : rpas.values())
      {
        boolean found = false;
        for (EElement e : eScreens.getItemElements())
          if (e.getData()==rpa)
          {
            ((EValue)e).setValue(rpa.getPanelTitle().toUpperCase());
            ((EValue)e).setLabel(rpa.getPeerHostName().toUpperCase());
            found = true;
            break;
          }
        if (!found)
        {
          EValue e = (EValue)eScreens.add(rpa.getRmiPeerUrl());
          e.setSelected(true);
          e.setData(rpa);
          e.setValue(rpa.getPanelTitle().toUpperCase());
          e.setValueMargin(43);
          e.setLabel(rpa.getPeerHostName().toUpperCase());
          eScreens.animate();
        }
        LoadStatistics lds = rpa.getLoadStatistics();
        ld  += lds.getLoad();
        fps += lds.getEventsPerPeriod();
      }
      if (eScreens.getItemCount()==0)
        eScreens.setTitle("NO SCREENS");
      else
        eScreens.setTitle("SCREENS");
      eSrvLd.setLabel(String.format("SRV LOAD\n%03d-%02d",ld,fps));
    }
    
    // Update selected screen display
    synchronized(this)
    {
      RmiPanelAdapter rpa = getSelectedPanelAdapter();
      eCscShutdown.setDisabled(rpa==null);
      eCscUpdate.setDisabled(rpa==null);
      eCscStart.setDisabled(rpa==null);
      eCscStop.setDisabled(rpa==null);
      eCscPnlSel.setDisabled(rpa==null);
      eCsc.setDisabled(rpa==null);
      if (rpa!=null)
      {
        eCsc.setLabel(rpa.getPeerHostName().toUpperCase());
        eCsc.setValue(rpa.getPanelClass().getSimpleName());
        try
        {
          LoadStatistics lds = ((IScreen)rpa.getPeer()).getLoadStatistics();
          eCscLd.setLabel(String.format("SCR LOAD\n%03d-%02d",lds.getLoad(),lds.getEventsPerPeriod()));

          int cnt = rpa.getLoadStatistics().getEventsPerPeriod();
          int mem = ((IRmiScreenAdapterRemote)rpa.getPeer()).getMemStat()/1024;
          eCscSize.setLabel(String.format(Locale.ENGLISH,"%03d kB, %04.1f MBit/s",mem,((float)(mem*cnt)*8)/1024.f));
        }
        catch (Exception e)
        {
          eCscLd.setLabel("SCR LOAD");
          eCscSize.setLabel(null);
        }
        eCscPnlUrl.setLabel(rpa.getRmiUrl());
        eCscScrUrl.setLabel(rpa.getRmiPeerUrl());
      }
      else
      {
        eCsc.setValue(null); eCsc.setLabel("NO SELECTION");
        eCscLd.setLabel("SCR LOAD");
        eCscPnlUrl.setLabel(null);
        eCscScrUrl.setLabel(null);
        eCscSize.setLabel(null);
      }
    }
    
    // EXPERIMENTAL
    if (eCscShutdown.getData()!=null)
    {
      Integer ctr = (Integer)eCscShutdown.getData();
      if (ctr%2==1)
      {
        eCscShutdown.setColor(cRed);
        eCscShutdown.setLabel("PRESS CONFIRM!");
      }
      else
      {
        eCscShutdown.setColor((ColorMeta)null);
        eCscShutdown.setLabel("SCREEN\nSHUTDOWN");
      }
      eCscShutdown.setData(ctr<9?new Integer(ctr+1):null);
      if (eCscShutdown.getData()==null)
        if (eConfirm.getData()==eCscShutdown)
          eConfirm.setData(null);
    }
    else
    {
      eCscShutdown.setColor((ColorMeta)null);
      eCscShutdown.setLabel("SCREEN\nSHUTDOWN");
    }
    
    eConfirm.setStatic(eConfirm.getData()==null);
    eConfirm.setBlinking(eConfirm.getData()!=null);
    eConfirm.setColor(eConfirm.getData()!=null?cRed:null);
  }
  
  // -- Getters and setters --
  
  /**
   * Returns the panel adapter currenly selected in the 
   */
  protected RmiPanelAdapter getSelectedPanelAdapter()
  {
    for (EElement e : eScreens.getItemElements())
      if (e.isSelected())
        try
        {
          return (RmiPanelAdapter)e.getData(); 
        }
        catch (Exception e1)
        {
          Log.err("Cannot get selected panel adapter.", e1);
        }
    return null;
  }
  
  // -- Logging --

  /**
   * Dispatches a log or error message to all server pabels.
   * 
   * @param pfx
   *          The message prefix (used for message filtering).
   * @param msg
   *          The message.
   * @param err
   *          <code>true</code> for an error message, <code>false</code> for an ordinary one.
   */
  public static void logMsg(String pfx, String msg, boolean err)
  {
    if (instances!=null)
      for (int i=0; i<instances.size(); )
        if (instances.get(i).get()!=null)
        {
          instances.get(i).get().logInt(pfx,msg,err);
          i++;
        }
        else
          instances.remove(i);
  }
  
  /**
   * Displays a log or error message.
   * 
   * @param pfx
   *          The message prefix (used for message filtering).
   * @param msg
   *          The message.
   * @param err
   *          <code>true</code> for an error message, <code>false</code> for an ordinary one.
   */
  private void logInt(String pfx, String msg, boolean err)
  {
    ELabel e = (ELabel)eLog.add(String.format("[%s:%s]",pfx,msg));
    if (err) e.setColor(cRed);
  }
  
}

// EOF
