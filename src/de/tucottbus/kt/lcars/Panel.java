package de.tucottbus.kt.lcars;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.Area;
import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import de.tucottbus.kt.lcars.contributors.EMessageBox;
import de.tucottbus.kt.lcars.contributors.EMessageBoxListener;
import de.tucottbus.kt.lcars.contributors.EPanelSelector;
import de.tucottbus.kt.lcars.elements.EElement;
import de.tucottbus.kt.lcars.elements.EEvent;
import de.tucottbus.kt.lcars.elements.EEventListener;
import de.tucottbus.kt.lcars.elements.ELabel;
import de.tucottbus.kt.lcars.elements.ElementData;
import de.tucottbus.kt.lcars.feedback.UserFeedback;
import de.tucottbus.kt.lcars.logging.Log;
import de.tucottbus.kt.lcars.speech.ISpeechEngine;
import de.tucottbus.kt.lcars.speech.ISpeechEventListener;
import de.tucottbus.kt.lcars.speech.events.SpeechEvent;
import de.tucottbus.kt.lcars.swt.ImageMeta;
import de.tucottbus.kt.lcars.util.LoadStatistics;
import de.tucottbus.kt.lcars.util.Objectt;

/**
 * An LCARS panel. A panel represents the contents and semantics of a graphical,
 * haptic, acoustic and speech user interface. Panels are physically displayed
 * at {@link Screen LCARS screens}. Screen and panel do not necessarily run on
 * the same virtual or physical machine.
 * 
 * @see Screen
 * @see APanel
 * @author Matthias Wolff
 */
public class Panel implements IPanel, EEventListener, ISpeechEventListener
{
  // -- Static fields --

  private static AtomicInteger serialGenCount = new AtomicInteger(0);

  /**
   * HACK: should not be necessary!
   */
  private static boolean speechEngineSearched;

  /**
   * Flag suppressing recurring network errors when connection to remote screen was 
   * lost (no important function, just for beautification).
   */
  private boolean noConnectionOnUpdate = false;
  
  // -- Fields --

  /**
   * The unique serial number of the {@link Panel} described by this instance.
   */
  public final int serialNo;

  /**
   * The screen this panel is running on.
   */
  private IScreen iscreen;

  /**
   * The speech engine working for this panel.
   */
  private static ISpeechEngine speechEngine;

  /**
   * The list of {@linkplain EElement elements} on this panel.
   */
  private final ArrayList<EElement> elements;

  /**
   * The set of elements known by the screen
   */
  private final HashSet<EElement> addedElements;

  /**
   * The panel state.
   */
  private PanelState state;

  /**
   * The list of key listeners registered with this panel.
   */
  private final Vector<KeyListener> keyListeners;

  /**
   * Time of the last full screen update (as obtained by
   * {@link System#nanoTime()}.
   */
  private long fullUpdateTime;

  /**
   * The panel load statistics.
   */
  private LoadStatistics loadStat;

  // --Dragged elements--//

  /**
   * Used to synchronize touch events
   */
  private final ArrayList<EElement> dragTouch = new ArrayList<>();

  private EElement dragMouse;

  /**
   * Flag indicating that the screen needs to be redrawn.
   */
  private final AtomicBoolean screenInvalid;

