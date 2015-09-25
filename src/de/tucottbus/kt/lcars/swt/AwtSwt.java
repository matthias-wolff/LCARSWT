package de.tucottbus.kt.lcars.swt;

import java.awt.Color;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;

import javax.swing.JPanel;

import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Path;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Transform;
import org.eclipse.swt.widgets.Display;
import org.jfree.experimental.swt.SWTUtils;

import de.tucottbus.kt.lcars.LCARS;

/* ===========================================================
 * JFreeChart : a free chart library for the Java(tm) platform
 * ===========================================================
 *
 * (C) Copyright 2000-2006, by Object Refinery Limited and Contributors.
 *
 * Project Info:  http://www.jfree.org/jfreechart/index.html
 *
 * This library is free software; you can redistribute it and/or modify it 
 * under the terms of the GNU Lesser General Public License as published by 
 * the Free Software Foundation; either version 2.1 of the License, or 
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY 
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public 
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, 
 * USA.  
 *
 * [Java is a trademark or registered trademark of Sun Microsystems, Inc. 
 * in the United States and other countries.]
 *
 * ------------------
 * SWTGraphics2D.java
 * ------------------
 * (C) Copyright 2006, by Henry Proudhon and Contributors.
 *
 * Original Author:  Henry Proudhon (henry.proudhon AT insa-lyon.fr);
 * Contributor(s):
 *
 * Changes
 * -------
 * 14-Jun-2006 : New class (HP);
 * 29-Jan-2007 : fixed the fillRect method (HP);
 * 31-Jan-2007 : moved the dummy JPanel to SWTUtils.java,
 *               implemented the drawLine method (HP);
 */
public class AwtSwt
{  
  private final static String Az = "ABCpqr";

  /** A dummy JPanel used to provide font metrics. */
  protected static final JPanel DUMMY_PANEL = new JPanel();
  
  /**
   * Converts an AWT <code>Shape</code> into a SWT <code>Path</code>.
   * 
   * @param shape  the shape.
   * 
   * @return The path.
   */
  public static Path toSwtPath(Shape shape, Device device) {
      int type;
      float[] coords = new float[6];
      Path path = new Path(device);
      PathIterator pit = shape.getPathIterator(null);
      while (!pit.isDone()) {
          type = pit.currentSegment(coords);
          switch (type) {
              case (PathIterator.SEG_MOVETO):
                  path.moveTo(coords[0], coords[1]);
                  break;
              case (PathIterator.SEG_LINETO):
                  path.lineTo(coords[0], coords[1]);
                  break;
              case (PathIterator.SEG_QUADTO):
                  path.quadTo(coords[0], coords[1], coords[2], coords[3]);
                  break;
              case (PathIterator.SEG_CUBICTO):
                  path.cubicTo(coords[0], coords[1], coords[2], 
                          coords[3], coords[4], coords[5]);
                  break;
              case (PathIterator.SEG_CLOSE):
                  path.close();
                  break;
              default:
                  break;
          }
          pit.next();
      }
      return path;
  }
  
  /**
   * Converts an AWT transform into the equivalent SWT transform.
   * 
   * @param awtTransform  the AWT transform.
   * 
   * @return The SWT transform.
   */
  public static Transform toSwtTransform(Device device, AffineTransform awtTransform) {
      Transform t = new Transform(device);
      double[] matrix = new double[6];
      awtTransform.getMatrix(matrix);
      t.setElements((float) matrix[0], (float) matrix[1],
              (float) matrix[2], (float) matrix[3],
              (float) matrix[4], (float) matrix[5]); 
      return t;
  }
  
  /**
   * Converts an AWT transform into the equivalent SWT transform.
   * 
   * @param awtTransform  the AWT transform.
   * 
   * @return The SWT transform.
   */
  public static Transform toTranslatedSwtTransform(Device device, AffineTransform awtTransform, int dx, int dy) {
      Transform t = new Transform(device);
      double[] matrix = new double[6];
      awtTransform.getMatrix(matrix);
      t.setElements((float) matrix[0], (float) matrix[1],
              (float) matrix[2], (float) matrix[3],
              (float) (matrix[4]+dx*matrix[0]), (float) (matrix[5]+dy*matrix[3])); 
      return t;
  }  
  
