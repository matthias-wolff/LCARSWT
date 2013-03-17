package de.tucottbus.kt.lcars.util;
/**
 * 
 * JFreeReport : a free Java reporting library
 * 
 *
 * Project Info:  http://reporting.pentaho.org/
 *
 * (C) Copyright 2001-2007, by Object Refinery Ltd, Pentaho Corporation and Contributors.
 *
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation;
 * either version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * library; if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307, USA.
 *
 * [Java is a trademark or registered trademark of Sun Microsystems, Inc.
 * in the United States and other countries.]
 *
 * ------------
 * MemoryByteArrayOutputStream.java
 * ------------
 * (C) Copyright 2001-2007, by Object Refinery Ltd, Pentaho Corporation and Contributors.
 */

import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;

/**
 * This class supplies static methods for determining the size of a objects.
 * 
 * @author Matthias Wolff
 */
public class ObjectSize extends OutputStream
{
  private int size;

  /**
   * Creates
   */
  private ObjectSize()
  {
    size = 0;
  }

  @Override
  public void write(final byte[] cbuf, final int off, final int len)
  throws IOException
  {
    if (len<0) { throw new IllegalArgumentException(); }
    if (off<0) { throw new IndexOutOfBoundsException(); }
    if (cbuf==null) { throw new NullPointerException(); }
    if ((len+off)>cbuf.length) { throw new IndexOutOfBoundsException(); }

    size += len;
  }
  
  @Override
  public void write(final byte[] b) throws IOException
  {
    size += 1;
  }

  @Override
  public void write(final int b) throws IOException
  {
    size += 1;
  }

  /**
   * Returns the serialize size of an object.
   */
  public static int getSerializedSize(Serializable obj)
  {
    ObjectSize objectSize = new ObjectSize();
    try
    {
      ObjectOutput output = new ObjectOutputStream(objectSize);
      try
      {
        output.writeObject(obj);
      }
      finally
      {
        output.close();
      }
    }
    catch (IOException ex)
    {
    }
    return objectSize.size;
  }
}

// EOF