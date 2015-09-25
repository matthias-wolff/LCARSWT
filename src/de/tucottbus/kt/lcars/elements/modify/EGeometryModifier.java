package de.tucottbus.kt.lcars.elements.modify;

import java.util.ArrayList;

import de.tucottbus.kt.lcars.elements.EElement;
import de.tucottbus.kt.lcars.geometry.AGeometry;

/**
 * Geometry modifiers are registered with {@link EElement}s in order to apply
 * customizations to standard element geometries.
 * 
 * @see EElement#addGeometryModifier(EGeometryModifier)
 * @see EElement#removeGeometryModifier(EGeometryModifier)
 * @author Matthias Wolff
 */
public interface EGeometryModifier
{
  /**
   * Called to modify an {@link EElement} geometry.
   * 
   * @param geos
   *          The geometries to modify.
   */
  public void modify(ArrayList<AGeometry> geos);
}

// EOF

