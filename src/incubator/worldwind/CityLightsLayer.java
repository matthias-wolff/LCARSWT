package incubator.worldwind;

import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.Renderable;
import gov.nasa.worldwind.render.SurfaceImage;
import gov.nasa.worldwindx.sunlight.SunController;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Date;

import javax.imageio.ImageIO;

import de.tucottbus.kt.lcars.LCARS;
import de.tucottbus.kt.lcars.logging.Log;

public class CityLightsLayer extends RenderableLayer
{
  private static final String IMAGE_PATH = "incubator/worldwind/resources/CityLights.png";
  
  private LatLon subsolarPoint;
  
  private BufferedImage bi;
  
  private final SurfaceImage si;
  
  private long lastUpdate = 0;
  
  public CityLightsLayer()
  {
    super();

    this.si = new SurfaceImage();
    setName("City Lights");
    addRenderable(this.si);
    // Disable picking for the layer because it covers the full sphere and will override a terrain pick.
    setPickEnabled(false);
    
    java.awt.EventQueue.invokeLater(new Runnable()
    {
      public void run()
      {
        try
        {
          CityLightsLayer.this.bi = loadImage(LCARS.class.getClassLoader().getResource(IMAGE_PATH));
        } catch (IOException e)
        {
          e.printStackTrace();
        }
      }
    });
  }

  @Override
  protected void doPreRender(DrawContext dc, Iterable<? extends Renderable> renderables)
  {
    dc.beginStandardLighting();
    try
    {
      super.doPreRender(dc, renderables);
    }
    finally
    {
      dc.endStandardLighting();
    }
 }

  @Override
  protected void doRender(DrawContext dc, Iterable<? extends Renderable> renderables)
  {
    dc.beginStandardLighting();
    try
    {
      super.doRender(dc, renderables);
    }
    finally
    {
      dc.endStandardLighting();
    }
  }

  public void setSubsolarPoint(LatLon subsolarPoint)
  {
    this.subsolarPoint = subsolarPoint;
    update();
  }
  
  public void update()
  {
    if (!isEnabled() || this.subsolarPoint==null || this.bi==null) return;
    long now = System.currentTimeMillis();
    if (now-lastUpdate<60e3) return;
    lastUpdate = now;

    Log.info("WWJ","CityLightsLayer - Starting update thread");
    (new Thread()
    {
      @Override
      public void run()
      {
        Log.info("WWJ","CityLightsLayer - Update alpha shade");
        int w = CityLightsLayer.this.bi.getWidth();
        int h = CityLightsLayer.this.bi.getHeight();
        BufferedImage imgAlpha = getAlpha(subsolarPoint,w,h);
        applyAlpha(CityLightsLayer.this.bi.getRaster(),imgAlpha.getRaster());
        CityLightsLayer.this.si.setImageSource(CityLightsLayer.this.bi,Sector.FULL_SPHERE);

//        long then = System.currentTimeMillis();
//        File file = new File("d:/xfer/"+CityLightsLayer.class.getSimpleName()+".png");
//        try
//        {
//          ImageIO.write(imgCli,"PNG",file);
//        } catch (IOException e)
//        {
//          e.printStackTrace();
//        }
//        System.out.println("save: "+(System.currentTimeMillis()-then)+" ms");
      }
    }
    ).start();
  }

  /**
   * Loads to city lights image.
   * 
   * @param imageSrc
   *          The image source, either a  {@link URL} or a {@link File} object.
   * @return The buffered image converted to {@link BufferedImage#TYPE_INT_ARGB}.
   * @throws IOException
   *          On I/O erros.
   * @throws IllegalArgumentException
   *          If <code>imageSrc/code> is <code>null</code> of not a {@link URL}
   *          or a {@link File} object.
   */
  protected static BufferedImage loadImage(Object imageSrc) throws IOException
  {
    long then = System.currentTimeMillis();

    // Load city lights image
    Image img;
    if (imageSrc==null)
      throw new IllegalArgumentException("Image source must be null");
    if (imageSrc instanceof URL)
      img = ImageIO.read((URL)imageSrc);
    else if (imageSrc instanceof String)
      img = ImageIO.read(new File((String)imageSrc));
    else
      throw new IllegalArgumentException("Image source must be a File or a URL");
    
    // Convert to BufferedImage.TYPE_INT_ARGB
    BufferedImage imgConverted = new BufferedImage(img.getWidth(null),
      img.getHeight(null),BufferedImage.TYPE_INT_ARGB);
    imgConverted.getGraphics().drawImage(img,0,0,null);
    
    System.out.println("loadImage(): "+(System.currentTimeMillis()-then)+" ms");
    return imgConverted;
  }

