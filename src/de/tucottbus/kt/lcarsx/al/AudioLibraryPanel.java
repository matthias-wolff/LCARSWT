package de.tucottbus.kt.lcarsx.al;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.rmi.RemoteException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;
import java.util.Map;

import javax.sound.sampled.AudioSystem;

import de.tucottbus.kt.lcars.IScreen;
import de.tucottbus.kt.lcars.LCARS;
import de.tucottbus.kt.lcars.MainPanel;
import de.tucottbus.kt.lcars.Panel;
import de.tucottbus.kt.lcars.contributors.EBrace;
import de.tucottbus.kt.lcars.contributors.EElementArray;
import de.tucottbus.kt.lcars.contributors.ElementContributor;
import de.tucottbus.kt.lcars.elements.EElbo;
import de.tucottbus.kt.lcars.elements.EElement;
import de.tucottbus.kt.lcars.elements.EEvent;
import de.tucottbus.kt.lcars.elements.EEventListenerAdapter;
import de.tucottbus.kt.lcars.elements.ELabel;
import de.tucottbus.kt.lcars.elements.ERect;
import de.tucottbus.kt.lcars.elements.EValue;
import de.tucottbus.kt.lcars.logging.Log;
import de.tucottbus.kt.lcars.swt.ColorMeta;
import de.tucottbus.kt.lcars.util.LoadStatistics;

/**
 * EXPERIMENTAL, MP3 player.
 * 
 * @author Matthias Wolff
 */
public class AudioLibraryPanel extends MainPanel implements IAudioPlayerEventListener
{ 
   private EValue eTitle;
   private EValue eTimecode;
   private ERect  eAuthor;
   private ERect  eTrackCurrent;
   private ERect  eTrackNext;
   private ERect  eTrackExclude;
   private ERect  eControlMax;
   private ERect  ePause;
   private ERect  eStop;
   private ELabel eGuiLd;
   
   // Controllers
   private EGainSlider eGain;
   
   // Elements in mode 0 (browse library) 
   private EBrace eBrace1;
   private EBrace eBrace2;
   private EBrace eBrace3;
   
   // Elements in mode 1 (track info)
   private ETrackSelector eTracks;
   private ELevelsDisplay eDisplay;
   private ETrackInfo     eInfo;

   private int trackMode  = 0;
   private int eBrace1Top = -1;
   private int eBrace2Top = -1;
   
  /**
   * Creates a new MP3 player panel
   * 
   * @param screen
   *          The screen to display the panel on.
   */
  public AudioLibraryPanel(IScreen screen)
  {
    super(screen);
  }

