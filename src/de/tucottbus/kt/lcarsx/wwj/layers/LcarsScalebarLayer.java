package de.tucottbus.kt.lcarsx.wwj.layers;

import gov.nasa.worldwind.layers.ScalebarLayer;

import org.jfree.experimental.swt.SWTUtils;

import de.tucottbus.kt.lcars.LCARS;

public class LcarsScalebarLayer extends ScalebarLayer
{

  public LcarsScalebarLayer()
  {
    super();
    setFont(SWTUtils.toAwtFont(LCARS.getDisplay(), LCARS.getFontData(LCARS.EF_SMALL), true));
  }

}
