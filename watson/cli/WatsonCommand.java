package watson.cli;

import net.minecraft.src.CommandBase;
import net.minecraft.src.ICommandSender;
import net.minecraft.src.SyntaxErrorException;
import watson.Controller;

// --------------------------------------------------------------------------
/**
 * An ICommand implementation for the Watson /w command set.
 */
public class WatsonCommand extends CommandBase
{
  // --------------------------------------------------------------------------
  /**
   * @see net.minecraft.src.ICommand#getCommandName()
   */
  @Override
  public String getCommandName()
  {
    return "w";
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
      else if (args[0].equals("clear"))
      {
        Controller.instance.clearBlockEditSet();
        return;
      }
      else if (args[0].equals("pre"))
      {
        Controller.instance.queryPreviousEdits();
        return;
      }
      else if (args[0].equals("display"))
      {
        // Toggle display.
        Controller.instance.setDisplayed(!Controller.instance.isDisplayed());
        return;
      }
    }
    else if (args.length == 2)
    {
      if (args[0].equals("display"))
      {
        if (args[1].equals("on"))
        {
          Controller.instance.setDisplayed(true);
          return;
        }
        else if (args[1].equals("off"))
        {
          Controller.instance.setDisplayed(false);
          return;
        }
        // Other args[1] values will throw.
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
    sender.sendChatToPlayer("  /w help");
    sender.sendChatToPlayer("  /w display on|off");
    sender.sendChatToPlayer("  /w clear");
    sender.sendChatToPlayer("  /w pre");
  }
} // class WatsonCommand
