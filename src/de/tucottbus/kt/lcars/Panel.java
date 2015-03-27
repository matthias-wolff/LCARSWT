package de.tucottbus.kt.lcars;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import de.tucottbus.kt.lcars.contributors.EMessageBox;
import de.tucottbus.kt.lcars.contributors.EMessageBoxListener;
import de.tucottbus.kt.lcars.contributors.EPanelSelector;
import de.tucottbus.kt.lcars.elements.EElement;
import de.tucottbus.kt.lcars.elements.EEvent;
import de.tucottbus.kt.lcars.elements.EEventListener;
import de.tucottbus.kt.lcars.elements.ELabel;
import de.tucottbus.kt.lcars.elements.ElementData;
import de.tucottbus.kt.lcars.feedback.UserFeedback;
import de.tucottbus.kt.lcars.speech.ISpeechEngine;
import de.tucottbus.kt.lcars.speech.ISpeechEventListener;
import de.tucottbus.kt.lcars.speech.events.SpeechEvent;
import de.tucottbus.kt.lcars.util.LoadStatistics;

/**
 * An LCARS panel. A panel represents the contents and semantics of a graphical, haptic, acoustic
 * and speech user interface. Panels are physically displayed at {@link Screen LCARS screens}.
 * Screen and panel do not necessarily run on the same virtual or physical machine.
 * 
 * @see Screen
 * @see IPanel
 * @author Matthias Wolff
 */
public class Panel implements IPanel, EEventListener, ISpeechEventListener
{
  /**
   * This panel for nested classes.
   */
  protected Panel thisPanel;
  
  /**
   * The screen this panel is running on.
   */
  private IScreen iscreen;

  /**
   * The speech engine working for this panel.
   */
  private static ISpeechEngine speechEngine;

  /**
   * HACK: should not be necessary!
   */
  private static boolean speechEngineSearched;
  
  /**
   * The list of {@linkplain EElement elements} on this panel.
   */
  private ArrayList<EElement> elements = new ArrayList<EElement>(); 

  /**
   * The panel state.
   */
  private PanelState state;
  
  /**
   * The list of key listeners registered with this panel.
   */
  private Vector<KeyListener> keyListeners;
  
  /**
   * Time of the last full screen update (as obtained by {@link System#nanoTime()}.
   */
  private long fullUpdateTime;
  
  /**
   * The panel load statistics.
   */
  private LoadStatistics loadStat;
  
  /**
   * Flag indicating that the screen needs to be redrawn.
   */
  private boolean screenInvalid;
  
  private   EMessageBox    eMsgBox;
  private   EPanelSelector ePnlSel;
  private   ELabel         eTitle;
  private   EElement       eLight;
  private   EElement       eDim;
  private   EElement       eSilent;
  private   EElement       dragElement;
  private   Timer          runt;
  private   int            runc;
  private   int            dimc;
  private   float          dimInc = 0.05f;

  // -- Static API --

  /**
   * Guesses a human readable name for a panel class. For a panel class named
   * <code>VeryFancyPanel</code> the would return "Very Fancy".
   * 
   * @param clazz
   *          The (panel) class to guess a name for.
   */
  public static String guessName(Class<?> clazz)
  {
    if (clazz==null) return null;
    String n = "";
    String s = clazz.getSimpleName();
    if (s.endsWith("Panel")) s = s.substring(0,s.length()-5);
    for (int i=0; i<s.length(); i++)
    {
      char c = s.charAt(i);
      if (i>0 && Character.isUpperCase(c)) n+=" ";
      n+=c;
    }
    return n;
  }
  
  // -- Constructors --
  
  /**
   * Creates a new LCARS panel.
   * 
   * @param iscreen
   *          The screen to display the panel on.
   */
   public Panel(IScreen iscreen)
  {
    this.thisPanel    = this;
    this.iscreen      = iscreen;
    this.elements     = new ArrayList<EElement>();
    this.state        = new PanelState(getDimension());
    this.keyListeners = new Vector<KeyListener>();
    this.loadStat     = new LoadStatistics(25);
    LCARS.setPanelDimension(getDimension());
    init();
  }
  
