package de.tucottbus.kt.lcars.geometry.rendering;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.swt.graphics.Image;

import de.tucottbus.kt.lcars.geometry.AGeometry;

/**
 * A static cache for images used by {@link AGeometry} with automatic garbage
 * collection.
 * 
 * @author Matthias Wolff
 */
public class GeometryImageCache
{
  /**
   * If <code>true<code> print debug messages at stderr.
   */
  private static final boolean DEBUG = false;
  
  /**
   *  Cache entries: Image with time of last access.
   */
  private static class CacheEntry
  {
    public final Image image;
    public long lastAccessTimeMillis;
    
    CacheEntry(Image image)
    {
      this.image = image;
      this.lastAccessTimeMillis = System.currentTimeMillis();
    }
  }
  
  /**
   * Timeout for cache objects in milliseconds. Images will be removed if not
   * accessed for longer than this period.
   */
  private static long TIMEOUT = 10000;

  /**
   * Global cache of images.
   */
  private static HashMap<Object,GeometryImageCache.CacheEntry> cache;
  
  /**
   * Puts a image into the global cache.
   * 
   * @param key
   *          The key.
   * @param image
   *          The image, <code>null</code> removes and
   *          {@linkplain Image#dispose() dispose} the image currently stored
   *          under <code>key</code>.
   * @see #getImage(Object)
   */
  public static void putImage(Object key, Image image)
  {
    cleanup();
    if (cache==null)
      cache = new HashMap<Object,GeometryImageCache.CacheEntry>();
    removeImage(key);
    if (image!=null)
      cache.put(key,new CacheEntry(image));
  }
  
  /**
   * Removes a image into the global cache and
   * {@linkplain Image#dispose() disposes} it.
   * 
   * @param key
   *          The key.
   * @see #getImage(Object)
   */
  public static void removeImage(Object key)
  {
    if (cache==null)
      return;
    cleanup();
    GeometryImageCache.CacheEntry entry = cache.get(key); 
    if (entry==null)
      return;
    if (entry.image!=null)
      entry.image.dispose();
    cache.remove(key);
  }
  
  /**
   * Retrieves a image from the global cache.
   * 
   * <p><b style="color:red">Important Remark:</b> Caching is <em>not</em> done
   * automatically! The method retrieves only images which were previously
   * stored by the application invoking {@link #putImage(int, Image)}.</p>
   * 
   * @param sliceType
   *          The key.
   * @return The image.
   * @see #putImage(Object, Image)
   */
  public static Image getImage(Object key)
  {
    cleanup();
    if (cache==null)
    {
      DebugMsg("MISS (cache does not exist)");
      return null;
    }
    GeometryImageCache.CacheEntry entry = cache.get(key);
    if (entry!=null)
    {
      entry.lastAccessTimeMillis = System.currentTimeMillis();
      //DebugMsg("HIT ("+cache.size()+" images in cache)");
      return entry.image;
    }
    else
    {
      DebugMsg("MISS ("+cache.size()+" images in cache)");
      return null;
    }
  }

  /**
   * Removes outdated images from the cache.
   */
  private static void cleanup()
  {
    if (cache==null)
      return;

    long now = System.currentTimeMillis();
    for 
    (
      Iterator<Map.Entry<Object,GeometryImageCache.CacheEntry>> it = cache.entrySet().iterator(); 
      it.hasNext(); 
    ) 
    {
      Map.Entry<Object,GeometryImageCache.CacheEntry> entry = it.next();
      if (entry.getValue()==null)
        it.remove();
      if (now-entry.getValue().lastAccessTimeMillis > TIMEOUT)
      {
        DebugMsg("AUTO-REMOVED key="+entry.getKey());
        if (entry.getValue().image!=null)
          entry.getValue().image.dispose();
        it.remove();
      }
    }      
  }
  
  /**
   * Prints a debug message.
   * 
   * @param msg
   *          The message.
   */
  private static void DebugMsg(String msg)
  {
    if (!DEBUG)
      return;
    System.err.println("GeometryImageCache: "+msg);
  }
  
}