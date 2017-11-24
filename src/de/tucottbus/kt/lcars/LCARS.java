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

package de.tucottbus.kt.lcars;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Area;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.NoSuchObjectException;
import java.rmi.RemoteException;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeSet;
import java.util.Vector;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.TextLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Monitor;
import org.jfree.experimental.swt.SWTUtils;

import de.tucottbus.kt.lcars.geometry.AGeometry;
import de.tucottbus.kt.lcars.geometry.GText;
import de.tucottbus.kt.lcars.logging.ILogObserver;
import de.tucottbus.kt.lcars.logging.Log;
import de.tucottbus.kt.lcars.net.LcarsServer;
import de.tucottbus.kt.lcars.net.NetUtils;
import de.tucottbus.kt.lcars.net.RmiScreenAdapter;
import de.tucottbus.kt.lcars.net.panels.ServerPanel;
import de.tucottbus.kt.lcars.speech.ISpeechEngine;
import de.tucottbus.kt.lcars.speech.SpeechEnginePanel;
import de.tucottbus.kt.lcars.swt.ColorMeta;
import de.tucottbus.kt.lcars.swt.FontMeta;

/**
 * The LCARS main class. Includes the main method, constants, static service
 * methods, and the panel server singleton.
 * 
 * <p>See documentation of the {@linkplain #main(String[]) main method} for a
 * description of supported command line options.</p>
 * 
 * <h3>Remark:</h3>
 * <ul>
 *   <li>TODO: Move questionable stuff to <code>retired</code> package.
 * </ul>
 * 
 * @author Matthias Wolff
 */
public class LCARS
{
  public static final String CLASSKEY = "LCARS";
  
  // ES_XXX - Element styles
  public static final int ES_SHAPE      = 0x0000000F;   // Mask for ES_SHAPE_XXX styles
  public static final int ES_LABEL      = 0x000000F0;   // Mask for ES_LABEL_XXX styles
  public static final int ES_STYLE      = 0x0000FC00;   // Mask for color style
  public static final int ES_COLOR      = 0x0000FF00;   // Mask for EC_XXX styles
  public static final int ES_FONT       = 0x000F0000;   // Mask for EF_XXX styles
  public static final int ES_STATE      = 0x0CF00000;   // Mask for EB_XXX styles
  public static final int ES_BEHAVIOR   = 0x03000000;   // Mask for EB_XXX styles
  public static final int ES_CLASS      = 0xF0000000;   // Mask for class specific styles
  public static final int ES_SELECTED   = 0x00000100;   // Element selected
  public static final int ES_DISABLED   = 0x00000200;   // Element disabled
  public static final int ES_SELDISED   = 0x00000300;   // Element selected and disabled
  public static final int ES_STATIC     = 0x00100000;   // Element does not accept user input
  public static final int ES_BLINKING   = 0x00200000;   // Element blinks
  public static final int ES_MODAL      = 0x00400000;   // Element is always opaque
  public static final int ES_SILENT     = 0x00800000;   // Element does not play an earcon
  public static final int ES_NOLOCK     = 0x04000000;   // Element is does no lock with panel
  public static final int ES_NONE       = 0x00000000;   // Element does not have a style
  
  // ES_SHAPE_XXX - Element shape orientation
  public static final int ES_SHAPE_NW   = 0x00000000;   // Shape oriented to the north-west
  public static final int ES_SHAPE_SW   = 0x00000001;   // Shape oriented to the south-west
  public static final int ES_SHAPE_NE   = 0x00000002;   // Shape oriented to the north-east
  public static final int ES_SHAPE_SE   = 0x00000003;   // Shape oriented to the south-east
  public static final int ES_OUTLINE    = 0x00000008;   // Outline

  // ES_LABEL_XXX - Element label position
  public static final int ES_LABEL_NW   = 0x00000000;   // Label in the north-west
  public static final int ES_LABEL_W    = 0x00000010;   // Label in the west
  public static final int ES_LABEL_SW   = 0x00000020;   // Label in the south-west
  public static final int ES_LABEL_N    = 0x00000030;   // Label in the north
  public static final int ES_LABEL_C    = 0x00000040;   // Label in the center
  public static final int ES_LABEL_S    = 0x00000050;   // Label in the south
  public static final int ES_LABEL_NE   = 0x00000060;   // Label in the north-east
  public static final int ES_LABEL_E    = 0x00000070;   // Label in the east
  public static final int ES_LABEL_SE   = 0x00000080;   // Label in the south-east
  
  // EB_XXX - Element behavior
  public static final int EB_OVERDRAG   = 0x01000000;   // Over-drag behavior
  
  // ES_ELBO_XXX - Elbo element styles
  public static final int ES_ELBO_RECT  = 0x10000000;   // No outer angle
  
  // ES_RECT_XXX - Rect element styles
  public static final int ES_RECT_RND   = 0x30000000;   // Rounded rectangle shape
  public static final int ES_RECT_RND_E = 0x10000000;   // Rounded rectangle shape in the east
  public static final int ES_RECT_RND_W = 0x20000000;   // Rounded rectangle shape in the west

  // ES_VALUE_XXX - Value element styles
  public static final int ES_VALUE_W    = 0x40000000;   // Value left aligned (default is right aligned)
  
  // ES_BROWSER_XXX - Browser element contributor styles
  public static final int ES_BROWSER_NORESTYLEHTML = 0x10000000; // Do not re-style HMTL in LCARS look
  
  // CS_XXX - Color schemes
  public static final int CS_KT         = 0;            // -- Reserved for KT systems --
  public static final int CS_PRIMARY    = 1;            // Primary systems
  public static final int CS_SECONDARY  = 2;            // Secondary systems
  public static final int CS_ANCILLARY  = 3;            // Ancillary systems
  public static final int CS_DATABASE   = 4;            // Database systems
  public static final int CS_MULTIDISP  = 5;            // Multi-Display
  public static final int CS_REDALERT   = 6;            // Red alert
  public static final int CS_MAX        = 6;

  // EC_XXX - Colors
  public static final int EC_ELBOUP     = 0x00000000;   // The upper elbo color (multidisplay)
  public static final int EC_ELBOLO     = 0x00000400;   // The lower elbo color (multidisplay)
  public static final int EC_PRIMARY    = 0x00000800;   // Color of primary elements
  public static final int EC_SECONDARY  = 0x00000C00;   // Color of secondary elements
  public static final int EC_TEXT       = 0x00001000;   // Text color
  public static final int EC_HEADLINE   = 0x00001400;   // Headline text color
  public static final int EC_COUNT      = 24;
  public static final int EC_SHIFT      = 8;
  
  // EF_XXX - Fonts
  public static final int EF_NORMAL     = 0x00000000;   // The normal LCARS font
  public static final int EF_HEAD1      = 0x00010000;   // The primary headline font
  public static final int EF_HEAD2      = 0x00020000;   // The secondary headline font
  public static final int EF_LARGE      = 0x00030000;   // A large font
  public static final int EF_SMALL      = 0x00040000;   // A small font 
  public static final int EF_TINY       = 0x00050000;   // A tiny font 
  public static final int EF_MONO       = 0x00060000;   // -- Reserved --
  public static final int EF_SHIFT      = 16;
  public static final int EF_COUNT      = 6;

  public static final String FN_COMPACTA = "Compacta LT Light";
  public static final String FN_SWISS911 = "Swiss911 UCm BT";

  // -- Fields --
  
