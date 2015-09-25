package de.tucottbus.kt.lcars.contributors;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.TimerTask;
import java.util.Vector;

import de.tucottbus.kt.lcars.LCARS;
import de.tucottbus.kt.lcars.Panel;
import de.tucottbus.kt.lcars.elements.EElbo;
import de.tucottbus.kt.lcars.elements.EElement;
import de.tucottbus.kt.lcars.elements.EEvent;
import de.tucottbus.kt.lcars.elements.EEventListenerAdapter;
import de.tucottbus.kt.lcars.elements.EImage;
import de.tucottbus.kt.lcars.elements.ELabel;
import de.tucottbus.kt.lcars.elements.ERect;
import de.tucottbus.kt.lcars.swt.ColorMeta;
import de.tucottbus.kt.lcars.swt.ImageMeta;

/**
 * A simple topographic map with a background image, a sector grid, point shaped
 * objects and a cursor brace.
 * 
 * <p> The layout anchor point of this contributor is the top left corner
 * specified by the constructor's <code>lBounds</code> parameter. The layout
 * will not exceed this bounding rectangle.</p>
 * 
 * @author Matthias Wolff
 */
public class ETopography extends ElementContributor
{
  // Common topography fields
  private Rectangle         lBounds;
  private Rectangle2D.Float pBounds;
  private String            pUnit;
  private int               style;
  private AffineTransform   pTx;

  // Grid fields
  private int               gridStyle;
  private Point2D.Float     pGridMajor;
  private float             gridMajorAlpha;
  @SuppressWarnings("unused")
  private Point2D.Float     pGridMinor;
  @SuppressWarnings("unused")
  private float             gridMinorAlpha;

  // Points fields
  private Vector<ERect>     points;

  // Map image fields
  private ImageMeta            mapImage;
  private Rectangle2D.Float mapImageBounds;
  private int               mapStyle;
  
  // Cursor fields
  private Vector<EElement>  cursor;
  private int               cursorSize;
  private Point2D.Float     cursorPos;
  
  private static final String TT_CURSORSLIDE = "CURSORSLIDE";

  /**
   * Creates a new topographic map contributor.
   * 
   * @param x
   *          The x-coordinate of the upper left corner (in LCARS panel pixels).
   * @param y
   *          The y-coordinate of the upper left corner (in LCARS panel pixels).
   * @param w
   *          The width (in LCARS panel pixels).
   * @param h
   *          The height (in LCARS panel pixels).
   * @param style
   *          The LCARS style (see class {@link LCARS}). Only the font and color attributes will be
   *          used.
   */
  public ETopography(int x, int y, int w, int h, int style)
  {
    super(x,y);
    this.lBounds        = new Rectangle(x,y,w,h);
    this.style          = style&(LCARS.ES_FONT|LCARS.ES_COLOR);
    this.points         = new Vector<ERect>();    
    this.cursor         = new Vector<EElement>();
    this.gridStyle      = -1;
    this.gridMajorAlpha = 0.3f;
    this.gridMinorAlpha = 0.3f;
    this.mapStyle       = -1;
  }

  /**
   * Returns the logical bounds of this topographic map.
   */
  public Rectangle getLogicalBounds()
  {
    return lBounds;
  }
  
  /**
   * Sets the physical bounds of this topographic map.
   * 
   * @param bounds
   *          the bounding rectangle in physical units (left, top, width, height) 
   * @param unit
   *          the name of the physical unit, e.g. "m"  
   * @param layout
   *          <code>true</code> to recompute the layout (this will make the
   *          changes visible)
   */
  public void setPhysicalBounds(Rectangle2D.Float bounds, String unit, boolean layout)
  {
    this.pUnit = unit;
    if (this.pBounds!=null && this.pBounds.equals(bounds)) return;
    this.pBounds = bounds;

    // Compute scale and inflate physical bounds to match the logical aspect ratio
    double scale;
    double scaleX = lBounds.getWidth ()/pBounds.getWidth ();
    double scaleY = lBounds.getHeight()/pBounds.getHeight();
    if (scaleX>scaleY)
    {
      double inflateX = (lBounds.width-scaleY*pBounds.width)/scaleY;
      pBounds.x-=inflateX/2; pBounds.width+=inflateX;
      scale = scaleY;
    }
    else
    {
      double inflateY = (lBounds.height-scaleX*pBounds.height)/scaleX;
      pBounds.y-=inflateY/2; pBounds.height+=inflateY;
      scale = scaleX;
    }
    
    // Compute the logical origin position
    //float ox = -pBounds.x*lBounds.width/pBounds.width;
    //float oy = -pBounds.y*lBounds.height/pBounds.height;
    //this.lOrigin  = new Point(Math.round(ox),Math.round(oy));
    
    // Compute placement transform and relayout
    pTx = new AffineTransform();
    pTx.scale(scale,-scale);
    pTx.translate(-pBounds.x,-pBounds.y-pBounds.height);
    if (layout) layout();
  }
  
