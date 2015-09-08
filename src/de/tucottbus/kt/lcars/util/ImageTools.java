package de.tucottbus.kt.lcars.util;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DirectColorModel;
import java.awt.image.FilteredImageSource;
import java.awt.image.IndexColorModel;
import java.awt.image.PixelGrabber;
import java.awt.image.RGBImageFilter;
import java.awt.image.WritableRaster;

import javax.swing.ImageIcon;

import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;

import de.tucottbus.kt.lcars.LCARS;
import de.tucottbus.kt.lcars.j2d.GImage;

/**
 * This class supplies assorted {@linkplain java.awt.Image image} tools.
 * 
 * @author Matthias Wolff
 */
public class ImageTools
{

  /**
   * Re-colors an image to match a given LCARS color style.
   * 
   * @param src
   *          The source image.
   * @param colorScheme
   *          The LCARS color scheme, one of the {@link LCARS}<code>.CS_XXX</code> constants.
   * @param style
   *          The LCARS style to adapt the image to.
   * @return A re-colored copy of the source image. 
   */
  public static Image colorFilter(Image src, int colorScheme, int style)
  {
    class LcarsColorFilter extends RGBImageFilter
    {
      float[] hsbD;
      float[] hsbS;
      
      public LcarsColorFilter(int colorScheme, int style)
      {
        Color c = LCARS.getColor(colorScheme,style);
        hsbD = Color.RGBtoHSB(c.getRed(),c.getGreen(),c.getBlue(),null);
      }
      
      @Override
      public int filterRGB(int x, int y, int rgb)
      {
        int r = (rgb>>16)&0xff;
        int g = (rgb>>8)&0xff;
        int b = rgb&0xff;
        hsbS = Color.RGBtoHSB(r,g,b,hsbS);
        return Color.HSBtoRGB(hsbD[0],hsbS[1],hsbS[2]);
      }
    }

    LcarsColorFilter    lcf = new LcarsColorFilter(colorScheme,style);
    FilteredImageSource fis = new FilteredImageSource(src.getSource(),lcf);
    return Toolkit.getDefaultToolkit().createImage(fis);
  }
  
  /**
   * Loads an image from a resource file.
   * 
   * @param imageFile
   *          The image file name
   * @return The image or <code>null</code> if the image file was not found.
   * @deprecated Image files handled by {@link GImage}
   */
  public static Image loadImage(String imageFile)
  {
    ImageData data = GImage.getImage(imageFile).getImageData();
    ColorModel colorModel = null;
    PaletteData palette = data.palette;
    if (palette.isDirect) {
      colorModel = new DirectColorModel(data.depth, palette.redMask, palette.greenMask, palette.blueMask);
      BufferedImage bufferedImage = new BufferedImage(colorModel, colorModel.createCompatibleWritableRaster(data.width, data.height), false, null);
      for (int y = 0; y < data.height; y++) {
        for (int x = 0; x < data.width; x++) {
          int pixel = data.getPixel(x, y);
          RGB rgb = palette.getRGB(pixel);
          bufferedImage.setRGB(x, y,  rgb.red << 16 | rgb.green << 8 | rgb.blue);
        }
      }
      return bufferedImage;
    } else {
      RGB[] rgbs = palette.getRGBs();
      byte[] red = new byte[rgbs.length];
      byte[] green = new byte[rgbs.length];
      byte[] blue = new byte[rgbs.length];
      for (int i = 0; i < rgbs.length; i++) {
        RGB rgb = rgbs[i];
        red[i] = (byte)rgb.red;
        green[i] = (byte)rgb.green;
        blue[i] = (byte)rgb.blue;
      }
      if (data.transparentPixel != -1) {
        colorModel = new IndexColorModel(data.depth, rgbs.length, red, green, blue, data.transparentPixel);
      } else {
        colorModel = new IndexColorModel(data.depth, rgbs.length, red, green, blue);
      }   
      BufferedImage bufferedImage = new BufferedImage(colorModel, colorModel.createCompatibleWritableRaster(data.width, data.height), false, null);
      WritableRaster raster = bufferedImage.getRaster();
      int[] pixelArray = new int[1];
      for (int y = 0; y < data.height; y++) {
        for (int x = 0; x < data.width; x++) {
          int pixel = data.getPixel(x, y);
          pixelArray[0] = pixel;
          raster.setPixel(x, y, pixelArray);
        }
      }
      return bufferedImage;
     }
  }

  /**
   * This method returns a buffered image with the contents of an image
   * http://www.exampledepot.com/egs/java.awt.image/Image2Buf.html
   */
  public static BufferedImage toBufferedImage(Image image)
  {
    if (image instanceof BufferedImage) { return (BufferedImage)image; }
  
    // This code ensures that all the pixels in the image are loaded
    image = new ImageIcon(image).getImage();
  
    // Determine if the image has transparent pixels; for this method's
    // implementation, see Determining If an Image Has Transparent Pixels
    boolean hasAlpha = ImageTools.hasAlpha(image);
  
    // Create a buffered image with a format that's compatible with the iscreen
    BufferedImage bimage = null;
    GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
    try
    {
      // Determine the type of transparency of the new buffered image
      int transparency = Transparency.OPAQUE;
      if (hasAlpha)
      {
        transparency = Transparency.BITMASK;
      }
  
      // Create the buffered image
      GraphicsDevice gs = ge.getDefaultScreenDevice();
      GraphicsConfiguration gc = gs.getDefaultConfiguration();
      bimage = gc.createCompatibleImage(image.getWidth(null), image.getHeight(null), transparency);
    }
    catch (HeadlessException e)
    {
      // The system does not have a iscreen
    }
  
    if (bimage == null)
    {
      // Create a buffered image using the default color model
      int type = BufferedImage.TYPE_INT_RGB;
      if (hasAlpha)
      {
        type = BufferedImage.TYPE_INT_ARGB;
      }
      bimage = new BufferedImage(image.getWidth(null), image.getHeight(null), type);
    }
  
    // Copy image to buffered image
    Graphics g = bimage.createGraphics();
  
    // Paint the image onto the buffered image
    g.drawImage(image, 0, 0, null);
    g.dispose();
  
    return bimage;
  }

  /**
   * This method returns true if the specified image has transparent pixels
   * http://www.exampledepot.com/egs/java.awt.image/HasAlpha.html
   */
  public static boolean hasAlpha(Image image)
  {
    // If buffered image, the color model is readily available
    if (image instanceof BufferedImage)
    {
      BufferedImage bimage = (BufferedImage)image;
      return bimage.getColorModel().hasAlpha();
    }
  
    // Use a pixel grabber to retrieve the image's color model;
    // grabbing a single pixel is usually sufficient
    PixelGrabber pg = new PixelGrabber(image,0,0,1,1,false);
    try
    {
      pg.grabPixels();
    }
    catch (InterruptedException e)
    {
    }
  
    // Get the image's color model
    ColorModel cm = pg.getColorModel();
    return cm.hasAlpha();
  }
  
}
