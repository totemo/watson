package watson.cli;

import java.util.regex.Pattern;

import net.minecraft.src.ICommandSender;
import net.minecraft.src.SyntaxErrorException;
import net.minecraft.src.mod_ClientCommands;
import watson.Configuration;
import watson.Controller;
import watson.DisplaySettings;
import watson.analysis.ServerTime;
import watson.db.Filters;
import watson.db.OreDB;
import ClientCommands.ClientCommandManager;

// --------------------------------------------------------------------------
/**
 * An ICommand implementation for the Watson /w command set.
 */
public class WatsonCommand extends WatsonCommandBase
{
  // --------------------------------------------------------------------------
  /**
   * @see net.minecraft.src.ICommand#getCommandName()
   */
  @Override
  public String getCommandName()
  {
    return Configuration.instance.getWatsonPrefix();
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
      else if (args[0].equals("ratio"))
      {
        Controller.instance.getBlockEditSet().getOreDB().showRatios();
        return;
      }
      else if (args[0].equals("servertime"))
      {
        ServerTime.instance.queryServerTime(true);
        return;
      }
    }

    // "/w ore [<page>]"
    if (args.length >= 1 && args[0].equals("ore"))
    {
      if (args.length == 1)
      {
        Controller.instance.getBlockEditSet().getOreDB().listDeposits(1);
        return;
      }
      else if (args.length == 2)
      {
        boolean validPage = false;
        try
        {
          int page = Integer.parseInt(args[1]);
          if (page > 0)
          {
            validPage = true;
            Controller.instance.getBlockEditSet().getOreDB().listDeposits(page);
          }
        }
        catch (NumberFormatException ex)
        {
          // Handled by validPage check.
        }
        if (!validPage)
        {
          Controller.instance.localError("The page number should be greater than zero.");
        }
        return;
      }
    } // "/w ore"

    // "/w pre [<count>]"
    if (args.length >= 1 && args[0].equals("pre"))
    {
      if (args.length == 1)
      {
        Controller.instance.queryPreEdits(Configuration.instance.getPreCount());
        return;
      }
      else if (args.length == 2)
      {
        boolean validCount = false;
        try
        {
          int count = Integer.parseInt(args[1]);
          if (count > 0)
          {
            validCount = true;
            Controller.instance.queryPreEdits(count);
          }
        }
        catch (NumberFormatException ex)
        {
          // Handled by validCount test.
        }

        if (!validCount)
        {
          Controller.instance.localError("The count parameter should be a positive number of edits to fetch.");
        }
        return;
      }
    }

