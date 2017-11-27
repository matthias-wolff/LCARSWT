package de.tucottbus.kt.lcars.contributors;

import java.awt.Dimension;
import java.awt.Point;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Vector;
import java.util.function.Consumer;

import de.tucottbus.kt.lcars.LCARS;
import de.tucottbus.kt.lcars.Panel;
import de.tucottbus.kt.lcars.elements.EElement;
import de.tucottbus.kt.lcars.elements.EEvent;
import de.tucottbus.kt.lcars.elements.EEventListener;
import de.tucottbus.kt.lcars.elements.ELabel;
import de.tucottbus.kt.lcars.elements.ERect;
import de.tucottbus.kt.lcars.elements.EScaledImage;
import de.tucottbus.kt.lcars.elements.EValue;
import de.tucottbus.kt.lcars.logging.Log;
import de.tucottbus.kt.lcars.swt.ImageMeta;

/**
 * An array of equally sized {@link ERect}s, {@link EValue}s, {@link ELabel}s or {@link EScaledImage}s..
 * 
 * @author Matthias Wolff
 */
public class EElementArray extends ElementContributor implements EEventListener
{
  protected int firstItem;
  protected Class<?> elemClass;
  protected Dimension elemSize;
  protected int rows;
  protected int cols;
  protected int elemStyle;
  protected Point[] elemPos;
  protected boolean lock;
  protected final ArrayList<EElement> eList;
  protected ELabel eTitle;
  protected EElement ePrev;
  protected EElement eNext;
  protected EElement eLock;

  /**
   * Creates a new element array.
   * 
   * @param x
   *          the x-coordinate of the top left corner (LCARS panel coordinates)
   * @param y
   *          the y-coordinate of the top left corner (LCARS panel coordinates)
   * @param elemClass
   *          the class of the elements; {@link ERect}, {@link EValue},
   *          {@link ELabel} or {@link EScaledImage}
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
  public EElementArray(int x, int y, Class<?> elemClass, Dimension elemSize,
      int rows, int cols, int elemStyle, String title)
  {
    super(x, y);
    this.elemClass = elemClass;
    this.elemSize = elemSize;
    this.rows = rows;
    this.cols = cols;
    this.elemStyle = elemStyle;
    this.eList = new ArrayList<EElement>();
    this.firstItem = 0;

    // Check class
    if (!this.elemClass.equals(ERect.class)
        && !this.elemClass.equals(EValue.class)
        && !this.elemClass.equals(ELabel.class)
				&& !this.elemClass.equals(EScaledImage.class))
    {
      throw new IllegalArgumentException("Class cannot be used");
    }

    // Compute element positions
    int w = this.elemSize.width;
    int h = this.elemSize.height;
    elemPos = new Point[this.rows * this.cols];
    for (int i = 0, c = 0; c < this.cols; c++)
      for (int r = 0; r < this.rows; r++, i++)
        elemPos[i] = new Point(c * (w + 3), r * (h + 3));

    // Create the title (if present)
    if (title != null)
    {
      int style = LCARS.EF_HEAD2 | LCARS.EC_TEXT | LCARS.ES_STATIC
          | LCARS.ES_LABEL_SW;
      eTitle = new ELabel(null, 0, -3, 0, 0, style, title);
      add(eTitle);
    }

  }

  // -- Getters and setters --

  /**
   * Sets a new title display above this array. Please note, that this method is
   * ineffective if the array was created with the <code>title=null</code>!
   * 
   * @param title
   *          the new title
   */
  public void setTitle(String title)
  {
    if (eTitle != null)
      eTitle.setLabel(title);
  }

  /**
   * Sets the list of labels to be displayed on this array.
   * 
   * @param list
   *          the list
   */
  public void setList(String[] list)
  {
    // Remove old elements
    synchronized (eList)
    {
      this.firstItem = -1;
      eList.clear();
    }
    if (list == null || list.length == 0)
      return;

    // Create new elements
    for (int i = 0; i < list.length; i++)
      add(list[i]);

    // Finish
    setFirstVisibleItemIndex(0);
  }

