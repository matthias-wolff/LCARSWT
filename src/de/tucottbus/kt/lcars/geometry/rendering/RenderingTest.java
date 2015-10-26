package de.tucottbus.kt.lcars.geometry.rendering;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;

import de.tucottbus.kt.lcars.elements.EElement;
import de.tucottbus.kt.lcars.elements.ElementData;
import de.tucottbus.kt.lcars.logging.Log;

public class RenderingTest
{  
  private static HashMap<String, AtomicInteger> createCountMap(String[] filters)
  {
    HashMap<String, AtomicInteger> result = new HashMap<>(filters.length);
    for (String filter : filters)
      result.put(filter, new AtomicInteger(0));
    return result;
  }
  
  public static HashMap<String, AtomicInteger> countEE(Collection<EElement> elements, String[] filters)
  {
    HashMap<String, AtomicInteger> result = createCountMap(filters);
    
    for (EElement el : elements)
    {
      String label = el.getLabel();
      AtomicInteger count = result.get(label);
      if (count != null)
        count.incrementAndGet();
    }
    return result;
  }  
  
  public static HashMap<String, AtomicInteger> countED(Collection<ElementData> elements, String[] filters)
  {
    HashMap<String, AtomicInteger> result = createCountMap(filters);
    
    for (ElementData el : elements)
    {
      String str = el.toString();
      int i = str.indexOf("text=\"") + 6;
      if (i == 5) continue;
      str = str.substring(i, str.indexOf("\"", i+1));
      AtomicInteger count = result.get(str);
      if (count != null)
        count.incrementAndGet();
    }
    return result;
  }
  
  private static void doEvalAllInRange(HashMap<String, AtomicInteger> ctrs, int from, int to)
  {
    for (AtomicInteger ctr : ctrs.values())
      if (ctr.get() < from || ctr.get() > to)
      {
        Log.err("Counters out of range ["+from+".."+to+"] - " + ctrs);
        return;
      }
  }
  
  private static void doEvalAllSameCount(HashMap<String, AtomicInteger> ctrs)
  {
    Iterator<AtomicInteger> it = ctrs.values().iterator();
    if (!it.hasNext()) return;
    
    int count = it.next().get();
    
    while(it.hasNext())
      if (count != it.next().get())
      {
        Log.err("Not all in same count - " + ctrs);
        return;
      }
  }
  
  public static long[] idsOfLabels(Collection<EElement> elements, String[] filters)
  {
    long[] result = new long[filters.length];
    
    HashMap<String, Long> idMap = new HashMap<>(elements.size());
    for (EElement el : elements)
      if (el.getLabel() != null && !el.getLabel().isEmpty())
        idMap.put(el.getLabel(), el.getSerialNo());

    for (int i = 0; i < result.length; i++)
    {
      Long id = idMap.get(filters[i]);
      result[i] = id != null ? id.longValue() : -1;
    }
    return result;
  }
  
  private static final String[] alBraces = {"U2|735", "U1|735", "L1|735", "L2|735",
      "U2|1281", "U1|1281", "L1|1281", "L2|1281",
      "U2|1827", "U1|1827", "L1|1827", "L2|1827"};

  public static void checkAudioLibraryBraceCountEE(Collection<EElement> elements)
  {
    HashMap<String, AtomicInteger> ctrs = countEE(elements, alBraces);
    doEvalAllInRange(ctrs, 1, 1);
    doEvalAllSameCount(ctrs);
  }

  public static void checkAudioLibraryBraceCountED(Collection<ElementData> elements)
  {
    HashMap<String, AtomicInteger> ctrs = countED(elements, alBraces);
    doEvalAllInRange(ctrs, 0, 1);
    doEvalAllSameCount(ctrs);
  }

//int count = strCount.get("U2|735").get() > 0 ? 1 : 0;
//for (String str : strCount.keySet())
//  if (count != strCount.get(str).get())
//  {
//    Log.err(strCount.toString());
//    return;
//  }
//

//checkAudioLibraryBraceCountPanel
//checkAudioLibraryBraceCountScreen

}