  @Override
  public void init()
  {
    setTitleLabel(new ELabel(this,298,23,202,46,LCARS.EC_HEADLINE|LCARS.EF_HEAD2|LCARS.ES_LABEL_W,null));
    super.init();
    setColorScheme(LCARS.CS_DATABASE);
    
    ColorMeta  cOutline = new ColorMeta(1f,1f,1f,0.25f);
    ERect  eRect;
    EElbo  eElbo;
    EElement e;
    
    // Frame
    add(new ERect(this,1851,23,46,46,LCARS.EC_ELBOUP|LCARS.ES_STATIC|LCARS.ES_RECT_RND_E,null));

    eTitle = new EValue(this,506,23,1343,46,LCARS.EC_PRIMARY|LCARS.ES_STATIC,"");
    eTitle.setValueMargin(0);
    add(eTitle);
    
    eElbo = new EElbo(this,23,23,268,146,LCARS.EC_ELBOUP|LCARS.ES_LABEL_SE,"LCARS");
    eElbo.setArmWidths(192,46);
    eElbo.addEEventListener(new EEventListenerAdapter()
    {
      @Override
      public void touchDown(EEvent ee)
      {
        panelSelectionDialog();
      }
    });
    add(eElbo);

    eAuthor = new ERect(this,23,174,192,89,LCARS.EC_SECONDARY|LCARS.ES_SELECTED|LCARS.ES_LABEL_SE,"AUTHOR");
    eAuthor.addEEventListener(new EEventListenerAdapter()
    {
      @Override
      public void touchDown(EEvent ee)
      {
        setMode(0);
        fillArtists();
      }
    });
    add(eAuthor);

    e = new ERect(this,23,268,191,148,LCARS.EC_ELBOUP|LCARS.ES_STATIC|LCARS.ES_OUTLINE,null);
    e.setColor(cOutline); add(e);
    e = new ELabel(this,23,268,190,147,LCARS.EC_ELBOUP|LCARS.ES_STATIC|LCARS.ES_LABEL_SE,"LIBRARY\nCONTROLS");
    e.setColor(cOutline); add(e);
    
    eGuiLd  = new ELabel(this,23,420,192,38,LCARS.ES_STATIC|LCARS.ES_LABEL_E,null);
    eGuiLd.setColor(cOutline);
    add(eGuiLd);

    add(new ERect(this,23,461,192,66,LCARS.EC_ELBOLO|LCARS.ES_LABEL_SE|LCARS.ES_STATIC,"TRACK SELECT"));
    
    eTrackCurrent = new ERect(this,23,530,192,76,LCARS.EC_PRIMARY|LCARS.ES_LABEL_SE,"CURRENT");
    eTrackCurrent.addEEventListener(new EEventListenerAdapter()
    {
      @Override
      public void touchDown(EEvent ee)
      {
        trackMode = 0;
      }
    });
    add(eTrackCurrent);

    eTrackNext = new ERect(this,23,609,192,76,LCARS.EC_PRIMARY|LCARS.ES_LABEL_SE,"NEXT");
    eTrackNext.addEEventListener(new EEventListenerAdapter()
    {
      @Override
      public void touchDown(EEvent ee)
      {
        trackMode = 1;
      }
    });
    add(eTrackNext);

    eTrackExclude = new ERect(this,23,688,192,76,LCARS.EC_PRIMARY|LCARS.ES_LABEL_SE,"EXCLUDE");
    eTrackExclude.addEEventListener(new EEventListenerAdapter()
    {
      @Override
      public void touchDown(EEvent ee)
      {
        trackMode = 2;
      }
    });
    add(eTrackExclude);
    
    eRect = new ERect(this,23,769,192,144,LCARS.EC_SECONDARY|LCARS.ES_LABEL_SE,"MODE\nSELECT");
    eRect.addEEventListener(new EEventListenerAdapter()
    {
      @Override
      public void touchDown(EEvent ee)
      {
        setMode(-1);
      }
    });
    add(eRect);
    
    eElbo = new EElbo(this,23,918,435,139,LCARS.EC_ELBOUP|LCARS.ES_STATIC|LCARS.ES_SHAPE_SW|LCARS.ES_LABEL_SE,"CONTROL SELECT");
    eElbo.setArmWidths(192,66);
    add(eElbo);

    eRect = new ERect(this,461,991,731,66,LCARS.ES_STATIC,null);
    eRect.setColor(new ColorMeta(0x333333));
    add(eRect);
    eGain = new EGainSlider(462,992,731,66);
    eGain.addToPanel(this);

    eControlMax = new ERect(this,1195,991,96,66,LCARS.EC_ELBOLO|LCARS.ES_STATIC|LCARS.ES_LABEL_SW,null);
    add(eControlMax);
    
    ePause = new ERect(this,1296,991,122,66,LCARS.EC_PRIMARY|LCARS.ES_LABEL_SE,"PAUSE");
    ePause.addEEventListener(new EEventListenerAdapter()
    {
      @Override
      public void touchDown(EEvent ee)
      {
        if (AudioPlayer.getInstance().isPaused())
          AudioPlayer.getInstance().play();
        else
          AudioPlayer.getInstance().pause();
      }
    });
    add(ePause);
    
    eStop = new ERect(this,1423,991,122,66,LCARS.EC_PRIMARY|LCARS.ES_LABEL_SE,"STOP");
    eStop.addEEventListener(new EEventListenerAdapter()
    {
      @Override
      public void touchDown(EEvent ee)
      {
        AudioPlayer.getInstance().stop();
      }
    });
    add(eStop);
    
    eTimecode = new EValue(this,1550,991,278,66,LCARS.EC_PRIMARY|LCARS.ES_STATIC|LCARS.ES_VALUE_W|LCARS.ES_LABEL_SE,"TIME INDEX");
    eTimecode.setValue("00:00.0"); eTimecode.setValueWidth(150); eTimecode.setValueMargin(0);
    add(eTimecode);
    
    add(new ERect(this,1831,991,66,66,LCARS.EC_ELBOUP|LCARS.ES_STATIC|LCARS.ES_RECT_RND_E,null));

    // Elements in mode 0: browse library
    eBrace1 = new EBrace(395,159,ERect.class,new Dimension(388,59),12,LCARS.EC_PRIMARY|LCARS.ES_LABEL_E|LCARS.ES_RECT_RND_W);
    eBrace1.setBraceSize(108,48); eBrace1.setTipSize(57,89);
    eBrace1.addEEventListener(new EEventListenerAdapter()
    {
      @Override
      public void touchDown(EEvent ee)
      {
        selectArtist((File)ee.el.getData());
      }
    });

    eBrace2 = new EBrace(941,159,ERect.class,new Dimension(388,59),12,LCARS.EC_PRIMARY|LCARS.ES_LABEL_E|LCARS.ES_RECT_RND_W);
    eBrace2.setBraceSize(108,48); eBrace2.setTipSize(37,59);
    eBrace2.addEEventListener(new EEventListenerAdapter()
    {
      @Override
      public void touchDown(EEvent ee)
      {
        selectAlbum((File)ee.el.getData());
      }
    });

    eBrace3 = new EBrace(1487,159,ERect.class,new Dimension(388,59),12,LCARS.EC_PRIMARY|LCARS.ES_LABEL_E|LCARS.ES_RECT_RND_W);
    eBrace3.setBraceSize(108,48); eBrace3.setTipSize(37,59);
    eBrace3.addEEventListener(new EEventListenerAdapter()
    {
      @Override
      public void touchDown(EEvent ee)
      {
        eBrace3.setLock(true);
        selectTrack((AudioTrack)ee.el.getData());
      }
    });

    // Elements in mode 1: track info
    eTracks = new ETrackSelector(266,129);
    eDisplay = new ELevelsDisplay(765,515,880,125,25);
    eDisplay.setBarColor (new ColorMeta(0x9999FF));
    eDisplay.setGridColor(new ColorMeta(0xDDB18E));
    eInfo = new ETrackInfo(1230,515);
    
    // Initialize
    fillArtists();
    invalidate();
  }

