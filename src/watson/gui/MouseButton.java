package watson.gui;

// ----------------------------------------------------------------------------
/**
 * An enumeration that maps mouse clicks and scrolls to key codes for use in
 * {@link ModifiedKeyBinding}.
 *
 * ModifiedKeyBinding uses an integer to encode the key, using one of the LWJGL
 * Keyboard constants. We use a negative integer value to signify mouse buttons
 * and scroll wheel actions.
 */
public enum MouseButton
{
  MOUSE_LEFT(-100), MOUSE_RIGHT(-99), MOUSE_MIDDLE(-98), SCROLL_UP(-50), SCROLL_DOWN(-49);

  // --------------------------------------------------------------------------
  /**
   * Return the key code value used for this mouse button.
   *
   * @return the key code value used for this mouse button.
   */
  public int getCode()
  {
    return _code;
  }

  // --------------------------------------------------------------------------
  /**
   * Return the enum constant corresponding to the specified key code, or null
   * if not matched.
   *
   * @param code the key code.
   * @return the enum constant corresponding to the specified key code, or null
   *         if not matched.
   */
  public static MouseButton valueOf(int code)
  {
    MouseButton[] buttons = values();
    for (int i = 0; i < buttons.length; ++i)
    {
      if (buttons[i]._code == code)
      {
        return buttons[i];
      }
    }
    return null;
  }

  // --------------------------------------------------------------------------
  /**
   * Constructor.
   *
   * @param code the key code value to use.
   */
  private MouseButton(int code)
  {
    _code = code;
  }

  // --------------------------------------------------------------------------
  /**
   * Key code; should be negative so as not to clash with valid Keyboard
   * constants.
   */
  private int _code;
} // class MouseButton