package de.tucottbus.kt.lcars.logging;

public interface ILogObserver
{
  /**
   * 
   * @param pfx
   * @param msg
   */
  public void log(String pfx, String msg);
  
  /**
   * 
   * @param src
   * @param message
   */
  public void warn(String pfx, String msg);
  
  /**
   * 
   * @param src
   * @param message
   */
  public void err(String pfx, String msg);
  
  /**
   * 
   * @param src
   * @param message
   */
  public void err(String pfx, String msg, Throwable e);
  
  /**
   * 
   * @param src
   * @param message
   */
  public void debug(String pfx, String msg);  

}
