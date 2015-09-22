package  de.tucottbus.kt.lcars.contributors;

import java.awt.Dimension;
import java.awt.Point;
import java.lang.ref.WeakReference;
import java.util.AbstractList;
import java.util.TimerTask;
import java.util.Vector;

import de.tucottbus.kt.lcars.LCARS;
import de.tucottbus.kt.lcars.Panel;
import de.tucottbus.kt.lcars.elements.EElement;
import de.tucottbus.kt.lcars.elements.EEvent;
import de.tucottbus.kt.lcars.elements.EEventListener;
import de.tucottbus.kt.lcars.elements.ELabel;
import de.tucottbus.kt.lcars.elements.ERect;
import de.tucottbus.kt.lcars.elements.EValue;

/**
 * An array of equally sized {@link ERect}s, {@link EValue}s or {@link ELabel}s.
 * 
 * @author Matthias Wolff
 */
public class EElementArray extends ElementContributor implements EEventListener
{
  protected int               firstItem;
  protected Class<?>          elemClass;
  protected Dimension         elemSize;
  protected int               rows;
  protected int               cols;
  protected int               elemStyle;
  protected Point[]           elemPos;
  protected boolean           lock;
  protected Vector<EElement>  eList;
  protected ELabel            eTitle;
  protected EElement          ePrev;
  protected EElement          eNext;
  protected EElement          eLock;
  
  private static final String TT_ANIMATION = "ANIMATION";
  private static final String TT_AUTOPAGE  = "AUTOPAGE";
  private static final String TT_UNHILITE  = "UNHILITE";
  
  /**
   * Creates a new element array.
   * 
   * @param x
   *          the x-coordinate of the top left corner (LCARS panel coordinates) 
   * @param y
   *          the y-coordinate of the top left corner (LCARS panel coordinates) 
   * @param elemClass
   *          the class of the elements; {@link ERect}, {@link EValue} or
   *          {@link ELabel}
   * @param elemSize
   *          the size of one element
   * @param rows
   *          the number of rows
   * @param cols
   *          the number of columns
   * @param elemStyle
   *          the style of the elements
   * @param title
   *          the title of the element array
   */
  public EElementArray
  (
    int       x,
    int       y,
    Class<?>  elemClass,
    Dimension elemSize,
    int       rows,
    int       cols,
    int       elemStyle,
    String    title
  )
  {
    super(x,y);
    this.elemClass = elemClass;
    this.elemSize  = elemSize;
    this.rows      = rows;
    this.cols      = cols;
    this.elemStyle = elemStyle;
    this.eList     = new Vector<EElement>();
    this.firstItem = 0;

    // Check class
    if
    (
      !this.elemClass.equals(ERect .class) &&
      !this.elemClass.equals(EValue.class) &&
      !this.elemClass.equals(ELabel.class)
    )
    {
      throw new IllegalArgumentException("Class cannot be used");
    }
    
    // Compute element positions
    int w = this.elemSize.width;
    int h = this.elemSize.height;
    elemPos = new Point[this.rows*this.cols];
    for (int i=0, c=0; c<this.cols; c++)
    for (int r=0; r<this.rows; r++, i++)
        elemPos[i] = new Point(c*(w+3),r*(h+3));
   
    // Create the title (if present)
    if (title!=null)
    {
      int style = LCARS.EF_HEAD2|LCARS.EC_TEXT|LCARS.ES_STATIC|LCARS.ES_LABEL_SW;
      eTitle = new ELabel(null,0,-3,0,0,style,title); 
      add(eTitle);
    }

  }
  
  // -- Getters and setters --

  /**
   * Sets a new title display above this array. Please note, that this method
   * is ineffective if the array was created with the <code>title=null</code>!
   * 
   * @param title the new title
   */
  public void setTitle(String title)
  {
    if (eTitle!=null) eTitle.setLabel(title);
  }
  
  /**
   * Sets the list of labels to be displayed on this array.
   * 
   * @param list the list
   */
  public synchronized void setList(String[] list)
  {
    this.firstItem = -1;

    // Remove old elements
    eList.clear();
    if (list==null || list.length==0) return;

    // Create new elements
    for (int i=0; i<list.length; i++)
      add(list[i]);
    
    // Finish
    setFirstVisibleItemIndex(0);
  }
  
