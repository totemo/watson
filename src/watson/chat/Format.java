package watson.chat;

// ----------------------------------------------------------------------------
/**
 * Represents Watson's encoding of Minecraft chat formatting codes, including
 * text formatting styles and colour.
 * 
 * Watson uses class {@link Colour} to represent the 16 Minecraft chat colours.
 * Formatting of chat (bold, italic, underline, strikethrough and random) is
 * expressed as punctuation marks: +, /, _, - and ? respectively. Thus, in the
 * Watson chat highlighter,"_red" is underlined red text and "+/blue" is bold,
 * italic blue text.
 * 
 * Style codes on their own set only the style, with no change in colour. So
 * "+-" would result in bold strikethrough text of the current colour.
 */
public class Format
{
  // --------------------------------------------------------------------------

  public static void main(String[] args)
  {
    Format format = new Format("+red");
  }

  // --------------------------------------------------------------------------
  /**
   * The {@link watson.ChatHighlighter} class smooshes the Minecraft colour
   * letter ('0'-'9', 'a'-'f') and 5 style bits into a 16-bit char, so style
   * bits are chosen such that they are outside the ASCII range.
   * 
   * Bitwise AND this constant with a combined character to get the style bits.
   */
  public static final int  STYLE_MASK     = (31 << 8);

  /**
   * Bitwise AND this constant with a combined character to get the Minecraft
   * colour code character.
   */
  public static final int  COLOUR_MASK    = ~STYLE_MASK;

  /**
   * Encodes bold formatting.
   */
  public static final int  BOLD           = (1 << 8);

  /**
   * Encodes italic formatting.
   */
  public static final int  ITALIC         = (1 << 9);

  /**
   * Encodes underline formatting.
   */
  public static final int  UNDERLINE      = (1 << 10);

  /**
   * Encodes strikethrough formatting.
   */
  public static final int  STRIKE         = (1 << 11);

  /**
   * Encodes "random characters" formatting.
   */
  public static final int  RANDOM         = (1 << 12);

  // --------------------------------------------------------------------------
  /**
   * The character code used to signify bold.
   */
  public static final char BOLD_CODE      = '+';

  /**
   * The character code used to signify italics.
   */
  public static final char ITALIC_CODE    = '/';

  /**
   * The character code used to signify underline.
   */
  public static final char UNDERLINE_CODE = '_';

  /**
   * The character code used to signify underline.
   */
  public static final char STRIKE_CODE    = '-';

  /**
   * The character code used to signify random glyphs.
   */
  public static final char RANDOM_CODE    = '?';

  // --------------------------------------------------------------------------
  /**
   * Return true if the specified character signifies a valid Minecraft
   * formatting style (but not colour) code.
   * 
   * @return true if the specified character signifies a valid Minecraft
   *         formatting style (but not colour) code.
   */
  public static boolean isStyle(char code)
  {
    return code == BOLD_CODE || code == ITALIC_CODE || code == UNDERLINE_CODE
           || code == STRIKE_CODE || code == RANDOM_CODE;
  }

  // --------------------------------------------------------------------------
  /**
   * Constructor.
   * 
   * @param colour the colour of the text; null for no change in colour.
   * @param styles the bit set of formatting styles; 0 for normal text.
   */
  public Format(Colour colour, int styles)
  {
    setColour(colour);
    setStyles(styles);
  }

  // --------------------------------------------------------------------------
  /**
   * Constructor.
   * 
   * @param code the parameter to the call to {@link #setCode(String)}.
   * @rthows IllegalArgumentException if the code is malformed.
   */
  public Format(String code)
  {
    setCode(code);
  }

  // --------------------------------------------------------------------------
  /**
   * Set this formatting style from the specified code.
   * 
   * The code should be of the form: [+/_-?]+[<colour>], where <colour> is the
   * name of a colour as recognised by {@link Colour}. If the <colour> is
   * omitted, the formatting styles will be set without changing the current
   * colour.
   * 
   * @param code the colour name, with leading style punctuation symbols.
   * @rthows IllegalArgumentException if the code is malformed.
   */
  public void setCode(String code)
  {
    for (int i = 0; i < code.length(); ++i)
    {
      char c = code.charAt(i);
      if (isStyle(c))
      {
        // applyStyle() accepts Minecraft codes as well. Doesn't matter
        // here.
        applyStyle(c);
      }
      else
      {
        String colour = code.substring(i);
        if (colour.length() != 0)
        {
          // Throws on invalid colour name or invalid style.
          setColour(Colour.getByCodeOrName(colour));
        }
        return;
      }
    } // for
  } // setCode

