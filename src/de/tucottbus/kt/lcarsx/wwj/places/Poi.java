package de.tucottbus.kt.lcarsx.wwj.places;

import java.io.InputStream;
import java.util.AbstractList;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.tucottbus.kt.lcars.logging.Log;

/**
 * <p><i><b style="color:red">Experimental.</b></i></p>
 * 
 * A list of places of interest on different worlds. 
 * 
 * @author Matthias Wolff, BTU Cottbus-Senftenberg
 */
public class Poi
{
  Vector<Place> places;
  
  /**
   * <p><i><b style="color:red">Experimental.</b></i></p>
   * 
   * Creates a new list of places from an XML input stream.
   * 
   * @param is
   *          The XML input stream.
   */
  public Poi(InputStream is)
  {
    places = new Vector<Place>();
    
    try
    {
      // Load XML file
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      DocumentBuilder db = dbf.newDocumentBuilder();
      Document doc = db.parse(is);
      doc.getDocumentElement().normalize();
      
      // Loop over <place>-tags
      NodeList nodeLst = doc.getElementsByTagName("place");
      for (int i=0; i<nodeLst.getLength(); i++)
      {
        Node node = nodeLst.item(i);
        if (node.getNodeType()!=Node.ELEMENT_NODE) continue;

        // Get name and world
        NamedNodeMap attr = node.getAttributes();
        String name  = attr.getNamedItem("name" ).getNodeValue();
        String world = attr.getNamedItem("world").getNodeValue();

        // Get camera (if present)
        String camera = null;
        NodeList camL = ((Element)node).getElementsByTagName("camera");
        if (camL.getLength()>0)
        {
          Node camN = camL.item(0);
          if (camN.getNodeType()==Node.ELEMENT_NODE && camN.getChildNodes().getLength()>0)
          {
            camN = camN.getChildNodes().item(0);
            if (camN.getNodeType()==Node.TEXT_NODE)
              camera = camN.getNodeValue();
          }
        }

        // Get grammar (if present)
        String grammar = null;
        NodeList grmL = ((Element)node).getElementsByTagName("grammar");
        if (grmL.getLength()>0)
        {
          Node grmN = grmL.item(0);
          if (grmN.getNodeType()==Node.ELEMENT_NODE && grmN.getChildNodes().getLength()>0)
          {
            grmN = grmN.getChildNodes().item(0);
            if (grmN.getNodeType()==Node.TEXT_NODE)
              grammar = grmN.getNodeValue();
          }
        }
        
        // Create a new place
        places.add(new Place(name,world,camera,grammar));
      }
    }
    catch (Exception e)
    {
      Log.err("Some error occured", e);
    }
  }
  
  /**
   * <p><i><b style="color:red">Experimental.</b></i></p>
   * 
   * Returns the list of places on one world.
   * 
   * @param world
   *          The world: {@link Place#ONEARTH}, {@link Place#ONMOON},
   *          {@link Place#ONMARS}, or {@link Place#ONSKY}.
   * @return The list of places.
   */
  public AbstractList<Place> getPlacesOn(String world)
  {
    Vector<Place> places = new Vector<Place>();
    for (Place place : this.places)
      if (place.world.equals(world))
        places.add(place);
    return places;
  }
}
