package incubator.ucui;

import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.geom.Area;
import java.rmi.RemoteException;
import java.util.ArrayList;

import de.tucottbus.kt.lcars.IScreen;
import de.tucottbus.kt.lcars.LCARS;
import de.tucottbus.kt.lcars.MainPanel;
import de.tucottbus.kt.lcars.contributors.ElementContributor;
import de.tucottbus.kt.lcars.elements.EElbo;
import de.tucottbus.kt.lcars.elements.EElement;
import de.tucottbus.kt.lcars.elements.EEvent;
import de.tucottbus.kt.lcars.elements.EEventListenerAdapter;
import de.tucottbus.kt.lcars.elements.EImage;
import de.tucottbus.kt.lcars.elements.ELabel;
import de.tucottbus.kt.lcars.elements.ERect;
import de.tucottbus.kt.lcars.elements.EValue;
import de.tucottbus.kt.lcars.elements.modify.EGeometryModifier;
import de.tucottbus.kt.lcars.geometry.AGeometry;
import de.tucottbus.kt.lcars.geometry.GArea;
import de.tucottbus.kt.lcars.swt.ColorMeta;
import de.tucottbus.kt.lcars.swt.ImageMeta;

/**
 * Draft of UCUI device GUI. <i>-- To be deleted --</i>
 * 
 * @author Matthias Wolff, BTU Cottbus-Senftenberg
 */
public class UcuiDraftPanel extends MainPanel 
{
  // -- Fields --
  
  protected       EValue             eSpeechLevel;
  protected       ERect              eInnenTemp;
  protected       ERect              eAussenTemp;
  protected       ERect              eThermostat;
  protected       ERect              eProgramm;
  protected       ETempDisp          cInnenTemp;
  protected       ETempDisp          cAussenTemp;
  protected       ETempDisp          cThermostat;
  protected       ElementContributor cProgramm;
  protected final ColorMeta          cRed   = LCARS.getColor(LCARS.CS_REDALERT,LCARS.EC_PRIMARY|LCARS.ES_SELECTED);
  protected final ColorMeta          cGreen = new ColorMeta(0,255,96);

  // -- Constructors --
  
  /**
   * Creates a new UCUI GUI draft panel.
   * 
   * @param iscreen
   *          The screen to display the panel on.
   */
	public UcuiDraftPanel(IScreen screen)
	{
		super(screen);
	}

	// -- Operations --
	