  public static boolean SCREEN_DEBUG;
  
  // -- Panel information --
  
  /**
   * The display size in pixels of a new instance of {@link de.tucottbus.kt.lcars.Panel} or an inherited class.
   */
  protected static Dimension panelDim = new Dimension(1920,1080);
  
  
  /**
   * Sets the display size in pixels of a new instance of {@link de.tucottbus.kt.lcars.Panel} or an inherited class
   * @param dim
   */
  public static void setPanelDimension(Dimension dim)
  {
    LCARS.panelDim = dim==null?new Dimension(1920,1080):dim;
    fonts = null;
  }
  
  public static boolean onPADD()
  {
    return getArg("--PADD")!=null;
  }
  
  // -- Color manager --
  private static HashMap<Integer,ColorMeta[]> colorSchemes;

  /**
   * Returns an LCARS color scheme.
   * 
   * @param colorScheme
   *          The color scheme, one of the <code>LCARS.CS_XXX</code> constants.
   */
  private static ColorMeta[] getColors(int colorScheme)
  {
    ColorMeta colors[] = new ColorMeta[EC_COUNT];
    
    ColorMeta cElbos    = null; // Elbos
    ColorMeta cElbosS   = null; // Elbos selected
    ColorMeta cUnavail  = null; // Element unavailable
    ColorMeta cPrimary  = null; // Primary element
    ColorMeta cPrimaryS = null; // Primary element selected
    ColorMeta cColor1   = null; // Auxiliary color 1 (secondary element selected)
    ColorMeta cColor2   = null; // Auxiliary color 2 (secondary element)
    switch (colorScheme)
    {
    case CS_SECONDARY: 
      
      cElbos    = new ColorMeta(0xFFAAA07C, true);
      cUnavail  = new ColorMeta(0xFF5355DE, true);
      cPrimary  = new ColorMeta(0xFF99AAFF, true);
      cPrimaryS = new ColorMeta(0xFFC9E8FD, true);
      cColor1   = new ColorMeta(0xFFFFCC00, true);
      cColor2   = new ColorMeta(0xFFFFFF99, true);
      break;
    case CS_ANCILLARY: 
      cElbos    = new ColorMeta(0xFFF1B1AF, true);
      cUnavail  = new ColorMeta(0xFFA27FA5, true);
      cPrimary  = new ColorMeta(0xFFADACD8, true);
      cColor1   = new ColorMeta(0xFFFFFF33, true);
      cColor2   = new ColorMeta(0xFFE6B0D4, true);
      break;
    case CS_DATABASE: 
      cElbos   = new ColorMeta(0xFFCC6666, true);
      cUnavail = new ColorMeta(0xFFCCCCFF, true);
      cPrimary = new ColorMeta(0xFF99CCFF, true);
      cColor1  = new ColorMeta(0xFFFF9900, true);
      cColor2  = cElbos;
      // Upper elbo
      colors[ EC_ELBOUP                >>EC_SHIFT] = new ColorMeta(0xCC6666);
      colors[(EC_ELBOUP   |ES_SELECTED)>>EC_SHIFT] = new ColorMeta(0xFF9999);
      colors[(EC_ELBOUP   |ES_DISABLED)>>EC_SHIFT] = new ColorMeta(0xCC6666).darker();
      colors[(EC_ELBOUP   |ES_SELDISED)>>EC_SHIFT] = new ColorMeta(0xFF9999).darker();
      // Lower elbo
      colors[ EC_ELBOLO                >>EC_SHIFT] = new ColorMeta(0xCC6666);
      colors[(EC_ELBOLO   |ES_SELECTED)>>EC_SHIFT] = new ColorMeta(0xFF9999);
      colors[(EC_ELBOLO   |ES_DISABLED)>>EC_SHIFT] = new ColorMeta(0xCC6666).darker();
      colors[(EC_ELBOLO   |ES_SELDISED)>>EC_SHIFT] = new ColorMeta(0xFF9999).darker();
      // Primary element
      colors[ EC_PRIMARY               >>EC_SHIFT] = new ColorMeta(0x99CCFF);
      colors[(EC_PRIMARY  |ES_SELECTED)>>EC_SHIFT] = new ColorMeta(0xCCCCFF);
      colors[(EC_PRIMARY  |ES_DISABLED)>>EC_SHIFT] = new ColorMeta(0x3399FF);
      colors[(EC_PRIMARY  |ES_SELDISED)>>EC_SHIFT] = new ColorMeta(0x9999FF);
      // Secondary element
      colors[ EC_SECONDARY             >>EC_SHIFT] = new ColorMeta(0xFF9900);
      colors[(EC_SECONDARY|ES_SELECTED)>>EC_SHIFT] = new ColorMeta(0xFFCC33);
      colors[(EC_SECONDARY|ES_DISABLED)>>EC_SHIFT] = new ColorMeta(0x996600);
      colors[(EC_SECONDARY|ES_SELDISED)>>EC_SHIFT] = new ColorMeta(0x996633);
      // Text
      colors[ EC_TEXT                  >>EC_SHIFT] = new ColorMeta(0xFFFFFF);
      colors[(EC_TEXT     |ES_SELECTED)>>EC_SHIFT] = new ColorMeta(0xFFFFFF);
      colors[(EC_TEXT     |ES_DISABLED)>>EC_SHIFT] = new ColorMeta(0xFFFFFF);
      colors[(EC_TEXT     |ES_SELDISED)>>EC_SHIFT] = new ColorMeta(0xFFFFFF);
      // Headline
      colors[ EC_HEADLINE              >>EC_SHIFT] = new ColorMeta(0xFF9900);
      colors[(EC_HEADLINE |ES_SELECTED)>>EC_SHIFT] = new ColorMeta(0xFF9900);
      colors[(EC_HEADLINE |ES_DISABLED)>>EC_SHIFT] = new ColorMeta(0xFF9900);
      colors[(EC_HEADLINE |ES_SELDISED)>>EC_SHIFT] = new ColorMeta(0xFF9900);
      // Special case: return!
      return colors;
    case CS_REDALERT: {
      int color1 = 0x00FF0066;  // yelling red
      int color2 = 0x00808080;  // grey
      int opaque = 0xFF000000;
      int dimmed = 0x80000000;
      // Upper elbo
      colors[ EC_ELBOUP                >>EC_SHIFT] = new ColorMeta(color2|opaque,true);
      colors[(EC_ELBOUP   |ES_SELECTED)>>EC_SHIFT] = new ColorMeta(color1|opaque,true);
      colors[(EC_ELBOUP   |ES_DISABLED)>>EC_SHIFT] = new ColorMeta(color2|opaque,true);
      colors[(EC_ELBOUP   |ES_SELDISED)>>EC_SHIFT] = new ColorMeta(color1|opaque,true);
      // Lower elbo
      colors[ EC_ELBOLO                >>EC_SHIFT] = new ColorMeta(color1|opaque,true);
      colors[(EC_ELBOLO   |ES_SELECTED)>>EC_SHIFT] = new ColorMeta(color2|opaque,true);
      colors[(EC_ELBOLO   |ES_DISABLED)>>EC_SHIFT] = new ColorMeta(color1|opaque,true);
      colors[(EC_ELBOLO   |ES_SELDISED)>>EC_SHIFT] = new ColorMeta(color2|opaque,true);
      // Primary element
      colors[ EC_PRIMARY               >>EC_SHIFT] = new ColorMeta(color2|opaque,true);
      colors[(EC_PRIMARY  |ES_SELECTED)>>EC_SHIFT] = new ColorMeta(color1|opaque,true);
      colors[(EC_PRIMARY  |ES_DISABLED)>>EC_SHIFT] = new ColorMeta(color2|dimmed,true);
      colors[(EC_PRIMARY  |ES_SELDISED)>>EC_SHIFT] = new ColorMeta(color1|dimmed,true);
      // Secondary element
      colors[ EC_SECONDARY             >>EC_SHIFT] = new ColorMeta(color2|opaque,true);
      colors[(EC_SECONDARY|ES_SELECTED)>>EC_SHIFT] = new ColorMeta(color1|opaque,true);
      colors[(EC_SECONDARY|ES_DISABLED)>>EC_SHIFT] = new ColorMeta(color2|dimmed,true);
      colors[(EC_SECONDARY|ES_SELDISED)>>EC_SHIFT] = new ColorMeta(color1|dimmed,true);
      // Text
      colors[ EC_TEXT                  >>EC_SHIFT] = new ColorMeta(0xFFFFFF);
      colors[(EC_TEXT     |ES_SELECTED)>>EC_SHIFT] = new ColorMeta(0xFFFFFF);
      colors[(EC_TEXT     |ES_DISABLED)>>EC_SHIFT] = new ColorMeta(0xFFFFFF);
      colors[(EC_TEXT     |ES_SELDISED)>>EC_SHIFT] = new ColorMeta(0xFFFFFF);
      // Headline
      colors[ EC_HEADLINE              >>EC_SHIFT] = new ColorMeta(color1|opaque);
      colors[(EC_HEADLINE |ES_SELECTED)>>EC_SHIFT] = new ColorMeta(color1|opaque);
      colors[(EC_HEADLINE |ES_DISABLED)>>EC_SHIFT] = new ColorMeta(color1|opaque);
      colors[(EC_HEADLINE |ES_SELDISED)>>EC_SHIFT] = new ColorMeta(color1|opaque);
      // Special case: return!
      return colors; }
    case CS_MULTIDISP:
      // Upper elbo
      colors[ EC_ELBOUP                >>EC_SHIFT] = new ColorMeta(0xFF9999FF,true);
      colors[(EC_ELBOUP   |ES_SELECTED)>>EC_SHIFT] = new ColorMeta(0xFFCC99CC,true);
      colors[(EC_ELBOUP   |ES_DISABLED)>>EC_SHIFT] = new ColorMeta(0xA09999FF,true);
      colors[(EC_ELBOUP   |ES_SELDISED)>>EC_SHIFT] = new ColorMeta(0xA0CC99CC,true);
      // Lower elbo
      colors[ EC_ELBOLO                >>EC_SHIFT] = new ColorMeta(0xFFCC6666,true);
      colors[(EC_ELBOLO   |ES_SELECTED)>>EC_SHIFT] = new ColorMeta(0xFFFF9900,true);
      colors[(EC_ELBOLO   |ES_DISABLED)>>EC_SHIFT] = new ColorMeta(0xA0CC6666,true);
      colors[(EC_ELBOLO   |ES_SELDISED)>>EC_SHIFT] = new ColorMeta(0xA0FF9900,true);
      // Primary element
      colors[ EC_PRIMARY               >>EC_SHIFT] = new ColorMeta(0xFFCC6666,true);
      colors[(EC_PRIMARY  |ES_SELECTED)>>EC_SHIFT] = new ColorMeta(0xFFFF9900,true);
      colors[(EC_PRIMARY  |ES_DISABLED)>>EC_SHIFT] = new ColorMeta(0xA0CC6666,true);
      colors[(EC_PRIMARY  |ES_SELDISED)>>EC_SHIFT] = new ColorMeta(0xA0FF9900,true);
      // Secondary element
      colors[ EC_SECONDARY             >>EC_SHIFT] = new ColorMeta(0xFFDDB18E,true);
      colors[(EC_SECONDARY|ES_SELECTED)>>EC_SHIFT] = new ColorMeta(0xFFEFC66A,true);
      colors[(EC_SECONDARY|ES_DISABLED)>>EC_SHIFT] = new ColorMeta(0xA0DDB18E,true);
      colors[(EC_SECONDARY|ES_SELDISED)>>EC_SHIFT] = new ColorMeta(0xA0EFC66A,true);
      // Text
      colors[ EC_TEXT                  >>EC_SHIFT] = new ColorMeta(0xFFFFFF);
      colors[(EC_TEXT     |ES_SELECTED)>>EC_SHIFT] = new ColorMeta(0xFFFFFF);
      colors[(EC_TEXT     |ES_DISABLED)>>EC_SHIFT] = new ColorMeta(0xFFFFFF);
      colors[(EC_TEXT     |ES_SELDISED)>>EC_SHIFT] = new ColorMeta(0xFFFFFF);
      // Headline
      colors[ EC_HEADLINE              >>EC_SHIFT] = new ColorMeta(0xFF9900);
      colors[(EC_HEADLINE |ES_SELECTED)>>EC_SHIFT] = new ColorMeta(0xFF9900);
      colors[(EC_HEADLINE |ES_DISABLED)>>EC_SHIFT] = new ColorMeta(0xFF9900);
      colors[(EC_HEADLINE |ES_SELDISED)>>EC_SHIFT] = new ColorMeta(0xFF9900);
      // Special case: return!
      return colors;
    default: // CS_PRIMARY
      cElbos   = new ColorMeta(0xFFF1DF6F,true);
      cUnavail = new ColorMeta(0xFF3399FF,true);
      cPrimary = new ColorMeta(0xFF99CCFF,true);
      cColor1  = new ColorMeta(0xFFFFFF33,true);
      cColor2  = new ColorMeta(0xFFFFFFCC,true);
      break;
    }

    if (cPrimaryS==null) cPrimaryS = cColor2;
    if (cElbosS  ==null) cElbosS   = cColor2;
    
    // Upper elbo
    colors[ EC_ELBOUP                >>EC_SHIFT] = cElbos;
    colors[(EC_ELBOUP   |ES_SELECTED)>>EC_SHIFT] = cElbosS;
    colors[(EC_ELBOUP   |ES_DISABLED)>>EC_SHIFT] = cUnavail;
    colors[(EC_ELBOUP   |ES_SELDISED)>>EC_SHIFT] = cUnavail.brighter();
    // Lower elbo
    colors[ EC_ELBOLO                >>EC_SHIFT] = cElbos;
    colors[(EC_ELBOLO   |ES_SELECTED)>>EC_SHIFT] = cElbosS;
    colors[(EC_ELBOLO   |ES_DISABLED)>>EC_SHIFT] = cUnavail;
    colors[(EC_ELBOLO   |ES_SELDISED)>>EC_SHIFT] = cUnavail.brighter();
    // Primary element
    colors[ EC_PRIMARY               >>EC_SHIFT] = cPrimary;
    colors[(EC_PRIMARY  |ES_SELECTED)>>EC_SHIFT] = cPrimaryS;
    colors[(EC_PRIMARY  |ES_DISABLED)>>EC_SHIFT] = cUnavail;
    colors[(EC_PRIMARY  |ES_SELDISED)>>EC_SHIFT] = cUnavail.brighter();
    // Secondary element
    colors[ EC_SECONDARY             >>EC_SHIFT] = cColor2;
    colors[(EC_SECONDARY|ES_SELECTED)>>EC_SHIFT] = cColor1;
    colors[(EC_SECONDARY|ES_DISABLED)>>EC_SHIFT] = cElbos.darker();
    colors[(EC_SECONDARY|ES_SELDISED)>>EC_SHIFT] = cElbos;
    // Text
    colors[ EC_TEXT                  >>EC_SHIFT] = ColorMeta.WHITE;
    colors[(EC_TEXT     |ES_SELECTED)>>EC_SHIFT] = ColorMeta.WHITE;
    colors[(EC_TEXT     |ES_DISABLED)>>EC_SHIFT] = ColorMeta.WHITE;
    colors[(EC_TEXT     |ES_SELDISED)>>EC_SHIFT] = ColorMeta.WHITE;
    // Headline
    colors[ EC_HEADLINE              >>EC_SHIFT] = cColor1;
    colors[(EC_HEADLINE |ES_SELECTED)>>EC_SHIFT] = cColor1;
    colors[(EC_HEADLINE |ES_DISABLED)>>EC_SHIFT] = cColor1;
    colors[(EC_HEADLINE |ES_SELDISED)>>EC_SHIFT] = cColor1;

    return colors;
  }

