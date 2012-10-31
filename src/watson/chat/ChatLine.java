package watson.chat;

// --------------------------------------------------------------------------
/**
 * Represents part of line that was split by the server.
 * 
 * It is seen by the client as a single line of chat text, but it may actually
 * be part of a longer line.
 */
public class ChatLine
{
  // --------------------------------------------------------------------------
  /**
   * Constructor.
   * 
   * @param chatText the formatted text of the chat message (with colours)
   *          exactly as it was sent to the client. It is assumed not to contain
   *          any line break characters (CR/NL).
   */
  public ChatLine(String chatText)
  {
    // Store formatted (coloured) and unformatted versions.
    // TODO: ImprovedChat sources indicate that splitting between \247 and next
    // character is possible here.
    // NOTE: § is \247
    _formatted = chatText;
    _unformatted = chatText.replaceAll("\247[0-9a-fA-F]", "");
  }
  // --------------------------------------------------------------------------
  /**
   * Set the ChatCategory that matches this line.
   * 
   * @param category the ChatCategory that matches this line.
   */
  public void setCategory(ChatCategory category)
  {
    _category = category;
  }

  // --------------------------------------------------------------------------
  /**
   * Return the ChatCategory that matches this line.
   * 
   * @return the ChatCategory that matches this line.
   */
  public ChatCategory getCategory()
  {
    return _category;
  }

  // --------------------------------------------------------------------------
  /**
   * Return the last colour code in this line as the single character after the
   * paragraph mark (§), or ChatClassifier.DEFAULT_COLOUR if the line has no
   * colour information.
   * 
   * @return the last colour code in this line as the single character after the
   *         paragraph mark (§), or ChatClassifier.DEFAULT_COLOUR if the line
   *         has no colour information.
   */
  public char getLastColour()
  {
    int index = getFormatted().lastIndexOf(ChatClassifier.COLOUR_CHAR);
    // It is possible that (index+1) could be out of bounds, if the line was
    // split between § and the colour code.
    if (index < 0 || index + 1 >= getFormatted().length())
    {
      return ChatClassifier.DEFAULT_COLOUR;
    }
    else
    {
      return getFormatted().charAt(index + 1);
    }
  } // getLastColour

  // --------------------------------------------------------------------------
  /**
   * If this line begins with a colour code, return that colour code as a single
   * character, or ChatClassifier.DEFAULT_COLOUR if there is none.
   * 
   * @return the colour code at the start of the line, or
   *         ChatClassifier.DEFAULT_COLOUR if there is none.
   */
  public char getStartColour()
  {
    if (getFormatted().length() >= 2
        && getFormatted().charAt(0) == ChatClassifier.COLOUR_CHAR)
    {
      return getFormatted().charAt(1);
    }
    else
    {
      return ChatClassifier.DEFAULT_COLOUR;
    }
  } // getStartColour

  // --------------------------------------------------------------------------
  /**
   * Return the original, formatted (with colour information) version of this
   * line.
   * 
   * @return the original, formatted (with colour information) version of this
   *         line.
   */
  public String getFormatted()
  {
    return _formatted;
  }

  // --------------------------------------------------------------------------
  /**
   * Return this line with all of the colour formatting removed.
   * 
   * @return this line with all of the colour formatting removed.
   */
  public String getUnformatted()
  {
    return _unformatted;
  }

  // --------------------------------------------------------------------------
  /**
   * Return this ChatLine as a String for debugging.
   * 
   * @return this ChatLine as a String for debugging.
   */
  public String toString()
  {
    String tag = (getCategory() != null) ? getCategory().getTag() : "?";
    return tag + ": " + getFormatted();
  }

  // --------------------------------------------------------------------------
  /**
   * The ChatCategory that matches this line.
   */
  private ChatCategory _category;

  /**
   * The chat text with its original colour formatting intact.
   */
  private String       _formatted;

  /**
   * The chat text with its colour formatting removed.
   */
  private String       _unformatted;
} // class ChatLine