  @Override
  public void init()
  {
    EElement  e;
    EValue    eV;
    EElbo     eE;
    ColorMeta cGrey = new ColorMeta(1f,1f,1f,0.25f);

    super.init();
    setColorScheme(LCARS.CS_MULTIDISP);
    setTitleLabel(null);
    setTitle("LCARS UCUI");
    
    // GUI design frame
    e = new ERect(this,0,481,820,1,LCARS.ES_STATIC,null);
    e.setColor(cGrey); add(e);
    e = new ERect(this,801,0,1,500,LCARS.ES_STATIC,null);
    e.setColor(cGrey); add(e);
    e = new ELabel(this,805,485,120,30,LCARS.ES_STATIC|LCARS.ES_LABEL_NW|LCARS.EF_SMALL,"800 x 480");
    e.setColor(cGrey); add(e);
    
    // Header bar
    eE = new EElbo(this,0,0,639,78,LCARS.ES_STATIC|LCARS.EC_ELBOLO|LCARS.ES_LABEL_SE|LCARS.ES_SHAPE_NW|LCARS.EF_SMALL,null);
    eE.setArmWidths(168,40); eE.setArcWidths(160,54); add(eE);

    eV = new EValue(this,639,-1,141,40,LCARS.ES_STATIC|LCARS.EC_PRIMARY|LCARS.ES_SELECTED,null);
    eV.setValue("LCARS UCUI"); eV.setValueMargin(0); add(eV);

    eV = new EValue(this,760,0,40,40,LCARS.ES_STATIC|LCARS.ES_RECT_RND_E|LCARS.EC_ELBOLO,null);
    eV.setValue("          "); // This hides the left side of the value bar
    add(eV);

    // Navigation bar (left)
    eInnenTemp = new ERect(this,0,81,168,57,LCARS.EC_SECONDARY|LCARS.ES_LABEL_SE|LCARS.EF_SMALL,"INNENTEMP.");
    add(eInnenTemp);
    eInnenTemp.addEEventListener(new EEventListenerAdapter()
    {
      @Override
      public void touchUp(EEvent ee)
      {
        setMainMode(ee.el);
      }
    });

    eAussenTemp = new ERect(this,0,141,168,57,LCARS.EC_SECONDARY|LCARS.ES_LABEL_SE|LCARS.EF_SMALL,"AUSSENTEMP.");
    add(eAussenTemp);
    eAussenTemp.addEEventListener(new EEventListenerAdapter()
    {
      @Override
      public void touchUp(EEvent ee)
      {
        setMainMode(ee.el);
      }
    });

    eThermostat = new ERect(this,0,201,168,57,LCARS.EC_SECONDARY|LCARS.ES_LABEL_SE|LCARS.EF_SMALL,"THERMOSTAT");
    add(eThermostat);
    eThermostat.addEEventListener(new EEventListenerAdapter()
    {
      @Override
      public void touchUp(EEvent ee)
      {
        setMainMode(ee.el);
      }
    });

    eProgramm = new ERect(this,0,261,168,57,LCARS.EC_SECONDARY|LCARS.ES_LABEL_SE|LCARS.EF_SMALL,"PROGRAMM");
    add(eProgramm);
    eProgramm.addEEventListener(new EEventListenerAdapter()
    {
      @Override
      public void touchUp(EEvent ee)
      {
        setMainMode(ee.el);
      }
    });

    // Footer bar
    eE = new EElbo(this,0,321,425,159,LCARS.EC_ELBOUP|LCARS.ES_SHAPE_SW,null);
    eE.setArmWidths(168,28); eE.setArcWidths(120,46); add(eE);
    eE.addEEventListener(new EEventListenerAdapter()
    {
      @Override
      public void touchUp(EEvent ee)
      {
        LCARS.invokeLater(new Runnable()
        {
          @Override
          public void run()
          {
            try
            {
              getScreen().exit();
            } catch (RemoteException e)
            {
              e.printStackTrace();
            }
          }
        });
      }
    });
    
    e = new ELabel(this,70,485,168,20,LCARS.ES_STATIC|LCARS.ES_LABEL_NW|LCARS.EF_SMALL,"^ EXIT LCARS");
    e.setColor(cGrey); add(e);
    
    e = new ELabel(this,70,505,168,20,LCARS.ES_STATIC|LCARS.ES_LABEL_NW|LCARS.EF_SMALL,
      "^ TOUCH MODE BUTTONS \"INNENTEMP.\", ETC.");
    e.setColor(cGrey); add(e);
    
    e = new ELabel(this,70,509,168,20,LCARS.ES_STATIC|LCARS.ES_LABEL_NW|LCARS.EF_SMALL,"^");
    e.setColor(cGrey); add(e);
    
    eSpeechLevel = new EValue(this,427,452,123,28,LCARS.ES_STATIC|LCARS.EC_SECONDARY,null);
    eSpeechLevel.setValueMargin(0); eSpeechLevel.setValue("IIIIIIIIIIIIIIIIIIII"); add(eSpeechLevel);
    
    e = new ELabel(this,427,485,123,20,LCARS.ES_STATIC|LCARS.ES_LABEL_N|LCARS.EF_SMALL,"^ SPEECH LEVEL");
    e.setColor(cGrey); add(e);

    e = new ERect(this,548,452,170,28,LCARS.EC_SECONDARY|LCARS.EF_SMALL|LCARS.ES_LABEL_E,"HÖREN");
    add(e);
    e.addEEventListener(new EEventListenerAdapter()
    {
      @Override
      public void touchUp(EEvent ee)
      {
        int style = LCARS.EF_SMALL|LCARS.ES_LABEL_E;
        ee.el.setColor((ColorMeta)null);
        ee.el.setBlinking(false);
        if ("SCHLAFEN"==ee.el.getLabel())
        {
          ee.el.setLabel("HÖREN");
          ee.el.setStyle(style|LCARS.EC_SECONDARY|LCARS.ES_SELECTED);
          eSpeechLevel.setStyle(LCARS.ES_STATIC|LCARS.EC_SECONDARY);
        }
        else if ("HÖREN"==ee.el.getLabel())
        {
          ee.el.setStyle(style|LCARS.EC_SECONDARY|LCARS.ES_SELECTED);
          ee.el.setLabel("VERSTANDEN");
          ee.el.setColor(cGreen);
          eSpeechLevel.setStyle(LCARS.ES_STATIC|LCARS.EC_SECONDARY);
        } 
        else if ("VERSTANDEN"==ee.el.getLabel())
        {
          ee.el.setStyle(style|LCARS.EC_SECONDARY|LCARS.ES_SELECTED);
          ee.el.setLabel("WIE BITTE?");
          ee.el.setColor(cRed);
          eSpeechLevel.setStyle(LCARS.ES_STATIC|LCARS.EC_SECONDARY);
        } 
        else if ("WIE BITTE?"==ee.el.getLabel())
        {
          ee.el.setStyle(style|LCARS.EC_ELBOUP);
          ee.el.setLabel("DEAKTIVIERT");
          ee.el.setBlinking(true);
          eSpeechLevel.setStyle(LCARS.ES_STATIC|LCARS.EC_ELBOUP);
        } 
        else
        {
          ee.el.setLabel("SCHLAFEN");
          ee.el.setStyle(style|LCARS.EC_ELBOUP|LCARS.ES_SELECTED);
          eSpeechLevel.setStyle(LCARS.ES_STATIC|LCARS.EC_ELBOUP|LCARS.ES_SELECTED);
        }
      }
    });
    
    e = new ELabel(this,548,485,170,20,LCARS.ES_STATIC|LCARS.ES_LABEL_N|LCARS.EF_SMALL,"^ TOUCH ME");
    e.setColor(cGrey); add(e);
    
    eV = new EValue(this,720,452,80,28,LCARS.ES_STATIC|LCARS.EC_ELBOUP|LCARS.ES_RECT_RND_E,null);
    eV.setValue("HEIZUNG"); add(eV);
    
    // Content area
    cInnenTemp  = new ETempDisp(300,160,"../../../incubator/ucui/resources/innentemp.png" ,false);
    cAussenTemp = new ETempDisp(300,160,"../../../incubator/ucui/resources/aussentemp.png",false);
    cThermostat = new ETempDisp(300,160,"../../../incubator/ucui/resources/thermostat.png",true );
    cProgramm   = new ElementContributor(300,160) {/*Empty placeholder*/};
    
    eInnenTemp.setData(cInnenTemp);
    eAussenTemp.setData(cAussenTemp);
    eThermostat.setData(cThermostat);
    eProgramm.setData(cProgramm);
    
    // Initialize
    setMainMode(eThermostat);
  }
  
