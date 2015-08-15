package de.tucottbus.kt.lcars.padd;

import java.awt.Dimension;
import java.rmi.RemoteException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import de.tucottbus.kt.lcars.IScreen;
import de.tucottbus.kt.lcars.LCARS;
import de.tucottbus.kt.lcars.PaddMainPanel;
import de.tucottbus.kt.lcars.contributors.EElementArray;
import de.tucottbus.kt.lcars.elements.EElbo;
import de.tucottbus.kt.lcars.elements.EElement;
import de.tucottbus.kt.lcars.elements.EEvent;
import de.tucottbus.kt.lcars.elements.EEventListenerAdapter;
import de.tucottbus.kt.lcars.elements.EImage;
import de.tucottbus.kt.lcars.elements.ELabel;
import de.tucottbus.kt.lcars.elements.ERect;
import de.tucottbus.kt.lcars.elements.EValue;
import de.tucottbus.kt.lcars.util.LoadStatistics;

public class WorldClockPanel extends PaddMainPanel
{
  private TimeZone      timezone;
  private int           mode;
  private ERect         eHome;
  private ELabel        eTzHead;
  private EElementArray eTzArray;
  private EElbo         eTzCount;
  private ERect         ePrev;
  private ERect         eNext;
  private ERect         eLock;
  private ERect         eGmtOffs;
  private ERect         eLight;
  private ERect         eDim;
  private EValue        eGuiFps;
  private EValue        eGuiLd;
  private ELabel        eDate;
  private EValue        eTime;
  private ELabel        eAmPm;
  private ELabel        eTimeZone;
  private EElbo         eCpuLd;

  public static final int CLOCK_WORLD   = 0;
  public static final int CLOCK_LECTURE = 1;
  
  public WorldClockPanel(IScreen screen)
  {
    super(screen);
  }
  
