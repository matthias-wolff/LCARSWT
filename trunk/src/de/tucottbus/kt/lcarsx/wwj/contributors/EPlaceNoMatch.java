package de.tucottbus.kt.lcarsx.wwj.contributors;

import de.tucottbus.kt.lcars.LCARS;
import de.tucottbus.kt.lcars.contributors.ElementContributor;
import de.tucottbus.kt.lcars.elements.EValue;

public class EPlaceNoMatch extends ElementContributor
{
  private EValue eNoMatch;

  public EPlaceNoMatch(int x, int y)
  {
    super(x,y);
    int style = LCARS.EC_PRIMARY|LCARS.EF_HEAD1|LCARS.ES_STATIC|LCARS.ES_BLINKING;
    eNoMatch = new EValue(null,0,0,500,100,style,"");
    eNoMatch.setValue("NO MATCH");
    eNoMatch.setValueMargin(0); eNoMatch.setValueWidth(500);
    add(eNoMatch);
  }
}