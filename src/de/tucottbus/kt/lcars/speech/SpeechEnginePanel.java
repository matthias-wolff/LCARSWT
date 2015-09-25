package de.tucottbus.kt.lcars.speech;

import java.awt.Rectangle;
import java.awt.geom.Area;
import java.rmi.RemoteException;

import de.tucottbus.kt.lcars.IScreen;
import de.tucottbus.kt.lcars.LCARS;
import de.tucottbus.kt.lcars.Panel;
import de.tucottbus.kt.lcars.contributors.ESignalDisplay;
import de.tucottbus.kt.lcars.elements.EElbo;
import de.tucottbus.kt.lcars.elements.EElement;
import de.tucottbus.kt.lcars.elements.EEvent;
import de.tucottbus.kt.lcars.elements.EEventListenerAdapter;
import de.tucottbus.kt.lcars.elements.ELabel;
import de.tucottbus.kt.lcars.elements.ERect;
import de.tucottbus.kt.lcars.elements.EValue;
import de.tucottbus.kt.lcars.feedback.UserFeedbackPlayer;
import de.tucottbus.kt.lcars.logging.Log;
import de.tucottbus.kt.lcars.speech.events.LevelEvent;
import de.tucottbus.kt.lcars.speech.events.PostprocEvent;
import de.tucottbus.kt.lcars.speech.events.RecognitionEvent;
import de.tucottbus.kt.lcars.speech.events.SpeechEvent;
import de.tucottbus.kt.lcars.swt.SWTColor;
import de.tucottbus.kt.lcars.util.Range;

public class SpeechEnginePanel extends Panel
{
  // GUI elements
  private   ERect           eLcars;
  private   EElbo           eElboU;
  private   EElbo           eElboL;
  private   ERect           eModeUAuto;
  private   ERect           eSrvName;
  private   ERect           eSrvPort;
  private   ERect           eModeU;
  private   ERect           eModeL;
  private   ERect           eLight;
  private   ERect           eDim;
  private   ERect           eLock;
  private   ERect           ePanic;
  private   ERect           eAudFdbk;
  private   ERect           eVisFdbk;
  private   EValue          eGuiLd;
  
  // Speech input display
  private   ESignalDisplay  cSpeechSig;
  private   ESpeechInput    cSpeechIo;
  private   ESpeechPostproc cSpeechPostproc;
  private   ERect           eVisFdbkMonitor;
  private   EValue          eVad;
  private   EValue          eRec;
  
  // Misc
  private   int             modeU;
  private   int             redAlertCtr;
  private   SWTColor           cRed = LCARS.getColor(LCARS.CS_REDALERT,LCARS.EC_PRIMARY|LCARS.ES_SELECTED);
  
  public SpeechEnginePanel(IScreen screen)
  {
    super(screen);
  }

  @Override
  protected Area getShape()
  {
    return new Area(new Rectangle(0,0,1920,1080));
  }
  
