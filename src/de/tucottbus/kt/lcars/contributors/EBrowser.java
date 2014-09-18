package de.tucottbus.kt.lcars.contributors;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Point;
import java.awt.Rectangle;
import java.rmi.RemoteException;
import java.util.Vector;

import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.LocationEvent;
import org.eclipse.swt.browser.LocationListener;
import org.eclipse.swt.browser.ProgressEvent;
import org.eclipse.swt.browser.ProgressListener;
import org.eclipse.swt.browser.StatusTextEvent;
import org.eclipse.swt.browser.StatusTextListener;
import org.eclipse.swt.browser.TitleEvent;
import org.eclipse.swt.browser.TitleListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.widgets.Shell;

import de.tucottbus.kt.lcars.LCARS;
import de.tucottbus.kt.lcars.Panel;
import de.tucottbus.kt.lcars.Screen;
import de.tucottbus.kt.lcars.feedback.UserFeedback;

/**
 * Wraps an SWT web {@link Browser browser} in an {@link ElementContributor}. 
 * 
 * @author Matthis Wolff
 */
public class EBrowser extends ElementContributor
{
  private EBrowser  eBrowser;
  private Rectangle bounds;
  private Screen    screen;
  private Canvas    canvas;
  private String    canvasText;
  private Browser   browser;
  private String    browserTitleText;
  private String    browserStatusText;
  private String    browserText;
  private String    browserUrl;
  private String    css;

  private Vector<EventListener> browserEventListeners;
  
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
   */
  public EBrowser(int x, int y, int w, int h)
  {
    super(x,y);
    this.eBrowser              = this;
    this.bounds                = new Rectangle(x,y,w,h);
    this.browserEventListeners = new Vector<EventListener>();
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
    if (canvas!=null || browser!=null) return; // FIXME: What if only one is non-null? 
    try
    {
      if (canvas!=null) throw new IllegalStateException("AWT canvas already existing");
      if (browser!=null) throw new IllegalStateException("Browser already existing");

      screen = Screen.getLocal(panel.getScreen());
      canvasText = "LOADING HTML ...";
      canvas = new Canvas();
      canvas.setBackground(Color.BLACK);
      screen.add(canvas);
      tl = screen.panelToComponent(new Point(bounds.x,bounds.y));
      br = screen.panelToComponent(new Point(bounds.x+bounds.width,bounds.y+bounds.height));
      canvas.setBounds(tl.x,tl.y,br.x-tl.x,br.y-tl.y);
      screen.getSwtDisplay().asyncExec(new Runnable()
      {
        public void run()
        {
          Shell swtShell = SWT_AWT.new_Shell(screen.getSwtDisplay(),canvas);
          swtShell.setBackground(screen.getSwtDisplay().getSystemColor(SWT.COLOR_BLACK));
          swtShell.setSize(br.x-tl.x,br.y-tl.y);
          swtShell.addPaintListener(new PaintListener()
          {
            public void paintControl(PaintEvent e)
            {
              org.eclipse.swt.graphics.Color color =
                new org.eclipse.swt.graphics.Color(e.gc.getDevice(),0xFF,0x99,0x00);
              org.eclipse.swt.graphics.Font font =
                new org.eclipse.swt.graphics.Font(e.gc.getDevice(),LCARS.getInstalledFont(LCARS.FN_COMPACTA),56,0); 
              e.gc.setForeground(color);
              e.gc.setFont(font);
              int x = (br.x-tl.x-e.gc.stringExtent(canvasText).x)/2;
              int y = (br.y-tl.y-e.gc.stringExtent(canvasText).y)/2;
              e.gc.drawText(canvasText,x,y);
              color.dispose();
              font.dispose();
            }
          });
          browser = new Browser(swtShell,SWT.NONE);
          browser.setBackground(screen.getSwtDisplay().getSystemColor(SWT.COLOR_BLACK));
          browser.setVisible(false);
          browser.setSize(br.x-tl.x+20/*<- this "hides" the scroll bar*/,br.y-tl.y);
          if (eBrowser.browserText!=null)
            browser.setText(eBrowser.browserText);
          else if (eBrowser.browserUrl!=null)
            browser.setUrl(eBrowser.browserUrl);
          browser.addTitleListener(new TitleListener()
          {
            public void changed(TitleEvent event)
            {
              eBrowser.browserTitleText = event.title;
              fireTitleChanged(event.title);
            }
          });
          browser.addStatusTextListener(new StatusTextListener()
          {
            public void changed(StatusTextEvent event)
            {
              synchronized(eBrowser)
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
              try
              {
                String script = LCARS.loadTextResource("de/tucottbus/kt/lcars/resources/LCARS-css.js");
                browser.execute(script);
              }
              catch (Exception e)
              {
                e.printStackTrace();
              }
              browser.setVisible(true);
            }
          });
          browser.addLocationListener(new LocationListener()
          {
            public void changed(LocationEvent event)
            {
              
            }
            public void changing(LocationEvent event)
            {
              try
              {
                eBrowser.panel.getScreen().userFeedback(UserFeedback.Type.TOUCH);
              }
              catch (RemoteException e)
              {
              } 
            }
          });
        }
      });
    }
    catch (ClassCastException e)
    {
      System.err.println("LCARS: Function not supported on remove screens.");
      e.printStackTrace();
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
    if (screen!=null)
    {
      if (canvas!=null)
      {
        screen.remove(canvas);
        canvas = null;
      }
      if (browser!=null) screen.getSwtDisplay().asyncExec(new Runnable()
      {
        public void run()
        {
          browser.dispose();
          browser = null;
        }
      });
    }
    super.removeFromPanel();
  }

  /**
   * Returns the LCARS cascading style sheet (CSS).
   * 
   * @return the style sheet in the W3C CSS format.
   */
  public String getCss()
  {
    if (css==null)
    {
      try
      {
        css = LCARS.loadTextResource("de/tucottbus/kt/lcars/resources/LCARS.css");
      }
      catch (Exception e)
      {
        e.printStackTrace();
        css = "";
      } 
    }
    return css;
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
        eBrowser.back_ = browser.back();
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
        eBrowser.forward_ = browser.forward();
      }
    });
    return this.forward_;
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
        eBrowser.isBackEnabled_ = browser.isBackEnabled();
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
        eBrowser.scrollBy_ = browser.execute("window.scrollBy(0,"+eBrowser.scrollBy_pixels+");");
      }
    });
    return this.scrollBy_;  }
  
  /**
   * Renders HTML
   * 
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
    screen.getSwtDisplay().syncExec(new Runnable()
    {
      public void run()
      {
        browser.setVisible(false);
        eBrowser.setText_ = browser.setText(eBrowser.browserText);
      }
    });
    return this.setText_;
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
    screen.getSwtDisplay().syncExec(new Runnable()
    {
      public void run()
      {
        browser.setVisible(false);
        eBrowser.setUrl_ = browser.setUrl(eBrowser.browserUrl);
      }
    });
    return this.setUrl_;
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
