package watson.cli;

import java.util.List;

// ----------------------------------------------------------------------------
/**
 * Describes the type information for a {@link Parameter} to a {@link Command}.
 */
public abstract class ParameterType
{
  /**
   * Constructor.
   * 
   * @param valueType the expected Java type of the parameter value.
   */
  public ParameterType(Class<?> valueType)
  {
    _valueType = valueType;
  }

  // --------------------------------------------------------------------------
  /**
   * Return the expected Java type of the parameter value.
   * 
   * @return the expected Java type of the parameter value.
   */
  public Class<?> getValueType()
  {
    return _valueType;
  }

  // --------------------------------------------------------------------------
  /**
   * Return a text description of the range of acceptable values for parameters
   * of this type, for the purpose of presentation in help text. e.g. "on|off".
   * 
   * @return a text description of the range of acceptable values for parameters
   *         of this type, for the purpose of presentation in help text. e.g.
   *         "on|off".
   */
  public abstract String getRangeHelp();

  // --------------------------------------------------------------------------
  /**
   * Parse the argument strings, args, as a Parameter value of this type.
   * 
   * @return an Object whose type is getValueType().
   * @throws CLIParseException if the arguments cannot be parsed as the expected
   *           types or if there are extra (unexpected) Strings in the args
   *           array.
   */
  public abstract Object parse(List<String> args)
    throws CLIParseException;

  // --------------------------------------------------------------------------
  /**
   * Checks that the argument list has the expected size and, if not,throws a
   * CLIParseException with a message that could be used in a user-facing error
   * message.
   * 
   * @param args the args passed to parse().
   * @param expectedSize the expected size() of args.
   * @throws CLIParseException if the size of the argument list does not match
   *           what is expected.
   */
  protected void checkArgumentListSize(List<String> args, int expectedSize)
    throws CLIParseException
  {
    if (args.size() < expectedSize)
    {
      throw new CLIParseException("missing argument");
    }
    else if (args.size() > expectedSize)
    {
      StringBuilder message = new StringBuilder("unexpected arguments:");
      for (int i = expectedSize; i < args.size(); ++i)
      {
        message.append(' ');
        message.append(args.get(i));
      }
      throw new CLIParseException(message.toString());
    }
  } // throwCLIParseException

  // --------------------------------------------------------------------------
  /**
   * The type of values that this parameter can have. Methods called by
   * {@link MethodCommandAction} must have a corresponding parameter of this
   * type.
   */
  private Class<?> _valueType;
} // class ParameterType