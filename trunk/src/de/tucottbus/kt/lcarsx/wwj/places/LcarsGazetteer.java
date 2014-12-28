package de.tucottbus.kt.lcarsx.wwj.places;

import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
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

/**
 * <p><i><b style="color:red">Experimental.</b></i></p>
 * 
 * A geocoder for Earth.
 * 
 * @author Matthias Wolff, BTU Cottbus-Senftenberg
 */
public class LcarsGazetteer
{
  private static final String Q_EARTH_XML = "https://maps.googleapis.com/maps/api/geocode/xml?address=[ADDRESS]&sensor=true_or_false";

  private static final String Q_EARTH_KML = "http://maps.google.com/maps?output=kml&oe=utf-8&q=[ADDRESS]&hl=en";
  private static final String Q_MARS_KML  = "http://sky-search.appspot.com/mars?q=[ADDRESS]&output=kml&hl=en";  
  private static final String Q_MOON_KML  = "http://sky-search.appspot.com/moon?q=[ADDRESS]&output=kml&hl=en";
  private static final String Q_SKY_KML   = "http://maps.google.com/maps?output=kml&oe=utf-8&q=sky%3A[ADDRESS]";

  /**
   * <p><i><b style="color:red">Experimental.</b></i></p>
   * 
   * Looks up places for a given address string.
   * 
   * @param world
   *          The world: {@link Place#ONEARTH}, {@link Place#ONMOON}, {@link
   *          Place#ONMARS}, or {@link Place#ONSKY}.
   * @param address
   *          The address to look up.
   * @return A (possibly empty) list of matching places.
   */
  public static AbstractList<Place> findPlaces(String world, String address)
  {
    return findPlacesViaGeocodeXML(world, address);
  }

  // -- Google Geocoding API (XML) --
  
