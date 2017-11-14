package de.tucottbus.kt.lcars.geometry;

import java.io.Serializable;

import org.eclipse.swt.graphics.Image;

@Deprecated
public interface IWorkspace <TData extends Serializable>
{
  void apply(Image buffer, TData data);
}
