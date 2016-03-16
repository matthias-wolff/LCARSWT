package de.tucottbus.kt.lcars;

import java.awt.Dimension;
import java.rmi.RemoteException;

import de.tucottbus.kt.lcars.contributors.EPanelSelector;
import de.tucottbus.kt.lcars.elements.EElement;
import de.tucottbus.kt.lcars.elements.EEvent;
import de.tucottbus.kt.lcars.elements.EEventListenerAdapter;
import de.tucottbus.kt.lcars.elements.EImage;
import de.tucottbus.kt.lcars.elements.ERect;
import de.tucottbus.kt.lcars.logging.Log;
import de.tucottbus.kt.lcars.swt.ColorMeta;
import de.tucottbus.kt.lcars.swt.ImageMeta;

/**
 * PADD main panels are displayed on the PADD panel selector.
 * 
 * @see EPanelSelector
 * @author Matthias Wolff
 */
public abstract class PaddMainPanel extends Panel
{
  private static final String IMAGE_WE_TAB_REMOVE_BUTTON = "lcars/padd/WeTabRemoveButton.png";
  
  public PaddMainPanel(IScreen screen)
  {
    super(screen);
  }
  
  @Override
  public void init()
  {
    super.init();
    Dimension dim = getDimension();
    
    final String remErr = "Some exception with remote screen occured.";
    
    // Hidden emergency button (in the lower right corner)
    EElement e;
    e = new ERect(this,0,dim.height-23,23,23,0,null);
    e.setColor(new ColorMeta(0,true));
    e.addEEventListener(new EEventListenerAdapter()
    {
      @Override
      public void touchHold(EEvent ee)
      {
        if (ee.ct==20)
        {
          setColorScheme(LCARS.CS_REDALERT);
          invalidate();
          return;
        }
        if (ee.ct>40)
        {
          dim((ee.ct%20)/20f);
          return;
        }
        if (ee.ct==100)
          try
          {
            getScreen().exit();
          }
          catch (RemoteException e)
          {
            Log.err(remErr, e);
          }
      }
      @Override
      public void touchUp(EEvent ee)
      {
        setColorScheme(LCARS.CS_MULTIDISP);
        dim(1);
        invalidate();
      }      
    });
    add(e);
    
    // On WeTab: add a close button
    if ("wetab".equals(LCARS.getArg("--device=")))
    {
      e = new EImage(this,dim.width-50,20,0,new ImageMeta.Resource(IMAGE_WE_TAB_REMOVE_BUTTON));
      e.addEEventListener(new EEventListenerAdapter()
      {
        @Override
        public void touchDown(EEvent ee)
        {
          setColorScheme(LCARS.CS_REDALERT);
          invalidate();
        }      
        @Override
        public void touchHold(EEvent ee)
        {
          if (ee.ct==30)
            try
            {
              getScreen().exit();
            }
            catch (RemoteException e)
            {
              Log.err(remErr, e);
            }
        }
        @Override
        public void touchUp(EEvent ee)
        {
          setColorScheme(LCARS.CS_MULTIDISP);
          invalidate();
        }      
      });
      add(e);
    }
  }
}

// EOF