  /**
   * Sets the list of labels to be displayed on this array.
   * 
   * @param list
   *          the list
   */
  public void setList(AbstractList<String> list)
  {
    if (list == null)
    {
      setList(new String[0]);
      return;
    }
    String names[] = new String[list.size()];
    for (int i = 0; i < list.size(); i++)
      names[i] = list.get(i);
    setList(names);
  }

  /**
   * Resets this array.
   */
  @Override
  public void removeAll()
  {
    synchronized (eList)
    {
      this.firstItem = -1;
      eList.clear();
    }
  }

  /**
   * Adds a new label to this array.
   * 
   * @param name
   *          the label
   * @return the {@link EElement} created for the label
   */
  public EElement add(String name)
  {
    EElement e = null;
    synchronized (eList)
    {
      int index = eList.size();
      int ePos = index % (this.cols * this.rows);
      int x = this.x + this.elemPos[ePos].x;
      int y = this.y + this.elemPos[ePos].y;
      int w = this.elemSize.width;
      int h = this.elemSize.height;
      String label = name.toUpperCase();
      if (this.elemClass.equals(ERect.class))
        e = new ERect(null, x, y, w, h, this.elemStyle, label);
      else if (this.elemClass.equals(EValue.class))
      {
        EValue ev =  new EValue(null, x, y, w, h, this.elemStyle, null);
        ev.setValue(label);
        e = ev;
      } else if (this.elemClass.equals(ELabel.class))
        e = new ELabel(null, x, y, w, h, this.elemStyle, label);
			else if (this.elemClass.equals(EScaledImage.class))
				e = new EScaledImage(null, x, y, w, h, this.elemStyle, new ImageMeta.File(name));
      e.addEEventListener(this);
      e.setData(name);
      eList.add(e);
      if (this.firstItem < 0)
        setFirstVisibleItemIndex(0);
    }
    return e;
  }

  /**
   * Removes a label form this array.
   * 
   * @param index
   *          The zero-based index of the label to remove.
   */
  @Override
  public void remove(int index)
  {
    synchronized (eList)
    {
      super.remove(eList.remove(index));
    }
  }

  /**
   * Sets the first list item to be displayed.
   * 
   * @see #setList(String[])
   * @param first
   *          the zero-based index of a list item
   */
  public void setFirstVisibleItemIndex(int first)
  {
    synchronized (eList)
    {
      if (this.firstItem == first)
        return;
      this.firstItem = first;      
    }
    animate();
  }

  /**
   * Attaches previous and next page controls to this array. These controls
   * allow turning the list pages. The array will register itself as listener to
   * these controls and modify their state (e.g. enabled/disabled).
   * <p>
   * <b>Note:</b> The paging controls must be set before invoking
   * {@link #addToPanel(Panel)}!
   * </p>
   * 
   * @param ePrev
   *          The "previous page" control, can be <code>null</code>.
   * @param eNext
   *          The "next page" control, can be <code>null</code>.
   */
  public void setPageControls(EElement ePrev, EElement eNext)
  {
    if (ePrev==null || eNext==null)
      bindControls(false);
    this.ePrev = ePrev;
    this.eNext = eNext;
    bindControls(true);
  }

  /**
   * Attaches a new lock control to this array. This controls can be used to
   * toggle the automatic page turning on or off. The array will register itself
   * as listener to this controls and modify its state (e.g. blinking).
   * <p>
   * <b>Note:</b> The lock control must be set before invoking
   * {@link #addToPanel(Panel)}!
   * </p>
   * 
   * @param eLock
   *          the "lock" control, can be <code>null</code>
   */
  public void setLockControl(EElement eLock)
  {
    if (eLock==null)
      bindControls(false);
    this.eLock = eLock;
    bindControls(true);
  }

  /**
   * Toggles automatic page turning on or off.
   * 
   * @param lock
   *          <true>true</code> to switch automatic page turning off,
   *          <code>false</code> to switch it on
   */
  public void setLock(boolean lock)
  {
    this.lock = lock;
    if (eLock != null)
      eLock.setBlinking(lock);
  }

