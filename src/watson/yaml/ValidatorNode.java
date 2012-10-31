package watson.yaml;

// ----------------------------------------------------------------------------
/**
 * Abstract base of classes that validate SnakeYAML DOM structures.
 */
public abstract class ValidatorNode
{
  // --------------------------------------------------------------------------
  /**
   * Specifies whether the corresponding structure has to appear in the DOM
   * tree.
   * 
   * @param optional true if the DOM structure is optional.
   */
  public void setOptional(boolean optional)
  {
    _optional = optional;
  }

  // --------------------------------------------------------------------------
  /**
   * Return true if the corresponding structure does not have to appear in the
   * DOM tree.
   * 
   * @return true if the corresponding structure does not have to appear in the
   *         DOM tree.
   */
  public boolean isOptional()
  {
    return _optional;
  }

  // --------------------------------------------------------------------------
  /**
   * Set the default value.
   * 
   * The default value is used to fix the DOM when an invalid value is loaded or
   * when an optional value is missing.
   * 
   * @param defaultValue the default.
   */
  public void setDefaultValue(Object defaultValue)
  {
    _defaultValue = defaultValue;
    _hasDefaultValue = true;
  }

  // --------------------------------------------------------------------------
  /**
   * Clear the default value, so that the value returned by getDefaultValue()
   * will not be used.
   */
  public void clearDefaultValue()
  {
    _hasDefaultValue = false;
  }

  // --------------------------------------------------------------------------
  /**
   * Return the default value of this node.
   * 
   * @return the default value of this node.
   * @see #setDefaultValue(Object)
   */
  public Object getDefaultValue()
  {
    return _defaultValue;
  }

  // --------------------------------------------------------------------------
  /**
   * Return true if a default value has been set.
   * 
   * Note that the default value can be null, so testing it against null is not
   * sufficient to determine whether there is a default.
   * 
   * @return true if a default value has been set.
   */
  public boolean hasDefaultValue()
  {
    return _hasDefaultValue;
  }

  // --------------------------------------------------------------------------
  /**
   * Check the specified node of the DOM against expectations.
   * 
   * All ValidatorNode implementations must verify that the node is non-null if
   * not optional.
   * 
   * @param node the node to validate.
   * @param path the path to the node in a name1.name2.name3... format.
   * @param sink used for error reporting.
   * @return true if the node is valid.
   */
  public abstract boolean validate(Object node, String path,
                                   ValidatorMessageSink sink);

  // --------------------------------------------------------------------------
  /**
   * True if the corresponding structure does not have to appear in the DOM
   * tree.
   */
  protected boolean _optional;

  /**
   * If true, there is a default value.
   */
  protected boolean _hasDefaultValue;

  /**
   * The default value. Only valid if _hasDefaultValue is true.
   */
  protected Object  _defaultValue;
} // class ValidatorNode