package de.tucottbus.kt.lcarsx.wwj.layers;

import gov.nasa.worldwind.layers.ScalebarLayer;
import de.tucottbus.kt.lcars.LCARS;
import de.tucottbus.kt.lcars.swt.AwtSwt;

public class LcarsScalebarLayer extends ScalebarLayer
{

  public LcarsScalebarLayer()
  {
    super();
    setFont(AwtSwt.toAwtFont(LCARS.getFont(LCARS.EF_SMALL)));
  }

}
