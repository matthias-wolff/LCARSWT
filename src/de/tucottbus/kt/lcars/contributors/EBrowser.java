package de.tucottbus.kt.lcars.contributors;

import java.awt.Point;
import java.awt.Rectangle;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Vector;

import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.LocationEvent;
import org.eclipse.swt.browser.LocationListener;
import org.eclipse.swt.browser.ProgressEvent;
import org.eclipse.swt.browser.ProgressListener;
import org.eclipse.swt.browser.StatusTextEvent;
import org.eclipse.swt.browser.StatusTextListener;
import org.eclipse.swt.browser.TitleEvent;
import org.eclipse.swt.browser.TitleListener;

import de.tucottbus.kt.lcars.LCARS;
import de.tucottbus.kt.lcars.Panel;
import de.tucottbus.kt.lcars.Screen;
import de.tucottbus.kt.lcars.logging.Log;
import de.tucottbus.kt.lcars.swt.ColorMeta;

/**
 * Wraps an SWT web {@link Browser browser} in an {@link ElementContributor}. 
 * 
 * @author Matthis Wolff
 */
public class EBrowser extends ElementContributor
{
  public static final String cssFile = "de/tucottbus/kt/lcars/resources/LCARS.css"; 
  
  private Rectangle bounds;
  private Screen    screen;
  private Browser   browser;
  private String    browserTitleText;
  private String    browserStatusText;
  private String    browserText;
  private String    browserUrl;
  private String    css;

  private Vector<EventListener> browserEventListeners;
  
  private int       style;
  
  private File      tmpHtmlFile;
  
  // SWT thread transfer fields
  private Point     tl;
  private Point     br;
  private boolean   setText_;
  private boolean   setUrl_;
  private boolean   scrollBy_;
  private int       scrollBy_pixels;
  private boolean   back_;
  private boolean   forward_;
  private boolean   isBackEnabled_;
  private boolean   execute_;

  /**
   * Creates a new web browser. 
   * 
   * <p>Note: To avoid display artifacts, {@link #setText(String)} or {@link
   * #setUrl(String)} should be called before {@link #addToPanel(Panel)}.</p>
   * 
   * @param x
   *          the x-coordinate of the top left corner (LCARS panel coordinates) 
   * @param y
   *          the y-coordinate of the top left corner (LCARS panel coordinates) 
   * @param w
   *          the width (LCARS panel coordinates)
   * @param h
   *          the height (LCARS panel coordinates)
   * @param style
   *          The style (see class {@link LCARS}).
   */
  public EBrowser(int x, int y, int w, int h, int style)
  {
    super(x,y);
    this.bounds                = new Rectangle(x,y,w,h);
    this.browserEventListeners = new Vector<EventListener>();
    this.style                 = style;
  }
  
  /*
   * (non-Javadoc)
   * @see de.tucottbus.kt.lcars.contributors.ElementContributor#addToPanel(de.tucottbus.kt.lcars.Panel)
   */
  @Override
  public void addToPanel(Panel panel)
  {
    super.addToPanel(panel);
    if (panel==null) return;
    if (browser!=null) return;
    
    //TODO: add some loading message before the page appears 
    
    try
    {
      if (browser!=null) throw new IllegalStateException("Browser already existing"); // FIXME: not reachable
      
      final Screen scr = screen = Screen.getLocal(panel.getScreen());
      tl = scr.panelToScreen(bounds.x,bounds.y);
      br = scr.panelToScreen(bounds.x+bounds.width,bounds.y+bounds.height);
      scr.getSwtDisplay().syncExec(()->
      {
        browser = new Browser(scr.getLcarsComposite(),SWT.NONE);
        browser.setBackground(ColorMeta.GREEN.getColor());
        browser.setVisible(false);
        browser.setLocation(tl.x, tl.y);
        browser.setSize(br.x-tl.x+28/*<- this "hides" the scroll bar*/,br.y-tl.y);
        if (EBrowser.this.browserText!=null)
          browser.setText(EBrowser.this.browserText);
        else if (EBrowser.this.browserUrl!=null)
          browser.setUrl(EBrowser.this.browserUrl);
        browser.addTitleListener(new TitleListener()
        {
          public void changed(TitleEvent event)
          {
            EBrowser.this.browserTitleText = event.title;
            fireTitleChanged(event.title);
          }
        });
        browser.addStatusTextListener(new StatusTextListener()
        {
          public void changed(StatusTextEvent event)
          {
            synchronized(EBrowser.this)
            {
              browserStatusText = event.text;
              fireStatusTextChanged(event.text);
            }
          }
        });
        browser.addProgressListener(new ProgressListener()
        {
          public void changed(ProgressEvent event)
          {
          }
          public void completed(ProgressEvent event)
          {
            if (!isNoRestyleHtml())
            {
              final String jsFile = "de/tucottbus/kt/lcars/resources/LCARS-css.js";
              try
              {
                String script = LCARS.loadTextResource(jsFile);
                browser.execute(script);
              }
              catch (Exception e)
              {
                Log.err("Could not load javascript \"" + jsFile + "\"", e);
              }
            }
            browser.setVisible(true);
          }
        });
        browser.addLocationListener(new LocationListener()
        {
          public void changed(LocationEvent event) {}
          public void changing(LocationEvent event)
          {
            /* WHY??
            try
            {
              EBrowser.this.panel.getScreen().userFeedback(UserFeedback.Type.TOUCH); 
            }
            catch (RemoteException e)
            {
            } 
            */
          }
        });
      });
    }
    catch (ClassCastException e)
    {
      Log.err("Function not supported on remote screens.", e);
    }
  }
  
