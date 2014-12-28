package de.tucottbus.kt.lcarsx.wwj.layers;

import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.layers.LayerList;

/**
 * <p><i><b style="color:red">Experimental.</b></i></p>
 * 
 * A named set of {@linkplain Layer layers} which can be enable and disabled
 * together.
 * 
 * @author Matthias Wolff, BTU Cottbus-Senftenberg
 */
public class LayerSet extends LayerList
{
  private static final long serialVersionUID = 1L;

  /**
   * <p><i><b style="color:red">Experimental.</b></i></p>
   * 
   * Creates a new layer set.
   * 
   * @param displayName
   *          The name.
   */
  public LayerSet(String displayName)
  {
    super();
    setDisplayName(displayName);
  }
  
  /**
   * <p><i><b style="color:red">Experimental.</b></i></p>
   * 
   * Indicates if the majority of layers in this set is enabled for rendering 
   * and selection.
   */
  public boolean isMajorityEnabled()
  {
    double numEnabled = 0;
    for (Layer layer:this)
      if (layer.isEnabled())
        numEnabled++;
    return (numEnabled/size()>0.5);
  }
  
  /**
   * <p><i><b style="color:red">Experimental.</b></i></p>
   * 
   * Controls whether the layers of this set are enabled for rendering and 
   * selection.
   * 
   * @param enabled
   *          The new enabled state.
   */
  public void setEnabled(boolean enabled)
  {
    for (Layer l:this)
      l.setEnabled(enabled);
  }
}
