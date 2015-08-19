package de.tucottbus.kt.lcars.j2d;

import java.awt.geom.Area;
import java.lang.invoke.ConstantCallSite;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicLong;

import de.tucottbus.kt.lcars.j2d.rendering.AHeavyRenderWorker;
import de.tucottbus.kt.lcars.j2d.rendering.AdvGraphics2D;

public abstract class AHeavyGeometry extends Geometry
{
  private static final long serialVersionUID = 5157875180660436224L;

  private static final HashMap<Long, AHeavyRenderWorker> workerList = new HashMap<Long, AHeavyRenderWorker>();

  private static final AtomicLong currentSerialNo = new AtomicLong();
    
  public final long serialNo;
  
  public AHeavyGeometry(boolean foreground)
  {
    super(foreground);
    this.serialNo = currentSerialNo.incrementAndGet();
  }

  public void onAddToScreen()
  {    
    workerList.put(serialNo,createWorker());
  }

  public void onRemoveFromScreen()
  {
    workerList.remove(serialNo);
  }

  protected final AHeavyRenderWorker getWorker() {
    return workerList.get(serialNo);
  } 
  
  protected abstract AHeavyRenderWorker createWorker();
  
  @Override
  public void paint2D(AdvGraphics2D g2d)
  {
    // TODO Auto-generated method stub

  }

}