  /**
   * Sets the list of labels to be displayed on this array.
   * 
   * @param list the list
   */
  public void setList(AbstractList<String> list)
  {
    if (list==null)
    {
      setList(new String[0]);
      return;
    }
    String names[] = new String[list.size()];
    for (int i=0; i<list.size(); i++)
      names[i] = list.get(i);
    setList(names);
  }  
  
  /**
   * Resets this array.
   */
  public void removeAll()
  {
    this.firstItem = -1;
    eList.clear();
  }
  
  /**
   * Adds a new label to this array.
   * 
   * @param name the label
   * @return the {@link EElement} created for the label
   */
  public EElement add(String name)
  {
    int      index = eList.size();
    int      ePos  = index % (this.cols*this.rows);
    int      x     = this.x+this.elemPos[ePos].x;
    int      y     = this.y+this.elemPos[ePos].y;
    int      w     = this.elemSize.width;
    int      h     = this.elemSize.height;
    String   label = name.toUpperCase();
    EElement e     = null;
    if (this.elemClass.equals(ERect.class))
      e = new ERect(null,x,y,w,h,this.elemStyle,label);
    else if (this.elemClass.equals(EValue.class))
    {
      e = new EValue(null,x,y,w,h,this.elemStyle,null);
      ((EValue)e).setValue(label);
    }
    else if (this.elemClass.equals(ELabel.class))
      e = new ELabel(null,x,y,w,h,this.elemStyle,label);
    e.addEEventListener(this);
    e.setData(name);
    eList.add(e);
    if (this.firstItem<0) setFirstVisibleItemIndex(0);
    return e;
  }
  
  /**
   * Removes a label form this array.
   * 
   * @param index
   *          The zero-based index of the label to remove.
   */
  public void remove(int index)
  {
    super.remove(getItemElement(index));
    eList.remove(index);
  }
  
  /**
   * Sets the first list item to be displayed.
   * 
   * @see #setList(String[])
   * @param first the zero-based index of a list item 
   */
  public void setFirstVisibleItemIndex(int first)
  {
    if (this.firstItem==first) return;
    this.firstItem = first;
    animate();
    autopage();
  }

  /**
   * Attaches previous and next page controls to this array. These controls
   * allow turning the list pages. The array will register itself as listener to
   * these controls and modify their state (e.g. enabled/disabled).
   * <p><b>Note:</b> The paging controls must be set before invoking {@link
   * #addToPanel(Panel)}!</p>
   * 
   * @param ePrev
   *          The "previous page" control, can be <code>null</code>.
   * @param eNext
   *          The "next page" control, can be <code>null</code>.
   */
  public void setPageControls(EElement ePrev, EElement eNext)
  {
    this.ePrev = ePrev;
    this.eNext = eNext;
  }

  /**
   * Attaches a new lock control to this array. This controls can be used to
   * toggle the automatic page turning on or off. The array will register itself
   * as listener to this controls and modify its state (e.g. blinking).
   * <p><b>Note:</b> The lock control must be set before invoking {@link
   * #addToPanel(Panel)}!</p>
   * 
   * @param eLock the "lock" control, can be <code>null</code>
   */
  public void setLockControl(EElement eLock)
  {
    this.eLock = eLock;
  }

  /**
   * Toggles automatic page turning on or off.
   * 
   * @param lock
   *          <true>true</code> to switch automatic page turning off,
              <code>false</code> to switch it on
   */
  public void setLock(boolean lock)
  {
    this.lock = lock;
    if (eLock!=null) eLock.setBlinking(lock);
  }

  /**
   * Determines if automatic page turning is on or off.
   * 
   * @return <true>true</code> if off, <code>false</code> if on
   */
  public boolean getLock()
  {
    if (eLock!=null) return eLock.isBlinking();
    return this.lock;
  }
  
  /**
   * Determines if this array displays a title.
   */
  public boolean hasTitle()
  {
    return this.eTitle!=null;    
  }
  
  /**
   * Returns a label.
   * 
   * @param index zero-based index in the list of labels
   * @return the label
   */
  public String getItem(int index)
  {
    if (index<0 || index>=eList.size()) return null;
    EElement e = eList.get(index);
    if (e.getData() instanceof String)
      return (String)e.getData();
    if (e instanceof EValue)
      return ((EValue)e).getValue();
    return e.getLabel();
  }
  
  /**
   * Returns the LCARS {@link EElement} for a list item. If the returned element
   * is be modified, the changes will be reflected at the UI.
   * 
   * @param index index zero-based index in the list of labels
   * @return the element
   */
  public EElement getItemElement(int index)
  {
    return eList.get(index);
  }

