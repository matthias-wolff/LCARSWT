package de.tucottbus.kt.lcars;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.Image;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.event.WindowStateListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.event.MouseInputListener;

import org.eclipse.swt.widgets.Display;

import de.tucottbus.kt.lcars.elements.ElementData;
import de.tucottbus.kt.lcars.feedback.UserFeedback;
import de.tucottbus.kt.lcars.feedback.UserFeedbackPlayer;
import de.tucottbus.kt.lcars.util.LoadStatistics;

/**
 * An LCARS screen. A screen is the representation of a physical unit with a display, a touch panel
 * and/or mouse and keyboard, and a loudspeaker. A screen provides a graphical, haptic and acoustic
 * user interface for an {@linkplain Panel LCARS panel}. Screen and panel do not necessarily run on
 * the same virtual or physical machine.
 * 
 * @see Panel
 * @see IScreen
 * @author Matthias Wolff
 */
public class Screen extends JFrame implements IScreen, MouseInputListener, KeyListener
{
  // -- Fields --

  /**
   * The default serial version ID.
   */
  private static final long  serialVersionUID = -1L;
  
  /**
   * The panel this screen is displaying. 
   */
  private IPanel ipanel;

  /**
   * The current state of the {@link Panel} displayed on this screen.
   */
  private PanelData panelData;
  
  /**
   * Full screen mode flag.
   */
  private boolean fullScreenMode;
  
  /**
   * The cache for the 2D rendering transform.
   */
  private AffineTransform renderingTransform;
  
  /**
   * The screen timer. Blocks the screen saver and keeps the frames-per-second-statistics.
   */
  private Timer screenTimer;

  /**
   * The user feedback player.
   */
  private UserFeedbackPlayer userFeedbackPlayer;
  
  /**
   * The screen rendering load statistics.
   */
  private LoadStatistics loadStat;
  
  /**
   * The background image.
   */
  private Image bgImg;
  
  /**
   * The background image resource file.
   */
  private String bgImgRes;
  
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
   * @throws ClassNotFoundException If <code>panelClass</code> is invalid
   */
  public Screen
  (
    GraphicsDevice device,
    String         panelClass,
    boolean        fullScreen
  ) throws ClassNotFoundException
  {
    super(device.getDefaultConfiguration());
    this.loadStat   = new LoadStatistics(25);

    // Create Swings widgets
    setTitle("LCARS");
    setDefaultCloseOperation(EXIT_ON_CLOSE);
    JComponent component = new JComponent()
    {
      private static final long serialVersionUID = 1L;

      @Override
      public void paintComponent(Graphics g)
      {
        super.paintComponent(g);
        paint2D((Graphics2D)g);
      }
    };
    setContentPane(component);
    setPanel(panelClass);
    fullScreenMode = fullScreen && device.isFullScreenSupported();
    setUndecorated(fullScreen);
    setResizable(!fullScreen);
    setBackground(Color.black);
    if (fullScreenMode && !"maximized".equals(LCARS.getArg("--mode=")))
    {
      // Full-screen mode
      device.setFullScreenWindow(this);
      setAlwaysOnTop(fullScreen);
      validate();
    }
    else
    {
      // Windowed mode
      setPreferredSize(new Dimension(950,560));
      int nXPos = 200;
      try
      {
        nXPos = Integer.parseInt(LCARS.getArg("--xpos="));
      }
      catch (NumberFormatException e){};
      setLocation(nXPos,200);
      pack();
      setVisible(true);
      if (fullScreen)
        setExtendedState(JFrame.MAXIMIZED_BOTH);
    }
    createBufferStrategy(2);
    
    if (LCARS.getArg("--nomouse")!=null)
      this.setCursor(LCARS.getBlankCursor());
    
    // The screen timer
    screenTimer = new Timer(true);
    screenTimer.scheduleAtFixedRate(new ScreenTimerTask(),40,40);
    
    // The user feedback player
    userFeedbackPlayer = new UserFeedbackPlayer(UserFeedbackPlayer.AUDITORY)
    {
      @Override
      public void writeColor(Color color)
      {
        // Does not give visual feedback
      }
    };
    
    // Window event handlers
    this.addWindowStateListener(new WindowStateListener()
    {
      @Override
      public void windowStateChanged(WindowEvent e)
      {
      }
    });
    this.addWindowListener(new WindowListener(){
      public void windowActivated(WindowEvent e)
      {
      }
      public void windowClosed(WindowEvent e)
      {
      }
      public void windowClosing(WindowEvent e)
      {
      }
      public void windowDeactivated(WindowEvent e)
      {
      }
      public void windowDeiconified(WindowEvent e)
      {
      }
      public void windowIconified(WindowEvent e)
      {
      }
      public void windowOpened(WindowEvent e)
      {
      }   
    });
    this.addComponentListener(new ComponentListener()
    {
      @Override
      public void componentShown(ComponentEvent e)
      {
        invalidateScreen();
      }
      
      @Override
      public void componentResized(ComponentEvent e)
      {
        invalidateScreen();
      }
      
      @Override
      public void componentMoved(ComponentEvent e)
      {
      }
      
      @Override
      public void componentHidden(ComponentEvent e)
      {
      }
    });

    // Mouse handlers
    getContentPane().addMouseListener(this);
    getContentPane().addMouseMotionListener(this);
    
    // Keyboard event handlers
    addKeyListener(this);
  }

