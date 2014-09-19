package de.tucottbus.kt.lcars;

import java.io.FileNotFoundException;

import de.tucottbus.kt.lcars.contributors.EBrowser;
import de.tucottbus.kt.lcars.elements.EElement;
import de.tucottbus.kt.lcars.elements.EEvent;
import de.tucottbus.kt.lcars.elements.EEventListenerAdapter;
import de.tucottbus.kt.lcars.elements.ERect;
import de.tucottbus.kt.lcars.elements.EValue;

/**
 * EXPERIMENTAL class; do not use!
 * 
 * @author Matthias Wolff
 */
public class HelpPanel extends Panel
{

  private Class<? extends Panel> helpFor;
  private EValue                 eCaption;
  private EBrowser               eBrowser;
  private String                 docIndex;
  private boolean                noRestyleHtml;
  
  private final int ST_AMBER = LCARS.EC_PRIMARY | LCARS.ES_SELECTED;
  private final int ST_YELLO = LCARS.EC_SECONDARY | LCARS.ES_SELECTED;
  
  public HelpPanel(Screen screen)
  {
    super(screen);
  }
  
  @Override
  public void init()
  {
    super.init();
    setTitle(null);

    // The top bar
    EElement e;
    add(new ERect(this,23,23,58,58,ST_YELLO|LCARS.ES_STATIC|LCARS.ES_RECT_RND_W,null));
    e = new ERect(this,85,23,156,58,ST_AMBER|LCARS.ES_LABEL_SE,"DISMISS");
    e.addEEventListener(new EEventListenerAdapter()
    {
      @Override
      public void touchDown(EEvent ee)
      {
        try { getScreen().setPanel(helpFor.getName()); } catch (Exception e) {}
      }
    });
    add(e);
    e = new ERect(this,244,23,156,58,ST_YELLO|LCARS.ES_LABEL_SE,"HELP");
    e.addEEventListener(new EEventListenerAdapter()
    {
      @Override
      public void touchDown(EEvent ee)
      {
        loadHelp();
      }
    });
    add(e);
    e = new ERect(this,403,23,156,58,ST_YELLO|LCARS.ES_LABEL_SE,"DOCUMENTS");
    e.addEEventListener(new EEventListenerAdapter()
    {
      @Override
      public void touchDown(EEvent ee)
      {
        loadDoc();
      }
    });
    add(e);
    
    eCaption = new EValue(this,562,23,1274,58,ST_YELLO|LCARS.ES_LABEL_SE|LCARS.ES_STATIC,"");
    eCaption.setValue("UNAVAILABLE"); eCaption.setValueMargin(0);
    add(eCaption);

    add(new ERect(this,1839,23,58,58,ST_YELLO|LCARS.ES_STATIC|LCARS.ES_RECT_RND_E,null));

    // The browser
    eBrowser = new EBrowser(85,85,1750,926,LCARS.ES_NONE);
    eBrowser.addBrowserEventListener(new EBrowser.EventListener()
    {
      public void titleChanged(String title)
      {
        eCaption.setValue(title.toUpperCase());
      }

      @Override
      public void statusTextChanged(String text)
      {
      }
    });
    
    // The bottom bar
    add(new ERect(this,23,1019,58,38,ST_YELLO|LCARS.ES_STATIC|LCARS.ES_RECT_RND_W,null));

    e = new ERect(this,84,1019,188,38,ST_AMBER|LCARS.ES_LABEL_E|LCARS.ES_DISABLED,"HELP ON HELP");
    e.addEEventListener(new EEventListenerAdapter()
    {
      @Override
      public void touchDown(EEvent ee)
      {
      }
    });
    add(e);
    e = new ERect(this,275,1019,116,38,ST_YELLO|LCARS.ES_LABEL_E,"BACK");
    e.addEEventListener(new EEventListenerAdapter()
    {
      @Override
      public void touchDown(EEvent ee)
      {
        if (!eBrowser.isBackEnabled()) 
          try { getScreen().setPanel(helpFor.getName()); } catch (Exception e) {}
        eBrowser.back();
      }
    });
    add(e);
    e = new ERect(this,394,1019,116,38,ST_YELLO|LCARS.ES_LABEL_E,"FORWARD");
    e.addEEventListener(new EEventListenerAdapter()
    {
      @Override
      public void touchDown(EEvent ee)
      {
        eBrowser.forward();
      }
    });
    add(e);

    //add(new ERect(this,513,1019,1085,38,ST_YELLO|LCARS.ES_LABEL_E,"[UP]/[DOWN] TO SCROLL, [DISMISS] TO CLOSE"));
    add(new ERect(this,513,1019,1085,38,ST_YELLO|LCARS.ES_LABEL_E,"BTU/KT"));
    
    e = new ERect(this,1601,1019,116,38,ST_AMBER|LCARS.ES_LABEL_E,"UP");
    e.addEEventListener(new EEventListenerAdapter()
    {
      @Override
      public void touchDown(EEvent ee)
      {
        touchHold(ee);
      }
      @Override
      public void touchHold(EEvent ee)
      {
        if (ee.ct==0)
        {
          eBrowser.scrollBy(-50);
          return;
        }
        if (ee.ct<6) return;
        eBrowser.scrollBy(-25);
      }
    });
    add(e);
    e = new ERect(this,1720,1019,116,38,ST_AMBER|LCARS.ES_LABEL_E,"DOWN");
    e.addEEventListener(new EEventListenerAdapter()
    {
      @Override
      public void touchDown(EEvent ee)
      {
        touchHold(ee);
      }
      @Override
      public void touchHold(EEvent ee)
      {
        if (ee.ct==0)
        {
          eBrowser.scrollBy(50);
          return;
        }
        if (ee.ct<6) return;
        eBrowser.scrollBy(25);
      }
    });
    add(e);
    
    add(new ERect(this,1839,1019,58,38,ST_YELLO|LCARS.ES_STATIC|LCARS.ES_RECT_RND_E,null));
    
  }
  