  /**
   * Returns the physical bounds of this topographic map.
   */
  public Rectangle2D.Float getPhysicalBounds()
  {
    return pBounds;
  }

  /**
   * Returns the physical unit.
   */
  public String getPhysicalUnit()
  {
    return pUnit;
  }
  
  /**
   * Sets a new topographic map image, e.g. a satellite photo.
   * 
   * @param imageFile
   *          the image file
   * @param bounds
   *          the physicals bounds of the area showed on the image
   * @param layout
   *          <code>true</code> to recompute the layout (this will make the
   *          changes visible)
   */
  public void setMapImage(ImageMeta imageMeta, Rectangle2D.Float bounds, boolean layout)
  {
    this.mapImage       = imageMeta;
    this.mapImageBounds = bounds;
    if (layout) layout();
  }

  /**
   * Sets the map image's color style.
   * 
   * @param style
   *          The new color style (see class {@link LCARS}). Only the color attributes will be used.
   *          A value of -1 turns off the styling and makes the image being used as supplied (this
   *          is the default behavior).
   * @param layout
   *          <code>true</code> to recompute the layout (this will make the changes visible)
   */
  public void setMapStyle(int style, boolean layout)
  {
    this.mapStyle = style & LCARS.ES_COLOR;
    if (layout) layout();
  }
  
  /**
   * Sets grid intervals.
   * 
   * @param major
   *          major grid intervals in physical units
   * @param minor
   *          -- reserved, must be <code>null</code> --
   * @param layout
   *          <code>true</code> to recompute the layout (this will make the changes visible)
   */
  public void setGrid(Point2D.Float major, Point2D.Float minor, boolean layout)
  {
    this.pGridMajor = major;
    this.pGridMinor = minor;
    if (layout) layout();
  }

  /**
   * Sets the grid's color style, font style and opacity.
   * 
   * @param style
   *          The new grid style (see class {@link LCARS}). Only the color and font attributes will
   *          be used. A value of -1 sets the grid's style to the topography's style.
   * @param majorAlpha
   *          The opacity of the major grid, 0.0f (transparent) through 1.0f (opaque).
   * @param minorAlpha
   *          -- reserved, must be 0.f
   * @param layout
   *          <code>true</code> to recompute the layout (this will make the changes visible)
   */
  public void setGridStyle(int style, float majorAlpha, float minorAlpha, boolean layout)
  {
    this.gridStyle      = style;
    this.gridMajorAlpha = majorAlpha;
    this.gridMinorAlpha = minorAlpha;
    if (layout) layout();
  }

  /**
   * Creates a cursor. If there already is a cursor, the old cursor will be destroyed and a new
   * cursor will be created.
   * 
   * @param size
   *          the size of the cursor
   * @param linewidth
   *          the width of the lines
   * @param textStyle
   *          the label text style
   * @param layout
   *          <code>true</code> to recompute the layout (this will make the changes visible)
   * @see #removeCursor(boolean)
   * @see #hasCursor()
   * @see #setCursorPos(float, float, String)
   * @see #slideCursor(float, float, long, String)
   */
  public void setCursor(int size, int linewidth, int textStyle, boolean layout)
  {
    EElbo e;
    size/=2;
    this.cursorSize = size;
    int size1 = (int)(0.4*size);
    int size2 = (int)(0.5*size);
    textStyle = (style&(~LCARS.ES_FONT))|(textStyle&LCARS.ES_FONT)|LCARS.ES_STATIC|LCARS.ES_LABEL_W;
    
    cursor.clear();
    e = new EElbo(null,this.x,this.y,size1,size,this.style|LCARS.ES_STATIC|LCARS.ES_SHAPE_NW,null);
    e.setArmWidths(linewidth,linewidth); e.setArcWidths(size2,size1); cursor.add(e);
    e = new EElbo(null,this.x,this.y+size,size1,size,this.style|LCARS.ES_STATIC|LCARS.ES_SHAPE_SW,null);
    e.setArmWidths(linewidth,linewidth); e.setArcWidths(size2,size1); cursor.add(e);
    e = new EElbo(null,this.x+2*size-size1,this.y,size1,size,this.style|LCARS.ES_STATIC|LCARS.ES_SHAPE_NE,null);
    e.setArmWidths(linewidth,linewidth); e.setArcWidths(size2,size1); cursor.add(e);
    e = new EElbo(null,this.x+2*size-size1,this.y+size,size1,size,this.style|LCARS.ES_STATIC|LCARS.ES_SHAPE_SE,null);
    e.setArmWidths(linewidth,linewidth); e.setArcWidths(size2,size1); cursor.add(e);
    cursor.add(new ERect(null,this.x+2*size,this.y+size-linewidth/2,size1,linewidth,this.style|LCARS.ES_STATIC,null));
    cursor.add(new ELabel(null,this.x+2*size+size1+3,this.y,2*size,2*size,textStyle,null));
    
    setCursorPos(0f,0f,"");
    if (layout) layout();
  }
  
