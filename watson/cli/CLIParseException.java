package watson.cli;

// --------------------------------------------------------------------------
/**
 * Represents an error parsing a command line.
 */
@SuppressWarnings("serial")
public class CLIParseException extends Exception
{
  /**
   * Constructor.
   * 
   * @param message the message text.
   */
  public CLIParseException(String message)
  {
    super(message);
  }
} // class CLIParseException