  @Override
  public void stop()
  {
    AudioPlayer.getInstance().removeEventListener(eInfo);
    AudioPlayer.getInstance().removeEventListener(this);
    super.stop();
  }
  
  @Override
  public void start()
  {
    super.start();
    AudioPlayer.getInstance().addEventListener(this);
    AudioPlayer.getInstance().addEventListener(eInfo);
  }

  @Override
  protected void fps2()
  {
    super.fps2();
 
    if (eBrace1Top>=0 && eBrace1Top!=eBrace1.getFirstVisibleItemIndex())
      selectArtist(null);
    
    if (eBrace2Top>=0 && eBrace2Top!=eBrace2.getFirstVisibleItemIndex())
      selectAlbum(null);
    
    // Display current and next track
    EElementArray ea = getPlaylistElementArray();
    if (ea!=null)      
      for (int i=0; i<ea.getItemCount(); i++)
      {
        EElement   e  = ea.getItemElement(i);
        if (!(e.getData() instanceof AudioTrack)) continue;
        AudioTrack at = (AudioTrack)e.getData();
        if (AudioPlayer.getInstance()!=null && AudioPlayer.getInstance().isPlaying())
        {
          if (AudioPlayer.getInstance().getCurrentTrack()==at && at.isExcluded())
            AudioPlayer.getInstance().stop();
          if (AudioPlayer.getInstance().getNextTrack()==at && at.isExcluded())
            AudioPlayer.getInstance().setNextTrack(null);
          
          e.setSelected(AudioPlayer.getInstance().getCurrentTrack()==at);
          e.setBlinking(AudioPlayer.getInstance().getNextTrack()   ==at);
          
          // - Automatically set next track
          if (AudioPlayer.getInstance().getCurrentTrack()==at && AudioPlayer.getInstance().getNextTrack()==null)
            for (int j=i+1; j<ea.getItemCount(); j++)
            {
              Object data = ea.getItemElement(j).getData();
              if (!(data instanceof AudioTrack)) continue;              
              AudioTrack at2 = (AudioTrack)data;
              if (!at2.isExcluded())
              {
                AudioPlayer.getInstance().setNextTrack(at2);
                break;
              }
            }
        }
        else
        {
          e.setSelected(false);
          e.setBlinking(false);
        }
        e.setAlpha(at.isExcluded()?0.33f:1f);
      }
  }

