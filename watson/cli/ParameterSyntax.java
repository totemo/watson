package watson.cli;

import java.util.LinkedHashMap;
import java.util.List;

// --------------------------------------------------------------------------
/**
 * Abstract base of classes that parse parameter lists.
 */
public abstract class ParameterSyntax
{
  // --------------------------------------------------------------------------
  /**
   * Parse the list of arguments and return a map of their values.
   * 
   * @param args the command line parameters.
   * @return a map from each parameter's name to its corresponding value.
   */
  public abstract LinkedHashMap<String, Object> parse(List<String> args)
    throws CLIParseException;

  // --------------------------------------------------------------------------
  /**
   * Add the specified formal parameter to this command.
   * 
   * @param parameter the Parameter.
   */
  public abstract void addParameter(Parameter parameter);

  // --------------------------------------------------------------------------
  /**
   * Return the help message describing the overall parameter syntax.
   * 
   * @return the help message describing the overall parameter syntax.
   */
  public abstract String getSyntaxHelp();
} // class ParameterSyntax