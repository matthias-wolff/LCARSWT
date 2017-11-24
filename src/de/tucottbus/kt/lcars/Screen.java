package de.tucottbus.kt.lcars;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.geom.Area;
import java.rmi.NoSuchObjectException;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.TouchListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Touch;

import de.tucottbus.kt.Root;
import de.tucottbus.kt.lcars.feedback.UserFeedback;
import de.tucottbus.kt.lcars.feedback.UserFeedbackPlayer;
import de.tucottbus.kt.lcars.geometry.rendering.LcarsComposite;
import de.tucottbus.kt.lcars.logging.Log;
import de.tucottbus.kt.lcars.net.LcarsServer;
import de.tucottbus.kt.lcars.net.NetUtils;
import de.tucottbus.kt.lcars.net.panels.ClientPanel;
import de.tucottbus.kt.lcars.swt.ColorMeta;
import de.tucottbus.kt.lcars.swt.SWTResourceManager;
import de.tucottbus.kt.lcars.swt.SwtKeyMapper;
import de.tucottbus.kt.lcars.util.LoadStatistics;
import de.tucottbus.kt.lcars.util.Objectt;

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

  private static final String defaultIcon = "lcars/resources/images/lcars.png";
  
  // -- Fields --

  /**
   * The window, where the screen is shown
   */
  protected Shell shell;

  /**
   * The panel this screen is displaying.
   */
  private IPanel panel;

  /**
   * Serial number of the panel.
   */
  private int panelId = -1;

  /**
   * The cache for the 2D rendering transform.
   */
  protected AtomicBoolean invalid = new AtomicBoolean(false);

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
   * Composite where all LCARS geometries will be drawn
   */
  protected final LcarsComposite composite;

  /**
   * Map of all AWT components added to this swt shell
   */
  protected HashMap<Component, Composite> awtComponents = new HashMap<>(5);

  /**
   * Used to convert {@link org.eclipse.swt.events.KeyEvent} to
   * {@link java.awt.event.KeyEvent}
   */
  private final Component keyEventDummy = new Container();

  // -- Constructors --

  /**
   * Creates a new LCARS screen.
   * 
   * @param swtDisplay
   *          The SWT display on which this screen is run.
   * @param device
   *          the graphics device to display the screen on
   * @param fullScreen
   *          full screen mode
   * @throws ClassNotFoundException
   *           If <code>panelClass</code> is invalid
   */
  public Screen(Display display, boolean fullScreen)
  throws ClassNotFoundException
  {
    loadStat = new LoadStatistics(25);

    // Prepare shell
    shell = new Shell(display, fullScreen ? SWT.NO_TRIM : SWT.SHELL_TRIM);
    shell.setBackground(display.getSystemColor(SWT.COLOR_BLACK));
    shell.setText("LCARS");    
    shell.setImage(SWTResourceManager.getImage(Root.class, defaultIcon));
    shell.setLayout(new FillLayout());
    
    // Instantiate the LCARS composite
    composite = new LcarsComposite(shell)
    {
      @Override
      public void paintControl(PaintEvent e)
      {
        long time = System.nanoTime();
        repostionAwtComponents();
        GC gc = e.gc;
        gc.setTextAntialias(SWT.ON);
        gc.setInterpolation(SWT.LOW);
        gc.setAntialias(SWT.ON);
        super.paintControl(e);
        loadStat.add((int) ((System.nanoTime() - time) / 400000));
      }
    };
    composite.setTouchEnabled(true);
    composite.addTouchListener(this);
    composite.addMouseListener(this);
    composite.addMouseMoveListener(this);
    composite.addKeyListener(this);
    composite.setBackground(ColorMeta.BLACK.getColor());
    //LCARS composite must have absolute layout!
    //composite.setLayout(new FillLayout());
    composite.setVisible(true);

    if (LCARS.getArg("--nomouse") != null)
      composite.setCursor(LCARS.createBlankCursor(display));
    
    // Initialize SWT shell
    if (fullScreen)
    {
      shell.setMaximized(true);
      shell.setFullScreen(true);
    } 
    else
    {
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
    }
    shell.open();

    // Initialize user feedback player
    userFeedbackPlayer = new UserFeedbackPlayer(UserFeedbackPlayer.AUDITORY)
    {
      @Override
      public void writeColor(ColorMeta color)
      {
        // Does not give visual feedback
      }
    };

    // Start screen timer
    screenTimer = new Timer("ScreenTimerTask", true);
    screenTimer.scheduleAtFixedRate(new ScreenTimerTask(), 40, 40);
  }

  // -- Getters and setters --

  @Override
  public boolean isDisposed() throws RemoteException
  {
    return shell.isDisposed();
  }

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
   * Returns the SWT shell s´this screen is running on.
   */
  public Shell getSwtShell()
  {
    return shell;
  }
  
  /**
   * Returns the top composite of the LCARS SWT screen.
   */
  public LcarsComposite getLcarsComposite()
  {
    return composite;
  }

  /**
   * Sets the panel to be displayed at this screen.
   * 
   * @param ipanel
   *          An interface to the new panel.
   */
  public void setPanel(IPanel ipanel)
  {
    if (Objectt.equals(ipanel, this.panel))
      return;

    // Stop current panel (if any)
    if (this.panel != null)
      try
      {
        this.panel.stop();
      } catch (RemoteException e)
      {
        if (!(ipanel instanceof ClientPanel)) 
          // Otherwise connection broke-down -> no error message required
          Log.err("Could not stop the previous panel while setting a new panel.", e);
      }

    this.panel = ipanel;

    if (ipanel == null)
    {
      // Clear screen
      composite.clear();
      return;
    }
    
    // Set and start new panel
    try
    {
      this.panelId = ipanel.serialNo();
    } catch (RemoteException e)
    {
      Log.err("Cannot set panel to screen, because of getting serial number from panel failed.", e);
      return;
    }

    // Set and start new panel
    composite.clear();

    try
    {
      Log.info("Starting panel " + ipanel.getClass().getSimpleName() + "...");
      ipanel.start();
      Log.info("...Panel started");
    } catch (RemoteException e)
    {
      Log.err("...Panel start FAILED", e);
    }
  }

  /**
   * Returns the physical size of the screen in pixels. 
   * @return
   */
  public Dimension getSize()
  {
    Dimension d = new Dimension();
    if (!shell.isDisposed())
      shell.getDisplay().syncExec(() -> 
      {
        org.eclipse.swt.graphics.Point size = shell.getSize();
        d.width = size.x;
        d.height = size.y;
      });
    return d;
  }

  // -- Operations --

  /**
   * Determines if the screen needs to be redrawn.
   */
  protected boolean isScreenInvalid()
  {
    return invalid.get();
  }

  /**
   * Marks the screen as needing to be redrawn.
   */
  public synchronized void invalidateScreen()
  {
    invalid.set(true);
  }

  /**
   * Converts component (LCARS screen) to panel coordinates.
   * 
   * @param pt
   *          The absolute display coordinates.
   * @return The panel coordinates
   * 
   * @see #panelToScreen(Point)
   */
  protected Point screenToPanel(int x, int y)
  {
    return composite.compositeToPanel(new Point(x,y));
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
    return panelToScreen(pt.x,pt.y);
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
    return composite.panelToComposite(new Point(x,y));
  }
  
  // -- SWT-AWT bridge --
  
  /**
   * Adds a {@link java.awt.Component} to the SWT based screen.
   * 
   * @param component 
   *   The {@link java.awt.Component} to add.
   * @param x
   *          The x-coordinate of the top left corner (LCARS panel coordinates). 
   * @param y
   *          The y-coordinate of the top left corner (LCARS panel coordinates).
   * @param w
   *          The width (LCARS panel coordinates).
   * @param h
   *          The height (LCARS panel coordinates).
   */
  public synchronized void addAwtComponent(Component component, int x, int y, int w, int h)
  {
    if (component == null)
      throw new NullPointerException("component");

    if (awtComponents.containsKey(component))
      return;
    
    Rectangle bounds = new Rectangle(x, y, w, h);
    Point tl = panelToScreen(new Point(bounds.x,bounds.y));
    Point br = panelToScreen(new Point(bounds.x+bounds.width,bounds.y+bounds.height));

    LCARS.getDisplay().syncExec(() -> 
    {
      Composite composite = new Composite(this.composite,SWT.DOUBLE_BUFFERED|SWT.EMBEDDED);
      composite.setBackground(ColorMeta.BLACK.getColor());
      composite.setData(bounds); // Remember bounds in LCARS panel coordinates
      composite.setBounds(tl.x,tl.y,br.x-tl.x,br.y-tl.y);
      composite.moveAbove(null);
      FillLayout fl = new FillLayout(SWT.HORIZONTAL);
      fl.marginHeight=0;
      fl.marginHeight=0;
      fl.spacing=0;
      composite.setLayout(fl);

      Frame awtFrame = SWT_AWT.new_Frame(composite);
      awtFrame.setBackground(Color.BLACK);
      awtComponents.put(component, composite);
      BorderLayout bl = new BorderLayout();
      bl.setHgap(0);
      bl.setVgap(0);
      awtFrame.setLayout(bl); 
      awtFrame.add(component,BorderLayout.CENTER);
      
      composite.layout(true);
    });
  }

  /**
   * Removes a {@link java.awt.Component} to the SWT based screen.
   * @param component - the {@link java.awt.Component} to remove
   */
  public void removeAwtComponent(Component component)
  {
    Composite composite = awtComponents.remove(component);
    composite.dispose();
  }
  
  /**
   * Repositions @link java.awt.Component} on the SWT based screen.
   */
  protected void repostionAwtComponents()
  {
    if (awtComponents==null)
      return;

    LCARS.getDisplay().syncExec(() -> 
    {
      awtComponents.forEach((component,composite) ->
      {
        Rectangle pnlBounds = (Rectangle)composite.getData();
        Point tl = panelToScreen(new Point(pnlBounds.x,pnlBounds.y));
        Point br = panelToScreen(new Point(pnlBounds.x+pnlBounds.width,pnlBounds.y+pnlBounds.height));
        org.eclipse.swt.graphics.Rectangle scrBoundsCmps = composite.getBounds();
        Rectangle scrBoundsCmp = component.getBounds();
        if 
        (  
          scrBoundsCmps.x    !=tl.x      || scrBoundsCmps.y     !=tl.y      ||
          scrBoundsCmps.width!=br.x-tl.x || scrBoundsCmps.height!=br.y-tl.y ||
          scrBoundsCmp .x    !=0         || scrBoundsCmp .y     !=0         ||
          scrBoundsCmp .width!=br.x-tl.x || scrBoundsCmp .height!=br.y-tl.y
        )
        {
          composite.setBounds(tl.x,tl.y,br.x-tl.x,br.y-tl.y);
          composite.layout(true);
        }
      });
    });
  }
  
  // -- Implementation of the IScreen interface --

  @Override
  public void setArea(Area area) throws RemoteException
  {
    Rectangle bnds = area.getBounds();
    shell.setBounds(bnds.x, bnds.y, bnds.width, bnds.height);
  }

  @Override
  public Area getArea()
  {
    Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
    return new Area(new Rectangle(0, 0, size.width, size.height));
  }

  @Override
  public String getHostName()
  {
    return NetUtils.getHostName();
  }

  @Override
  public void setPanel(String className) throws ClassNotFoundException
  {
    setPanel(Panel.createPanel(className, this));
  }
  
  @Override
  public void setPanelId(int panelId) {
    this.panelId = panelId;
  }

  @Override
  public IPanel getPanel()
  {
    return panel;
  }

  @Override
  public void update(PanelData data, boolean incremental)
  {
    if (panel != null && data.panelId != panelId)
      return;

    // TODO: remove incremental
    synchronized (composite)
    {
      composite.applyUpdate(data, incremental);
    }
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
    try
    {
      if (panel!=null)
      {
        panel.stop();
        panel = null;
      }
    } catch (NoSuchObjectException e) { 
      // Because RMI has already been shut down -> ignore
    } catch (Exception e)
    {
      Log.err("Failed to stop panel.",e);
    }
    try
    {
      screenTimer.cancel();
      screenTimer.purge();
      screenTimer=null;
    } catch (Exception e)
    {
      Log.err("Failed to stop screen timer.",e);
    }
    try
    {
      userFeedbackPlayer.cancel();
      userFeedbackPlayer = null;
    } catch (Exception e)
    {
      Log.err("Failed to user feedback player.",e);
    }
    LcarsServer.shutDown();
    getSwtShell().getDisplay().asyncExec(shell::dispose);
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
    Point pt = screenToPanel(e.x, e.y);
    return new TouchEvent[]
      { new TouchEvent(eventType, pt, true, e.count==0) };
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
          touchCount = 0;
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
  public void keyPressed(KeyEvent e)
  {
    if (panel != null)
      try
      {
        int keycode = SwtKeyMapper.swt2AwtKeycode(e);
        //FIXME: wrong keyCode mapping from swt to awt, also with de.tucottbus.kt.lcars.swt.SwtKeyMapper
        java.awt.event.KeyEvent ke = new java.awt.event.KeyEvent(keyEventDummy,
            java.awt.event.KeyEvent.KEY_PRESSED, e.time, SwtKeyMapper.transformStatemask(e),
            keycode, e.character);
        panel.processKeyEvent(ke);
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
        //FIXME: see at keyPressed(KeyEvent)
        int keycode = SwtKeyMapper.swt2AwtKeycode(e);
        panel.processKeyEvent(new java.awt.event.KeyEvent(keyEventDummy,
            java.awt.event.KeyEvent.KEY_RELEASED, e.time, SwtKeyMapper.transformStatemask(e),
            keycode, e.character));
      } catch (RemoteException e1)
      {
        Log.err("Error while transmission of a key released event" + e, e1);
      }
  }

  // -- Implementation of the TouchListener interface --

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

      //Control ctrl = (Control) e.widget;

      touches = new TouchEvent[e.touches.length];
      int i = 0;

      for (Touch touch : e.touches)
      {
        //org.eclipse.swt.graphics.Point absPos = ctrl.toDisplay(touch.x,touch.y);
        org.eclipse.swt.graphics.Point pos = composite.toControl(touch.x,touch.y);
        Point pt = screenToPanel(pos.x, pos.y);
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
        if (invalid.getAndSet(false) && !shell.isDisposed())
          shell.getDisplay().syncExec(() -> 
          {
            composite.redraw();
          });
      }

      // Every second...
      if (ctr % 25 == 0)
      {
        if (!isScreenInvalid() && loadStat.getEventCount()==0 && !shell.isDisposed())
          shell.getDisplay().syncExec(() -> 
          {
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

}

// EOF
