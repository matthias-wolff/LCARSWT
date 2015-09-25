package de.tucottbus.kt.lcars.elements;

import java.awt.Point;
import java.util.ArrayList;

import de.tucottbus.kt.lcars.LCARS;
import de.tucottbus.kt.lcars.Panel;
import de.tucottbus.kt.lcars.geometry.AGeometry;

public class ELabel extends EElement
{

  public ELabel(Panel panel, int x, int y, int w, int h, int style, String label)
  {
    super(panel,x,y,w,h,style,label);
  }
  
  @Override
  protected Point computeLabelInsets()
  {
    return new Point(0,0);
  }

  @Override
  public ArrayList<AGeometry> createGeometriesInt()
  {
    return LCARS.createTextGeometry2D(label,getBounds(),getStyle(),computeLabelInsets(),false);
  }

}
