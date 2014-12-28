/* This file is part of the LCARS Widget Toolkit.
 *
 * The LCARS Widget Toolkit is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * The LCARS Widget Toolkit is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * the LCARS Widget Toolkit. If not, see <http://www.gnu.org/licenses/>.
 *  
 * Copyright 2013, Matthias Wolff
 */

package de.tucottbus.kt.lcars.util;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.rmi.RemoteException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import de.tucottbus.kt.lcars.IScreen;
import de.tucottbus.kt.lcars.LCARS;
import de.tucottbus.kt.lcars.Panel;
import de.tucottbus.kt.lcars.contributors.EElementArray;
import de.tucottbus.kt.lcars.contributors.EMessageBoxListener;
import de.tucottbus.kt.lcars.elements.EElbo;
import de.tucottbus.kt.lcars.elements.EElement;
import de.tucottbus.kt.lcars.elements.EEvent;
import de.tucottbus.kt.lcars.elements.EEventListenerAdapter;
import de.tucottbus.kt.lcars.elements.EImage;
import de.tucottbus.kt.lcars.elements.ELabel;
import de.tucottbus.kt.lcars.elements.ERect;
import de.tucottbus.kt.lcars.elements.EValue;
import de.tucottbus.kt.lcars.j2d.EGeometryModifier;
import de.tucottbus.kt.lcars.j2d.GArea;
import de.tucottbus.kt.lcars.j2d.Geometry;

/**
 * A simple coding example for an LCARS panel showcasing elements (widgets) and color schemes.
 * 
 * @author Matthias Wolff
 */
public class ShowcasePanel extends Panel
{
  ERect[] eCsBtn;
  EValue  eCsVal;
  ERect   eGuiLd;
  
  /**
   * Creates a new LCARS showcase panel.
   * 
   * @param iscreen
   *          The screen to display the panel on.
   */
  public ShowcasePanel(IScreen iscreen)
  {
    super(iscreen);
  }

