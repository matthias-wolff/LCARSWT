package de.tucottbus.kt.lcars.contributors;

import java.awt.Dimension;

import de.tucottbus.kt.lcars.LCARS;
import de.tucottbus.kt.lcars.elements.EElbo;
import de.tucottbus.kt.lcars.elements.ERect;

/**
 * EXPERIMENTAL; An element list with a curly brace shaped frame on the left side.
 *  
 * @author Matthias Wolff
 */
public class EBrace extends EElementArray
{
  private int tipW;
  private int tipH;
  private int tipY;
  private int braceH;
  private int braceW;
  private int space = 10;
  
  private EElbo eElboU1;
  private EElbo eElboU2;
  private EElbo eElboL1;
  private EElbo eElboL2;
  private ERect eRectU;
  private ERect eRectL;
  
  public EBrace(int x, int y, Class<?> elemClass, Dimension elemSize, int rows, int elemStyle)
  {
    super(x,y,elemClass,elemSize,rows,1,elemStyle,null);
    
    // Make intelligent guesses about the brace's geometry
    tipH   = elemSize.height;
    tipW   = (int)(1.0*elemSize.height);
    tipY   = (int)(((elemSize.height+3)*rows-3)/2);
    braceH = (int)(1.0*elemSize.height);
    braceW = (int)(2.0*elemSize.height);
  }

  public void setTipSize(int w, int h)
  {
    if (tipW==w && tipH==h) return;
    tipW = w;
    tipH = h;
    layout();
  }
  
  public void setBraceSize(int w, int h)
  {
    if (braceW==w && braceH==h) return;
    braceW = w;
    braceH = h;
    layout();
  }

  public void setTipPos(int y)
  {
    if (tipY==y-this.y) return;
    tipY = y-this.y;
    layout();
  }
  
  /**
   * Do the layout.
   */
  protected void layout()
  {
    // Remove brace elements
    remove(eElboU1);
    remove(eElboU2);
    remove(eElboL1);
    remove(eElboL2);
    remove(eRectU);
    remove(eRectL);
    remove(eLock);
    remove(ePrev);
    remove(eNext);
    
    // Create brace elements
    int buttonW = (elemSize.width-elemSize.height-3)/3;
    int tipH2   = (tipH-3)/2;

    // - The upper bar
    int x = this.x-space-braceW;
    int y = this.y-space-braceH;
    int w = elemSize.width+space+braceW-(2*buttonW+9+braceH);
    int h = braceH+space-3;
    eElboU2 = new EElbo(null,x,y,w,h,LCARS.EC_ELBOLO|LCARS.ES_STATIC,null);
    eElboU2.setArmWidths(braceW,braceH); eElboU2.setArcWidths(2*braceH,2*space);

    if (eLock==null)
    {
      x = this.x+elemSize.width-(2*buttonW+6+braceH);
      eLock = new ERect(null,x,y,buttonW,braceH,LCARS.EC_SECONDARY|LCARS.ES_LABEL_SE,"LOCK");
      eLock.addEEventListener(this);
    }

    if (ePrev==null)
    {
      x = this.x+elemSize.width-(buttonW+3+braceH);
      ePrev = new ERect(null,x,y,buttonW,braceH,LCARS.EC_PRIMARY|LCARS.ES_LABEL_SE,"PREV");
      ePrev.addEEventListener(this);
    }

    x = this.x+elemSize.width-braceH;
    eRectU = new ERect(null,x,y,braceH,braceH,LCARS.EC_ELBOLO|LCARS.ES_RECT_RND_E,null);

    // -- The tip
    x = this.x-space-braceW-tipW;
    y = this.y-3;
    w = braceW+tipW;
    h = tipY+2;
    eElboU1 = new EElbo(null,x,y,w,h,LCARS.EC_ELBOLO|LCARS.ES_STATIC|LCARS.ES_SHAPE_SE,null);
    eElboU1.setArmWidths(braceW,tipH2); eElboU1.setArcWidths(2*tipH2,2*space);
    
    y = this.y+tipY+2;
    h = (elemSize.height+3)*rows-2-tipY;
    eElboL1 = new EElbo(null,x,y,w,h,LCARS.EC_ELBOLO|LCARS.ES_STATIC|LCARS.ES_SHAPE_NE,null);
    eElboL1.setArmWidths(braceW,tipH2); eElboL1.setArcWidths(2*tipH2,2*space);
    
    // -- The lower bar
    x = this.x-space-braceW;
    y = this.y+(elemSize.height+3)*rows;
    w = elemSize.width+space+braceW-(buttonW+6+braceH);
    h = braceH+space-3;
    eElboL2 = new EElbo(null,x,y,w,h,LCARS.EC_ELBOLO|LCARS.ES_STATIC|LCARS.ES_SHAPE_SW,null);
    eElboL2.setArmWidths(braceW,braceH); eElboL2.setArcWidths(2*braceH,2*space);

    y+=space-3;
    if (eNext==null)
    {
      x = this.x+elemSize.width-(buttonW+3+braceH);
      eNext = new ERect(null,x,y,buttonW,braceH,LCARS.EC_PRIMARY|LCARS.ES_LABEL_SE,"NEXT");
      eNext.addEEventListener(this);
    }

    x = this.x+elemSize.width-braceH;
    eRectL = new ERect(null,x,y,braceH,braceH,LCARS.EC_ELBOLO|LCARS.ES_RECT_RND_E,null);
    
    // Add brace elements
    add(eElboU1,false);
    add(eElboU2,false);
    add(eElboL1,false);
    add(eElboL2,false);
    add(eRectU,false);
    add(eRectL,false);
    add(eLock,false);
    add(ePrev,false);
    add(eNext,false);
  }
  
  // -- Overrides --

  @Override
  protected synchronized void showItemsInt(int first, int count)
  {
    super.showItemsInt(first,count);
    layout();
  }
  
}

// EOF
