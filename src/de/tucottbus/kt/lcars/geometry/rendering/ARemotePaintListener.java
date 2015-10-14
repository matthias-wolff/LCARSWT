package de.tucottbus.kt.lcars.geometry.rendering;

import java.io.Serializable;

import de.tucottbus.kt.lcars.geometry.IWorkspace;

public abstract class ARemotePaintListener <TData extends Serializable> implements Serializable
{
  private static final long serialVersionUID = -4116020572081158964L;

  public abstract IWorkspace<TData> initialize();
  
  public abstract void shutdown(IWorkspace<TData> workspace);
  
}