  /**
   * Removes the cursor. The method does nothing if there is no cursor.
   * 
   * @param layout
   *          <code>true</code> to recompute the layout (this will make the
   *          changes visible)
   * @see #setCursor(int, int, int, boolean)
   * @see #hasCursor()
   * @see #setCursorPos(float, float, String)
   * @see #slideCursor(float, float, long, String)
   */
  public void removeCursor(boolean layout)
  {
    if (!hasCursor()) return;
    cursor.clear();
    if (layout) layout();
  }
  
  /**
   * Determines if this map has a cursor.
   * 
   * @see #setCursor(int, int, int, boolean)
   * @see #removeCursor(boolean)
   * @see #setCursorPos(float, float, String)
   * @see #slideCursor(float, float, long, String)
   */
  public boolean hasCursor()
  {
    return cursor!=null && cursor.size()>0;
  }

  /**
   * Sets the label of the cursor. The method does nothing if there is no cursor.
   * 
   * @param label
   *          The new cursor label. If <code>null</code>, the method does nothing.
   */
  public void setCursorLabel(String label)
  {
    if (!hasCursor()) return;
    if (label==null)  return;
    for (int i=0; i<cursor.size(); i++)
    {
      EElement e = cursor.get(i);
      if (e instanceof ELabel)
        ((ELabel)e).setLabel(label);
    }
  }
  
  /**
   * Instantly moves the cursor to a new target position. The method does nothing if there is no
   * cursor.
   * 
   * @param x
   *          physical x-coordinate of target position
   * @param y
   *          physical x-coordinate of target position
   * @param label
   *          the new cursor label or <code>null</code> to keep the current label
   * @see #slideCursor(float, float, long, String)
   * @see #setCursor(int, int, int, boolean)
   * @see #removeCursor(boolean)
   * @see #hasCursor()
   */
  public synchronized void setCursorPos(float x, float y, String label)
  {
    if(cursor==null || cursor.isEmpty()) return;
    Point     p  = pToL(x,y);
    Rectangle r  = cursor.get(0).getBounds();
    int       cx = r.x;
    int       cy = r.y;
    
    final ColorMeta transparent = new ColorMeta(0, true);    
    Point2D.Float ncp = new Point2D.Float(x,y);
    
    int n = cursor.size();
    
    if (cursorPos==null || !equalPos(ncp,cursorPos))
      for (int i=0; i<n; i++)
      {
        EElement e = cursor.get(i);
        r = e.getBounds();
        r.x=p.x+(r.x-cx)-cursorSize+this.x;
        r.y=p.y+(r.y-cy)-cursorSize+this.y;
        e.setBounds(r);
        if (e instanceof ELabel && label!=null)
          ((ELabel)e).setLabel(label);
      }
    else
      for (int i=0; i<n; i++)
      {
        EElement e = cursor.get(i);
        if (e instanceof ELabel && label!=null)
          ((ELabel)e).setLabel(label);
      }
    if (n > 4)
      cursor.get(4).setColor("".equals(label)?transparent:null);
    this.cursorPos = ncp;
  }

