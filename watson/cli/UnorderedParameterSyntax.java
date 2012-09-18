package watson.cli;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// ----------------------------------------------------------------------------
/**
 * A {@link ParameterSyntax} implementation that that parses parameters in any
 * order.
 * 
 * Each parameter is preceded by a unique name that allows the parser to
 * determine the number of and types of the parameters. The parser is intended
 * to be able to deal with LogBlock parameter syntax.
 */
public class UnorderedParameterSyntax extends ParameterSyntax
{
  // --------------------------------------------------------------------------
  /**
   * Constructor.
   * 
   * @param syntaxHelp the help message summarising the parameters.
   */
  public UnorderedParameterSyntax(String syntaxHelp)
  {
    _syntaxHelp = syntaxHelp;
  }

  // --------------------------------------------------------------------------
  /**
   * Parse the list of arguments and return a map of their values.
   * 
   * @param args the command line parameters.
   * @return a map from each parameter's name to its corresponding value.
   */
  @Override
  public LinkedHashMap<String, Object> parse(List<String> args)
    throws CLIParseException
  {
    LinkedHashMap<String, Object> values = new LinkedHashMap<String, Object>();

    int head = 0;
    int tail = 1;
    while (head < args.size() && tail <= args.size())
    {
      // We expect the next parameter identifier to be at the start of the args
      // list.
      if (_parameters.containsKey(args.get(head)))
      {
        // Collect parameters up to the next parameter identifier, or the end
        // of the args list.
        while (tail < args.size() && !_parameters.containsKey(args.get(tail)))
        {
          ++tail;
        } // while searching for the end of this parameter

        Parameter param = _parameters.get(args.get(head));
        param.getType().parse(args.subList(head, tail));
      }

    } // while searching for the start of the parameter
    return values;
  } // parse

  // --------------------------------------------------------------------------
  /**
   * Implements inherited method.
   */
  @Override
  public void addParameter(Parameter parameter)
  {
    _parameters.put(parameter.getName(), parameter);
  }

  // --------------------------------------------------------------------------
  /**
   * Return the help message describing the overall parameter syntax.
   * 
   * @return the help message describing the overall parameter syntax.
   */
  @Override
  public String getSyntaxHelp()
  {
    return _syntaxHelp;
  }

  // --------------------------------------------------------------------------
  /**
   * The help message describing the overall parameter syntax.
   */
  protected String                 _syntaxHelp;

  /**
   * A map from {@link Parameter#getName()} to corresponding {@link Parameter}
   * instance.
   */
  protected Map<String, Parameter> _parameters = new LinkedHashMap<String, Parameter>();
} // class UnorderedParameterSyntax