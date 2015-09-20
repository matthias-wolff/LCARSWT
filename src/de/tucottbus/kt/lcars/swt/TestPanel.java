package de.tucottbus.kt.lcars.swt;

import de.tucottbus.kt.lcars.IScreen;
import de.tucottbus.kt.lcars.LCARS;
import de.tucottbus.kt.lcars.Panel;
import de.tucottbus.kt.lcars.elements.ERect;

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
    ERect eRect;
//    eRect = new ERect(this,1209,22,208,80,LCARS.EC_ELBOUP|LCARS.ES_LABEL_E|LCARS.ES_RECT_RND,"HELP");
//    eRect.addEEventListener(new EEventListenerAdapter()
//    {
//      @Override
//      public void touchDown(EEvent ee)
//      {
//        help();
//      }
//    });
//    add(eRect);
//    eRect = new ERect(this,1420,22,208,80,LCARS.EC_ELBOUP|LCARS.ES_LABEL_E|LCARS.ES_RECT_RND,"EXIT");
//    eRect.addEEventListener(new EEventListenerAdapter()
//    {
//      @Override
//      public void touchDown(EEvent ee)
//      {
//        try
//        {
//          getScreen().exit();
//        } catch (RemoteException e)
//        {
//        }
//      }
//    });
//    add(eRect);
    String text = "A\nBB";
    //text += "\nCCC\nDDDD\nEEEEE\nFFFFFF\nGGGGGGG\nHHHHHHHH";
    
    final int m  = 10;
    int w  = 200;
    int h  = 300;
    final int x0 = m;
    final int x1 = x0+w+m;
    final int x2 = x1+w+m;
    final int y0 = m;
    final int y1 = y0+h+m;
    final int y2 = y1+h+m;
    
    eRect = new ERect(null,x0,y0,108,48,LCARS.EC_SECONDARY|LCARS.ES_LABEL_E,"LOCK");
    add(eRect);
//    eRect = new ERect(this,x0,y0,w2,h2,LCARS.EC_SECONDARY|LCARS.ES_LABEL_NW,text);
//    add(eRect);
    
//    eRect = new ERect(this,x1,y0,w2,h2,LCARS.EC_SECONDARY|LCARS.ES_LABEL_N,text);
//    add(eRect);
//    
//    eRect = new ERect(this,x2,y0,w2,h2,LCARS.EC_SECONDARY|LCARS.ES_LABEL_NE,text);
//    add(eRect);
//    
//    eRect = new ERect(this,x0,y1,w2,h2,LCARS.EC_SECONDARY|LCARS.ES_LABEL_W,text);
//    add(eRect);
//    
//    eRect = new ERect(this,x1,y1,w2,h2,LCARS.EC_SECONDARY|LCARS.ES_LABEL_C,text);
//    add(eRect);
    
//    eRect = new ERect(this,x2,y1,w2,h2,LCARS.EC_SECONDARY|LCARS.ES_LABEL_E,text);
//    add(eRect);
//    
//    eRect = new ERect(this,x0,y2,w2,h2,LCARS.EC_SECONDARY|LCARS.ES_LABEL_SW,text);
//    add(eRect);
//    
//    eRect = new ERect(this,x1,y2,w2,h2,LCARS.EC_SECONDARY|LCARS.ES_LABEL_S,text);
//    add(eRect);
    
//    eRect = new ERect(this,x2,y2,w2,h2,LCARS.EC_SECONDARY|LCARS.ES_LABEL_SE,text);
//    add(eRect);
    
    
    
//    int x = 300;
//    int y = 500;
//    w = 1000;
//    h = 100;
//    
//    eRect = new ERect(this,x-1,y-1,w+2,h+2,LCARS.ES_STATIC|LCARS.ES_OUTLINE,null);
//    eRect.setColor(new SwtColor(0x404040));
//    add(eRect);
//   
//    ESpeechInput.EFvrValue eFvr = new ESpeechInput.EFvrValue(this,x,y,w,h,LCARS.ES_STATIC,null);
//    eFvr.setLabel("SWITCH[switch[on]][MCID[CN2[o[1]]]][MCID[-]][MCID[CN2[o[0]][t[1]]]][MCID[CN2[t[2]]]][MCID[CN2[o[8]][t[2]]]][MCID[CN2[o[4]][t[4]]]]");
//    add(eFvr);
  }

  /**
   * Convenience method: Runs the test panel.
   * 
   * @param args
   *          The command line arguments, see {@link LCARS#main(String[])}.
   */
  public static void main(String[] args)
  {
    LCARS.main(LCARS.setArg(args,"--panel=",TestPanel.class.getName()));
  }

}
