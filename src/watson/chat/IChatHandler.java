package watson.chat;

import net.minecraft.util.IChatComponent;

// ----------------------------------------------------------------------------
/**
 * Event handler interface for chat messages received by the client and
 * processed by {@link ChatProcessor}.
 */
public interface IChatHandler
{
  // --------------------------------------------------------------------------
  /**
   * Called when a chat is received.
   * 
   * @param chat the chat.
   * @return true if the chat should be echoed in the client chat GUI; false if
   *         it should be filtered out.
   */
  public boolean onChat(IChatComponent chat);
}