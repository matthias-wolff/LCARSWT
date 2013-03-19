package de.tucottbus.kt.lcars.ge;

import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.AbstractList;
import java.util.Calendar;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.tucottbus.kt.lcars.LCARS;
import de.tucottbus.kt.lcars.MainPanel;
import de.tucottbus.kt.lcars.Screen;
import de.tucottbus.kt.lcars.contributors.EAlphaKeyboard;
import de.tucottbus.kt.lcars.contributors.EElementArray;
import de.tucottbus.kt.lcars.contributors.ElementContributor;
import de.tucottbus.kt.lcars.elements.EElbo;
import de.tucottbus.kt.lcars.elements.EElement;
import de.tucottbus.kt.lcars.elements.EEvent;
import de.tucottbus.kt.lcars.elements.EEventListenerAdapter;
import de.tucottbus.kt.lcars.elements.ERect;
import de.tucottbus.kt.lcars.elements.EValue;
import de.tucottbus.kt.lcars.ge.orbits.GEOrbit;
import de.tucottbus.kt.lcars.ge.orbits.N2yoOrbit;

public class GoogleEarthPanel extends MainPanel
{
  private EValue          eDate;
  private EValue          eTime;
  private EValue          eCaption;
  private GE              ge;
  private EArrayControls  eArrayControls;
  private EElementArray   eWorlds;
  private EElementArray   eOptions;
  private EElement        eOptClouds;
  private EElement        eOptSun;
  private EElement        eOptGrid;
  private ENavigation     eNavigation;
  private EAlphaKeyboard  eKeyboard;
  private EPlaceSearch    ePlaceSearch;
  private EPlaceNoMatch   ePlaceNoMatch;
  private EElementArray   ePlaces;
  private ERect           eOrbit;

  private int             mode;

  private final String    GE_PLACES = "de/tucottbus/kt/lcars/ge/places.xml";
  private final int       style     = LCARS.EC_SECONDARY | LCARS.ES_SELECTED;

  private Places          places;
  
  /**
   * Creates a new Google Earth panel.
   * 
   * @param screen
   *          The (local!) screen to run the panel on.
   */
  public GoogleEarthPanel(Screen screen)
  {
    super(screen);
  }

