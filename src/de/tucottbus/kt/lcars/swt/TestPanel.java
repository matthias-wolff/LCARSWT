package de.tucottbus.kt.lcars.swt;

import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import javax.sound.sampled.UnsupportedAudioFileException;

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
import de.tucottbus.kt.lcars.elements.EElbo;
import de.tucottbus.kt.lcars.elements.EElement;
import de.tucottbus.kt.lcars.elements.EEvent;
import de.tucottbus.kt.lcars.elements.EEventListenerAdapter;
import de.tucottbus.kt.lcars.elements.EImage;
import de.tucottbus.kt.lcars.elements.ELabel;
import de.tucottbus.kt.lcars.elements.ERect;
import de.tucottbus.kt.lcars.elements.EValue;
import de.tucottbus.kt.lcars.logging.Log;
import de.tucottbus.kt.lcars.speech.ESpeechInput;
import de.tucottbus.kt.lcars.util.LoadStatistics;
import de.tucottbus.kt.lcarsx.al.AudioPlayer;
import de.tucottbus.kt.lcarsx.al.AudioTrack;
import de.tucottbus.kt.lcarsx.al.ELevelsDisplay;

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
    els.add(new ELabel(this, x1, y1, w * 5, h * 3,
        LCARS.EC_SECONDARY | LCARS.EF_HEAD2 | LCARS.ES_LABEL_W, null));

    ELabel eLabel = new ELabel(this, x1, y1 + h, w * 2, h,
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
    int[] sec =
    { 0 };
    runAtFrameRate(() -> {
      eTimecode.setValue(String.format(Locale.ENGLISH, "%02d:%01d", sec[0] / 60,
          sec[0]++ % 60));
    });
    
    final int style1 = LCARS.EC_PRIMARY|LCARS.ES_SELECTED|LCARS.ES_STATIC|LCARS.ES_MODAL;
    final int w = 1920;
    add(new ERect(null,0,50,w-75,3,style1,null));
    add(new ELabel(null,0,50,w-20,14,style1|LCARS.ES_LABEL_NE|LCARS.EF_TINY,"LCARS DIALOG"));


  }

  private void initSpeechInputPanel()
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
    eRect.setColor(new ColorMeta(0x404040));
    add(eRect);

    ESpeechInput.EFvrValue eFvr = new ESpeechInput.EFvrValue(this, x, y, w, h,
        LCARS.ES_STATIC, null);
    eFvr.setLabel(
        "SWITCH[switch[on]][MCID[CN2[o[1]]]][MCID[-]][MCID[CN2[o[0]][t[1]]]][MCID[CN2[t[2]]]][MCID[CN2[o[8]][t[2]]]][MCID[CN2[o[4]][t[4]]]]");
    add(eFvr);
  }

  private void initStatistics()
  {
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

  private void initDimmed()
  {
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
    
    EElbo eElbo = new EElbo(this,23,23,268,146,LCARS.EC_ELBOUP|LCARS.ES_LABEL_SE,"LCARS");
    eElbo.setArmWidths(192,46);
    add(eElbo);
    
    dim(.3f);
  }

  private static void addComp(Composite parent, int fg, int bg, int xOff)
  {
    int h = parent.getSize().y;
    Display display = parent.getDisplay();
    Composite composite = new Composite(parent, SWT.TRANSPARENT);
    composite.setLayout(new FillLayout());
    composite.setEnabled(true);
    composite.setSize(parent.getSize());
    composite.setBackground(parent.getDisplay().getSystemColor(bg));
    composite.addPaintListener(new PaintListener()
    {
      final int xMax = parent.getSize().x * 2;
      int x = xOff % h;

      @Override
      public void paintControl(PaintEvent e)
      {
        e.gc.setForeground(e.gc.getDevice().getSystemColor(fg));
        e.gc.drawLine(xMax / 4, h, Math.abs((x = (x + 5) % xMax) - h), 0);
        // shell.redraw();
      }
    });
    Timer timer = new Timer();
    timer.schedule(new TimerTask()
    {
      @Override
      public void run()
      {
        display.asyncExec(() -> {
          composite.redraw();
        });
      }
    }, 40, 40);
  }

  private void initAudioPlayer()
  {
    AudioPlayer player = AudioPlayer.getInstance();

    ELevelsDisplay eDisplay = new ELevelsDisplay(765, 515, 880, 125, 25);
    eDisplay.setBarColor(new ColorMeta(0x9999FF));
    eDisplay.setGridColor(new ColorMeta(0xDDB18E));
    eDisplay.addToPanel(this);

    try
    {
      AudioTrack track = new AudioTrack(new File(
          "C:/Users/borck/AppData/Local/Temp/lcars-wt-music/Matthias Wolff/L2/Captain's Lounge.mp3"));
      track.fetch();
      player.setCurrentTrack(track);
    } catch (UnsupportedAudioFileException | IOException e)
    {
      // TODO Auto-generated catch block
      e.printStackTrace();
      return;
    }
    
    player.play();
    runAtFrameRate(() -> {
      eDisplay.setAudioWindow(player.getAudioWindow(), player.getMediaTime());
    });
  }

  private void initAudioSlider()
  {
    //EGainSlider eGain = new EGainSlider(462,992,731,66);
    //eGain.addToPanel(this);
    
//    Log.debug(Integer.toHexString(((byte)(.6f*0xFF))&0xFF));
//    Log.debug(Integer.toHexString(Float.floatToRawIntBits(.0f)));
//    Log.debug(Integer.toHexString(Float.floatToRawIntBits(1f)));
//    
    Log.debug(Integer.toHexString((byte)(127)&0xFF));
    
    ERect eback = new ERect(null,0,0,1731,166,LCARS.EF_SMALL|LCARS.ES_LABEL_W,null);
    add(eback);

    ERect eTouchArea = new ERect(null,0,0,731,66,LCARS.EB_OVERDRAG,null);
    eTouchArea.setColor(new ColorMeta(0,true));
    add(eTouchArea);

  }

  public void initEImage()
  {
    add(new EImage(this, 7, 650, LCARS.ES_STATIC,new ImageMeta.Resource("resources/images/flare.png")));
  }

  public static void initShell()
  {
    final int l = 400;

    Display display = Display.getDefault();
    Shell shell = new Shell(display);
    shell.setSize(l, l);
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
      } catch (Exception e)
      {
        Log.err("Error in screen execution.", e);
      }
  }

  @Override
  public void init()
  {
    super.init();
    setTitle("TEST PANEL");
    if (LCARS.getArg("--testLayout") != null)
      initLayout();
    if (LCARS.getArg("--testSpeechInput") != null)
      initSpeechInputPanel();
    if (LCARS.getArg("--testELabel") != null)
      initLabels();
    if (LCARS.getArg("--testAudioPlayer") != null)
      initAudioPlayer();
    if (LCARS.getArg("--testAudioSlider") != null)
      initAudioSlider();
    if (LCARS.getArg("--testImage") != null)
      initEImage();
    if (LCARS.getArg("--testDim") != null)
      initDimmed();
    if (LCARS.getArg("--noStats") == null)
      initStatistics();
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
    // initShell();
  }
}
