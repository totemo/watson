package watson.cli;

import java.util.Arrays;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatMessageComponent;
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
   * With reference to my remarks at
   * https://github.com/MinecraftForge/MinecraftForge/issues/809, Forge doubles
   * up the final argument passed to ICommand implementations and tells lies
   * about how many arguments are in that array (increases the size by one).
   * 
   * This method fixes the args array when it is clear that the last arg is
   * repeated.
   * 
   * @param args the args array passed to ICommand.processCommand().
   * @return the args array that should have been passed in.
   */
  public String[] fixArgs(String[] args)
  {
    int lastIndex = args.length - 1;
    if (lastIndex >= 1 && args[lastIndex - 1] == args[lastIndex])
    {
      return Arrays.copyOfRange(args, 0, lastIndex);
    }
    else
    {
      return args;
    }
  }

  // --------------------------------------------------------------------------
  /**
   * Show successful command output.
   * 
   * @param message the output to show.
   */
  public void localOutput(ICommandSender sender, String message)
  {
    ChatMessageComponent chat = new ChatMessageComponent();
    chat.setColor(EnumChatFormatting.AQUA);
    chat.addText(message);
    sender.sendChatToPlayer(chat);
  }

  // --------------------------------------------------------------------------
  /**
   * Show an unsuccessful command's error output.
   * 
   * @param message the output to show.
   */
  public void localError(ICommandSender sender, String message)
  {
    ChatMessageComponent chat = new ChatMessageComponent();
    chat.setColor(EnumChatFormatting.DARK_RED);
    chat.addText(message);
    sender.sendChatToPlayer(chat);
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
