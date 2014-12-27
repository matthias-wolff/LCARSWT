package de.tucottbus.kt.lcarsx.wwj.contributors;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import de.tucottbus.kt.lcars.LCARS;
import de.tucottbus.kt.lcars.contributors.ElementContributor;
import de.tucottbus.kt.lcars.elements.EElbo;
import de.tucottbus.kt.lcars.elements.EElement;
import de.tucottbus.kt.lcars.elements.EEvent;
import de.tucottbus.kt.lcars.elements.EEventListenerAdapter;
import de.tucottbus.kt.lcars.elements.ERect;
import de.tucottbus.kt.lcars.elements.EValue;
import de.tucottbus.kt.lcarsx.wwj.WorldWindPanel;

public class EPlaceSearch extends ElementContributor implements KeyListener
{
  private final WorldWindPanel worldWindPanel;
  private ERect  ePrev;
  private ERect  eNext;
  private ERect  eLock;
  EValue eQuery;
  
  public EPlaceSearch(WorldWindPanel worldWindPanel, int x, int y)
  {
    super(x,y);
    this.worldWindPanel = worldWindPanel;
    
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
    setEPrev(new ERect(null,0,349,192,68,LCARS.EC_PRIMARY|LCARS.ES_LABEL_SE,"PREV"));
    add(getEPrev());
    setENext(new ERect(null,0,420,192,68,LCARS.EC_PRIMARY|LCARS.ES_LABEL_SE,"NEXT"));
    add(getENext());
    setELock(new ERect(null,0,491,192,68,LCARS.EC_PRIMARY|LCARS.ES_LABEL_SE,"LOCK"));
    add(getELock());
    add(new ERect(null,0,562,192,109,LCARS.EC_SECONDARY|LCARS.ES_STATIC,null));
    e = new EElbo(null,0,674,426,156,LCARS.EC_ELBOUP|LCARS.ES_STATIC|LCARS.ES_SHAPE_SW,null);
    ((EElbo)e).setArcWidths(194,72); ((EElbo)e).setArmWidths(192,58);
    add(e);
  }

  public void keyPressed(KeyEvent e)
  {
    if (e.getKeyCode()==KeyEvent.VK_CANCEL)
    {
      this.worldWindPanel.setMainMode(WorldWindPanel.MODE_WORLDWIND);
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
        this.worldWindPanel.fillPlacesArray(null /*Show known places*/);
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
    this.worldWindPanel.fillPlacesArray("" /*Clear places array*/);
  }
  
  protected void Ok()
  {
    String q = eQuery.isBlinking()?"":eQuery.getValue();
    this.worldWindPanel.fillPlacesArray(q);      
  }

  public ERect getENext()
  {
    return eNext;
  }

  public void setENext(ERect eNext)
  {
    this.eNext = eNext;
  }

  public ERect getEPrev()
  {
    return ePrev;
  }

  public void setEPrev(ERect ePrev)
  {
    this.ePrev = ePrev;
  }

  public ERect getELock()
  {
    return eLock;
  }

  public void setELock(ERect eLock)
  {
    this.eLock = eLock;
  }
}