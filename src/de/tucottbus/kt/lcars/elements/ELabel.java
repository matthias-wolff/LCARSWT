package de.tucottbus.kt.lcars.elements;

import java.awt.Font;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;

import de.tucottbus.kt.lcars.LCARS;
import de.tucottbus.kt.lcars.Panel;
import de.tucottbus.kt.lcars.j2d.Geometry;

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
  public ArrayList<Geometry> createGeometriesInt()
  {
    ArrayList<Geometry> geos = new ArrayList<Geometry>(); 
    
    // Create label geometries
    Font      font   = LCARS.getFont(getStyle());
    Rectangle bounds = getBounds();
    Point     insets = computeLabelInsets();
    geos.addAll(LCARS.createTextGeometry2D(font,label,bounds,getStyle(),insets,false));
    
    // This is it
    return geos;
  }

}
