package de.tucottbus.kt.lcarsx.wwj.layers;

import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.layers.placename.PlaceNameLayer;
import gov.nasa.worldwind.layers.placename.PlaceNameService;
import gov.nasa.worldwind.layers.placename.PlaceNameServiceSet;

import java.util.GregorianCalendar;

import de.tucottbus.kt.lcars.LCARS;

/**
 * <p><i><b style="color:red">Experimental.</b> FIXME: Should be derived 
 * from {@link gov.nasa.worldwind.layers.Earth.NASAWFSPlaceNameLayer 
 * NASAWFSPlaceNameLayer}, but I did not find a way to change the fonts...
 * </i></p>
 *
 * Remake of the {@link gov.nasa.worldwind.layers.Earth.NASAWFSPlaceNameLayer 
 * NASAWFSPlaceNameLayer} using LCARS fonts and colors.
 * 
 * @author Matthias Wolff, BTU Cottbus-Senftenberg
 */
public class LcarsPlaceNameLayer extends PlaceNameLayer
{
  public LcarsPlaceNameLayer()
  {
    super(makePlaceNameServiceSet());
  }
  
  private static PlaceNameServiceSet makePlaceNameServiceSet() 
  {
    final String service = "http://worldwind22.arc.nasa.gov/geoserver/wfs";
    final String fileCachePath = "Earth/PlaceNames/WFSPlaceNamesVersion1.0";
    PlaceNameServiceSet placeNameServiceSet = new PlaceNameServiceSet();
    placeNameServiceSet.setExpiryTime(new GregorianCalendar(2008, 1, 11).getTimeInMillis());
    PlaceNameService placeNameService;
    final boolean addVersionTag=true;  //true if pointing to a new wfs server
    // Oceans
    {
        placeNameService = new PlaceNameService(service, "topp:wpl_oceans", fileCachePath, Sector.FULL_SPHERE, GRID_1x1,
                LCARS.getFont(LCARS.EF_SMALL), addVersionTag);
        placeNameService.setColor(new java.awt.Color(200, 200, 200));
        placeNameService.setMinDisplayDistance(0d);
        placeNameService.setMaxDisplayDistance(LEVEL_A);
        placeNameServiceSet.addService(placeNameService, false);
    }
    // Continents
    {
        placeNameService = new PlaceNameService(service, "topp:wpl_continents", fileCachePath, Sector.FULL_SPHERE,
                GRID_1x1, LCARS.getFont(LCARS.EF_NORMAL), addVersionTag);
        placeNameService.setColor(new java.awt.Color(255, 255, 240));
        placeNameService.setMinDisplayDistance(LEVEL_G);
        placeNameService.setMaxDisplayDistance(LEVEL_A);
        placeNameServiceSet.addService(placeNameService, false);
    }

     // Water Bodies
    {
        placeNameService = new PlaceNameService(service, "topp:wpl_waterbodies", fileCachePath, Sector.FULL_SPHERE,
                GRID_4x8, LCARS.getFont(LCARS.EF_SMALL), addVersionTag);
        placeNameService.setColor(LCARS.getColor(LCARS.CS_MULTIDISP,LCARS.EC_ELBOUP));
        placeNameService.setMinDisplayDistance(0d);
        placeNameService.setMaxDisplayDistance(LEVEL_B);
        placeNameServiceSet.addService(placeNameService, false);
    }
    // Trenches & Ridges
    {
        placeNameService = new PlaceNameService(service, "topp:wpl_trenchesridges", fileCachePath, Sector.FULL_SPHERE,
                GRID_4x8, LCARS.getFont(LCARS.EF_SMALL), addVersionTag);
        placeNameService.setColor(LCARS.getColor(LCARS.CS_MULTIDISP,LCARS.EC_ELBOUP));
        placeNameService.setMinDisplayDistance(0d);
        placeNameService.setMaxDisplayDistance(LEVEL_B);
        placeNameServiceSet.addService(placeNameService, false);
    }
    // Deserts & Plains
    {
        placeNameService = new PlaceNameService(service, "topp:wpl_desertsplains", fileCachePath, Sector.FULL_SPHERE,
                GRID_4x8, LCARS.getFont(LCARS.EF_SMALL), addVersionTag);
        placeNameService.setColor(LCARS.getColor(LCARS.CS_MULTIDISP,LCARS.EC_ELBOLO));
        placeNameService.setMinDisplayDistance(0d);
        placeNameService.setMaxDisplayDistance(LEVEL_B);
        placeNameServiceSet.addService(placeNameService, false);
    }
    // Lakes & Rivers
    {
        placeNameService = new PlaceNameService(service, "topp:wpl_lakesrivers", fileCachePath, Sector.FULL_SPHERE,
                GRID_8x16, LCARS.getFont(LCARS.EF_SMALL), addVersionTag);
        placeNameService.setColor(LCARS.getColor(LCARS.CS_MULTIDISP,LCARS.EC_ELBOUP));
        placeNameService.setMinDisplayDistance(0d);
        placeNameService.setMaxDisplayDistance(LEVEL_C);
        placeNameServiceSet.addService(placeNameService, false);
    }
    // Mountains & Valleys
    {
        placeNameService = new PlaceNameService(service, "topp:wpl_mountainsvalleys", fileCachePath, Sector.FULL_SPHERE,
                GRID_8x16,LCARS.getFont(LCARS.EF_SMALL), addVersionTag);
        placeNameService.setColor(LCARS.getColor(LCARS.CS_MULTIDISP,LCARS.EC_PRIMARY|LCARS.ES_SELECTED));
        placeNameService.setMinDisplayDistance(0d);
        placeNameService.setMaxDisplayDistance(LEVEL_C);
        placeNameServiceSet.addService(placeNameService, false);
    }
    // Countries
    {
        placeNameService = new PlaceNameService(service, "topp:countries", fileCachePath, Sector.FULL_SPHERE, GRID_4x8,
            LCARS.getFont(LCARS.EF_SMALL), addVersionTag);
        placeNameService.setColor(java.awt.Color.white);
        placeNameService.setMinDisplayDistance(LEVEL_G);
        placeNameService.setMaxDisplayDistance(LEVEL_D);
        placeNameServiceSet.addService(placeNameService, false);
    }
    // GeoNet World Capitals
    {
        placeNameService = new PlaceNameService(service, "topp:wpl_geonet_p_pplc", fileCachePath, Sector.FULL_SPHERE,
                GRID_16x32,  LCARS.getFont(LCARS.EF_SMALL), addVersionTag);
        placeNameService.setColor(LCARS.getColor(LCARS.CS_MULTIDISP,LCARS.EC_SECONDARY|LCARS.ES_SELECTED));
        placeNameService.setMinDisplayDistance(0d);
        placeNameService.setMaxDisplayDistance(LEVEL_D);
        placeNameServiceSet.addService(placeNameService, false);
    }
    // World Cities >= 500k
    {
        placeNameService = new PlaceNameService(service, "topp:citiesover500k", fileCachePath, Sector.FULL_SPHERE,
                GRID_8x16, LCARS.getFont(LCARS.EF_SMALL), addVersionTag);
        placeNameService.setColor(LCARS.getColor(LCARS.CS_MULTIDISP,LCARS.EC_SECONDARY|LCARS.ES_SELECTED));
        placeNameService.setMinDisplayDistance(0);
        placeNameService.setMaxDisplayDistance(LEVEL_D);
        placeNameServiceSet.addService(placeNameService, false);
    }
    // World Cities >= 100k
    {
        placeNameService = new PlaceNameService(service, "topp:citiesover100k", fileCachePath, Sector.FULL_SPHERE,
                GRID_16x32,  LCARS.getFont(LCARS.EF_SMALL), addVersionTag);
        placeNameService.setColor(LCARS.getColor(LCARS.CS_MULTIDISP,LCARS.EC_SECONDARY|LCARS.ES_SELECTED));
        placeNameService.setMinDisplayDistance(LEVEL_N);
        placeNameService.setMaxDisplayDistance(LEVEL_F);
        placeNameServiceSet.addService(placeNameService, false);
    }
    // World Cities >= 50k and <100k
    {
        placeNameService = new PlaceNameService(service, "topp:citiesover50k", fileCachePath, Sector.FULL_SPHERE,
                GRID_16x32, LCARS.getFont(LCARS.EF_SMALL), addVersionTag);
        placeNameService.setColor(LCARS.getColor(LCARS.CS_MULTIDISP,LCARS.EC_SECONDARY|LCARS.ES_SELECTED));
        placeNameService.setMinDisplayDistance(LEVEL_N);
        placeNameService.setMaxDisplayDistance(LEVEL_H);
        placeNameServiceSet.addService(placeNameService, false);
    }

    // World Cities >= 10k and <50k
    {
        placeNameService = new PlaceNameService(service, "topp:citiesover10k", fileCachePath, Sector.FULL_SPHERE,
                GRID_36x72, LCARS.getFont(LCARS.EF_SMALL), addVersionTag);
        placeNameService.setColor(LCARS.getColor(LCARS.CS_MULTIDISP,LCARS.EC_SECONDARY|LCARS.ES_SELECTED));
        placeNameService.setMinDisplayDistance(0d);
        placeNameService.setMaxDisplayDistance(LEVEL_I);
        placeNameServiceSet.addService(placeNameService, false);
    }

    // World Cities >= 1k and <10k
    {
        placeNameService = new PlaceNameService(service, "topp:citiesover1k", fileCachePath, Sector.FULL_SPHERE,
                GRID_36x72, LCARS.getFont(LCARS.EF_SMALL), addVersionTag);
        placeNameService.setColor(LCARS.getColor(LCARS.CS_MULTIDISP,LCARS.EC_SECONDARY|LCARS.ES_SELECTED));
        placeNameService.setMinDisplayDistance(0d);
        placeNameService.setMaxDisplayDistance(LEVEL_K);
        placeNameServiceSet.addService(placeNameService, false);
    }
    // US Cities (Population Over 0)
    {
        //values for masking sector pulled from wfs capabilities request
        Sector maskingSector = new Sector(Angle.fromDegrees(18.0), Angle.fromDegrees(70.7), Angle.fromDegrees(-176.66), Angle.fromDegrees(-66.0));
        placeNameService = new PlaceNameService(service, "topp:wpl_uscitiesover0", fileCachePath, maskingSector,
                GRID_36x72, LCARS.getFont(LCARS.EF_SMALL), addVersionTag);
        placeNameService.setColor(LCARS.getColor(LCARS.CS_MULTIDISP,LCARS.EC_SECONDARY|LCARS.ES_SELECTED));
        placeNameService.setMinDisplayDistance(0d);
        placeNameService.setMaxDisplayDistance(LEVEL_N);
        placeNameServiceSet.addService(placeNameService, false);
    }
    // US Cities (No Population)
    {
        //values for masking sector pulled from wfs capabilities request
        Sector maskingSector = new Sector(Angle.fromDegrees(-14.4), Angle.fromDegrees(71.3), Angle.fromDegrees(-176.66), Angle.fromDegrees(178.88));
        placeNameService = new PlaceNameService(service, "topp:wpl_uscities0", fileCachePath, maskingSector,
                GRID_288x576, LCARS.getFont(LCARS.EF_SMALL), addVersionTag);
        placeNameService.setColor(LCARS.getColor(LCARS.CS_MULTIDISP,LCARS.EC_PRIMARY|LCARS.ES_SELECTED));
        placeNameService.setMinDisplayDistance(0d);
        placeNameService.setMaxDisplayDistance(LEVEL_N);//M);
        placeNameServiceSet.addService(placeNameService, false);
    }

    // US Anthropogenic Features
    {
        placeNameService = new PlaceNameService(service, "topp:wpl_us_anthropogenic", fileCachePath, Sector.FULL_SPHERE, GRID_288x576,
                LCARS.getFont(LCARS.EF_SMALL), addVersionTag);
        placeNameService.setColor(LCARS.getColor(LCARS.CS_MULTIDISP,LCARS.EC_PRIMARY|LCARS.ES_SELECTED));
        placeNameService.setMinDisplayDistance(0d);
        placeNameService.setMaxDisplayDistance(LEVEL_P);
        placeNameServiceSet.addService(placeNameService, false);
    }

    // US Water Features
    {
        placeNameService = new PlaceNameService(service, "topp:wpl_us_water", fileCachePath, Sector.FULL_SPHERE, GRID_144x288,
                LCARS.getFont(LCARS.EF_SMALL), addVersionTag);
        placeNameService.setColor(java.awt.Color.cyan);
        placeNameService.setMinDisplayDistance(0d);
        placeNameService.setMaxDisplayDistance(LEVEL_M);
        placeNameServiceSet.addService(placeNameService, false);
    }
   // US Terrain Features
    {
        placeNameService = new PlaceNameService(service, "topp:wpl_us_terrain", fileCachePath, Sector.FULL_SPHERE, GRID_72x144,
                LCARS.getFont(LCARS.EF_SMALL), addVersionTag);
        placeNameService.setColor(LCARS.getColor(LCARS.CS_MULTIDISP,LCARS.EC_PRIMARY|LCARS.ES_SELECTED));
        placeNameService.setMinDisplayDistance(0d);
        placeNameService.setMaxDisplayDistance(LEVEL_O);
        placeNameServiceSet.addService(placeNameService, false);
    }
    // GeoNET Administrative 1st Order
    {
        placeNameService = new PlaceNameService(service, "topp:wpl_geonet_a_adm1", fileCachePath, Sector.FULL_SPHERE, GRID_36x72,
                LCARS.getFont(LCARS.EF_SMALL), addVersionTag);
        placeNameService.setColor(LCARS.getColor(LCARS.CS_MULTIDISP,LCARS.EC_SECONDARY|LCARS.ES_SELECTED));
        placeNameService.setMinDisplayDistance(0d);
        placeNameService.setMaxDisplayDistance(LEVEL_N);
        placeNameServiceSet.addService(placeNameService, false);
    }
    // GeoNET Administrative 2nd Order
    {
        placeNameService = new PlaceNameService(service, "topp:wpl_geonet_a_adm2", fileCachePath, Sector.FULL_SPHERE, GRID_36x72,
                LCARS.getFont(LCARS.EF_SMALL), addVersionTag);
        placeNameService.setColor(LCARS.getColor(LCARS.CS_MULTIDISP,LCARS.EC_SECONDARY|LCARS.ES_SELECTED));
        placeNameService.setMinDisplayDistance(0d);
        placeNameService.setMaxDisplayDistance(LEVEL_N);
        placeNameServiceSet.addService(placeNameService, false);
    }
    // GeoNET Populated Place Administrative
    {
        placeNameService = new PlaceNameService(service, "topp:wpl_geonet_p_ppla", fileCachePath, Sector.FULL_SPHERE, GRID_36x72,
                LCARS.getFont(LCARS.EF_SMALL), addVersionTag);
        placeNameService.setColor(LCARS.getColor(LCARS.CS_MULTIDISP,LCARS.EC_ELBOUP));
        placeNameService.setMinDisplayDistance(0d);
        placeNameService.setMaxDisplayDistance(LEVEL_N);
        placeNameServiceSet.addService(placeNameService, false);
    }
    // GeoNET Populated Place
    {
        placeNameService = new PlaceNameService(service, "topp:wpl_geonet_p_ppl", fileCachePath, Sector.FULL_SPHERE, GRID_36x72,
                LCARS.getFont(LCARS.EF_SMALL), addVersionTag);
        placeNameService.setColor(LCARS.getColor(LCARS.CS_MULTIDISP,LCARS.EC_ELBOUP));
        placeNameService.setMinDisplayDistance(0d);
        placeNameService.setMaxDisplayDistance(LEVEL_O);
        placeNameServiceSet.addService(placeNameService, false);
    }

    return placeNameServiceSet;
  }

}
