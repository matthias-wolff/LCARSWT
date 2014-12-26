package de.tucottbus.kt.lcars.ge;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.BrowserFunction;
import org.eclipse.swt.browser.ProgressEvent;
import org.eclipse.swt.browser.ProgressListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.widgets.Shell;

import de.tucottbus.kt.lcars.LCARS;
import de.tucottbus.kt.lcars.Panel;
import de.tucottbus.kt.lcars.Screen;
import de.tucottbus.kt.lcars.ge.orbits.GEOrbit;
import de.tucottbus.kt.lcars.ge.orbits.StdOrbit;

/**
 * Wrapper class embedding Google Earth on an LCARS panel. The class places
 * the Google Earth plugin in a {@link Browser} and builds an
 * {@link SWT_AWT}-bridge which embeds the browser in the panel.
 * @deprecated Because Goggle Earth API has been deprecated.
 */
// TODO: Subclass EBrowser?
public class GE
{
  private Screen              screen;
  private Shell               swtShell;
  private Rectangle           bounds;
  private Canvas              canvas;
  private String              canvasText;
  private Browser             browser;
  private String              world;
  private float               cameraSpeed;
  private boolean             grid;
  private boolean             clouds;
  private boolean             sun;
  private String              geHtml;
  private ComponentListener   scl;
  private WindowListener      swl;
  
  /**
   * Flag indicating that the Google Earth plug-in has completed initialization.
   */
  private boolean pluginUp;
  
  /**
   * LCARS camera (target).
   */
  private GECamera camTarget;
  
  /**
   * Google Earth camera (actual).
   */
  protected GECamera camActual;

  /**
   * The current camera orbit, can be <code>null</code>.
   */
  private GEOrbit orbit;

  /**
   * The orbiting timer.
   */
  private Timer orbitTimer;

  // -- Constants --
  
  /**
   * World identifier for earth.
   * @see #MOON
   * @see #MARS
   * @see #SKY
   */
  public static final String EARTH = "EARTH";

  /**
   * World identifier for the moon.
   * @see #EARTH
   * @see #MARS
   * @see #SKY
   */
  public static final String MOON = "MOON";
  
  /**
   * World identifier for the mars.
   * @see #EARTH
   * @see #MOON
   * @see #SKY
   */
  public static final String MARS = "MARS";
  
  /**
   * World identifier for the sky.
   * @see #EARTH
   * @see #MOON
   * @see #MARS
   */
  public static final String SKY = "SKY";

  /**
   * Instant movement of camera.
   */
  public static final float SPEED_TELEPORT = 10;
  
  /**
   * Automatic choice of camera speed.
   */
  public static final float SPEED_AUTO = -1;
  
  /**
   * The minimal fly-to speed.
   */
  private static final float SPEED_MIN = 0; 
  
  /**
   * The maximal fly-to speed.
   */
  private static final float SPEED_MAX = 5; 
  
  // -- HTML and Javascript --
  
  private static final String GE_HTML = "de/tucottbus/kt/lcars/ge/GE.html";
  
  private static final String JS_FLYTO = "flyTo(%f,%f,%f,%f,%f,%f,%f);";
  
  private static final String JS_CLOUDS =
    "try { clouds.setVisibility(%b); } catch (e) {}";
  
  private static final String JS_GRID =
    "try { ge.getOptions().setGridVisibility(%b); } catch (e) {}";
  
  private static final String JS_SUN =
    "try { ge.getSun().setVisibility(%b); } catch (e) {}";
  
  // -- Life cycle --
  
