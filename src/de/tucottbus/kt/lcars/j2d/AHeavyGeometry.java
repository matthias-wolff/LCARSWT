package de.tucottbus.kt.lcars.j2d;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicLong;

import de.tucottbus.kt.lcars.j2d.rendering.AdvGraphics2D;
import de.tucottbus.kt.lcars.j2d.rendering.HeavyRenderWorker;

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

  public void onAddToScreen()
  {
    if(workerList.containsKey(_serialNo))
      return;
        
    // create worker and add to worker list
    workerList.put(_serialNo,
        new HeavyRenderWorker(new Dimension(_width, _height),
            (input, img) -> paint2DAsync(input, img.createGraphics())));
  }

  public void onRemoveFromScreen()
  {
    workerList.remove(_serialNo);
  }

  /**
   * Implement this method to render asynchronous on a background worker
   * @param input
   * @param image
   */
  protected abstract void paint2DAsync(Object input, Graphics2D image);

  @Override
  public void paint2D(AdvGraphics2D g2d)
  {
    HeavyRenderWorker worker = workerList.get(_serialNo);
    if (worker == null)
      throw new NullPointerException("missing worker in worker list");
    g2d.drawImage(worker.GetImage(), _x, _y, null); // TODO: ImageObserver?
  }

  public void setX(int x)
  {
    _x = x;
  }

  public void setY(int y)
  {
    _y = y;
  }

  public void setWidth(int width)
  {
    if (width < 0)
      throw new IllegalArgumentException("width");
    _width = width;
  }

  public void setHeight(int height)
  {
    if (height < 0)
      throw new IllegalArgumentException("height");
    _height = height;
  }
}