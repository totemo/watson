package watson.chat;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;

// ----------------------------------------------------------------------------
/**
 * Interfaces to chat methods in vanilla Minecraft code with less-than-ideal
 * de-obfuscation.
 */
public class Chat
{
  // --------------------------------------------------------------------------
  /**
   * Send a chat message to the server.
   * 
   * @param message the text to display.
   */
  public static void serverChat(String message)
  {
    try
    {
      Minecraft mc = Minecraft.getMinecraft();
      mc.thePlayer.sendChatMessage(message);
    }
    catch (Exception ex)
    {
      // TODO: switch to Log class.
      // Log.exception(Level.SEVERE, "Sending chat to the server.", ex);
      ex.printStackTrace();
    }
  }

  // --------------------------------------------------------------------------
  /**
   * Display a chat message locally.
   * 
   * @param message the text to display.
   */
  public static void localChat(String message)
  {
    localChat(new ChatComponentText(message));
  }

  // --------------------------------------------------------------------------
  /**
   * Display a chat message locally.
   * 
   * @param colour the colour to format the text as.
   * @param message the text to display.
   */
  public static void localChat(EnumChatFormatting colour, String message)
  {
    ChatComponentText chat = new ChatComponentText(message);
    ChatStyle style = new ChatStyle();
    style.setColor(colour);
    chat.setChatStyle(style);
    localChat(chat);
  }

  // --------------------------------------------------------------------------
  /**
   * Display the chat locally.
   * 
   * @param chat the chat component.
   */
  public static void localChat(IChatComponent chat)
  {
    Minecraft mc = Minecraft.getMinecraft();
    if (mc.ingameGUI != null && mc.ingameGUI.getChatGUI() != null)
    {
      mc.ingameGUI.getChatGUI().func_146227_a(chat);
    }
  }

  // --------------------------------------------------------------------------
  /**
   * Display the output of Watson commands, locally in the client.
   * 
   * @param message the text to display.
   */
  public static void localOutput(String message)
  {
    localChat(EnumChatFormatting.AQUA, message);
  }

  // --------------------------------------------------------------------------
  /**
   * Display Watson error messages, locally in the client.
   * 
   * @param message the text to display.
   */
  public static void localError(String message)
  {
    localChat(EnumChatFormatting.DARK_RED, message);
  }

} // class Chat