  /*
   * (non-Javadoc)
   * @see de.tucottbus.kt.lcars.PaddMainPanel#init()
   */
  @Override
  public void init()
  {
    super.init();
    int w = onWeTab() ? 1254 : 1366;
    EElement e;
    setTitle(null);
    
    // The top bar
    e = new EElbo(this,5,5,212,128,LCARS.ES_SHAPE_NW|LCARS.EC_PRIMARY|LCARS.ES_LABEL_SE,"WePADD");
    ((EElbo)e).setArmWidths(158,48); ((EElbo)e).setArcWidths(118,48);
    e.addEEventListener(new EEventListenerAdapter()
    {
      @Override
      public void touchDown(EEvent ee)
      {
        // Array animation would bring array items on top of panel selector -> stop it
        eTzArray.setLock(true);
        eTzArray.cancelAllTimerTasks();
        panelSelectionDialog();
      }
    });
    add(e);
    
    eHome = new ERect(this,220,5,285,48,LCARS.EC_SECONDARY|LCARS.ES_LABEL_E,"HOME");
    eHome.addEEventListener(new EEventListenerAdapter()
    {
      @Override
      public void touchDown(EEvent ee)
      {
        timezone = Calendar.getInstance().getTimeZone();
      }
    });
    add(eHome);    
    
    e = new EValue(this,508,5,w-513,48,LCARS.ES_STATIC|LCARS.EC_ELBOUP|LCARS.ES_SELECTED|LCARS.ES_RECT_RND_E,null);
    ((EValue)e).setValue("WORLD CLOCK"); ((EValue)e).setValueMargin(38);
    add(e);

    // The left bar    
    eTzCount = new EElbo(this,5,136,500,237,LCARS.EC_SECONDARY|LCARS.ES_STATIC|LCARS.ES_LABEL_SE,"000");
    eTzCount.setArcWidths(1,1); eTzCount.setArmWidths(158,3);
    add(eTzCount);

    eTzHead = new ELabel(this,165,90,340,43,LCARS.EC_TEXT|LCARS.EF_LARGE|LCARS.ES_STATIC|LCARS.ES_LABEL_SE,""); 
    add(eTzHead);
    
    ePrev = new ERect(this,5,376,158,48,LCARS.EC_SECONDARY|LCARS.ES_LABEL_E,"PREV");
    eNext = new ERect(this,5,427,158,48,LCARS.EC_SECONDARY|LCARS.ES_LABEL_E,"NEXT");
    eLock = new ERect(this,5,478,158,48,LCARS.EC_PRIMARY|LCARS.ES_LABEL_E,"LOCK");
    add(ePrev); add(eNext); add(eLock);    
    
    e = new ERect(this,5,529,158,141,LCARS.EC_SECONDARY|LCARS.ES_SELECTED|LCARS.ES_LABEL_SE,"MODE\nSELECT");
    e.addEEventListener(new EEventListenerAdapter()
    {
      @Override
      public void touchDown(EEvent ee)
      {
        mode=(mode+1)%4;
        int style = eAmPm.getStyle()&~LCARS.ES_LABEL;
        eAmPm.setStyle(style|(mode%2>0?LCARS.ES_LABEL_SE:LCARS.ES_LABEL_SW));
        eAmPm.invalidate(true);
      }
    });
    add(e);
    
    // The time zone selector
    timezone = Calendar.getInstance().getTimeZone();
    Dimension dim = new Dimension(330,40);
    eTzArray = new EElementArray(175,150,ELabel.class,dim,13,1,LCARS.EC_ELBOUP,null);
    eTzArray.setPageControls(ePrev,eNext); eTzArray.setLockControl(eLock);
    eTzArray.addEEventListener(new EEventListenerAdapter()
    {
      @Override
      public void touchDown(EEvent ee)
      {
        eTzArray.setLock(true);
        timezone = TimeZone.getTimeZone((String)ee.el.getData());
      }
    });
    eTzArray.addToPanel(this);
    fill();
    
    // The bottom bar
    int wb = onWeTab() ? w-=325 : w;
    
    eCpuLd = new EElbo(this,5,673,212,90,LCARS.ES_STATIC|LCARS.ES_SHAPE_SW|LCARS.EC_ELBOUP|LCARS.ES_LABEL_NE,null);
    eCpuLd.setArmWidths(158,38); eCpuLd.setArcWidths(118,48);
    add(eCpuLd);
  
    eGmtOffs = new ERect(this,220,725,wb-605,38,LCARS.EC_ELBOUP|LCARS.ES_STATIC|LCARS.ES_LABEL_W|LCARS.ES_SELECTED,null);
    add(eGmtOffs);
    
    eLight = new ERect(this,wb-382,725,69,38,LCARS.EC_PRIMARY|LCARS.ES_LABEL_E,"LIGHT");
    eDim = new ERect(this,wb-310,725,69,38,LCARS.EC_SECONDARY|LCARS.ES_LABEL_E,"DIM");
    add(eLight);
    add(eDim);
    setDimContols(eLight,eDim);    
    
    eGuiFps = new EValue(this,wb-238,725,127,38,LCARS.EC_ELBOUP|LCARS.ES_STATIC|LCARS.ES_LABEL_E,"GUI FPS");
    eGuiFps.setValueMargin(0); eGuiFps.setValue("00");
    add(eGuiFps);
    
    eGuiLd = new EValue(this,wb-108,725,103,38,LCARS.EC_ELBOUP|LCARS.ES_STATIC|LCARS.ES_LABEL_E|LCARS.ES_RECT_RND_E,"LD");
    eGuiLd.setValueMargin(18); eGuiLd.setValue("000");
    add(eGuiLd);
    
    if (onWeTab())
    {
      e = new EImage(this,wb-7,650,LCARS.ES_STATIC,"de/tudresden/ias/lcars/padd/WePaddLogo.png");
      add(e);
    }
    
    // The clock
    eDate = new ELabel(this,onWeTab()?635:685,220,600,80,LCARS.ES_STATIC|LCARS.EC_TEXT|LCARS.EF_HEAD2|LCARS.ES_LABEL_W,"");
    add(eDate);

    eTime = new EValue(this,onWeTab()?620:670,314,600,200,LCARS.ES_STATIC|LCARS.EC_HEADLINE|LCARS.ES_VALUE_W,null);
    eTime.setValueWidth(600); eTime.setValueMargin(0);
    add(eTime);

    eAmPm = new ELabel(this,onWeTab()?965:1100,467,250,50,LCARS.ES_STATIC|LCARS.EC_HEADLINE|LCARS.EF_HEAD1|LCARS.ES_LABEL_SE,"");
    add(eAmPm);
    
    eTimeZone = new ELabel(this,onWeTab()?635:685,510,600,80,LCARS.ES_STATIC|LCARS.EC_TEXT|LCARS.EF_LARGE|LCARS.ES_LABEL_W,"");
    add(eTimeZone);
  }

