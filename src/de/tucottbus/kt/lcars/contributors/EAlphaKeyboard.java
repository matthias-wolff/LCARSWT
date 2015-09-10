package de.tucottbus.kt.lcars.contributors;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;

import de.tucottbus.kt.lcars.LCARS;
import de.tucottbus.kt.lcars.elements.EElement;
import de.tucottbus.kt.lcars.elements.ERect;
import de.tucottbus.kt.lcars.elements.ESector;
import de.tucottbus.kt.lcars.j2d.EGeometryModifier;
import de.tucottbus.kt.lcars.j2d.GArea;
import de.tucottbus.kt.lcars.j2d.Geometry;

public class EAlphaKeyboard extends EKeyboard
{
  
  private Point navi;
  private int   naviR;
  
  public EAlphaKeyboard(int x, int y, int keyHeight, int style)
  {
    super(x,y,keyHeight,style);
  }

  /**
   * Layout and key map:
   * http://upload.wikimedia.org/wikipedia/commons/thumb/2/22/KB_US-International.svg/1000px-KB_US-International.svg.png 
   */
  @Override
  protected KeyMap createKeyMap()
  {
    KeyMap keyMap = new KeyMap();

    // Row 1
    keyMap.put(KeyEvent.VK_BACK_QUOTE   ,'`','~'        );
    keyMap.put(KeyEvent.VK_1            ,'1','!','¡','¹');
    keyMap.put(KeyEvent.VK_2            ,'2','@','²'    );
    keyMap.put(KeyEvent.VK_3            ,'3','#','³'    );
    keyMap.put(KeyEvent.VK_4            ,'4','$','¤','£');
    keyMap.put(KeyEvent.VK_5            ,'5','%'        );
    keyMap.put(KeyEvent.VK_6            ,'6','^','¼'    );
    keyMap.put(KeyEvent.VK_7            ,'7','&','½'    );
    keyMap.put(KeyEvent.VK_8            ,'8','*','¾'    );
    keyMap.put(KeyEvent.VK_9            ,'9','('        );
    keyMap.put(KeyEvent.VK_0            ,'0',')'        );
    keyMap.put(KeyEvent.VK_MINUS        ,'-','_','¥'    );
    keyMap.put(KeyEvent.VK_PLUS         ,'+','=','×','÷');
    
    // Row 2
    keyMap.put(KeyEvent.VK_Q            ,'q','Q','ä','Ä');
    keyMap.put(KeyEvent.VK_W            ,'w','W','å','Å');
    keyMap.put(KeyEvent.VK_E            ,'e','E','é','É');
    keyMap.put(KeyEvent.VK_R            ,'r','R','®'    );
    keyMap.put(KeyEvent.VK_T            ,'t','T','þ','Þ');
    keyMap.put(KeyEvent.VK_Y            ,'y','Y','ü','Ü');
    keyMap.put(KeyEvent.VK_U            ,'u','U','ú','Ú');
    keyMap.put(KeyEvent.VK_I            ,'i','I','í','Í');
    keyMap.put(KeyEvent.VK_O            ,'o','O','ó','Ó');
    keyMap.put(KeyEvent.VK_P            ,'p','P','ö','Ö');
    keyMap.put(KeyEvent.VK_OPEN_BRACKET ,'[','{','«'    );
    keyMap.put(KeyEvent.VK_CLOSE_BRACKET,']','}','»'    );
    keyMap.put(KeyEvent.VK_BACK_SLASH   ,'\\','|','¬','¦');
    
    // Row 3
    keyMap.put(KeyEvent.VK_A            ,'a','A','á','Á');
    keyMap.put(KeyEvent.VK_S            ,'s','S','ß','§');
    keyMap.put(KeyEvent.VK_D            ,'d','D','ð','Ð');
    keyMap.put(KeyEvent.VK_F            ,'f','F'        );
    keyMap.put(KeyEvent.VK_G            ,'g','G'        );
    keyMap.put(KeyEvent.VK_H            ,'h','H'        );
    keyMap.put(KeyEvent.VK_J            ,'j','J'        );
    keyMap.put(KeyEvent.VK_K            ,'k','K'        );
    keyMap.put(KeyEvent.VK_L            ,'l','L','ø','Ø');
    keyMap.put(KeyEvent.VK_SEMICOLON    ,';',',','¶','°');
    keyMap.put(KeyEvent.VK_QUOTE        ,'\'','\"','´','¨');

    // Row 4
    keyMap.put(KeyEvent.VK_Z            ,'z','Z','æ','Æ');
    keyMap.put(KeyEvent.VK_X            ,'x','X'        );
    keyMap.put(KeyEvent.VK_C            ,'c','C','©','¢');
    keyMap.put(KeyEvent.VK_V            ,'v','V'        );
    keyMap.put(KeyEvent.VK_B            ,'b','B'        );
    keyMap.put(KeyEvent.VK_N            ,'n','N','ñ','Ñ');
    keyMap.put(KeyEvent.VK_M            ,'m','M'        );
    keyMap.put(KeyEvent.VK_COMMA        ,',','<','ç','Ç');
    keyMap.put(KeyEvent.VK_STOP         ,'.','>'        );
    keyMap.put(KeyEvent.VK_SLASH        ,'/','?'        );

    // Row 5
    keyMap.put(KeyEvent.VK_SPACE        ,' ',' '        );
    
    return keyMap;
  }
  
