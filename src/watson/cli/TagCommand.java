package watson.cli;

import net.minecraft.src.ICommandSender;
import net.minecraft.src.SyntaxErrorException;
import watson.chat.ChatProcessor;

// --------------------------------------------------------------------------
/**
 * An ICommand implementation for the /tag commands.
 */
public class TagCommand extends WatsonCommandBase
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
    localOutput(sender, "Usage:");
    localOutput(sender, "  /tag help");
    localOutput(sender, "  /tag list");
    localOutput(sender, "  /tag hide <name>");
    localOutput(sender, "  /tag show <name>");
    localOutput(sender, "Documentation: http://github.com/totemo/watson");
  }
} // class TagCommand