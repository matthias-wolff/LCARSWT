package de.tucottbus.kt.lcars.j2d.rendering;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BiConsumer;

public class HeavyRenderWorker
{
  private static ExecutorService es = Executors.newWorkStealingPool();  
  
  private BufferedImage _buffer0;
  private BufferedImage _buffer1;
  
  private boolean _readToggle;
    
  private Object _input = null;
  
  private final BiConsumer<Object,BufferedImage> _paintMethod;
  
  public HeavyRenderWorker(Dimension size, int imageType, BiConsumer<Object,BufferedImage> paintMethod) {
    if (paintMethod == null)
      throw new NullPointerException("paintMethod");
    
    _buffer0 = new BufferedImage(size.width, size.height, imageType);
    _buffer1 = new BufferedImage(size.width, size.height, imageType);
    _paintMethod = paintMethod;
  }
  
  public HeavyRenderWorker(Dimension size, BiConsumer<Object,BufferedImage> paintMethod) {
    this(size, BufferedImage.TYPE_3BYTE_BGR, paintMethod);
  }
  
  public void Invalidate(Object newInput) {
    if(newInput == null)
      throw new NullPointerException("newInput");
    
    synchronized (_buffer0)
    {
      if (_input != null) {
        // do not run, only update input, worker is currently running
        _input = newInput;
        return;
      }
      
      _input = newInput;      
      _readToggle = !_readToggle; // swap
      
      es.execute(() -> {
        
        // exclusive execution of paint task
        synchronized (_buffer1)
        {
          Object input;
          BufferedImage image;
          
          // synchronize double buffer swap
          synchronized (_buffer0)
          {
            input = _input;
            image = _readToggle ? _buffer0 : _buffer1;
            _input = null;
          }
          _paintMethod.accept(input, image);         
        }        
      });
      
    }
    
  }
  
  public BufferedImage GetImage() {
    return _readToggle ? _buffer1 : _buffer0;
  }
  
}