  /**
   * Using the <a href="https://developers.google.com/maps/documentation/geocoding">Google Geocoding API</a>.
   */
  protected static AbstractList<Place> findPlacesViaGeocodeXML(String world, String address)
  {
    String        url    = null;
    Vector<Place> places = new Vector<Place>();

    // Get query URL
    if  (Place.ONEARTH.equals(world)) url = Q_EARTH_XML;
    if (url==null) throw new IllegalArgumentException("Geocoding not supported on"+world);
    url = url.replace("[ADDRESS]",urlEncode(address));

    URLConnection conn;
    try
    {
      // Get and parse XML
      LCARS.log("GAZ","HTTP GET \""+url+"\"");
      conn = (new URL(url)).openConnection();
      conn.setRequestProperty("User-Agent","GoogleEarth");
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      DocumentBuilder db = dbf.newDocumentBuilder();
      Document doc = db.parse(conn.getInputStream());
      doc.getDocumentElement().normalize();
      //System.out.println(serializeXml(doc));

      // Dummy picking of some information from the DOM
      NodeList nl_res = doc.getElementsByTagName("result");
      for (int i_res = 0; i_res<nl_res.getLength(); i_res++)
        try
        {
          Node n_res = nl_res.item(i_res);

          // Get <formatted_address>
          Node n_adr = ((Element)n_res).getElementsByTagName("formatted_address").item(0);
          String name = n_adr.getFirstChild().getNodeValue();

          // Get <geometry><viewport>...</viewport></geometry>
          NodeList nl_geo = ((Element)n_res).getElementsByTagName("geometry");
          NodeList nl_vpr = ((Element)nl_geo.item(0)).getElementsByTagName("viewport");
          Node n_pos = ((Element)nl_vpr.item(0)).getElementsByTagName("southwest").item(0);
          Node n_lat = ((Element)n_pos).getElementsByTagName("lat").item(0);
          Node n_lon = ((Element)n_pos).getElementsByTagName("lng").item(0);
          double west = Double.valueOf(n_lon.getFirstChild().getNodeValue());
          double south = Double.valueOf(n_lat.getFirstChild().getNodeValue());
          n_pos = ((Element)nl_vpr.item(0)).getElementsByTagName("northeast").item(0);
          n_lat = ((Element)n_pos).getElementsByTagName("lat").item(0);
          n_lon = ((Element)n_pos).getElementsByTagName("lng").item(0);
          double east = Double.valueOf(n_lon.getFirstChild().getNodeValue());
          double north = Double.valueOf(n_lat.getFirstChild().getNodeValue());

          // Create place
          Place place = Place.fromLatLonBox(name,world,north,south,east,west);
          LCARS.log("GAZ","Result: name=\""+name+"\"; camera=\""+place.camera+"\"");
          places.add(place);
        }
        catch (Exception e)
        {
          e.printStackTrace();
        }
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
    finally
    {
      // TODO: Close connection?
    }
    
    return places;
  }
  
  // -- Google Geocoding API (KML) --
  
  /**
   * @deprecated
   */
  // TODO: Fix character encoding!
  protected static AbstractList<Place> findPlacesViaGeocodeKML(String world, String address)
  {
    String        url    = null;
    Vector<Place> places = new Vector<Place>();

    // Get query URL
    if      (Place.ONEARTH.equals(world)) url = Q_EARTH_KML;
    else if (Place.ONMARS .equals(world)) url = Q_MARS_KML;
    else if (Place.ONMOON .equals(world)) url = Q_MOON_KML;
    else if (Place.ONSKY  .equals(world)) url = Q_SKY_KML;
    if (url==null) throw new IllegalArgumentException("["+world+": unknown world]");
    
    // Query
    LCARS.log("GAZ","Geocoding \""+address+"\" on "+world);
    url = url.replace("[ADDRESS]",urlEncode(address));

    try
    {
      // Get and parse KML
      LCARS.log("GAZ","HTTP GET \""+url+"\"");
      URLConnection conn = (new URL(url)).openConnection();
      conn.setRequestProperty("User-Agent","GoogleEarth");
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      DocumentBuilder db = dbf.newDocumentBuilder();
      Document doc = db.parse(conn.getInputStream());
      doc.getDocumentElement().normalize();
      //System.out.println(serializeXml(doc));
      
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
          places.add(Place.fromLookAt(name,world,lat,lon,rng));
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
          places.add(Place.fromLatLonBox(name,world,north,south,east,west));
          added = true;
        }
        
        if (!added)
          places.add(new Place(name,world,(Camera)null,null));
        
        LCARS.log("GAZ",log);
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
 
  // -- Helpers --
  
  /**
   * Returns the URL-encoding for a string.
   * 
   * @param string
   *          The string.
   * @return the URL-encoded version of the string.
   */
  protected final static String urlEncode(String string)
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
   * @param doc
   *          The XML node or document.
   * @return The XML text.
   */
  protected final static String serializeXml(Node root)
  {
    StringWriter xmlWriter  = new StringWriter();
    Transformer  serializer;
    try
    {
      serializer = TransformerFactory.newInstance().newTransformer();
      serializer.setOutputProperty(OutputKeys.ENCODING,"ISO-8859-1");
      serializer.setOutputProperty(OutputKeys.INDENT,"yes");
      serializer.transform(new DOMSource(root),new StreamResult(xmlWriter));       
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
    return xmlWriter.toString();
  }

  // == MAIN METHOD ==
  
  /**
   * DEBUGGING: Main method.
   * 
   * @param args
   *          The method does not use any command line arguments.
   */
  public static void main(String[] args)
  {
    LcarsGazetteer.findPlaces(Place.ONEARTH,"Europe");
    
/*
    //String url = "http://maps.google.com/maps?q=sky%3Am83&sll=50.9995,96.5138&sspn=141,360&output=kml&ie=utf-8&v=2.2&cv=6.1.0.5001&hl=de";
    //String url = "http://maps.google.com/maps?output=kml&oe=utf-8&q=Africa&hl=en";
    String url = "https://maps.googleapis.com/maps/api/geocode/xml?address=Africa&sensor=false";
    System.out.println("HTTP GET: "+url+"\n");

    HttpURLConnection conn;
    try
    {
      conn = (HttpURLConnection)(new URL(url)).openConnection();
      conn.setRequestMethod("GET");
      //conn.setRequestProperty("Accept","application/vnd.google-earth.kml+xml, application/vnd.google-earth.kmz, image");
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
*/
  }
}