  @Override
  public void init()
  {
    super.init();
    setColorScheme(LCARS.CS_MULTIDISP);
    setTitle(null);
    places = new Places(getClass().getClassLoader().getResourceAsStream(GE_PLACES));
    
    // The top bar
    EElement e;
    add(new ERect(this,23,23,58,58,style|LCARS.ES_STATIC|LCARS.ES_RECT_RND_W,null));
    eDate = new EValue(this,85,23,288,58,style|LCARS.ES_LABEL_SE,"LCARS");
    eDate.setValueMargin(0);
    eDate.addEEventListener(new EEventListenerAdapter(){
      @Override
      public void touchDown(EEvent ee)
      {
        try { getScreen().setPanel(null); } catch (Exception e) {}
        (new Timer()).schedule(new TimerTask()
        {
          public void run()
          {
            thisPanel.invalidate();
          }
        },1000);
      }
    });
    add(eDate);
    
    eTime = new EValue(this,376,23,148,58,style|LCARS.ES_STATIC,"");
    eTime.setValueMargin(0);
    add(eTime);
    displayTime();
    
    eCaption = new EValue(this,527,23,1309,58,style|LCARS.ES_LABEL_SE|LCARS.ES_STATIC,"");
    eCaption.setValue("EARTH"); eCaption.setValueMargin(0);
    add(eCaption);

    add(new ERect(this,1839,23,58,58,style|LCARS.ES_STATIC|LCARS.ES_RECT_RND_E,null));
    
    // Google Earth wrapper and alpha keyboard 
    ge = new GE(this,52,85,1816,926);
    eKeyboard    = new EAlphaKeyboard(417,633,58,LCARS.EC_SECONDARY);
    ePlaceSearch = new EPlaceSearch(206,105);
    ePlaces = new EElementArray(417,198,ERect.class,new Dimension(470,52),7,3,LCARS.EC_SECONDARY|LCARS.ES_SELECTED|LCARS.ES_RECT_RND,null);
    ePlaces.setPageControls(ePlaceSearch.ePrev,ePlaceSearch.eNext);
    ePlaces.setLockControl(ePlaceSearch.eLock);
    ePlaces.addEEventListener(new EEventListenerAdapter()
    {
      @Override
      public void touchDown(EEvent ee)
      {
        String   caption = ee.el.getLabel();
        GECamera camera  = null;
        
        // Get camera and place name stored with widget
        if (ee.el.getData()!=null && ee.el.getData() instanceof GEPlace)
        {
          GEPlace place = (GEPlace)ee.el.getData();
          camera  = place.camera;
          caption = place.name; 
        }
        
        // No camera with widget: geocode widget's label
        if (camera==null)
        {
          AbstractList<GEPlace> places = Geocoder.geocode(ge.getWorld(),caption);
          if (places!=null && places.size()>0)
            camera = places.get(0).camera;
        }
        
        // Still no camera: display error
        if (camera==null)
        {
          // TODO: Display "Unknown place <caption>"
          return;
        }
        showSearchPanel(false);
        ge.flyTo(camera);
      }
    });
    ePlaceNoMatch = new EPlaceNoMatch(600,440);
    eKeyboard.addKeyListener(ePlaceSearch);
    
    // The bottom bar
    add(new ERect(this,23,1015,58,38,style|LCARS.ES_STATIC|LCARS.ES_RECT_RND_W,null));

    e = new ERect(this,84,1015,131,38,style|LCARS.ES_LABEL_E,"MODE SELECT");
    e.addEEventListener(new EEventListenerAdapter()
    {
      @Override
      public void touchDown(EEvent ee)
      {
        setMode(-1);
      }
    });
    add(e);

    eArrayControls = new EArrayControls(218,1015);
    eWorlds = new EElementArray(279,1015,ERect.class,new Dimension(242,38),1,5,style|LCARS.ES_LABEL_E,"");
    eWorlds.setPageControls(eArrayControls.getEPrev(),eArrayControls.getENext());
    eWorlds.setLockControl(eArrayControls.getELock());
    eWorlds.add(GE.EARTH);
    eWorlds.add(GE.MOON );
    eWorlds.add(GE.MARS );
    eWorlds.add(GE.SKY+" (SEEN FROM EARTH)");
    eWorlds.add("MILKY WAY GALAXY");
    eWorlds.add("ISS 1");
    eWorlds.add("ISS 2");
    eWorlds.addEEventListener(new EEventListenerAdapter()
    {
      @Override
      public void touchDown(EEvent ee)
      {
        eCaption.setValue(ee.el.getLabel());
        showSearchPanel(false);
        String world = ee.el.getLabel().split(" ")[0]; 
        if
        (
          world.equals(GE.EARTH) || world.equals(GE.MOON) ||
          world.equals(GE.MARS ) || world.equals(GE.SKY )
        )
        {
          setWorld(world);
        }
        else if ("ISS 1".equals(ee.el.getLabel()))
        {
          setWorld(GE.EARTH);
          GEOrbit orbit = new N2yoOrbit(N2yoOrbit.ID_ISS);
          orbit.setViewAngle(GEOrbit.AUTO,GEOrbit.AUTO,GEOrbit.AUTO);
          ge.setOrbit(orbit);
        }
        else if ("ISS 2".equals(ee.el.getLabel()))
        {
          setWorld(GE.EARTH);
          GEOrbit orbit = new N2yoOrbit(N2yoOrbit.ID_ISS);
          orbit.setViewAngle(GEOrbit.AUTO,0,0);
          ge.setOrbit(orbit);
        }
      }
    });
    eNavigation = new ENavigation(218,1015);
    eOptions = new EElementArray(279,1015,ERect.class,new Dimension(242,38),1,5,style|LCARS.ES_LABEL_E,"");
    eOptions.setPageControls(eArrayControls.getEPrev(),eArrayControls.getENext());
    eOptions.setLockControl(eArrayControls.getELock());
    eOptGrid = eOptions.add("GRID");
    eOptGrid.addEEventListener(new EEventListenerAdapter()
    {
      @Override
      public void touchDown(EEvent ee)
      {
        if (eOptGrid.getColorStyle()==LCARS.EC_PRIMARY)
          eOptGrid.setColorStyle(LCARS.EC_SECONDARY);
        else
          eOptGrid.setColorStyle(LCARS.EC_PRIMARY);
        ge.setGridVisible(eOptGrid.getColorStyle()==LCARS.EC_PRIMARY);
      }
    });
    eOptClouds = eOptions.add("CLOUDS");
    eOptClouds.addEEventListener(new EEventListenerAdapter()
    {
      @Override
      public void touchDown(EEvent ee)
      {
        if (eOptClouds.getColorStyle()==LCARS.EC_PRIMARY)
          eOptClouds.setColorStyle(LCARS.EC_SECONDARY);
        else
          eOptClouds.setColorStyle(LCARS.EC_PRIMARY);
        ge.setCloudsVisible(eOptClouds.getColorStyle()==LCARS.EC_PRIMARY);
      }
    });
    eOptSun = eOptions.add("SUN");
    eOptSun.addEEventListener(new EEventListenerAdapter()
    {
      @Override
      public void touchDown(EEvent ee)
      {
        if (eOptSun.getColorStyle()==LCARS.EC_PRIMARY)
          eOptSun.setColorStyle(LCARS.EC_SECONDARY);
        else
          eOptSun.setColorStyle(LCARS.EC_PRIMARY);
        ge.setSunVisible(eOptSun.getColorStyle()==LCARS.EC_PRIMARY);
      }
    });
    e = eOptions.add("SNAPSHOT");
    e.addEEventListener(new EEventListenerAdapter()
    {
      @Override
      public void touchDown(EEvent ee)
      {
        try
        {
          String NL = System.getProperty("line.separator");
          File temp = File.createTempFile("LCARS-GEPanel",".txt");
          temp.deleteOnExit();
          BufferedWriter out = new BufferedWriter(new FileWriter(temp));
          out.write("<place world=\""+ge.getWorld()+"\" name=\"\">"+NL);
          out.write("  <camera>" + ge.getActualCamera().toString() + "</camera>"+NL);
          out.write("</place>"+NL);
          out.close();
          Runtime.getRuntime().exec("notepad.exe "+temp.getAbsolutePath());
        }
        catch (IOException e)
        {
          e.printStackTrace();
        }
      }
    });
    e = eOptions.add("[DEBUG]");
    e.addEEventListener(new EEventListenerAdapter()
    {
      @Override
      public void touchDown(EEvent ee)
      {
        if (ge.isStarted()) ge.stop(); else ge.start();
        /*
        try
        {
          File temp = File.createTempFile("LCARS-GEPanel",".txt");
          temp.deleteOnExit();
          BufferedWriter out = new BufferedWriter(new FileWriter(temp));
          out.write(Geocoder.getKml());
          out.close();
          Runtime.getRuntime().exec("notepad.exe "+temp.getAbsolutePath());
        }
        catch (IOException e)
        {
          e.printStackTrace();
        }
        */
      }
    });

    eOrbit = new ERect(this,1626,1015,88,38,LCARS.EC_PRIMARY|LCARS.ES_LABEL_W,"ORBIT");
    eOrbit.addEEventListener(new EEventListenerAdapter()
    {
      @Override
      public void touchDown(EEvent ee)
      {
        ge.setOrbiting(!ge.isOrbiting());
      }
    });
    add(eOrbit);
    e = new ERect(this,1717,1015,119,38,style|LCARS.ES_LABEL_E,"SEARCH");
    e.addEEventListener(new EEventListenerAdapter()
    {
      @Override
      public void touchDown(EEvent ee)
      {
        showSearchPanel(!ePlaceSearch.isDisplayed());
      }
    });
    add(e);
    
    add(new ERect(this,1839,1015,58,38,style|LCARS.ES_STATIC|LCARS.ES_RECT_RND_E,null));
    
    // Initialize
    setMode(0);
    fillPlacesArray(null);
  }