  /**
   * Creates new Google Earth wrapper. Applications must call {@link #start()}
   * after creating the instance to display and activate Google Earth.
   * 
   * @param panel the LCARS panel to place the wrapper on
   * @param x     x-coordinate of the top left corner (in LCARS panel units)
   * @param y     y-coordinate of the top left corner (in LCARS panel units)
   * @param w     width (in LCARS panel units)
   * @param h     height (in LCARS panel units)
   */
  public GE(Panel panel, int x, int y, int w, int h)
  {
    try
    {
      //this.panel     = panel;
      this.screen    = Screen.getLocal(panel.getScreen());
      this.bounds    = new Rectangle(x,y,w,h);
      this.world     = GE.EARTH;
      this.camTarget = new GECamera(GE.EARTH);
      this.camActual = null;
      this.pluginUp  = false;
      
      // Load Google Earth page
      try
      {
        geHtml = LCARS.loadTextResource(GE_HTML);
      }
      catch (Exception e)
      {
        e.printStackTrace();
      }
      
      // Create the screen listeners
      scl = new ComponentListener()
      {
        @Override
        public void componentShown(ComponentEvent e)
        {
          zoning();
        }
        
        @Override
        public void componentResized(ComponentEvent e)
        {
          zoning();
        }
        
        @Override
        public void componentMoved(ComponentEvent e)
        {
        }
        
        @Override
        public void componentHidden(ComponentEvent e)
        {
        }
      };
      swl = new WindowListener()
      {
        @Override
        public void windowOpened(WindowEvent e)
        {
        }
        
        @Override
        public void windowIconified(WindowEvent e)
        {
        }
        
        @Override
        public void windowDeiconified(WindowEvent e)
        {
        }
        
        @Override
        public void windowDeactivated(WindowEvent e)
        {
        }
        
        @Override
        public void windowClosing(WindowEvent e)
        {
          GE.this.stop();
        }
        
        @Override
        public void windowClosed(WindowEvent e)
        {
        }
        
        @Override
        public void windowActivated(WindowEvent e)
        {
        }
      };
    }
    catch (ClassCastException e)
    {
      System.err.println("LCARS: GE cannot run on remove screens.");
      e.printStackTrace();
    }
  }
  
  /**
   * Starts the Google earth wrapper. The method
   * <ul>
   *   <li>creates and starts a web browser and places it on the LCARS panel,</li>
   *   <li>creates and starts a Google Earth plugin in the browser and</li>
   *   <li>starts a communications thread with the plugin.</li>
   * </ul>
   * @see #stop()
   * @see #isStarted()
   */
  public synchronized void start()
  {
    if (isStarted()) return;

    LCARS.log("GEA","Starting ...");
    pluginUp = false;

    // Initialize GUI
    canvasText = "ACCESSING DATABASE ...";
    canvas = new Canvas();
    canvas.setBackground(Color.BLACK);
    screen.add(canvas);
    zoning();
    screen.addComponentListener(scl);
    screen.addWindowListener(swl);
    canvas.addNotify();
    screen.addNotify();
    screen.getSwtDisplay().syncExec(new Runnable()
    {
      public void run()
      {
        //Screen screen = Screen.getLocal(panel.getScreen());
        swtShell = SWT_AWT.new_Shell(screen.getSwtDisplay(),canvas);
        swtShell.setBackground(screen.getSwtDisplay().getSystemColor(SWT.COLOR_BLACK));
        swtShell.addPaintListener(new PaintListener()
        {
          public void paintControl(PaintEvent e)
          {
            org.eclipse.swt.graphics.Color color = new org.eclipse.swt.graphics.Color(
                e.gc.getDevice(),0xFF,0x99,0x00);
            org.eclipse.swt.graphics.Font font = new org.eclipse.swt.graphics.Font(
                e.gc.getDevice(),LCARS.getInstalledFont(LCARS.FN_COMPACTA),56,0);
            e.gc.setForeground(color);
            e.gc.setFont(font);
            Point tl = screen.panelToComponent(new Point(bounds.x,bounds.y));
            Point br = screen.panelToComponent(new Point(bounds.x+bounds.width,bounds.y+bounds.height));            
            int x = (br.x-tl.x-e.gc.stringExtent(canvasText).x)/2;
            int y = (br.y-tl.y-e.gc.stringExtent(canvasText).y)/2;
            e.gc.drawText(canvasText,x,y);
            color.dispose();
            font.dispose();
          }
        });
        swtShell.addMouseListener(new MouseListener()
        {
          @Override
          public void mouseUp(MouseEvent e)
          {
          }
          
          @Override
          public void mouseDown(MouseEvent e)
          {
            LCARS.log("GEA","Click on SWT canvas...");
            browser.setVisible(true);
          }
          
          @Override
          public void mouseDoubleClick(MouseEvent e)
          {
          }
        });
        zoningSync();
        
        browser = new Browser(swtShell,SWT.NONE);
        browser.setBackground(screen.getSwtDisplay().getSystemColor(SWT.COLOR_BLACK));
        browser.setVisible(false);
        new NotifyCameraPositionFunction(GE.this,browser,"javaNotifyCameraPosition");
        new NotifyGlobeClick(GE.this,browser,"javaNotifyGlobeClick");
        new NotifyInitializedFunction(GE.this,browser,"javaNotifyInitialized");
        browser.addProgressListener(new ProgressListener()
        {
          public void changed(ProgressEvent event)
          {
          }

          public void completed(ProgressEvent event)
          {
          }
        });
        zoningSync();
        
        browser.setText(geHtml.replace("${WORLD}",GE.this.world));
      }
    });
    
    // Initialize orbit and camera target
    camTarget = new GECamera(this.world);
    setOrbit(new StdOrbit(world));

    LCARS.log("GEA","... Started");    
  }
  