  @Override
  public void init()
  {
    super.init();
    setTitle("LCARS SHOWCASE");
    
    /*
     *  The main layout (upper and lower elbos):
     *  
     *  Demonstrates ERects, EElbos and EValues. All these elements can be
     *  touch-sensitive or static.
     */
    ERect eRect = new ERect(this,23,23,208,117,LCARS.EC_ELBOUP|LCARS.ES_LABEL_SE|LCARS.ES_SELECTED,"LCARS");
    eRect.addEEventListener(new EEventListenerAdapter(){
      @Override
      public void touchDown(EEvent ee)
      {
        messageBox("QUESTION","EXIT LCARS?","YES","NO",new EMessageBoxListener()
        {
          @Override
          public void answer(String answer)
          {
            if ("YES".equals(answer)) try
            {
              getScreen().exit();
            }
            catch (RemoteException e)
            {
              // Silently ignore network exceptions...
            }
          }
        });
      }
    });
    add(eRect);
    
    EElbo eElbo = new EElbo(this,23,143,248,171,LCARS.EC_ELBOUP|LCARS.ES_SHAPE_SW|LCARS.ES_LABEL_NE|LCARS.ES_STATIC,null);
    eElbo.setArmWidths(208,38); eElbo.setArcWidths(170,90);
    add(eElbo);

    eElbo = new EElbo(this,23,317,498,122,LCARS.EC_ELBOLO|LCARS.ES_SHAPE_NW|LCARS.ES_LABEL_SE|LCARS.ES_STATIC,LCARS.getHostName().toUpperCase());
    eElbo.setArmWidths(208,38); eElbo.setArcWidths(170,90);
    add(eElbo);

    final ERect eIp = new ERect(this,23,442,208,261,LCARS.EC_ELBOLO|LCARS.ES_STATIC|LCARS.ES_LABEL_SE,"000");
    add(eIp);
    (new Timer()).schedule(new TimerTask()
    {
      @Override
      public void run()
      {
        eIp.setLabel(LCARS.getIP(false).getHostAddress());
      }
    },1);
    
    eGuiLd =  new ERect(this,23,706,208,58,LCARS.EC_PRIMARY|LCARS.ES_SELECTED|LCARS.ES_STATIC|LCARS.ES_LABEL_SE,"00-000");
    add(eGuiLd);
    
    eRect = new ERect(this,23,766,208,291,LCARS.EC_SECONDARY|LCARS.ES_SELECTED|LCARS.ES_LABEL_NE,"MODE\nSELECT");
    eRect.addEEventListener(new EEventListenerAdapter()
    {
      @Override
      public void touchDown(EEvent ee)
      {
        colorSchemeSelect(-1);
      }
    });
    add(eRect);
    
    eCsBtn = new ERect[LCARS.CS_MAX+1];
    eCsBtn[LCARS.CS_MULTIDISP] = new ERect(this,274,276,247,38,LCARS.EC_ELBOUP|LCARS.ES_LABEL_E,"MULTI DISP");
    eCsBtn[LCARS.CS_MULTIDISP].addEEventListener(new EEventListenerAdapter()
    {
      @Override
      public void touchDown(EEvent ee)
      {
        colorSchemeSelect(LCARS.CS_MULTIDISP);
      }
    });
    add(eCsBtn[LCARS.CS_MULTIDISP]);
    
    eCsBtn[LCARS.CS_PRIMARY] = new ERect(this,524,276,247,38,LCARS.EC_ELBOUP|LCARS.ES_LABEL_E,"PRIMARY");
    eCsBtn[LCARS.CS_PRIMARY].addEEventListener(new EEventListenerAdapter()
    {
      @Override
      public void touchDown(EEvent ee)
      {
        colorSchemeSelect(LCARS.CS_PRIMARY);
      }
    });
    add(eCsBtn[LCARS.CS_PRIMARY]);
    
    eCsBtn[LCARS.CS_SECONDARY] = new ERect(this,524,317,247,38,LCARS.EC_ELBOLO|LCARS.ES_LABEL_E,"SECONDARY");
    eCsBtn[LCARS.CS_SECONDARY].addEEventListener(new EEventListenerAdapter()
    {
      @Override
      public void touchDown(EEvent ee)
      {
        colorSchemeSelect(LCARS.CS_SECONDARY);
      }
    });
    add(eCsBtn[LCARS.CS_SECONDARY]);
    
    eCsBtn[LCARS.CS_ANCILLARY] = new ERect(this,774,276,247,38,LCARS.EC_ELBOUP|LCARS.ES_LABEL_E,"ANCILLARY");
    eCsBtn[LCARS.CS_ANCILLARY].addEEventListener(new EEventListenerAdapter()
    {
      @Override
      public void touchDown(EEvent ee)
      {
        colorSchemeSelect(LCARS.CS_ANCILLARY);
      }
    });
    add(eCsBtn[LCARS.CS_ANCILLARY]);
    
    eCsBtn[LCARS.CS_DATABASE] = new ERect(this,774,317,247,38,LCARS.EC_ELBOLO|LCARS.ES_LABEL_E,"DATABASE");
    eCsBtn[LCARS.CS_DATABASE].addEEventListener(new EEventListenerAdapter()
    {
      @Override
      public void touchDown(EEvent ee)
      {
        colorSchemeSelect(LCARS.CS_DATABASE);
      }
    });
    add(eCsBtn[LCARS.CS_DATABASE]);
    
    add(new ERect(this,1027,276,41,38,LCARS.EC_PRIMARY|LCARS.ES_SELECTED|LCARS.ES_STATIC,null));
    add(new ERect(this,1027,317,41,38,LCARS.EC_PRIMARY|LCARS.ES_SELECTED|LCARS.ES_STATIC,null));
    
    eCsBtn[LCARS.CS_REDALERT] = new ERect(this,1074,276,197,38,LCARS.EC_SECONDARY|LCARS.ES_LABEL_E,"RED ALERT");
    eCsBtn[LCARS.CS_REDALERT].addEEventListener(new EEventListenerAdapter()
    {
      @Override
      public void touchDown(EEvent ee)
      {
        colorSchemeSelect(LCARS.CS_REDALERT);
      }
    });
    add(eCsBtn[LCARS.CS_REDALERT]);
    
    eCsVal = new EValue(this,1274,276,573,38,LCARS.ES_STATIC|LCARS.EC_SECONDARY|LCARS.ES_SELECTED|LCARS.ES_LABEL_E,"COLOR SCHEME");
    eCsVal.setValue("PRIMARY");
    add(eCsVal);
    
    eElbo = new EElbo(this,1074,317,773,100,LCARS.EC_ELBOUP|LCARS.ES_STATIC|LCARS.ES_SHAPE_NW,null);
    eElbo.setArmWidths(88,18); eElbo.setArcWidths(128,64);
    add(eElbo);
    
    ERect eDim = new ERect(this,1074,420,88,140,LCARS.EC_PRIMARY|LCARS.ES_LABEL_SE,"DIM");
    add(eDim);
    
    ERect eLight = new ERect(this,1074,563,88,140,LCARS.EC_PRIMARY|LCARS.ES_SELECTED|LCARS.ES_LABEL_SE,"LIGHT");
    add(eLight);
    
    eRect = new ERect(this,1074,706,88,97,LCARS.EC_PRIMARY|LCARS.ES_LABEL_SE,"BGND");
    eRect.addEEventListener(new EEventListenerAdapter()
    {
      @Override
      public void touchDown(EEvent ee)
      {
        if (getBgImageResource()==null)
          setBgImageResource("de/tucottbus/kt/lcars/resources/images/PanelBackground.png");
        else
          setBgImageResource(null);
      }
    });
    add(eRect);
    
    ERect eSilent = new ERect(this,1074,806,88,251,LCARS.EC_ELBOUP|LCARS.ES_SELECTED|LCARS.ES_LABEL_SE,"SILENT");
    add(eSilent);
    
    add(new ERect(this,1853,276,44,38,LCARS.EC_PRIMARY|LCARS.ES_SELECTED|LCARS.ES_STATIC,null));
    add(new ERect(this,1853,317,44,18,LCARS.EC_PRIMARY|LCARS.ES_SELECTED|LCARS.ES_STATIC,null));
    
    /*
     * The upper-left panel section:
     * 
     * The following code demonstrates the use of the element array contributor.
     * The EElementArray class supplies the array layout, the typical list
     * animation, and paging. The basic way to go is to instantiate the array,
     * to add elements, optionally to attach paging and locking controls and,
     * finally, to add the array to the panel. The example below additionally
     * styles the elements, ERects in this case, of the array individually. 
     */
    EElementArray eArray = new EElementArray(274,43,ERect.class,new Dimension(247,47),4,4,LCARS.ES_NONE,null);
    ((ERect)eArray.add("PRIMARY")).setStyle(LCARS.EC_PRIMARY|LCARS.ES_STATIC);
    ((ERect)eArray.add("SELECTED")).setStyle(LCARS.EC_PRIMARY|LCARS.ES_STATIC|LCARS.ES_SELECTED);
    ((ERect)eArray.add("DISABLED")).setStyle(LCARS.EC_PRIMARY|LCARS.ES_STATIC|LCARS.ES_DISABLED);
    ((ERect)eArray.add("SELECTED/DISABLED")).setStyle(LCARS.EC_PRIMARY|LCARS.ES_STATIC|LCARS.ES_SELDISED);
    ((ERect)eArray.add("SECONDARY")).setStyle(LCARS.EC_SECONDARY|LCARS.ES_STATIC);
    ((ERect)eArray.add("SELECTED")).setStyle(LCARS.EC_SECONDARY|LCARS.ES_STATIC|LCARS.ES_SELECTED);
    ((ERect)eArray.add("DISABLED")).setStyle(LCARS.EC_SECONDARY|LCARS.ES_STATIC|LCARS.ES_DISABLED);
    ((ERect)eArray.add("SELECTED/DISABLED")).setStyle(LCARS.EC_SECONDARY|LCARS.ES_STATIC|LCARS.ES_SELDISED);
    ((ERect)eArray.add("ELBOUP")).setStyle(LCARS.EC_ELBOUP|LCARS.ES_STATIC);
    ((ERect)eArray.add("SELECTED")).setStyle(LCARS.EC_ELBOUP|LCARS.ES_STATIC|LCARS.ES_SELECTED);
    ((ERect)eArray.add("DISABLED")).setStyle(LCARS.EC_ELBOUP|LCARS.ES_STATIC|LCARS.ES_DISABLED);
    ((ERect)eArray.add("SELECTED/DISABLED")).setStyle(LCARS.EC_ELBOUP|LCARS.ES_STATIC|LCARS.ES_SELDISED);
    ((ERect)eArray.add("ELBOLO")).setStyle(LCARS.EC_ELBOLO|LCARS.ES_STATIC);
    ((ERect)eArray.add("SELECTED")).setStyle(LCARS.EC_ELBOLO|LCARS.ES_STATIC|LCARS.ES_SELECTED);
    ((ERect)eArray.add("DISABLED")).setStyle(LCARS.EC_ELBOLO|LCARS.ES_STATIC|LCARS.ES_DISABLED);
    ((ERect)eArray.add("SELECTED/DISABLED")).setStyle(LCARS.EC_ELBOLO|LCARS.ES_STATIC|LCARS.ES_SELDISED);
    eRect = new ERect(this,1274,43,150,197,LCARS.EC_SECONDARY|LCARS.ES_LABEL_SE,"ARRAY\nLOCK");
    add(eRect);
    eArray.setLockControl(eRect);
    eArray.addToPanel(this);
    
    /*
     * The upper-right panel section:
     * 
     * Demonstrates label colors and styles and how to place custom labels over
     * other elements.
     */
    eElbo = new EElbo(this,1274,157,623,116,LCARS.EC_SECONDARY|LCARS.ES_STATIC|LCARS.ES_SHAPE_SE,null);
    eElbo.setArmWidths(150,7); eElbo.setArcWidths(0,0);
    add(eElbo);

    add(new ELabel(this,1427, 99,470,0,LCARS.EF_HEAD2|LCARS.ES_STATIC|LCARS.EC_HEADLINE,"HEADLINE 2"));    
    add(new ELabel(this,1427,154,300,0,LCARS.EF_LARGE|LCARS.ES_STATIC|LCARS.EC_TEXT,"LARGE TEXT"));
    add(new ELabel(this,1427,189,300,0,LCARS.EF_NORMAL|LCARS.ES_STATIC|LCARS.EC_TEXT,"NORMAL TEXT"));
    add(new ELabel(this,1427,215,300,0,LCARS.EF_SMALL|LCARS.ES_STATIC|LCARS.EC_TEXT,"SMALL TEXT"));
    add(new ELabel(this,1427,236,300,0,LCARS.EF_TINY|LCARS.ES_STATIC|LCARS.EC_TEXT,"TINY TEXT"));
    
    ELabel eLabel = new ELabel(this,1730,154,164,0,LCARS.EF_LARGE|LCARS.ES_STATIC|LCARS.EC_TEXT,"LARGE TEXT");
    eLabel.setColor(Color.BLACK);
    add(eLabel);

    eLabel = new ELabel(this,1730,189,164,0,LCARS.EF_NORMAL|LCARS.ES_STATIC|LCARS.EC_TEXT,"NORMAL TEXT");
    eLabel.setColor(Color.BLACK);
    add(eLabel);

    eLabel = new ELabel(this,1730,215,164,0,LCARS.EF_SMALL|LCARS.ES_STATIC|LCARS.EC_TEXT,"SMALL TEXT");
    eLabel.setColor(Color.BLACK);
    add(eLabel);

    eLabel = new ELabel(this,1730,236,164,0,LCARS.EF_TINY|LCARS.ES_STATIC|LCARS.EC_TEXT,"TINY TEXT");
    eLabel.setColor(Color.BLACK);
    add(eLabel);

    /*
     * The lower-right panel section:
     * 
     * Demonstrates how to create elements with a custom shape.
     */
    add(new ELabel(this,1170,355,327,205,LCARS.EF_LARGE|LCARS.EC_PRIMARY|LCARS.ES_STATIC|LCARS.ES_LABEL_SE,"CUSTOM ELEMENT"));
    EElement eElement = new EElement(this,1560,362,0,0,LCARS.EC_SECONDARY,null)
    {
      @Override
      protected Vector<Geometry> createGeometriesInt()
      {
        // Create element geometries vector
        Vector<Geometry> geos = new Vector<Geometry>();
        
        // Create a custom background shape
        // Note: Converted from EPS using pstoedit -f java2 <source>.eps <target>.java
        GeneralPath currentPath = new GeneralPath(GeneralPath.WIND_EVEN_ODD);
        currentPath.moveTo(230.48f, 132.57f);
        currentPath.curveTo(186.105f, 199.41f, 171.137f, 267.473f, 165.953f, 337.578f);
        currentPath.curveTo(194.73f, 300.859f, 220.059f, 276.789f, 238.664f, 265.609f);
        currentPath.curveTo(259.207f, 253.266f, 276.898f, 272.789f, 294.285f, 320.992f);
        currentPath.curveTo(282.746f, 258.09f, 274.77f, 196.531f, 230.48f, 132.57f);
        currentPath.closePath();
        
        // Remove x/y-offset from path
        Rectangle2D pBounds = currentPath.getBounds2D();
        AffineTransform tx = new AffineTransform();
        tx.translate(-pBounds.getX(),-pBounds.getY());
        currentPath.transform(tx);
        
        // Move path to element's panel coordinates and scale to fit bounds
        Rectangle eBounds = getBounds();
        tx = new AffineTransform();
        tx.translate(eBounds.x,eBounds.y);
        currentPath.transform(tx);

        // Add path to the element geometries vector
        geos.add(new GArea(new Area(currentPath),false));
        return geos;
      }
    };
    add(eElement);
    
    add(new ELabel(this,1170,636,327,58,LCARS.EF_LARGE|LCARS.EC_PRIMARY|LCARS.ES_STATIC|LCARS.ES_LABEL_E,"CUSTOMIZED ELEMENT"));
    eElbo = new EElbo(this,1530,634,180,58,LCARS.EC_SECONDARY|LCARS.ES_SHAPE_SW,null);
    eElbo.setArcWidths(84,22); eElbo.setArmWidths(28,38);
    eElbo.addGeometryModifier(new EGeometryModifier()
    {
      @Override
      public void modify(Vector<Geometry> geos)
      {
        Area are = new Area(((GArea)geos.get(0)).getArea());
        Rectangle r = are.getBounds();
        are.subtract(new Area(new Rectangle(r.x+60,r.y,120,43)));
        for (int i=63; i<r.width; i+=6)
          are.add(new Area(new Rectangle(r.x+i,r.y+20,3,20)));
        ((GArea)geos.get(0)).setShape(are);
      }
    });
    add(eElbo);
    
    add(new ELabel(this,1170,706,327,97,LCARS.EF_LARGE|LCARS.EC_PRIMARY|LCARS.ES_STATIC|LCARS.ES_LABEL_SE,"IMAGE"));
    EImage eImage = new EImage(this,1500,700,0,"de/tucottbus/kt/lcars/resources/images/flare.png");
    add(eImage);
    
    add(new ELabel(this,1170,939,327,58,LCARS.EF_LARGE|LCARS.EC_PRIMARY|LCARS.ES_STATIC|LCARS.ES_LABEL_E,"ADVANCED STYLES"));
    eRect = new ERect(this,1530,941,180,58,LCARS.ES_RECT_RND|LCARS.ES_LABEL_W,"CUSTOM COLOR");
    eRect.setColor(new Color(0,255,96));
    add(eRect);
    add(new ERect(this,1530,1002,180,58,LCARS.EC_SECONDARY|LCARS.ES_OUTLINE|LCARS.ES_RECT_RND|LCARS.ES_LABEL_W,"TRANSPARENCY"));
    add(new ELabel(this,1539,1002,180,58,LCARS.EC_SECONDARY|LCARS.ES_LABEL_W,"OUTLINE"));

    // Finish
    setDimContols(eLight,eDim);
    setSilentControl(eSilent);
    colorSchemeSelect(LCARS.CS_PRIMARY);
  }
  
