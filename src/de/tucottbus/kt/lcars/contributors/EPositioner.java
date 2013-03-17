package de.tucottbus.kt.lcars.contributors;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Vector;

import de.tucottbus.kt.lcars.LCARS;
import de.tucottbus.kt.lcars.Panel;
import de.tucottbus.kt.lcars.elements.EElement;
import de.tucottbus.kt.lcars.elements.EEvent;
import de.tucottbus.kt.lcars.elements.ERect;
import de.tucottbus.kt.lcars.elements.EValue;

/**
 * A {@linkplain ETopography topographioc map} with one movable point.
 * 
 * @author Matthias Wolff
 */
public class EPositioner extends ETopography
{
  /**
   * The (invisible) slider touch control.
   */
  private ERect eTouchArea;

  /**
   * The lock control.
   */
  private EElement eLock;
  
  /**
   * The value display control.
   */
  private EValue eValue;
  
  /**
   * The point on the map representing the movable object.
   */
  private ERect eObject;
  
  /**
   * The locked state. If the slider is locked, the user cannot specify a target position.
   */
  private boolean lock = false;
  
  /**
   * The positioning constraints in physical units. Positioning constraints define a rectangular
   * area within the physical bounds in which the object can be positioned. 
   */
  private Rectangle2D.Float constraints;
  
  /**
   * The target position.
   */
  private Point2D.Float targetPos;
  
  /**
   * The actual position.
   */
  private Point2D.Float actualPos;

  /**
   * The list of position listeners.
   */
  private Vector<IPositionListener> listeners; 
  
  /**
   * 
   * @param sBounds
   * @param pBounds
   * @param pUnit
   */
  public EPositioner
  (
    Rectangle         sBounds,
    Rectangle2D.Float pBounds,
    String            pUnit
  )
  {
    super(sBounds.x,sBounds.y,sBounds.width,sBounds.height,LCARS.EC_HEADLINE);
    setPhysicalBounds(pBounds,pUnit,false);
    setGrid(new Point2D.Float(50,50),null,false);
    setGridStyle(LCARS.EC_SECONDARY|LCARS.EF_TINY,0.3f,0,false);
    eObject = addPoint(new Point2D.Float(0,0),15,LCARS.EC_ELBOUP|LCARS.ES_STATIC,false);
    targetPos = new Point2D.Float(0,0);
    actualPos = new Point2D.Float(0,0);
    listeners = new Vector<IPositionListener>();
  }

  /**
   * Sets new positioning constraints.
   * 
   * @param constraints
   *          The constraints (in physical units), can be <code>null</code> which will make
   *          the physical bounds of the slider the constraints.
   */
  public void setConstraints(Rectangle2D.Float constraints, boolean layout)
  {
    this.constraints = constraints;
    if (layout) layout();
  }

  /**
   * Returns the positioning constraints in physical units.
   */
  public Rectangle2D.Float getConstraints()
  {
    float x = getPhysicalBounds().x;
    float y = getPhysicalBounds().y;
    float w = getPhysicalBounds().width;
    float h = getPhysicalBounds().height;
    if (constraints!=null)
    {
      x = constraints.x;
      y = constraints.y;
      w = constraints.width;
      h = constraints.height;
    }
    return new Rectangle2D.Float(x,y,w,h);
  }

  /**
   * Attaches lock and value controls to this position slider. The slider will register itself as
   * listener to these controls and modify their content and state.
   * 
   * @param eLock
   *          The lock control, can be <code>null</code>.
   * @param eVlaue
   *          The value display control, can be <code>null</code>. The control is used for
   *          displaying the current position only. It cannot be used to modify the current
   *          position.
   */
  public void setControls(EElement eLock, EValue eValue)
  {
    this.eLock  = eLock;
    this.eValue = eValue;
    
    // Reflect actual position
    setActualPos(this.actualPos);
  }

  /**
   * Returns the target position. The returned value can be <code>null</code>.
   */
  public Point2D.Float getTargetPos()
  {
    return targetPos;
  }
  
  /**
   * Sets a new target position.
   * 
   * @param targetPos
   *          The new target position in physical units.
   * @param slide
   *          Slides the cursor indicating the target position if <code>true</code>, otherwise
   *          the cursor will be moved instantaneously.
   */
  public void setTargetPos(Point2D targetPos, boolean slide)
  {
    this.targetPos = applyConstraints(targetPos);
    if (this.targetPos==null || isLocked())
    {
      removeCursor(true);
      return;
    }
    if (!hasCursor())
      setCursor(60,3,LCARS.EF_SMALL|LCARS.ES_LABEL_W,true);
    String label = makeCursorLabel();
    if (slide)
      slideCursor(this.targetPos.x,this.targetPos.y,200,label);
    else
      setCursorPos(this.targetPos.x,this.targetPos.y,label);
  }

  /**
   * Returns the actual position. The returned value can be <code>null</code>.
   */
  public Point2D.Float getActualPos()
  {
    return actualPos;
  }
  
  /**
   * Sets a new actual position.
   * 
   * @param actualPos
   *          The new actual position in physical units.
   */
  public void setActualPos(Point2D.Float actualPos)
  {
    if (this.actualPos==actualPos) return;
    this.actualPos = actualPos;
    if (eValue!=null)
      eValue.setValue(makePositionLabel(this.actualPos,true));
    if (this.actualPos==null)
     eObject.setVisible(false);
    else
    {
      eObject.setVisible(true);
      movePoint(eObject,actualPos);
    }
  }