  /**
   * Stops the Google earth wrapper. The method
   * <ul>
   *   <li>stops the communications thread with the plugin,</li>
   *   <li>stops and destroys the Google Earth plugin in the browser and</li>
   *   <li>stops and destroys the web browser and removes it from the LCARS
   *       panel.</li>
   * </ul>
   * @see #start()
   * @see #isStarted()
   */
  public synchronized void stop()
  {
    LCARS.log("GEA","Stopping ...");
    pluginUp = false;
    orbit = null;

    screen.removeComponentListener(scl);
    screen.removeWindowListener(swl);
    if (canvas!=null) screen.remove(canvas);
    screen.getSwtDisplay().syncExec(new Runnable()
    {
      public void run()
      {
        try
        {
          if (browser!=null) browser.dispose();
        }
        catch (Exception e)
        {
          e.printStackTrace();
        }
      }
    });
    
    if (swtShell!=null) swtShell.dispose();
    browser  = null;
    swtShell = null;
    canvas   = null;
    LCARS.log("GEA","... Stopped");    
  }

  // -- Getters and setters --
  
  /**
   * Determines if this Google Earth wrapper is started.
   * 
   * @see #start()
   * @see #stop()
   */
  public boolean isStarted()
  {
    return (swtShell!=null && canvas!=null && browser!=null);
  }

  /**
   * Toggles the visibility of this Google Earth wrapper.
   * 
   * @param visible
   *          The new visibility.
   */
  public void setVisible(boolean visible)
  {
    canvas.setVisible(visible);
  }

  /**
   * Determines if this Google Earth wrapper is visible. 
   */
  public boolean isVisible()
  {
    return canvas!=null && canvas.isVisible();
  }

  /**
   * Switches to a new world (earth, moon, mars or sky). The method stops orbiting and sets
   * the default camera position for the selected world. 
   * 
   * @param world
   *          The new world; {@link #EARTH}, {@link #MOON}, {@link #MARS}, or {@link #SKY}.
   * @see #getWorld() 
   */
  public void setWorld(String world)
  {
    stop();
    this.world = world;
    start();
  }
  
  /**
   * Returns the current world.
   * 
   * @return {@link #EARTH}, {@link #MOON}, {@link #MARS}, or {@link #SKY}.
   * @see #setWorld(String)
   */
  public String getWorld()
  {
    return this.world;
  }

  /**
   * Sets a new camera orbit and starts orbiting.
   * 
   * @param orbit
   *          The orbit, can be <code>null</code> to stop orbiting and fix the camera at its
   *          current position.
   * 
   * @see #getOrbit()
   * @see #setOrbiting(boolean)
   * @see #isOrbiting()
   */
  public void setOrbit(GEOrbit orbit)
  {
    if (orbit==null)
    {
      setOrbiting(false);
      this.orbit = null;
      return;
    }
    if (orbit.getWorld()!=this.world) setWorld(orbit.getWorld());
    this.orbit = orbit;
    setOrbiting(true);
  }

  /**
   * Returns the current camera orbit (or <code>null</code> if none).
   * 
   * @see #setOrbit(GEOrbit)
   * @see #setOrbiting(boolean)
   * @see #isOrbiting()
   */
  public GEOrbit getOrbit()
  {
    return orbit;
  }