  /*
   * (non-Javadoc)
   * @see de.tucottbus.kt.lcars.Panel#start()
   */
  @Override
  public void start()
  {
    super.start();
    ge.start();
    initControls(ge.getWorld());
  }

  /*
   * (non-Javadoc)
   * @see de.tucottbus.kt.lcars.Panel#stop()
   */
  @Override
  public void stop()
  {
    ge.stop();
    super.stop();
  }

  /**
   * Sets the lower bar's mode.
   * 
   * @param mode the mode
   */
  protected void setMode(int mode)
  {
    this.mode = mode<0?this.mode+1:mode;
    if (this.mode>2) this.mode=0;
    if (!ge.isStarted()) this.mode=0;
    
    eArrayControls.removeFromPanel();
    eWorlds.removeFromPanel();
    eOptions.removeFromPanel();
    eNavigation.removeFromPanel();
    
    if (this.mode==1)
    {
      eNavigation.addToPanel(this);
      return;
    }
    else if (this.mode==2)
    {
      eArrayControls.addToPanel(this);
      eOptions.addToPanel(this);
      return;
    }

    eArrayControls.addToPanel(this);
    eWorlds.addToPanel(this);
  }

  /**
   * Sets the title and fills the option array {@link #eOptions}.
   * 
   * @param world one of the {@link GECamera}<code>.XXXX</code> constants
   */
  protected void initControls(String world)
  {
    boolean clouds =  world.equals(GE.EARTH);
    boolean sun    = !world.equals(GE.SKY  );
    eOptClouds.setDisabled(!clouds);
    eOptClouds.setColorStyle(clouds?LCARS.EC_PRIMARY:LCARS.EC_SECONDARY);
    eOptSun.setDisabled(!sun);
    eOptSun.setColorStyle(sun?LCARS.EC_PRIMARY:LCARS.EC_SECONDARY);
    eOptGrid.setColorStyle(LCARS.EC_SECONDARY);
  }
  