  @Override
  public void init()
  {
    super.init();
    
    setTitle(guessName(getClass()));
    
    // The GUI elements
    EElement e;
    eLcars = new ERect(this,23, 21,208,148,LCARS.EC_ELBOUP|LCARS.ES_LABEL_SE|LCARS.ES_SELECTED,"LCARS");
    eLcars.addEEventListener(new EEventListenerAdapter()
    {
      @Override
      public void touchDown(EEvent ee)
      {
        panelSelectionDialog();
      }
    });
    add(eLcars);

    String pnlHost = LCARS.getHostName().toUpperCase();
    eElboU = new EElbo(this,23,172,464,142,LCARS.EC_ELBOUP|LCARS.ES_SHAPE_SW|LCARS.ES_LABEL_NE|LCARS.ES_STATIC,pnlHost);
    eElboU.setArmWidths(208,38); eElboU.setArcWidths(170,90);
    add(eElboU);

    eElboL = new EElbo(this,23,317,464,202,LCARS.EC_ELBOLO|LCARS.ES_SHAPE_NW|LCARS.ES_LABEL_SE|LCARS.ES_STATIC,"UNK");
    eElboL.setArmWidths(208,38); eElboL.setArcWidths(170,90);
    add(eElboL);

    eLight = new ERect(this,1828,276,69,38,LCARS.EC_PRIMARY|LCARS.ES_LABEL_E,"LIGHT");
    eDim = new ERect(this,1828,317,69,38,LCARS.EC_SECONDARY|LCARS.ES_LABEL_E,"DIM");
    add(eLight);
    add(eDim);
    setDimContols(eLight,eDim);
    
    eModeU  = new ERect(this,1486,276,159,38,LCARS.EC_SECONDARY|LCARS.ES_LABEL_W|LCARS.ES_SELECTED,"MODE SELECT");
    eModeU.addEEventListener(new EEventListenerAdapter(){
      @Override
      public void touchDown(EEvent ee)
      {
        modeU = (modeU+1)%2;
        switchModeU(modeU);
        eModeUAuto.setBlinking(false);
      }
    });
    add(eModeU);
    
    eModeUAuto = new ERect(this,1486,317,338,38,LCARS.EC_SECONDARY|LCARS.ES_LABEL_E|LCARS.ES_SELECTED|LCARS.ES_BLINKING,"MODE AUTO");
    eModeUAuto.addEEventListener(new EEventListenerAdapter()
    {
      @Override
      public void touchDown(EEvent ee)
      {
        eModeUAuto.setBlinking(!eModeUAuto.isBlinking());
      }
    });
    add(eModeUAuto);
        
    eSrvName = new ERect(this,23,522,208,232,LCARS.EC_SECONDARY|LCARS.ES_LABEL_SE|LCARS.ES_STATIC,"UNK");
    add(eSrvName);
    
    eSrvPort = new ERect(this,23,757,208, 47,LCARS.EC_SECONDARY|LCARS.ES_LABEL_E |LCARS.ES_STATIC,"UNK");
    add(eSrvPort);

    eModeL = new ERect(this,23,807,208,250,LCARS.EC_SECONDARY|LCARS.ES_LABEL_SE|LCARS.ES_SELECTED,"MODE\nSELECT");
    add(eModeL);
    
    eLock = new ERect(this,490,276,151,38,LCARS.EC_PRIMARY|LCARS.ES_LABEL_E,"LOCK");
    eLock.addEEventListener(new EEventListenerAdapter()
    {
      public void touchDown(EEvent ee)
      {
        boolean lock = !ee.el.isSelected();
        ee.el.setSelected(lock);
        ee.el.setBlinking(lock);
      }
    });
    add(eLock);

    ePanic = new ERect(this,644,276,131,38,LCARS.EC_PRIMARY|LCARS.ES_LABEL_E,"PANIC");
    ePanic.addEEventListener(new EEventListenerAdapter(){
      @Override
      public void touchDown(EEvent ee)
      {
        redAlertCtr = 150;
        setColorScheme(ePanic.isSelected()?LCARS.CS_REDALERT:LCARS.CS_MULTIDISP);
        setTitle("RESTARTING");
        invalidate();
        ISpeechEngine spe = getSpeechEngine();
        if (spe!=null)
        {
          spe.stop();
          spe.start();
        }
      }
    });
    add(ePanic);

    e = new EElbo(this,778,276,705,38,LCARS.ES_SHAPE_NW|LCARS.EC_ELBOUP|LCARS.ES_STATIC,null);
    ((EElbo)e).setArmWidths(289,15);
    ((EElbo)e).setArcWidths(1,50);
    add(e);
    
    e = new EElbo(this,490,317,993,38,LCARS.ES_SHAPE_SW|LCARS.EC_ELBOUP|LCARS.ES_SELECTED|LCARS.ES_STATIC,null);
    ((EElbo)e).setArmWidths(577,15);
    ((EElbo)e).setArcWidths(1,50);
    add(e);
    
    add(new ERect(this,1087,359,396, 8,LCARS.EC_ELBOUP|LCARS.ES_SELECTED|LCARS.ES_STATIC,null));

    eAudFdbk = new ERect(this,1073,297,136,38,LCARS.EC_PRIMARY|LCARS.ES_LABEL_W|LCARS.ES_SELECTED|LCARS.ES_RECT_RND_W,"AUD FDBK");
    eAudFdbk.addEEventListener(new EEventListenerAdapter()
    {
      @Override
      public void touchDown(EEvent ee)
      {
        ISpeechEngine se = getSpeechEngine();
        if (se!=null)
        {
          int mode = se.getUserFeedbackMode();
          if ((mode&UserFeedbackPlayer.AUDITORY)!=0)
            mode &= ~UserFeedbackPlayer.AUDITORY;
          else
            mode |= UserFeedbackPlayer.AUDITORY;
          se.setUserFeedbackMode(mode);
        }
      }
    });
    add(eAudFdbk);

    eVisFdbk = new ERect(this,1212,297,136,38,LCARS.EC_PRIMARY|LCARS.ES_LABEL_W|LCARS.ES_SELECTED,"VIS FDBK");
    eVisFdbk.addEEventListener(new EEventListenerAdapter()
    {
      @Override
      public void touchDown(EEvent ee)
      {
        ISpeechEngine se = getSpeechEngine();
        if (se!=null)
        {
          int mode = se.getUserFeedbackMode();
          if ((mode&UserFeedbackPlayer.VISUAL)!=0)
            mode &= ~UserFeedbackPlayer.VISUAL;
          else
            mode |= UserFeedbackPlayer.VISUAL;
          se.setUserFeedbackMode(mode);
        }
      }
    });
    add(eVisFdbk);
    
    eGuiLd = new EValue(this,1648,276,176,38,LCARS.EC_ELBOUP|LCARS.ES_LABEL_E|LCARS.ES_STATIC,"GUI LD");
    eGuiLd.setValueWidth(54);
    eGuiLd.setValue("000");
    add(eGuiLd);

    // The speech signal display (upper sub-panel)
    cSpeechSig = new ESignalDisplay(489,159,320,3,40,200,ESignalDisplay.MODE_CONTINUOUS);
    cSpeechSig.setLocked(true);
    cSpeechSig.addToPanel(this);

    // The speech I/O element group (upper sub panel)
    cSpeechIo = new ESpeechInput(489,159,930,200);
    
    // The recognizer element group (upper sub panel)
    add(new ELabel(this,264,55,200,0,LCARS.EC_TEXT|LCARS.EF_HEAD2|LCARS.ES_LABEL_NE,"SPEECH INPUT"));
    e = new EElbo(this,1486,122,411,105,LCARS.ES_SHAPE_NW|LCARS.EC_ELBOUP|LCARS.ES_STATIC|LCARS.ES_LABEL_NE,"RECOGNIZER");
    ((EElbo)e).setArmWidths(159,58); ((EElbo)e).setArcWidths(100,52);
    add(e);

    eVisFdbkMonitor = new ERect(this,1486,230,159,43,LCARS.EC_ELBOUP|LCARS.ES_LABEL_W|LCARS.ES_STATIC,"");
    add(eVisFdbkMonitor);
    ISpeechEngine spe = getSpeechEngine();
    if (spe!=null)
      spe.addUserFeedbackPlayer(new UserFeedbackPlayer(UserFeedbackPlayer.VISUAL)
      {
        @Override
        public void writeColor(SWTColor color)
        {
          eVisFdbkMonitor.setColor(color);
        }
      });
    
    eVad = new EValue(this,1649,184,248,43,LCARS.EC_ELBOUP|LCARS.ES_RECT_RND_W|LCARS.ES_LABEL_E,"VAD");
    eVad.setValueWidth(125); eVad.setValue("OFFLINE");
    eVad.addEEventListener(new EEventListenerAdapter(){
      @Override
      public void touchDown(EEvent ee)
      {
        onVad();
      }
    });
    add(eVad);

    eRec = new EValue(this,1649,230,248,43,LCARS.EC_ELBOUP|LCARS.ES_RECT_RND_W|LCARS.ES_LABEL_E,"RECOG");
    eRec.setValueWidth(125); eRec.setValue("OFFLINE");
    eRec.addEEventListener(new EEventListenerAdapter(){
      @Override
      public void touchDown(EEvent ee)
      {
        ISpeechEngine spe = getSpeechEngine();
        if (spe!=null)
        {
          if (spe.getListenMode()==0)
            spe.setListenMode(1);
          else
            spe.setListenMode(0);
        }
      }
    });
    add(eRec);
    
    // The speech recognition result display (lower sub panel)
    cSpeechPostproc = new ESpeechPostproc(294,408,212,7,569);
    cSpeechPostproc.addToPanel(this);
  }

