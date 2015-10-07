package de.tucottbus.kt.lcars.elements;

import java.awt.Image;
import java.awt.Point;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.geom.Area;
import java.awt.image.ImageObserver;

import java.util.Vector;

import de.tucottbus.kt.lcars.LCARS;
import de.tucottbus.kt.lcars.Panel;
import de.tucottbus.kt.lcars.j2d.GArea;
import de.tucottbus.kt.lcars.j2d.GImage;
import de.tucottbus.kt.lcars.j2d.Geometry;

/**
 * A scaled image.
 * Based on EImage.java of Matthias Wolff
 * Required changes in GImage.java
 * 
 * @author Markus Huber
 */
public class EScaledImage extends EElement implements ImageObserver
{
  private String resourceName;
  private boolean keepAspectRatio;

  public EScaledImage(Panel panel, int x, int y, int w, int h, int style, String imageFile)
  {
    super(panel,x,y,w,h,style,null);
    this.resourceName = imageFile;
  }

  public void setKeepAspectRatio(boolean k)
  {
    if (keepAspectRatio == k)
      return;

    keepAspectRatio = k;
    invalidate(true);
  }

  public void setImageFile(String imageFile)
  {
    this.resourceName = imageFile;
    invalidate(true);
  }

  public String getImageFile()
  {
    return resourceName;
  }

  @Override
  public Vector<Geometry> createGeometriesInt()
  {
    int x = getBounds().x;
    int y = getBounds().y;
    int w = getBounds().width;
    int h = getBounds().height;

    Image img;
    if (keepAspectRatio && (img = getImage()) != null)
    {
      int width  = img.getWidth(null);
      int height = img.getHeight(null);
      float ratio = (float)width/height;

      width = (int)(ratio*h);
      if (width <= w)
      {
        x += (w-width)/2;
        w = width;
      }
      else
      {
        height = (int)(w/ratio);
        if (height <= h)
        {
          y += (h-height)/2;
          h = height;
        }
      }
    }

    Vector<Geometry> geos = new Vector<Geometry>();

    // Create background geometry if element is selected
    int       style  = getStyle();
    if ((style&LCARS.ES_SELECTED)!=0)
    {
      Area area = new Area(new Rectangle(x,y,w,h));
      x += 2;
      y += 2;
      w -= 4;
      h -= 4;
      area.subtract(new Area(new Rectangle(x,y,w,h)));
      geos.add(new GArea(area,false));
      x += 4;
      y += 4;
      w -= 8;
      h -= 8;
    }

    geos.add(new GImage(resourceName,new Point(x,y),new Dimension(w,h),this));
    return geos;
  }
  
  public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height)
  {
    if ((infoflags & (ALLBITS|SOMEBITS)) >0)
      invalidate(true);

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

  public void setSelected(boolean selected)
  {
    if (isSelected() == selected)
      return;
    super.setSelected(selected);
    invalidate(true);
  }
}