  /**
   * Sets a new world (earth, moon, mars or sky).
   * 
   * @param world one of the {@link GECamera}<code>.XXXX</code> constants
   */
  protected void setWorld(String world)
  {
    initControls(world);
    if (world!=null && !world.equals(ge.getWorld())) ge.setWorld(world);
    fillPlacesArray(null);
  }

  /**
   * Displays the place search panel and hides the Google Earth panel and vice
   * versa.
   * 
   * @param show show search
   */
  protected void showSearchPanel(boolean show)
  {
    if (show)
    {
      eKeyboard.addToPanel(this);
      ePlaceSearch.addToPanel(this);
      ePlaces.addToPanel(this);
      ge.setVisible(false);
    }
    else
    {
      ge.setVisible(true);
      eKeyboard.removeFromPanel();
      ePlaceSearch.removeFromPanel();
      ePlaces.removeFromPanel();
      ePlaceNoMatch.removeFromPanel();
    }
  }
  
  /**
   * Fills the places array ({@link #ePlaces}) in the search panel. If
   * <code>address</code> is not <code>null</code> and not empty, the method
   * will geocode the address an display the result. 
   * 
   * @param address
   *          <code>null</code>: display standard places on the current world,
   *          "": clear places array, otherwise: geocode address an fill places
   *          array with the result
   */
  protected void fillPlacesArray(String address)
  {
    ePlaces.removeAll();
    if (address!=null && address.equals("")) return;
    AbstractList<GEPlace> places = null;
    if (address==null)
      places = this.places.getPlacesOn(ge.getWorld());
    else
      places = Geocoder.geocode(ge.getWorld(),address);    
    
    if (places==null || places.size()==0)
    {
      ePlaces.removeFromPanel();
      ePlaceNoMatch.addToPanel(this);
    }
    else
    {
      ePlaceNoMatch.removeFromPanel();
      if (eKeyboard.isDisplayed()) ePlaces.addToPanel(this);
      for (GEPlace place : places)
      {
        EElement el = ePlaces.add(place.name);
        if (place.name.length()>50)
          el.setLabel((place.name.substring(0,47)+"...").toUpperCase());
        el.setData(place);
      }
    }
  }
  
  /**
   * Displays the current date and time in the panel's headline.
   */
  protected void displayTime()
  {
    Calendar now = Calendar.getInstance();
    String yyyy = String.format("%04d",now.get(Calendar.YEAR        )  );
    String mm   = String.format("%02d",now.get(Calendar.MONTH       )+1);
    String dd   = String.format("%02d",now.get(Calendar.DAY_OF_MONTH)  );
    String hh   = String.format("%02d",now.get(Calendar.HOUR_OF_DAY )  );
    String ii   = String.format("%02d",now.get(Calendar.MINUTE      )  );
    eDate.setValue(yyyy+"-"+mm+"-"+dd);
    eTime.setValue(hh+":"+ii);
  }

  /*
   * (non-Javadoc)
   * @see de.tucottbus.kt.lcars.Panel#fps2()
   */
  @Override
  protected void fps10()
  {
    String caption = ge.getTargetCamera().name;
    if (ePlaceSearch.isDisplayed())
      caption = "PLACE SEARCH - "+ge.getWorld();
    eCaption.setValue(caption.toUpperCase());

    displayTime();
    eOrbit.setDisabled(ge.getOrbit()==null);
    eOrbit.setBlinking(ge.isOrbiting());
    eNavigation.displayCamera();
  }

  // -- Local UI classes --
  
  class EArrayControls extends ElementContributor
  {
    private ERect ePrev;
    private ERect eNext;
    private ERect eLock;
    
    public EArrayControls(int x, int y)
    {
      super(x,y);
      ePrev = new ERect(null,   0,0,58,38,style|LCARS.ES_LABEL_W,"PREV");
      eNext = new ERect(null,1286,0,58,38,style|LCARS.ES_LABEL_W,"NEXT");
      eLock = new ERect(null,1347,0,58,38,style|LCARS.ES_LABEL_W,"LOCK");
      add(ePrev);
      add(eNext);
      add(eLock);
    }
    
    public EElement getEPrev()
    {
      return this.ePrev;
    }
    
    public EElement getENext()
    {
      return this.eNext;
    }
    
    public EElement getELock()
    {
      return this.eLock;
    }
  }
  
