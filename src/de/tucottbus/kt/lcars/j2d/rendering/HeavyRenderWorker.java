package de.tucottbus.kt.lcars.j2d.rendering;

import java.awt.image.BufferedImage;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import de.tucottbus.kt.lcars.j2d.AHeavyGeometry;

public class HeavyRenderWorker
{
  private static ExecutorService es = Executors.newWorkStealingPool();  
  
  private BufferedImage _buffer0;
  private BufferedImage _buffer1;
  
  private boolean _readToggle;
    
  private AHeavyGeometry _geom;
    
  public HeavyRenderWorker(AHeavyGeometry geom, int imageType) {
    if (geom == null)
      throw new NullPointerException("geom");
    
    _buffer0 = new BufferedImage(geom.getWidth(), geom.getHeight(), imageType);
    _buffer1 = new BufferedImage(geom.getWidth(), geom.getHeight(), imageType);
    _geom = geom;
  }
  
  public HeavyRenderWorker(AHeavyGeometry geom) {
    this(geom, BufferedImage.TYPE_3BYTE_BGR);
  }
  
  public void Invalidate(AHeavyGeometry newGeom) {
    if(newGeom == null)
      throw new NullPointerException("newInput");
    
    synchronized (_buffer0)
    {
      if (_geom != null) {
        // do not run, only update input, worker is currently running
        _geom = newGeom;
        return;
      }
      
      _geom = newGeom;      
      _readToggle = !_readToggle; // swap
      
      es.execute(() -> {
        
        // exclusive execution of paint task
        synchronized (_buffer1)
        {
          AHeavyGeometry geom;
          BufferedImage image;
          
          // synchronize double buffer swap
          synchronized (_buffer0)
          {
            geom = _geom;
            image = _readToggle ? _buffer0 : _buffer1;
            _geom = null;
          }
          geom.paint2DAsync(image.createGraphics());
        }        
      });
    }
  }
  
  public BufferedImage GetImage() {
    return _readToggle ? _buffer1 : _buffer0;
  }
  
}