  /**
   * Slides the cursor to a new target position. The method does nothing if there is no cursor.
   * 
   * @param x
   *          physical x-coordinate of the target position
   * @param y
   *          physical y-coordinate of the target position
   * @param time
   *          time to target in milliseconds
   * @param label
   *          the cursor label to display when arriving at the target
   * @see #setCursorPos(float, float, String)
   * @see #setCursor(int, int, int, boolean)
   * @see #removeCursor(boolean)
   * @see #hasCursor()
   */
  public synchronized void slideCursor(float x, float y, long time, String label)
  {
    if (panel==null)
    {
      setCursorPos(x,y,label);
      return;
    }
    if (cursorPos!=null && equalPos(new Point2D.Float(x,y),cursorPos))
    {
      // Not moving
      setCursorPos(x,y,label);
      return;
    }
    int   steps = (int)Math.round(time/40);
    long period = time/steps;
    CursorSlideTask tt = new CursorSlideTask(steps,new Point2D.Float(x,y),label); 
    scheduleTimerTask(tt,TT_CURSORSLIDE,period,period);
  }
  
  /**
   * Adds a point shaped object to the map.
   * 
   * @param pos
   *          The position of in physical units.
   * @param radius
   *          The radius in panel pixels.
   * @param style
   *          The LCARS element style.
   * @param data
   *          Custom data to be associated with the point (can be <code>null</code>).
   * @param layout
   *          <code>true</code> to recompute the layout (this will make the changes visible).
   * @see #addPoint(java.awt.geom.Point2D.Float, int, int, boolean)
   * @see #movePoint(ERect, java.awt.geom.Point2D.Float)
   * @see #getPoints()
   */
  public ERect addPoint(Point2D.Float pos, int radius, int style, Object data, boolean layout)
  {
    Point lPos = pToL(pos);
    ERect e = new ERect(null,lPos.x-radius+this.x,lPos.y-radius+this.y,2*radius,2*radius,style|LCARS.ES_RECT_RND,null);
    e.setData(data);
    e.addEEventListener(this);
    points.add(e);
    if (layout) layout();
    return e;
  }

  /**
   * Adds a point shaped object to the map.
   * 
   * @param pos
   *          The position of in physical units.
   * @param radius
   *          The radius in panel pixels.
   * @param style
   *          The LCARS element style.
   * @param layout
   *          <code>true</code> to recompute the layout (this will make the changes visible).
   * @see #addPoint(java.awt.geom.Point2D.Float, int, int, Object, boolean)
   * @see #movePoint(ERect, java.awt.geom.Point2D.Float)
   * @see #getPoints()
   */
  public ERect addPoint(Point2D.Float pos, int radius, int style, boolean layout)
  {
    return addPoint(pos,radius,style,null,layout);
  }

  /**
   * Returns the points in this topography. The returned vector is a copy, modifying it has no effect
   * on the internal data.
   * 
   * @see #addPoint(java.awt.geom.Point2D.Float, int, int, boolean)
   * @see #addPoint(java.awt.geom.Point2D.Float, int, int, Object, boolean)
   * @see #movePoint(ERect, java.awt.geom.Point2D.Float)
   */
  public Vector<ERect> getPoints()
  {
    return new Vector<ERect>(points);
  }
  
  /**
   * Instantly moves a point to a new position on the map. If <code>ePoint</code> is <code>null</code> or
   * no point on the map, the method does nothing.
   * 
   * @param ePoint
   *          The point (return value of
   *          {@link #addPoint(java.awt.geom.Point2D.Float, int, int, Object, boolean)
   *          addPoint(...)}).
   * @param pos
   *          The new position of in physical units.
   * @see #addPoint(java.awt.geom.Point2D.Float, int, int, boolean)
   * @see #addPoint(java.awt.geom.Point2D.Float, int, int, Object, boolean)
   * @see #getPoints()
   */
  public void movePoint(ERect ePoint, Point2D.Float pos)
  {
    if (pos==null) return;
    if (points.indexOf(ePoint)<0) return;
    Rectangle lBounds = ePoint.getBounds();
    Point lPos = pToL(pos);
    lBounds.x = lPos.x-lBounds.width/2 +this.x;
    lBounds.y = lPos.y-lBounds.height/2+this.y;
    ePoint.setBounds(lBounds);
  }
  