  /**
   * Creates a panel of the specified class.
   * 
   * @param className
   *          The new {@link Panel}'s class name.
   * @param iscreen
   *          The screen to run the panel on.
   * @return The panel or <code>null</code> if the panel could not be created.
   * @throws ClassNotFoundException
   *           If <code>className</code> is invalid.
   */
  public static Panel createPanel(String className, IScreen iscreen)
  throws ClassNotFoundException
  {
    Panel panel;
    Class<?> panelClass = Panel.class;
    if (className!=null) panelClass = Class.forName(className);
    try
    {
      Object[] args = { iscreen };
      panel = (Panel)panelClass.getConstructors()[0].newInstance(args);
      LCARS.setPanelDimension(panel.getDimension());
      panel.start();
      if (className==null)
      {
        panel.panelSelectionDialog();
      }
      return panel;
    }
    catch (Throwable e)
    {
      e.printStackTrace();
      return null;
    }
  }
  
  // -- Overrides --
  
  public String getDocIndex()
  {
    return null;
  }

  /**
   * Called when the panel is initialized.
   */
  public void init()
  {
    Dimension dim = getDimension();
    
    int style = LCARS.EC_HEADLINE|LCARS.EF_HEAD1|LCARS.ES_LABEL_NE|LCARS.ES_STATIC;
    if (eTitle==null)
    {
      eTitle = new ELabel(this,dim.width-523,6,500,0,style,null); 
      add(eTitle);
    }
    try
    {
      eTitle.setLabel(guessName(getClass()).toUpperCase());
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }

    setBgImageResource(LCARS.getArg("--wallpaper="));
    
    eMsgBox = new EMessageBox((dim.width-600)/2,(dim.height-280)/2,600,280);
    ePnlSel = new EPanelSelector((dim.width-800)/2,(dim.height-480)/2,800,480);
  }

  /**
   * Displays the documentation root for this panel on the {@link HelpPanel}.
   */
  public void doc()
  {
    doc(false);
  }

  /**
   * Displays the documentation root for this panel on the {@link HelpPanel}.
   * 
   * @param noRestyleHtml
   *          <code>true</code> to prevent the documentation to be re-styled to the LCARS look.
   */
  public void doc(boolean noRestyleHtml)
  {
    try
    {
      getScreen().setPanel(HelpPanel.class.getName());
      ((HelpPanel)getScreen().getPanel()).setDocs(this.getClass(),getDocIndex(),noRestyleHtml);
      ((HelpPanel)getScreen().getPanel()).loadDoc();
    }
    catch (ClassNotFoundException e)
    {
      e.printStackTrace();
    }
    catch (RemoteException e){}
  }

  /**
   * Displays help for this panel on the {@link HelpPanel}.
   */
  public void help()
  {
    help(false);
  }

  /**
   * Displays help for this panel on the {@link HelpPanel}.
   * 
   * @param noRestyleHtml
   *          <code>true</code> to prevent help to be re-styled to the LCARS look.
   */
  public void help(boolean noRestyleHtml)
  {
    try
    {
      getScreen().setPanel(HelpPanel.class.getName());
      ((HelpPanel)getScreen().getPanel()).setDocs(this.getClass(),getDocIndex(),noRestyleHtml);
      ((HelpPanel)getScreen().getPanel()).loadHelp();
    }
    catch (ClassNotFoundException e)
    {
      e.printStackTrace();
    }
    catch (RemoteException e){}
  }
  
  // -- Getters and setters --

  /**
   * Sets a new screen interface.
   * 
   * @param iscreen
   *          The new screen interface.
   */
  public void setScreen(IScreen iscreen)
  {
    invalidate();
    if (iscreen==null || iscreen.equals(this.iscreen)) return;
    this.iscreen = iscreen;
  }
  
  /**
   * Returns an interface to the {@linkplain IScreen LCARS screen} on which this panel is running.
   */
  public IScreen getScreen()
  {
    return this.iscreen;
  }

