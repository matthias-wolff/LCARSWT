package de.tucottbus.kt.lcars.swt;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import de.tucottbus.kt.lcars.IScreen;
import de.tucottbus.kt.lcars.LCARS;
import de.tucottbus.kt.lcars.Panel;
import de.tucottbus.kt.lcars.elements.EElement;
import de.tucottbus.kt.lcars.elements.EEvent;
import de.tucottbus.kt.lcars.elements.EEventListenerAdapter;
import de.tucottbus.kt.lcars.elements.ELabel;
import de.tucottbus.kt.lcars.elements.ERect;
import de.tucottbus.kt.lcars.elements.EValue;
import de.tucottbus.kt.lcars.logging.Log;
import de.tucottbus.kt.lcars.speech.ESpeechInput;
import de.tucottbus.kt.lcars.util.LoadStatistics;

public class TestPanel extends Panel
{

  private static final int m = 10;
  private static final int w = 200;
  private static final int h = 100;

  private static final int x0 = m;
  private static final int x1 = x0 + w + m;
  private static final int x2 = x1 + w + m;
  private static final int x3 = x2 + w + m;
  private static final int y0 = m;
  private static final int y1 = y0 + h + m;
  private static final int y2 = y1 + h + m;
  private static final int y3 = y2 + h + m;

  private Timer timer;

  public TestPanel(IScreen iscreen)
  {
    super(iscreen);
  }

  private TimerTask runAtFrameRate(Runnable task)
  {
    if (timer == null)
      timer = new Timer();
    TimerTask result = new TimerTask()
    {
      @Override
      public void run()
      {
        task.run();
      }
    };
    timer.schedule(result, 40, 40);
    return result;
  }

  private TimerTask runEverySecond(Runnable task)
  {
    if (timer == null)
      timer = new Timer();
    TimerTask result = new TimerTask()
    {
      @Override
      public void run()
      {
        task.run();
      }
    };
    timer.schedule(result, 1000, 1000);
    return result;
  }

  private void initLayout()
  {
    String text = "A\nBB";
    // text += "\nCCC\nDDDD\nEEEEE\nFFFFFF\nGGGGGGG\nHHHHHHHH";

    add(new ERect(this, x1, y1, w, h, LCARS.EC_SECONDARY | LCARS.ES_LABEL_NW,
        text));
    add(new ERect(this, x2, y1, w, h, LCARS.EC_SECONDARY | LCARS.ES_LABEL_N,
        text));
    add(new ERect(this, x3, y1, w, h, LCARS.EC_SECONDARY | LCARS.ES_LABEL_NE,
        text));
    add(new ERect(this, x1, y2, w, h, LCARS.EC_SECONDARY | LCARS.ES_LABEL_W,
        text));
    add(new ERect(this, x2, y2, w, h, LCARS.EC_SECONDARY | LCARS.ES_LABEL_C,
        text));
    add(new ERect(this, x3, y2, w, h, LCARS.EC_SECONDARY | LCARS.ES_LABEL_E,
        text));
    add(new ERect(this, x1, y3, w, h, LCARS.EC_SECONDARY | LCARS.ES_LABEL_SW,
        text));
    add(new ERect(this, x2, y3, w, h, LCARS.EC_SECONDARY | LCARS.ES_LABEL_S,
        text));
    add(new ERect(this, x3, y3, w, h, LCARS.EC_SECONDARY | LCARS.ES_LABEL_SE,
        text));
  }

  private void initLabels()
  {
    final int h = 50;

    final ArrayList<EElement> els = new ArrayList<EElement>(10);
    els.add(new ELabel(this, x1, y1, w, h,
        LCARS.EC_SECONDARY | LCARS.ES_LABEL_W, null));

    ELabel eLabel = new ELabel(this, x1 + 10, y1 + 10, w, h,
        LCARS.EC_SECONDARY | LCARS.ES_LABEL_W, null);
    els.add(eLabel);
    eLabel.setAlpha(.5f);

    for (EElement el : els)
      add(el);
    runAtFrameRate(() -> {
      String dt = (new Date()).toString();
      for (EElement el : els)
        el.setLabel(dt);
    });

    EValue eTimecode = add(
        new EValue(this, m, 991, 278, 66, LCARS.EC_PRIMARY | LCARS.ES_STATIC
            | LCARS.ES_VALUE_W | LCARS.ES_LABEL_SE, "TIME INDEX"));
    int[] sec = {0};
    runAtFrameRate(() -> {
      eTimecode.setValue(String.format(Locale.ENGLISH,"%02d:%01d",sec[0]/60,sec[0]++%60));
    });

  }