  /**
   * Layout.
   */
  protected void layout()
  {
    if (panel==null || pTx==null) return;
    while (getElements().size()>0) remove(getElements().get(0));
    
    Point tl = pToL(pBounds.x,pBounds.y+pBounds.height);
    Point br = pToL(pBounds.x+pBounds.width,pBounds.y);

    // Create map
    try
    {
//      if (mapImageFile==null || mapImageBounds==null)
//        throw new IllegalStateException("No map image");
//      Image image = GImage.getImage(mapImageFile); 
//      int w = image.getWidth(null);
//      int h = image.getHeight(null);
//      if (w<0 || h<0) throw new IllegalStateException("Image incomplete");
//
//      float mapScale  = lBounds.width/pBounds.width;
//      float imgScaleX = w/mapImageBounds.width; 
//      float imgScaleY = h/mapImageBounds.height; 
//      float factorX   = mapScale/imgScaleX;
//      float factorY   = mapScale/imgScaleY;
//      Point ltl = pToL(mapImageBounds.x,mapImageBounds.y+mapImageBounds.height);
//
//      Image img = mapStyle==-1?mapImageFile:ImageTools.colorFilter(mapImageFile,mapStyle);
//      BufferedImage bi = LCARS.toBufferedImage(img);
//      int x = Math.round(-ltl.x/factorX);
//      int y = Math.round(-ltl.y/factorY);
//      w     = Math.round(lBounds.width/factorX);
//      h     = Math.round(lBounds.height/factorY);
//      bi = bi.getSubimage(x,y,w,h);
//      img = bi.getScaledInstance(lBounds.width,lBounds.height,Image.SCALE_DEFAULT);
//      add(new EImage(null,0,0,LCARS.ES_STATIC,img));
      add(new EImage(null,0,0,LCARS.ES_STATIC,mapImage));
    }
    catch (IllegalStateException e)
    {
    }
    
    final ColorMeta transparent = new ColorMeta(0, true);
    
    // Create points
    for (ERect point : points)
    {
      Rectangle r = point.getBounds();

      // Add an invisible circle for fat finger touching
      if (!point.isStatic() && (r.height<38 || r.width<38))
      {
        int x = (int)Math.round(r.getCenterX());
        int y = (int)Math.round(r.getCenterY());
        ERect e = new ERect(null,x-19,y-19,38,38,0,null);
        e.setColor(transparent);
        e.setData(point);
        e.addEEventListener(new EEventListenerAdapter()
        {
          @Override
          public void touchDown(EEvent ee)
          {
            ee.el = (ERect)ee.el.getData();
            fireEEvent(ee);
          }
        });
        add(e,false);
      }
      
      // Add the point itself
      add(point,false);
    }
    
    // Create major grid
    int gs = ( this.style & LCARS.ES_COLOR ) | LCARS.EF_TINY ;
    if (this.gridStyle!=-1)
      gs = this.gridStyle & (LCARS.ES_COLOR|LCARS.ES_FONT);
      
    ColorMeta color = new ColorMeta(LCARS.getColor(panel.getColorScheme(),gs), (int)(255*gridMajorAlpha));
    ELabel unitLabel = null;
    if (pGridMajor!=null)
    {
      double px0 = Math.ceil(pBounds.x/pGridMajor.x)*pGridMajor.x;
      double py0 = Math.ceil(pBounds.y/pGridMajor.y)*pGridMajor.y;
      for (double px=px0; px<pBounds.x+pBounds.width; px+=pGridMajor.x)
      {
        int lx = pToL((float)px,0f).x;
        ERect e = new ERect(null,lx-1,tl.y-1,2,br.y-tl.y,gs|LCARS.ES_STATIC,null);
        e.setColor(color);
        add(e);
        for (double py=py0; py<pBounds.y+pBounds.height; py+=pGridMajor.y)
        {
          int ly = pToL(0f,(float)py).y;
          if (px==px0)
          {
            e = new ERect(null,tl.x-1,ly-1,br.x-tl.x,2,gs|LCARS.ES_STATIC,null);
            e.setColor(color);
            add(e);
          }
          String labelX = (px<0?"-":"")+String.format("%02.0f",Math.abs(px)); 
          String labelY = (py<0?"-":"")+String.format("%02.0f",Math.abs(py));
          int ls = gs|LCARS.ES_LABEL_NW|LCARS.ES_STATIC;
          if ((lx+52)<lBounds.width && (ly-16)<lBounds.height)
          {
            ELabel l = new ELabel(null,lx+2,ly+2,50,14,ls,labelX+"/"+labelY);
            l.setColor(color);
            add(l);
            if (py==py0) unitLabel = l;
          }
        }
      }
      if (unitLabel!=null) 
        unitLabel.setLabel(" "+pUnit+"/"+pUnit);
    }

    // Create the cursor
    String label = "";
    for (EElement el : cursor)
    {
      add(el,false);
      if (el instanceof ELabel)
        label = el.getLabel();
    }
    if (cursorPos!=null)
    {
      float x = cursorPos.x;
      float y = cursorPos.y;
      cursorPos = null;
      setCursorPos(x,y,label);
    }
    
    if (panel!=null) panel.invalidate();
  }
  
