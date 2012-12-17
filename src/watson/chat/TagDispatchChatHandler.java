package watson.chat;

import java.util.HashMap;
import java.util.HashSet;

// --------------------------------------------------------------------------
/**
 * An IChatHandler that dispatches to other IChatHandler implementations based
 * on the tag of the {@link ChatCategory} assigned to a {@link ChatLine}.
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
  public void addChatHandler(String tag, IChatHandler handler)
  {
    HashSet<IChatHandler> handlerSet = _handlers.get(tag);
    if (handlerSet == null)
    {
      handlerSet = new HashSet<IChatHandler>();
      _handlers.put(tag, handlerSet);
    }
    handlerSet.add(handler);
  }

  // --------------------------------------------------------------------------
  /**
   * Implements inherited method.
   */
  @Override
  public void classify(ChatLine line)
  {
    HashSet<IChatHandler> handlerSet = getHandlerSet(line);
    if (handlerSet != null)
    {
      for (IChatHandler handler : handlerSet)
      {
        handler.classify(line);
      }
    }
  } // classify

  // --------------------------------------------------------------------------
  /**
   * Implements inherited method.
   */
  @Override
  public void revise(ChatLine oldLine, ChatLine newLine)
  {
    HashSet<IChatHandler> handlerSet = getHandlerSet(newLine);
    if (handlerSet != null)
    {
      for (IChatHandler handler : handlerSet)
      {
        handler.revise(oldLine, newLine);
      }
    }
  } // revise

  // --------------------------------------------------------------------------
  /**
   * Return the set of {@link IChatHandler}s to dispatch to based on the
   * assigned {@link ChatCategory} of the {@link ChatLine}.
   * 
   * @return the set of {@link IChatHandler}s to dispatch to based on the
   *         assigned {@link ChatCategory} of the {@link ChatLine}.
   */
  private HashSet<IChatHandler> getHandlerSet(ChatLine line)
  {
    return _handlers.get(line.getCategory().getTag());
  }

  // --------------------------------------------------------------------------
  /**
   * A map from the ChatCategory tag string to a set of {@link IChatHandler}s to
   * invoke.
   */
  HashMap<String, HashSet<IChatHandler>> _handlers = new HashMap<String, HashSet<IChatHandler>>();
} // class TagDispatchChatHandler