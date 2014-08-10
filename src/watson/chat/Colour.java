package watson.chat;

import java.util.HashMap;

// ----------------------------------------------------------------------------
/**
 * Represents a Minecraft colour sequence in chat.
 * 
 * NOTE: (compiler-generated) Colour.valueOf(String) will throw an
 * IllegalArgumentException if the specified colour name is not valid.
 */
public enum Colour
{
  black('0'), darkblue('1'), navy('1'), green('2'), darkgreen('2'), cyan('3'), red(
    '4'), darkred('4'), purple('5'), orange('6'), gold('6'), brown('6'), lightgrey(
    '7'), lightgray('7'), grey('8'), darkgrey('8'), gray('8'), darkgray('8'), blue(
    '9'), lightgreen('a'), lightblue('b'), lightred('c'), brightred('c'), rose(
    'c'), pink('d'), lightpurple('d'), magenta('d'), yellow('e'), white('f');

  /**
   * The character that begins a 2-character colour sequence.
   */
  public static final char   ESCAPE_CHAR   = '\247';

  /**
   * The character that begins a 2-character colour sequence, as a string.
   */
  public static final String ESCAPE_STRING = "" + ESCAPE_CHAR;

  // --------------------------------------------------------------------------
  /**
   * Return the single character colour code for this colour.
   * 
   * @return the single character colour code for this colour.
   */
  public char getCode()
  {
    return _code;
  }

  // --------------------------------------------------------------------------
  /**
   * Return the two-character escape sequence encoding this colour.
   * 
   * @return the two-character escape sequence encoding this colour.
   */
  public String getCodeString()
  {
    return _codeString;
  }

  // --------------------------------------------------------------------------
  /**
   * Return the canonical Colour corresponding to the specified colour code
   * character.
   * 
   * @param code the colour code character, in the character class [0-9a-fA-F].
   * @return the corresponding Colour.
   * @throws IllegalArgumentException if code is not a valid colour code.
   */
  public static Colour getByCode(char code)
  {
    Colour colour = _byCode.get(Character.toLowerCase(code));
    if (colour == null)
    {
      throw new IllegalArgumentException("invalid colour code: " + code);
    }
    return colour;
  }

  // --------------------------------------------------------------------------
  /**
   * Return the canonical Colour corresponding to the specified colour code
   * character or colour name.
   * 
   * @param codeOrName the single colour code character or colour name, handled
   *          case insensitively in both cases.
   * @return the corresponding Colour.
   * @throws IllegalArgumentException if code is not a valid colour code.
   */
  public static Colour getByCodeOrName(String codeOrName)
  {
    if (codeOrName.length() == 1)
    {
      return getByCode(codeOrName.charAt(0));
    }
    else
    {
      return Colour.valueOf(codeOrName.toLowerCase());
    }
  }

  // --------------------------------------------------------------------------
  /**
   * Return true if the specified character signifies a valid Minecraft colour
   * (but not formatting attribute) code.
   * 
   * @return true if the specified character signifies a valid Minecraft colour
   *         (but not formatting attribute) code.
   */
  public static boolean isColour(char code)
  {
    // Characer.isDigit() is too general for this.
    if (code >= '0' && code <= '9')
    {
      return true;
    }
    else
    {
      char lower = Character.toLowerCase(code);
      return lower >= 'a' && lower <= 'f';
    }
  } // isColour

  // --------------------------------------------------------------------------
  /**
   * Constructor.
   * 
   * @param code the colour code character.
   */
  private Colour(char code)
  {
    _code = code;
    _codeString = ESCAPE_STRING + code;
  }

  // --------------------------------------------------------------------------
  /**
   * The single character colour code for this colour.
   */
  private char                              _code;

  /**
   * The two-character escape sequence encoding this colour.
   */
  private String                            _codeString;

  /**
   * Map colour code characters to canonical Colour instances, and hence
   * canonical names via Colour.name(). Note that some colours have synonyms via
   * additional Colour instances with the same code.
   * 
   * The reverse mapping, from name to code, is achieved by
   * Colour.valueOf(name).getCode().
   */
  private static HashMap<Character, Colour> _byCode = new HashMap<Character, Colour>();

  // Register the canonical colour names (Colour.name()).
  static
  {
    _byCode.put('0', Colour.black);
    _byCode.put('1', Colour.darkblue);
    _byCode.put('2', Colour.green);
    _byCode.put('3', Colour.cyan);

    _byCode.put('4', Colour.red);
    _byCode.put('5', Colour.purple);
    _byCode.put('6', Colour.orange);
    _byCode.put('7', Colour.lightgray);

    _byCode.put('8', Colour.gray);
    _byCode.put('9', Colour.blue);
    _byCode.put('a', Colour.lightgreen);
    _byCode.put('b', Colour.lightblue);

    _byCode.put('c', Colour.lightred);
    _byCode.put('d', Colour.pink);
    _byCode.put('e', Colour.yellow);
    _byCode.put('f', Colour.white);
  };
} // class Colour