  public boolean getModeUAuto()
  {
    return eModeUAuto.isBlinking();
  }
  
  public void switchModeU(int mode)
  {
    switch (mode)
    {
    case 0:
      cSpeechIo.removeFromPanel();
      cSpeechSig.addToPanel(this);
      eLock.setStatic(false);
      eLock.setColorStyle(LCARS.EC_PRIMARY);
      break;
    case 1:
      cSpeechSig.removeFromPanel();
      eLock.setStatic(true);
      eLock.setColorStyle(LCARS.EC_ELBOUP);
      cSpeechIo.addToPanel(this);
      break;
    }
    this.modeU = mode;
  }
  
  // -- Speech --
  
  @Override
  public void speechEvent(SpeechEvent event)
  {
    if (event==null) return;
    
    // Speech input level events
    if (event instanceof LevelEvent)
    {
      int  amp = ((LevelEvent)event).getAmp();
      long frm = event.frame;
      
      // Feed signal display
      if (!eLock.isSelected())
        synchronized (cSpeechSig)
        {
          int   smp = cSpeechSig.addSample(new Range(-amp,amp),
              event.spe.getListenMode()<0
                ? SWTColor.GRAY
                : (event.spe.getVoiceActivity()
                    ? cRed
                    : null));
          cSpeechSig.getSampleElement(smp).setData(new Long(frm));
        }
      
      // Feed speech I/O contributor
      cSpeechIo.setLevel(((LevelEvent)event).level);

      return;
    }
    
    // Speech recognition events
    if (event instanceof RecognitionEvent)
    {
      RecognitionEvent re = (RecognitionEvent)event;
      if (re.incremenral==true)
      {
        // Incremental result
        cSpeechIo.setRecResult(re);
      }
      else
      {
        // Final result
        if (eModeUAuto.isBlinking()) switchModeU(1);
        cSpeechIo.setRecResult(re);
      }
    }
    
    // Speech recognition post-processing events
    if (event instanceof PostprocEvent)
    {
      PostprocEvent pe = (PostprocEvent)event;
      cSpeechPostproc.setPostprocResult(pe);
      Log.info("Post-processing event ("+pe.frames.size()+" frames)");
    }
  }

