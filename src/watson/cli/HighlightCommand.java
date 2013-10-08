package watson.cli;

import net.minecraft.command.ICommandSender;
import watson.Controller;
import watson.chat.ChatHighlighter;

// --------------------------------------------------------------------------
/**
 * The Watson /hl command.
 */
public class HighlightCommand extends WatsonCommandBase
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
    args = fixArgs(args);
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
    else if (args.length >= 3)
    {
      if (args[0].equals("add") || args[0].equals("select"))
      {
        // Allow patterns to contain spaces, rather than requiring \s.
        StringBuilder pattern = new StringBuilder();
        for (int i = 2; i < args.length; ++i)
        {
          pattern.append(args[i]);
          if (i < args.length - 1)
          {
            pattern.append(' ');
          }
        }
        highlighter.addHighlight(args[1], pattern.toString(),
          args[0].equals("select"));
        return;
      }
    }

    localError(sender, "Invalid command syntax.");
  } // processCommand

  // --------------------------------------------------------------------------
  /**
   * Show a help message.
   */
  public void help(ICommandSender sender)
  {
    localOutput(sender, "Usage:");
    localOutput(sender, "  /hl help");
    localOutput(sender, "  /hl add <colour> <pattern>");
    localOutput(sender, "  /hl list");
    localOutput(sender, "  /hl remove <number>");
    localOutput(sender, "Documentation: http://github.com/totemo/watson");
  }
} // class HighlightCommand