package de.tucottbus.kt.lcars.contributors;

import java.awt.Rectangle;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import de.tucottbus.kt.lcars.IPanelTimerListener;
import de.tucottbus.kt.lcars.Panel;
import de.tucottbus.kt.lcars.elements.EElement;
import de.tucottbus.kt.lcars.elements.EEvent;
import de.tucottbus.kt.lcars.elements.EEventListener;

/**
 * Contributes elements to an LCARS {@link Panel}.
 * 
 * @author Matthias Wolff
 */
public abstract class ElementContributor implements EEventListener
{
  private transient WeakReference<Panel>           panel;
  private final     ArrayList<EElement>            elements;
  protected final   int                            x;
  protected final   int                            y;
  private final     Vector<EEventListener>         listeners;
  private final     Collection<ElementContributor> children;
  private final     IPanelTimerListener            panelTimerListener;

  // -- Constructors --

  /**
   * Abstract constructor of element contributors.
   * 
   * @param x
   *          The x-coordinate of the top-left corner in LCARS panel pixels
   * @param y
   *          The y-coordinate of the top-left corner in LCARS panel pixels
   */
  public ElementContributor(int x, int y)
  {
    elements   = new ArrayList<EElement>();
    this.x     = x;
    this.y     = y;
    listeners  = new Vector<EEventListener>();
    timerTasks = new HashMap<String,TimerTask>();
    panel      = new WeakReference<Panel>(null);
    children   = new ArrayList<ElementContributor>();
    panelTimerListener = new IPanelTimerListener()
    {
      
      @Override
      public void fps25()
      {
        ElementContributor.this.fps25();
      }
      
      @Override
      public void fps10()
      {
        ElementContributor.this.fps10();
      }
      
      @Override
      public void fps2()
      {
        ElementContributor.this.fps2();
      }
      
      @Override
      public void fps1()
      {
        ElementContributor.this.fps1();
      }
    };
  }

  // -- Element management --

  protected <T extends EElement> T add(T el, boolean reposition)
  {
    if (el==null)
      return null;
    if (reposition)
    {
      Rectangle bounds = el.getBounds();
      bounds.x += this.x;
      bounds.y += this.y;
      el.setBounds(bounds);
    }

    synchronized (this.elements)
    {
      this.elements.remove(el);
      this.elements.add(el);
      Panel panel = getPanel();
      if (panel!=null)
      {
        panel.add(el);
        el.setPanel(panel);
      }
    }
    return el;
  }

  protected <T extends EElement> T add(T el)
  {
    return add(el,true);
  }

  /**
   * Adds the elements of another element contributor.
   * 
   * @param ec
   *          The other contributor.
   * @param reposition
   *          If <code>true</code>, offset all added elements by the coordinates
   *          of the top-left corner of this element contributor, i.e. by
   *          (<code>this.</code>{@link #x}, <code>this.</code>{@link #y}).
   * @return The argument <code>ec</code>.
   */
  protected ElementContributor add(ElementContributor ec, boolean reposition)
  {
    if (ec==null)
      return null;
    children.add(ec);
    ec.forAllElements((el)->{ this.add(el,reposition); });
    ec.onAddToPanelOrContributor();
    return ec;
  }

  /**
   * Adds the elements of another element contributor. Equivalent to
   * {@link #add(ElementContributor, boolean) add}<code>(ec,true)</code>}.
   * 
   * @param ec
   *          The other contributor.
   * @return The argument <code>ec</code>.
   */
  protected ElementContributor add(ElementContributor ec)
  {
    return add(ec,true);
  }

  protected void addAll(Collection<EElement> elements, boolean reposition)
  {
    if (elements==null)
      return;

    if (reposition)
      doReposition(elements);

    synchronized (this.elements)
    {
      doAddAll(elements);
    }
  }

  protected void addAll(Collection<EElement> elements)
  {
    addAll(elements, true);
  }

  protected void remove(EElement el)
  {
    if (el==null)
      return;
    synchronized (this.elements)
    {
      this.elements.remove(el);
      Panel panel = getPanel();
      if (panel!=null)
        panel.remove(el);
    }
  }

  protected void remove(int i)
  {
    EElement el;
    synchronized (this.elements)
    {
      el = this.elements.remove(i);
      Panel panel = getPanel();
      if (panel!=null)
        panel.remove(el);
    }
  }

