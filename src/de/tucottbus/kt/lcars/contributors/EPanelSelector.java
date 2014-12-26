package de.tucottbus.kt.lcars.contributors;

import java.awt.Color;
import java.awt.Dimension;
import java.rmi.RemoteException;
import java.util.List;

import de.tucottbus.kt.lcars.IScreen;
import de.tucottbus.kt.lcars.LCARS;
import de.tucottbus.kt.lcars.Panel;
import de.tucottbus.kt.lcars.elements.EEvent;
import de.tucottbus.kt.lcars.elements.EEventListenerAdapter;
import de.tucottbus.kt.lcars.elements.ELabel;
import de.tucottbus.kt.lcars.elements.ERect;
import de.tucottbus.kt.lcars.elements.EValue;

public class EPanelSelector extends EMessageBox
{
  private ERect eDismiss;
	
  public EPanelSelector(int x, int y, int w, int h)
  {
    super(x,y,w,h);
  }
  
  @Override
  protected void init()
  {
    int       style1 = LCARS.EC_PRIMARY|LCARS.ES_SELECTED|LCARS.ES_STATIC|LCARS.ES_MODAL; 
    int       style2 = LCARS.EC_SECONDARY|LCARS.ES_MODAL; 
    Dimension d      = getDimension();
    
    EValue eTit = new EValue(null,0,0,d.width,53,style1|LCARS.ES_RECT_RND,null);
    eTit.setValue("PANEL SELECT");
    add(eTit);
    ERect e = new ERect(null,0,64,d.width,d.height-77,LCARS.ES_STATIC|LCARS.ES_MODAL,null);
    e.setColor(new Color(0x80000000,true));
    add(e);
    e = new ERect(null,0,d.height+3,d.width-75,3,style1,null);
    add(e);
    add(new ELabel(null,0,d.height+3,d.width-10,14,style1|LCARS.ES_LABEL_SE|LCARS.EF_TINY,"LCARS DIALOG"));

    // Add the panel buttons
    List<Class<?>> l = LCARS.getMainPanelClasses();
    if (LCARS.getArg("--server")!=null)
      try
      {
        l.add(Class.forName("de.tucottbus.kt.lcars.net.ServerPanel"));
      }
      catch (ClassNotFoundException e1)
      {
      }
    if (Panel.getSpeechEngine()!=null)
      try
      {
        l.add(Class.forName("de.tucottbus.kt.lcars.speech.SpeechEnginePanel"));
      }
      catch (ClassNotFoundException e1)
      {
      }

    for (int i=0; i<l.size(); i++)
    {
      String label = Panel.guessName(l.get(i)).toUpperCase();
      ERect btn = new ERect(null,0,66+i*56,250,53,style2|LCARS.ES_RECT_RND|LCARS.ES_LABEL_E,label);
      btn.setData(l.get(i).getCanonicalName());
      btn.addEEventListener(new EEventListenerAdapter()
      {
        public void touchDown(EEvent ee)
        {
          String className = (String)ee.el.getData();
          IScreen screen = panel.getScreen();
          boolean cancel = (panel.getClass().getName().equals(className));
          fireAnswer(null);
          if (cancel) return;
          try
          {
            screen.getPanel().stop();
            try
            {
              screen.setPanel(className);
            }
            catch (ClassNotFoundException e)
            {
              e.printStackTrace();
            }
          }
          catch (RemoteException e){}
        }
      });
      add(btn);
      add(new ERect (null,250,93+i*56,90,3,style2|LCARS.ES_STATIC,null));
      String s = l.get(i).getPackage().getName().toUpperCase();
      add(new ELabel(null,340,66+i*56,250,53,LCARS.EC_SECONDARY|LCARS.EF_NORMAL|LCARS.ES_STATIC|LCARS.ES_LABEL_W|LCARS.ES_MODAL,s));
    }
    
    // Add the dismiss and exit buttons
    eDismiss = new ERect(null,0,d.height+16,123,53,style2|LCARS.ES_RECT_RND_W|LCARS.ES_LABEL_E,"DISMISS");
    eDismiss.addEEventListener(new EEventListenerAdapter()
    {
      public void touchDown(EEvent ee)
      {
        close();
      }
    });
    add(eDismiss);

    ERect btn = new ERect(null,126,d.height+16,124,53,style2|LCARS.ES_RECT_RND_E|LCARS.ES_LABEL_E,"EXIT");
    btn.addEEventListener(new EEventListenerAdapter()
    {
      public void touchDown(EEvent ee)
      {
        try { panel.getScreen().exit(); } catch (RemoteException e) {}
        removeFromPanel();
      }
    });
    add(btn);
    add(new ERect (null,250,d.height+43,90,3,style2|LCARS.ES_STATIC,null));
    add(new ELabel(null,340,d.height+16,250,53,LCARS.EC_SECONDARY|LCARS.EF_NORMAL|LCARS.ES_STATIC|LCARS.ES_LABEL_W|LCARS.ES_MODAL,"EXIT LCARS"));
  }

  public void open(Panel panel)
  {
    if (panel==null) return;
    panel.dim(0.3f);
    panel.setModal(true);
    addToPanel(panel);
    System.out.println(panel.getClass().getCanonicalName());
    eDismiss.setDisabled(panel!=null?Panel.class.equals(panel.getClass()):true);
  }

  public void close()
  {
    if (panel==null) return;
    panel.dim(1f);
    panel.setModal(false);
    removeFromPanel();
  }
  
}