  @Override
  public void start()
  {
    eBrowser.addToPanel(this);
    super.start();
  }

  @Override
  public void stop()
  {
    super.stop();
    eBrowser.removeFromPanel();
  }

  protected void setDocs(Class<? extends Panel> helpFor, String docIndex, boolean noRestyleHtml)
  {
    this.helpFor       = helpFor;
    this.docIndex      = docIndex;
    this.noRestyleHtml = noRestyleHtml;
  }
  
  protected boolean loadDoc()
  {
    eCaption.setLabel("DOCUMENTS");
    if (this.docIndex==null)
    {
      loadBlank();
      return false;
    }
    try
    {
      eBrowser.setNoRestyleHtml(noRestyleHtml);
      return eBrowser.setUrl(this.docIndex);
    }
    catch (Exception e)
    {
      e.printStackTrace();
      loadBlank();
      return false;
    }
  }
  
  protected boolean loadHelp()
  {
    eCaption.setLabel("HELP");
    if (this.helpFor==null) return false;
    try
    {
      String name = this.helpFor.getName().replace(".","/")+".html";
      String html = LCARS.loadTextResource(name);
      if (html==null) throw new FileNotFoundException(name);
      eBrowser.setNoRestyleHtml(noRestyleHtml);
      return eBrowser.setText(html);
    }
    catch (Exception e)
    {
      System.out.println("ERROR: Cannot load \""+e.getMessage()+"\"");
      loadBlank();
      return false;
     }
  }
  
  protected void loadBlank()
  {
    String msg = "<h1>NO "+eCaption.getLabel()+" AVAILABLE";
    if (this.helpFor!=null)
      msg+=" FOR</h1><h2>"+ this.helpFor.getName()+"</h2>";
    else
      msg+="</h1>";
    String html
    = "<html>\n"
    + "  <head>\n"
    + "    <title>UNAVAILABLE</title>\n"
    + "  </head>\n"
    + "  <body>\n"
    + "    <table width=100% height=100%><tr>\n"
    + "      <td style='vertical-align:middle; text-align:center'>\n"
    + msg +"\n"
    + "      </td>\n"
    + "    </tr></table>\n"
    + "  </body>\n"
    + "</html>\n";
    eBrowser.setNoRestyleHtml(false);
    eBrowser.setText(html);
  }


}

// EOF

