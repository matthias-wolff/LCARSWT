package de.tucottbus.kt.lcars.geometry;

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
  
  /**
   * Creates a new image geometry.
   * 
   * @param resourceName
   * @param pos
   * @param imageObeserver
   */
  public GImage(ImageMeta imageMeta, Point pos, ImageObserver imageObeserver)
  {
    super(false);
    meta  = imageMeta;
    //TODO check observer
    //this.imageObserver = imageObeserver;
    this.pos           = pos;
  }

  @Override
  public Area getArea()
  {
    return new Area(getBounds());
  }
  
  @Override
  public Rectangle getBounds() {
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
      gc.drawImage(image, pos.x, pos.y);
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + " pos=(" + pos.x + "," + pos.y + ") meta=" + meta;
  }  
}