  @Override
  protected void fps10()
  {
    LoadStatistics ls1 = getLoadStatistics();
    String s = String.format("%03d-%02d",ls1.getLoad(),ls1.getEventsPerPeriod());
    try
    {
      LoadStatistics ls2 = getScreen().getLoadStatistics();
      s += String.format("/%03d-%02d",ls2.getLoad(),ls2.getEventsPerPeriod());
    }
    catch (RemoteException e)
    {
      e.printStackTrace();
    }
    eGuiLd.setLabel(s);
  }

  @Override
  protected void fps25()
  {
    // Display track select mode
    if (trackMode<0 || trackMode>2) trackMode = 0;
    eTrackCurrent.setSelected(trackMode==0);
    eTrackNext   .setSelected(trackMode==1);
    eTrackExclude.setSelected(trackMode==2);

    AudioPlayer player = AudioPlayer.getInstance();
    AudioTrack track = player.getCurrentTrack();
   
    // Display current player status
    if (player.isPlaying())
    {
      eTitle.setLabel((""+track.getArtist()).toUpperCase());
      if (getMode()==0)
        eTitle.setValue((""+track.getTitle()).toUpperCase());
      else
        eTitle.setValue((""+track.getAlbum()).toUpperCase());

      double sec = player.getMediaTime();
      int    min = (int)sec/60;
      String s   = String.format(Locale.ENGLISH,"%02d:%04.1f",min,sec-min*60);
      eTimecode.setValue(s);
      
      ePause.setDisabled(false);
      ePause.setBlinking(player.isPaused());
      ePause.setColorStyle(player.isPaused()?LCARS.EC_SECONDARY:LCARS.EC_PRIMARY);
      eStop.setDisabled(false);
    }
    else
    {
      eTitle.setLabel("");
      eTitle.setValue("");
      eTimecode.setValue("00:00.0");
      ePause.setDisabled(true);
      ePause.setBlinking(false);
      ePause.setColorStyle(LCARS.EC_PRIMARY);
      eStop.setDisabled(true);
    }

    // Feed audio visualization
    if (eDisplay.isDisplayed())
    {
      eDisplay.setAudioWindow(player.getAudioWindow(),player.getMediaTime());
      if (player.isPlaying())
      {
        eDisplay.setTitle((track.getTitle()).toUpperCase());
        eDisplay.setDuration(track.getLength());
      }
      else
      {
        eDisplay.setTitle("");
        eDisplay.setDuration(0);
        eDisplay.setAudioWindow(null,0);
      }
    }
  }

  // -- Implementation of IAudioPlayerEventListener --

  @Override
  public void processEvent(AudioPlayerEvent event)
  {
    eGain.setControl(AudioPlayer.getInstance().getGainControl());
    switch (event.type)
    {
    case AudioPlayerEvent.STARTED:
      setMode(1);
      break;
    case AudioPlayerEvent.STOPPED:
      setMode(0);
      break;
    }
    Log.info(event.toString());
  }
  
  // -- Operations --
  
  public static File getMusicDirectory()
  {
    if (LCARS.getArg("--musiclib=")!=null)
      return new File(LCARS.getArg("--musiclib="));
    
    // Fake a music library
    Log.warn("Command line option --musiclib=<music-dir> not specified, entering demo mode");
    String tmpDir = System.getProperty("java.io.tmpdir");
    try
    {
      File f = new File(tmpDir+"/lcars-wt-music/Matthias Wolff/L2");
      f.mkdirs();
      f = new File(f.getAbsolutePath()+"/Captain's Lounge.mp3");
      if (f.exists()) f.delete();
      f.createNewFile();
      FileOutputStream fos = new FileOutputStream(f);
      InputStream is = LCARS.class.getClassLoader().getResourceAsStream("de/tucottbus/kt/lcarsx/al/resource/Captain'sLounge.mp3");
      byte[] buf = new byte[1024];
      int len;
      while ((len = is.read(buf)) > 0)
        fos.write(buf, 0, len);
      is.close();
      fos.close();
      f.deleteOnExit();
    }
    catch (Exception e)
    {
      Log.err("Error while getting music directory.", e);
    }
    return new File(tmpDir+"/lcars-wt-music");
    
    // HACK: only Windoze...
    //return new File(System.getenv("USERPROFILE")+"/Music");
  }

