package watson.cli;

// ----------------------------------------------------------------------------
/**
 * Describes a formal parameter to a Command, including its name, type and help
 * text.
 * 
 * TODO: either do something useful with the help text (e.g. flag to trigger
 * in-depth explanation of a command's parameters) or remove it.
 */
public class Parameter
{
  /**
   * Constructor.
   * 
   * @param name the name of this Parameter.
   * @param mandatory true if the Parameter must be specified.
   * @param type the {@link ParameterType} of this Parameter, defining it's
   *          value type and how it is parsed.
   */
  public Parameter(String name, boolean mandatory, ParameterType type)
  {
    this(name, mandatory, type, "");
  }

  // --------------------------------------------------------------------------
  /**
   * Constructor.
   * 
   * @param name the name of this Parameter.
   * @param mandatory true if the Parameter must be specified.
   * @param type the {@link ParameterType} of this Parameter, defining it's
   *          value type and how it is parsed.
   * @param help the help text of this Parameter.
   */
  public Parameter(String name, boolean mandatory, ParameterType type,
                   String help)
  {
    _name = name;
    _mandatory = mandatory;
    _type = type;
    _help = help;
  }

  // --------------------------------------------------------------------------
  /**
   * Return the name of this Parameter.
   * 
   * @rerturn the name of this Parameter.
   */
  public String getName()
  {
    return _name;
  }

  // --------------------------------------------------------------------------
  /**
   * Return true if this parameter is mandatory (false if optional).
   * 
   * @return true if this parameter is mandatory (false if optional).
   */
  public boolean isMandatory()
  {
    return _mandatory;
  }

  // --------------------------------------------------------------------------
  /**
   * Return true if this parameter is optional.
   * 
   * @return true if this parameter is optional.
   */
  public boolean isOptional()
  {
    return !isMandatory();
  }

  // --------------------------------------------------------------------------
  /**
   * Return the type of this Parameter.
   * 
   * @return the type of this Parameter.
   */
  public ParameterType getType()
  {
    return _type;
  }

  // --------------------------------------------------------------------------
  /**
   * Return the help text for this Parameter.
   * 
   * @return the help text for this Parameter.
   */
  public String getHelp()
  {
    return _help;
  }

  // --------------------------------------------------------------------------

  private String        _name;

  private boolean       _mandatory;

  /**
   * The type of this Parameter.
   */
  private ParameterType _type;

  /**
   * The help text for this Parameter.
   */
  private String        _help;
} // class Parameter