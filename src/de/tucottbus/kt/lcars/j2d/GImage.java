package de.tucottbus.kt.lcars.j2d;

import java.awt.Point;
import java.awt.geom.Area;
import java.awt.image.ImageObserver;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Vector;

import org.eclipse.swt.SWTException;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;

import de.tucottbus.kt.lcars.logging.Log;

/**
 * An image geometry.
 * 
 * @author Matthias Wolff
 */
public class GImage extends Geometry
{
  private static final long       serialVersionUID = 1L;
  private String                  resourceName;
  //private transient ImageObserver imageObserver;
  private Point                   pos;
  private Image                   cachedImg;
  private boolean                 resNotFound = false;
  
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
    Image image = getImage();
    if(image == null)
      return new Area();
    Rectangle rect = image.getBounds();
    return new Area(new java.awt.Rectangle(pos.x,pos.y,rect.width,rect.height));
  }
  
  
  public Image getImage()
  {
    if(cachedImg == null && !resNotFound)
    {
      cachedImg = getImage(this.resourceName);
      resNotFound = cachedImg == null;
    }
    return cachedImg;
  }
  
  @Override
  public void paint2D(GC gc)
  {
    if (resourceName == null)
      return;
    
    Image image = GImage.getImage(resourceName);
    if(image != null)
      gc.drawImage(image, pos.x, pos.y);
    else
      Log.debug("Image not found at Location: "+ resourceName);
  }

  // -- The image cache --
  
  /**
   * The image cache.
   */
  private static HashMap<String,Image> images;

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
  public static Image getImage(String resourceName)
  {
    if (resourceName ==null) return null;    
    if (GImage.images==null) GImage.images = new HashMap<String,Image>();
    if (GImage.used  ==null) GImage.used   = new HashSet<String>();

    Image image = null;
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
  private static Image loadImageFile(String path, boolean inJar)
  {
    try
    {
      return inJar
          ? new Image(null, GImage.class.getClassLoader().getResourceAsStream(path))
          : new Image(null, path);
    }
    catch(SWTException ex)
    {
      Log.warn("ERROR: Cannot find image file \""+path+"\"");
      return null;
    }  
  }
  
}