  /**
   * Returns the speech engine working for this panel or <code>null</code> if there is no speech
   * input/output.
   */
  public static ISpeechEngine getSpeechEngine()
  {
    if (!speechEngineSearched)
    {
      speechEngineSearched = true;
      if (LCARS.getArg("--nospeech")!=null) return null;
      LCARS.log("PNL","Looking for a speech engine");
      
      // List available speech engine implementations
      Vector<Class<?>> cEngines = LCARS.getSpeechEngines();
      if (cEngines.size()==0)
      {
        LCARS.err("PNL","No speech engines found");
        return null;
      }

      // HACK: Just get the first one...
      Class<?> cEngine = cEngines.get(0);
      LCARS.log("PNL","ISpeechEngine = "+cEngine.getCanonicalName());
      try
      {
        Method mGetInstance = cEngine.getMethod("getInstance");
        speechEngine = (ISpeechEngine)mGetInstance.invoke(null);
      }
      catch (Throwable e)
      {
        // TODO: Auto-generated catch block
        e.printStackTrace();
      }
      LCARS.log("PNL","Speech engine found.");
    }
    return speechEngine;
  }
  
  public static void disposeSpeechEngine()
  {
    if (speechEngine!=null) speechEngine.stop();
    speechEngine = null;
  }
  
  /**
   * Returns the panel's title.
   */
  public String getTitle()
  {
    return eTitle!=null?eTitle.getLabel():null;
  }

  /**
   * Sets the panel's title.
   * 
   * @param title
   *          The title, can be <code>null</code> which will hide the title.
   */
  public void setTitle(String title)
  {
    String s = title!=null?title:"";
    if (eTitle!=null) eTitle.setLabel(s.toUpperCase());
  }

  /**
   * Sets the {@link ELabel} displaying the panel title.
   * 
   * @param eTitle
   *          The label element.
   */
  public void setTitleLabel(ELabel eTitle)
  {
    if (this.eTitle!=null) remove(this.eTitle);
    this.eTitle = eTitle;
    if (this.eTitle!=null) add(this.eTitle);
  }
  
  /**
   * Returns the color scheme of this panel.
   * 
   * @return One of the {@link LCARS}<code>.CS_XXX</code> constants.
   */
  public int getColorScheme()
  {
    return state.colorScheme;
  }

  /**
   * Sets the color scheme of this panel.
   * 
   * @param colorScheme
   *          The new color style, one of the {@link LCARS}<code>.CS_XXX</code> constants.
   */
  public void setColorScheme(int colorScheme)
  {
    if (state.colorScheme==colorScheme) return;
    state.colorScheme = colorScheme;
    invalidate();
  }
  
  /**
   * Returns the resource name of the background image of this panel. Can be
   * <code>null</code>.
   */
  public String getBgImageResource()
  {
    return state.bgImageRes;
  }

  /**
   * Sets the resource name of the background image of this panel.
   * 
   * @param bgImageRes
   *          The resource file name (<code>package/file.ext</code>) of the
   *          background image. The package separator '.' must be replaced by a
   *          slash '/'. Can be <code>null</code> for no background image. PNG,
   *          GIF and JPG image files are acceptable.
   */
  public void setBgImageResource(String bgImageRes)
  {
    if (bgImageRes==null && state.bgImageRes==null) return;
    if (bgImageRes!=null && bgImageRes.equals(state.bgImageRes)) return;
    state.bgImageRes = bgImageRes;
    invalidate();
  }
  
  /**
   * Returns the panel's state. 
   */
  public PanelState getState()
  {
    return this.state;
  }
  
  /**
   * Returns the panel's alpha value.
   * 
   * @return the alpha value, 0.0f (transparent) through 1.0f (opaque)
   */
  public float getAlpha()
  {
    return this.state.alpha;
  }
  
