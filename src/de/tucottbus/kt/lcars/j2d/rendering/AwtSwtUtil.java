package de.tucottbus.kt.lcars.j2d.rendering;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;

import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Path;
import org.eclipse.swt.graphics.Transform;

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

public class AwtSwtUtil
{
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
  public static Transform toSwtTransform(AffineTransform awtTransform, Device device) {
      Transform t = new Transform(device);
      double[] matrix = new double[6];
      awtTransform.getMatrix(matrix);
      t.setElements((float) matrix[0], (float) matrix[1],
              (float) matrix[2], (float) matrix[3],
              (float) matrix[4], (float) matrix[5]); 
      return t;
  }

}
