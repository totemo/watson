package watson.cli;

import java.util.LinkedHashMap;

// ----------------------------------------------------------------------------
/**
 * A callback interface by which successfully parsed {@link Command}s are
 * performed.
 */
public interface ICommandAction
{
  // --------------------------------------------------------------------------
  /**
   * Called when a Command is successfully parsed in a command line.
   * 
   * @param command the actual Command instance that was recognised.
   * @param values a map of named values of parameters from the command line;
   *          the iteration order is the order that they were parsed, from left
   *          to right
   * @return an error message, or null if successful.
   */
  public String perform(Command command, LinkedHashMap<String, Object> values);
} // class ICommandAction
