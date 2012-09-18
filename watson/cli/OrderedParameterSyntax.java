package watson.cli;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

// ----------------------------------------------------------------------------
/**
 * A {@link ParameterSyntax} that parses parameters whose interpretation depends
 * on the order in which they are listed, i.e. positional parameters.
 */
public class OrderedParameterSyntax extends ParameterSyntax
{
  /**
   * Add a {@link Parameter}.
   * 
   * @param parameter the Parameter.
   */
  public void addParameter(Parameter parameter)
  {
    _parameters.add(parameter);
  }

  // --------------------------------------------------------------------------
  /**
   * Return a help message describing all of the parameters.
   * 
   * @return a help message describing all of the parameters.
   */
  public String getSyntaxHelp()
  {
    StringBuilder b = new StringBuilder();
    for (Parameter p : _parameters)
    {
      b.append(' ');
      if (p.isOptional())
      {
        b.append('[');
      }
      b.append(p.getType().getRangeHelp());
      if (p.isOptional())
      {
        b.append(']');
      }
    }
    return b.toString();
  } // getSyntaxHelp

  // --------------------------------------------------------------------------
  /**
   * Parse the parameter list.
   * 
   * TODO: currently this code does not deal with optional parameters.
   * Fixitfixitfixit.
   */
  @Override
  public LinkedHashMap<String, Object> parse(List<String> args)
    throws CLIParseException
  {
    LinkedHashMap<String, Object> values = new LinkedHashMap<String, Object>();

    if (_parameters.size() == 0 && args.size() > 0)
    {
      // Almost the same code also appears in class ParameterType, implying that
      // the validation of the number of arguments to be validated by a
      // Parameter needs to be performed more centrally (this class?).
      // TODO: Perhaps ParameterType should declare how many args it expects?
      StringBuilder message = new StringBuilder("unexpected arguments:");
      for (int i = 0; i < args.size(); ++i)
      {
        message.append(' ');
        message.append(args.get(i));
      }
      throw new CLIParseException(message.toString());
    }
    else
    {
      // Map a single arg to each parameter. If there are more arguments in the
      // list than Parameter instances, pass the excess args to the last
      // Parameter instance.
      for (int i = 0; i < _parameters.size(); ++i)
      {
        Parameter parameter = _parameters.get(i);
        int tail = (i < _parameters.size() - 1) ? i + 1 : args.size();

        // Prevent exceptions from being thrown when the number of parameters,
        // and hence i & tail, exceeds the number of arguments.
        List<String> parameterArgs = args.subList(Math.min(i, args.size()),
          Math.min(tail, args.size()));
        values.put(parameter.getName(),
          parameter.getType().parse(parameterArgs));
      }
    }
    return values;
  } // parse

  // --------------------------------------------------------------------------
  /**
   * The formal parameters to this command.
   */
  ArrayList<Parameter> _parameters = new ArrayList<Parameter>();
} // class OrderedParameterSyntax