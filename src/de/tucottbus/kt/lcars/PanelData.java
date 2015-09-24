package de.tucottbus.kt.lcars;

import java.io.Serializable;
import java.util.ArrayList;

import de.tucottbus.kt.lcars.elements.EElement;
import de.tucottbus.kt.lcars.elements.ElementData;

/**
 * The serializable data of an {@linkplain Panel LCARS panel}. An instance of this class is passed
 * when a panel updates its {@linkplain Screen screen}.
 * 
 * @author Matthias Wolff
 */
public class PanelData implements Serializable
{
  /**
   * The default serial version ID. 
   */
  private static final long serialVersionUID = 1L;

  /**
   * The panel state.
   */
  public PanelState panelState;
  
  /**
   * The rendering data of the {@linkplain EElement LCARS GUI elements} on the panel.
   */
  public ArrayList<ElementData> elementData;
  
  @Override
  public String toString() {
    return PanelData.class.getSimpleName()
        + " panelState="+(panelState != null ? panelState.toString() : "null")
        + " elementData="+(elementData != null ? elementData.toString() : "null");
  }
  
}

// EOF
