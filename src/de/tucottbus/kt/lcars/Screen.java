package de.tucottbus.kt.lcars;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.TouchListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Touch;

import de.tucottbus.kt.lcars.feedback.UserFeedback;
import de.tucottbus.kt.lcars.feedback.UserFeedbackPlayer;
import de.tucottbus.kt.lcars.geometry.rendering.LcarsComposite;
import de.tucottbus.kt.lcars.logging.Log;
import de.tucottbus.kt.lcars.swt.ColorMeta;
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
public class Screen implements IScreen, MouseListener, MouseMoveListener,
    TouchListener, KeyListener
{
  // -- Constants --

  private static final int preferedWidth = 950;
  private static final int preferedHeight = 560;

  // -- Fields --

  /**
   * The window, where the screen is shown
   */
  protected Shell shell;

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
  protected boolean invalid;

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
   * The list contains done touch events to ignore the corresponding mouse
   * event.
   */
  private Integer touchCount = 0;

  /**
   * Composite where all lcars geometries will be drawn
   */
  protected final LcarsComposite composite;

  /**
   * Map of all awt components added to this swt shell
   */
  protected HashMap<Component, Frame> awtComponents = new HashMap<>(5);

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

    loadStat = new LoadStatistics(25);
    // Create Swings widgets
    shell.setText("LCARS");
    // TODO: setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    final Screen _this = this;

    fullScreenMode = fullScreen;// && device.isFullScreenSupported();
    // TODO: setUndecorated(fullScreen);
    // TODO: setResizable(!fullScreen);

    if (fullScreenMode && !"maximized".equals(LCARS.getArg("--mode=")))
    {
      // Full-screen mode
      shell.setLayout(new FillLayout());
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
      final String argPfx = "--xpos=";
      String arg = LCARS.getArg(argPfx);
      if (arg != null)
        try
        {
          nXPos = Integer.parseInt(arg);
        } catch (NumberFormatException e)
        {
          Log.warn("Cannot parse lcars argument for \"" + argPfx + arg + "\"");
        }

      shell.setLocation(nXPos, 200);
      shell.pack();
      shell.setVisible(true);
      if (fullScreen)
        shell.setMaximized(true);

      // TODO: sometimes an error occurs that the buffer has not been created
      // TODO: createBufferStrategy(2);
    }

    final Dimension size = getSize();
    final int w = size.width;
    final int h = size.height;
    composite = new LcarsComposite(shell,
        /* SWT.NO_BACKGROUND | */ SWT.DOUBLE_BUFFERED | SWT.EMBEDDED)
    {
      @Override
      public void paintControl(PaintEvent e)
      {
        long time = System.nanoTime();

        // Prepare setup
        GC gc = e.gc;
        gc.setTextAntialias(SWT.ON);
        gc.setInterpolation(SWT.LOW);
        gc.setAntialias(SWT.ON);

        // TODO: gc.setRenderingHint(RenderingHints.KEY_RENDERING,
        // RenderingHints.VALUE_RENDER_QUALITY);
        // TODO: gc.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,
        // RenderingHints.VALUE_STROKE_NORMALIZE);
        // TODO: gc.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION,
        // RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED);
        // TODO:
        // gc.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
        // 1.0f));
        super.paintControl(e);
        loadStat.add((int) ((System.nanoTime() - time) / 400000));
      }
    };
    composite.setTouchEnabled(true);
    composite.addTouchListener(_this);
    composite.addMouseListener(_this);
    composite.addMouseMoveListener(_this);
    composite.setBackground(ColorMeta.BLACK.getColor());
    composite.setSize(w, h);
    composite.setLayout(new FillLayout());
    composite.setVisible(true);

    if (LCARS.getArg("--nomouse") != null)
      composite.setCursor(LCARS.createBlankCursor(display));

    // The user feedback player
    userFeedbackPlayer = new UserFeedbackPlayer(UserFeedbackPlayer.AUDITORY)
    {
      @Override
      public void writeColor(ColorMeta color)
      {
        // Does not give visual feedback
      }
    };

    setPanel(panelClass);

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

    shell.open();

    // The screen timer
    screenTimer = new Timer("ScreenTimerTask", true);
    screenTimer.scheduleAtFixedRate(new ScreenTimerTask(), 40, 40);
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
    return shell.getDisplay();// Display.getDefault();
  }

  /**
   * Returns the top composite of the LCARS SWT screen.
   */
  public LcarsComposite getLcarsComposite()
  {
    return composite;
  }

  // -- 2D rendering --

  /**
   * Determines if the screen needs to be redrawn.
   */
  protected boolean isScreenInvalid()
  {
    return invalid;
  }

  /**
   * Marks the screen as needing to be redrawn.
   */
  public synchronized void invalidateScreen()
  {
    invalid = true;
  }

  /**
   * Converts component (LCARS screen) to panel coordinates.
   * 
   * @param pt
   *          The AWT component coordinates.
   * @return The panel coordinates
   * 
   * @see #panelToScreen(Point)
   */
  protected Point screenToPanel(int x, int y)
  {
    Point2D.Float scale = composite.getScale();
    return new Point((int) (x / scale.x + .5f), (int) (y / scale.y + .5f));
  }

  /**
   * Converts panel to component (LCARS screen) coordinates.out
   * 
   * @param pt
   *          The LCARS panel coordinates.
   * @return The AWT component coordinates.
   * 
   * @see #componentToPanel(Point)
   */
  public Point panelToScreen(Point pt)
  {
    Point2D.Float scale = composite.getScale();
    return new Point((int) (pt.x * scale.x + .5f),
        (int) (pt.y * scale.y + .5f));
  }

  /**
   * Converts panel to component (LCARS screen) coordinates.out
   * 
   * @param x
   *          The LCARS panel x-coordinate.
   * @param y
   *          The LCARS panel y-coordinate.
   * @return The AWT component coordinates.
   * 
   * @see #componentToPanel(Point)
   */
  public Point panelToScreen(int x, int y)
  {
    Point2D.Float scale = composite.getScale();
    return new Point((int) (x * scale.x + .5f),
        (int) (y * scale.y + .5f));
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
        Log.err("Could not stop the previous panel while setting a new panel.", e);
      }

    // Set and start new panel
    if (this.panel != null && composite != null)
      composite.clear();
    this.panel = ipanel;

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
    Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
    return new Area(new Rectangle(0, 0, size.width, size.height));
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
    // TODO: remove incremental
    composite.applyUpdate(data, incremental);
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
    shell.dispose();
    System.exit(0);
  }

  // -- Implementation of the MouseInputListener interface --
  protected void processTouchEvents(TouchEvent[] touchEvent)
  {
    if (touchEvent == null)
    {
      Log.warn("Touch event ignored");
      return;
    }
    try
    {
      if (panel != null)
        panel.processTouchEvents(touchEvent);
    } catch (RemoteException e)
    {
      Log.err("Error while transmission of touch events" + touchEvent, e);
    }
  }

  protected TouchEvent[] toTouchEvents(MouseEvent e, int eventType)
  {
    if (!(e.widget instanceof Control))
      return null;
    org.eclipse.swt.graphics.Point absPos = ((Control) e.widget).toDisplay(e.x,
        e.y);
    Point pt = screenToPanel(absPos.x, absPos.y);
    return new TouchEvent[]
    { new TouchEvent(eventType, pt, true, true) };
  }

  @Override
  public void mouseDoubleClick(MouseEvent e)
  {
  }

  @Override
  public void mouseDown(MouseEvent e)
  {
    synchronized (touchCount)
    {
      if (touchCount > 0)
        return;
      touchCount--;
    }
    processTouchEvents(toTouchEvents(e, TouchEvent.DOWN));
  }

  @Override
  public void mouseUp(MouseEvent e)
  {
    synchronized (touchCount)
    {
      if (touchCount < 0)
        touchCount++;
      else
      {
        if (e.count == 1)
          touchCount--;
        return;
      }
    }
    processTouchEvents(toTouchEvents(e, TouchEvent.UP));
  }

  @Override
  public void mouseMove(MouseEvent e)
  {
    if (touchCount < 0)
      processTouchEvents(toTouchEvents(e, TouchEvent.DRAG));
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
        Log.err("Error while transmission of a key typed event" + e, e1);
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
        Log.err("Error while transmission of a key pressed event" + e, e1);
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
        Log.err("Error while transmission of a key released event" + e, e1);
      }
  }

  @Override
  public void touch(org.eclipse.swt.events.TouchEvent e)
  {
    TouchEvent[] touches;
    synchronized (touchCount)
    {
      if (touchCount < 0)
        return;

      if (!(e.widget instanceof Control))
        return;

      Control ctrl = (Control) e.widget;

      touches = new TouchEvent[e.touches.length];
      int i = 0;

      for (Touch touch : e.touches)
      {
        org.eclipse.swt.graphics.Point absPos = ctrl.toDisplay(touch.x, touch.y);
        Point pt = screenToPanel(absPos.x, absPos.y);
        int eventType;
        switch (touch.state)
        {
        case SWT.TOUCHSTATE_DOWN:
          eventType = de.tucottbus.kt.lcars.TouchEvent.DOWN;
          touchCount++;
          break;
        case SWT.TOUCHSTATE_MOVE:
          eventType = de.tucottbus.kt.lcars.TouchEvent.DRAG;
          break;
        case SWT.TOUCHSTATE_UP:
          eventType = de.tucottbus.kt.lcars.TouchEvent.UP;
          break;
        default:
          return;
        }
        touches[i++] = new de.tucottbus.kt.lcars.TouchEvent(eventType, pt,
            false, touch.primary);
        // if (touch.primary)
        // mouseIgnores.add(touch);
      }
    }
    processTouchEvents(touches);
  }

  public synchronized void add(Component component)
  {
    if (component == null)
      throw new NullPointerException("component");

    if (awtComponents.containsKey(component))
      return;
    LCARS.getDisplay().syncExec(() -> {
      // TODO: check awtFrame is in embedded full screen mode
      if (awtComponents.containsKey(component))
        return;
      Frame awtFrame = SWT_AWT.new_Frame(composite);
      awtComponents.put(component, awtFrame);
      awtFrame.setBounds(component.getBounds());
      shell.redraw();
    });
  }

  public void remove(Component component)
  {
    Frame frame = awtComponents.remove(component);
    frame.dispose();
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
    if (shell.isDisposed())
      return;
    shell.getDisplay().syncExec(action);
  }

  // -- Nested classes --

  /**
   * The screen timer task. Blocks the screen saver, does the regular repainting
   * and keeps the 2D rendering load statistics.
   */
  protected final class ScreenTimerTask extends TimerTask
  {
    private long ctr = 25;

    @Override
    public void run()
    {
      // Every 40 milliseconds...
      {
        if (isScreenInvalid())
        {
          invalid = false;
          invoke(() -> {
            composite.redraw();
          });
        }
      }

      // Every second...
      if (ctr % 25 == 0)
      {
        if (!isScreenInvalid() && loadStat.getEventCount() == 0)
          invoke(() -> {
            composite.redraw();
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
  
  @Override
  public boolean isDisposed()
  {
    return shell.isDisposed();
  }
}

// EOF
