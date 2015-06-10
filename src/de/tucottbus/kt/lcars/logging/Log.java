package de.tucottbus.kt.lcars.logging;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class provides logging methods and supports debug messages. In addition observers can be registered that gets the logs too.
 * Hint: to colorize the logs in the eclipse console  
 * @author Christian Borck
 *
 */
public class Log {
  public static final String CLASSKEY = "LOGGER";
  
  private static final Logger LOG;
  private static final String FORMAT = "[%s%c %s]";
  private static final Set<ILogObserver> OBSERVERS = new HashSet<ILogObserver>();
  
  private static final BlockingQueue<Runnable> logBuffer = new ArrayBlockingQueue<Runnable>(5);
  private static final Thread logWorker = new Thread(() -> {
    while(true)
      try {
        while(true)
          logBuffer.take().run();        
      }
      catch (Throwable e) {
        err(CLASSKEY, "Logging interrupted");
      }
  });
  
  public static Boolean DebugMode = false;
  
  static {
    StackTraceElement[] els = Thread.currentThread().getStackTrace();
    LOG = LoggerFactory.getLogger(els.length > 2? els[2].getClass() : Log.class);
    logWorker.setName("Log:Worker");
    logWorker.start();   
  }
  
  // hide constructor
  private Log(){}
  
  /**
   * Log a message at the INFO level. Equivalent to {@link #info(char, String, 
   * String) info}<code>(':',pfx,msg)</code>.
   * 
   * @param pfx
   *          Prefix string, typically a string of three characters identifying
   *          the object writing to the log.
   * @param msg
   *          The log message.
   */
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
   * @param pfx
   *          Prefix string, typically a string of three characters identifying
   *          the object writing to the log. Used for message filtering.
   * @param msg
   *          The log message.
   */
  public static void info(char type, String pfx, String msg) {
    try {
      logBuffer.put(() -> {
        LOG.info(String.format(FORMAT,pfx,type,msg));
        for (ILogObserver obs : OBSERVERS)
          obs.log(pfx, msg);      
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
   * @param pfx
   *          Prefix string, typically a string of three characters identifying
   *          the object writing to the log. Used for message filtering.
   * @param msg
   *          The log message.
   */
  public static void warn(String pfx, String msg) {
    try {
      logBuffer.put(() -> {
        LOG.warn(String.format(FORMAT,pfx,'!',msg));
        for (ILogObserver obs : OBSERVERS)
          obs.warn(pfx, msg);      
      });
    }
    catch(Exception e)
    {
      logIfInterrupted(pfx, msg);
    }
  }
  
  /**
   * Log a message at the ERROR level.
   * 
   * @param pfx
   *          Prefix string, typically a string of three characters identifying
   *          the object writing to the log. Used for message filtering.
   * @param msg
   *          The log message.
   */
  public static void err(String pfx, String msg) {
    try {
      logBuffer.put(() -> {
        LOG.error(String.format(FORMAT,pfx,'!',msg));
        for (ILogObserver obs : OBSERVERS)
          obs.err(pfx, msg);      
      });
    }
    catch(Exception e)
    {
      logIfInterrupted(pfx, msg);
    }
  }
  
  /**
   * Log a message at the ERROR level.
   * 
   * @param pfx
   *          Prefix string, typically a string of three characters identifying
   *          the object writing to the log. Used for message filtering.
   * @param msg
   *          The log message.
   * @param e
   *          The {@link Throwable} causing the error.
   */
  public static void err(String pfx, String msg, Throwable e) {
    try {
      logBuffer.put(() -> {
        LOG.error(String.format(FORMAT,pfx,'!',msg), e);
        for (ILogObserver obs : OBSERVERS)
          obs.err(pfx, msg);      
      });
    }
    catch(Exception e2)
    {
      logIfInterrupted(pfx, msg, e);
    }
  }
  
  /**
   * Log a message at the DEBUG level.
   * 
   * @param pfx
   *          Prefix string, typically a string of three characters identifying
   *          the object writing to the log. Used for message filtering.
   * @param msg
   *          The log message.
   * @param e
   *          The {@link Throwable} causing the error.
   */
  public static void debug(String pfx, String msg) {
    if(!DebugMode)
      return;
    
    try {
      logBuffer.put(() -> {
        LOG.debug(String.format(FORMAT,pfx,':',msg));
        for (ILogObserver obs : OBSERVERS)
          obs.debug(pfx, msg);      
      });
    }
    catch(Exception e2)
    {
      logIfInterrupted(pfx, msg);
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
}
