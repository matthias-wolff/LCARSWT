package incubator.worldwind; /* gov.nasa.worldwind.layers */
/*
Copyright (C) 2001, 2006 United States Government
as represented by the Administrator of the
National Aeronautics and Space Administration.
All Rights Reserved.
*/
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.layers.BasicTiledImageLayer;
import gov.nasa.worldwind.util.LevelSet;
import gov.nasa.worldwind.util.Tile;
import gov.nasa.worldwind.util.TileUrlBuilder;

import java.net.MalformedURLException;
import java.net.URL;

public class PlanetLayer extends BasicTiledImageLayer
{
        private String planet_name_ = null;
        private String layer_label_ = null;

        public PlanetLayer( String wms_url, String planet_name, String data_cache_name, String layer_name, String full_layer_label )
        {
                super( makeLevels( wms_url, planet_name, data_cache_name, layer_name ) );

                this.setForceLevelZeroLoads(true);
                this.setRetainLevelZeroTiles(true);

                //setToolTipText( full_layer_label );

                planet_name_ = planet_name ;

                layer_label_ = data_cache_name ;
        }

        private static LevelSet makeLevels( String wms_url, String planet_name, String data_cache_name, String layer_name )
        {
                AVList params = new AVListImpl();

                params.setValue(AVKey.TILE_WIDTH, 512);
                params.setValue(AVKey.TILE_HEIGHT, 512);

                params.setValue(AVKey.DATA_CACHE_NAME, planet_name + "/" + data_cache_name.replaceAll(" ","_").replaceAll("/","_") );
                params.setValue(AVKey.SERVICE_NAME, "OGC:WMS");
                params.setValue(AVKey.VERSION, "1.1.1");
                params.setValue(AVKey.SERVICE, wms_url );
                params.setValue(AVKey.TILE_URL_BUILDER, new URLBuilder() );

                params.setValue(AVKey.DATASET_NAME, layer_name );

                params.setValue(AVKey.FORMAT_SUFFIX, ".dds");
                params.setValue(AVKey.NUM_LEVELS, 10 );
                params.setValue(AVKey.NUM_EMPTY_LEVELS, 0);
                params.setValue(AVKey.LEVEL_ZERO_TILE_DELTA, new LatLon(Angle.fromDegrees(36d), Angle.fromDegrees(36d)));
                params.setValue(AVKey.SECTOR, Sector.FULL_SPHERE);

                return new LevelSet(params);
        }

        private static class URLBuilder implements TileUrlBuilder
        {
                public URL getURL(Tile tile, String name ) throws MalformedURLException
                {
                        StringBuffer sb = new StringBuffer(tile.getLevel().getService());

                        if (sb.lastIndexOf("?") == -1 )
                                sb.append("?");
                        else if (sb.lastIndexOf("?") != sb.length() - 1)
                                sb.append("&");

                        sb.append("request=GetMap");
                        sb.append("&layers="); sb.append(tile.getLevel().getDataset());
                        sb.append("&srs=EPSG:4326");
                        //sb.append("&srs=IAU2000:49900");
                        sb.append("&width="); sb.append(tile.getLevel().getTileWidth());
                        sb.append("&height="); sb.append(tile.getLevel().getTileHeight());

                        Sector s = tile.getSector();

                        double south = s.getMinLatitude().getDegrees() ;
                        double north = s.getMaxLatitude().getDegrees() ;
                        double east = s.getMaxLongitude().getDegrees();
                        double west = s.getMinLongitude().getDegrees();

                        // Standard option
                        sb.append("&bbox="); sb.append( "" + west + "," + south + "," + east + "," + north );

                        sb.append("&format=image/jpeg");
                        //sb.append("&format=image/png");
                        sb.append("&service=WMS");
                        sb.append("&version=1.1.1");
                        sb.append("&styles=");
                        //sb.append("&bgcolor=0x000000");
                        //sb.append("&transparent=TRUE");

                        System.out.println("URL: " + sb);

                        return new URL(sb.toString());
                }
        }

        public String getPlanet()
        {
                return planet_name_ ;
        }

        @Override
        public String toString()
        {
                return layer_label_ ;
        }
}