  /**
   * Removes the elements of another element contributor.
   * 
   * @param ec
   *          The other contributor.
   */
  protected void remove(ElementContributor ec)
  {
    if (ec==null)
      return;
    children.remove(ec);
    ec.onRemoveFromPanelOrContributor();
    ec.forAllElements((el)->{ this.remove(el); });
  }

  protected void removeAll(Collection<EElement> elements)
  {
    synchronized (this.elements)
    {
      doRemoveAll(elements);
    }
  }

  protected void removeAll()
  {
    synchronized (this.elements)
    {
      Panel panel = getPanel();
      if (panel!=null)
      {
        for (ElementContributor ec : children)
          panel.removePanelTimerListener(ec.panelTimerListener);
        panel.removeAll(this.elements);
      }
      this.children.clear();
      this.elements.clear();
    }
  }

  /**
   * Removes those elements from this {@link ElementConstributor} and from the
   * panel where the filter returns true.
   * 
   * @param filter
   *          - mapping of EElement to boolean (see {@link Predicate})
   */
  protected void removeIf(Predicate<EElement> filter)
  {
    synchronized (this.elements)
    {
      Panel panel = getPanel();
      Iterator<EElement> it = this.elements.iterator();

      if (panel != null)
        while (it.hasNext())
        {
          EElement el = it.next();
          if (!filter.test(el))
            return;
          it.remove();
          panel.remove(el);
        }
      else
        while (it.hasNext())
        {
          EElement el = it.next();
          if (!filter.test(el))
            return;
          it.remove();
        }
    }
  }

  protected void removeAllAndAddAll(Collection<EElement> remove, Collection<EElement> add, boolean repositionAdd)
  {
    if (repositionAdd)
      doReposition(add);

    synchronized (this.elements)
    {
      doRemoveAll(remove);
      doAddAll(add);
    }
  }

  protected void removeAllAndAddAll(Collection<EElement> remove, Collection<EElement> add)
  {
    removeAllAndAddAll(remove, add, true);
  }

  /**
   * Iterates over the elements list in the given bounds
   * 
   * @param fromInclusive
   *          - lower bound, index is included
   * @param toExclusive
   *          - higher bound, index is excluded
   * @param action
   *          - method with the parameters (int index, EElement element)
   */
  public void forAllElements(int fromInclusive, int toExclusive, Consumer<EElement> action)
  {
    synchronized (this.elements)
    {
      if (fromInclusive < 0 || fromInclusive >= toExclusive || toExclusive > this.elements.size())
        throw new IllegalArgumentException("iteration limits out of bounds.");
      for (; fromInclusive < toExclusive; fromInclusive++)
        action.accept(this.elements.get(fromInclusive));
    }
  }

  /**
   * Iterates over the elements list in the given bounds
   * 
   * @param fromInclusive
   *          - lower bound, index is included
   * @param pradicate
   *          - method with the parameters (EElement element) and boolean as
   *          return type to break loop if true
   */
  public void forAllElements(int fromInclusive, Function<EElement, Boolean> action)
  {
    synchronized (this.elements)
    {
      int toExclusive = this.elements.size();
      if (fromInclusive >= toExclusive)
        return;
      if (fromInclusive < 0)
        throw new IllegalArgumentException("iteration limits out of bounds.");
      for (; fromInclusive < toExclusive; fromInclusive++)
        if (action.apply(this.elements.get(fromInclusive)))
          break;
    }
  }

  /**
   * Iterates over the elements list in the given bounds
   * 
   * @param fromInclusive
   *          - lower bound, index is included
   * @param toExclusive
   *          - higher bound, index is excluded
   * @param action
   *          - method with the parameters (EElement element)
   */
  public void forAllElements(Consumer<EElement> action)
  {
    synchronized (this.elements)
    {
      for (EElement el : this.elements)
        action.accept(el);
    }
  }

  /**
   * Iterates over the elements list in the given bounds
   * 
   * @param fromInclusive
   *          - lower bound, index is included
   * @param toExclusive
   *          - higher bound, index is excluded
   * @param action
   *          - method with the parameters (int index, EElement element)
   */
  public void forAllElements(BiConsumer<Integer, EElement> action)
  {
    synchronized (this.elements)
    {
      Integer i = 0;
      for (EElement el : this.elements)
        action.accept(i++, el);
    }
  }

  protected int size()
  {
    synchronized (this.elements)
    {
      return this.elements.size();
    }
  }

