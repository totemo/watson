package watson.chat;

// ----------------------------------------------------------------------------
/**
 * Represents Minecraft coloured text by separating out the colour escape
 * sequences and maintaining two parallel character sequences of equal length:
 * <ol>
 * <li>The unformatted characters.</li>
 * <li>The corresponding single colour code character for each unformatted
 * character.</li>
 * </ol>
 * 
 * TODO: This class could have extra methods from String, such as substring(),
 * if that proves useful.
 */
public class Text
{
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
    char colour = Colour.white.getCode();

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
          colour = text.charAt(i);
        }
      }
      else
      {
        // An ordinary, non-colour-escape character.
        _unformatted.append(c);
        _colours.append(colour);
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
    char colour = '\0';
    for (int i = 0; i < _unformatted.length(); ++i)
    {
      // Detect a change in colour and add colour escape to result.
      if (_colours.charAt(i) != colour)
      {
        colour = _colours.charAt(i);
        result.append(Colour.ESCAPE_CHAR);
        result.append(colour);
      }
      result.append(_unformatted.charAt(i));
    }
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
   * Set the colour of the specified range of characters, [begin,end), (from
   * inclusive begin , to exclusive end).
   * 
   * Note that (begin == end) is an empty range.
   * 
   * @param begin the index of the first character in the range.
   * @param end one more than the index of the last character in the range.
   * @param the Colour to set.
   * @throws IllegalArgumentException if begin or end are out of the range
   *           [0,toUnformattedString().length()] (inclusive) or (begin > end).
   */
  public void setColour(int begin, int end, Colour colour)
  {
    if (begin < 0 || end > _unformatted.length() || begin > end)
    {
      throw new IllegalArgumentException("illegal range in setColour()");
    }

    for (int i = begin; i < end; ++i)
    {
      _colours.setCharAt(i, colour.getCode());
    }
  } // setColour

  // --------------------------------------------------------------------------
  /**
   * The unformatted version of the text.
   */
  protected StringBuilder _unformatted = new StringBuilder();

  /**
   * The colour code characters for each character in _unformatted.
   * 
   * Invariant: _unformatted.length() == _colours.length() && (c in _colours ==>
   * c in {0-9, a-f}).
   */
  protected StringBuilder _colours     = new StringBuilder();
} // class Text