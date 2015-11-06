package de.tucottbus.kt.lcars.swt;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Consumer;

import javax.sound.sampled.UnsupportedAudioFileException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.TouchEvent;
import org.eclipse.swt.events.TouchListener;
import org.eclipse.swt.events.TypedEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import de.tucottbus.kt.lcars.IScreen;
import de.tucottbus.kt.lcars.LCARS;
import de.tucottbus.kt.lcars.Panel;
import de.tucottbus.kt.lcars.contributors.EBrowser;
import de.tucottbus.kt.lcars.contributors.EElementArray;
import de.tucottbus.kt.lcars.elements.EElbo;
import de.tucottbus.kt.lcars.elements.EElement;
import de.tucottbus.kt.lcars.elements.EEvent;
import de.tucottbus.kt.lcars.elements.EEventListenerAdapter;
import de.tucottbus.kt.lcars.elements.EImage;
import de.tucottbus.kt.lcars.elements.ELabel;
import de.tucottbus.kt.lcars.elements.ERect;
import de.tucottbus.kt.lcars.elements.EValue;
import de.tucottbus.kt.lcars.geometry.rendering.LcarsComposite;
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
  private static final int x4 = x2 + w + m;
  private static final int y0 = m;
  private static final int y1 = y0 + h + m;
  private static final int y2 = y1 + h + m;
  private static final int y3 = y2 + h + m;
  private static final int y4 = y2 + h + m;

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
    
    add(new EElbo(this,23,23,268,146,LCARS.EC_ELBOUP|LCARS.ES_LABEL_SE,"LCARS"));
  }

  private void initLabels()
  {
    final int style1 = LCARS.EC_PRIMARY|LCARS.ES_SELECTED|LCARS.ES_STATIC|LCARS.ES_MODAL;
    final int h = 50;

    final ArrayList<EElement> els = new ArrayList<EElement>(10);
    els.add(new ELabel(this, x1, y1, w * 5, h * 3,
        LCARS.EC_ELBOUP | LCARS.EF_HEAD2 | LCARS.ES_LABEL_W, null));

    ELabel eLabel = new ELabel(this, x1, y1 + h, w * 2, h,
        LCARS.EC_TEXT | LCARS.EF_SMALL | LCARS.ES_LABEL_W, null);
    els.add(eLabel);
    eLabel.setAlpha(.5f);
    
    for (EElement el : els)
      add(el);
    runAtFrameRate(() -> {
      String dt = (new Date()).toString();
      for (EElement el : els)
        el.setLabel(dt);
    });

    ArrayList<ArrayList<EValue>> values = new ArrayList<ArrayList<EValue>>();
    
    int wVal = 50;
    int hVal = 30;
    
    for (int y = 400; y < 1080-hVal; y+=hVal) {
      ArrayList<EValue> line = new ArrayList<>();
      values.add(line);
      for (int x = 0; x < 1920-wVal; x+=wVal)
        line.add(add(new EValue(this, x, y, wVal, hVal, LCARS.EC_PRIMARY | LCARS.ES_STATIC
            | LCARS.ES_VALUE_W | LCARS.ES_LABEL_SE | LCARS.EF_SMALL, "TIME INDEX")));
    }
    
    int[] secArr =
    { 0 };
    String str = String.format(Locale.ENGLISH, "%02d:%01d", secArr[0] / 60, secArr[0] % 60);
    runAtFrameRate(() -> {
      int sec = secArr[0]++;
      for (ArrayList<EValue> line : values)
      {
        boolean visible = (sec++ % 8) == 0;
        for (EValue value : line) {
          value.setValue(str);
          value.setVisible(visible);
        }
      }
    });
    
    
    final int w = 1920;
    add(new ERect(null,0,50,w-75,3,style1,null));
    add(new ELabel(null,0,50,w-10,14,style1|LCARS.ES_LABEL_NE|LCARS.EF_TINY,"LCARS DIALOG"));

    EValue eTit = new EValue(null,0,0,1120/2,53,style1|LCARS.ES_RECT_RND,null);
    eTit.setValue("PANEL SELECT");
    add(eTit);
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

  
  private void initEElementArrayAnimation() {
    final int x = 200;
    final int y = 400;
    
    EValue        eTitle;
    EElementArray eKeys;
    EElementArray eValues;
    eTitle = new EValue(this,x+233,y-40,0,32,LCARS.ES_STATIC|LCARS.EC_TEXT,null);
    eTitle.setValueMargin(0); eTitle.setValue("TRACK TAGS");
    eTitle.setVisible(false);
    add(eTitle);
    int style = LCARS.ES_STATIC|LCARS.EF_SMALL|LCARS.EC_ELBOUP; 
    eKeys = new EElementArray(x,y,ELabel.class,new Dimension(230,18),18,1,style|LCARS.ES_LABEL_E,null);
    eValues = new EElementArray(x+240,y,ELabel.class,new Dimension(432,18),18,1,style|LCARS.ES_LABEL_W,null);
    
    for (int i = 0; i < 20; i++)
    {
      eKeys.add("Key"+i);
      eValues.add("Value"+i);
    }
    eKeys.addToPanel(this);
    eValues.addToPanel(this);
    
    eKeys.animate();
    eValues.animate();
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
    add(new EImage(this, 7, 650, LCARS.ES_STATIC,new ImageMeta.Resource("lcars/resources/images/flare.png")));
  }

  public static void initShell()
  {  
    final int w = 2200;
    final int h = 1200;

    Display display = Display.getDefault();
    Shell shell = new Shell(display);
    shell.setSize(w, h);
    shell.setBackground(display.getSystemColor(SWT.COLOR_BLACK));
    //addComp(shell, SWT.COLOR_CYAN, SWT.COLOR_BLUE, 0);
    //addComp(shell, SWT.COLOR_RED, SWT.COLOR_DARK_RED, 300);

    final Text text = new Text(shell, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
    text.setSize(w-32, (int)(h*.75)-32);
    text.setLayoutData(new FillLayout());
    text.setVisible(true);
    
    final Button clear = new Button(shell, SWT.NONE);
    clear.setLocation(0, text.getSize().y);
    clear.setText("Clear");
    clear.setSize(200,50);
    clear.setVisible(true);
    clear.addMouseListener(new MouseListener()
    {
      @Override
      public void mouseUp(MouseEvent e)
      {
        text.setText("");
      }
      
      @Override
      public void mouseDown(MouseEvent e){}
      
      @Override
      public void mouseDoubleClick(MouseEvent e){}
    });
    
    Consumer<TypedEvent> log = (e) -> {text.append(e.toString()+"\n");};
    
    shell.addTouchListener(new TouchListener()
    {
      @Override
      public void touch(TouchEvent e)
      {
        //if (e.touches[0].state == SWT.TOUCHSTATE_MOVE) return;
        log.accept(e);
      }
    });
    
    shell.addMouseListener(new MouseListener()
    {      
      @Override
      public void mouseUp(MouseEvent e)
      {
        log.accept(e);
      }
      
      @Override
      public void mouseDown(MouseEvent e)
      {
        log.accept(e);
      }
      
      @Override
      public void mouseDoubleClick(MouseEvent e)
      {
        log.accept(e);
      }
    });
    
    shell.addMouseMoveListener(new MouseMoveListener()
    {
      @Override
      public void mouseMove(MouseEvent e)
      {
        log.accept(e);
      }
    });
    
    shell.setTouchEnabled(true);
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

  public static void initBrowserShell()
  {  
    final int w = 1600;
    final int h = 1000;

    Display display = Display.getDefault();
    Shell shell = new Shell(display);
    shell.setSize(w, h);

    Composite composite = new Composite(shell, SWT.NONE);
    composite.setSize(w,h);
    
    Browser browser = new Browser(composite, SWT.NONE);
    String html
    = "<html>\n"
    + "  <head>\n"
    + "    <title>UNAVAILABLE</title>\n"
    + "  </head>\n"
    + "  <body>\n"
    + "    <table width=100% height=100%><tr>\n"
    + "      <td style='vertical-align:middle; text-align:center'>\n"
    + "        fsdjghdjghd\n"
    + "      </td>\n"
    + "    </tr></table>\n"
    + "  </body>\n"
    + "</html>\n";
    browser.setSize(w,h);

    browser.setText(html);
    //browser.setUrl("http://www.w3schools.com/website/");
    
    
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

  private static void initAwtSwtBridge() {
    final Display display = new Display();
    final Shell shell = new Shell(display, SWT.NO_TRIM);
//    shell.setLayout(new FillLayout());
//    shell.setFullScreen(true);
//    Composite composite = new Composite(shell, SWT.DOUBLE_BUFFERED | SWT.EMBEDDED | SWT.NO_BACKGROUND);
//    composite.setBackground(ColorMeta.BLACK.getColor());

    // Create Swings widgets
    shell.setText("LCARS");
    
      // Full-screen mode
    shell.setLayout(new FillLayout());
    shell.setFullScreen(true);
        
    Composite composite = new LcarsComposite(shell, /*SWT.NO_BACKGROUND |*/ SWT.DOUBLE_BUFFERED | SWT.EMBEDDED)
    {
      @Override
      public void paintControl(PaintEvent e)
      {
        // Prepare setup
        GC gc = e.gc;
        gc.setForeground(ColorMeta.BLUE.getColor());
        gc.drawLine(getSize().x, 0, 0, getSize().y);
      }
    };
    composite.setTouchEnabled(true);
    composite.setBackground(ColorMeta.BLACK.getColor());
    //composite.setSize(size.x, size.y);
    composite.setLayout(new FillLayout());
    new Timer().schedule(new TimerTask()
    {
      @Override
      public void run()
      {
        display.asyncExec(() -> {
          Frame frame = SWT_AWT.new_Frame(composite);
          Canvas canvas = new Canvas() {
            private static final long serialVersionUID = -8288002304817297959L;
            @Override
            public void paint(Graphics g) {
              Dimension d = getSize();
              g.setColor(Color.RED);
              g.drawLine(0, 0, d.width, d.height);
            }
          };
          canvas.setBackground(Color.BLACK);
          frame.add(canvas);
        });
      }
    }, 3000,3000);
        
    shell.layout();
    shell.open();
    while (!shell.isDisposed()) {
      if (!display.readAndDispatch())
        display.sleep();
    }
    display.dispose();
  }
  
  public void initEBrowser() {
    EBrowser browser = new EBrowser(0, 0, 1920, 1080, LCARS.ES_NONE);
    browser.addToPanel(this);
    browser.setVisible(true);

    String html
    = "<html>\n"
    + "  <head>\n"
    + "    <title>UNAVAILABLE</title>\n"
    + "  </head>\n"
    + "  <body>\n"
    + "    <table width=100% height=100%><tr>\n"
    + "      <td style='vertical-align:middle; text-align:center'>\n"
    + "jdgjdhdjjggsf\n"
    + "      </td>\n"
    + "    </tr></table>\n"
    + "  </body>\n"
    + "</html>\n";

    browser.setText(html);
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
    if (LCARS.getArg("--testAnimation") != null)
      initEElementArrayAnimation();
    if (LCARS.getArg("--testHelp") != null)
      help();
    if (LCARS.getArg("--testBrowser") != null)
      initEBrowser();
    
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
    //initShell();
    //initBrowserShell();
    //initAwtSwtBridge();
  }
}