  /**
   * Sets the panel's alpha value.
   * 
   * @param alpha the alpha value, 0.0f (transparent) through 1.0f (opaque)
   */
  public void setAlpha(float alpha)
  {
    if (alpha<0.f) alpha=0.f;
    if (alpha>1.f) alpha=1.f;
    this.state.alpha = alpha;
    invalidate();
  }
  
  /**
   * Determines if this panel is running in the modal mode.
   * 
   * @see #setModal(boolean)
   */
  public boolean isModal()
  {
    return this.state.modal;
  }

  /**
   * Switches the modal mode on or off. In the modal mode only {@linkplain EElement GUI elements}
   * which have the {@link LCARS#ES_MODAL} style process user input. The modal mode is useful for
   * dialogs or message boxes displayed on top of a panel.
   * 
   * @param modal
   *          The new modal mode.
   * @see #isModal()
   */
  public void setModal(boolean modal)
  {
    for (int i=0; i<elements.size(); i++)
      elements.get(i).clearTouch();
    this.state.modal = modal;
  }
  
  /**
   * Determines if this panel is running in the silent mode.
   * 
   * @see #setSilent(boolean)
   */
  public boolean isSilent()
  {
    return this.state.silent;
  }
  
  /**
   * Switches the silent mode on or off. In the silent mode the pabel does not play any earcons.
   * 
   * @param silent
   *          The new silent mode.
   * @see #isSilent()
   */
  public void setSilent(boolean silent)
  {
    this.state.silent = silent;
  }

  /**
   * Sets controls for panel dimming.
   * 
   * @param eLight the light control
   * @param eDim   the dim control
   */
  public void setDimContols(EElement eLight, EElement eDim)
  {
    if (this.eLight!=null) this.eLight.removeEEventListener(this);
    if (this.eDim  !=null) this.eDim  .removeEEventListener(this);
    this.eLight = eLight;  this.eLight.addEEventListener(this);
    this.eDim   = eDim;    this.eDim  .addEEventListener(this);
  }
  
  /**
   * Sets the control for toggling earcons on and off.
   * 
   * @param eSilent the control
   */
  public void setSilentControl(EElement eSilent)
  {
    if (this.eSilent!=null) this.eSilent.removeEEventListener(this);
    this.eSilent = eSilent;
    this.eSilent.addEEventListener(this);
  }
  
  /**
   * Returns a vector of the LCARS GUI {@link EElement}s contained in this panel. 
   * 
   * @return the vector
   */
  public synchronized Vector<EElement> getElements()
  {
    return new Vector<EElement>(elements);
  }

  /**
   * Returns the computational load statistics of the panel. The load depends on the number and
   * complexity of the GUI elements.
   * <ul>
   * <li>Call {@link LoadStatistics#getLoad()} on the return value to determine the average
   * percentage of 40 milliseconds consumed by {@linkplain EElement#validateGeometry() creating
   * geometries} and {@linkplain #updateScreen() submitting them to the screen}. A return value of
   * more than 100 % indicates possible display juddering and the consumption of the entire capacity
   * of one processor core if 25 frames per second are painted.</li>
   * <li>Call {@link LoadStatistics#getEventsPerPeriod()} on the return value to determine the
   * actual frame rate in the last complete second.</li>
   * </ul>
   */
  public LoadStatistics getLoadStatistics()
  {
    return loadStat;
  }
  
  // -- GUI element management --
  
  /**
   * Adds a new LCARS GUI element to the panel. This does <em>not</em> trigger
   * a repaint of the panel!
   * 
   * @param el the element
   */
  public synchronized EElement add(EElement el)
  {
    if (!elements.contains(el)) elements.add(el);
    return el;
  }

  /**
   * Removes an LCARS GUI element from the panel. This does <em>not</em> trigger
   * a repaint of the panel!
   * 
   * @param el the element
   */
  public synchronized void remove(EElement el)
  {
    elements.remove(el);
  }
  
