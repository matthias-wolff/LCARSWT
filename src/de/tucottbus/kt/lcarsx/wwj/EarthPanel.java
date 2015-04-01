package de.tucottbus.kt.lcarsx.wwj;

import gov.nasa.worldwind.BasicModel;
import gov.nasa.worldwind.Model;
import gov.nasa.worldwind.event.PositionEvent;
import gov.nasa.worldwind.event.PositionListener;
import gov.nasa.worldwind.geom.Vec4;
import gov.nasa.worldwind.globes.Earth;
import gov.nasa.worldwind.layers.CompassLayer;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.layers.LayerList;
import gov.nasa.worldwind.layers.ScalebarLayer;
import gov.nasa.worldwind.layers.WorldMapLayer;
import gov.nasa.worldwind.layers.Earth.MSVirtualEarthLayer;
import gov.nasa.worldwind.layers.Earth.NASAWFSPlaceNameLayer;
import gov.nasa.worldwindx.sunlight.AtmosphereLayer;
import gov.nasa.worldwindx.sunlight.SunController;
import gov.nasa.worldwindx.sunlight.SunLayer;
import incubator.worldwind.CityLightsLayer;

import java.util.ArrayList;
import java.util.Date;

import de.tucottbus.kt.lcars.IScreen;
import de.tucottbus.kt.lcars.LCARS;
import de.tucottbus.kt.lcarsx.wwj.layers.CloudLayer;
import de.tucottbus.kt.lcarsx.wwj.layers.LayerSet;
import de.tucottbus.kt.lcarsx.wwj.layers.LcarsGraticuleLayer;
import de.tucottbus.kt.lcarsx.wwj.layers.LcarsPlaceNameLayer;
import de.tucottbus.kt.lcarsx.wwj.layers.LcarsScalebarLayer;
import de.tucottbus.kt.lcarsx.wwj.orbits.IssOrbit;
import de.tucottbus.kt.lcarsx.wwj.orbits.Orbit;
import de.tucottbus.kt.lcarsx.wwj.orbits.Orbit.ListItem;
import de.tucottbus.kt.lcarsx.wwj.orbits.StdEarthOrbit;
import de.tucottbus.kt.lcarsx.wwj.places.Place;
import de.tucottbus.kt.lcarsx.wwj.places.Poi;

/**
 * <p><i><b style="color:red">Experimental.</b></i></p>
 * 
 * The NASA World Wind panel for Earth.
 * 
 * @author Matthias Wolff, BTU Cottbus-Senftenberg
 */
public class EarthPanel extends WorldWindPanel
{
  private Model model;
  private ArrayList<LayerSet> layerSets;
  private Poi poi;

  private SunController sunController;
  private SunLayer sunLayer;
  //private CityLightsLayer cityLightsLayer;
  private CloudLayer cloudLayer;
  private AtmosphereLayer atmosphereLayer;
  
  private static final String POI_URI = "de/tucottbus/kt/lcarsx/wwj/places/poi.xml";
  
  public EarthPanel(IScreen screen)
  {
    super(screen);
  }

  @Override
  protected void fatInit()
  {
    poi = new Poi(getClass().getClassLoader().getResourceAsStream(POI_URI));
    super.fatInit();

    getEWorldWind().getWwd().addPositionListener(new PositionListener()
    {
      Vec4 eyePoint;

      public void moved(PositionEvent event)
      {
        if (eyePoint == null || eyePoint.distanceTo3(getEWorldWind().getView().getEyePoint()) > 1000)
        {
          sunController.update(new Date());
          eyePoint = getEWorldWind().getView().getEyePoint();
        }
      }
    });

  }

