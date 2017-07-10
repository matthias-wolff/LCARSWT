package de.tucottbus.kt.lcars.net;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.URL;
import java.net.URLConnection;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Enumeration;

import de.tucottbus.kt.lcars.LCARS;
import de.tucottbus.kt.lcars.logging.Log;

/**
 * Static network and RMI utility methods.
 * 
 * @author Matthias Wolff, BTU Cottbus - Senftenberg
 */
public class NetUtils
{
  
  /**
   * Cache for the {@link NetUtils#getHostName()} method.
   */
  private static String hostName;

  /**
   * Cache for the {@link NetUtils#getRmiRegistry()} method.
   */
  private static Registry rmiRegistry; 

  /**
   * Performs an HTTP GET request.
   * <p><b>Author:</b> http://www.aviransplace.com/2008/01/08/make-http-post-or-get-request-from-java/</p>
   * 
   * @param  url
   *           The URL, e.g. "http://www.myserver.com?q=whats%20up".
   * @return The response or <code>null</code> in case of errors.
   */
  public static String HttpGet(String url)
  {
    String result = null;
    if (url.startsWith("http://"))
    {
      // Send a GET request to the servlet
      try
      {
        URLConnection conn = (new URL(url)).openConnection();
  
        // Get the response
        BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuffer sb = new StringBuffer();
        String line;
        while ((line = rd.readLine()) != null)
        {
          sb.append(line);
        }
        rd.close();
        result = sb.toString();
      }
      catch (Exception e)
      {
        Log.err("Cannot perform HTTP GET on \"" + url + "\"", e);
      }
    }
    return result;
    
  }

  /**
   * Returns the IP address of the computer running this iscreen.
   * 
   * @param verbose
   *          If <code>true</code> do some console logging.
   */
  public static InetAddress getIP(boolean verbose)
  {
    if (System.getSecurityManager()!=null)
    {
      System.out.println("myIP warning: Security manager active. Some IP addresses might not be detected.");
    }
    try
    {
      InetAddress myIP = null;
      InetAddress host = InetAddress.getLocalHost();
      if (verbose) System.out.println("myIP: Local host");
      InetAddress[] ips = InetAddress.getAllByName(host.getHostName());
      for (int i=0; i<ips.length; i++)
        if (verbose) System.out.println("  address = " + ips[i]);
      if (verbose) System.out.println("myIP: Network interfaces");
      Enumeration<NetworkInterface> netInterfaces = NetworkInterface.getNetworkInterfaces();
      while (netInterfaces.hasMoreElements())
      {
        NetworkInterface ni = netInterfaces.nextElement();
        if (verbose) System.out.println("  "+ni.getName());
        Enumeration<InetAddress> inetAddresses = ni.getInetAddresses();
        while (inetAddresses.hasMoreElements())
        {
          InetAddress ip = inetAddresses.nextElement();
          if
          (
            myIP==null &&
            !ip.isLoopbackAddress() &&
            ip.getHostAddress().indexOf(":")==-1
          )
          {
            myIP = ip;
          }
          ips = InetAddress.getAllByName(ip.getHostName());
          for (InetAddress ip2 : ips)
          {
            if (verbose) System.out.println("  - "+ip2);
          }
        }
      }
      if (myIP!=null)
        return myIP;
      else
        throw new Exception();
    }
    catch (Throwable e)
    {
      System.out.println("myIP error: cannot detect IP Address.");
      return null;
    }
  }

  /**
   * Returns the serverName of the local host. 
   */
  public static String getHostName()
  { 
    if (hostName==null)
    {
      if (LCARS.getArg("--rminame=")!=null)
      {
        hostName = LCARS.getArg("--rminame=");
      }
      else
        try
        {
          hostName = InetAddress.getLocalHost().getHostName();
        }
        catch (java.net.UnknownHostException e)
        {
          hostName = "127.0.0.1";
        }
    }
    return hostName;
  }

  /**
   * Returns the port of the RMI registry listing for LCARS remove panel requests.
   */
  public static int getRmiPort()
  {
    return 1099;
  }

  /**
   * Returns the LCARS RMI name prefix.
   */
  public static String getRmiName()
  {
    return "LCARS";
  }

  /**
   * Returns the local RMI registry at the port returned by {@link getRmiPort}. If no
   * such registry exists, the method creates one.
   * 
   * @return The local RMI registry.
   * @throws RemoteException
   *           If the registry could not be exported or no reference could be created.
   */
  public static Registry getRmiRegistry() throws RemoteException
  {
    // TODO: Make sure that local registry is not removed by JVM shutting down! How?
    if (rmiRegistry==null)
    {
      System.setSecurityManager(new RmiSecurityManager());
      if (LCARS.getArg("--rminame=")!=null)
        System.setProperty("java.rmi.server.hostname",LCARS.getArg("--rminame=")); 
      try
      {
        
        rmiRegistry = LocateRegistry.createRegistry(getRmiPort());
        /* TODO: Here is how to create an RMI registry bound to a specific IP address --> 
        LCARS.rmiRegistry = LocateRegistry.createRegistry(getRmiPort(), null,
            new RMIServerSocketFactory()
            {
              @Override
              public ServerSocket createServerSocket(int port) throws IOException
              {
                ServerSocket serverSocket = null;
                try
                {
                  serverSocket = new ServerSocket(port,50,Inet4Address.getByAddress(new byte[]{(byte)141,(byte)43,(byte)71,(byte)26}));
                } catch (Exception e)
                {
                  e.printStackTrace();
                }
                LCARS.log("DBG","RMI Server Socket="+serverSocket.toString());
                return (serverSocket);
              }
  
              @Override
              public boolean equals(Object that)
              {
                return (that != null && that.getClass() == this.getClass());
              }
            });
         <-- */
      }
      catch (Exception e)
      {
        rmiRegistry = LocateRegistry.getRegistry(getRmiPort());
      }
      Log.info("RMI registry: "+rmiRegistry);
    }
    return rmiRegistry;
  }

}

// EOF