  /**
   * Returns the elements in this array. The return value is a copy, modifying it has no effect on
   * the array.
   */
  public Vector<EElement> getItemElements()
  {
    return new Vector<EElement>(eList);
  }
  
  /**
   * Returns the number of elements contained in this array.
   */
  public int getItemCount()
  {
    return eList.size();
  }
  
  /**
   * Returns the number of element displaying on one page.
   */
  public int getItemCountPerPage()
  {
    return this.rows*this.cols;
  }
  
  /**
   * Returns the index of the first visible item.
   * 
   *  @see #setFirstVisibleItemIndex(int)
   */
  public int getFirstVisibleItemIndex()
  {
    return firstItem;
  }
  
  // -- UI methods --

  /**
   * Displays the next page of list items.
   *  
   * @param turnOver start over after last page
   */
  public void nextPage(boolean turnOver)
  {
    if (eList==null) return;
    firstItem+=rows*cols;
    if (turnOver && firstItem>=eList.size()) firstItem = 0;
    animate();
  }

  /**
   * Displays the previous page of list items.
   * 
   * @param turnOver start over before the first page
   */
  public void prevPage(boolean turnOver)
  {
    if (eList==null) return;
    this.firstItem-=rows*cols;
    if (turnOver && firstItem<0) firstItem = eList.size()-rows*cols;
    animate();
  }
  
  /**
   * Highlights a list item. The previous highlight will be cleared. 
   * 
   * @param item
   *          the zero-based index of a list item, -1 to clear the current
   *          highlight
   * @see #hiliteItem(int, int)
   */
  public void hiliteItem(int item)
  {
    for (int i=0; i<this.eList.size(); i++)
    {
      EElement e = this.eList.get(i);
      e.setHighlighted(i==item);
    }
    if (this.panel!=null) this.panel.invalidate();    
  }
  
  /**
   * Highlights a list item and automatically clears the highlight after a specified time. 
   * 
   * @param item
   *          the zero-based index of a list item, -1 to clear the current
   *          highlight
   * @param time
   * @see #hiliteItem(int)
   */
  public void hiliteItem(int item, int time)
  {
    EElement e = this.eList.get(item);
    e.setHighlighted(true);
    scheduleTimerTask(new UnhiliteTask(e),TT_UNHILITE+e.hashCode(),time,0);
    if (this.panel!=null) this.panel.invalidate();    
  }
  
  /**
   * 
   * @param first
   * @param count
   */
  protected synchronized void showItemsInt(int first, int count)
  {
    // Remove old elements
    final EElement eTitle = this.eTitle;
    getElements().removeIf((el) -> {
      if (el!=eTitle)
      {
        el.clearTouch();
        return true;
      }
      return false;
    });
    
    // Do nothing of there are no items
    if (this.eList==null)
    {
      this.firstItem = -1;
      return;
    }
    
    // Set first item
    this.firstItem = first;
    if (this.firstItem>this.eList.size())
      this.firstItem = this.eList.size()-this.rows*this.cols-1;
    if (this.firstItem<0) this.firstItem=0;

    // Add new elements
    if (count<0) count = this.cols*this.rows;
    for (int i=0; i<count && firstItem+i<this.eList.size() && i<this.cols*this.rows; i++)
    {
      EElement e = eList.get(this.firstItem+i);
      e.clearTouch();
      add(e,false);
    }
    
    // GUI reflection
    if (ePrev!=null) { ePrev.clearTouch(); ePrev.setDisabled(firstItem<=0); }
    if (eNext!=null) { eNext.clearTouch(); eNext.setDisabled(firstItem>=eList.size()-rows*cols); }
    if (panel!=null) panel.invalidate();
  }
  
  // -- Overrides --
  
  /*
   * (non-Javadoc)
   * @see de.tucottbus.kt.lcars.contributors.ElementContributor#addToPanel(de.tucottbus.kt.lcars.Panel)
   */
  @Override
  public void addToPanel(Panel panel)
  {
    super.addToPanel(panel);
    if (ePrev!=null)
    {
      ePrev.removeAllEEventListeners();
      ePrev.addEEventListener(this);
    }
    if (eNext!=null)
    {
      eNext.removeAllEEventListeners();
      eNext.addEEventListener(this);
    }
    if (eLock!=null)
    {
      eLock.removeAllEEventListeners();
      eLock.addEEventListener(this);
    }
    animate();
    autopage();
  }
  