  /**
   * Determines if automatic page turning is on or off.
   * 
   * @return <true>true</code> if off, <code>false</code> if on
   */
  public boolean getLock()
  {
    return eLock != null ? eLock.isBlinking() : this.lock;
  }

  /**
   * Determines if this array displays a title.
   */
  public boolean hasTitle()
  {
    return this.eTitle != null;
  }

  /**
   * Returns a label.
   * 
   * @param index
   *          zero-based index in the list of labels
   * @return the label
   */
  public String getItem(int index)
  {
    synchronized (eList)
    {
      if (index < 0 || index >= eList.size())
        return null;
      EElement e = eList.get(index);
      if (e.getData() instanceof String)
        return (String) e.getData();
      if (e instanceof EValue)
        return ((EValue) e).getValue();
			if (e instanceof EScaledImage)
				return ((EScaledImage) e).getImagePath();
      return e.getLabel();
    }
  }

  /**
   * Returns the LCARS {@link EElement} for a list item. If the returned element
   * is be modified, the changes will be reflected at the UI.
   * 
   * @param index
   *          index zero-based index in the list of labels
   * @return the element
   */
  public EElement getItemElement(int index)
  {
    synchronized (eList)
    {
      return eList.get(index);
    }
  }

  /**
   * Returns the elements in this array. The return value is a copy, modifying
   * it has no effect on the array.
   */
  public Vector<EElement> getItemElements()
  {
    synchronized (eList)
    {
      return new Vector<EElement>(eList);
    }
  }

  /**
   * Returns the number of elements contained in this array.
   */
  public int getItemCount()
  {
    synchronized (eList)
    {
      return eList.size();
    }
  }

  /**
   * Returns the number of element displaying on one page.
   */
  public int getItemCountPerPage()
  {
    return this.rows * this.cols;
  }

  /**
   * Returns the index of the first visible item.
   * 
   * @see #setFirstVisibleItemIndex(int)
   */
  public int getFirstVisibleItemIndex()
  {
    return firstItem;
  }

  // -- UI methods --

  /**
   * Displays the next page of list items.
   * 
   * @param turnOver
   *          start over after last page
   */
  public void nextPage(boolean turnOver)
  {
    synchronized (eList)
    {
      if (eList == null)
        return;
      firstItem += rows * cols;
      if (turnOver && firstItem >= eList.size())
        firstItem = 0;
      animate();
    }
  }

  /**
   * Displays the previous page of list items.
   * 
   * @param turnOver
   *          start over before the first page
   */
  public void prevPage(boolean turnOver)
  {
    synchronized (eList)
    {
      if (eList == null)
        return;
      this.firstItem -= rows * cols;
      if (turnOver && firstItem < 0)
        firstItem = eList.size() - rows * cols;
      animate();
    }
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
    synchronized (eList)
    {
      for (int i = 0; i < this.eList.size(); i++)
        this.eList.get(i).setHighlighted(i == item);
      Panel panel = getPanel();
      if (panel != null)
        panel.invalidate();
    }
  }

  /**
   * Highlights a list item and automatically clears the highlight after a
   * specified time.
   * 
   * @param item
   *          the zero-based index of a list item, -1 to clear the current
   *          highlight
   * @param time
   * @see #hiliteItem(int)
   */
  public void hiliteItem(int item, int time)
  {
    synchronized (eList)
    {
      EElement el = this.eList.get(item);
      el.setHighlighted(true);
      LCARS.invokeLater(()->{
        try
        {
          el.setHighlighted(false);
        }
        catch (Exception e)
        {
          Log.err("Unhighlighting element",e);
        }
      }, time);
    }
  }

