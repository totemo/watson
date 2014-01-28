package watson.chat;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;

import net.minecraft.util.IChatComponent;
import watson.Configuration;
import watson.analysis.CoalBlockAnalysis;
import watson.analysis.CoreProtectAnalysis;
import watson.analysis.LbCoordsAnalysis;
import watson.analysis.ModModeAnalysis;
import watson.analysis.PrismAnalysis;
import watson.analysis.RatioAnalysis;
import watson.analysis.RegionInfoAnalysis;
import watson.analysis.ServerTime;
import watson.analysis.TeleportAnalysis;
import watson.db.BlockEdit;
import watson.db.BlockEditSet;

// ----------------------------------------------------------------------------
/**
 * A Singleton that processes all incoming {@link net.minecraft.src.Packet3Chat}
 * chat packets and routes them through various Watson components and/or back
 * into the Minecraft GUI.
 * 
 * Normally, net.minecraft.src.NetClientHandler.handleChat() calls
 * GuiNewChat.printChatMessage() for every chat packet received. But Watson uses
 * lots of /lb coords queries that we would like to exclude from chat, so
 * instead, we categorise all incoming chats in this class and then only pass
 * some of them to the normal Minecraft handling.
 * 
 * Since a chat message tagged as "lb.coord" causes a new {@link BlockEdit} to
 * be added to the currently displayed {@link BlockEditSet}, if we happen to be
 * iterating that set to draw the blocks when the chat arrives, we will get a
 * ConcurrentModificationException, since the chat arrives asynchronously (the
 * network and game rendering threads are different). To avoid that, incoming
 * chats are simply added to a queue, and then later de-queued and processed
 * from the game thread.
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
   * Add the specified chat message to the queue.
   * 
   * @param chat the chat message.
   */
  public void addChatToQueue(IChatComponent chat)
  {
    if (Configuration.instance.isEnabled())
    {
      _chatQueue.add(chat);
    }
    else
    {
      // Watson is disabled. Use default Minecraft processing.
      Chat.localChat(chat);
    }
  } // addToChatQueue

  // --------------------------------------------------------------------------
  /**
   * Process each of the chat messages that was added to the queue by
   * addChatToQueue() in the order they arrived.
   */
  public void processChatQueue()
  {
    // Clear the queue of chat messages that have arrived from the network
    // thread. Do this even if Watson is disabled, to clear any chats from just
    // before it was disabled.
    for (;;)
    {
      IChatComponent chat = _chatQueue.poll();
      if (chat == null)
      {
        break;
      }

      boolean echo = true;
      for (IChatHandler handler : _handlers)
      {
        echo &= handler.onChat(chat);
      }

      if (echo)
      {
        Chat.localChat(chat);
      }
    }
  } // processChatQueue

  // --------------------------------------------------------------------------
  /**
   * Private constructor to enforce single instance.
   */
  private ChatProcessor()
  {
    addChatHandler(new LbCoordsAnalysis());
    addChatHandler(new CoalBlockAnalysis());
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
  protected ArrayList<IChatHandler>               _handlers  = new ArrayList<IChatHandler>();

  /**
   * A queue to pass incoming chat messages from the network thread to the game
   * thread. This is necessary to avoid a ConcurrentModificationException while
   * traversing the set of BlockEdit instances and rendering them.
   */
  protected ConcurrentLinkedQueue<IChatComponent> _chatQueue = new ConcurrentLinkedQueue<IChatComponent>();

} // class ChatProcessor
