package de.tucottbus.kt.lcars.geometry;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Area;
import java.awt.image.ImageObserver;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;

import de.tucottbus.kt.lcars.swt.ImageMeta;

/**
 * An image geometry.
 * 
 * @author Matthias Wolff
 */
public class GImage extends AGeometry
{
  private static final long serialVersionUID = -1878671224748589604L;
  
  
  public  final ImageMeta   meta;
  private final Point       pos;  
  private final Dimension   size;

  /**
   * Creates a new image geometry.
   * 
   * @param resourceName
   * @param pos
   * @param imageObeserver
   */
  public GImage(ImageMeta imageMeta, Point pos, ImageObserver imageObserver)
  {
    this(imageMeta, pos, null, imageObserver);
  }

  /**
   * Creates a new image geometry.
   * 
   * @param resourceName
   * @param pos
   * @param size
   * @param imageObeserver
   */
  public GImage(ImageMeta imageMeta, Point pos, Dimension size, ImageObserver imageObserver)
  {
    super(false);
    meta  = imageMeta;
    //TODO check observer
    //this.imageObserver = imageObserver;
    this.pos           = pos;
		this.size          = size;
  }

  @Override
  public Area getArea()
  {
    return new Area(getBounds());
  }
  
  @Override
  public Rectangle getBounds() {
		if (size != null)
			return new Rectangle(pos.x,pos.y,size.width,size.height);

    Image image = meta.getImage(); 
    return image != null
        ? new java.awt.Rectangle(pos.x,pos.y,image.getImageData().width,image.getImageData().height)
        : new Rectangle();
  }
  
  public Image getImage()
  {
    return meta.getImage();
  }
  
  @Override
  public void paint2D(GC gc)
  {
    if (meta == null) return;
    Image image = meta.getImage();
    if (image!= null)
			if (size!=null)
				gc.drawImage(image, 0, 0, image.getImageData().width, image.getImageData().height, pos.x, pos.y, size.width, size.height);
			else
				gc.drawImage(image, pos.x, pos.y);
  }

  @Override
  public String toString() {
		if (size != null)
			return getClass().getSimpleName() + " pos=(" + pos.x + "," + pos.y + ") size=(" + size.width + "," + size.height +") meta=" + meta;
		else
			return getClass().getSimpleName() + " pos=(" + pos.x + "," + pos.y + ") meta=" + meta;
  }  
}
