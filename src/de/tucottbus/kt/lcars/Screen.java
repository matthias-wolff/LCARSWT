package de.tucottbus.kt.lcars;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.rmi.RemoteException;
import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.TouchEvent;
import org.eclipse.swt.events.TouchListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Touch;

import de.tucottbus.kt.lcars.feedback.UserFeedback;
import de.tucottbus.kt.lcars.feedback.UserFeedbackPlayer;
import de.tucottbus.kt.lcars.logging.Log;
import de.tucottbus.kt.lcars.swt.AwtSwt;
import de.tucottbus.kt.lcars.swt.PanelDataComposite;
import de.tucottbus.kt.lcars.swt.SwtColor;
import de.tucottbus.kt.lcars.util.LoadStatistics;

/**
 * An LCARS screen. A screen is the representation of a physical unit with a
 * display, a touch panel and/or mouse and keyboard, and a loudspeaker. A screen
 * provides a graphical, haptic and acoustic user interface for an
 * {@linkplain Panel LCARS panel}. Screen and panel do not necessarily run on
 * the same virtual or physical machine.
 * 
 * @see Panel
 * @see IScreen
 * @author Matthias Wolff
 */
public class Screen
    implements IScreen, MouseListener, MouseMoveListener, TouchListener, KeyListener
{
  // -- Constants --

  private static final int preferedWidth = 950;
  private static final int preferedHeight = 560;

  // -- Fields --

  /**
   * The panel this screen is displaying.
   */
  protected IPanel panel;

  /**
   * Full screen mode flag.
   */
  protected boolean fullScreenMode;

  /**
   * The cache for the 2D rendering transform.
   */
  protected AffineTransform renderingTransform;

  /**
   * The screen timer. Blocks the screen saver and keeps the
   * frames-per-second-statistics.
   */
  protected Timer screenTimer;

  /**
   * The user feedback player.
   */
  protected UserFeedbackPlayer userFeedbackPlayer;

  /**
   * The screen rendering load statistics.
   */
  protected LoadStatistics loadStat;
  
  /**
   * 
   */
  //protected AdvGraphics2D g2dWrapper;

  protected Shell shell;
  
  protected PanelDataComposite composite;

  private java.awt.Frame awtFrame;
  
  private int mouseButton = 0;
  
  //private boolean running = true;

  // -- Constructors --

  /**
   * Creates a new LCARS screen.
   * 
   * @param swtDisplay
   *          The SWT display on which this screen is run.
   * @param device
   *          the graphics device to display the screen on
   * @param panelClass
   *          the class name of the LCARS {@link Panel} to display on the screen
   * @param fullScreen
   *          full screen mode
   * @throws ClassNotFoundException
   *           If <code>panelClass</code> is invalid
   */
  public Screen(Display display, String panelClass, boolean fullScreen)
      throws ClassNotFoundException
  {    
    shell = new Shell(display, SWT.NO_TRIM);
    // shell.forceActive();
    //shell.forceFocus(); //Keyboard focus
    loadStat = new LoadStatistics(25);
    // Create Swings widgets
    shell.setText("LCARS");
    // TODO: setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    // initContentPane();

    setPanel(panelClass);
    fullScreenMode = fullScreen;// && device.isFullScreenSupported();
    // TODO: setUndecorated(fullScreen);
    // TODO: setResizable(!fullScreen);
    
    final Color black = display.getSystemColor(SWT.COLOR_BLACK);
    //shell.setBackground(black);

    if (fullScreenMode && !"maximized".equals(LCARS.getArg("--mode=")))
    {
      // Full-screen mode
      shell.setFullScreen(true);
      // setAlwaysOnTop(fullScreen);
      // TODO: validate();
      // TODO: createBufferStrategy(1);
    } else
    {
      // Windowed mode
      shell.setSize(preferedWidth, preferedHeight);
      // TODO: setPreferredSize(new Dimension(950,560));
      int nXPos = 200;
      try
      {
        nXPos = Integer.parseInt(LCARS.getArg("--xpos="));
      } catch (NumberFormatException e)
      {
      }
      ;
      shell.setLocation(nXPos, 200);
      shell.pack();
      shell.setVisible(true);
      if (fullScreen)
        shell.setMaximized(true);
      // TODO: setExtendedState(JFrame.MAXIMIZED_BOTH);

      // TODO: sometimes an error occurs that the buffer has not been created
      // TODO: createBufferStrategy(2);
    }

    if (LCARS.getArg("--nomouse")!=null)
      composite.setCursor(LCARS.createBlankCursor(display));
    composite = new PanelDataComposite(shell, SWT.NONE /*SWT.NO_BACKGROUND | SWT.DOUBLE_BUFFERED | SWT.EMBEDDED*/)
    {
      
      @Override
      public void prePaint(GC gc)
      {
        // Prepare setup
        gc.setTransform(AwtSwt.toSwtTransform(getTransform(), gc.getDevice()));
        gc.setTextAntialias(SWT.ON);
        gc.setInterpolation(SWT.LOW);
        
        //TODO: gc.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        //TODO: gc.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
        //TODO: gc.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED);
        //TODO: gc.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
//        renderer.setSize(shell.getSize());
//        renderer.paint2D(gc);
      }
      
      @Override
      public void postPaint(GC gc) {}
    };
    composite.setBackground(black);
    composite.setSize(shell.getSize());
    composite.setLayout(new FillLayout());
    
    //TODO:
    //awtFrame = SWT_AWT.new_Frame(composite);
    //awtFrame.setSize(getSize());
      
    // TODO: check awtFrame is in embedded full screen mode

    composite.setTouchEnabled(true);
    composite.addTouchListener(this);
    composite.addMouseListener(this);
    composite.addMouseMoveListener(this);

    composite.setEnabled(true);
    
    // The screen timer
    screenTimer = new Timer("ScreenTimerTask", true);
    screenTimer.scheduleAtFixedRate(new ScreenTimerTask(), 40, 40);

    // The user feedback player
    userFeedbackPlayer = new UserFeedbackPlayer(UserFeedbackPlayer.AUDITORY)
    {
      @Override
      public void writeColor(SwtColor color)
      {
        // Does not give visual feedback
      }
    };

    // Window event handlers
    // addWindowStateListener(new WindowStateListener()
    // {
    // @Override
    // public void windowStateChanged(WindowEvent e)
    // {
    // }
    // });
    // addWindowListener(new WindowListener(){
    // public void windowActivated(WindowEvent e){}
    // public void windowClosed(WindowEvent e){}
    // public void windowClosing(WindowEvent e){}
    // public void windowDeactivated(WindowEvent e){}
    // public void windowDeiconified(WindowEvent e){}
    // public void windowIconified(WindowEvent e){}
    // public void windowOpened(WindowEvent e){}
    // });
    // addComponentListener(new ComponentListener()
    // {
    // @Override
    // public void componentShown(ComponentEvent e)
    // {
    // invalidateScreen();
    // }
    //
    // @Override
    // public void componentResized(ComponentEvent e)
    // {
    // invalidateScreen();
    // }
    //
    // @Override
    // public void componentMoved(ComponentEvent e){}
    //
    // @Override
    // public void componentHidden(ComponentEvent e){}
    // });

    // Mouse handlers
    // TODO: canvas.addMouseListener(this);
    // getContentPane().addMouseListener(this);
    // getContentPane().addMouseMotionListener(this);

    // Keyboard event handlers
    // addKeyListener(this);
    //canvas.pack();
    //shell.pack();
    shell.open();

//    while (running)
//      if (!display.readAndDispatch())
//        display.sleep();
//    //display.dispose();
//    shell.dispose();
    //renderer.dispose();
//    System.exit(0);
  }

  // -- Getters and setters --

  /**
   * Returns the actual {@link Screen} for an {@linkplain IScreen LCARS screen
   * interface}. This is only possible if the screen is run by the virtual
   * machine on which this method is invoked. If <code>screen</code> refers to a
   * remote screen, the method will throw a {@link ClassCastException}.
   * 
   * @param screen
   *          The screen interface.
   * @return The actual screen (only if it is local).
   * @throws ClassCastException
   *           If the actual screen is run by another virtual machine.
   */
  public static Screen getLocal(IScreen screen)
  {
    return (Screen) screen;
  }

  /**
   * Returns the SWT display this screen is running on.
   */
  public Display getSwtDisplay()
  {
    return Display.getDefault();
  }

  // -- 2D rendering --

  /**
   * Determines if the screen needs to be redrawn.
   */
  protected boolean isScreenInvalid()
  {
    return renderingTransform == null;
  }

  /**
   * Marks the screen as needing to be redrawn.
   */
  public synchronized void invalidateScreen()
  {
    renderingTransform = null;
  }

  /**
   * Returns the rendering transform
   */
  protected synchronized AffineTransform getTransform()
  {
    if (this.renderingTransform != null)
      return this.renderingTransform;

    org.eclipse.swt.graphics.Rectangle dp = composite.getBounds();
    if (dp == null)
      return new AffineTransform();

    Dimension ds = getSize();
    renderingTransform = new AffineTransform();
    double sx = (double) ds.width / (double) dp.width;
    double sy = (double) ds.height / (double) dp.height;
    if (sy < sx)
      sx = sy;
    renderingTransform.scale(sx, sx);
    int x = (int) ((ds.getWidth() - sx * dp.width) / 2);
    int y = (int) ((ds.getHeight() - sx * dp.height) / 2);
    renderingTransform.translate(x / sx, y / sx);
    return renderingTransform;
  }

  /**
   * Converts component (LCARS screen) to panel coordinates.
   * 
   * @param pt
   *          The AWT component coordinates.
   * @return The panel coordinates
   * 
   * @see #panelToComponent(Point)
   */
  protected Point componentToPanel(Point pt)
  {
    Point2D pt2d;
    try
    {
      pt2d = getTransform().inverseTransform(pt, null);
      return new Point((int) Math.round(pt2d.getX()),
          (int) Math.round(pt2d.getY()));
    } catch (NoninvertibleTransformException e)
    {
      // Cannot happen
      e.printStackTrace();
      return null;
    }
  }

  /**
   * Converts panel to component (LCARS screen) coordinates.
   * 
   * @param pt
   *          The LCARS panel coordinates.
   * @return The AWT component coordinates.
   * 
   * @see #componentToPanel(Point)
   */
  public Point panelToComponent(Point pt)
  {
    Point2D pt2d = getTransform().transform(pt, null);
    return new Point((int) Math.round(pt2d.getX()),
        (int) Math.round(pt2d.getY()));
  }

  // -- Getters and setters --

  /**
   * Sets the panel to be displayed at this screen.
   * 
   * @param ipanel
   *          An interface to the new panel.
   */
  public void setPanel(IPanel ipanel)
  {
    if (ipanel == null && this.panel == null)
      return;
    if (ipanel != null && ipanel.equals(this.panel))
      return;

    // Stop current panel (if any)
    if (this.panel != null)
      try
      {
        this.panel.stop();
      } catch (RemoteException e)
      {
      }

    // Set and start new panel
    this.panel = ipanel;
     if(composite != null)
       composite.clear();

    if (this.panel != null)
      try
      {
        Log.info(
            "Starting panel " + this.panel.getClass().getSimpleName() + "...");
        this.panel.start();
        Log.info("...Panel started");
      } catch (RemoteException e)
      {
        Log.err("...Panel start FAILED", e);
      }
  }

  // -- Implementation of the IScreen interface --

  @Override
  public Area getArea()
  {
    int w = Toolkit.getDefaultToolkit().getScreenSize().width;
    int h = Toolkit.getDefaultToolkit().getScreenSize().height;
    return new Area(new Rectangle(0, 0, w, h));
  }

  @Override
  public String getHostName()
  {
    return LCARS.getHostName();
  }

  @Override
  public void setPanel(String className) throws ClassNotFoundException
  {
    setPanel(Panel.createPanel(className, this));
  }

  @Override
  public IPanel getPanel()
  {
    return panel;
  }

  @Override
  public void update(PanelData data, boolean incremental)
  {
    //TODO: remove incremental
    composite.applyUpdate(data);
    invalidateScreen();      
  }

  @Override
  public void userFeedback(UserFeedback.Type type)
  {
    UserFeedback signal = UserFeedback.getInstance(type);
    userFeedbackPlayer.play(signal);
  }

  @Override
  public LoadStatistics getLoadStatistics()
  {
    return loadStat;
  }

  @Override
  public void exit()
  {
    //running = false;
    System.exit(0);
  }

  // -- Implementation of the MouseInputListener interface --

  @Override
  public void mouseDoubleClick(MouseEvent e)
  {
    // ignored   
  }

  @Override
  public void mouseDown(MouseEvent e)
  {
    if(mouseButton != 0) {
      mouseButton = 0;
      return;
    }
    
    mouseButton = e.button;
    Point pt = componentToPanel(new Point(e.x, e.y));
    de.tucottbus.kt.lcars.TouchEvent te = new de.tucottbus.kt.lcars.TouchEvent();
    te.type = de.tucottbus.kt.lcars.TouchEvent.DOWN;
    te.x = pt.x;
    te.y = pt.y;
    try
    {
      panel.processTouchEvent(te);
    } catch (RemoteException e1)
    {
      Log.err("Mouse down ignored", e1);
    }
  }

  @Override
  public void mouseUp(MouseEvent e)
  {
    if(mouseButton != e.button)
      return;
    mouseButton = 0;
    Point pt = componentToPanel(new Point(e.x, e.y));
    de.tucottbus.kt.lcars.TouchEvent te = new de.tucottbus.kt.lcars.TouchEvent();
    te.type = de.tucottbus.kt.lcars.TouchEvent.UP;
    te.x = pt.x;
    te.y = pt.y;
    try
    {
      panel.processTouchEvent(te);
    } catch (RemoteException e1)
    {
      Log.err("Mouse up ignored", e1);
    }    
  }
  
  @Override
  public void mouseMove(MouseEvent e)
  {
    if(mouseButton == 0)
      return;    
    Point pt = componentToPanel(new Point(e.x, e.y));
    de.tucottbus.kt.lcars.TouchEvent te = new de.tucottbus.kt.lcars.TouchEvent();
    te.type = de.tucottbus.kt.lcars.TouchEvent.DRAG;
    te.x = pt.x;
    te.y = pt.y;
    try
    {
      panel.processTouchEvent(te);
    } catch (RemoteException e1)
    {
      Log.err("Mouse drag ignored", e1);
    }    
  }

  // -- Implementation of the KeyListener interface --

  @Override
  public void keyTyped(KeyEvent e)
  {
    if (panel != null)
      try
      {
        panel.processKeyEvent(e);
      } catch (RemoteException e1)
      {
      }
  }

  @Override
  public void keyPressed(KeyEvent e)
  {
    if (panel != null)
      try
      {
        panel.processKeyEvent(e);
      } catch (RemoteException e1)
      {
      }
  }

  @Override
  public void keyReleased(KeyEvent e)
  {
    if (panel != null)
      try
      {
        panel.processKeyEvent(e);
      } catch (RemoteException e1)
      {
      }
  }

  @Override
  public void touch(TouchEvent e)
  {
    if (e.touches.length <= 0)
      return;
    Log.info(e.touches[0].toString());
    Touch pt = e.touches[0];

    de.tucottbus.kt.lcars.TouchEvent te = new de.tucottbus.kt.lcars.TouchEvent();
    te.x = pt.x;
    te.y = pt.y;

    switch (pt.state)
    {
    case SWT.TOUCHSTATE_DOWN:
      te.type = de.tucottbus.kt.lcars.TouchEvent.DOWN;
      break;
    case SWT.TOUCHSTATE_MOVE:
      te.type = de.tucottbus.kt.lcars.TouchEvent.DRAG;
      break;
    case SWT.TOUCHSTATE_UP:
      te.type = de.tucottbus.kt.lcars.TouchEvent.UP;
      break;

    default:
      return;
    }
    try
    {
      panel.processTouchEvent(te);
    } catch (RemoteException e1)
    {
    }
  }

  public void add(Component component)
  {
    awtFrame.add(component);
  }

  public void remove(Component component)
  {
    awtFrame.remove(component);
  }

  public Dimension getSize()
  {
    Dimension d = new Dimension();
    invoke(() -> {
      org.eclipse.swt.graphics.Point size = shell.getSize();
      d.width = size.x;
      d.height = size.y;
    });
    return d;
  }

  private void invoke(Runnable action)
  {
    final Display display = shell.getDisplay();
    display.syncExec(action);
  }

  // -- Nested classes --

  /**
   * The screen timer task. Blocks the screen saver, does the regular repainting
   * and keeps the 2D rendering load statistics.
   */
  protected final class ScreenTimerTask extends TimerTask
  {
    private long ctr;

    @Override
    public void run()
    {
      // Every 40 milliseconds...
      {
        if (isScreenInvalid())
          invoke(() -> {
            composite.redraw();
            //shell.redraw();
            //awtFrame.repaint();
          });
      }

      // Every second...
      if (ctr % 25 == 0)
      {
        if (!isScreenInvalid() && loadStat.getEventCount() == 0)
          invoke(() -> {
            composite.redraw();
            //shell.redraw();
            //awtFrame.repaint();

          });
        loadStat.period();
      }

      // Every 60 seconds...
      if (ctr % 1500 == 0)
        try
        {
          Robot r = new Robot();
          Point m = MouseInfo.getPointerInfo().getLocation();
          r.mouseMove(m.x - 1, m.y);
          r.delay(10);
          m = MouseInfo.getPointerInfo().getLocation();
          r.mouseMove(m.x + 1, m.y);
        } catch (Exception e)
        {
          Log.err("Error in timer while do a \"every second\" update.", e);
        }
      ctr++;
    }
  }

  @Override
  public void setArea(Area area) throws RemoteException
  {
    Rectangle bnds = area.getBounds();
    shell.setBounds(bnds.x, bnds.y, bnds.width, bnds.height);    
  }  
}

// EOF
