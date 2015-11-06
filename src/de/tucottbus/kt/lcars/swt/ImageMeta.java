package de.tucottbus.kt.lcars.swt;

import java.io.Serializable;

import org.eclipse.swt.graphics.Image;

import de.tucottbus.kt.Root;
import de.tucottbus.kt.lcars.util.Objectt;

public abstract class ImageMeta implements Serializable
{
  private static final long serialVersionUID = -7940109195240204823L;
  
  public final String path;
  
  private ImageMeta(String path) {
    this.path = path;
  }
    
  public abstract Image getImage(); 
  
  public boolean equals(ImageMeta other) {
    return getClass() == other.getClass()
        && Objectt.equals(path, other.path);
  }
  
  @Override
  public boolean equals(Object other) {
    return other      != null
        && getClass() == other.getClass()
        && other instanceof ImageMeta
        && Objectt.equals(path, ((ImageMeta)other).path);
  }
  
  //--Nested classes
  
  public static class Resource extends ImageMeta {
    private static final long serialVersionUID = -7940109195240204822L;
    
    /**
     * Instantiates information structure to load an image from the resource 
     * @param resourceName path to resources (root: 'de/tucottbus/kt')
     */
    public Resource(String resourceName) {
      super(resourceName);
    }
    
    @Override
    public Image getImage() {
      return (path != null) ? SWTResourceManager.getImage(Root.class, path) : null;
    }  
    
    @Override
    public String toString() {    
      return ImageMeta.class.getSimpleName() + "." + Resource.class.getSimpleName() + " path=" + (path!=null ? "\"" + path + "\"" : "null");
    }        
  }
  
  public static class File extends ImageMeta {
    private static final long serialVersionUID = -7940109195240204821L;
    
    public File(String fileName) {
      super(fileName);
    }
    
    @Override
    public Image getImage() {
      return (path != null) ? SWTResourceManager.getImage(path) : null;
    }  
    
    @Override
    public String toString() {    
      return ImageMeta.class.getSimpleName() + "." + File.class.getSimpleName() + " path=" + (path!=null ? "\"" + path + "\"" : "null");
    }        
  }
  
  public static class None extends Resource {
    private static final long serialVersionUID = -7940109195240204821L;
    
    public None() {
      super(null);
    }    

    @Override
    public Image getImage() {
      return null;
    }  
    
}
}

