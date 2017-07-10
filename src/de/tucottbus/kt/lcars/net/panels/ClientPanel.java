package de.tucottbus.kt.lcars.net.panels;

import java.awt.Dimension;

import de.tucottbus.kt.lcars.IScreen;
import de.tucottbus.kt.lcars.LCARS;
import de.tucottbus.kt.lcars.Panel;
import de.tucottbus.kt.lcars.elements.EEvent;
import de.tucottbus.kt.lcars.elements.EEventListenerAdapter;
import de.tucottbus.kt.lcars.elements.ELabel;
import de.tucottbus.kt.lcars.elements.ERect;
import de.tucottbus.kt.lcars.net.NetUtils;

/**
 * Default panel being displayed at client screens.
 * 
 * @author Matthias Wolff
 */
public class ClientPanel extends Panel
{
  protected ERect eLock;

  public ClientPanel(IScreen iscreen)
  {
    super(iscreen);
  }

  @Override
  public void init()
  {
    super.init();
    setColorScheme(LCARS.CS_REDALERT);

    Dimension dim = getDimension();

    ELabel eTitle = new ELabel(this,0,dim.height/2-30,dim.width,60, 
      LCARS.EC_SECONDARY|LCARS.EF_HEAD1|LCARS.ES_STATIC|LCARS.ES_LABEL_C, null);
    setTitleLabel(eTitle);
    setTitle("LCARS CLIENT AT "+NetUtils.getHostName().toUpperCase());

    ELabel eSubTitle = new ELabel(this,0,dim.height/2+50,dim.width,20, 
      LCARS.EC_PRIMARY|LCARS.EF_LARGE|LCARS.ES_STATIC|LCARS.ES_LABEL_C, 
      "NO CONNECTION TO "+LCARS.getArg("--clientof="));
    eSubTitle.setBlinking(true);
    add(eSubTitle);
    
    ERect eRect = new ERect(this,25,22,208,49,LCARS.EC_SECONDARY|LCARS.ES_LABEL_E|LCARS.ES_RECT_RND_W,"EXIT");
    eRect.addEEventListener(new EEventListenerAdapter()
    {
      @Override
      public void touchDown(EEvent ee)
      {
        System.exit(0);
      }
    });
    add(eRect);
    eLock = new ERect(this,236,22,208,49,LCARS.ES_NOLOCK|LCARS.EC_SECONDARY|LCARS.ES_LABEL_E|LCARS.ES_RECT_RND_E,"PANEL LOCK");
    eLock.addEEventListener(new EEventListenerAdapter()
    {
      @Override
      public void touchDown(EEvent ee)
      {
        setLocked(!isLocked());
      }
    });
    add(eLock);

    // Enable automatic panel re-locking
    setAutoRelockTime(10);
  }

  @Override
  public void stop()
  {
    super.stop();
  }

  @Override
  public void panelSelectionDialog()
  {
    // Not allowed
  }

  @Override
  protected void fps10() 
  {
    eLock.setBlinking(isLocked());
    if (!isLocked() && getAutoRelockTime()>0)
      eLock.setLabel(String.format("PANEL LOCK (%02d)",getAutoRelock()));
    else
      eLock.setLabel("PANEL LOCK");
  }

}
