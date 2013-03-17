package de.tucottbus.kt.lcars.ge;

/**
 * One place comprising a name, a world, a camera position and a speech
 * control grammar. 
 */
class GEPlace
{
  String name;
  String world;
  GECamera camera;
  String grammar;

  /**
   * Creates a new place
   * 
   * @param name
   *          the name
   * @param world
   *          the world, one of the {@link GECamera}<code>.XXXX</code> constants
   * @param camera
   *          the camera to view this place in Google Eearth
   * @param grammar
   *          contribution to the speech dialog (UASR finite state grammar
   *          format)  
   */
  public GEPlace(String name, String world, GECamera camera, String grammar)
  {
    this.name    = name;
    this.world   = world;
    this.camera  = camera;
    this.grammar = grammar;
    if (this.camera.name==null) this.camera.name = name;
  }

  /**
   * Creates a new place
   * 
   * @param name
   *          the name
   * @param world
   *          the world, one of the {@link GECamera}<code>.XXXX</code> constants
   * @param camera
   *          the camera to view this place in Google Eearth in the format
   *          "&lt;latitude&gt;, &lt;longitude&gt;, &lt;altitude&gt;, 
   *          &lt;heading&gt;, &lt;tilt&gt;, &lt;roll&gt;" where the fields
   *          enclosed in arrow brackets denote floating point values
   * @param grammar
   *          contribution to the speech dialog (UASR finite state grammar
   *          format)  
   */
  public GEPlace(String name, String world, String camera, String grammar)
  {
    this.name    = name;
    this.world   = world;
    this.camera  = camera!=null && camera.length()>0 ? new GECamera(world,camera) : null;
    if (this.camera!=null) this.camera.name = name;
    this.grammar = grammar;
  }

  public static GEPlace fromLookAt(String name, String world, float longitude, float latitude, float range)
  {
    GECamera camera = new GECamera(world);
    camera.longitude = longitude;
    camera.latitude  = latitude;
    camera.altitude  = range;
    camera.heading   = 0;
    camera.tilt      = 0;
    camera.roll      = 0;
    camera.name      = name;
    return new GEPlace(name,world,camera,null);
  }

  public static GEPlace fromLatLonBox(String name, String world, float north, float south, float east, float west)
  {
    float fullalt = 16000000;
    if (world==GE.MOON) fullalt = 5000000;
    if (world==GE.MARS) fullalt = 10000000;

    if (east<west)
    {
      if (Math.abs(east)>Math.abs(west)) east += 360;
      else west -= 360;
    }
    if (north<south)
    {
      if (Math.abs(north)>Math.abs(south)) north += 180;
      else south -= 180;
    }
    float latAlpha = north-south;
    float lonAlpha = east-west;
    float alpha    = Math.max(latAlpha,lonAlpha);
    float lat      = (north+south)/2;
    float lon      = (west+east)/2;
    float alt      = (float)(Math.sin(Math.min(alpha,180)/360*Math.PI)*fullalt);

    GECamera camera  = new GECamera(world);
    camera.latitude  = lat;
    camera.longitude = lon;
    camera.altitude  = alt;
    camera.heading   = 0;
    camera.tilt      = 0;
    camera.roll      = 0;
    camera.name      = name;
    return new GEPlace(name,world,camera,null);
  }
  
}