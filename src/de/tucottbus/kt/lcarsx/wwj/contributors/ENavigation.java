package de.tucottbus.kt.lcarsx.wwj.contributors;

import gov.nasa.worldwind.View;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Position;

import java.util.Locale;

import de.tucottbus.kt.lcars.LCARS;
import de.tucottbus.kt.lcars.contributors.ElementContributor;
import de.tucottbus.kt.lcars.elements.EEvent;
import de.tucottbus.kt.lcars.elements.EEventListenerAdapter;
import de.tucottbus.kt.lcars.elements.ERect;
import de.tucottbus.kt.lcars.elements.EValue;
import de.tucottbus.kt.lcarsx.wwj.WorldWindPanel;

public class ENavigation extends ElementContributor
{
  private final WorldWindPanel worldWindPanel;
  private final int ST_E = LCARS.EC_PRIMARY|LCARS.ES_LABEL_E;
  private final int ST_C = LCARS.EC_PRIMARY|LCARS.ES_LABEL_C;
  //private final int ST_T = LCARS.EC_PRIMARY|LCARS.ES_SELECTED|LCARS.ES_LABEL_E|LCARS.EF_TINY|LCARS.ES_STATIC;

  private EValue eLat;
  private ERect  eLatDec;
  private ERect  eLatInc;
  private EValue eLon;
  private ERect  eLonDec;
  private ERect  eLonInc;
  private EValue eAlt;
  private ERect  eAltDec;
  private ERect  eAltInc;
  private EValue ePit;
  private ERect  ePitDec;
  private ERect  ePitInc;
  private EValue eHdg;
  private ERect  eHdgDec;
  private ERect  eHdgInc;
  
