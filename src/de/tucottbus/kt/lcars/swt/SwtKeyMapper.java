package de.tucottbus.kt.lcars.swt;

import java.awt.event.KeyEvent;
import java.util.HashMap;

import org.eclipse.swt.SWT;

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
  
  private static final HashMap<Integer, Integer> swtAwtKeyMap;
  private static final HashMap<Character, Integer> swtAwtCharMap;
  
  static{
    swtAwtKeyMap = new HashMap<>(20);
    swtAwtKeyMap.put(SWT.F1,          KeyEvent.VK_F1    | (PRESSED << 16));
    swtAwtKeyMap.put(SWT.F2,          KeyEvent.VK_F2    | (PRESSED << 16));
    swtAwtKeyMap.put(SWT.F3,          KeyEvent.VK_F3    | (PRESSED << 16));
    swtAwtKeyMap.put(SWT.F4,          KeyEvent.VK_F4    | (PRESSED << 16));
    swtAwtKeyMap.put(SWT.F5,          KeyEvent.VK_F5    | (PRESSED << 16));
    swtAwtKeyMap.put(SWT.F6,          KeyEvent.VK_F6    | (PRESSED << 16));
    swtAwtKeyMap.put(SWT.F7,          KeyEvent.VK_F7    | (PRESSED << 16));
    swtAwtKeyMap.put(SWT.F8,          KeyEvent.VK_F8    | (PRESSED << 16));
    swtAwtKeyMap.put(SWT.F9,          KeyEvent.VK_F9    | (PRESSED << 16));
    swtAwtKeyMap.put(SWT.F10,         KeyEvent.VK_F10   | (PRESSED << 16));
    swtAwtKeyMap.put(SWT.F11,         KeyEvent.VK_F11   | (PRESSED << 16));
    swtAwtKeyMap.put(SWT.F12,         KeyEvent.VK_F12   | (PRESSED << 16));
    swtAwtKeyMap.put(SWT.F13,         KeyEvent.VK_F13   | (PRESSED << 16));
    swtAwtKeyMap.put(SWT.F14,         KeyEvent.VK_F14   | (PRESSED << 16));
    swtAwtKeyMap.put(SWT.F15,         KeyEvent.VK_F15   | (PRESSED << 16));
    swtAwtKeyMap.put(SWT.HELP,        KeyEvent.VK_HELP  | (PRESSED << 16));
    swtAwtKeyMap.put(SWT.ARROW_LEFT,  KeyEvent.VK_LEFT  | (PRESSED << 16));
    swtAwtKeyMap.put(SWT.ARROW_RIGHT, KeyEvent.VK_RIGHT | (PRESSED << 16));
    swtAwtKeyMap.put(SWT.ARROW_UP,    KeyEvent.VK_UP    | (PRESSED << 16));
    swtAwtKeyMap.put(SWT.ARROW_DOWN,  KeyEvent.VK_DOWN  | (PRESSED << 16));
    
    swtAwtCharMap = new HashMap<Character, Integer>(200);
    swtAwtCharMap.put('Q', KeyEvent.VK_Q);
    swtAwtCharMap.put('W', KeyEvent.VK_W);
    swtAwtCharMap.put('E', KeyEvent.VK_E);
    swtAwtCharMap.put('R', KeyEvent.VK_R);
    swtAwtCharMap.put('T', KeyEvent.VK_T);
    swtAwtCharMap.put('Y', KeyEvent.VK_Y);
    swtAwtCharMap.put('U', KeyEvent.VK_U);
    swtAwtCharMap.put('I', KeyEvent.VK_I);
    swtAwtCharMap.put('O', KeyEvent.VK_O);
    swtAwtCharMap.put('P', KeyEvent.VK_P);
    swtAwtCharMap.put('A', KeyEvent.VK_A);
    swtAwtCharMap.put('S', KeyEvent.VK_S);
    swtAwtCharMap.put('D', KeyEvent.VK_D);
    swtAwtCharMap.put('F', KeyEvent.VK_F);
    swtAwtCharMap.put('G', KeyEvent.VK_G);
    swtAwtCharMap.put('H', KeyEvent.VK_H);
    swtAwtCharMap.put('J', KeyEvent.VK_J);
    swtAwtCharMap.put('K', KeyEvent.VK_K);
    swtAwtCharMap.put('L', KeyEvent.VK_L);
    swtAwtCharMap.put('Z', KeyEvent.VK_Z);
    swtAwtCharMap.put('X', KeyEvent.VK_X);
    swtAwtCharMap.put('C', KeyEvent.VK_C);
    swtAwtCharMap.put('V', KeyEvent.VK_V);
    swtAwtCharMap.put('B', KeyEvent.VK_B);
    swtAwtCharMap.put('N', KeyEvent.VK_N);
    swtAwtCharMap.put('M', KeyEvent.VK_M);
    swtAwtCharMap.put('q', KeyEvent.VK_Q);
    swtAwtCharMap.put('w', KeyEvent.VK_W);
    swtAwtCharMap.put('e', KeyEvent.VK_E);
    swtAwtCharMap.put('r', KeyEvent.VK_R);
    swtAwtCharMap.put('t', KeyEvent.VK_T);
    swtAwtCharMap.put('y', KeyEvent.VK_Y);
    swtAwtCharMap.put('u', KeyEvent.VK_U);
    swtAwtCharMap.put('i', KeyEvent.VK_I);
    swtAwtCharMap.put('o', KeyEvent.VK_O);
    swtAwtCharMap.put('p', KeyEvent.VK_P);
    swtAwtCharMap.put('a', KeyEvent.VK_A);
    swtAwtCharMap.put('s', KeyEvent.VK_S);
    swtAwtCharMap.put('d', KeyEvent.VK_D);
    swtAwtCharMap.put('f', KeyEvent.VK_F);
    swtAwtCharMap.put('g', KeyEvent.VK_G);
    swtAwtCharMap.put('h', KeyEvent.VK_H);
    swtAwtCharMap.put('j', KeyEvent.VK_J);
    swtAwtCharMap.put('k', KeyEvent.VK_K);
    swtAwtCharMap.put('l', KeyEvent.VK_L);
    swtAwtCharMap.put('z', KeyEvent.VK_Z);
    swtAwtCharMap.put('x', KeyEvent.VK_X);
    swtAwtCharMap.put('c', KeyEvent.VK_C);
    swtAwtCharMap.put('v', KeyEvent.VK_V);
    swtAwtCharMap.put('b', KeyEvent.VK_B);
    swtAwtCharMap.put('n', KeyEvent.VK_N);
    swtAwtCharMap.put('m', KeyEvent.VK_M);
    swtAwtCharMap.put('1', KeyEvent.VK_1);
    swtAwtCharMap.put('2', KeyEvent.VK_2);
    swtAwtCharMap.put('3', KeyEvent.VK_3);
    swtAwtCharMap.put('4', KeyEvent.VK_4);
    swtAwtCharMap.put('5', KeyEvent.VK_5);
    swtAwtCharMap.put('6', KeyEvent.VK_6);
    swtAwtCharMap.put('7', KeyEvent.VK_7);
    swtAwtCharMap.put('8', KeyEvent.VK_8);
    swtAwtCharMap.put('9', KeyEvent.VK_9);
    swtAwtCharMap.put('0', KeyEvent.VK_0);
    swtAwtCharMap.put('+', KeyEvent.VK_PLUS);
    swtAwtCharMap.put('-', KeyEvent.VK_MINUS);
    swtAwtCharMap.put('.', KeyEvent.VK_PERIOD);
    swtAwtCharMap.put(':', KeyEvent.VK_COLON);
    swtAwtCharMap.put(',', KeyEvent.VK_COMMA);
    swtAwtCharMap.put(';', KeyEvent.VK_SEMICOLON);
    swtAwtCharMap.put('^', KeyEvent.VK_CIRCUMFLEX);
    swtAwtCharMap.put('\t', KeyEvent.VK_TAB);
    swtAwtCharMap.put('`', KeyEvent.VK_BACK_QUOTE);
    swtAwtCharMap.put('´', KeyEvent.VK_DEAD_ACUTE);
    swtAwtCharMap.put('=', KeyEvent.VK_EQUALS);
    swtAwtCharMap.put('~', KeyEvent.VK_DEAD_TILDE);
    swtAwtCharMap.put('!', KeyEvent.VK_EXCLAMATION_MARK);
    swtAwtCharMap.put('@', KeyEvent.VK_AT);
    swtAwtCharMap.put('#', KeyEvent.VK_NUMBER_SIGN);
    swtAwtCharMap.put('$', KeyEvent.VK_DOLLAR);
    swtAwtCharMap.put('%', KeyEvent.VK_5);
    swtAwtCharMap.put('&', KeyEvent.VK_AMPERSAND);
    swtAwtCharMap.put('*', KeyEvent.VK_ASTERISK);
    swtAwtCharMap.put('(', KeyEvent.VK_LEFT_PARENTHESIS);
    swtAwtCharMap.put(')', KeyEvent.VK_RIGHT_PARENTHESIS);
    swtAwtCharMap.put('_', KeyEvent.VK_UNDERSCORE);
    swtAwtCharMap.put('\n', KeyEvent.VK_ENTER);
    swtAwtCharMap.put('[', KeyEvent.VK_OPEN_BRACKET);
    swtAwtCharMap.put(']', KeyEvent.VK_CLOSE_BRACKET);
    swtAwtCharMap.put('\\', KeyEvent.VK_BACK_SLASH);
    swtAwtCharMap.put('{', KeyEvent.VK_OPEN_BRACKET);
    swtAwtCharMap.put('}', KeyEvent.VK_CLOSE_BRACKET);
    swtAwtCharMap.put('|', KeyEvent.VK_BACK_SLASH);
    swtAwtCharMap.put('"', KeyEvent.VK_QUOTEDBL);
    swtAwtCharMap.put('<', KeyEvent.VK_LESS);
    swtAwtCharMap.put('>', KeyEvent.VK_GREATER);
    swtAwtCharMap.put('/', KeyEvent.VK_SLASH);
    swtAwtCharMap.put('?', KeyEvent.VK_GREATER+1);
    swtAwtCharMap.put(' ', KeyEvent.VK_SPACE);
    swtAwtCharMap.put('\'', KeyEvent.VK_QUOTE);
  }
  
  public static int swt2AwtKeycode(org.eclipse.swt.events.KeyEvent swtEvent)
  {
    if ((swtEvent.keyCode & ~SWT.MODIFIER_MASK) == 0) // It's a modifier key
      return -1;
    
    Integer keycode = (swtEvent.keyCode & SWT.KEYCODE_BIT) == 0 ?
                      swtAwtCharMap.get(swtEvent.character) : // It's a unicode
                      swtAwtKeyMap.get(swtEvent.keyCode);     // It's a key code    
    return keycode != null ? keycode.intValue() : 0;
  }
  
  public static int transformStatemask(org.eclipse.swt.events.KeyEvent swtEvent)
  {
    return ((swtEvent.stateMask & SWT.ALT)     != 0 ? META_MASK  : 0) |
	   ((swtEvent.stateMask & SWT.COMMAND) != 0 ? ALT_MASK   : 0) |
	   ((swtEvent.stateMask & SWT.SHIFT)   != 0 ? SHIFT_MASK : 0) |
	   ((swtEvent.stateMask & SWT.CONTROL) != 0 ? CTRL_MASK  : 0);
  }
}
