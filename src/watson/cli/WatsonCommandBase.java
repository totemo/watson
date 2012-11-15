package watson.cli;

import net.minecraft.src.CommandBase;
import net.minecraft.src.ICommandSender;
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
} // class WatsonCommandBase