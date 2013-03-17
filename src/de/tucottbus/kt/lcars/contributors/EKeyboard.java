package de.tucottbus.kt.lcars.contributors;

import java.awt.Component;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.rmi.RemoteException;
import java.util.AbstractList;
import java.util.HashMap;
import java.util.Vector;

import de.tucottbus.kt.lcars.LCARS;
import de.tucottbus.kt.lcars.Panel;
import de.tucottbus.kt.lcars.elements.EElement;
import de.tucottbus.kt.lcars.elements.EEvent;
import de.tucottbus.kt.lcars.elements.EEventListener;
import de.tucottbus.kt.lcars.elements.ERect;
import de.tucottbus.kt.lcars.feedback.UserFeedback;

public abstract class EKeyboard extends ElementContributor implements EEventListener, KeyListener
{
  private   int                 rows;
  protected int                 keyHeight;
  private   int                 style;
  private   KeyMap              keyMap;
  private   Vector<KeyListener> listeners;
  private   long                lastKeyPressed; 

  public EKeyboard(int x, int y, int keyHeight, int style)
  {
    super(x,y);
    this.rows      = 0;
    this.keyHeight = keyHeight;
    this.style     = style & (LCARS.ES_COLOR|LCARS.ES_FONT);
    this.listeners = new Vector<KeyListener>();
    this.keyMap    = createKeyMap();
    layout();
  }

  @Override
  public void addToPanel(Panel panel)
  {
    super.addToPanel(panel);
    if (panel!=null)
      panel.addKeyListener(this);
  }

  @Override
  public void removeFromPanel()
  {
    if (panel!=null)
      panel.removeKeyListener(this);
    super.removeFromPanel();
  }

  /**
   * Creates the key mapping
   * 
   * @return a {@link KeyMap}
   */
  protected abstract KeyMap createKeyMap();
  
  /**
   * Creates the keyboard layout by adding individual keys or rows of keys.
   */
  protected abstract void layout();
  
  /**
   * Returns the current modifiers of this keyboard
   * 
   * @return 0 or a combination of {@link InputEvent#SHIFT_DOWN_MASK} and
   *         {@link InputEvent#ALT_GRAPH_DOWN_MASK} 
   */
  protected abstract int getModifiers();
  
  /**
   * Adds a row of <code>EKeyboard.</code>{@link Key}s.
   * 
   * @param keys the keys
   */
  public void addRow(Key[] keys, float indent)
  {
    int x = Math.round(indent*this.keyHeight);
    int y = this.rows*(this.keyHeight+3);
    for (int i=0; i<keys.length; i++)
    {
      int    style = this.style|LCARS.ES_LABEL_C;
      int    width = Math.round(this.keyHeight*keys[i].w);
      String label = keys[i].label;
      if (label==null)
        label = String.valueOf(keyMap.map(keys[i].code,0)).toUpperCase();
      ERect e = new ERect(null,x,y,width,this.keyHeight,style,label);
      e.setData(keys[i]);
      keys[i].e = e;
      e.addEEventListener(this);
      add(e);
      x+=width+3;
    }
    this.rows++;
  }
  
  public void addRow(Key[] keys)
  {
    addRow(keys,0);
  }
  
  public void addKey(int code, EElement e)
  {
    Key key = new Key(code,-1,e.getLabel());
    key.e = e;
    e.setData(key);
    e.addEEventListener(this);    
    add(e);
  }
  
  /**
   * Adds an extra key. Extra keys typically do not fit into the row layout
   * (e. g. the "Return" key on an alphanumeric keyboard).
   * 
   * @param e
   * @param normal
   * @param shift
   * @param symbol
   */
  public void addExtraKey(EElement e, String normal, String shift, String symbol)
  {
    // TODO: Implement EKeyboard.addExtraKey
  }
  
  public AbstractList<Key> findKeys(int keyCode)
  {
    Vector<Key> keys = new Vector<Key>();
    for (EElement e : getElements())
      if (e.getData()!=null && (e.getData() instanceof Key))
      {
        Key key = (Key)e.getData();
        if (key.code==keyCode)
          keys.add(key);
      }    
    return keys;
  }
  
  public void selectKeys(int keyCode, boolean selected)
  {
    for (Key key : findKeys(keyCode))
      key.e.setSelected(selected);
  }
  
  public boolean isKeySelected(int keyCode)
  {
    for (EElement e : getElements())
      if (e.getData()!=null && (e.getData() instanceof Key))
      {
        Key key = (Key)e.getData();
        if (key.code==keyCode)
          return e.isSelected();
      }
    return false;
  }
  
