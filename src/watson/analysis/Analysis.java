package watson.analysis;

import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.minecraft.util.IChatComponent;
import watson.chat.IChatHandler;
import watson.chat.IMatchedChatHandler;

// ----------------------------------------------------------------------------
/**
 * Common functionality for implementing {@link IChatHandler}s that scrape
 * useful information out of chat.
 */
public class Analysis implements IChatHandler
{
  // --------------------------------------------------------------------------
  /**
   * Check each of the registered Patterns for a match, in the order that they
   * were added and call the onMatchedChat() method of the first corresponding
   * IMatchedChatHandler.
   * 
   * Only the first matching Pattern results in a callback.
   * 
   * @param chat the chat to match.
   * @return the return value of the IMatchedChatHandler that was called, or
   *         true if none were called. The chat is added to the client's chat
   *         GUI if true was returned.
   */
  public boolean dispatchMatchedChat(IChatComponent chat)
  {
    String unformatted = chat.getUnformattedText();
    for (Entry<Pattern, IMatchedChatHandler> entry : _handlers.entrySet())
    {
      Matcher m = entry.getKey().matcher(unformatted);
      if (m.matches())
      {
        return entry.getValue().onMatchedChat(chat, m);
      }
    }
    return true;
  }

  // --------------------------------------------------------------------------
  /**
   * Specify that the handler's onMatchedChat() should be called for chat that
   * matches the specified pattern.
   * 
   * @param pattern the regexp that the unformatted chat must match.
   * @param handler the handler whose method is called.
   */
  public void addMatchedChatHandler(Pattern pattern, IMatchedChatHandler handler)
  {
    _handlers.put(pattern, handler);
  }

  // --------------------------------------------------------------------------
  /**
   * @see watson.chat.IChatHandler#onChat(net.minecraft.util.IChatComponent)
   */
  @Override
  public boolean onChat(IChatComponent chat)
  {
    return dispatchMatchedChat(chat);
  } // onChat

  // --------------------------------------------------------------------------
  /**
   * Handlers for chats that match specific regexps.
   */
  protected LinkedHashMap<Pattern, IMatchedChatHandler> _handlers = new LinkedHashMap<Pattern, IMatchedChatHandler>();
} // class Analysis