  /**
   * Sets the panel mode.
   * 
   * @param eButton
   *          {@link #eInnenTemp}, {@link #eAussenTemp}, {@link #eThermostat},
   *          or {@link #eProgramm}.
   */
  private void setMainMode(EElement eButton)
  {
    cInnenTemp.removeFromPanel();
    cAussenTemp.removeFromPanel();
    cThermostat.removeFromPanel();
    cProgramm.removeFromPanel();
    
    eInnenTemp.setSelected(eButton==eInnenTemp);
    eAussenTemp.setSelected(eButton==eAussenTemp);
    eThermostat.setSelected(eButton==eThermostat);
    eProgramm.setSelected(eButton==eProgramm);

    ((ElementContributor)eButton.getData()).addToPanel(this);
  }

  // -- Nested classes --
  
  protected class ETempDisp extends ElementContributor
  {
    /**
     * The temperature value display.
     */
    private EValue eTemp;

    /**
     * Creates a new temperature display element contributor.
     * 
     * @param x
     *          The x-coordinate of the top-left corner in LCARS panel pixels
     * @param y
     *          The y-coordinate of the top-left corner in LCARS panel pixels
     * @param iconResource
     *          Image resource name of temperature icon, see
     *          {@link ImageMeta.Resource}. Image files are expected to be
     *          125x125 pixels.
     * @param editable
     *          <code>true</code> if the temperature displayed is editable.
     */
    public ETempDisp(int x, int y, String iconResource, boolean editable)
    {
      super(x,y);
      
      add(new EImage(null,0,35,LCARS.ES_STATIC,new ImageMeta.Resource(iconResource)));
      
      eTemp = new EValue(null,152,0,150,180,LCARS.ES_STATIC|LCARS.EC_ELBOUP|LCARS.ES_SELECTED,null);
      eTemp.setValueMargin(0); eTemp.setValueWidth(150);
      eTemp.setValue("23");
      eTemp.setSelected(!editable);
      add(eTemp);

      ELabel eL = new ELabel(null,297,-10,100,180,LCARS.EF_HEAD1|LCARS.ES_STATIC|LCARS.EC_ELBOUP|LCARS.ES_SELECTED,"°C");
      eL.setSelected(!editable);
      add(eL);
      
      if (editable)
      {
        EElbo eE = new EElbo(null,172,-63,230,60,LCARS.ES_STATIC|LCARS.EC_ELBOUP|LCARS.ES_SELECTED|LCARS.ES_SHAPE_NW,null);
        eE.setArmWidths(120,40); eE.setArcWidths(120,40);
        add(eE);

        eE = new EElbo(null,172,183,230,60,LCARS.ES_STATIC|LCARS.EC_ELBOUP|LCARS.ES_SELECTED|LCARS.ES_SHAPE_SW,null);
        eE.setArmWidths(120,40); eE.setArcWidths(120,40);
        add(eE);
        
        ERect eR = new ERect(null,405,-63,68,40,LCARS.EC_SECONDARY,null);
        eR.addGeometryModifier(new EGeometryModifier()
        {
          @Override
          public void modify(ArrayList<AGeometry> geos)
          {
            Area are = new Area(((GArea)geos.get(0)).getArea());
            Rectangle r = are.getBounds();
            geos.add(new GArea(new Area(new Polygon(new int[]{r.x+24,r.x+34,r.x+44},new int[]{r.y+26,r.y+14,r.y+26},3)), true));
          }
        });
        add(eR);
        
        eR = new ERect(null,405,203,68,40,LCARS.EC_SECONDARY,null);
        eR.addGeometryModifier(new EGeometryModifier()
        {
          @Override
          public void modify(ArrayList<AGeometry> geos)
          {
            Area are = new Area(((GArea)geos.get(0)).getArea());
            Rectangle r = are.getBounds();
            geos.add(new GArea(new Area(new Polygon(new int[]{r.x+24,r.x+34,r.x+44},new int[]{r.y+14,r.y+26,r.y+14},3)), true));
          }
        });
        add(eR);
        
        eR = new ERect(null,405,-20,68,220,LCARS.ES_STATIC|LCARS.EC_SECONDARY,null);
        eR.setAlpha(0.3f);
        add(eR);
        
        eL = new ELabel(null,188,249,473,15,LCARS.ES_STATIC|LCARS.EC_SECONDARY|LCARS.EF_SMALL,
          "ZUM EINSTELLEN NACH OBEN/UNTEN WISCHEN ^");
        eL.setAlpha(0.3f);
        add(eL);
      }
    }
    
  }
  
  // -- Main method --

  /**
   * Runs the UCUI draft panel.
   * 
   * @param args
   *          The command line arguments, see {@link LCARS#main(String[])}.
   */
  public static void main(String[] args)
  {
    args = LCARS.setArg(args,"--panel=",UcuiDraftPanel.class.getName());
    args = LCARS.setArg(args,"--nospeech",null);
    LCARS.main(args);
  }

}

// EOF
