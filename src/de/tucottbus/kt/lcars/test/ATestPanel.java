package de.tucottbus.kt.lcars.test;

import java.rmi.RemoteException;

import de.tucottbus.kt.lcars.IScreen;
import de.tucottbus.kt.lcars.LCARS;
import de.tucottbus.kt.lcars.Panel;
import de.tucottbus.kt.lcars.elements.EElement;
import de.tucottbus.kt.lcars.elements.EEvent;
import de.tucottbus.kt.lcars.elements.EEventListenerAdapter;
import de.tucottbus.kt.lcars.elements.ELabel;
import de.tucottbus.kt.lcars.elements.ERect;
import de.tucottbus.kt.lcars.swt.ColorMeta;

/**
 * An abstract testing panel with a extensible tool bar.
 * 
 * <h3>Remarks:</h3>
 * <ul>
 *   <li>TODO: Move to LCARSWT when testing is complete.
 *     </li>
 * </ul>
 * 
 * @author Matthias Wolff
 */
public abstract class ATestPanel extends Panel
{
  protected ELabel eColorScheme;

  public ATestPanel(IScreen iscreen)
  {
    super(iscreen);
  }

  @Override
  public void init()
  {
    super.init();
    
    int       ex = 1720;
    int       ey = 120;
    final int ew = 177;
    final int eh = 60;
    
    ERect eRect = new ERect(this,ex,ey,ew,eh,LCARS.ES_RECT_RND|LCARS.ES_LABEL_E,"EXIT");
    eRect.addEEventListener(new EEventListenerAdapter()
    {
      @Override
      public void touchUp(EEvent ee)
      {
        try
        {
          getScreen().exit();
        } 
        catch (RemoteException e)
        {
          e.printStackTrace();
        }
      }
    });
    add(eRect);
    ey += getElements().get(getElements().size()-1).getBounds().height +23;

    ey += createToolBar(ex,ey,ew,eh)+23;
    
    ELabel eLabel = new ELabel(this,ex,ey,ew-7,26,LCARS.ES_STATIC|LCARS.ES_LABEL_E,"000/000");
    eLabel.setColor(new ColorMeta(1f,1f,1f,0.25f));
    add(eLabel);
    setLoadStatControl(eLabel);
    ey += getElements().get(getElements().size()-1).getBounds().height +3;
    
    ERect eDim = add(new ERect(this,ex,ey,ew/2,eh,LCARS.ES_RECT_RND_W|LCARS.EC_SECONDARY|LCARS.ES_LABEL_SE,"DIM"));
    ERect eLight = add(new ERect(this,ex+ew/2+3,ey,ew/2,eh,LCARS.ES_RECT_RND_E|LCARS.EC_SECONDARY|LCARS.ES_LABEL_SE,"LIGHT"));
    setDimContols(eLight, eDim);
    ey += getElements().get(getElements().size()-1).getBounds().height +3;
    
    eRect = new ERect(this,ex,ey,ew,eh,LCARS.ES_RECT_RND|LCARS.EC_SECONDARY|LCARS.ES_LABEL_SE,"COLOR SCHEME");
    eRect.addEEventListener(new EEventListenerAdapter()
    {
      @Override
      public void touchUp(EEvent ee)
      {
        int cs = getColorScheme() +1;
        if (cs>LCARS.CS_MAX)
          cs = 0;
        setColorScheme(cs);
        switch (cs)
        {
        case LCARS.CS_KT       : eColorScheme.setLabel("CS_KT"       ); break;
        case LCARS.CS_PRIMARY  : eColorScheme.setLabel("CS_PRIMARY"  ); break;
        case LCARS.CS_SECONDARY: eColorScheme.setLabel("CS_SECONDARY"); break;
        case LCARS.CS_ANCILLARY: eColorScheme.setLabel("CS_ANCILLARY"); break;
        case LCARS.CS_DATABASE : eColorScheme.setLabel("CS_DATABASE" ); break;
        case LCARS.CS_MULTIDISP: eColorScheme.setLabel("CS_MULTIDISP"); break;
        case LCARS.CS_REDALERT : eColorScheme.setLabel("CS_REDALERT" ); break;
        default                : eColorScheme.setLabel("CS_???"      ); break;
        }
      }
    });
    add(eRect);
    ey += getElements().get(getElements().size()-1).getBounds().height +3;
    
    eColorScheme = new ELabel(this,1720,ey,ew-7,26,LCARS.ES_STATIC|LCARS.ES_LABEL_E,"CS_MULIDISP");
    eColorScheme.setColor(new ColorMeta(1f,1f,1f,0.25f));
    add(eColorScheme);
  }
  
  /**
   * Called once during {@linkplain ATestPanel#init() initialization} in order
   * to create extra tool bar elements. Implementations may add a
   * <em>column</em> of {@link EElement}s&mdash;all with the same x-coordinate
   * <code>ex</code> and all with the same width <code>ew</code>&mdash;to the panel.
   * 
   * @param x
   *          The x-coordinate of <em>all</em> elements to be created.
   * @param y
   *          The y-coordinate of the first element to be created.
   * @param w
   *          The width of <em>all</em> elements to be created.
   * @param h
   *          The suggested height of the elements to be created.
   * @return The total height of all elements that were added.
   */
  protected abstract int createToolBar(int x, int y, int w, int h);
  
}

// EOF
