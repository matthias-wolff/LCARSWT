package de.tucottbus.kt.lcars.j2d;

import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.util.Vector;

import de.tucottbus.kt.lcars.contributors.ElementContributor;


/**
 * A {@linkplain EGeometryModifier geometry modifier} realizing a perspective projection.
 * 
 * @author Matthias Wolff
 */
public class EPerspective implements EGeometryModifier
{
  private float x;
  private float y;
  private float[][] m;

  /**
   * Creates a new perspective projection.
   * 
   * @param matrix
   *          The projection matrix, see <a
   *          href="http://en.wikipedia.org/wiki/Transformation_matrix#Perspective_projection"
   *          >http://en.wikipedia.org/wiki/Transformation_matrix#Perspective_projection</a> for
   *          details.
   * @param x
   *          The x-coordinate offset in panel coordinates (for use with {@link ElementContributor}
   *          s, 0 otherwise).
   * @param y
   *          The y-coordinate offset in panel coordinates (for use with {@link ElementContributor}
   *          s, 0 otherwise).
   */
  public EPerspective(float[][] matrix, float x, float y)
  {
    this.m = matrix;
    this.x = x;
    this.y = y;
  }

  /**
   * Transforms a point.
   * 
   * @param point
   *          The original point.
   * @return The transformed point.
   */
  public Point2D.Float transform(Point2D.Float point)
  {
    float[] o = { point.x-this.x, point.y-this.y, 1, 1 };
    float[] t = { 0, 0, 0, 0 };
    
    for (int i=0; i<4; i++)
      for (int j=0; j<4; j++)
        t[i] += m[i][j]*o[j];

    Point2D.Float newPoint = new Point2D.Float(t[0]/t[3]+this.x,t[1]/t[3]+this.y);
    //System.out.print("\nw="+t[3]+", x="+newPoint.x+", y="+newPoint.y);
    return newPoint;
  }
  
  /**
   * Transforms a shape.
   * <p><b>Author:</b> Tom Nelson
   * (<code>edu.uci.ics.jung.visualization.jai</code>)</p>
   * 
   * @param shape
   *          The original shape.
   * @return The transformed shape.
   */
  public Shape transform(Shape shape)
  {
    GeneralPath newPath = new GeneralPath();
    float[] coords = new float[6];
    PathIterator iterator = shape.getPathIterator(null);
    for (; iterator.isDone()==false; iterator.next())
    {
      int type = iterator.currentSegment(coords);
      switch (type)
      {
      case PathIterator.SEG_MOVETO:
        Point2D p = transform(new Point2D.Float(coords[0],coords[1]));
        newPath.moveTo((float)p.getX(),(float)p.getY());
        break;
      case PathIterator.SEG_LINETO:
        p = transform(new Point2D.Float(coords[0],coords[1]));
        newPath.lineTo((float)p.getX(),(float)p.getY());
        break;
      case PathIterator.SEG_QUADTO:
        p = transform(new Point2D.Float(coords[0],coords[1]));
        Point2D q = transform(new Point2D.Float(coords[2],coords[3]));
        newPath.quadTo((float)p.getX(),(float)p.getY(),(float)q.getX(),
            (float)q.getY());
        break;
      case PathIterator.SEG_CUBICTO:
        p = transform(new Point2D.Float(coords[0],coords[1]));
        q = transform(new Point2D.Float(coords[2],coords[3]));
        Point2D r = transform(new Point2D.Float(coords[4],coords[5]));
        newPath.curveTo((float)p.getX(),(float)p.getY(),(float)q.getX(),
            (float)q.getY(),(float)r.getX(),(float)r.getY());
        break;
      case PathIterator.SEG_CLOSE:
        newPath.closePath();
        break;
      }
    }
    return newPath;
  }

  // -- Implementation of the EGeometryModifier interface --
  
  @Override
  public void modify(Vector<Geometry> geos)
  {
    for (Geometry geo : geos)
      if (geo instanceof GArea)
      {
        GArea are = (GArea)geo;
        are.setShape(new Area(transform(new Area(are.shape))));
      }
  }
  
}

// EOF
