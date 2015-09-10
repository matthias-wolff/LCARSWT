package de.tucottbus.kt.lcarsx.wwj.layers;

import gov.nasa.worldwind.layers.GraticuleRenderingParams;
import gov.nasa.worldwind.layers.LatLonGraticuleLayer;

import de.tucottbus.kt.lcars.LCARS;
import de.tucottbus.kt.lcars.swt.SwtColor;

public class LcarsGraticuleLayer extends LatLonGraticuleLayer
{
  @Override
  protected void initRenderingParams()
  {
      GraticuleRenderingParams params;
      SwtColor color;
      
      // Ten degrees grid
      color = SwtColor.WHITE;
      params = new GraticuleRenderingParams();
      params.setValue(GraticuleRenderingParams.KEY_LINE_COLOR, color);
      params.setValue(GraticuleRenderingParams.KEY_LABEL_COLOR, color);
      params.setValue(GraticuleRenderingParams.KEY_LABEL_FONT, LCARS.getFont(LCARS.EF_NORMAL));
      setRenderingParams(GRATICULE_LATLON_LEVEL_0, params);
      // One degree
      color = LCARS.getColor(LCARS.CS_MULTIDISP,LCARS.EC_SECONDARY|LCARS.ES_SELECTED);
      params = new GraticuleRenderingParams();
      params.setValue(GraticuleRenderingParams.KEY_LINE_COLOR, color);
      params.setValue(GraticuleRenderingParams.KEY_LABEL_COLOR, color);
      params.setValue(GraticuleRenderingParams.KEY_LABEL_FONT, LCARS.getFont(LCARS.EF_NORMAL));
      setRenderingParams(GRATICULE_LATLON_LEVEL_1, params);
      // 1/10th degree - 1/6th (10 minutes)
      color = LCARS.getColor(LCARS.CS_MULTIDISP,LCARS.EC_SECONDARY);
      params = new GraticuleRenderingParams();
      params.setValue(GraticuleRenderingParams.KEY_LINE_COLOR, color);
      params.setValue(GraticuleRenderingParams.KEY_LABEL_COLOR, color);
      params.setValue(GraticuleRenderingParams.KEY_LABEL_FONT, LCARS.getFont(LCARS.EF_SMALL));
      setRenderingParams(GRATICULE_LATLON_LEVEL_2, params);
      // 1/100th degree - 1/60th (one minutes)
      color = LCARS.getColor(LCARS.CS_MULTIDISP,LCARS.EC_SECONDARY|LCARS.ES_SELECTED).darker();
      params = new GraticuleRenderingParams();
      params.setValue(GraticuleRenderingParams.KEY_LINE_COLOR, color);
      params.setValue(GraticuleRenderingParams.KEY_LABEL_COLOR, color);
      params.setValue(GraticuleRenderingParams.KEY_LABEL_FONT, LCARS.getFont(LCARS.EF_SMALL));
      setRenderingParams(GRATICULE_LATLON_LEVEL_3, params);
      // 1/1000 degree - 1/360th (10 seconds)
      color = LCARS.getColor(LCARS.CS_MULTIDISP,LCARS.EC_SECONDARY).darker();
      params = new GraticuleRenderingParams();
      params.setValue(GraticuleRenderingParams.KEY_LINE_COLOR, color);
      params.setValue(GraticuleRenderingParams.KEY_LABEL_COLOR, color);
      params.setValue(GraticuleRenderingParams.KEY_LABEL_FONT, LCARS.getFont(LCARS.EF_SMALL));
      setRenderingParams(GRATICULE_LATLON_LEVEL_4, params);
      // 1/10000 degree - 1/3600th (one second)
      params = new GraticuleRenderingParams();
      color = LCARS.getColor(LCARS.CS_MULTIDISP,LCARS.EC_SECONDARY|LCARS.ES_SELECTED).darker().darker();
      params.setValue(GraticuleRenderingParams.KEY_LINE_COLOR, color);
      params.setValue(GraticuleRenderingParams.KEY_LABEL_COLOR, color);
      params.setValue(GraticuleRenderingParams.KEY_LABEL_FONT, LCARS.getFont(LCARS.EF_SMALL));
      setRenderingParams(GRATICULE_LATLON_LEVEL_5, params);
      
      setOpacity(0.33);
  }

}
