package watson.gui;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

// ----------------------------------------------------------------------------
/**
 * A key binding implementation that understands the Ctrl, Alt and Shift
 * modifiers.
 */
public class ModifiedKeyBinding
{
  // --------------------------------------------------------------------------
  /**
   * Return true if the specified key code is the left or right Ctrl key.
   *
   * @param keyCode the key code.
   * @return true if the specified key code is the left or right Ctrl key.
   */
  public static boolean isCtrl(int keyCode)
  {
    return keyCode == Keyboard.KEY_LCONTROL || keyCode == Keyboard.KEY_RCONTROL;
  }

  // --------------------------------------------------------------------------
  /**
   * Return true if the specified key code is the left or right Alt key.
   *
   * @param keyCode the key code.
   * @return true if the specified key code is the left or right Alt key.
   */
  public static boolean isAlt(int keyCode)
  {
    return keyCode == Keyboard.KEY_LMENU || keyCode == Keyboard.KEY_RMENU;
  }

  // --------------------------------------------------------------------------
  /**
   * Return true if the specified key code is the left or right Shift key.
   *
   * @param keyCode the key code.
   * @return true if the specified key code is the left or right Shift key.
   */
  public static boolean isShift(int keyCode)
  {
    return keyCode == Keyboard.KEY_LSHIFT || keyCode == Keyboard.KEY_RSHIFT;
  }

  // --------------------------------------------------------------------------
  /**
   * Return true if a Ctrl key is currently held down.
   *
   * @return true if a Ctrl key is currently held down.
   */
  public static boolean isCtrlDown()
  {
    return isKeyDown(Keyboard.KEY_LCONTROL) || isKeyDown(Keyboard.KEY_RCONTROL);
  }

  // --------------------------------------------------------------------------
  /**
   * Return true if an Alt key is currently held down.
   *
   * @return true if an Alt key is currently held down.
   */
  public static boolean isAltDown()
  {
    return isKeyDown(Keyboard.KEY_LMENU) || isKeyDown(Keyboard.KEY_RMENU);
  }

  // --------------------------------------------------------------------------
  /**
   * Return true if a Shift key is currently held down.
   *
   * @return true if a Shift key is currently held down.
   */
  public static boolean isShiftDown()
  {
    return isKeyDown(Keyboard.KEY_LSHIFT) || isKeyDown(Keyboard.KEY_RSHIFT);
  }

  // --------------------------------------------------------------------------
  /**
   * Return a string describing the specified modifiers.
   *
   * @param control true if the Ctrl key is held.
   * @param alt true if the Alt key is held.
   * @param shift true if the Shift key is held.
   * @return a string describing the specified modifiers.
   */
  public static String getModifierString(boolean control, boolean alt, boolean shift)
  {
    StringBuilder s = new StringBuilder();
    if (control)
    {
      s.append("Ctrl + ");
    }
    if (alt)
    {
      s.append("Alt + ");
    }
    if (shift)
    {
      s.append("Shift + ");
    }
    return s.toString();
  }

  // --------------------------------------------------------------------------
  /**
   * Constructor.
   *
   * @param description the internationalisation key for the description of this
   *          key binding displayed in the GUI.
   * @param keyCode the key code.
   * @param category the internationalisation key for the category of this key
   *          binding displayed in the GUI.
   * @param Ctrl true if the (left or right) Ctrl key is expected.
   * @param alt true if the (left or right) Alt key is expected.
   * @param shift true if the (left or right) Shift key is expected.
   */
  public ModifiedKeyBinding(String description, int keyCode, String category,
                            boolean ctrl, boolean alt, boolean shift)
  {
    _description = description;
    _keyCode = keyCode;
    _category = category;
    _ctrl = ctrl;
    _alt = alt;
    _shift = shift;
  }

  // --------------------------------------------------------------------------
  /**
   * Return true if the key combination is pressed.
   *
   * This method checks that all required modifier keys are held down and that
   * the keyCode parameter matches getKeyCode(). Note that the non-modifier key
   * must be pressed last for the activation to be detected.
   *
   * The method should be called from the event handling code that deals with a
   * key press event for keyCode.
   *
   * @param keyCode the key code or mouse button/scroll pressed last.
   * @return true if the key combination is pressed.
   */
  public boolean isActivated(int keyCode)
  {
    // Backslash generates KEY_NONE for me. But NONE signifies keybind unset.
    if (keyCode == Keyboard.KEY_NONE)
    {
      return false;
    }

    boolean ctrlDown = isKeyDown(Keyboard.KEY_LCONTROL) || isKeyDown(Keyboard.KEY_RCONTROL);
    boolean altDown = isKeyDown(Keyboard.KEY_LMENU) || isKeyDown(Keyboard.KEY_RMENU);
    boolean shiftDown = isKeyDown(Keyboard.KEY_LSHIFT) || isKeyDown(Keyboard.KEY_RSHIFT);
    boolean hasRequiredModifiers = isCtrl() == ctrlDown && isAlt() == altDown && isShift() == shiftDown;
    return _keyCode == keyCode && hasRequiredModifiers;
  }

