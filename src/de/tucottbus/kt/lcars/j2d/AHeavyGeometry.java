package de.tucottbus.kt.lcars.j2d;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.swt.graphics.GC;

import de.tucottbus.kt.lcars.j2d.rendering.HeavyRenderWorker;
import de.tucottbus.kt.lcars.logging.Log;

/**
 * 
 * @author Christian Borck
 *
 */
public abstract class AHeavyGeometry extends Geometry
{
  private static final long serialVersionUID = 5157875180660436224L;

  private static final HashMap<Long, HeavyRenderWorker> workerList = new HashMap<Long, HeavyRenderWorker>();

  private static final AtomicLong currentSerialNo = new AtomicLong();

  public final long _serialNo;

  private int _x;
  private int _y;
  private int _width;
  private int _height;

  private Object _input = null;

  public AHeavyGeometry(Point position, Dimension size, boolean foreground)
  {
    super(foreground);
    this._serialNo = currentSerialNo.incrementAndGet();
    _x = position.x;
    _y = position.y;
    _width = size.width;
    _height = size.height;
  }

  public AHeavyGeometry(Rectangle bounds, boolean foreground)
  {
    super(foreground);
    this._serialNo = currentSerialNo.incrementAndGet();
    _x = bounds.x;
    _y = bounds.y;
    _width = bounds.width;
    _height = bounds.height;
  }

  @Override
  public void onVisibilityChanged(boolean visible)
  {
    if (visible)
    {
      // TODO: register in FrameData
      
      if (workerList.containsKey(_serialNo))
        return;
      
      // create worker and add to worker list
      workerList.put(_serialNo, new HeavyRenderWorker(this));      
    }
    else
      workerList.remove(_serialNo);    
  }

  /**
   * Implement this method to render asynchronous on a background worker
   * 
   * @param input
   * @param image
   */
  public abstract void paint2DAsync(GC image);

  @Override
  public void paint2D(GC gc)
  {
    HeavyRenderWorker worker = workerList.get(_serialNo);
    if (worker == null)
      throw new NullPointerException("missing worker in worker list");
    gc.drawImage(worker.GetImage(), _x, _y); // TODO: ImageObserver?
  }

  public int getX()
  {
    return _x;
  }

  public void setX(int x)
  {
    _x = x;
  }

  public int getY()
  {
    return _y;
  }

  public void setY(int y)
  {
    _y = y;
  }

  public int getWidth()
  {
    return _width;
  }

  public void setWidth(int width)
  {
    if (width < 0)
      throw new IllegalArgumentException("width");
    _width = width;
  }

  public int getHeight()
  {
    return _height;
  }

  public void setHeight(int height)
  {
    if (height < 0)
      throw new IllegalArgumentException("height");
    _height = height;
  }

  /**
   * Invalidates the object by setting new input data.
   * @param input - data which will be used as input for the rendering worker on the screen.
   */
  protected void invalidate(Object input)
  {
    if (input == null)
      throw new NullPointerException("input");
    _input = input;
  }

  public AHeavyGeometry getUpdate(boolean incremental)
  {
    // TODO: right way to flat (not deep) copy including inheritage?
    try
    {
      AHeavyGeometry result = (AHeavyGeometry) this.clone();
      if (incremental)
        _input = null;
      return result;
    } catch (CloneNotSupportedException e)
    {
      // TODO Auto-generated catch block
      e.printStackTrace();
      return null;
    }
  }

  public void applyUpdate()
  {
    HeavyRenderWorker worker = workerList.get(_serialNo);
    if (worker == null)
      Log.err("Invalid state: some Geometry update data come up but the worker with the serial number "
              + _serialNo + " does not exist to process it.");
    else if (_input != null)
      worker.Invalidate(this);
  }

}
