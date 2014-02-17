package watson.cli;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;

// ----------------------------------------------------------------------------
/**
 * Abstract base for Watson commands.
 * 
 * This class simply extends the Minecraft CommandBase class with some helper
 * messages to provide consistent colouring of messages.
 */
public abstract class WatsonCommandBase extends CommandBase
{
  // --------------------------------------------------------------------------
  /**
   * Return the usage message for the command.
   * 
   * @return the usage message for the command.
   */
  public String getCommandUsage(ICommandSender icommandsender)
  {
    return "";
  }

  // --------------------------------------------------------------------------
  /**
   * Override the default permissions mechanism so that all Watson commands are
   * usable client-side.
   * 
   * @param sender the command sender.
   * @returns true if the given command sender is allowed to use this command.
   */
  public boolean canCommandSenderUseCommand(ICommandSender sender)
  {
    return true;
  }

  // --------------------------------------------------------------------------
  /**
   * Show successful command output.
   * 
   * @param message the output to show.
   */
  public void localOutput(ICommandSender sender, String message)
  {
    sendColouredText(sender, EnumChatFormatting.AQUA, message);
  }

  // --------------------------------------------------------------------------
  /**
   * Show an unsuccessful command's error output.
   * 
   * @param message the output to show.
   */
  public void localError(ICommandSender sender, String message)
  {
    sendColouredText(sender, EnumChatFormatting.DARK_RED, message);
  }

  // --------------------------------------------------------------------------
  /**
   * Send a message to the specified command sender in the specified colour.
   * 
   * @param sender the command sender who is the message recipient.
   * @param colour the colour.
   * @param message the text.
   */
  public void sendColouredText(ICommandSender sender, EnumChatFormatting colour, String message)
  {
    ChatComponentText chat = new ChatComponentText(message);
    ChatStyle style = new ChatStyle();
    style.setColor(colour);
    chat.setChatStyle(style);
    sender.addChatMessage(chat);
  }

  // --------------------------------------------------------------------------
  /**
   * Return the concatenation of arguments from the args[] array, for the
   * indices between begin (inclusive) and end (exclusive) and using the
   * specified separator between arguments (but not leading or trailing).
   * 
   * @param args the array of command argument strings.
   * @param begin the inclusive start index of the range.
   * @param end the inclusive start index of the range.
   * @param separator the separator to be inserted between consecutive arguments
   *          in the range.
   */
  public static String concatArgs(String[] args, int begin, int end,
                                  String separator)
  {
    StringBuilder result = new StringBuilder();
    for (int i = begin; i < end; ++i)
    {
      result.append(args[i]);
      if (i < end - 1)
      {
        result.append(separator);
      }
    }
    return result.toString();
  } // concatArgs
} // class WatsonCommandBase
