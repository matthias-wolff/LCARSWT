package de.tucottbus.kt.lcars.contributors;

import java.awt.Dimension;
import java.rmi.RemoteException;
import java.util.AbstractList;
import java.util.ArrayList;

import de.tucottbus.kt.lcars.IScreen;
import de.tucottbus.kt.lcars.LCARS;
import de.tucottbus.kt.lcars.MainPanel;
import de.tucottbus.kt.lcars.Panel;
import de.tucottbus.kt.lcars.elements.EElement;
import de.tucottbus.kt.lcars.elements.EEvent;
import de.tucottbus.kt.lcars.elements.EEventListenerAdapter;
import de.tucottbus.kt.lcars.elements.ELabel;
import de.tucottbus.kt.lcars.elements.ERect;
import de.tucottbus.kt.lcars.elements.EValue;
import de.tucottbus.kt.lcars.logging.Log;
import de.tucottbus.kt.lcars.swt.ColorMeta;

public class EPanelSelector extends EMessageBox
{
  private static final int style1 = LCARS.EC_PRIMARY|LCARS.ES_SELECTED|LCARS.ES_STATIC|LCARS.ES_MODAL; 
  private static final int style2 = LCARS.EC_SECONDARY|LCARS.ES_MODAL; 
  
  private AbstractList<Class<? extends Panel>> panelList;
  
  private ERect eDismiss;
  
  private ArrayList<EElement> veList = new ArrayList<EElement>();
	
  public EPanelSelector(int x, int y, int w, int h)
  {
    super(x,y,w,h);
  }
  
  @Override
  protected void init()
  {
    Dimension d = getDimension();
    
    EValue eTit = new EValue(null,0,0,d.width,53,style1|LCARS.ES_RECT_RND,null);
    eTit.setValue("PANEL SELECT");
    add(eTit);
    ERect e = new ERect(null,0,64,d.width,d.height-77,LCARS.ES_STATIC|LCARS.ES_MODAL,null);
    e.setColor(new ColorMeta(0x80000000,true));
    add(e);
    add(new ERect(null,0,d.height+3,d.width-75,3,style1,null));
    add(new ELabel(null,0,d.height+3,d.width-10,14,style1|LCARS.ES_LABEL_NE|LCARS.EF_TINY,"LCARS DIALOG"));

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

    // Add the panel buttons
    int y = 66;
    for (Class<? extends Panel> panelClass : getPanelList())
    {
      String label = Panel.guessName(panelClass).toUpperCase();
      ERect btn = new ERect(null,0,y,250,53,style2|LCARS.ES_RECT_RND|LCARS.ES_LABEL_E,label);
      btn.setData(panelClass.getName());
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
              Log.err("Cannot start panel.", e);
            }
          }
          catch (RemoteException e)
          {
            Log.err("Cannot stop panel.", e);
          }
        }
      });
      add(btn); veList.add(btn);
      
      EElement e = new ERect (null,250,y+27,90,3,style2|LCARS.ES_STATIC,null);
      add(e); veList.add(e);

      String s = panelClass.getPackage().getName().toUpperCase();
      e = new ELabel(null,340,y,250,53,LCARS.EC_SECONDARY|LCARS.EF_NORMAL|LCARS.ES_STATIC|LCARS.ES_LABEL_W|LCARS.ES_MODAL,s);
      add(e); veList.add(e);
      y+=56;
    }
    
    panel.dim(0.3f);
    panel.setModal(true);
    addToPanel(panel);
    eDismiss.setDisabled(panel!=null?Panel.class.equals(panel.getClass()):true);
  }

  public void close()
  {
    if (panel==null) return;
    for (EElement el : veList)
      el.removeAllEEventListeners();
    panel.removeAll(veList);
    veList.clear();
    panel.dim(1f);
    panel.setModal(false);
    removeFromPanel();
  }
  
  /**
   * Sets the list of classes to be displayed on the {@linkplain EPanelSelector
   * panel selector}. 
   * 
   * @param list
   *          A list of panel classes, can be <code>null</code> in which case
   *          the list of {@link MainPanel}s is displayed.
   */
  public void setPanelList(AbstractList<Class<? extends Panel>> list)
  {
    this.panelList = list;
  }
  
  /**
   * Retrieves the list of panel classes being displayed on the panel selector.
   */
  public AbstractList<Class<? extends Panel>> getPanelList()
  {
    if (this.panelList!=null)
      return this.panelList;
    
    AbstractList<Class<? extends Panel>> list = LCARS.getMainPanelClasses();
    if (LCARS.getArg("--server")!=null)
      try
      {
        list.add(Class.forName("de.tucottbus.kt.lcars.net.ServerPanel").asSubclass(Panel.class));
      }
      catch (Exception e)
      {
        Log.err("Cannot get 'de.tucottbus.kt.lcars.net.ServerPanel'", e);
      }
    if (Panel.getSpeechEngine()!=null)
      try
      {
        list.add(Class.forName("de.tucottbus.kt.lcars.speech.SpeechEnginePanel").asSubclass(Panel.class));
      }
      catch (Exception e)
      {
        Log.err("Cannot get 'de.tucottbus.kt.lcars.speech.SpeechEnginePanel'", e);
      }
    return list;
  }
}