  class ENavigation extends ElementContributor
  {
    private final int ST_E = LCARS.EC_PRIMARY|LCARS.ES_LABEL_E;
    private final int ST_C = LCARS.EC_PRIMARY|LCARS.ES_LABEL_C;
    private final int ST_T = LCARS.EC_PRIMARY|LCARS.ES_SELECTED|LCARS.ES_LABEL_E|LCARS.EF_TINY|LCARS.ES_STATIC;
    private EValue eLat;
    private EValue eLon;
    private EValue eAlt;
    private EValue eTlt;
    private EValue eHdg;
    private EValue eSpeed;
    private EValue eLatT;
    private EValue eLonT;
    private EValue eAltT;
    private EValue eTltT;
    private EValue eHdgT;
    
    public ENavigation(int x, int y)
    {
      super(x,y);
      EElement e;
      
      // Speed display
      eSpeed = new EValue(null,0,41,79,18,ST_T|LCARS.ES_RECT_RND_W,"SPEED");
      eSpeed.setValue("0.00"); eSpeed.setValueMargin(0);
      add(eSpeed);
      
      // Latitude control
      eLat = new EValue(null,0,0,198,38,ST_E|LCARS.ES_STATIC,"LAT/°");
      eLat.setValue("00.0N"); eLat.setValueMargin(0);
      add(eLat);
      e = new ERect(null,201,0,38,38,ST_C,"\u2013");
      e.addEEventListener(new EEventListenerAdapter()
      {
        @Override
        public void touchDown(EEvent ee)
        {
          eLat.setSelected(true);
          touchHold(ee);
        }
        @Override
        public void touchHold(EEvent ee)
        {
          if (ee.ct>0 && ee.ct<=5) return;
          GECamera camera = ge.getTargetCamera();
          camera.incLatitude(-1);
          ge.flyTo(camera,GE.SPEED_AUTO);
          displayCamera();
        }
        @Override
        public void touchUp(EEvent ee)
        {
          eLat.setSelected(false);
        }
      });
      add(e);
      e = new ERect(null,242,0,38,38,ST_E,"+");
      e.addEEventListener(new EEventListenerAdapter()
      {
        @Override
        public void touchDown(EEvent ee)
        {
          eLat.setSelected(true);
          touchHold(ee);
        }
        @Override
        public void touchHold(EEvent ee)
        {
          if (ee.ct>0 && ee.ct<=5) return;
          GECamera camera = ge.getTargetCamera();
          camera.incLatitude(1);
          ge.flyTo(camera,GE.SPEED_AUTO);
          displayCamera();
        }
        @Override
        public void touchUp(EEvent ee)
        {
          eLat.setSelected(false);
        }
      });
      add(e);
      eLatT = new EValue(null,79,41,119,18,ST_T,"TARGET");
      eLatT.setValue("00.0N"); eLatT.setValueMargin(0);
      add(eLatT);
      add(new ERect(null,201,41,79,18,ST_T|LCARS.ES_RECT_RND_E,null));

      // Longitude control
      eLon = new EValue(null,283,0,198,38,ST_E|LCARS.ES_STATIC,"LON/°");
      eLon.setValue("00.0E"); eLon.setValueMargin(0);
      add(eLon);
      e = new ERect(null,481,0,38,38,ST_C,"\u2013");
      e.addEEventListener(new EEventListenerAdapter()
      {
        @Override
        public void touchDown(EEvent ee)
        {
          eLon.setSelected(true);
          touchHold(ee);
        }
        @Override
        public void touchHold(EEvent ee)
        {
          if (ee.ct>0 && ee.ct<=5) return;
          GECamera camera = ge.getTargetCamera();
          camera.incLongitude(-1);
          ge.flyTo(camera,GE.SPEED_AUTO);
          displayCamera();
        }
        @Override
        public void touchUp(EEvent ee)
        {
          eLon.setSelected(false);
        }
      });
      add(e);
      e = new ERect(null,522,0,38,38,ST_E,"+");
      e.addEEventListener(new EEventListenerAdapter()
      {
        @Override
        public void touchDown(EEvent ee)
        {
          eLon.setSelected(true);
          touchHold(ee);
        }
        @Override
        public void touchHold(EEvent ee)
        {
          if (ee.ct>0 && ee.ct<=5) return;
          GECamera camera = ge.getTargetCamera();
          camera.incLongitude(1);
          ge.flyTo(camera,GE.SPEED_AUTO);
          displayCamera();
        }
        @Override
        public void touchUp(EEvent ee)
        {
          eLon.setSelected(false);
        }
      });
      add(e);
      eLonT = new EValue(null,283,41,198,18,ST_T|LCARS.ES_RECT_RND_W,null);
      eLonT.setValue("00.0E"); eLonT.setValueMargin(0);
      add(eLonT);
      add(new ERect(null,481,41,79,18,ST_T|LCARS.ES_RECT_RND_E,null));

      // Altitude control
      eAlt = new EValue(null,563,0,203,38,ST_E|LCARS.ES_STATIC,"ALT/km");
      eAlt.setValue("0.000"); eAlt.setValueMargin(0);
      add(eAlt);
      e = new ERect(null,766,0,38,38,ST_C,"\u2013");
      e.setDisabled(true);
      add(e);
      e = new ERect(null,807,0,38,38,ST_E,"+");
      e.setDisabled(true);
      add(e);
      eAltT = new EValue(null,563,41,203,18,ST_T|LCARS.ES_RECT_RND_W,null);
      eAltT.setValue("0.000"); eAltT.setValueMargin(0);
      add(eAltT);
      add(new ERect(null,766,41,79,18,ST_T|LCARS.ES_RECT_RND_E,null));

      // Tilt control
      eTlt = new EValue(null,848,0,198,38,ST_E|LCARS.ES_STATIC,"TLT/°");
      eTlt.setValue("00.0"); eTlt.setValueMargin(0);
      add(eTlt);
      e = new ERect(null,1046,0,38,38,ST_C,"\u2013");
      e.setDisabled(true);
      add(e);
      e = new ERect(null,1087,0,38,38,ST_E,"+");
      e.setDisabled(true);
      add(e);
      eTltT = new EValue(null,848,41,198,18,ST_T|LCARS.ES_RECT_RND_W,null);
      eTltT.setValue("00.0"); eTltT.setValueMargin(0);
      add(eTltT);
      add(new ERect(null,1046,41,79,18,ST_T|LCARS.ES_RECT_RND_E,null));

      // Heading control
      eHdg = new EValue(null,1128,0,198,38,ST_E|LCARS.ES_STATIC,"HDG/°");
      eHdg.setValue("000.00"); eHdg.setValueMargin(0);
      add(eHdg);
      e = new ERect(null,1326,0,38,38,ST_C,"\u2013");
      e.setDisabled(true);
      add(e);
      e = new ERect(null,1367,0,38,38,ST_E,"+");
      e.setDisabled(true);
      add(e);
      eHdgT = new EValue(null,1128,41,198,18,ST_T|LCARS.ES_RECT_RND_W,null);
      eHdgT.setValue("000.00"); eHdgT.setValueMargin(0);
      add(eHdgT);
      add(new ERect(null,1326,41,79,18,ST_T|LCARS.ES_RECT_RND_E,null));
    }
    
