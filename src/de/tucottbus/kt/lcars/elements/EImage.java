package de.tucottbus.kt.lcars.elements;

import java.awt.Point;
import java.awt.image.ImageObserver;
import java.util.ArrayList;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;

import de.tucottbus.kt.lcars.Panel;
import de.tucottbus.kt.lcars.geometry.GImage;
import de.tucottbus.kt.lcars.swt.ImageMeta;
import de.tucottbus.kt.lcars.geometry.AGeometry;

/**
 * An image.
 * 
 * @author Matthias Wolff
 */
public class EImage extends EElement implements ImageObserver
{
  private ImageMeta meta;
  
  public EImage(Panel panel, int x, int y, int style, ImageMeta imageMeta)
  {
    super(panel,x,y,0,0,style,null);
    meta = imageMeta;
  }

  @Override
  public ArrayList<AGeometry> createGeometriesInt()
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
    ArrayList<AGeometry> geos = new ArrayList<AGeometry>();
    geos.add(new GImage(meta,new Point(x,y),this));
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
  public Image getImage()
  {
    return meta.getImage();  
  }

  @Override
  public boolean imageUpdate(java.awt.Image img, int infoflags, int x, int y,
      int width, int height)
  {
    // TODO Auto-generated method stub
    return false;
  }

}
