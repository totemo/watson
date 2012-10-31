package watson.cli;

import net.minecraft.src.CommandBase;
import net.minecraft.src.ICommandSender;
import net.minecraft.src.SyntaxErrorException;
import watson.Controller;
import watson.DisplaySettings;

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
    DisplaySettings display = Controller.instance.getDisplaySettings();
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
    }

    // "display" command.
    if (args.length >= 1 && args[0].equals("display"))
    {
      if (args.length == 1)
      {
        // Toggle display.
        display.setDisplayed(!display.isDisplayed());
        return;
      }
      else if (args.length == 2)
      {
        if (args[1].equals("on"))
        {
          display.setDisplayed(true);
          return;
        }
        else if (args[1].equals("off"))
        {
          display.setDisplayed(false);
          return;
        }
      }
      // Other args.length and args[1] values will throw.
    } // display

    // "outline" command.
    if (args.length >= 1 && args[0].equals("outline"))
    {
      if (args.length == 1)
      {
        // Toggle display.
        display.setOutlineShown(!display.isOutlineShown());
        return;
      }
      else if (args.length == 2)
      {
        if (args[1].equals("on"))
        {
          display.setOutlineShown(true);
          return;
        }
        else if (args[1].equals("off"))
        {
          display.setOutlineShown(false);
          return;
        }
      }
      // Other args.length and args[1] values will throw.
    } // outline

    // "/w anno" command.
    if (args.length >= 1 && args[0].equals("anno"))
    {
      if (args.length == 1)
      {
        // Toggle display.
        display.setAnnotationsShown(!display.areAnnotationsShown());
        return;
      }
      else if (args.length == 2)
      {
        if (args[1].equals("on"))
        {
          display.setAnnotationsShown(true);
          return;
        }
        else if (args[1].equals("off"))
        {
          display.setAnnotationsShown(false);
          return;
        }
      }
      // Other args.length and args[1] values will throw.
    } // anno

    // "vector" command.
    if (args.length >= 1 && args[0].equals("vector"))
    {
      if (args.length == 1)
      {
        // Toggle vector drawing.
        display.setVectorsShown(!display.areVectorsShown());
        return;
      }
      else if (args.length == 2)
      {
        if (args[1].equals("on"))
        {
          display.setVectorsShown(true);
          return;
        }
        else if (args[1].equals("off"))
        {
          display.setVectorsShown(false);
          return;
        }
        else if (args[1].equals("creations"))
        {
          display.setLinkedCreations(!display.isLinkedCreations());
          return;
        }
        else if (args[1].equals("destructions"))
        {
          display.setLinkedDestructions(!display.isLinkedDestructions());
          return;
        }
      }
      else if (args.length == 3)
      {
        if (args[1].equals("creations"))
        {
          if (args[2].equals("on"))
          {
            display.setLinkedCreations(true);
            return;
          }
          else if (args[2].equals("off"))
          {
            display.setLinkedCreations(false);
            return;
          }
        }
        else if (args[1].equals("destructions"))
        {
          if (args[2].equals("on"))
          {
            display.setLinkedDestructions(true);
            return;
          }
          else if (args[2].equals("off"))
          {
            display.setLinkedDestructions(false);
            return;
          }
        }
        else if (args[1].equals("length"))
        {
          display.setMinVectorLength(Float.parseFloat(args[2]));
          return;
        }
      }
      // Other args.length and args[1] values will throw.
    } // vector

    // File commands.
    if (args.length >= 2 && args[0].equals("file"))
    {
      if (args[1].equals("list"))
      {
        if (args.length == 2)
        {
          Controller.instance.listBlockEditFiles(null);
          return;
        }
        else if (args.length == 3)
        {
          Controller.instance.listBlockEditFiles(args[2]);
          return;
        }
      }
      else if (args[1].equals("load") && args.length == 3)
      {
        // args[2] is either a full file name or a player name.
        Controller.instance.loadBlockEditFile(args[2]);
        return;
      }
      else if (args[1].equals("save"))
      {
        if (args.length == 2)
        {
          Controller.instance.saveBlockEditFile(null);
          return;
        }
        else if (args.length == 3)
        {
          Controller.instance.saveBlockEditFile(args[2]);
          return;
        }
      }
    } // file

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
    sender.sendChatToPlayer("  /w display [on|off]");
    sender.sendChatToPlayer("  /w outline [on|off]");
    sender.sendChatToPlayer("  /w anno [on|off]");
    sender.sendChatToPlayer("  /w vector [on|off]");
    sender.sendChatToPlayer("  /w vector creations [on|off]");
    sender.sendChatToPlayer("  /w vector destructions [on|off]");
    sender.sendChatToPlayer("  /w vector length <decimal>");
    sender.sendChatToPlayer("  /w clear");
    sender.sendChatToPlayer("  /w pre");
    sender.sendChatToPlayer("  /w file list [<playername>]");
    sender.sendChatToPlayer("  /w file load <filename>|<playername>");
    sender.sendChatToPlayer("  /w file save [<filename>]");
  }
} // class WatsonCommand