  @Override
  public void fps2()
  {
    LoadStatistics ls;
    try
    {
      ls = getScreen().getLoadStatistics();
      String s = String.format("%02d-%03d",ls.getEventsPerPeriod(),ls.getLoad());
      eGuiLd.setLabel(s);
    }
    catch (RemoteException e)
    {
      eGuiLd.setLabel("00-000");
    }
  }
  
  /**
   * Selects a color scheme.
   * 
   * @param colorScheme
   *          The color scheme, one of the {@link LCARS}<code>.CS_XXX</code>
   *          constants or a negative value to toggle.
   */
  protected void colorSchemeSelect(int colorScheme)
  {
    if (colorScheme<0) colorScheme = (getColorScheme()+1) % (LCARS.CS_MAX+1);
    if (colorScheme==0) colorScheme = 1; // LCARS.CS_KT is just reserved!
    setColorScheme(colorScheme);

    switch(colorScheme)
    {
    case LCARS.CS_PRIMARY  : eCsVal.setValue("PRIMARY"   ); break;
    case LCARS.CS_SECONDARY: eCsVal.setValue("SECONDARY" ); break;
    case LCARS.CS_ANCILLARY: eCsVal.setValue("ANCILLARY" ); break;
    case LCARS.CS_DATABASE : eCsVal.setValue("DATABASE"  ); break;
    case LCARS.CS_MULTIDISP: eCsVal.setValue("MULTI DISP"); break;
    case LCARS.CS_REDALERT : eCsVal.setValue("RED ALERT" ); break;
    default                : eCsVal.setValue("DEFAULT"   ); break;
    }

    for (int i=1; i<=LCARS.CS_MAX; i++)
      eCsBtn[i].setSelected(i==colorScheme);
  }
  
  /**
   * Convenience method: Runs the showcase panel.
   * 
   * @param args
   *          The command line arguments, see {@link LCARS#main(String[])}.
   */
  public static void main(String[] args)
  {
    args = LCARS.setArg(args,"--panel=",ShowcasePanel.class.getCanonicalName());
    args = LCARS.setArg(args,"--nospeech",null);
    LCARS.main(args);
  }

}

// EOF
