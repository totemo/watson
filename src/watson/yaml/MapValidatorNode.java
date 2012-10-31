package watson.yaml;

import java.util.LinkedHashMap;

// ----------------------------------------------------------------------------
/**
 * A {@link ValidatorNode} that verifies that a node is a LinkedHashMap with the
 * expected children.
 * 
 * Children are validated in the order that they were added by
 * {@link MapValidatorNode#addChild(String, ValidatorNode)}.
 */
public class MapValidatorNode extends TypeValidatorNode
{
  // --------------------------------------------------------------------------
  /**
   * Default constructor.
   */
  public MapValidatorNode()
  {
    super(LinkedHashMap.class);
  }

  // --------------------------------------------------------------------------
  /**
   * Add an expected child node.
   * 
   * @param name the name of the child in the map.
   * @param child the ValidatorNode that validates the child.
   */
  public void addChild(String name, ValidatorNode child)
  {
    _children.put(name, child);
  }

  // --------------------------------------------------------------------------
  /**
   * @see watson.yaml.ValidatorNode#validate(java.lang.Object,
   *      watson.yaml.ValidatorMessageSink)
   */
  @Override
  public boolean validate(Object node, String path, ValidatorMessageSink sink)
  {
    // Check that node is a LinkedHashMap.
    if (!super.validate(node, path, sink))
    {
      return false;
    }

    boolean valid = true;
    @SuppressWarnings("unchecked")
    LinkedHashMap<String, Object> map = (LinkedHashMap<String, Object>) node;
    for (String name : _children.keySet())
    {
      ValidatorNode childValidator = _children.get(name);
      Object child = map.get(name);
      boolean childValid = childValidator.validate(child, path + '/' + name,
        sink);
      if ((!childValid || child == null) && childValidator.hasDefaultValue())
      {
        map.put(name, childValidator.getDefaultValue());
      }

      // Valid if the child is valid or has a default to patch things up.
      valid &= (childValid || childValidator.hasDefaultValue());
    } // for

    return valid;
  } // validate
  // --------------------------------------------------------------------------
  /**
   * Expected child nodes.
   */
  protected LinkedHashMap<String, ValidatorNode> _children = new LinkedHashMap<String, ValidatorNode>();
} // class MapValidatorNode
