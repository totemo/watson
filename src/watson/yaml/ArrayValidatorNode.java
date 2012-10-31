package watson.yaml;

import java.util.ArrayList;

// ----------------------------------------------------------------------------
/**
 * A {@link ValidatorNode} that expects a DOM node to be an ArrayList<>.
 * 
 * If setChild() is called to set an element type, all array elements will be
 * validated against that type.
 */
public class ArrayValidatorNode extends TypeValidatorNode
{
  // --------------------------------------------------------------------------
  /**
   * Default constructor.
   * 
   * Validates nodes that are ArrayList<>s.
   */
  public ArrayValidatorNode()
  {
    this(null, false, 0, Integer.MAX_VALUE);
  }

  // --------------------------------------------------------------------------
  /**
   * Constructor.
   * 
   * Validates nodes that are ArrayList<>s and verifies that all elements are of
   * the specified type.
   * 
   * @param child the validator for array elements.
   * @param optional if true, this node is optional.
   * @param minElements the minimum allowed number of array elements.
   * @param maxElements the maximum allowed number of array elements.
   */
  public ArrayValidatorNode(ValidatorNode child, boolean optional)
  {
    this(child, optional, 0, Integer.MAX_VALUE);
  }

  // --------------------------------------------------------------------------
  /**
   * Constructor.
   * 
   * Validates nodes that are ArrayList<>s and verifies that all elements are of
   * the specified type.
   * 
   * @param child the validator for array elements.
   * @param optional if true, this node is optional.
   * @param minElements the minimum allowed number of array elements.
   * @param maxElements the maximum allowed number of array elements.
   */
  public ArrayValidatorNode(ValidatorNode child, boolean optional,
                            int minElements, int maxElements)
  {
    super(ArrayList.class);
    setChild(child);
    setOptional(optional);
    setMinElements(minElements);
    setMaxElements(maxElements);
  }

  // --------------------------------------------------------------------------
  /**
   * Constructor.
   * 
   * Validates nodes that are ArrayList<>s and verifies that all elements are of
   * the specified type.
   * 
   * @param child the validator for array elements.
   * @param optional if true, this node is optional.
   * @param minElements the minimum allowed number of array elements.
   * @param maxElements the maximum allowed number of array elements.
   * @param defaultValue the default value.
   */
  public ArrayValidatorNode(ValidatorNode child, boolean optional,
                            int minElements, int maxElements,
                            Object defaultValue)
  {
    super(ArrayList.class);
    setChild(child);
    setOptional(optional);
    setMinElements(minElements);
    setMaxElements(maxElements);
    setDefaultValue(defaultValue);
  }

  // --------------------------------------------------------------------------
  /**
   * Set the validator for array elements.
   * 
   * @param child the validator.
   */

  public void setChild(ValidatorNode child)
  {
    _child = child;
  }

  // --------------------------------------------------------------------------
  /**
   * Return the validator for array elements.
   * 
   * @return the validator for array elements.
   */
  public ValidatorNode getChild()
  {
    return _child;
  }

  // --------------------------------------------------------------------------
  /**
   * Set the minimum allowable number of array elements.
   * 
   * @param minElements that number.
   */
  public void setMinElements(int minElements)
  {
    _minElements = minElements;
  }

  // --------------------------------------------------------------------------
  /**
   * Return the minimum allowable number of array elements.
   * 
   * @return the minimum allowable number of array elements.
   */
  public int getMinElements()
  {
    return _minElements;
  }

  // --------------------------------------------------------------------------
  /**
   * Set the maximum allowable number of array elements.
   * 
   * @param maxElements that number.
   */
  public void setMaxElements(int maxElements)
  {
    _maxElements = maxElements;
  }

  // --------------------------------------------------------------------------
  /**
   * Return the maximum allowable number of array elements.
   * 
   * @return the maximum allowable number of array elements.
   */
  public int getMaxElements()
  {
    return _maxElements;
  }

  // --------------------------------------------------------------------------
  /**
   * @see watson.yaml.ValidatorNode#validate(java.lang.Object, java.lang.String,
   *      watson.yaml.ValidatorMessageSink)
   */
  @Override
  public boolean validate(Object node, String path, ValidatorMessageSink sink)
  {
    // Optionality is dealt with here.
    if (!super.validate(node, path, sink))
    {
      return false;
    }

    if (getChild() != null && node != null)
    {
      @SuppressWarnings("unchecked")
      ArrayList<Object> array = (ArrayList<Object>) node;
      if (array.size() < getMinElements() || array.size() > getMaxElements())
      {
        StringBuilder text = new StringBuilder();
        text.append("array ");
        text.append(path);
        text.append(" should have ");
        if (getMinElements() == getMaxElements())
        {
          text.append("exactly ");
          text.append(getMinElements());
        }
        else
        {
          text.append("between ");
          text.append(getMinElements());
          text.append(" and ");
          text.append(getMaxElements());
        }
        text.append(" elements, rather than ");
        text.append(array.size());

        sink.message(text.toString());

        // Let the parent try to patch the DOM.
        return false;
      }
      else
      {
        boolean valid = true;
        for (int i = 0; i < array.size(); ++i)
        {
          Object element = array.get(i);
          boolean childValid = getChild().validate(element, path + "/" + i,
            sink);

          // Valid if the child is valid or has a default to patch things up.
          valid &= (childValid || getChild().hasDefaultValue());
          if (!childValid && getChild().hasDefaultValue())
          {
            // Replace that element with the default value.
            array.set(i, getChild().getDefaultValue());
          }
        } // for
        return valid;
      }
    }
    return true;
  } // validate

  // --------------------------------------------------------------------------
  /**
   * The validator of array elements.
   */
  protected ValidatorNode _child;

  /**
   * The minimum allowed number of array elements.
   */
  protected int           _minElements;

  /**
   * The maximum allowed number of array elements.
   */
  protected int           _maxElements;
} // class ArrayValidatorNode