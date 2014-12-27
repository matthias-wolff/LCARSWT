package de.tucottbus.kt.lcarsx.wwj;

import gov.nasa.worldwind.Model;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.LayerList;

import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import de.tucottbus.kt.lcars.IScreen;
import de.tucottbus.kt.lcars.LCARS;
import de.tucottbus.kt.lcars.MainPanel;
import de.tucottbus.kt.lcars.contributors.EAlphaKeyboard;
import de.tucottbus.kt.lcars.contributors.EElementArray;
import de.tucottbus.kt.lcars.contributors.ElementContributor;
import de.tucottbus.kt.lcars.elements.EElbo;
import de.tucottbus.kt.lcars.elements.EElement;
import de.tucottbus.kt.lcars.elements.EEvent;
import de.tucottbus.kt.lcars.elements.EEventListenerAdapter;
import de.tucottbus.kt.lcars.elements.ELabel;
import de.tucottbus.kt.lcars.elements.ERect;
import de.tucottbus.kt.lcars.elements.EValue;
import de.tucottbus.kt.lcarsx.wwj.layers.LayerSet;
import de.tucottbus.kt.lcarsx.wwj.orbits.Orbit;
import de.tucottbus.kt.lcarsx.wwj.places.Camera;
import de.tucottbus.kt.lcarsx.wwj.places.LcarsGazetteer;
import de.tucottbus.kt.lcarsx.wwj.places.Place;

/**
 * <p><i><b style="color:red">Experimental API.</b></i></p>
 * 
 * The NASA World Wind panel.
 * 
 * @author Matthias Wolff, BTU Cottbus-Senftenberg
 */
public abstract class WorldWindPanel extends MainPanel
{
  // -- Constants --
  // Main modes
  protected static final int MODE_INIT      = 0;
  protected static final int MODE_WORLDWIND = 1;
  protected static final int MODE_PLACES    = 2;
  
  // Bottom bar modes
  protected static final int NUM_BARMODES   = 3;
  protected static final int BARMODE_TOGGLE = -1;
  protected static final int BARMODE_NAVI   = 0;
  protected static final int BARMODE_ORBITS = 1;
  protected static final int BARMODE_VIEW   = 2;
  
  // -- Private fields --
  private   EValue          eDate;
  private   EValue          eTime;
  private   EValue          eCaption;
  protected EWorldWind      eWw;
  private   ELabel          eInsteadOfWwHead;
  private   ELabel          eInsteadOfWwText;
  private   ERect           eModeSel;
  private   EArrayControls  eArrayControls;
  private   EElementArray   eOrbitArray;
  private   EElementArray   eViewArray;
  private   ENavigation     eNavi;
  private   EAlphaKeyboard  eKeyboard;
  private   EPlaceSearch    ePlaceSearch;
  private   EPlaceNoMatch   ePlaceNoMatch;
  private   EElementArray   ePlaceArray;
  private   ERect           eOrbit;
  private   ERect           ePlaces;
  
  private final int style = LCARS.EC_SECONDARY | LCARS.ES_SELECTED;
  
  // -- LCARS API --
  
  /**
   * <p><i><b style="color:red">Experimental API.</b></i></p>
   * 
   * Creates a new World Wind panel.
   * 
   * @param screen
   *          The (local!) screen to run the panel on.
   */
  public WorldWindPanel(IScreen screen)
  {
    super(screen);
  }

