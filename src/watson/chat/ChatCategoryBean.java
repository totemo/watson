package watson.chat;

// ----------------------------------------------------------------------------
/**
 * The Java Bean representation of a ChatCategory, as loaded from
 * chatcategories.yml.
 * 
 * This is just a little boilerplate to finesse SnakeYAML into loading
 * chatcategories.yml in a tidy fashion.
 * 
 * @see ChatCategoryListBean
 */
public class ChatCategoryBean
{
  // --------------------------------------------------------------------------
  /**
   * Set the ID that uniquely identifies the corresponding {@link ChatCategory}
   * instance.
   * 
   * If unspecified in chatcategories.yml, the corresponding
   * {@link ChatCategory} will not be indexed.
   * 
   * @param id the unique ID.
   */
  public void setId(String id)
  {
    _id = id;
  }

  // --------------------------------------------------------------------------
  /**
   * Return the ID that uniquely identifies the corresponding
   * {@link ChatCategory} instance.
   * 
   * @return the ID that uniquely identifies the corresponding
   *         {@link ChatCategory} instance.
   */
  public String getId()
  {
    return _id;
  }

  // --------------------------------------------------------------------------
  /**
   * Set the tag identifying this chat category.
   * 
   * 
   * @param tag the tag.
   */
  public void setTag(String tag)
  {
    _tag = tag;
  }

  // --------------------------------------------------------------------------
  /**
   * Return the tag identifying this chat category.
   * 
   * @return the tag identifying this chat category.
   */
  public String getTag()
  {
    return _tag;
  }

  // --------------------------------------------------------------------------
  /**
   * Set the regular expression that matches the start of a line of chat
   * belonging to this category (up to where it is split by the server).
   * 
   * @param initialRegex the regular expression.
   */
  public void setInitialRegex(String initialRegex)
  {
    _initialRegex = initialRegex;
  }

  // --------------------------------------------------------------------------
  /**
   * Return the regular expression that matches the start of a line of chat
   * belonging to this category (up to where it is split by the server).
   * 
   * @return the regular expression that matches the start of a line of chat
   *         belonging to this category (up to where it is split by the server).
   */
  public String getInitialRegex()
  {
    return _initialRegex;
  }

  // --------------------------------------------------------------------------
  /**
   * Set the regular expression that matches a whole (unsplit) line of chat
   * belonging to this category.
   * 
   * @param fullRegex the regular expression.
   */
  public void setFullRegex(String fullRegex)
  {
    _fullRegex = fullRegex;
  }

  // --------------------------------------------------------------------------
  /**
   * Return the regular expression that matches a whole (unsplit) line of chat
   * belonging to this category.
   * 
   * @return the regular expression that matches a whole (unsplit) line of chat
   *         belonging to this category.
   */
  public String getFullRegex()
  {
    return _fullRegex;
  }

  // --------------------------------------------------------------------------
  /**
   * If true, the chat line can be extended by concatenation more than just a
   * single time; if false, it can only be extended once.
   * 
   * @param extensible
   */
  public void setExtensible(boolean extensible)
  {
    _extensible = extensible;
  }

  // --------------------------------------------------------------------------
  /**
   * Return true if the chat line can be extended by concatenation more than
   * once, or false if it can only be extended once.
   * 
   * @return
   */
  public boolean isExtensible()
  {
    return _extensible;
  }

  // --------------------------------------------------------------------------
  /**
   * Return a String representation for debugging.
   * 
   * @return a String representation for debugging.
   */
  public String toString()
  {
    return '{' + _tag + ", " + _initialRegex + ", " + _fullRegex + ", "
           + _extensible + '}';
  }

  // --------------------------------------------------------------------------
  /**
   * Construct a {@link ChatCategory} corresponding to the properties loaded
   * into this bean.
   * 
   * @return the corresponding {@link ChatCategory} instance.
   */
  public ChatCategory makeChatCategory()
  {
    return new ChatCategory(getId(), getTag(), getInitialRegex(),
      getFullRegex(), isExtensible());
  }

  // --------------------------------------------------------------------------
  /**
   * The unique ID of this chat category. If unspecified in chatcategories.yml,
   * the corresponding {@link ChatCategory} will not be indexed.
   */
  private String  _id;

  /**
   * The tag identifying this chat category. Note that the tag is not guaranteed
   * to be unique. There may be multiple {@link ChatCategory} instances with
   * different regular expressions using the same tag, e.g. "server.obituary".
   * If you want to look up a {@link ChatCategory} by name, use its _id.
   */
  private String  _tag;

  /**
   * The regular expression that matches the start of a line of chat belonging
   * to this category (up to where it is split by the server).
   */
  private String  _initialRegex;

  /**
   * The regular expression that matches a whole (unsplit) line of chat
   * belonging to this category.
   */
  private String  _fullRegex;

  /**
   * If true, the chat line can be extended by concatenation more than just a
   * single time; if false, it can only be extended once.
   */
  private boolean _extensible;
} // class ChatCategoryBean