package de.tucottbus.kt.lcars.elements;

import java.awt.Image;
import java.awt.Point;
import java.awt.image.ImageObserver;
import java.util.Vector;

import de.tucottbus.kt.lcars.Panel;
import de.tucottbus.kt.lcars.j2d.GImage;
import de.tucottbus.kt.lcars.j2d.Geometry;

/**
 * An image.
 * 
 * @author Matthias Wolff
 */
public class EImage extends EElement implements ImageObserver
{
  private String resourceName;
  
  public EImage(Panel panel, int x, int y, int style, String imageFile)
  {
    super(panel,x,y,0,0,style,null);
    this.resourceName = imageFile;
  }

  @Override
  public Vector<Geometry> createGeometriesInt()
  {
    int x = getBounds().x;
    int y = getBounds().y;
    
    Image image = getImage();
    if (image!=null)
    {
      getImage().getWidth(this);
      getImage().getHeight(this);
    }
    Vector<Geometry> geos = new Vector<Geometry>();
    geos.add(new GImage(resourceName,new Point(x,y),this));
    return geos;
  }
  
  public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height)
  {
    if ((infoflags & (ALLBITS|SOMEBITS)) >0)
    {
      getBounds().width  = img.getWidth(null);
      getBounds().height = img.getHeight(null);
      invalidate(true);
    }
    return true;
  }
  
  /**
   *   
   * @return image
   */
  public Image getImage()
  {
    return GImage.getImage(this.resourceName);  
  }

}