  /**
   * Handler for the VAD button.
   */
  protected void onVad()
  {
    ISpeechEngine spe = getSpeechEngine();
    if (spe==null) return;
    spe.setListenMode(spe.getListenMode()<0?0:-1);
    //if (spe.isStarted()) spe.stop(); else spe.start();
  }
  
  // -- Overrides --

  @Override
  public void fps2()
  {
    if (eGuiLd!=null)
    {
      String s = "----";
      try
      {
        s = getScreen().getLoadStatistics().getLoad()+"";
      }
      catch (RemoteException e) {}
      while (s.length()<3) s="0"+s;
      eGuiLd.setValue(s);
    }
  }

  @Override
  public void fps10()
  {
    // Reflect voice activity
    ISpeechEngine spe = getSpeechEngine();
    int    vadCs = LCARS.EC_ELBOUP;
    int    recCs = LCARS.EC_ELBOUP;
    String vadLb = "UNAVAIL";
    String recLb = "UNAVAIL";
    if (spe!=null)
    {
      vadCs = spe.getVoiceActivity()?LCARS.EC_PRIMARY:LCARS.EC_SECONDARY;
      recCs = spe.getListenMode()>0 ?LCARS.EC_PRIMARY:LCARS.EC_SECONDARY;
      vadLb = spe.getVoiceActivity()?"SPEECH":"SILENCE";
      if (spe.getListenMode()<0) vadLb="OFFLINE";
      recLb = spe.getListenMode()!=0?"ONLINE":"SLEEPING";
      if (spe.hasFailed())
      {
        vadLb = "OFFLINE";
        recLb = "MALFUNCTION";
      }
      else if (!spe.isStarted())
      {
        vadLb = "OFFLINE";
        recLb = "OFFLINE";
      }
      else if (spe.isBusy())
        recLb = "BUSY";
    }

    eVad.setStatic    (spe==null||spe.hasFailed());
    eVad.setBlinking  (spe!=null&&!spe.isStarted());
    eVad.setColor     (spe!=null&&!spe.isStarted()?cRed:null);
    eVad.setColorStyle(spe!=null&&!spe.hasFailed()?vadCs:LCARS.EC_ELBOUP);
    eVad.setSelected  (spe!=null&&!spe.hasFailed()&&spe.isStarted()&&spe.getVoiceActivity());
    eVad.setValue     (vadLb);
    
    eRec.setStatic    (spe==null||spe.hasFailed()||!spe.isStarted());
    eRec.setColor     ((spe==null||spe.hasFailed()||spe.isBusy())?cRed:null);
    eRec.setColorStyle(spe!=null&&!spe.hasFailed()&&spe.isStarted()?recCs:LCARS.EC_ELBOUP);
    eRec.setSelected  (spe!=null&&!spe.hasFailed()&&spe.isStarted()&&spe.getListenMode()>0);
    eRec.setValue     (recLb);
    
    eAudFdbk.setSelected((spe.getUserFeedbackMode()&UserFeedbackPlayer.AUDITORY)!=0);
    eVisFdbk.setSelected((spe.getUserFeedbackMode()&UserFeedbackPlayer.VISUAL)!=0);
    
    if (spe!=null && !spe.isStarted())
      cSpeechSig.reset();
  }
  