  /**
   * Return the LCARS GUI element at the specified position (panel coordinates).
   * Use {@link Screen#componentToPanel(Point)} to convert screen coordinates (e.g.
   * from mouse events) to panel coordinates.
   * 
   * @param pt
   *         Panel coordinates.
   * @return An LCARS GUI {@link EElement} or <code>null</code> if there is no
   *         element at this position
   */
  public EElement elementAt(Point pt)
  {
    if (isModal())
      return elementAt(pt,true);
    else
    {
      EElement el = elementAt(pt,true);
      if (el==null)
        el = elementAt(pt,false);
      return el;
    }
  }
  
  /**
   * Return the LCARS GUI element at the specified position (panel coordinates).
   * Use {@link Screen#componentToPanel(Point)} to convert screen coordinates (e.g.
   * from mouse events) to panel coordinates.
   * 
   * @param pt
   *         The panel coordinates.
   * @param modal
   *         If <code>true</code> consider {@link EElement#isModal() modal}
   *         {@link EElement}s only, otherwise consider non-modal {@link EElement}s
   *         only.
   * @return An LCARS GUI {@link EElement} or <code>null</code> if there is no
   *         element at this position
   */
  protected EElement elementAt(Point pt, boolean modal)
  {
    synchronized (elements)
    {
      for (int i=elements.size()-1; i>=0; i--)
      {
        try
        {
          EElement el = elements.get(i);
          Shape   es = el.getShape();
          if (es==null || el.isStatic()) continue;
          if (el.isModal()!=modal) continue;
          if (el.getShape().contains(new Point2D.Float(pt.x,pt.y)))
            return el;
        }
        catch (IndexOutOfBoundsException e)
        {
          return null;
        }
      }
      return null;
    }
  }
  
  // -- Operations --
  
  /**
   * Displays a message box. The user's answer will be dispatched to the
   * specified {@link EMessageBoxListener}.
   * 
   * @param title the title
   * @param msg   the message text
   * @param bn1   the left button text
   * @param bn2   the right button text
   * @param l     the message box listener
   */
  public void messageBox(String title, String msg, String bn1, String bn2, EMessageBoxListener l)
  {
    eMsgBox.addListener(l);
    eMsgBox.open(this,title,msg,bn1,bn2);
  }

  @Override
  public void panelSelectionDialog()
  {
    ePnlSel.open(this);
  }
  
  /**
   * Dims this panel to the specified alpha value.
   * 
   * @param alpha
   *          The new alpha value (0: transparent ... 1: opaque).
   */
  public void dim(float alpha)
  {
    dimInc = Math.abs(dimInc)*((alpha>this.state.alpha)?1f:-1f);
    dimc = Math.abs((int)((alpha-this.state.alpha)/this.dimInc))+1;
  }
  
  // -- Event handling --

  /**
   * Adds the specified key listener to receive key events from this panel.
   * 
   * @param listener
   *          The key listener. If <code>null</code> or if the specified listened is already
   *          registered, the method does nothing.
   */
  public void addKeyListener(KeyListener listener)
  {
    if (keyListeners.indexOf(listener)>=0) return;
    keyListeners.add(listener);
  }

  /**
   * Removes the specified key listener from the listeners list. 
   * 
   * @param listener
   *          The key listener.
   */
  public void removeKeyListener(KeyListener listener)
  {
    keyListeners.remove(listener);
  }
  
  // -- Implementation of the ISpeechEventListener interface --

  @Override
  public void speechEvent(SpeechEvent event)
  {
  }
  
  // -- Periodic and timer actions --
  