  protected EElementArray getPlaylistElementArray()
  {
    if (eBrace3.isDisplayed()) return eBrace3;
    if (eTracks.eTracks.isDisplayed()) return eTracks.eTracks; 
    return null;
  }
  
  protected void fillArtists()
  {
    if (getMode()!=0) return;
    eBrace1.removeAll(); eBrace1.addToPanel(this);
    eBrace2.removeAll(); eBrace2.removeFromPanel();
    eBrace3.removeAll(); eBrace3.removeFromPanel();
    File[] artists = getMusicDirectory().listFiles(new FileFilter()
    {
      @Override
      public boolean accept(File pathname)
      {
        return pathname.isDirectory();
      }
    });
    if (artists!=null)
      for (File artist : artists)
      {
        EElement el = eBrace1.add(LCARS.abbreviate(artist.getName(),40,false));
        el.setData(artist);
      }

    eBrace1.setTipPos(219);
  }
  
  protected void selectArtist(File artistDir)
  {
    if (getMode()!=0) return;
    eBrace2.removeAll(); eBrace2.addToPanel(this);
    eBrace3.removeAll(); eBrace3.removeFromPanel();
    int artistIndex = findFileInBrace(eBrace1,artistDir);
    if (artistDir==null || artistIndex<0)
    {
      for (EElement el : eBrace1.getItemElements()) el.setSelected(false);
      eBrace2.removeAll(); eBrace2.removeFromPanel();
      eBrace1Top = -1;
      eBrace2Top = -1;
      return;
    }
    
    // Select artist in brace 1
    int pageSize = eBrace1.getItemCountPerPage();
    eBrace1Top = (artistIndex/pageSize)*pageSize;
    eBrace1.setFirstVisibleItemIndex(eBrace1Top);
    eBrace1.setLock(true);
    for (int i=0; i<eBrace1.getItemCount(); i++)
      eBrace1.getItemElement(i).setSelected(i==artistIndex);
    
    // Fill brace 2
    File[] albums = artistDir.listFiles(new FileFilter()
    {
      @Override
      public boolean accept(File pathname)
      {
        return pathname.isDirectory();
      }
    });
    if (albums!=null)
      for (File album : albums)
      {
        EElement el = eBrace2.add(LCARS.abbreviate(album.getName(),40,false));
        el.setData(album);
      }

    Rectangle r = eBrace1.getItemElement(artistIndex).getBounds();
    eBrace2.setTipPos(r.y+r.height/2);
  }

  protected void selectAlbum(File albumDir)
  {
    eBrace3.removeAll();

    if (getMode()==0)
    {
      eBrace3.addToPanel(this);
      int albumIndex = findFileInBrace(eBrace2,albumDir);
      if (albumDir==null || albumIndex<0)
      {
        for (EElement el : eBrace2.getItemElements()) el.setSelected(false);
        eBrace3.removeAll(); eBrace3.removeFromPanel();
        eBrace2Top = -1;
        return;
      }
  
      // Select album in brace 2
      int pageSize = eBrace2.getItemCountPerPage();
      eBrace2Top = (albumIndex/pageSize)*pageSize;
      eBrace2.setFirstVisibleItemIndex(eBrace2Top);
      eBrace2.setLock(true);
      for (int i=0; i<eBrace2.getItemCount(); i++)
        eBrace2.getItemElement(i).setSelected(i==albumIndex);

      Rectangle r = eBrace2.getItemElement(albumIndex).getBounds();
      eBrace3.setTipPos(r.y+r.height/2);    
    }
    
    // Fill track selectors
    eTracks.eTracks.removeAll();
    File[] trackFiles = albumDir.listFiles(new FileFilter()
    {
      @Override
      public boolean accept(File pathname)
      {
        if (!pathname.isFile()) return false;
        try
        {
          AudioSystem.getAudioInputStream(pathname);
          return true;
        }
        catch (Exception e)
        {
          return false;
        }
      }
    });
    if (trackFiles!=null)
    {
      AudioTrackList trackList = new AudioTrackList();
      for (File trackFile : trackFiles)
        try
        {
          trackList.add(new AudioTrack(trackFile));
        }
        catch (Exception e)
        {
          e.printStackTrace();
        }
      Collections.sort(trackList, new Comparator<AudioTrack>()
      {
        @Override
        public int compare(AudioTrack o1, AudioTrack o2)
        {
          if (o1.getTrackNumber()!=null && o2.getTrackNumber()!=null)
            return o1.getTrackNumber().compareToIgnoreCase(o2.getTrackNumber());
          return o1.getTitle().compareToIgnoreCase(o2.getTitle());
        }
      });

      for (AudioTrack track : trackList)
      {
        String s = track.getTitle();
        if (track.getTrackNumber()!=null)
          s = track.getTrackNumber()+" - "+s;
        EElement el = eBrace3.add(LCARS.abbreviate(s,40,false));
        el.setData(track);
        eBrace3.animate();
        el = eTracks.eTracks.add(LCARS.abbreviate(s,50,false));
        el.setData(track);
      }
    }
    while (eTracks.eTracks.getItemCount()%eTracks.eTracks.getItemCountPerPage()!=0)
    {
      EElement el = eTracks.eTracks.add("");
      el.setStatic(true); el.setAlpha(0.15f);
    }
  }

