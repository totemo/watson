package watson.yaml;

// ----------------------------------------------------------------------------
/**
 * A {@link ValidatorNode} that checks that a given DOM node is an instance of a
 * specified type.
 */
public class TypeValidatorNode extends ValidatorNode
{
  // --------------------------------------------------------------------------
  /**
   * Constructor.
   * 
   * @param type the expected type of the DOM node.
   */
  public TypeValidatorNode(Class<?> type)
  {
    this(type, false, null);
  }

  // --------------------------------------------------------------------------
  /**
   * Constructor.
   * 
   * @param type the expected type of the DOM node.
   * @param optional if true, the node is optional
   * @param defaultValue the default value.
   */
  public TypeValidatorNode(Class<?> type, boolean optional)
  {
    _type = type;
    setOptional(optional);
  }

  // --------------------------------------------------------------------------
  /**
   * Constructor.
   * 
   * @param type the expected type of the DOM node.
   * @param optional if true, the node is optional
   */
  public <T> TypeValidatorNode(Class<T> type, boolean optional, T defaultValue)
  {
    _type = type;
    setOptional(optional);
    setDefaultValue(defaultValue);
  }

  // --------------------------------------------------------------------------
  /**
   * Validate the node as either present and the expected type, or missing and
   * optional.
   */
  public boolean validate(Object node, String path, ValidatorMessageSink sink)
  {
    if (node == null)
    {
      boolean valid = isOptional();
      if (!valid)
      {
        sink.message("missing value for mandatory field " + path);
      }
      return valid;
    }
    else
    {
      boolean valid = _type.isInstance(node);
      if (!valid)
      {
        sink.message("for " + path + " expected " + _type.getName()
                     + " but got " + node.getClass().getName() + " (" + node
                     + ")");
      }
      return valid;
    }
  } // validate

  // --------------------------------------------------------------------------
  /**
   * The expected type of the DOM node.
   */
  protected Class<?> _type;
} // class TypeValidatorNode