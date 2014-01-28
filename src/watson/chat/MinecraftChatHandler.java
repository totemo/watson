package watson.chat;

// ----------------------------------------------------------------------------
/**
 * An IChatHandler that passes chat lines back into the Minecraft code for its
 * normal vanilla handling.
 */
public class MinecraftChatHandler implements IChatHandler
{

  // --------------------------------------------------------------------------
  /**
   * Implements inherited method.
   */
  @Override
  public void classify(ChatLine line)
  {
    Chat.localChat(line.getFormatted());
  }

} // class MinecraftChatHandler

