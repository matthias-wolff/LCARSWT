package incubator.worldwind; /* gov.nasa.worldwindx.examples */

import gov.nasa.worldwind.BasicModel;
import gov.nasa.worldwind.Model;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.awt.WorldWindowGLCanvas;
import gov.nasa.worldwind.globes.Earth;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.layers.CompassLayer;
import gov.nasa.worldwind.layers.LatLonGraticuleLayer;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.layers.LayerList;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.layers.ScalebarLayer;
import gov.nasa.worldwind.layers.StarsLayer;
import gov.nasa.worldwind.layers.ViewControlsLayer;
import gov.nasa.worldwind.layers.ViewControlsSelectListener;
import gov.nasa.worldwind.layers.Earth.BMNGWMSLayer;
import gov.nasa.worldwind.layers.Earth.LandsatI3WMSLayer;
import gov.nasa.worldwind.ogc.wms.WMSCapabilities;
import gov.nasa.worldwind.ogc.wms.WMSLayerCapabilities;
import gov.nasa.worldwind.util.StatusBar;
import gov.nasa.worldwind.util.WWUtil;
import gov.nasa.worldwind.util.layertree.LayerTree;
import gov.nasa.worldwindx.examples.util.HotSpotController;

import java.awt.BorderLayout;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

/**
 * This class illustrates how to use multiple World Wind windows with a {@link JTabbedPane}.
 *
 * @version $Id: TabbedPaneUsage.java 1 2011-07-16 23:22:47Z dcollins $
 */
public class MultiPlanet extends JFrame
{
  private static final long serialVersionUID = 1L;

        private static class WWPanel extends JPanel
        {
          private static final long serialVersionUID = 1L;
                WorldWindowGLCanvas wwd;

                public WWPanel( int width, int height)
                {
                        // To share resources among World Windows, pass the first World Window to the constructor of the other
                        // World Windows.
                        this.wwd = new WorldWindowGLCanvas();
                        this.wwd.setSize(new java.awt.Dimension(width, height));

                        this.setLayout(new BorderLayout(5, 5));
                        this.add(this.wwd, BorderLayout.CENTER);
                        this.setOpaque(false);

                        StatusBar statusBar = new StatusBar();
                        statusBar.setEventSource(wwd);
                        this.add(statusBar, BorderLayout.SOUTH);
                }
        }