    /**
     * Displays the current camera position at the navigation contributor.
     */
    public void displayCamera()
    {
      if (ge.getActualCamera()==null) return;

      String s;
      float  v;
      boolean sky = ge.getWorld().startsWith("SKY");

      // Display actual camera position
      v = eLat.isSelected()?ge.getTargetCamera().latitude:ge.getActualCamera().latitude;
      s = String.format(Locale.US,"%05.2f",Math.abs(v))+(v<0?"S":"N");
      eLat.setLabel(sky?"DEC/°":"LAT/°");
      eLat.setValue(s);

      v = eLon.isSelected()?ge.getTargetCamera().longitude:ge.getActualCamera().longitude;
      if (sky)
      {
        eLon.setLabel("RA/h");
        v = (v+180)/15;
        s = String.format(Locale.US,"%06.2f",Math.abs(v));
      }
      else
      {
        eLon.setLabel("LON/°");
        s = String.format(Locale.US,"%06.2f",Math.abs(v))+(v<0?"W":"E");
      }
      eLon.setValue(s);
      
      v = ge.getActualCamera().altitude/1000;
      eAlt.setDisabled(sky);
      if      (v<1E3) s = String.format(Locale.US,"%06.2f" ,v    );
      else if (v<1E6) s = String.format(Locale.US,"%06.2fT",v/1E3);
      else if (v<1E9) s = String.format(Locale.US,"%06.2fM",v/1E6);
      else            s = String.format(Locale.US,"%06.2fB",v/1E9);
      eAlt.setValue(sky?"NA":s);

      v = ge.getActualCamera().tilt;
      s = String.format(Locale.US,"%s%05.2f",v<0?"-":"",Math.abs(v));
      eTlt.setValue(s);

      v = ge.getActualCamera().heading;
      s = String.format(Locale.US,"%s%06.2f",v<0?"-":"",Math.abs(v));
      eHdg.setValue(s);
      
      // Display target camera position
      v = ge.getCameraSpeed();
      eSpeed.setValue(String.format(Locale.US,"%04.2f",v));
      
      v = ge.getTargetCamera().latitude;
      s = String.format(Locale.US,"%05.2f",Math.abs(v))+(v<0?"S":"N");
      eLatT.setValue(s);

      v = ge.getTargetCamera().longitude;
      if (sky)
      {
        v = (v+180)/15;
        s = String.format(Locale.US,"%06.2f",Math.abs(v));
      }
      else
        s = String.format(Locale.US,"%06.2f",Math.abs(v))+(v<0?"W":"E");
      eLonT.setValue(s);
      
      v = ge.getTargetCamera().altitude/1000;
      eAlt.setDisabled(sky);
      if      (v<1E3) s = String.format(Locale.US,"%06.2f" ,v    );
      else if (v<1E6) s = String.format(Locale.US,"%06.2fT",v/1E3);
      else if (v<1E9) s = String.format(Locale.US,"%06.2fM",v/1E6);
      else            s = String.format(Locale.US,"%06.2fB",v/1E9);
      eAltT.setValue(sky?"NA":s);

      v = ge.getTargetCamera().tilt;
      s = String.format(Locale.US,"%s%05.2f",v<0?"-":"",Math.abs(v));
      eTltT.setValue(s);

      v = ge.getTargetCamera().heading;
      s = String.format(Locale.US,"%s%06.2f",v<0?"-":"",Math.abs(v));
      eHdgT.setValue(s);
    }

  }