  /**
   * Create an awt font by converting as much information
   * as possible from the provided swt <code>FontData.
   * <p>Generally speaking, given a font size, an swt font will
   * display differently on the screen than the corresponding awt
   * one. Because the SWT toolkit use native graphical ressources whenever
   * it is possible, this fact is platform dependent. To address
   * this issue, it is possible to enforce the method to return
   * an awt font with the same height as the swt one.
   *
   * @param device The swt device being drawn on (display or gc device).
   * @param fontData The swt font to convert.
   * @param ensureSameSize A boolean used to enforce the same size
   * (in pixels) between the swt font and the newly created awt font.
   * @return An awt font converted from the provided swt font.
   */
  public static java.awt.Font toAwtFont(Device device, FontData fontData,
          boolean ensureSameSize) {
      int height = (int) Math.round(fontData.getHeight() * device.getDPI().y
              / 72.0);
      // hack to ensure the newly created awt fonts will be rendered with the
      // same height as the swt one
      if (ensureSameSize) {
          GC tmpGC = new GC(device);
          Font tmpFont = new Font(device, fontData);
          tmpGC.setFont(tmpFont);
          JPanel DUMMY_PANEL = new JPanel();
          java.awt.Font tmpAwtFont = new java.awt.Font(fontData.getName(),
                  fontData.getStyle(), height);
          if (DUMMY_PANEL.getFontMetrics(tmpAwtFont).stringWidth(Az)
                  > tmpGC.textExtent(Az).x) {
              while (DUMMY_PANEL.getFontMetrics(tmpAwtFont).stringWidth(Az)
                      > tmpGC.textExtent(Az).x) {
                  height--;
                  tmpAwtFont = new java.awt.Font(fontData.getName(),
                          fontData.getStyle(), height);
              }
          }
          else if (DUMMY_PANEL.getFontMetrics(tmpAwtFont).stringWidth(Az)
                  < tmpGC.textExtent(Az).x) {
              while (DUMMY_PANEL.getFontMetrics(tmpAwtFont).stringWidth(Az)
                      < tmpGC.textExtent(Az).x) {
                  height++;
                  tmpAwtFont = new java.awt.Font(fontData.getName(),
                          fontData.getStyle(), height);
              }
          }
          tmpFont.dispose();
          tmpGC.dispose();
      }
      return new java.awt.Font(fontData.getName(), fontData.getStyle(),
              height);
  }

  /**
   * Create an awt font by converting as much information
   * as possible from the provided swt <code>Font.
   *
   * @param device The swt device to draw on (display or gc device).
   * @param font The swt font to convert.
   * @return An awt font converted from the provided swt font.
   */
  public static java.awt.Font toAwtFont(Device device, Font font) {
      FontData fontData = font.getFontData()[0];
      return toAwtFont(device, fontData, true);
  }

  
  
  public static SWTColor toSwtColor(Color color) {
    return new SWTColor(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());        
  }
  
  public static Color toAwtColor(SWTColor color) {
    return new Color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());        
  }
  
  public static java.awt.Font toAwtFont(FontData font, boolean ensureSameSize) {
    return SWTUtils.toAwtFont(LCARS.getDisplay(), font, ensureSameSize);
  }
  
  public static java.awt.Font toAwtFont(FontData font) {
    Display display = Display.getDefault();
    java.awt.Font[] result = new java.awt.Font[1];
    display.syncExec(()-> {result[0] = SWTUtils.toAwtFont(Display.getDefault(), font, true);});    
    return result[0];
  }
  
  public static java.awt.Rectangle toAwtRectangle(Rectangle rectangle) {
    return new java.awt.Rectangle(rectangle.x, rectangle.y, rectangle.width, rectangle.height);
  }
}
