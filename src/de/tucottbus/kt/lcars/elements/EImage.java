package de.tucottbus.kt.lcars.elements;

import java.awt.Point;
import java.awt.image.ImageObserver;
import java.util.ArrayList;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Rectangle;

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
  public ArrayList<Geometry> createGeometriesInt()
  {
    int x = getBounds().x;
    int y = getBounds().y;
    
    //Image image = getImage();
//    if (image!=null)
//    {
//      java.awt.Image im;
//      im.
//      getImage().getWidth(this);
//      getImage().getHeight(this);
//    }
    ArrayList<Geometry> geos = new ArrayList<Geometry>();
    geos.add(new GImage(resourceName,new Point(x,y),this));
    return geos;
  }
  
  public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height)
  {
    if ((infoflags & (ALLBITS|SOMEBITS)) >0)
    {
      Rectangle rect = img.getBounds();
      getBounds().width  = rect.width;
      getBounds().height = rect.height;
      invalidate(true);
    }
    return true;
  }
  
  /**
   *   
   * @return image
   */
  public ImageData getImage()
  {
    return GImage.getImage(this.resourceName);  
  }

  @Override
  public boolean imageUpdate(java.awt.Image img, int infoflags, int x, int y,
      int width, int height)
  {
    // TODO Auto-generated method stub
    return false;
  }

}