  /**
   * Sets the lock state of this slider. If the slider is locked, the user cannot specify a target
   * position.
   * 
   * @param lock
   *          The new lock state.
   */
  public void setLock(boolean lock)
  {
    this.lock = lock;
    if (lock)
    {
      removeCursor(true);
      if (eTouchArea!=null) eTouchArea.setStatic(true);
    }
    else
    {
      setTargetPos(this.targetPos,false); // Re-display cursor
      if (eTouchArea!=null) eTouchArea.setStatic(false);
    }
    if (eLock!=null) eLock.setBlinking(lock);
  }

  /**
   * Determines if this slider is locked. If the slider is locked, the user cannot specify a target
   * position.
   */
  public boolean isLocked()
  {
    return lock;
  }

  @Override
  protected void layout()
  {
    super.layout();
    
    // Add slider touch control
    Rectangle lBounds = getLogicalBounds();
    eTouchArea = new ERect(null,0,0,lBounds.width,lBounds.height,LCARS.EB_OVERDRAG,null);
    eTouchArea.setColor(new Color(0,true));
    eTouchArea.addEEventListener(this);
    eTouchArea.setStatic(this.lock);
    add(eTouchArea);
  }

  @Override
  public void addToPanel(Panel panel)
  {
    super.addToPanel(panel);
    if (eLock!=null)
    {
      eLock.removeAllEEventListeners();
      eLock.addEEventListener(this);
      eLock.setBlinking(this.lock);
    }
    if (eValue!=null)
    {
      eValue.removeAllEEventListeners();
      eValue.addEEventListener(this);
    }
    eTouchArea.setStatic(this.lock);
    setTargetPos(this.targetPos,false);
    setActualPos(this.actualPos);
  }

  @Override
  public void removeFromPanel()
  {
    if (eLock !=null)
    {
      eLock.setBlinking(false);
      eLock.removeAllEEventListeners();
    }
    if (eValue!=null) eValue.removeAllEEventListeners();
    super.removeFromPanel();
  }

  @Override
  public void touchUp(EEvent ee)
  {
    if (ee.el==eTouchArea && !lock)
    {
      setTargetPos(lToP(ee.pt),true);
      fireTargetChanged(false);
    }
    else if (ee.el==eLock)
    {
      setLock(!isLocked());
    }
    // Do not dispatch other events to parent
  }

  @Override
  public void touchDown(EEvent ee)
  {
    // Do not dispatch any events to parent
  }

  @Override
  public void touchDrag(EEvent ee)
  {
    if (ee.el==eTouchArea && !lock)
    {
      setTargetPos(lToP(ee.pt),false);
      fireTargetChanged(true);
    }
    // Do not dispatch any events to parent
  }

  @Override
  public void touchHold(EEvent ee)
  {
    // Do not dispatch any events to parent
  }

  /**
   * Creates a human readable position label.
   * 
   * @param pos
   *          The position.
   * @return The label.
   */
  protected String makePositionLabel(Point2D pos, boolean bNoUnit)
  {
    if (pos==null) return "N/A";
    Rectangle2D c = getConstraints();
    String s = "";
    if (c.getWidth()==0)
      s = String.format("%03.0f",pos.getY());
    else if (c.getHeight()==0)
      s = String.format("%03.0f",pos.getX());
    else
      s = String.format("%03.0f, %03.0f",pos.getX(),pos.getY());
    if (!bNoUnit && getPhysicalUnit()!=null && getPhysicalUnit().length()>0)
      s += " "+getPhysicalUnit();
    return s;
  }
  
  /**
   * Creates the target position cursor label.
   */
  protected String makeCursorLabel()
  {
    return "TARGET\n"+makePositionLabel(this.targetPos,false);
  }
  
  /**
   * Applies the positioning constraints to a point.
   * 
   * @param pos
   *          The point (in physical units).
   * @return The closest point matching the constraints.
   */
  protected Point2D.Float applyConstraints(Point2D pos)
  {
    if (pos==null) return null;
    Rectangle2D c = getConstraints();
    float x = (float)Math.min(Math.max(pos.getX(),c.getX()),c.getX()+c.getWidth());
    float y = (float)Math.min(Math.max(pos.getY(),c.getY()),c.getY()+c.getHeight());
    return new Point2D.Float(x,y);
  }

  // -- Event handling --
  
  /**
   * Notifies listeners that the position target has changed.
   */
  protected void fireTargetChanged(boolean dragging)
  {
    for (IPositionListener listener : listeners)
      if (dragging)
        listener.positionChanging(targetPos);
      else
        listener.positionChanged(targetPos);
  }
  
  /**
   * Adds a position listener.
   * 
   * @param listener
   *          The listener.
   */
  public void addPositionListener(IPositionListener listener)
  {
    if (listener==null) return;
    if (listeners.contains(listener)) return;
    listeners.add(listener);
  }

  /**
   * Removes a position listener.
   * 
   * @param listener
   *          The listener.
   */
  public void removePositionListener(IPositionListener listener)
  {
    listeners.remove(listener);
  }
}

// EOF

