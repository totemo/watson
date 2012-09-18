package watson.cli;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

// --------------------------------------------------------------------------
/**
 * Describes parameters with a limited set of predefined string values.
 */
public class EnumerationParameterType extends ParameterType
{
  // --------------------------------------------------------------------------
  /**
   * Constructor.
   * 
   * @param options the list of all possible String values.
   */
  public EnumerationParameterType(String... options)
  {
    this(String.class, options);
  }

  // --------------------------------------------------------------------------
  /**
   * Constructor for subclassing.
   * 
   * This constructor is intended for use in subclasses that want to post-
   * process the result returned by parse() and convert it from a string to the
   * specifed type.
   * 
   * @param type the type that parse() will convert parsed option values to.
   * @param options the list of all possible String values.
   */
  protected EnumerationParameterType(Class<?> type, String... options)
  {
    super(type);
    _options.addAll(Arrays.asList(options));
  }

  // --------------------------------------------------------------------------
  /**
   * Implements inherited method.
   * 
   * Lists the range of values in the form "option1|option2|...|optionN".
   */
  @Override
  public String getRangeHelp()
  {
    // Count of number of options listed so far.
    int count = 0;
    StringBuilder result = new StringBuilder();
    for (String option : _options)
    {
      result.append(option);
      ++count;
      if (count < _options.size())
      {
        result.append('|');
      }
    }
    return result.toString();
  } // getRange

  // --------------------------------------------------------------------------
  /**
   * Implements inherited method.
   * 
   * Returns args[0] (String) if it is a valid option.
   * 
   * @throws CLIParseException if args[0] is not one of the valid options, or if
   *           there is more than one argument.
   */
  @Override
  public Object parse(List<String> args)
    throws CLIParseException
  {
    checkArgumentListSize(args, 1);

    if (_options.contains(args.get(0)))
    {
      return args.get(0);
    }

    throw new CLIParseException("invalid argument: " + args.get(0));
  } // parse

  // --------------------------------------------------------------------------
  /**
   * The set of all possible valid values.
   */
  private Set<String> _options = new LinkedHashSet<String>();

} // class EnumerationParameterType
