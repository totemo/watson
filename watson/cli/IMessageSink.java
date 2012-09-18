package watson.cli;

// --------------------------------------------------------------------------
/**
 * Acts as a sink for messages to be displayed.
 * 
 * The main purpose of this interface is to decouple CommandManager from the
 * rest of the Watson code base so that I can test it (or {@link watson.cli.CLI}
 * ) without having to have essentially all Minecraft classes loaded.
 */
public interface IMessageSink
{
  /**
   * Do something with a message.
   * 
   * @param message the message.
   */
  public void process(String message);
} // class IMessageSink