import java.net.InetAddress;

import de.tucottbus.kt.lcars.LCARS;

public class myIP
{
  public static void main(String[] args)
  {
    boolean verbose = args.length>0 && "-v".equals(args[0]); 
    
    InetAddress myIP = LCARS.getIP(verbose);
    System.out.println("myIP: "+myIP.getHostAddress());
    
  }
}
