package watson.cli;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import watson.Controller;

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
   * Show successful command output.
   * 
   * @param message the output to show.
   */
  public void localOutput(ICommandSender sender, String message)
  {
    sender.sendChatToPlayer(Controller.OUTPUT_COLOUR + message);
  }

  // --------------------------------------------------------------------------
  /**
   * Show an unsuccessful command's error output.
   * 
   * @param message the output to show.
   */
  public void localError(ICommandSender sender, String message)
  {
    sender.sendChatToPlayer(Controller.ERROR_COLOUR + message);
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