  public ENavigation(WorldWindPanel worldWindPanel, int x, int y)
  {
    super(x,y);
    this.worldWindPanel = worldWindPanel;
    
    // Latitude control
    eLat = new EValue(null,0,0,198,38,ST_E,"LAT/°");
    eLat.setValue("00.0N"); eLat.setValueMargin(0);
    eLat.addEEventListener(new EEventListenerAdapter()
    {
      @Override
      public void touchDown(EEvent ee)
      {
        if (ENavigation.this.worldWindPanel.getEWorldWind()==null) return;
        ENavigation.this.worldWindPanel.getEWorldWind().setEyePosition(null);
      }
    });
    add(eLat);
    eLatDec = new ERect(null,201,0,38,38,ST_C,"\u2013");
    eLatDec.addEEventListener(new EEventListenerAdapter()
    {
      @Override
      public void touchDown(EEvent ee)
      {
        eLat.setSelected(true);
        touchHold(ee);
      }
      @Override
      public void touchHold(EEvent ee)
      {
        if (ee.ct>0 && ee.ct<=5) return;
        if (ENavigation.this.worldWindPanel.getEWorldWind()==null) return;
        Position pos = ENavigation.this.worldWindPanel.getEWorldWind().getEyePosition();
        if (pos==null) return;
        Angle  lat = Angle.fromDegreesLatitude(pos.latitude.degrees-1);
        Angle  lon = pos.longitude;
        double alt = pos.elevation;
        ENavigation.this.worldWindPanel.getEWorldWind().setEyePosition(new Position(lat,lon,alt));
      }
      @Override
      public void touchUp(EEvent ee)
      {
        eLat.setSelected(false);
      }
    });
    add(eLatDec);
    eLatInc = new ERect(null,242,0,38,38,ST_E,"+");
    eLatInc.addEEventListener(new EEventListenerAdapter()
    {
      @Override
      public void touchDown(EEvent ee)
      {
        eLat.setSelected(true);
        touchHold(ee);
      }
      @Override
      public void touchHold(EEvent ee)
      {
        if (ee.ct>0 && ee.ct<=5) return;
        if (ENavigation.this.worldWindPanel.getEWorldWind()==null) return;
        Position pos = ENavigation.this.worldWindPanel.getEWorldWind().getEyePosition();
        if (pos==null) return;
        Angle  lat = Angle.fromDegreesLatitude(pos.latitude.degrees+1);
        Angle  lon = pos.longitude;
        double alt = pos.elevation;
        ENavigation.this.worldWindPanel.getEWorldWind().setEyePosition(new Position(lat,lon,alt));
      }
      @Override
      public void touchUp(EEvent ee)
      {
        eLat.setSelected(false);
      }
    });
    add(eLatInc);

    // Longitude control
    eLon = new EValue(null,283,0,198,38,ST_E,"LON/°");
    eLon.setValue("00.0E"); eLon.setValueMargin(0);
    eLon.addEEventListener(new EEventListenerAdapter()
    {
      @Override
      public void touchDown(EEvent ee)
      {
        if (ENavigation.this.worldWindPanel.getEWorldWind()==null) return;
        ENavigation.this.worldWindPanel.getEWorldWind().setEyePosition(null);
      }
    });
    add(eLon);
    eLonDec = new ERect(null,481,0,38,38,ST_C,"\u2013");
    eLonDec.addEEventListener(new EEventListenerAdapter()
    {
      @Override
      public void touchDown(EEvent ee)
      {
        eLon.setSelected(true);
        touchHold(ee);
      }
      @Override
      public void touchHold(EEvent ee)
      {
        if (ee.ct>0 && ee.ct<=5) return;
        if (ENavigation.this.worldWindPanel.getEWorldWind()==null) return;
        Position pos = ENavigation.this.worldWindPanel.getEWorldWind().getEyePosition();
        if (pos==null) return;
        Angle  lat = pos.latitude;
        Angle  lon = Angle.fromDegreesLongitude(pos.longitude.degrees-1);
        double alt = pos.elevation;
        ENavigation.this.worldWindPanel.getEWorldWind().setEyePosition(new Position(lat,lon,alt));
      }
      @Override
      public void touchUp(EEvent ee)
      {
        eLon.setSelected(false);
      }
    });
    add(eLonDec);
    eLonInc = new ERect(null,522,0,38,38,ST_E,"+");
    eLonInc.addEEventListener(new EEventListenerAdapter()
    {
      @Override
      public void touchDown(EEvent ee)
      {
        eLon.setSelected(true);
        touchHold(ee);
      }
      @Override
      public void touchHold(EEvent ee)
      {
        if (ee.ct>0 && ee.ct<=5) return;
        if (ENavigation.this.worldWindPanel.getEWorldWind()==null) return;
        Position pos = ENavigation.this.worldWindPanel.getEWorldWind().getEyePosition();
        if (pos==null) return;
        Angle  lat = pos.latitude;
        Angle  lon = Angle.fromDegreesLongitude(pos.longitude.degrees+1);
        double alt = pos.elevation;
        ENavigation.this.worldWindPanel.getEWorldWind().setEyePosition(new Position(lat,lon,alt));
      }
      @Override
      public void touchUp(EEvent ee)
      {
        eLon.setSelected(false);
      }
    });
    add(eLonInc);

    // Altitude control
    eAlt = new EValue(null,563,0,203,38,ST_E,"ALT/km");
    eAlt.setValue("0.000"); eAlt.setValueMargin(0);
    eAlt.addEEventListener(new EEventListenerAdapter()
    {
      @Override
      public void touchDown(EEvent ee)
      {
        if (ENavigation.this.worldWindPanel.getEWorldWind()==null) return;
        ENavigation.this.worldWindPanel.getEWorldWind().setEyePosition(null);
      }
    });
    add(eAlt);
    eAltDec = new ERect(null,766,0,38,38,ST_C,"\u2013");
    eAltDec.addEEventListener(new EEventListenerAdapter()
    {
      @Override
      public void touchDown(EEvent ee)
      {
        eAlt.setSelected(true);
        touchHold(ee);
      }
      @Override
      public void touchHold(EEvent ee)
      {
        if (ee.ct>0 && ee.ct<=5) return;
        if (ENavigation.this.worldWindPanel.getEWorldWind()==null) return;
        Position pos = ENavigation.this.worldWindPanel.getEWorldWind().getEyePosition();
        if (pos==null) return;
        Angle  lat = pos.latitude;
        Angle  lon = pos.longitude;
        double alt = pos.elevation*0.95;
        ENavigation.this.worldWindPanel.getEWorldWind().setEyePosition(new Position(lat,lon,alt));
      }
      @Override
      public void touchUp(EEvent ee)
      {
        eAlt.setSelected(false);
      }
    });
    add(eAltDec);
    eAltInc = new ERect(null,807,0,38,38,ST_E,"+");
    eAltInc.addEEventListener(new EEventListenerAdapter()
    {
      @Override
      public void touchDown(EEvent ee)
      {
        eAlt.setSelected(true);
        touchHold(ee);
      }
      @Override
      public void touchHold(EEvent ee)
      {
        if (ee.ct>0 && ee.ct<=5) return;
        if (ENavigation.this.worldWindPanel.getEWorldWind()==null) return;
        Position pos = ENavigation.this.worldWindPanel.getEWorldWind().getEyePosition();
        if (pos==null) return;
        Angle  lat = pos.latitude;
        Angle  lon = pos.longitude;
        double alt = pos.elevation/0.95;
        ENavigation.this.worldWindPanel.getEWorldWind().setEyePosition(new Position(lat,lon,alt));
      }
      @Override
      public void touchUp(EEvent ee)
      {
        eAlt.setSelected(false);
      }
    });
    add(eAltInc);

    // Pitch control
    ePit = new EValue(null,848,0,198,38,ST_E|LCARS.ES_STATIC,"PIT/°");
    ePit.setValue("00.0"); ePit.setValueMargin(0);
    ePit.addEEventListener(new EEventListenerAdapter()
    {
      @Override
      public void touchDown(EEvent ee)
      {
        if (ENavigation.this.worldWindPanel.getEWorldWind()==null) return;
        ENavigation.this.worldWindPanel.getEWorldWind().setPitch(null);
      }
    });
    add(ePit);
    ePitDec = new ERect(null,1046,0,38,38,ST_C,"\u2013");
    ePitDec.addEEventListener(new EEventListenerAdapter()
    {
      @Override
      public void touchDown(EEvent ee)
      {
        ePit.setSelected(true);
        touchHold(ee);
      }
      @Override
      public void touchHold(EEvent ee)
      {
        if (ee.ct>0 && ee.ct<=5) return;
        if (ENavigation.this.worldWindPanel.getEWorldWind()==null) return;
        Angle pitch = ENavigation.this.worldWindPanel.getEWorldWind().getPitch();
        if (pitch==null) return;
        pitch = Angle.fromDegreesLongitude(pitch.degrees-1);
        ENavigation.this.worldWindPanel.getEWorldWind().setPitch(pitch);
      }
      @Override
      public void touchUp(EEvent ee)
      {
        ePit.setSelected(false);
      }
    });
    add(ePitDec);
    ePitInc = new ERect(null,1087,0,38,38,ST_E,"+");
    ePitInc.addEEventListener(new EEventListenerAdapter()
    {
      @Override
      public void touchDown(EEvent ee)
      {
        ePit.setSelected(true);
        touchHold(ee);
      }
      @Override
      public void touchHold(EEvent ee)
      {
        if (ee.ct>0 && ee.ct<=5) return;
        if (ENavigation.this.worldWindPanel.getEWorldWind()==null) return;
        Angle pitch = ENavigation.this.worldWindPanel.getEWorldWind().getPitch();
        if (pitch==null) return;
        pitch = Angle.fromDegreesLongitude(pitch.degrees+1);
        ENavigation.this.worldWindPanel.getEWorldWind().setPitch(pitch);
      }
      @Override
      public void touchUp(EEvent ee)
      {
        ePit.setSelected(false);
      }
    });
    add(ePitInc);

    // Heading control
    eHdg = new EValue(null,1128,0,198,38,ST_E|LCARS.ES_STATIC,"HDG/°");
    eHdg.setValue("000.00"); eHdg.setValueMargin(0);
    eHdg.addEEventListener(new EEventListenerAdapter()
    {
      @Override
      public void touchDown(EEvent ee)
      {
        if (ENavigation.this.worldWindPanel.getEWorldWind()==null) return;
        ENavigation.this.worldWindPanel.getEWorldWind().setHeading(null);
      }
    });
    add(eHdg);
    eHdgDec = new ERect(null,1326,0,38,38,ST_C,"\u2013");
    eHdgDec.addEEventListener(new EEventListenerAdapter()
    {
      @Override
      public void touchDown(EEvent ee)
      {
        eHdg.setSelected(true);
        touchHold(ee);
      }
      @Override
      public void touchHold(EEvent ee)
      {
        if (ee.ct>0 && ee.ct<=5) return;
        if (ENavigation.this.worldWindPanel.getEWorldWind()==null) return;
        Angle heading = ENavigation.this.worldWindPanel.getEWorldWind().getHeading();
        if (heading==null) return;
        heading = Angle.fromDegrees(heading.degrees-1);
        ENavigation.this.worldWindPanel.getEWorldWind().setHeading(heading);
      }
      @Override
      public void touchUp(EEvent ee)
      {
        eHdg.setSelected(false);
      }
    });
    add(eHdgDec);
    eHdgInc = new ERect(null,1367,0,38,38,ST_E,"+");
    eHdgInc.addEEventListener(new EEventListenerAdapter()
    {
      @Override
      public void touchDown(EEvent ee)
      {
        eHdg.setSelected(true);
        touchHold(ee);
      }
      @Override
      public void touchHold(EEvent ee)
      {
        if (ee.ct>0 && ee.ct<=5) return;
        if (ENavigation.this.worldWindPanel.getEWorldWind()==null) return;
        Angle heading = ENavigation.this.worldWindPanel.getEWorldWind().getHeading();
        if (heading==null) return;
        heading = Angle.fromDegrees(heading.degrees+1);
        ENavigation.this.worldWindPanel.getEWorldWind().setHeading(heading);
      }
      @Override
      public void touchUp(EEvent ee)
      {
        eHdg.setSelected(false);
      }
    });
    add(eHdgInc);
    
    /* TODO: Make a field of view slider out of these -->
    e = new EValue(null,0,41,79,18,ST_T|LCARS.ES_RECT_RND_W,"SPEED");
    ((EValue)e).setValue("0.00"); ((EValue)e).setValueMargin(0);
    add(e);
    e = new EValue(null,79,41,119,18,ST_T,"TARGET");
    ((EValue)e).setValue("00.0N"); ((EValue)e).setValueMargin(0);
    add(e);
    add(new ERect(null,201,41,79,18,ST_T|LCARS.ES_RECT_RND_E,null));
    e = new EValue(null,283,41,198,18,ST_T|LCARS.ES_RECT_RND_W,null);
    ((EValue)e).setValue("00.0E"); ((EValue)e).setValueMargin(0);
    add(e);
    add(new ERect(null,481,41,79,18,ST_T|LCARS.ES_RECT_RND_E,null));
    e = new EValue(null,563,41,203,18,ST_T|LCARS.ES_RECT_RND_W,null);
    ((EValue)e).setValue("0.000"); ((EValue)e).setValueMargin(0);
    add(e);
    add(new ERect(null,766,41,79,18,ST_T|LCARS.ES_RECT_RND_E,null));
    e = new EValue(null,848,41,198,18,ST_T|LCARS.ES_RECT_RND_W,null);
    ((EValue)e).setValue("00.0"); ((EValue)e).setValueMargin(0);
    add(e);
    add(new ERect(null,1046,41,79,18,ST_T|LCARS.ES_RECT_RND_E,null));
    e = new EValue(null,1128,41,198,18,ST_T|LCARS.ES_RECT_RND_W,null);
    ((EValue)e).setValue("000.00"); ((EValue)e).setValueMargin(0);
    add(e);
    add(new ERect(null,1326,41,79,18,ST_T|LCARS.ES_RECT_RND_E,null));
    // <-- */
  }
  