  // -- Overrides --
  
  /*
   * (non-Javadoc)
   * @see de.tucottbus.kt.lcars.contributors.ElementContributor#addToPanel(de.tucottbus.kt.lcars.Panel)
   */
  @Override
  public void addToPanel(Panel panel)
  {
    super.addToPanel(panel);
    layout();
  }
 
  // -- Coordinate conversion --

  /**
   * Converts physical to logical coordinates.
   * 
   * @param point
   *          The physical coordinates.
   * @return The logical coordinates.
   */
  public Point pToL(Point2D.Float point)
  {
    if (pTx==null) return new Point(0,0);
    Point2D p = pTx.transform(point,null);
    int x = (int)Math.round(p.getX());
    int y = (int)Math.round(p.getY());
    return new Point(x,y);
  }

  /**
   * Converts physical to logical coordinates.
   * 
   * @param x
   *          The physical x-coordinate.
   * @param y
   *          The physical y-coordinate.
   * @return The logical coordinates.
   */
  public Point pToL(float x, float y)
  {
    return pToL(new Point2D.Float(x,y));
  }
  
  /**
   * Converts logical tp physical coordinates.
   * 
   * @param point
   *          The logical coordinates.
   * @return The physical coordinates.
   */
  public Point2D lToP(Point point)
  {
    if (pTx==null) return new Point2D.Float(0,0);
    try
    {
      return pTx.inverseTransform(point,null);
    }
    catch (NoninvertibleTransformException e)
    {
      return new Point2D.Float(0,0);
    }
  }
  
  /**
   * Converts logical to physical coordinates.
   * 
   * @param x
   *          The logical x-coordinate.
   * @param y
   *          The logical y-coordinate.
   * @return The physical coordinates.
   */
  public Point2D lToP(int x, int y)
  {
    return lToP(new Point(x,y));
  }

  /**
   * Determines if to physical positions are equal. Two positions are equal if and only of they are
   * represented by the same LCARS panel pixel on the topography.
   * 
   * @param pos1
   *          The first position (physical coordinates).
   * @param pos2
   *          The second position (physical coordinates).
   * @return <code>true</code> if the positions are equal, <code>false</code> otherwise.
   */
  public boolean equalPos(Point2D.Float pos1, Point2D.Float pos2)
  {
    Point l1 = pToL(pos1);
    Point l2 = pToL(pos2);
    return l1.x==l2.x && l1.y==l2.y;
  }
  
  // -- Animations --
  
  /**
   * Cursor sliding animation.
   */
  class CursorSlideTask extends TimerTask
  {
    private int           steps;
    private Point2D.Float target;
    private String        label;
    
    /**
     * Moves the cursor to the specified target coordinates.
     * 
     * @param steps
     *          number of steps to target
     * @param target
     *          physical coordinates to move the cursor to
     * @param targetLabel
     *          the cursor label to display when arrived at the target
     */
    public CursorSlideTask(int steps, Point2D.Float target, String targetLabel)
    {
      this.steps  = steps;
      this.target = target;
      this.label  = targetLabel;
    }

    @Override
    public void run()
    {
      if (!hasCursor()) return;
      if (steps>0)
      {
        float x  = cursorPos.x+(target.x-cursorPos.x)/steps;
        float y  = cursorPos.y+(target.y-cursorPos.y)/steps;
        if (cursorPos!=null && equalPos(new Point2D.Float(x,y),cursorPos))
        {
          setCursorPos(target.x,target.y,label);
          cancel();
        }
        else
        {
          setCursorPos(x,y,"");
          steps--;
        }
      }
      else
      {
        setCursorPos(target.x,target.y,label);
        cancel();
      }
    }
  }
  
}
