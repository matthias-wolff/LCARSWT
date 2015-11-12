package de.tucottbus.kt.lcars.swt;

import java.util.HashMap;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;

/**
 * A utility class to do mapping between swt- and awt keyevents
 * source: https://sourceware.org/svn/kawa/trunk/gnu/jemacs/buffer/EKeymap.java
 * 
 */
public class SwtKeyMapper
{
  public static final int CTRL_MASK = java.awt.event.InputEvent.CTRL_MASK;
  public static final int SHIFT_MASK = java.awt.event.InputEvent.SHIFT_MASK;
  // Note ALT_MASK and META_MASK are shifted!
  public static final int META_MASK = java.awt.event.InputEvent.ALT_MASK;
  public static final int ALT_MASK = java.awt.event.InputEvent.META_MASK;

  public static int PRESSED = 0x100;
  public static int RELEASED = 0x200;
  
  public static final HashMap<Integer, Integer> swtAwtKeyMap;
  
  static{
    swtAwtKeyMap = new HashMap<>();
    swtAwtKeyMap.put(SWT.F1,          java.awt.event.KeyEvent.VK_F1    | (PRESSED << 16));
    swtAwtKeyMap.put(SWT.F2,          java.awt.event.KeyEvent.VK_F2    | (PRESSED << 16));
    swtAwtKeyMap.put(SWT.F3,          java.awt.event.KeyEvent.VK_F3    | (PRESSED << 16));
    swtAwtKeyMap.put(SWT.F4,          java.awt.event.KeyEvent.VK_F4    | (PRESSED << 16));
    swtAwtKeyMap.put(SWT.F5,          java.awt.event.KeyEvent.VK_F5    | (PRESSED << 16));
    swtAwtKeyMap.put(SWT.F6,          java.awt.event.KeyEvent.VK_F6    | (PRESSED << 16));
    swtAwtKeyMap.put(SWT.F7,          java.awt.event.KeyEvent.VK_F7    | (PRESSED << 16));
    swtAwtKeyMap.put(SWT.F8,          java.awt.event.KeyEvent.VK_F8    | (PRESSED << 16));
    swtAwtKeyMap.put(SWT.F9,          java.awt.event.KeyEvent.VK_F9    | (PRESSED << 16));
    swtAwtKeyMap.put(SWT.F10,         java.awt.event.KeyEvent.VK_F10   | (PRESSED << 16));
    swtAwtKeyMap.put(SWT.F11,         java.awt.event.KeyEvent.VK_F11   | (PRESSED << 16));
    swtAwtKeyMap.put(SWT.F12,         java.awt.event.KeyEvent.VK_F12   | (PRESSED << 16));
    swtAwtKeyMap.put(SWT.F13,         java.awt.event.KeyEvent.VK_F13   | (PRESSED << 16));
    swtAwtKeyMap.put(SWT.F14,         java.awt.event.KeyEvent.VK_F14   | (PRESSED << 16));
    swtAwtKeyMap.put(SWT.F15,         java.awt.event.KeyEvent.VK_F15   | (PRESSED << 16));
    swtAwtKeyMap.put(SWT.HELP,        java.awt.event.KeyEvent.VK_HELP  | (PRESSED << 16));
    swtAwtKeyMap.put(SWT.ARROW_LEFT,  java.awt.event.KeyEvent.VK_LEFT  | (PRESSED << 16));
    swtAwtKeyMap.put(SWT.ARROW_RIGHT, java.awt.event.KeyEvent.VK_RIGHT | (PRESSED << 16));
    swtAwtKeyMap.put(SWT.ARROW_UP,    java.awt.event.KeyEvent.VK_UP    | (PRESSED << 16));
    swtAwtKeyMap.put(SWT.ARROW_DOWN,  java.awt.event.KeyEvent.VK_DOWN  | (PRESSED << 16));
  }
  
  public static int swtKey2EKey(KeyEvent swtEvent)
  {
    int mods = transformStatemask(swtEvent);
   
    if ((swtEvent.keyCode & ~SWT.MODIFIER_MASK) == 0) // It's a modifier key
    {
      return -1;
    }
    else if ((swtEvent.keyCode & SWT.KEYCODE_BIT) == 0) // It's a unicode
    {
      char ch = swtEvent.character;
      if (mods == SHIFT_MASK && ch != swtEvent.keyCode)
      {
        // ch already 'contains' shift so no need to flag it.
        mods = 0;
      }
      if (0 < ch  && ch < 0x1A)
      {
        mods |= PRESSED;
        if ((mods & CTRL_MASK) != 0)
        	ch = (char) (ch + 'A' - 1);
      }
      return ch | (mods << 16);
    }
    else // It's a key code.
    {
      Integer keyKeycode = swtAwtKeyMap.get(swtEvent.keyCode);
      return keyKeycode != null ? keyKeycode.intValue() : 0;
    }
  }

  public static int transformStatemask(KeyEvent swtEvent)
  {
    return ((swtEvent.stateMask & SWT.ALT)     != 0 ? META_MASK  : 0) |
	   ((swtEvent.stateMask & SWT.COMMAND) != 0 ? ALT_MASK   : 0) |
	   ((swtEvent.stateMask & SWT.SHIFT)   != 0 ? SHIFT_MASK : 0) |
	   ((swtEvent.stateMask & SWT.CONTROL) != 0 ? CTRL_MASK  : 0);
  }
}
