package de.tucottbus.kt.lcars.elements;

import java.awt.Point;
import java.awt.Dimension;
import java.awt.geom.Area;
import java.awt.image.ImageObserver;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import java.util.ArrayList;

import de.tucottbus.kt.lcars.LCARS;
import de.tucottbus.kt.lcars.Panel;
import de.tucottbus.kt.lcars.geometry.GImage;
import de.tucottbus.kt.lcars.swt.ImageMeta;
import de.tucottbus.kt.lcars.geometry.AGeometry;
import de.tucottbus.kt.lcars.geometry.GArea;

/**
 * A scaled image.
 * Based on EImage.java of Matthias Wolff
 * Required changes in GImage.java
 * 
 * @author Markus Huber
 */
public class EScaledImage extends EElement implements ImageObserver
{
  private ImageMeta meta;
  private boolean keepAspectRatio;
  
  public EScaledImage(Panel panel, int x, int y, int w, int h, int style, ImageMeta imageMeta)
  {
    super(panel,x,y,w,h,style,null);
    meta = imageMeta;
  }

  public void setKeepAspectRatio(boolean k)
  {
    if (keepAspectRatio == k)
      return;

    keepAspectRatio = k;
    invalidate(true);
  }

  public void setImageMeta(ImageMeta imageMeta)
  {
    meta = imageMeta;
    invalidate(true);
  }

	public String getImagePath()
	{
		if (meta == null)
			return null;
		else
			return meta.getPath();
	}

  @Override
  public ArrayList<AGeometry> createGeometriesInt()
  {
    int x = getBounds().x;
    int y = getBounds().y;
    int w = getBounds().width;
    int h = getBounds().height;

    Image img;
    if (keepAspectRatio && (img = getImage()) != null)
    {
			Rectangle b = img.getBounds();
      int width  = b.width;
      int height = b.height;
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

    ArrayList<AGeometry> geos = new ArrayList<AGeometry>();

    // Create background geometry if element is selected
    int       style  = getStyle();
    if ((style&LCARS.ES_SELECTED)!=0)
    {
      Area area = new Area(new java.awt.Rectangle(x,y,w,h));
      x += 2;
      y += 2;
      w -= 4;
      h -= 4;
      area.subtract(new Area(new java.awt.Rectangle(x,y,w,h)));
      geos.add(new GArea(area,false));
      x += 4;
      y += 4;
      w -= 8;
      h -= 8;
    }

    geos.add(new GImage(meta,new Point(x,y),new Dimension(w,h),this));
    return geos;
  }
  
  @Override
  public boolean imageUpdate(java.awt.Image img, int infoflags, int x, int y,
      int width, int height)
  {
    // TODO Auto-generated method stub
    return false;
  }
  
  /**
   *   
   * @return image
   */
  public Image getImage()
  {
		if (meta != null)
			return meta.getImage();
		else
			return null;
  }

  public void setSelected(boolean selected)
  {
    if (isSelected() == selected)
      return;
    super.setSelected(selected);
    invalidate(true);
  }
}