  protected void selectTrack(AudioTrack track)
  {
    final AudioPlayer player = AudioPlayer.getInstance();
    switch (trackMode)
    {
    case 1:
      // Next
      track.setExcluded(false);
      if (!player.isPlaying())
        try
        {
          player.setCurrentTrack(track);
          player.play();
        }
        catch (Exception e)
        {
          Log.err("Cannot play track " + track + ".", e);
        }
      else
        player.setNextTrack(/*player.getNextTrack()==track?null:*/track);
      break;
    case 2:
      // Exclude
      track.setExcluded(!track.isExcluded());
      break;
    default:
      // Current
      track.setExcluded(false);
      player.stop();
      try
      {
        player.setCurrentTrack(track);
        player.play();
      }
      catch (Exception e)
      {
        Log.err("Cannot play track " + track + ".", e);
      }
      break;
    }
  }
  
  public int getMode()
  {
    if (eAuthor.isSelected()) return 0;
    else return 1;
  }
  
  public void setMode(int mode)
  {
    if (mode<0)
      if (eBrace1.isDisplayed()) mode=1; else mode=0;
    
    switch (mode)
    {
    case 1:
      // Mode 1: Track info
      eBrace1.removeFromPanel();
      eBrace2.removeFromPanel();
      eBrace3.removeFromPanel();
      eAuthor.setSelected(false);
      eDisplay.addToPanel(this);
      eInfo.show();
      eTracks.addToPanel(this);
      break;
      
    default:
      // Mode 0: Browse library
      eDisplay.removeFromPanel();
      eInfo.hide();
      eTracks.removeFromPanel();
      eBrace1.addToPanel(this);
      if (eBrace1Top>=0) eBrace1.setFirstVisibleItemIndex(eBrace1Top);
      if (eBrace2.getItemCount()>0)
      {
        eBrace2.addToPanel(this);
        if (eBrace2Top>=0) eBrace2.setFirstVisibleItemIndex(eBrace2Top);
      }
      else
        eBrace2.removeFromPanel();
      if (eBrace3.getItemCount()>0)
        eBrace3.addToPanel(this);
      else
        eBrace3.removeFromPanel();
      eAuthor.setSelected(true);
      break;
    }
  }
  
  /**
   * Finds the element representing a specified file or directory in a brace contributor.
   * 
   * @param eBrace
   *          The brace to find the file element in.
   * @param file
   *          The file.
   * @return The zero based index of the {@linkplain EElement element} in the brace representing the
   *         file or -1 if no such element exists.
   */
  protected int findFileInBrace(EBrace eBrace, File file)
  {
    if (eBrace==null) return -1;
    if (file  ==null) return -1;
    for (int i=0; i<eBrace.getItemCount(); i++)
    {
      EElement el = eBrace.getItemElement(i);
      if (((File)el.getData()).equals(file))
        return i;
    }
    return -1;
  }

  // -- Nested classes --
  
  protected class ETrackInfo implements IAudioPlayerEventListener
  {
    EValue        eTitle;
    EElementArray eKeys;
    EElementArray eValues;
    
