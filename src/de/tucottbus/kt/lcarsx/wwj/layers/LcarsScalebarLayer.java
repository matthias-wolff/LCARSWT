package de.tucottbus.kt.lcarsx.wwj.layers;

import gov.nasa.worldwind.layers.ScalebarLayer;

import org.eclipse.swt.widgets.Display;
import org.jfree.experimental.swt.SWTUtils;

import de.tucottbus.kt.lcars.LCARS;

public class LcarsScalebarLayer extends ScalebarLayer
{

  public LcarsScalebarLayer()
  {
    super();
    Display disp = LCARS.getDisplay();
    disp.asyncExec(() -> {
      setFont(SWTUtils.toAwtFont(disp, LCARS.getFontMeta(LCARS.EF_SMALL).getFont()));      
    });
    
  }

}
