package de.tucottbus.kt.lcars.util;

import gov.nasa.worldwind.BasicModel;
import gov.nasa.worldwind.awt.WorldWindowGLCanvas;

import java.awt.BorderLayout;
import java.awt.Rectangle;
import java.rmi.RemoteException;

import javax.media.nativewindow.util.Point;

import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.widgets.Composite;

import de.tucottbus.kt.lcars.IScreen;
import de.tucottbus.kt.lcars.LCARS;
import de.tucottbus.kt.lcars.Panel;
import de.tucottbus.kt.lcars.Screen;
import de.tucottbus.kt.lcars.elements.EEvent;
import de.tucottbus.kt.lcars.elements.EEventListener;
import de.tucottbus.kt.lcars.elements.EEventListenerAdapter;
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

    // WorldWind test
    final Composite swtCmpsWwj = new Composite(((Screen)getScreen()).getLcarsComposite(),SWT.EMBEDDED);
    swtCmpsWwj.setBounds(100,100,640,480);
    final java.awt.Frame awtFrameWwj = SWT_AWT.new_Frame(swtCmpsWwj);
    final java.awt.Panel awtPanelWwj = new java.awt.Panel(new java.awt.BorderLayout());
    awtFrameWwj.add(awtPanelWwj);

    final WorldWindowGLCanvas wwj = new WorldWindowGLCanvas();
    wwj.setModel(new BasicModel());
    awtPanelWwj.add(wwj, BorderLayout.CENTER);

    // Push and drag test
    ERect eRect = new ERect(this,990,22,208,80,LCARS.EC_ELBOUP|LCARS.ES_LABEL_E|LCARS.ES_RECT_RND,"PUSH ME");
    eRect.addEEventListener(new EEventListener()
    {
      
      @Override
      public void touchUp(EEvent ee)
      {
        // TODO Auto-generated method stub
        System.err.println("UP");
      }
      
      @Override
      public void touchHold(EEvent ee)
      {
        // TODO Auto-generated method stub
        System.err.println("HOLD");
      }
      
      @Override
      public void touchDrag(EEvent ee)
      {
        // TODO Auto-generated method stub
        System.err.println("DRAG");
      }
      
      @Override
      public void touchDown(EEvent ee)
      {
        // TODO Auto-generated method stub
        System.err.println("DOWN");
      }
    });
    add(eRect);

    // Drag buttons
    for (int i = 0, x = 500; i < 10; i++, x+=111)
      add(createDragButton("DRAG ME", x, 152));
        
    // Other buttons
    eRect = new ERect(this,1209,22,208,80,LCARS.EC_ELBOUP|LCARS.ES_LABEL_E|LCARS.ES_RECT_RND,"HELP");
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
        } 
        catch (RemoteException e)
        {
          e.printStackTrace();
        }
      }
    });
    add(eRect);
  }

  private ERect createDragButton(String label, int x, int y) {
    final Point dragOffset = new Point();
    ERect eRect = new ERect(this,x,y,100,80,LCARS.EC_ELBOUP|LCARS.ES_LABEL_E|LCARS.ES_RECT_RND|LCARS.EB_OVERDRAG,label);
    eRect.addEEventListener(new EEventListener()
    {
      @Override
      public void touchDown(EEvent ee)
      {
        dragOffset.set(ee.pt.x,ee.pt.y);
        System.err.println("DOWN");
      }
      
      @Override
      public void touchUp(EEvent ee)
      {
        System.err.println("UP");
      }
      
      @Override
      public void touchHold(EEvent ee)
      {
        System.err.println("HOLD");
      }
      
      @Override
      public void touchDrag(EEvent ee)
      {
        System.err.println("DRAG");
        Rectangle r = ee.el.getBounds();
        r = new Rectangle(r.x+ee.pt.x-dragOffset.getX(),r.y+ee.pt.y-dragOffset.getY(),r.width,r.height);
        ee.el.setBounds(r);
      }
      
    });
    return eRect;
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
