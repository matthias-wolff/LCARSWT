package de.tucottbus.kt.lcars.contributors;

import java.awt.Rectangle;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import de.tucottbus.kt.lcars.Panel;
import de.tucottbus.kt.lcars.elements.EElement;
import de.tucottbus.kt.lcars.elements.EEvent;
import de.tucottbus.kt.lcars.elements.EEventListener;

/**
 * Contributes elements to an LCARS {@link Panel}.
 * 
 * <p><b>TODO:</b><br>Rename to <code>ElementGroup</code>!</p>
 * 
 * @author Matthias Wolff
 */
public abstract class ElementContributor implements EEventListener
{
  protected Panel                      panel;
  private   Vector<EElement>           elements;
  protected int                        x;
  protected int                        y;
  private   Vector<EEventListener>     listeners;
  private   Timer                      timer;
  private   HashMap<String, TimerTask> timerTasks;
  
  // -- Constructors --
  
  public ElementContributor(int x, int y)
  {
    elements   = new Vector<EElement>();
    this.x     = x;
    this.y     = y;
    listeners  = new Vector<EEventListener>();
    timerTasks = new HashMap<String,TimerTask>();
  }

  // -- Element management --
  
  protected void add(EElement el, boolean reposition)
  {
    if (el==null) return;
    if (reposition)
    {
      Rectangle bounds = el.getBounds();
      bounds.x += this.x;
      bounds.y += this.y;
      el.setBounds(bounds);
    }
    elements.add(el);
    if (this.panel!=null)
    {
      el.setPanel(this.panel);
      this.panel.add(el);
    }
  }
  
  protected void add(EElement el)
  {
    add(el,true);
  }
  
  protected void remove(EElement el)
  {
    if (el==null) return;
    elements.remove(el);
    if (this.panel!=null) this.panel.remove(el);
  }
  
  protected void removeAll()
  {
    if (this.panel!=null)
      for (EElement el : elements)
        panel.remove(el);
    elements.removeAllElements();
  }
  
  protected Vector<EElement> getElements()
  {
    return this.elements;
  }
  
  public void addToPanel(Panel panel)
  {
    if (panel==null) return;
    if (this.panel==panel) return;
    this.panel = panel;
    for (int i=0; i<elements.size(); i++)
    {
      EElement el = elements.get(i);
      el.setPanel(this.panel);
      this.panel.add(el);
    }
    
    this.panel.invalidate();
  }
  
  public void removeFromPanel()
  {
    if (this.panel==null) return;
    for (int i=0; i<elements.size(); i++)
    {
      EElement el = elements.get(i);
      el.setPanel(null);
      this.panel.remove(el);
    }
       
    this.panel.invalidate();
    this.panel = null;
  }

  public boolean isDisplayed()
  {
    return this.panel!=null;
  }

  // -- EEvent handling --
  
  public void addEEventListener(EEventListener listener)
  {
    this.listeners.add(listener);
  }

  public void removeEEventListener(EEventListener listener)
  {
    this.listeners.add(listener);
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

  public void touchDown(EEvent ee)
  {
    fireEEvent(ee);
  }

  public void touchDrag(EEvent ee)
  {
    fireEEvent(ee);
  }

  public void touchHold(EEvent ee)
  {
    fireEEvent(ee);
  }

  public void touchUp(EEvent ee)
  {
    fireEEvent(ee);
  }

  // -- Timers --
  
  public synchronized void scheduleTimerTask(TimerTask task, String name, long firstTime, long period)
  {
    if (timer==null) timer = new Timer(true);
    cancelTimerTask(name);
    if (period>0)
      timer.schedule(task,firstTime,period);
    else
      timer.schedule(task,firstTime);
    timerTasks.put(name,task);
  }
  
  public synchronized void cancelTimerTask(String name)
  {
    cancelTimerTask(timerTasks.get(name));
  }
  
  public synchronized void cancelTimerTask(TimerTask task)
  {
    if (task==null) return;
    task.cancel();
    Iterator<TimerTask> i = timerTasks.values().iterator();
    while (i.hasNext())
    {
      TimerTask tt = i.next();
      if (tt.equals(task)) i.remove();
    }
    if (timer!=null) timer.purge();
  }
  
  public synchronized void cancelAllTimerTasks()
  {
    Iterator<TimerTask> i = timerTasks.values().iterator();
    while (i.hasNext())
    {
      TimerTask tt = i.next();
      tt.cancel();
      i.remove();
    }
    if (timer!=null) timer.purge();
  }
  
}
