package de.tucottbus.kt.lcars.j2d.rendering;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;

import de.tucottbus.kt.lcars.PanelData;
import de.tucottbus.kt.lcars.logging.Log;
import de.tucottbus.kt.lcars.util.BlockingBoundedBuffer;

/**
 * This Class organizes the screen updates by using asynchronous pre
 * calculations to relieve the paint process.
 * 
 * @author Christian Borck
 *
 */
public class AsyncRenderer extends ARenderer
{

  // -- CONSTANTS --
  public static final int PREPAINT_BUFFER_SIZE = 100;


  /**
   * Synchronized buffer stores the incoming update data for the prepaint
   * calculations
   */
  BlockingBoundedBuffer<FrameData> buffer = new BlockingBoundedBuffer<FrameData>(
      PREPAINT_BUFFER_SIZE, FrameData.class);

  /**
   * Synchronizes the paint of the frame context with the pre calculations
   * process
   */
  Semaphore semaPaint = new Semaphore(0);

  /**
   * Synchronizes the pre calculations of the frame context with the paint
   * process
   */
  Semaphore semaContext = new Semaphore(1);
  
  /**
   * Background thread for preparing paints asynchronous
   */
  Thread worker;
  
  /**
   * Sychronizes the shut down of the worker with the shut down call
   */
  Semaphore workerShutdown = new Semaphore(0);
  
  /**
   * Indicates is the worker is runnable
   */
  boolean workerRun = true;
     
  /**
   * Creates an asynchronous renderer with the given render size. Background worker will be started instantly.
   * @param initialSize
   */
  public AsyncRenderer(Dimension initialSize)
  {
    super(initialSize);
    initWorker();
  }

  /**
   * Creates an asynchronous renderer with the given render size. Background worker will be started instantly.
   * @param initialSize
   */
  public AsyncRenderer(ARenderer renderer)
  {
    super(renderer);
    initWorker();
  }

  private void initWorker() {
    this.worker = new Thread(new Runnable()
    {
      @Override
      public void run()
      {
        doWork();
      }
    });

    this.worker.setName("async rendering worker");
    this.worker.start();
  }
  
  private void doWork() {
    while (workerRun || !buffer.isEmpty())
    {
      try
      {
        // read the next context and precalculate the updates
        FrameData[] nextContexts = buffer.takeAll();
        int count = nextContexts.length;
        FrameData nextContext = nextContexts[count - 1];
        FrameData currContext = super.getContext();
        if (nextContext != null)
        {
          if (count > 1)
          {
            // collapse with previous contexts
            ArrayList<FrameData> skipped = new ArrayList<FrameData>(
                count - 1);
            int iCollapse = count - 2;
            for (; iCollapse >= 0; iCollapse--)
            {
              FrameData pred = nextContexts[iCollapse];
              if (pred == null) // find resets
              {
                currContext = null;
                break;
              }
              skipped.add(0, pred);
            }
            nextContext.collapse(skipped.toArray(new FrameData[count - 2
                - iCollapse]));
          }

          nextContext.apply(currContext);
        }
        
        // update data
        // System.out.println("(Worker) Update ... [SCtxt "+semaContext.availablePermits()+"|SPaint "+
        // semaPaint.availablePermits()+"]");
        semaContext.acquire();
        setContext(nextContext);

        // System.out.println("(Worker) done with "+ (context == null ?
        // "null" : (context.getElementsToPaint().size() + " elements"))
        // +".");
        semaPaint.release();
      } catch (InterruptedException e)
      {
        Log.err("Sychronisation error while reading panel data buffer.", e);
      }
    }
    workerShutdown.release();
  }
  
  @Override
  public void applyUpdate(PanelData data, boolean incremental)
  {
    try
    {
      // System.out.println("(Input) update ... ");
      buffer.put(FrameData.create(data, incremental, this.selectiveRepaint));
      onUpdate();
      // System.out.println("(Input) done with "+(data == null ? "null" :
      // (data.elementData.size() + " elements")) +".");
    } catch (InterruptedException e)
    {
      Log.err("Sychronisation error while update panel data.", e);
    }
  }

  protected FrameData getContext() {
    FrameData result;
    try
    {
      semaPaint.acquire();
      result = super.getContext();
      semaContext.release();
    } catch (InterruptedException e)
    {
      Log.err("Synchronization error while painting on sreen.", e);
      return null;
    }
    return result;
  }
  
  
  /**
   * Resets the painter and fills the screen with the default background color (
   * {@value #DEFAULT_BG_COLOR}).
   */
  public void clear()
  {
    try
    {
      buffer.put(null);
    } catch (InterruptedException e)
    {
      Log.err("Synchronization error while resetting screen.", e);
    }
  }

  /**
   * Start the asynchronous worker thread
   */
  public void start() {
    workerRun = true;
    worker.start();
  }
  
  /**
   * Shutting down the renderer synchronously and wait till the work is finished.
   */
  public void shutdown() {
    workerRun = false;
    try
    {
      workerShutdown.acquire();
    } catch (Exception e)
    {
      Log.err("Error while shutting down asynchronious renderer.", e);
    }
  }
}
