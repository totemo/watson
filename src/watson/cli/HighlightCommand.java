package watson.cli;

import net.minecraft.src.CommandBase;
import net.minecraft.src.ICommandSender;
import net.minecraft.src.SyntaxErrorException;
import watson.ChatHighlighter;
import watson.Controller;

// --------------------------------------------------------------------------
/**
 * The Watson /hl command.
 */
public class HighlightCommand extends CommandBase
{
  // --------------------------------------------------------------------------
  /**
   * @see net.minecraft.src.ICommand#getCommandName()
   */
  @Override
  public String getCommandName()
  {
    return "hl";
  }

  // --------------------------------------------------------------------------
  /**
   * @see net.minecraft.src.ICommand#processCommand(net.minecraft.src.ICommandSender,
   *      java.lang.String[])
   */
  @Override
  public void processCommand(ICommandSender sender, String[] args)
  {
    ChatHighlighter highlighter = Controller.instance.getChatHighlighter();
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
        highlighter.listHighlights();
        return;
      }
    }
    else if (args.length == 2)
    {
      if (args[0].equals("remove"))
      {
        int index = parseIntWithMin(sender, args[1], 1);
        highlighter.removeHighlight(index);
        return;
      }
    }
    else if (args.length == 3)
    {
      if (args[0].equals("add"))
      {
        highlighter.addHighlight(args[1], args[2]);
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
    sender.sendChatToPlayer("  /hl help");
    sender.sendChatToPlayer("  /hl add colour pattern");
    sender.sendChatToPlayer("  /hl list");
    sender.sendChatToPlayer("  /hl remove number");
  }
} // class HighlightCommand