        public MultiPlanet()
        {
                try
                {
                        // Create the application frame and the tabbed pane and add the pane to the frame.
                        JTabbedPane tabbedPanel = new JTabbedPane();
                        this.add(tabbedPanel, BorderLayout.CENTER);

                        // Create World Windows
                        WWPanel earth_panel = new WWPanel( 600, 600);
                        WWPanel moon_panel = new WWPanel( 600, 600 );
                        WWPanel mars_panel = new WWPanel( 600, 600 );
                        WWPanel venus_panel = new WWPanel( 600, 600 );
                        WWPanel callisto_panel = new WWPanel( 600, 600 );
                        WWPanel europa_panel = new WWPanel( 600, 600 );
                        WWPanel ganymede_panel = new WWPanel( 600, 600 );
                        WWPanel io_panel = new WWPanel( 600, 600 );

                        tabbedPanel.add( earth_panel, "Earth" );
                        tabbedPanel.add( moon_panel, "Moon" );
                        tabbedPanel.add( mars_panel, "Mars" );
                        tabbedPanel.add( venus_panel, "Venus" );
                        tabbedPanel.add( callisto_panel, "Callisto" );
                        tabbedPanel.add( europa_panel, "Europa" );
                        tabbedPanel.add( ganymede_panel, "Ganymede" );
                        tabbedPanel.add( io_panel, "Io" );

                        // Common Layers
                        ArrayList<Layer> common_layers = new ArrayList<Layer>();
                        common_layers.add( new StarsLayer() );
                        common_layers.add( new LatLonGraticuleLayer() );
                        common_layers.add( new CompassLayer() );

                        // Layers for Earth
                        ArrayList<Layer> earth_layers = new ArrayList<Layer>();
                        earth_layers.add( new BMNGWMSLayer() );
                        earth_layers.add( new LandsatI3WMSLayer() );

                        // Layers for Moon
                        ArrayList<Layer> moon_layers = new ArrayList<Layer>();
                        load( moon_layers, "NASA", "http://OnMoon.jpl.nasa.gov/wms.cgi", "Moon" );

                        // Layers for Mars
                        ArrayList<Layer> mars_layers = new ArrayList<Layer>();

//                        load( mars_layers, "NASA", "http://OnMars.jpl.nasa.gov/wms.cgi", "Mars" );
            load( mars_layers, "USGS", "http://www.mapaplanet.org/explorer-bin/imageMaker.cgi?map=Mars", "Mars" ) ;
            load( mars_layers, "USGS2", "http://mars-wms.wr.usgs.gov/cgi-bin/mapserv?map=/var/www/html/mapfiles/mars/mars_simp_cyl.map", "Mars" ) ;
            load( mars_layers, "USGS3", "http://66.85.141.154/cgi-bin/mapserv?map=/var/www/html/mapfiles/mars/mars_simp_cyl_span540.map", "Mars" ) ;

                        // Layers for Venus
                        ArrayList<Layer> venus_layers = new ArrayList<Layer>();
                        load( venus_layers, "USGS", "http://www.mapaplanet.org/explorer-bin/imageMaker.cgi?map=Venus", "Venus" );

                        // Layers for Callisto
                        ArrayList<Layer> callisto_layers = new ArrayList<Layer>();
                        load( callisto_layers, "USGS", "http://www.mapaplanet.org/explorer-bin/imageMaker.cgi?map=Callisto", "Callisto" );

                        // Layers for Europa
                        ArrayList<Layer> europa_layers = new ArrayList<Layer>();
                        load( europa_layers, "USGS", "http://www.mapaplanet.org/explorer-bin/imageMaker.cgi?map=Europa", "Europa" );

                        // Layers for Ganymede
                        ArrayList<Layer> ganymede_layers = new ArrayList<Layer>();
                        load( ganymede_layers, "USGS", "http://www.mapaplanet.org/explorer-bin/imageMaker.cgi?map=Ganymede", "Ganymede" );

                        // Layers for Io
                        ArrayList<Layer> io_layers = new ArrayList<Layer>();
                        load( io_layers, "USGS", "http://www.mapaplanet.org/explorer-bin/imageMaker.cgi?map=Io", "Io" );

                        // Add Layer Panels
                        ArrayList<Layer> all_earth_layers = setLayerPanels( common_layers, earth_layers, earth_panel );
                        ArrayList<Layer> all_moon_layers = setLayerPanels( common_layers, moon_layers, moon_panel );
                        ArrayList<Layer> all_mars_layers = setLayerPanels( common_layers, mars_layers, mars_panel );
                        ArrayList<Layer> all_venus_layers = setLayerPanels( common_layers, venus_layers, venus_panel );
                        ArrayList<Layer> all_callisto_layers = setLayerPanels( common_layers, callisto_layers, callisto_panel );
                        ArrayList<Layer> all_europa_layers = setLayerPanels( common_layers, europa_layers, europa_panel );
                        ArrayList<Layer> all_ganymede_layers = setLayerPanels( common_layers, ganymede_layers, ganymede_panel );
                        ArrayList<Layer> all_io_layers = setLayerPanels( common_layers, io_layers, io_panel );

                        // Create models and pass them the layers.
                        setModel( new Earth(), all_earth_layers, earth_panel );
                        setModel( new Earth()/*new Moon()*/, all_moon_layers, moon_panel );
                        setModel( new Mars(), all_mars_layers, mars_panel );
                        setModel( new Earth()/*Venus()*/, all_venus_layers, venus_panel );
                        setModel( new Earth()/*Venus()*/, all_callisto_layers, callisto_panel );
                        setModel( new Earth()/*Europa()*/, all_europa_layers, europa_panel );
                        setModel( new Earth()/*Ganymede()*/, all_ganymede_layers, ganymede_panel );
                        setModel( new Earth()/*Io()*/, all_io_layers, io_panel );

                        // Add view control layers
                        setViewControlsLayer( earth_panel );
                        setViewControlsLayer( moon_panel );
                        setViewControlsLayer( mars_panel );
                        setViewControlsLayer( venus_panel );
                        setViewControlsLayer( callisto_panel );
                        setViewControlsLayer( europa_panel );
                        setViewControlsLayer( ganymede_panel );
                        setViewControlsLayer( io_panel );

                        // Add the card panel to the frame.
                        this.add(tabbedPanel, BorderLayout.CENTER);

                        // Position and display the frame.
                        this.setTitle("World Wind Multi Planet Demo");
                        this.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);
                        this.pack();
                        WWUtil.alignComponent(null, this, AVKey.CENTER); // Center the application on the screen.
                        this.setResizable(true);
                        this.setVisible(true);
                }
                catch (Exception e)
                {
                        e.printStackTrace();
                }
        }

        private ArrayList<Layer> setLayerPanels( ArrayList<Layer> common_layers, ArrayList<Layer> layers, WWPanel planet_panel )
        {
                LayerTree layerTree = new LayerTree();

                // Select only the first one
                boolean first = true ;
                for ( Layer layer : layers )
                {
                        layer.setEnabled( first );
                        first = false ;
                }

                ArrayList<Layer> all_layers = new ArrayList<Layer>();
                all_layers.addAll( common_layers );
                all_layers.addAll( layers );

                // Set up a layer to display the on-screen layer tree in the WorldWindow.
                RenderableLayer hiddenLayer = new RenderableLayer();
                hiddenLayer.addRenderable( layerTree );
                //planet_panel.wwd.getModel().getLayers().add( hiddenLayer );
                all_layers.add( hiddenLayer );

                // Mark the layer as hidden to prevent it being included in the layer tree's model. Including the layer in
                // the tree would enable the user to hide the layer tree display with no way of bringing it back.
                hiddenLayer.setValue(AVKey.HIDDEN, true);

                // Refresh the tree model with the WorldWindow's current layer list.
                layerTree.getModel().refresh( new LayerList( all_layers.toArray(new Layer[0]) ));

                // Add a controller to handle input events on the layer tree.
                @SuppressWarnings("unused")
                HotSpotController controller = new HotSpotController( planet_panel.wwd );

                return all_layers ;
        }

        private void setModel( Globe globe, ArrayList<Layer> layers, WWPanel planet_panel )
        {
                Model model = new BasicModel();
                model.setGlobe( globe );
                model.setLayers( new LayerList( layers.toArray(new Layer[0] )) );
                planet_panel.wwd.setModel( model );
        }

        private void setViewControlsLayer( WWPanel planet_panel )
        {
                ViewControlsLayer view_controls = new ViewControlsLayer();
                planet_panel.wwd.getModel().getLayers().add( view_controls );
                planet_panel.wwd.addSelectListener(new ViewControlsSelectListener( planet_panel.wwd, view_controls ));

                // add also a scale bar
                planet_panel.wwd.getModel().getLayers().add( new ScalebarLayer() );
        }

        protected void load( ArrayList<Layer> layers, String source, String url, String planet )
        {
                        WMSCapabilities caps;

                        try
                        {
                                caps = WMSCapabilities.retrieve( new URI( url ) );
                                caps.parse();
                        }
                        catch (Exception e)
                        {
                                e.printStackTrace();
                                return ;
                        }

                        ArrayList<PlanetLayer> planet_layers = new ArrayList<PlanetLayer>();

                        // Gather up all the named layers and make a world wind layer for each.
                        final List<WMSLayerCapabilities> namedLayerCaps = caps.getNamedLayers();

                        System.out.println("\n*LAYERS on "+planet);
                        if (namedLayerCaps != null)
                        {
                                for (WMSLayerCapabilities lc : namedLayerCaps)
                                {
                                        String name = lc.getName();
                                        String title = source + "--" + lc.getTitle() ;
                                        String description = lc.getLayerAbstract() ;
                                        System.out.println(name+"; "+title);
                                        planet_layers.add( new PlanetLayer( url, planet, title, name, description ) );
                                }
                        }
                        if ( planet_layers.isEmpty() == false )
                        {
                                layers.addAll( planet_layers );
                        }
        }

        public static void main(String[] args)
        {
                SwingUtilities.invokeLater(new Runnable()
                {
                        public void run()
                        {
                                new MultiPlanet();
                        }
                });
        }
}