  class PanelTimerTask extends TimerTask
  {
    @Override
    public void run()
    {
      // Call periodic panel methods
      if (runc% 2==0) try { fps25(); } catch (Exception e) {}
      if (runc% 5==0) try { fps10(); } catch (Exception e) {}
      if (runc%25==0) try { fps2 (); } catch (Exception e) {}
      if (runc%50==0) try { fps1 (); } catch (Exception e) {}
      if (++runc>=50) runc=0;
      
      // Dimming
      if (dimc>0)
      {
        setAlpha(state.alpha+dimInc);
        dimc--;
      }
      
      // Blinking
      if (runc%25==0)
      {
        state.blink=(runc>0)?LCARS.ES_SELECTED:0x00000000;
        for (int i=0; i<elements.size(); i++)
        {
          try
          {
            EElement e = elements.get(i);
            if (e.isBlinking()) e.invalidate(false);
          }
          catch (Exception e) {}
        }
      }
      
      // UI reflections
      if (runc%25==0)
      {
        float a = getAlpha();
        if (eLight !=null) eLight .setDisabled(a>=1.0f);
        if (eDim   !=null) eDim   .setDisabled(a<=0.1f);
        if (eSilent!=null) eSilent.setBlinking(isSilent());
      }
      
      // Set period of load statistics
      if (runc%50==0)
        loadStat.period();
      
      // Update screen
      if (runc%50==0) invalidate();
      if (runc% 2==0) updateScreen();
    }
  }
  
  /**
   * <p><i><b style="color:red">Experimental.</b></i></p>
   *
   * Runs the <code>runnable</code> after a short period of time.
   * 
   * @param runnable
   *          The runnable.
   */
  protected void invokeLater(Runnable runnable)
  {
    final Runnable __runnable = runnable;
    (new Timer()).schedule(new TimerTask()
    {
      @Override
      public void run()
      {
        __runnable.run();
      }
    },10);
    
  }
  
  /**
   * Called 25 times per second. Derived classes may override this method to
   * perform periodic actions. It is <em>not</em> recommended to start own
   * threads for that purpose.
   */
  protected void fps25()
  {
  }
  
  /**
   * Called 10 times per second. Derived classes may override this method to
   * perform periodic actions. It is <em>not</em> recommended to start own
   * threads for that purpose.
   */
  protected void fps10()
  {
  }
  
  /**
   * Called twice per second. Derived classes may override this method to
   * perform periodic actions. It is <em>not</em> recommended to start own
   * threads for that purpose.
   */
  protected void fps2()
  {
  }
  
  /**
   * Called once per second. Derived classes may override this method to
   * perform periodic actions. It is <em>not</em> recommended to start own
   * threads for that purpose.
   */
  protected void fps1()
  {
  }
  
  /**
   * Call this method to notify the framework that the panel needs to be redrawn. 
   */
  public synchronized void invalidate()
  {
    screenInvalid = true;
  }
  
  /**
   * Updates the screen with the current panel and GUI elements state.
   */
  private void updateScreen()
  {
    if (iscreen==null || runt==null) return;
    if (!screenInvalid) return;
    
    screenInvalid = false;
    
    // Decide on incremental update
    boolean incremental = true;
    if (getScreen() instanceof Screen) incremental = false;
    long time = System.nanoTime();
    if (time-fullUpdateTime>1E9)
    {
      fullUpdateTime = time;
      //incremental = false;
    }

    // Make update data
    PanelData data = new PanelData();
    synchronized (this)
    {
      data.panelState = state; // TODO: better make a copy?

      //GImage.beginCacheRun();
      data.elementData = new Vector<ElementData>(elements.size());
      for (EElement element : elements)
      {
        ElementData ed = element.getUpdateData(incremental);
        data.elementData.add(ed);
      }
      //GImage.endCacheRun();
    }
    
    // Update screen
    try
    {
      iscreen.update(data,incremental);
    }
    catch (RemoteException e)
    {
      LCARS.err("PNL", "Error while sending update to screen. Reason: " + e.getMessage());
    }
    time = System.nanoTime()-time;
    loadStat.add((int)(time/400000));
  }
  
  // -- Implementation of the IPanel interface --

  @Override
  public void start()
  {
    if (runt==null)
    {
      runt = new Timer(true);
      runt.scheduleAtFixedRate(new PanelTimerTask(),20,20);
    }
    if (getSpeechEngine()!=null)
      getSpeechEngine().addSpeechEventListener(this);
    invalidate();
  }
  
  @Override
  public void stop()
  {
    if (getSpeechEngine()!=null)
      getSpeechEngine().removeSpeechEventListener(this);
    if (runt!=null)
    {
      runt.cancel();
      runt = null;
    }
  }