  @Override
  public void fps25()
  {
    // Panic...
    if (redAlertCtr>=25)
    {
      if ((redAlertCtr)%25==0)
      {
        int colorScheme = ((redAlertCtr)%50==0)?LCARS.CS_REDALERT:LCARS.CS_MULTIDISP;
        setColorScheme(colorScheme);
      }
      float alpha = (float)Math.pow(((redAlertCtr-1)%25)/24.f+0.5f,0.5);
      redAlertCtr--;
      setAlpha(alpha);
    }
    if (redAlertCtr<25 && redAlertCtr>0)
    {
      redAlertCtr=0;
      setAlpha(1.f);
      setTitle(guessName(getClass()).toUpperCase());
    }
  }

  // -- Getters and setters --

  /**
   * Returns the LCARS button.
   */
  protected ERect getELcars()
  {
    return eLcars;
  }

  /**
   * Returns the VAD button
   */
  protected EValue getEVad()
  {
    return eVad;
  }


  /**
   * Returns the lower elbo element.
   */
  protected EElbo getEElboL()
  {
    return eElboL;
  }

  /**
   * Returns the lower mode select button. 
   */
  protected ERect getEModeL()
  {
    return eModeL;
  }

  /**
   * Return the recognition post-processing element group.
   * @return
   */
  protected ESpeechPostproc getCSpeechPostproc()
  {
    return cSpeechPostproc;
  }

  // -- Main method --
  
  /**
   * Runs the speech engine panel.
   * 
   * @param args
   *          The command line arguments, see {@link LCARS#main(String[])}.
   */
  public static void main(String[] args)
  {
    args = LCARS.setArg(args,"--panel=",SpeechEnginePanel.class.getName());
    LCARS.main(args);
  }

}