  public void setDisabled(boolean disabled)
  {
    forAllElements((el) -> {
      el.setDisabled(disabled);
    });
  }
  
  public void displayCurrentState(EWorldWind eWw)
  {
    if (eWw==null || eWw.getView()==null)
    {
      final String na = "N/A";
      eLat.setValue(na);
      eLon.setValue(na);
      eAlt.setValue(na);
      ePit.setValue(na);
      eHdg.setValue(na);
      return;
    }
    
    String s;
    double v;
    boolean b;
    
    // TODO: Bogus...
    boolean sky = this.worldWindPanel.getTitle().startsWith("SKY");

    // Display actual view
    //eWw.setView(null);
    View view = eWw.getView();
    Position pos = view.getEyePosition();
    if (pos==null) return;
    Angle angle = pos.getLatitude();
    v = angle.getDegrees();
    s = String.format(Locale.US,"%05.2f",Math.abs(v))+(v<0?"S":"N");
    eLat.setLabel(sky?"DEC/°":"LAT/°");
    eLat.setValue(s);
    b = !(eWw.getOrbit()==null || !eWw.getOrbit().controlsLatitude());
    eLat.setStatic(b);
    eLatDec.setDisabled(b);
    eLatInc.setDisabled(b);

    v = eWw.getView().getEyePosition().getLongitude().getDegrees();
    if (sky)
    {
      eLon.setLabel("RA/h");
      v = (v+180)/15;
      s = String.format(Locale.US,"%06.2f",Math.abs(v));
    }
    else
    {
      eLon.setLabel("LON/°");
      s = String.format(Locale.US,"%06.2f",Math.abs(v))+(v<0?"W":"E");
    }
    eLon.setValue(s);
    b = !(eWw.getOrbit()==null || !eWw.getOrbit().controlsLongitude());
    eLon.setStatic(b);
    eLonDec.setDisabled(b);
    eLonInc.setDisabled(b);
    
    v = eWw.getView().getEyePosition().getAltitude()/1000.;
    eAlt.setDisabled(sky);
    if      (v<1E3) s = String.format(Locale.US,"%06.2f" ,v    );
    else if (v<1E6) s = String.format(Locale.US,"%06.2fT",v/1E3);
    else if (v<1E9) s = String.format(Locale.US,"%06.2fM",v/1E6);
    else            s = String.format(Locale.US,"%06.2fB",v/1E9);
    eAlt.setValue(sky?"NA":s);
    b = !(eWw.getOrbit()==null || !eWw.getOrbit().controlsAltitude());
    eAlt.setStatic(b);
    eAltDec.setDisabled(b);
    eAltInc.setDisabled(b);

    v = eWw.getView().getPitch().getDegrees();
    s = String.format(Locale.US,"%s%05.2f",v<0?"-":"",Math.abs(v));
    ePit.setValue(s);
    b = !(eWw.getOrbit()==null || !eWw.getOrbit().controlsPitch());
    ePit.setStatic(b);
    ePitDec.setDisabled(b);
    ePitInc.setDisabled(b);

    v = eWw.getView().getHeading().getDegrees();
    s = String.format(Locale.US,"%s%06.2f",v<0?"-":"",Math.abs(v));
    eHdg.setValue(s);
    b = !(eWw.getOrbit()==null || !eWw.getOrbit().controlsHeading());
    eHdg.setStatic(b);
    eHdgDec.setDisabled(b);
    eHdgInc.setDisabled(b);
  }

}