  /*
   * (non-Javadoc)
   * @see de.tucottbus.kt.lcars.contributors.ElementContributor#removeFromPanel()
   */
  @Override
  public void removeFromPanel()
  {
    if (panel==null) return;
    if (screen!=null && browser != null)
      screen.getSwtDisplay().asyncExec(() ->
      {
        try
        {
          browser.dispose();
        }
        catch (Exception e)
        {
          Log.err("Removing composite from screen failed.", e);
        }
        browser = null;
      });
    super.removeFromPanel();
  }

  /**
   * Shows or hides this browser contributor. This method may be used instead
   * of {@link #addToPanel(Panel)} or {@link #removeFromPanel()} if the browser 
   * content is to be conserved for re-display. Note, however, that at least
   * when closing the panel {@link #removeFromPanel()} must be invoked in order
   * to release the browser widget.
   * 
   * @see #isVisible()
   */
  public void setVisible(boolean visible)
  {
    if (browser != null)
      browser.setVisible(visible);
    else
      throw new UnsupportedOperationException("Cannot set browser to visible add to a panel.", new NullPointerException("browser"));
  }
  
  /**
   * Determines if this browser contributor is visible.
   * 
   * @see #setVisible(boolean)
   */
  public boolean isVisible()
  {
    return (browser != null) && browser.isVisible();
  }
  
  /**
   * Returns the LCARS cascading style sheet (CSS).
   * 
   * @return the style sheet in the W3C CSS format.
   */
  public String getCss()
  {
    if (css!=null) return css;
    try
    {
      return css = LCARS.loadTextResource(cssFile);
    }
    catch (Exception e)
    {
      Log.err("Cannot find css file: \""+cssFile+"\"");
      return css = "";
    } 
  }
  
  public String getTitleText()
  {
    return this.browserTitleText;
  }
  
  /**
   * Returns the latest known status test of the browser.
   * 
   * @return the status text
   */
  public String getStatusText()
  {
    return this.browserStatusText;
  }
  
  public boolean back()
  {
    if (browser==null       ) return true;
    if (browser.isDisposed()) return false;
    this.back_ = false;
    screen.getSwtDisplay().syncExec(new Runnable()
    {
      public void run()
      {
        EBrowser.this.back_ = browser.back();
      }
    });
    return this.back_;
  }
  
  public boolean forward()
  {
    if (browser==null       ) return true;
    if (browser.isDisposed()) return false;
    this.forward_ = false;
    screen.getSwtDisplay().syncExec(new Runnable()
    {
      public void run()
      {
        EBrowser.this.forward_ = browser.forward();
      }
    });
    return this.forward_;
  }

  public boolean execute(String script)
  {
    if (browser==null       ) return false;
    if (browser.isDisposed()) return false;
    final String script_ = script;
    execute_ = false;
    screen.getSwtDisplay().syncExec(new Runnable()
    {
      public void run()
      {
        EBrowser.this.execute_ = browser.execute(script_);
      }
    });
    return execute_;
  }
  
  public boolean isBackEnabled()
  {
    if (browser==null       ) return true;
    if (browser.isDisposed()) return false;
    this.isBackEnabled_ = false;
    screen.getSwtDisplay().syncExec(new Runnable()
    {
      public void run()
      {
        EBrowser.this.isBackEnabled_ = browser.isBackEnabled();
      }
    });
    return this.isBackEnabled_;
  }