  @Override
  protected void fps10()
  {
    Calendar now = Calendar.getInstance(timezone,Locale.US);
    int h = now.get(mode<2?Calendar.HOUR_OF_DAY:Calendar.HOUR);
    int m = now.get(Calendar.MINUTE);
    int s = now.get(Calendar.SECOND);
    int p = now.get(Calendar.AM_PM);
    if (mode>=2 && p==Calendar.PM && h==0) h=12;
    
    switch (mode)
    {
    default: 
      eTime.setValue(String.format("%02d:%02d",h,m));
      eAmPm.setLabel("");
      break;
    case 1: 
      eTime.setValue(String.format("%02d:%02d.%02d",h,m,s));
      eAmPm.setLabel("");
      break;
    case 2: 
      eTime.setValue(String.format("%02d:%02d",h,m));
      eAmPm.setLabel(p==Calendar.AM?"AM":"PM");
      break;
    case 3: 
      eTime.setValue(String.format("%02d:%02d.%02d",h,m,s));
      eAmPm.setLabel(p==Calendar.AM?"AM":"PM");
      break;
    }

    DateFormat df = DateFormat.getDateInstance(DateFormat.FULL,Locale.US);
    df.setCalendar(now);
    eDate.setLabel(df.format(now.getTime()).toUpperCase());

    boolean daylight = now.get(Calendar.DST_OFFSET)>0;
    eTimeZone.setLabel(now.getTimeZone().getDisplayName(daylight,TimeZone.LONG,Locale.US).toUpperCase());
    
    int zo = now.get(Calendar.ZONE_OFFSET)/60000;
    eGmtOffs.setLabel(String.format("GMT%s%02d:%02d",zo>0?"+":"\u2013",(int)Math.abs(zo)/60,Math.abs(zo)%60));

    boolean home = Calendar.getInstance().getTimeZone().getDisplayName().equals(timezone.getDisplayName());
    eHome.setSelected(home);
    
    try
    {
      LoadStatistics lds = getScreen().getLoadStatistics();
      int ld  = lds.getLoad();
      int fps = lds.getEventsPerPeriod();
      eGuiFps.setValue(String.format("%02d",fps));
      eGuiLd.setValue(String.format("%03d",ld));    
      eCpuLd.setLabel(String.format("%03d",Math.round(ld*fps/25)));
    }
    catch (RemoteException e)
    {
      eGuiFps.setValue("--");
      eGuiLd.setValue("----");    
      eCpuLd.setLabel("---");
    }
  }  
  
  protected boolean onWeTab()
  {
    return "wetab".equals(LCARS.getArg("--device="));
  }
  
  /**
   * Returns the current clock mode
   * @return one of the <code>CLOCK_XXX</code> constants
   */
  public int getClockMode()
  {
    return CLOCK_WORLD;
  }
  
  /**
   * Sets the clock mode
   * @param mode one of the <code>CLOCK_XXX</code> constants
   */
  public void setClockMode(int mode)
  {
    // TODO: implement WorldClockPanel.setClockMode
  }

  /**
   * Fills the display elements according to the currently selected mode.
   */
  protected void fill()
  {
    if (getClockMode()==CLOCK_LECTURE)
    {
      eTzHead.setLabel("LECTURE UNIT");
      eTzArray.removeAll();
      eTzArray.add("DS 1   07:30 - 09:00");
      eTzArray.add("DS 2   09:15 - 10:45");
      eTzArray.add("DS 3   11:30 - 13:00");
      eTzCount.setLabel("007");      
    }
    else /* CLOCK_WORLD */
    {
      eTzHead.setLabel("TIME ZONE");
      eTzArray.removeAll();
      ArrayList<String> tz = new ArrayList<String>();
      String[] tzid = TimeZone.getAvailableIDs();
      for (String id : tzid)
      {
        String tzname = TimeZone.getTimeZone(id).getDisplayName(Locale.US);
        tzname = tzname.toUpperCase();
        tzname = tzname.replace("TIME","").trim();
        tzname = tzname.replace("IS.","ISLANDS").trim();
        if (tzname.length()>35) tzname = tzname.substring(0,33)+" ...";
        if (!tzname.startsWith("GMT") && !tz.contains(tzname))
        {
          tz.add(tzname);
          EElement e = eTzArray.add(tzname);
          e.setData(id);
        }
      }
      eTzCount.setLabel(String.format("%03d",tz.size()));
    }
  }
  
  // -- Main method --
  
  /**
   * Runs the world clock panel.
   * 
   * @param args
   *          The command line arguments, see {@link LCARS#main(String[])}.
   */
  public static void main(String[] args)
  {
    args = LCARS.setArg(args,"--panel=",WorldClockPanel.class.getName());
    args = LCARS.setArg(args,"--PADD",null);
    LCARS.main(args);
  }
  
}