  // --------------------------------------------------------------------------
  /**
   * Return the string representation of this formatting code, suitable for
   * saving to file or displaying to the user.
   * 
   * @return the string representation of this formatting code, suitable for
   *         saving to file or displaying to the user.
   */
  public String getCode()
  {
    StringBuilder code = new StringBuilder();
    if (isStyles(BOLD))
    {
      code.append(BOLD_CODE);
    }
    if (isStyles(ITALIC))
    {
      code.append(ITALIC_CODE);
    }
    if (isStyles(UNDERLINE))
    {
      code.append(UNDERLINE_CODE);
    }
    if (isStyles(STRIKE))
    {
      code.append(STRIKE_CODE);
    }
    if (isStyles(RANDOM))
    {
      code.append(RANDOM_CODE);
    }
    if (getColour() != null)
    {
      code.append(getColour().name());
    }
    return code.toString();
  } // getCode

  // --------------------------------------------------------------------------
  /**
   * Return the string representation of this Format, which is the same as
   * getCode().
   * 
   * @return the string representation of this Format, which is the same as
   *         getCode().
   */
  public String toString()
  {
    return getCode();
  }

  // --------------------------------------------------------------------------
  /**
   * Set the colour.
   * 
   * @param colour the colour.
   */
  public void setColour(Colour colour)
  {
    _colour = colour;
  }

  // --------------------------------------------------------------------------
  /**
   * Return the colour.
   * 
   * @return the colour.
   */
  public Colour getColour()
  {
    return _colour;
  }

  // --------------------------------------------------------------------------
  /**
   * Add the formattting style indicated by the Minecraft or Watson-specific
   * style formatting code.
   * 
   * @param code the code ('+'. '/', '_', '-', /?', 'k' - 'o' or 'r').
   */
  public void applyStyle(char code)
  {
    switch (code)
    {
      case BOLD_CODE:
      case 'l':
        addStyles(BOLD);
        break;
      case ITALIC_CODE:
      case 'o':
        addStyles(ITALIC);
        break;
      case UNDERLINE_CODE:
      case 'n':
        addStyles(UNDERLINE);
        break;
      case STRIKE_CODE:
      case 'm':
        addStyles(STRIKE);
        break;
      case RANDOM_CODE:
      case 'k':
        addStyles(RANDOM);
        break;
      case 'r':
        setStyles(0);
        setColour(Colour.white);
      default:
        break;
    } // switch
  } // applyStyle

  // --------------------------------------------------------------------------
  /**
   * Set the specified formatting style(s).
   * 
   * @param styles a bitset of BOLD, ITALIC, UNDERLINE, STRIKE and RANDOM
   *          styles.
   */
  public void setStyles(int bits)
  {
    _styles = bits;
  }

  // --------------------------------------------------------------------------
  /**
   * Return the formatting style bit mask.
   * 
   * @return the formatting style bit mask.
   */
  public int getStyles()
  {
    return _styles;
  }

  // --------------------------------------------------------------------------
  /**
   * Return the combination of the Minecraft colour code character for the
   * colour and the bit flags representing the styles, as a char.
   * 
   * Technically, you probably don't want to call this method unless the Colour
   * is non-null, but to prevent a NullPointerException, that case is treated as
   * (getColour() == Colour.white).
   * 
   * @return the combination of the Minecraft colour code character for the
   *         colour and the bit flags representing the styles, as a char.
   */
  public char getColourStyle()
  {
    Colour colour = (getColour() == null) ? Colour.white : getColour();
    return (char) (colour.getCode() | getStyles());
  }

  // --------------------------------------------------------------------------
  /**
   * Add the specified formatting style(s).
   * 
   * @param styles a bitset of BOLD, ITALIC, UNDERLINE, STRIKE and RANDOM
   *          styles.
   */
  public void addStyles(int bits)
  {
    _styles |= bits;
  }

  // --------------------------------------------------------------------------
  /**
   * Remove the specified formatting style(s).
   * 
   * @param styles a bitset of BOLD, ITALIC, UNDERLINE, STRIKE and RANDOM
   *          styles.
   */
  public void removeStyles(int bits)
  {
    _styles &= ~bits;
  }

  // --------------------------------------------------------------------------
  /**
   * Return true if the specified style bits are set.
   * 
   * @return true if the specified style bits are set.
   */
  public boolean isStyles(int bits)
  {
    return (_styles & bits) != 0;
  }

  // --------------------------------------------------------------------------
  /**
   * The colour. Null can be used to signify no change in colour.
   */
  protected Colour _colour;

  /**
   * A bit set of BOLD, ITALIC, UNDERLINE, STRIKE and RANDOM styles.
   */
  protected int    _styles;
} // class Format