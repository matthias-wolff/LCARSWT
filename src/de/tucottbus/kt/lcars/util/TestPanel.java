package de.tucottbus.kt.lcars.util;

import java.rmi.RemoteException;

import de.tucottbus.kt.lcars.IScreen;
import de.tucottbus.kt.lcars.LCARS;
import de.tucottbus.kt.lcars.Panel;
import de.tucottbus.kt.lcars.elements.EEvent;
import de.tucottbus.kt.lcars.elements.EEventListenerAdapter;
import de.tucottbus.kt.lcars.elements.ERect;
import de.tucottbus.kt.lcars.speech.ESpeechInput;
import de.tucottbus.kt.lcars.swt.SwtColor;

public class TestPanel extends Panel
{

  public TestPanel(IScreen iscreen)
  {
    super(iscreen);
  }

  @Override
  public void init()
  {
    super.init();
    setTitle("TEST PANEL");
    
    ERect eRect = new ERect(this,1209,22,208,80,LCARS.EC_ELBOUP|LCARS.ES_LABEL_E|LCARS.ES_RECT_RND,"HELP");
    eRect.addEEventListener(new EEventListenerAdapter()
    {
      @Override
      public void touchDown(EEvent ee)
      {
        help();
      }
    });
    add(eRect);
    eRect = new ERect(this,1420,22,208,80,LCARS.EC_ELBOUP|LCARS.ES_LABEL_E|LCARS.ES_RECT_RND,"EXIT");
    eRect.addEEventListener(new EEventListenerAdapter()
    {
      @Override
      public void touchDown(EEvent ee)
      {
        try
        {
          getScreen().exit();
        } catch (RemoteException e)
        {
        }
      }
    });
    add(eRect);
    
    int x = 1;
    int y = 1;
    int w = 1000;
    int h = 100;
    
    eRect = new ERect(this,x-1,y-1,w+2,h+2,LCARS.ES_STATIC|LCARS.ES_OUTLINE,null);
    eRect.setColor(new SwtColor(0x404040));
    add(eRect);
   
    ESpeechInput.EFvrValue eFvr = new ESpeechInput.EFvrValue(this,x,y,w,h,LCARS.ES_STATIC,null);
    eFvr.setLabel("SWITCH[switch[on]][MCID[CN2[o[1]]]][MCID[-]][MCID[CN2[o[0]][t[1]]]][MCID[CN2[t[2]]]][MCID[CN2[o[8]][t[2]]]][MCID[CN2[o[4]][t[4]]]]");
    add(eFvr);
  }

  /**
   * Convenience method: Runs the test panel.
   * 
   * @param args
   *          The command line arguments, see {@link LCARS#main(String[])}.
   */
  public static void main(String[] args)
  {
    args = LCARS.setArg(args,"--panel=",TestPanel.class.getName());
    LCARS.main(args);
  }

}