  /**
   * Returns a color from the specified LCARS color scheme.
   *
   * @param colorScheme
   *          The color scheme, one of the <code>LCARS.CS_XXX</code> constants.
   * @param style
   *          The LCARS element style, a combination of <code>LCARS.ES_XXX</code> and
   *          <code>LCARS.EC_XXX</code> constants.
   * @return The color (according to the currently selected color scheme) or the black color if
   *         <code>color</code> is invalid.
   */
  public static ColorMeta getColor(int colorScheme, int style)
  {
    if (colorSchemes==null) colorSchemes=new HashMap<Integer,ColorMeta[]>();
    ColorMeta colors[] = colorSchemes.get(new Integer(colorScheme));
    if (colors==null) 
    {
      colors = getColors(colorScheme);
      colorSchemes.put(new Integer(colorScheme),colors);
    }
    int color = (style & ES_COLOR) >> EC_SHIFT;
    if (color<0 || color>=colors.length) return ColorMeta.BLACK;
    return colors[color];
  }

  // -- Font manager --
  private static FontMeta.Explicit[] fonts = null;
  private static Map<String,Boolean> insFnts = new Hashtable<String,Boolean>();

//  /** 
//   * The scaling to get a swt font with the same height as there awt variant
//   */
//  protected static double fontScale; 
//  
//  static {
//    Display display = getDisplay();
//    display.syncExec(() -> {
//      org.eclipse.swt.graphics.Point p = display.getDPI();
//      System.err.println("*** DPI.x="+p.x+", DPI.y="+p.y+" ***");
//      fontScale = 72.0 / display.getDPI().y;
//    });
//  }
  