  /**
   * Layout and key map:
   * http://upload.wikimedia.org/wikipedia/commons/thumb/2/22/KB_US-International.svg/1000px-KB_US-International.svg.png 
   */  
  @Override
  protected void layout()
  {
    EElement  e;
    Rectangle rect;
    int ST_BLUE  = LCARS.EC_ELBOUP|LCARS.ES_LABEL_C;
    int ST_YELLO = LCARS.EC_SECONDARY|LCARS.ES_LABEL_C;
    int ST_BROWN = LCARS.EC_PRIMARY|LCARS.ES_LABEL_C;
    
    // Initialize Fields
    int cx     = (int)((keyHeight+3)*17.5);
    int cy     = (int)((keyHeight+3)*3.5)-1;
    this.navi  = new Point(cx,cy);
    this.naviR = (keyHeight+3)*5/2;    
    int r0     = naviR;
    
    // The main keyboard;
    EKeyboard.Key[][] layout = 
    {
      { // Row 1
        new EKeyboard.Key(KeyEvent.VK_BACK_QUOTE   ,1.00f,null  ),
        new EKeyboard.Key(KeyEvent.VK_1            ,1.00f,null  ),
        new EKeyboard.Key(KeyEvent.VK_2            ,1.00f,null  ),
        new EKeyboard.Key(KeyEvent.VK_3            ,1.00f,null  ),
        new EKeyboard.Key(KeyEvent.VK_4            ,1.00f,null  ),
        new EKeyboard.Key(KeyEvent.VK_5            ,1.00f,null  ),
        new EKeyboard.Key(KeyEvent.VK_6            ,1.00f,null  ),
        new EKeyboard.Key(KeyEvent.VK_7            ,1.00f,null  ),
        new EKeyboard.Key(KeyEvent.VK_8            ,1.00f,null  ),
        new EKeyboard.Key(KeyEvent.VK_9            ,1.00f,null  ),
        new EKeyboard.Key(KeyEvent.VK_0            ,1.00f,null  ),
        new EKeyboard.Key(KeyEvent.VK_MINUS        ,1.00f,null  ),
        new EKeyboard.Key(KeyEvent.VK_PLUS         ,1.00f,null  ),
        new EKeyboard.Key(KeyEvent.VK_BACK_SPACE   ,2.00f,"BKSP"),
      },
      { // Row 2
        new EKeyboard.Key(KeyEvent.VK_TAB          ,1.50f,"TAB" ),
        new EKeyboard.Key(KeyEvent.VK_Q            ,1.00f,null  ),
        new EKeyboard.Key(KeyEvent.VK_W            ,1.00f,null  ),
        new EKeyboard.Key(KeyEvent.VK_E            ,1.00f,null  ),
        new EKeyboard.Key(KeyEvent.VK_R            ,1.00f,null  ),
        new EKeyboard.Key(KeyEvent.VK_T            ,1.00f,null  ),
        new EKeyboard.Key(KeyEvent.VK_Y            ,1.00f,null  ),
        new EKeyboard.Key(KeyEvent.VK_U            ,1.00f,null  ),
        new EKeyboard.Key(KeyEvent.VK_I            ,1.00f,null  ),
        new EKeyboard.Key(KeyEvent.VK_O            ,1.00f,null  ),
        new EKeyboard.Key(KeyEvent.VK_P            ,1.00f,null  ),
        new EKeyboard.Key(KeyEvent.VK_OPEN_BRACKET ,1.00f,null  ),
        new EKeyboard.Key(KeyEvent.VK_CLOSE_BRACKET,1.00f,null  ),
        new EKeyboard.Key(KeyEvent.VK_BACK_SLASH   ,1.50f,null  ),
      },
      { // Row 3
        new EKeyboard.Key(KeyEvent.VK_CAPS_LOCK    ,1.75f,"CAPS"),
        new EKeyboard.Key(KeyEvent.VK_A            ,1.00f,null  ),
        new EKeyboard.Key(KeyEvent.VK_S            ,1.00f,null  ),
        new EKeyboard.Key(KeyEvent.VK_D            ,1.00f,null  ),
        new EKeyboard.Key(KeyEvent.VK_F            ,1.00f,null  ),
        new EKeyboard.Key(KeyEvent.VK_G            ,1.00f,null  ),
        new EKeyboard.Key(KeyEvent.VK_H            ,1.00f,null  ),
        new EKeyboard.Key(KeyEvent.VK_J            ,1.00f,null  ),
        new EKeyboard.Key(KeyEvent.VK_K            ,1.00f,null  ),
        new EKeyboard.Key(KeyEvent.VK_L            ,1.00f,null  ),
        new EKeyboard.Key(KeyEvent.VK_SEMICOLON    ,1.00f,null  ),
        new EKeyboard.Key(KeyEvent.VK_QUOTE        ,1.00f,null  ),
        new EKeyboard.Key(KeyEvent.VK_ENTER        ,2.25f,"ENT" ),
      },
      { // Row 4
        new EKeyboard.Key(KeyEvent.VK_SHIFT        ,2.25f,"SHIFT"),
        new EKeyboard.Key(KeyEvent.VK_Z            ,1.00f,null   ),
        new EKeyboard.Key(KeyEvent.VK_X            ,1.00f,null   ),
        new EKeyboard.Key(KeyEvent.VK_C            ,1.00f,null   ),
        new EKeyboard.Key(KeyEvent.VK_V            ,1.00f,null   ),
        new EKeyboard.Key(KeyEvent.VK_B            ,1.00f,null   ),
        new EKeyboard.Key(KeyEvent.VK_N            ,1.00f,null   ),
        new EKeyboard.Key(KeyEvent.VK_M            ,1.00f,null   ),
        new EKeyboard.Key(KeyEvent.VK_COMMA        ,1.00f,null   ),
        new EKeyboard.Key(KeyEvent.VK_STOP         ,1.00f,null   ),
        new EKeyboard.Key(KeyEvent.VK_SLASH        ,1.00f,null   ),
        new EKeyboard.Key(KeyEvent.VK_SHIFT        ,2.75f,"SHIFT"),
      },
      { // Row 5
        new EKeyboard.Key(KeyEvent.VK_SPACE        ,7.00f,null   ),
        new EKeyboard.Key(KeyEvent.VK_ALT_GRAPH    ,2.00f,"SYM"  ),
      }
    };
    addRow(layout[0]);
    addRow(layout[1]);
    addRow(layout[2]);
    addRow(layout[3]);  
    addRow(layout[4],3.75f);

    // Style some of the keys
    // - VK_BACK_QUOTE
    e = findKeys(KeyEvent.VK_BACK_QUOTE).get(0).e;
    e.setStyle(e.getStyle()|LCARS.ES_RECT_RND_W);
    
    // - VK_TAB
    e = findKeys(KeyEvent.VK_TAB).get(0).e;
    e.setStyle((e.getStyle()&~LCARS.ES_COLOR)|LCARS.ES_RECT_RND_W|LCARS.EC_ELBOUP);

    // - VK_CAPS_LOCK
    e = findKeys(KeyEvent.VK_CAPS_LOCK).get(0).e;
    e.setStyle((e.getStyle()&~LCARS.ES_COLOR)|LCARS.ES_RECT_RND_W|LCARS.EC_ELBOUP);
    
    // - VK_SHIFT (left)
    e = findKeys(KeyEvent.VK_SHIFT).get(0).e;
    e.setStyle((e.getStyle()&~LCARS.ES_COLOR)|LCARS.ES_RECT_RND_W|LCARS.EC_ELBOUP);

    // -VK_SPACE
    e = findKeys(KeyEvent.VK_SPACE).get(0).e;
    //e.setStyle(e.getStyle()|LCARS.ES_RECT_RND_W);

    // - VK_BACK_SPACE
    e = findKeys(KeyEvent.VK_BACK_SPACE).get(0).e;
    e.setStyle((e.getStyle()&~LCARS.ES_COLOR)|LCARS.EC_ELBOUP);
    rect = e.getBounds(); rect.width+=37; e.setBounds(rect);
    e.addGeometryModifier(new EGeometryModifier()
    {
      public void modify(ArrayList<Geometry> geos)
      {
        int   cx  = navi.x+x;
        int   cy  = navi.y+y;
        Area  sh  = ESector.sector(cx,cy,2*naviR,naviR+3,110,129.0f,3);
        Area  ex  = ESector.limit(sh,cx,cy,naviR,(int)(1.4*naviR),0);
        Area  are = ((GArea)geos.get(0)).getArea();
        are.add(ex);
        ((GArea)geos.get(0)).setShape(are);
      }
    });

    // - VK_SLASH
    e = findKeys(KeyEvent.VK_BACK_SLASH).get(0).e;
    rect = e.getBounds(); rect.width+=14; e.setBounds(rect);
    e.addGeometryModifier(new EGeometryModifier()
    {
      public void modify(ArrayList<Geometry> geos)
      {
        int   cx  = navi.x+x;
        int   cy  = navi.y+y;
        Area  sh  = ESector.sector(cx,cy,2*naviR,naviR+3,129.0f,148.8f,3);
        Area  ex  = new Area(ESector.limit(sh,cx,cy,naviR,(int)(0.995*naviR),0));
        Area  are = ((GArea)geos.get(0)).getArea();
        are.add(ex);
        ((GArea)geos.get(0)).setShape(are);
      }
    });

    // - VK_ENTER
    e = findKeys(KeyEvent.VK_ENTER).get(0).e;
    e.setStyle((e.getStyle()&~LCARS.ES_COLOR)|LCARS.EC_ELBOUP);
    rect = e.getBounds(); rect.width+=8; e.setBounds(rect);
    e.addGeometryModifier(new EGeometryModifier()
    {
      public void modify(ArrayList<Geometry> geos)
      {
        int   cx  = navi.x+x;
        int   cy  = navi.y+y;
        Area  sh  = ESector.sector(cx,cy,2*naviR,naviR+3,148.8f,180,3);
        Area  ex  = new Area(ESector.limit(sh,cx,cy,naviR,2*naviR,0));
        Area  are = new Area(((GArea)geos.get(0)).getArea());
        are.add(ex);
        ((GArea)geos.get(0)).setShape(are);
      }
    });
    
    // - VK_SHIFT (right)
    e = findKeys(KeyEvent.VK_SHIFT).get(1).e;
    rect = e.getBounds(); rect.width+=11; e.setBounds(rect);
    e.setStyle((e.getStyle()&~LCARS.ES_COLOR)|LCARS.EC_ELBOUP);

    // - VK_ALT_GRAPH
    e = findKeys(KeyEvent.VK_ALT_GRAPH).get(0).e;
    e.setStyle((e.getStyle()&~LCARS.ES_COLOR)|LCARS.ES_RECT_RND_E|LCARS.EC_ELBOUP);
    
    // The navigation control
    e = new ESector(null,cx,cy,r0,0,0,90,keyHeight+6,ST_BLUE,"ESC");
    addKey(KeyEvent.VK_ESCAPE,e);
    e = new ESector(null,cx,cy,r0,0,90,180,keyHeight+6,ST_BLUE,"DEL");
    addKey(KeyEvent.VK_DELETE,e);
    e = new ESector(null,cx,cy,r0,0,180,270,keyHeight+6,ST_BLUE,"CLR");
    addKey(KeyEvent.VK_CLEAR,e);
    e = new ESector(null,cx,cy,r0,0,270,360,keyHeight+6,ST_BLUE,"ENT");
    addKey(KeyEvent.VK_ENTER,e);
    e = new ERect(null,cx-keyHeight/2,cy-keyHeight/2,keyHeight,keyHeight,ST_YELLO,"HOME");
    add(e);
    e = new ERect(null,cx-5*keyHeight/2-4,cy-keyHeight/2,keyHeight-2,keyHeight,ST_BROWN,"POS1");
    addKey(KeyEvent.VK_HOME,e);
    e = new ERect(null,cx-3*keyHeight/2-3,cy-keyHeight/2,keyHeight,keyHeight,ST_BROWN,"LEFT");
    addKey(KeyEvent.VK_LEFT,e);
    e = new ERect(null,cx-keyHeight/2,cy-3*keyHeight/2-3,keyHeight,keyHeight,ST_BROWN,"UP");
    addKey(KeyEvent.VK_UP,e);
    e = new ERect(null,cx-keyHeight/2,cy-5*keyHeight/2-9,keyHeight,keyHeight+3,ST_BROWN,"PG UP");
    e.addGeometryModifier(new TrimToRadiusModifier());
    addKey(KeyEvent.VK_PAGE_UP,e);
    e = new ERect(null,cx-keyHeight/2,cy+keyHeight/2+3,keyHeight,keyHeight,ST_BROWN,"DOWN");
    addKey(KeyEvent.VK_DOWN, e);
    e = new ERect(null,cx-keyHeight/2,cy+3*keyHeight/2+6,keyHeight,keyHeight+3,ST_BROWN,"PG DN");
    e.addGeometryModifier(new TrimToRadiusModifier());
    addKey(KeyEvent.VK_PAGE_DOWN,e);
    e = new ERect(null,cx+keyHeight/2+3,cy-keyHeight/2,keyHeight,keyHeight,ST_BROWN,"RIGHT");
    addKey(KeyEvent.VK_RIGHT,e);
    e = new ERect(null,cx+3*keyHeight/2+6,cy-keyHeight/2,keyHeight+3,keyHeight,ST_BROWN,"END");
    e.addGeometryModifier(new TrimToRadiusModifier());
    addKey(KeyEvent.VK_END,e);
    e = new ESector(null,cx,cy,(int)(1.7f*r0),r0+3,11.5f,53,3,ST_BROWN,"OK");
    ((ESector)e).setLabelPos((int)(1.17*r0),-(int)(0.70*r0));
    addKey(KeyEvent.VK_ACCEPT,e);
    e = new ESector(null,cx,cy,(int)(1.35f*r0),r0+3,-11.5f,11.5f,3,ST_BLUE|LCARS.ES_STATIC,"WST");
    add(e);
    e = new ESector(null,cx,cy,3*r0,r0+3,-50,-11.5f,3,ST_YELLO,"CANCEL");
    ((ESector)e).setLimit((int)(2.32f*r0),(int)(1.1f*r0),0);
    ((ESector)e).setLabelPos((int)(2.04f*r0),(int)(0.94f*r0));
    addKey(KeyEvent.VK_CANCEL,e);
  }

  protected int getModifiers()
  {
    int modifiers = 0;
    if (isKeySelected(KeyEvent.VK_SHIFT)||isKeySelected(KeyEvent.VK_CAPS_LOCK))
      modifiers |= InputEvent.SHIFT_DOWN_MASK;
    if (isKeySelected(KeyEvent.VK_ALT_GRAPH))
      modifiers |= InputEvent.ALT_GRAPH_DOWN_MASK;
    return modifiers;
  }
 
  class TrimToRadiusModifier implements EGeometryModifier
  {
    public void modify(ArrayList<Geometry> geos)
    {
      int  cx   = navi.x+x;
      int  cy   = navi.y+y;
      int  r    = naviR;
      Area are  = new Area(((GArea)geos.get(0)).getArea());
      Area trim = new Area(new Ellipse2D.Float(cx-r,cy-r,2*r,2*r));
      are.intersect(trim);
      ((GArea)geos.get(0)).setShape(are);
    }
  }  
}
