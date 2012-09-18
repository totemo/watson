package watson.cli;

import java.util.List;

// --------------------------------------------------------------------------
/**
 * Describes the type of Parameters parsed as type Integer.
 */
public class IntegerParameterType extends ParameterType
{
  public IntegerParameterType()
  {
    super(Integer.class);
  }

  // --------------------------------------------------------------------------
  /**
   * Implements inherited method.
   */
  @Override
  public String getRangeHelp()
  {
    return "<integer>";
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
    return Integer.parseInt(args.get(0));
  }
} // class IntegerParameterType