  /**
   * Determines if the specified font is installed.
   * 
   * @param name
   *          The font name (as used by {@link Font#Font(String, int, int)})
   * @return <code>true</code> if the font is installed, <code>false</code>
   *          otherwise
   */
  public static boolean isFontInstalled(String name)
  {
    if (!insFnts.containsKey(name))
    {
      java.awt.Font f = new java.awt.Font(name,java.awt.Font.PLAIN,24);
      insFnts.put(name,new Boolean(f.getFamily().equals(name)));
    }
    return insFnts.get(name);
  }
  
  /**
   * Returns the name of the actually installed LCARS font.
   * 
   * <p>Originally the (commercial) "Compacta LT Light" font by Linotype was
   * used for LCARS panels. Alternatively the more readily available "Swiss 911
   * Ultra Condensed" font can be used. As both fonts have extremely narrow
   * glyphs there is hardly any other acceptable font.</p>
   * 
   * @param name
   *          {@link #FN_COMPACTA} or {@link #FN_SWISS911}
   * @return <code>name</code> if it is installed, {@link #FN_COMPACTA} or
   *         {@link #FN_SWISS911} (which ever is installed) or "Sans-Serif" if
   *         no acceptable LCARS font is installed.
   */
  public static String getInstalledFont(String name)
  {
    if (name.equals(FN_COMPACTA))
    {
      if (isFontInstalled(FN_COMPACTA)) return FN_COMPACTA;
      if (isFontInstalled(FN_SWISS911)) return FN_SWISS911;
    }
    else if (name.equals(FN_SWISS911))
    {
      if (isFontInstalled(FN_SWISS911)) return FN_SWISS911;
      if (isFontInstalled(FN_COMPACTA)) return FN_COMPACTA;
    }
    return "Sans-Serif";
  }
  
  /**
   * Returns an LCARS font. 
   * 
   * @param style
   *          The LCARS element style, a combination of <code>LCARS.ES_XXX</code>
   *          and <code>LCARS.EC_XXX</code> constants.
   * @return The font.
   */
  public static FontMeta.Explicit getFontMeta(int style)
  {
    if (fonts==null)
    {
      String[] f = {LCARS.getInstalledFont(FN_COMPACTA)};
      
      int    h = 1080;/*LCARS.panelDim.height;*/
      
      FontMeta.Explicit[] fonts = new FontMeta.Explicit[EF_COUNT];
      final Function<Integer, FontMeta.Explicit> newFont = (height) -> {       
        return new FontMeta.Explicit(f[0], height, java.awt.Font.PLAIN);
      };
      
      fonts[EF_HEAD1 >>EF_SHIFT] = newFont.apply((int)Math.round(h/10.)); //12.0
      fonts[EF_HEAD2 >>EF_SHIFT] = newFont.apply((int)Math.round(h/16.)); //25.0
      fonts[EF_LARGE >>EF_SHIFT] = newFont.apply((int)Math.round(h/28.));
      
      /* --> */
      if (f[0].equals(FN_COMPACTA))
        f[0] = LCARS.getInstalledFont(FN_SWISS911);
      
      fonts[EF_NORMAL >>EF_SHIFT] = newFont.apply((int)Math.round(h/37.5));
      fonts[EF_SMALL >>EF_SHIFT] = newFont.apply((int)Math.round(h/50.));
      fonts[EF_TINY >>EF_SHIFT] = newFont.apply((int)Math.round(h/65.));                                
      LCARS.fonts = fonts;
    }
    int font = (style & ES_FONT) >> EF_SHIFT;
    if (font<0 || font>=fonts.length) return fonts[0];
    return fonts[font];
  }
  
  /**
   * Returns an LCARS font with a custom font size.
   * 
   * @param style
   *          The LCARS element style, a combination of <code>LCARS.ES_XXX</code>
   *          and <code>LCARS.EC_XXX</code> constants.
   * @param size
   *          The size of the font in LCARS panel pixels.
   * @return The font.
   */
  public static FontMeta.Explicit getFontMeta(int style, int size)
  {
    return new FontMeta.Explicit(getFontMeta(style),size);
  }

  // -- Cursor manager --
  
  /**
   * Returns a blank cursor.
   */
  public static Cursor createBlankCursor(Display display)
  {
    ImageData sourceData = new ImageData(16, 16, 1,
        new PaletteData(new RGB[] {
            display.getSystemColor(SWT.COLOR_WHITE).getRGB(),
            display.getSystemColor(SWT.COLOR_BLACK).getRGB()
        }));
    sourceData.transparentPixel = 0;
    return new Cursor(display, sourceData, 0, 0);
  }
  
  // -- Panel selector utilities
  
