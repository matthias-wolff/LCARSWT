package de.tucottbus.kt.lcars.geometry;

import java.awt.Rectangle;
import java.awt.geom.Area;
import java.io.Serializable;
import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;

import de.tucottbus.kt.lcars.geometry.rendering.ARemotePaintListener;

/**
 * 
 * @author Christian Borck
 *
 */
public final class HeavyGeometry <TData extends Serializable> extends AGeometry
{
  private static final long serialVersionUID = 5157875180660436224L;

  private static final AtomicLong currentSerialNo = new AtomicLong();

  public final long serialNo;

  private int _x;
  private int _y;
  private int _width;
  private int _height;
    
  private boolean                     _dataInvalid     = false;
  private TData                       _data            = null;
  private boolean                     _listenerInvalid = false;
  private ARemotePaintListener<TData> _listener;
  
  public HeavyGeometry(Rectangle bounds, boolean foreground)
  {
    super(foreground);
    this.serialNo = currentSerialNo.incrementAndGet();
    _x = bounds.x;
    _y = bounds.y;
    _width = bounds.width;
    _height = bounds.height;
  }

  public HeavyGeometry(int x, int y, int width, int height, boolean foreground)
  {
    super(foreground);
    this.serialNo = currentSerialNo.incrementAndGet();
    _x = x;
    _y = y;
    _width = width;
    _height = height;
  }

  public void setPaintListener(ARemotePaintListener<TData> listener) {
    if (listener == _listener) return;
    _listener = listener;
    _listenerInvalid = true;
  }
  
  public HeavyRenderWorker<TData> createWorker() {
    return new HeavyRenderWorker<TData>(this);
  }
  
  @Override
  public void update(boolean visible)
  {
    if (visible)
      HeavyRenderWorker.invalidate(this);            
    else
      HeavyRenderWorker.shutDown(this);    
  }

  @Override
  public final void paint2D(GC gc)
  {
    HeavyRenderWorker.paint2D(gc, this);
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
  public void invalidate(TData data)
  {
    if (data == null)
      throw new NullPointerException("input");    
    _data = data;
    _dataInvalid = true;
  }

  public HeavyGeometry<TData> getUpdate(boolean incremental)
  {
    // TODO: right way to flat (not deep) copy including inheritage?
    HeavyGeometry<TData> result = new HeavyGeometry<TData>(_x, _y, _width, _height, incremental);
    boolean invalid = _dataInvalid;
    _dataInvalid = false;
    if (incremental || invalid)
      result._data = _data;
    result._dataInvalid = invalid;
    
    invalid = _listenerInvalid;
    _listenerInvalid = false;
    if (incremental || invalid)
      result._data = _data;
    result._listenerInvalid = invalid;
    
    return result;
  }

  @Override
  public Area getArea()
  {
    return new Area(new Rectangle(_y, _x, _width, _height));
  }

  @Override
  public Rectangle getBounds()
  {
    return new Rectangle(_y, _x, _width, _height);
  }

  public boolean isDataInvalid() {
    return _dataInvalid;
  }
  
  public boolean isListenerInvalid() {
    return _listenerInvalid;
  }
 
  public ARemotePaintListener<TData> getListener() {
    return _listener;
  }
  
  public TData getData() {
    return _data;
  }
  
}