  private EMessageBox eMsgBox;
  private EPanelSelector ePnlSel;
  private ELabel eTitle;
  private EElement eLight;
  private EElement eDim;
  private EElement eSilent;
  private EElement eLoadStat;
  private Timer runt;
  private int runc;
  private int dimc;
  private float dimInc = 0.05f;

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
    if (clazz == null)
      return null;
    String n = "";
    String s = clazz.getSimpleName();
    if (s.endsWith("Panel"))
      s = s.substring(0, s.length() - 5);
    for (int i = 0; i < s.length(); i++)
    {
      char c = s.charAt(i);
      if (i > 0 && Character.isUpperCase(c))
        n += " ";
      n += c;
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
    this.serialNo = serialGenCount.getAndIncrement();
    this.iscreen = iscreen;
    this.elements = new ArrayList<EElement>(200);
    this.addedElements = new HashSet<EElement>(20);
    this.state = new PanelState(getDimension());
    this.keyListeners = new Vector<KeyListener>();
    this.loadStat = new LoadStatistics(25);
    this.screenInvalid = new AtomicBoolean(true);
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

    if (className == null || className == Panel.class.getName())
    {
      panel = new Panel(iscreen);
      panel.panelSelectionDialog();
    } else
    {
      Class<?> panelClass = (className != null) ? Class.forName(className)
          : Panel.class;
      try
      {
        Object[] args =
        { iscreen };
        panel = (Panel) panelClass.getConstructors()[0].newInstance(args);
      } catch (Exception e)
      {
        Log.err("Could not create panel \"" + className + "\".", e);
        return null;
      }
    }
    LCARS.setPanelDimension(panel.getDimension());
    panel.start();
    return panel;
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

    int style = LCARS.EC_HEADLINE | LCARS.EF_HEAD1 | LCARS.ES_LABEL_NE
        | LCARS.ES_STATIC;
    if (eTitle == null)
    {
      eTitle = new ELabel(this, dim.width - 523, 6, 500, 0, style, null);
      add(eTitle);
    }
    try
    {
      eTitle.setLabel(guessName(getClass()).toUpperCase());
    } catch (Exception e)
    {
      Log.err("Cannot initiate panel.", e);
    }
    setBackground(new ImageMeta.Resource(LCARS.getArg("--wallpaper=")));

    eMsgBox = new EMessageBox((dim.width - 600) / 2, (dim.height - 280) / 2,
        600, 280);
    ePnlSel = new EPanelSelector((dim.width - 800) / 2, (dim.height - 480) / 2,
        800, 480);
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
   *          <code>true</code> to prevent the documentation to be re-styled to
   *          the LCARS look.
   */
  public void doc(boolean noRestyleHtml)
  {
    try
    {
      getScreen().setPanel(HelpPanel.class.getName());
      HelpPanel hp = (HelpPanel) getScreen().getPanel();
      hp.setDocs(this.getClass(), getDocIndex(), noRestyleHtml);
      hp.loadDoc();
    } catch (ClassNotFoundException | RemoteException e)
    {
      Log.err("Cannot display the documentation root for the panel.", e);
    }
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
   *          <code>true</code> to prevent help to be re-styled to the LCARS
   *          look.
   */
  public void help(boolean noRestyleHtml)
  {
    try
    {
      getScreen().setPanel(HelpPanel.class.getName());
      HelpPanel hp = (HelpPanel) getScreen().getPanel();
      hp.setDocs(this.getClass(), getDocIndex(), noRestyleHtml);
      hp.loadHelp();
    } catch (ClassNotFoundException | RemoteException e)
    {
      Log.err("Cannot load help panel", e);
    }
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
    if (iscreen != null)
      this.iscreen = iscreen;
  }

  /**
   * Returns an interface to the {@linkplain IScreen LCARS screen} on which this
   * panel is running.
   */
  public IScreen getScreen()
  {
    return this.iscreen;
  }

  /**
   * Returns the speech engine working for this panel or <code>null</code> if
   * there is no speech input/output.
   */
  public static ISpeechEngine getSpeechEngine()
  {
    if (!speechEngineSearched)
    {
      speechEngineSearched = true;
      if (LCARS.getArg("--nospeech") != null)
        return null;
      Log.info("Looking for a speech engine");

      // List available speech engine implementations
      Vector<Class<?>> cEngines = LCARS.getSpeechEngines();
      if (cEngines.size() == 0)
      {
        Log.warn("No speech engines found");
        return null;
      }

      // HACK: Just get the first one...
      Class<?> cEngine = cEngines.get(0);
      Log.info("ISpeechEngine = " + cEngine.getName());
      try
      {
        Method mGetInstance = cEngine.getMethod("getInstance");
        speechEngine = (ISpeechEngine) mGetInstance.invoke(null);
      } catch (Throwable e)
      {
        Log.err("Cannot load speech engine.", e);
      }
      Log.info("Speech engine found.");
    }
    return speechEngine;
  }

  public static void disposeSpeechEngine()
  {
    if (speechEngine != null)
      speechEngine.stop();
    speechEngine = null;
  }

  /**
   * Returns the panel's title.
   */
  public String getTitle()
  {
    return eTitle != null ? eTitle.getLabel() : null;
  }

  /**
   * Sets the panel's title.
   * 
   * @param title
   *          The title, can be <code>null</code> which will hide the title.
   */
  public void setTitle(String title)
  {
    String s = title != null ? title : "";
    if (eTitle != null)
      eTitle.setLabel(s.toUpperCase());
  }

  /**
   * Sets the {@link ELabel} displaying the panel title.
   * 
   * @param eTitle
   *          The label element.
   */
  public void setTitleLabel(ELabel eTitle)
  {
    if (this.eTitle != null)
      remove(this.eTitle);
    this.eTitle = eTitle;
    if (this.eTitle != null)
      add(this.eTitle);
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
   *          The new color style, one of the {@link LCARS}<code>.CS_XXX</code>
   *          constants.
   */
  public void setColorScheme(int colorScheme)
  {
    if (state.colorScheme == colorScheme)
      return;
    state.colorScheme = colorScheme;
    invalidate();
  }

  /**
   * Returns the resource name of the background image of this panel. Can be
   * <code>null</code>.
   */
  public ImageMeta getBackground()
  {
    return state.bgImage;
  }

  /**
   * Sets the resource name of the background image of this panel.
   * 
   * @param bgImage
   *          The resource file name (<code>package/file.ext</code>) of the
   *          background image. The package separator '.' must be replaced by a
   *          slash '/'. Can be <code>null</code> for no background image. PNG,
   *          GIF and JPG image files are acceptable.
   */
  public void setBackground(ImageMeta bgImageRes)
  {
    if (Objectt.equals(bgImageRes, state.bgImage))
      return;
    state.bgImage = bgImageRes;
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
   * @param alpha
   *          the alpha value, 0.0f (transparent) through 1.0f (opaque)
   */
  public void setAlpha(float alpha)
  {
    if (alpha < 0.f)
      alpha = 0.f;
    if (alpha > 1.f)
      alpha = 1.f;
    this.state.alpha = alpha;
    invalidate();
  }

  /**
   * Determines if this panel is locked.
   * 
   * @see #setLocked(boolean,int)
   */
  public boolean isLocked()
  {
    return this.state.locked;
  }

  /**
   * Locks or unlocks this panel. In the locked state only {@linkplain EElement
   * GUI elements} which have the {@link LCARS#ES_NOLOCK} style process user
   * input. If the argument is <code>false</code> and
   * {@link #setAutoRelockTime(int)} has been called to with a non-negative
   * argument before, the panel will automatically re-enter the locked state
   * when a specified GUI idle time has passed.
   * 
   * <p>
   * <b>Caution:</b> Make sure you set the {@link LCARS#ES_NOLOCK} style on a
   * panel lock/unlock button! Otherwise users will not be able to unlock the
   * panel.
   * </p>
   * 
   * @param locked
   *          The new modal mode.
   * @see #isLocked()
   * @see #setAutoRelockTime(int)
   */
  public void setLocked(boolean locked)
  {
    clearTouch();
    this.state.locked = locked;
    if (locked && this.state.autoRelockTime > 0)
      this.state.autoRelock = this.state.autoRelockTime;
  }

  /**
   * Enables or disabled automatic re-locking after unlocking a panel.
   * 
   * @param time
   *          The GUI idle time in seconds after which the panel will
   *          automatically re-enter the locked state. The GUI idle time is the
   *          interval that passed after the last user input. If the argument is
   *          &le; 0, the panel will remain unlocked until
   *          {@link #setLocked(boolean, int) setLocked}<code>(true)</code> is
   *          invoked.
   * @see #getAutoRelockTime()
   * @see #getAutoRelock()
   * @see #setLocked(boolean)
   */
  public void setAutoRelockTime(int time)
  {
    this.state.autoRelockTime = time;
    if (!isLocked())
      this.state.autoRelock = time;
  }

  /**
   * Returns the GUI idle time in seconds after which the panel will
   * automatically re-locked. A return value &le;0 indicates that the panel will
   * not automatically re-lock.
   * 
   * @see #setAutoRelockTime(int)
   * @see #getAutoRelock()
   */
  public int getAutoRelockTime()
  {
    return this.state.autoRelockTime;
  }

  /**
   * Returns the remaining GUI idle time until the panel will automatically
   * re-lock.
   * 
   * @return The remaning time. Return value is meaningless if
   *         {@link #isLocked()} returns <code>true</code> <em>or</em>
   *         {@link #getAutoRelockTime()} returns a value &le;0.
   */
  public int getAutoRelock()
  {
    return this.state.autoRelock;
  }

  /**
   * Internal use only! Breaks the auto re-lock countdown.
   * 
   * @see #setAutoRelockTime(int)
   */
  public void breakAutoRelock()
  {
    if (isLocked())
      return;
    if (getAutoRelockTime() <= 0)
      return;
    this.state.autoRelock = getAutoRelockTime();
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
   * Switches the modal mode on or off. In the modal mode only
   * {@linkplain EElement GUI elements} which have the {@link LCARS#ES_MODAL}
   * style process user input. The modal mode is useful for dialogs or message
   * boxes displayed on top of a panel.
   * 
   * @param modal
   *          The new modal mode.
   * @see #isModal()
   */
  public void setModal(boolean modal)
  {
    clearTouch();
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
   * Switches the silent mode on or off. In the silent mode the pabel does not
   * play any earcons.
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
   * @param eLight
   *          the light control
   * @param eDim
   *          the dim control
   */
  public void setDimContols(EElement eLight, EElement eDim)
  {
    if (this.eLight != null)
      this.eLight.removeEEventListener(this);
    if (this.eDim != null)
      this.eDim.removeEEventListener(this);
    this.eLight = eLight;
    this.eLight.addEEventListener(this);
    this.eDim = eDim;
    this.eDim.addEEventListener(this);
  }

  /**
   * Sets the control for toggling earcons on and off.
   * 
   * @param eSilent
   *          the control
   */
  public void setSilentControl(EElement eSilent)
  {
    if (this.eSilent != null)
      this.eSilent.removeEEventListener(this);
    this.eSilent = eSilent;
    this.eSilent.addEEventListener(this);
  }

  /**
   * Sets the control for displaying the panel and screen computational load
   * statistics. The statistics will display in the form
   * <code>RRR-UU/SSS-VV</code> with the following elements:
   * <table>
   *   <tr><th>Field</th><th>Description</th></tr>
   *   <tr><td><code>RRR</code></td><td>
   *       Average real-time factor of creating one panel update
   *     </td>
   *   </tr>
   *   <tr><td><code>UU</code></td><td>
   *       Average panel update rate (number of updates per second)
   *     </td>
   *   </tr>
   *   <tr><td><code>SSS</code></td><td>
   *       Average real-time factor of rendering the LCARS screen
   *     </td>
   *   </tr>
   *   <tr><td><code>VV</code></td><td>
   *       Average screen update rate (number of frames per second)
   *     </td>
   *   </tr>
   * </table>
   * 
   * @param eLoadStat
   *          The control, can be <code>null</code>.
   * @see #getLoadStatistics()
   * @see IScreen#getLoadStatistics()
   */
  public void setLoadStatControl(EElement eLoadStat)
  {
    this.eLoadStat = eLoadStat;
  }
  
  /**
   * Calls {@link EElement#clearTouch()} of all {@link Panel#elements} to reset
   * the touch state to not touched
   */
  protected void clearTouch()
  {
    synchronized (elements)
    {
      for (EElement el : elements)
        el.clearTouch();
    }
  }

  /**
   * Returns a vector of the LCARS GUI {@link EElement}s contained in this
   * panel.
   * 
   * @return the vector
   */
  public ArrayList<EElement> getElements()
  {
    synchronized (elements)
    {
      return new ArrayList<EElement>(elements);
    }
  }

  /**
   * Returns the computational load statistics of the panel. The load depends on
   * the number and complexity of the GUI elements.
   * <ul>
   * <li>Call {@link LoadStatistics#getLoad()} on the return value to determine
   * the average percentage of 40 milliseconds consumed by
   * {@linkplain EElement#validateGeometry() creating geometries} and
   * {@linkplain #updateScreen() submitting them to the screen}. A return value
   * of more than 100 % indicates possible display juddering and the consumption
   * of the entire capacity of one processor core if 25 frames per second are
   * painted.</li>
   * <li>Call {@link LoadStatistics#getEventsPerPeriod()} on the return value to
   * determine the actual frame rate in the last complete second.</li>
   * </ul>
   * 
   * @see #setLoadStatControl(EElement)
   */
  public LoadStatistics getLoadStatistics()
  {
    return loadStat;
  }

  /**
   * Sets the list of classes to be displayed on the {@linkplain EPanelSelector
   * panel selector dialog}.
   * 
   * @param list
   *          A list of panel classes, can be <code>null</code> in which case
   *          the list of {@link MainPanel}s is displayed.
   */
  public void setPanelSelectorList(AbstractList<Class<? extends Panel>> list)
  {
    ePnlSel.setPanelList(list);
  }

  // -- GUI element management --

  /**
   * Adds a new LCARS GUI element to the panel. This does <em>not</em> trigger a
   * repaint of the panel!
   * 
   * @param el
   *          the element
   */
  public <T extends EElement> T add(T el)
  {
    if (el == null)
      return null;
    synchronized (elements)
    {
      doAdd(el);
    }
    return el;
  }

  private void doAdd(EElement el)
  {
    if (!doRemove(el))
      addedElements.add(el);
    elements.add(el);
  }

  /**
   * Adds a range of new LCARS GUI elements to the panel. This does <em>not</em>
   * trigger a repaint of the panel!
   * 
   * @param el
   *          the element
   */
  public void addAll(Collection<EElement> elements)
  {
    if (elements == null)
      throw new NullPointerException("elements");
    if (elements.isEmpty())
      return;

    synchronized (this.elements)
    {
      for (EElement el : elements)
        doAdd(el);
    }
  }

  /**
   * <em>NOT SYNCHRONIZED</em> variant of {@link Panel#remove(EElement)}
   * 
   * @param el
   */
  private boolean doRemove(EElement el)
  {
    addedElements.remove(el);
    return elements.remove(el);
  }

  /**
   * Removes an LCARS GUI element from the panel. This does <em>not</em> trigger
   * a repaint of the panel!
   * 
   * @param el
   *          the element
   */
  public boolean remove(EElement el)
  {
    synchronized (elements)
    {
      return doRemove(el);
    }
  }

  /**
   * Removes a range of LCARS GUI elements from the panel. This does
   * <em>not</em> trigger a repaint of the panel!
   * 
   * @param el
   *          the element
   */
  public void removeAll(Collection<EElement> elements)
  {
    synchronized (this.elements)
    {
      for (EElement el : elements)
        doRemove(el);
    }
  }

  /**
   * Removes a range and adds a range of LCARS GUI elements synchronous from/to
   * the panel. This does <em>not</em> trigger a repaint of the panel!
   * 
   * @param remove
   * @param add
   */
  public void removeAllAndAddAll(Collection<EElement> remove,
      Collection<EElement> add)
  {
    synchronized (this.elements)
    {
      for (EElement el : remove)
        doRemove(el);
      for (EElement el : add)
        doAdd(el);
    }
  }

  /**
   * Return the LCARS GUI element at the specified position (panel coordinates).
   * Use {@link Screen#componentToPanel(Point)} to convert screen coordinates
   * (e.g. from mouse events) to panel coordinates.
   * 
   * @param pt
   *          Panel coordinates.
   * @return An LCARS GUI {@link EElement} or <code>null</code> if there is no
   *         element at this position
   */
  public EElement elementAt(Point pt)
  {
    synchronized (this.elements)
    {
      if (isModal())
        return doElementAt(pt, true);

      EElement el = doElementAt(pt, true);
      if (el == null)
        el = doElementAt(pt, false);
      return el;
    }
  }

  /**
   * <em>NOT SYNCHRONIZED</em> variant of
   * {@link Panel#elementAt(Point, boolean)}
   * 
   * @param pt
   * @param modal
   * @return
   */
  private EElement doElementAt(Point pt, boolean modal)
  {
    for (int i = elements.size() - 1; i >= 0; i--)
    {
      EElement el = elements.get(i);
      if (el.isModal() != modal)
        continue;
      if (el.isStatic())
        continue;

      Area es = new Area();
      el.getArea(es);
      if (es.contains(pt))
        return el;
    }
    return null;
  }

  /**
   * Return the LCARS GUI element at the specified position (panel coordinates).
   * Use {@link Screen#componentToPanel(Point)} to convert screen coordinates
   * (e.g. from mouse events) to panel coordinates.
   * 
   * @param pt
   *          The panel coordinates.
   * @param modal
   *          If <code>true</code> consider {@link EElement#isModal() modal}
   *          {@link EElement}s only, otherwise consider non-modal
   *          {@link EElement}s only.
   * @return An LCARS GUI {@link EElement} or <code>null</code> if there is no
   *         element at this position
   */
  protected EElement elementAt(Point pt, boolean modal)
  {
    synchronized (elements)
    {
      return doElementAt(pt, modal);
    }
  }

  // -- Operations --

  /**
   * Displays a message box. The user's answer will be dispatched to the
   * specified {@link EMessageBoxListener}.
   * 
   * @param title
   *          the title
   * @param msg
   *          the message text
   * @param bn1
   *          the left button text
   * @param bn2
   *          the right button text
   * @param l
   *          the message box listener
   */
  public void messageBox(String title, String msg, String bn1, String bn2,
      EMessageBoxListener l)
  {
    eMsgBox.addListener(l);
    eMsgBox.open(this, title, msg, bn1, bn2);
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
    dimInc = Math.abs(dimInc) * ((alpha > this.state.alpha) ? 1f : -1f);
    dimc = Math.abs((int) ((alpha - this.state.alpha) / this.dimInc)) + 1;
  }

  private void err(Exception e)
  {
    Log.err("Error in Panel execution.", e);
  }

  /**
   * Call this method to notify the framework that the panel needs to be
   * redrawn.
   */
  public void invalidate()
  {
    screenInvalid.set(true);
  }

  /**
   * Updates the screen with the current panel and GUI elements state.
   */
  private void updateScreen()
  {
    if (iscreen == null || runt == null)
      return;
    boolean invalid = screenInvalid.getAndSet(false);
    if (!invalid)
      return;

    // Update load statistics display
    if (eLoadStat!=null)
    {
      LoadStatistics lsp = getLoadStatistics();
      String s = String.format("%03d-%02d",lsp.getLoad(),lsp.getEventsPerPeriod());
      try
      {
        LoadStatistics lss = getScreen().getLoadStatistics();
        s += String.format("/%03d-%02d",lss.getLoad(),lss.getEventsPerPeriod());
      }
      catch (Exception e) {}
      try
      {
        eLoadStat.setLabel(s);
      }
      catch (Exception e) {}
    }

    // Decide on incremental update
    boolean incremental = !(getScreen() instanceof Screen);
    /*incremental = true; // Force incremental */
    long time = System.nanoTime();
    if (time - fullUpdateTime > 1E9)
    {
      fullUpdateTime = time;
      incremental = false;
    }

    try
    {
      // Make update data
      ElementData[] els;
      int i = 0;
      synchronized (this.elements)
      {
        els = new ElementData[this.elements.size()];
        for (EElement el : this.elements)
          try
          {
            els[i++] 
              = el.getUpdateData(incremental && !this.addedElements.contains(el));
          }
          catch (Exception e)
          {
            Log.err("Failed to get update data for element "+el,e);
          }
        this.addedElements.clear();
      }

      // Update screen
      PanelData data = new PanelData(this, state, els);
      iscreen.update(data, incremental);
      noConnectionOnUpdate = false;
    } 
    catch (RemoteException e)
    {
      if (!noConnectionOnUpdate)
        Log.err("Remote screen of "+getClass().getSimpleName()
          +" not updated (no connection).",e);
      noConnectionOnUpdate = true;
    }
    catch (Exception e)
    {
      Log.err("Failed to make screen update data.",e);
    }

    time = System.nanoTime() - time;
    loadStat.add((int) (time / 400000));
  }
  
  // -- Keyboard event  handling --

  /**
   * Adds the specified key listener to receive key events from this panel.
   * 
   * @param listener
   *          The key listener. If <code>null</code> or if the specified
   *          listened is already registered, the method does nothing.
   */
  public void addKeyListener(KeyListener listener)
  {
    if (keyListeners.indexOf(listener) >= 0)
      return;
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
      if (runc % 2 == 0)
      {
        try { fps25(); } catch (Exception e) {err(e); }
        firePanelTimer((listener)->listener.fps25());
      }
      if (runc % 5 == 0)
      {
        if (eLoadStat!=null)
          try
          {
            getScreen().getLoadStatistics();
          }
          catch (Exception e) {}
        try { fps10(); } catch (Exception e) { err(e); }
        firePanelTimer((listener)->listener.fps10());
      }
      if (runc % 25 == 0)
      {
        try { fps2(); } catch (Exception e) { err(e); }
        firePanelTimer((listener)->listener.fps2());
      }
      if (runc % 50 == 0)
      {
        try { fps1(); } catch (Exception e) { err(e); }
        firePanelTimer((listener)->listener.fps1());
      }
      if (++runc >= 50)
        runc = 0;

      // Dimming
      if (dimc > 0)
      {
        setAlpha(state.alpha + dimInc);
        dimc--;
      }

      // Blinking
      if (runc % 25 == 0)
      {
        state.blink = (runc > 0) ? LCARS.ES_SELECTED : 0x00000000;

        synchronized (elements)
        {
          for (EElement el : elements)
            if (el.isBlinking())
              el.invalidate(false);
        }
      }

      // Automatic re-locking
      if (runc % 50 == 0 && !isLocked() && state.autoRelock > 0)
      {
        state.autoRelock--;
        if (state.autoRelock == 0)
          setLocked(true);
      }

      // UI reflections
      if (runc % 25 == 0)
      {
        float a = getAlpha();
        if (eLight != null)
          eLight.setDisabled(a >= 1.0f);
        if (eDim != null)
          eDim.setDisabled(a <= 0.1f);
        if (eSilent != null)
          eSilent.setBlinking(isSilent());
      }

      // Set period of load statistics
      if (runc % 50 == 0)
        loadStat.period();

      // Update screen
      if (runc % 50 == 0)
        invalidate();
      if (runc % 2 == 0)
        updateScreen();
    }
  }
  
  /**
   * Called 25 times per second as long as the panel {@linkplain #isRunning() is
   * running}. Derived classes may override this method to perform periodic
   * actions. The base class implementation does nothing.
   */
  protected void fps25()
  {
  }

  /**
   * Called 10 times per second as long as the panel {@linkplain #isRunning() is
   * running}. Derived classes may override this method to perform periodic
   * actions. The base class implementation does nothing.
   */
  protected void fps10()
  {
  }

  /**
   * Called twice per second as long as the panel {@linkplain #isRunning() is
   * running}. Derived classes may override this method to perform periodic
   * actions. The base class implementation does nothing.
   */
  protected void fps2()
  {
  }

  /**
   * Called once per second as long as the panel {@linkplain #isRunning() is
   * running}. Derived classes may override this method to perform periodic
   * actions. The base class implementation does nothing.
   */
  protected void fps1()
  {
  }

  /**
   * List of panel timer listeners.
   */
  private ArrayList<IPanelTimerListener> timerListeners = null;
  
  /**
   * Adds a panel timer listener.
   * 
   * @param listener
   *          The listener.
   */
  public void addPanelTimerListener(IPanelTimerListener listener)
  {
    if (listener==null)
      return;
    if (timerListeners==null)
      timerListeners = new ArrayList<IPanelTimerListener>();
    if (timerListeners.contains(listener))
      return;
    timerListeners.add(listener);
  }
 
  /**
   * Removes a panel timer listener.
   * 
   * @param listener
   *          The listener.
   */
  public void removePanelTimerListener(IPanelTimerListener listener)
  {
    if (listener==null || timerListeners==null)
      return;
    timerListeners.remove(listener);  
  }
  
  /**
   * Removes all panel timer listener.
   * 
   * @param listener
   *          The listener.
   */
  protected void removeAllPanelTimerListeners()
  {
    timerListeners.clear();
  }
  
  /**
   * Fires a panel timer event.
   * 
   * @param action
   *          A consumer being fed with all registered
   *          {@linkplain #timerListeners panel timer listeners}.
   */
  private void firePanelTimer(Consumer<IPanelTimerListener> action)
  {
    if (timerListeners==null)
      return;
    for (IPanelTimerListener listener : timerListeners)
      try 
      {
        action.accept(listener);
      } 
      catch (Exception e) 
      {
        err(e); 
      }
  }

  // -- Implementation of the IPanel interface --

  @Override
  public void start()
  {
    if (runt == null)
    {
      (runt = new Timer(getClass().getSimpleName()+".runt (panel timer)", true))
          .scheduleAtFixedRate(new PanelTimerTask(), 20, 20);
    }
    if (getSpeechEngine() != null)
      getSpeechEngine().addSpeechEventListener(this);
    invalidate();
  }

  @Override
  public void stop()
  {
    if (getSpeechEngine() != null)
      getSpeechEngine().removeSpeechEventListener(this);
    if (runt == null)
      return;
    runt.cancel();
    runt.purge();
    runt = null;
  }

  @Override
  public boolean isRunning() 
  {
    return runt!=null;
  }
  
  @Override
  public void processTouchEvents(TouchEvent[] events)
  {
    if (events.length == 0) return;
    int i = 0;

    synchronized (dragTouch)
    {
      boolean clearDragTouch = true;
      for (TouchEvent event : events)
      {
        EEvent ee = new EEvent();
        ee.pt = new Point(event.x, event.y);

        switch (event.type)
        {
        case TouchEvent.DOWN:
          ee.id = EEvent.TOUCH_DOWN;
          ee.el = elementAt(ee.pt);
          if (event.isMouseEvent){
            this.dragMouse = ee.el;
          }
          else{
            clearDragTouch = false;
            this.dragTouch.add(ee.el);
          }

          if (ee.el == null)
          {
            if (!isSilent())
              try
              {
                getScreen().userFeedback(UserFeedback.Type.DENY);
              } catch (RemoteException | NullPointerException e)
              {
                Log.err("Cannot perform audio-visual user feedback.", e);
              }
            return;
          }

          ee.pt = ee.el.panelToElement(ee.pt);
          UserFeedback.Type ft = ee.el.fireEEvent(ee);
          if (!isSilent() && getScreen() != null)
            try
            {
              getScreen().userFeedback(ft);
            } catch (RemoteException e)
            {
              Log.err("Performing an audio-visual user feedback failed", e);
            }
          break;
        case TouchEvent.UP:
          ee.id = EEvent.TOUCH_UP;
          EElement de;
          if (event.isMouseEvent)
          {
            de = this.dragMouse;
            this.dragMouse = null;
          } else{
            if (i >= this.dragTouch.size()) continue; //TODO: should never occur
            de = this.dragTouch.remove(i);
          }

          if (de == null)
            return;
          ee.el = de;
          ee.pt = ee.el.panelToElement(ee.pt);
          de.fireEEvent(ee);
          break;
        case TouchEvent.DRAG:
          EElement dragElement;
          if (event.isMouseEvent)
           dragElement = this.dragMouse;
          else {
            if (i > this.dragTouch.size())
              continue;
            try
            {
              dragElement = this.dragTouch.get(i++);
            }
            catch (IndexOutOfBoundsException e)
            {
              dragElement = null;
            }
          }
          clearDragTouch &= event.isMouseEvent;
          
          if (dragElement == null)
            return;
          boolean inBounds = ee.el == dragElement;

          if (inBounds || dragElement.isOverDrag())
          {
            ee.el = dragElement;
            ee.id = EEvent.TOUCH_DRAG;
            ee.pt = ee.el.panelToElement(ee.pt);
            dragElement.fireEEvent(ee);

            if (inBounds)
            {
              ee = new EEvent();
              ee.el = dragElement;
              ee.id = EEvent.TOUCH_HOLD;
              ee.pt = ee.el.panelToElement(ee.pt);
              dragElement.fireEEvent(ee);
            }
            this.dragMouse = ee.el;
          }
          break;
        }
      }
      if (clearDragTouch)
        dragTouch.clear();
    }

  }

  /**
   * Returns the panel's shape. The default shape is a 1920 x 1080 rectangle.
   * Derived classes may override this method to provide other shapes.
   */
  protected Area getShape()
  {
    return new Area(this instanceof PaddMainPanel
        ? new Rectangle(0, 0, 1366, 768) : new Rectangle(0, 0, 1920, 1080));
  }

  /**
   * Returns the rectangular dimension of this panel.
   * 
   * @return The dimension in LCARS panel coordinates.
   */
  protected final Dimension getDimension()
  {
    Rectangle bounds = getShape().getBounds();
    return new Dimension(bounds.width, bounds.height);
  }

  @Override
  public void processKeyEvent(KeyEvent event)
  {
    switch (event.getID())
    {
    case KeyEvent.KEY_PRESSED:
      for (KeyListener listener : keyListeners)
      {
        listener.keyPressed(event);
        if (event.isConsumed())
          return;
      }
      break;
    case KeyEvent.KEY_RELEASED:
      for (KeyListener listener : keyListeners)
      {
        listener.keyReleased(event);
        if (event.isConsumed())
          return;
      }
      break;
    case KeyEvent.KEY_TYPED: // TODO: never occurs because not supported in swt,
                             // see Screen
      for (KeyListener listener : keyListeners)
      {
        listener.keyTyped(event);
        if (event.isConsumed())
          return;
      }
      break;
    }
  }

  // -- Implementation of the EEventListener interface --

  @Override
  public void touchDown(EEvent ee)
  {
    if (ee.el == eLight)
      touchHold(ee);
    else if (ee.el == eLight)
      touchHold(ee); // FIXME: remove?
    else if (ee.el == eSilent)
      setSilent(!isSilent());
  }

  @Override
  public void touchDrag(EEvent ee)
  {
  }

  @Override
  public void touchHold(EEvent ee)
  {
    if (ee.el == eLight)
    {
      float a = getAlpha() + 0.04f;
      if (a > 1f)
        a = 1f;
      setAlpha(a);
    } else if (ee.el == eDim)
    {
      float a = getAlpha() - 0.04f;
      if (a < 0.1f)
        a = 0.1f;
      setAlpha(a);
    }
  }

  @Override
  public void touchUp(EEvent ee)
  {
  }

  @Override
  public final int hashCode()
  {
    return serialNo;
  }

  @Override
  public final int serialNo() throws RemoteException
  {
    return serialNo;
  }

  @Override
  public String getElementInfo(int serialNo) throws RemoteException
  {
    synchronized (this.elements)
    {
      for (EElement el : this.elements)
        if (el.getSerialNo() == serialNo)
          return el.toString();
    }
    return null;

  }
}

// EOF