  @Override
  public void init()
  {
    super.init();
    setTitleLabel(null);
    setTitle("EARTH");
    setColorScheme(LCARS.CS_MULTIDISP);
    
    // The top bar
    add(new ERect(this,23,23,58,58,style|LCARS.ES_STATIC|LCARS.ES_RECT_RND_W,null));
    eDate = new EValue(this,85,23,288,58,style|LCARS.ES_LABEL_SE,"LCARS");
    eDate.setValueMargin(0);
    eDate.addEEventListener(new EEventListenerAdapter()
    {
      @Override
      public void touchDown(EEvent ee)
      {
        try
        {
          getScreen().setPanel(null);
        } catch (ClassNotFoundException | RemoteException e)
        {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }
    });
    add(eDate);
    
    eTime = new EValue(this,376,23,148,58,style|LCARS.ES_STATIC,"");
    eTime.setValueMargin(0);
    add(eTime);
    displayTime();
    
    eCaption = new EValue(this,527,23,1309,58,style|LCARS.ES_LABEL_SE|LCARS.ES_STATIC,"");
    eCaption.setValue(getTitle()); eCaption.setValueMargin(0);
    add(eCaption);

    add(new ERect(this,1839,23,58,58,style|LCARS.ES_STATIC|LCARS.ES_RECT_RND_E,null));
    
    // Alpha keyboard and place search contributors 
    eKeyboard    = new EAlphaKeyboard(417,633,58,LCARS.EC_SECONDARY);
    ePlaceSearch = new EPlaceSearch(206,105);
    ePlaceArray = new EElementArray(417,198,ERect.class,new Dimension(470,52),7,3,LCARS.EC_SECONDARY|LCARS.ES_SELECTED|LCARS.ES_RECT_RND,null);
    ePlaceArray.setPageControls(ePlaceSearch.ePrev,ePlaceSearch.eNext);
    ePlaceArray.setLockControl(ePlaceSearch.eLock);
    ePlaceArray.addEEventListener(new EEventListenerAdapter()
    {
      @Override
      public void touchDown(EEvent ee)
      {
        setMainMode(MODE_WORLDWIND);

        invokeLater(new Runnable()
        {
          @Override
          public void run()
          {
            Place  place;
            Camera camera;
                
            // Get camera and place name stored with widget
            if (ee.el.getData()!=null && ee.el.getData() instanceof Place)
              place = (Place)ee.el.getData();
            else
              place = new Place(ee.el.getLabel(),Place.ONEARTH,(Camera)null,null);
            camera = place.camera;
                
            // No camera with widget: find place for widget's label
            if (camera==null)
            {
              AbstractList<Place> places = LcarsGazetteer.findPlaces(place.world,place.name);
              if (places!=null && places.size()>0)
                camera = places.get(0).camera;
            }
            
            // Still no camera: display error
            if (camera==null)
            {
              // TODO: Display "Unknown place <caption>"
              return;
            }
            eWw.flyTo(camera);
          }
        });
      }
    });
    ePlaceNoMatch = new EPlaceNoMatch(600,440);
    eKeyboard.addKeyListener(ePlaceSearch);
    
    // The main viewer
    eInsteadOfWwHead = new ELabel(this,85,85,1783,159,LCARS.ES_STATIC|
      LCARS.EF_HEAD1|LCARS.EC_PRIMARY|LCARS.ES_SELECTED|LCARS.ES_LABEL_SW,
      "INITIALIZATION");
    add(eInsteadOfWwHead);
    eInsteadOfWwText = new ELabel(this,85,247,1783,669,LCARS.ES_STATIC|
        LCARS.EF_LARGE|LCARS.EC_TEXT|LCARS.ES_LABEL_NW,"");
      add(eInsteadOfWwText);
    
    // The bottom bar
    // - NOTE: The first secret debug button...
    ERect e = new ERect(this,23,1015,58,38,style|LCARS.ES_STATIC|LCARS.ES_RECT_RND_W,null);
    e.setStatic(false);
    e.addEEventListener(new EEventListenerAdapter()
    {
      @Override
      public void touchDown(EEvent ee)
      {
        if (getMainMode()!=MODE_WORLDWIND) return;
        try
        {
          String NL = System.getProperty("line.separator");
          File temp = File.createTempFile("LCARS-GEPanel",".txt");
          temp.deleteOnExit();
          BufferedWriter out = new BufferedWriter(new FileWriter(temp));
          out.write("<camera>" + Camera.fromView(eWw.getView()).toString() + "</camera>"+NL);
          out.close();
          Runtime.getRuntime().exec("notepad.exe "+temp.getAbsolutePath());
        }
        catch (IOException e)
        {
          e.printStackTrace();
        }
      }
    });
    add(e);

    eModeSel = new ERect(this,84,1015,131,38,style|LCARS.ES_LABEL_E,"MODE SELECT");
    eModeSel.addEEventListener(new EEventListenerAdapter()
    {
      @Override
      public void touchDown(EEvent ee)
      {
        setBarMode(BARMODE_TOGGLE);
      }
    });
    add(eModeSel);

    eArrayControls = new EArrayControls(218,1015);

    eNavi = new ENavigation(218,1015);
    
    eOrbitArray = new EElementArray(394,1015,ERect.class,new Dimension(219,38),1,5,style|LCARS.ES_LABEL_E,"");
    eOrbitArray.setPageControls(eArrayControls.getEPrev(),eArrayControls.getENext());
    eOrbitArray.setLockControl(eArrayControls.getELock());
    
    eViewArray = new EElementArray(394,1015,ERect.class,new Dimension(219,38),1,5,style|LCARS.ES_LABEL_E,"");
    eViewArray.setPageControls(eArrayControls.getEPrev(),eArrayControls.getENext());
    eViewArray.setLockControl(eArrayControls.getELock());

    eOrbit = new ERect(this,1626,1015,88,38,LCARS.EC_PRIMARY|LCARS.ES_LABEL_W,"ORBIT");
    eOrbit.addEEventListener(new EEventListenerAdapter()
    {
      @Override
      public void touchDown(EEvent ee)
      {
        if (eWw.getOrbit()!=null)
          eWw.setOrbit(null);
        else
          setBarMode(BARMODE_ORBITS);
      }
    });
    add(eOrbit);

    ePlaces = new ERect(this,1717,1015,119,38,style|LCARS.ES_LABEL_E,"PLACES");
    ePlaces.addEEventListener(new EEventListenerAdapter()
    {
      @Override
      public void touchDown(EEvent ee)
      {
        if (ePlaceArray.isDisplayed())
          setMainMode(MODE_WORLDWIND);
        else
          setMainMode(MODE_PLACES);
      }
    });
    add(ePlaces);

    // - NOTE: The second secret debug button...
    e = new ERect(this,1839,1015,58,38,style|LCARS.ES_STATIC|LCARS.ES_RECT_RND_E,null);
    e.setStatic(false);
    e.addEEventListener(new EEventListenerAdapter()
    {
      @Override
      public void touchDown(EEvent ee)
      {
        if (getMainMode()!=MODE_WORLDWIND) return;
        getEWorldWind().flyTo(new Camera(51.7577,14.3234,1000,2.1,52,0,0));
      }
    });
    add(e);
    
    // Initialize
    setBarMode(BARMODE_NAVI);

    // Schedule fatInit()
    // NOTE: invokeLater() is more responsive that EventQueue.invokeLater
    invokeLater(new Runnable()
    //EventQueue.invokeLater(new Runnable()
    {
      @Override
      public void run()
      {
        fatInit();
      }
    });
  }

  /**
   * <p><i><b style="color:red">Experimental API.</b></i></p>
   * 
   * Initializes "fat" components which take their time. Invoked by a timer task
   * scheduled by {@link #init()}.
   */
  protected void fatInit()
  {
    eInsteadOfWwText.setLabel("MODEL");
    Model model = getModel();

    eInsteadOfWwText.setLabel("NASA WORLD WIND");
    eWw = new EWorldWind(52,85,1816,926,LCARS.ES_NONE);
    eWw.setModel(model);
    eWw.addToPanel(WorldWindPanel.this);

    eInsteadOfWwText.setLabel("OPTIONS");
    
    /* TODO: ...
    places = new Places(getClass().getClassLoader().getResourceAsStream(GE_PLACES));
    */
    fillOrbitArray();    
    fillViewArray();
    fillPlacesArray(null);

    eInsteadOfWwText.setLabel("COMPLETE");
  }
  
  @Override
  public void start()
  {
    super.start();
  }

  @Override
  protected void fps25()
  {
    displayTime();
    displayWorldWindState();
  }

  @Override
  public void stop()
  {
    try 
    {
      eWw.removeFromPanel();
    }
    catch (Exception e) {}
    super.stop();
  }

  @Override
  public String getTitle()
  {
    return "EARTH";
  }
  
  //-- Abstract API --

  /**
   * <p><i><b style="color:red">Experimental API.</b></i></p>
   * 
   * Returns the {@linkplain Model globe model} to be displayed on this panel.
   */
  public abstract Model getModel();
  
  /**
   * <p><i><b style="color:red">Experimental API.</b></i></p>
   * 
   * Returns the list of {@linkplain LayerList layer sets} to be displayed on
   * this panel.
   */
  public abstract ArrayList<LayerSet> getLayerSets();
  
  /**
   * <p><i><b style="color:red">Experimental API.</b></i></p>
   * 
   * Returns the list of {@linkplain Orbit orbits} for this World Wind panel.
   */
  public abstract ArrayList<Orbit.ListItem> getOrbitList();

  /**
   * <p><i><b style="color:red">Experimental API.</b></i></p>
   * 
   * Returns the places of interest list.
   */
  public abstract ArrayList<Place> getPoiList();
  
  // -- Getters and setters --
  
  /**
   * <p><i><b style="color:red">Experimental API.</b></i></p>
   * 
   * Returns the World Wind element contributor.
   */
  protected EWorldWind getEWorldWind()
  {
    return this.eWw;
  }
  
  /**
   * <p><i><b style="color:red">Experimental API.</b></i></p>
   * 
   * Sets the main mode.
   * 
   * @param mode The mode (one of the <code>MODE_XXX</code> constants).
   */
  protected void setMainMode(int mode)
  {
    if (getMainMode()==mode) return;
    switch (mode)
    {
    case MODE_INIT:
      if (eWw!=null)
        eWw.removeFromPanel();
      eKeyboard.removeFromPanel();
      ePlaceSearch.removeFromPanel();
      ePlaceArray.removeFromPanel();
      ePlaceNoMatch.removeFromPanel();
      break;
    case MODE_PLACES:
      if (eWw!=null)
        eWw.removeFromPanel();
      eKeyboard.addToPanel(this);
      ePlaceSearch.addToPanel(this);
      ePlaceArray.addToPanel(this);
      break;
    default: // = MODE_WORLDWIND
      eKeyboard.removeFromPanel();
      ePlaceSearch.removeFromPanel();
      ePlaceArray.removeFromPanel();
      ePlaceNoMatch.removeFromPanel();
      if (eWw!=null)
        eWw.addToPanel(this);
      break;
    }
  }

  /**
   * <p><i><b style="color:red">Experimental API.</b></i></p>
   * 
   * Returns the current main mode.
   * 
   * @return one of the <code>MODE_XXX</code> constants
   */
  protected int getMainMode()
  {
    if (eWw!=null && eWw.isDisplayed()) return MODE_WORLDWIND;
    if (ePlaceSearch.isDisplayed()) return MODE_PLACES;
    return MODE_INIT;
  }
  
  /**
   * <p><i><b style="color:red">Experimental API.</b></i></p>
   * 
   * Sets the bottom bar mode.
   * 
   * @param mode The mode (one of the <code>BARMODE_XXX</code> constants).
   */
  protected void setBarMode(int mode)
  {
    if (mode<BARMODE_TOGGLE || mode>=NUM_BARMODES) return;
    mode = (mode==BARMODE_TOGGLE?getBarMode()+1:mode);
    mode %= NUM_BARMODES;
    
    eArrayControls.removeFromPanel();
    eOrbitArray.removeFromPanel();
    eViewArray.removeFromPanel();
    eNavi.removeFromPanel();

    switch (mode)
    {
    case BARMODE_VIEW:
      eArrayControls.addToPanel(this);
      ((EValue)eArrayControls.getEPrev()).setValue("VIEW");
      eViewArray.addToPanel(this);
      break;
    case BARMODE_ORBITS:
      eArrayControls.addToPanel(this);
      ((EValue)eArrayControls.getEPrev()).setValue("ORBIT");
      eOrbitArray.addToPanel(this);
      break;
    default:
      eNavi.addToPanel(this);
      break;
    }
  }

  /**
   * <p><i><b style="color:red">Experimental API.</b></i></p>
   * 
   * Returns the current bottom bar mode.
   * 
   * @return one of the <code>BARMODE_XXX</code> constants
   */
  protected int getBarMode()
  {
    if (eNavi.isDisplayed()) return BARMODE_NAVI;
    if (eOrbitArray.isDisplayed()) return BARMODE_ORBITS;
    if (eViewArray.isDisplayed()) return BARMODE_VIEW;
    throw new IllegalStateException("Unkonwn bar mode");
  }

  // -- Operations --

  /**
   * <p><i><b style="color:red">Experimental API.</b></i></p>
   * 
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

  /**
   * <p><i><b style="color:red">Experimental API.</b></i></p>
   * 
   * Updates the GUI.
   */
  protected void displayWorldWindState()
  {
    // Fall back if World Wind control does not exist
    eDate.setStatic(eWw==null);
    eModeSel.setDisabled(eWw==null);
    eNavi.setDisabled(eWw==null);
    eOrbit.setDisabled(eWw==null);
    ePlaces.setDisabled(eWw==null);
    eInsteadOfWwHead.setVisible(eWw==null);
    eInsteadOfWwText.setVisible(eWw==null);
    if (eWw==null) return;

    // Display main mode
    if (ePlaceSearch.isDisplayed())
    {
      eCaption.setValue("PLACES:"+getTitle());
    }
    else
    {
      Orbit orbit = eWw.getOrbit();
      eCaption.setValue(getTitle()+(orbit!=null?":"+orbit.getName():""));
    }
    ePlaces.setBlinking(ePlaceSearch.isDisplayed());

    // Display coordinates and orbiting state
    eNavi.displayCurrentState(eWw);
    eOrbit.setBlinking(eWw.getOrbit()!=null);
    
    // Display layer set states
    for (EElement e:eViewArray.getItemElements())
    {
      if (e.getData()==null || !(e.getData() instanceof LayerSet))
        return;
      
      LayerSet layerSet = (LayerSet)e.getData();
      e.setSelected(layerSet.isMajorityEnabled());
    }
  }

  /**
   * <p><i><b style="color:red">Experimental API.</b></i></p>
   * 
   * Fills the orbit array in the bottom bar.
   */
  protected void fillOrbitArray()
  {
    // Standard orbits
    eOrbitArray.removeAll();
    eOrbitArray.add("NONE").addEEventListener(new EEventListenerAdapter()
    {
      @Override
      public void touchUp(EEvent ee)
      {
        eWw.setOrbit(null);
        setBarMode(BARMODE_NAVI);
      }
    });

    // Panel orbits
    ArrayList<Orbit.ListItem> ol = getOrbitList();
    if (ol==null) return;
    for (Orbit.ListItem oli:ol)
    {
      EElement e = eOrbitArray.add(oli.name);
      e.setData(oli.clazz);
      e.addEEventListener(new EEventListenerAdapter()
      {
        @SuppressWarnings("unchecked")
        @Override
        public void touchUp(EEvent ee)
        {
          try
          {
            eWw.setOrbit(((Class<Orbit>)ee.el.getData()).newInstance());
            setBarMode(BARMODE_NAVI);
          } catch (InstantiationException | IllegalAccessException e)
          {
            e.printStackTrace();
          }
        }
      });
    }
  }
  
  /**
   * <p><i><b style="color:red">Experimental API.</b></i></p>
   * 
   * Fills the view array in the bottom bar.
   */
  protected void fillViewArray()
  {
    eViewArray.removeAll();

    ArrayList<LayerSet> layerSets = getLayerSets();
    if (layerSets==null) return;
    for (LayerSet layerSet:layerSets)
    {
      EElement e = eViewArray.add(layerSet.getDisplayName());
      e.setData(layerSet);
      e.setDisabled(layerSet.isEmpty());
      e.addEEventListener(new EEventListenerAdapter()
      {
        @Override
        public void touchDown(EEvent ee)
        {
          LayerSet layerSet = (LayerSet)ee.el.getData();
          layerSet.setEnabled(!layerSet.isMajorityEnabled());
        }
      });
    }
  }
  
  /**
   * Fills the places array ({@link #ePlaceArray}) in the search panel. If
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
    ePlaceArray.removeAll();
    if (address!=null && address.equals("")) return;
    AbstractList<Place> places = null;
    if (address==null)
      places = getPoiList();
    else
      places = LcarsGazetteer.findPlaces(Place.ONEARTH,address);    
    
    if (places==null || places.size()==0)
    {
      ePlaceArray.removeFromPanel();
      ePlaceNoMatch.addToPanel(this);
    }
    else
    {
      ePlaceNoMatch.removeFromPanel();
      if (eKeyboard.isDisplayed()) ePlaceArray.addToPanel(this);
      for (Place place : places)
      {
        EElement el = ePlaceArray.add(place.name);
        if (place.name.length()>50)
          el.setLabel((place.name.substring(0,47)+"...").toUpperCase());
        el.setData(place);
      }
    }
  }
  
  // == NESTED UI CLASSES ==
  
  class EArrayControls extends ElementContributor
  {
    private EValue ePrev;
    private ERect  eNext;
    private ERect  eLock;
    
    public EArrayControls(int x, int y)
    {
      super(x,y);
      ePrev  = new EValue(null,   0,0,174,38,style|LCARS.ES_LABEL_E,"PREV");
      eNext  = new ERect (null,1286,0, 58,38,style|LCARS.ES_LABEL_W,"NEXT");
      eLock  = new ERect (null,1347,0, 58,38,style|LCARS.ES_LABEL_W,"LOCK");
      ePrev.setValueMargin(0);
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
    private ERect  eLatDec;
    private ERect  eLatInc;
    private EValue eLon;
    private ERect  eLonDec;
    private ERect  eLonInc;
    private EValue eAlt;
    private ERect  eAltDec;
    private ERect  eAltInc;
    private EValue ePit;
    private ERect  ePitDec;
    private ERect  ePitInc;
    private EValue eHdg;
    private ERect  eHdgDec;
    private ERect  eHdgInc;
    
    public ENavigation(int x, int y)
    {
      super(x,y);
      EElement e;
      
      // Latitude control
      eLat = new EValue(null,0,0,198,38,ST_E,"LAT/°");
      eLat.setValue("00.0N"); eLat.setValueMargin(0);
      eLat.addEEventListener(new EEventListenerAdapter()
      {
        @Override
        public void touchDown(EEvent ee)
        {
          if (eWw==null) return;
          eWw.setEyePosition(null);
        }
      });
      add(eLat);
      eLatDec = new ERect(null,201,0,38,38,ST_C,"\u2013");
      eLatDec.addEEventListener(new EEventListenerAdapter()
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
          if (eWw==null) return;
          Position pos = eWw.getEyePosition();
          if (pos==null) return;
          Angle  lat = Angle.fromDegreesLatitude(pos.latitude.degrees-1);
          Angle  lon = pos.longitude;
          double alt = pos.elevation;
          eWw.setEyePosition(new Position(lat,lon,alt));
        }
        @Override
        public void touchUp(EEvent ee)
        {
          eLat.setSelected(false);
        }
      });
      add(eLatDec);
      eLatInc = new ERect(null,242,0,38,38,ST_E,"+");
      eLatInc.addEEventListener(new EEventListenerAdapter()
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
          if (eWw==null) return;
          Position pos = eWw.getEyePosition();
          if (pos==null) return;
          Angle  lat = Angle.fromDegreesLatitude(pos.latitude.degrees+1);
          Angle  lon = pos.longitude;
          double alt = pos.elevation;
          eWw.setEyePosition(new Position(lat,lon,alt));
        }
        @Override
        public void touchUp(EEvent ee)
        {
          eLat.setSelected(false);
        }
      });
      add(eLatInc);

      // Longitude control
      eLon = new EValue(null,283,0,198,38,ST_E,"LON/°");
      eLon.setValue("00.0E"); eLon.setValueMargin(0);
      eLon.addEEventListener(new EEventListenerAdapter()
      {
        @Override
        public void touchDown(EEvent ee)
        {
          if (eWw==null) return;
          eWw.setEyePosition(null);
        }
      });
      add(eLon);
      eLonDec = new ERect(null,481,0,38,38,ST_C,"\u2013");
      eLonDec.addEEventListener(new EEventListenerAdapter()
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
          if (eWw==null) return;
          Position pos = eWw.getEyePosition();
          if (pos==null) return;
          Angle  lat = pos.latitude;
          Angle  lon = Angle.fromDegreesLongitude(pos.longitude.degrees-1);
          double alt = pos.elevation;
          eWw.setEyePosition(new Position(lat,lon,alt));
        }
        @Override
        public void touchUp(EEvent ee)
        {
          eLon.setSelected(false);
        }
      });
      add(eLonDec);
      eLonInc = new ERect(null,522,0,38,38,ST_E,"+");
      eLonInc.addEEventListener(new EEventListenerAdapter()
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
          if (eWw==null) return;
          Position pos = eWw.getEyePosition();
          if (pos==null) return;
          Angle  lat = pos.latitude;
          Angle  lon = Angle.fromDegreesLongitude(pos.longitude.degrees+1);
          double alt = pos.elevation;
          eWw.setEyePosition(new Position(lat,lon,alt));
        }
        @Override
        public void touchUp(EEvent ee)
        {
          eLon.setSelected(false);
        }
      });
      add(eLonInc);

      // Altitude control
      eAlt = new EValue(null,563,0,203,38,ST_E,"ALT/km");
      eAlt.setValue("0.000"); eAlt.setValueMargin(0);
      eAlt.addEEventListener(new EEventListenerAdapter()
      {
        @Override
        public void touchDown(EEvent ee)
        {
          if (eWw==null) return;
          eWw.setEyePosition(null);
        }
      });
      add(eAlt);
      eAltDec = new ERect(null,766,0,38,38,ST_C,"\u2013");
      eAltDec.addEEventListener(new EEventListenerAdapter()
      {
        @Override
        public void touchDown(EEvent ee)
        {
          eAlt.setSelected(true);
          touchHold(ee);
        }
        @Override
        public void touchHold(EEvent ee)
        {
          if (ee.ct>0 && ee.ct<=5) return;
          if (eWw==null) return;
          Position pos = eWw.getEyePosition();
          if (pos==null) return;
          Angle  lat = pos.latitude;
          Angle  lon = pos.longitude;
          double alt = pos.elevation*0.95;
          eWw.setEyePosition(new Position(lat,lon,alt));
        }
        @Override
        public void touchUp(EEvent ee)
        {
          eAlt.setSelected(false);
        }
      });
      add(eAltDec);
      eAltInc = new ERect(null,807,0,38,38,ST_E,"+");
      eAltInc.addEEventListener(new EEventListenerAdapter()
      {
        @Override
        public void touchDown(EEvent ee)
        {
          eAlt.setSelected(true);
          touchHold(ee);
        }
        @Override
        public void touchHold(EEvent ee)
        {
          if (ee.ct>0 && ee.ct<=5) return;
          if (eWw==null) return;
          Position pos = eWw.getEyePosition();
          if (pos==null) return;
          Angle  lat = pos.latitude;
          Angle  lon = pos.longitude;
          double alt = pos.elevation/0.95;
          eWw.setEyePosition(new Position(lat,lon,alt));
        }
        @Override
        public void touchUp(EEvent ee)
        {
          eAlt.setSelected(false);
        }
      });
      add(eAltInc);

      // Pitch control
      ePit = new EValue(null,848,0,198,38,ST_E|LCARS.ES_STATIC,"PIT/°");
      ePit.setValue("00.0"); ePit.setValueMargin(0);
      ePit.addEEventListener(new EEventListenerAdapter()
      {
        @Override
        public void touchDown(EEvent ee)
        {
          if (eWw==null) return;
          eWw.setPitch(null);
        }
      });
      add(ePit);
      ePitDec = new ERect(null,1046,0,38,38,ST_C,"\u2013");
      ePitDec.addEEventListener(new EEventListenerAdapter()
      {
        @Override
        public void touchDown(EEvent ee)
        {
          ePit.setSelected(true);
          touchHold(ee);
        }
        @Override
        public void touchHold(EEvent ee)
        {
          if (ee.ct>0 && ee.ct<=5) return;
          if (eWw==null) return;
          Angle pitch = eWw.getPitch();
          if (pitch==null) return;
          pitch = Angle.fromDegreesLongitude(pitch.degrees-1);
          eWw.setPitch(pitch);
        }
        @Override
        public void touchUp(EEvent ee)
        {
          ePit.setSelected(false);
        }
      });
      add(ePitDec);
      ePitInc = new ERect(null,1087,0,38,38,ST_E,"+");
      ePitInc.addEEventListener(new EEventListenerAdapter()
      {
        @Override
        public void touchDown(EEvent ee)
        {
          ePit.setSelected(true);
          touchHold(ee);
        }
        @Override
        public void touchHold(EEvent ee)
        {
          if (ee.ct>0 && ee.ct<=5) return;
          if (eWw==null) return;
          Angle pitch = eWw.getPitch();
          if (pitch==null) return;
          pitch = Angle.fromDegreesLongitude(pitch.degrees+1);
          eWw.setPitch(pitch);
        }
        @Override
        public void touchUp(EEvent ee)
        {
          ePit.setSelected(false);
        }
      });
      add(ePitInc);

      // Heading control
      eHdg = new EValue(null,1128,0,198,38,ST_E|LCARS.ES_STATIC,"HDG/°");
      eHdg.setValue("000.00"); eHdg.setValueMargin(0);
      eHdg.addEEventListener(new EEventListenerAdapter()
      {
        @Override
        public void touchDown(EEvent ee)
        {
          if (eWw==null) return;
          eWw.setHeading(null);
        }
      });
      add(eHdg);
      eHdgDec = new ERect(null,1326,0,38,38,ST_C,"\u2013");
      eHdgDec.addEEventListener(new EEventListenerAdapter()
      {
        @Override
        public void touchDown(EEvent ee)
        {
          eHdg.setSelected(true);
          touchHold(ee);
        }
        @Override
        public void touchHold(EEvent ee)
        {
          if (ee.ct>0 && ee.ct<=5) return;
          if (eWw==null) return;
          Angle heading = eWw.getHeading();
          if (heading==null) return;
          heading = Angle.fromDegreesLongitude(heading.degrees-1);
          eWw.setHeading(heading);
        }
        @Override
        public void touchUp(EEvent ee)
        {
          eHdg.setSelected(false);
        }
      });
      add(eHdgDec);
      eHdgInc = new ERect(null,1367,0,38,38,ST_E,"+");
      eHdgInc.addEEventListener(new EEventListenerAdapter()
      {
        @Override
        public void touchDown(EEvent ee)
        {
          eHdg.setSelected(true);
          touchHold(ee);
        }
        @Override
        public void touchHold(EEvent ee)
        {
          if (ee.ct>0 && ee.ct<=5) return;
          if (eWw==null) return;
          Angle heading = eWw.getHeading();
          if (heading==null) return;
          heading = Angle.fromDegreesLongitude(heading.degrees+1);
          eWw.setHeading(heading);
        }
        @Override
        public void touchUp(EEvent ee)
        {
          eHdg.setSelected(false);
        }
      });
      add(eHdgInc);
      
      // TODO: Make a field of view slider out of these -->
      e = new EValue(null,0,41,79,18,ST_T|LCARS.ES_RECT_RND_W,"SPEED");
      ((EValue)e).setValue("0.00"); ((EValue)e).setValueMargin(0);
      add(e);
      e = new EValue(null,79,41,119,18,ST_T,"TARGET");
      ((EValue)e).setValue("00.0N"); ((EValue)e).setValueMargin(0);
      add(e);
      add(new ERect(null,201,41,79,18,ST_T|LCARS.ES_RECT_RND_E,null));
      e = new EValue(null,283,41,198,18,ST_T|LCARS.ES_RECT_RND_W,null);
      ((EValue)e).setValue("00.0E"); ((EValue)e).setValueMargin(0);
      add(e);
      add(new ERect(null,481,41,79,18,ST_T|LCARS.ES_RECT_RND_E,null));
      e = new EValue(null,563,41,203,18,ST_T|LCARS.ES_RECT_RND_W,null);
      ((EValue)e).setValue("0.000"); ((EValue)e).setValueMargin(0);
      add(e);
      add(new ERect(null,766,41,79,18,ST_T|LCARS.ES_RECT_RND_E,null));
      e = new EValue(null,848,41,198,18,ST_T|LCARS.ES_RECT_RND_W,null);
      ((EValue)e).setValue("00.0"); ((EValue)e).setValueMargin(0);
      add(e);
      add(new ERect(null,1046,41,79,18,ST_T|LCARS.ES_RECT_RND_E,null));
      e = new EValue(null,1128,41,198,18,ST_T|LCARS.ES_RECT_RND_W,null);
      ((EValue)e).setValue("000.00"); ((EValue)e).setValueMargin(0);
      add(e);
      add(new ERect(null,1326,41,79,18,ST_T|LCARS.ES_RECT_RND_E,null));
      // <--
    }
    
    public void setDisabled(boolean disabled)
    {
      for (EElement e:getElements())
        e.setDisabled(disabled);
    }
    
    public void displayCurrentState(EWorldWind eWw)
    {
      if (eWw==null || eWw.getView()==null)
      {
        eLat.setValue("N/A");
        eLon.setValue("N/A");
        eAlt.setValue("N/A");
        ePit.setValue("N/A");
        eHdg.setValue("N/A");
        return;
      }
      
      String s;
      double v;
      boolean b;
      
      // TODO: Bogus...
      boolean sky = getTitle().startsWith("SKY");

      // Display actual view
      v = eWw.getView().getEyePosition().getLatitude().getDegrees();
      s = String.format(Locale.US,"%05.2f",Math.abs(v))+(v<0?"S":"N");
      eLat.setLabel(sky?"DEC/°":"LAT/°");
      eLat.setValue(s);
      b = !(eWw.getOrbit()==null || !eWw.getOrbit().controlsLatitude());
      eLat.setStatic(b);
      eLatDec.setDisabled(b);
      eLatInc.setDisabled(b);

      v = eWw.getView().getEyePosition().getLongitude().getDegrees();
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
      b = !(eWw.getOrbit()==null || !eWw.getOrbit().controlsLongitude());
      eLon.setStatic(b);
      eLonDec.setDisabled(b);
      eLonInc.setDisabled(b);
      
      v = eWw.getView().getEyePosition().getAltitude()/1000.;
      eAlt.setDisabled(sky);
      if      (v<1E3) s = String.format(Locale.US,"%06.2f" ,v    );
      else if (v<1E6) s = String.format(Locale.US,"%06.2fT",v/1E3);
      else if (v<1E9) s = String.format(Locale.US,"%06.2fM",v/1E6);
      else            s = String.format(Locale.US,"%06.2fB",v/1E9);
      eAlt.setValue(sky?"NA":s);
      b = !(eWw.getOrbit()==null || !eWw.getOrbit().controlsAltitude());
      eAlt.setStatic(b);
      eAltDec.setDisabled(b);
      eAltInc.setDisabled(b);

      v = eWw.getView().getPitch().getDegrees();
      s = String.format(Locale.US,"%s%05.2f",v<0?"-":"",Math.abs(v));
      ePit.setValue(s);
      b = !(eWw.getOrbit()==null || !eWw.getOrbit().controlsPitch());
      ePit.setStatic(b);
      ePitDec.setDisabled(b);
      ePitInc.setDisabled(b);

      v = eWw.getView().getHeading().getDegrees();
      s = String.format(Locale.US,"%s%06.2f",v<0?"-":"",Math.abs(v));
      eHdg.setValue(s);
      b = !(eWw.getOrbit()==null || !eWw.getOrbit().controlsHeading());
      eHdg.setStatic(b);
      eHdgDec.setDisabled(b);
      eHdgInc.setDisabled(b);
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
      e = new ERect(null,1374,4,258,71,LCARS.EC_PRIMARY|LCARS.ES_SELECTED|LCARS.ES_LABEL_W,"FIND PLACES");
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
        setMainMode(MODE_WORLDWIND);
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
}