  /**
   * 
   * @param first
   * @param count
   */
  protected void showItemsInt(int first, int count)
  {
    // Remove old elements
    final EElement eTitle = this.eTitle;
    removeIf((el) -> {
      if (el == eTitle) return false;
      el.clearTouch();
      return true;
    });

    synchronized (eList)
    {
      // Do nothing of there are no items
      if (eList.isEmpty())
      {
        firstItem = -1;
        return;
      }
    
      final int n = eList.size();

      if (first > n)
        first = n - this.rows * this.cols - 1;
      if (first < 0)
        first = 0;

      // Set first item
      firstItem = first;

      // Add new elements
      if (count < 0)
        count = this.cols * this.rows;

      final int fromIn = first;
      final int toEx = Math.min(first + Math.min(count, cols * rows), n);

      int i = 0;
      for (;i < fromIn; i++)
        eList.get(i).setVisible(false);      
      for (;i < toEx; i++)
      {
        EElement el = eList.get(i);
        el.clearTouch();
        add(el, false).setVisible(true);
      }      
      for (;i < n; i++)
        eList.get(i).setVisible(false);

      // GUI reflection
      EElement e;
      if ((e = ePrev) != null)
      {
        e.clearTouch();
        e.setDisabled(first <= 0);
      }
      if ((e = eNext) != null)
      {
        e.clearTouch();
        e.setDisabled(first >= n - rows * cols);
      }
      Panel panel = getPanel();
      if (panel!=null)
        panel.invalidate();
    }
  }

  /**
   * Binds or unbinds any external controls by registering or unregistering this
   * element array as {@link EEventListener}.
   * 
   * @param bind
   *          <code>true</code> to bind, <code>false</code> to unbind.
   */
  protected void bindControls(boolean bind)
  {
    if (getPanel()==null && bind)
      return;
    Consumer<EElement> bindControl = el ->
    {
      if (el==null)
        return;
      el.removeAllEEventListeners();
      if (bind)
        el.addEEventListener(this);
    };
    bindControl.accept(eLock);
    bindControl.accept(ePrev);
    bindControl.accept(eNext);
  }

  // -- Overrides ---

  @Override
  protected void onAddToPanelOrContributor()
  {
    super.onAddToPanelOrContributor();
    bindControls(true);
    animate();
  }

  @Override
  protected void onRemoveFromPanelOrContributor()
  {
    super.onRemoveFromPanelOrContributor();
    bindControls(false);
  }
  
  // -- Implementation of the EEventlistener interface --

  @Override
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

  @Override
  public void touchDrag(EEvent ee)
  {
    if (ee.el.equals(ePrev))
      return;
    if (ee.el.equals(eNext))
      return;
    if (ee.el.equals(eLock))
      return;
    super.touchDrag(ee);
  }

  @Override
  public void touchHold(EEvent ee)
  {
    if (ee.el.equals(ePrev))
      return;
    if (ee.el.equals(eNext))
      return;
    if (ee.el.equals(eLock))
      return;
    super.touchHold(ee);
  }

  @Override
  public void touchUp(EEvent ee)
  {
    if (ee.el.equals(ePrev))
      return;
    if (ee.el.equals(eNext))
      return;
    if (ee.el.equals(eLock))
      return;
    super.touchUp(ee);
  }

  // -- Animations --

  private int autoPageCtr = -1;
  private int animateCtr  = -1; // Animation off
  
  @Override
  protected void fps10()
  {
    // Animate
    if (animateCtr>=0)
    {
      if (animateCtr<=cols*rows)
      {
        int ctr;
        int firstItem;
        synchronized (eList)
        {
          ctr = ++this.animateCtr;
          firstItem = this.firstItem;
        }
        showItemsInt(firstItem, ctr);
        hiliteItem(firstItem + ctr - 1);
      }
      else
      {
        animateCtr = -1; // Stop animation
        autoPageCtr = 0;
      }
    }
    
    // Auto-page
    if (!getLock() && animateCtr<0)
    {
      autoPageCtr++;
      if (autoPageCtr==50)
      {
        nextPage(true);
        autoPageCtr = 0;
      }
    }
    
  }
  
  /**
   * Animates the array.
   */
  public void animate()
  {
    animateCtr = 0;
  }

}

// EOF
