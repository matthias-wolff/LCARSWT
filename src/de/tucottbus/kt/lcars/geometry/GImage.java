package de.tucottbus.kt.lcars.geometry;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Area;
import java.awt.image.ImageObserver;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;

import de.tucottbus.kt.lcars.swt.ImageMeta;

/**
 * An image geometry.
 * 
 * @author Matthias Wolff
 */
public class GImage extends AGeometry
{
  private static final long serialVersionUID = -1878671224748589604L;
  
  /**
   * Meta information about the image that will be drawn using {@link paint2D(GC)}
   */
  public  final ImageMeta   meta;
  private final Point       pos;  
  private final Dimension   size;

  /**
   * Creates a new image geometry.
   * 
   * @param imageMeta
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
   * @param imageMeta
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
  public Rectangle getBounds()
  {
		if (size != null)
			return new Rectangle(pos.x,pos.y,size.width,size.height);

		if (meta==null)
		  return new Rectangle();
    Image image = meta.getImage(); 
    return image != null
        ? new java.awt.Rectangle(pos.x,pos.y,image.getImageData().width,image.getImageData().height)
        : new Rectangle();
  }
  
  /**
   * Returns the image which will be drawn using {@link paint2D(GC)} and is provided by the member {@link #meta}.
   * @return
   */
  public Image getImage()
  {
    return meta.getImage();
  }
  
  @Override
  public void paint2D(GC gc)
  {
    Image image;
    if (meta == null || (image=meta.getImage()) == null) return;
		if (size!=null)
		{
		  ImageData dt = image.getImageData();
		  gc.drawImage(image, 0, 0, dt.width, dt.height, pos.x, pos.y, size.width, size.height);
		}
		else
			gc.drawImage(image, pos.x, pos.y);
  }

  @Override
  public String toString()
  {
    return getClass().getSimpleName() + " pos=(" + pos.x + "," + pos.y +
           (size != null ?             ") size=(" + size.width + "," + size.height : "") +
                                       ") meta=" + meta;
  }  
}