  // -- Getters and setters --

  /**
   * Returns the actual {@link Screen} for an {@linkplain IScreen LCARS screen interface}. This is
   * only possible if the screen is run by the virtual machine on which this method is invoked. If
   * <code>screen</code> refers to a remote screen, the method will throw a
   * {@link ClassCastException}.
   * 
   * @param screen
   *          The screen interface.
   * @return The actual screen (only if it is local).
   * @throws ClassCastException
   *           If the actual screen is run by another virtual machine.
   */
  public static Screen getLocal(IScreen screen)
  {
    return (Screen)screen;
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
    return renderingTransform==null;
  }
  
  /**
   * Marks the screen as needing to be redrawn. 
   */
  protected synchronized void invalidateScreen()
  {
    renderingTransform = null;
  }
  
  /**
   * Returns the rendering transform 
   */
  protected synchronized AffineTransform getTransform()
  {
    if (this.renderingTransform!=null)
      return this.renderingTransform;
    if (panelData==null)
      return new AffineTransform();
    
    Dimension dp = panelData.panelState.dimension;
    Dimension ds = getContentPane().getSize();
    renderingTransform = new AffineTransform();
    double sx = (double)ds.getWidth() /(double)dp.width;
    double sy = (double)ds.getHeight()/(double)dp.height;
    if (sy<sx) sx=sy;
    renderingTransform.scale(sx,sx);
    int x = (int)((ds.getWidth() -sx*dp.getWidth() )/2);
    int y = (int)((ds.getHeight()-sx*dp.getHeight())/2);
    renderingTransform.translate(x/sx,y/sx);
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
      pt2d = getTransform().inverseTransform(pt,null);
      return new Point((int)Math.round(pt2d.getX()),(int)Math.round(pt2d.getY()));
    }
    catch (NoninvertibleTransformException e)
    {
      // Cannot happen
      e.printStackTrace();
      return null;
    }
  }
  
  /**
   * Converts panel to component (LCARS screen) coordinates.
   * 
   * @param pt The LCARS panel coordinates.
   * @return The AWT component coordinates.
   * 
   * @see #componentToPanel(Point)
   */
  public Point panelToComponent(Point pt)
  {
    Point2D pt2d = getTransform().transform(pt,null);
    return new Point((int)Math.round(pt2d.getX()),(int)Math.round(pt2d.getY()));
  }
  
  /**
   * Paints the panel elements of this screen on a {@link Graphics2D} context. 
   * 
   * @param g2d the graphics context
   * @see #elements
   */
  protected void paint2D(Graphics2D g2d)
  {
    PanelState panelState;
    Vector<ElementData> elementData;
    synchronized (this)
    {
      if (this.panelData!=null)
      {
        panelState  = this.panelData.panelState;
        elementData = new Vector<ElementData>(this.panelData.elementData);
      }
      else
      {
        panelState  = null;
        elementData = null;
      }
    }

    // Prepare
    g2d.setTransform(getTransform());
    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
    g2d.setRenderingHint(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY);
    g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,RenderingHints.VALUE_STROKE_NORMALIZE);
    g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION,RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED);
    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,1.0f));
    long time = System.nanoTime();

    // Draw background
    if (bgImg==null)
    {
      g2d.setColor(Color.black);
      g2d.fillRect(0,0,getWidth(),getHeight());
    }
    else
      g2d.drawImage(bgImg,null,this);

    // Draw LCARS widgets
    //GImage.beginCacheRun();
    if (elementData!=null)
      for (ElementData data : elementData)
        try
        {
          data.render2D(g2d,panelState);
        }
        catch (Throwable e)
        {
          e.printStackTrace();
        }
    //GImage.endCacheRun();

    // Aftermath
    time = System.nanoTime()-time;
    loadStat.add((int)(time/400000));
  }
  
  /**
   * Called to update the background image of the screen.
   */
  protected void updateBgImage()
  {
    String bgImgRes = null;
    if (panelData!=null&&panelData.panelState!=null)
      bgImgRes = panelData.panelState.bgImageRes;    
    if (bgImgRes==this.bgImgRes && !(bgImgRes!=null^this.bgImg!=null)) return;
    this.bgImgRes = bgImgRes;
    if (this.bgImgRes==null)
    {
      bgImg = null;
      return;
    }
    
    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    LCARS.log("SCR","background="+bgImgRes);
    URL resource = classLoader.getResource(bgImgRes);
    if (resource==null) { bgImg=null; return; }
    bgImg = Toolkit.getDefaultToolkit().createImage(resource.getFile());
//    Dimension d = updateData.panelState.dimension;
//    bgImg.getScaledInstance(d.width,d.height,Image.SCALE_DEFAULT);
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
    if (ipanel==null && this.ipanel==null         ) return;
    if (ipanel!=null && ipanel.equals(this.ipanel)) return;
    
    // Stop current panel (if any)
    if (this.ipanel!=null) 
      try
      {
        this.ipanel.stop();
      }
      catch (RemoteException e)
      {
      }

    // Set and start new panel  
    this.ipanel = ipanel;
    this.panelData = null;
    if (this.ipanel!=null)
      try
      {
        LCARS.log("SCR","Starting panel "+this.ipanel.getClass().getSimpleName()+"...");
        this.ipanel.start();
        LCARS.log("SCR","...Panel started");
      }
      catch (RemoteException e)
      {
        LCARS.err("SCR","...Panel start FAILED");
        e.printStackTrace();
      }
  }
  
  // -- Implementation of the IScreen interface --
  
  @Override
  public Area getArea()
  {
    int w = Toolkit.getDefaultToolkit().getScreenSize().width;
    int h = Toolkit.getDefaultToolkit().getScreenSize().height;
    return new Area(new Rectangle(0,0,w,h));
  }
  
  @Override
  public String getHostName()
  {
    return LCARS.getHostName();
  }
  
  @Override
  public void setPanel(String className) throws ClassNotFoundException
  {
    setPanel(Panel.createPanel(className,this));
  }

  @Override
  public IPanel getPanel()
  {
    return ipanel;
  }

  @Override
  public synchronized void update(PanelData data, boolean incremental)
  {
    if (incremental && panelData!=null)
    {
      // 1. Create a hash map of the current ElementData
      int i = panelData.elementData.size();
      HashMap<Long,ElementData> h = new HashMap<Long,ElementData>(i);
      for (ElementData edp: panelData.elementData)
        h.put(edp.serialNo,edp);
        
      // 2. Complete the received ElementData with the present information 
      for (ElementData edu : data.elementData)
        try
        {
          ElementData edp = h.get(edu.serialNo);
          if (edp!=null) edu.applyUpdate(edp);
        }
        catch (Exception e)
        {
          LCARS.err("SCR","Update failed on element #"+edu.serialNo+": "+e.getMessage());
        }
    }
    
    // Set new panel data and invalidate the screen
    panelData = data;
    updateBgImage();
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
    System.exit(0);
  }
  
  // -- Implementation of the MouseInputListener interface --

  @Override
  public void mouseClicked(MouseEvent e)
  {
    // Ignored
  }

  @Override
  public void mousePressed(MouseEvent e)
  {
    Point pt = componentToPanel(e.getPoint());
    TouchEvent te = new TouchEvent();
    te.type = TouchEvent.DOWN;
    te.x    = pt.x;
    te.y    = pt.y;
    try { ipanel.processTouchEvent(te); } catch (RemoteException e1) {}
  }

  @Override
  public void mouseReleased(MouseEvent e)
  {
    Point pt = componentToPanel(e.getPoint());
    TouchEvent te = new TouchEvent();
    te.type = TouchEvent.UP;
    te.x    = pt.x;
    te.y    = pt.y;
    try { ipanel.processTouchEvent(te); } catch (RemoteException e1) {}
  }

  @Override
  public void mouseEntered(MouseEvent e)
  {
    // Ignored
  }

  @Override
  public void mouseExited(MouseEvent e)
  {
    // Ignored
  }

  @Override
  public void mouseDragged(MouseEvent e)
  {
    Point pt = componentToPanel(e.getPoint());
    TouchEvent te = new TouchEvent();
    te.type = TouchEvent.DRAG;
    te.x    = pt.x;
    te.y    = pt.y;
    try { ipanel.processTouchEvent(te); } catch (RemoteException e1) {}
  }

  @Override
  public void mouseMoved(MouseEvent e)
  {
    // Ignored
  }

  // -- Implementation of the KeyListener interface --
  
  @Override
  public void keyTyped(KeyEvent e)
  {
    if (ipanel!=null) 
      try { ipanel.processKeyEvent(e); } catch (RemoteException e1) {}
  }

  @Override
  public void keyPressed(KeyEvent e)
  {
    if (ipanel!=null) 
      try { ipanel.processKeyEvent(e); } catch (RemoteException e1) {}
  }

  @Override
  public void keyReleased(KeyEvent e)
  {
    if (ipanel!=null) 
      try { ipanel.processKeyEvent(e); } catch (RemoteException e1) {}
  }

  // -- Nested classes --
  
  /**
   * The screen timer task. Blocks the screen saver, does the regular repainting and keeps the
   * 2D rendering load statistics.
   */
  protected final class ScreenTimerTask extends TimerTask
  {
    private long ctr;

    @Override
    public void run()
    {
      // Every 40 milliseconds...
      {
        if (isScreenInvalid()) repaint();
      }

      // Every second...
      if (ctr%25==0)
      {
        if (!isScreenInvalid() && loadStat.getEventCount()==0) repaint();
        loadStat.period();
      }

      // Every 60 seconds...
      if (ctr%1500==0)
        try
        {
          Robot r = new Robot();
          Point m = MouseInfo.getPointerInfo().getLocation();
          r.mouseMove(m.x-1,m.y);
          r.delay(10);
          m = MouseInfo.getPointerInfo().getLocation();
          r.mouseMove(m.x+1,m.y);
        }
        catch (Exception e)
        {
        }
        
      ctr++;
    }
  }

}

// EOF
