package watson.cli;

import java.util.List;

// ----------------------------------------------------------------------------
/**
 * Describes parameter values that can be arbitrary strings.
 */
public class StringParameterType extends ParameterType
{
  /**
   * Constructor.
   * 
   * @param range the help text descripton of the range of valid values.
   */
  public StringParameterType(String range)
  {
    super(String.class);
    _range = range;
  }

  // --------------------------------------------------------------------------
  /**
   * Implements inherited method.
   */
  @Override
  public String getRangeHelp()
  {
    return _range;
  }

  // --------------------------------------------------------------------------
  /**
   * Implements inherited method.
   */
  @Override
  public Object parse(List<String> args)
    throws CLIParseException
  {
    checkArgumentListSize(args, 1);
    return args.get(0);
  }

  // --------------------------------------------------------------------------
  /**
   * The help text descripton of the range of valid values.
   */
  private String _range;
} // class StringParameterType