package de.tucottbus.kt.lcars.geometry;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Area;
import java.awt.image.ImageObserver;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Vector;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;

import de.tucottbus.kt.lcars.logging.Log;

/**
 * An image geometry.
 * 
 * @author Matthias Wolff
 */
public class GImage extends AGeometry
{
  private static final long serialVersionUID = -1878671224748589604L;
  
  private String            resourceName;
  private Point             pos;  
  private transient Image   cachedImage;
  
  /**
   * Creates a new image geometry.
   * 
   * @param resourceName
   * @param pos
   * @param imageObeserver
   */
  public GImage(String resourceName, Point pos, ImageObserver imageObeserver)
  {
    super(false);
    this.resourceName  = resourceName;
    //TODO check observer
    //this.imageObserver = imageObeserver;
    this.pos           = pos;
  }

  @Override
  public Area getArea()
  {
    ImageData image = getImage(); 
    return image != null
        ? new Area(new java.awt.Rectangle(pos.x,pos.y,image.width,image.height))
        : new Area();
  }
  
  @Override
  public Rectangle getBounds() {
    ImageData image = getImage(); 
    return image != null
        ? new java.awt.Rectangle(pos.x,pos.y,image.width,image.height)
        : new Rectangle();
  }
  
  public ImageData getImage()
  {
    final ImageData imgData = getImage(this.resourceName);
    return imgData;
  }
  
  @Override
  public void paint2D(GC gc)
  {
    if (resourceName == null)
      return;
    
    if (cachedImage == null) {
      ImageData imgData = GImage.getImage(resourceName);
      if(imgData != null)
        cachedImage = new Image(gc.getDevice(), imgData);
      else
        Log.debug("Image not found at Location: "+ resourceName);
    }
          
    gc.drawImage(cachedImage, pos.x, pos.y);
  }

  // -- The image cache --
  
  /**
   * The image cache.
   */
  private static HashMap<String,ImageData> images;

  /**
   * The keys (file names) of all images in use. Images in {@link #images} whose keys are not part
   * of this set are "unused".
   */
  private static HashSet<String> used;
  
  /**
   * Retrieves an image.
   * 
   * @param resourceName
   *          The image file name as used by {@link ClassLoader#getResource(String)}.
   * @return The image or <code>null</code> if the image could not be loaded from the file
   */
  public static ImageData getImage(String resourceName)
  {
    if (resourceName ==null) return null;    
    if (GImage.images==null) GImage.images = new HashMap<String,ImageData>();
    if (GImage.used  ==null) GImage.used   = new HashSet<String>();

    ImageData image = null;
    if (GImage.images.containsKey(resourceName))
      image = GImage.images.get(resourceName);
    else
    {
      image = loadImageFile(resourceName, true);
      GImage.images.put(resourceName,image);
    }
    GImage.used.add(resourceName);

    return image;
  }

  /**
   * Marks all cached images unused. Images are marked as used by calls to {@link #getImage(String)}.
   * 
   * @see #endCacheRun()
   */
  public synchronized static void beginCacheRun()
  {
    GImage.used = null;
  }

  /**
   * Removes all unused images from the image cache. Images are marked unused by calls to
   * {@link #beginCacheRun()} and used by calls to {@link #getImage(String)}.
   */
  public synchronized static void endCacheRun()
  {
    if (GImage.used==null) return;
    Vector<String> remove = new Vector<String>();
    for (String fileName : images.keySet())
      if (!used.contains(fileName))
        remove.add(fileName);
    for (String fileName : remove)
      images.remove(fileName);
  }

  /**
   * Loads a image from a resource file.
   * 
   * @param resourceName
   *          The image resource file name as used by {@link ClassLoader#getResource(String)}.
   * @return The image or <code>null</code> if the image could not be loaded from the file
   */
  private static ImageData loadImageFile(String path, boolean inJar)
  {
    try
    {
      return inJar
          ? new ImageData(GImage.class.getClassLoader().getResourceAsStream(path))
          : new ImageData(path);
    }
    catch(Exception e)
    {
      Log.err("ERROR: Cannot find image file \""+path+"\"", e);
      return null;
    }  
  }
  
  @Override
  public String toString() {
    return getClass().getSimpleName() + " pos=(" + pos.x + "," + pos.y + ") source="
          +(resourceName!=null ? "\"" + resourceName + "\"" : "null");
  }
  
  @Override
  protected void finalize() throws Throwable
  {
    if (cachedImage != null)
      cachedImage.dispose();
    super.finalize();
  }

}
