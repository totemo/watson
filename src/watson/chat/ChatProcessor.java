package watson.chat;

import java.util.ArrayList;

import net.minecraft.util.IChatComponent;
import watson.Configuration;
import watson.analysis.LbToolBlockAnalysis;
import watson.analysis.CoreProtectAnalysis;
import watson.analysis.LbCoordsAnalysis;
import watson.analysis.ModModeAnalysis;
import watson.analysis.PrismAnalysis;
import watson.analysis.RatioAnalysis;
import watson.analysis.RegionInfoAnalysis;
import watson.analysis.ServerTime;
import watson.analysis.TeleportAnalysis;

// ----------------------------------------------------------------------------
/**
 * A singleton that handles received chats by passing them through a set of
 * {@link IChatHandler} instances to decide what to filter out of chat, and
 * process various interesting chat messages as a side effect.
 * 
 * Normally, NetClientHandler.handleChat() calls GuiNewChat.printChatMessage()
 * for every chat packet received. But Watson uses lots of /lb coords queries
 * that we would like to exclude from chat, so instead, IChatHandler
 * implementations process the chats and decide which should reach Minecraft's
 * normal handling.
 */
public class ChatProcessor
{
  /**
   * The single instance of this class.
   */
  public static ChatProcessor instance = new ChatProcessor();

  // --------------------------------------------------------------------------
  /**
   * Add the handler to the list of those that are notified of a received chat.
   * 
   * @param handler the IChatHandler whose onChat() method is called.
   */
  public void addChatHandler(IChatHandler handler)
  {
    _handlers.add(handler);
  }

  // --------------------------------------------------------------------------
  /**
   * Process the chat
   * 
   * @param chat the chat message.
   * @return true if the chat should be echoed in the client chat GUI; false if
   *         it should be filtered out.
   */
  public boolean onChat(IChatComponent chat)
  {
    if (Configuration.instance.isEnabled())
    {
      boolean allow = true;
      for (IChatHandler handler : _handlers)
      {
        allow &= handler.onChat(chat);
      }
      return allow;
    }
    else
    {
      return true;
    }
  }

  // --------------------------------------------------------------------------
  /**
   * Private constructor to enforce single instance.
   */
  private ChatProcessor()
  {
    addChatHandler(new LbCoordsAnalysis());
    addChatHandler(new LbToolBlockAnalysis());
    addChatHandler(new TeleportAnalysis());
    addChatHandler(new RatioAnalysis());
    addChatHandler(ServerTime.instance);

    addChatHandler(new ModModeAnalysis());
    addChatHandler(new RegionInfoAnalysis());

    addChatHandler(new PrismAnalysis());
    addChatHandler(new CoreProtectAnalysis());
  }

  // --------------------------------------------------------------------------
  /**
   * Handlers notified of chat arriving at the client.
   */
  protected ArrayList<IChatHandler> _handlers = new ArrayList<IChatHandler>();
} // class ChatProcessor
