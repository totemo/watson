package watson.cli;

import java.util.LinkedHashMap;

// ----------------------------------------------------------------------------
/**
 * An {@link ICommandAction} implementation that simply dumps command parameters
 * to stdout.
 * 
 * This is useful when interactively testing command parsing and processing in
 * {@link watson.cli.CLI}.
 */
public class DebugCommandAction implements ICommandAction
{
  // --------------------------------------------------------------------------
  /**
   * Implements inherited method.
   */
  @Override
  public String perform(Command command, LinkedHashMap<String, Object> values)
  {
    for (String name : values.keySet())
    {
      System.out.println(name + ": " + values.get(name));
    }
    return "";
  }
} // class DebugCommandAction