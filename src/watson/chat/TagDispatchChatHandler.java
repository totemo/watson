package watson.chat;

import java.util.HashMap;

// --------------------------------------------------------------------------
/**
 * An IChatHandler that dispatches to other IChatHandler implementations based
 * on the tag of the {@link ChatCategory} assigned to a {@link ChatLine}.
 * 
 * This class only supports a single {@link IChatHandler} for each distinct tag.
 * 
 * TODO: Add support for multiple handlers for each tag, because there may be
 * contention for hooking the header lines in the future.
 */
public class TagDispatchChatHandler implements IChatHandler
{
  // --------------------------------------------------------------------------
  /**
   * Set the handler for {@link ChatLine}s marked with the {@link ChatCategory}
   * that has the specified tag.
   * 
   * @param tag the tag attribute of the {@link ChatCategory} of lines to be
   *          sent to the handler.
   * @param handler the {@link IChatHandler} implementation that handles the
   *          lines.
   */
  public void setChatHandler(String tag, IChatHandler handler)
  {
    _handlers.put(tag, handler);
  }

  // --------------------------------------------------------------------------
  /**
   * Implements inherited method.
   */
  @Override
  public void classify(ChatLine line)
  {
    IChatHandler handler = getHandler(line);
    if (handler != null)
    {
      handler.classify(line);
    }
  }

  // --------------------------------------------------------------------------
  /**
   * Implements inherited method.
   */
  @Override
  public void revise(ChatLine oldLine, ChatLine newLine)
  {
    IChatHandler handler = getHandler(newLine);
    if (handler != null)
    {
      handler.revise(oldLine, newLine);
    }
  }

  // --------------------------------------------------------------------------
  /**
   * Return the {@link IChatHandler} to dispatched to based on the assigned
   * {@link ChatCategory} of the {@link ChatLine}.
   */
  private IChatHandler getHandler(ChatLine line)
  {
    return _handlers.get(line.getCategory().getTag());
  }

  // --------------------------------------------------------------------------
  /**
   * A map from the ChatCategory tag string to the IChatHandler to invoke.
   */
  HashMap<String, IChatHandler> _handlers = new HashMap<String, IChatHandler>();
} // class TagDispatchChatHandler