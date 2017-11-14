package de.tucottbus.kt.lcars.geometry;

import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.util.HashMap;
import java.util.concurrent.ArrayBlockingQueue;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

import de.tucottbus.kt.lcars.geometry.rendering.ARemotePaintListener;
import de.tucottbus.kt.lcars.logging.Log;

/**
 * Represents a worker which draws {@link TData} to a buffer image which will be drawn to a
 * {@link de.tucottbus.kt.lcars.geometry.HeavyGeometry<TData>} asynchronous. The drawing of
 * {@link TData} to the buffer image will be defined by a
 * {@link de.tucottbus.kt.lcars.geometry.IWorkspace<TData>}.
 * @author Christian Borck
 *
 * @param <TData> type of input used to draw an image.
 * @deprecated
 */
@Deprecated
public class HeavyRenderWorker<TData extends Serializable>
{
  private static final HashMap<Long, HeavyRenderWorker<?>> activeRenderer = new HashMap<>(20);
  
  private final Class<? extends HeavyGeometry<TData>> clazz;
  private final Thread worker;
  private Image _buffer0;
  private Image _buffer1;
  private boolean _readToggle;
  private boolean _running;
  private TData _data;
  private Point pos = new Point(0, 0);
  
  private ARemotePaintListener<TData> _listener;
  
  private ArrayBlockingQueue<Runnable> renderQueue = new ArrayBlockingQueue<>(20);
  
  private IWorkspace<TData> workspace;
    
  @SuppressWarnings("unchecked")
  private HeavyRenderWorker(HeavyGeometry<TData> geom, int imageType) {
    if (geom == null)
      throw new NullPointerException("geom");
    _buffer0 = new Image(null, geom.getWidth(), geom.getHeight());
    _buffer0.type = imageType;
    _buffer1 = new Image(null, geom.getWidth(), geom.getHeight());
    _buffer1.type = imageType;
    _running = true;
    
    clazz = (Class<? extends HeavyGeometry<TData>>)geom.getClass();
    
    HeavyRenderWorker<TData> hrw = this;
    worker = new Thread(() ->
    {
      while (_running)
        try
        {
          while (_running)
            renderQueue.take().run();
        } catch (Exception e)
        {
          Log.err("Some error occured while executing asynchronous renderer " + hrw.toString(), e);
        }
    },HeavyRenderWorker.class.getSimpleName() + "#" + geom.serialNo);
    worker.run();
    doInvalidate(geom);
  }
  
  HeavyRenderWorker(HeavyGeometry<TData> geom)
  {
    this(geom, BufferedImage.TYPE_3BYTE_BGR);
  }
  
  private void doInvalidate(HeavyGeometry<?> geometry)
  {
    if(geometry == null)
      throw new NullPointerException("newInput");
    
    HeavyGeometry<TData> geo = clazz.cast(geometry);
    try
    {
      renderQueue.put(() ->
      {
        if (geometry.getX() != pos.x || geometry.getY() != pos.y)
          pos = new Point(geometry.getX(), geometry.getY());
           
        //TODO: generic type check
        
        if (geometry.isListenerInvalid())
        {
          if (_listener != null)
            _listener.shutdown(workspace);
          ARemotePaintListener<TData> listener = geo.getListener();
          _listener = listener;
          workspace = (listener != null) ? listener.initialize() : null;
        }
        
        if (geometry.isDataInvalid())
          _data = geo.getData();      
        if (_listener == null || (!geometry.isListenerInvalid() && !geometry.isDataInvalid()))
          return;
        
        Image buffer = (_readToggle = !_readToggle)
            ? _buffer0 : _buffer1;        
        workspace.apply(buffer, _data);
      });
    } catch (InterruptedException e)
    {
      Log.err("Interupted asynchronous rendering.", e);
    }    
  }
  
  public static void invalidate(HeavyGeometry<?> newGeom)
  {
    if (newGeom == null) return;
    HeavyRenderWorker<?> worker = activeRenderer.get(newGeom.serialNo);
    if (worker != null)
      worker.doInvalidate(newGeom);
    else
      activeRenderer.put(newGeom.serialNo, newGeom.createWorker());
  }
  
  private void doPaint2D(GC gc)
  {
    Point pos = this.pos;
    gc.drawImage(_readToggle ? _buffer0 : _buffer1, pos.x, pos.y);
  }
  
  public static void paint2D(GC gc, HeavyGeometry<?> newGeom)
  {
    HeavyRenderWorker<?> worker = activeRenderer.get(newGeom.serialNo);
    if (worker != null)
      worker.doPaint2D(gc);
  }
  
  public static void shutDown(HeavyGeometry<?> geometry) 
  {
    HeavyRenderWorker<?> worker = activeRenderer.remove(geometry.serialNo);
    if (worker == null) return;
    worker._running = false;
  }
  
  public Image GetImage() {
    return _readToggle ? _buffer1 : _buffer0;
  }
  
}
