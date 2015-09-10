package de.tucottbus.kt.lcarsx.wwj;

import gov.nasa.worldwind.Model;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.layers.LayerList;

import java.awt.Dimension;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Calendar;

import de.tucottbus.kt.lcars.IScreen;
import de.tucottbus.kt.lcars.LCARS;
import de.tucottbus.kt.lcars.MainPanel;
import de.tucottbus.kt.lcars.contributors.EAlphaKeyboard;
import de.tucottbus.kt.lcars.contributors.EElementArray;
import de.tucottbus.kt.lcars.elements.EElement;
import de.tucottbus.kt.lcars.elements.EEvent;
import de.tucottbus.kt.lcars.elements.EEventListenerAdapter;
import de.tucottbus.kt.lcars.elements.ELabel;
import de.tucottbus.kt.lcars.elements.ERect;
import de.tucottbus.kt.lcars.elements.EValue;
import de.tucottbus.kt.lcars.swt.SwtColor;
import de.tucottbus.kt.lcarsx.wwj.contributors.EArrayControls;
import de.tucottbus.kt.lcarsx.wwj.contributors.ENavigation;
import de.tucottbus.kt.lcarsx.wwj.contributors.EPlaceNoMatch;
import de.tucottbus.kt.lcarsx.wwj.contributors.EPlaceSearch;
import de.tucottbus.kt.lcarsx.wwj.contributors.EWorldWind;
import de.tucottbus.kt.lcarsx.wwj.layers.LayerSet;
import de.tucottbus.kt.lcarsx.wwj.orbits.Orbit;
import de.tucottbus.kt.lcarsx.wwj.places.Camera;
import de.tucottbus.kt.lcarsx.wwj.places.LcarsGazetteer;
import de.tucottbus.kt.lcarsx.wwj.places.Place;

/**
 * <p><i><b style="color:red">Experimental.</b></i></p>
 * 
 * The NASA World Wind panel.
 * 
 * @author Matthias Wolff, BTU Cottbus-Senftenberg
 */
public abstract class WorldWindPanel extends MainPanel
{
  // -- Constants --
  // Main modes
  public static final int MODE_INIT      = 0;
  public static final int MODE_WORLDWIND = 1;
  public static final int MODE_PLACES    = 2;
  
  // Bottom bar modes
  public static final int NUM_BARMODES   = 3;
  public static final int BARMODE_TOGGLE = -1;
  public static final int BARMODE_NAVI   = 0;
  public static final int BARMODE_ORBITS = 1;
  public static final int BARMODE_VIEW   = 2;
  
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
  private   EValue          eNetworkState;
  
  public final int style = LCARS.EC_SECONDARY | LCARS.ES_SELECTED;
  
  // -- LCARS API --
  
  /**
   * <p><i><b style="color:red">Experimental.</b></i></p>
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
    ePlaceSearch = new EPlaceSearch(this, 206,105);
    ePlaceArray = new EElementArray(417,198,ERect.class,new Dimension(470,52),7,3,LCARS.EC_SECONDARY|LCARS.ES_SELECTED|LCARS.ES_RECT_RND,null);
    ePlaceArray.setPageControls(ePlaceSearch.getEPrev(),ePlaceSearch.getENext());
    ePlaceArray.setLockControl(ePlaceSearch.getELock());
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

    eArrayControls = new EArrayControls(this, 218,1015);

    eNavi = new ENavigation(this, 218,1015);
    
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
    
    // The status line
    eNetworkState = new EValue(null,1346,1056,278,18,LCARS.EC_PRIMARY|LCARS.ES_SELECTED|LCARS.ES_STATIC|LCARS.ES_RECT_RND,null);
    eNetworkState.setVisible(false);
    add(eNetworkState);

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
   * <p><i><b style="color:red">Experimental.</b></i></p>
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
  protected void fps2()
  {
    final SwtColor nullColor = null;
    
    // Show network state
    if (WorldWind.getNetworkStatus().isNetworkUnavailable())
    {
      if (eNetworkState.getData()==null)
      {
        eNetworkState.setColor(LCARS.getColor(LCARS.CS_REDALERT,LCARS.EC_PRIMARY|LCARS.ES_SELECTED));
        eNetworkState.setData(new Boolean(true));
      }
      else
      {
        eNetworkState.setColor(LCARS.getColor(LCARS.CS_REDALERT,LCARS.EC_SECONDARY));
        eNetworkState.setData(null);
      }
      eNetworkState.setValue("NO CONNECTION");
      eNetworkState.setVisible(true);
    }
    else if (WorldWind.getRetrievalService().hasActiveTasks())
    {
      eNetworkState.setVisible(true);
      eNetworkState.setColor(nullColor);
      eNetworkState.setValue("DATA TRANSMISSION");
    }
    else
    {
      eNetworkState.setVisible(false);
      eNetworkState.setColor(nullColor);
      eNetworkState.setValue("");
    }
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
   * <p><i><b style="color:red">Experimental.</b></i></p>
   * 
   * Returns the {@linkplain Model globe model} to be displayed on this panel.
   */
  public abstract Model getModel();
  
  /**
   * <p><i><b style="color:red">Experimental.</b></i></p>
   * 
   * Returns the list of {@linkplain LayerList layer sets} to be displayed on
   * this panel.
   */
  public abstract ArrayList<LayerSet> getLayerSets();
  
  /**
   * <p><i><b style="color:red">Experimental.</b></i></p>
   * 
   * Returns the list of {@linkplain Orbit orbits} for this World Wind panel.
   */
  public abstract ArrayList<Orbit.ListItem> getOrbitList();

  /**
   * <p><i><b style="color:red">Experimental.</b></i></p>
   * 
   * Returns the places of interest list.
   */
  public abstract ArrayList<Place> getPoiList();
  
  // -- Getters and setters --
  
  /**
   * <p><i><b style="color:red">Experimental.</b></i></p>
   * 
   * Returns the World Wind element contributor.
   */
  public EWorldWind getEWorldWind()
  {
    return this.eWw;
  }
  
  /**
   * <p><i><b style="color:red">Experimental.</b></i></p>
   * 
   * Sets the main mode.
   * 
   * @param mode The mode (one of the <code>MODE_XXX</code> constants).
   */
  public void setMainMode(int mode)
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
   * <p><i><b style="color:red">Experimental.</b></i></p>
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
   * <p><i><b style="color:red">Experimental.</b></i></p>
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
   * <p><i><b style="color:red">Experimental.</b></i></p>
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
   * <p><i><b style="color:red">Experimental.</b></i></p>
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
   * <p><i><b style="color:red">Experimental.</b></i></p>
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
   * <p><i><b style="color:red">Experimental.</b></i></p>
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
   * <p><i><b style="color:red">Experimental.</b></i></p>
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
  public void fillPlacesArray(String address)
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

}