    ETrackInfo(int x, int y)
    {
      eTitle = new EValue(AudioLibraryPanel.this,x+233,y-40,0,32,LCARS.ES_STATIC|LCARS.EC_TEXT,null);
      eTitle.setValueMargin(0); eTitle.setValue("TRACK TAGS");
      eTitle.setVisible(false);
      add(eTitle);
      int style = LCARS.ES_STATIC|LCARS.EF_SMALL|LCARS.EC_ELBOUP; 
      eKeys = new EElementArray(x,y,ELabel.class,new Dimension(230,18),18,1,style|LCARS.ES_LABEL_E,null);
      eValues = new EElementArray(x+240,y,ELabel.class,new Dimension(432,18),18,1,style|LCARS.ES_LABEL_W,null);      
    }
    
    void show()
    {
      eTitle.setVisible(true);
      eKeys.addToPanel(AudioLibraryPanel.this);
      eValues.addToPanel(AudioLibraryPanel.this);
      eKeys.animate(); eValues.animate();
    }
    
    void hide()
    {
      eTitle.setVisible(false);
      eKeys.removeFromPanel();
      eValues.removeFromPanel();
    }

    @Override
    public void processEvent(AudioPlayerEvent event)
    {
      switch (event.type)
      {
      case AudioPlayerEvent.TRACKSTART:
        eKeys.removeAll();
        eValues.removeAll();
        Map<String,String> props = event.track.getProperties();
        for (String key : props.keySet())
        {
          eKeys.add(key);
          eValues.add(LCARS.abbreviate(props.get(key),60,false));
        }
        break;
        
      case AudioPlayerEvent.STOPPED:  // Fall through
      case AudioPlayerEvent.TRACKEND:
        eKeys.removeAll();
        eValues.removeAll();
        break;
      }
    }
  }
  
  protected class ETrackSelector extends ElementContributor
  {
    EElementArray eTracks;
    
    ETrackSelector(int x, int y)
    {
      super(x,y);
      
      EElbo eElbo;
      EElbo ePrev;
      ERect eNext;
      EElbo eLock;

      // The left brace
      eElbo = new EElbo(null,0,0,24,130,LCARS.ES_STATIC|LCARS.EC_ELBOUP|LCARS.ES_SHAPE_NW,null);
      eElbo.setArcWidths(48,1); eElbo.setArmWidths(24,130);
      add(eElbo);
      eElbo = new EElbo(null,0,130,24,127,LCARS.ES_STATIC|LCARS.EC_ELBOUP|LCARS.ES_SHAPE_SW,null);
      eElbo.setArcWidths(48,1); eElbo.setArmWidths(24,130);
      add(eElbo);
      
      // The track element array
      Dimension dim = new Dimension(468,49);
      int style = LCARS.EC_PRIMARY|LCARS.ES_LABEL_W|LCARS.ES_RECT_RND_E;
      eTracks = new EElementArray(x+27,y,ERect.class,dim,5,3,style,null);
      eTracks.addEEventListener(new EEventListenerAdapter()
      {
        @Override
        public void touchDown(EEvent ee)
        {
          selectTrack((AudioTrack)ee.el.getData());
        }
      });
      
      // The right brace
      ePrev = new EElbo(null,1443,0,142,135,LCARS.EC_PRIMARY|LCARS.ES_SHAPE_NE|LCARS.ES_LABEL_SE,"PREV");
      ePrev.setArcWidths(48,1); ePrev.setArmWidths(142,58);
      add(ePrev);
      eNext = new ERect(null,1443,138,142,58,LCARS.EC_PRIMARY|LCARS.ES_LABEL_E,"NEXT");
      add(eNext);
      eLock = new EElbo(null,1443,199,142,58,LCARS.EC_SECONDARY|LCARS.ES_SHAPE_SE|LCARS.ES_LABEL_E,"LOCK");
      eLock.setArcWidths(48,1); eLock.setArmWidths(142,58);
      add(eLock);
      eTracks.setPageControls(ePrev,eNext);
      eTracks.setLockControl(eLock);
    }

    @Override
    public void addToPanel(Panel panel)
    {
      super.addToPanel(panel);
      eTracks.addToPanel(panel);
    }

    @Override
    public void removeFromPanel()
    {
      eTracks.removeFromPanel();
      super.removeFromPanel();
    }
  }
  
  // -- Main method --
  
  /**
   * Runs the audio library panel.
   * 
   * @param args
   *          The command line arguments, see {@link LCARS#main(String[])}.
   */
  public static void main(String[] args)
  {
    args = LCARS.setArg(args,"--panel=",AudioLibraryPanel.class.getName());
    LCARS.main(args);
  }

}

// EOF