  class EPlaceSearch extends ElementContributor implements KeyListener
  {
    ERect  ePrev;
    ERect  eNext;
    ERect  eLock;
    EValue eQuery;
    
    public EPlaceSearch(int x, int y)
    {
      super(x,y);
      
      EElement e;
      e = new EElbo(null,0,4,321,152,LCARS.EC_ELBOUP|LCARS.ES_STATIC|LCARS.ES_LABEL_NE,"PLACE");
      ((EElbo)e).setArcWidths(194,72); ((EElbo)e).setArmWidths(192,71);
      add(e);
      eQuery = new EValue(null,324,16,937,47,LCARS.EC_ELBOUP|LCARS.ES_SELECTED|LCARS.ES_STATIC|LCARS.ES_LABEL_E,null);
      eQuery.setValue("ENTER QUERY");
      eQuery.setBlinking(true);
      eQuery.setValueMargin(0); eQuery.setValueWidth(937);
      add(eQuery);
      e = new EElbo(null,324,4,1047,34,LCARS.EC_ELBOUP|LCARS.ES_SELECTED|LCARS.ES_STATIC|LCARS.ES_SHAPE_NE,null);
      ((EElbo)e).setArcWidths(1,34); ((EElbo)e).setArmWidths(100,9);
      add(e);
      e = new EElbo(null,324,41,1047,34,LCARS.EC_ELBOUP|LCARS.ES_SELECTED|LCARS.ES_STATIC|LCARS.ES_SHAPE_SE,null);
      ((EElbo)e).setArcWidths(1,34); ((EElbo)e).setArmWidths(100,9);
      add(e);
      e = new ERect(null,1374,4,258,71,LCARS.EC_PRIMARY|LCARS.ES_SELECTED|LCARS.ES_LABEL_NW,"GO");
      e.addEEventListener(new EEventListenerAdapter()
      {
        @Override
        public void touchDown(EEvent ee)
        {
          Ok();
        }
      });
      add(e);

      add(new ERect(null,0,159,192,187,LCARS.EC_SECONDARY|LCARS.ES_STATIC,null));
      ePrev = new ERect(null,0,349,192,68,LCARS.EC_PRIMARY|LCARS.ES_LABEL_SE,"PREV");
      add(ePrev);
      eNext = new ERect(null,0,420,192,68,LCARS.EC_PRIMARY|LCARS.ES_LABEL_SE,"NEXT");
      add(eNext);
      eLock = new ERect(null,0,491,192,68,LCARS.EC_PRIMARY|LCARS.ES_LABEL_SE,"LOCK");
      add(eLock);
      add(new ERect(null,0,562,192,109,LCARS.EC_SECONDARY|LCARS.ES_STATIC,null));
      e = new EElbo(null,0,674,426,156,LCARS.EC_ELBOUP|LCARS.ES_STATIC|LCARS.ES_SHAPE_SW,null);
      ((EElbo)e).setArcWidths(194,72); ((EElbo)e).setArmWidths(192,58);
      add(e);
    }

    public void keyPressed(KeyEvent e)
    {
      if (e.getKeyCode()==KeyEvent.VK_CANCEL)
      {
        showSearchPanel(false);
        return;
      }
      /*else if (e.getKeyCode()==KeyEvent.VK_CLEAR)
      {
        eQuery.setValue("ENTER QUERY",false);
        eQuery.setColor(new Color(0x666666,false),true);
        placeSearch(null / *Show known places* /);
        return;
      }*/
    }

    public void keyReleased(KeyEvent e)
    {
    }