  @Override
  public void processTouchEvent(TouchEvent event)
  {
    EEvent ee = new EEvent();
    ee.pt = new Point(event.x,event.y);
    ee.el = elementAt(ee.pt);
    
    switch (event.type)
    {
    case TouchEvent.DOWN:
      ee.id = EEvent.TOUCH_DOWN;
      dragElement = ee.el;
      
      if (ee.el==null)
      {
        if (!isSilent())
        {
          try { getScreen().userFeedback(UserFeedback.Type.DENY); }
          catch (RemoteException e){}
          catch (NullPointerException e){}
        }
        return;
      }

      ee.pt = ee.el.panelToElement(ee.pt);
      UserFeedback.Type ft = ee.el.fireEEvent(ee);
      if (!isSilent() && getScreen()!=null)
      {
        try { getScreen().userFeedback(ft); }
        catch (RemoteException e){}
      }
      break;
    case TouchEvent.UP:
      ee.id = EEvent.TOUCH_UP;
      if (dragElement==null) return;
      if (dragElement.isOverDrag())
      {
        ee.el = dragElement;
        ee.pt = ee.el.panelToElement(ee.pt);
        dragElement.fireEEvent(ee);
        dragElement = null;
      }
      else
      {
        dragElement = null;
        if (ee.el==null) return;
        ee.pt = ee.el.panelToElement(ee.pt);
        ee.el.fireEEvent(ee);
      }
      break;
    case TouchEvent.DRAG:
      if (dragElement==null) return;
      if (dragElement.isOverDrag())
      {
        ee.el = dragElement;
        ee.id = EEvent.TOUCH_DRAG;
        ee.pt = ee.el.panelToElement(ee.pt);
        dragElement.fireEEvent(ee);      
      }
      else
      {
        ee.id = ee.el==dragElement?EEvent.TOUCH_DRAG:EEvent.TOUCH_UP;
        ee.el = dragElement;
        ee.pt = ee.el.panelToElement(ee.pt);
        dragElement.fireEEvent(ee);
        dragElement=ee.el;
      }
      break;
    }
  }

  /**
   * Returns the panel's shape. The default shape is a 1920 x 1080 rectangle. Derived classes may
   * override this method to provide other shapes.
   */
  protected Area getShape()
  {
    if (this instanceof PaddMainPanel)
      return new Area(new Rectangle(0,0,1366,768));
    else
      return new Area(new Rectangle(0,0,1920,1080));
  }
  
  /**
   * Returns the rectangular dimension of this panel.
   * 
   * @return The dimension in LCARS panel coordinates.
   */
  protected final Dimension getDimension()
  {
    Area shape = getShape();
    return new Dimension(shape.getBounds().width,shape.getBounds().height);
  }  
 
  @Override
  public void processKeyEvent(KeyEvent event)
  {
    for (KeyListener listener : keyListeners)
    {
      switch(event.getID())
      {
      case KeyEvent.KEY_TYPED:
        listener.keyTyped(event);
        break;
      case KeyEvent.KEY_PRESSED:
        listener.keyPressed(event);
        break;
      case KeyEvent.KEY_RELEASED:
        listener.keyReleased(event);
        break;
      }
      if (event.isConsumed()) break;
    }
  }

  // -- Implementation of the EEventListener interface --

  public void touchDown(EEvent ee)
  {
    if      (ee.el==eLight) touchHold(ee);
    else if (ee.el==eLight) touchHold(ee);
    else if (ee.el==eSilent) setSilent(!isSilent());
  }

  public void touchDrag(EEvent ee)
  {
  }

  public void touchHold(EEvent ee)
  {
    if (ee.el==eLight)
    {
      float a = getAlpha()+0.04f;
      if (a>1f) a=1f;
      setAlpha(a);
    }
    else if (ee.el==eDim)
    {
      float a = getAlpha()-0.04f;
      if (a<0.1f) a=0.1f;
      setAlpha(a);
    }
  }

  public void touchUp(EEvent ee)
  {
  }
  
}

//EOF