  public void modify()
  {
    
    for (EElement e : getElements())
      if (e.getData()!=null && (e.getData() instanceof Key))
      {
        Key key = (Key)e.getData();
        String label = key.label;
        if (label==null)
          label = String.valueOf(keyMap.map(key.code,getModifiers())).toUpperCase();
        if (label.equals(String.valueOf('\0')))
        {
          e.setDisabled(true);
          label="";
        }
        else
          e.setDisabled(false);
        e.setLabel(label);
      }
    if (panel!=null) panel.invalidate();
  }
  
  // -- Event handling --
  
  public void addKeyListener(KeyListener listener)
  {
    listeners.add(listener);
  }

  public void removeKeyListener(KeyListener listener)
  {
    listeners.remove(listener);
  }
  
  /**
   * Fires an AWT {@link KeyEvent} to all registered {@link KeyListener}s.
   * 
   * @see #addKeyListener(KeyListener)
   * @see #removeKeyListener(KeyListener)
   * @param key the key firing the event
   * @param eeid {@link EEvent#TOUCH_DOWN} or {@link EEvent#TOUCH_UP}
   */
  protected void fireKey(Key key, int eeid)
  {
    long time = System.currentTimeMillis();
    int  code = key.code;
    int  modi = getModifiers();
    int  vkun = KeyEvent.VK_UNDEFINED;
    char chun = KeyEvent.CHAR_UNDEFINED;
    char chr  = keyMap.map(key.code,modi);
    if (chr=='\0') chr = (char)key.code;
    
    if (eeid==EEvent.TOUCH_DOWN)
    {
      @SuppressWarnings("serial")
      Component c = new Component(){};
      KeyEvent ep = new KeyEvent(c,KeyEvent.KEY_PRESSED,time,modi,code,chun);
      KeyEvent et = new KeyEvent(c,KeyEvent.KEY_TYPED  ,time,modi,vkun,chr);
      for (KeyListener listener : listeners)
      {
        listener.keyPressed(ep);
        listener.keyTyped(et);
      }
    }
    else if (eeid==EEvent.TOUCH_UP)
    {
      KeyEvent er = new KeyEvent(null,KeyEvent.KEY_RELEASED,time,modi,code,chun);
      for (KeyListener listener : listeners)
        listener.keyPressed(er);
    }
  }
  
  // -- Implementation of KeyListener

  public void keyPressed(KeyEvent e)
  {
    if (panel==null) return;
    e.consume();
    AbstractList<Key> keys = findKeys(e.getKeyCode());
    for (Key key : keys)
      key.e.setSelected(true);
    for (KeyListener listener : listeners)
      listener.keyPressed(e);
    panel.invalidate();

    // HACK:Tackle auto-repeat
    if (e.getWhen()-lastKeyPressed>100)
      try
      {
        panel.getScreen().userFeedback(UserFeedback.Type.TOUCH);
      }
      catch (RemoteException e1)
      {
      }
    lastKeyPressed = e.getWhen();    
  }

  public void keyReleased(KeyEvent e)
  {
    if (panel==null) return;
    e.consume();
    AbstractList<Key> keys = findKeys(e.getKeyCode());
    for (Key key : keys)
      key.e.setSelected(false);
    for (KeyListener listener : listeners)
      listener.keyReleased(e);
    panel.invalidate();
  }

  public void keyTyped(KeyEvent e)
  {
    if (panel==null) return;
    e.consume();
    for (KeyListener listener : listeners)
      listener.keyTyped(e);
  }  
  
  // -- Implementation of EEventListener --

  @Override
  public void touchDown(EEvent ee)
  {
    Key key = (Key)ee.el.getData(); 

    if (key.code==KeyEvent.VK_SHIFT)
    {
      selectKeys(KeyEvent.VK_SHIFT,!isKeySelected(KeyEvent.VK_SHIFT));
      selectKeys(KeyEvent.VK_CAPS_LOCK,false);      
    }
    else if (key.code==KeyEvent.VK_ALT_GRAPH)
      selectKeys(KeyEvent.VK_ALT_GRAPH,!isKeySelected(KeyEvent.VK_ALT_GRAPH));
    else if (key.code==KeyEvent.VK_CAPS_LOCK)
      selectKeys(KeyEvent.VK_CAPS_LOCK,!isKeySelected(KeyEvent.VK_CAPS_LOCK));
    else
    {
      fireKey(key,ee.id);
      selectKeys(KeyEvent.VK_SHIFT    ,false);
      selectKeys(KeyEvent.VK_ALT_GRAPH,false);
    }
    modify();   
  }

