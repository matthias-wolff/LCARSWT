package de.tucottbus.kt.lcars;

import java.io.Serializable;

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
   * The default serial version UID. 
   */
  private static final long serialVersionUID = -7703907295943231273L;
  
  /**
   * The serial number of the panel that is associated to this {@link PanelData}.
   */
  public final long panelId;
  
  /**
   * The panel state.
   */
  public final PanelState panelState;
  
  /**
   * The rendering data of the {@linkplain EElement LCARS GUI elements} on the panel.
   */
  public final ElementData[] elementData;
  
  public PanelData(IPanel panel, PanelState state, ElementData[] elementData) {
    this.panelId = panel.serialNo();
    this.panelState = state;
    this.elementData = elementData;
  }
  
  @Override
  public String toString() {
    return PanelData.class.getSimpleName()
        + " panelState="+(panelState != null ? panelState.toString() : "null")
        + " elementData="+(elementData != null ? elementData.toString() : "null");
  }
  
}

// EOF