  /**
   * List of panel classes to be displayed on the panel selector dialog.
   */
  private static AbstractList<Class<? extends Panel>> panelSelectorList;
  static
  {
    panelSelectorList = getMainPanelClasses();
  }

  /**
   * Returns a list of all available LCARS main panels.
   */
  private static AbstractList<Class<? extends Panel>> getMainPanelClasses()
  {
    ArrayList<Class<? extends Panel>> l = new ArrayList<Class<? extends Panel>>();

    // Find and add main panel classes 
    final Class<? extends Panel> subclass = onPADD() ? PaddMainPanel.class : MainPanel.class;
    try
    {
      for (Class<?> cl : getClassesInPackage("",null))
      {
        if (!subclass.isAssignableFrom(cl)) continue;
        Class<? extends Panel> clazz = cl.asSubclass(subclass);
        if (clazz.equals(Panel.class)) continue;
        if (Modifier.isAbstract(clazz.getModifiers())) continue;
        l.add(clazz);
      }
    }
    catch (Exception e)
    {
      Log.err("Creating list of all loadable LCARS main panels failed.", e);
    }

    return l;
  }
  
  /**
   * Returns the list of panel classes to be displayed on the panel selector dialog.
   * 
   * @see #addToPanelSelectorList(Class)
   */
  public static AbstractList<Class<? extends Panel>> getPanelSelectorList()
  {
    return panelSelectorList;
  }

  /**
   * Adds a panel class to the list of panel classes to be displayed on the
   * panel selector dialog.
   * 
   * @param panelClass
   *          The panel class. If the argument is already on the list, the 
   *          method will do nothing.
   */
  public static void addToPanelSelectorList(Class<? extends Panel> panelClass)
  {
    if (panelSelectorList.contains(panelClass))
      return;
    panelSelectorList.add(panelClass);
  }

  /**
   * Removes a panel class from the list of panel classes to be displayed on the
   * panel selector dialog.
   * 
   * @param panelClass
   *          The panel class.
   * @return <code>true</code> if an element was removed from the list,
   *         <code>false</code> otherwise.
   */
  public static boolean removeFromPanelSelectorList(Class<? extends Panel> panelClass)
  {
    if (!panelSelectorList.contains(panelClass))
      return false;
    return panelSelectorList.remove(panelClass);
  }
  
  // -- Service methods --  
  
  /**
   * Computes the shape of a text.
   * 
   * @param fnt
   *          The font.
   * @param text
   *          The text.
   * @return The shape.
   */
  public static TextLayout getTextLayout(Font font, String text)  
  {    
    if (text==null || text.length()==0) return null;
    TextLayout tl = new TextLayout(font.getDevice());
    tl.setFont(font);
    tl.setText(text);
    return tl;
  }

  /**
   * Computes the bounding rectangle of a text.
   * 
   * @param meta
   *          The font descriptor.
   * @param text
   *          The text.
   */
  public static Rectangle getTextBounds(FontMeta meta, String text) 
  {    
    if (text == null || text == "") return new Rectangle();
    Font font = meta.getFont();    
    TextLayout lt = new TextLayout(font.getDevice());
    lt.setFont(font);
    lt.setText(text);
    org.eclipse.swt.graphics.Rectangle result = lt.getBounds();
    lt.dispose();
    return new Rectangle(result.x, result.y, result.width, result.height);    
  }
  
  /**
   * Creates the 2D geometry/geometries of a multi-line text.
   * 
   * @param text
   *          The text.
   * @param bounds
   *          The bounding box to align the text to.
   * @param style
   *          The text position, one of the {@link LCARS}<code>.ES_LABEL_XXX</code> constants.
   * @param insets
   *          The margin which the text should keep from the bounding box; may be <code>null</code>.
   */
  public static ArrayList<AGeometry> createTextGeometry2D
  (
    String              text,
    java.awt.Rectangle  bounds,
    int                 style,
    Point               insets,
    boolean             foreground    
  )
  {
    return createTextGeometry2D(getFontMeta(style), text, bounds, style, insets, foreground);
  }

  /**
   * Creates the 2D geometry/geometries of a multi-line text.
   * 
   * @param fnt
   *          The font.
   * @param text
   *          The text.
   * @param bounds
   *          The bounding box to align the text to.
   * @param style
   *          The text position, one of the {@link LCARS}<code>.ES_LABEL_XXX</code> constants.
   * @param insets
   *          The margin which the text should keep from the bounding box; may be <code>null</code>.
   */
  public static ArrayList<AGeometry> createTextGeometry2D
  (
    FontMeta            fontMeta,
    String              text,
    java.awt.Rectangle  bounds,
    int                 style,
    Point               insets,
    boolean             foreground    
  )
  {
    //TODO: This implementation is entirely wrong. Re-do it (see
    //      http://pawlan.com/monica/articles/texttutorial/other.html)!
    
    ArrayList<AGeometry> geos = new ArrayList<AGeometry>(); 
    if (text==null || text.length()==0 || bounds==null) return geos;
    if (insets==null) insets = new Point(0,0);
    
    
    // Measure text lines
    TextLayout tl = new TextLayout(fontMeta.getFont().getDevice());
    String s[] = text.split("\n");
    
    Font font = fontMeta.getFont();
    tl.setFont(font);
    tl.setText(text);
    
    org.eclipse.swt.graphics.Rectangle tlBnds = tl.getBounds();
    
    int align = (style & ES_LABEL) >> 4;
    if(align > ES_LABEL_SE)
      align = ES_LABEL_NW >> 4;
    
    // Position the text box
    int tx = bounds.x+insets.x;
    int ty = bounds.y+insets.y;
    int tw = bounds.width-insets.x*2;
    int th = bounds.height-insets.y*2;
    
    int tlx;
    int tly;
        
    //if (tw <= 0 || th <= 0) return geos;
    
    switch (align / 3) // horizontal alignment
    {
      case 0: // left
        tl.setAlignment(SWT.LEFT);
        tlx = tx;
        break;
      case 1: // middle
        tl.setAlignment(SWT.CENTER);
        tlx = tx + (tw-tlBnds.width)/2;
        break;
      case 2: // right
        tl.setAlignment(SWT.RIGHT);        
        tlx = tx + tw-tlBnds.width;
        break;
      default: return geos;
    }
     
//    System.err.println("*** ASCENT   : "+tl.getAscent());
//    System.err.println("*** DESCENT  : "+tl.getDescent());
//    System.err.println("*** LEADING  : "+tl.getLineMetrics(0).getLeading());
//    System.err.println("*** L_ASCENT : "+tl.getLineMetrics(0).getAscent());
//    System.err.println("*** L_DESCENT: "+tl.getLineMetrics(0).getDescent());
    
    switch (align % 3) // vertical alignment
    {
      case 0: // top
        tly = ty-tl.getLineMetrics(0).getLeading();
        break;  
      case 1: // middle
        tly = ty + (th-tlBnds.height-tl.getDescent()+tl.getLineMetrics(0).getLeading())/2;
        break;
      case 2: // bottom
        tly = ty + th-tlBnds.height;
        break;
      default: return geos;
    }
            
    int n = tl.getLineCount();
        
    for (int i=0; i<n; i++)
    {
      org.eclipse.swt.graphics.Rectangle linBnds = tl.getLineBounds(i);
      //if (linBnds.y > th) break; // line out of vertical bounds
      int x = linBnds.x+tlx;
      int y = linBnds.y+tly;
      
      Rectangle b = new Rectangle(Math.max(x, tx), Math.max(y, ty), Math.min(linBnds.width, tw-linBnds.x), Math.min(linBnds.height, th-linBnds.y));
      //if (b.width <= 0 || b.height <=0) continue;
      
      try
      {
        GText gt = new GText(
            s[i],
            b,
            fontMeta,
            foreground);
        if (x < tx) gt.setIndent(x-tx);
        if (y < ty) gt.setDescent(y-ty);
        //TODO:
        geos.add(gt);
      }
      catch (Exception e)
      {
        Log.err("Invalid line index "+i+" rendering text \""+text+"\"");
      }
    }
    
    tl.dispose();
    return geos;
  }