  protected EElement getElement(int i)
  {
    synchronized (this.elements)
    {
      return this.elements.get(i);
    }
  }

  protected ArrayList<EElement> createElementList()
  {
    synchronized (this.elements)
    {
      return new ArrayList<>(this.elements);
    }
  }

  /**
   * Return <code>true</code> if the element list is empty, otherwise
   * <code>false</code>.
   */
  public final boolean isEmpty()
  {
    return this.elements.isEmpty();
  }

  /**
   * Returns the {@linkplain Panel LCARS panel} this contributor is supplying
   * elements for.
   */
  public final Panel getPanel()
  {
    return panel.get();
  }

  /**
   * Adds the elements of this contributor to an {@linkplain Panel LCARS panel}.
   * 
   * @param panel
   *          The panel.
   * @see #removeFromPanel()
   * @see #isDisplayed()
   * @see #getPanel()
   */
  public void addToPanel(Panel panel)
  {
    if (panel==null)
      return;
    synchronized (this.elements)
    {
      if (getPanel()==panel)
        return;
      this.panel = new WeakReference<Panel>(panel);
      panel.addAll(this.elements);
      this.elements.forEach((el) -> {
        el.setPanel(panel);
      });
      panel.invalidate();
    }
    onAddToPanelOrContributor();
    panel.addPanelTimerListener(panelTimerListener);
    for (ElementContributor ec : children)
    {
      ec.panel = new WeakReference<Panel>(panel);
      panel.addPanelTimerListener(ec.panelTimerListener);
    }
  }

  /**
   * Removes the elements of this contributor from the {@linkplain Panel LCARS
   * panel}.
   * 
   * @see #addToPanel(Panel)
   * @see #isDisplayed()
   * @see #getPanel()
   */
  public void removeFromPanel()
  {
    cancelAllTimerTasks();
    synchronized (this.elements)
    {
      Panel panel = getPanel();
      if (panel==null)
        return;
      for (ElementContributor ec : children)
      {
        ec.panel = new WeakReference<Panel>(null);
        panel.removePanelTimerListener(ec.panelTimerListener);
      }
      panel.removePanelTimerListener(panelTimerListener);
      onRemoveFromPanelOrContributor();
      this.panel = new WeakReference<Panel>(null);

      for (EElement el : this.elements)
        el.setPanel(null);
      panel.removeAll(this.elements);
      panel.invalidate();
    }
  }

  /**
   * Called after the elements of this contributor have been added to an
   * {@linkplain Panel LCARS panel} or another {@linkplain ElementContributor
   * element contributor} but <em>before</em> one of the <code>fpsXXX()</code>
   * methods is invoked for the first time. The base class implementation does
   * nothing.
   * 
   * <h3>Remarks:</h3>
   * <ul>
   *   <li>Overrides should <em>not</em> invoke {@link #getPanel()} as the 
   *   returned value may be <code>null</code>.</li>
   * </ul>
   */
  protected void onAddToPanelOrContributor()
  {
    // The base class implementation does nothing.
  }

  /**
   * Called before the elements of this contributor are removed from an
   * {@linkplain Panel LCARS panel} or another {@linkplain ElementContributor
   * element contributor}. The base class implementation does nothing.
   * 
   * <h3>Remarks:</h3>
   * <ul>
   *   <li>Overrides should <em>not</em> invoke {@link #getPanel()} as the 
   *   returned value may be <code>null</code>.</li>
   * </ul>
   */
  protected void onRemoveFromPanelOrContributor()
  {
    // The base class implementation does nothing.
  }
  
  /**
   * Determines if the elements of this contributor are displayed on an
   * {@linkplain Panel LCARS panel}.
   * 
   * @see #addToPanel(Panel)
   * @see #removeFromPanel()
   */
  public boolean isDisplayed()
  {
    return getPanel()!=null;
  }

  // -- Private element management --

  private void doReposition(Collection<EElement> elements)
  {
    int x = this.x;
    int y = this.y;
    for (EElement el : elements)
    {
      Rectangle bounds = el.getBounds();
      bounds.x += x;
      bounds.y += y;
      el.setBounds(bounds);
    }
  }

  private void doAddAll(Collection<EElement> elements)
  {
    this.elements.removeAll(elements);
    this.elements.addAll(elements);

    Panel panel = getPanel();
    if (panel==null)
      return;
    panel.addAll(elements);
    for (EElement el : elements)
      el.setPanel(panel);

  }

