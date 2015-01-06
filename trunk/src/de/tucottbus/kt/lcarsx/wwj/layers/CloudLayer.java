package de.tucottbus.kt.lcarsx.wwj.layers;

import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.TexturedLayer;

import java.awt.Color;

public class CloudLayer extends RenderableLayer
{
  private static final String IMAGE_URL = "http://www.barnabu.co.uk/files/clouds/cloudimage.png";
  
  private final TexturedLayer image;
  
  public CloudLayer()
  {
    super();
    
    // This-->
    this.image = new TexturedLayer(IMAGE_URL, Sector.FULL_SPHERE);
    this.image.setElevation(10e3);
    this.image.invalidate();
    this.image.setLightColor(Color.WHITE);
    this.image.setAmbientColor(new Color(.1f,.1f,.1f));
    // <-- or that -->
    //image = new SurfaceImage(IMAGE_URL, Sector.FULL_SPHERE);
    // <--
    setName("Clouds");
    addRenderable(this.image);
    // Disable picking for the layer because it covers the full sphere and will override a terrain pick.
    setPickEnabled(false);
    
    // TODO: Reload cloud image every hour via this.image.setImageSource()
  }

  public void setLightDirection(Vec4 direction)
  {
      this.image.setLightDirection(direction);
  }

}
