package de.tucottbus.kt.lcars.elements;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;

import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Display;

import de.tucottbus.kt.lcars.LCARS;
import de.tucottbus.kt.lcars.Panel;
import de.tucottbus.kt.lcars.geometry.GArea;
import de.tucottbus.kt.lcars.geometry.AGeometry;
import de.tucottbus.kt.lcars.util.Objectt;

public class EValue extends ERect
{

  private String value;
  private int    vw;
  private int    vm;
  
  public EValue(Panel panel, int x, int y, int w, int h, int style, String label)
  {
    super(panel,x,y,w,h,style,label);
    this.vw = 0;
    this.vm = -1;
  }

  public void setValueWidth(int vw)
  {
    this.vw = vw;
    invalidate(true);
  }
  
  public void setValueMargin(int vm)
  {
    this.vm = vm;
  }
  
  public String getValue()
  {
    return this.value;
  }
  
  public void setValue(String value)
  {
    if (Objectt.equals(this.value, value)) return;
    this.value = value;
    invalidate(true);
  }

  private Rectangle computeValueRect()
  {
    Rectangle r    = getBounds();
    int       m    = computeValueMargin();
    int       w    = this.vw;
    if (w<=0) {
      Font      font = new Font(Display.getDefault(), getValueFont());
      w = LCARS.getTextBounds(font,value).width + 6;
      font.dispose();
    }
    return (getStyle()&LCARS.ES_VALUE_W)!=0
        ? new Rectangle(r.x+m,r.y,w,r.height)
        : new Rectangle(r.x+(r.width-w-m),r.y,w,r.height);
  }
  
  private int computeValueMargin()
  {
    if (this.vm>=0) return this.vm;
    Rectangle r = getBounds();
    return Math.max(computeArc()/2,Math.min(r.width,r.height)/2);
  }
  
  @Override
  protected Point computeLabelInsets()
  {
    int x = 10;
    if ((getStyle()&LCARS.ES_VALUE_W)==0)
      switch (this.getStyle()&LCARS.ES_LABEL)
      {
      case LCARS.ES_LABEL_NE:
      case LCARS.ES_LABEL_E:
      case LCARS.ES_LABEL_SE:
        x += computeValueRect().width+computeValueMargin();
      }
    else
      switch (this.getStyle()&LCARS.ES_LABEL)
      {
      case LCARS.ES_LABEL_NW:
      case LCARS.ES_LABEL_W:
      case LCARS.ES_LABEL_SW:
        x += computeValueRect().width+computeValueMargin();
      }
    return new Point(x,10);
  }
  
  @Override
  public ArrayList<AGeometry> createGeometriesInt()
  {
    ArrayList<AGeometry> geos = new ArrayList<AGeometry>(); 
    
    // Create background geometry
    Rectangle bounds = getBounds();
    int       style  = getStyle();
    int       x      = bounds.x;
    int       y      = bounds.y;
    int       w      = bounds.width;
    int       h      = bounds.height;
    int       arc    = computeArc();
    Area      area   = new Area(new RoundRectangle2D.Float(x,y,w,h,arc,arc));
    if ((style&LCARS.ES_RECT_RND)!=0)
    {
      if ((style&LCARS.ES_RECT_RND_E)==0)
        area.add(new Area(new Rectangle2D.Float(x+w-w/2,y,w/2,h)));
      if ((style&LCARS.ES_RECT_RND_W)==0)
        area.add(new Area(new Rectangle2D.Float(x,y,w/2,h)));
    }
    area.subtract(new Area(bounds = computeValueRect()));
    geos.add(new GArea(area,false));

    // Create value geometry
    int   vstyle = (style&LCARS.ES_VALUE_W)!=0 ? LCARS.ES_LABEL_W : LCARS.ES_LABEL_E;
    Font  font   = new Font(LCARS.getDisplay(), getValueFont());
    Point insets = new Point(3,1);
    if (font.getFontData()[0].getName().equals(LCARS.FN_COMPACTA)) bounds.y-=(int)(0.05*bounds.height);
    
    //geos.add(new GText(value, new Point2D.Float(bounds.x, bounds.y), null, fd, isBlinking()))
    geos.addAll(LCARS.createTextGeometry2D(font,value,bounds,vstyle,insets,false));
    
    font.dispose();
    
    // Create label geometries
    font   = LCARS.getFont(style);
    bounds = getBounds();
    insets = computeLabelInsets();
    geos.addAll(LCARS.createTextGeometry2D(font,label,bounds,style,insets,true));
    font.dispose();
    
    // This is it
    return geos;
  }  

  public FontData getValueFont()
  {
    return LCARS.getFontData(LCARS.EF_HEAD1,(int)(getBounds().height *
                                              (LCARS.getFontData(LCARS.EF_HEAD1).getName().equals(LCARS.FN_COMPACTA)?1.45f:1.30f)));
  }
  
}
