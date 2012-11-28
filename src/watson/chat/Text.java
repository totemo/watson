package watson.chat;

// ----------------------------------------------------------------------------
/**
 * Represents Minecraft coloured text by separating out the colour escape
 * sequences and maintaining two parallel character sequences of equal length:
 * <ol>
 * <li>The unformatted characters.</li>
 * <li>The corresponding single colour code character for each unformatted
 * character, with formatting attributes squeezed into the upper bits, as
 * dictated by {@link Format}.</li>
 * </ol>
 * 
 * TODO: This class could have extra methods from String, such as substring(),
 * if that proves useful.
 */
public class Text
{
  // --------------------------------------------------------------------------
  /**
   * Return true if the character is one of the 6 characters that Minecraft uses
   * to signify formatting style ('k', 'l', 'm', 'n', 'o' or 'r').
   * 
   * Treat upper case letters as their lower case equivalent.
   * 
   * @return true if the character is one of the 6 characters that Minecraft
   *         uses to signify formatting style ('k', 'l', 'm', 'n', 'o' or 'r').
   */

  public static boolean isAttribute(char code)
  {
    char lower = Character.toLowerCase(code);
    return (lower >= 'k' && lower <= 'o') || lower == 'r';
  }

  // --------------------------------------------------------------------------
  /**
   * Constructor.
   * 
   * A pleasant side-effect of constructing a Text() is that consecutive
   * (redundant) colour escape sequences are collapsed down to just the last
   * colour.
   * 
   * @param text the text with embedded colour escape sequences.
   */
  public Text(String text)
  {
    // By default, text is white.
    Format format = new Format(Colour.white, 0);

    for (int i = 0; i < text.length(); ++i)
    {
      char c = text.charAt(i);
      if (c == Colour.ESCAPE_CHAR)
      {
        ++i;

        // Guard against the pathological case of a chat line ending in
        // Colour.ESCAPE_CHAR.
        if (i < text.length())
        {
          char code = text.charAt(i);
          if (Colour.isColour(code))
          {
            format.setColour(Colour.getByCode(code));
            // Setting the colour disables any style attributes.
            format.setStyles(0);
          }
          // Note: different from Format.isAttribute()
          else if (isAttribute(code))
          {
            format.applyStyle(code);
          }
          // else: silently delete format codes we don't understand.
          // At the time of writing, no such codes exist.
        }
      }
      else
      {
        // An ordinary, non-colour-escape character.
        _unformatted.append(c);
        _colourStyles.append(format.getColourStyle());
      }
    } // for
  } // Text

  // --------------------------------------------------------------------------
  /**
   * Return the full formatted representation of the Text, with colour escapes.
   * 
   * @return the full formatted representation of the Text, with colour escapes.
   */
  public String toString()
  {
    return toFormattedString();
  }

  // --------------------------------------------------------------------------
  /**
   * Return the full formatted representation of the Text, with colour escapes.
   * 
   * @return the full formatted representation of the Text, with colour escapes.
   */
  public String toFormattedString()
  {
    StringBuilder result = new StringBuilder();

    // Sentinel:
    char colourStyle = '\0';
    for (int i = 0; i < _unformatted.length(); ++i)
    {
      // Detect a change in colour or style and add colour escape to result.
      if (_colourStyles.charAt(i) != colourStyle)
      {
        // Set the new colour. This also clears the current style.
        char newColourStyle = _colourStyles.charAt(i);
        char colour = (char) (newColourStyle & Format.COLOUR_MASK);
        result.append(Colour.ESCAPE_CHAR);
        result.append(colour);

        if ((newColourStyle & Format.BOLD) != 0)
        {
          result.append(Colour.ESCAPE_CHAR);
          result.append('l');
        }
        if ((newColourStyle & Format.ITALIC) != 0)
        {
          result.append(Colour.ESCAPE_CHAR);
          result.append('o');
        }
        if ((newColourStyle & Format.UNDERLINE) != 0)
        {
          result.append(Colour.ESCAPE_CHAR);
          result.append('n');
        }
        if ((newColourStyle & Format.STRIKE) != 0)
        {
          result.append(Colour.ESCAPE_CHAR);
          result.append('m');
        }
        if ((newColourStyle & Format.RANDOM) != 0)
        {
          result.append(Colour.ESCAPE_CHAR);
          result.append('k');
        }
        colourStyle = newColourStyle;
      } // colour or style changed
      result.append(_unformatted.charAt(i));
    } // for
    return result.toString();
  } // toFormatttedString

  // --------------------------------------------------------------------------
  /**
   * Return the text without any colour formatting.
   * 
   * @return the text without any colour formatting.
   */
  public String toUnformattedString()
  {
    return _unformatted.toString();
  }

  // --------------------------------------------------------------------------
  /**
   * Set the format of the specified range of characters, [begin,end), (from
   * inclusive begin to exclusive end).
   * 
   * Note that (begin == end) is an empty range.
   * 
   * @param begin the index of the first character in the range.
   * @param end one more than the index of the last character in the range.
   * @param format the Format to set.
   * @throws IllegalArgumentException if begin or end are out of the range
   *           [0,toUnformattedString().length()] (inclusive) or (begin > end).
   */
  public void setFormat(int begin, int end, Format format)
  {
    if (begin < 0 || end > _unformatted.length() || begin > end)
    {
      throw new IllegalArgumentException("illegal range in setColour()");
    }

    // Does the format have a colour set?
    if (format.getColour() != null)
    {
      char colourStyle = format.getColourStyle();
      for (int i = begin; i < end; ++i)
      {
        _colourStyles.setCharAt(i, colourStyle);
      }
    }
    else
    {
      // No colour. Just set the style bits of the characters in the range.
      for (int i = begin; i < end; ++i)
      {
        int colourStyle = (_colourStyles.charAt(i) & Format.COLOUR_MASK)
                          | format.getStyles();
        _colourStyles.setCharAt(i, (char) colourStyle);
      }
    }
  } // setFormat

  // --------------------------------------------------------------------------
  /**
   * The unformatted version of the text.
   */
  protected StringBuilder _unformatted  = new StringBuilder();

  /**
   * The colour code characters for each character in _unformatted.
   * 
   * Invariant: _unformatted.length() == _colours.length() && (c in _colours ==>
   * c in {0-9, a-f}).
   */
  protected StringBuilder _colourStyles = new StringBuilder();
} // class Text