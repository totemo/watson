package watson.cli;

// --------------------------------------------------------------------------
/**
 * An {@link IMessageSink} implementation that simply prints the message to
 * stdout.
 */
public class StdoutMessageSink implements IMessageSink
{
  // --------------------------------------------------------------------------
  /**
   * Print the message.
   */
  @Override
  public void process(String message)
  {
    System.out.println(message);
  }
} // class StdoutMessageSink