  @Override
  public void touchDrag(EEvent ee)
  {
  }

  @Override
  public void touchHold(EEvent ee)
  {
  }

  @Override
  public void touchUp(EEvent ee)
  {
  }

  // -- Class Key --
  
  /**
   * Instances of this class represent one key on an {@link EKeyboard}.
   */
  public class Key
  {
    EElement e;
    float    w;
    int      code;
    String   label;
    
    /**
     * Creates a new key.
     * 
     * @param code
     *          one of the {@link KeyEvent}<code>.VK_XXX</code> constants
     * @param w
     *          the key width relative to the keyboards {@link EKeyboard#keyHeight}
     *          (1.0f will create a quadratic key)
     * @param label
     *          the key's label (only actually displayed if no {@link KeyMap}
     *          is created for the key)
     */
    public Key(int code, float w, String label)
    {
      this.code  = code;
      this.w     = w;
      this.label = label;
    }
    
  }
  
  // -- Class KeyMap --
  
  /**
   * Instances of this class map key codes ({@link KeyEvent}<code>.VK_XXX</code>
   * constants) to characters.
   */
  public class KeyMap
  {
    private HashMap<Integer,Character[]> keyMap;
    
    /**
     * Creates a new key map.
     */
    public KeyMap()
    {
      keyMap = new HashMap<Integer, Character[]>();
    }
    
    /**
     * Adds a key mapping.
     * 
     * @param keyCode
     *          one of the {@link KeyEvent}<code>.VK_XXX</code> constants
     * @param normal
     *          the mapped character without modifiers
     * @param shift
     *          the mapped character with the {@link InputEvent#SHIFT_DOWN_MASK}
     *          modifier
     * @param symbol
     *          the mapped character with the {@link InputEvent#ALT_GRAPH_DOWN_MASK}
     *          modifier
     * @param symshift
     *          the mapped character with the {@link InputEvent#SHIFT_DOWN_MASK}
     *          and {@link InputEvent#ALT_GRAPH_DOWN_MASK} modifiers
     */
    public void put(int keyCode, char normal, char shift, char symbol, char symshift)
    {
      Character[] chars = new Character[4];
      chars[0] = normal;
      chars[1] = shift;
      chars[2] = symbol;
      chars[3] = symshift;
      keyMap.put(keyCode,chars);
    }
    
    /**
     * Adds a key mapping.
     * 
     * @param keyCode
     *          one of the {@link KeyEvent}<code>.VK_XXX</code> constants
     * @param normal
     *          the mapped character without modifiers
     * @param shift
     *          the mapped character with the {@link InputEvent#SHIFT_DOWN_MASK}
     *          modifier
     */
    public void put(int keyCode, char normal, char shift)
    {
      put(keyCode,normal,shift,'\0','\0');
    }
    
    /**
     * Adds a key mapping.
     * 
     * @param keyCode
     *          one of the {@link KeyEvent}<code>.VK_XXX</code> constants
     * @param normal
     *          the mapped character without modifiers
     * @param shift
     *          the mapped character with the {@link InputEvent#SHIFT_DOWN_MASK}
     *          modifier
     * @param symbol
     *          the mapped character with the {@link InputEvent#ALT_GRAPH_DOWN_MASK}
     *          modifier
     */
    public void put(int keyCode, char normal, char shift, char symbol)
    {
      put(keyCode,normal,shift,symbol,'\0');
    }
    
    /**
     * Returns the character mapped to a key. 
     * 
     * @param keyCode
     *          one of the {@link KeyEvent}<code>.VK_XXX</code> constants
     * @param modifiers
     *          0 or a combination of {@link InputEvent#SHIFT_DOWN_MASK} and
     *          {@link InputEvent#ALT_GRAPH_DOWN_MASK} 
     * @return the character mapped to this key code and modifiers
     */
    public char map(int keyCode, int modifiers)
    {
      Character[] chars = keyMap.get(keyCode);
      if (chars==null) return '\0';
      
      int shift    = InputEvent.SHIFT_DOWN_MASK;
      int symbol   = InputEvent.ALT_GRAPH_DOWN_MASK;
      int symshift = shift|symbol; 
      
      if      (modifiers==0       ) return chars[0];
      else if (modifiers==shift   ) return chars[1];
      else if (modifiers==symbol  ) return chars[2];
      else if (modifiers==symshift) return chars[3];
      return '\0';
    }
  }
  
}