    public void keyTyped(KeyEvent e)
    {
      String q = eQuery.isBlinking()?"":eQuery.getValue();
      char   c = e.getKeyChar(); 
      if (c==KeyEvent.VK_TAB) return;
      if (c==KeyEvent.VK_CLEAR)
      {
        q=" ";
        c=KeyEvent.VK_BACK_SPACE;
      }
      if (c==KeyEvent.VK_BACK_SPACE || c==KeyEvent.VK_DELETE)
      {
        if (q.length()==0) return;
        q = q.substring(0,q.length()-1);
        if (q.length()>0)
          eQuery.setValue(q);
        else
        {
          eQuery.setValue("ENTER QUERY");
          eQuery.setColorStyle(LCARS.EC_ELBOUP);
          eQuery.setBlinking(true);
          fillPlacesArray(null /*Show known places*/);
        }
        return;
      }
      if (c==KeyEvent.VK_ENTER || c==KeyEvent.VK_ACCEPT)
      {
        Ok();
        return;
      }
      q+=e.getKeyChar();
      eQuery.setBlinking(false);      
      eQuery.setColorStyle(LCARS.EC_PRIMARY);
      eQuery.setValue(q.toUpperCase());
      fillPlacesArray("" /*Clear places array*/);
    }
    
    protected void Ok()
    {
      String q = eQuery.isBlinking()?"":eQuery.getValue();
      fillPlacesArray(q);      
    }
  }
  
  class EPlaceNoMatch extends ElementContributor
  {
    private EValue eNoMatch;

    public EPlaceNoMatch(int x, int y)
    {
      super(x,y);
      int style = LCARS.EC_PRIMARY|LCARS.EF_HEAD1|LCARS.ES_STATIC|LCARS.ES_BLINKING;
      eNoMatch = new EValue(null,0,0,500,100,style,"");
      eNoMatch.setValue("NO MATCH");
      eNoMatch.setValueMargin(0); eNoMatch.setValueWidth(500);
      add(eNoMatch);
    }
  }
  
  // -- Places class --
  
  /**
   * A list of places on different worlds. 
   */
  class Places
  {
    Vector<GEPlace> places;
    
    /**
     * Creates a new list of places from an XML input stream.
     * 
     * @param is
     *          The XML input stream.
     */
    Places(InputStream is)
    {
      places = new Vector<GEPlace>();
      
      try
      {
        // Load XML file
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(is);
        doc.getDocumentElement().normalize();
        
        // Loop over <place>-tags
        NodeList nodeLst = doc.getElementsByTagName("place");
        for (int i=0; i<nodeLst.getLength(); i++)
        {
          Node node = nodeLst.item(i);
          if (node.getNodeType()!=Node.ELEMENT_NODE) continue;

          // Get name and world
          String name  = node.getAttributes().getNamedItem("name" ).getNodeValue();
          String world = node.getAttributes().getNamedItem("world").getNodeValue();

          // Get camera (if present)
          String camera = null;
          NodeList camL = ((Element)node).getElementsByTagName("camera");
          if (camL.getLength()>0)
          {
            Node camN = camL.item(0);
            if (camN.getNodeType()==Node.ELEMENT_NODE && camN.getChildNodes().getLength()>0)
            {
              camN = camN.getChildNodes().item(0);
              if (camN.getNodeType()==Node.TEXT_NODE)
                camera = camN.getNodeValue();
            }
          }

          // Get grammar (if present)
          String grammar = null;
          NodeList grmL = ((Element)node).getElementsByTagName("grammar");
          if (grmL.getLength()>0)
          {
            Node grmN = grmL.item(0);
            if (grmN.getNodeType()==Node.ELEMENT_NODE && grmN.getChildNodes().getLength()>0)
            {
              grmN = grmN.getChildNodes().item(0);
              if (grmN.getNodeType()==Node.TEXT_NODE)
                grammar = grmN.getNodeValue();
            }
          }
          
          // Create a new place
          places.add(new GEPlace(name,world,camera,grammar));
        }
      }
      catch (Exception e)
      {
        e.printStackTrace();
      }
    }
    
    /**
     * Returns the list of places on one world.
     * 
     * @param world the world
     * @return the list of places
     */
    public AbstractList<GEPlace> getPlacesOn(String world)
    {
      Vector<GEPlace> places = new Vector<GEPlace>();
      for (GEPlace place : this.places)
        if (place.world.equals(world))
          places.add(place);
      return places;
    }
  }

  // -- Main method --
  
  /**
   * Runs the Google Earth panel.
   * 
   * @param args
   *          The command line arguments, see {@link LCARS#main(String[])}.
   */
  public static void main(String[] args)
  {
    args = LCARS.setArg(args,"--panel=",GoogleEarthPanel.class.getCanonicalName());
    LCARS.main(args);
  }

}