  /**
   * Interpolates two colors.
   * 
   * @param clr1  the first color
   * @param clr2  the second color
   * @param value interpolation value; 0 (first color) ... 1 (second color)
   * @return the interpolated color
   */
  public static ColorMeta interpolateColors(ColorMeta clr1, ColorMeta clr2, float value)
  {
    if (value<=0f) return clr1;
    if (value>=1f) return clr2;
    final float norm = 1/255f;

    float val2 = value*norm;
    float val1 = norm - val2;
    return new ColorMeta(
        val1*clr1.getRed  () +val2*clr2.getRed  (),
        val1*clr1.getGreen() +val2*clr2.getGreen(),
        val1*clr1.getBlue () +val2*clr2.getBlue (),
        val1*clr1.getAlpha() +val2*clr2.getRed  ());
  }
  
  /**
   * Returns a list of all loadable speech engine implementations.
   */
  public static Vector<Class<?>> getSpeechEngines()
  {
    Vector<Class<?>> l = new Vector<Class<?>>();
    try
    {
      for (Class<?> cl : getClassesInPackage("",null))
        try
        {
          Class<?> claszz = cl.asSubclass(ISpeechEngine.class);
          if (Modifier.isAbstract(claszz.getModifiers())) continue;
          l.add(claszz);
        }
        catch (Exception e) {}
    }
    catch (Exception e)
    {
      Log.err("Cannot get any loadable speech engine implementation.", e);
    }
    return l;
  }
  
  /**
   * Scans all classes accessible from the context class loader which belong to
   * the given package and subpackages. Adapted from
   * http://snippets.dzone.com/posts/show/4831 and extended to support use of
   * JAR files.
   * 
   * @param packageName
   *          The base package.
   * @param regexFilter
   *          An optional class name pattern.
   * @return The classes
   */
  public static Class<?>[] getClassesInPackage(String packageName, String regexFilter)
  {
    Pattern regex = null;
    if (regexFilter != null)
      regex = Pattern.compile(regexFilter);
    try
    {
      String path = packageName.replace('.', '/');
      Enumeration<URL> resources = ClassLoader.getSystemResources(path);
      List<String> dirs = new ArrayList<String>(1000);
      while (resources.hasMoreElements())
      {
        URL resource = resources.nextElement();
        dirs.add(resource.getFile());
      }
      TreeSet<String> classes = new TreeSet<String>();
      for (String directory : dirs)
        classes.addAll(findClasses(directory, packageName, regex));

      ArrayList<Class<?>> classList = new ArrayList<Class<?>>(1000);
      for (String clazz : classes)
        try
        {
          classList.add(Class.forName(clazz));
        }
        catch (ClassNotFoundException e)
        {
          Log.err("Class \""+clazz+"\" is not accessible", e);
        }

      // FIXME: empty base package name does not work for JAR files!
      //    --> Find at least system classes in package(s) de.* -->
      if (path.isEmpty() && classList.isEmpty())
        return getClassesInPackage("de",regexFilter);
      // <--

      return classList.toArray(new Class[classes.size()]);
    } catch (Exception e)
    {
      Log.err("Cannot get class from package \"" + packageName + "\" using the regular expression \"" + regexFilter + "\"");
      return null;
    }
  }
    
  /**
   * Recursive method used to find all classes in a given path (directory or zip
   * file URL). Directories are searched recursively.
   * 
   * Adapted from http://snippets.dzone.com/posts/show/4831 and extended to
   * support use of JAR files
   * 
   * @param path
   *          The base directory or URL from which to search.
   * @param packageName
   *          The package name for classes found inside the base directory.
   * @param regex
   *          An optional class name pattern. e.g. .*Test
   * @return The classes.
   * @throws MalformedURLException
   *           if no protocol is specified, or an unknown protocol is found, or
   *           spec is <code>null</code>.
   * @throws IOException
   *           if an I/O exception occurs.
   */
  private static TreeSet<String> findClasses
  (
    String  path,
    String  packageName,
    Pattern regex
  )
  throws MalformedURLException, IOException
  {
    TreeSet<String> classes = new TreeSet<String>();
    if (path.startsWith("file:") && path.contains("!"))
    {
      String[] split = path.split("!");
      URL jar = new URL(split[0]);
      ZipInputStream zip = new ZipInputStream(jar.openStream());
      ZipEntry entry;
      while ((entry = zip.getNextEntry()) != null)
      {
        if (entry.getName().endsWith(".class"))
        {
          String className = entry.getName().replaceAll("[$].*", "")
              .replaceAll("[.]class", "").replace('/', '.');
          if (className.startsWith(packageName)
              && (regex == null || regex.matcher(className).matches()))
            classes.add(className);
        }
      }
    }
    File dir = new File(path);
    if (!dir.exists())
    {
      return classes;
    }
    File[] files = dir.listFiles();
    for (File file : files)
    {
      if (file.isDirectory())
      {
        assert !file.getName().contains(".");
        classes.addAll(findClasses(file.getAbsolutePath(), packageName + "."
            + file.getName(), regex));
      } else if (file.getName().endsWith(".class"))
      {
        String className = packageName + '.'
            + file.getName().substring(0, file.getName().length() - 6);
        if (regex == null || regex.matcher(className).matches())
        {
          if (className.startsWith(".")) className=className.substring(1);
          classes.add(className);
        }
      }
    }
    return classes;
  }
  
  /**
   * Returns a resource file (a file bundled with Java packages). The method
   * does not check if the file actually exists.
   * 
   * @param pckg the package name
   * @param file the file name
   * @return The file.
   * @throws FileNotFoundException
   *           If the resource file was not found.
   */
  public static File getResourceFile(String pckg, String file)
  throws FileNotFoundException
  {
    if (file==null) return null;
    pckg = (pckg!=null ? pckg.replace(".","/")+"/" : "");
    return getResourceFile(pckg+file);
  }
  
  /**
   * Returns a resource file (a file bundled with Java packages). The method
   * does not check if the file actually exists.
   * 
   * @param file
   *          The file name (the package separator '.' must be replaced by a
   *          slash '/')
   * @return The file.
   * @throws FileNotFoundException
   *           If the resource file was not found.
   */
  public static File getResourceFile(String file) throws FileNotFoundException
  {
    if (file==null) return null;
    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    URL resource = classLoader.getResource(file);
    if (resource==null) throw new FileNotFoundException(file);
    try
    {
      File f = new File(resource.getFile());
      if (!f.exists()) throw new FileNotFoundException(file);
      return f;
    }
    catch (Exception e)
    {
      FileNotFoundException e2 = new FileNotFoundException(file+" ("+e.toString()+")");
      e2.initCause(e);
      throw e2;
    }
  }
  
