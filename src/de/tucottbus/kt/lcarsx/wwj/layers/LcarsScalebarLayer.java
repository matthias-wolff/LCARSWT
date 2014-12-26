package de.tucottbus.kt.lcarsx.wwj.layers;

import gov.nasa.worldwind.layers.ScalebarLayer;
import de.tucottbus.kt.lcars.LCARS;

public class LcarsScalebarLayer extends ScalebarLayer
{

  public LcarsScalebarLayer()
  {
    super();
    setFont(LCARS.getFont(LCARS.EF_SMALL));
  }

}