  /**
   * Resumes or pauses orbiting of the Google Earth camera. If no camera orbit has been set, the
   * method does noting.
   * 
   * @param orbiting
   *          The new orbiting state.
   * 
   * @see #isOrbiting()
   * @see #setOrbit(GEOrbit)
   * @see #getOrbit()
   */
  public void setOrbiting(boolean orbiting)
  {
    if (orbit==null) return;
    if (orbiting)
    {
      setOrbiting(false);
      orbitTimer = new Timer(true);
      orbitTimer.scheduleAtFixedRate(new TimerTask()
      {
        @Override
        public void run()
        {
          if (orbit==null)
          {
            setOrbiting(false);
            return;
          }
          if (!GE.this.pluginUp) return;
          GECamera camera = orbit.getCamera();
          float    speed  = orbit.getFlytoSpeed();
          setTargetCamera(camera,speed);
        }
      },1,80);
    }
    else
    {
      if (orbitTimer==null) return;
      orbitTimer.cancel();
      orbitTimer.purge();
      orbitTimer = null;
    }
  }

  /**
   * Determines if the Google Earth camera is orbiting.
   * 
   * @see #setOrbiting(boolean)
   * @see #setOrbit(GEOrbit)
   * @see #getOrbit()
   */
  public boolean isOrbiting()
  {
    return orbit!=null && orbitTimer!=null;
  }
  