  private void doRemoveAll(Collection<EElement> elements)
  {
    this.elements.removeAll(elements);
    Panel panel = getPanel();
    if (panel!=null)
      panel.removeAll(elements);
    elements.clear();
  }

  // -- EEvent handling --

  public void addEEventListener(EEventListener listener)
  {
    this.listeners.add(listener);
  }

  public void removeEEventListener(EEventListener listener)
  {
    this.listeners.remove(listener);
  }

  public void fireEEvent(EEvent ee)
  {
    // Dispatch event
    for (int i=0; i<listeners.size(); i++)
      switch (ee.id)
      {
      case EEvent.TOUCH_DOWN: listeners.get(i).touchDown(ee); break;
      case EEvent.TOUCH_UP  : listeners.get(i).touchUp  (ee); break;
      case EEvent.TOUCH_DRAG: listeners.get(i).touchDrag(ee); break;
      case EEvent.TOUCH_HOLD: listeners.get(i).touchHold(ee); break;
      }
  }

  @Override
  public void touchDown(EEvent ee)
  {
    fireEEvent(ee);
  }

  @Override
  public void touchDrag(EEvent ee)
  {
    fireEEvent(ee);
  }

  @Override
  public void touchHold(EEvent ee)
  {
    fireEEvent(ee);
  }

  @Override
  public void touchUp(EEvent ee)
  {
    fireEEvent(ee);
  }

  // -- Panel timer event handling

  /**
   * Called 25 times per second as long as the element contributor
   * {@linkplain #isDisplayed() is displayed}. Derived classes may override this
   * method to perform periodic actions. The base class implementation does
   * nothing.
   */
  protected void fps25()
  {
  }

  /**
   * Called 10 times per second as long as the element contributor
   * {@linkplain #isDisplayed() is displayed}. Derived classes may override this
   * method to perform periodic actions. The base class implementation does
   * nothing.
   */
  protected void fps10()
  {
  }

  /**
   * Called twice per second as long as the element contributor
   * {@linkplain #isDisplayed() is displayed}. Derived classes may override this
   * method to perform periodic actions. The base class implementation does
   * nothing.
   */
  protected void fps2()
  {
  }

  /**
   * Called once per second as long as the element contributor
   * {@linkplain #isDisplayed() is displayed}. Derived classes may override this
   * method to perform periodic actions. The base class implementation does
   * nothing.
   */
  protected void fps1()
  {
  }

  // -- Deprecated API --

  private Timer timer;
  private final HashMap<String, TimerTask> timerTasks;

  /**
   * @deprecated Use {@link #fps25()}, {@link #fps10()}, {@link #fps2()}, or
   *             {@link #fps1()}.
   */
  @Deprecated
  public void scheduleTimerTask(TimerTask task, String name, long firstTime, long period)
  {
    synchronized (timerTasks)
    {
      if (timer==null)
      {
        String s = getClass().getSimpleName();
        if (panel!=null)
          s += "@"+panel.getClass().getSimpleName();
        timer = new Timer(s+".timer",true);
      }
      cancelTimerTask(name);
      if (period>0)
        timer.schedule(task,firstTime,period);
      else
        timer.schedule(task,firstTime);
      timerTasks.put(name,task);
    }
  }

  @Deprecated
  public void cancelTimerTask(String name)
  {
    synchronized (timerTasks)
    {
      cancelTimerTask(timerTasks.get(name));
    }
  }

  @Deprecated
  public void cancelTimerTask(TimerTask task)
  {
    if (task==null)
      return;
    task.cancel();

    synchronized (timerTasks)
    {
      for (Iterator<TimerTask> i = timerTasks.values().iterator(); i.hasNext();)
      {
        TimerTask tt = i.next();
        if (tt.equals(task))
          i.remove();
      }
      if (timer != null)
        timer.purge();
    }
  }

  @Deprecated
  public void cancelAllTimerTasks()
  {
    synchronized (timerTasks)
    {
      for (Iterator<TimerTask> i = timerTasks.values().iterator(); i.hasNext();)
      {
        TimerTask tt = i.next();
        tt.cancel();
        i.remove();
      }
      if (timer!=null)
      {
        timer.cancel();
        timer.purge();
      }
      timer = null;
    }
  }
}

// EOF
