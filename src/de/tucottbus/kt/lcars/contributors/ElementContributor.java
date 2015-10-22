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
  protected Panel                            panel;
  private   final Vector<EElement>           elements;
  protected final int                        x;
  protected final int                        y;
  private   final Vector<EEventListener>     listeners;
  private   Timer                            timer;
  private   final HashMap<String, TimerTask> timerTasks;
  
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
  
  protected <T extends EElement> T add(T el, boolean reposition)
  {
    if (el==null) return null;
    if (reposition)
    {
      Rectangle bounds = el.getBounds();
      bounds.x += this.x;
      bounds.y += this.y;
      el.setBounds(bounds);
    }
    elements.remove(el);
    elements.add(el);
    if (this.panel!=null)
    {
      el.setPanel(this.panel);
      this.panel.add(el);
    }
    return el;
  }
  
  protected <T extends EElement> T add(T el)
  {
    return add(el,true);
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
    elements.clear();;
  }
  
  protected Vector<EElement> getElements()
  {
    return this.elements;
  }
  
  public void addToPanel(Panel panel)
  {
    if (panel==null || this.panel==panel) return;
    this.panel = panel;
    elements.forEach((el) -> {
      this.panel.add(el).setPanel(this.panel);
    });
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
  
  public void scheduleTimerTask(TimerTask task, String name, long firstTime, long period)
  {
    synchronized (timerTasks)
    {
      if (timer==null) timer = new Timer(true);
      cancelTimerTask(name);
      if (period>0)
        timer.schedule(task,firstTime,period);
      else
        timer.schedule(task,firstTime);
      timerTasks.put(name,task);      
    }
  }
  
  public void cancelTimerTask(String name)
  {
    synchronized (timerTasks)
    {
      cancelTimerTask(timerTasks.get(name));
    }
  }
  
  public void cancelTimerTask(TimerTask task)
  {
    if (task==null) return;
    task.cancel();
    
    synchronized (timerTasks)
    {
      for (Iterator<TimerTask> i = timerTasks.values().iterator(); i.hasNext();)
      {
        TimerTask tt = i.next();
        if (tt.equals(task)) i.remove();
      }
      if (timer!=null) timer.purge();      
    }
  }
  
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
      if (timer!=null) timer.purge();      
    }
  }
}
