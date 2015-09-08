package de.tucottbus.kt.lcars.util;

/**
 * This class represents a global contract which provides extensions to java.lang.Object
 * @author Christian Borck
 *
 */
public class Object
{
  public static <T> boolean equals(T o1, T o2) {
    return o1 == o2 || (o1 != null && o1.equals(o2));    
  }
}