  // --------------------------------------------------------------------------
  /**
   * Return true if the key combination is currently held down (with correct
   * modifiers) and was not held down the last time this method was called.
   *
   * This method is intended to be called in contexts where we don't have access
   * to the key press event for the main key in the key combination and must
   * instead poll the key in onTick(). Since the combination may be held down
   * for multiple ticks, we need to detect the transition from the not-held-down
   * state to the held-down state.
   *
   * @return true if the key combination is currently held down (with correct
   *         modifiers) and was not held down the last time this method was
   *         called.
   */
  public boolean isHeld()
  {
    // Backslash generates KEY_NONE for me. But NONE signifies keybind unset.
    if (_keyCode == Keyboard.KEY_NONE)
    {
      return false;
    }

    boolean ctrlDown = isKeyDown(Keyboard.KEY_LCONTROL) || isKeyDown(Keyboard.KEY_RCONTROL);
    boolean altDown = isKeyDown(Keyboard.KEY_LMENU) || isKeyDown(Keyboard.KEY_RMENU);
    boolean shiftDown = isKeyDown(Keyboard.KEY_LSHIFT) || isKeyDown(Keyboard.KEY_RSHIFT);
    boolean hasRequiredModifiers = isCtrl() == ctrlDown && isAlt() == altDown && isShift() == shiftDown;
    boolean mainKeyDown = isKeyDown(_keyCode);

    if (mainKeyDown && hasRequiredModifiers)
    {
      // Detect the transition from not-held-down to held-down.
      if (!_held)
      {
        _held = true;
        return true;
      }
    }
    else
    {
      // Key combination is no longer held down.
      _held = false;
    }
    return false;
  } // isHeld

  // --------------------------------------------------------------------------
  /**
   * Return the description of this key binding.
   *
   * @return the description of this key binding.
   */
  public String getDescription()
  {
    return _description;
  }

  // --------------------------------------------------------------------------
  /**
   * Set the description of this key binding.
   *
   * @param description the description.
   */
  public void setDescription(String description)
  {
    _description = description;
  }

  // --------------------------------------------------------------------------
  /**
   * Return the category of this key binding.
   *
   * @return the category of this key binding.
   */
  public String getCategory()
  {
    return _category;
  }

  // --------------------------------------------------------------------------
  /**
   * Set the category of this key binding.
   *
   * @param category the category of this key binding.
   */
  public void setCategory(String category)
  {
    _category = category;
  }

  // --------------------------------------------------------------------------
  /**
   * Return the key code.
   *
   * @return the key code.
   */
  public int getKeyCode()
  {
    return _keyCode;
  }

  // --------------------------------------------------------------------------
  /**
   * Set the key code.
   *
   * Mouse buttons can be signified by using a keyCode value that is the mouse
   * button code (which will be negative) plus 100.
   *
   * @param keyCode the key code.
   */

  public void setKeyCode(int keyCode)
  {
    _keyCode = keyCode;
  }

  // --------------------------------------------------------------------------
  /**
   * Return true if the (left or right) Ctrl key is expected.
   *
   * @return true if the (left or right) Ctrl key is expected.
   */
  public boolean isCtrl()
  {
    return _ctrl;
  }

  // --------------------------------------------------------------------------
  /**
   * Specify whether the (left or right) Ctrl key is expected.
   *
   * @param ctrl if true, a Ctrl key is expected.
   */
  public void setCtrl(boolean ctrl)
  {
    _ctrl = ctrl;
  }

  // --------------------------------------------------------------------------
  /**
   * Return true if the (left or right) Alt key is expected.
   *
   * @return true if the (left or right) Alt key is expected.
   */
  public boolean isAlt()
  {
    return _alt;
  }

  // --------------------------------------------------------------------------
  /**
   * Specify whether the (left or right) Alt key is expected.
   *
   * @param alt if true, a Alt key is expected.
   */
  public void setAlt(boolean alt)
  {
    _alt = alt;
  }

  // --------------------------------------------------------------------------
  /**
   * Return true if the (left or right) Shift key is expected.
   *
   * @return true if the (left or right) Shift key is expected.
   */
  public boolean isShift()
  {
    return _shift;
  }

  // --------------------------------------------------------------------------
  /**
   * @param shift
   */
  public void setShift(boolean shift)
  {
    _shift = shift;
  }

  // --------------------------------------------------------------------------
  /**
   * Specify whether this key binding depends on the Watson display being
   * visible in order to activate.
   *
   * This property is true by default; key bindings won't activate when the
   * display is disabled.
   *
   * @param displayDependent if true, the key binding can only be activated if
   *          the Watson display is visible.
   */
  public void setDisplayDependent(boolean displayDependent)
  {
    _displayDependent = displayDependent;
  }

  // --------------------------------------------------------------------------
  /**
   * Return true if this key binding depends on the Watson display being visible
   * in order to activate.
   *
   * @return true if this key binding depends on the Watson display being
   *         visible in order to activate.
   */
  public boolean isDisplayDependent()
  {
    return _displayDependent;
  }