    // "/w post [<count>]"
    if (args.length >= 1 && args[0].equals("post"))
    {
      if (args.length == 1)
      {
        Controller.instance.queryPostEdits(Configuration.instance.getPostCount());
        return;
      }
      else if (args.length == 2)
      {
        boolean validCount = false;
        try
        {
          int count = Integer.parseInt(args[1]);
          if (count > 0)
          {
            validCount = true;
            Controller.instance.queryPostEdits(count);
          }
        }
        catch (NumberFormatException ex)
        {
          // Handled by validCount test.
        }

        if (!validCount)
        {
          Controller.instance.localError("The count parameter should be a positive number of edits to fetch.");
        }
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
      if (handleVectorCommand(sender, args))
      {
        return;
      }
    }

    // "/w label" command.
    if (args.length >= 1 && args[0].equals("label"))
    {
      if (args.length == 1)
      {
        // Toggle display.
        display.setLabelsShown(!display.areLabelsShown());
        return;
      }
      else if (args.length == 2)
      {
        if (args[1].equals("on"))
        {
          display.setLabelsShown(true);
          return;
        }
        else if (args[1].equals("off"))
        {
          display.setLabelsShown(false);
          return;
        }
      }
      // Other args.length and args[1] values will throw.
    } // "/w label"

    // Ore teleport commands: /w tp [next|prev|<number>]
    if (args.length >= 1 && args[0].equals("tp"))
    {
      OreDB oreDB = Controller.instance.getBlockEditSet().getOreDB();
      if (args.length == 1)
      {
        oreDB.tpNext();
        return;
      }
      else if (args.length == 2)
      {
        if (args[1].equals("next"))
        {
          oreDB.tpNext();
          return;
        }
        else if (args[1].equals("prev"))
        {
          oreDB.tpPrev();
          return;
        }
        else
        {
          try
          {
            oreDB.tpIndex(Integer.parseInt(args[1]));
          }
          catch (NumberFormatException ex)
          {
            Controller.instance.localError("The tp argument should be next, prev or an integer.");
          }
          return;
        }
      }
    } // /w tp

    // "/w edits" command.
    if (args[0].equals("edits"))
    {
      if (args.length == 1 || (args.length == 2 && args[1].equals("list")))
      {
        Controller.instance.getBlockEditSet().listEdits();
        return;
      }
      else if (args.length >= 3)
      {
        if (args[1].equals("hide") || args[1].equals("show"))
        {
          for (int i = 2; i < args.length; ++i)
          {
            Controller.instance.getBlockEditSet().setEditVisibility(args[i],
              args[1].equals("show"));
          }
          return;
        }
        else if (args[1].equals("remove"))
        {
          for (int i = 2; i < args.length; ++i)
          {
            Controller.instance.getBlockEditSet().removeEdits(args[i]);
          }
          return;
        }
      }
    } // "/w edits"

    // "/w filter" command.
    if (args[0].equals("filter"))
    {
      Filters filters = Controller.instance.getFilters();
      if (args.length == 1 || (args.length == 2 && args[1].equals("list")))
      {
        filters.list();
        return;
      }
      else if (args.length == 2 && args[1].equals("clear"))
      {
        filters.clear();
        return;
      }
      else if (args.length >= 3)
      {
        if (args[1].equals("add"))
        {
          for (int i = 2; i < args.length; ++i)
          {
            filters.addPlayer(args[i]);
          }
          return;
        }
        else if (args[1].equals("remove"))
        {
          for (int i = 2; i < args.length; ++i)
          {
            filters.removePlayer(args[i]);
          }
          return;
        }
      }
    } // "/w filter"

    // File commands.
    if (args.length >= 2 && args[0].equals("file"))
    {
      if (args[1].equals("list"))
      {
        if (args.length == 2)
        {
          Controller.instance.listBlockEditFiles("*", 1);
          return;
        }
        else if (args.length == 3)
        {
          Controller.instance.listBlockEditFiles(args[2], 1);
          return;
        }
        else if (args.length == 4)
        {
          boolean validPage = false;
          try
          {
            int page = Integer.parseInt(args[3]);
            if (page > 0)
            {
              validPage = true;
              Controller.instance.listBlockEditFiles(args[2], page);
            }
          }
          catch (NumberFormatException ex)
          {
            // Handled by validPage check.
          }
          if (!validPage)
          {
            Controller.instance.localError("The page number should be greater than zero.");
          }
          return;
        }
      }
      else if (args[1].equals("delete") && args.length == 3)
      {
        Controller.instance.deleteBlockEditFiles(args[2]);
        return;
      }
      else if (args[1].equals("expire") && args.length == 3)
      {
        Controller.instance.expireBlockEditFiles(args[2]);
        return;
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

    // "/w config" command.
    if (args.length >= 2 && args[0].equals("config"))
    {
      if (handleConfigCommand(sender, args))
      {
        return;
      }
    } // config

    throw new SyntaxErrorException("commands.generic.syntax", new Object[0]);
  } // processCommand

  // --------------------------------------------------------------------------
  /**
   * Handle the various /w vector subcommands.
   * 
   * @return true if the command was processed successfully.
   */
  protected boolean handleVectorCommand(ICommandSender sender, String[] args)
  {
    DisplaySettings display = Controller.instance.getDisplaySettings();
    if (args.length == 1)
    {
      // Toggle vector drawing.
      display.setVectorsShown(!display.areVectorsShown());
      return true;
    }
    else if (args.length == 2)
    {
      if (args[1].equals("on"))
      {
        display.setVectorsShown(true);
        return true;
      }
      else if (args[1].equals("off"))
      {
        display.setVectorsShown(false);
        return true;
      }
      else if (args[1].equals("creations"))
      {
        display.setLinkedCreations(!display.isLinkedCreations());
        return true;
      }
      else if (args[1].equals("destructions"))
      {
        display.setLinkedDestructions(!display.isLinkedDestructions());
        return true;
      }
    }
    else if (args.length == 3)
    {
      if (args[1].equals("creations"))
      {
        if (args[2].equals("on"))
        {
          display.setLinkedCreations(true);
          return true;
        }
        else if (args[2].equals("off"))
        {
          display.setLinkedCreations(false);
          return true;
        }
      }
      else if (args[1].equals("destructions"))
      {
        if (args[2].equals("on"))
        {
          display.setLinkedDestructions(true);
          return true;
        }
        else if (args[2].equals("off"))
        {
          display.setLinkedDestructions(false);
          return true;
        }
      }
      else if (args[1].equals("length"))
      {
        display.setMinVectorLength(Float.parseFloat(args[2]));
        return true;
      }
    }
    return false;
  } // handleVectorCommand

  // --------------------------------------------------------------------------
  /**
   * Handle the various /w config subcommands.
   * 
   * @return true if the command was processed successfully.
   */
  protected boolean handleConfigCommand(ICommandSender sender, String[] args)
  {
    // Enable or disable the mod as a whole.
    if (args[1].equals("watson"))
    {
      if (args.length == 2)
      {
        Configuration.instance.setEnabled(!Configuration.instance.isEnabled());
        return true;
      }
      else if (args.length == 3)
      {
        if (args[2].equals("on"))
        {
          Configuration.instance.setEnabled(true);
          return true;
        }
        else if (args[2].equals("off"))
        {
          Configuration.instance.setEnabled(false);
          return true;
        }
      }
    } // /w config watson

    // Enable or disable debug logging.
    if (args[1].equals("debug"))
    {
      if (args.length == 2)
      {
        Configuration.instance.setDebug(!Configuration.instance.isDebug());
        return true;
      }
      else if (args.length == 3)
      {
        if (args[2].equals("on"))
        {
          Configuration.instance.setDebug(true);
          return true;
        }
        else if (args[2].equals("off"))
        {
          Configuration.instance.setDebug(false);
          return true;
        }
      }
    } // /w config debug

    // Enable or disable automatic "/lb coords" paging.
    if (args[1].equals("auto_page"))
    {
      if (args.length == 2)
      {
        Configuration.instance.setAutoPage(!Configuration.instance.isAutoPage());
        return true;
      }
      else if (args.length == 3)
      {
        if (args[2].equals("on"))
        {
          Configuration.instance.setAutoPage(true);
          return true;
        }
        else if (args[2].equals("off"))
        {
          Configuration.instance.setAutoPage(false);
          return true;
        }
      }
    } // /w config auto_page

    // Set minimum time separation between automatic "/region info"s.
    if (args[1].equals("region_info_timeout"))
    {
      if (args.length == 3)
      {
        try
        {
          double seconds = Math.abs(Double.parseDouble(args[2]));
          Configuration.instance.setRegionInfoTimeoutSeconds(seconds);
          return true;
        }
        catch (NumberFormatException ex)
        {
          Controller.instance.localError("The timeout should be a decimal number of seconds.");
          return true;
        }
      }
    } // /w config region_info_timeout

    // Set the text billboard background colour.
    if (args[1].equals("billboard_background"))
    {
      if (args.length == 3)
      {
        try
        {
          // Need to truncate 64-bit values, otherwise numbers >7FFFFFFF will
          // throw.
          int argb = (int) Long.parseLong(args[2], 16);
          Configuration.instance.setBillboardBackground(argb);
          return true;
        }
        catch (NumberFormatException ex)
        {
          Controller.instance.localError("The colour should be a 32-bit hexadecimal ARGB value.");
          return true;
        }
      }
    } // /w config billboard_background

    // Set the text billboard foreground colour.
    if (args[1].equals("billboard_foreground"))
    {
      if (args.length == 3)
      {
        try
        {
          // Need to truncate 64-bit values, otherwise numbers >7FFFFFFF will
          // throw.
          int argb = (int) Long.parseLong(args[2], 16);
          Configuration.instance.setBillboardForeground(argb);
          return true;
        }
        catch (NumberFormatException ex)
        {
          Controller.instance.localError("The colour should be a 32-bit hexadecimal ARGB value.");
          return true;
        }
      }
    } // /w config billboard_foreground

    // Enable or disable forced grouping of ores in creative mode.
    if (args[1].equals("group_ores_in_creative"))
    {
      if (args.length == 2)
      {
        Configuration.instance.setGroupingOresInCreative(!Configuration.instance.isGroupingOresInCreative());
        return true;
      }
      else if (args.length == 3)
      {
        if (args[2].equals("on"))
        {
          Configuration.instance.setGroupingOresInCreative(true);
          return true;
        }
        else if (args[2].equals("off"))
        {
          Configuration.instance.setGroupingOresInCreative(false);
          return true;
        }
      }
    } // /w config group_ores_in_creative

    if (args[1].equals("teleport_command") && args.length >= 3)
    {
      String format = concatArgs(args, 2, args.length, " ");
      Configuration.instance.setTeleportCommand(format);
      return true;
    }

    // Set minimum time separation between programmatically generated chat
    // messages sent to the server
    if (args[1].equals("chat_timeout"))
    {
      if (args.length == 3)
      {
        try
        {
          double seconds = Math.abs(Double.parseDouble(args[2]));
          Configuration.instance.setChatTimeoutSeconds(seconds);
          return true;
        }
        catch (NumberFormatException ex)
        {
          Controller.instance.localError("The timeout should be a decimal number of seconds.");
          return true;
        }
      }
    } // /w config chat_timeout

    // Set the maximum number of pages of "/lb coords" results automatically
    // paged through.
    if (args[1].equals("max_auto_pages"))
    {
      if (args.length == 3)
      {
        boolean validCount = false;
        try
        {
          int count = Integer.parseInt(args[2]);
          if (count > 0)
          {
            validCount = true;
            Configuration.instance.setMaxAutoPages(count);
          }
        }
        catch (NumberFormatException ex)
        {
          // Handled by validCount flag.
        }

        if (!validCount)
        {
          Controller.instance.localError("The minimum number of pages should be at least 1.");
        }
        return true;
      } // if
    } // /w config pre_count

    // Set the default number of edits to query when no count parameter is
    // specified with "/w pre".
    if (args[1].equals("pre_count"))
    {
      if (args.length == 3)
      {
        boolean validCount = false;
        try
        {
          int count = Integer.parseInt(args[2]);
          if (count > 0)
          {
            validCount = true;
            Configuration.instance.setPreCount(count);
          }
        }
        catch (NumberFormatException ex)
        {
          // Handled by validCount flag.
        }

        if (!validCount)
        {
          Controller.instance.localError("The count should be a positive integer.");
        }
        return true;
      } // if
    } // /w config pre_count

    // Set the default number of edits to query when no count parameter is
    // specified with "/w post".
    if (args[1].equals("post_count"))
    {
      if (args.length == 3)
      {
        boolean validCount = false;
        try
        {
          int count = Integer.parseInt(args[2]);
          if (count > 0)
          {
            validCount = true;
            Configuration.instance.setPostCount(count);
          }
        }
        catch (NumberFormatException ex)
        {
          // Handled by validCount flag.
        }

        if (!validCount)
        {
          Controller.instance.localError("The count should be a positive integer.");
        }
        return true;
      } // if
    } // /w config post_count

    // Set the prefix for Watson commands.
    if (args[1].equals("watson_prefix") && args.length == 3)
    {
      String newPrefix = args[2];
      if (!PREFIX_PATTERN.matcher(newPrefix).matches())
      {
        Controller.instance.localError("The command prefix can only contain letters, digits and underscores.");
      }
      else
      {
        // De-register, set the prefix and re-register this command.
        ClientCommandManager ccm = mod_ClientCommands.getInstance().getClientCommandManager();
        try
        {
          ccm.unregisterCommand(this);
        }
        catch (Exception ex)
        {
          // Future proof the inevitable fuck up when a conflicting version of
          // mod_ClientCommands is released without the unregisterCommand
          // method.
        }
        Configuration.instance.setWatsonPrefix(newPrefix);
        ccm.registerCommand(this);
      }
      return true;
    } // /w config watson_prefix <prefix>

    // Enable or disable per-player screenshot subdirectories.
    if (args[1].equals("ss_player_directory"))
    {
      if (args.length == 2)
      {
        Configuration.instance.setSsPlayerDirectory(!Configuration.instance.isSsPlayerDirectory());
        return true;
      }
      else if (args.length == 3)
      {
        if (args[2].equals("on"))
        {
          Configuration.instance.setSsPlayerDirectory(true);
          return true;
        }
        else if (args[2].equals("off"))
        {
          Configuration.instance.setSsPlayerDirectory(false);
          return true;
        }
      }
    } // /w config ss_player_directory

    // Enable or disable per-player screenshot suffixes.
    if (args[1].equals("ss_player_suffix"))
    {
      if (args.length == 2)
      {
        Configuration.instance.setSsPlayerSuffix(!Configuration.instance.isSsPlayerSuffix());
        return true;
      }
      else if (args.length == 3)
      {
        if (args[2].equals("on"))
        {
          Configuration.instance.setSsPlayerSuffix(true);
          return true;
        }
        else if (args[2].equals("off"))
        {
          Configuration.instance.setSsPlayerSuffix(false);
          return true;
        }
      }
    } // /w config ss_player_directory

    // Set the anonymous screenshot subdirectory format specifier.
    if (args[1].equals("ss_date_directory"))
    {
      if (args.length == 2)
      {
        Configuration.instance.setSsDateDirectory("");
        return true;
      }
      else if (args.length >= 3)
      {
        String format = concatArgs(args, 2, args.length, " ");
        Configuration.instance.setSsDateDirectory(format);
        return true;
      }
    } // /w config ss_date_directory

    return false;
  } // handleConfigCommand

  // --------------------------------------------------------------------------
  /**
   * Show a help message.
   */
  public void help(ICommandSender sender)
  {
    String w = Configuration.instance.getWatsonPrefix();
    localOutput(sender, "Usage:");
    localOutput(sender, "  /" + w + " help");
    localOutput(sender, "  /" + w + " display [on|off]");
    localOutput(sender, "  /" + w + " outline [on|off]");
    localOutput(sender, "  /" + w + " anno [on|off]");
    localOutput(sender, "  /" + w + " vector [on|off]");
    localOutput(sender, "  /" + w + " vector (creations|destructions) [on|off]");
    localOutput(sender, "  /" + w + " vector length <decimal>");
    localOutput(sender, "  /" + w + " label [on|off]");
    localOutput(sender, "  /" + w + " clear");
    localOutput(sender, "  /" + w + " pre [<count>]");
    localOutput(sender, "  /" + w + " post [<count>]");
    localOutput(sender, "  /" + w + " ore [<page>]");
    localOutput(sender, "  /" + w + " ratio");
    localOutput(sender, "  /" + w + " tp [next|prev|<number>]");
    localOutput(sender, "  /" + w + " edits [list]");
    localOutput(sender, "  /" + w + " edits (hide|show|remove) <player> ...");
    localOutput(sender, "  /" + w + " filter [list|clear]");
    localOutput(sender, "  /" + w + " filter (add|remove) <player> ...");
    localOutput(sender, "  /" + w + " servertime");
    localOutput(sender, "  /" + w + " file list [*|<playername>] [<page>]");
    localOutput(sender, "  /" + w + " file delete *|<filename>|<playername>");
    localOutput(sender, "  /" + w + " file expire <YYYY-MM-DD>");
    localOutput(sender, "  /" + w + " file load <filename>|<playername>");
    localOutput(sender, "  /" + w + " file save [<filename>]");
    localOutput(sender, "  /" + w + " config <name> [<value>]");
    localOutput(sender, "  /hl help");
    localOutput(sender, "  /anno help");
    localOutput(sender, "  /tag help");
    localOutput(sender, "Documentation: http://github.com/totemo/watson");
    if (!Configuration.instance.isEnabled())
    {
      localOutput(sender, "Watson is currently disabled.");
      localOutput(sender, "To re-enable, use: /" + w + " config watson on");
    }
  } // help

  // --------------------------------------------------------------------------
  /**
   * Allowable patterns of command prefixes (setCommandPrefix()).
   */
  protected static final Pattern PREFIX_PATTERN = Pattern.compile("\\w+");
} // class WatsonCommand