  public synchronized boolean scrollBy(int pixels)
  {
    if (browser==null       ) return true;
    if (browser.isDisposed()) return false;
    this.scrollBy_       = false;
    this.scrollBy_pixels = pixels;
    screen.getSwtDisplay().syncExec(new Runnable()
    {
      public void run()
      {
        EBrowser.this.scrollBy_ = browser.execute("window.scrollBy(0,"+EBrowser.this.scrollBy_pixels+");");
      }
    });
    return this.scrollBy_;  }

  /**
   * Renders HTML
   * 
   * @param text
   *          The HTML text.
   * @see #setTextViaTmpFile(String)
   * @see Browser#setText(String)
   * @param text the HTML content to be rendered
   * @return true if the operation was successful and false otherwise.
   */
  public synchronized boolean setText(String text)
  {
    this.browserText = text;
    this.setText_    = false;
    if (browser==null       ) return true;
    if (browser.isDisposed()) return false;
    screen.getSwtDisplay().syncExec(() ->
    {
      browser.setVisible(false);
      EBrowser.this.setText_ = browser.setText(EBrowser.this.browserText);
    });
    return this.setText_;
  }

  /**
   * Renders HTML by writing and displaying a temporary file.
   * 
   * @param text
   *          The HTML text.
   * @see #setText(String)
   * @param text the HTML content to be rendered
   * @return true if the operation was successful and false otherwise.
   */
  public void setTextViaTmpFile(final String text)
  {
    if (browser==null       ) return;
    if (browser.isDisposed()) return;
    if (text==null          ) return;

    try
    {
      if (tmpHtmlFile==null)
        (tmpHtmlFile = File.createTempFile("Lcarswt",null)).deleteOnExit();
  
      FileOutputStream fos = new FileOutputStream(tmpHtmlFile);
      fos.write(text.getBytes());
      fos.flush();
      fos.close();
    }
    catch (IOException e)
    {
      Log.err("Cannot set text via temporary file.", e);
    }
    screen.getSwtDisplay().asyncExec(() ->
    {
      //browser.setVisible(false);
      try
      {
        browser.setUrl(tmpHtmlFile.toURI().toURL().toString());
      }
      catch (MalformedURLException e)
      {
        Log.err("Cannot set text via temporary file.", e);
      }
    });
  }
  
  /**
   * Loads a URL.
   *
   * @see Browser#setUrl(String)
   * @param url the URL to be loaded
   * @return true if the operation was successful and false otherwise.
   */
  public synchronized boolean setUrl(String url)
  {
    this.browserUrl = url;
    this.setUrl_    = false;
    if (browser==null       ) return true;
    if (browser.isDisposed()) return false;
    screen.getSwtDisplay().asyncExec(() ->
    {
      browser.setVisible(false);
      EBrowser.this.setUrl_ = browser.setUrl(EBrowser.this.browserUrl);
    });
    return this.setUrl_;
  }

  /**
   * Determines if this browser re-styles its HTML content to the LCARS look.
   */
  public boolean isNoRestyleHtml()
  {
    return (style & LCARS.ES_BROWSER_NORESTYLEHTML)!=0;
  }

  /**
   * Set the HTML no-restyle flag. If set, this browser does <em>not</em>
   * re-style its HTML content to the LCARS look.
   * 
   * @param noRestyleHtml
   *          <code>true</code> to disable HTML re-styling, <code>false</code>
   *          to enable.
   */
  public void setNoRestyleHtml(boolean noRestyleHtml)
  {
    if (isNoRestyleHtml()==noRestyleHtml) return;

    if (noRestyleHtml)
      style |= LCARS.ES_BROWSER_NORESTYLEHTML;
    else
      style &= ~LCARS.ES_BROWSER_NORESTYLEHTML;

    screen.getSwtDisplay().asyncExec(() ->
    {
      try
      {
        browser.refresh();
      }
      catch (Exception e)
      {
        Log.err("Cannot refresh browser.", e);
      }
    });  
  }

  // -- Event handling --
  
  public interface EventListener
  {
    public void titleChanged(String title);
    public void statusTextChanged(String text);
  }
  
  public void addBrowserEventListener(EventListener listener)
  {
    this.browserEventListeners.add(listener);
  }
  
  public void removeBrowserEventListener(EventListener listener)
  {
    this.browserEventListeners.remove(listener);
  }
  
  protected void fireTitleChanged(String title)
  {
    for (EventListener listener : browserEventListeners)
      if (listener!=null)
        listener.titleChanged(title);
  }
  
  protected void fireStatusTextChanged(String text)
  {
    for (EventListener listener : browserEventListeners)
      if (listener!=null)
        listener.statusTextChanged(text);
  }
  
}