  /**
   * Returns an image of the sun shade.
   * 
   * @param subsolarPoint
   *          The sub-solar point.
   * @param width
   *           The image width.
   * @param width
   *           The image height.
   * @return The sun shade image.
   */
  protected static BufferedImage getAlpha(LatLon subsolarPoint, int width, int height)
  {
    long then = System.currentTimeMillis();
    System.out.println("subsolar point: "+subsolarPoint);

    // Create sun shade image
    int w = 360;
    int h = 180;
    BufferedImage imgAlpha = new BufferedImage(w,h,BufferedImage.TYPE_INT_RGB);
    double twilight = 5. / 180.*Math.PI; // Twilight zone in radians
    for (int y=0; y<h; y++)
    {
      double lat = ((double)y/(double)h-0.5)*Math.PI;
      for (int x=0; x<w; x++)
      {
        double xr = ((double)x/(double)w-0.5)*Math.PI*2;
        double lon = xr;
        double dst = LatLon.greatCircleDistance(LatLon.fromRadians(lat,lon),subsolarPoint).radians;
        //int c = 255-(int)(Math.min(1,Math.max(0,dst/Math.PI))*255.);
        //int c = dst<=Math.PI/2 ? 200 : 0;
        int c = (int)(255*Math.min(1,Math.max(0,(dst-Math.PI/2)/twilight)));
        imgAlpha.setRGB(x,h-y-1,(c<<16)|(c<<8)|c);
      }
    }
    BufferedImage imgAlphaScaled = new BufferedImage(width,height,BufferedImage.TYPE_INT_ARGB);
    imgAlphaScaled.getGraphics().drawImage(imgAlpha.getScaledInstance(width,height,Image.SCALE_FAST),0,0,null);
    System.out.println("getAlpha(): "+(System.currentTimeMillis()-then)+" ms");
    return imgAlphaScaled;
  }
  
  public static void applyAlpha(WritableRaster imgCli, Raster imgAlpha)
  {
    long then = System.currentTimeMillis();
    int[] rgba = new int[4];
    int[] a    = new int[4];
    for (int x=0; x<imgCli.getWidth(); x++)
      for (int y=0; y<imgCli.getHeight(); y++)
      {
        imgAlpha.getPixel(x,y,a);
        if (a[0]==255) continue;
        imgCli.getPixel(x,y,rgba);
        rgba[3]=Math.min(a[0],rgba[3]);
        imgCli.setPixel(x,y,rgba);
      }
    System.out.println("applyAlpha(): "+(System.currentTimeMillis()-then)+" ms");
  }

  // == MAIN METHOD (FOR TESTING ONLY!) ==
  
  public static void main(String[] args)
  {
    long then = System.currentTimeMillis();
    try
    {
      String IMAGE_PATH = "de/tucottbus/kt/lcarsx/wwj/layers/resources/CityLights.png";
      BufferedImage imgCli = loadImage(LCARS.class.getClassLoader().getResource(IMAGE_PATH));
      int w = imgCli.getWidth();
      int h = imgCli.getHeight();
      double JD = SunController.calcJulianDate(new Date());
      double[] ll = SunController.subsolarPoint(JD);
      BufferedImage imgAlpha = getAlpha(LatLon.fromRadians(ll[0],ll[1]),w,h);

      applyAlpha(imgCli.getRaster(),imgAlpha.getRaster());
      
      then = System.currentTimeMillis();
      File file = new File("d:/xfer/"+CityLightsLayer.class.getSimpleName()+".png");
      ImageIO.write(imgCli,"PNG",file);
      System.out.println("save: "+(System.currentTimeMillis()-then)+" ms");
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
  }


}