  /*
   * (non-Javadoc)
   * @see de.tucottbus.kt.lcars.contributors.ElementContributor#removeFromPanel()
   */
  @Override
  public void removeFromPanel()
  {
    cancelAllTimerTasks();
    if (ePrev!=null) ePrev.removeAllEEventListeners();
    if (eNext!=null) eNext.removeAllEEventListeners();
    if (eLock!=null) eLock.removeAllEEventListeners();
    super.removeFromPanel();
  }
  
  // -- Implementation of the EEventlistener interface --

  /*
   * (non-Javadoc)
   * @see de.tucottbus.kt.lcars.elements.EEventListener#touchDown(de.tucottbus.kt.lcars.elements.EEvent)
   */
  public void touchDown(EEvent ee)
  {
    if (ee.el.equals(ePrev))
    {
      setLock(true);
      prevPage(false);
      return;
    }
    if (ee.el.equals(eNext))
    {
      setLock(true);
      nextPage(false);
      return;
    }
    if (ee.el.equals(eLock))
    {
      setLock(!getLock());
      return;
    }
    super.touchDown(ee);
  }
  
  /*
   * (non-Javadoc)
   * @see de.tucottbus.kt.lcars.elements.EEventListener#touchDrag(de.tucottbus.kt.lcars.elements.EEvent)
   */
  public void touchDrag(EEvent ee)
  {
    if (ee.el.equals(ePrev)) return;
    if (ee.el.equals(eNext)) return;
    if (ee.el.equals(eLock)) return;
    super.touchDrag(ee);
  }

  /*
   * (non-Javadoc)
   * @see de.tucottbus.kt.lcars.elements.EEventListener#touchHold(de.tucottbus.kt.lcars.elements.EEvent)
   */
  public void touchHold(EEvent ee)
  {
    if (ee.el.equals(ePrev)) return;
    if (ee.el.equals(eNext)) return;
    if (ee.el.equals(eLock)) return;
    super.touchHold(ee);
  }

  /*
   * (non-Javadoc)
   * @see de.tucottbus.kt.lcars.elements.EEventListener#touchUp(de.tucottbus.kt.lcars.elements.EEvent)
   */
  public void touchUp(EEvent ee)
  {
    if (ee.el.equals(ePrev)) return;
    if (ee.el.equals(eNext)) return;
    if (ee.el.equals(eLock)) return;
    super.touchUp(ee);
  }
  
  // -- Animations --
  
  /**
   * Animates the array.
   */
  public void animate()
  {
    try
    {
      if (panel==null) cancelTimerTask(TT_ANIMATION);
      scheduleTimerTask(new AnimationTask(),TT_ANIMATION,1,100);
    }
    catch (IllegalStateException e)
    {
      e.printStackTrace();
    }
  }
  
  /**
   * (Re)starts auto-paging.
   */
  public void autopage()
  {
    if (panel==null) cancelTimerTask(TT_AUTOPAGE);
    int timeout = this.cols*this.rows*100 + 5000;
    scheduleTimerTask(new AutoPagerTask(),TT_AUTOPAGE,timeout,timeout);    
  }
  
  // -- Nested classes --
  
  /**
   * The array animation timer task. 
   */
  class AnimationTask extends TimerTask
  {
    public int ctr = 0;
    
    public void run()
    {
      if (panel==null || ctr>cols*rows) { cancel(); return; }
      ctr++;
      showItemsInt(firstItem,ctr);
      hiliteItem(firstItem+ctr-1);
    }
  }
  
  /**
   * The automatic page turning timer task. 
   */
  class AutoPagerTask extends TimerTask
  {
    public void run()
    {
      if (panel==null) { cancel(); return; }
      if (getLock()) return;
      nextPage(true);
    }
  }
  
  /**
   * The item unhighlighting task.
   */
  class UnhiliteTask extends TimerTask
  {
    private WeakReference<EElement> elementRef;
    
    public UnhiliteTask(EElement element)
    {
      this.elementRef = new WeakReference<EElement>(element);
    }
    
    @Override
    public void run()
    {
      try
      {
        elementRef.get().setHighlighted(false);
        if (panel!=null) panel.invalidate();
      }
      catch (NullPointerException e)
      {
        e.printStackTrace();
      }
    }
  }
  
}
