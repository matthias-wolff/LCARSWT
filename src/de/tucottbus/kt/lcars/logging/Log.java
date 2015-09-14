package de.tucottbus.kt.lcars.logging;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class provides logging methods and supports debug messages. In addition observers can be registered that gets the logs too.  
 * @author Christian Borck
 *
 */
public class Log {
  public static final String CLASSKEY = "LOGGER";
  
  private static final Logger LOG;
  private static final String FORMAT = "[%s%c %s]";
  private static final Set<ILogObserver> OBSERVERS = new HashSet<ILogObserver>();
  
  private static final String INFO_PFX  = "INFO:";
  private static final String WARN_PFX  = "WARN:";
  private static final String ERROR_PFX = "ERRO:";
  private static final String DEBUG_PFX = "DBUG:";
  
  private static final BlockingQueue<Runnable> logBuffer = new ArrayBlockingQueue<Runnable>(5);
  private static final Thread logWorker = new Thread(() -> {
    while(true)
      try {
        while(true)
          logBuffer.take().run();        
      }
      catch (Throwable e) {
        warn("Logger thread interrupted and was restarted.");
      }
  });
  
  public static Boolean DebugMode = false;
  
  static {
    StackTraceElement[] els = Thread.currentThread().getStackTrace();
    LOG = LoggerFactory.getLogger(els.length > 2? els[2].getClass() : Log.class);
    logWorker.setName("Log:Worker");
    logWorker.setDaemon(true);
    logWorker.start();   
  }
  
  // hide constructor
  private Log(){}
  
  /**
   * Log a message at the INFO level. Equivalent to {@link #info(char, String, 
   * String) info}<code>(':',msg)</code>.
   * 
   * @param pfx
   *          Prefix string, typically a string of three characters identifying
   *          the object writing to the log.
   * @param msg
   *          The log message.
   */
  public static void info(String msg) {
    info(':',getPrefix(Thread.currentThread().getStackTrace()[2]), msg);
  }
  
  /**
   * @deprecated Use overloading method instead.
   * @param pfx
   * @param msg
   */
  @Deprecated
  public static void info(String pfx, String msg) {
    info(':',pfx,msg);
  }
  
  /**
   * Log a message at the INFO level.
   * 
   * @param type
   *          <code>':'</code> for info messages, <code>'>'</code> for echos of
   *          output to external programs or hardware, or <code>'>'</code> for 
   *          echos of input from external programs or hardware.
   * @param msg
   *          The log message.
   */
  public static void info(char type, String msg) {
    info(type, getPrefix(Thread.currentThread().getStackTrace()[2]),msg);
  }

  /**
   * @deprecated Use overloading method instead.
   * @param type
   * @param pfx
   * @param msg
   */
  @Deprecated
  //TODO: set to private, remove deprecated state
  public static void info(char type, String pfx, String msg) {
    String pfx2 = INFO_PFX+pfx;
    try {
      logBuffer.put(() -> {
        LOG.info(String.format(FORMAT,pfx2,type,msg));
        for (ILogObserver obs : OBSERVERS)
          obs.log(pfx2, msg);      
      });
    }
    catch(Exception e)
    {
      logIfInterrupted(pfx, msg);
    }
  }

  /**
   * Log a message at the WARN level.
   * 
   * @param msg
   *          The log message.
   */
  public static void warn(String msg) {
    warn(getPrefix(Thread.currentThread().getStackTrace()[2]), msg);
  }
  
  /**
   * @deprecated Use overloading method instead.
   * @param pfx
   * @param msg
   */
  @Deprecated
  public static void warn(String pfx, String msg) {
    String pfx2 = WARN_PFX + pfx;
    try {
      logBuffer.put(() -> {
        LOG.warn(String.format(FORMAT,pfx2,'!',msg));
        for (ILogObserver obs : OBSERVERS)
          obs.warn(pfx2, msg);      
      });
    }
    catch(Exception e)
    {
      logIfInterrupted(pfx2, msg);
    }
  }
    
  /**
   * Log a message at the ERROR level.
   * 
   * @param msg
   *          The log message.
   */
  public static void err(String msg) {
    err(getPrefix(Thread.currentThread().getStackTrace()[2]), msg);
  }
  
  /**
   * @deprecated Use overloading method instead.
   * @param pfx
   * @param msg
   */
  @Deprecated
  public static void err(String pfx, String msg) {
    String pfx2 = ERROR_PFX + pfx;
    try {
      logBuffer.put(() -> {
        LOG.error(String.format(FORMAT,pfx2,'!',msg));
        for (ILogObserver obs : OBSERVERS)
          obs.err(pfx2, msg);      
      });
    }
    catch(Exception e)
    {
      logIfInterrupted(pfx2, msg);
    }
  }
  
  /**
   * Log a message at the ERROR level.
   * 
   * @param msg
   *          The log message.
   * @param e
   *          The {@link Throwable} causing the error.
   */
  public static void err(String msg, Throwable e) {
    err(getPrefix(Thread.currentThread().getStackTrace()[2]), msg, e);
  }
  
  /**
   * @deprecated Use overloading method instead.
   * @param pfx
   * @param msg
   * @param e
   */
  @Deprecated
  public static void err(String pfx, String msg, Throwable e) {
    String pfx2 = ERROR_PFX + pfx;
    try {
      logBuffer.put(() -> {
        LOG.error(String.format(FORMAT,pfx2,'!',msg), e);
        for (ILogObserver obs : OBSERVERS)
          obs.err(pfx2, msg);      
      });
    }
    catch(Exception e2)
    {
      logIfInterrupted(pfx2, msg, e);
    }
  }
  
  /**
   * Log a message at the DEBUG level.
   * 
   * @param msg
   *          The log message.
   * @param e
   *          The {@link Throwable} causing the error.
   */
  public static void debug(String msg) {
    if(!DebugMode)
      return;
    debug(getPrefix(Thread.currentThread().getStackTrace()[2]), msg);
  }    
  
  /**
   * @deprecated Use overloading method instead.
   * @param pfx
   * @param msg
   */
  @Deprecated
  public static void debug(String pfx, String msg) {
    if(!DebugMode)
      return;
    String pfx2 = DEBUG_PFX + pfx;
    try {
      logBuffer.put(() -> {
        LOG.debug(String.format(FORMAT,pfx2,':',msg));
        for (ILogObserver obs : OBSERVERS)
          obs.debug(pfx2, msg);      
      });
    }
    catch(Exception e2)
    {
      logIfInterrupted(pfx2, msg);
    }
  }
  
  /**
   * 
   * @param obs
   */
  public static void addObserver(ILogObserver obs) {
    if (obs != null)
      OBSERVERS.add(obs);
  }
  
  /**
   * 
   * @param obs
   */
  public static void removeObserver(ILogObserver obs) {
    OBSERVERS.remove(obs);
  }
  
  /**
   * 
   */
  private static void logIfInterrupted(String pfx, String msg) {
    System.out.println(String.format(FORMAT,CLASSKEY,':',"log interrupted"));
    System.out.println(String.format(FORMAT,pfx,':',msg));
  }
  
  /**
   * 
   */
  private static void logIfInterrupted(String pfx, String msg, Throwable e) {
    System.out.println(String.format(FORMAT,CLASSKEY,':',"log interrupted"));
    System.out.println(String.format(FORMAT,pfx,':',msg));
    e.printStackTrace();
  }
  
  private static String getPrefix(StackTraceElement stackTrace) {
    String filename = stackTrace.getFileName();
    return filename.substring(0, filename.length()-5) + ":" + stackTrace.getLineNumber();
  }  
}
