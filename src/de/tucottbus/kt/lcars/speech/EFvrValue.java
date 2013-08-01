package de.tucottbus.kt.lcars.speech;

import java.awt.Font;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Area;
import java.io.StringWriter;
import java.util.Vector;

import de.tucottbus.kt.lcars.LCARS;
import de.tucottbus.kt.lcars.Panel;
import de.tucottbus.kt.lcars.elements.EElement;
import de.tucottbus.kt.lcars.j2d.GArea;
import de.tucottbus.kt.lcars.j2d.Geometry;

/**
 * An LCARS {@link EElement} displaying feature-value relation strings. A
 * feature-value relation string represents tree structures with labeled nodes,
 * e.~g. <code>"[root[child1[grandchild]][child2]]"</code>.
 * 
 * @author Matthias Wolff
 */
public class EFvrValue extends EElement
{
  
  /**
   * Create a new semantic value display.
   * 
   * @param panel
   *          The LCARS panel to place the GUI element on.
   * @param x
   *          The x-coordinate of the upper left corner (in LCARS panel pixels).
   * @param y
   *          The y-coordinate of the upper left corner (in LCARS panel pixels).
   * @param w
   *          The width (in LCARS panel pixels).
   * @param h
   *          The height (in LCARS panel pixels).
   * @param style
   *          The style (see class {@link LCARS}).
   * @param label
   *          The label (a feature-value relation string). A feature-value
   *          relation string represents tree structures with labeled nodes,
   *          e.~g. <code>"[root[child1[grandchild]][child2]]"</code>.
   */
  public EFvrValue
  (
    Panel  panel,
    int    x,
    int    y,
    int    w,
    int    h,
    int    style,
    String label
  )
  {
    super(panel,x,y,w,h,style,label);
  }
  
  @Override
  protected Vector<Geometry> createGeometriesInt()
  {
    Vector<Geometry> geos = new Vector<Geometry>();
    Rectangle        bnds = getBounds();
    Font             font = getFont();
    String           fvrs = label==null?"":label;
    int              xofs = 0;
    int              yofs = 0;
    int              linh = LCARS.getTextShape(font,"M").getBounds().height;
    int              yinc = linh/3;

    // Trim on leading and one tailing square brace from label 
    if (fvrs.startsWith("[") && fvrs.endsWith("]"))
      fvrs = fvrs.substring(1,fvrs.length()-1);
    
    // Count lines
    int lcnt = 0;
    int bcnt = 0;
    for (char c : fvrs.toCharArray())
      if (c=='[')
      {
        bcnt++;
        lcnt = Math.max(bcnt,lcnt);
      }
      else if (c==']')
        bcnt--;
    lcnt++;

    // Adjust line increment
    if (linh+lcnt*yinc>bnds.height)
      yinc = Math.max((bnds.height-linh)/lcnt,3);
    
    // Create lines
    Area[] lines = new Area[lcnt];
    for (int i=0; i<lines.length; i++)
      lines[i] = new Area(new Rectangle(bnds.x,bnds.y+i*yinc+linh+1,bnds.width,1));
    
    // Create node label geometries
    StringWriter sw = new StringWriter();
    for (int i=0; i<fvrs.length(); i++)
    {
      char c = fvrs.charAt(i);
      if (c=='[' || c==']' || i==fvrs.length()-1)
      {
        if (i==fvrs.length()-1 && c!='[' && c!=']')
          sw.append(c);
        if (sw.getBuffer().length()>0)
        {
          String    nlab = sw.toString();
          Rectangle tbnd = LCARS.getTextShape(font,nlab).getBounds();
          tbnd.x = bnds.x + xofs;
          tbnd.y = bnds.y + yofs - (int)(tbnd.height*0.15);
          tbnd.height = linh;
          for (Area area : lines)
          {
            Rectangle r = new Rectangle(tbnd);
            r.x-=2; r.y-=2; r.width+=4; r.height+=4;
            area.subtract(new Area(r));
          }
          geos.addAll(LCARS.createTextGeometry2D(font,nlab,tbnd,LCARS.ES_LABEL_NW,null,false));      
          xofs += tbnd.width + 6;
          sw = new StringWriter();
        }
        yofs += c=='[' ? yinc : -yinc;
      }
      else
        sw.append(c);
    }

    // Add line geometries
    for (Area area : lines)
      geos.add(new GArea(area,false));
    
    return geos;
  }

  /**
   * Returns a font which allows to display the semantic value within the bounds
   * of this element.
   */
  protected Font getFont()
  {
    Rectangle bnds  = getBounds();
    Font      font  = LCARS.getFont(LCARS.EF_LARGE);
    Shape     tshp  = LCARS.getTextShape(font,rawLabel(label));
    
    if (tshp.getBounds().width<=bnds.width) return font;

    float size = font.getSize()*(float)bnds.width/(float)tshp.getBounds().width;
    return LCARS.getFont(LCARS.EF_LARGE,(int)(size));
  }

  /**
   * Removed heading and tailing braces, replaces every remaining sequence of
   * opening and closing square brackets by a space and returns the result of
   * this operation.
   * 
   * @param label
   *          The label string to be processed.
   */
  protected String rawLabel(String label)
  {
    if (label==null) return "";
    StringWriter sw = new StringWriter();
    boolean braceflag = true;
    for (char c : label.toCharArray())
      if (c!='[' && c!=']')
      {
        sw.append(c);
        braceflag = false;
      }
      else if (!braceflag)
      {
        sw.append(' ');
        braceflag = true;
      }
    return sw.toString().trim();
  }
}
