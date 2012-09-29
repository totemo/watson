package watson.cli;

import net.minecraft.src.CommandBase;
import net.minecraft.src.ICommandSender;
import net.minecraft.src.SyntaxErrorException;
import watson.chat.ChatProcessor;

// --------------------------------------------------------------------------
/**
 * An ICommand implementation for the /tag commands.
 */
public class TagCommand extends CommandBase
{
  // --------------------------------------------------------------------------
  /**
   * @see net.minecraft.src.ICommand#getCommandName()
   */
  @Override
  public String getCommandName()
  {
    return "tag";
  }

  // --------------------------------------------------------------------------
  /**
   * @see net.minecraft.src.ICommand#processCommand(net.minecraft.src.ICommandSender,
   *      java.lang.String[])
   */
  @Override
  public void processCommand(ICommandSender sender, String[] args)
  {
    if (args.length == 0)
    {
      help(sender);
      return;
    }
    else if (args.length == 1)
    {
      if (args[0].equals("help"))
      {
        help(sender);
        return;
      }
      else if (args[0].equals("list"))
      {
        ChatProcessor.getInstance().listHiddenTags();
        return;
      }
    }
    else if (args.length == 2)
    {
      if (args[0].equals("hide") || args[0].equals("show"))
      {
        ChatProcessor.getInstance().setChatTagVisible(args[1],
          args[0].equals("show"));
        return;
      }
    }

    throw new SyntaxErrorException("commands.generic.syntax", new Object[0]);
  } // processCommand

  // --------------------------------------------------------------------------
  /**
   * Show a help message.
   */
  public void help(ICommandSender sender)
  {
    sender.sendChatToPlayer("Usage:");
    sender.sendChatToPlayer("  /tag help");
    sender.sendChatToPlayer("  /tag list");
    sender.sendChatToPlayer("  /tag hide name");
    sender.sendChatToPlayer("  /tag show name");
  }
} // class TagCommand