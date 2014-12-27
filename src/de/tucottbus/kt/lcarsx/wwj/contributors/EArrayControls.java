package de.tucottbus.kt.lcarsx.wwj.contributors;

import de.tucottbus.kt.lcars.LCARS;
import de.tucottbus.kt.lcars.contributors.ElementContributor;
import de.tucottbus.kt.lcars.elements.EElement;
import de.tucottbus.kt.lcars.elements.ERect;
import de.tucottbus.kt.lcars.elements.EValue;
import de.tucottbus.kt.lcarsx.wwj.WorldWindPanel;

public class EArrayControls extends ElementContributor
{
  private final WorldWindPanel worldWindPanel;
  private EValue ePrev;
  private ERect  eNext;
  private ERect  eLock;
  
  public EArrayControls(WorldWindPanel worldWindPanel, int x, int y)
  {
    super(x,y);
    this.worldWindPanel = worldWindPanel;
    ePrev  = new EValue(null,   0,0,174,38,this.worldWindPanel.style|LCARS.ES_LABEL_E,"PREV");
    eNext  = new ERect (null,1286,0, 58,38,this.worldWindPanel.style|LCARS.ES_LABEL_W,"NEXT");
    eLock  = new ERect (null,1347,0, 58,38,this.worldWindPanel.style|LCARS.ES_LABEL_W,"LOCK");
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