  /**
   * Loads a text file and returns it as a string.
   * 
   * @param file the file
   * @return the contents
   * @deprecated Use {@link #loadTextResource(String)} instead!
   */
  @Deprecated
  public static String loadTextFile(File file) throws FileNotFoundException
  {
    if (file==null) return null;
    StringBuilder text = new StringBuilder();
    String NL = System.getProperty("line.separator");
    Scanner scanner = new Scanner(file);
    try
    {
      while (scanner.hasNextLine())
        text.append(scanner.nextLine() + NL);
    }
    finally
    {
      scanner.close();
    }

    return text.toString();
  }
  
  /**
   * Loads a text resource.
   * 
   * @param name
   *          The resource name, e.&nbsp;g. "de/tucottbus/kt/lcars/ge/GE.html".
   * @throws FileNotFoundException if the resource could not be found.
   * @return The text or <code>null</code> if the parameter is <code>null</code>.
   */
  public static String loadTextResource(String name) throws FileNotFoundException
  {
    InputStream is = LCARS.class.getClassLoader().getResourceAsStream(name);
    if (is==null) throw new FileNotFoundException(name);
    StringBuilder text = new StringBuilder();
    String NL = System.getProperty("line.separator");
    Scanner scanner = new Scanner(is);
    try
    {
      while (scanner.hasNextLine())
        text.append(scanner.nextLine() + NL);
    }
    finally
    {
      scanner.close();
    }
    return text.toString();
  }
  
  /**
   * Abbreviates a string.
   * 
   * @param string
   *          The string to abbreviate.
   * @param length
   *          The maximal length (including the possibly included ellipsis mark "...").
   * @param left
   *          If <code>true</code> abbreviate from the beginning, otherwise abbreviate from the right.
   * @return The abbreviated string.
   */
  public static String abbreviate(String string, int length, boolean left)
  {
    if (string==null) return null;
    if (string.length()<=length) return string;
    if (left)
      return "..."+string.substring(string.length()-length-3);
    else
      return string.substring(0,length-3)+"...";
  }
  
  // -- Static methods --
  
  /**
   * Runs a <code>runnable</code> after a short period of time.
   * 
   * @param runnable
   *          The runnable.
   */
  public static void invokeLater(Runnable runnable)
  {
    invokeLater(runnable,1);
  }

  /**
   * Runs a <code>runnable</code> after a <code>delay</code> milliseconds.
   * 
   * @param runnable
   *          The runnable.
   * @param delay
   *          The delay in milliseconds
   */
  public static void invokeLater(Runnable runnable, long delay)
  {
    String name = "LCARS.invokeLater";
    try
    {
      name += "@"+Thread.currentThread().getStackTrace()[2];
    } catch(Exception e) 
    {
    }
    Thread thread = new Thread(name){
      @Override
      public void run()
      {
        try { Thread.sleep(delay); } catch (InterruptedException e) {}
        runnable.run();
      }
    };
    thread.setDaemon(true);
    thread.start();
  }

  /**
   * Scans the command line for the first argument with a specified prefix.
   * 
   * @param prefix
   *          The prefix to scan for.
   * @return The remaining argument (without the prefix) or <code>null</code> if no argument with
   *         this prefix was found.
   */
  public static String getArg(String prefix)
  {
    return LCARS.getArg(LCARS.args,prefix);
  }
  
  /**
   * Scans the given command line for the first argument with a specified prefix. Note: this method
   * solely works on the committed string array, the actual command line or the static field {@link
   * LCARS#args} is ignored!
   * 
   * @param args
   *          The command line arguments.
   * @param prefix
   *          The prefix to scan for.
   * @return The remaining argument (without the prefix) or <code>null</code> if no argument with
   *         this prefix was found.
   */
  public static String getArg(String[] args, String prefix)
  {
    if (args==null) return null;
    if (prefix==null || prefix.equals("")) return null;
    for (int i=0; i<args.length; i++)
      if (args[i].startsWith(prefix))
        return args[i].substring(prefix.length());
    return null;
  }

  	/**
	 * Adds or changes a command line argument. Note: this method solely works
	 * on the committed string array, the actual command line or the static
	 * field {@link LCARS#args} will not be changed! A typical usage of this
	 * method is to modify the command line arguments before calling the
	 * {@link LCARS#main(String[])} method.
	 * 
	 * @param args
	 *            The command line arguments.
	 * @param prefix
	 *            The prefix of the argument to add or change, e.g.
	 *            <code>"--key="</code>. If an argument with this prefix is
	 *            already present, the method will replace it.
	 * @param suffix
	 *            The suffix of the argument to add or change, e.g.
	 *            <code>"value"</code>.
	 * @return The modified command line arguments.
	 */
  public static String[] setArg(String[] args, String prefix, String suffix)
  {
    ArrayList<String> largs = new ArrayList<String>(args.length);
    for (String arg : args)
      if (!arg.startsWith(prefix))
        largs.add(arg);
    largs.add(prefix+(suffix!=null?suffix:""));
    return largs.toArray(args);
  }
  
  /**
   * Returns the LCARS SWT display.
   */
  public static Display getDisplay() 
  {
    return Display.getDefault();
  }

  // -- LCARS main function --

  private static IScreen  iscreen;
  private static String[] args;
  
