package watson.chat;

import java.util.regex.Pattern;

// --------------------------------------------------------------------------
/**
 * Represents one of the categories into which individual lines of chat,
 * <i>prior to being split by the server</i>, can be assigned.
 * 
 * Two consecutive lines of chat will be rejoined and assigned the same category
 * if it is clear that they were split by the server.
 */
public class ChatCategory
{
  // --------------------------------------------------------------------------
  /**
   * Constructor.
   * 
   * @param id the unique ID under which this ChatCategory will be indexed by
   *          {@link ChatClassifier}. See {@link #getId()} for the distinction
   *          between the tag and the ID.
   * @param tag the tag with which all lines in this category will be marked.
   * @param initialRegex the regular expression describing the part of the line
   *          before where the server inserted a line break.
   * @param fullRegex the regular expression describing the whole line when the
   *          first and second parts of it are rejoined.
   * @param extensible if true, the line can be extended by concatenation more
   *          than just a single time; if false, it can only be extended once.
   */
  public ChatCategory(String id, String tag, String initialRegex,
                      String fullRegex, boolean extensible)
  {
    _id = id;
    _tag = tag;

    if (initialRegex == null)
    {
      initialRegex = fullRegex;
    }
    _initialPattern = Pattern.compile(initialRegex);
    _fullPattern = Pattern.compile(fullRegex);
    _extensible = extensible;
    _pedantic = (!initialRegex.equals(fullRegex));
  }

  // --------------------------------------------------------------------------
  /**
   * Return the unique ID under which this ChatCategory will be indexed by
   * {@link ChatClassifier}.
   * 
   * If unspecified in chatcategories.yml, the corresponding ChatCategory will
   * not be indexed.
   * 
   * Note that the tag of a ChatCategory is not guaranteed to be unique, and
   * there may be multiple ChatCategory instances with the same tag by different
   * regular expressions, e.g. "server.obituary".
   * 
   * The purpose of the tag is to refer to a group of lines matching multiple
   * regular expressions as one unit, and filter them from chat if desired. By
   * contrast, the ID property refers to exactly one regular expression for chat
   * lines.
   * 
   * @return the unique ID under which this ChatCategory will be indexed by
   *         {@link ChatClassifier}.
   */
  public String getId()
  {
    return _id;
  }

  // --------------------------------------------------------------------------
  /**
   * Return the regular expression that matches a full (rejoined) chat line in
   * this category.
   * 
   * This is useful for getting a java.util.regex.Matcher to extract matched
   * groups from a chat line.
   * 
   * @return the regular expression that matches a full (rejoined) chat line in
   *         this category.
   */
  public Pattern getFullPattern()
  {
    return _fullPattern;
  }

  // --------------------------------------------------------------------------
  /**
   * Return true if the line can be extended by concatenation more than just a
   * single time; or false if it can only be extended once.
   * 
   * @return true if the line can be extended by concatenation more than just a
   *         single time; or false if it can only be extended once.
   */
  public boolean isExtensible()
  {
    return _extensible;
  }

  // --------------------------------------------------------------------------
  /**
   * Return true if the initial regular expression is sufficient to identify the
   * start of a split line, but does not fully describe the contents of the
   * unsplit line.
   * 
   * @return true if the initial regular expression is sufficient to identify
   *         the start of a split line, but does not fully describe the contents
   *         of the unsplit line.
   */
  public boolean isPedantic()
  {
    return _pedantic;
  }

  // --------------------------------------------------------------------------
  /**
   * Return true if the specified Line matches the pattern for the start of a
   * chat line prior to being split by the server.
   * 
   * @param firstPart the chat line prior to the split.
   * @return true if this matches the initialRegex parameter to the constructor.
   */
  public boolean matchesStart(ChatLine firstPart)
  {
    return matchesStart(firstPart, null);
  }

  // --------------------------------------------------------------------------
  /**
   * Return true if the concatenation of firstPart and secondPart matches the
   * regular expression for the start of a chat line prior to being split by the
   * server.
   * 
   * This is a special case matching behaviour for very long lines, like the
   * list of members in a region, which get split by the server many times and
   * therefore require multiple concatentations.
   * 
   * @param firstPart the start of the line.
   * @param secondPart the subsequent line, after the line break; this parameter
   *          can also be null, in which case it is treated as the empty string.
   * @return true if the concatenated line matches the pattern for the start of
   *         a line prior to the split - the initialRegex constructor parameter.
   */
  public boolean matchesStart(ChatLine firstPart, ChatLine secondPart)
  {
    String unformatted = firstPart.getUnformatted()
                         + ((secondPart != null) ? secondPart.getUnformatted()
                           : "");
    return _initialPattern.matcher(unformatted).matches();
  }

  // --------------------------------------------------------------------------
  /**
   * Return true if the concatenation of firstPart and secondPart matches the
   * regular expression for a complete (reassembled) line.
   * 
   * @param firstPart the start of the line.
   * @param secondPart the subsequent line, after the line break; this parameter
   *          can also be null, in which case, firstPart will be tested to see
   *          if it matches the pattern for a full line.
   * @return true if the concatenated line matches the pattern for a full
   *         (unsplit) line - the fullRegex parameter to the constructor.
   */
  public boolean matchesFully(ChatLine firstPart, ChatLine secondPart)
  {
    String unformatted = firstPart.getUnformatted()
                         + ((secondPart != null) ? secondPart.getUnformatted()
                           : "");
    return _fullPattern.matcher(unformatted).matches();
  }

  // --------------------------------------------------------------------------
  /**
   * Return the tag with which all lines in this category will be marked.
   * 
   * @return the tag with which all lines in this category will be marked.
   */
  public String getTag()
  {
    return _tag;
  }

  // --------------------------------------------------------------------------
  /**
   * Return a String representation for debugging.
   * 
   * @return a String representation for debugging.
   */
  public String toString()
  {
    return '{' + _tag + ", " + _initialPattern.pattern() + ", "
           + _fullPattern.pattern() + ", " + _extensible + '}';
  }

  // --------------------------------------------------------------------------
  /**
   * The unique ID of this chat category. If unspecified in chatcategories.yml,
   * the ID will be null and this instance will not be indexed by
   * {@link ChatClassifier}. (I want to issue a warning for duplicate IDs, which
   * I can't do if the ID automatically defaults to the tag, because it would
   * result in spam.)
   */
  private String  _id;

  /**
   * The tag with which all lines in this category will be marked.
   */
  private String  _tag;

  /**
   * The compiled {@link java.util.regex.Pattern} that matches the part of the
   * line before the line break.
   */
  private Pattern _initialPattern;

  /**
   * The compiled {@link java.util.regex.Pattern} that matches the whole line
   * prior to it being split by the server.
   */
  private Pattern _fullPattern;

  /**
   * True if more than two lines can be joined together for this category. If
   * false, at most two lines can be concatenated.
   * 
   * Long lists, like the members of a region are extensible.
   */
  private boolean _extensible;

  /**
   * True if the initial pattern uniquely identifies the category but does not
   * fully describe the unsplit line. The flag is true if the initial and full
   * regexps differ.
   */
  private boolean _pedantic;
} // class ChatCategory