package watson.cli;

import watson.Controller;

// --------------------------------------------------------------------------
/**
 * An {@link IMessageSink} implementation that displays a message via the
 * client's chat GUI.
 */
public class LocalChatMessageSink implements IMessageSink
{
  // --------------------------------------------------------------------------
  /**
   * Implements inherited method.
   */
  @Override
  public void process(String message)
  {
    Controller.instance.localChat(message);
  }
} // class LocalChatMessageSink