  /**
   * The LCARS main method.
   * <h3>Usage</h3>
   * <p><code>java -cp "target/lcarswt-&lt;version&gt;.&lt;profile&gt;-jar-with-dependencies.jar" de.tucottbus.kt.lcars.LCARS [options]</code></p>
   * <p>Note: Fat jar archive built by Maven goal <code>package</code>. Profiles supported: Win86, Win64, Linux86, Linux64, MacOSX86, and MacOSX64.</p>
   * 
   * @param args
   *  Command line options<pre>
   *  --clientof=hostname        - Serve a remote screen [1]
   *  --debug                    - Print debug messages
   *  --device=devicename        - Name of host device, e.g. wetab [2]
   *  --help, -h, ?              - Print help and exit
   *  --mode=[fullscreen|window] - Screen mode (default: fullscreen)
   *  --nogui                    - Do not display a screen [3]
   *  --nomouse                  - Hide mouse cursor
   *  --nospeech                 - Disable speech I/O
   *  --PADD                     - Running on a PADD
   *  --panel=classname          - LCARS panel to display at start-up 
   *  --rminame=name             - RMI name (default: &lt;hostname&gt;) [4]
   *  --screen=n                 - Use n-th screen (default: 1) [5]
   *  --server                   - Serve remote panels [1]
   *  --wallpaper=filename       - Use wall paper (slower!)
   *  --xpos=n                   - Horizontal position of window [6]
   *  
   *  [1] mutually exclusive
   *  [2] currently the only use is --device=wetab which adjusts PADD-panels
   *  [3] only valid with --server
   *  [4] useful when multiple NICs are installed in a host
   *  [5] implies --mode=fullscreen
   *  [6] valid with --mode=maximized for displaying panel at secondary screen
   *  </pre>
   */
  public static void main(String[] args)
  {
		// To detect loss of network connection this timeout is introduced.
		// This setting is specific to Java 1.8 - see the following URL for details:
		// http://docs.oracle.com/javase/8/docs/technotes/guides/rmi/sunrmiproperties.html
		System.setProperty("sun.rmi.transport.tcp.responseTimeout", "1000");

    LCARS.args = args;
    
    if (getArg("--help")!=null || getArg("-h")!=null || getArg("?")!=null)
    {
      System.out.print("\n----------------------------------------------------------------------------");
      System.out.print("\nLCARS");
      System.out.print("\n----------------------------------------------------------------------------");
      System.out.print("\n\nUsage");
      System.out.print("\n\n  java -cp \"target/lcarswt-<version>.<profile>-jar-with-dependencies.jar\" de.tucottbus.kt.lcars.LCARS [options]");
      System.out.print("\n\nCommand line options");
      System.out.print("\n  --clientof=hostname        - Serve a remote screen [1]");
      System.out.print("\n  --debug                    - Print debug messages");
      System.out.print("\n  --device=devicename        - Name of host device, e.g. wetab [2]");
      System.out.print("\n  --help, -h, ?              - Print help and exit");
      System.out.print("\n  --mode=[fullscreen|window] - Screen mode (default: fullscreen)");
      System.out.print("\n  --musiclib=<music-dir>     - Audio library folder");
      System.out.print("\n  --nogui                    - Do not display a screen [3]");
      System.out.print("\n  --nomouse                  - Hide mouse cursor");
      System.out.print("\n  --nospeech                 - Disable speech I/O");
      System.out.print("\n  --PADD                     - Running on a PADD");
      System.out.print("\n  --panel=classname          - LCARS panel to display at start-up"); 
      System.out.print("\n  --rminame=name             - RMI name (default: &lt;hostname&gt;) [4]");
      System.out.print("\n  --screen=n                 - Use n-th screen (default: 1) [5]");
      System.out.print("\n  --server                   - Serve remote panels [1]");
      System.out.print("\n  --wallpaper=filename       - Use wall paper (slower!)");
      System.out.print("\n  --xpos=n                   - Horizontal position of window [6]");
      System.out.print("\n  ");
      System.out.print("\n  [1] mutually exclusive");
      System.out.print("\n  [2] currently the only use is --device=wetab which adjusts PADD-panels");
      System.out.print("\n  [3] only valid with --server");
      System.out.print("\n  [4] useful when multiple NICs are installed in a host");
      System.out.print("\n  [5] implies --mode=fullscreen");
      System.out.print("\n  [6] valid with --mode=maximized for displaying panel at secondary screen");
      System.out.print("\n----------------------------------------------------------------------------");
      System.out.print("\n\n");
      return;
    }
    
    Log.DebugMode = getArg("--debug") != null;
    LCARS.SCREEN_DEBUG = getArg("--screenDebug") != null;
    
    Log.addObserver(new ILogObserver()
    {
      private void doLog(String pfx, String msg, Boolean err) 
      {
        if ("LCARS".equals(pfx) || "NET".equals(pfx))
          ServerPanel.logMsg(pfx,msg,err);       
      }
      
      @Override
      public void log(String pfx, String msg)
      {
        doLog(pfx, msg, false);
      }

      @Override
      public void warn(String pfx, String msg)
      {
        doLog(pfx, msg, false);
      }

      @Override
      public void err(String pfx, String msg, Throwable e)
      {
        doLog(pfx, msg, true);
      }
      
      @Override
      public void err(String pfx, String msg)
      {
        doLog(pfx, msg, true);
      }
      
      @Override
      public void debug(String pfx, String msg)
      {
        doLog(pfx, msg, false);
      }
    });
    
    try
    {
      if (getArg("--nogui")==null)
      {
        boolean fullscreen = !("window".equals(getArg("--mode=")));
        Display display = LCARS.getDisplay();

        String scrIdArg = getArg("--screen=");
        if (scrIdArg!=null)
        {
          fullscreen = true;
          Monitor[] monitors = display.getMonitors();
          int scrid = 0;
          try
          {
            scrid = Math.max(Math.min(Integer.parseInt(scrIdArg)-1, monitors.length), 0);
          }
          catch (NumberFormatException e)
          {
            Log.err("Invalis screen ID \""+scrIdArg+"\", using default screen.");
          }
          iscreen = new Screen(display,fullscreen);
          iscreen.setArea(new Area(SWTUtils.toAwtRectangle(monitors[scrid].getBounds())));
        }
        else
          iscreen = new Screen(display,fullscreen);
      }
      else
        Log.info("Command line mode (no GUI)");

      // Install shut-down hook
      Runtime.getRuntime().addShutdownHook(new Thread()
      {
        @Override
        public void run()
        {
          // Intentionally left blank
        }
      });
      
      // Check network command line options
      if (getArg("--server")!=null && getArg("--clientof")!=null)
      {
        Log.err("FATAL ERROR cannot be client and server at the same time");
        Log.err("Use either \"--clientof\" or \"--server\"!");
        System.exit(-1);
      }
      
      // Start LCARS server (command line option "--server")
      if (getArg("--server")!=null)
        LcarsServer.start();
      
      // Start LCARS client (command line option "--clientof")
      String clientOf = getArg("--clientof=");
      if (clientOf!=null)
      {
        Log.info("client of "+clientOf+" at "+NetUtils.getHostName());
        NetUtils.getRmiRegistry();
        iscreen = new RmiScreenAdapter((Screen)iscreen,clientOf);
      }
      
      // Add special panels to panel selector list
      if (LCARS.getArg("--server")!=null)
        LCARS.addToPanelSelectorList(ServerPanel.class);
      if (LCARS.getArg("--nospeech")==null)
        LCARS.addToPanelSelectorList(SpeechEnginePanel.class);

      // Create initial panel and run screen
      if (iscreen!=null)
      {
        try
        {
          String pcn = getArg("--panel=");
          if (pcn==null) {
            pcn = getArg("--server")!=null
                ? ServerPanel.class.getName()
                : Panel.class.getName();
          } 
          iscreen.setPanel(pcn);          
        }
        catch (ClassNotFoundException | RemoteException e)
        {
          Log.err("Error while initiation.", e);
        }
      }

      // Run SWT event loop 
      while (iscreen==null || !iscreen.isDisposed())
      {
        try
        {
          if (!getDisplay().readAndDispatch())
            getDisplay().sleep();
        }
        catch (Exception e)
        {
          Log.err("Error in screen execution.", e);
        }
      }
    }
    catch (Exception e)
    {
      Log.err("Uncaught exception in LCARS main.", e);
    }
    
    // Shut down    
    System.out.println("[Shutting down...]");

    // - Local screen and panel
    if (iscreen!=null)
    {
      try
      {
        IPanel panel = iscreen.getPanel();
        if (panel!=null)
        {
          System.out.println("[Shutting down panel...]");
          try
          {
            panel.stop();
            System.out.println("[...Panel shut down]");
          } catch (NoSuchObjectException e) { 
            // Because RMI has already been shut down -> ignore
          }
          catch (Exception e)
          {
            System.out.println("[...FAILED to shut down local panel]");
            e.printStackTrace();
          }
        }
      }
      catch (Exception e)
      {
      }
      
      // - RMI screen adapter
      if (iscreen instanceof RmiScreenAdapter)
      {
        System.out.println("[Shutting down RMI screen adapter...]");
        ((RmiScreenAdapter)iscreen).shutDown();
        System.out.println("[...RMI screen adapter shut down]");
      }
        iscreen = null;
    }

    // - RMI panel adapters
    LcarsServer.shutDown();

    // - Speech engine
    System.out.println("[Disposing speech engine...]");
    Panel.disposeSpeechEngine();
    System.out.println("[...Speech engine disposed]");
    
    // - The end
    System.out.println("[...Shut down complete]");
    Log.info("END OF LCARS MAIN");
    //System.exit(0); // HACK: Hard off; should not be necessary!
  }
}