  // --------------------------------------------------------------------------
  /**
   * Format this key combination as a String.
   *
   * This string representation is used in the GUI and is also used to save the
   * key in the configuration file.
   *
   * @return the string representation of the key combination.
   */
  public String toString()
  {
    StringBuilder s = new StringBuilder();
    s.append(getModifierString(_ctrl, _alt, _shift));
    if (_keyCode < 0)
    {
      MouseButton mouse = MouseButton.valueOf(_keyCode);
      s.append(mouse.name());
    }
    else
    {
      s.append(Keyboard.getKeyName(_keyCode));
    }
    return s.toString();
  }

  // --------------------------------------------------------------------------
  /**
   * Parse this key combination from its string representation, as generated by
   * toString() and set the current instance if parsing was successful.
   *
   * @param s the string representation of the key combination.
   * @return true if the string could be parsed in full.
   */
  public boolean parse(String s)
  {
    // True if the key code part is parsed successfully.
    boolean codeParsed = false;
    int code = Keyboard.KEY_NONE;
    boolean ctrl = false;
    boolean alt = false;
    boolean shift = false;
    String[] parts = s.split("\\s?\\+\\s?");
    for (int i = 0; i < parts.length; ++i)
    {
      String part = parts[i].toUpperCase();
      if (i == parts.length - 1)
      {
        // Parsing the final part: the key code.
        code = Keyboard.getKeyIndex(part);
        if (code != Keyboard.KEY_NONE)
        {
          codeParsed = true;
        }
        else
        {
          // A code of NONE is used to signify "not found" by getKeyIndex().
          // But it could also be literally the string "NONE".
          if (part.toUpperCase().equals("NONE"))
          {
            codeParsed = true;
          }
          else
          {
            // Key code not found. Check for a MouseButton enum.
            for (MouseButton button : MouseButton.values())
            {
              if (part.toUpperCase().equals(button.name()))
              {
                code = button.getCode();
                break;
              }
            }
          }
        }
      }
      else
      {
        // Parsing a modifier.
        boolean modifierParsed = false;
        if (part.equals("CTRL"))
        {
          ctrl = true;
          modifierParsed = true;
        }
        else if (part.equals("ALT"))
        {
          alt = true;
          modifierParsed = true;
        }
        else if (part.equals("SHIFT"))
        {
          shift = true;
          modifierParsed = true;
        }

        if (!modifierParsed)
        {
          // There's something in the text that we don't understand.
          return false;
        }

      } // else not the last part - a modifier
    } // for

    if (codeParsed)
    {
      _ctrl = ctrl;
      _alt = alt;
      _shift = shift;
      _keyCode = code;
      return true;
    }
    else
    {
      return false;
    }
  } // parse

  // --------------------------------------------------------------------------
  /**
   * Set the callback object that handles this key binding.
   *
   * @param handler the callback object that handles this key binding.
   */
  public void setHandler(Runnable handler)
  {
    _handler = handler;
  }

  // --------------------------------------------------------------------------
  /**
   * Return the callback object that handles this key binding.
   *
   * @return the callback object that handles this key binding.
   */
  public Runnable getHandler()
  {
    return _handler;
  }

  // --------------------------------------------------------------------------
  /**
   * Perform that handler action associated with this key binding.
   */
  public void perform()
  {
    if (_handler != null)
    {
      _handler.run();
    }
  }

  // --------------------------------------------------------------------------
  /**
   * Return true if the specified key is currently down.
   *
   * @param keyCode the key code (can be a negative number for mouse buttons).
   * @return true if the specified key is currently down.
   */
  protected static boolean isKeyDown(int keyCode)
  {
    try
    {
      if (keyCode < 0)
      {
        return Mouse.isButtonDown(keyCode + 100);
      }
      return Keyboard.isKeyDown(keyCode);
    }
    catch (Exception ex)
    {
      return false;
    }
  }

  // --------------------------------------------------------------------------
  /**
   * The internationalisation key for the description of this key binding
   * displayed in the GUI.
   */
  protected String   _description;

  /**
   * The internationalisation key for the category of this key binding displayed
   * in the GUI.
   */
  protected String   _category;

  /**
   * The key code in org.lwjgl.input.Keyboard.
   */
  protected int      _keyCode;

  /**
   * True if the (left or right) Ctrl key is expected.
   */
  protected boolean  _ctrl;

  /**
   * True if the (left or right) Alt key is expected.
   */
  protected boolean  _alt;

  /**
   * True if the (left or right) Shift key is expected.
   */
  protected boolean  _shift;

  /**
   * If true, the key binding can only be activated if the Watson display is
   * visible.
   */
  protected boolean  _displayDependent = true;

  /**
   * True if the key combination is currently held down.
   */
  protected boolean  _held             = false;

  /**
   * The callback object that handles this key binding.
   */
  protected Runnable _handler;
} // class ModifiedKeyBinding
