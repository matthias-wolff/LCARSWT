package de.tucottbus.kt.lcars.ge;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.AbstractList;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import de.tucottbus.kt.lcars.LCARS;

public class Geocoder
{
  private static       String kml     = ""; 
  private static final String Q_EARTH = "http://maps.google.com/maps/geo?output=kml&oe=utf-8&q=[ADDRESS]&hl=en";
  private static final String Q_MARS  = "http://sky-search.appspot.com/mars?q=[ADDRESS]&output=kml&hl=en";  
  private static final String Q_MOON  = "http://sky-search.appspot.com/moon?q=[ADDRESS]&output=kml&hl=en";
  private static final String Q_SKY   = "http://maps.google.com/maps?output=kml&oe=utf-8&q=sky%3A[ADDRESS]";
  
  /**
   * Performs geocoding.
   * 
   * @param world
   * @param address
   * @return
   */
  // TODO: Fix character encoding!
  public static AbstractList<GEPlace> geocode(String world, String address)
  {
    String          url    = null;
    Vector<GEPlace> places = new Vector<GEPlace>();

    // Get query URL
    if      (GE.EARTH.equals(world)) url = Q_EARTH;
    else if (GE.MARS .equals(world)) url = Q_MARS;
    else if (GE.MOON .equals(world)) url = Q_MOON;
    else if (GE.SKY  .equals(world)) url = Q_SKY;
    if (url==null) throw new IllegalArgumentException("["+world+": unknown world]");
    
    // Query
    LCARS.log("GCD","Geocoding \""+address+"\" on "+world);
    url = url.replace("[ADDRESS]",urlEncode(address));

    try
    {
      // Get and parse KML
      LCARS.log("GCD","HTTP GET \""+url+"\"");
      URLConnection conn = (new URL(url)).openConnection();
      conn.setRequestProperty("User-Agent","GoogleEarth");
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      DocumentBuilder db = dbf.newDocumentBuilder();
      Document doc = db.parse(conn.getInputStream());
      kml = serializeXmlDocument(doc);
      //System.out.println(kml);
      doc.getDocumentElement().normalize();
      
      // Dummy picking of some information from the DOM
      // Loop over all <Placemark>s
      NodeList nodeLst = doc.getElementsByTagName("Placemark");
      for (int i=0; i<nodeLst.getLength(); i++)
      {
        String log = "";
        Node node = nodeLst.item(i);
        if (node.getNodeType()!=Node.ELEMENT_NODE) continue;

        // Get <name> or <address>
        NodeList nameL = ((Element)node).getElementsByTagName("name");
        if (nameL.getLength()==0)
          nameL = ((Element)node).getElementsByTagName("address");
        Node nameN = nameL.item(0);
        nameN = nameN.getChildNodes().item(0);
        String name = nameN.getNodeValue();
        if (name.startsWith("<a href="))
        {
          name = name.substring(name.indexOf('>')+1);
          name = name.substring(0,name.indexOf('<'));
        }
        if ("Your search returned no results".equals(name)) continue; // HACK!
        log += "- "+name;

        boolean added = false;

        // Get <LookAt>
        NodeList lkaL = ((Element)node).getElementsByTagName("LookAt");
        if (lkaL.getLength()>0)
        {
          Node lkaN = lkaL.item(0);
          float lon = Float.valueOf(((Element)lkaN).getElementsByTagName("longitude").item(0).getChildNodes().item(0).getNodeValue());
          float lat = Float.valueOf(((Element)lkaN).getElementsByTagName("latitude" ).item(0).getChildNodes().item(0).getNodeValue());
          float rng = Float.valueOf(((Element)lkaN).getElementsByTagName("range"    ).item(0).getChildNodes().item(0).getNodeValue());
          log += ", LookAt(LON="+lon+", LAT="+lat+", RNG="+rng+")";
          places.add(GEPlace.fromLookAt(name,world,lon,lat,rng));
          added = true;
        }
        
        // Get <LatLonBox>
        NodeList llbL = ((Element)node).getElementsByTagName("LatLonBox");
        if (!added && llbL.getLength()>0)
        {
          Node llbN = llbL.item(0);
          float north = Float.valueOf(llbN.getAttributes().getNamedItem("north").getNodeValue());
          float south = Float.valueOf(llbN.getAttributes().getNamedItem("south").getNodeValue());
          float east  = Float.valueOf(llbN.getAttributes().getNamedItem("east" ).getNodeValue());
          float west  = Float.valueOf(llbN.getAttributes().getNamedItem("west" ).getNodeValue());
          log += ", LatLonBox(N="+north+", S="+south+", E="+east+", W="+west+")";
          places.add(GEPlace.fromLatLonBox(name,world,north,south,east,west));
          added = true;
        }
        
        if (!added)
          places.add(new GEPlace(name,world,(GECamera)null,null));
        
        LCARS.log("GCD",log);
      }
    }
    catch (MalformedURLException e1)
    {
      e1.printStackTrace();
    }
    catch (IOException e1)
    {
      e1.printStackTrace();
    }
    catch (ParserConfigurationException e)
    {
      e.printStackTrace();
    }
    catch (SAXException e)
    {
      e.printStackTrace();
    }

    return places;
  }
 
  /**
   * Returns a proper URL-encoding for a string.
   * 
   * @param string the string
   * @return the URL-encoded version of the string
   */
  private static String urlEncode(String string)
  {
    try
    {
      return URLEncoder.encode(string,"UTF-8");
    }
    catch (UnsupportedEncodingException e)
    {
      e.printStackTrace();
      return string;
    }
  }
  
  /**
   * Serializes an XML {@link Document} into a string.
   * 
   * @param doc the document
   * @return the XML text
   */
  private static String serializeXmlDocument(Document doc)
  {
    StringWriter xmlWriter  = new StringWriter();
    Transformer  serializer;
    try
    {
      serializer = TransformerFactory.newInstance().newTransformer();
      serializer.setOutputProperty(OutputKeys.ENCODING,"ISO-8859-1");
      serializer.setOutputProperty(OutputKeys.INDENT,"yes");
      serializer.transform(new DOMSource(doc),new StreamResult(xmlWriter));       
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
    return xmlWriter.toString();
  }
  
  /**
   * Returns the most recently processed KML text.
   * 
   * @return the text
   */
  public static String getKml()
  {
    return kml;
  }
  
  public static void main(String[] args)
  {
    String url = "http://maps.google.com/maps?q=sky%3Am83&sll=50.9995,96.5138&sspn=141,360&output=kml&ie=utf-8&v=2.2&cv=6.1.0.5001&hl=de";
    System.out.println("HTTP GET: "+url+"\n");

    HttpURLConnection conn;
    try
    {
      conn = (HttpURLConnection)(new URL(url)).openConnection();
      conn.setRequestMethod("GET");
//      conn.setRequestProperty("Accept","application/vnd.google-earth.kml+xml, application/vnd.google-earth.kmz, image/*, */*");
      conn.setRequestProperty("User-Agent","GoogleEarth");
      System.out.println(conn.getRequestProperties().toString());
      conn.connect();
      InputStream in = conn.getInputStream();
      BufferedReader reader = new BufferedReader(new InputStreamReader(in));
      String text = reader.readLine();
      System.out.println(text);
      conn.disconnect();
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }
}
