package de.tucottbus.kt.lcars.swt;

import java.io.Serializable;

import org.eclipse.swt.graphics.Image;

import de.tucottbus.kt.Root;
import de.tucottbus.kt.lcars.util.Objectt;

/**
 * This class contains meta information and provides methods to load an image different sources
 * using the SWT-API. The loaded image will be handled as a system resource. The
 * {@link de.tucottbus.kt.lcars.swt.SWTResourceManager} is used to share and dispose this system
 * resource. The type of image source can by specified using the following subclasses:
 * <p><ul>
   * <li>{@link de.tucottbus.kt.lcars.swt.ImageMeta.Resource}
   * <li>{@link de.tucottbus.kt.lcars.swt.ImageMeta.File}
   * <li>{@link de.tucottbus.kt.lcars.swt.ImageMeta.None}
   * </ul><p>
 * @author Christian Borck
 */
public abstract class ImageMeta implements Serializable
{
  private static final long serialVersionUID = -7940109195240204823L;
  
  public final String path;
  
  private ImageMeta(String path) {
    this.path = path;
  }
  
  /**
   * Loads the {@link org.eclipse.swt.graphics.Image} that will be shared and disposed using
   * {@link de.tucottbus.kt.lcars.swt.SWTResourceManager}.
   * @return
   */
  public final Image getImage()
  {
    return doGetImage();
  } 
  
  abstract Image doGetImage();
  
  private boolean doEquals(ImageMeta other)
  {
    final Class<? extends ImageMeta> c = getClass();
    return c == other.getClass()
        && Objectt.equals(path, other.path);
  }
  
  public final boolean equals(ImageMeta other)
  {
    return other != null && doEquals(other);
  }
  
  @Override
  public final boolean equals(Object other)
  {
    return other != null
        && other instanceof ImageMeta
        && doEquals((ImageMeta)other);
  }

  //--Nested classes
  
  /**
   * This class handles meta information to load a image from the application resource. See also: {@link de.tucottbus.kt.lcars.swt.ImageMeta}
   */
  public static class Resource extends ImageMeta
  {
    private static final long serialVersionUID = -7940109195240204822L;
    
    /**
     * Instantiates information structure to load an image from the resource 
     * @param resourceName path to resources (root: 'de/tucottbus/kt'), i.e.: "{@code lcars/resources/LCARS-css.js}"
     */
    public Resource(String resourceName)
    {
      super(resourceName);
    }
    
    @Override
    final Image doGetImage()
    {
      return (path != null) ? SWTResourceManager.getImage(Root.class, path) : null;
    }  
    
    @Override
    public String toString()
    {    
      return Resource.class.getSimpleName() + "." + Resource.class.getSimpleName() + " path=" + (path!=null ? "\"" + path + "\"" : "null");
    }        
  }
  
  /**
   * This class handles meta information to load a image from a file. See also:
   * {@link de.tucottbus.kt.lcars.swt.ImageMeta}
   */
  public static class File extends ImageMeta
  {
    private static final long serialVersionUID = -7940109195240204821L;
    
    public File(String fileName)
    {
      super(fileName);
    }
    
    @Override
    final Image doGetImage()
    {
      return (path != null) ? SWTResourceManager.getImage(path) : null;
    }  
    
    @Override
    public final String toString()
    {    
      return File.class.getSimpleName() + "." + File.class.getSimpleName() + " path=" + (path!=null ? "\"" + path + "\"" : "null");
    }        
  }
  
  /**
   * This class a none image thus {@link de.tucottbus.kt.lcars.swt.ImageMeta#getImage()} returns
   * {@code null}. See also: {@link de.tucottbus.kt.lcars.swt.ImageMeta}
   */
  public static class None extends ImageMeta
  {
    private static final long serialVersionUID = -7940109195240204821L;
    
    public None()
    {
      super(null);
    }    

    @Override
    final Image doGetImage()
    {
      return null;
    }  
    
    @Override
    public final String toString()
    { 
      return None.class.getSimpleName();
    }        
  }
}