  @Override
  public Model getModel()
  {
    if (this.model==null)
    {
      this.model = new BasicModel();
      this.model.setGlobe(new Earth());
      
      // Remove standard layers
      this.model.getLayers().removeIf(l -> l instanceof ScalebarLayer);
      this.model.getLayers().removeIf(l -> l instanceof NASAWFSPlaceNameLayer);

      // Add layers
      this.model.getLayers().add(new LcarsGraticuleLayer());
      this.model.getLayers().add(new LcarsScalebarLayer());
      this.model.getLayers().add(new LcarsPlaceNameLayer());
      this.model.getLayers().add(new MSVirtualEarthLayer());

      // Adjust sun and atmosphere
      this.sunLayer = new SunLayer();
      //this.cityLightsLayer = new CityLightsLayer();
      this.cloudLayer = new CloudLayer();
      this.atmosphereLayer = new AtmosphereLayer();
      this.model.getLayers().add(this.sunLayer);
      //this.model.getLayers().add(this.cityLightsLayer);
      this.model.getLayers().add(cloudLayer);
      this.sunController = new SunController(this.model,this.sunLayer,
        this.atmosphereLayer,null/*this.cityLightsLayer*/,this.cloudLayer);
      
      // Group layers
      getLayerSets();
      
      // Protocol
      LCARS.log("WWJ","LAYERS:");
      for (Layer layer : this.model.getLayers())
        LCARS.log("WWJ","- "+layer.getClass().getSimpleName()+" "+
          layer.getName()+(layer.isEnabled()?" (on)":" (off)"));
    }
    return this.model;
  }

  @Override
  protected void fps1()
  {
    super.fps1();
    sunController.update(new Date());
  }
  
  @Override
  public ArrayList<LayerSet> getLayerSets()
  {
    if (this.layerSets==null)
    {
      this.layerSets = new ArrayList<LayerSet>();
      LayerList ll = this.model.getLayers();
      LayerSet ls;
  
      // World map and scale bar
      ls = new LayerSet("WORLD INFO");
      ls.addAll(ll.getLayersByClass(LcarsPlaceNameLayer.class));
      ls.addAll(ll.getLayersByClass(WorldMapLayer.class));
      ls.addAll(ll.getLayersByClass(LcarsScalebarLayer.class));
      this.layerSets.add(ls);
      
      // Sun, sun shade, atmospheric scattering and lens flares 
      ls = new LayerSet("SUN");
      ls.addAll(ll.getLayersByClass(SunLayer.class));
      ls.addAll(ll.getLayersByClass(CityLightsLayer.class));
      this.layerSets.add(ls);
  
      // Graticule and compass
      ls = new LayerSet("GRATICULE");
      ls.addAll(ll.getLayersByClass(LcarsGraticuleLayer.class));
      ls.addAll(ll.getLayersByClass(CompassLayer.class));
      for (Layer layer : ls) layer.setEnabled(false);
      this.layerSets.add(ls);
  
      // Clouds
      ls = new LayerSet("CLOUDS");
      ls.addAll(ll.getLayersByClass(CloudLayer.class));
      this.layerSets.add(ls);
      
      // City lights
//      ls = new LayerSet("CITY LIGHTS");
//      ls.addAll(ll.getLayersByClass(CityLightsLayer.class));
//      //ls.add(ll.getLayerByName("Earth at Night"));
//      for (Layer layer : ls) layer.setEnabled(false);
//      this.layerSets.add(ls);
      
    }
    return this.layerSets;
  }

  @Override
  public ArrayList<ListItem> getOrbitList()
  {
    ArrayList<ListItem> ol = new ArrayList<ListItem>();
    ol.add(new Orbit.ListItem(StdEarthOrbit.class,"STANDARD"));
    ol.add(new Orbit.ListItem(IssOrbit.class,"ISS"));
    return ol;
  }

  @Override
  public ArrayList<Place> getPoiList()
  {
    if (poi==null) return new ArrayList<Place>();
    return new ArrayList<Place>(poi.getPlacesOn(Place.ONEARTH));
  }

  // == MAIN METHOD ==

  /**
   * Runs the World Wind earth panel.
   * 
   * @param args
   *          The command line arguments, see {@link LCARS#main(String[])}.
   */
  public static void main(String[] args)
  {
    args = LCARS.setArg(args,"--panel=",EarthPanel.class.getCanonicalName());
    LCARS.main(args);
  }

}