  /**
   * Moves the Google Earth camera. It will take some time until this position is actually reached.
   * The actual camera position of Google Earth can be obtained through {@link #getActualCamera()}.
   * 
   * @param camera
   *          The position to fly to.
   * @param speed
   *          The fly-to speed. Possible values are:
   *          <table style="margin-left:2em">
   *            <tr><td>0..5</td><td>Speed values supported by Google Earth</td></tr>
   *            <tr><td>{@link #SPEED_TELEPORT}</td><td>Instant move</td></tr>
   *            <tr><td>{@link #SPEED_AUTO}</td><td>Chose automatically</td></tr>
   *          </table>
   * 
   * @see #getTargetCamera()
   * @see #getActualCamera()
   * @see #flyTo(GECamera, float)
   * @see #flyTo(GECamera)
   */
  private synchronized void setTargetCamera(GECamera camera, float speed)
  {
    if (camTarget!=null && this.camTarget.equals(camera))
      // Nothing to be done.
      return;
    
    camTarget = new GECamera(camera);
    if (isAutoSpeed(speed) && camActual!=null)
    {
      float speedLat = Math.abs(camera.latitude -camActual.latitude )/8;
      float speedLon = Math.abs(camera.longitude-camActual.longitude)/4;
      speed = Math.max(speedLat,speedLon);
      if (speed<0) speed=0;
      if (speed>4.99) speed=4.99f;
    }

    GE.this.cameraSpeed = speed; //Math.min(Math.max(speed,0.5f),1.5f);
    String script = String.format(Locale.ENGLISH,JS_FLYTO,
      camera.latitude,camera.longitude,camera.altitude,camera.heading,
      camera.tilt,camera.roll,GE.this.cameraSpeed);
    try
    {
      if (!jsExecute(script))
        LCARS.err("GEA","Failed to execute JavaScript "+script);
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }

  /**
   * Returns the Google Earth camera. Please note that the returned camera represents the target
   * position which is being navigated to and not necessarily the actual camera position. The actual
   * position can be retrieved through {@link #getActualCamera()}. The returned object is a copy,
   * modifying it has no effect on the target camera.
   * 
   * @see #setTargetCamera(GECamera, float)
   * @see #getActualCamera()
   * @see #flyTo(GECamera, float)
   * @see #flyTo(GECamera)
   */
  public GECamera getTargetCamera()
  {
    return new GECamera(camTarget);
  }

  /**
   * Returns the actual position of the Google Earth camera. The returned object is a copy,
   * modifying it has no effect on the internal data.
   * 
   * @see #getTargetCamera()
   */
  public GECamera getActualCamera()
  {
    return new GECamera(camActual);
  }

  /**
   * Returns the camera speed.
   * 
   * @return the speed
   */
  public float getCameraSpeed()
  {
    return this.cameraSpeed;
  }
  
  /**
   * Toggles the visibility of the coordinate grid.
   * 
   * @param visible
   *          The new visibility.
   */
  public void setGridVisible(boolean visible)
  {
    this.grid = visible;
    String script = String.format(Locale.US,JS_GRID,visible);
    jsExecute(script);
  }
  
  /**
   * Determines of the coordinate grid is visible.
   */
  public boolean isGridVisible()
  {
    return grid;
  }
  
  /**
   * Toggles the visibility of the clouds (ineffective on worlds other than earth).
   * 
   * @param visible
   *          The new visibility.
   */
  public void setCloudsVisible(boolean visible)
  {
    clouds = visible;
    String script = String.format(Locale.US,JS_CLOUDS,visible);
    jsExecute(script);
  }
  
  /**
   * Determines of clouds are visible.
   */
  public boolean isClounsVisible()
  {
    return clouds;
  }

  /**
   * Toggles the visibility of the sun.
   * 
   * @param visible
   *          The new visibility.
   */
  public void setSunVisible(boolean visible)
  {
    this.sun = visible;
    String script = String.format(Locale.US,JS_SUN,visible);
    jsExecute(script);
  }
  
  /**
   * Determines of sun is visible.
   */
  public boolean isSunVisible()
  {
    return sun;
  }

  // -- Operations --
  
  /**
   * Moves the Google Earth camera. It will take some time until this position is actually reached.
   * The actual camera position of Google Earth can be obtained through {@link #getActualCamera()}.
   * The method cancels orbiting and calls {@link #setTargetCamera(GECamera, float)}.
   * 
   * @param camera
   *          The position to fly to.
   * @param speed
   *          The fly-to speed. Possible values are:
   *          <table style="margin-left:2em">
   *            <tr><td>0..5</td><td>Speed values supported by Google Earth</td></tr>
   *            <tr><td>{@link #SPEED_TELEPORT}</td><td>Instant move</td></tr>
   *            <tr><td>{@link #SPEED_AUTO}</td><td>Chose automatically</td></tr>
   *          </table>
   * 
   * @see #flyTo(GECamera)
   * @see #setTargetCamera(GECamera, float)
   * @see #getTargetCamera()
   * @see #getActualCamera()
   */
  public void flyTo(GECamera camera, float speed)
  {
    setOrbiting(false);
    setTargetCamera(camera,speed);
  }

  /**
   * Moves the Google Earth camera. It will take some time until this position is actually reached.
   * The actual camera position of Google Earth can be obtained through {@link #getActualCamera()}.
   * Invocation is identical to {@link #flyTo(GECamera, float) flyTo(camera,1)}.
   * 
   * @param camera
   *          The position to fly to.
   * 
   * @see #flyTo(GECamera, float)
   * @see #setTargetCamera(GECamera, float)
   * @see #getTargetCamera()
   * @see #getActualCamera()
   */
  public void flyTo(GECamera camera)
  {
    flyTo(camera,1);
  }
  
  /**
   * Executes a script in the {@link #browser}. The method will catch and print any exceptions. In
   * case of exceptions, the method will return <code>false</code>.
   * 
   * @param script
   *          The script to be executed.
   * @return <code>true</code> if the script was executed successfully, <code>false</code>
   *         otherwise.
   */
  protected boolean jsExecute(String script)
  {
    JsRunnable runnable = new JsRunnable(script,JsRunnable.EXECUTE);
    screen.getSwtDisplay().syncExec(runnable);
    return (Boolean)runnable.result;
  }

  /**
   * Evaluates a script in the {@link #browser} and returns the result of
   * {@link Browser#evaluate(String)}. The method will catch and print any exceptions. In case of
   * exceptions, the method will return <code>null</code>.
   * 
   * @param script
   *          The script to be evaluated.
   * @return The result of the evaluation.
   */
  protected Object jsEvaluate(String script)
  {
    JsRunnable runnable = new JsRunnable(script,JsRunnable.EVALUATE);
    screen.getSwtDisplay().syncExec(runnable);
    return runnable.result;
  }

  /**
   * (Re-)adjusts the placement of AWT and SWT widgets outside the SWT thread. Call
   * {@link #zoningSync()} from inside the SWT thread!
   */
  protected void zoning()
  {
    screen.getSwtDisplay().syncExec(new Runnable()
    {
      public void run()
      {
        zoningSync();
      }
    });
  }

  /**
   * (Re-)adjusts the placement of AWT and SWT widgets inside the SWT thread. Call {@link #zoning()}
   * from outside the SWT thread!
   */
  protected void zoningSync()
  {
    final Point tl = screen.panelToComponent(new Point(bounds.x,bounds.y));
    final Point br = screen.panelToComponent(new Point(bounds.x+bounds.width,bounds.y+bounds.height));
    if (canvas!=null) canvas.setBounds(tl.x,tl.y,br.x-tl.x,br.y-tl.y);
    if (swtShell!=null) swtShell.setSize(br.x-tl.x,br.y-tl.y);
    if (browser!=null) browser.setSize(br.x-tl.x+28/* <- this "hides" the scroll bar */,br.y-tl.y);
  }
  
  /**
   * Determines if a fly-to speed value indicates automatic speed choice.
   * 
   * @param speed
   *          The speed value.
   */
  protected boolean isAutoSpeed(float speed)
  {
    return speed<SPEED_MIN;
  }
  
  /**
   * Determines if a fly-to speed value indicates teleport speed (i.&nbsp;e. instant movement).
   * 
   * @param speed
   *          The speed value.
   */
  protected boolean isTeleportSpeed(float speed)
  {
    return speed>SPEED_MAX;
  }

  // -- Nested classes --

  /**
   * Runnable for script execution in the browser.
   */
  class JsRunnable implements Runnable
  {
    static final int EXECUTE  = 0;
    static final int EVALUATE = 1;
    
    String script;
    int    mode;
    Object result;
    
    /**
     * Creates a new script execution runnable.
     * 
     * @param script
     *          The script.
     * @param mode
     *          {@link #EXECUTE} or {@link #EVALUATE}
     */
    public JsRunnable(String script, int mode)
    {
      this.script = script;
      this.mode   = mode;
    }
  
    @Override
    public void run()
    {
      try
      {
        if (mode==EXECUTE)
        {
          //LCARS.log("GEA","Executing \""+script+"\"");
          result = browser.execute(script);
        }
        else
        {
          //LCARS.log("GEA","Evaluating \""+script+"\"");
          result = browser.evaluate(script);
        }
      }
      catch (Exception e)
      {
        e.printStackTrace();
        result = mode==EXECUTE?false:null;
      }
    }
  }

  // -- Browser functions --
  
  /**
   * Browser function being called when Google Earth plug-in has been initialized.
   */
  static class NotifyInitializedFunction extends BrowserFunction
  {
    private GE ge;
    
    NotifyInitializedFunction(GE ge, Browser browser, String name)
    {
      super(browser,name);
      this.ge = ge;
    }

    public Object function(Object[] arguments)
    {
      ge.pluginUp = true;
      
      ge.zoning();
      ge.browser.setVisible(true);

      return null;
    }
  }
 
  /**
   * Browser function being called to update the actual camera position.
   */
  static class NotifyCameraPositionFunction extends BrowserFunction
  {
    private GE ge;
    
    NotifyCameraPositionFunction(GE ge, Browser browser, String name)
    {
      super(browser,name);
      this.ge = ge;
    }

    public Object function(Object[] arguments)
    {
      if (arguments.length==1 && arguments[0] instanceof String)
      {
        String s = (String)arguments[0];
        //LCARS.log("GEA","CamPosition "+s);
        GECamera camera = new GECamera(ge.world);
        if (s!=null && s.length()>0)
        {
          String[] fields = s.split(":");
          if (s!="?" && fields.length==6)
          try
          {
            camera.latitude  = Float.parseFloat(fields[0]);
            camera.longitude = Float.parseFloat(fields[1]);
            camera.altitude  = Float.parseFloat(fields[2]);
            camera.heading   = Float.parseFloat(fields[3]);
            camera.tilt      = Float.parseFloat(fields[4]);
            camera.roll      = Float.parseFloat(fields[5]);
            ge.camActual = camera;
          }
          catch (Exception e)
          {
            e.printStackTrace();
          }
        }
      }
      return null;
    }
  }

  /**
   * Browser function being called when the user clicks the Google Earth globe.
   */
  static class NotifyGlobeClick extends BrowserFunction
  {
    private GE ge;
    
    NotifyGlobeClick(GE ge, Browser browser, String name)
    {
      super(browser,name);
      this.ge = ge;
    }

    public Object function(Object[] arguments)
    {
      ge.setOrbiting(false);
      return null;
    }
  }
}