  private void initSpeechInpuPanel()
  {
    ERect eRect;
    eRect = new ERect(this, 1209, 22, 208, 80,
        LCARS.EC_ELBOUP | LCARS.ES_LABEL_E | LCARS.ES_RECT_RND, "HELP");
    eRect.addEEventListener(new EEventListenerAdapter()
    {
      @Override
      public void touchDown(EEvent ee)
      {
        help();
      }
    });
    add(eRect);
    eRect = new ERect(this, 1420, 22, 208, 80,
        LCARS.EC_ELBOUP | LCARS.ES_LABEL_E | LCARS.ES_RECT_RND, "EXIT");
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

    int x = 300;
    int y = 500;
    int w = 1000;
    int h = 100;

    eRect = new ERect(this, x - 1, y - 1, w + 2, h + 2,
        LCARS.ES_STATIC | LCARS.ES_OUTLINE, null);
    eRect.setColor(new SwtColor(0x404040));
    add(eRect);

    ESpeechInput.EFvrValue eFvr = new ESpeechInput.EFvrValue(this, x, y, w, h,
        LCARS.ES_STATIC, null);
    eFvr.setLabel(
        "SWITCH[switch[on]][MCID[CN2[o[1]]]][MCID[-]][MCID[CN2[o[0]][t[1]]]][MCID[CN2[t[2]]]][MCID[CN2[o[8]][t[2]]]][MCID[CN2[o[4]][t[4]]]]");
    add(eFvr);
  }

  private void initStatistics() {
    ELabel eGuiLd = add(new ELabel(this, 0, 0, 192, 60, LCARS.ES_STATIC, null));
    runEverySecond(() -> {
      LoadStatistics ls1 = getLoadStatistics();
      String s = String.format("%03d-%02d", ls1.getLoad(),
          ls1.getEventsPerPeriod());
      try
      {
        LoadStatistics ls2 = getScreen().getLoadStatistics();
        s += String.format("/%03d-%02d", ls2.getLoad(),
            ls2.getEventsPerPeriod());
      } catch (RemoteException e)
      {
        e.printStackTrace();
      }
      eGuiLd.setLabel(s);
    });
  }
  
  
  
  @Override
  public void init()
  {
    super.init();
    setTitle("TEST PANEL");

    if (LCARS.getArg("--testLayout") != null)
      initLayout();
    if (LCARS.getArg("--testSpeechInput") != null)
      initSpeechInpuPanel();
    if (LCARS.getArg("--testELabel") != null)
      initLabels();
    if (LCARS.getArg("--noStats") == null)
      initStatistics();    
  }

  
  private static void addComp(Composite parent, int fg, int bg, int xOff) {
    int h = parent.getSize().y;
    Display display = parent.getDisplay();
    Composite composite = new Composite(parent, SWT.TRANSPARENT);
    composite.setLayout(new FillLayout());
    composite.setEnabled(true);
    composite.setSize(parent.getSize());
    composite.setBackground(parent.getDisplay().getSystemColor(bg));
    composite.addPaintListener(new PaintListener()
    {
      final int xMax = parent.getSize().x*2;
      int x = xOff%h;
      @Override
      public void paintControl(PaintEvent e)
      {
        e.gc.setForeground(e.gc.getDevice().getSystemColor(fg));
        e.gc.drawLine(xMax/4, h, Math.abs((x = (x+5)%xMax)-h), 0);
        //shell.redraw();
        
      }
    });
    Timer timer = new Timer();
    timer.schedule(new TimerTask()
    {
      
      @Override
      public void run()
      {
        display.asyncExec(()-> {
          composite.redraw();
        });
      }
    }, 40, 40);    
  }
  
  public static void initShell() {
    final int l = 400;
    
    Display display = Display.getDefault();
    Shell shell = new Shell(display);
    shell.setSize(l,l);
    shell.setBackground(display.getSystemColor(SWT.COLOR_BLACK));
    addComp(shell, SWT.COLOR_CYAN, SWT.COLOR_BLUE, 0);
    addComp(shell, SWT.COLOR_RED, SWT.COLOR_DARK_RED, 300);
    
    shell.open();
    
    // Run SWT event loop 
    while (!shell.isDisposed())
      try
      {
        while (!shell.isDisposed())
          if (!Display.getDefault().readAndDispatch())
            Display.getDefault().sleep();
      }
      catch (Exception e)
      {
        Log.err("Error in screen execution.", e);
      }    
  }
  
  /**
   * Convenience method: Runs the test panel.
   * 
   * @param args
   *          The command line arguments, see {@link LCARS#main(String[])}.
   */
  public static void main(String[] args)
  {    
    LCARS.main(LCARS.setArg(args, "--panel=", TestPanel.class.getName()));
    //initShell();
  }
}
