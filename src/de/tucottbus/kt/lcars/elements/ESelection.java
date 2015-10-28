package de.tucottbus.kt.lcars.elements;

import java.awt.Rectangle;
import java.util.ArrayList;

import de.tucottbus.kt.lcars.LCARS;
import de.tucottbus.kt.lcars.Panel;
import de.tucottbus.kt.lcars.geometry.AGeometry;
import de.tucottbus.kt.lcars.geometry.GArea;
import de.tucottbus.kt.lcars.geometry.GText;
import de.tucottbus.kt.lcars.util.Objectt;

public class ESelection extends EElement
{

  protected EElement selected;
  
  
  public ESelection(Panel panel, int style, String label)
  {
    super(panel, 0, 0, 0, 0, style, label);
    // TODO Auto-generated constructor stub
  }

  @Override
  protected ArrayList<AGeometry> createGeometriesInt()
  {
    ArrayList<AGeometry> result = new ArrayList<>();
    EElement selected = this.selected;
    if(selected == null)
      return result;
    GArea area = new GArea(selected.getArea(), false);
    area.setOutline(true);
    result.add(area);
    
    GText text = new GText("#"+selected.getSerialNo(), area.getBounds(), LCARS.getFontMeta(this.data.state.getStyle()), false);
    result.add(text);
    
    return result;
  }

  public void setSelectedElement(EElement element)
  {
    if (!Objectt.equals(this.selected, this.selected = element))
      invalidate(true);
  }
  
  /**
   * Returns the rectangular bounds of this LCARS GUI element.
   * 
   * @see #setBounds(Rectangle)
   */
  @Override
  public Rectangle getBounds()
  {
    EElement selected = this.selected;
    return selected != null ? selected.getBounds() : new Rectangle();
  }

  @Override
  public void setBounds(Rectangle bounds)
  {
    throw new UnsupportedOperationException();
  }

  
}
